package net.enelson.soparenas.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ArenaDefinition {
    private final String id;
    private String displayName;
    private int minPlayers = 1;
    private int maxPlayers = 4;
    private int autoStartSeconds = 15;
    private final Map<String, ArenaLocation> points = new LinkedHashMap<>();
    private final Map<String, ArenaRegion> regions = new LinkedHashMap<>();

    public ArenaDefinition(String id) {
        this.id = id;
        this.displayName = id;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public void displayName(String value) { displayName = value; }
    public int minPlayers() { return minPlayers; }
    public void minPlayers(int value) { minPlayers = Math.max(1, value); maxPlayers = Math.max(maxPlayers, minPlayers); }
    public int maxPlayers() { return maxPlayers; }
    public void maxPlayers(int value) { maxPlayers = Math.max(minPlayers, value); }
    public int autoStartSeconds() { return autoStartSeconds; }
    public void autoStartSeconds(int value) { autoStartSeconds = Math.max(0, value); }
    public void point(String name, ArenaLocation point) { points.put(name.toLowerCase(), point); }
    public ArenaLocation point(String name) { return points.get(name.toLowerCase()); }
    public void region(String name, ArenaRegion region) { regions.put(name.toLowerCase(), region); }
    public ArenaRegion region(String name) { return regions.get(name.toLowerCase()); }
    public Map<String, ArenaLocation> points() { return Collections.unmodifiableMap(points); }
    public Map<String, ArenaRegion> regions() { return Collections.unmodifiableMap(regions); }
}
