<?php
// Page de connexion — fichier d'exemple pour la démonstration du lexer
require_once 'base.php';

$email = $_POST['email'];
$motDePasse = $_POST['mot_de_passe'];

if (!empty($email)) {
    $requete = "SELECT * FROM utilisateurs";
    $requete .= " WHERE email = '" . $email . "'";

    $resultat = mysqli_query($connexion, $requete);

    if ($resultat) {
        return mysqli_fetch_assoc($resultat);
    }
}
$id = intval($_GET['id']);
?>