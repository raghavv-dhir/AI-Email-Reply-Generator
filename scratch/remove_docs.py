import os, re

d = 'backend/src/main/java'
p = re.compile(r'\s*/\*\*.*?\*/', re.DOTALL)

for r, _, fs in os.walk(d):
    for f in fs:
        if f.endswith('.java'):
            path = os.path.join(r, f)
            with open(path, 'r', encoding='utf-8') as file:
                c = file.read()
            new_c = p.sub('', c)
            if new_c != c:
                with open(path, 'w', encoding='utf-8') as file:
                    file.write(new_c)

