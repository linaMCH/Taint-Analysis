package com.teintescope.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration : DemoData doit produire de vrais tokens via le lexer,
 * à partir du fichier PHP de démonstration (src/main/resources/samples/connexion.php).
 */
class DemoDataTest {

    @Test
    void tokensProviennentDuLexer() {
        List<TokenInfo> tokens = DemoData.tokens();

        assertFalse(tokens.isEmpty(), "Le fichier de démonstration doit produire des tokens");
        assertTrue(tokens.stream().anyMatch(t -> t.type().equals("OPEN_TAG") && t.value().equals("<?php")));
        assertTrue(tokens.stream().anyMatch(t -> t.type().equals("CLOSE_TAG") && t.value().equals("?>")));
    }

    @Test
    void tokensSourceEtSinkMarques() {
        List<TokenInfo> tokens = DemoData.tokens();

        assertTrue(tokens.stream().anyMatch(t -> t.type().equals("SOURCE")
                        && t.value().equals("$_POST") && t.source()),
                "$_POST doit être marqué SOURCE");
        assertTrue(tokens.stream().anyMatch(t -> t.type().equals("SINK")
                        && t.value().equals("mysqli_query")),
                "mysqli_query doit être marqué SINK");
    }

    @Test
    void positionsExactesDuFichierReel() {
        List<TokenInfo> tokens = DemoData.tokens();

        // Ligne 5 du fichier : $email = $_POST['email'];
        TokenInfo post = tokens.stream()
                .filter(t -> t.value().equals("$_POST"))
                .findFirst()
                .orElseThrow();
        assertEquals(5, post.line());
        assertEquals(10, post.column());
    }
}