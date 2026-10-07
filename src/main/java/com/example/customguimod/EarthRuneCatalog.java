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
        new Definition("Булава", "Клик"),
        new Definition("Булава", "Клик"),
        new Definition("Булава", "Клик"),
        new Definition("Булава", "Клик"),
        new Definition("Накидка вора", "Клик"),
        new Definition("Накидка вора", "Клик"),
        new Definition("Накидка вора", "Клик"),
        new Definition("Лесной дух", "Клик"),
        new Definition("Щит", "Усиление"),
        new Definition("Щит", "Усиление"),
        new Definition("Низший голем", "Усиление"),
        new Definition("Речной дракончик", "Клик / Земля"),
        new Definition("Речной дракончик", "Клик / Земля"),
        new Definition("Энт-пугатель", "Земля"),
        new Definition("Энт-пугатель", "Земля"),
        new Definition("Энт-пугатель", "Земля"),
        new Definition("Энт-пугатель", "Земля"),
        new Definition("Хижина", "Земля"),
        new Definition("Хижина", "Земля"),
        new Definition("Хижина", "Земля"),
        new Definition("Посох друида", "Усиление"),
        new Definition("Посох друида", "Усиление"),
        new Definition("Книга земли", "Усиление"),
        new Definition("Книга земли", "Усиление"),
        new Definition("Книга земли", "Усиление"),
        new Definition("Чудо", "Ресурс")
    };

    static int size() { return RUNES.length; }
    static Definition at(int slot) {
        return slot < 0 || slot >= RUNES.length ? null : RUNES[slot];
    }
}
