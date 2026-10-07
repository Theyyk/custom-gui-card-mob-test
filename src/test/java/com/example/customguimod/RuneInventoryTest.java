package com.example.customguimod;

/** Exercises loading and grouping purchases without Minecraft or MongoDB. */
public final class RuneInventoryTest {
    public static void main(String[] args) {
        RuneInventory inventory = new RuneInventory();
        inventory.set(0, 4, 1);
        inventory.set(26, 4, 2);
        inventory.set(49, 9, 1);
        check(inventory.occupiedCount() == 3, "Grouping must preserve all server purchases");
        check(inventory.occupiedTypeCount() == 2, "Duplicates must use one boost cell");
        check(inventory.getBoost(4).layer == 2,
                "The boost cell must represent its highest owned rank");
        check(inventory.get(0).layer == 1 && inventory.get(26).layer == 2,
                "Grouping must not change individual ranks");
        check(inventory.getBoost(9).cardIndex == 9, "Legacy slot 49 must contribute to the catalog");
        check(inventory.getBoost(1) == null, "Unowned types must remain empty");
        inventory.set(26, 7, 3);
        check(inventory.occupiedCount() == 3 && inventory.getBoost(4).layer == 1
                        && inventory.getBoost(7).layer == 3,
                "Replacing a slot must update both represented types and their best ranks");
        inventory.set(26, 7, 3);
        check(inventory.occupiedCount() == 3, "Repeated load packets must not duplicate purchases");
        inventory.set(-1, 2, 1);
        inventory.set(50, 2, 1);
        inventory.set(1, -1, 1);
        inventory.set(1, 26, 1);
        check(inventory.occupiedCount() == 3 && inventory.getBoost(26) == null,
                "Invalid slots and boost types must be ignored");
        inventory.set(1, 25, 1);
        check(inventory.getBoost(25).cardIndex == 25, "The 26th future boost type must be supported");
        check(inventory.occupiedTypeCount(10) == 3 && inventory.occupiedTypeCount() == 4,
                "Available-type progress must exclude future catalog positions");
        check(!inventory.isPurchaseLimitReached(), "A partial collection must permit purchases");
        inventory.clear();
        check(inventory.occupiedCount() == 0 && inventory.occupiedTypeCount() == 0
                        && inventory.getBoost(4) == null,
                "Switching collections must clear records and represented types");
        for (int slot = 0; slot < RuneInventory.CAPACITY; slot++) inventory.set(slot, slot % 10, 1);
        check(inventory.occupiedCount() == 50 && inventory.occupiedTypeCount() == 10,
                "An existing fifty-rune collection must become ten populated boost cells");
        check(inventory.isPurchaseLimitReached() && inventory.occupiedTypeCount(10) == 10,
                "All available types can be owned while the separate purchase limit is full");
        for (int type = 0; type < RuneInventory.BOOST_TYPE_COUNT; type++) {
            RuneInventory.Entry boost = inventory.getBoost(type);
            check((type < 10) == (boost != null), "Only owned types may be populated");
        }
        for (int slot = 0; slot < RuneInventory.CAPACITY; slot++) {
            check(inventory.get(slot).cardIndex == slot % 10, "Grouping must not change saved purchases");
        }
        inventory.clear();
        check(!inventory.isPurchaseLimitReached(), "Switching to an empty collection must enable purchases");
        System.out.println("26-type catalog and purchase availability regression checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
