package com.teintescope.model;

public enum Severity {
    CRITIQUE("Critique", "sev-critique"),
    ELEVEE("Élevée", "sev-elevee"),
    MOYENNE("Moyenne", "sev-moyenne");

    private final String label;
    private final String cssClass;

    Severity(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String label() { return label; }
    public String cssClass() { return cssClass; }
}
