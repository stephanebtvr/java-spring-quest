package com.javaquest.backend.entity;

import com.javaquest.backend.entity.enums.Difficulty;
import com.javaquest.backend.entity.enums.QuestionCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une question de quiz.
 * 
 * Une question est composée de :
 * - Un titre (l'énoncé de la question)
 * - Un extrait de code optionnel (pour les questions techniques)
 * - 4 options de réponse
 * - L'index de la réponse correcte (0-3)
 * - Une explication détaillée (affichée après la réponse)
 * - Un niveau de difficulté
 * - Une catégorie technique
 * 
 * Relations :
 * - ManyToMany avec Quiz (une question peut être dans plusieurs quiz)
 * 
 * Table en base de données : "questions"
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {
    
    /**
     * Titre de la question (énoncé).
     * 
     * Exemple : "Quelle est la sortie du code suivant ?"
     * 
     * Contraintes :
     * - Obligatoire (NotBlank)
     * - Entre 10 et 500 caractères
     * - Stocké dans un TEXT en base (pas de limite VARCHAR)
     */
    @NotBlank(message = "Question title is required")
    @Size(min = 10, max = 500, message = "Title must be between 10 and 500 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    /**
     * Extrait de code source associé à la question.
     * 
     * Optionnel, utilisé pour les questions techniques nécessitant un contexte.
     * 
     * Exemple :
     * ```java
     * public class Test {
     *     public static void main(String[] args) {
     *         List<String> list = new ArrayList<>();
     *         list.add("Hello");
     *         System.out.println(list.get(0));
     *     }
     * }
     * ```
     * 
     * Stocké en TEXT car peut être long (plusieurs lignes).
     * Sera affiché avec coloration syntaxique dans le frontend.
     */
    @Column(columnDefinition = "TEXT")
    private String codeSnippet;
    
    /**
     * Les 4 options de réponse.
     * 
     * Stockées dans une liste ordonnée (l'ordre compte).
     * 
     * @ElementCollection : Crée une table séparée "question_options"
     *                      avec une colonne pour l'index et une pour la valeur
     * @CollectionTable : Définit le nom de la table et la clé étrangère
     * @OrderColumn : Maintient l'ordre d'insertion (index 0, 1, 2, 3)
     * 
     * Exemple de structure en base :
     * question_options
     * ----------------
     * question_id | option_order | option_value
     * 1           | 0            | "Option A"
     * 1           | 1            | "Option B"
     * 1           | 2            | "Option C"
     * 1           | 3            | "Option D"
     */
    @ElementCollection(fetch = FetchType.EAGER)  // EAGER car on veut toujours les options
    @CollectionTable(
        name = "question_options",  // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "question_id")  // FK vers Question
    )
    @OrderColumn(name = "option_order")  // Colonne pour l'ordre (0, 1, 2, 3)
    @Column(name = "option_value", nullable = false, columnDefinition = "TEXT")
    @Size(min = 4, max = 4, message = "A question must have exactly 4 options")
    @Builder.Default  // Valeur par défaut dans le builder
    private List<String> options = new ArrayList<>();
    
    /**
     * Index de la réponse correcte (0 à 3).
     * 
     * 0 = première option
     * 1 = deuxième option
     * 2 = troisième option
     * 3 = quatrième option
     * 
     * Contraintes de validation :
     * - Doit être entre 0 et 3 (4 options)
     * - Obligatoire
     * 
     * ⚠️ IMPORTANT : Ne JAMAIS exposer ce champ dans le DTO de réponse
     *    envoyé au frontend avant que l'utilisateur n'ait répondu !
     */
    @Min(value = 0, message = "Correct answer must be between 0 and 3")
    @Max(value = 3, message = "Correct answer must be between 0 and 3")
    @Column(nullable = false)
    private Integer correctAnswer;
    
    /**
     * Explication détaillée de la réponse.
     * 
     * Affichée après que l'utilisateur a répondu, qu'il ait bon ou faux.
     * 
     * Doit expliquer :
     * - Pourquoi la réponse correcte est correcte
     * - Pourquoi les autres options sont incorrectes
     * - Les concepts clés à retenir
     * - Des références vers la documentation Java/Spring si pertinent
     * 
     * Exemple :
     * "La bonne réponse est B. En Java, les ArrayList utilisent un tableau 
     * dynamique en interne. Lorsqu'on ajoute un élément, l'ArrayList vérifie 
     * si le tableau est plein. Si oui, elle crée un nouveau tableau avec une 
     * capacité augmentée de 50% et copie tous les éléments."
     * 
     * Obligatoire car l'objectif est pédagogique.
     */
    @NotBlank(message = "Explanation is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;
    
    /**
     * Niveau de difficulté de la question.
     * 
     * Utilisé pour :
     * - Filtrer les questions par niveau
     * - Créer des parcours progressifs
     * - Calculer un score pondéré (questions difficiles = plus de points)
     * - Afficher des badges de niveau
     * 
     * Stocké en STRING pour plus de flexibilité (facile d'ajouter des niveaux).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;
    
    /**
     * Catégorie technique de la question.
     * 
     * Permet de :
     * - Organiser les questions par domaine (Java Core, Spring, etc.)
     * - Créer des quiz spécialisés
     * - Générer des statistiques par catégorie
     * - Recommander des questions en fonction des lacunes de l'utilisateur
     * 
     * Exemples : JAVA_BASICS, SPRING_BOOT, DESIGN_PATTERNS, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionCategory category;
    
    /**
     * Nombre de fois que cette question a été posée.
     * 
     * Statistique utile pour :
     * - Identifier les questions populaires
     * - Calculer le taux de réussite (nbCorrect / nbAsked)
     * - Prioriser la génération de nouvelles questions
     * 
     * Incrémenté à chaque fois qu'un quiz contenant cette question est lancé.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timesAsked = 0;
    
    /**
     * Nombre de fois que cette question a été répondue correctement.
     * 
     * Utilisé avec timesAsked pour calculer :
     * - Taux de réussite = (timesAnsweredCorrectly / timesAsked) * 100
     * 
     * Permet d'identifier :
     * - Les questions trop faciles (taux > 90%)
     * - Les questions trop difficiles (taux < 30%)
     * - Les questions mal formulées (comportement inattendu)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timesAnsweredCorrectly = 0;
    
    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================
    
    /**
     * Vérifie si une réponse donnée est correcte.
     * 
     * @param answerIndex l'index de la réponse donnée par l'utilisateur (0-3)
     * @return true si la réponse est correcte, false sinon
     */
    public boolean isAnswerCorrect(Integer answerIndex) {
        return correctAnswer.equals(answerIndex);
    }
    
    /**
     * Calcule le taux de réussite de cette question en pourcentage.
     * 
     * @return le taux de réussite (0-100), ou 0 si jamais posée
     */
    public double getSuccessRate() {
        if (timesAsked == 0) {
            return 0.0;
        }
        return (timesAnsweredCorrectly * 100.0) / timesAsked;
    }
    
    /**
     * Indique si cette question est considérée comme difficile.
     * Une question est difficile si son taux de réussite est < 40%.
     * 
     * @return true si difficile (taux < 40%)
     */
    public boolean isDifficultQuestion() {
        return getSuccessRate() < 40.0;
    }
    
    /**
     * Incrémente le compteur de fois posée.
     * Appelé au démarrage d'un quiz.
     */
    public void incrementTimesAsked() {
        this.timesAsked++;
    }
    
    /**
     * Incrémente le compteur de réponses correctes.
     * Appelé quand l'utilisateur répond correctement.
     */
    public void incrementCorrectAnswers() {
        this.timesAnsweredCorrectly++;
    }
    
    /**
     * Représentation textuelle pour le debugging.
     * N'inclut PAS la réponse correcte pour éviter les fuites dans les logs.
     * 
     * @return description de la question
     */
    @Override
    public String toString() {
        return "Question{" +
                "id=" + getId() +
                ", title='" + title.substring(0, Math.min(50, title.length())) + "...'" +
                ", difficulty=" + difficulty +
                ", category=" + category +
                ", successRate=" + String.format("%.1f%%", getSuccessRate()) +
                '}';
    }
}