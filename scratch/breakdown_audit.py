import os
from collections import defaultdict

RESULTS_FILE = r"d:\My Apps Creations\RobHouseRental\scratch\audit_results.txt"

with open(RESULTS_FILE, "r", encoding="utf-8") as f:
    content = f.read()

xml_section, java_section = content.split("=== JAVA HARDCODED LITERALS")

xml_by_file = defaultdict(list)
for line in xml_section.splitlines()[1:]:
    if line.strip():
        parts = line.split(":", 1)
        filename = parts[0]
        xml_by_file[filename].append(line)

print("=== XML ISSUES BY FILE ===")
for filename, items in sorted(xml_by_file.items(), key=lambda x: -len(x[1])):
    print(f"  {filename}: {len(items)} items")

java_by_file = defaultdict(list)
for line in java_section.splitlines()[1:]:
    if line.strip():
        parts = line.split(":", 1)
        filename = parts[0]
        java_by_file[filename].append(line)

print("\n=== JAVA ISSUES BY FILE ===")
for filename, items in sorted(java_by_file.items(), key=lambda x: -len(x[1])):
    print(f"  {filename}: {len(items)} items")
