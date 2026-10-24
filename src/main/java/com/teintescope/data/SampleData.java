package com.teintescope.data;

import com.teintescope.model.*;

import java.util.List;

import static com.teintescope.model.SegKind.*;

/**
 * Données d'exemple pour la maquette.
 * Plus tard, ces données viendront de l'analyseur (lexer, parser, analyse de teinte).
 */
public final class SampleData {
    private SampleData() {}

    public static final String PROJECT = "boutique-php";

    public static List<ProjectFile> files() {
        return List.of(
                new ProjectFile("index.php", FileState.CLEAN),
                new ProjectFile("connexion.php", FileState.DANGER),
                new ProjectFile("utilisateurs.php", FileState.WARNING),
                new ProjectFile("profil.php", FileState.CLEAN),
                new ProjectFile("recherche.php", FileState.DANGER),
                new ProjectFile("base.php", FileState.CLEAN));
    }

    public static List<Vulnerability> vulnerabilities() {
        return List.of(
                new Vulnerability(Severity.CRITIQUE, "Injection SQL", "connexion.php", 18,
                        "Entrée utilisateur injectée sans filtrage dans la requête."),
                new Vulnerability(Severity.ELEVEE, "Injection SQL", "recherche.php", 24,
                        "Paramètre de recherche concaténé directement."),
                new Vulnerability(Severity.MOYENNE, "Propagation risquée", "utilisateurs.php", 42,
                        "Valeur teintée transmise à une fonction non vérifiée."));
    }

    public static List<CodeLine> linesOf(String file) {
        return switch (file) {
            case "connexion.php" -> connexion();
            case "recherche.php" -> recherche();
            default -> List.of(
                    l(1, LineKind.NORMAL, s("// Aperçu d'exemple : " + file, PLAIN)));
        };
    }

    private static List<CodeLine> connexion() {
        return List.of(
                l(11, LineKind.SOURCE, p("$email = "), s("$_POST['email']", SOURCE_VAR), p(";")),
                l(12, LineKind.SOURCE, p("$motDePasse = "), s("$_POST['mot_de_passe']", SOURCE_VAR), p(";")),
                l(13, LineKind.NORMAL),
                l(14, LineKind.FLOW, s("if", KEYWORD), p(" (!empty("), s("$email", TAINTED), p(")) {")),
                l(15, LineKind.FLOW, p("$requete = "), s("\"SELECT * FROM utilisateurs\"", STRING), p(";")),
                l(16, LineKind.FLOW, s("$requete", TAINTED), p(" .= "), s("\" WHERE email = '\"", STRING),
                        p(" . "), s("$email", TAINTED), p(" . "), s("\"'\"", STRING), p(";")),
                l(17, LineKind.NORMAL),
                l(18, LineKind.SINK, p("$resultat = "), s("mysqli_query($connexion, $requete);", SINK_CALL)),
                l(19, LineKind.NORMAL, s("if", KEYWORD), p(" ($resultat) {")),
                l(20, LineKind.NORMAL, p("    "), s("return", KEYWORD), p(" mysqli_fetch_assoc($resultat);")),
                l(21, LineKind.NORMAL, p("}")));
    }

    private static List<CodeLine> recherche() {
        return List.of(
                l(21, LineKind.SOURCE, p("$terme = "), s("$_GET['q']", SOURCE_VAR), p(";")),
                l(22, LineKind.NORMAL),
                l(23, LineKind.FLOW, p("$sql = "), s("\"SELECT * FROM produits WHERE nom LIKE '%\"", STRING),
                        p(" . "), s("$terme", TAINTED), p(" . "), s("\"%'\"", STRING), p(";")),
                l(24, LineKind.SINK, p("$liste = "), s("mysqli_query($connexion, $sql);", SINK_CALL)));
    }

    // petits raccourcis pour écrire les exemples
    private static CodeSegment s(String text, SegKind kind) { return new CodeSegment(text, kind); }
    private static CodeSegment p(String text) { return new CodeSegment(text, PLAIN); }
    private static CodeLine l(int number, LineKind kind, CodeSegment... segments) {
        return new CodeLine(number, kind, List.of(segments));
    }
}
