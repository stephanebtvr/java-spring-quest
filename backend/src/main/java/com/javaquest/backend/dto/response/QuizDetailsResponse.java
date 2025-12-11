package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour afficher un quiz avec tous ses détails (vue complète).
 * 
 * Utilisé par le endpoint :
 * - GET /api/quizzes/{id} (démarrer un quiz ou voir les détails)
 * 
 * Ce DTO contient TOUTES les informations nécessaires pour afficher
 * et passer un quiz, incluant la liste complète des questions.
 * 
 * IMPORTANT : Les questions sont retournées SANS correctAnswer et SANS explanation
 * pour éviter les spoilers. L'utilisateur ne doit pas voir les réponses avant
 * de soumettre le quiz.
 * 
 * Exemple de JSON retourné :
 * {
 *   "id": 1,
 *   "name": "Spring Boot Fundamentals",
 *   "description": "Test your knowledge of Spring Boot basics...",
 *   "difficulty": "INTERMEDIATE",
 *   "durationMinutes": 30,
 *   "questions": [
 *     {
 *       "id": 5,
 *       "title": "Quelle annotation permet l'injection de dépendances ?",
 *       "codeSnippet": null,
 *       "options": ["@Autowired", "@Component", "@Bean", "@Inject"],
 *       "difficulty": "BEGINNER",
 *       "category": "SPRING_BOOT"
 *     },
 *     {
 *       "id": 12,
 *       "title": "Quel est le résultat de ce code ?",
 *       "codeSnippet": "List<String> list = List.of(\"a\");\nlist.add(\"b\");",
 *       "options": ["[a, b]", "Exception", "Compilation error", "[a]"],
 *       "difficulty": "INTERMEDIATE",
 *       "category": "JAVA_COLLECTIONS"
 *     }
 *     // ... 18 autres questions
 *   ],
 *   "timesAttempted": 450,
 *   "averageScore": 72.5,
 *   "createdBy": {
 *     "id": 1,
 *     "username": "john_doe",
 *     "email": "john@example.com"
 *   },
 *   "createdAt": "2024-12-01T10:30:00"
 * }
 * 
 * Workflow utilisateur :
 * 1. GET /api/quizzes → liste des quiz (QuizResponse)
 * 2. Clic sur un quiz → GET /api/quizzes/{id} (QuizDetailResponse)
 * 3. L'utilisateur répond aux questions
 * 4. POST /api/scores/submit avec SubmitQuizRequest
 * 5. Réception de ScoreResponse avec les corrections
 */
public record QuizDetailsResponse(
        Long id,
        String name,
        String description,
        String difficulty,
        Integer durationMinutes,
        /**
     * Liste complète des questions du quiz (SANS les réponses).
     * 
     * IMPORTANT : Les questions sont mappées avec toResponseWithoutAnswer()
     * pour éviter d'exposer correctAnswer et explanation.
     * 
     * Dans le mapper :
     * @Mapping(target = "questions", 
     *          expression = "java(questionMapper.toResponseListWithoutAnswers(quiz.getQuestions()))")
     * 
     * Chaque QuestionResponse contiendra :
     * - id, title, codeSnippet, options, difficulty, category
     * - MAIS PAS correctAnswer ni explanation (spoilers)
     * 
     * L'ordre des questions est important car l'utilisateur soumettra
     * ses réponses dans le même ordre avec SubmitQuizRequest.answers.
     */
        List<QuestionResponse> questions,
        Integer timesAttempted,
        Double averageScore,
        /**
     * Informations complètes sur le créateur du quiz.
     * 
     * Contrairement à QuizResponse qui expose seulement le username (String),
     * ici on retourne l'objet UserResponse complet.
     * 
     * Dans le mapper :
     * @Mapping(target = "createdBy", source = "createdBy")
     * 
     * Le UserMapper sera automatiquement utilisé pour convertir
     * User → UserResponse.
     * 
     * Utilisation :
     * - Afficher le profil du créateur
     * - Lien vers "Autres quiz de cet auteur"
     */
        UserResponse createdBy,
        LocalDateTime createdAt
) {}
