package com.javaquest.backend.dto.response;

public record CategoryStats(


 /**
     * Nom de la catégorie.
     */
    String category,
    
    /**
     * Score moyen dans cette catégorie.
     */
     Double averageScore,
    
    /**
     * Nombre de tentatives dans cette catégorie.
     */
     Long attemptsCount
    
    /*
     * CALCUL DANS ScoreService.calculateCategoryStats() :
     * 
     * @Query("SELECT q.category, AVG(s.scorePercentage), COUNT(s) " +
     *        "FROM Score s " +
     *        "JOIN s.quiz.questions q " +
     *        "WHERE s.user.id = :userId " +
     *        "GROUP BY q.category " +
     *        "ORDER BY AVG(s.scorePercentage) DESC")
     * List<Object[]> getCategoryStats(@Param("userId") Long userId);
     * 
     * Cette requête retourne :
     * - SPRING_BOOT, 85.0, 15
     * - JAVA_CORE, 72.0, 20
     * - ALGORITHMS, 60.0, 8
     * 
     * On peut alors identifier :
     * - Forces : catégories avec averageScore > 80%
     * - Faiblesses : catégories avec averageScore < 65%
     * - Recommandations : proposer des quiz dans les catégories faibles
     */

) {
    
}
