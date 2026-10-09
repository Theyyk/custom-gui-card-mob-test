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
        new Definition("Кинжал убийцы", "Клик", "killer_dagger.png"),
        new Definition("Кинжал убийцы", "Клик", "killer_dagger.png"),
        new Definition("Кинжал убийцы", "Клик", "killer_dagger.png"),
        new Definition("Кинжал убийцы", "Клик", "killer_dagger.png"),
        new Definition("Накидка убийцы", "Клик", "killer_cloak.png"),
        new Definition("Накидка убийцы", "Клик", "killer_cloak.png"),
        new Definition("Накидка убийцы", "Клик", "killer_cloak.png"),
        new Definition("Перчатка скрытого удара", "Клик", "hidden_strike_glove.png"),
        new Definition("Камень заточки", "Клик", "sharpening_stone.png"),
        new Definition("Камень заточки", "Клик", "sharpening_stone.png"),
        new Definition("Амулет ярости", "Клик", "rage_amulet.png"),
        new Definition("Токсичный осколок", "Клик / Яд", "toxic_shard.png"),
        new Definition("Токсичный осколок", "Клик / Яд", "toxic_shard.png"),
        new Definition("Клык змеи", "Яд", "snake_fang.png"),
        new Definition("Клык змеи", "Яд", "snake_fang.png"),
        new Definition("Клык змеи", "Яд", "snake_fang.png"),
        new Definition("Клык змеи", "Яд", "snake_fang.png"),
        new Definition("Печать заражения", "Яд", "infection_seal.png"),
        new Definition("Печать заражения", "Яд", "infection_seal.png"),
        new Definition("Печать заражения", "Яд", "infection_seal.png"),
        new Definition("Усилитель токсина", "Яд", "toxin_booster.png"),
        new Definition("Усилитель токсина", "Яд", "toxin_booster.png"),
        new Definition("Метка заражения", "Яд", "infection_mark.png"),
        new Definition("Метка заражения", "Яд", "infection_mark.png"),
        new Definition("Метка заражения", "Яд", "infection_mark.png"),
        new Definition("Реликвия охотника", "Ресурс", "hunter_relic.png")
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
