import json
from pathlib import Path
from collections import defaultdict

p = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\8c979f18-f351-440b-91ad-bcb19edfd157.txt"
)
data = json.loads(p.read_text(encoding="utf-8"))
print(type(data))
if isinstance(data, dict):
    print("keys sample", list(data.keys())[:5])
    first = next(iter(data.values()))
    print("first value type", type(first), first if not isinstance(first, (dict, list)) else list(first.keys()) if isinstance(first, dict) else first[:1])
elif isinstance(data, list):
    print("len", len(data), "sample", data[0])
