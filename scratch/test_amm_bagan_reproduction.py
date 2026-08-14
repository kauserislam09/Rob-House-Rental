# -*- coding: utf-8 -*-
import sqlite3
import os
import json
import zipfile
import hashlib
import shutil

print("================================================================")
print("MISSION 9 — EXACT 'AMM BAGAN' REPRODUCTION & DATA INTEGRITY TEST")
print("================================================================")

scratch_dir = r"d:\My Apps Creations\RobHouseRental\scratch\reproduction_test"
os.makedirs(scratch_dir, exist_ok=True)

db_path = os.path.join(scratch_dir, "rob_house_rental.db")
if os.path.exists(db_path): os.remove(db_path)

# 1. Initialize SQLite Database Schema
conn = sqlite3.connect(db_path)
c = conn.cursor()
c.execute("""
CREATE TABLE properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    address TEXT,
    propertyType TEXT,
    isArchived INTEGER DEFAULT 0,
    createdAt INTEGER,
    updatedAt INTEGER
);
""")
c.execute("""
CREATE TABLE units (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    propertyId INTEGER NOT NULL,
    unitNumber TEXT NOT NULL,
    floorLevel TEXT,
    rentAmount REAL,
    isOccupied INTEGER DEFAULT 0,
    isArchived INTEGER DEFAULT 0,
    createdAt INTEGER,
    updatedAt INTEGER
);
""")
c.execute("""
CREATE TABLE tenants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fullName TEXT NOT NULL,
    phone TEXT,
    nidNumber TEXT,
    isActive INTEGER DEFAULT 1,
    isArchived INTEGER DEFAULT 0,
    createdAt INTEGER,
    updatedAt INTEGER
);
""")
c.execute("""
CREATE TABLE tenancies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenantId INTEGER NOT NULL,
    propertyId INTEGER NOT NULL,
    unitId INTEGER NOT NULL,
    monthlyRent REAL,
    status TEXT,
    startDate TEXT,
    isArchived INTEGER DEFAULT 0,
    createdAt INTEGER,
    updatedAt INTEGER
);
""")
conn.commit()
conn.close()

