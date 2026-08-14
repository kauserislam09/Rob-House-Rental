package com.rob.houserental.backup;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDriveService {

    public static final String SHA1_FINGERPRINT = "5D:FE:77:F2:22:15:BD:4F:95:8F:55:17:AB:0A:7C:5D:9D:F7:56:5D";
    public static final String PACKAGE_NAME = "com.rob.houserental";

    private static final String APP_FOLDER_NAME = "Rob House Rental";
    private static final String BACKUPS_FOLDER_NAME = "Backups";
    public static final String BACKUP_ZIP_MIME_TYPE = "application/zip";

    private final Drive driveService;
    private final GoogleAccountCredential credential;

    public GoogleDriveService(Context context, String accountEmail) {
        credential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(),
                Collections.singletonList(DriveScopes.DRIVE_FILE)
        );
        if (accountEmail != null && !accountEmail.trim().isEmpty()) {
            credential.setSelectedAccountName(accountEmail.trim());
        }

        driveService = new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("Rob House Rental")
                .build();
    }

    public static Intent getAccountPickerIntent(String selectedEmail) {
        Account selectedAccount = (selectedEmail != null && !selectedEmail.isEmpty()) ? new Account(selectedEmail, "com.google") : null;
        return AccountPicker.newChooseAccountIntent(
                new AccountPicker.AccountChooserOptions.Builder()
                        .setSelectedAccount(selectedAccount)
                        .setAllowableAccountsTypes(Collections.singletonList("com.google"))
                        .setAlwaysShowAccountPicker(true)
                        .build()
        );
    }

    public String getOrCreateBackupsFolderId() throws IOException {
        // 1. Check or create "Rob House Rental" root folder
        String rootFolderId = findFolderId(APP_FOLDER_NAME, "root");
        if (rootFolderId == null) {
            rootFolderId = createFolder(APP_FOLDER_NAME, "root");
        }

        // 2. Check or create "Backups" subfolder
        String backupsFolderId = findFolderId(BACKUPS_FOLDER_NAME, rootFolderId);
        if (backupsFolderId == null) {
            backupsFolderId = createFolder(BACKUPS_FOLDER_NAME, rootFolderId);
        }

        return backupsFolderId;
    }

    private String findFolderId(String folderName, String parentId) throws IOException {
        String query = "mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "' and '" + parentId + "' in parents and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }
        return null;
    }

    private String createFolder(String folderName, String parentId) throws IOException {
        com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        if (!"root".equals(parentId)) {
            folderMetadata.setParents(Collections.singletonList(parentId));
        }

        com.google.api.services.drive.model.File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();
        return folder.getId();
    }

    public com.google.api.services.drive.model.File uploadAndVerifyBackupFile(
            File localFile,
            String displayName,
            BackupManifest manifest,
            String archiveChecksum
    ) throws IOException {
        if (localFile == null || !localFile.exists() || localFile.length() == 0) {
            throw new IOException("Local backup file is missing or empty.");
        }

        String parentFolderId = getOrCreateBackupsFolderId();

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(displayName);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));

        Map<String, String> properties = new HashMap<>();
        if (manifest != null) {
            properties.put("backupFormatVersion", String.valueOf(manifest.getBackupFormatVersion()));
            properties.put("databaseVersion", String.valueOf(manifest.getDatabaseVersion()));
            properties.put("appVersion", manifest.getAppVersion());
            properties.put("createdAt", String.valueOf(manifest.getCreatedAt()));
            properties.put("backupId", manifest.getBackupId() != null ? manifest.getBackupId() : "");
            properties.put("databaseChecksum", manifest.getDatabaseChecksum() != null ? manifest.getDatabaseChecksum() : "");
            properties.put("documentCount", String.valueOf(manifest.getDocumentCount()));
            properties.put("propertyCount", String.valueOf(manifest.getPropertyCount()));
            properties.put("tenantCount", String.valueOf(manifest.getTenantCount()));
            properties.put("unitCount", String.valueOf(manifest.getUnitCount()));
            properties.put("propertySummary", manifest.getPropertySummary() != null ? manifest.getPropertySummary() : "");
        }
        if (archiveChecksum != null && !archiveChecksum.isEmpty()) {
            properties.put("archiveChecksum", archiveChecksum);
        }
        fileMetadata.setAppProperties(properties);

        FileContent mediaContent = new FileContent(BACKUP_ZIP_MIME_TYPE, localFile);

        // 1. Execute upload
        com.google.api.services.drive.model.File uploaded = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, size, createdTime, appProperties, trashed")
                .execute();

        if (uploaded == null || uploaded.getId() == null || uploaded.getId().isEmpty()) {
            throw new IOException("Drive upload failed: no file ID returned.");
        }

        // 2. Explicit post-upload verification (Read back from Drive)
        com.google.api.services.drive.model.File verifiedFile = driveService.files().get(uploaded.getId())
                .setFields("id, name, size, createdTime, appProperties, trashed")
                .execute();

        if (verifiedFile == null) {
            throw new IOException("Drive upload verification failed: file could not be read back from Google Drive.");
        }

        if (Boolean.TRUE.equals(verifiedFile.getTrashed())) {
            throw new IOException("Drive upload verification failed: file is marked as trashed on Google Drive.");
        }

        if (verifiedFile.getSize() == null || verifiedFile.getSize() <= 0) {
            throw new IOException("Drive upload verification failed: uploaded file size is zero or null.");
        }

        if (manifest != null && verifiedFile.getAppProperties() != null) {
            String remoteBackupId = verifiedFile.getAppProperties().get("backupId");
            if (remoteBackupId != null && !remoteBackupId.equals(manifest.getBackupId())) {
                throw new IOException("Drive upload verification failed: backup ID mismatch.");
            }
        }

        return verifiedFile;
    }

    public List<com.google.api.services.drive.model.File> listBackups() throws IOException {
        String parentFolderId = getOrCreateBackupsFolderId();
        String query = "'" + parentFolderId + "' in parents and trashed = false and mimeType != 'application/vnd.google-apps.folder'";

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, size, createdTime, appProperties, trashed)")
                .execute();

        List<com.google.api.services.drive.model.File> validBackups = new ArrayList<>();
        if (result.getFiles() != null) {
            for (com.google.api.services.drive.model.File f : result.getFiles()) {
                String name = f.getName();
                if (name != null && (name.endsWith(".zip") || name.startsWith("RobHouseRental_Backup"))) {
                    validBackups.add(f);
                }
            }
        }

        // Sort backups primarily by manifest createdAt timestamp (descending)
        Collections.sort(validBackups, (a, b) -> {
            long timeA = getBackupTimestamp(a);
            long timeB = getBackupTimestamp(b);
            return Long.compare(timeB, timeA); // Newest first
        });

        return validBackups;
    }

    private static long getBackupTimestamp(com.google.api.services.drive.model.File file) {
        if (file.getAppProperties() != null) {
            String createdAtStr = file.getAppProperties().get("createdAt");
            if (createdAtStr != null && !createdAtStr.isEmpty()) {
                try {
                    return Long.parseLong(createdAtStr);
                } catch (Exception ignored) {
                }
            }
        }
        if (file.getCreatedTime() != null) {
            return file.getCreatedTime().getValue();
        }
        return 0;
    }

    public com.google.api.services.drive.model.File getLatestBackup() throws IOException {
        List<com.google.api.services.drive.model.File> backups = listBackups();
        if (!backups.isEmpty()) {
            return backups.get(0);
        }
        return null;
    }

    public void downloadBackupFile(String driveFileId, File targetFile) throws IOException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            driveService.files().get(driveFileId)
                    .executeMediaAndDownloadTo(outputStream);
        }

        if (!targetFile.exists() || targetFile.length() == 0) {
            throw new IOException("Downloaded backup file is missing or 0 bytes.");
        }
    }

    public void deleteBackupFile(String driveFileId) throws IOException {
        driveService.files().delete(driveFileId).execute();
    }
}
