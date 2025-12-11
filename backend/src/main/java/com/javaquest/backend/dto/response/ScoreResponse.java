package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse après la soumission d'un quiz.
 * 
 * Utilisé par les endpoints :
 * - POST /api/scores/submit (soumission d'un quiz)
 * - GET /api/scores/{id} (détail d'un score)
 * - GET /api/scores/user/{userId} (historique des scores d'un utilisateur)
 * 
 * Ce DTO contient :
 * - Le score global (pourcentage, note, badge)
 * - Les détails de chaque réponse (correcte ou non + explication)
 * - Le classement de l'utilisateur
 * 
 * Exemple de JSON retourné :
 * {
 *   "id": 1,
 *   "quiz": {
 *     "id": 15,
 *     "name": "Spring Boot Fundamentals",
 *     "difficulty": "INTERMEDIATE"
 *   },
 *   "scorePercentage": 85,
 *   "correctAnswers": 17,
 *   "totalQuestions": 20,
 *   "timeSpentSeconds": 1250,
 *   "badge": "SILVER",
 *   "isPassed": true,
 *   "isBestScore": true,
 *   "rank": 42,
 *   "completedAt": "2024-12-07T14:30:00",
 *   "answerDetails": [
 *     {
 *       "questionId": 5,
 *       "title": "Quelle annotation permet l'injection de dépendances ?",
 *       "userAnswer": 0,
 *       "correctAnswer": 0,
 *       "isCorrect": true,
 *       "explanation": "@Autowired permet l'injection automatique..."
 *     },
 *     {
 *       "questionId": 12,
 *       "title": "Quel est le résultat de ce code ?",
 *       "userAnswer": 0,
 *       "correctAnswer": 1,
 *       "isCorrect": false,
 *       "explanation": "List.of() crée une liste immutable..."
 *     }
 *     // ... 18 autres réponses
 *   ]
 * }
 */
public record ScoreResponse(
        Long id,
         /**
     * Informations sur le quiz complété.
     * 
     * On n'inclut pas tous les détails du quiz (pas besoin de la liste
     * complète des questions), juste les infos essentielles.
     */
        QuizResponse quiz,
        /**
     * Score en pourcentage (0 à 100).
     * 
     * Calculé avec :
     * scorePercentage = (correctAnswers * 100) / totalQuestions
     * 
     * Exemples :
     * - 100 : Perfect score !
     * - 85 : Très bon
     * - 70 : Passable (seuil de réussite)
     * - 50 : Insuffisant
     */
        Integer scorePercentage,
         /**
     * Nombre de réponses correctes.
     * 
     * Exemple : 17 bonnes réponses sur 20 questions
     */
        Integer correctAnswers,
         /**
     * Nombre total de questions dans le quiz.
     * 
     * Correspond à quiz.getQuestions().size()
     */
        Integer totalQuestions,
        /**
     * Temps passé pour compléter le quiz (en secondes).
     * 
     * Exemples :
     * - 1250 secondes = 20 minutes et 50 secondes
     * - 600 secondes = 10 minutes
     * 
     * Utilisation :
     * - Afficher "Complété en 20 min 50 sec"
     * - Comparer avec durationMinutes recommandé
     * - Calculer le badge (bonus si rapide)
     */

        Integer timeSpentSeconds,
         /**
     * Badge obtenu selon le score et le temps.
     * 
     * Valeurs possibles :
     * - "GOLD" : Score ≥ 90% ET temps ≤ 70% du temps recommandé
     * - "SILVER" : Score ≥ 90% OU (score ≥ 80% ET temps ≤ 80%)
     * - "BRONZE" : Score ≥ 70%
     * - null : Score < 70% (pas de badge)
     * 
     * Logique de calcul dans ScoreService :
     * 
     * int expectedTime = quiz.getDurationMinutes() * 60;
     * 
     * if (scorePercentage >= 90) {
     *     if (timeSpentSeconds <= expectedTime * 0.7) {
     *         badge = "GOLD"; // Excellent + rapide
     *     } else {
     *         badge = "SILVER"; // Excellent
     *     }
     * } else if (scorePercentage >= 70) {
     *     badge = "BRONZE"; // Bien
     * } else {
     *     badge = null; // Insuffisant
     * }
     * 
     * Le frontend affichera une icône/médaille colorée.
     */
        String badge,
        Boolean isPassed,
         /**
     * Indique si c'est le meilleur score de l'utilisateur sur ce quiz.
     * 
     * Calculé en comparant avec les tentatives précédentes :
     * 
     * List<Score> previousScores = scoreRepository.findByUserAndQuiz(user, quiz);
     * boolean isBestScore = previousScores.stream()
     *     .noneMatch(s -> s.getScorePercentage() >= scorePercentage);
     * 
     * Si c'est le meilleur score, il sera marqué dans la base :
     * score.setIsBestScore(true);
     * 
     * Et les anciens meilleurs scores seront mis à jour :
     * previousBestScore.setIsBestScore(false);
     * 
     * Utilisation :
     * - Afficher "🏆 Nouveau record personnel !"
     * - Seul le meilleur score apparaît dans le leaderboard
     */
        Boolean isBestScore,
         /**
     * Classement de l'utilisateur sur ce quiz (position dans le leaderboard).
     * 
     * Exemple : 42 signifie que l'utilisateur est 42ème sur ce quiz.
     * 
     * Calculé avec :
     * Long rank = scoreRepository.findUserRankOnQuiz(user.getId(), quiz.getId());
     * 
     * La requête compte combien d'utilisateurs ont un meilleur score :
     * 
     * SELECT COUNT(DISTINCT s.user.id) + 1
     * FROM Score s
     * WHERE s.quiz.id = :quizId
     *   AND s.isBestScore = true
     *   AND s.scorePercentage > :userScore
     * 
     * Utilisation :
     * - Afficher "Vous êtes 42ème sur 1250 participants"
     * - Motivation pour réessayer et améliorer son classement
     */
        Long rank,
        LocalDateTime completedAt,
         /**
     * Détail de chaque réponse avec correction.
     * 
     * Cette liste permet à l'utilisateur de revoir ses erreurs et
     * de comprendre pourquoi certaines réponses étaient incorrectes.
     * 
     * Chaque AnswerDetailResponse contient :
     * - La question
     * - La réponse choisie par l'utilisateur
     * - La bonne réponse
     * - Si c'est correct ou non
     * - L'explication pédagogique
     * 
     * IMPORTANT : C'est ici que correctAnswer et explanation sont révélés,
     * après la soumission du quiz.
     */
        List<AnswerDetailResponse> correctAnswersDetails
) {}
