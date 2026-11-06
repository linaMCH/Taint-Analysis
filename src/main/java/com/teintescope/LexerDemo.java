package com.teintescope;

import com.teintescope.lexer.Lexer;
import com.teintescope.lexer.LexerException;
import com.teintescope.lexer.Token;
import com.teintescope.model.ConfigModel;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Démo en ligne de commande du lexer TaintScope.
 * Lit un fichier PHP, affiche tous les tokens avec positions,
 * et marque les sources/sinks/sanitizers selon la configuration.
 */
public class LexerDemo {

    public static void main(String[] args) {
        // Force l'UTF-8 : les accents et les caractères de cadre seraient remplacés
        // par '?' avec l'encodage par défaut de la console Windows.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        if (args.length < 1) {
            System.err.println("Usage: LexerDemo <fichier.php>");
            System.err.println("Exemple: LexerDemo connexion.php");
            System.exit(1);
        }

        String filePath = args[0];
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            System.err.println("Erreur : fichier introuvable : " + filePath);
            System.exit(1);
        }

        try {
            String source = Files.readString(path);
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("  TAINTSCOPE — Analyse lexicale de : " + path.getFileName());
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println();

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize(ConfigModel.INSTANCE);

            System.out.printf(" %-6s %-8s %-20s %s%n", "LIGNE", "COL", "TYPE", "VALEUR");
            System.out.println("─────────────────────────────────────────────────────────────");

            int sourceCount = 0;
            int sinkCount = 0;
            int sanitizerCount = 0;

            for (Token token : tokens) {
                if (token.type().name().equals("EOF")) continue;

                String marker = "";
                if (token.isSource()) {
                    marker = " [SOURCE]";
                    sourceCount++;
                } else if (token.isSink()) {
                    marker = " [SINK]";
                    sinkCount++;
                } else if (token.isSanitizer()) {
                    marker = " [SANITIZER]";
                    sanitizerCount++;
                }

                String displayValue = token.value().length() > 30
                        ? token.value().substring(0, 27) + "..."
                        : token.value();

                System.out.printf(" %-6d %-8d %-20s %s%s%n",
                        token.line(), token.column(), token.type(), displayValue, marker);
            }

            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.printf("Total : %d tokens (%d sources, %d sinks, %d sanitizers)%n",
                    tokens.size() - 1, sourceCount, sinkCount, sanitizerCount);
            System.out.println("═══════════════════════════════════════════════════════════════");

        } catch (LexerException e) {
            System.err.println("Erreur lexicale : " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
            System.exit(3);
        }
    }
}
