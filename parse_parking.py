import json

existing_file = "app/src/main/assets/import_template.json"
try:
    with open(existing_file, "r", encoding="utf-8") as f:
        data = json.load(f)
except Exception:
    data = []

new_data = [
    # 固定停车 (from Image 1)
    {"dateStr": "2026-05-13", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2026-04-08", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2026-04-02", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2026-04-02", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2026-04-02", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-08-23", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-07-04", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-06-08", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-02-12", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-02-02", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2025-01-20", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2024-11-11", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2024-12-27", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},
    {"dateStr": "2024-12-11", "category": "停车", "amount": 100.0, "note": "固定期费1个月", "tag": "固定月租"},

    # 临时停车 (from Image 2)
    {"dateStr": "2026-07-07", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-07-19", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-17", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-16", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-14", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-09", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-12", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-10", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-09", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-05-08", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-28", "category": "停车", "amount": 14.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-28", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-21", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-20", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-20", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-21", "category": "停车", "amount": 10.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-04-18", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-02-18", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-02-13", "category": "停车", "amount": 15.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-02-12", "category": "停车", "amount": 4.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-01-31", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2026-01-04", "category": "停车", "amount": 6.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2025-12-06", "category": "停车", "amount": 20.0, "note": "临停缴费", "tag": "临时停车"},
    {"dateStr": "2025-12-06", "category": "停车", "amount": 5.0, "note": "临停缴费", "tag": "临时停车"}
]

seen = set()
deduped_data = []
for item in data + new_data:
    key = (item["dateStr"], item["category"], item.get("amount"), item.get("note"), item.get("tag"))
    if key not in seen:
        seen.add(key)
        deduped_data.append(item)

with open(existing_file, "w", encoding="utf-8") as f:
    json.dump(deduped_data, f, ensure_ascii=False, indent=2)

print("Updated template with", len(new_data), "new records. Total records:", len(deduped_data))
