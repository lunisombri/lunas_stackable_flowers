from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

SRC_DIR = BASE_DIR / "base_models"
DST_DIR = Path("src/main/resources/assets/minecraft/models/block")

DST_DIR.mkdir(parents=True, exist_ok=True)


def rename_with_group_count(stem: str, new_count: int):
    # replaces _<number>_ with the new group count
    parts = stem.split("_")
    for i, part in enumerate(parts):
        if part.isdigit():
            parts[i] = str(new_count)
            break
    return "_".join(parts)

def split_top_level_blocks(array_text: str):
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
                blocks.append(array_text[start:i + 1])
                start = None

    return blocks


def extract_array(text: str, key: str):
    key_pos = text.index(f'"{key}"')
    start = text.index("[", key_pos)
    depth = 1
    i = start + 1

    while depth > 0:
        if text[i] == "[":
            depth += 1
        elif text[i] == "]":
            depth -= 1
        i += 1

    return text[start:i], start, i


for src_file in SRC_DIR.glob("*.json"):
    print("Processing:", src_file.name)

    original = src_file.read_text(encoding="utf-8")

    elements_text, e_start, e_end = extract_array(original, "elements")
    groups_text, g_start, g_end = extract_array(original, "groups")

    element_blocks = split_top_level_blocks(elements_text[1:-1])
    group_blocks = split_top_level_blocks(groups_text[1:-1])

    total_groups = len(group_blocks)

    dst_original = DST_DIR / src_file.name
    dst_original.write_text(original, encoding="utf-8")

    for keep in range(total_groups - 1, 1, -1):
        multiplier = 4 if "emissive" in src_file.name else 2
        kept_elements = element_blocks[: keep * multiplier]
        kept_groups = group_blocks[:keep]

        new_elements = "[\n\t\t" + ",\n\t\t".join(kept_elements) + "\n\t]"
        new_groups = "[\n\t\t" + ",\n\t\t".join(kept_groups) + "\n\t]"

        modified = (
                original[:e_start]
                + new_elements
                + original[e_end:g_start]
                + new_groups
                + original[g_end:]
        )

        new_stem = rename_with_group_count(src_file.stem, keep)
        dst_path = DST_DIR / f"{new_stem}.json"

        dst_path.write_text(modified, encoding="utf-8")


print("Done.")
