from pathlib import Path
import re
import shutil

BASE_DIR = Path(__file__).resolve().parent

MODELS_DIR = BASE_DIR / "models"
TMP_DIR = MODELS_DIR / "tmp"

# DST_DIR = Path("src/main/resources/assets/minecraft/models/block")
DST_DIR = Path("test")


TMP_DIR.mkdir(parents=True, exist_ok=True)
DST_DIR.mkdir(parents=True, exist_ok=True)

def extract_json_array_block_as_text(full_text: str, key: str):
    key_pos = full_text.index(f'"{key}"')
    array_start = full_text.index("[", key_pos)
    depth = 0
    i = array_start
    while True:
        if full_text[i] == "[":
            depth += 1
        elif full_text[i] == "]":
            depth -= 1
            if depth == 0:
                array_end = i + 1
                break
        i += 1
    array_text = full_text[array_start:array_end]

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
    return array_start, array_end, blocks


def make_emissive(block: str) -> str:
    # switch texture and add light emission
    block = block.replace('"#cross"', '"#cross_emissive"')
    block = block.replace(
        '"shade": false',
        '"shade": false,\n\t\t\t"light_emission": 15'
    )
    return block


def rewrite_group_children(group_block: str, layer_index: int) -> str:
    base = layer_index * 4
    children = f'"children": [{base}, {base+1}, {base+2}, {base+3}]'
    return re.sub(r'"children"\s*:\s*\[[^\]]*\]', children, group_block)


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


def rename_with_group_count(stem: str, new_count: int):
    parts = stem.split("_")
    for i, part in enumerate(parts):
        if part.isdigit():
            parts[i] = str(new_count)
            break
    return "_".join(parts)


def generate_base_max_stack_models():
    for src_file in MODELS_DIR.glob("*.json"):
        print("Copying master model:", src_file.name)

        original = src_file.read_text(encoding="utf-8")
        tmp_base_path = TMP_DIR / src_file.name
        tmp_base_path.write_text(original, encoding="utf-8")

        try:
            e_start, e_end, element_blocks = extract_json_array_block_as_text(original, "elements")
            g_start, g_end, group_blocks = extract_json_array_block_as_text(original, "groups")
        except Exception as exc:
            print("Skipping emissive generation for", src_file.name, ":", exc)
            continue

        total_groups = len(group_blocks)
        if total_groups * 2 != len(element_blocks):
            print("Unexpected layout, skipping emissive for", src_file.name)
            continue

        final_elements = []
        final_groups = []

        for i in range(total_groups):
            base_pair = element_blocks[i * 2 : i * 2 + 2]
            emissive_pair = [make_emissive(b) for b in base_pair]

            final_elements.extend(base_pair)
            final_elements.extend(emissive_pair)
            final_groups.append(rewrite_group_children(group_blocks[i], i))

        new_elements = "[\n\t\t" + ",\n\t\t".join(final_elements) + "\n\t]"
        new_groups = "[\n\t\t" + ",\n\t\t".join(final_groups) + "\n\t]"

        modified = (
                original[:e_start]
                + new_elements
                + original[e_end:g_start]
                + new_groups
                + original[g_end:]
        )

        emissive_name = src_file.name.replace("cross_", "cross_emissive_")
        tmp_emissive_path = TMP_DIR / emissive_name
        tmp_emissive_path.write_text(modified, encoding="utf-8")

        print("Created emissive model:", emissive_name)

def generate_all_stack_variations_based_on_max_stack_models():
    for src_file in TMP_DIR.glob("*.json"):
        print("Processing:", src_file.name)

        original = src_file.read_text(encoding="utf-8")

        try:
            elements_text, e_start, e_end = extract_array(original, "elements")
            groups_text, g_start, g_end = extract_array(original, "groups")
        except Exception as exc:
            print("Skipping variant generation for", src_file.name, ":", exc)
            continue

        element_blocks = split_top_level_blocks(elements_text[1:-1])
        group_blocks = split_top_level_blocks(groups_text[1:-1])

        total_groups = len(group_blocks)

        # write original (full stack) to destination
        (DST_DIR / src_file.name).write_text(original, encoding="utf-8")

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

generate_base_max_stack_models()
generate_all_stack_variations_based_on_max_stack_models()

# Step 3: cleanup TMP_DIR
print("Removing temporary models directory...")
shutil.rmtree(TMP_DIR)
print("Cleanup complete.")

print("Done.")