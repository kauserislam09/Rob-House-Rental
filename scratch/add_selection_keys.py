# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

needed = [
    ("select_backup_to_restore", "Select Backup to Restore", "পুনরুদ্ধার করার জন্য ব্যাকআপ নির্বাচন করুন"),
    ("drive_backups_count", "%d backups found on Drive", "ড্রাইভে %dটি ব্যাকআপ পাওয়া গেছে"),
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
