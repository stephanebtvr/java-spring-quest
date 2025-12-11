package com.javaquest.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant le résultat d'une tentative de quiz.
 * 
 * Un Score enregistre :
 * - Quel utilisateur a fait le quiz
 * - Quel quiz a été tenté
 * - Le score obtenu (pourcentage)
 * - Le nombre de réponses correctes
 * - Le temps passé
 * - La date de complétion
 * 
 * Relations :
 * - ManyToOne avec User (plusieurs scores par utilisateur)
 * - ManyToOne avec Quiz (plusieurs scores par quiz)
 * 
 * Cette entité permet :
 * - L'historique des tentatives par utilisateur
 * - Le leaderboard (classement)
 * - Les statistiques de progression
 * - L'analyse de la difficulté des quiz
 * 
 * Table en base de données : "scores"
 */
@Entity
@Table(name = "scores", indexes = {
    // Index pour requêtes fréquentes
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_quiz_id", columnList = "quiz_id"),
    @Index(name = "idx_completed_at", columnList = "completed_at"),
    @Index(name = "idx_score_percentage", columnList = "score_percentage")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score extends BaseEntity {
    
    /**
     * L'utilisateur qui a fait le quiz.
     * 
     * Relation ManyToOne :
     * - Un User peut avoir plusieurs Scores
     * - Un Score appartient à un seul User
     * 
     * FetchType.LAZY : L'utilisateur n'est chargé que si nécessaire.
     * 
     * Obligatoire : on doit toujours savoir qui a fait le quiz.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Le quiz qui a été tenté.
     * 
     * Relation ManyToOne :
     * - Un Quiz peut avoir plusieurs Scores (plusieurs tentatives)
     * - Un Score correspond à un seul Quiz
     * 
     * FetchType.LAZY pour optimiser les performances.
     * 
     * Obligatoire : un score doit forcément être lié à un quiz.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    /**
     * Score obtenu en pourcentage (0-100).
     * 
     * Calculé comme : (correctAnswers / totalQuestions) * 100
     * 
     * Exemple :
     * - 15 bonnes réponses sur 20 questions = 75%
     * - 8 bonnes réponses sur 10 questions = 80%
     * 
     * Contraintes :
     * - Entre 0 et 100 (validation Bean Validation)
     * - Obligatoire
     * 
     * Utilisé pour :
     * - Afficher le résultat à l'utilisateur
     * - Calculer le classement (leaderboard)
     * - Attribuer des badges (Gold > 90%, Silver > 75%, Bronze > 60%)
     */
    @Min(value = 0, message = "Score must be between 0 and 100")
    @Max(value = 100, message = "Score must be between 0 and 100")
    @Column(name = "score_percentage", nullable = false)
    private Integer scorePercentage;
    
    /**
     * Nombre de réponses correctes.
     * 
     * Exemple : 15 (sur 20 questions)
     * 
     * Permet d'afficher : "15/20 réponses correctes"
     */
    @Min(value = 0, message = "Correct answers cannot be negative")
    @Column(nullable = false)
    private Integer correctAnswers;
    
    /**
     * Nombre total de questions dans le quiz.
     * 
     * Stocké ici car le quiz peut être modifié après la tentative.
     * On veut garder le nombre de questions au moment de la tentative.
     * 
     * Exemple : 20
     */
    @Min(value = 1, message = "Total questions must be at least 1")
    @Column(nullable = false)
    private Integer totalQuestions;
    
    /**
     * Temps passé sur le quiz en secondes.
     * 
     * Mesuré depuis le début du quiz jusqu'à la soumission finale.
     * 
     * Utilisé pour :
     * - Afficher "Complété en 12m 34s"
     * - Calculer des bonus de rapidité (si implémenté)
     * - Détecter les comportements suspects (trop rapide = triche possible)
     * - Statistiques de performance
     * 
     * Exemple : 754 secondes = 12 minutes 34 secondes
     */
    @Min(value = 1, message = "Time spent must be at least 1 second")
    @Column(nullable = false)
    private Integer timeSpentSeconds;
    
    /**
     * Date et heure de complétion du quiz.
     * 
     * Différent de createdAt (date de début potentielle).
     * C'est le moment où l'utilisateur a soumis ses réponses.
     * 
     * Utilisé pour :
     * - Afficher "Complété le 05/12/2024 à 14:30"
     * - Trier l'historique par date
     * - Calculer les classements sur une période (leaderboard du mois)
     * 
     * Indexé pour améliorer les performances des requêtes de tri.
     */
    @Column(nullable = false)
    private LocalDateTime completedAt;
    
    /**
     * Indique si ce score est le meilleur pour ce quiz par cet utilisateur.
     * 
     * true = C'est son meilleur score pour ce quiz
     * false = Il a fait mieux ailleurs
     * 
     * Permet d'optimiser les requêtes :
     * - "Affiche seulement mon meilleur score par quiz"
     * - "Leaderboard basé sur les meilleurs scores"
     * 
     * Mis à jour automatiquement quand un utilisateur améliore son score.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isBestScore = false;
    
    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================
    
    /**
     * Calcule le score en pourcentage.
     * 
     * Formule : (correctAnswers / totalQuestions) * 100
     * 
     * Exemple : 15 bonnes / 20 questions = 75%
     * 
     * @return le pourcentage (0-100)
     */
    public static int calculatePercentage(int correctAnswers, int totalQuestions) {
        if (totalQuestions == 0) {
            return 0;
        }
        return (correctAnswers * 100) / totalQuestions;
    }
    
    /**
     * Détermine le badge obtenu en fonction du score.
     * 
     * Badges :
     * - GOLD : 90% et plus (excellence)
     * - SILVER : 75-89% (très bien)
     * - BRONZE : 60-74% (bien)
     * - NONE : moins de 60% (à améliorer)
     * 
     * @return le badge obtenu sous forme de String
     */
    public String getBadge() {
        if (scorePercentage >= 90) {
            return "GOLD";
        } else if (scorePercentage >= 75) {
            return "SILVER";
        } else if (scorePercentage >= 60) {
            return "BRONZE";
        } else {
            return "NONE";
        }
    }
    
    /**
     * Retourne le temps passé formaté en minutes et secondes.
     * 
     * Exemple : 754 secondes → "12m 34s"
     * 
     * @return temps formaté
     */
    public String getFormattedTimeSpent() {
        int minutes = timeSpentSeconds / 60;
        int seconds = timeSpentSeconds % 60;
        return String.format("%dm %02ds", minutes, seconds);
    }
    
    /**
     * Vérifie si le quiz a été réussi (score >= 60%).
     * 
     * @return true si réussi (>= 60%)
     */
    public boolean isPassed() {
        return scorePercentage >= 60;
    }
    
    /**
     * Vérifie si c'est un score parfait (100%).
     * 
     * @return true si score parfait
     */
    public boolean isPerfectScore() {
        return scorePercentage == 100;
    }
    
    /**
     * Calcule les points obtenus (pour un système de gamification futur).
     * 
     * Formule simple :
     * - Score de base : scorePercentage
     * - Bonus temps : si complété en moins de 50% du temps alloué → +10 points
     * - Bonus perfection : si 100% → +20 points
     * 
     * @param maxTimeSeconds temps maximum alloué pour le quiz
     * @return les points totaux
     */
    public int calculatePoints(int maxTimeSeconds) {
        int points = scorePercentage;
        
        // Bonus de rapidité
        if (timeSpentSeconds < (maxTimeSeconds * 0.5)) {
            points += 10;
        }
        
        // Bonus de perfection
        if (isPerfectScore()) {
            points += 20;
        }
        
        return points;
    }
    
    /**
     * Compare ce score avec un autre pour déterminer lequel est meilleur.
     * 
     * Critères :
     * 1. Score en pourcentage (prioritaire)
     * 2. Si égalité : temps le plus rapide gagne
     * 
     * @param other l'autre score à comparer
     * @return négatif si ce score < other, positif si ce score > other, 0 si égaux
     */
    public int compareTo(Score other) {
        // D'abord comparer les pourcentages
        int scoreComparison = this.scorePercentage.compareTo(other.scorePercentage);
        
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        
        // Si égalité, le plus rapide gagne (ordre inversé)
        return other.timeSpentSeconds.compareTo(this.timeSpentSeconds);
    }
    
    /**
     * Représentation textuelle pour le debugging.
     * 
     * @return description du score
     */
    @Override
    public String toString() {
        return "Score{" +
                "id=" + getId() +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", quiz=" + (quiz != null ? quiz.getName() : "null") +
                ", score=" + scorePercentage + "%" +
                ", correctAnswers=" + correctAnswers + "/" + totalQuestions +
                ", timeSpent=" + getFormattedTimeSpent() +
                ", badge=" + getBadge() +
                ", completedAt=" + completedAt +
                '}';
    }
}