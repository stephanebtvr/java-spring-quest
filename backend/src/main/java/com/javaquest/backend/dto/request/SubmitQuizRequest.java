package com.javaquest.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO pour la soumission des réponses d'un quiz.
 *
 * Utilisé par le endpoint POST /api/scores/submit
 *
 * Exemple de payload JSON :
 * {
 *   "quizId": 15,
 *   "answers": [0, 2, 1, 3, 0],
 *   "timeSpentSeconds": 1250
 * }
 *
 * Workflow :
 * 1. L'utilisateur commence un quiz GET /api/quizzes/{id}
 * 2. Il répond aux questions (frontend stocke les réponses)
 * 3. Il soumet ses réponses POST /api/scores/submit
 * 4. Le backend calcule le score et retourne ScoreResponse
 */
public record SubmitQuizRequest(

    /**
     * ID du quiz complété.
     *
     * Contraintes :
     * - Ne peut pas être null
     *
     * Le service vérifiera que le quiz existe et est publié :
     *
     * Quiz quiz = quizRepository.findById(quizId)
     * .orElseThrow(() -> new QuizNotFoundException());
     *
     * if (!quiz.isPublished()) {
     * throw new QuizNotPublishedException("Ce quiz n'est pas encore publié");
     * }
     */
    @NotNull(message = "L'ID du quiz est obligatoire")
    Long quizId,

    /**
     * Liste des réponses de l'utilisateur (indices 0-3).
     *
     * Contraintes :
     * - Ne peut pas être vide
     * - Doit contenir exactement le même nombre d'éléments que de questions dans le quiz
     *
     * Format :
     * - Chaque élément est l'index de la réponse choisie (0 à 3)
     * - L'ordre correspond à l'ordre des questions dans le quiz
     *
     * Exemple pour un quiz de 5 questions :
     * [0, 2, 1, 3, 0]
     *
     * Signification :
     * - Question 1 : l'utilisateur a choisi l'option 0 (première option)
     * - Question 2 : l'utilisateur a choisi l'option 2 (troisième option)
     * - Question 3 : l'utilisateur a choisi l'option 1 (deuxième option)
     * - Question 4 : l'utilisateur a choisi l'option 3 (quatrième option)
     * - Question 5 : l'utilisateur a choisi l'option 0 (première option)
     *
     * VALIDATION CÔTÉ SERVICE :
     *
     * List<Question> questions = quiz.getQuestions();
     * if (answers.size() != questions.size()) {
     * throw new InvalidAnswersCountException(
     * "Le nombre de réponses (" + answers.size() + ") ne correspond pas " +
     * "au nombre de questions (" + questions.size() + ")"
     * );
     * }
     *
     * for (Integer answer : answers) {
     * if (answer < 0 || answer > 3) {
     * throw new InvalidAnswerIndexException("L'index doit être entre 0 et 3");
     * }
     * }
     */
    @NotEmpty(message = "Les réponses ne peuvent pas être vides")
    List<@NotNull(message = "Une réponse ne peut pas être null") Integer> answers,

    /**
     * Temps passé pour compléter le quiz (en secondes).
     *
     * Contraintes :
     * - Ne peut pas être null
     * - Doit être au moins 1 seconde (éviter les soumissions instantanées)
     *
     * Utilisation :
     * - Calcul du badge (bonus si terminé rapidement)
     * - Détection de triche potentielle (temps trop court)
     * - Statistiques utilisateur (temps moyen par quiz)
     *
     * Le frontend envoie le temps calculé avec :
     * const startTime = Date.now();
     * // L'utilisateur répond aux questions
     * const endTime = Date.now();
     * const timeSpentSeconds = Math.floor((endTime - startTime) / 1000);
     *
     * BADGE CALCULATION :
     * Le badge est attribué selon le score ET le temps :
     *
     * int expectedTime = quiz.getDurationMinutes() * 60;
     *
     * if (scorePercentage >= 90) {
     * if (timeSpentSeconds <= expectedTime * 0.7) {
     * badge = "GOLD"; // Excellent score + rapide
     * } else {
     * badge = "SILVER"; // Excellent score mais temps normal
     * }
     * } else if (scorePercentage >= 70) {
     * badge = "BRONZE"; // Bon score
     * } else {
     * badge = null; // Pas de badge
     * }
     *
     * DÉTECTION DE TRICHE :
     * Si le temps est suspicieusement court, on peut logger ou rejeter :
     *
     * int minimumReasonableTime = questions.size() * 5; // 5 sec par question minimum
     * if (timeSpentSeconds < minimumReasonableTime) {
     * throw new SuspiciousSubmissionException("Soumission trop rapide");
     * }
     */
    @NotNull(message = "Le temps passé est obligatoire")
    @Min(value = 1, message = "Le temps passé doit être d'au moins 1 seconde")
    Integer timeSpentSeconds

    /*
     * TRAITEMENT DANS ScoreService.submitQuiz()
     *
     * 1. Charger le quiz avec ses questions :
     * Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
     * .orElseThrow(() -> new QuizNotFoundException());
     *
     * 2. Valider le nombre de réponses :
     * if (answers.size() != quiz.getQuestions().size()) {
     * throw new InvalidAnswersCountException();
     * }
     *
     * 3. Calculer le score :
     * int correctAnswers = 0;
     * List<Question> questions = quiz.getQuestions();
     * for (int i = 0; i < questions.size(); i++) {
     * if (answers.get(i).equals(questions.get(i).getCorrectAnswer())) {
     * correctAnswers++;
     * }
     * }
     * int scorePercentage = (correctAnswers * 100) / questions.size();
     *
     * 4. Déterminer le badge :
     * String badge = calculateBadge(scorePercentage, timeSpentSeconds, quiz);
     *
     * 5. Vérifier si c'est le meilleur score :
     * List<Score> previousScores = scoreRepository.findByUserAndQuiz(currentUser, quiz);
     * boolean isBestScore = previousScores.stream()
     * .noneMatch(s -> s.getScorePercentage() >= scorePercentage);
     *
     * 6. Sauvegarder le score :
     * Score score = new Score();
     * score.setUser(currentUser);
     * score.setQuiz(quiz);
     * score.setScorePercentage(scorePercentage);
     * score.setCorrectAnswers(correctAnswers);
     * score.setTotalQuestions(questions.size());
     * score.setTimeSpentSeconds(timeSpentSeconds);
     * score.setBadge(badge);
     * score.setIsBestScore(isBestScore);
     * scoreRepository.save(score);
     *
     * 7. Retourner ScoreResponse avec les détails complets
     */
) {}