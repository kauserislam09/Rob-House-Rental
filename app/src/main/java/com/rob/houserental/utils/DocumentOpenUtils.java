package com.rob.houserental.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.rob.houserental.R;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Centralized, identity-safe document opening utility.
 *
 * SAFETY RULE: Document identity is always determined by the database filePath.
 * Global filename searching is NEVER used, because two tenants can have documents
 * with the same filename (e.g. Tenant A: NID.pdf, Tenant B: NID.pdf).
 *
 * Resolution order:
 *   1. Use database filePath directly if the file exists at that path.
 *   2. If filePath is stale (e.g. after restore to a new device), the caller
 *      is responsible for updating filePath in the DB. BackupManager does this
 *      during restore using the collision-safe tenant/<id>/, app/<id>/, expense/<id>/
 *      subfolders. DocumentOpenUtils does NOT guess by filename.
 *   3. If the file cannot be found at the given path, show a localized error message.
 */
public class DocumentOpenUtils {

    private static final Map<String, String> EXTENSION_MIME_MAP = new HashMap<>();

    static {
        EXTENSION_MIME_MAP.put("pdf",  "application/pdf");
        EXTENSION_MIME_MAP.put("jpg",  "image/jpeg");
        EXTENSION_MIME_MAP.put("jpeg", "image/jpeg");
        EXTENSION_MIME_MAP.put("png",  "image/png");
        EXTENSION_MIME_MAP.put("webp", "image/webp");
        EXTENSION_MIME_MAP.put("gif",  "image/gif");
        EXTENSION_MIME_MAP.put("bmp",  "image/bmp");
        EXTENSION_MIME_MAP.put("txt",  "text/plain");
        EXTENSION_MIME_MAP.put("csv",  "text/csv");
        EXTENSION_MIME_MAP.put("doc",  "application/msword");
        EXTENSION_MIME_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_MIME_MAP.put("xls",  "application/vnd.ms-excel");
        EXTENSION_MIME_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_MIME_MAP.put("ppt",  "application/vnd.ms-powerpoint");
        EXTENSION_MIME_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXTENSION_MIME_MAP.put("zip",  "application/zip");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Primary entry point: opens a document by its database filePath.
    // NEVER performs global filename searching. If the exact path does not
    // exist, the user sees "Document file could not be found."
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Open a document identified by its exact database {@code filePath}.
     *
     * @param context         calling context
     * @param filePath        absolute path as stored in the database
     * @param storedMimeType  MIME type as stored in the database (may be null/octet-stream)
     * @param displayName     human-readable name shown in the chooser title
     */
    public static boolean openDocument(Context context,
                                       String filePath,
                                       String storedMimeType,
                                       String displayName) {
        if (context == null) return false;

        // Validate path – do NOT fall back to any global search.
        if (filePath == null || filePath.trim().isEmpty()) {
            Toast.makeText(context, R.string.document_file_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            Toast.makeText(context, R.string.document_file_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }

        return openFile(context, file, storedMimeType, displayName);
    }

    /**
     * Open an already-resolved {@link File} object. Use when the caller already
     * holds a verified File reference (e.g. from a temp export path).
     */
    public static boolean openFile(Context context,
                                   File file,
                                   String storedMimeType,
                                   String displayName) {
        if (context == null) return false;

        if (file == null || !file.exists() || !file.isFile()) {
            Toast.makeText(context, R.string.document_file_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }

        String mimeType = resolveMimeType(file, storedMimeType, displayName);

        try {
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            String chooserTitle = (displayName != null && !displayName.trim().isEmpty())
                    ? displayName
                    : context.getString(R.string.view_document);

            Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(chooserIntent);
            return true;

        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_compatible_app_error, Toast.LENGTH_LONG).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.document_open_error), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MIME resolution
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Determine the MIME type to use when opening a file.
     *
     * Priority:
     *   1. Use storedMimeType if it is specific (not octet-stream, not null/empty, not "*\/*").
     *   2. Derive from the actual file extension (internal map).
     *   3. Derive from Android's MimeTypeMap.
     *   4. Fall back to "*\/*".
     */
    public static String resolveMimeType(File file, String storedMimeType, String displayName) {
        // Determine filename for extension extraction
        String fileName = (file != null) ? file.getName()
                : (displayName != null ? displayName : "");

        String extension = getFileExtension(fileName);

        // Use stored MIME if it is meaningful
        if (storedMimeType != null && !storedMimeType.trim().isEmpty()) {
            String lower = storedMimeType.trim().toLowerCase(Locale.US);
            if (!"application/octet-stream".equals(lower)
                    && !"*/*".equals(lower)
                    && !"null".equals(lower)) {
                return lower;
            }
        }

        // Map from file extension
        if (!extension.isEmpty()) {
            String mapped = EXTENSION_MIME_MAP.get(extension.toLowerCase(Locale.US));
            if (mapped != null) {
                return mapped;
            }
            String sysMime = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase(Locale.US));
            if (sysMime != null && !sysMime.trim().isEmpty()) {
                return sysMime;
            }
        }

        return "*/*";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Extract the file extension from a filename, or "" if none. */
    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).trim();
    }
}
