#!/usr/bin/env python3
"""Generates vertical slab assets, data and the Java registration table from slab_table.json."""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
TABLE = json.load(open(os.path.join(ROOT, "tools/slab_table.json")))
NEW_IN_26_2 = {"cinnabar_slab", "cinnabar_brick_slab", "polished_cinnabar_slab",
               "sulfur_slab", "sulfur_brick_slab", "polished_sulfur_slab"}
MODULE_CONDITION = {"condition": "valence:module_enabled", "module": "vertical_slabs"}


def write(path, obj):
    path = os.path.join(RES, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2, sort_keys=False)
        f.write("\n")


def conditions(slab):
    conds = [MODULE_CONDITION]
    if slab["id"] in NEW_IN_26_2:
        conds.append({"condition": "fabric:registry_contains", "values": ["minecraft:" + slab["id"]]})
    return conds


def gen(slab):
    base = slab["id"].removesuffix("_slab")
    vid = base + "_vertical_slab"

    write(f"assets/valence/blockstates/{vid}.json", {"variants": {
        "facing=north,double=false": {"model": f"valence:block/{vid}"},
        "facing=east,double=false": {"model": f"valence:block/{vid}", "y": 90},
        "facing=south,double=false": {"model": f"valence:block/{vid}", "y": 180},
        "facing=west,double=false": {"model": f"valence:block/{vid}", "y": 270},
        "facing=north,double=true": {"model": slab["double_model"]},
        "facing=east,double=true": {"model": slab["double_model"]},
        "facing=south,double=true": {"model": slab["double_model"]},
        "facing=west,double=true": {"model": slab["double_model"]},
    }})

    write(f"assets/valence/models/block/{vid}.json", {
        "parent": "valence:block/vertical_slab",
        "textures": slab["textures"],
    })

    write(f"assets/valence/models/block/{vid}_item.json", {
        "parent": "valence:block/vertical_slab_item",
        "textures": slab["textures"],
    })

    write(f"assets/valence/items/{vid}.json", {
        "model": {"type": "minecraft:model", "model": f"valence:block/{vid}_item"},
    })

    write(f"data/valence/loot_table/blocks/{vid}.json", {
        "type": "minecraft:block",
        "pools": [{
            "entries": [{
                "type": "minecraft:item",
                "functions": [
                    {"conditions": [{
                        "block": f"valence:{vid}",
                        "condition": "minecraft:block_state_property",
                        "properties": {"double": "true"},
                    }], "count": 2.0, "function": "minecraft:set_count"},
                    {"function": "minecraft:explosion_decay"},
                ],
                "name": f"valence:{vid}",
            }],
            "rolls": 1.0,
        }],
        "random_sequence": f"valence:blocks/{vid}",
    })

    column = {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "key": {"#": "minecraft:" + slab["id"]},
        "pattern": ["#", "#", "#"],
        "result": {"count": 3, "id": f"valence:{vid}"},
        "fabric:load_conditions": conditions(slab),
    }
    if slab["mineable"] == "axe":
        column["group"] = "wooden_vertical_slab"
    write(f"data/valence/recipe/{vid}.json", column)

    write(f"data/valence/recipe/{vid}_from_slab.json", {
        "type": "minecraft:crafting_shapeless",
        "category": "building",
        "ingredients": ["minecraft:" + slab["id"]],
        "result": {"id": f"valence:{vid}"},
        "fabric:load_conditions": conditions(slab),
    })

    write(f"data/valence/recipe/{base}_slab_from_vertical.json", {
        "type": "minecraft:crafting_shapeless",
        "category": "building",
        "ingredients": [f"valence:{vid}"],
        "result": {"id": "minecraft:" + slab["id"]},
        "fabric:load_conditions": conditions(slab),
    })

    if slab["mineable"] == "pickaxe" and slab["parent"]:
        parent_path = slab["parent"].split(":", 1)[1]
        write(f"data/valence/recipe/{vid}_from_{parent_path}_stonecutting.json", {
            "type": "minecraft:stonecutting",
            "ingredient": slab["parent"],
            "result": {"count": 2, "id": f"valence:{vid}"},
            "fabric:load_conditions": conditions(slab),
        })

    return base, vid


def main():
    rows, names, mineable = [], {}, {"axe": [], "pickaxe": []}
    for slab in TABLE["slabs"]:
        base, vid = gen(slab)
        rows.append((base, slab["id"], slab["id"] in NEW_IN_26_2))
        names[f"block.valence.{vid}"] = " ".join(w.capitalize() for w in base.split("_")) + " Vertical Slab"
        mineable[slab["mineable"]].append(f"valence:{vid}")

    for tool, values in mineable.items():
        path = os.path.join(RES, f"data/minecraft/tags/block/mineable/{tool}.json")
        existing = json.load(open(path))["values"] if os.path.exists(path) else []
        known = {e["id"] if isinstance(e, dict) else e for e in existing}
        existing += [{"id": v, "required": False} for v in values if v not in known]
        write(f"data/minecraft/tags/block/mineable/{tool}.json", {"replace": False, "values": existing})

    lang_path = os.path.join(RES, "assets/valence/lang/en_us.json")
    lang = json.load(open(lang_path)) if os.path.exists(lang_path) else {}
    lang.update(names)
    write("assets/valence/lang/en_us.json", dict(sorted(lang.items())))

    java = os.path.join(ROOT, "src/main/java/com/chaevsfe/valence/modules/verticalslabs/VerticalSlabTable.java")
    with open(java, "w") as f:
        f.write("package com.chaevsfe.valence.modules.verticalslabs;\n\n")
        f.write("import java.util.List;\n\n")
        f.write("// Generated by tools/gen_vertical_slabs.py. Do not edit.\n")
        f.write("// Slabs absent from the running version's registry are skipped at registration.\n")
        f.write("public final class VerticalSlabTable\n{\n")
        f.write("    public record Row (String base, String slabId) { }\n\n")
        f.write("    public static final List<Row> ROWS = List.of(\n")
        lines = [f'        new Row("{base}", "minecraft:{slab_id}")' for base, slab_id, _ in rows]
        f.write(",\n".join(lines) + ");\n\n")
        f.write("    private VerticalSlabTable () { }\n}\n")

    print(f"{len(rows)} slabs generated")


if __name__ == "__main__":
    main()
