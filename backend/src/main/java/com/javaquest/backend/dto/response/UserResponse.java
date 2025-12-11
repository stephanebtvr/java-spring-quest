package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO pour représenter un utilisateur dans l'API REST.
 *
 * Utilisé par les endpoints GET /api/users/{id}, /api/users, /api/profiles/me
 *
 * Exemple de payload JSON :
 * {
 *   "id": 1,
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "role": "USER",
 *   "enabled": true,
 *   "createdAt": "2024-12-01T10:30:00"
 * }
 *
 * Bonnes pratiques :
 * - Ne jamais exposer le mot de passe hashé
 * - Encapsuler les informations sensibles
 * - Conversion automatique via MapStruct depuis l'entité User
 */
public record UserResponse(
        /**
         * ID unique de l'utilisateur (clé primaire)
         * Généré automatiquement par la base de données
         */
        Long id,

        /**
         * Nom d'utilisateur unique
         * Utilisé pour l'authentification et l'affichage
         */
        String username,

        /**
         * Email de l'utilisateur
         * Doit être unique
         * Format validé lors de l'inscription
         */
        String email,

        /**
         * Rôle de l'utilisateur (USER ou ADMIN)
         * Conversion de l'enum Role vers String
         * @see com.javaquest.backend.entity.enums.Role
         */
        String role,

        /**
         * Indique si le compte est actif
         * false si l'utilisateur a été désactivé ou banni
         */
        Boolean enabled,

        /**
         * Date et heure de création du compte
         * Utile pour audit et statistiques
         */
        LocalDateTime createdAt
) {}
