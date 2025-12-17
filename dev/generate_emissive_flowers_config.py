from pathlib import Path
import re

BASE_DIR = Path(__file__).resolve().parent
SRC_DIR = BASE_DIR / "placeholders/resources"
DST_DIR = Path("src/main/resources")

FLOWERS = [
    "open_eyeblossom",
]

DST_DIR.mkdir(parents=True, exist_ok=True)

for flower in FLOWERS:
    print("Processing flower:", flower)

    for src_file in SRC_DIR.rglob("*.json"):
        print("  Processing file:", src_file.name)

        text = src_file.read_text(encoding="utf-8")

        text = text.replace("lunas_flower_patch_cross_", "lunas_flower_patch_cross_emissive_")
        text = text.replace("placeholder_flower_name", flower)

        def add_emissive_texture(match):
            block = match.group(0)
            m = re.search(r'(^\s*)"cross"\s*:\s*"[^"]+"', block, flags=re.MULTILINE)
            if m:
                indent = m.group(1)
                block = re.sub(
                    r'("cross"\s*:\s*"[^"]+")',
                    r'\1,\n{}"cross_emissive": "minecraft:block/{}_emissive"'.format(indent, flower),
                    block
                )
            return block

        text = re.sub(r'"textures"\s*:\s*{[^}]*}', add_emissive_texture, text, flags=re.MULTILINE)

        rel_path = src_file.relative_to(SRC_DIR)
        dest_path = DST_DIR / rel_path.parent / src_file.name.replace("placeholder_flower_name", flower)
        dest_path.parent.mkdir(parents=True, exist_ok=True)

        dest_path.write_text(text, encoding="utf-8")

print("Done.")
