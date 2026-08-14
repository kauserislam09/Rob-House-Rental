import re

# Fix EN
path_en = r"app\src\main\res\values\strings.xml"
content = open(path_en, encoding="utf-8").read()
content = content.replace(
    "Document file not found on device storage",
    "Document file could not be found."
)
open(path_en, "w", encoding="utf-8").write(content)

# Fix BN
path_bn = r"app\src\main\res\values-bn\strings.xml"
content_bn = open(path_bn, encoding="utf-8").read()

if 'document_file_not_found' in content_bn:
    pattern = r'(<string name="document_file_not_found">)[^<]*(</string>)'
    replacement = r'\1ডকুমেন্ট ফাইলটি খুঁজে পাওয়া যায়নি।\2'
    content_bn = re.sub(pattern, replacement, content_bn)
else:
    content_bn = content_bn.replace(
        "</resources>",
        '    <string name="document_file_not_found">ডকুমেন্ট ফাইলটি খুঁজে পাওয়া যায়নি।</string>\n</resources>'
    )

open(path_bn, "w", encoding="utf-8").write(content_bn)
print("Done")
