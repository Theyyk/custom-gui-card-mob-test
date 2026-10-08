package com.example.customguimod;

final class EarthRuneCatalogTest {
    static void run() {
        check(EarthRuneCatalog.size() == 26, "The reference must define all 26 positions");
        String[] expected = {"Каменный молот", "Каменный молот", "Каменный молот", "Каменный молот",
                "Теневой клинок", "Теневой клинок", "Теневой клинок", "Сердце леса", "Печать кузнеца", "Печать кузнеца",
                "Сердце голема", "Мшистый осколок", "Мшистый осколок",
                "Древний корень", "Древний корень", "Древний корень", "Древний корень",
                "Обсидиановый шип", "Обсидиановый шип", "Обсидиановый шип", "Семя древолеса", "Семя древолеса",
                "Треснувшая печать", "Треснувшая печать", "Треснувшая печать", "Золотой самородок"};
        java.util.Set<String> textures = new java.util.HashSet<>();
        RuneInventory inventory = new RuneInventory();
        for (int slot = 0; slot < expected.length; slot++) {
            check(EarthRuneCatalog.at(slot).name.equals(expected[slot]), "Reference order differs at " + slot);
            check(!EarthRuneCatalog.at(slot).type.isEmpty(), "Every position needs a type");
            check(EarthRuneCatalog.iconIndex(slot) >= 0 && EarthRuneCatalog.iconIndex(slot) <= 10, "Every rune needs a placeholder icon");
            String texture = EarthRuneCatalog.at(slot).iconTexture;
            check(texture.startsWith("customguimod:textures/gui/runes/") && texture.endsWith(".png"), "PNG path required");
            textures.add(texture);
            if (slot > 0 && expected[slot].equals(expected[slot - 1]))
                check(texture.equals(EarthRuneCatalog.at(slot - 1).iconTexture), "Duplicates must share PNG paths");
            inventory.set(slot, slot, slot % 3 + 1);
        }
        check(textures.size() == 11, "Exactly eleven unique PNG paths required");
        check(inventory.ownedCatalogSlotCount() == 26, "Repeated names must occupy distinct positions");
        check(inventory.get(0).layer == 1 && inventory.get(1).layer == 2,
                "Two Bulavas must keep their own ownership and ranks");
        check(inventory.get(25).cardIndex == 25, "The last rune must remain a distinct purchased position");
        check(EarthRuneCatalog.iconIndex(0) == EarthRuneCatalog.iconIndex(3), "Repeated Bulavas reuse the same item icon");
        check(EarthRuneCatalog.iconIndex(25) == 10, "Miracle needs its own resource icon");
        inventory.clear();
        check(inventory.ownedCatalogSlotCount() == 0, "Changing elements must clear owned positions");
        check(EarthRuneCatalog.at(0).name.equals("Каменный молот"), "Definitions must remain visible without ownership");
        check(EarthRuneCatalog.at(11).type.equals("Клик / Земля"), "Dragon type must follow the reference");
        check(EarthRuneCatalog.at(17).type.equals("Земля"), "Hut belongs to earth damage");
        check(EarthRuneCatalog.at(25).type.equals("Ресурс"), "Miracle belongs to resources");
        System.out.println("Fixed 26-position earth catalog regression checks passed");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
