package com.teintescope.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teintescope.lexer.Lexer;
import com.teintescope.lexer.Token;
import com.teintescope.lexer.TokenType;

import static com.teintescope.model.FileState.*;
import static com.teintescope.model.LineKind.*;

/**
 * DONNÉES DE DÉMONSTRATION : le moteur d'analyse n'est pas encore branché.
 * Quand le lexer/parser seront prêts, on remplacera cette classe par les vrais résultats.
 */
public final class DemoData {
    private DemoData() {}

    // ---------------------------------------------------------------- coloration
    private static final Pattern TOKEN = Pattern.compile(
            "(\"[^\"]*\"|'[^']*')"                                   // 1 chaîne
                    + "|(\\$_(?:POST|GET|COOKIE|REQUEST))"                   // 2 source
                    + "|(\\$\\w+)"                                           // 3 variable
                    + "|(\\b(?:if|else|return|echo|function|foreach|while)\\b)" // 4 mot-clé
                    + "|(\\b[A-Za-z_]\\w*(?=\\())");                         // 5 appel de fonction

    private static CodeLine code(int n, LineKind kind, String text, String... tainted) {
        List<CodeSegment> segs = new ArrayList<>();
        if (text.stripLeading().startsWith("//")) {
            segs.add(new CodeSegment(text, SegKind.COMMENT));
            return new CodeLine(n, kind, segs);
        }
        Set<String> t = new HashSet<>(Arrays.asList(tainted));
        Matcher m = TOKEN.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) segs.add(new CodeSegment(text.substring(last, m.start()), SegKind.PLAIN));
            SegKind k;
            if (m.group(1) != null) k = SegKind.STRING;
            else if (m.group(2) != null) k = SegKind.SOURCE;
            else if (m.group(3) != null) k = t.contains(m.group(3)) ? SegKind.TAINTED : SegKind.PLAIN;
            else if (m.group(4) != null) k = SegKind.KEYWORD;
            else k = kind == SINK ? SegKind.SINK : SegKind.FUNCTION;
            segs.add(new CodeSegment(m.group(), k));
            last = m.end();
        }
        if (last < text.length()) segs.add(new CodeSegment(text.substring(last), SegKind.PLAIN));
        return new CodeLine(n, kind, segs);
    }

    private static final class B {
        final List<CodeLine> out = new ArrayList<>();
        B add(LineKind k, String t, String... tv) { out.add(code(out.size() + 1, k, t, tv)); return this; }
        B at(int n, LineKind k, String t, String... tv) {
            while (out.size() < n - 1) add(NORMAL, "");
            return add(k, t, tv);
        }
        List<CodeLine> done() { return List.copyOf(out); }
    }

    // ---------------------------------------------------------------- fichiers & code
    private static final Map<String, List<CodeLine>> CODE = buildCode();

    private static Map<String, List<CodeLine>> buildCode() {
        Map<String, List<CodeLine>> m = new HashMap<>();
        m.put("connexion.php", new B()
                .add(NORMAL, "<?php")
                .add(NORMAL, "// Page de connexion")
                .add(NORMAL, "require_once 'base.php';")
                .add(NORMAL, "$connexion = new mysqli(\"localhost\", \"root\", \"\", \"boutique\");")
                .add(NORMAL, "")
                .add(NORMAL, "// Verifie les identifiants saisis")
                .add(NORMAL, "function authentifier($connexion) {")
                .add(NORMAL, "    // Champs du formulaire")
                .add(NORMAL, "    $erreurs = [];")
                .add(NORMAL, "")
                .add(SOURCE, "    $email = $_POST['email'];", "$email")
                .add(SOURCE, "    $motDePasse = $_POST['mot_de_passe'];", "$motDePasse")
                .add(NORMAL, "")
                .add(FLOW, "    if (!empty($email)) {", "$email")
                .add(FLOW, "        $requete = \"SELECT * FROM utilisateurs\";", "$requete")
                .add(FLOW, "        $requete .= \" WHERE email = '\" . $email . \"'\";", "$requete", "$email")
                .add(NORMAL, "")
                .add(SINK, "        $resultat = mysqli_query($connexion, $requete);", "$requete")
                .add(NORMAL, "        if ($resultat) {")
                .add(NORMAL, "            return mysqli_fetch_assoc($resultat);")
                .add(NORMAL, "        }")
                .add(NORMAL, "    }")
                .add(NORMAL, "}")
                .done());
        m.put("recherche.php", new B()
                .add(NORMAL, "<?php")
                .add(NORMAL, "// Recherche de produits")
                .add(NORMAL, "require_once 'base.php';")
                .at(21, SOURCE, "$q = $_GET['q'];", "$q")
                .add(FLOW, "$titre = \"Resultats pour : \" . $q;", "$titre", "$q")
                .add(NORMAL, "")
                .add(SINK, "echo $titre;", "$titre")
                .done());
        m.put("utilisateurs.php", new B()
                .add(NORMAL, "<?php")
                .add(NORMAL, "// Fiche utilisateur")
                .at(30, SOURCE, "$id = $_GET['id'];", "$id")
                .at(35, FLOW, "$q = \"SELECT * FROM utilisateurs WHERE id = \" . $id;", "$q", "$id")
                .at(42, SINK, "$res = mysqli_query($c, $q);", "$q")
                .done());
        m.put("index.php", new B().add(NORMAL, "<?php").add(NORMAL, "// Page d accueil")
                .add(NORMAL, "require_once 'base.php';").add(NORMAL, "echo \"Bienvenue\";").done());
        m.put("profil.php", new B().add(NORMAL, "<?php").add(NORMAL, "// Profil")
                .add(NORMAL, "require_once 'base.php';").done());
        m.put("base.php", new B().add(NORMAL, "<?php").add(NORMAL, "// Configuration de la base")
                .add(NORMAL, "$hote = \"localhost\";").done());
        return m;
    }

    public static List<ProjectFile> files() {
        return List.of(new ProjectFile("index.php", CLEAN), new ProjectFile("connexion.php", DANGER),
                new ProjectFile("utilisateurs.php", WARNING), new ProjectFile("profil.php", CLEAN),
                new ProjectFile("recherche.php", DANGER), new ProjectFile("base.php", CLEAN));
    }

    public static List<CodeLine> code(String file) { return CODE.getOrDefault(file, List.of()); }

    public static String scopeOf(String file) {
        return switch (file) {
            case "connexion.php" -> "authentifier()";
            default -> null;
        };
    }

    public static List<String> recentProjects() { return List.of("boutique-php|Ouvert hier"); }

    // ---------------------------------------------------------------- vulnérabilités
    private static final List<Vulnerability> VULNS = buildVulns();

    private static List<Vulnerability> buildVulns() {
        TraceStep noSanitizer = new TraceStep(StepKind.SANITIZER, 0, "—",
                "Aucun assainisseur rencontré sur ce chemin", TaintState.TAINTED);
        return List.of(
                new Vulnerability("Injection SQL", VulnCategory.SQLI, Severity.CRITIQUE, "connexion.php", 18,
                        "Une entrée utilisateur atteint directement une requête SQL.",
                        List.of(
                                new TraceStep(StepKind.SOURCE, 11, "$_POST['email']", "Entrée utilisateur non fiable", TaintState.TAINTED),
                                new TraceStep(StepKind.PROPAGATION, 14, "$email", "La valeur teintée circule dans la variable", TaintState.TAINTED),
                                new TraceStep(StepKind.TRANSFORMATION, 16, "$requete .= $email", "Concaténation : la teinte est transmise à la requête", TaintState.TAINTED),
                                noSanitizer,
                                new TraceStep(StepKind.SINK, 18, "mysqli_query($connexion, $requete)", "Fonction sensible atteinte par une donnée teintée", TaintState.TAINTED)),
                        "Utilisez une requête préparée (mysqli_prepare + bind_param) ou assainissez la valeur avant la concaténation."),
                new Vulnerability("Cross-Site Scripting", VulnCategory.XSS, Severity.ELEVEE, "recherche.php", 24,
                        "Une valeur non échappée est affichée dans la réponse HTML.",
                        List.of(
                                new TraceStep(StepKind.SOURCE, 21, "$_GET['q']", "Paramètre d'URL non fiable", TaintState.TAINTED),
                                new TraceStep(StepKind.TRANSFORMATION, 22, "$titre = \"...\" . $q", "Concaténation dans un titre", TaintState.TAINTED),
                                noSanitizer,
                                new TraceStep(StepKind.SINK, 24, "echo $titre", "Affichage HTML sans échappement", TaintState.TAINTED)),
                        "Échappez la sortie : htmlspecialchars($titre, ENT_QUOTES)."),
                new Vulnerability("Injection SQL", VulnCategory.SQLI, Severity.MOYENNE, "utilisateurs.php", 42,
                        "Paramètre de recherche concaténé directement dans la requête.",
                        List.of(
                                new TraceStep(StepKind.SOURCE, 30, "$_GET['id']", "Paramètre d'URL non fiable", TaintState.TAINTED),
                                new TraceStep(StepKind.TRANSFORMATION, 35, "$q = \"...\" . $id", "Concaténation dans la requête", TaintState.TAINTED),
                                noSanitizer,
                                new TraceStep(StepKind.SINK, 42, "mysqli_query($c, $q)", "Exécution SQL avec donnée teintée", TaintState.TAINTED)),
                        "Convertissez l'identifiant : intval($id) avant de construire la requête."));
    }

    public static List<Vulnerability> vulnerabilities() { return VULNS; }

    // ---------------------------------------------------------------- détails techniques
        /** Fichier PHP de démonstration chargé depuis le classpath (src/main/resources/samples). */
        private static final String SAMPLE_RESOURCE = "/samples/connexion.php";

        /** Contenu de secours si la ressource est absente du classpath (build incomplet). */
        private static final String SAMPLE_FALLBACK = """
                <?php
                // Page de connexion
                $email = $_POST['email'];
                $requete = "SELECT * FROM utilisateurs WHERE email = '" . $email . "'";
                $resultat = mysqli_query($connexion, $requete);
                ?>
                """;

        /**
         * Tokens réels du fichier de démonstration, produits par le lexer.
         * Remplace les anciennes données factices.
         */
        public static List<TokenInfo> tokens() {
            List<Token> tokens = new Lexer(loadSample()).tokenize(ConfigModel.INSTANCE);
            return tokens.stream()
                    .filter(t -> t.type() != TokenType.EOF)
                    .map(t -> new TokenInfo(t.type().name(), t.value(), t.line(), t.column(), t.isSource()))
                    .toList();
        }

        /** Lit le fichier PHP d'exemple depuis le classpath. */
        private static String loadSample() {
            try (InputStream in = DemoData.class.getResourceAsStream(SAMPLE_RESOURCE)) {
                if (in == null) return SAMPLE_FALLBACK;
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return SAMPLE_FALLBACK;
            }
        }

    public static AstNode ast() {
        return AstNode.of("Programme", "connexion.php",
                AstNode.of("Fonction", "authentifier($connexion)",
                        AstNode.of("Affectation", "$email ← $_POST['email']"),
                        AstNode.of("Affectation", "$motDePasse ← $_POST['mot_de_passe']"),
                        AstNode.of("Condition", "!empty($email)",
                                AstNode.of("Concaténation", "$requete .= $email")),
                        AstNode.of("Appel", "mysqli_query($connexion, $requete)")));
    }

    public static List<SymbolEntry> symbols() {
        return List.of(new SymbolEntry("$email", "chaîne", "authentifier()", 11, TaintState.TAINTED),
                new SymbolEntry("$motDePasse", "chaîne", "authentifier()", 12, TaintState.TAINTED),
                new SymbolEntry("$requete", "chaîne", "authentifier()", 15, TaintState.TAINTED),
                new SymbolEntry("$connexion", "mysqli", "global", 4, TaintState.SAFE));
    }

    public static List<LogEntry> logs() {
        return List.of(new LogEntry("14:32:04", "LEXICAL", "WARN", "1 token ambigu normalisé"),
                new LogEntry("14:32:05", "SYNTAXE", "INFO", "Aucun problème de syntaxe détecté"),
                new LogEntry("14:32:06", "SÉMANTIQUE", "INFO", "Analyse de 6 fichiers terminée"));
    }

    public static List<StepInfo> pipeline() {
        return List.of(new StepInfo("Analyse lexicale", "Terminé · 0,3 s", StepStatus.DONE),
                new StepInfo("Analyse syntaxique", "Terminé · 0,8 s", StepStatus.DONE),
                new StepInfo("Analyse sémantique", "Terminé · 1,2 s", StepStatus.DONE),
                new StepInfo("Propagation de teinte", "Terminé · 2,4 s", StepStatus.DONE),
                new StepInfo("Rapport", "Terminé · 0,4 s", StepStatus.DONE));
    }

    public static ReportData report(String project) {
        return new ReportData(project, LocalDateTime.now(), 4.7, files().size(), 1248, 10, 32, VULNS);
    }
}