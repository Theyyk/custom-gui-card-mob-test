package com.example.customguimod;

/** Standalone regression checks: run main with assertions independent of Minecraft. */
public final class RuneInventoryTest {
    public static void main(String[] args) {
        RuneInventory inventory = new RuneInventory();
        inventory.set(0, 4, 1);
        inventory.set(26, 4, 2);
        inventory.set(49, 9, 1);
        check(inventory.occupiedCount() == 3, "Duplicate types must occupy separate server slots");
        check(inventory.get(0).layer == 1 && inventory.get(26).layer == 2,
                "Ranks must stay attached to their own slots");
        check(inventory.get(49).cardIndex == 9, "Last server slot must be visible");
        check(inventory.get(1) == null, "Unoccupied slots must remain empty");
        inventory.set(26, 7, 3);
        check(inventory.occupiedCount() == 3 && inventory.get(26).cardIndex == 7,
                "Repeated slot packets must replace rather than duplicate a slot");
        inventory.set(-1, 2, 1);
        inventory.set(50, 2, 1);
        check(inventory.occupiedCount() == 3 && inventory.get(50) == null,
                "Out-of-range packets must be ignored");
        inventory.clear();
        check(inventory.occupiedCount() == 0 && inventory.get(49) == null,
                "Switching collections must clear all fifty slots");
        for (int slot = 0; slot < RuneInventory.CAPACITY; slot++) inventory.set(slot, slot % 10, 1);
        check(inventory.occupiedCount() == 50, "All fifty purchases must be represented");
        for (int slot = 0; slot < RuneInventory.CAPACITY; slot++) {
            check(inventory.get(slot).cardIndex == slot % 10, "Loading must preserve slot identity");
        }
        System.out.println("RuneInventory regression checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
