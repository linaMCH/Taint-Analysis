package com.teintescope.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/** Configuration d'analyse (sources, sinks, assainisseurs, règles, catégories). Exportable en JSON. */
public final class ConfigModel {
    public static final ConfigModel INSTANCE = new ConfigModel();

    public final ObservableList<ConfigEntry> sources = FXCollections.observableArrayList(
            new ConfigEntry("$_GET", "Toutes"), new ConfigEntry("$_POST", "Toutes"),
            new ConfigEntry("$_COOKIE", "Toutes"), new ConfigEntry("$_REQUEST", "Toutes"));

    public final ObservableList<ConfigEntry> sinks = FXCollections.observableArrayList(
            new ConfigEntry("mysqli_query", "SQL"), new ConfigEntry("PDO::query", "SQL"),
            new ConfigEntry("mysql_query", "SQL"), new ConfigEntry("echo", "XSS"),
            new ConfigEntry("exec", "CMD"));

    public final ObservableList<ConfigEntry> sanitizers = FXCollections.observableArrayList(
            new ConfigEntry("intval", "SQL"), new ConfigEntry("mysqli_real_escape_string", "SQL"),
            new ConfigEntry("htmlspecialchars", "XSS"), new ConfigEntry("escapeshellarg", "CMD"));

    public final Map<String, BooleanProperty> rules = new LinkedHashMap<>();
    public final Map<VulnCategory, BooleanProperty> categories = new EnumMap<>(VulnCategory.class);

    private ConfigModel() {
        rules.put("Propager la teinte à travers les affectations", new SimpleBooleanProperty(true));
        rules.put("Propager la teinte à travers les concaténations", new SimpleBooleanProperty(true));
        rules.put("Tenir compte des assainisseurs configurés", new SimpleBooleanProperty(true));
        rules.put("Signaler aussi les flux à faible confiance", new SimpleBooleanProperty(false));
        rules.put("Analyse inter-fichiers (prévu)", new SimpleBooleanProperty(false));
        for (VulnCategory c : VulnCategory.values())
            categories.put(c, new SimpleBooleanProperty(c.maturity() == Maturity.AVAILABLE));
    }

    /** Retourne une action qui restaure l'état actuel (utilisée par « Annuler »). */
    public Runnable snapshot() {
        List<ConfigEntry> a = new ArrayList<>(sources), b = new ArrayList<>(sinks), c = new ArrayList<>(sanitizers);
        Map<String, Boolean> r = new HashMap<>();
        rules.forEach((k, v) -> r.put(k, v.get()));
        Map<VulnCategory, Boolean> cat = new EnumMap<>(VulnCategory.class);
        categories.forEach((k, v) -> cat.put(k, v.get()));
        return () -> {
            sources.setAll(a); sinks.setAll(b); sanitizers.setAll(c);
            r.forEach((k, v) -> rules.get(k).set(v));
            cat.forEach((k, v) -> categories.get(k).set(v));
        };
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"sources\": ").append(arr(sources)).append(",\n");
        sb.append("  \"sinks\": ").append(arr(sinks)).append(",\n");
        sb.append("  \"sanitizers\": ").append(arr(sanitizers)).append(",\n");
        sb.append("  \"rules\": {");
        int i = 0;
        for (var e : rules.entrySet())
            sb.append(i++ > 0 ? "," : "").append("\n    ").append(q(e.getKey())).append(": ").append(e.getValue().get());
        sb.append("\n  },\n  \"categories\": {");
        i = 0;
        for (var e : categories.entrySet())
            sb.append(i++ > 0 ? "," : "").append("\n    ").append(q(e.getKey().name())).append(": ").append(e.getValue().get());
        sb.append("\n  }\n}\n");
        return sb.toString();
    }

    private static String arr(List<ConfigEntry> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++)
            sb.append(i > 0 ? ", " : "").append("{\"name\": ").append(q(l.get(i).name()))
                    .append(", \"category\": ").append(q(l.get(i).category())).append("}");
        return sb.append("]").toString();
    }

    private static String q(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}