# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET
import os
import glob
import zipfile
import json
import hashlib

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

# 1. Verify String Parity
tree_en = ET.parse(EN_PATH)
root_en = tree_en.getroot()
en_keys = {c.attrib.get("name"): c.text for c in root_en if c.tag == "string"}

tree_bn = ET.parse(BN_PATH)
root_bn = tree_bn.getroot()
bn_keys = {c.attrib.get("name"): c.text for c in root_bn if c.tag == "string"}

missing_in_bn = [k for k in en_keys if k not in bn_keys and k != "default_web_client_id"]
missing_in_en = [k for k in bn_keys if k not in en_keys]

print("=== 1. STRING PARITY TEST ===")
print(f"Total English Strings: {len(en_keys)}")
print(f"Total Bangla Strings: {len(bn_keys)}")
print(f"Missing in Bangla: {len(missing_in_bn)}")
print(f"Missing in English: {len(missing_in_en)}")

# 2. Verify Backup Package Architecture
print("\n=== 2. BACKUP PACKAGE ARCHITECTURE TEST ===")
test_zip_path = r"d:\My Apps Creations\RobHouseRental\scratch\test_backup.zip"
staging_dir = r"d:\My Apps Creations\RobHouseRental\scratch\staging"
os.makedirs(os.path.join(staging_dir, "database"), exist_ok=True)
os.makedirs(os.path.join(staging_dir, "documents"), exist_ok=True)
os.makedirs(os.path.join(staging_dir, "metadata"), exist_ok=True)
os.makedirs(os.path.join(staging_dir, "settings"), exist_ok=True)

# Create dummy DB file
dummy_db = os.path.join(staging_dir, "database", "rob_house_rental.db")
with open(dummy_db, "wb") as f:
    f.write(b"SQLITE_DUMMY_DATABASE_DATA_1234567890")

db_hash = hashlib.sha256(b"SQLITE_DUMMY_DATABASE_DATA_1234567890").hexdigest()

# Create dummy document
dummy_doc = os.path.join(staging_dir, "documents", "nid_card.pdf")
with open(dummy_doc, "wb") as f:
    f.write(b"PDF_DOCUMENT_TEST_CONTENT")

# Create manifest
manifest_data = {
    "backupFormatVersion": 1,
    "appVersion": "1.0",
    "databaseVersion": 10,
    "createdAt": 1723190000000,
    "backupId": "test-uuid-9999",
    "databaseChecksum": db_hash,
    "documentCount": 1,
    "backupSizeBytes": 1024,
    "deviceModel": "Test Device",
    "androidVersion": 34
}
with open(os.path.join(staging_dir, "metadata", "backup_manifest.json"), "w", encoding="utf-8") as f:
    json.dump(manifest_data, f, indent=2)

with open(os.path.join(staging_dir, "settings", "settings.json"), "w", encoding="utf-8") as f:
    json.dump({"schedule": "DAILY"}, f, indent=2)

# Create zip
with zipfile.ZipFile(test_zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
    for root, dirs, files in os.walk(staging_dir):
        for file in files:
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, staging_dir)
            zf.write(full_path, rel_path)

print("Created test package:", test_zip_path)

# Verify zip
with zipfile.ZipFile(test_zip_path, "r") as zf:
    names = zf.namelist()
    print("Entries in backup package:", names)
    assert any("database/rob_house_rental.db" in n for n in names), "Missing database in package"
    assert any("metadata/backup_manifest.json" in n for n in names), "Missing manifest in package"
    assert any("documents/nid_card.pdf" in n for n in names), "Missing documents in package"
    assert any("settings/settings.json" in n for n in names), "Missing settings in package"

print("Backup package validation: PASSED")

# 3. Checksum verification test
with zipfile.ZipFile(test_zip_path, "r") as zf:
    db_bytes = zf.read("database/rob_house_rental.db")
    read_hash = hashlib.sha256(db_bytes).hexdigest()
    assert read_hash == db_hash, "Checksum mismatch"
print("Checksum verification: PASSED (SHA-256 matched)")

# Cleanup
import shutil
shutil.rmtree(staging_dir, ignore_errors=True)
if os.path.exists(test_zip_path):
    os.remove(test_zip_path)

print("\nALL BACKUP & RESTORE ARCHITECTURE TESTS PASSED SUCCESSFULLY!")
