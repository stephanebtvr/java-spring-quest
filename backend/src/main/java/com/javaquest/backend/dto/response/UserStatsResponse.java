package com.javaquest.backend.dto.response;

import java.util.List;

/**
 * DTO de réponse pour les statistiques complètes d'un utilisateur.
 * 
 * Utilisé par les endpoints :
 * - GET /api/users/{id}/stats (statistiques d'un utilisateur - ADMIN)
 * - GET /api/users/me/stats (statistiques de l'utilisateur connecté)
 * 
 * Ce DTO agrège toutes les informations pertinentes sur l'activité
 * et les performances d'un utilisateur sur la plateforme.
 * 
 * Exemple de JSON retourné :
 * {
 *   "userId": 1,
 *   "username": "john_doe",
 *   "totalAttempts": 50,
 *   "uniqueQuizzesAttempted": 12,
 *   "passedQuizzes": 10,
 *   "averageScore": 78.5,
 *   "bestScores": [
 *     {
 *       "quiz": { "id": 5, "name": "Spring Boot Fundamentals" },
 *       "scorePercentage": 95,
 *       "badge": "SILVER"
 *     },
 *     {
 *       "quiz": { "id": 8, "name": "Java Streams API" },
 *       "scorePercentage": 88,
 *       "badge": "BRONZE"
 *     }
 *     // Top 5 meilleurs scores
 *   ],
 *   "recentActivity": [
 *     {
 *       "quiz": { "id": 15, "name": "REST API Design" },
 *       "scorePercentage": 85,
 *       "completedAt": "2024-12-07T14:30:00"
 *     }
 *     // 5 dernières tentatives
 *   ],
 *   "progression": {
 *     "recentAverage": 82.5,
 *     "previousAverage": 70.0,
 *     "trend": "IMPROVING",
 *     "improvement": 12.5
 *   },
 *   "categoryStats": [
 *     {
 *       "category": "SPRING_BOOT",
 *       "averageScore": 85.0,
 *       "attemptsCount": 15
 *     },
 *     {
 *       "category": "JAVA_CORE",
 *       "averageScore": 72.0,
 *       "attemptsCount": 20
 *     }
 *   ]
 * }
 * 
 * Affichage frontend type dashboard :
 * 
 * ┌─────────────────────────────────────────────┐
 * │ 📊 STATISTIQUES - john_doe                  │
 * ├─────────────────────────────────────────────┤
 * │                                             │
 * │ 🎯 Tentatives : 50                          │
 * │ 📚 Quiz différents : 12                     │
 * │ ✅ Quiz réussis : 10 (83%)                  │
 * │ ⭐ Score moyen : 78.5%                      │
 * │                                             │
 * │ 📈 PROGRESSION                              │
 * │ Recent : 82.5% (+12.5% vs avant)            │
 * │ Tendance : 📈 EN AMÉLIORATION               │
 * │                                             │
 * │ 🏆 MEILLEURS SCORES                         │
 * │ 1. Spring Boot Fundamentals - 95% 🥈       │
 * │ 2. Java Streams API - 88% 🥉                │
 * │                                             │
 * │ 📝 ACTIVITÉ RÉCENTE                         │
 * │ REST API Design - 85% (il y a 2h)           │
 * └─────────────────────────────────────────────┘
 */
public record UserStatsResponse(
        Long userId,
        String username,
        /**
     * Nombre total de tentatives (toutes tentatives, même quiz répété).
     * 
     * Calculé avec :
     * Long totalAttempts = scoreRepository.countByUser(user);
     * 
     * Exemple :
     * - L'utilisateur a tenté le quiz "Spring Boot" 3 fois
     * - Il a tenté le quiz "Java Core" 2 fois
     * - totalAttempts = 5
     */
        Long totalAttempts,
         /**
     * Nombre de quiz différents tentés (quiz uniques).
     * 
     * Calculé avec :
     * Long uniqueQuizzes = scoreRepository.countDistinctQuizzesByUser(user.getId());
     * 
     * Requête :
     * SELECT COUNT(DISTINCT s.quiz_id)
     * FROM scores s
     * WHERE s.user_id = :userId
     * 
     * Exemple :
     * - L'utilisateur a tenté 3 fois "Spring Boot"
     * - Il a tenté 2 fois "Java Core"
     * - uniqueQuizzesAttempted = 2 (2 quiz différents)
     */
        Long uniqueQuizzesAttempted,
         /**
     * Nombre de quiz réussis (score ≥ 70%).
     * 
     * Calculé avec :
     * Long passed = scoreRepository.countPassedQuizzesByUser(user.getId());
     * 
     * Requête :
     * SELECT COUNT(DISTINCT s.quiz_id)
     * FROM scores s
     * WHERE s.user_id = :userId
     *   AND s.is_best_score = true
     *   AND s.score_percentage >= 70
     * 
     * Note : On compte seulement le meilleur score par quiz.
     * Si l'utilisateur a raté puis réussi, on compte 1 quiz réussi.
     */
        Long passedQuizzes,
         /**
     * Score moyen sur tous les quiz (meilleurs scores uniquement).
     * 
     * Calculé avec :
     * Double avgScore = scoreRepository.getAverageScoreByUser(user.getId());
     * 
     * Requête :
     * SELECT AVG(s.score_percentage)
     * FROM scores s
     * WHERE s.user_id = :userId
     *   AND s.is_best_score = true
     * 
     * Exemple :
     * - Quiz 1 : 85% (meilleur score)
     * - Quiz 2 : 72% (meilleur score)
     * - averageScore = (85 + 72) / 2 = 78.5%
     */
        Double averageScore,
         /**
     * Top 5 des meilleurs scores de l'utilisateur.
     * 
     * Liste triée par score décroissant, limitée à 5 entrées.
     * 
     * Utilisé pour afficher une section "Vos meilleurs résultats" :
     * - Spring Boot Fundamentals : 95% 🥈
     * - Java Streams API : 88% 🥉
     * - REST API Design : 85%
     */
        List<ScoreSummary> bestScores,
         /**
     * 5 dernières tentatives de l'utilisateur.
     * 
     * Liste triée par date décroissante (plus récent en premier).
     * 
     * Utilisé pour afficher l'activité récente :
     * - REST API Design : 85% (il y a 2h)
     * - Design Patterns : 75% (hier)
     * - Spring Security : 90% (il y a 3 jours)
     */
        List<ScoreSummary> recentActivity,
        ProgressionStats progression,

         /**
     * Statistiques par catégorie de questions.
     * 
     * Permet d'identifier les forces et faiblesses de l'utilisateur :
     * - SPRING_BOOT : 85% (fort)
     * - JAVA_CORE : 72% (moyen)
     * - ALGORITHMS : 60% (faible)
     * 
     * Utilisé pour recommander des quiz ciblés.
     */
     List<CategoryStats> categoryStats
) {}
