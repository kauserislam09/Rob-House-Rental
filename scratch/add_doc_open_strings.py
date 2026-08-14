# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

needed = [
    ("no_app_available_to_open", "No app is available to open this file.", "এই ফাইলটি খোলার জন্য কোনো অ্যাপ পাওয়া যায়নি।"),
    ("document_file_not_found", "Document file could not be found.", "ডকুমেন্ট ফাইলটি খুঁজে পাওয়া যায়নি।"),
]

def add_keys(path, is_bn=False):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    insertions = ""
    for k, en, bn in needed:
        val = bn if is_bn else en
        val_esc = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "\\'")
        if f'name="{k}"' not in content:
            insertions += f'    <string name="{k}">{val_esc}</string>\n'
    
    if insertions:
        content = content.replace("</resources>", insertions + "</resources>")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

add_keys(EN_PATH, False)
add_keys(BN_PATH, True)
print("Keys added successfully.")
