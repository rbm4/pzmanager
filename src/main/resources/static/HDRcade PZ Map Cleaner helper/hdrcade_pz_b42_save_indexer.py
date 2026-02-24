#!/usr/bin/env python3
"""
HDRcade PZ B42 Map Cleaner - Save Index Helper
----------------------------------------------
Creates save_index.json for fast loading in the web tool.

What it does:
- Scans the save root's map/ folder for coordinate .bin files
- Compresses them as per-folder Y ranges (massively smaller than listing 1.2M filenames)
- Extracts safehouse rectangles + owner/town from map_meta.bin (Build 42+)
- Writes save_index.json into the save root (same folder you point the helper at)

It does NOT delete anything.

B42 note:
- Zone filename is often map_zone.bin (singular) now; safehouse ownership/rects are in map_meta.bin.
"""

from __future__ import annotations
import sys, json, time, re, struct
from pathlib import Path
from typing import Dict, List, Tuple, Any, Optional

BIN_TILE_SIZE = 8  # folder/file indices correspond to world tiles / 8
TOWN_RE = re.compile(r".+, KY$")

def read_u16(b: bytes, i: int) -> Optional[int]:
    if i+2 > len(b): return None
    return struct.unpack(">H", b[i:i+2])[0]

def read_i32(b: bytes, i: int) -> Optional[int]:
    if i+4 > len(b): return None
    return struct.unpack(">i", b[i:i+4])[0]

def read_utf(b: bytes, i: int) -> Optional[Tuple[str,int]]:
    L = read_u16(b,i)
    if L is None: return None
    j=i+2
    if j+L > len(b): return None
    raw = b[j:j+L]
    s = raw.decode("utf-8", errors="ignore")
    return s, j+L

def is_reasonable_name(s: str) -> bool:
    if not (1 <= len(s) <= 40): return False
    if any(ord(c) < 32 for c in s): return False
    if not re.fullmatch(r"[A-Za-z0-9_\-\.\s]{1,40}", s): return False
    return s.strip() != ""

def scan_utf_strings(b: bytes, start: int, end: int, max_len: int = 120) -> List[Tuple[int,str]]:
    out: List[Tuple[int,str]] = []
    i = start
    while i + 2 < end:
        L = read_u16(b,i)
        if L is None: break
        if 0 < L <= max_len and i + 2 + L <= end:
            raw = b[i+2:i+2+L]
            printable = sum(1 for c in raw if 32 <= c < 127)
            if printable / max(1, L) > 0.85:
                s = raw.decode("utf-8", errors="ignore")
                if s.strip() and all(ord(c) >= 32 for c in s):
                    out.append((i, s))
        i += 1
    return out

def extract_safehouses_from_map_meta(meta_path: Path, warnings: List[str]) -> List[Dict[str,Any]]:
    if not meta_path.exists():
        warnings.append(f"map_meta.bin not found at {meta_path}")
        return []

    b = meta_path.read_bytes()

    # Signature:
    # [int x][int y][int w][int h][UTF owner][long][UTF owner]
    candidates: List[Tuple[int,int,int,int,int,str,int]] = []
    for i in range(0, len(b) - 48):
        x = read_i32(b,i); y = read_i32(b,i+4); w = read_i32(b,i+8); h = read_i32(b,i+12)
        if x is None: break
        if not (0 <= x <= 40000 and 0 <= y <= 40000): 
            continue
        if not (1 <= w <= 800 and 1 <= h <= 800):
            continue
        t = read_utf(b, i+16)
        if not t: 
            continue
        owner, p = t
        if not is_reasonable_name(owner):
            continue
        if p + 8 + 2 > len(b): 
            continue
        t2 = read_utf(b, p+8)
        if not t2:
            continue
        owner2, p2 = t2
        if owner2 != owner:
            continue
        candidates.append((i, x, y, w, h, owner, p2))

    if not candidates:
        warnings.append("No safehouse signature blocks found in map_meta.bin.")
        return []

    records: List[Dict[str,Any]] = []
    for idx, c in enumerate(candidates):
        off, x, y, w, h, owner, p2 = c
        next_off = candidates[idx+1][0] if idx+1 < len(candidates) else min(len(b), p2 + 2500)
        window_start = p2
        window_end = min(len(b), next_off)

        strs = scan_utf_strings(b, window_start, window_end, max_len=140)
        seen=set(); ordered=[]
        for pos,s in strs:
            if s in seen: continue
            seen.add(s)
            ordered.append((pos,s))

        town = ""
        town_pos = None
        for pos,s in ordered:
            if s == owner:
                continue
            if TOWN_RE.match(s):
                town = s
                town_pos = pos
                break

        if not town:
            continue

        name = ""
        if town_pos is not None:
            before = [(pos,s) for pos,s in ordered if pos < town_pos and s not in (owner, town)]
            before.sort(key=lambda t: abs(t[0]-town_pos))
            for pos,s in before:
                if town_pos - pos <= 140:
                    name = s
                    break
            if not name:
                after = [(pos,s) for pos,s in ordered if pos > town_pos and s not in (owner, town)]
                after.sort(key=lambda t: t[0])
                if after and after[0][0] - town_pos <= 220:
                    name = after[0][1]

        members=[]
        for pos,s in ordered:
            if s in (owner, town, name):
                continue
            if TOWN_RE.match(s):
                continue
            if is_reasonable_name(s):
                members.append(s)
            if town_pos is not None and pos > town_pos + 300:
                break

        mseen=set()
        members=[m for m in members if not (m in mseen or mseen.add(m))]

        records.append({
            "x": x, "y": y, "w": w, "h": h,
            "owner": owner,
            "town": town,
            "name": name,
            "members": members,
        })

    if not records:
        warnings.append("Safehouse candidates found, but no records passed town-validation.")
    return records

