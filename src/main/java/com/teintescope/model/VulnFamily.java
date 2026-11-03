package com.teintescope.model;

/** Familles utilisées par les filtres du panneau des problèmes. */
public enum VulnFamily {
    ALL("Toutes"), SQL("SQL"), NON_SQL("Non-SQL"), XSS("XSS"), COMMAND("Commandes"), OTHER("Autres");

    private final String label;
    VulnFamily(String label) { this.label = label; }
    public String label() { return label; }
}