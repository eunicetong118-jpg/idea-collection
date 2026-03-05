# Idea Collection Application Design

**Date:** 2026-03-05

## 1. Architecture Overview

**Tech Stack:**
- **Frontend:** React SPA
- **Backend:** Spring Boot REST API
- **Database:** MongoDB
- **Real-time:** WebSockets (STOMP over WebSocket)
- **Containerization:** Docker + Docker Compose

**High-Level Flow:**
- Users access the landing page to register/login
- After login, they see the main dashboard with theme and sub-topics
- They can browse, submit ideas, like, and comment in real-time
- Admin accesses a separate admin panel to manage themes/sub-topics
- WebSockets ensure all users see updates instantly

---

## 2. Frontend Components and Pages

**Pages:**

1. **Landing Page** — Login/Registration forms, admin button (top-right)
2. **Admin Panel** — Theme input, sub-topic management table
3. **Main Dashboard** — Theme display, sub-topic tabs, idea cards

**Key Components:**

- **Header:** Shows main theme + sub-topic buttons (tab navigation)
- **Dashboard Area:** Displays cards for selected sub-topic
- **Idea Card:**
  - Status bar: `{Stage}: {Stage_status}` (clickable by admin to change)
  - Idea details (title, description, author, timestamp)
  - Like button with counter (unique per user)
  - Comment button — expands card to show comments + input
- **Add Button** (bottom-right): Opens idea input form modal
- **Sort Controls:** Toggle between combined/default, recent, most liked

**Behavior:**
- Completed cards (Stage=Implement, Status=Done) are greyed and pushed to bottom
- Real-time updates via WebSockets for likes, comments, new cards

---

## 3. Backend Services and API Design

**Services:**

1. **AuthService** — Registration, login, JWT token management
2. **ThemeService** — Manage main theme (CRUD)
3. **SubTopicService** — Manage sub-topics (CRUD, get card counts)
4. **IdeaService** — Submit ideas, retrieve, update status
5. **LikeService** — Handle likes (unique per user, toggle on/off)
6. **CommentService** — Handle comments (add, retrieve)
7. **WebSocketService** — Push real-time updates

**API Endpoints (REST):**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register user |
| `/api/auth/login` | POST | Login, returns JWT |
| `/api/theme` | GET/POST/PUT | Get/set main theme |
| `/api/subtopics` | GET/POST | List/create sub-topics |
| `/api/subtopics/{id}` | PUT/DELETE | Edit/delete sub-topic |
| `/api/ideas` | GET/POST | List ideas / submit idea |
| `/api/ideas/{id}/status` | PUT | Update stage/status |
| `/api/ideas/{id}/like` | POST | Toggle like |
| `/api/ideas/{id}/comments` | GET/POST | Get/add comments |

**WebSocket Topics:**
- `/topic/ideas/{subtopicId}` — New ideas, status changes
- `/topic/ideas/{ideaId}/likes` — Like updates
- `/topic/ideas/{ideaId}/comments` — New comments

**Error Handling:**
- All errors return structured JSON: `{ "error": "message", "code": "ERROR_CODE" }`
- FE catches errors and displays user-friendly messages

---

## 4. Data Models (MongoDB Collections)

**Users:**
```json
{
  "_id": "ObjectId",
  "username": "String (unique)",
  "passwordHash": "String",
  "isAdmin": "Boolean",
  "createdAt": "Date"
}
```

**Theme:**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "updatedAt": "Date"
}
```

**SubTopics:**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "cardCount": "Number",
  "createdAt": "Date"
}
```

**Ideas:**
```json
{
  "_id": "ObjectId",
  "subTopicId": "ObjectId (ref: SubTopics)",
  "title": "String",
  "description": "String",
  "author": "String",
  "stage": "String (Review | Implement)",
  "stageStatus": "String (New | WIP | Done)",
  "likes": ["ObjectId"] (user IDs),
  "likeCount": "Number",
  "createdAt": "Date",
  "updatedAt": "Date",
  "lastCommentAt": "Date"
}
```

**Comments:**
```json
{
  "_id": "ObjectId",
  "ideaId": "ObjectId (ref: Ideas)",
  "author": "String",
  "content": "String",
  "createdAt": "Date"
}
```

---

## 5. User Interactions and Flows

### Authentication

**Registration/Login Flow:**
1. User enters username/password on landing page
2. On submit, FE validates inputs (no empty fields, password min length)
3. If FE validation fails, show error locally — no API call
4. If valid, POST to `/api/auth/register` or `/api/auth/login`
5. On success, store JWT in localStorage, redirect to dashboard
6. On error, display backend error message

### Admin Panel

**Edit Main Theme:**
1. Admin clicks "Admin" button (top-right on landing)
2. Redirected to Admin Panel
3. Theme section shows current main theme
4. Admin edits text input, clicks "Update Theme"
5. PUT to `/api/theme` → main dashboard updates in real-time

**Create Sub-Topic:**
1. Admin Panel has "Add Sub-Topic" section
2. Admin enters sub-topic name, clicks "Create"
3. POST to `/api/subtopics` → new dashboard tab appears
4. Table below updates with new row

**Edit Sub-Topic:**
1. Admin Panel shows table of existing sub-topics
2. Admin clicks Edit button on a row
3. Input field becomes editable inline
4. Admin changes name, clicks Save → PUT to `/api/subtopics/{id}`

**Delete Sub-Topic:**
1. Admin clicks Delete button on a row
2. Confirmation dialog appears
3. On confirm → DELETE to `/api/subtopics/{id}`
4. Sub-topic removed from dashboard tabs, cards deleted

### Dashboard

**Submit Idea Flow:**
1. User clicks Add button (bottom-right)
2. Modal opens with form: title, description
3. User fills form, clicks Submit
4. FE validates → POST to `/api/ideas`
5. On success, new card appears (real-time via WebSocket)
6. On error, display error in modal

**Like Flow:**
1. User clicks like button on card
2. FE validates logged-in → POST toggle to `/api/ideas/{id}/like`
3. Like count updates (real-time), button shows liked state
4. One user = one like (toggle on/off)

**Comment Flow:**
1. User clicks comment button on card
2. Card expands downward
3. Existing comments shown + input box at bottom
4. User types, clicks Submit → POST to `/api/ideas/{id}/comments`
5. Comment appears (real-time), card moves up in sorting

**Admin Status Change:**
1. Admin clicks status bar on any card
2. Dropdown shows stage options (Review/Implement) + status (New/WIP/Done)
3. Admin selects new status → PUT to `/api/ideas/{id}/status`
4. Card updates (real-time); if Done, greyed and pushed to bottom

**Card Sorting:**
- Default: Combined score (recent activity + likes)
- User can toggle: "Most Recent", "Most Liked", "Most Discussed"

---

## 6. Security and Real-Time

### Authentication
- JWT tokens (access token, no refresh for simplicity)
- Passwords hashed with BCrypt
- Token stored in localStorage, sent in Authorization header
- Protected endpoints require valid JWT
- Admin endpoints checked via `user.isAdmin` flag

### WebSocket Security
- Same JWT used for WebSocket handshake
- Subscribe allowed only for authenticated users
- Topics scoped by subTopicId (users can subscribe to any)

### Input Validation
- Backend: Spring Validation annotations on DTOs
- Frontend: Form validation before API calls

---

## 7. Docker and Project Structure

### Docker Compose Setup

```yaml
services:
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/idea-collection
    depends_on:
      - mongodb

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  mongo_data:
```

### Project Structure

```
/idea-collection
├── docker-compose.yml
├── backend/
│   ├── src/main/java/...
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── package.json
└── docs/plans/
```
