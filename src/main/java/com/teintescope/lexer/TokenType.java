package com.teintescope.lexer;

/**
 * Types de tokens PHP supportés par le lexer TaintScope.
 * Couvre le sous-ensemble nécessaire à l'analyse de sécurité.
 */
public enum TokenType {
    // Délimiteurs PHP
    OPEN_TAG,           // <?php
    CLOSE_TAG,          // ?>
    
    // Variables
    VARIABLE,           // $nom
    SUPERGLOBAL,        // $_GET, $_POST, $_COOKIE, $_REQUEST
    
    // Littéraux
    STRING_SINGLE,      // 'texte'
    STRING_DOUBLE,      // "texte"
    NUMBER_INT,         // 42
    NUMBER_FLOAT,       // 3.14
    BOOLEAN,            // true, false, TRUE, FALSE
    NULL_LITERAL,       // null, NULL
    
    // Opérateurs d'affectation
    ASSIGN,             // =
    CONCAT_ASSIGN,      // .=
    PLUS_ASSIGN,        // +=
    MINUS_ASSIGN,       // -=
    MUL_ASSIGN,         // *=
    DIV_ASSIGN,         // /=
    MOD_ASSIGN,         // %=
    
    // Opérateurs arithmétiques
    PLUS,               // +
    MINUS,              // -
    MUL,                // *
    DIV,                // /
    MOD,                // %
    
    // Opérateurs de concaténation
    CONCAT,             // .
    
    // Opérateurs de comparaison
    EQ,                 // ==
    NEQ,                // !=
    IDENTICAL,          // ===
    NOT_IDENTICAL,      // !==
    LT,                 // <
    GT,                 // >
    LE,                 // <=
    GE,                 // >=
    SPACESHIP,          // <=>
    
    // Opérateurs logiques
    AND,                // &&
    OR,                 // ||
    NOT,                // !
    
    // Opérateurs d'incrémentation/décrémentation
    INC,                // ++
    DEC,                // --
    
    // Délimiteurs
    LPAREN,             // (
    RPAREN,             // )
    LBRACE,             // {
    RBRACE,             // }
    LBRACKET,           // [
    RBRACKET,           // ]
    SEMICOLON,          // ;
    COMMA,              // ,
    ARROW,              // ->
    DOUBLE_ARROW,       // =>
    DOUBLE_COLON,       // ::
    
    // Identifiants et mots-clés
    IDENTIFIER,         // nom de fonction, classe, constante
    KEYWORD,            // if, else, function, return, etc.
    
    // Commentaires
    COMMENT_LINE,       // // ...
    COMMENT_BLOCK,      // /* ... */
    COMMENT_SHELL,      // # ...
    
    // Catégories sémantiques (marquage selon ConfigModel)
    SOURCE,             // tagué si dans ConfigModel.sources
    SINK,               // tagué si dans ConfigModel.sinks
    SANITIZER,          // tagué si dans ConfigModel.sanitizers
    
    // Fin de fichier
    EOF;
    
    /**
     * Vérifie si ce type est un opérateur.
     */
    public boolean isOperator() {
        return name().endsWith("ASSIGN") || 
               this == PLUS || this == MINUS || this == MUL || this == DIV || this == MOD ||
               this == CONCAT || this == EQ || this == NEQ || this == IDENTICAL || 
               this == NOT_IDENTICAL || this == LT || this == GT || this == LE || this == GE ||
               this == SPACESHIP || this == AND || this == OR || this == NOT ||
               this == INC || this == DEC;
    }
    
    /**
     * Vérifie si ce type est un littéral.
     */
    public boolean isLiteral() {
        return this == STRING_SINGLE || this == STRING_DOUBLE ||
               this == NUMBER_INT || this == NUMBER_FLOAT ||
               this == BOOLEAN || this == NULL_LITERAL;
    }
    
    /**
     * Vérifie si ce type est un commentaire.
     */
    public boolean isComment() {
        return this == COMMENT_LINE || this == COMMENT_BLOCK || this == COMMENT_SHELL;
    }
}
