# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

needed = [
    ("restore_preview_title", "Restore Preview", "পুনরুদ্ধারের পূর্বরূপ"),
    ("restore_preview_properties", "Properties (%d):", "সম্পত্তি (%dটি):"),
    ("restore_preview_tenants", "Tenants: %d", "ভাড়াটিয়া: %d জন"),
    ("restore_preview_units", "Units: %d", "ইউনিট: %dটি"),
    ("restore_preview_documents", "Documents: %d", "নথিপত্র: %dটি"),
    ("restore_preview_warning", "Restoring this backup will replace the current application data.", "এই ব্যাকআপটি পুনরুদ্ধার করলে বর্তমান অ্যাপের তথ্য প্রতিস্থাপিত হবে।"),
    ("snapshot_mismatch_error", "Backup could not be completed because the database snapshot does not contain the latest data.", "ব্যাকআপ সম্পন্ন করা যায়নি কারণ ডাটাবেস স্ন্যাপশটে সাম্প্রতিক তথ্য অনুপস্থিত।"),
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
print("Keys merged successfully.")
