package com.javaquest.backend.repository;

import com.javaquest.backend.entity.Question;
import com.javaquest.backend.entity.enums.Difficulty;
import com.javaquest.backend.entity.enums.QuestionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des questions de quiz.
 * 
 * Ce repository offre des méthodes pour :
 * - Filtrer les questions par difficulté et catégorie
 * - Rechercher des questions par mots-clés (titre, code)
 * - Obtenir des statistiques (taux de réussite, popularité)
 * - Paginer les résultats pour de grosses collections
 * 
 * @see Question
 * @see Difficulty
 * @see QuestionCategory
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ==================== FILTRES SIMPLES ====================
    
    /**
     * Recherche toutes les questions d'un niveau de difficulté donné.
     * 
     * Requête générée : SELECT * FROM questions WHERE difficulty = :difficulty
     * 
     * @param difficulty le niveau de difficulté (BEGINNER, INTERMEDIATE, etc.)
     * @return Liste des questions correspondantes
     * 
     * Exemple :
     * List<Question> beginnerQuestions = questionRepository.findByDifficulty(Difficulty.BEGINNER);
     */
    List<Question> findByDifficulty(Difficulty difficulty);

    /**
     * Recherche toutes les questions d'une catégorie donnée.
     * 
     * Requête générée : SELECT * FROM questions WHERE category = :category
     * 
     * @param category la catégorie (JAVA_CORE, SPRING_BOOT, etc.)
     * @return Liste des questions correspondantes
     * 
     * Exemple :
     * List<Question> springQuestions = questionRepository.findByCategory(QuestionCategory.SPRING_BOOT);
     */
    List<Question> findByCategory(QuestionCategory category);

    /**
     * Filtre par difficulté ET catégorie (combinaison de critères).
     * 
     * Requête générée : 
     * SELECT * FROM questions WHERE difficulty = :difficulty AND category = :category
     * 
     * @param difficulty niveau de difficulté
     * @param category catégorie
     * @return Liste des questions correspondantes
     * 
     * Exemple : Questions Spring niveau Expert
     * List<Question> expertSpring = questionRepository.findByDifficultyAndCategory(
     *     Difficulty.EXPERT, 
     *     QuestionCategory.SPRING_BOOT
     * );
     */
    List<Question> findByDifficultyAndCategory(Difficulty difficulty, QuestionCategory category);

    // ==================== FILTRES AVEC PAGINATION ====================
    
    /**
     * Recherche par difficulté avec pagination et tri.
     * 
     * Page<T> contient :
     * - content : List<Question> de la page actuelle
     * - totalElements : nombre total de questions (toutes pages confondues)
     * - totalPages : nombre total de pages
     * - number : numéro de la page actuelle (0-based)
     * - size : taille de la page
     * 
     * @param difficulty niveau de difficulté
     * @param pageable configuration de pagination (page, size, sort)
     * @return Page de questions
     * 
     * Exemple d'utilisation :
     * // Page 0, 10 questions par page, triées par titre
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
     * Page<Question> page = questionRepository.findByDifficulty(Difficulty.INTERMEDIATE, pageable);
     * 
     * System.out.println("Total questions: " + page.getTotalElements());
     * System.out.println("Total pages: " + page.getTotalPages());
     * page.getContent().forEach(q -> System.out.println(q.getTitle()));
     */
    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);

    /**
     * Recherche par catégorie avec pagination.
     * 
     * @param category catégorie de question
     * @param pageable configuration de pagination
     * @return Page de questions
     */
    Page<Question> findByCategory(QuestionCategory category, Pageable pageable);

    // ==================== RECHERCHE FULL-TEXT ====================
    
    /**
     * Recherche des questions par mots-clés dans le titre ou l'énoncé.
     * 
     * CONCAT('%', :keyword, '%') : ajoute des % pour recherche partielle
     * LOWER() : rend la recherche insensible à la casse
     * 
     * Exemple : keyword = "loop" trouve :
     * - "For Loop in Java"
     * - "Understanding while loops"
     * - "LOOP vs RECURSION"
     * 
     * @param keyword mot-clé à rechercher (sans les %)
     * @return Liste des questions contenant le mot-clé
     * 
     * Utilisation :
     * List<Question> results = questionRepository.searchByKeyword("stream");
     */
    @Query("SELECT q FROM Question q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Question> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Recherche avancée dans titre ET code snippet.
     * 
     * Cette requête cherche le mot-clé à la fois dans :
     * - Le titre de la question
     * - Le code Java fourni (codeSnippet)
     * 
     * @param keyword mot-clé à rechercher
     * @param pageable pagination
     * @return Page de questions correspondantes
     * 
     * Exemple : keyword = "lambda" trouve des questions avec "lambda" 
     * dans le titre OU dans le code (q -> q.toString())
     */
    @Query("SELECT q FROM Question q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.codeSnippet) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> searchInTitleAndCode(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Recherche multicritères : mots-clés + difficulté + catégorie.
     * 
     * Permet de combiner filtres et recherche textuelle.
     * Les paramètres peuvent être null (recherche flexible).
     * 
     * @param keyword mot-clé optionnel (peut être null)
     * @param difficulty difficulté optionnelle (peut être null)
     * @param category catégorie optionnelle (peut être null)
     * @param pageable pagination
     * @return Page de questions filtrées
     * 
     * Exemple : Questions Spring de niveau Expert contenant "security"
     * Page<Question> results = questionRepository.searchQuestions(
     *     "security", 
     *     Difficulty.EXPERT, 
     *     QuestionCategory.SPRING_SECURITY,
     *     PageRequest.of(0, 20)
     * );
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:category IS NULL OR q.category = :category)")
    Page<Question> searchQuestions(
        @Param("keyword") String keyword,
        @Param("difficulty") Difficulty difficulty,
        @Param("category") QuestionCategory category,
        Pageable pageable
    );

    // ==================== STATISTIQUES ====================
    
    /**
     * Compte le nombre de questions par catégorie.
     * 
     * Requête générée : SELECT COUNT(*) FROM questions WHERE category = :category
     * 
     * @param category catégorie à compter
     * @return nombre de questions dans cette catégorie
     * 
     * Exemple : Combien de questions Spring Boot ?
     * Long count = questionRepository.countByCategory(QuestionCategory.SPRING_BOOT);
     */
    Long countByCategory(QuestionCategory category);

    /**
     * Trouve les questions les plus posées (popularité).
     * 
     * ORDER BY q.timesAsked DESC : tri décroissant par popularité
     * LIMIT intégré dans le Pageable
     * 
     * @param pageable limite le nombre de résultats (ex: top 10)
     * @return Page des questions les plus populaires
     * 
     * Exemple : Top 10 questions les plus posées
     * Pageable top10 = PageRequest.of(0, 10);
     * Page<Question> popular = questionRepository.findMostAskedQuestions(top10);
     */
    @Query("SELECT q FROM Question q ORDER BY q.timesAsked DESC")
    Page<Question> findMostAskedQuestions(Pageable pageable);

    /**
     * Trouve les questions les plus difficiles (faible taux de réussite).
     * 
     * Calcul : timesAnsweredCorrectly / timesAsked < 0.4 (moins de 40% de bonnes réponses)
     * Cette requête identifie les questions piégeuses ou mal comprises.
     * 
     * @return Liste des questions avec taux de succès < 40%
     * 
     * Exemple : Questions à revoir pour améliorer l'explication
     * List<Question> hardQuestions = questionRepository.findDifficultQuestions();
     * hardQuestions.forEach(q -> 
     *     System.out.println(q.getTitle() + " - Success rate: " + q.getSuccessRate())
     * );
     */
    @Query("SELECT q FROM Question q WHERE q.timesAsked > 10 AND " +
           "(CAST(q.timesAnsweredCorrectly AS double) / q.timesAsked) < 0.4 " +
           "ORDER BY (CAST(q.timesAnsweredCorrectly AS double) / q.timesAsked) ASC")
    List<Question> findDifficultQuestions();

    /**
     * Calcule le taux de réussite moyen par catégorie.
     * 
     * Requête d'agrégation avec GROUP BY.
     * Retourne un array : [category, avgSuccessRate]
     * 
     * @return Liste de Object[] contenant [QuestionCategory, Double]
     * 
     * Exemple de traitement :
     * List<Object[]> stats = questionRepository.getSuccessRateByCategory();
     * stats.forEach(row -> {
     *     QuestionCategory category = (QuestionCategory) row[0];
     *     Double avgRate = (Double) row[1];
     *     System.out.println(category + ": " + avgRate + "%");
     * });
     */
    @Query("SELECT q.category, " +
           "AVG(CAST(q.timesAnsweredCorrectly AS double) / NULLIF(q.timesAsked, 0)) * 100 " +
           "FROM Question q " +
           "WHERE q.timesAsked > 0 " +
           "GROUP BY q.category " +
           "ORDER BY AVG(CAST(q.timesAnsweredCorrectly AS double) / NULLIF(q.timesAsked, 0)) DESC")
    List<Object[]> getSuccessRateByCategory();

    // ==================== REQUÊTES OPTIMISÉES ====================
    
    /**
     * Récupère une question avec ses quiz associés (évite N+1 queries).
     * 
     * LEFT JOIN FETCH : charge immédiatement la collection quizzes
     * Sans FETCH : chaque appel à q.getQuizzes() déclencherait une nouvelle requête SQL
     * Avec FETCH : tout est chargé en 1 seule requête
     * 
     * @param id ID de la question
     * @return Optional<Question> avec quizzes pré-chargés
     * 
     * Problème N+1 sans FETCH :
     * Question q = repo.findById(1L).get();     // 1 requête
     * q.getQuizzes().forEach(...);              // N requêtes (1 par quiz) ❌
     * 
     * Solution avec FETCH :
     * Question q = repo.findByIdWithQuizzes(1L).get(); // 1 seule requête ✅
     * q.getQuizzes().forEach(...);                     // 0 requête supplémentaire
     */
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.quizzes WHERE q.id = :id")
    Optional<Question> findByIdWithQuizzes(@Param("id") Long id);

    /**
     * Trouve des questions aléatoires pour générer un quiz.
     * 
     * FUNCTION('RANDOM') : utilise la fonction SQL RANDOM() de PostgreSQL
     * Cette méthode est simple mais pas optimale pour de très grosses tables.
     * 
     * Pour une meilleure performance sur de gros volumes, il faudrait :
     * - Utiliser un algorithme de sélection aléatoire en Java
     * - Ou utiliser une requête native SQL avec TABLESAMPLE
     * 
     * @param difficulty niveau de difficulté
     * @param category catégorie
     * @param limit nombre de questions à récupérer
     * @param pageable pagination (size = limit)
     * @return Page de questions aléatoires
     * 
     * Exemple : Générer un quiz de 10 questions Spring niveau Intermediate
     * Pageable pageable = PageRequest.of(0, 10);
     * Page<Question> randomQuestions = questionRepository.findRandomQuestions(
     *     Difficulty.INTERMEDIATE,
     *     QuestionCategory.SPRING_BOOT,
     *     10,
     *     pageable
     * );
     */
    @Query("SELECT q FROM Question q WHERE " +
           "q.difficulty = :difficulty AND " +
           "q.category = :category " +
           "ORDER BY FUNCTION('RANDOM')")
    Page<Question> findRandomQuestions(
        @Param("difficulty") Difficulty difficulty,
        @Param("category") QuestionCategory category,
        @Param("limit") Integer limit,
        Pageable pageable
    );

    // ==================== VALIDATION ====================
    
    /**
     * Vérifie si une question avec ce titre existe déjà.
     * 
     * Utile pour éviter les doublons lors de la création de questions.
     * 
     * @param title titre de la question
     * @return true si une question avec ce titre existe déjà
     * 
     * Exemple dans un service :
     * if (questionRepository.existsByTitle(title)) {
     *     throw new DuplicateQuestionException("Cette question existe déjà");
     * }
     */
    Boolean existsByTitle(String title);
}