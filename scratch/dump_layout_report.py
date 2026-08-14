# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET
import os
import glob
import re

LAYOUT_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\layout"
OUT_PATH = r"d:\My Apps Creations\RobHouseRental\scratch\layout_hardcoded_report.txt"

hardcoded_in_layouts = []
text_attr_pat = re.compile(r'android:(text|hint|title)="([^@\n][^"]*)"')

for file_path in glob.glob(os.path.join(LAYOUT_DIR, "*.xml")):
    fname = os.path.basename(file_path)
    with open(file_path, "r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, 1):
            matches = text_attr_pat.findall(line)
            for attr, val in matches:
                if val.strip() and not val.startswith("?") and not val.startswith("@"):
                    hardcoded_in_layouts.append(f"{fname}:{line_num} {attr}=\"{val}\"")

with open(OUT_PATH, "w", encoding="utf-8") as f:
    f.write(f"Total hardcoded strings: {len(hardcoded_in_layouts)}\n")
    for item in hardcoded_in_layouts:
        f.write(item + "\n")

print("Report written.")
