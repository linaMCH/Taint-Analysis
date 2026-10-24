# TeinteScope : interface principale (JavaFX)

Projet Maven (JDK 21 + JavaFX 21).

## Lancer
- IntelliJ : ouvrir `pom.xml` (« Open as Project »), attendre l'import Maven, puis lancer `Launcher.main()`.
- Ou en ligne de commande : `mvn javafx:run`

## Structure
- `model/`  : enregistrements de données (fichiers, lignes de code, vulnérabilités)
- `data/`   : données d'exemple (à remplacer plus tard par les résultats de l'analyseur)
- `ui/`     : composants d'interface (un fichier par zone de l'écran)
- `resources/styles/teintescope.css` : couleurs et styles
- `resources/fonts/` : polices facultatives (Manrope, DM Mono)

## Prochaines étapes
Chercher les `TODO` dans `ui/MainView.java` : chaque bouton y est relié à un message temporaire.
