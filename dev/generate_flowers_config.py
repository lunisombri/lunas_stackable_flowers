import shutil
from pathlib import Path
import re

BASE_DIR = Path(__file__).resolve().parent

SRC_DIR = BASE_DIR / "placeholders/resources"
DST_DIR = Path("src/main/resources")

PLACEHOLDER = "placeholder_flower_name"

FLOWERS = [
    "dandelion",
    "poppy",
    "blue_orchid",
    "allium",
    "azure_bluet",
    "red_tulip",
    "orange_tulip",
    "white_tulip",
    "pink_tulip",
    "oxeye_daisy",
    "cornflower",
    "lily_of_the_valley",
    "wither_rose",
    "torchflower",
    "closed_eyeblossom",
]

EMISSIVE_FLOWERS = [
    "open_eyeblossom",
]

DST_DIR.mkdir(parents=True, exist_ok=True)

def get_destination_file_path(template_file: Path, flower: str) -> Path:
    template_relative_path = (template_file.relative_to(SRC_DIR))
    destination_filename = (template_file.name.replace(PLACEHOLDER, flower))
    destination_path = DST_DIR / template_relative_path.parent / destination_filename
    destination_path.parent.mkdir(parents=True, exist_ok=True)
    return destination_path

def add_emissive_textures(text: str, destination_path: Path, flower: str) -> str:
    text = text.replace("lunas_flower_patch_cross_", "lunas_flower_patch_cross_emissive_")

    emissive_suffix = (
        f"{flower}_emissive_sheared"
        if "sheared" in destination_path.name
        else f"{flower}_emissive"
    )

    def add_emissive_texture(match):
        block = match.group(0)
        m = re.search(r'(^\s*)"cross"\s*:\s*"[^"]+"', block, flags=re.MULTILINE)
        if m:
            indent = m.group(1)
            block = re.sub(
                r'("cross"\s*:\s*"[^"]+")',
                r'\1,\n{}"cross_emissive": "minecraft:block/{}"'.format(
                    indent,
                    emissive_suffix
                ),
                block
            )
        return block

    return re.sub(r'"textures"\s*:\s*{[^}]*}', add_emissive_texture, text, flags=re.MULTILINE)

def replace_flower_in_destination_file(destination_path: Path, flower: str, emissive: bool = False):
    text = destination_path.read_text(encoding="utf-8")
    text = text.replace(PLACEHOLDER, flower)
    if emissive:
        text = add_emissive_textures(text, destination_path, flower)
    destination_path.write_text(text, encoding="utf-8")


def process_template_files(flower: str, emissive: bool = False) -> None:
    for template_file in SRC_DIR.rglob(f"{PLACEHOLDER}*.json"):
        print("Processing file:", template_file.name)
        destination_path = get_destination_file_path(template_file, flower)
        shutil.copyfile(template_file, destination_path)
        replace_flower_in_destination_file(destination_path, flower, emissive=emissive)

def process_all_flowers():
    for flower in FLOWERS:
        print("Processing flower:", flower)
        process_template_files(flower)

        for flower in EMISSIVE_FLOWERS:
            print("Processing emissive flower:", flower)
            process_template_files(flower, emissive=True)


process_all_flowers()
print("Done.")