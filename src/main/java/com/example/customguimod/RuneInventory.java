package com.example.customguimod;

/** Keeps server records intact and groups them into the visible boost catalog. */
final class RuneInventory {
    static final int CAPACITY = 50;
    static final int BOOST_TYPE_COUNT = 26;

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
        if (slot < 0 || slot >= CAPACITY || cardIndex < 0 || cardIndex >= BOOST_TYPE_COUNT) return;
        slots[slot] = new Entry(cardIndex, layer);
    }

    Entry get(int slot) {
        return slot < 0 || slot >= CAPACITY ? null : slots[slot];
    }

    Entry getBoost(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= BOOST_TYPE_COUNT) return null;
        Entry best = null;
        for (Entry entry : slots) {
            if (entry == null || entry.cardIndex != cardIndex) continue;
            if (best == null || entry.layer > best.layer) best = entry;
        }
        return best;
    }

    int occupiedTypeCount() {
        return occupiedTypeCount(BOOST_TYPE_COUNT);
    }

    int occupiedTypeCount(int availableTypes) {
        int limit = Math.max(0, Math.min(BOOST_TYPE_COUNT, availableTypes));
        boolean[] present = new boolean[BOOST_TYPE_COUNT];
        int count = 0;
        for (Entry entry : slots) {
            if (entry != null && entry.cardIndex < limit && !present[entry.cardIndex]) {
                present[entry.cardIndex] = true;
                count++;
            }
        }
        return count;
    }

    int occupiedCount() {
        int count = 0;
        for (Entry entry : slots) if (entry != null) count++;
        return count;
    }

    boolean isPurchaseLimitReached() {
        return occupiedCount() >= CAPACITY;
    }

    void clear() {
        java.util.Arrays.fill(slots, null);
    }
}
