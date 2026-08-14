import os
import re
import sys

JAVA_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\java"
RES_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\res"
OUTPUT_FILE = r"d:\My Apps Creations\RobHouseRental\scratch\audit_results.txt"

# 1. Audit XML files
xml_issues = []
for root, _, files in os.walk(RES_DIR):
    if "values" in root: # skip strings.xml, colors.xml etc
        continue
    for f in files:
        if f.endswith(".xml"):
            path = os.path.join(root, f)
            with open(path, "r", encoding="utf-8") as file:
                lines = file.readlines()
            for line_idx, line in enumerate(lines, 1):
                # Search for android:text="...", android:hint="...", android:title="...", android:contentDescription="..."
                for attr in ["android:text", "android:hint", "android:title", "android:contentDescription", "app:title", "app:hint"]:
                    pattern = rf'{attr}="([^"@?][^"]*)"'
                    matches = re.finditer(pattern, line)
                    for m in matches:
                        val = m.group(1).strip()
                        # Ignore pure numbers or single symbols
                        if val and not val.isdigit() and len(val) > 1 and not re.match(r'^[\d\s\.,:;/\-\(\)৳⚡💧🔥🌐☁️ℹ️›•]+$', val):
                            xml_issues.append((path, line_idx, attr, val, line.strip()))

# 2. Audit Java files
java_issues = []
string_literal_pattern = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

IGNORED_STRINGS = {
    "", " ", "  ", "\n", ",", ":", "-", "•", "/", "\\", "(", ")", "[", "]", "{", "}",
    "SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ACTIVE", "INACTIVE", "ARCHIVED",
    "VACANT", "OCCUPIED", "RESERVED", "MAINTENANCE", "PAID", "UNPAID", "PARTIAL", "OVERDUE", "WAIVED",
    "ENDED", "CANCELLED", "MANUAL", "AUTO", "EXPORT", "IMPORT", "GOOGLE_DRIVE", "RESTORE", "RESTORED",
    "CASH", "BANK_TRANSFER", "MOBILE_BANKING", "CARD", "CHEQUE", "OTHER",
    "ELECTRICITY", "WATER", "GAS", "INTERNET", "SERVICE_CHARGE", "SECURITY", "GENERATOR",
    "REPAIR", "CLEANING", "PLUMBING", "ELECTRICAL", "PAINTING", "RENOVATION", "PROPERTY_TAX",
    "DEED", "TAX_RECORD", "NID", "PASSPORT", "AGREEMENT", "VOUCHER", "PAYMENT_SLIP", "RECEIPT",
    "image/*", "application/pdf", "application/zip", "*/*", "pdf", "jpg", "jpeg", "png",
    "yyyy-MM-dd", "dd MMM yyyy", "yyyyMMdd_HHmm", "dd MMM yyyy, hh:mm a", "dd/MM/yyyy", "MMM yyyy",
    "property_id", "unit_id", "tenant_id", "tenancy_id", "rent_record_id", "bill_id", "expense_id", "document_id",
    "application/json", "UTF-8", "tag", "TAG", "rob_house_rental.db", "rob-personal-stock",
    "language_prefs", "app_language_tag", "autoStoreLocales", "en", "bn", "system",
    "com.rob.houserental", "rob.houserental", "RobHouseRental"
}

for root, _, files in os.walk(JAVA_DIR):
    for f in files:
        if f.endswith(".java"):
            path = os.path.join(root, f)
            with open(path, "r", encoding="utf-8") as file:
                lines = file.readlines()
            for line_idx, line in enumerate(lines, 1):
                sline = line.strip()
                if sline.startswith("//") or sline.startswith("*") or sline.startswith("/*"):
                    continue
                # Ignore log tags, database queries, intent extras, color hex, format codes
                if sline.startswith("Log.") or "query" in sline.lower() or "sqlite" in sline.lower() or "table" in sline.lower() or "column" in sline.lower() or sline.startswith("@"):
                    continue
                matches = string_literal_pattern.findall(line)
                for m in matches:
                    if m in IGNORED_STRINGS:
                        continue
                    if m.startswith("#") or m.startswith("SELECT ") or m.startswith("INSERT ") or m.startswith("UPDATE ") or m.startswith("DELETE ") or m.startswith("CREATE "):
                        continue
                    if m.startswith("com.google") or m.startswith("https://") or m.startswith("http://") or m.startswith("content://"):
                        continue
                    if re.match(r'^[A-Z0-9_]+$', m):
                        continue
                    if m.startswith("%") or m.startswith("fileprovider"):
                        continue
                    
                    if any(c.isalpha() for c in m) and not m.startswith("android."):
                        java_issues.append((path, line_idx, m, sline))

with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
    out.write(f"=== XML HARDCODED ATTRIBUTES ({len(xml_issues)}) ===\n")
    for path, line_no, attr, val, line in xml_issues:
        rel_path = os.path.relpath(path, RES_DIR)
        out.write(f"{rel_path}:{line_no} [{attr}] -> \"{val}\"\n")
    
    out.write(f"\n=== JAVA HARDCODED LITERALS ({len(java_issues)}) ===\n")
    for path, line_no, val, sline in java_issues:
        rel_path = os.path.relpath(path, JAVA_DIR)
        out.write(f"{rel_path}:{line_no} -> \"{val}\" (line: {sline})\n")

print(f"Audit completed: {len(xml_issues)} XML issues and {len(java_issues)} Java issues found. Written to {OUTPUT_FILE}")
