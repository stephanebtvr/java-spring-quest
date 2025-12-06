# JavaQuest – Master Java & Spring Boot

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3+-success)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-18+-red)](https://angular.dev)
[![Java 21](https://img.shields.io/badge/Java-21-important)](https://openjdk.org/)

**JavaQuest** est une application web moderne de quizzes interactifs dédiée à Java et Spring Boot.

## 🎯 Objectif du Projet

Application éducative permettant aux développeurs Java de :

- S'entraîner aux entretiens techniques
- Préparer les certifications (OCA/OCP, Spring Professional)
- Progresser de Beginner à Architect

## 📦 Modules

- **backend/** : API REST Spring Boot 3.3+ (Java 21)
- **frontend/** : Application Angular 18+ (Standalone Components)
- **docs/** : Documentation technique et guides

## 🚀 Démarrage Rapide

### Prérequis

- Docker & Docker Compose
- Java 21 (pour développement backend)
- Node.js 20+ (pour développement frontend)

### Lancement avec Docker

```bash
docker-compose up --build
```

L'application sera accessible sur :

- Frontend : http://localhost:4200
- Backend API : http://localhost:8080
- Swagger UI : http://localhost:8080/swagger-ui.html

### Développement Local

Voir les README spécifiques :

- [Backend Setup](./backend/README.md)
- [Frontend Setup](./frontend/README.md)

## 🏗️ Architecture

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│   Angular   │─────▶│  Spring Boot │─────▶│  PostgreSQL  │
│  Frontend   │◀─────│   REST API   │◀─────│   Database   │
└─────────────┘      └──────────────┘      └──────────────┘
```

## 🛠️ Technologies

### Backend

- Java 21 (Virtual Threads)
- Spring Boot 3.3+
- Spring Security 6 + JWT
- PostgreSQL
- JUnit 5 + Mockito

### Frontend

- Angular 18 (Signals, Standalone)
- Angular Material + TailwindCSS
- RxJS 7+
- Jest + Cypress

## 📝 Conventions Git

### Branches

- `main` : production
- `develop` : développement
- `feature/nom-feature` : nouvelles fonctionnalités
- `bugfix/nom-bug` : corrections de bugs

### Commits (Conventional Commits)

- `feat:` nouvelle fonctionnalité
- `fix:` correction de bug
- `docs:` documentation
- `chore:` tâches maintenance
- `test:` ajout/modification tests
- `refactor:` refactoring code

## 👨‍💻 Auteur

Bettaver Stéphane

---

**Status**: 🚧 En développement actif
