package com.javaquest.backend.repository;

import com.javaquest.backend.entity.Quiz;
import com.javaquest.backend.entity.Score;
import com.javaquest.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des scores des utilisateurs.
 * 
 * Ce repository offre des méthodes pour :
 * - Gérer l'historique des tentatives d'un utilisateur
 * - Obtenir les leaderboards (globaux et par quiz)
 * - Récupérer les meilleurs scores (isBestScore = true)
 * - Calculer des statistiques de performance
 * - Analyser la progression des utilisateurs
 * 
 * @see Score
 * @see User
 * @see Quiz
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    // ==================== FILTRES DE BASE ====================
    
    /**
     * Récupère tous les scores d'un utilisateur (historique complet).
     * 
     * Requête générée : SELECT * FROM scores WHERE user_id = :userId
     * 
     * @param user l'utilisateur
     * @return Liste de tous ses scores
     * 
     * Exemple : Afficher l'historique complet
     * List<Score> history = scoreRepository.findByUser(currentUser);
     */
    List<Score> findByUser(User user);

    /**
     * Récupère les scores d'un utilisateur avec pagination et tri.
     * 
     * @param user l'utilisateur
     * @param pageable pagination
     * @return Page de scores
     * 
     * Exemple : Historique paginé, trié par date (plus récent en premier)
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("completedAt").descending());
     * Page<Score> history = scoreRepository.findByUser(currentUser, pageable);
     */
    Page<Score> findByUser(User user, Pageable pageable);

    /**
     * Récupère tous les scores pour un quiz donné.
     * 
     * Requête générée : SELECT * FROM scores WHERE quiz_id = :quizId
     * 
     * @param quiz le quiz
     * @return Liste de tous les scores pour ce quiz
     */
    List<Score> findByQuiz(Quiz quiz);

    /**
     * Récupère les scores d'un quiz avec pagination.
     * 
     * @param quiz le quiz
     * @param pageable pagination
     * @return Page de scores
     */
    Page<Score> findByQuiz(Quiz quiz, Pageable pageable);

    /**
     * Récupère le meilleur score d'un utilisateur pour un quiz spécifique.
     * 
     * isBestScore = true : marqué automatiquement lors de la sauvegarde
     * Seul 1 score par (user, quiz) peut avoir isBestScore = true
     * 
     * @param user l'utilisateur
     * @param quiz le quiz
     * @return Optional contenant le meilleur score si existant
     * 
     * Exemple : Afficher le record personnel
     * Optional<Score> bestScore = scoreRepository.findByUserAndQuizAndIsBestScoreTrue(
     *     currentUser, 
     *     quiz
     * );
     * bestScore.ifPresent(score -> 
     *     System.out.println("Your best: " + score.getScorePercentage() + "%")
     * );
     */
    Optional<Score> findByUserAndQuizAndIsBestScoreTrue(User user, Quiz quiz);

    /**
     * Récupère tous les meilleurs scores d'un utilisateur (tous quiz confondus).
     * 
     * @param user l'utilisateur
     * @return Liste de ses meilleurs scores pour chaque quiz tenté
     * 
     * Exemple : Dashboard utilisateur - mes records
     * List<Score> bestScores = scoreRepository.findByUserAndIsBestScoreTrue(currentUser);
     * System.out.println("You have " + bestScores.size() + " quiz records");
     */
    List<Score> findByUserAndIsBestScoreTrue(User user);

    // ==================== LEADERBOARDS ====================
    
    /**
     * Leaderboard global : top scores tous quiz confondus.
     * 
     * Prend uniquement les meilleurs scores (isBestScore = true)
     * Tri par scorePercentage décroissant, puis timeSpent croissant (tie-breaker)
     * 
     * @param pageable limite le nombre de résultats (ex: top 100)
     * @return Page des meilleurs scores globaux
     * 
     * Exemple : Top 10 mondial
     * Pageable top10 = PageRequest.of(0, 10);
     * Page<Score> leaderboard = scoreRepository.findGlobalLeaderboard(top10);
     * 
     * int rank = 1;
     * for (Score score : leaderboard.getContent()) {
     *     System.out.println(rank++ + ". " + 
     *                        score.getUser().getUsername() + " - " + 
     *                        score.getScorePercentage() + "% on " + 
     *                        score.getQuiz().getName());
     * }
     */
    @Query("SELECT s FROM Score s WHERE s.isBestScore = true " +
           "ORDER BY s.scorePercentage DESC, s.timeSpentSeconds ASC")
    Page<Score> findGlobalLeaderboard(Pageable pageable);

    /**
     * Leaderboard par quiz : top scores pour un quiz spécifique.
     * 
     * Affiche uniquement les meilleurs scores de chaque utilisateur pour ce quiz.
     * Tri : score décroissant, puis temps croissant (plus rapide = meilleur)
     * 
     * @param quiz le quiz
     * @param pageable pagination
     * @return Page des meilleurs scores pour ce quiz
     * 
     * Exemple : Top 20 du quiz "Spring Boot Expert"
     * Quiz quiz = quizRepository.findById(quizId).get();
     * Page<Score> leaderboard = scoreRepository.findLeaderboardByQuiz(
     *     quiz, 
     *     PageRequest.of(0, 20)
     * );
     */
    @Query("SELECT s FROM Score s WHERE s.quiz = :quiz AND s.isBestScore = true " +
           "ORDER BY s.scorePercentage DESC, s.timeSpentSeconds ASC")
    Page<Score> findLeaderboardByQuiz(@Param("quiz") Quiz quiz, Pageable pageable);

    /**
     * Classement d'un utilisateur sur un quiz spécifique.
     * 
     * Compte combien d'utilisateurs ont un meilleur score.
     * Rang = nombre de meilleurs scores + 1
     * 
     * @param quiz le quiz
     * @param scorePercentage le score de l'utilisateur
     * @param timeSpent le temps de l'utilisateur (tie-breaker)
     * @return le rang de l'utilisateur (1 = premier)
     * 
     * Exemple : "You are ranked #15 out of 250 players"
     * Score myScore = scoreRepository.findByUserAndQuizAndIsBestScoreTrue(user, quiz).get();
     * Long rank = scoreRepository.findUserRankOnQuiz(
     *     quiz, 
     *     myScore.getScorePercentage(), 
     *     myScore.getTimeSpentSeconds()
     * );
     * Long totalPlayers = scoreRepository.countDistinctUsersByQuiz(quiz);
     * System.out.println("You are ranked #" + rank + " out of " + totalPlayers);
     */
    @Query("SELECT COUNT(s) + 1 FROM Score s WHERE " +
           "s.quiz = :quiz AND s.isBestScore = true AND " +
           "(s.scorePercentage > :scorePercentage OR " +
           " (s.scorePercentage = :scorePercentage AND s.timeSpentSeconds < :timeSpent))")
    Long findUserRankOnQuiz(
        @Param("quiz") Quiz quiz,
        @Param("scorePercentage") Integer scorePercentage,
        @Param("timeSpent") Integer timeSpent
    );

    // ==================== STATISTIQUES UTILISATEUR ====================
    
    /**
     * Compte le nombre total de tentatives d'un utilisateur.
     * 
     * @param user l'utilisateur
     * @return nombre total de quiz tentés (toutes tentatives confondues)
     */
    Long countByUser(User user);

    /**
     * Compte le nombre de quiz distincts tentés par un utilisateur.
     * 
     * Différence avec countByUser :
     * - countByUser : 50 (tentatives totales, même quiz plusieurs fois)
     * - countDistinctQuizzesByUser : 12 (quiz uniques tentés)
     * 
     * @param user l'utilisateur
     * @return nombre de quiz différents tentés
     * 
     * Exemple : Profil utilisateur
     * Long totalAttempts = scoreRepository.countByUser(user);        // 50 tentatives
     * Long uniqueQuizzes = scoreRepository.countDistinctQuizzesByUser(user); // 12 quiz
     * System.out.println("Attempted " + uniqueQuizzes + " quizzes (" + totalAttempts + " times total)");
     */
    @Query("SELECT COUNT(DISTINCT s.quiz) FROM Score s WHERE s.user = :user")
    Long countDistinctQuizzesByUser(@Param("user") User user);

    /**
     * Compte le nombre de quiz réussis par un utilisateur (score >= 70%).
     * 
     * Seuil de réussite : 70% (défini dans Score.isPassed())
     * 
     * @param user l'utilisateur
     * @return nombre de quiz réussis
     * 
     * Exemple : Taux de réussite
     * Long passed = scoreRepository.countPassedQuizzesByUser(user);
     * Long total = scoreRepository.countDistinctQuizzesByUser(user);
     * double successRate = (passed * 100.0) / total;
     * System.out.println("Success rate: " + String.format("%.1f%%", successRate));
     */
    @Query("SELECT COUNT(DISTINCT s.quiz) FROM Score s WHERE " +
           "s.user = :user AND s.isBestScore = true AND s.scorePercentage >= 70")
    Long countPassedQuizzesByUser(@Param("user") User user);

    /**
     * Calcule le score moyen d'un utilisateur (tous quiz confondus).
     * 
     * Prend uniquement les meilleurs scores pour chaque quiz.
     * 
     * @param user l'utilisateur
     * @return score moyen en pourcentage
     * 
     * Exemple : "Your average score: 78.5%"
     * Double avgScore = scoreRepository.getAverageScoreByUser(user);
     */
    @Query("SELECT AVG(s.scorePercentage) FROM Score s WHERE " +
           "s.user = :user AND s.isBestScore = true")
    Double getAverageScoreByUser(@Param("user") User user);

    /**
     * Récupère les scores récents d'un utilisateur (derniers N jours).
     * 
     * @param user l'utilisateur
     * @param afterDate date minimale (ex: il y a 7 jours)
     * @param pageable pagination
     * @return Page des scores récents
     * 
     * Exemple : Activité de la semaine
     * LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
     * Page<Score> recentScores = scoreRepository.findRecentScoresByUser(
     *     currentUser, 
     *     lastWeek, 
     *     PageRequest.of(0, 20)
     * );
     */
    @Query("SELECT s FROM Score s WHERE s.user = :user AND s.completedAt >= :afterDate " +
           "ORDER BY s.completedAt DESC")
    Page<Score> findRecentScoresByUser(
        @Param("user") User user,
        @Param("afterDate") LocalDateTime afterDate,
        Pageable pageable
    );

    // ==================== STATISTIQUES QUIZ ====================
    
    /**
     * Compte le nombre d'utilisateurs distincts ayant tenté un quiz.
     * 
     * @param quiz le quiz
     * @return nombre de joueurs uniques
     * 
     * Exemple : "250 players have attempted this quiz"
     * Long players = scoreRepository.countDistinctUsersByQuiz(quiz);
     */
    @Query("SELECT COUNT(DISTINCT s.user) FROM Score s WHERE s.quiz = :quiz")
    Long countDistinctUsersByQuiz(@Param("quiz") Quiz quiz);

    /**
     * Calcule le score moyen d'un quiz.
     * 
     * @param quiz le quiz
     * @return score moyen en pourcentage
     * 
     * Note : Cette valeur est aussi stockée dans Quiz.averageScore
     * mais cette méthode permet de recalculer en temps réel.
     */
    @Query("SELECT AVG(s.scorePercentage) FROM Score s WHERE s.quiz = :quiz")
    Double getAverageScoreByQuiz(@Param("quiz") Quiz quiz);

    /**
     * Compte le nombre de réussites d'un quiz (score >= 70%).
     * 
     * @param quiz le quiz
     * @return nombre d'utilisateurs ayant réussi (meilleur score >= 70%)
     */
    @Query("SELECT COUNT(s) FROM Score s WHERE " +
           "s.quiz = :quiz AND s.isBestScore = true AND s.scorePercentage >= 70")
    Long countPassedAttemptsByQuiz(@Param("quiz") Quiz quiz);

    // ==================== OPTIMISATION PERFORMANCE ====================
    
    /**
     * Récupère un score avec user et quiz pré-chargés (évite N+1).
     * 
     * JOIN FETCH : charge immédiatement les relations
     * 
     * @param id ID du score
     * @return Optional<Score> avec user et quiz chargés
     * 
     * Exemple : Afficher un score dans le leaderboard
     * Score score = scoreRepository.findByIdWithUserAndQuiz(scoreId).get();
     * System.out.println(score.getUser().getUsername() + " - " +  // 0 requête
     *                    score.getQuiz().getName() + " - " +      // 0 requête
     *                    score.getScorePercentage() + "%");
     */
    @Query("SELECT s FROM Score s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.quiz " +
           "WHERE s.id = :id")
    Optional<Score> findByIdWithUserAndQuiz(@Param("id") Long id);

    /**
     * Récupère l'historique d'un utilisateur avec quiz pré-chargés.
     * 
     * Évite N+1 lors de l'affichage de l'historique.
     * 
     * @param user l'utilisateur
     * @param pageable pagination
     * @return Page de scores avec quiz chargés
     * 
     * Exemple : Historique optimisé
     * Page<Score> history = scoreRepository.findByUserWithQuiz(
     *     currentUser, 
     *     PageRequest.of(0, 10, Sort.by("completedAt").descending())
     * );
     * 
     * history.forEach(score -> 
     *     System.out.println(score.getQuiz().getName()) // 0 requête supplémentaire !
     * );
     */
    @Query("SELECT s FROM Score s LEFT JOIN FETCH s.quiz " +
           "WHERE s.user = :user ORDER BY s.completedAt DESC")
    Page<Score> findByUserWithQuiz(@Param("user") User user, Pageable pageable);

    // ==================== STATISTIQUES AVANCÉES ====================
    
    /**
     * Obtient la distribution des scores pour un quiz (histogramme).
     * 
     * Regroupe les scores par tranches de 10% (0-10, 10-20, ..., 90-100).
     * Utile pour visualiser la difficulté d'un quiz.
     * 
     * @param quiz le quiz
     * @return Liste de [tranche, nombre] : [[0, 5], [10, 12], [20, 18], ...]
     * 
     * Exemple : Créer un histogramme
     * List<Object[]> distribution = scoreRepository.getScoreDistribution(quiz);
     * distribution.forEach(row -> {
     *     Integer bucket = (Integer) row[0];  // 0, 10, 20, ..., 90
     *     Long count = (Long) row[1];
     *     System.out.println(bucket + "-" + (bucket+10) + "%: " + count + " users");
     * });
     * 
     * Résultat :
     * 0-10%: 5 users
     * 10-20%: 12 users
     * 20-30%: 18 users
     * ...
     * 90-100%: 25 users
     */
    @Query("SELECT (s.scorePercentage / 10) * 10, COUNT(s) " +
           "FROM Score s WHERE s.quiz = :quiz AND s.isBestScore = true " +
           "GROUP BY (s.scorePercentage / 10) " +
           "ORDER BY (s.scorePercentage / 10)")
    List<Object[]> getScoreDistribution(@Param("quiz") Quiz quiz);

    /**
     * Calcule la progression d'un utilisateur (tendance).
     * 
     * Compare les 5 derniers scores aux 5 précédents.
     * Retourne [avgLast5, avgPrevious5, difference]
     * 
     * @param user l'utilisateur
     * @return Object[] : [Double avgRecent, Double avgOld, Double trend]
     * 
     * Exemple : "Your scores are improving by +12.5%"
     * Object[] trend = scoreRepository.getUserTrend(user);
     * Double recent = (Double) trend[0];    // 82.5%
     * Double old = (Double) trend[1];       // 70.0%
     * Double improvement = (Double) trend[2]; // +12.5%
     * 
     * if (improvement > 0) {
     *     System.out.println("📈 Improving by " + improvement + "%!");
     * }
     */
    @Query(value = "WITH recent AS (" +
           "    SELECT AVG(score_percentage) as avg_score " +
           "    FROM (SELECT score_percentage FROM scores " +
           "          WHERE user_id = :userId " +
           "          ORDER BY completed_at DESC LIMIT 5) sub" +
           "), " +
           "previous AS (" +
           "    SELECT AVG(score_percentage) as avg_score " +
           "    FROM (SELECT score_percentage FROM scores " +
           "          WHERE user_id = :userId " +
           "          ORDER BY completed_at DESC OFFSET 5 LIMIT 5) sub" +
           ") " +
           "SELECT recent.avg_score, previous.avg_score, " +
           "       (recent.avg_score - previous.avg_score) " +
           "FROM recent, previous",
           nativeQuery = true)
    Object[] getUserTrend(@Param("userId") Long userId);

    // ==================== VALIDATION ====================
    
    /**
     * Vérifie si un utilisateur a déjà tenté un quiz.
     * 
     * @param user l'utilisateur
     * @param quiz le quiz
     * @return true si l'utilisateur a au moins une tentative
     * 
     * Exemple : Afficher un badge "Already attempted"
     * boolean attempted = scoreRepository.existsByUserAndQuiz(user, quiz);
     */
    Boolean existsByUserAndQuiz(User user, Quiz quiz);

    /**
     * Compte le nombre de tentatives d'un utilisateur sur un quiz.
     * 
     * @param user l'utilisateur
     * @param quiz le quiz
     * @return nombre de fois que l'utilisateur a tenté ce quiz
     * 
     * Exemple : "This is your 3rd attempt"
     * Long attemptCount = scoreRepository.countByUserAndQuiz(user, quiz);
     * System.out.println("Attempt #" + (attemptCount + 1));
     */
    Long countByUserAndQuiz(User user, Quiz quiz);
}