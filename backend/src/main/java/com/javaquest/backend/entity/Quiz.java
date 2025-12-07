package com.javaquest.backend.entity;

import com.javaquest.backend.entity.enums.Difficulty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un quiz complet.
 * 
 * Un quiz est une collection de questions organisées avec :
 * - Un nom et une description
 * - Une liste de questions (ManyToMany)
 * - Un niveau de difficulté global
 * - Une durée estimée
 * - Un créateur (User)
 * 
 * Relations :
 * - ManyToMany avec Question (un quiz contient plusieurs questions,
 *   une question peut être dans plusieurs quiz)
 * - ManyToOne avec User (créateur du quiz)
 * - OneToMany avec Score (les tentatives de ce quiz)
 * 
 * Table en base de données : "quizzes"
 */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends BaseEntity {
    
    /**
     * Nom du quiz.
     * 
     * Exemple : "Java Collections Masterclass", "Spring Boot Essentials"
     * 
     * Contraintes :
     * - Obligatoire
     * - Entre 5 et 100 caractères
     * - Doit être accrocheur et descriptif
     */
    @NotBlank(message = "Quiz name is required")
    @Size(min = 5, max = 100, message = "Name must be between 5 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Description détaillée du quiz.
     * 
     * Décrit :
     * - Les sujets couverts
     * - Les prérequis
     * - Ce que l'utilisateur va apprendre
     * 
     * Exemple :
     * "Ce quiz couvre les concepts avancés des Collections Java : ArrayList vs 
     * LinkedList, HashMap internals, ConcurrentHashMap, et les performances 
     * comparées. Idéal pour préparer les entretiens techniques niveau Senior."
     * 
     * Optionnel mais fortement recommandé.
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Liste des questions du quiz.
     * 
     * Relation ManyToMany :
     * - Une question peut être dans plusieurs quiz différents
     * - Un quiz contient plusieurs questions
     * 
     * @ManyToMany avec cascade = {PERSIST, MERGE} :
     * - PERSIST : Si on sauvegarde un Quiz avec de nouvelles Questions, 
     *             elles sont aussi sauvegardées
     * - MERGE : Si on met à jour un Quiz, les Questions associées sont aussi mises à jour
     * - ATTENTION : On n'utilise PAS REMOVE car supprimer un Quiz ne doit PAS 
     *               supprimer les Questions (elles peuvent être dans d'autres quiz)
     * 
     * @JoinTable définit la table de jointure :
     * quiz_questions
     * --------------
     * quiz_id | question_id
     * 1       | 5
     * 1       | 12
     * 1       | 23
     * 2       | 5 (même question dans quiz 2)
     * 
     * FetchType.LAZY : Les questions ne sont chargées que si on y accède.
     * Améliore les performances car on ne charge pas toujours toutes les questions.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "quiz_questions",  // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "quiz_id"),  // FK vers Quiz
        inverseJoinColumns = @JoinColumn(name = "question_id")  // FK vers Question
    )
    @Builder.Default
    private List<Question> questions = new ArrayList<>();
    
    /**
     * Niveau de difficulté global du quiz.
     * 
     * Peut être :
     * - Calculé automatiquement (moyenne des difficultés des questions)
     * - Défini manuellement par le créateur
     * 
     * Utilisé pour filtrer les quiz adaptés au niveau de l'utilisateur.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;
    
    /**
     * Durée estimée du quiz en minutes.
     * 
     * Calculée généralement comme :
     * nombre_de_questions * 1 minute (60 secondes par question)
     * 
     * Peut être personnalisée si certaines questions nécessitent plus de réflexion.
     * 
     * Affichée à l'utilisateur avant de commencer le quiz.
     */
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer durationMinutes;
    
    /**
     * Créateur du quiz.
     * 
     * Relation ManyToOne :
     * - Un User peut créer plusieurs Quiz
     * - Un Quiz a un seul créateur
     * 
     * FetchType.LAZY : Le User n'est chargé que si on y accède.
     * 
     * Cette relation permet :
     * - D'afficher "Créé par @username"
     * - De permettre au créateur de modifier/supprimer ses quiz
     * - De générer des statistiques par créateur
     * 
     * Peut être null si le quiz est créé par le système (questions générées par IA).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    /**
     * Indique si le quiz est publié et visible par tous.
     * 
     * États possibles :
     * - false : Brouillon (visible uniquement par le créateur)
     * - true : Publié (visible par tous les utilisateurs)
     * 
     * Permet de créer des quiz sans les publier immédiatement.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;
    
    /**
     * Nombre de fois que ce quiz a été tenté par des utilisateurs.
     * 
     * Statistique utile pour :
     * - Identifier les quiz populaires
     * - Calculer des métriques de difficulté
     * - Recommander des quiz aux nouveaux utilisateurs
     * 
     * Incrémenté à chaque nouvelle tentative (création d'un Score).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timesAttempted = 0;
    
    /**
     * Score moyen obtenu sur ce quiz (en pourcentage 0-100).
     * 
     * Calculé comme la moyenne de tous les scores obtenus.
     * 
     * Permet de :
     * - Évaluer la difficulté réelle du quiz
     * - Ajuster le niveau de difficulté si nécessaire
     * - Afficher "Taux de réussite moyen : 67%" dans l'interface
     * 
     * Recalculé à chaque nouvelle tentative.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double averageScore = 0.0;
    
    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================
    
    /**
     * Ajoute une question au quiz.
     * 
     * Gère automatiquement la relation bidirectionnelle si nécessaire.
     * 
     * @param question la question à ajouter
     */
    public void addQuestion(Question question) {
        if (!questions.contains(question)) {
            questions.add(question);
        }
    }
    
    /**
     * Retire une question du quiz.
     * 
     * @param question la question à retirer
     */
    public void removeQuestion(Question question) {
        questions.remove(question);
    }
    
    /**
     * Retourne le nombre de questions dans le quiz.
     * 
     * @return le nombre de questions
     */
    public int getQuestionCount() {
        return questions.size();
    }
    
    /**
     * Incrémente le compteur de tentatives.
     * Appelé quand un utilisateur commence le quiz.
     */
    public void incrementAttempts() {
        this.timesAttempted++;
    }
    
    /**
     * Met à jour le score moyen du quiz.
     * 
     * Formule : nouveauMoyenne = (ancienneMoyenne * nbTentatives + nouveauScore) / (nbTentatives + 1)
     * 
     * @param newScore le nouveau score obtenu (0-100)
     */
    public void updateAverageScore(double newScore) {
        if (timesAttempted == 0) {
            this.averageScore = newScore;
        } else {
            // Moyenne pondérée
            this.averageScore = ((this.averageScore * this.timesAttempted) + newScore) 
                                / (this.timesAttempted + 1);
        }
    }
    
    /**
     * Calcule automatiquement la durée en fonction du nombre de questions.
     * 1 minute par question.
     */
    public void calculateDuration() {
        this.durationMinutes = questions.size(); // 1 min par question
    }
    
    /**
     * Calcule automatiquement la difficulté en fonction des questions.
     * Prend la difficulté médiane des questions du quiz.
     * 
     * @return la difficulté calculée
     */
    public Difficulty calculateDifficulty() {
        if (questions.isEmpty()) {
            return Difficulty.BEGINNER;
        }
        
        // Compte les niveaux de difficulté
        int totalLevel = questions.stream()
            .mapToInt(q -> q.getDifficulty().getLevel())
            .sum();
        
        double avgLevel = (double) totalLevel / questions.size();
        
        // Convertit le niveau moyen en Difficulty
        if (avgLevel <= 1.5) return Difficulty.BEGINNER;
        if (avgLevel <= 2.5) return Difficulty.INTERMEDIATE;
        if (avgLevel <= 3.5) return Difficulty.ADVANCED;
        return Difficulty.ARCHITECT;
    }
    
    /**
     * Vérifie si le quiz est prêt à être publié.
     * 
     * Conditions :
     * - Au moins 5 questions
     * - Durée définie
     * - Nom et description non vides
     * 
     * @return true si prêt à être publié
     */
    public boolean isReadyToPublish() {
        return questions.size() >= 5 
            && durationMinutes != null 
            && durationMinutes > 0
            && name != null 
            && !name.isBlank();
    }
    
    /**
     * Représentation textuelle pour le debugging.
     * 
     * @return description du quiz
     */
    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", difficulty=" + difficulty +
                ", questionCount=" + questions.size() +
                ", published=" + published +
                ", averageScore=" + String.format("%.1f%%", averageScore) +
                '}';
    }
}