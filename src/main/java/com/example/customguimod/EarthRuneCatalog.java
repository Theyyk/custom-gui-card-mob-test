package com.example.customguimod;

/** First element: fixed positions, in reading order from the supplied reference. */
final class EarthRuneCatalog {
    static final class Definition {
        final String name;
        final String type;
        Definition(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    private static final Definition[] RUNES = {
        new Definition("Каменный молот", "Клик"),
        new Definition("Каменный молот", "Клик"),
        new Definition("Каменный молот", "Клик"),
        new Definition("Каменный молот", "Клик"),
        new Definition("Теневой клинок", "Клик"),
        new Definition("Теневой клинок", "Клик"),
        new Definition("Теневой клинок", "Клик"),
        new Definition("Сердце леса", "Клик"),
        new Definition("Печать кузнеца", "Усиление"),
        new Definition("Печать кузнеца", "Усиление"),
        new Definition("Сердце голема", "Усиление"),
        new Definition("Мшистый осколок", "Клик / Земля"),
        new Definition("Мшистый осколок", "Клик / Земля"),
        new Definition("Древний корень", "Земля"),
        new Definition("Древний корень", "Земля"),
        new Definition("Древний корень", "Земля"),
        new Definition("Древний корень", "Земля"),
        new Definition("Обсидиановый шип", "Земля"),
        new Definition("Обсидиановый шип", "Земля"),
        new Definition("Обсидиановый шип", "Земля"),
        new Definition("Семя древолеса", "Усиление"),
        new Definition("Семя древолеса", "Усиление"),
        new Definition("Треснувшая печать", "Усиление"),
        new Definition("Треснувшая печать", "Усиление"),
        new Definition("Треснувшая печать", "Усиление"),
        new Definition("Золотой самородок", "Ресурс")
    };

    // Reuse existing item placeholders; repeated names keep separate slot identities.
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
