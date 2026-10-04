package com.example.customguimod;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoManager {

    private static MongoClient client;
private static MongoDatabase database;
private static MongoCollection<Document> players;

    public static void connect() {
    try {
        client = MongoClients.create("mongodb://localhost:27017");
        database = client.getDatabase("cristalix_demo");
        players = database.getCollection("players");
        CustomGuiMod.logger.info("MongoDB connected!");
    } catch (Exception e) {
        CustomGuiMod.logger.error("MongoDB error: " + e.getMessage());
    }
}

    public static int getBalance(UUID uuid) {
        if (players == null) return 0;
        Document doc = players.find(new Document("_id", uuid.toString())).first();
        if (doc == null) return 0;
        Integer b = doc.getInteger("balance", 0);
        return b == null ? 0 : b;
    }

    public static void setBalance(UUID uuid, int balance) {
        if (players == null) return;
        players.updateOne(
                new Document("_id", uuid.toString()),
                new Document("$set", new Document("balance", balance)),
                new UpdateOptions().upsert(true)
        );
    }

    // === КАРТОЧКИ ===

    public static class SavedCard {
        public int slot, cardIndex, layer;

        public SavedCard(int slot, int cardIndex, int layer) {
            this.slot = slot;
            this.cardIndex = cardIndex;
            this.layer = layer;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<SavedCard> getCards(UUID uuid) {
        List<SavedCard> result = new ArrayList<>();
        if (players == null) return result;
        Document doc = players.find(new Document("_id", uuid.toString())).first();
        if (doc == null) return result;
        List<Document> cards = (List<Document>) doc.get("cards");
        if (cards == null) return result;
        for (Document c : cards) {
            result.add(new SavedCard(
                    c.getInteger("slot", 0),
                    c.getInteger("cardIndex", 0),
                    c.getInteger("layer", 1)
            ));
        }
        return result;
    }

    public static void addCard(UUID uuid, int slot, int cardIndex, int layer) {
        if (players == null) return;
        List<SavedCard> cards = getCards(uuid);
        cards.add(new SavedCard(slot, cardIndex, layer));
        List<Document> docs = new ArrayList<>();
        for (SavedCard c : cards) {
            docs.add(new Document("slot", c.slot).append("cardIndex", c.cardIndex).append("layer", c.layer));
        }
        players.updateOne(
                new Document("_id", uuid.toString()),
                new Document("$set", new Document("cards", docs)),
                new UpdateOptions().upsert(true)
        );
    }
	
	public static void clearCards(UUID uuid) {
    if (players == null) return;
    players.updateOne(
            new Document("_id", uuid.toString()),
            new Document("$set", new Document("cards", new ArrayList<Document>())),
            new UpdateOptions().upsert(true)
    );
    CustomGuiMod.logger.info("Cleared cards for " + uuid);
}
	
	public static List<Integer> getFreeSlots(UUID uuid) {
    List<Integer> free = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
        free.add(i);
    }

    List<SavedCard> cards = getCards(uuid);
    for (SavedCard card : cards) {
        free.remove(Integer.valueOf(card.slot));
    }

    return free;
}

    // === КАСТОМНЫЕ МОБЫ ===

    public static class CustomMob {
    public String name;
    public String type;
    public int hp;
    public int money;
    public double x, y, z;
    public boolean enabled;

    public CustomMob(String name, String type, int hp, int money, double x, double y, double z, boolean enabled) {
        this.name = name;
        this.type = type;
        this.hp = hp;
        this.money = money;
        this.x = x;
        this.y = y;
        this.z = z;
        this.enabled = enabled;
    }
}

public static MongoCollection<Document> getMobsCollection() {
    return database.getCollection("custom_mobs");
}

    public static void createMob(String name, String type, int hp, int money) {
    if (players == null) return;
    getMobsCollection().insertOne(new Document("name", name)
            .append("type", type)
            .append("hp", hp)
            .append("money", money)
            .append("x", 0.0)
            .append("y", 0.0)
            .append("z", 0.0)
            .append("enabled", false));
    CustomGuiMod.logger.info("Mob created: " + name + " (" + type + ", HP " + hp + ", " + money + " money)");
}

    public static void setMobSpawn(String name, double x, double y, double z) {
        if (players == null) return;
        getMobsCollection().updateOne(
                new Document("name", name),
                new Document("$set", new Document("x", x).append("y", y).append("z", z).append("enabled", true))
        );
        CustomGuiMod.logger.info("Spawn set for " + name + ": " + x + ", " + y + ", " + z);
    }

    public static void unspawnMob(String name) {
        if (players == null) return;
        getMobsCollection().updateOne(
                new Document("name", name),
                new Document("$set", new Document("enabled", false))
        );
        CustomGuiMod.logger.info("Mob " + name + " disabled");
    }

    public static List<CustomMob> getAllMobs() {
    List<CustomMob> result = new ArrayList<>();
    if (players == null) return result;
    for (Document doc : getMobsCollection().find()) {
        result.add(new CustomMob(
                doc.getString("name"),
                doc.getString("type"),
                doc.getInteger("hp", 10),
                doc.getInteger("money", 50),
                doc.getDouble("x"),
                doc.getDouble("y"),
                doc.getDouble("z"),
                doc.getBoolean("enabled", false)
        ));
    }
    return result;
}

    public static CustomMob getMob(String name) {
    if (players == null) return null;
    Document doc = getMobsCollection().find(new Document("name", name)).first();
    if (doc == null) return null;
    return new CustomMob(
            doc.getString("name"),
            doc.getString("type"),
            doc.getInteger("hp", 10),
            doc.getInteger("money", 50),
            doc.getDouble("x"),
            doc.getDouble("y"),
            doc.getDouble("z"),
            doc.getBoolean("enabled", false)
    );
}
    public static void deleteMob(String name) {
        if (players == null) return;
        getMobsCollection().deleteMany(new Document("name", name));
        CustomGuiMod.logger.info("Mob deleted: " + name);
    }
}
