package com.javaquest.backend.repository;

import com.javaquest.backend.entity.User;
import com.javaquest.backend.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs.
 * 
 * Spring Data JPA génère automatiquement l'implémentation de cette interface.
 * Les méthodes suivent la convention de nommage pour générer des requêtes SQL automatiques.
 * 
 * Méthodes héritées de JpaRepository :
 * - save(User user) : INSERT ou UPDATE
 * - findById(Long id) : SELECT par ID
 * - findAll() : SELECT tous les users
 * - delete(User user) : DELETE
 * - count() : COUNT total
 * - existsById(Long id) : EXISTS par ID
 * 
 * @see JpaRepository
 * @see User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==================== REQUÊTES DÉRIVÉES ====================
    // Spring génère automatiquement le SQL à partir du nom de la méthode
    
    /**
     * Recherche un utilisateur par son nom d'utilisateur (username).
     * 
     * Requête générée par Spring :
     * SELECT * FROM users WHERE username = :username
     * 
     * @param username le nom d'utilisateur (unique)
     * @return Optional<User> contenant l'utilisateur si trouvé, sinon Optional.empty()
     * 
     * Exemple d'utilisation :
     * Optional<User> user = userRepository.findByUsername("john_doe");
     * if (user.isPresent()) {
     *     System.out.println("User found: " + user.get().getEmail());
     * }
     */
    Optional<User> findByUsername(String username);

    /**
     * Recherche un utilisateur par son email.
     * 
     * Requête générée : SELECT * FROM users WHERE email = :email
     * 
     * @param email l'adresse email (unique)
     * @return Optional<User> contenant l'utilisateur si trouvé
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un nom d'utilisateur existe déjà en base.
     * 
     * Requête générée : SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)
     * 
     * @param username le nom d'utilisateur à vérifier
     * @return true si le username existe déjà, false sinon
     * 
     * Utilisation typique lors de l'inscription :
     * if (userRepository.existsByUsername(username)) {
     *     throw new UsernameAlreadyExistsException();
     * }
     */
    Boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe déjà en base.
     * 
     * Requête générée : SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)
     * 
     * @param email l'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    Boolean existsByEmail(String email);

    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique.
     * 
     * Requête générée : SELECT * FROM users WHERE role = :role
     * 
     * @param role le rôle à filtrer (USER ou ADMIN)
     * @return Liste des utilisateurs avec ce rôle
     * 
     * Exemple : List<User> admins = userRepository.findByRole(Role.ADMIN);
     */
    List<User> findByRole(RoleEnum role);

    /**
     * Recherche tous les utilisateurs actifs (enabled = true).
     * 
     * Requête générée : SELECT * FROM users WHERE enabled = true
     * 
     * Note : Le suffixe "True" indique une comparaison avec true
     * On peut aussi utiliser "False" pour enabled = false
     * 
     * @return Liste des utilisateurs avec compte activé
     */
    List<User> findByEnabledTrue();

    /**
     * Recherche tous les utilisateurs dont le compte est verrouillé.
     * 
     * Requête générée : SELECT * FROM users WHERE account_non_locked = false
     * 
     * @return Liste des utilisateurs verrouillés (potentiellement après brute-force)
     */
    List<User> findByAccountNonLockedFalse();

    // ==================== REQUÊTES PERSONNALISÉES AVEC @Query ====================
    // Pour des requêtes plus complexes, on écrit du JPQL manuellement
    
    /**
     * Recherche tous les utilisateurs créés après une certaine date.
     * 
     * JPQL : Java Persistence Query Language (comme du SQL mais avec des objets)
     * - "User u" = alias pour l'entité User (pas le nom de la table !)
     * - u.createdAt = propriété Java (pas le nom de colonne created_at)
     * - :afterDate = paramètre nommé (lié avec @Param)
     * 
     * @param afterDate date de création minimale
     * @return Liste des utilisateurs créés après cette date
     * 
     * Exemple : 
     * LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
     * List<User> recentUsers = userRepository.findUsersCreatedAfter(lastMonth);
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :afterDate ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("afterDate") LocalDateTime afterDate);

    /**
     * Compte le nombre d'utilisateurs par rôle et statut actif.
     * 
     * Exemple de requête d'agrégation avec COUNT.
     * 
     * @param role le rôle à compter
     * @param enabled statut actif/inactif
     * @return nombre d'utilisateurs correspondants
     * 
     * Exemple :
     * Long activeAdmins = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = :enabled")
    Long countByRoleAndEnabled(@Param("role") RoleEnum role, @Param("enabled") Boolean enabled);

    /**
     * Recherche des utilisateurs par pattern dans le username ou l'email.
     * 
     * LIKE %pattern% : recherche insensible à la position (contient le pattern)
     * LOWER() : comparaison insensible à la casse
     * 
     * @param searchTerm terme de recherche (sera entouré de % automatiquement)
     * @return Liste des utilisateurs correspondants
     * 
     * Exemple :
     * List<User> results = userRepository.searchUsers("john");
     * // Trouve : "john_doe", "Johnny", "john@example.com"
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Récupère les statistiques utilisateur (nombre total, actifs, admins).
     * 
     * Exemple de requête personnalisée retournant des données agrégées.
     * On pourrait créer un DTO pour le résultat, mais pour simplifier on retourne une List<Object[]>.
     * 
     * Résultat : [totalUsers, activeUsers, adminCount]
     * 
     * @return Array contenant [COUNT(total), COUNT(actifs), COUNT(admins)]
     */
    @Query("SELECT " +
           "COUNT(u), " +
           "SUM(CASE WHEN u.enabled = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.role = 'ADMIN' THEN 1 ELSE 0 END) " +
           "FROM User u")
    Object[] getUserStatistics();

    // ==================== MÉTHODES COMBINÉES ====================
    // Combinaison de plusieurs critères avec "And"
    
    /**
     * Recherche un utilisateur par username et statut actif.
     * 
     * Requête générée : 
     * SELECT * FROM users WHERE username = :username AND enabled = :enabled
     * 
     * @param username nom d'utilisateur
     * @param enabled statut actif
     * @return Optional<User> si trouvé
     */
    Optional<User> findByUsernameAndEnabled(String username, Boolean enabled);

    /**
     * Recherche des utilisateurs par rôle et compte non verrouillé.
     * 
     * Utile pour filtrer les admins actifs par exemple.
     * 
     * @param role rôle recherché
     * @param accountNonLocked statut du verrouillage
     * @return Liste des utilisateurs correspondants
     */
    List<User> findByRoleAndAccountNonLocked(RoleEnum role, Boolean accountNonLocked);
}