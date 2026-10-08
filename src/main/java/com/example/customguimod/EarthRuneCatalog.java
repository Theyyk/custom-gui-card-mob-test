package com.example.customguimod;

/** First status: fixed rune positions in reading order. */
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
        new Definition("Кинжал убийцы", "Клик", "stone_hammer.png"),
        new Definition("Кинжал убийцы", "Клик", "stone_hammer.png"),
        new Definition("Кинжал убийцы", "Клик", "stone_hammer.png"),
        new Definition("Кинжал убийцы", "Клик", "stone_hammer.png"),
        new Definition("Накидка убийцы", "Клик", "shadow_blade.png"),
        new Definition("Накидка убийцы", "Клик", "shadow_blade.png"),
        new Definition("Накидка убийцы", "Клик", "shadow_blade.png"),
        new Definition("Перчатка скрытого удара", "Клик", "heart_of_forest.png"),
        new Definition("Камень заточки", "Клик", "forge_seal.png"),
        new Definition("Камень заточки", "Клик", "forge_seal.png"),
        new Definition("Амулет ярости", "Клик", "golem_heart.png"),
        new Definition("Токсичный осколок", "Клик / Яд", "moss_shard.png"),
        new Definition("Токсичный осколок", "Клик / Яд", "moss_shard.png"),
        new Definition("Клык змеи", "Яд", "ancient_root.png"),
        new Definition("Клык змеи", "Яд", "ancient_root.png"),
        new Definition("Клык змеи", "Яд", "ancient_root.png"),
        new Definition("Клык змеи", "Яд", "ancient_root.png"),
        new Definition("Печать заражения", "Яд", "obsidian_spike.png"),
        new Definition("Печать заражения", "Яд", "obsidian_spike.png"),
        new Definition("Печать заражения", "Яд", "obsidian_spike.png"),
        new Definition("Усилитель токсина", "Яд", "ancient_seed.png"),
        new Definition("Усилитель токсина", "Яд", "ancient_seed.png"),
        new Definition("Метка заражения", "Яд", "cracked_seal.png"),
        new Definition("Метка заражения", "Яд", "cracked_seal.png"),
        new Definition("Метка заражения", "Яд", "cracked_seal.png"),
        new Definition("Реликвия охотника", "Ресурс", "gold_nugget.png")
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
