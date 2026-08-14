import xml.etree.ElementTree as ET

en_tree = ET.parse('app/src/main/res/values/strings.xml')
bn_tree = ET.parse('app/src/main/res/values-bn/strings.xml')

en_keys = {x.attrib['name'] for x in en_tree.getroot().findall('string')}
bn_keys = {x.attrib['name'] for x in bn_tree.getroot().findall('string')}

print("Total EN strings:", len(en_keys))
print("Total BN strings:", len(bn_keys))
missing_in_bn = sorted(list(en_keys - bn_keys))
missing_in_en = sorted(list(bn_keys - en_keys))

print("\nMissing in BN ({}):".format(len(missing_in_bn)))
for k in missing_in_bn:
    print("  -", k)

print("\nMissing in EN ({}):".format(len(missing_in_en)))
for k in missing_in_en:
    print("  -", k)
