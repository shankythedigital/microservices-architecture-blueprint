#!/usr/bin/env python3
"""
convert_excel_to_sql.py
Reads 'ALM Design Detailing B2C 0.2-2.xlsx' -> 'Asset Registration' sheet
and emits SQL INSERTs into V2__seed_from_excel.sql.
Usage:
    python3 convert_excel_to_sql.py /path/to/ALM\ Design\ Detailing\ B2C\ 0.2-2.xlsx
"""
import pandas as pd, sys, pathlib

if len(sys.argv)<2:
    print("Usage: python3 convert_excel_to_sql.py <excel_file>")
    sys.exit(1)

excel_path = pathlib.Path(sys.argv[1])
out_path = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/db/migration/V2__seed_from_excel.sql"

try:
    df = pd.read_excel(excel_path, sheet_name="Asset Registration")
except Exception as e:
    print("❌ Failed to open sheet:", e)
    sys.exit(2)

sql_lines=["-- Auto-generated from Asset Registration sheet",
           "DELETE FROM asset_master;",
           ""]

for i, row in df.iterrows():
    name = str(row.get("Asset Name","")).replace("'","''")
    status = str(row.get("Status","AVAILABLE"))
    purchase = str(row.get("Purchase Date","2024-01-01"))
    sql_lines.append(
        f"INSERT INTO asset_master (asset_name_udv, asset_status, purchase_date, created_date)"
        f" VALUES ('{name}','{status}','{purchase}',NOW());"
    )

out_path.write_text("\n".join(sql_lines))
print(f"✅ Wrote {len(df)} INSERTs to {out_path}")
