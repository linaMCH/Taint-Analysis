package com.teintescope.model;

/**
 * Information sur un token avec sa position dans le code source.
 * Utilisé pour l'affichage et le rapport de vulnérabilités.
 */
public record TokenInfo(String type, String value, int line, int column, boolean source) {
    
    /**
     * Constructeur de compatibilité pour l'ancien format (sans position).
     */
    public TokenInfo(String type, String value, boolean source) {
        this(type, value, 0, 0, source);
    }
}
