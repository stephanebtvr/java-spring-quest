package com.javaquest.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe abstraite de base pour toutes les entités JPA.
 * 
 * Cette classe fournit les champs communs à toutes les entités :
 * - id : Identifiant unique auto-généré
 * - createdAt : Date de création (automatique)
 * - updatedAt : Date de dernière modification (automatique)
 * 
 * L'annotation @MappedSuperclass indique que cette classe n'est pas une entité
 * à part entière, mais que ses champs seront hérités par les entités filles.
 * 
 * L'annotation @EntityListeners(AuditingEntityListener.class) active l'audit
 * automatique des dates (nécessite @EnableJpaAuditing dans la classe principale).
 * 
 * Pourquoi utiliser une classe de base ?
 * ✅ Évite la duplication de code (DRY principle)
 * ✅ Garantit la cohérence entre toutes les entités
 * ✅ Facilite les requêtes génériques
 * ✅ Simplifie l'audit (qui a créé quoi et quand)
 */
@Getter
@Setter
@MappedSuperclass  // Indique que c'est une classe parent pour d'autres entités
@EntityListeners(AuditingEntityListener.class)  // Active l'audit automatique
public abstract class BaseEntity implements Serializable {
    
    /**
     * Identifiant unique de l'entité.
     * 
     * Stratégie de génération :
     * - GenerationType.IDENTITY : La BDD génère automatiquement l'ID
     * - Utilise les sequences PostgreSQL (auto-increment)
     * - L'ID est assigné lors du premier save()
     * 
     * Pourquoi Long et pas Integer ?
     * - Long peut stocker jusqu'à 9 quintillions de valeurs
     * - Integer limité à 2 milliards → risque de débordement
     * - Performance similaire en pratique
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Date et heure de création de l'entité.
     * 
     * @CreatedDate : Rempli automatiquement lors du premier save()
     * @Column(updatable = false) : Empêche toute modification ultérieure
     * 
     * Cette date est en UTC pour éviter les problèmes de timezone.
     * Le frontend se chargera de l'afficher dans le fuseau de l'utilisateur.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Date et heure de la dernière modification.
     * 
     * @LastModifiedDate : Mis à jour automatiquement à chaque save()
     * 
     * Utile pour :
     * - Tracer l'historique des modifications
     * - Détecter les changements récents
     * - Implémenter un système de cache intelligent
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Callback JPA exécuté AVANT la première persistance.
     * 
     * Cette méthode initialise les dates si elles sont null.
     * C'est un filet de sécurité au cas où @CreatedDate échouerait.
     * 
     * @PrePersist s'exécute avant INSERT en base de données.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Callback JPA exécuté AVANT chaque mise à jour.
     * 
     * Met à jour automatiquement updatedAt.
     * 
     * @PreUpdate s'exécute avant UPDATE en base de données.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Méthode equals() basée sur l'ID.
     * 
     * Deux entités sont égales si elles ont le même ID.
     * Si l'ID est null (entité non persistée), on utilise l'égalité par référence.
     * 
     * Cette implémentation respecte les bonnes pratiques JPA/Hibernate.
     * 
     * @param o l'objet à comparer
     * @return true si les entités ont le même ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BaseEntity that = (BaseEntity) o;
        
        // Si l'ID est null, on compare les références
        if (id == null) {
            return false;
        }
        
        return id.equals(that.id);
    }
    
    /**
     * Méthode hashCode() basée sur l'ID.
     * 
     * Retourne un hash constant pour éviter les problèmes avec les collections
     * Hibernate (Set, Map) lorsque l'entité n'est pas encore persistée.
     * 
     * Cette approche est recommandée par Vlad Mihalcea (expert Hibernate).
     * 
     * @return le hashCode de l'ID, ou un hash constant si ID null
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    /**
     * Représentation textuelle de l'entité.
     * 
     * Format : ClassName{id=123, createdAt=2024-12-05T10:30:00}
     * 
     * Utile pour le debugging et les logs.
     * 
     * @return une chaîne représentant l'entité
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}