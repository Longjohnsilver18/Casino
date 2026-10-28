package de.royalcasino;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CoinStore {
    public static final long DAY_MS = 24L * 60 * 60 * 1000;
    public static final int DAILY = 1000;

    private static class Data {
        long coins = 0;
        long lastClaim = 0;
    }

    private static Data data = new Data();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("royalcasino.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Data d = GSON.fromJson(Files.readString(p), Data.class);
                if (d != null) data = d;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long coins() { return data.coins; }

    public static boolean take(long n) {
        if (n <= 0 || data.coins < n) return false;
        data.coins -= n;
        save();
        return true;
    }

    public static void give(long n) {
        if (n <= 0) return;
        data.coins += n;
        save();
    }

    public static long millisUntilClaim() {
        long left = data.lastClaim + DAY_MS - System.currentTimeMillis();
        return Math.max(0, Math.min(DAY_MS, left));
    }

    public static boolean canClaim() { return millisUntilClaim() == 0; }

    public static boolean claim() {
        if (!canClaim()) return false;
        data.coins += DAILY;
        data.lastClaim = System.currentTimeMillis();
        save();
        return true;
    }
}
