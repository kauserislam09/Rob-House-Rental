# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET

EN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values\strings.xml"
BN_PATH = r"d:\My Apps Creations\RobHouseRental\app\src\main\res\values-bn\strings.xml"

needed = [
    ("status_local_only", "LOCAL ONLY", "শুধুমাত্র লোকাল"),
    ("local_backup_created_drive_failed", "Local backup created, but Google Drive upload failed: %s", "লোকাল ব্যাকআপ তৈরি হয়েছে, কিন্তু গুগল ড্রাইভে আপলোড ব্যর্থ হয়েছে: %s"),
    ("local_backup_success_no_drive", "Local backup created successfully.", "লোকাল ব্যাকআপ সফলভাবে তৈরি হয়েছে।"),
    ("backup_zip_slip_error", "Security error: backup file contains invalid path traversal.", "নিরাপত্তা ত্রুটি: ব্যাকআপ ফাইলটিতে অবৈধ পাথ রয়েছে।"),
    ("db_integrity_failed", "Database integrity verification failed.", "ডাটাবেস সততা যাচাইকরণ ব্যর্থ হয়েছে।"),
    ("drive_file_verification_failed", "Google Drive upload verification failed: file size or metadata mismatch.", "গুগল ড্রাইভ আপলোড যাচাইকরণ ব্যর্থ হয়েছে: ফাইলের আকার বা মেটাডাটা অমিল।"),
    ("restore_rollback_success_msg", "Restore failed. Previous data was safely recovered.", "পুনরুদ্ধার ব্যর্থ হয়েছে। পূর্ববর্তী তথ্য নিরাপদে পুনরুদ্ধার করা হয়েছে।"),
    ("cloud_backup_success_msg", "Backup uploaded to Google Drive and verified successfully.", "ব্যাকআপ সফলভাবে গুগল ড্রাইভে আপলোড এবং যাচাই করা হয়েছে।"),
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
