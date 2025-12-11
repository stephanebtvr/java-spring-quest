package com.javaquest.backend.dto.response;

import java.util.List;

/**
 * DTO pour représenter une question dans l'API REST.
 *
 * Utilisé par :
 * - GET /api/questions/{id}
 * - GET /api/quizzes/{id}/questions
 *
 * Exemple :
 * {
 *   "id": 1,
 *   "title": "What is a Lambda?",
 *   "codeSnippet": "Function<String,Integer> f = s -> s.length();",
 *   "options": ["A functional interface", "A class", "A method", "An annotation"],
 *   "difficulty": "INTERMEDIATE",
 *   "category": "JAVA_CORE",
 *   "timesAsked": 150,
 *   "successRate": 78.5
 * }
 *
 * Remarques :
 * - Les champs correctAnswer et explanation ne sont pas inclus pour éviter le spoil
 * - successRate et timesAsked sont calculés depuis l'entité Question
 */
public record QuestionResponse(
        /**
         * ID unique de la question
         */
        Long id,

        /**
         * Intitulé de la question
         * Doit être clair et compréhensible
         */
        String title,

        /**
         * Bloc de code associé (optionnel)
         * Peut être null si aucune illustration n'est nécessaire
         */
        String codeSnippet,

        /**
         * Liste des options de réponse
         * Toujours 4 éléments : index 0 à 3
         * L'ordre est important pour l'affichage frontend
         */
        List<String> options,

/**
     * Index de la bonne réponse (0-3).
     * 
     * ATTENTION : Ce champ doit être NULL lors de l'affichage d'un quiz en cours.
     * Il ne sera rempli que dans 2 cas :
     * 
     * 1. ADMIN : lors de la création/édition de questions
     * 2. Après soumission : pour montrer les erreurs à l'utilisateur
     * 
     * Le mapper aura une méthode spéciale :
     * @Mapping(target = "correctAnswer", ignore = true)
     * QuestionResponse toResponseWithoutAnswer(Question question);
     */
     Integer correctAnswer,
    
    /**
     * Explication pédagogique de la bonne réponse.
     * 
     * ATTENTION : Comme correctAnswer, ce champ doit être NULL pendant le quiz.
     * 
     * Il sera affiché uniquement après soumission pour aider l'utilisateur
     * à comprendre ses erreurs.
     * 
     * Exemple :
     * "Une lambda expression est une façon concise d'implémenter une interface
     * fonctionnelle. Elle remplace les classes anonymes pour plus de lisibilité."
     */
    String explanation,

        /**
         * Difficulté de la question
         * Conversion de l'enum Difficulty vers String
         * @see com.javaquest.backend.entity.enums.Difficulty
         */
        String difficulty,

        /**
         * Catégorie de la question
         * Conversion de l'enum QuestionCategory vers String
         * @see com.javaquest.backend.entity.enums.QuestionCategory
         */
        String category,

        /**
         * Nombre de fois où cette question a été posée
         * Utile pour analytics et quiz random
         */
        Integer timesAsked,

        /**
         * Pourcentage moyen de succès des utilisateurs
         * Calculé avec : (timesAnsweredCorrectly / timesAsked) * 100
         */
        Double successRate
) {}
