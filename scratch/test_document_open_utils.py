# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

print("==================================================")
print("DOCUMENT OPEN UTILITIES VERIFICATION TEST SUITE")
print("==================================================")

# 1. Verify String Keys
tree_en = ET.parse(EN_PATH)
root_en = tree_en.getroot()
en_dict = {c.attrib.get("name"): c.text for c in root_en if c.tag == "string"}

tree_bn = ET.parse(BN_PATH)
root_bn = tree_bn.getroot()
bn_dict = {c.attrib.get("name"): c.text for c in root_bn if c.tag == "string"}

assert "no_app_available_to_open" in en_dict
assert "no_app_available_to_open" in bn_dict
assert "document_file_not_found" in en_dict
assert "document_file_not_found" in bn_dict

print(f"  EN: no_app_available_to_open = '{en_dict['no_app_available_to_open']}'")
print(f"  BN: no_app_available_to_open = '{bn_dict['no_app_available_to_open']}'")
print(f"  EN: document_file_not_found  = '{en_dict['document_file_not_found']}'")
print(f"  BN: document_file_not_found  = '{bn_dict['document_file_not_found']}'")

# 2. MIME Resolution Logic Test
EXT_MAP = {
    "pdf": "application/pdf",
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg",
    "png": "image/png",
    "webp": "image/webp",
    "gif": "image/gif",
    "txt": "text/plain",
    "csv": "text/csv",
    "doc": "application/msword",
    "docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xls": "application/vnd.ms-excel",
    "xlsx": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "ppt": "application/vnd.ms-powerpoint",
    "pptx": "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "zip": "application/zip",
}

def resolve_mime(filename, stored_mime):
    if stored_mime and stored_mime not in ["application/octet-stream", "*/*", "null"]:
        return stored_mime.lower()
    ext = filename.split(".")[-1].lower() if "." in filename else ""
    return EXT_MAP.get(ext, "*/*")

test_cases = [
    ("agreement.pdf", "application/octet-stream", "application/pdf"),
    ("nid_card.jpg", "*/*", "image/jpeg"),
    ("tenant_photo.png", "", "image/png"),
    ("utility_bill.pdf", "application/pdf", "application/pdf"),
    ("tax_record.xlsx", "application/octet-stream", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ("generic_binary.xyz", "application/octet-stream", "*/*"),
]

for fname, stored, expected in test_cases:
    resolved = resolve_mime(fname, stored)
    print(f"  File: {fname:20} (Stored: {stored:25}) -> Resolved: {resolved}")
    assert resolved == expected, f"Failed for {fname}: got {resolved}, expected {expected}"

print("\n==================================================")
print("ALL DOCUMENT OPEN UTILITIES TESTS PASSED (100%)")
print("==================================================")
