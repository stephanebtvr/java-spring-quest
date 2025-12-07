package com.javaquest.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la requête d'inscription (register).
 *
 * Utilisé par le endpoint POST /api/auth/register.
 *
 * Exemple de payload JSON :
 * {
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "password": "MySecurePass123!"
 * }
 *
 * Ce DTO contient plus de validations que LoginRequest car on crée un nouvel utilisateur.
 * L'unicité du username et de l'email est vérifiée côté service.
 */
public record RegisterRequest(

        /**
         * Nom d'utilisateur unique (alphanumérique + underscore).
         *
         * Contraintes :
         * - Ne peut pas être vide
         * - Longueur entre 3 et 50 caractères
         * - Format alphanumérique avec underscore autorisé
         *
         * Regex : ^[a-zA-Z0-9_]+$
         */
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50,
              message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores"
        )
        String username,

        /**
         * Adresse email unique et valide.
         *
         * Contraintes :
         * - Ne peut pas être vide
         * - Format email valide (RFC 5322)
         */
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        /**
         * Mot de passe en clair (sera hashé avec BCrypt).
         *
         * Contraintes :
         * - Ne peut pas être vide
         * - Longueur entre 8 et 100 caractères
         *
         * IMPORTANT :
         * - Ne jamais logger ou afficher le mot de passe en clair.
         */
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100,
              message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String password

) {
    /*
     * VALIDATION MÉTIER SUPPLÉMENTAIRE (dans le Service) :
     *
     * 1. Vérification de l'unicité du username.
     * 2. Vérification de l'unicité de l'email.
     * 3. Vérification optionnelle de la force du mot de passe.
     *
     * Ces validations nécessitent un accès à la base de données
     * et ne doivent donc pas être dans le DTO.
     */
}
