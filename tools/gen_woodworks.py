#!/usr/bin/env python3
"""Generates woodworks assets, data and the Java table from wood_table.json."""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
TABLE = json.load(open(os.path.join(ROOT, "tools/wood_table.json")))
CONDITIONS = [{"condition": "valence:module_enabled", "module": "woodworks"}]


def write(path, obj):
    path = os.path.join(RES, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def merge_tag(path, values):
    full = os.path.join(RES, path)
    existing = json.load(open(full))["values"] if os.path.exists(full) else []
    known = {e["id"] if isinstance(e, dict) else e for e in existing}
    existing += [{"id": v, "required": False} for v in values if v not in known]
    write(path, {"replace": False, "values": existing})


def self_loot(vid):
    return {
        "type": "minecraft:block",
        "pools": [{
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": f"valence:{vid}"}],
            "rolls": 1.0,
        }],
        "random_sequence": f"valence:blocks/{vid}",
    }


def bookshelf_loot(vid):
    return {
        "type": "minecraft:block",
        "pools": [{
            "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {
                        "type": "minecraft:item",
                        "conditions": [{
                            "condition": "minecraft:match_tool",
                            "predicate": {"predicates": {"minecraft:enchantments": [
                                {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}},
                        }],
                        "name": f"valence:{vid}",
                    },
                    {
                        "type": "minecraft:item",
                        "functions": [
                            {"count": 3.0, "function": "minecraft:set_count"},
                            {"function": "minecraft:explosion_decay"},
                        ],
                        "name": "minecraft:book",
                    },
                ],
            }],
            "rolls": 1.0,
        }],
        "random_sequence": f"valence:blocks/{vid}",
    }


def title(name):
    return " ".join(w.capitalize() for w in name.split("_"))


