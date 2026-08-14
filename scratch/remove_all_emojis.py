import os
import re
import unicodedata

project_dir = r"d:\My Apps Creations\RobHouseRental\app\src\main"

# Regex pattern matching standard emojis and symbols
emoji_regex = re.compile(
    "[\U0001F000-\U0001FFFF"
    "\u2600-\u27BF"
    "\u2300-\u23FF"
    "\u2B50\u2B55\u2934\u2935"
    "\u2190-\u21FF"
    "\u3297\u3299"
    "\uFE0F]" # variation selector
)

def clean_line(line):
    # Custom cleaning for specific strings
    # Keep Bangla currency symbol ৳ (0x09F3) and standard math/punctuation
    cleaned = []
    for char in line:
        code = ord(char)
        if 0x0980 <= code <= 0x09FF: # Bangla characters block
            cleaned.append(char)
            continue
        category = unicodedata.category(char)
        # Check if emoji symbol
        if emoji_regex.search(char) or (category == 'So' and char not in ('৳', '°', '±', '×', '÷', '%', '+', '-', '=', '>', '<', '›', '‹', '«', '»', '•', '…', '&', '|', '$')):
            continue
        cleaned.append(char)
    
    result = "".join(cleaned)
    # Fix double spaces resulting from emoji removal (e.g., "👤  Name" -> "Name")
    # But preserve formatting whitespace at beginning of line
    leading_spaces = len(result) - len(result.lstrip())
    body = result[leading_spaces:]
    body = re.sub(r'  +', ' ', body)
    body = body.replace('" "', '""').replace('"" + ', '').replace(' + ""', '')
    return result[:leading_spaces] + body

modified_files = 0
total_replacements = 0

for root, dirs, files in os.walk(project_dir):
    for f in files:
        if f.endswith(('.xml', '.java')):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as file:
                content = file.readlines()
            
            new_content = []
            changed = False
            for line in content:
                cleaned = clean_line(line)
                if cleaned != line:
                    changed = True
                    total_replacements += 1
                new_content.append(cleaned)
            
            if changed:
                with open(path, 'w', encoding='utf-8') as file:
                    file.writelines(new_content)
                modified_files += 1
                print(f"Updated: {os.path.relpath(path, project_dir)}")

print(f"Done! Modified {modified_files} files, removed emojis across {total_replacements} lines.")
