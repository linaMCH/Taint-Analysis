package com.teintescope.lexer;

import com.teintescope.model.ConfigModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Analyseur lexical pour le sous-ensemble PHP supporté par TaintScope.
 * <p>
 * Produit une liste de tokens avec positions exactes (ligne, colonne).
 * Peut marquer les tokens SOURCE/SINK/SANITIZER selon la configuration.
 * </p>
 * <p>
 * Hors scope : heredoc/nowdoc, expressions régulières /pattern/ → LexerException.
 * Les chaînes interpolées ("texte $var") sont traitées comme un seul STRING_DOUBLE.
 * </p>
 */
public class Lexer {

    // Ensemble des mots-clés PHP du sous-ensemble supporté
    private static final Set<String> KEYWORDS = Set.of(
            "if", "else", "elseif", "while", "for", "foreach", "function", "return",
            "echo", "print", "require", "include", "require_once", "include_once",
            "new", "class", "public", "private", "protected", "array",
            "isset", "empty", "unset", "true", "false", "null",
            "TRUE", "FALSE", "NULL", "break", "continue", "switch", "case", "default"
    );

    // Superglobales PHP
    private static final Set<String> SUPERGLOBALS = Set.of(
            "$_GET", "$_POST", "$_COOKIE", "$_REQUEST", "$_SERVER",
            "$_FILES", "$_SESSION", "$_ENV", "$GLOBALS"
    );

    private final String source;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    /**
     * Crée un lexer pour le code source donné.
     *
     * @param sourceCode le code PHP à analyser
     */
    public Lexer(String sourceCode) {
        this.source = sourceCode != null ? sourceCode : "";
    }

    /**
     * Tokenize le code source et retourne la liste des tokens.
     * Sans marquage sémantique (SOURCE/SINK/SANITIZER).
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) break;

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Tokenize avec marquage sémantique selon la configuration.
     * Les identifiants correspondant à des sources/sinks/sanitizers sont re-typés.
     */
    public List<Token> tokenize(ConfigModel config) {
        // Extraire les noms de la configuration
        Set<String> sourceNames = config.sources.stream()
                .map(e -> e.name())
                .collect(Collectors.toSet());
        Set<String> sinkNames = config.sinks.stream()
                .map(e -> e.name())
                .collect(Collectors.toSet());
        Set<String> sanitizerNames = config.sanitizers.stream()
                .map(e -> e.name())
                .collect(Collectors.toSet());

        return tokenizeWithConfig(sourceNames, sinkNames, sanitizerNames);
    }

    /**
     * Tokenize avec marquage sémantique à partir de sets de noms (sans dépendance JavaFX).
     * Utile pour les tests unitaires et les contextes hors JavaFX.
     */
    public List<Token> tokenizeWithConfig(Set<String> sourceNames,
                                           Set<String> sinkNames,
                                           Set<String> sanitizerNames) {
        List<Token> tokens = new ArrayList<>();
        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) break;

