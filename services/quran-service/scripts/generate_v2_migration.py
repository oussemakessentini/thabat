"""Rebuild V2 Flyway migration with accurate Madani Mushaf reference data.

Sources:
- Surah metadata: quran-qcf4 index.json
- Page/ayah mapping: quran-qcf4 verses.json (verse_key -> page)
- Juz starts: King Fahd Complex Madani Mushaf
- Hizb starts: Qurani.ai hizb metadata firstPage values
"""

from __future__ import annotations

import json
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT.parent / "src" / "main" / "resources" / "db" / "migration"

INDEX_PATH = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\f83f8436-6062-4c30-8358-76642eca4725.txt"
)
VERSES_PATH = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\8c979f18-f351-440b-91ad-bcb19edfd157.txt"
)
HIZB_PATH = Path(
    r"C:\Users\oussema\.cursor\projects\c-Users-oussema-OneDrive-Bureau-Oussama-Projects-thabat\agent-tools\61acc9e0-9ce3-48b9-b8fe-f3605df6cac9.txt"
)

JUZ_STARTS = [
    1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
    201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
    402, 422, 442, 462, 482, 502, 522, 542, 562, 582,
]


def sql_str(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def main() -> None:
    index = json.loads(INDEX_PATH.read_text(encoding="utf-8"))
    chapters = index["chapters"]
    assert len(chapters) == 114

    verses = json.loads(VERSES_PATH.read_text(encoding="utf-8"))
    assert len(verses) == 6236

    hizb_payload = json.loads(HIZB_PATH.read_text(encoding="utf-8"))
    hizbs_raw = sorted(hizb_payload["data"]["hizbs"], key=lambda h: h["number"])
    assert len(hizbs_raw) == 60

    # Aggregate ayah ranges per (page, surah) from verse keys.
    ranges: dict[tuple[int, int], list[int]] = defaultdict(list)
    for verse_key, meta in verses.items():
        surah_str, ayah_str = verse_key.split(":")
        surah_number = int(surah_str)
        ayah_number = int(ayah_str)
        page_number = int(meta["page"])
        ranges[(page_number, surah_number)].append(ayah_number)

    page_surah_rows = []
    for (page_number, surah_number), ayahs in sorted(ranges.items()):
        page_surah_rows.append(
            {
                "page_number": page_number,
                "surah_number": surah_number,
                "start_ayah": min(ayahs),
                "end_ayah": max(ayahs),
            }
        )

    juz_rows = []
    for i, start in enumerate(JUZ_STARTS, start=1):
        end = (JUZ_STARTS[i] - 1) if i < len(JUZ_STARTS) else 604
        juz_rows.append((i, start, end))

    page_to_juz = {}
    for juz_number, start, end in juz_rows:
        for page in range(start, end + 1):
            page_to_juz[page] = juz_number

    hizb_starts = [h["firstPage"] for h in hizbs_raw]
    hizb_rows = []
    for index, h in enumerate(hizbs_raw):
        number = h["number"]
        start = h["firstPage"]
        end = (hizb_starts[index + 1] - 1) if index + 1 < len(hizb_starts) else 604
        hizb_rows.append((number, page_to_juz[start], start, end))

    page_to_hizb = {}
    for number, _juz, start, end in hizb_rows:
        for page in range(start, end + 1):
            page_to_hizb[page] = number

    missing_pages = [p for p in range(1, 605) if p not in page_to_juz or p not in page_to_hizb]
    if missing_pages:
        raise SystemExit(f"Missing juz/hizb coverage: {missing_pages[:10]}")

    surah_pages: dict[int, list[int]] = {c["id"]: [] for c in chapters}
    for row in page_surah_rows:
        surah_pages[row["surah_number"]].append(row["page_number"])

    surah_rows = []
    for c in chapters:
        pages = sorted(set(surah_pages[c["id"]]))
        revelation = "MECCAN" if c["revelation_place"] == "makkah" else "MEDINAN"
        surah_rows.append(
            {
                "surah_number": c["id"],
                "name_arabic": c["name_arabic"],
                "name_english": c["translated_name"],
                "transliteration": c["name"],
                "ayah_count": c["verses_count"],
                "revelation_type": revelation,
                "start_page": pages[0],
                "end_page": pages[-1],
            }
        )

    lines: list[str] = []
    lines.append("-- Madani Mushaf (604-page) reference data")
    lines.append("-- Sources: quran-qcf4 index/verses (Madinah Mushaf layout) + standard juz/hizb page markers")
    lines.append("-- User progress source of truth remains quran_page_progress.")
    lines.append("-- Surah progress is page-based for MVP (not ayah-weighted).")
    lines.append("")

    lines.append("""CREATE TABLE quran_surah (
    surah_number INT PRIMARY KEY,
    name_arabic VARCHAR(100) NOT NULL,
    name_english VARCHAR(120) NOT NULL,
    transliteration VARCHAR(120) NOT NULL,
    ayah_count INT NOT NULL,
    revelation_type VARCHAR(16) NOT NULL,
    start_page INT NOT NULL,
    end_page INT NOT NULL,
    CONSTRAINT chk_quran_surah_number CHECK (surah_number BETWEEN 1 AND 114),
    CONSTRAINT chk_quran_surah_revelation CHECK (revelation_type IN ('MECCAN', 'MEDINAN')),
    CONSTRAINT chk_quran_surah_pages CHECK (
        start_page BETWEEN 1 AND 604
        AND end_page BETWEEN 1 AND 604
        AND start_page <= end_page
    )
);""")

    lines.append("""CREATE TABLE quran_juz (
    juz_number INT PRIMARY KEY,
    start_page INT NOT NULL,
    end_page INT NOT NULL,
    CONSTRAINT chk_quran_juz_number CHECK (juz_number BETWEEN 1 AND 30),
    CONSTRAINT chk_quran_juz_pages CHECK (
        start_page BETWEEN 1 AND 604
        AND end_page BETWEEN 1 AND 604
        AND start_page <= end_page
    )
);""")

    lines.append("""CREATE TABLE quran_hizb (
    hizb_number INT PRIMARY KEY,
    juz_number INT NOT NULL REFERENCES quran_juz (juz_number),
    start_page INT NOT NULL,
    end_page INT NOT NULL,
    CONSTRAINT chk_quran_hizb_number CHECK (hizb_number BETWEEN 1 AND 60),
    CONSTRAINT chk_quran_hizb_pages CHECK (
        start_page BETWEEN 1 AND 604
        AND end_page BETWEEN 1 AND 604
        AND start_page <= end_page
    )
);""")

    lines.append("""CREATE TABLE quran_page_ref (
    page_number INT PRIMARY KEY,
    juz_number INT NOT NULL REFERENCES quran_juz (juz_number),
    hizb_number INT NOT NULL REFERENCES quran_hizb (hizb_number),
    CONSTRAINT chk_quran_page_ref_number CHECK (page_number BETWEEN 1 AND 604)
);""")

    lines.append("""CREATE TABLE quran_page_surah_range (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    page_number INT NOT NULL REFERENCES quran_page_ref (page_number),
    surah_number INT NOT NULL REFERENCES quran_surah (surah_number),
    start_ayah INT NOT NULL,
    end_ayah INT NOT NULL,
    CONSTRAINT uq_quran_page_surah_range UNIQUE (page_number, surah_number),
    CONSTRAINT chk_quran_page_surah_ayah CHECK (
        start_ayah >= 1
        AND end_ayah >= start_ayah
    )
);""")

    lines.append(
        "CREATE INDEX idx_quran_page_surah_range_surah ON quran_page_surah_range (surah_number);"
    )
    lines.append("CREATE INDEX idx_quran_page_ref_juz ON quran_page_ref (juz_number);")
    lines.append("CREATE INDEX idx_quran_page_ref_hizb ON quran_page_ref (hizb_number);")
    lines.append("")

    lines.append(
        "INSERT INTO quran_surah "
        "(surah_number, name_arabic, name_english, transliteration, ayah_count, "
        "revelation_type, start_page, end_page) VALUES"
    )
    lines.append(
        ",\n".join(
            f"({s['surah_number']}, {sql_str(s['name_arabic'])}, {sql_str(s['name_english'])}, "
            f"{sql_str(s['transliteration'])}, {s['ayah_count']}, '{s['revelation_type']}', "
            f"{s['start_page']}, {s['end_page']})"
            for s in surah_rows
        )
        + ";"
    )
    lines.append("")

    lines.append("INSERT INTO quran_juz (juz_number, start_page, end_page) VALUES")
    lines.append(",\n".join(f"({n}, {s}, {e})" for n, s, e in juz_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO quran_hizb (hizb_number, juz_number, start_page, end_page) VALUES"
    )
    lines.append(",\n".join(f"({n}, {j}, {s}, {e})" for n, j, s, e in hizb_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO quran_page_ref (page_number, juz_number, hizb_number) VALUES"
    )
    lines.append(
        ",\n".join(
            f"({page}, {page_to_juz[page]}, {page_to_hizb[page]})"
            for page in range(1, 605)
        )
        + ";"
    )
    lines.append("")

    lines.append(
        "INSERT INTO quran_page_surah_range "
        "(page_number, surah_number, start_ayah, end_ayah) VALUES"
    )
    lines.append(
        ",\n".join(
            f"({r['page_number']}, {r['surah_number']}, {r['start_ayah']}, {r['end_ayah']})"
            for r in page_surah_rows
        )
        + ";"
    )
    lines.append("")

    migration = OUT / "V2__quran_reference_data.sql"
    migration.write_text("\n".join(lines) + "\n", encoding="utf-8")

    (ROOT / "page_surah_ranges.json").write_text(
        json.dumps(page_surah_rows, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (ROOT / "surahs.json").write_text(
        json.dumps(surah_rows, ensure_ascii=False, indent=2), encoding="utf-8"
    )

    print("wrote", migration)
    print(
        "counts surahs=%s juz=%s hizbs=%s page_surah=%s"
        % (len(surah_rows), len(juz_rows), len(hizb_rows), len(page_surah_rows))
    )
    print("fatiha", page_surah_rows[0])
    print("baqarah page2", next(r for r in page_surah_rows if r["page_number"] == 2))


if __name__ == "__main__":
    main()
