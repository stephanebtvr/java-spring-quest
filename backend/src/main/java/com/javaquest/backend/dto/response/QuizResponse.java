package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour afficher un quiz dans une liste (vue résumée).
 * 
 * Utilisé par les endpoints :
 * - GET /api/quizzes (liste de tous les quiz publiés)
 * - GET /api/quizzes/search (recherche de quiz)
 * - GET /api/quizzes/popular (quiz les plus populaires)
 * - GET /api/quizzes/by-difficulty/{difficulty} (filtrer par difficulté)
 * 
 * Ce DTO contient les informations essentielles pour afficher
 * une carte de quiz dans la liste, SANS les questions complètes.
 * 
 * Pour obtenir les questions, il faudra appeler GET /api/quizzes/{id}
 * qui retournera un QuizDetailResponse.
 * 
 * Exemple de JSON retourné :
 * {
 *   "id": 1,
 *   "name": "Spring Boot Fundamentals",
 *   "description": "Test your knowledge of Spring Boot basics...",
 *   "difficulty": "INTERMEDIATE",
 *   "durationMinutes": 30,
 *   "questionCount": 20,
 *   "timesAttempted": 450,
 *   "averageScore": 72.5,
 *   "createdBy": "john_doe",
 *   "createdAt": "2024-12-01T10:30:00"
 * }
 * 
 * Le frontend affichera ces infos dans une carte type :
 * 
 * ┌─────────────────────────────────────┐
 * │ Spring Boot Fundamentals            │
 * │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
 * │ Test your knowledge of Spring...    │
 * │                                      │
 * │ 🎯 Intermediate | ⏱️ 30 min | 📝 20Q │
 * │ 📊 450 attempts | ⭐ 72.5% avg       │
 * │ 👤 by john_doe | 📅 Dec 01, 2024    │
 * │                                      │
 * │          [Commencer le quiz]         │
 * └─────────────────────────────────────┘
 */
public record QuizResponse(
        Long id,
        String name,
        /**
     * Description détaillée du quiz.
     * 
     * Peut être tronquée dans l'affichage liste (ex: 200 premiers caractères).
     * La description complète sera visible sur la page de détail.
     */
        String description,
          /**
     * Niveau de difficulté (converti de l'enum en String).
     * 
     * Valeurs possibles :
     * - "BEGINNER" : Débutant
     * - "INTERMEDIATE" : Intermédiaire
     * - "ADVANCED" : Avancé
     * - "EXPERT" : Expert
     * - "ARCHITECT" : Architecte
     * 
     * Utilisé pour :
     * - Filtrer les quiz par niveau
     * - Afficher un badge coloré (vert=BEGINNER, rouge=EXPERT)
     * - Recommandations personnalisées
     * 
     * Dans le mapper :
     * @Mapping(target = "difficulty", expression = "java(quiz.getDifficulty().name())")
     */
        String difficulty,
        /**
     * Durée recommandée en minutes.
     * 
     * Exemple : 30 signifie que le quiz devrait prendre environ 30 minutes.
     * 
     * Cette durée est indicative, pas une limite stricte.
     * L'utilisateur peut prendre plus ou moins de temps.
     */
        Integer durationMinutes,
         /**
     * Nombre de questions dans le quiz.
     * 
     * Calculé avec : quiz.getQuestions().size()
     * 
     * Utilisation :
     * - Informer l'utilisateur avant de commencer
     * - Calculer la progression (ex: "Question 5/20")
     * 
     * Dans le mapper :
     * @Mapping(target = "questionCount", expression = "java(quiz.getQuestions().size())")
     */
        Integer questionCount,
        /**
     * Nombre total de fois que le quiz a été tenté.
     * 
     * Incrémenté à chaque soumission de quiz (même utilisateur peut tenter plusieurs fois).
     * 
     * Utilisation :
     * - Indicateur de popularité
     * - Tri par "tendances" (quiz les plus tentés récemment)
     * - Statistiques globales
     */
        Integer timesAttempted,
         /**
     * Score moyen en pourcentage (0.0 à 100.0).
     * 
     * Calculé avec :
     * averageScore = SUM(scores) / COUNT(scores)
     * 
     * Exemples :
     * - 72.5 : en moyenne, les utilisateurs obtiennent 72.5%
     * - 45.0 : quiz difficile
     * - 85.0 : quiz facile
     * 
     * Utilisation :
     * - Indicateur de difficulté réelle (vs difficulty théorique)
     * - Comparaison avec le score de l'utilisateur
     * - Détection de quiz mal calibrés
     */
        Double averageScore,
        /**
     * Nom d'utilisateur du créateur du quiz.
     * 
     * Exemple : "john_doe", "admin"
     * 
     * Dans le mapper :
     * @Mapping(target = "createdBy", expression = "java(quiz.getCreatedBy().getUsername())")
     * 
     * Note : On expose seulement le username, pas l'objet User complet
     * pour alléger la réponse.
     */
        String createdBy,
          /**
     * Date et heure de création du quiz.
     * 
     * Format ISO 8601 : 2024-12-01T10:30:00
     * 
     * Utilisation :
     * - Tri par "plus récents"
     * - Affichage "Créé le..."
     */
        LocalDateTime createdAt
) {}