def main():
    lang, mineable = {}, []
    climbable, shelves, c_shelves, chest_tags = [], [], [], []

    for wood in TABLE["woods"]:
        name = wood["name"]

        if name != "oak":
            vid = f"{name}_ladder"
            write(f"assets/valence/blockstates/{vid}.json", {"variants": {
                "facing=north": {"model": "minecraft:block/ladder"},
                "facing=east": {"model": "minecraft:block/ladder", "y": 90},
                "facing=south": {"model": "minecraft:block/ladder", "y": 180},
                "facing=west": {"model": "minecraft:block/ladder", "y": 270},
            }})
            write(f"assets/valence/items/{vid}.json", {"model": {"type": "minecraft:model", "model": "minecraft:item/ladder"}})
            write(f"data/valence/loot_table/blocks/{vid}.json", self_loot(vid))
            write(f"data/valence/recipe/{vid}.json", {
                "type": "minecraft:crafting_shaped",
                "group": "wooden_ladder",
                "key": {"#": "minecraft:stick", "P": wood["planks_block"]},
                "pattern": ["# #", "#P#", "# #"],
                "result": {"count": 4, "id": f"valence:{vid}"},
                "fabric:load_conditions": CONDITIONS,
            })
            lang[f"block.valence.{vid}"] = f"{title(name)} Ladder"
            climbable.append(f"valence:{vid}")
            mineable.append(f"valence:{vid}")

            vid = f"{name}_bookshelf"
            write(f"assets/valence/blockstates/{vid}.json", {"variants": {"": {"model": f"valence:block/{vid}"}}})
            write(f"assets/valence/models/block/{vid}.json", {
                "parent": "minecraft:block/cube_column",
                "textures": {"end": wood["planks"], "side": "minecraft:block/bookshelf"},
            })
            write(f"assets/valence/items/{vid}.json", {"model": {"type": "minecraft:model", "model": f"valence:block/{vid}"}})
            write(f"data/valence/loot_table/blocks/{vid}.json", bookshelf_loot(vid))
            write(f"data/valence/recipe/{vid}.json", {
                "type": "minecraft:crafting_shaped",
                "category": "building",
                "key": {"#": wood["planks_block"], "X": "minecraft:book"},
                "pattern": ["###", "XXX", "###"],
                "result": {"id": f"valence:{vid}"},
                "fabric:load_conditions": CONDITIONS,
            })
            lang[f"block.valence.{vid}"] = f"{title(name)} Bookshelf"
            shelves.append(f"valence:{vid}")
            c_shelves.append(f"valence:{vid}")
            mineable.append(f"valence:{vid}")

            vid = f"{name}_chest"
            write(f"assets/valence/blockstates/{vid}.json", {"variants": {"": {"model": f"valence:block/{vid}"}}})
            write(f"assets/valence/models/block/{vid}.json", {"textures": {"particle": wood["planks"]}})
            write(f"assets/valence/items/{vid}.json", {"model": {
                "type": "minecraft:special",
                "base": "minecraft:item/chest",
                "model": {"type": "minecraft:chest", "texture": "minecraft:normal"},
            }})
            write(f"data/valence/loot_table/blocks/{vid}.json", self_loot(vid))
            write(f"data/valence/recipe/{vid}.json", {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "ingredients": ["minecraft:chest", wood["planks_block"]],
                "result": {"id": f"valence:{vid}"},
                "fabric:load_conditions": CONDITIONS,
            })
            lang[f"block.valence.{vid}"] = f"{title(name)} Chest"
            chest_tags.append(f"valence:{vid}")
            mineable.append(f"valence:{vid}")

        if wood["log_block"]:
            vid = f"{name}_post"
            write(f"assets/valence/blockstates/{vid}.json", {"variants": {
                "axis=y": {"model": f"valence:block/{vid}"},
                "axis=x": {"model": f"valence:block/{vid}", "x": 90, "y": 90},
                "axis=z": {"model": f"valence:block/{vid}", "x": 90},
            }})
            write(f"assets/valence/models/block/{vid}.json", {
                "parent": "valence:block/post",
                "textures": {"side": wood["log_side"], "top": wood["log_top"]},
            })
            write(f"assets/valence/items/{vid}.json", {"model": {"type": "minecraft:model", "model": f"valence:block/{vid}"}})
            write(f"data/valence/loot_table/blocks/{vid}.json", self_loot(vid))
            write(f"data/valence/recipe/{vid}.json", {
                "type": "minecraft:crafting_shaped",
                "group": "wooden_post",
                "key": {"#": wood["log_block"]},
                "pattern": ["#", "#", "#"],
                "result": {"count": 8, "id": f"valence:{vid}"},
                "fabric:load_conditions": CONDITIONS,
            })
            lang[f"block.valence.{vid}"] = f"{title(name)} Post"
            mineable.append(f"valence:{vid}")

    merge_tag("data/minecraft/tags/block/mineable/axe.json", mineable)
    merge_tag("data/minecraft/tags/block/climbable.json", climbable)
    merge_tag("data/minecraft/tags/block/enchantment_power_provider.json", shelves)
    merge_tag("data/minecraft/tags/block/features_cannot_replace.json", chest_tags)
    merge_tag("data/minecraft/tags/block/guarded_by_piglins.json", chest_tags)
    merge_tag("data/c/tags/block/bookshelves.json", c_shelves)
    merge_tag("data/c/tags/item/bookshelves.json", c_shelves)
    merge_tag("data/c/tags/block/chests.json", chest_tags)
    merge_tag("data/c/tags/item/chests.json", chest_tags)
    merge_tag("data/c/tags/block/chests/wooden.json", chest_tags)
    merge_tag("data/c/tags/item/chests/wooden.json", chest_tags)

    lang_path = os.path.join(RES, "assets/valence/lang/en_us.json")
    existing = json.load(open(lang_path)) if os.path.exists(lang_path) else {}
    existing.update(lang)
    write("assets/valence/lang/en_us.json", dict(sorted(existing.items())))

    java = os.path.join(ROOT, "src/main/java/com/chaevsfe/valence/modules/woodworks/WoodworksTable.java")
    with open(java, "w") as f:
        f.write("package com.chaevsfe.valence.modules.woodworks;\n\n")
        f.write("import java.util.List;\n\n")
        f.write("// Generated by tools/gen_woodworks.py. Do not edit.\n")
        f.write("// Families absent from the running version's registry are skipped at registration.\n")
        f.write("public final class WoodworksTable\n{\n")
        f.write("    public record Row (String name, String planksBlock, String logBlock, boolean stem) { }\n\n")
        f.write("    public static final List<Row> ROWS = List.of(\n")
        rows = []
        for wood in TABLE["woods"]:
            log = f'"{wood["log_block"]}"' if wood["log_block"] else "null"
            stem = "true" if wood["stem"] else "false"
            rows.append(f'        new Row("{wood["name"]}", "{wood["planks_block"]}", {log}, {stem})')
        f.write(",\n".join(rows) + ");\n\n")
        f.write("    private WoodworksTable () { }\n}\n")

    print(f"{len(TABLE['woods'])} families generated")


if __name__ == "__main__":
    main()
