package com.teintescope.lexer;

import com.teintescope.model.ConfigModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le lexer TaintScope.
 * Couvre les cas de base, les positions, les erreurs et le marquage sémantique.
 */
class LexerTest {

    @Test
    void testInjectionSqlBasique() {
        String code = "<?php\n" +
                "$email = $_POST['email'];\n" +
                "$requete = \"SELECT * FROM utilisateurs WHERE email = '\" . $email . \"'\";\n" +
                "$resultat = mysqli_query($connexion, $requete);\n" +
                "?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize(ConfigModel.INSTANCE);

        // Vérifier que les tokens principaux sont présents
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.OPEN_TAG));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.VARIABLE && t.value().equals("$email")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SOURCE && t.value().equals("$_POST")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SINK && t.value().equals("mysqli_query")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.CLOSE_TAG));
    }

    @Test
    void testCommentairesIgnore() {
        String code = "<?php\n" +
                "// Commentaire ligne\n" +
                "/* Commentaire\n" +
                "   multi-lignes */\n" +
                "# Commentaire shell\n" +
                "$x = 1;";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.COMMENT_LINE));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.COMMENT_BLOCK));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.COMMENT_SHELL));
    }

    @Test
    void testChainesSimplesEtDoubles() {
        String code = "<?php $a = 'simple'; $b = \"double\"; ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.STRING_SINGLE && t.value().equals("'simple'")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.STRING_DOUBLE && t.value().equals("\"double\"")));
    }

    @Test
    void testTousOperateurs() {
        String code = "<?php $a = 1 + 2 - 3 * 4 / 5 % 6; $b = ($a == 1) && ($a != 2) || ($a === 3); ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.ASSIGN));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.PLUS));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.MINUS));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.MUL));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.DIV));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.MOD));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.EQ));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NEQ));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTICAL));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.AND));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.OR));
    }

    @Test
    void testFichierVide() {
        Lexer lexer = new Lexer("");
        List<Token> tokens = lexer.tokenize();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }

    @Test
    void testErreurLexicale() {
        String code = "<?php $x = @; ?>";

        Lexer lexer = new Lexer(code);
        // @ est toléré comme opérateur de suppression d'erreur en PHP
        assertDoesNotThrow(() -> lexer.tokenize());
    }

    @Test
    void testErreurLexicaleCaractereInvalide() {
        String code = "<?php $x = `; ?>";

        Lexer lexer = new Lexer(code);
        assertThrows(LexerException.class, () -> lexer.tokenize());
    }

    @Test
    void testNombresEntiersEtFlottants() {
        String code = "<?php $a = 42; $b = 3.14; $c = 1e10; $d = .5; ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NUMBER_INT && t.value().equals("42")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NUMBER_FLOAT && t.value().equals("3.14")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NUMBER_FLOAT && t.value().equals("1e10")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NUMBER_FLOAT && t.value().equals(".5")));
    }

    @Test
    void testSuperglobalesMarqueesSource() {
        String code = "<?php $a = $_GET['id']; $b = $_POST['name']; $c = $_COOKIE['sess']; ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize(ConfigModel.INSTANCE);

        // Les superglobales doivent être marquées SOURCE
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SOURCE && t.value().equals("$_GET")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SOURCE && t.value().equals("$_POST")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SOURCE && t.value().equals("$_COOKIE")));
    }

    @Test
    void testFonctionsSensiblesMarqueesSink() {
        String code = "<?php mysqli_query($c, $q); echo $x; exec($cmd); ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize(ConfigModel.INSTANCE);

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SINK && t.value().equals("mysqli_query")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SINK && t.value().equals("echo")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SINK && t.value().equals("exec")));
    }

    @Test
    void testPositionsLigneColonne() {
        String code = "<?php\n$x = 1;";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        // <?php à ligne 1, colonne 1
        assertEquals(1, tokens.get(0).line());
        assertEquals(1, tokens.get(0).column());

        // $x à ligne 2, colonne 1
        Token varToken = tokens.stream().filter(t -> t.value().equals("$x")).findFirst().orElseThrow();
        assertEquals(2, varToken.line());
        assertEquals(1, varToken.column());

        // = à ligne 2, colonne 4
        Token assignToken = tokens.stream().filter(t -> t.type() == TokenType.ASSIGN).findFirst().orElseThrow();
        assertEquals(2, assignToken.line());
        assertEquals(4, assignToken.column());
    }

    @Test
    void testMotsClesReconnus() {
        String code = "<?php if ($x) { return true; } else { return false; } ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.KEYWORD && t.value().equals("if")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.KEYWORD && t.value().equals("return")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.KEYWORD && t.value().equals("else")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.BOOLEAN && t.value().equals("true")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.BOOLEAN && t.value().equals("false")));
    }

    @Test
    void testOperateursCompose() {
        String code = "<?php $a === $b; $c !== $d; $e <=> $f; $g .= $h; ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTICAL));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.NOT_IDENTICAL));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SPACESHIP));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.CONCAT_ASSIGN));
    }

    @Test
    void testSanitizersMarques() {
        String code = "<?php $x = intval($y); $z = htmlspecialchars($w); ?>";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize(ConfigModel.INSTANCE);

        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SANITIZER && t.value().equals("intval")));
        assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.SANITIZER && t.value().equals("htmlspecialchars")));
    }

    @Test
    void testCommentaireBlockNonTermine() {
        String code = "<?php /* commentaire non terminé";

        Lexer lexer = new Lexer(code);
        LexerException ex = assertThrows(LexerException.class, () -> lexer.tokenize());
        assertTrue(ex.getMessage().contains("Commentaire bloc non terminé"));
    }

    @Test
    void testChaineNonTermine() {
        String code = "<?php $x = 'chaîne non terminée;";

        Lexer lexer = new Lexer(code);
        LexerException ex = assertThrows(LexerException.class, () -> lexer.tokenize());
        assertTrue(ex.getMessage().contains("Chaîne simple non terminée"));
    }
}
