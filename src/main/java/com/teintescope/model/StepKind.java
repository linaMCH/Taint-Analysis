package com.teintescope.model;

/** Étapes d'un chemin de teinte. Couleurs : source=bleu, propagation=violet, assainisseur=vert, sink=rouge. */
public enum StepKind {
    SOURCE("SOURCE", "k-source", "#2563EB"),
    PROPAGATION("PROPAGATION", "k-propagation", "#7C3AED"),
    TRANSFORMATION("TRANSFORMATION", "k-transformation", "#A855F7"),
    SANITIZER("ASSAINISSEUR", "k-sanitizer", "#159B72"),
    SINK("SINK", "k-sink", "#DC454F");

    private final String label, css, color;
    StepKind(String label, String css, String color) { this.label = label; this.css = css; this.color = color; }
    public String label() { return label; }
    public String css() { return css; }
    public String color() { return color; }
}