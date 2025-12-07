package com.javaquest.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Énumération des catégories de questions techniques.
 * 
 * Organisées en 3 groupes principaux :
 * 1. Java Core (fondamentaux du langage)
 * 2. Spring Framework (écosystème Spring)
 * 3. Concepts avancés (architecture, patterns, etc.)
 * 
 * Ces catégories permettent :
 * - Filtrer les questions par technologie
 * - Créer des quiz spécialisés
 * - Afficher des statistiques par domaine
 * - Générer des parcours d'apprentissage ciblés
 */
public enum QuestionCategory {
    
    // ==========================================
    // JAVA CORE - Fondamentaux du langage
    // ==========================================
    
    /**
     * Syntaxe de base Java : types, variables, opérateurs, boucles.
     */
    JAVA_BASICS("Java Basics", "☕", "Fondamentaux Java"),
    
    /**
     * Programmation Orientée Objet : classes, héritage, polymorphisme, encapsulation.
     */
    OOP("Object-Oriented Programming", "🎯", "POO"),
    
    /**
     * Collections Framework : List, Set, Map, Queue et leurs implémentations.
     */
    COLLECTIONS("Collections Framework", "📦", "Collections"),
    
    /**
     * Streams API et programmation fonctionnelle Java 8+.
     */
    STREAMS("Streams & Functional", "🌊", "Streams API"),
    
    /**
     * Gestion des exceptions : try-catch, custom exceptions, best practices.
     */
    EXCEPTIONS("Exception Handling", "⚠️", "Exceptions"),
    
    /**
     * Multithreading et concurrence : Thread, Executor, synchronized, volatile.
     */
    CONCURRENCY("Concurrency & Threads", "🔀", "Concurrence"),
    
    /**
     * Generics Java : type parameters, wildcards, type erasure.
     */
    GENERICS("Generics", "📝", "Génériques"),
    
    // ==========================================
    // SPRING FRAMEWORK - Écosystème Spring
    // ==========================================
    
    /**
     * Spring Core : IoC, Dependency Injection, ApplicationContext.
     */
    SPRING_CORE("Spring Core", "🍃", "Spring Core"),
    
    /**
     * Spring Boot : auto-configuration, starters, properties.
     */
    SPRING_BOOT("Spring Boot", "🚀", "Spring Boot"),
    
    /**
     * Spring Data JPA : repositories, queries, relations.
     */
    SPRING_DATA("Spring Data JPA", "💾", "Spring Data"),
    
    /**
     * Spring Security : authentication, authorization, JWT.
     */
    SPRING_SECURITY("Spring Security", "🔐", "Sécurité"),
    
    /**
     * REST APIs avec Spring Web : @RestController, @RequestMapping, validation.
     */
    REST_API("REST API", "🌐", "API REST"),
    
    // ==========================================
    // CONCEPTS AVANCÉS
    // ==========================================
    
    /**
     * Design Patterns : Singleton, Factory, Observer, Strategy, etc.
     */
    DESIGN_PATTERNS("Design Patterns", "🎨", "Patterns"),
    
    /**
     * Architecture logicielle : microservices, clean architecture, DDD.
     */
    ARCHITECTURE("Software Architecture", "🏗️", "Architecture"),
    
    /**
     * Tests unitaires : JUnit, Mockito, TDD.
     */
    TESTING("Testing & TDD", "🧪", "Tests"),
    
    /**
     * Performance et optimisation : profiling, caching, memory management.
     */
    PERFORMANCE("Performance", "⚡", "Performance"),
    
    /**
     * Bases de données : SQL, transactions, indexation.
     */
    DATABASE("Database & SQL", "🗄️", "Base de données");
    
    // Attributs
    private final String displayName;  // Nom complet affiché
    private final String icon;         // Emoji représentatif
    private final String shortName;    // Nom court pour badges
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param displayName nom complet de la catégorie
     * @param icon emoji représentatif
     * @param shortName nom court (pour affichage compact)
     */
    QuestionCategory(String displayName, String icon, String shortName) {
        this.displayName = displayName;
        this.icon = icon;
        this.shortName = shortName;
    }
    
    /**
     * Retourne le nom d'affichage (utilisé pour la sérialisation JSON).
     * 
     * @return le nom complet de la catégorie
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne l'emoji associé à la catégorie.
     * 
     * @return un emoji représentatif
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * Retourne le nom court pour affichage compact.
     * 
     * @return le nom abrégé
     */
    public String getShortName() {
        return shortName;
    }
    
    /**
     * Vérifie si cette catégorie fait partie de l'écosystème Spring.
     * 
     * @return true si c'est une catégorie Spring
     */
    public boolean isSpringRelated() {
        return this == SPRING_CORE || 
               this == SPRING_BOOT || 
               this == SPRING_DATA || 
               this == SPRING_SECURITY || 
               this == REST_API;
    }
    
    /**
     * Vérifie si cette catégorie concerne Java Core.
     * 
     * @return true si c'est du Java fondamental
     */
    public boolean isJavaCore() {
        return this == JAVA_BASICS || 
               this == OOP || 
               this == COLLECTIONS || 
               this == STREAMS || 
               this == EXCEPTIONS || 
               this == CONCURRENCY || 
               this == GENERICS;
    }
    
    /**
     * Trouve une catégorie par son nom d'affichage.
     * 
     * @param displayName le nom à rechercher (case-insensitive)
     * @return la catégorie correspondante, ou null si non trouvée
     */
    public static QuestionCategory fromDisplayName(String displayName) {
        for (QuestionCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return null;
    }
}