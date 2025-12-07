package com.javaquest.backend.entity.enums;
/**
 * Énumération des rôles utilisateurs dans l'application.
 * 
 * Ces rôles sont utilisés pour :
 * - Contrôler l'accès aux ressources (Spring Security)
 * - Déterminer les permissions (création de questions, modération, etc.)
 * - Afficher/masquer des fonctionnalités dans le frontend
 * 
 */
public enum RoleEnum {
     /**
     * Utilisateur standard.
     * Peut :
     * - Passer des quiz
     * - Voir son historique
     * - Consulter le leaderboard
     */
    USER,
     /**
     * Administrateur.
     * Peut :
     * - Tout ce que USER peut faire
     * - Créer/modifier/supprimer des questions
     * - Créer/modifier/supprimer des quiz
     * - Gérer les utilisateurs
     * - Accéder aux statistiques globales
     */
    ADMIN;

     /**
     * Vérifie si le rôle a des privilèges administrateur.
     * 
     * @return true si ADMIN, false sinon
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

     /**
     * Retourne le nom du rôle avec le préfixe ROLE_ pour Spring Security.
     * Spring Security attend les rôles au format "ROLE_XXX".
     * 
     * @return le nom du rôle préfixé (ex: "ROLE_USER", "ROLE_ADMIN")
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
