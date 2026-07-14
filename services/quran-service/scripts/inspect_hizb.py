import json
from pathlib import Path

p = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\61acc9e0-9ce3-48b9-b8fe-f3605df6cac9.txt"
)
d = json.loads(p.read_text(encoding="utf-8"))
data = d["data"]
print(type(data), list(data.keys()) if isinstance(data, dict) else len(data))
if isinstance(data, dict):
    for k, v in data.items():
        print("key", k, "type", type(v), "len", len(v) if hasattr(v, "__len__") else v)
        if isinstance(v, list) and v:
            print(" sample0", {kk: vv for kk, vv in v[0].items() if not isinstance(vv, str) or ord(vv[0]) < 128})
elif isinstance(data, list):
    print("len", len(data))
    print(data[0])
