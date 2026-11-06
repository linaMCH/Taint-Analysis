package com.teintescope.lexer;

/**
 * Exception levée lorsque le lexer rencontre un caractère ou une construction invalide.
 * Contient la position exacte (ligne, colonne) de l'erreur.
 */
public class LexerException extends RuntimeException {
    
    private final int line;
    private final int column;
    
    /**
     * Crée une exception lexicale avec position.
     * 
     * @param message description de l'erreur
     * @param line numéro de ligne (1-indexé)
     * @param column numéro de colonne (1-indexé)
     */
    public LexerException(String message, int line, int column) {
        super(String.format("Erreur lexicale ligne %d, colonne %d : %s", line, column, message));
        this.line = line;
        this.column = column;
    }
    
    /**
     * Retourne le numéro de ligne où l'erreur s'est produite.
     */
    public int getLine() {
        return line;
    }
    
    /**
     * Retourne le numéro de colonne où l'erreur s'est produite.
     */
    public int getColumn() {
        return column;
    }
}
