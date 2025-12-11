package com.javaquest.backend.dto.request;

import com.javaquest.backend.entity.enums.Difficulty;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO pour la création/modification d'un quiz (version record).
 */
public record QuizRequest(

        @NotBlank(message = "Le nom du quiz est obligatoire")
        @Size(min = 5, max = 100, message = "Le nom du quiz doit contenir entre 5 et 100 caractères")
        String name,

        @NotBlank(message = "La description du quiz est obligatoire")
        @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
        String description,

        @NotEmpty(message = "Le quiz doit contenir au moins une question")
        @Size(min = 5, max = 50, message = "Le quiz doit contenir entre 5 et 50 questions")
        List<
                @NotNull(message = "Les IDs des questions ne peuvent pas être null")
                Long
        > questionIds,

        Difficulty difficulty,

        @Positive(message = "La durée doit être un nombre positif")
        Integer durationMinutes,

        Boolean published

) {
        /**
         * Constructeur compact permettant d'appliquer une valeur par défaut
         * pour "published" si null.
         */
        public QuizRequest {
                if (published == null) {
                        published = false;
                }
        }
}
