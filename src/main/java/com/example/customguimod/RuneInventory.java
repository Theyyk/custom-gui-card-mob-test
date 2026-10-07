package com.example.customguimod;

/** Client mirror of the server's physical inventory slots. */
final class RuneInventory {
    static final int CAPACITY = 50;

    static final class Entry {
        final int cardIndex;
        final int layer;

        Entry(int cardIndex, int layer) {
            this.cardIndex = cardIndex;
            this.layer = layer;
        }
    }

    private final Entry[] slots = new Entry[CAPACITY];

    void set(int slot, int cardIndex, int layer) {
        if (slot < 0 || slot >= CAPACITY) return;
        slots[slot] = new Entry(cardIndex, layer);
    }

    Entry get(int slot) {
        return slot < 0 || slot >= CAPACITY ? null : slots[slot];
    }

    int occupiedCount() {
        int count = 0;
        for (Entry entry : slots) if (entry != null) count++;
        return count;
    }

    void clear() {
        java.util.Arrays.fill(slots, null);
    }
}
