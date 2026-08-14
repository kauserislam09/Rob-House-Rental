# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

needed = [
    ("add_property_title", "Add Property", "প্রপার্টি যুক্ত করুন"),
    ("latest_cloud_backup", "Latest Cloud Backup", "সর্বশেষ ক্লাউড ব্যাকআপ"),
    ("doc_type_general", "Document File", "ডকুমেন্ট ফাইল"),
    ("total_documents", "Total Documents", "মোট ডকুমেন্ট"),
    ("properties_title", "Properties", "প্রপার্টিসমূহ"),
    ("no_properties_found", "No Properties Yet", "এখনো কোনো প্রপার্টি নেই"),
    ("add_property_to_get_started", "Add your first property to get started.", "শুরু করতে প্রথম প্রপার্টি যুক্ত করুন।"),
    ("add_property", "Add Property", "প্রপার্টি যুক্ত করুন"),
    ("property_details_title", "Property Details", "প্রপার্টির বিবরণ"),
    ("property_type", "Property Type", "প্রপার্টির ধরন"),
    ("edit_property", "Edit Property", "প্রপার্টি এডিট করুন"),
    ("nid_label", "NID", "এনআইডি"),
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
print("Additional keys added successfully.")
