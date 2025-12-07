package com.javaquest.backend.entity;

import com.javaquest.backend.entity.enums.RoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entité représentant un utilisateur de l'application.
 * 
 * Cette classe implémente UserDetails (interface Spring Security)
 * pour permettre l'authentification et l'autorisation.
 * 
 * Relations :
 * - Un User peut avoir plusieurs Scores (OneToMany)
 * - Un User peut créer plusieurs Quiz (OneToMany)
 * 
 * Sécurité :
 * - Le mot de passe est hashé avec BCrypt (jamais stocké en clair)
 * - Le username et l'email sont uniques
 * - Les validations empêchent les données invalides
 * 
 * Table en base de données : "users" (nom au pluriel par convention)
 * 
 * @author Ton Nom
 * @version 1.0
 * @since 2024-12-05
 */
@Entity
@Table(name = "users")  // "user" est un mot réservé en SQL, donc on utilise "users"
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Pattern Builder pour créer des instances facilement
public class User extends BaseEntity implements UserDetails {
    
    /**
     * Nom d'utilisateur unique.
     * 
     * Contraintes :
     * - Obligatoire (NotBlank)
     * - Entre 3 et 50 caractères
     * - Unique en base de données (index)
     * 
     * Utilisé pour la connexion et l'affichage public.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * Adresse email unique.
     * 
     * Contraintes :
     * - Obligatoire
     * - Format email valide (@Email)
     * - Unique en base de données
     * - Maximum 100 caractères
     * 
     * Peut être utilisé pour :
     * - Réinitialisation de mot de passe
     * - Notifications
     * - Connexion alternative
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * Mot de passe hashé avec BCrypt.
     * 
     * ⚠️ ATTENTION : Ce champ contient le hash, JAMAIS le mot de passe en clair !
     * 
     * Processus :
     * 1. L'utilisateur envoie son mot de passe (HTTPS obligatoire)
     * 2. Le backend hash le mot de passe avec BCryptPasswordEncoder
     * 3. Seul le hash est stocké en base
     * 4. À la connexion, on compare le hash du mot de passe saisi avec celui en base
     * 
     * BCrypt génère automatiquement un "salt" aléatoire pour chaque hash,
     * rendant impossible les attaques par rainbow tables.
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;  // Hash BCrypt (ex: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
    
    /**
     * Rôle de l'utilisateur (USER ou ADMIN).
     * 
     * @Enumerated(EnumType.STRING) stocke le nom de l'enum ("USER", "ADMIN")
     * plutôt que l'ordinal (0, 1) qui est fragile aux changements d'ordre.
     * 
     * Par défaut : USER (défini dans le constructeur ou le builder)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleEnum role = RoleEnum.USER;  // Valeur par défaut
    
    /**
     * Indique si le compte est actif.
     * 
     * Un compte désactivé ne peut pas se connecter.
     * Utilisé pour :
     * - Suspensions temporaires
     * - Suppression "soft" (sans perdre les données)
     * - Validation d'email (compte inactif tant que l'email n'est pas vérifié)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // Actif par défaut
    
    /**
     * Indique si le compte est verrouillé (trop de tentatives de connexion échouées).
     * 
     * Sécurité contre le brute force.
     * Peut être déverrouillé après un certain délai ou par un admin.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    // ==========================================
    // MÉTHODES SPRING SECURITY (UserDetails)
    // ==========================================
    
    /**
     * Retourne les autorités (rôles + permissions) de l'utilisateur.
     * 
     * Spring Security utilise cette méthode pour vérifier les autorisations.
     * 
     * Format : "ROLE_USER" ou "ROLE_ADMIN"
     * 
     * @return Collection d'autorités (un seul rôle dans notre cas)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertit l'enum Role en GrantedAuthority
        return List.of(new SimpleGrantedAuthority(role.getRoleName()));
    }
    
    /**
     * Retourne le mot de passe hashé.
     * 
     * Spring Security compare ce hash avec le mot de passe fourni lors du login.
     * 
     * @return le hash BCrypt
     */
    @Override
    public String getPassword() {
        return password;
    }
    
    /**
     * Retourne le nom d'utilisateur.
     * 
     * Utilisé par Spring Security pour identifier l'utilisateur.
     * 
     * @return le username
     */
    @Override
    public String getUsername() {
        return username;
    }
    
    /**
     * Indique si le compte a expiré.
     * 
     * Dans notre cas, les comptes n'expirent jamais.
     * Tu pourrais implémenter une logique d'expiration après X mois d'inactivité.
     * 
     * @return toujours true (jamais expiré)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;  // Pas d'expiration dans notre MVP
    }
    
    /**
     * Indique si le compte est verrouillé.
     * 
     * @return true si le compte n'est PAS verrouillé
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    /**
     * Indique si les identifiants (mot de passe) ont expiré.
     * 
     * Dans notre cas, les mots de passe n'expirent jamais.
     * Une bonne pratique serait de forcer le changement tous les 90 jours.
     * 
     * @return toujours true (jamais expiré)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Pas d'expiration de mot de passe dans notre MVP
    }
    
    /**
     * Indique si le compte est activé.
     * 
     * @return true si le compte est actif
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================
    
    /**
     * Vérifie si l'utilisateur a le rôle ADMIN.
     * 
     * Méthode utilitaire pour simplifier les vérifications dans le code.
     * 
     * @return true si ADMIN, false sinon
     */
    public boolean isAdmin() {
        return role == RoleEnum.ADMIN;
    }
    
    /**
     * Retourne une version "safe" de l'utilisateur sans le mot de passe.
     * 
     * Utile pour les logs ou le debugging.
     * 
     * @return représentation textuelle sans données sensibles
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
        // Pas de mot de passe dans le toString pour la sécurité !
    }
}