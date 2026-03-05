# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the "Idea Collection" application - a full-stack web app where users can submit, browse, like, and comment on ideas organized by sub-topics.

**Tech Stack:**
- Frontend: React SPA
- Backend: Spring Boot REST API
- Database: MongoDB
- Real-time: WebSockets (STOMP over WebSocket)
- Containerization: Docker + Docker Compose

## Development Workflow

This project uses the **superpowers** workflow for feature development:

1. **Brainstorming** - Use `superpowers:brainstorming` skill before any creative work
2. **Design** - Document design in `docs/plans/YYYY-MM-DD-<feature>-design.md`
3. **Planning** - Use `superpowers:writing-plans` to create implementation plan
4. **Implementation** - Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement

## Architecture

```
/idea-collection
├── docker-compose.yml          # Orchestrates mongodb, backend, frontend
├── backend/                    # Spring Boot app (port 8080)
│   ├── src/main/java/...       # Controllers, Services, Models
│   └── Dockerfile
├── frontend/                   # React app (port 3000)
│   ├── src/                    # Components, Pages, API
│   └── Dockerfile
└── docs/plans/                 # Design and implementation plans
```

## Common Commands

```bash
# Start all services
docker-compose up --build

# Start only backend for development
cd backend && ./mvnw spring-boot:run

# Start only frontend for development
cd frontend && npm start
```

## Key Design Decisions

- JWT authentication with BCrypt password hashing
- Single hardcoded admin user (username: admin)
- WebSocket topics: `/topic/ideas/{subtopicId}`, `/topic/ideas/{ideaId}/likes`, `/topic/ideas/{ideaId}/comments`
- Completed cards (Stage=Implement, Status=Done) are greyed and pushed to bottom
