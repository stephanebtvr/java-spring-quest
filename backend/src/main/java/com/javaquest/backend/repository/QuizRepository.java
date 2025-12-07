package com.javaquest.backend.repository;

import com.javaquest.backend.entity.Quiz;
import com.javaquest.backend.entity.User;
import com.javaquest.backend.entity.enums.Difficulty;
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
 * Repository pour la gestion des quiz.
 * 
 * Ce repository offre des méthodes pour :
 * - Gérer la publication des quiz (brouillons vs publiés)
 * - Filtrer par créateur, difficulté
 * - Obtenir les quiz populaires et tendances
 * - Charger les quiz avec leurs questions (optimisation performance)
 * - Calculer des statistiques (score moyen, nb tentatives)
 * 
 * @see Quiz
 * @see User
 * @see Difficulty
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ==================== FILTRES DE BASE ====================
    
    /**
     * Recherche tous les quiz publiés (disponibles pour les utilisateurs).
     * 
     * Requête générée : SELECT * FROM quizzes WHERE published = true
     * 
     * Les quiz non publiés (brouillons) ne sont visibles que par leur créateur.
     * 
     * @return Liste des quiz publiés
     * 
     * Exemple : Afficher tous les quiz disponibles dans le catalogue
     * List<Quiz> availableQuizzes = quizRepository.findByPublishedTrue();
     */
    List<Quiz> findByPublishedTrue();

    /**
     * Recherche les quiz publiés avec pagination et tri.
     * 
     * @param pageable configuration de pagination
     * @return Page de quiz publiés
     * 
     * Exemple : Afficher 12 quiz par page, triés par date de création
     * Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt").descending());
     * Page<Quiz> quizzes = quizRepository.findByPublishedTrue(pageable);
     */
    Page<Quiz> findByPublishedTrue(Pageable pageable);

    /**
     * Recherche tous les quiz créés par un utilisateur (brouillons + publiés).
     * 
     * Requête générée : SELECT * FROM quizzes WHERE created_by_id = :userId
     * 
     * @param createdBy l'utilisateur créateur
     * @return Liste de tous ses quiz
     * 
     * Exemple : Dashboard créateur - voir tous mes quiz
     * User currentUser = getCurrentUser();
     * List<Quiz> myQuizzes = quizRepository.findByCreatedBy(currentUser);
     */
    List<Quiz> findByCreatedBy(User createdBy);

    /**
     * Recherche les quiz d'un utilisateur avec pagination.
     * 
     * @param createdBy l'utilisateur créateur
     * @param pageable pagination
     * @return Page de quiz de cet utilisateur
     */
    Page<Quiz> findByCreatedBy(User createdBy, Pageable pageable);

    /**
     * Filtre les quiz par niveau de difficulté.
     * 
     * Requête générée : SELECT * FROM quizzes WHERE difficulty = :difficulty
     * 
     * @param difficulty niveau de difficulté
     * @return Liste des quiz de ce niveau
     * 
     * Exemple : Filtrer uniquement les quiz débutants
     * List<Quiz> beginnerQuizzes = quizRepository.findByDifficulty(Difficulty.BEGINNER);
     */
    List<Quiz> findByDifficulty(Difficulty difficulty);

    /**
     * Filtre les quiz publiés par difficulté avec pagination.
     * 
     * Requête générée : 
     * SELECT * FROM quizzes WHERE published = true AND difficulty = :difficulty
     * 
     * @param difficulty niveau de difficulté
     * @param pageable pagination
     * @return Page de quiz publiés de ce niveau
     */
    Page<Quiz> findByPublishedTrueAndDifficulty(Difficulty difficulty, Pageable pageable);

    // ==================== RECHERCHE PAR NOM ====================
    
    /**
     * Recherche des quiz par mots-clés dans le nom ou la description.
     * 
     * LIKE %keyword% : recherche partielle insensible à la position
     * LOWER() : recherche insensible à la casse
     * 
     * @param keyword mot-clé à rechercher
     * @param pageable pagination
     * @return Page de quiz correspondants
     * 
     * Exemple : Recherche "spring"
     * Page<Quiz> results = quizRepository.searchByNameOrDescription("spring", pageable);
     * // Trouve : "Spring Boot Basics", "Advanced Spring Security", etc.
     */
    @Query("SELECT q FROM Quiz q WHERE " +
           "q.published = true AND " +
           "(LOWER(q.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Quiz> searchByNameOrDescription(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Recherche multicritères : nom, difficulté, créateur.
     * 
     * Tous les paramètres sont optionnels (peuvent être null).
     * Permet une recherche flexible selon les besoins.
     * 
     * @param keyword mot-clé dans nom/description (optionnel)
     * @param difficulty niveau de difficulté (optionnel)
     * @param createdBy créateur (optionnel)
     * @param publishedOnly si true, seulement les quiz publiés
     * @param pageable pagination
     * @return Page de quiz filtrés
     * 
     * Exemple 1 : Mes quiz contenant "java"
     * Page<Quiz> myJavaQuizzes = quizRepository.searchQuizzes(
     *     "java", null, currentUser, false, pageable
     * );
     * 
     * Exemple 2 : Quiz publiés niveau Expert contenant "architecture"
     * Page<Quiz> expertQuizzes = quizRepository.searchQuizzes(
     *     "architecture", Difficulty.EXPERT, null, true, pageable
     * );
     */
    @Query("SELECT q FROM Quiz q WHERE " +
           "(:keyword IS NULL OR LOWER(q.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:createdBy IS NULL OR q.createdBy = :createdBy) AND " +
           "(:publishedOnly = false OR q.published = true)")
    Page<Quiz> searchQuizzes(
        @Param("keyword") String keyword,
        @Param("difficulty") Difficulty difficulty,
        @Param("createdBy") User createdBy,
        @Param("publishedOnly") Boolean publishedOnly,
        Pageable pageable
    );

    // ==================== STATISTIQUES ET POPULARITÉ ====================
    
    /**
     * Trouve les quiz les plus populaires (par nombre de tentatives).
     * 
     * ORDER BY q.timesAttempted DESC : tri décroissant
     * Pageable limite le nombre de résultats (ex: top 10)
     * 
     * @param pageable limite (ex: top 10, top 20)
     * @return Page des quiz les plus tentés
     * 
     * Exemple : Afficher le top 10 des quiz les plus populaires
     * Pageable top10 = PageRequest.of(0, 10);
     * Page<Quiz> popular = quizRepository.findMostPopularQuizzes(top10);
     * 
     * popular.forEach(quiz -> 
     *     System.out.println(quiz.getName() + " - " + quiz.getTimesAttempted() + " attempts")
     * );
     */
    @Query("SELECT q FROM Quiz q WHERE q.published = true " +
           "ORDER BY q.timesAttempted DESC")
    Page<Quiz> findMostPopularQuizzes(Pageable pageable);

    /**
     * Trouve les quiz avec le meilleur score moyen.
     * 
     * Utile pour identifier les quiz "faciles" ou bien conçus.
     * 
     * @param pageable limite de résultats
     * @return Page des quiz avec meilleurs scores moyens
     * 
     * Exemple : Top 10 quiz avec meilleurs scores
     * Page<Quiz> bestRated = quizRepository.findQuizzesWithBestScores(PageRequest.of(0, 10));
     */
    @Query("SELECT q FROM Quiz q WHERE q.published = true AND q.timesAttempted > 5 " +
           "ORDER BY q.averageScore DESC")
    Page<Quiz> findQuizzesWithBestScores(Pageable pageable);

    /**
     * Trouve les quiz avec le pire score moyen (les plus difficiles).
     * 
     * Identifie les quiz challengeants ou peut-être mal calibrés.
     * Le seuil de 5 tentatives évite les statistiques non significatives.
     * 
     * @param pageable limite de résultats
     * @return Page des quiz les plus difficiles
     * 
     * Exemple : Identifier les quiz à revoir (trop difficiles)
     * Page<Quiz> hardest = quizRepository.findMostDifficultQuizzes(PageRequest.of(0, 10));
     */
    @Query("SELECT q FROM Quiz q WHERE q.published = true AND q.timesAttempted > 5 " +
           "ORDER BY q.averageScore ASC")
    Page<Quiz> findMostDifficultQuizzes(Pageable pageable);

    /**
     * Trouve les quiz récents avec au moins N tentatives (tendances).
     * 
     * Combine récence (derniers 30 jours) et popularité (min 10 tentatives).
     * Parfait pour une section "Tendances" ou "Popular this month".
     * 
     * @param minAttempts nombre minimum de tentatives
     * @param afterDate date à partir de laquelle chercher (ex: il y a 30 jours)
     * @param pageable pagination
     * @return Page des quiz tendances
     * 
     * Exemple : Quiz populaires du dernier mois
     * LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
     * Page<Quiz> trending = quizRepository.findTrendingQuizzes(10, lastMonth, pageable);
     */
    @Query("SELECT q FROM Quiz q WHERE " +
           "q.published = true AND " +
           "q.timesAttempted >= :minAttempts AND " +
           "q.createdAt >= :afterDate " +
           "ORDER BY q.timesAttempted DESC, q.averageScore DESC")
    Page<Quiz> findTrendingQuizzes(
        @Param("minAttempts") Integer minAttempts,
        @Param("afterDate") LocalDateTime afterDate,
        Pageable pageable
    );

    /**
     * Compte le nombre de quiz par difficulté.
     * 
     * Requête générée : SELECT COUNT(*) FROM quizzes WHERE difficulty = :difficulty
     * 
     * @param difficulty niveau de difficulté
     * @return nombre de quiz de ce niveau
     * 
     * Exemple : Statistiques du catalogue
     * Long beginnerCount = quizRepository.countByDifficulty(Difficulty.BEGINNER);
     * Long expertCount = quizRepository.countByDifficulty(Difficulty.EXPERT);
     */
    Long countByDifficulty(Difficulty difficulty);

    /**
     * Compte le nombre de quiz créés par un utilisateur.
     * 
     * @param createdBy l'utilisateur créateur
     * @return nombre de quiz créés par cet utilisateur
     * 
     * Exemple : Afficher le compteur dans le profil
     * Long myQuizCount = quizRepository.countByCreatedBy(currentUser);
     * System.out.println("You have created " + myQuizCount + " quizzes");
     */
    Long countByCreatedBy(User createdBy);

    // ==================== OPTIMISATION PERFORMANCE ====================
    
    /**
     * Récupère un quiz avec ses questions pré-chargées (évite N+1).
     * 
     * LEFT JOIN FETCH : charge immédiatement la collection questions
     * 
     * Sans FETCH (❌ problème N+1) :
     * Quiz quiz = quizRepository.findById(1L).get();     // 1 requête
     * quiz.getQuestions().forEach(...);                  // N requêtes !
     * 
     * Avec FETCH (✅) :
     * Quiz quiz = quizRepository.findByIdWithQuestions(1L).get(); // 1 requête
     * quiz.getQuestions().forEach(...);                           // 0 requête !
     * 
     * @param id ID du quiz
     * @return Optional<Quiz> avec questions pré-chargées
     * 
     * Exemple : Afficher un quiz avec toutes ses questions
     * Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
     *     .orElseThrow(() -> new QuizNotFoundException());
     * 
     * quiz.getQuestions().forEach(q -> 
     *     System.out.println(q.getTitle()) // Pas de requête supplémentaire
     * );
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Récupère un quiz avec questions ET créateur pré-chargés.
     * 
     * Multiple FETCH : charge plusieurs relations en une seule requête.
     * 
     * @param id ID du quiz
     * @return Optional<Quiz> avec questions et createdBy pré-chargés
     * 
     * Exemple : Afficher page de quiz complète (questions + info créateur)
     * Quiz quiz = quizRepository.findByIdWithQuestionsAndCreator(quizId)
     *     .orElseThrow(() -> new QuizNotFoundException());
     * 
     * System.out.println("Creator: " + quiz.getCreatedBy().getUsername()); // 0 requête
     * quiz.getQuestions().forEach(...);                                    // 0 requête
     */
    @Query("SELECT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions " +
           "LEFT JOIN FETCH q.createdBy " +
           "WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestionsAndCreator(@Param("id") Long id);

    /**
     * Récupère tous les quiz publiés avec leurs questions.
     * 
     * ⚠️ ATTENTION : Cette requête peut être lourde si beaucoup de quiz.
     * À utiliser uniquement pour des exports ou opérations batch.
     * Pour l'affichage normal, préférer la pagination sans FETCH.
     * 
     * @return Liste de tous les quiz publiés avec questions
     */
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions " +
           "WHERE q.published = true")
    List<Quiz> findAllPublishedWithQuestions();

    // ==================== VALIDATION ====================
    
    /**
     * Vérifie si un quiz avec ce nom existe déjà pour cet utilisateur.
     * 
     * Évite les doublons de noms pour un même créateur.
     * Note : Plusieurs utilisateurs peuvent avoir des quiz avec le même nom.
     * 
     * @param name nom du quiz
     * @param createdBy créateur
     * @return true si ce nom existe déjà pour cet utilisateur
     * 
     * Exemple dans un service (création de quiz) :
     * if (quizRepository.existsByNameAndCreatedBy(quizName, currentUser)) {
     *     throw new DuplicateQuizNameException("You already have a quiz with this name");
     * }
     */
    Boolean existsByNameAndCreatedBy(String name, User createdBy);

    /**
     * Vérifie si un quiz existe et est publié.
     * 
     * Utile pour valider qu'un quiz est accessible publiquement.
     * 
     * @param id ID du quiz
     * @return true si le quiz existe et est publié
     * 
     * Exemple : Avant de permettre une tentative
     * if (!quizRepository.existsByIdAndPublishedTrue(quizId)) {
     *     throw new QuizNotAvailableException();
     * }
     */
    Boolean existsByIdAndPublishedTrue(Long id);

    // ==================== STATISTIQUES AVANCÉES ====================
    
    /**
     * Calcule les statistiques globales de tous les quiz.
     * 
     * Retourne : [totalQuizzes, publishedQuizzes, totalAttempts, avgScore]
     * 
     * @return Object[] contenant les statistiques
     * 
     * Exemple d'utilisation :
     * Object[] stats = quizRepository.getGlobalStatistics();
     * Long totalQuizzes = (Long) stats[0];
     * Long publishedQuizzes = (Long) stats[1];
     * Long totalAttempts = (Long) stats[2];
     * Double avgScore = (Double) stats[3];
     * 
     * System.out.println("Total quizzes: " + totalQuizzes);
     * System.out.println("Published: " + publishedQuizzes);
     * System.out.println("Total attempts: " + totalAttempts);
     * System.out.println("Average score: " + avgScore + "%");
     */
    @Query("SELECT " +
           "COUNT(q), " +
           "SUM(CASE WHEN q.published = true THEN 1 ELSE 0 END), " +
           "SUM(q.timesAttempted), " +
           "AVG(q.averageScore) " +
           "FROM Quiz q")
    Object[] getGlobalStatistics();

    /**
     * Obtient les statistiques par niveau de difficulté.
     * 
     * GROUP BY : regroupe les résultats par difficulté
     * Retourne : [difficulty, count, avgAttempts, avgScore]
     * 
     * @return Liste de Object[] avec stats par difficulté
     * 
     * Exemple d'utilisation :
     * List<Object[]> stats = quizRepository.getStatisticsByDifficulty();
     * stats.forEach(row -> {
     *     Difficulty diff = (Difficulty) row[0];
     *     Long count = (Long) row[1];
     *     Double avgAttempts = (Double) row[2];
     *     Double avgScore = (Double) row[3];
     *     
     *     System.out.println(diff + ": " + count + " quizzes, " + 
     *                        avgAttempts + " avg attempts, " + 
     *                        avgScore + "% avg score");
     * });
     */
    @Query("SELECT q.difficulty, " +
           "COUNT(q), " +
           "AVG(q.timesAttempted), " +
           "AVG(q.averageScore) " +
           "FROM Quiz q " +
           "WHERE q.published = true " +
           "GROUP BY q.difficulty " +
           "ORDER BY q.difficulty")
    List<Object[]> getStatisticsByDifficulty();
}