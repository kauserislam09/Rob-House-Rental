import os
import unicodedata

project_dir = r"d:\My Apps Creations\RobHouseRental\app\src\main"
matches = []

def is_emoji(char):
    code = ord(char)
    # Exclude Bangla characters and standard ASCII/Latin/punctuation
    if 0x0980 <= code <= 0x09FF: # Bangla Unicode block
        return False
    category = unicodedata.category(char)
    if category in ('So', 'Sk', 'Sm'):
        # Exclude currency symbols like Taka ৳ (0x09F3), degree, etc., keep only emojis
        if char not in ('৳', '°', '±', '×', '÷', '%', '+', '-', '=', '>', '<', '›', '‹', '«', '»', '•', '…', '&', '|', '$'):
            return True
    return False

for root, dirs, files in os.walk(project_dir):
    for f in files:
        if f.endswith(('.xml', '.java')):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as file:
                for line_num, line in enumerate(file, 1):
                    emojis_in_line = [ch for ch in line if is_emoji(ch)]
                    if emojis_in_line:
                        matches.append((path, line_num, line.strip(), set(emojis_in_line)))

out_path = r"d:\My Apps Creations\RobHouseRental\scratch\emojis_found.txt"
with open(out_path, 'w', encoding='utf-8') as out:
    out.write(f"Total emoji matches found: {len(matches)}\n")
    for path, line_num, line, emojis in matches:
        rel_path = os.path.relpath(path, project_dir)
        out.write(f"{rel_path}:{line_num} -> {' '.join(emojis)} -> {line}\n")

print(f"Results written to {out_path}, total matches: {len(matches)}")
