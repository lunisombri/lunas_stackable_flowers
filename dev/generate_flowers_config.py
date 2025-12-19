import shutil
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

DIR_A_PREFIX = BASE_DIR / "placeholders/resources"
DIR_B_PREFIX = Path("src/main/resources")

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

DIR_B_PREFIX.mkdir(parents=True, exist_ok=True)

for flower in FLOWERS:
    print("Processing flower:", flower)
    for src_file in DIR_A_PREFIX.rglob(f"{PLACEHOLDER}*.json"):
        print("Processing file:", src_file)

        rel = src_file.relative_to(DIR_A_PREFIX)
        dest_name = src_file.name.replace(PLACEHOLDER, flower)
        dest_path = DIR_B_PREFIX / rel.parent / dest_name

        dest_path.parent.mkdir(parents=True, exist_ok=True)

        shutil.copyfile(src_file, dest_path)
        text = dest_path.read_text(encoding="utf-8")
        text = text.replace(PLACEHOLDER, flower)
        dest_path.write_text(text, encoding="utf-8")

print("Done.")