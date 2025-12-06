/**
 * Configuration de l'environnement de DÉVELOPPEMENT
 *
 * Ce fichier est utilisé lors du développement local (ng serve)
 * Il active les outils de debug et pointe vers le backend local
 *
 * 💡 Ce fichier peut être commité car il ne contient pas de secrets
 */
export const environment = {
  /**
   * Mode développement actif
   * Active les logs détaillés et les outils de debug
   */
  production: false,

  /**
   * URL du backend local
   * Par défaut, Spring Boot tourne sur le port 8080
   */
  apiUrl: 'http://localhost:8080',

  /**
   * Endpoints API (identiques à la prod)
   */
  api: {
    base: '/api/v1',
    auth: '/api/v1/auth',
    questions: '/api/v1/questions',
    quizzes: '/api/v1/quizzes',
    users: '/api/v1/users',
    leaderboard: '/api/v1/leaderboard',
  },

  /**
   * Clés d'API de test (versions sandbox)
   */
  firebase: {
    // Exemple de config Firebase pour dev
    // apiKey: 'AIzaSyTestKey123456789',
  },

  /**
   * Features flags pour tester de nouvelles fonctionnalités
   */
  features: {
    enableAIGeneration: true,
    enableLeaderboard: true,
    enableSocialSharing: true,
    enableDebugMode: true, // Active les logs supplémentaires
    enableMockData: false, // Utilise des données mockées si true
  },

  /**
   * Cache désactivé ou très court en dev pour voir les changements immédiatement
   */
  cache: {
    ttl: 0, // Pas de cache en dev
  },

  /**
   * Timeouts plus courts en dev pour détecter rapidement les problèmes
   */
  timeouts: {
    api: 10000, // 10 secondes
    quiz: 60000,
  },

  /**
   * Version de l'application
   */
  appVersion: '0.0.1-dev',

  /**
   * Configuration de debug
   */
  debug: {
    logApiCalls: true, // Log toutes les requêtes HTTP
    logStateChanges: true, // Log les changements d'état
    showPerformanceMetrics: true, // Affiche les métriques de performance
  },
};