            Token token = nextToken();
            if (token != null) {
                tokens.add(applySemanticTag(token, sourceNames, sinkNames, sanitizerNames));
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Applique le marquage sémantique (SOURCE/SINK/SANITIZER) sur un token.
     */
    private Token applySemanticTag(Token token, Set<String> sourceNames,
                                    Set<String> sinkNames, Set<String> sanitizerNames) {
        // Les superglobales sont toujours des sources
        if (token.type() == TokenType.SUPERGLOBAL) {
            return new Token(TokenType.SOURCE, token.value(), token.line(), token.column());
        }

        // Les variables dont le nom est dans les sources configurées
        if (token.type() == TokenType.VARIABLE && sourceNames.contains(token.value())) {
            return new Token(TokenType.SOURCE, token.value(), token.line(), token.column());
        }

        // Les identifiants correspondant à des sinks
        if (token.type() == TokenType.IDENTIFIER && sinkNames.contains(token.value())) {
            return new Token(TokenType.SINK, token.value(), token.line(), token.column());
        }

        // Les identifiants correspondant à des sanitizers
        if (token.type() == TokenType.IDENTIFIER && sanitizerNames.contains(token.value())) {
            return new Token(TokenType.SANITIZER, token.value(), token.line(), token.column());
        }

        return token;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES — LECTURE DES TOKENS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Lit le prochain token dans le flux.
     */
    private Token nextToken() {
        int startLine = line;
        int startCol = column;
        char c = peek();

        // Délimiteurs PHP
        if (c == '<' && lookingAt("<?php")) {
            consume("<?php");
            return new Token(TokenType.OPEN_TAG, "<?php", startLine, startCol);
        }
        if (c == '<' && lookingAt("<?=")) {
            consume("<?=");
            return new Token(TokenType.OPEN_TAG, "<?=", startLine, startCol);
        }
        if (c == '?' && lookingAt("?>")) {
            consume("?>");
            return new Token(TokenType.CLOSE_TAG, "?>", startLine, startCol);
        }

        // Commentaires
        if (c == '/' && pos + 1 < source.length()) {
            char next = source.charAt(pos + 1);
            if (next == '/') {
                return readLineComment(startLine, startCol);
            }
            if (next == '*') {
                return readBlockComment(startLine, startCol);
            }
        }
        if (c == '#') {
            return readShellComment(startLine, startCol);
        }

        // Chaînes de caractères
        if (c == '\'') return readSingleQuotedString(startLine, startCol);
        if (c == '"') return readDoubleQuotedString(startLine, startCol);

        // Nombres
        if (Character.isDigit(c)) return readNumber(startLine, startCol);
        // Nombre commençant par un point (.5)
        if (c == '.' && pos + 1 < source.length() && Character.isDigit(source.charAt(pos + 1))) {
            return readNumber(startLine, startCol);
        }

        // Variables et superglobales
        if (c == '$') return readVariable(startLine, startCol);

        // Identifiants et mots-clés
        if (Character.isLetter(c) || c == '_') return readIdentifierOrKeyword(startLine, startCol);

        // Opérateurs et délimiteurs
        return readOperatorOrDelimiter(startLine, startCol);
    }

    /**
     * Lit un commentaire sur une ligne (// ...).
     */
    private Token readLineComment(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && peek() != '\n') {
            sb.append(advance());
        }
        return new Token(TokenType.COMMENT_LINE, sb.toString(), startLine, startCol);
    }

    /**
     * Lit un commentaire multi-lignes (/* ... *​/).
     */
    private Token readBlockComment(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // /
        sb.append(advance()); // *

        while (pos < source.length()) {
            if (peek() == '*' && pos + 1 < source.length() && source.charAt(pos + 1) == '/') {
                sb.append(advance()); // *
                sb.append(advance()); // /
                return new Token(TokenType.COMMENT_BLOCK, sb.toString(), startLine, startCol);
            }
            sb.append(advance());
        }
        throw new LexerException("Commentaire bloc non terminé (*/ manquant)", startLine, startCol);
    }

    /**
     * Lit un commentaire shell (# ...).
     */
    private Token readShellComment(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && peek() != '\n') {
            sb.append(advance());
        }
        return new Token(TokenType.COMMENT_SHELL, sb.toString(), startLine, startCol);
    }

    /**
     * Lit une chaîne à guillemets simples (pas d'interpolation).
     */
    private Token readSingleQuotedString(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // ' ouvrant

        while (pos < source.length()) {
            char c = peek();
            if (c == '\\') {
                sb.append(advance()); // backslash
                if (pos < source.length()) sb.append(advance()); // caractère échappé
            } else if (c == '\'') {
                sb.append(advance()); // ' fermant
                return new Token(TokenType.STRING_SINGLE, sb.toString(), startLine, startCol);
            } else {
                sb.append(advance());
            }
        }
        throw new LexerException("Chaîne simple non terminée (' manquant)", startLine, startCol);
    }

    /**
     * Lit une chaîne à guillemets doubles (interpolation traitée comme un seul token pour l'instant).
     */
    private Token readDoubleQuotedString(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // " ouvrant

        while (pos < source.length()) {
            char c = peek();
            if (c == '\\') {
                sb.append(advance()); // backslash
                if (pos < source.length()) sb.append(advance()); // caractère échappé
            } else if (c == '"') {
                sb.append(advance()); // " fermant
                return new Token(TokenType.STRING_DOUBLE, sb.toString(), startLine, startCol);
            } else {
                sb.append(advance());
            }
        }
        throw new LexerException("Chaîne double non terminée (\" manquant)", startLine, startCol);
    }

