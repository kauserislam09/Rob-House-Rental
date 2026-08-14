# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

tree = ET.parse(EN_PATH)
root = tree.getroot()
keys = {child.attrib.get("name"): child.text for child in root if child.tag == "string"}

needed = [
    ("property_label", "Property", "প্রপার্টি"),
    ("tenant_label", "Tenant", "ভাড়াটিয়া"),
    ("type_document", "Document", "ডকুমেন্ট"),
    ("agreed_rent", "Agreed Rent", "চুক্তিবদ্ধ ভাড়া"),
]

missing_en = {}
for k, en, bn in needed:
    if k not in keys:
        missing_en[k] = (en, bn)

print("Missing needed:", missing_en)
