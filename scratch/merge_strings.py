# -*- coding: utf-8 -*-
import os
import re
import xml.etree.ElementTree as ET
from new_strings_catalog import NEW_STRINGS

STRINGS_EN = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
STRINGS_BN = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

def read_xml_keys(path):
    tree = ET.parse(path)
    root = tree.getroot()
    return {child.attrib.get("name") for child in root if child.tag == "string"}

en_keys = read_xml_keys(STRINGS_EN)
bn_keys = read_xml_keys(STRINGS_BN)

en_to_add = {}
bn_to_add = {}

for key, (en_val, bn_val) in NEW_STRINGS.items():
    if key not in en_keys:
        en_to_add[key] = en_val
    if key not in bn_keys:
        bn_to_add[key] = bn_val

print(f"Adding {len(en_to_add)} keys to English strings.xml")
print(f"Adding {len(bn_to_add)} keys to Bangla strings.xml")

def escape_xml(val):
    val = val.replace("&", "&amp;")
    val = val.replace("<", "&lt;").replace(">", "&gt;")
    val = val.replace("'", "\\'")
    val = val.replace('"', '\\"')
    val = val.replace("\n", "\\n")
    return val

if en_to_add:
    with open(STRINGS_EN, "r", encoding="utf-8") as f:
        content = f.read()
    insertion = "\n    <!-- Mission 8.5 Full Localization Strings -->\n"
    for k, v in en_to_add.items():
        insertion += f'    <string name="{k}">{escape_xml(v)}</string>\n'
    content = content.replace("</resources>", insertion + "</resources>")
    with open(STRINGS_EN, "w", encoding="utf-8") as f:
        f.write(content)

if bn_to_add:
    with open(STRINGS_BN, "r", encoding="utf-8") as f:
        content = f.read()
    insertion = "\n    <!-- Mission 8.5 Full Localization Strings (Bangla) -->\n"
    for k, v in bn_to_add.items():
        insertion += f'    <string name="{k}">{escape_xml(v)}</string>\n'
    content = content.replace("</resources>", insertion + "</resources>")
    with open(STRINGS_BN, "w", encoding="utf-8") as f:
        f.write(content)

print("Strings updated successfully.")