    /**
     * Lit un nombre entier ou flottant.
     */
    private Token readNumber(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        // Partie entière
        while (pos < source.length() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        // Point décimal
        if (pos < source.length() && peek() == '.') {
            // Vérifier que ce n'est pas l'opérateur de concaténation
            if (pos + 1 < source.length() && Character.isDigit(source.charAt(pos + 1))) {
                hasDot = true;
                sb.append(advance()); // .
                while (pos < source.length() && Character.isDigit(peek())) {
                    sb.append(advance());
                }
            }
        }

        // Notation scientifique (e/E)
        if (pos < source.length() && (peek() == 'e' || peek() == 'E')) {
            hasDot = true; // considéré comme flottant
            sb.append(advance()); // e/E
            if (pos < source.length() && (peek() == '+' || peek() == '-')) {
                sb.append(advance());
            }
            while (pos < source.length() && Character.isDigit(peek())) {
                sb.append(advance());
            }
        }

        return new Token(hasDot ? TokenType.NUMBER_FLOAT : TokenType.NUMBER_INT,
                sb.toString(), startLine, startCol);
    }

    /**
     * Lit une variable ($nom) ou une superglobale ($_GET, etc.).
     */
    private Token readVariable(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // $

        while (pos < source.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String value = sb.toString();
        if (SUPERGLOBALS.contains(value)) {
            return new Token(TokenType.SUPERGLOBAL, value, startLine, startCol);
        }
        return new Token(TokenType.VARIABLE, value, startLine, startCol);
    }

    /**
     * Lit un identifiant ou un mot-clé PHP.
     */
    private Token readIdentifierOrKeyword(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String value = sb.toString();

        // Booléens
        if (value.equals("true") || value.equals("false") ||
                value.equals("TRUE") || value.equals("FALSE")) {
            return new Token(TokenType.BOOLEAN, value, startLine, startCol);
        }

        // Null
        if (value.equals("null") || value.equals("NULL")) {
            return new Token(TokenType.NULL_LITERAL, value, startLine, startCol);
        }

        // Mots-clés
        if (KEYWORDS.contains(value)) {
            return new Token(TokenType.KEYWORD, value, startLine, startCol);
        }

        return new Token(TokenType.IDENTIFIER, value, startLine, startCol);
    }

    /**
     * Lit un opérateur ou un délimiteur.
     * Gère les opérateurs multi-caractères en consommant le plus long possible.
     */
    private Token readOperatorOrDelimiter(int startLine, int startCol) {
        char c = advance();

        switch (c) {
            // Délimiteurs simples
            case '(' -> { return new Token(TokenType.LPAREN, "(", startLine, startCol); }
            case ')' -> { return new Token(TokenType.RPAREN, ")", startLine, startCol); }
            case '{' -> { return new Token(TokenType.LBRACE, "{", startLine, startCol); }
            case '}' -> { return new Token(TokenType.RBRACE, "}", startLine, startCol); }
            case '[' -> { return new Token(TokenType.LBRACKET, "[", startLine, startCol); }
            case ']' -> { return new Token(TokenType.RBRACKET, "]", startLine, startCol); }
            case ';' -> { return new Token(TokenType.SEMICOLON, ";", startLine, startCol); }
            case ',' -> { return new Token(TokenType.COMMA, ",", startLine, startCol); }
            case '~' -> { return new Token(TokenType.IDENTIFIER, "~", startLine, startCol); }
            case '@' -> { return new Token(TokenType.IDENTIFIER, "@", startLine, startCol); }

            // Point (concaténation ou début de flottant)
            case '.' -> {
                if (match('.')) {
                    throw new LexerException("Opérateur '..' non supporté en PHP", startLine, startCol);
                }
                return new Token(TokenType.CONCAT, ".", startLine, startCol);
            }

            // Opérateurs d'affectation et comparaison commençant par =
            case '=' -> {
                if (match('=')) {
                    if (match('=')) return new Token(TokenType.IDENTICAL, "===", startLine, startCol);
                    return new Token(TokenType.EQ, "==", startLine, startCol);
                }
                if (match('>')) return new Token(TokenType.DOUBLE_ARROW, "=>", startLine, startCol);
                return new Token(TokenType.ASSIGN, "=", startLine, startCol);
            }

            // Opérateurs commençant par !
            case '!' -> {
                if (match('=')) {
                    if (match('=')) return new Token(TokenType.NOT_IDENTICAL, "!==", startLine, startCol);
                    return new Token(TokenType.NEQ, "!=", startLine, startCol);
                }
                return new Token(TokenType.NOT, "!", startLine, startCol);
            }

            // Opérateurs commençant par <
            case '<' -> {
                if (match('=')) {
                    if (match('>')) return new Token(TokenType.SPACESHIP, "<=>", startLine, startCol);
                    return new Token(TokenType.LE, "<=", startLine, startCol);
                }
                return new Token(TokenType.LT, "<", startLine, startCol);
            }

            // Opérateurs commençant par >
            case '>' -> {
                if (match('=')) return new Token(TokenType.GE, ">=", startLine, startCol);
                return new Token(TokenType.GT, ">", startLine, startCol);
            }

            // Opérateurs commençant par +
            case '+' -> {
                if (match('+')) return new Token(TokenType.INC, "++", startLine, startCol);
                if (match('=')) return new Token(TokenType.PLUS_ASSIGN, "+=", startLine, startCol);
                return new Token(TokenType.PLUS, "+", startLine, startCol);
            }

            // Opérateurs commençant par -
            case '-' -> {
                if (match('-')) return new Token(TokenType.DEC, "--", startLine, startCol);
                if (match('>')) return new Token(TokenType.ARROW, "->", startLine, startCol);
                if (match('=')) return new Token(TokenType.MINUS_ASSIGN, "-=", startLine, startCol);
                return new Token(TokenType.MINUS, "-", startLine, startCol);
            }

            // Opérateurs commençant par *
            case '*' -> {
                if (match('=')) return new Token(TokenType.MUL_ASSIGN, "*=", startLine, startCol);
                return new Token(TokenType.MUL, "*", startLine, startCol);
            }

            // Opérateurs commençant par /
            case '/' -> {
                if (match('=')) return new Token(TokenType.DIV_ASSIGN, "/=", startLine, startCol);
                return new Token(TokenType.DIV, "/", startLine, startCol);
            }

            // Opérateurs commençant par %
            case '%' -> {
                if (match('=')) return new Token(TokenType.MOD_ASSIGN, "%=", startLine, startCol);
                return new Token(TokenType.MOD, "%", startLine, startCol);
            }

            // Opérateurs logiques
            case '&' -> {
                if (match('&')) return new Token(TokenType.AND, "&&", startLine, startCol);
                throw new LexerException("Opérateur '&' (bitwise) non supporté dans ce sous-ensemble", startLine, startCol);
            }
            case '|' -> {
                if (match('|')) return new Token(TokenType.OR, "||", startLine, startCol);
                throw new LexerException("Opérateur '|' (bitwise) non supporté dans ce sous-ensemble", startLine, startCol);
            }

            // Double colonne ::
            case ':' -> {
                if (match(':')) return new Token(TokenType.DOUBLE_COLON, "::", startLine, startCol);
                return new Token(TokenType.IDENTIFIER, ":", startLine, startCol);
            }

            default -> throw new LexerException(
                    String.format("Caractère inattendu '%c' (code %d)", c, (int) c),
                    startLine, startCol);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES DE NAVIGATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Retourne le caractère actuel sans avancer.
     */
    private char peek() {
        if (pos >= source.length()) return '\0';
        return source.charAt(pos);
    }

    /**
     * Avance d'un caractère et le retourne. Met à jour ligne/colonne.
     */
    private char advance() {
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    /**
     * Si le caractère actuel correspond à celui attendu, le consomme et retourne true.
     */
    private boolean match(char expected) {
        if (pos >= source.length() || source.charAt(pos) != expected) return false;
        advance();
        return true;
    }

    /**
     * Vérifie si la position actuelle commence par le préfixe donné.
     */
    private boolean lookingAt(String prefix) {
        if (pos + prefix.length() > source.length()) return false;
        return source.startsWith(prefix, pos);
    }

    /**
     * Consomme exactement la chaîne donnée (vérifie d'abord avec lookingAt).
     */
    private void consume(String s) {
        for (int i = 0; i < s.length(); i++) {
            advance();
        }
    }

    /**
     * Ignore les espaces, tabulations, retours à la ligne.
     */
    private void skipWhitespace() {
        while (pos < source.length()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else {
                break;
            }
        }
    }
}
