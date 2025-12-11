package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une entrée du leaderboard (classement).
 * 
 * Utilisé par les endpoints :
 * - GET /api/scores/leaderboard/global (classement global tous quiz confondus)
 * - GET /api/scores/leaderboard/quiz/{quizId} (classement pour un quiz spécifique)
 * 
 * Exemple de JSON retourné (leaderboard d'un quiz) :
 * [
 *   {
 *     "rank": 1,
 *     "user": {
 *       "id": 15,
 *       "username": "alice_dev",
 *       "email": "alice@example.com"
 *     },
 *     "quiz": {
 *       "id": 5,
 *       "name": "Spring Boot Fundamentals"
 *     },
 *     "scorePercentage": 100,
 *     "timeSpentSeconds": 1200,
 *     "badge": "GOLD",
 *     "completedAt": "2024-12-05T18:30:00"
 *   },
 *   {
 *     "rank": 2,
 *     "user": {
 *       "id": 8,
 *       "username": "bob_coder",
 *       "email": "bob@example.com"
 *     },
 *     "quiz": {
 *       "id": 5,
 *       "name": "Spring Boot Fundamentals"
 *     },
 *     "scorePercentage": 95,
 *     "timeSpentSeconds": 1350,
 *     "badge": "SILVER",
 *     "completedAt": "2024-12-03T14:20:00"
 *   }
 *   // ... top 100
 * ]
 * 
 * Affichage frontend type :
 * 
 * 🏆 LEADERBOARD - Spring Boot Fundamentals
 * ┌──────┬─────────────────┬───────┬──────────┬────────┐
 * │ Rank │ User            │ Score │ Time     │ Badge  │
 * ├──────┼─────────────────┼───────┼──────────┼────────┤
 * │  1   │ 🥇 alice_dev    │ 100%  │ 20m 00s  │ 🥇GOLD │
 * │  2   │ 🥈 bob_coder    │  95%  │ 22m 30s  │ 🥈SILV │
 * │  3   │ 🥉 charlie_pro  │  90%  │ 25m 15s  │ 🥉BRON │
 * │  4   │    dave_learn   │  85%  │ 28m 45s  │        │
 * └──────┴─────────────────┴───────┴──────────┴────────┘
 */
public record LeaderBoardEntryResponse(
         /**
     * Position dans le classement (1 = premier).
     * 
     * Le rang n'est PAS stocké dans la base de données, il est calculé
     * dynamiquement lors de la requête avec RANK() OVER ou ROW_NUMBER().
     * 
     * Dans ScoreRepository :
     * 
     * @Query(value = "SELECT ROW_NUMBER() OVER (ORDER BY s.score_percentage DESC, " +
     *                "s.time_spent_seconds ASC) as rank, " +
     *                "s.* FROM scores s WHERE s.quiz_id = :quizId " +
     *                "AND s.is_best_score = true",
     *        nativeQuery = true)
     * 
     * Critères de classement (par ordre de priorité) :
     * 1. score_percentage DESC : meilleur score d'abord
     * 2. time_spent_seconds ASC : à score égal, le plus rapide est devant
     * 
     * Exemple :
     * - Alice : 95% en 20 min → Rank 1
     * - Bob : 95% en 25 min → Rank 2 (même score mais plus lent)
     * - Charlie : 90% en 15 min → Rank 3 (score inférieur)
     */
        Long rank,
         /**
     * Informations sur l'utilisateur.
     * 
     * Note de confidentialité :
     * Pour un leaderboard public, on pourrait masquer partiellement l'email :
     * - Complet pour les admins : alice@example.com
     * - Masqué pour le public : a***e@example.com
     * 
     * Cela peut être géré avec une méthode dans UserMapper :
     * 
     * default String maskEmail(String email) {
     *     if (email == null || email.length() < 5) return email;
     *     int atIndex = email.indexOf('@');
     *     return email.charAt(0) + "***" + email.substring(atIndex - 1);
     * }
     */
        UserResponse user,
         /**
     * Informations sur le quiz (optionnel).
     * 
     * Ce champ est présent dans le leaderboard global (tous quiz confondus)
     * mais peut être null dans le leaderboard d'un quiz spécifique (on sait
     * déjà de quel quiz il s'agit).
     * 
     * Exemples d'utilisation :
     * 
     * 1. Leaderboard global :
     *    GET /api/scores/leaderboard/global
     *    → quiz contient les infos du quiz pour chaque entrée
     * 
     * 2. Leaderboard spécifique :
     *    GET /api/scores/leaderboard/quiz/15
     *    → quiz peut être null (on sait que c'est le quiz 15)
     */
        QuizResponse quiz,
        Integer scorePercentage,
         /**
     * Temps passé en secondes.
     * 
     * Utilisé comme critère secondaire de classement (tie-breaker).
     * À score égal, le plus rapide est devant.
     */
        Integer timeSpentSeconds,
        String badge,
         /**
     * Date de complétion du quiz.
     * 
     * Utilisation :
     * - Afficher "Réalisé le..."
     * - Filtrer le leaderboard par période (top du mois, de la semaine)
     */
        LocalDateTime completedAt

        /*
     * REQUÊTES POUR GÉNÉRER LE LEADERBOARD :
     * 
     * 1. LEADERBOARD GLOBAL (tous quiz) :
     * 
     * SELECT 
     *     ROW_NUMBER() OVER (ORDER BY AVG(s.score_percentage) DESC) as rank,
     *     u.*,
     *     AVG(s.score_percentage) as avg_score
     * FROM users u
     * JOIN scores s ON s.user_id = u.id
     * WHERE s.is_best_score = true
     * GROUP BY u.id
     * LIMIT 100;
     * 
     * 
     * 2. LEADERBOARD D'UN QUIZ SPÉCIFIQUE :
     * 
     * SELECT 
     *     ROW_NUMBER() OVER (ORDER BY s.score_percentage DESC, 
     *                                 s.time_spent_seconds ASC) as rank,
     *     s.*
     * FROM scores s
     * WHERE s.quiz_id = :quizId
     *   AND s.is_best_score = true
     * ORDER BY rank
     * LIMIT 100;
     * 
     * 
     * 3. LEADERBOARD PAR DIFFICULTÉ :
     * 
     * SELECT 
     *     ROW_NUMBER() OVER (ORDER BY AVG(s.score_percentage) DESC) as rank,
     *     u.*
     * FROM users u
     * JOIN scores s ON s.user_id = u.id
     * JOIN quizzes q ON s.quiz_id = q.id
     * WHERE q.difficulty = :difficulty
     *   AND s.is_best_score = true
     * GROUP BY u.id
     * LIMIT 100;
     * 
     * 
     * OPTIMISATION :
     * Ces requêtes peuvent être lourdes sur de grosses bases.
     * Solutions :
     * - Limiter à top 100 (LIMIT 100)
     * - Mettre en cache le leaderboard (Redis, 5 minutes de TTL)
     * - Index sur (quiz_id, is_best_score, score_percentage)
     */
) {}
