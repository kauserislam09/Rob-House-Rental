import os
import re

JAVA_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\java"
OUT_FILE = r"d:\My Apps Creations\RobHouseRental\scratch\java_ui_strings.txt"

# Files to inspect (UI files: Activities, Fragments, Adapters, Helpers)
ui_files = []
for root, _, files in os.walk(JAVA_DIR):
    for f in files:
        if f.endswith(".java") and not f.endswith("Dao.java") and not f.endswith("Database.java") and not f.endswith("Entity.java"):
            ui_files.append(os.path.join(root, f))

string_literal_pattern = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

IGNORED = {
    "", " ", "  ", "\n", ",", ":", "-", "•", "/", "\\", "(", ")", "[", "]", "{", "}",
    "image/*", "application/pdf", "application/zip", "*/*", "pdf", "jpg", "jpeg", "png",
    "yyyy-MM-dd", "dd MMM yyyy", "yyyyMMdd_HHmm", "dd MMM yyyy, hh:mm a", "dd/MM/yyyy", "MMM yyyy",
    "property_id", "unit_id", "tenant_id", "tenancy_id", "rent_record_id", "bill_id", "expense_id", "document_id",
    "application/json", "UTF-8", "tag", "TAG", "rob_house_rental.db", "rob-personal-stock",
    "language_prefs", "app_language_tag", "autoStoreLocales", "en", "bn", "system",
    "com.rob.houserental", "rob.houserental", "RobHouseRental"
}

with open(OUT_FILE, "w", encoding="utf-8") as out:
    for path in sorted(ui_files):
        rel = os.path.relpath(path, JAVA_DIR)
        with open(path, "r", encoding="utf-8") as f:
            lines = f.readlines()
        file_issues = []
        for idx, line in enumerate(lines, 1):
            sline = line.strip()
            if sline.startswith("//") or sline.startswith("*") or sline.startswith("/*") or sline.startswith("import ") or sline.startswith("package "):
                continue
            matches = string_literal_pattern.findall(line)
            for m in matches:
                if m in IGNORED or m.startswith("#") or m.startswith("SELECT ") or m.startswith("http"):
                    continue
                if any(c.isalpha() for c in m):
                    file_issues.append((idx, m, sline))
        if file_issues:
            out.write(f"=== {rel} ({len(file_issues)}) ===\n")
            for l_num, m, sline in file_issues:
                out.write(f"  L{l_num:3d}: \"{m}\" in `{sline}`\n")

print(f"Written Java UI strings to {OUT_FILE}")