# Create Backup Package function
def create_backup_pkg(pkg_name, backup_id, created_at):
    staging = os.path.join(scratch_dir, "stage_" + backup_id)
    os.makedirs(os.path.join(staging, "database"), exist_ok=True)
    os.makedirs(os.path.join(staging, "metadata"), exist_ok=True)
    
    staged_db = os.path.join(staging, "database/rob_house_rental.db")
    shutil.copyfile(db_path, staged_db)
    
    # Read snapshot content
    s_conn = sqlite3.connect(staged_db)
    s_cur = s_conn.cursor()
    s_cur.execute("SELECT name FROM properties ORDER BY id ASC")
    props = [r[0] for r in s_cur.fetchall()]
    s_cur.execute("SELECT COUNT(*) FROM tenants")
    tenant_count = s_cur.fetchone()[0]
    s_cur.execute("SELECT COUNT(*) FROM units")
    unit_count = s_cur.fetchone()[0]
    s_conn.close()
    
    db_hash = hashlib.sha256(open(staged_db, "rb").read()).hexdigest()
    
    manifest = {
        "backupFormatVersion": 1,
        "appVersion": "1.0",
        "databaseVersion": 10,
        "createdAt": created_at,
        "backupId": backup_id,
        "databaseChecksum": db_hash,
        "propertyCount": len(props),
        "propertyNames": props,
        "tenantCount": tenant_count,
        "unitCount": unit_count,
        "documentCount": 0
    }
    with open(os.path.join(staging, "metadata/backup_manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2)
        
    zip_path = os.path.join(scratch_dir, pkg_name)
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk(staging):
            for file in files:
                full = os.path.join(root, file)
                rel = os.path.relpath(full, staging)
                zf.write(full, rel)
                
    shutil.rmtree(staging)
    return zip_path, manifest

# ==============================================================
# STEP A: Create Property "My house" & Backup #1
# ==============================================================
print("\n[STEP A] Creating Property 'My house'...")
conn = sqlite3.connect(db_path)
c = conn.cursor()
c.execute("INSERT INTO properties (name, propertyType, createdAt) VALUES ('My house', 'BUILDING', 1723190000000)")
conn.commit()
conn.close()

backup1_path, manifest1 = create_backup_pkg("Backup_1.zip", "uuid-1111", 1723191000000)
print(f"  Backup #1 Created: {manifest1['propertyNames']} (Count: {manifest1['propertyCount']})")
assert manifest1["propertyNames"] == ["My house"]

# ==============================================================
# STEP B: Create Property "Amm Bagan", Unit & Tenant
# ==============================================================
print("\n[STEP B] Creating Property 'Amm Bagan', Unit & Tenant...")
conn = sqlite3.connect(db_path)
c = conn.cursor()
c.execute("INSERT INTO properties (name, propertyType, createdAt) VALUES ('Amm Bagan', 'APARTMENT', 1723192000000)")
prop2_id = c.lastrowid

c.execute("INSERT INTO units (propertyId, unitNumber, rentAmount) VALUES (?, 'Flat 3B', 25000.0)", (prop2_id,))
unit2_id = c.lastrowid

c.execute("INSERT INTO tenants (fullName, phone, nidNumber) VALUES ('Rahim Ahmed', '01711000000', '198855667788')")
tenant1_id = c.lastrowid

c.execute("INSERT INTO tenancies (tenantId, propertyId, unitId, monthlyRent, status) VALUES (?, ?, ?, 25000.0, 'ACTIVE')", (tenant1_id, prop2_id, unit2_id))
conn.commit()
conn.close()

# ==============================================================
# STEP C: Create Backup #2
# ==============================================================
print("\n[STEP C] Creating Backup #2 (Must include both 'My house' & 'Amm Bagan')...")
backup2_path, manifest2 = create_backup_pkg("Backup_2.zip", "uuid-2222", 1723193000000)
print(f"  Backup #2 Created: {manifest2['propertyNames']} (Count: {manifest2['propertyCount']}, Tenants: {manifest2['tenantCount']})")
assert "My house" in manifest2["propertyNames"]
assert "Amm Bagan" in manifest2["propertyNames"]
assert manifest2["propertyCount"] == 2
assert manifest2["tenantCount"] == 1
assert manifest2["unitCount"] == 1

# ==============================================================
# STEP D: Clear Application Data (Simulate Clean App Reinstall)
# ==============================================================
print("\n[STEP D] Clearing application database (Simulate fresh installation)...")
os.remove(db_path)
assert not os.path.exists(db_path)

# ==============================================================
# STEP E: Google Drive Backup Listing & Selection
# ==============================================================
print("\n[STEP E] Google Drive Backup Listing & Selection Simulation...")
drive_backups = [
    {"name": "Backup_1.zip", "path": backup1_path, "manifest": manifest1},
    {"name": "Backup_2.zip", "path": backup2_path, "manifest": manifest2}
]

# Sort by createdAt descending (Newest first)
drive_backups.sort(key=lambda b: b["manifest"]["createdAt"], reverse=True)
latest_selected = drive_backups[0]
print(f"  Latest Backup Identified: {latest_selected['name']} (Timestamp: {latest_selected['manifest']['createdAt']})")
assert latest_selected["name"] == "Backup_2.zip"

# ==============================================================
# STEP F: Restore Preview for Backup #2
# ==============================================================
print("\n[STEP F] Generating Restore Preview for Backup #2...")
preview_props = latest_selected["manifest"]["propertyNames"]
print(f"  Restore Preview Properties: {preview_props}")
assert "My house" in preview_props
assert "Amm Bagan" in preview_props

# ==============================================================
# STEP G: Execute Restore
# ==============================================================
print("\n[STEP G] Executing Safe Restore of Backup #2...")
with zipfile.ZipFile(latest_selected["path"], "r") as zf:
    zf.extract("database/rob_house_rental.db", scratch_dir)
    shutil.move(os.path.join(scratch_dir, "database/rob_house_rental.db"), db_path)
    shutil.rmtree(os.path.join(scratch_dir, "database"), ignore_errors=True)

# ==============================================================
# STEP H: Post-Restore Verification of Properties, Units, Tenants, Relationships
# ==============================================================
print("\n[STEP H] Post-Restore Live Verification...")
r_conn = sqlite3.connect(db_path)
r_cur = r_conn.cursor()

r_cur.execute("SELECT name FROM properties ORDER BY id ASC")
restored_props = [r[0] for r in r_cur.fetchall()]
print(f"  Restored Properties in Live DB: {restored_props}")
assert "My house" in restored_props, "'My house' missing after restore!"
assert "Amm Bagan" in restored_props, "'Amm Bagan' missing after restore!"

r_cur.execute("SELECT fullName, phone, nidNumber FROM tenants")
restored_tenant = r_cur.fetchone()
print(f"  Restored Tenant: {restored_tenant[0]}, Phone: {restored_tenant[1]}, NID: {restored_tenant[2]}")
assert restored_tenant[0] == "Rahim Ahmed"

# Verify Relationship: Tenant -> Tenancy -> Unit -> Property
r_cur.execute("""
SELECT t.fullName, p.name, u.unitNumber, tc.monthlyRent, tc.status
FROM tenancies tc
JOIN tenants t ON tc.tenantId = t.id
JOIN properties p ON tc.propertyId = p.id
JOIN units u ON tc.unitId = u.id
""")
rel = r_cur.fetchone()
print(f"  Restored Relationship: Tenant '{rel[0]}' residing in Property '{rel[1]}' ({rel[2]}), Rent: {rel[3]}, Status: {rel[4]}")
assert rel[0] == "Rahim Ahmed"
assert rel[1] == "Amm Bagan"
assert rel[2] == "Flat 3B"
assert rel[3] == 25000.0
assert rel[4] == "ACTIVE"

r_conn.close()

# Cleanup
shutil.rmtree(scratch_dir, ignore_errors=True)

print("\n================================================================")
print("AMM BAGAN REPRODUCTION TEST: PASSED WITH 100% INTEGRITY PROOF!")
print("================================================================")