def compress_sorted_ints_to_ranges(vals: List[int]) -> List[List[int]]:
    if not vals: return []
    vals.sort()
    out=[]
    a=b=vals[0]
    for v in vals[1:]:
        if v == b + 1:
            b = v
        else:
            out.append([a,b])
            a=b=v
    out.append([a,b])
    return out

def scan_map_bins(save_root: Path, warnings: List[str]) -> Tuple[Dict[str,List[List[int]]], int]:
    map_dir = save_root / "map"
    if not map_dir.exists() or not map_dir.is_dir():
        warnings.append(f"map/ folder not found under save root: {map_dir}")
        return {}, 0

    bins_by_x: Dict[str, List[List[int]]] = {}
    total = 0

    for entry in map_dir.iterdir():
        if not entry.is_dir():
            continue
        if not entry.name.isdigit():
            continue
        bx = entry.name
        ys=[]
        try:
            for f in entry.iterdir():
                if not f.is_file():
                    continue
                if f.suffix.lower() != ".bin":
                    continue
                stem = f.stem
                if stem.isdigit():
                    ys.append(int(stem))
        except PermissionError:
            warnings.append(f"Permission error reading folder: {entry}")
            continue

        if ys:
            ranges = compress_sorted_ints_to_ranges(ys)
            bins_by_x[bx] = ranges
            for a,b in ranges:
                total += (b-a+1)

    return bins_by_x, total

def main():
    print("HDRcade Save Index Helper")
    print("------------------------")
    if len(sys.argv) >= 2:
        save_root = Path(sys.argv[1]).expanduser().resolve()
    else:
        save_root = Path(input("Paste SAVE ROOT path (contains map\\) then press ENTER: ").strip()).expanduser().resolve()

    warnings: List[str] = []
    if not save_root.exists():
        print(f"ERROR: Save root not found: {save_root}")
        sys.exit(1)

    t0=time.time()
    bins_by_x, total = scan_map_bins(save_root, warnings)
    t1=time.time()

    meta_path = save_root / "map_meta.bin"
    safehouses = extract_safehouses_from_map_meta(meta_path, warnings)
    t2=time.time()

    out = {
        "version": 1,
        "generated_at_utc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "save_root": str(save_root),
        "bin_tile_size": BIN_TILE_SIZE,
        "bins": total,
        "bins_by_x": bins_by_x,
        "safehouses": safehouses,
        "warnings": warnings,
        "timing_seconds": {
            "scan_map_bins": round(t1-t0, 3),
            "extract_safehouses": round(t2-t1, 3),
            "total": round(t2-t0, 3),
        }
    }

    out_path = save_root / "save_index.json"
    out_path.write_text(json.dumps(out, indent=2), encoding="utf-8")

    print(f"Save root: {save_root}")
    print(f"OK: wrote {out_path}")
    print(f"bins={total} safehouses={len(safehouses)} warnings={len(warnings)}")

if __name__ == "__main__":
    main()
