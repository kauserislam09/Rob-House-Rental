# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET
import os
import glob
import re

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"
LAYOUT_DIR = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\layout"

tree_en = ET.parse(EN_PATH)
root_en = tree_en.getroot()
en_strings = {c.attrib.get("name"): c.text for c in root_en if c.tag == "string"}

tree_bn = ET.parse(BN_PATH)
root_bn = tree_bn.getroot()
bn_strings = {c.attrib.get("name"): c.text for c in root_bn if c.tag == "string"}

print(f"Total English Strings: {len(en_strings)}")
print(f"Total Bangla Strings: {len(bn_strings)}")

missing_in_bn = [k for k in en_strings if k not in bn_strings and k != "default_web_client_id"]
missing_in_en = [k for k in bn_strings if k not in en_strings]

print(f"Missing in BN: {len(missing_in_bn)}")
print(f"Missing in EN: {len(missing_in_en)}")

# Layout audit
hardcoded_in_layouts = []
text_attr_pat = re.compile(r'android:(text|hint|title)="([^@\n][^"]*)"')

for file_path in glob.glob(os.path.join(LAYOUT_DIR, "*.xml")):
    fname = os.path.basename(file_path)
    with open(file_path, "r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, 1):
            matches = text_attr_pat.findall(line)
            for attr, val in matches:
                # filter tools:text or pure numbers/symbols if any
                if val.strip() and not val.startswith("?") and not val.startswith("@"):
                    hardcoded_in_layouts.append((fname, line_num, attr, val))

print(f"Hardcoded layout strings found: {len(hardcoded_in_layouts)}")
for item in hardcoded_in_layouts:
    print(f"  {item[0]}:{item[1]} {item[2]}=\"{item[3]}\"")
