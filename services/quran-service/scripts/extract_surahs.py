import json
from pathlib import Path

chapters_path = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\d996d6e3-8bdc-43ff-9f9b-58b106cb46ef.txt"
)
ch = json.loads(chapters_path.read_text(encoding="utf-8"))
print("chapters", len(ch["chapters"]))
print(json.dumps(ch["chapters"][0], ensure_ascii=False, indent=2)[:800])

out = []
for c in ch["chapters"]:
    out.append(
        {
            "id": c["id"],
            "name_simple": c["name_simple"],
            "name_arabic": c["name_arabic"],
            "verses_count": c["verses_count"],
            "start_page": c["pages"][0],
            "end_page": c["pages"][1],
            "revelation_place": c.get("revelation_place", {}).get("name"),
        }
    )

dest = Path(
    r"C:\Users\oussema\OneDrive\Bureau\Oussama Projects\thabat\services\quran-service\scripts\surahs.json"
)
dest.parent.mkdir(parents=True, exist_ok=True)
dest.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
print("wrote", dest, "count", len(out))
print("first3", out[:3])
print("last3", out[-3:])
