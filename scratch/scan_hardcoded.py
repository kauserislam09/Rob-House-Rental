import os
import re

JAVA_DIR = 'app/src/main/java/com/rob/houserental'

# Look for Toast.makeText, AlertDialog titles/messages, TextView.setText with string literal
toast_pattern = re.compile(r'Toast\.makeText\([^,]+,\s*"([^"]+)"')
dialog_pattern = re.compile(r'\.(?:setTitle|setMessage)\(\s*"([^"]+)"')
set_text_pattern = re.compile(r'\.setText\(\s*"([^"]+)"')

hardcoded_strings = []

for root, dirs, files in os.walk(JAVA_DIR):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as jf:
                lines = jf.readlines()
                for i, line in enumerate(lines, 1):
                    # check toasts
                    for m in toast_pattern.finditer(line):
                        hardcoded_strings.append((f, i, "Toast", m.group(1)))
                    # check dialogs
                    for m in dialog_pattern.finditer(line):
                        hardcoded_strings.append((f, i, "Dialog", m.group(1)))

print("Found {} hardcoded user-visible strings:".format(len(hardcoded_strings)))
for item in hardcoded_strings:
    print(f"{item[0]}:{item[1]} [{item[2]}] -> \"{item[3]}\"")
