package com.teintescope.lexer;

/**
 * Token immutable produit par le lexer.
 * Contient le type, la valeur et la position exacte (ligne, colonne) dans le code source.
 */
public record Token(TokenType type, String value, int line, int column) {
    
    /**
     * Vérifie si ce token est une source de données non fiable.
     */
    public boolean isSource() {
        return type == TokenType.SOURCE || type == TokenType.SUPERGLOBAL;
    }
    
    /**
     * Vérifie si ce token est un sink (fonction sensible).
     */
    public boolean isSink() {
        return type == TokenType.SINK;
    }
    
    /**
     * Vérifie si ce token est un assainisseur (sanitizer).
     */
    public boolean isSanitizer() {
        return type == TokenType.SANITIZER;
    }
    
    /**
     * Vérifie si ce token est un mot-clé PHP.
     */
    public boolean isKeyword() {
        return type == TokenType.KEYWORD;
    }
    
    /**
     * Vérifie si ce token est un opérateur.
     */
    public boolean isOperator() {
        return type.isOperator();
    }
    
    /**
     * Vérifie si ce token est un littéral (chaîne, nombre, booléen, null).
     */
    public boolean isLiteral() {
        return type.isLiteral();
    }
    
    /**
     * Vérifie si ce token est un commentaire.
     */
    public boolean isComment() {
        return type.isComment();
    }
    
    /**
     * Vérifie si ce token est une variable.
     */
    public boolean isVariable() {
        return type == TokenType.VARIABLE || type == TokenType.SUPERGLOBAL;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s) @%d:%d", type, value, line, column);
    }
}
