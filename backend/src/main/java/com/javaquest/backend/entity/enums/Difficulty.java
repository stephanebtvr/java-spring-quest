package com.javaquest.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Énumération des niveaux de difficulté des questions et quiz.
 * 
 * Cette progression suit le parcours d'apprentissage classique :
 * BEGINNER → INTERMEDIATE → ADVANCED → ARCHITECT
 * 
 * Chaque niveau correspond à un type de questions :
 * - BEGINNER : Syntaxe de base, concepts fondamentaux
 * - INTERMEDIATE : APIs standard, patterns courants
 * - ADVANCED : Optimisations, edge cases, architecture
 * - ARCHITECT : Design patterns, scalabilité, best practices
 */
public enum Difficulty {
    
    /**
     * Niveau débutant.
     * Questions sur la syntaxe de base Java, types primitifs, boucles, etc.
     * Correspond aux certifications OCA (Oracle Certified Associate).
     */
    BEGINNER("Beginner", 1, "#10b981"),
    
    /**
     * Niveau intermédiaire.
     * Questions sur Collections, Streams, Exceptions, I/O.
     * Correspond aux certifications OCP (Oracle Certified Professional).
     */
    INTERMEDIATE("Intermediate", 2, "#3b82f6"),
    
    /**
     * Niveau avancé.
     * Questions sur Concurrency, Generics, Reflection, Performance.
     * Prépare aux entretiens techniques seniors.
     */
    ADVANCED("Advanced", 3, "#f59e0b"),
    
    /**
     * Niveau architecte.
     * Questions sur Design Patterns, Microservices, Scalabilité.
     * Pour les rôles de lead developer et architect.
     */
    ARCHITECT("Architect", 4, "#ef4444");
    
    // Attributs de l'enum
    private final String displayName;  // Nom affiché dans le frontend
    private final int level;           // Niveau numérique (pour tri et comparaison)
    private final String color;        // Couleur associée (HEX pour le frontend)
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param displayName nom lisible du niveau
     * @param level valeur numérique (1-4)
     * @param color code couleur hexadécimal
     */
    Difficulty(String displayName, int level, String color) {
        this.displayName = displayName;
        this.level = level;
        this.color = color;
    }
    
    /**
     * Retourne le nom d'affichage du niveau.
     * @JsonValue indique que c'est cette valeur qui sera sérialisée en JSON.
     * 
     * @return le nom lisible (ex: "Beginner")
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne le niveau numérique.
     * Utile pour trier les questions ou vérifier la progression.
     * 
     * @return niveau de 1 à 4
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Retourne la couleur associée au niveau.
     * Cette couleur sera utilisée dans le frontend pour les badges.
     * 
     * @return code couleur hexadécimal (ex: "#10b981")
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Vérifie si ce niveau est supérieur ou égal à un autre.
     * 
     * Exemple :
     * ADVANCED.isAtLeast(INTERMEDIATE) → true
     * BEGINNER.isAtLeast(ADVANCED) → false
     * 
     * @param other le niveau à comparer
     * @return true si ce niveau >= other
     */
    public boolean isAtLeast(Difficulty other) {
        return this.level >= other.level;
    }
    
    /**
     * Retourne le niveau suivant dans la progression.
     * Si déjà au niveau maximum (ARCHITECT), retourne ARCHITECT.
     * 
     * @return le niveau suivant
     */
    public Difficulty getNext() {
        return switch (this) {
            case BEGINNER -> INTERMEDIATE;
            case INTERMEDIATE -> ADVANCED;
            case ADVANCED -> ARCHITECT;
            case ARCHITECT -> ARCHITECT; // Pas de niveau supérieur
        };
    }
    
    /**
     * Trouve un niveau par son nom d'affichage.
     * Utile pour la désérialisation JSON ou les requêtes API.
     * 
     * @param displayName le nom à rechercher (case-insensitive)
     * @return le Difficulty correspondant, ou null si non trouvé
     */
    public static Difficulty fromDisplayName(String displayName) {
        for (Difficulty difficulty : values()) {
            if (difficulty.displayName.equalsIgnoreCase(displayName)) {
                return difficulty;
            }
        }
        return null;
    }
}