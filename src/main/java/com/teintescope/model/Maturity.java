package com.teintescope.model;

public enum Maturity {
    AVAILABLE("Disponible"), IN_PROGRESS("En cours"), PLANNED("Prévu");

    private final String label;
    Maturity(String label) { this.label = label; }
    public String label() { return label; }
}