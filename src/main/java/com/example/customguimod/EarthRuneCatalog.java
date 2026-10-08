package com.example.customguimod;

/** First element: fixed positions, in reading order from the supplied reference. */
final class EarthRuneCatalog {
    static final class Definition {
        final String name;
        final String type;
        final String iconTexture;
        Definition(String name, String type, String iconFile) {
            this.name = name;
            this.type = type;
            this.iconTexture = "customguimod:textures/gui/runes/" + iconFile;
        }
    }

    private static final Definition[] RUNES = {
        new Definition("Каменный молот", "Клик", "stone_hammer.png"),
        new Definition("Каменный молот", "Клик", "stone_hammer.png"),
        new Definition("Каменный молот", "Клик", "stone_hammer.png"),
        new Definition("Каменный молот", "Клик", "stone_hammer.png"),
        new Definition("Теневой клинок", "Клик", "shadow_blade.png"),
        new Definition("Теневой клинок", "Клик", "shadow_blade.png"),
        new Definition("Теневой клинок", "Клик", "shadow_blade.png"),
        new Definition("Сердце леса", "Клик", "heart_of_forest.png"),
        new Definition("Печать кузнеца", "Усиление", "forge_seal.png"),
        new Definition("Печать кузнеца", "Усиление", "forge_seal.png"),
        new Definition("Сердце голема", "Усиление", "golem_heart.png"),
        new Definition("Мшистый осколок", "Клик / Земля", "moss_shard.png"),
        new Definition("Мшистый осколок", "Клик / Земля", "moss_shard.png"),
        new Definition("Древний корень", "Земля", "ancient_root.png"),
        new Definition("Древний корень", "Земля", "ancient_root.png"),
        new Definition("Древний корень", "Земля", "ancient_root.png"),
        new Definition("Древний корень", "Земля", "ancient_root.png"),
        new Definition("Обсидиановый шип", "Земля", "obsidian_spike.png"),
        new Definition("Обсидиановый шип", "Земля", "obsidian_spike.png"),
        new Definition("Обсидиановый шип", "Земля", "obsidian_spike.png"),
        new Definition("Семя древолеса", "Усиление", "ancient_seed.png"),
        new Definition("Семя древолеса", "Усиление", "ancient_seed.png"),
        new Definition("Треснувшая печать", "Усиление", "cracked_seal.png"),
        new Definition("Треснувшая печать", "Усиление", "cracked_seal.png"),
        new Definition("Треснувшая печать", "Усиление", "cracked_seal.png"),
        new Definition("Золотой самородок", "Ресурс", "gold_nugget.png")
    };

    // Legacy icon indices are retained for compatibility; PNG paths live in each definition.
    private static final int[] ICONS = {
        0, 0, 0, 0, 1, 1, 1, 2, 3, 3, 4, 5, 5,
        6, 6, 6, 6, 7, 7, 7, 8, 8, 9, 9, 9, 10
    };

    static int iconIndex(int slot) {
        return slot < 0 || slot >= ICONS.length ? -1 : ICONS[slot];
    }

    static int size() { return RUNES.length; }
    static Definition at(int slot) {
        return slot < 0 || slot >= RUNES.length ? null : RUNES[slot];
    }
}
