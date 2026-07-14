import json
from pathlib import Path
import urllib.request

chapters_path = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\d996d6e3-8bdc-43ff-9f9b-58b106cb46ef.txt"
)
ch = json.loads(chapters_path.read_text(encoding="utf-8"))

out = []
for c in ch["chapters"]:
    revelation = c.get("revelation_place")
    if isinstance(revelation, dict):
        revelation = revelation.get("name")
    out.append(
        {
            "id": c["id"],
            "name_simple": c["name_simple"],
            "name_arabic": c["name_arabic"],
            "verses_count": c["verses_count"],
            "start_page": c["pages"][0],
            "end_page": c["pages"][1],
            "revelation_place": revelation,
        }
    )

dest = Path(__file__).resolve().parent / "surahs.json"
dest.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
print("wrote surahs", len(out))

req = urllib.request.Request(
    "https://api.qurani.ai/gw/qh/v1/hizb/metadata",
    headers={"User-Agent": "thabat-quran-service/1.0", "Accept": "application/json"},
)
with urllib.request.urlopen(req, timeout=60) as resp:
    hizb_payload = json.loads(resp.read().decode("utf-8"))

hizbs = hizb_payload.get("data") or hizb_payload
hizb_dest = Path(__file__).resolve().parent / "hizbs.json"
hizb_dest.write_text(json.dumps(hizbs, ensure_ascii=False, indent=2), encoding="utf-8")
print("wrote hizbs", len(hizbs))
for i in (0, 1, 29, 58, 59):
    h = hizbs[i]
    print(
        "hizb",
        h.get("number"),
        "pages",
        h.get("firstPage"),
        "-",
        h.get("lastPage"),
    )
