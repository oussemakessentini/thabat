import json
from pathlib import Path

p = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\61acc9e0-9ce3-48b9-b8fe-f3605df6cac9.txt"
)
d = json.loads(p.read_text(encoding="utf-8"))
hizbs = d["data"]["hizbs"]
print("keys", list(hizbs[0].keys()))
for i in (0, 1, 2, 58, 59):
    h = hizbs[i]
    nums = {k: v for k, v in h.items() if not isinstance(v, str)}
    print(i, nums)
