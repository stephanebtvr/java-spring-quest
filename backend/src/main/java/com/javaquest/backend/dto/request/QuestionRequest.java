package com.javaquest.backend.dto.request;

import com.javaquest.backend.entity.enums.Difficulty;
import com.javaquest.backend.entity.enums.QuestionCategory;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO pour la requête de création/modification d'une question.
 *
 * Utilisé par :
 * - POST /api/questions (création)
 * - PUT /api/questions/{id} (modification)
 *
 * Exemple JSON :
 * {
 *   "title": "What is a Lambda in Java?",
 *   "codeSnippet": "Function<String, Integer> f = s -> s.length();",
 *   "options": [
 *     "A functional interface",
 *     "A class",
 *     "A method",
 *     "An annotation"
 *   ],
 *   "correctAnswer": 0,
 *   "explanation": "Lambda expressions implement functional interfaces...",
 *   "difficulty": "INTERMEDIATE",
 *   "category": "JAVA_CORE"
 * }
 */
public record QuestionRequest(

        /**
         * Titre de la question.
         *
         * Contraintes :
         * - Non vide
         * - 10 à 500 caractères
         */
        @NotBlank(message = "Le titre de la question est obligatoire")
        @Size(min = 10, max = 500,
              message = "Le titre doit contenir entre 10 et 500 caractères")
        String title,

        /**
         * Snippet de code optionnel.
         *
         * Contraintes :
         * - Peut être null
         * - Max 2000 caractères
         */
        @Size(max = 2000, message = "Le code ne peut pas dépasser 2000 caractères")
        String codeSnippet,

        /**
         * Liste des 4 options de réponse.
         *
         * Contraintes :
         * - Doit contenir exactement 4 éléments
         * - Chaque option : 1 à 200 caractères
         */
        @NotNull(message = "Les options de réponse sont obligatoires")
        @Size(min = 4, max = 4,
              message = "Il doit y avoir exactement 4 options de réponse")
        List<
                @NotBlank(message = "Une option ne peut pas être vide")
                @Size(min = 1, max = 200,
                      message = "Chaque option doit contenir entre 1 et 200 caractères")
                String
        > options,

        /**
         * Index de la bonne réponse (0–3).
         */
        @NotNull(message = "L'index de la bonne réponse est obligatoire")
        @Min(value = 0, message = "L'index de la réponse correcte doit être entre 0 et 3")
        @Max(value = 3, message = "L'index de la réponse correcte doit être entre 0 et 3")
        Integer correctAnswer,

        /**
         * Explication détaillée de la bonne réponse.
         */
        @NotBlank(message = "L'explication de la réponse est obligatoire")
        @Size(min = 10, max = 1000,
              message = "L'explication doit contenir entre 10 et 1000 caractères")
        String explanation,

        /**
         * Niveau de difficulté.
         */
        @NotNull(message = "Le niveau de difficulté est obligatoire")
        Difficulty difficulty,

        /**
         * Catégorie/thème de la question.
         */
        @NotNull(message = "La catégorie de la question est obligatoire")
        QuestionCategory category

) {

    /*
     * VALIDATION MÉTIER SUPPLÉMENTAIRE (dans QuestionService) :
     *
     * 1. Vérifier que l’index correctAnswer est cohérent avec options.
     * 2. Vérifier l’unicité du titre (optionnel).
     * 3. Vérifier la validité/duplicité des options.
     * 4. (Optionnel avancé) Valider la syntaxe du code.
     */
}
