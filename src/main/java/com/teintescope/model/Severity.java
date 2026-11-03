package com.teintescope.model;

public enum Severity {
    CRITIQUE("Critique", "sev-critique"),
    ELEVEE("Élevée", "sev-elevee"),
    MOYENNE("Moyenne", "sev-moyenne"),
    FAIBLE("Faible", "sev-faible");

    private final String label, css;
    Severity(String label, String css) { this.label = label; this.css = css; }
    public String label() { return label; }
    public String css() { return css; }
}