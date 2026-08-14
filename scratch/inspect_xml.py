import os
import re

RES_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\layout"
OUT_FILE = r"d:\My Apps Creations\RobHouseRental\scratch\xml_issues.txt"

with open(OUT_FILE, "w", encoding="utf-8") as out:
    for f in sorted(os.listdir(RES_DIR)):
        if f.endswith(".xml"):
            path = os.path.join(RES_DIR, f)
            with open(path, "r", encoding="utf-8") as file:
                lines = file.readlines()
            file_issues = []
            for line_idx, line in enumerate(lines, 1):
                for attr in ["android:text", "android:hint", "android:title", "android:contentDescription", "app:title", "app:hint"]:
                    pattern = rf'{attr}="([^"@?][^"]*)"'
                    matches = re.finditer(pattern, line)
                    for m in matches:
                        val = m.group(1).strip()
                        if val and not val.isdigit() and len(val) > 1 and not re.match(r'^[\d\s\.,:;/\-\(\)৳⚡💧🔥🌐☁️ℹ️›•]+$', val):
                            file_issues.append((line_idx, attr, val, line.strip()))
            if file_issues:
                out.write(f"=== {f} ({len(file_issues)}) ===\n")
                for l_num, attr, val, line in file_issues:
                    out.write(f"  Line {l_num:3d} [{attr}]: \"{val}\" -> {line}\n")

print(f"Written XML issues to {OUT_FILE}")
