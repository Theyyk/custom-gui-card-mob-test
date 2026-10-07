package com.example.customguimod;

final class EarthRuneCatalogTest {
    static void run() {
        check(EarthRuneCatalog.size() == 26, "The reference must define all 26 positions");
        String[] expected = {"Булава", "Булава", "Булава", "Булава",
                "Накидка вора", "Накидка вора", "Накидка вора", "Лесной дух", "Щит", "Щит",
                "Низший голем", "Речной дракончик", "Речной дракончик",
                "Энт-пугатель", "Энт-пугатель", "Энт-пугатель", "Энт-пугатель",
                "Хижина", "Хижина", "Хижина", "Посох друида", "Посох друида",
                "Книга земли", "Книга земли", "Книга земли", "Чудо"};
        RuneInventory inventory = new RuneInventory();
        for (int slot = 0; slot < expected.length; slot++) {
            check(EarthRuneCatalog.at(slot).name.equals(expected[slot]), "Reference order differs at " + slot);
            check(!EarthRuneCatalog.at(slot).type.isEmpty(), "Every position needs a type");
            check(EarthRuneCatalog.iconIndex(slot) >= 0 && EarthRuneCatalog.iconIndex(slot) <= 10, "Every rune needs a placeholder icon");
            inventory.set(slot, slot, slot % 3 + 1);
        }
        check(inventory.ownedCatalogSlotCount() == 26, "Repeated names must occupy distinct positions");
        check(inventory.get(0).layer == 1 && inventory.get(1).layer == 2,
                "Two Bulavas must keep their own ownership and ranks");
        check(inventory.get(25).cardIndex == 25, "The last rune must remain a distinct purchased position");
        check(EarthRuneCatalog.iconIndex(0) == EarthRuneCatalog.iconIndex(3), "Repeated Bulavas reuse the same item icon");
        check(EarthRuneCatalog.iconIndex(25) == 10, "Miracle needs its own resource icon");
        inventory.clear();
        check(inventory.ownedCatalogSlotCount() == 0, "Changing elements must clear owned positions");
        check(EarthRuneCatalog.at(0).name.equals("Булава"), "Definitions must remain visible without ownership");
        check(EarthRuneCatalog.at(11).type.equals("Клик / Земля"), "Dragon type must follow the reference");
        check(EarthRuneCatalog.at(17).type.equals("Земля"), "Hut belongs to earth damage");
        check(EarthRuneCatalog.at(25).type.equals("Ресурс"), "Miracle belongs to resources");
        System.out.println("Fixed 26-position earth catalog regression checks passed");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
