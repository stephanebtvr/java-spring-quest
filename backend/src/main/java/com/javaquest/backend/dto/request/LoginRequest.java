package com.javaquest.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la requête d'authentification (login).
 *
 * Utilisé par le endpoint POST /api/auth/login.
 *
 * Exemple de payload JSON :
 * {
 *   "username": "john_doe",
 *   "password": "MySecurePass123!"
 * }
 *
 * Les annotations Jakarta Bean Validation garantissent
 * que les données reçues respectent les contraintes métier.
 */
public record LoginRequest(

        /**
         * Nom d'utilisateur unique.
         *
         * Contraintes :
         * - Ne peut pas être vide
         * - Longueur entre 3 et 50 caractères
         */
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50,
              message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        String username,

        /**
         * Mot de passe en clair (sera hashé avec BCrypt).
         *
         * Contraintes :
         * - Ne peut pas être vide
         * - Longueur entre 8 et 100 caractères
         */
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100,
              message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String password

) {
    /*
     * POURQUOI PAS DE @Pattern POUR LE MOT DE PASSE ?
     *
     * On évite d’imposer une regex stricte dans le DTO pour garder de la flexibilité.
     * Une validation avancée peut être ajoutée via une annotation custom
     * @ValidPassword si nécessaire.
     */
}
