package com.javaquest.backend.dto.response;

/**
 * Sous-DTO pour représenter la progression d'un utilisateur
 *
 * Contient :
 * - recentAverage : moyenne des dernières tentatives
 * - previousAverage : moyenne des tentatives précédentes
 * - improvement : delta entre recentAverage et previousAverage
 */
public record ProgressionStats(
        /**
     * Score moyen sur les 5 dernières tentatives.
     */
     Double recentAverage,
    
    /**
     * Score moyen sur les 5 tentatives précédentes (6 à 10).
     */
     Double previousAverage,
    
    /**
     * Tendance détectée.
     * 
     * Valeurs possibles :
     * - "IMPROVING" : recentAverage > previousAverage + 5%
     * - "STABLE" : différence < 5%
     * - "DECLINING" : recentAverage < previousAverage - 5%
     */
     String trend,
    
    /**
     * Pourcentage d'amélioration (peut être négatif si régression).
     * 
     * Exemple :
     * - recentAverage = 82.5, previousAverage = 70.0
     * - improvement = 82.5 - 70.0 = +12.5%
     */
     Double improvement
    
    /*
     * CALCUL DANS ScoreService.calculateProgression() :
     * 
     * List<Score> allScores = scoreRepository.findByUserOrderByCompletedAtDesc(user),
     * 
     * if (allScores.size() < 10) {
     *     // Pas assez de données pour calculer la progression
     *     return new ProgressionStats(null, null, "INSUFFICIENT_DATA", 0.0),
     * }
     * 
     * List<Score> recent = allScores.subList(0, 5),
     * List<Score> previous = allScores.subList(5, 10),
     * 
     * double recentAvg = recent.stream()
     *     .mapToInt(Score::getScorePercentage)
     *     .average()
     *     .orElse(0.0),
     * 
     * double previousAvg = previous.stream()
     *     .mapToInt(Score::getScorePercentage)
     *     .average()
     *     .orElse(0.0),
     * 
     * double improvement = recentAvg - previousAvg,
     * 
     * String trend,
     * if (improvement > 5) {
     *     trend = "IMPROVING",
     * } else if (improvement < -5) {
     *     trend = "DECLINING",
     * } else {
     *     trend = "STABLE",
     * }
     * 
     * return new ProgressionStats(recentAvg, previousAvg, trend, improvement),
     */
) {}
