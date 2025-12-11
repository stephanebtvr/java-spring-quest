package com.javaquest.backend.dto.response;

/**
 * DTO pour détailler la réponse d'un utilisateur à une question spécifique
 * 
 * Utilisé dans ScoreResponse.correctAnswersDetails
 *
 * Exemple JSON :
 * {
 *   "questionId": 5,
 *   "title": "What is @Autowired?",
 *   "userAnswer": 0,
 *   "correctAnswer": 0,
 *   "isCorrect": true,
 *   "explanation": "L'annotation @Autowired injecte automatiquement les dépendances..."
 * }
 *
 * Rôle :
 * - Permet au frontend d'afficher le feedback question par question
 * - Evite de spoiler les questions pour lesquelles l'utilisateur n'a pas encore répondu
 * - Permet de calculer des statistiques détaillées côté frontend
 */
public record AnswerDetailResponse(
        /**
         * ID de la question
         * Correspond à question.getId()
         */
        Long questionId,

        /**
         * Intitulé de la question
         */
        String title,

        /**
         * Index choisi par l'utilisateur (0 à 3)
         * Doit correspondre à l'une des options
         */
        Integer userAnswer,

        /**
         * Index de la bonne réponse (0 à 3)
         */
        Integer correctAnswer,

        /**
         * Indique si la réponse est correcte
         */
        Boolean isCorrect,

        /**
         * Explication de la réponse correcte
         * Affiché après la soumission du quiz
         */
        String explanation
) {}
