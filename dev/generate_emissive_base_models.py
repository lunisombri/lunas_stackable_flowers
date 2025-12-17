import re
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

SRC_DIR = BASE_DIR / "base_models"
DST_DIR = Path("dev/base_models")

BASE_TEXTURE = '"#cross"'
EMISSIVE_TEXTURE = '"#cross_emissive"'

DST_DIR.mkdir(parents=True, exist_ok=True)

def extract_array_block(text: str, key: str):
    key_pos = text.index(f'"{key}"')
    array_start = text.index("[", key_pos)
    depth = 0
    i = array_start

    while True:
        if text[i] == "[":
            depth += 1
        elif text[i] == "]":
            depth -= 1
            if depth == 0:
                array_end = i + 1
                break
        i += 1

    array_text = text[array_start:array_end]

    blocks = []
    depth = 0
    start = None

    for i, ch in enumerate(array_text):
        if ch == "{":
            if depth == 0:
                start = i
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0 and start is not None:
                blocks.append(array_text[start:i+1])
                start = None

    return array_start, array_end, blocks


def make_emissive(block: str) -> str:
    block = block.replace('"#cross"', '"#cross_emissive"')

    block = block.replace(
        '"shade": false',
        '"shade": false,\n\t\t\t"light_emission": 15'
    )

    return block

def rewrite_group_children(group_block: str, layer_index: int) -> str:
    base = layer_index * 4
    children = f'"children": [{base}, {base+1}, {base+2}, {base+3}]'

    return re.sub(
        r'"children"\s*:\s*\[[^\]]*\]',
        children,
        group_block
    )

for src_file in SRC_DIR.glob("*.json"):
    print("Processing emissive base model:", src_file.name)

    original = src_file.read_text(encoding="utf-8")

    e_start, e_end, element_blocks = extract_array_block(original, "elements")
    g_start, g_end, group_blocks = extract_array_block(original, "groups")

    total_groups = len(group_blocks)
    assert total_groups * 2 == len(element_blocks), "Unexpected element/group layout"

    final_elements = []
    final_groups = []

    for i in range(total_groups):
        base_pair = element_blocks[i*2 : i*2 + 2]
        emissive_pair = [make_emissive(b) for b in base_pair]

        final_elements.extend(base_pair)
        final_elements.extend(emissive_pair)

        final_groups.append(
            rewrite_group_children(group_blocks[i], i)
        )

    new_elements = "[\n\t\t" + ",\n\t\t".join(final_elements) + "\n\t]"
    new_groups = "[\n\t\t" + ",\n\t\t".join(final_groups) + "\n\t]"

    modified = (
            original[:e_start]
            + new_elements
            + original[e_end:g_start]
            + new_groups
            + original[g_end:]
    )

    dst_name = src_file.name.replace("cross_", "cross_emissive_")
    dst_path = DST_DIR / dst_name
    dst_path.write_text(modified, encoding="utf-8")

print("Done.")
