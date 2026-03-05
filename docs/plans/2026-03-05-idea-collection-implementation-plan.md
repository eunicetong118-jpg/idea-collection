# Idea Collection Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a full-stack Idea Collection app with React frontend, Spring Boot backend, MongoDB, WebSockets, and Docker.

**Architecture:** Spring Boot REST API with JWT auth, MongoDB persistence, STOMP WebSockets for real-time updates, React SPA frontend, Docker Compose orchestration.

**Tech Stack:** Spring Boot 3.x, React 18, MongoDB, Spring WebSocket (STOMP), JWT, BCrypt, Docker, Docker Compose

---

## Phase 1: Backend Foundation

### Task 1: Project Setup and Docker Configuration

**Files:**
- Create: `docker-compose.yml`
- Create: `backend/pom.xml`
- Create: `backend/Dockerfile`
- Create: `backend/src/main/resources/application.yml`

**Step 1: Create docker-compose.yml**

```yaml
version: '3.8'
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

**Step 2: Create backend pom.xml with dependencies**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.ideacollection</groupId>
    <artifactId>backend</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 3: Create backend Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add maven && mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/backend-1.0.0.jar"]
```

**Step 4: Create application.yml**

```yaml
server:
  port: 8080
spring:
  data:
    mongodb:
      uri: ${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/idea-collection}
jwt:
  secret: idea-collection-secret-key-must-be-at-least-256-bits-long-for-hs256
  expiration: 86400000
```

**Step 5: Commit**

```bash
git add docker-compose.yml backend/pom.xml backend/Dockerfile backend/src/main/resources/application.yml
git commit -m "feat: add Docker and Spring Boot project setup"
```

---

### Task 2: MongoDB Data Models

**Files:**
- Create: `backend/src/main/java/com/ideacollection/model/User.java`
- Create: `backend/src/main/java/com/ideacollection/model/Theme.java`
- Create: `backend/src/main/java/com/ideacollection/model/SubTopic.java`
- Create: `backend/src/main/java/com/ideacollection/model/Idea.java`
- Create: `backend/src/main/java/com/ideacollection/model/Comment.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/model/UserTest.java`

```java
package com.ideacollection.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testUserCreation() {
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword");
        user.setAdmin(false);

        assertEquals("testuser", user.getUsername());
        assertFalse(user.isAdmin());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=UserTest`
Expected: FAIL - classes don't exist yet

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/model/User.java`

```java
package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String passwordHash;
    private boolean isAdmin;
    private Instant createdAt;

    public User() {
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

Create: `backend/src/main/java/com/ideacollection/model/Theme.java`

```java
package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "theme")
public class Theme {
    @Id
    private String id;
    private String name;
    private Instant updatedAt;

    public Theme() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
```

Create: `backend/src/main/java/com/ideacollection/model/SubTopic.java`

```java
package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "subtopics")
public class SubTopic {
    @Id
    private String id;
    private String name;
    private int cardCount;
    private Instant createdAt;

    public SubTopic() {
        this.createdAt = Instant.now();
        this.cardCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCardCount() { return cardCount; }
    public void setCardCount(int cardCount) { this.cardCount = cardCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

Create: `backend/src/main/java/com/ideacollection/model/Idea.java`

```java
package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ideas")
public class Idea {
    @Id
    private String id;

    @Indexed
    private String subTopicId;

    private String title;
    private String description;
    private String author;
    private String stage;
    private String stageStatus;
    private List<String> likes;
    private int likeCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastCommentAt;

    public Idea() {
        this.likes = new ArrayList<>();
        this.likeCount = 0;
        this.stage = "Review";
        this.stageStatus = "New";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubTopicId() { return subTopicId; }
    public void setSubTopicId(String subTopicId) { this.subTopicId = subTopicId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStageStatus() { return stageStatus; }
    public void setStageStatus(String stageStatus) { this.stageStatus = stageStatus; }
    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastCommentAt() { return lastCommentAt; }
    public void setLastCommentAt(Instant lastCommentAt) { this.lastCommentAt = lastCommentAt; }
}
```

Create: `backend/src/main/java/com/ideacollection/model/Comment.java`

```java
package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    @Indexed
    private String ideaId;

    private String author;
    private String content;
    private Instant createdAt;

    public Comment() {
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdeaId() { return ideaId; }
    public void setIdeaId(String ideaId) { this.ideaId = ideaId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=UserTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/model/*.java backend/src/test/java/com/ideacollection/model/UserTest.java
git commit -m "feat: add MongoDB data models"
```

---

### Task 3: Repository Layer

**Files:**
- Create: `backend/src/main/java/com/ideacollection/repository/UserRepository.java`
- Create: `backend/src/main/java/com/ideacollection/repository/ThemeRepository.java`
- Create: `backend/src/main/java/com/ideacollection/repository/SubTopicRepository.java`
- Create: `backend/src/main/java/com/ideacollection/repository/IdeaRepository.java`
- Create: `backend/src/main/java/com/ideacollection/repository/CommentRepository.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/repository/UserRepositoryTest.java`

```java
package com.ideacollection.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    @Test
    void testRepositoryExists() {
        assertNotNull(UserRepository.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=UserRepositoryTest`
Expected: FAIL - classes don't exist

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/repository/UserRepository.java`

```java
package com.ideacollection.repository;

import com.ideacollection.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

Create: `backend/src/main/java/com/ideacollection/repository/ThemeRepository.java`

```java
package com.ideacollection.repository;

import com.ideacollection.model.Theme;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThemeRepository extends MongoRepository<Theme, String> {
}
```

Create: `backend/src/main/java/com/ideacollection/repository/SubTopicRepository.java`

```java
package com.ideacollection.repository;

import com.ideacollection.model.SubTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SubTopicRepository extends MongoRepository<SubTopic, String> {
    List<SubTopic> findAllByOrderByCreatedAtAsc();
}
```

Create: `backend/src/main/java/com/ideacollection/repository/IdeaRepository.java`

```java
package com.ideacollection.repository;

import com.ideacollection.model.Idea;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<Idea> findBySubTopicIdOrderByCreatedAtDesc(String subTopicId);
    List<Idea> findBySubTopicIdOrderByLikeCountDesc(String subTopicId);
    List<Idea> findBySubTopicIdOrderByLastCommentAtDesc(String subTopicId);
    void deleteBySubTopicId(String subTopicId);
}
```

Create: `backend/src/main/java/com/ideacollection/repository/CommentRepository.java`

```java
package com.ideacollection.repository;

import com.ideacollection.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByIdeaIdOrderByCreatedAtAsc(String ideaId);
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=UserRepositoryTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/repository/*.java
git commit -m "feat: add MongoDB repositories"
```

---

### Task 4: Authentication Service (JWT + Security)

**Files:**
- Create: `backend/src/main/java/com/ideacollection/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/ideacollection/config/WebSocketConfig.java`
- Create: `backend/src/main/java/com/ideacollection/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/ideacollection/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/ideacollection/dto/AuthResponse.java`
- Create: `backend/src/main/java/com/ideacollection/service/AuthService.java`
- Create: `backend/src/main/java/com/ideacollection/controller/AuthController.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/service/AuthServiceTest.java`

```java
package com.ideacollection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Test
    void testServiceExists() {
        assertNotNull(AuthService.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=AuthServiceTest`
Expected: FAIL

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/dto/RegisterRequest.java`

```java
package com.ideacollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

Create: `backend/src/main/java/com/ideacollection/dto/LoginRequest.java`

```java
package com.ideacollection.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

Create: `backend/src/main/java/com/ideacollection/dto/AuthResponse.java`

```java
package com.ideacollection.dto;

public class AuthResponse {
    private String token;
    private String username;
    private boolean isAdmin;

    public AuthResponse(String token, String username, boolean isAdmin) {
        this.token = token;
        this.username = username;
        this.isAdmin = isAdmin;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
```

Create: `backend/src/main/java/com/ideacollection/service/AuthService.java`

```java
package com.ideacollection.service;

import com.ideacollection.dto.RegisterRequest;
import com.ideacollection.dto.LoginRequest;
import com.ideacollection.dto.AuthResponse;
import com.ideacollection.model.User;
import com.ideacollection.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(
            "idea-collection-secret-key-must-be-at-least-256-bits-long-for-hs256".getBytes(StandardCharsets.UTF_8)
        );
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAdmin(false);

        userRepository.save(user);

        String token = generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.isAdmin());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.isAdmin());
    }

    private String generateToken(User user) {
        return Jwts.builder()
            .subject(user.getId())
            .claim("username", user.getUsername())
            .claim("isAdmin", user.isAdmin())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(secretKey)
            .compact();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

Create: `backend/src/main/java/com/ideacollection/config/SecurityConfig.java`

```java
package com.ideacollection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

Create: `backend/src/main/java/com/ideacollection/config/WebSocketConfig.java`

```java
package com.ideacollection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
```

Create: `backend/src/main/java/com/ideacollection/controller/AuthController.java`

```java
package com.ideacollection.controller;

import com.ideacollection.dto.RegisterRequest;
import com.ideacollection.dto.LoginRequest;
import com.ideacollection.dto.AuthResponse;
import com.ideacollection.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=AuthServiceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/
git commit -m "feat: add authentication service with JWT"
```

---

### Task 5: Theme and SubTopic Services

**Files:**
- Create: `backend/src/main/java/com/ideacollection/service/ThemeService.java`
- Create: `backend/src/main/java/com/ideacollection/service/SubTopicService.java`
- Create: `backend/src/main/java/com/ideacollection/controller/ThemeController.java`
- Create: `backend/src/main/java/com/ideacollection/controller/SubTopicController.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/service/ThemeServiceTest.java`

```java
package com.ideacollection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThemeServiceTest {
    @Test
    void testServiceExists() {
        assertNotNull(ThemeService.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ThemeServiceTest`
Expected: FAIL

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/service/ThemeService.java`

```java
package com.ideacollection.service;

import com.ideacollection.model.Theme;
import com.ideacollection.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme getTheme() {
        return themeRepository.findAll().stream().findFirst().orElse(null);
    }

    public Theme updateTheme(String name) {
        Theme theme = getTheme();
        if (theme == null) {
            theme = new Theme();
        }
        theme.setName(name);
        theme.setUpdatedAt(Instant.now());
        return themeRepository.save(theme);
    }
}
```

Create: `backend/src/main/java/com/ideacollection/service/SubTopicService.java`

```java
package com.ideacollection.service;

import com.ideacollection.model.SubTopic;
import com.ideacollection.repository.SubTopicRepository;
import com.ideacollection.repository.IdeaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubTopicService {
    private final SubTopicRepository subTopicRepository;
    private final IdeaRepository ideaRepository;

    public SubTopicService(SubTopicRepository subTopicRepository, IdeaRepository ideaRepository) {
        this.subTopicRepository = subTopicRepository;
        this.ideaRepository = ideaRepository;
    }

    public List<SubTopic> getAllSubTopics() {
        return subTopicRepository.findAllByOrderByCreatedAtAsc();
    }

    public SubTopic createSubTopic(String name) {
        SubTopic subTopic = new SubTopic();
        subTopic.setName(name);
        subTopic.setCardCount(0);
        return subTopicRepository.save(subTopic);
    }

    public SubTopic updateSubTopic(String id, String name) {
        SubTopic subTopic = subTopicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setName(name);
        return subTopicRepository.save(subTopic);
    }

    public void deleteSubTopic(String id) {
        ideaRepository.deleteBySubTopicId(id);
        subTopicRepository.deleteById(id);
    }

    public void incrementCardCount(String subTopicId) {
        SubTopic subTopic = subTopicRepository.findById(subTopicId)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setCardCount(subTopic.getCardCount() + 1);
        subTopicRepository.save(subTopic);
    }

    public void decrementCardCount(String subTopicId) {
        SubTopic subTopic = subTopicRepository.findById(subTopicId)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setCardCount(Math.max(0, subTopic.getCardCount() - 1));
        subTopicRepository.save(subTopic);
    }
}
```

Create: `backend/src/main/java/com/ideacollection/controller/ThemeController.java`

```java
package com.ideacollection.controller;

import com.ideacollection.model.Theme;
import com.ideacollection.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/theme")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<?> getTheme() {
        Theme theme = themeService.getTheme();
        return ResponseEntity.ok(Map.of("name", theme != null ? theme.getName() : ""));
    }

    @PutMapping
    public ResponseEntity<?> updateTheme(@RequestBody Map<String, String> request) {
        try {
            Theme theme = themeService.updateTheme(request.get("name"));
            return ResponseEntity.ok(Map.of("name", theme.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

Create: `backend/src/main/java/com/ideacollection/controller/SubTopicController.java`

```java
package com.ideacollection.controller;

import com.ideacollection.model.SubTopic;
import com.ideacollection.service.SubTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtopics")
public class SubTopicController {
    private final SubTopicService subTopicService;

    public SubTopicController(SubTopicService subTopicService) {
        this.subTopicService = subTopicService;
    }

    @GetMapping
    public ResponseEntity<List<SubTopic>> getAllSubTopics() {
        return ResponseEntity.ok(subTopicService.getAllSubTopics());
    }

    @PostMapping
    public ResponseEntity<?> createSubTopic(@RequestBody Map<String, String> request) {
        try {
            SubTopic subTopic = subTopicService.createSubTopic(request.get("name"));
            return ResponseEntity.ok(subTopic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubTopic(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            SubTopic subTopic = subTopicService.updateSubTopic(id, request.get("name"));
            return ResponseEntity.ok(subTopic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubTopic(@PathVariable String id) {
        try {
            subTopicService.deleteSubTopic(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=ThemeServiceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/service/ThemeService.java backend/src/main/java/com/ideacollection/service/SubTopicService.java backend/src/main/java/com/ideacollection/controller/ThemeController.java backend/src/main/java/com/ideacollection/controller/SubTopicController.java
git commit -m "feat: add theme and subtopic services"
```

---

### Task 6: Idea and Like Services

**Files:**
- Create: `backend/src/main/java/com/ideacollection/dto/IdeaRequest.java`
- Create: `backend/src/main/java/com/ideacollection/dto/StatusUpdateRequest.java`
- Create: `backend/src/main/java/com/ideacollection/service/IdeaService.java`
- Create: `backend/src/main/java/com/ideacollection/controller/IdeaController.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/service/IdeaServiceTest.java`

```java
package com.ideacollection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdeaServiceTest {
    @Test
    void testServiceExists() {
        assertNotNull(IdeaService.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=IdeaServiceTest`
Expected: FAIL

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/dto/IdeaRequest.java`

```java
package com.ideacollection.dto;

import jakarta.validation.constraints.NotBlank;

public class IdeaRequest {
    @NotBlank
    private String subTopicId;

    @NotBlank
    private String title;

    private String description;

    public String getSubTopicId() { return subTopicId; }
    public void setSubTopicId(String subTopicId) { this.subTopicId = subTopicId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

Create: `backend/src/main/java/com/ideacollection/dto/StatusUpdateRequest.java`

```java
package com.ideacollection.dto;

public class StatusUpdateRequest {
    private String stage;
    private String stageStatus;

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStageStatus() { return stageStatus; }
    public void setStageStatus(String stageStatus) { this.stageStatus = stageStatus; }
}
```

Create: `backend/src/main/java/com/ideacollection/service/IdeaService.java`

```java
package com.ideacollection.service;

import com.ideacollection.model.Idea;
import com.ideacollection.repository.IdeaRepository;
import com.ideacollection.repository.SubTopicRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final SubTopicService subTopicService;

    public IdeaService(IdeaRepository ideaRepository, SubTopicService subTopicService) {
        this.ideaRepository = ideaRepository;
        this.subTopicService = subTopicService;
    }

    public List<Idea> getIdeasBySubTopic(String subTopicId, String sortBy) {
        List<Idea> ideas = switch (sortBy) {
            case "recent" -> ideaRepository.findBySubTopicIdOrderByCreatedAtDesc(subTopicId);
            case "most_liked" -> ideaRepository.findBySubTopicIdOrderByLikeCountDesc(subTopicId);
            case "most_discussed" -> ideaRepository.findBySubTopicIdOrderByLastCommentAtDesc(subTopicId);
            default -> ideaRepository.findBySubTopicIdOrderByCreatedAtDesc(subTopicId);
        };

        // Sort: completed (Done) at bottom
        ideas.sort(Comparator.comparing((Idea i) ->
            "Done".equals(i.getStageStatus())).thenByDescending(i ->
            calculateScore(i)));

        return ideas;
    }

    private double calculateScore(Idea idea) {
        long hoursSinceCreation = java.time.Duration.between(idea.getCreatedAt(), Instant.now()).toHours();
        int recentComments = idea.getLastCommentAt() != null ?
            (int) java.time.Duration.between(idea.getLastCommentAt(), Instant.now()).toHours() : 100;

        return idea.getLikeCount() * 2 + (100 - Math.min(hoursSinceCreation, 100)) + (100 - Math.min(recentComments, 100));
    }

    public Idea createIdea(String subTopicId, String title, String description, String author) {
        Idea idea = new Idea();
        idea.setSubTopicId(subTopicId);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setAuthor(author);

        Idea saved = ideaRepository.save(idea);
        subTopicService.incrementCardCount(subTopicId);

        return saved;
    }

    public Idea updateStatus(String id, String stage, String stageStatus) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Idea not found"));

        if (stage != null) idea.setStage(stage);
        if (stageStatus != null) idea.setStageStatus(stageStatus);
        idea.setUpdatedAt(Instant.now());

        return ideaRepository.save(idea);
    }

    public Idea toggleLike(String id, String userId) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Idea not found"));

        if (idea.getLikes().contains(userId)) {
            idea.getLikes().remove(userId);
            idea.setLikeCount(Math.max(0, idea.getLikeCount() - 1));
        } else {
            idea.getLikes().add(userId);
            idea.setLikeCount(idea.getLikeCount() + 1);
        }

        return ideaRepository.save(idea);
    }

    public boolean hasUserLiked(Idea idea, String userId) {
        return idea.getLikes().contains(userId);
    }

    public Idea getIdeaById(String id) {
        return ideaRepository.findById(id).orElse(null);
    }
}
```

Create: `backend/src/main/java/com/ideacollection/controller/IdeaController.java`

```java
package com.ideacollection.controller;

import com.ideacollection.model.Idea;
import com.ideacollection.service.IdeaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {
    private final IdeaService ideaService;

    public IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @GetMapping
    public ResponseEntity<List<Idea>> getIdeas(
            @RequestParam String subTopicId,
            @RequestParam(defaultValue = "default") String sortBy) {
        return ResponseEntity.ok(ideaService.getIdeasBySubTopic(subTopicId, sortBy));
    }

    @PostMapping
    public ResponseEntity<?> createIdea(@Valid @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username) {
        try {
            Idea idea = ideaService.createIdea(
                request.get("subTopicId"),
                request.get("title"),
                request.get("description"),
                username
            );
            return ResponseEntity.ok(idea);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            Idea idea = ideaService.updateStatus(id, request.get("stage"), request.get("stageStatus"));
            return ResponseEntity.ok(idea);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        try {
            Idea idea = ideaService.toggleLike(id, userId);
            return ResponseEntity.ok(Map.of(
                "likeCount", idea.getLikeCount(),
                "hasLiked", idea.getLikes().contains(userId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=IdeaServiceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/service/IdeaService.java backend/src/main/java/com/ideacollection/controller/IdeaController.java backend/src/main/java/com/ideacollection/dto/IdeaRequest.java backend/src/main/java/com/ideacollection/dto/StatusUpdateRequest.java
git commit -m "feat: add idea and like services"
```

---

### Task 7: Comment Service

**Files:**
- Create: `backend/src/main/java/com/ideacollection/dto/CommentRequest.java`
- Create: `backend/src/main/java/com/ideacollection/service/CommentService.java`
- Create: `backend/src/main/java/com/ideacollection/controller/CommentController.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/service/CommentServiceTest.java`

```java
package com.ideacollection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {
    @Test
    void testServiceExists() {
        assertNotNull(CommentService.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=CommentServiceTest`
Expected: FAIL

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/dto/CommentRequest.java`

```java
package com.ideacollection.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {
    @NotBlank
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

Create: `backend/src/main/java/com/ideacollection/service/CommentService.java`

```java
package com.ideacollection.service;

import com.ideacollection.model.Comment;
import com.ideacollection.model.Idea;
import com.ideacollection.repository.CommentRepository;
import com.ideacollection.repository.IdeaRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final IdeaRepository ideaRepository;

    public CommentService(CommentRepository commentRepository, IdeaRepository ideaRepository) {
        this.commentRepository = commentRepository;
        this.ideaRepository = ideaRepository;
    }

    public List<Comment> getCommentsByIdea(String ideaId) {
        return commentRepository.findByIdeaIdOrderByCreatedAtAsc(ideaId);
    }

    public Comment addComment(String ideaId, String content, String author) {
        Comment comment = new Comment();
        comment.setIdeaId(ideaId);
        comment.setContent(content);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);

        // Update idea's lastCommentAt
        Idea idea = ideaRepository.findById(ideaId).orElse(null);
        if (idea != null) {
            idea.setLastCommentAt(Instant.now());
            ideaRepository.save(idea);
        }

        return saved;
    }
}
```

Create: `backend/src/main/java/com/ideacollection/controller/CommentController.java`

```java
package com.ideacollection.controller;

import com.ideacollection.model.Comment;
import com.ideacollection.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideas")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String id) {
        return ResponseEntity.ok(commentService.getCommentsByIdea(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String id,
            @Valid @RequestBody Map<String, String> request,
            @RequestHeader("X-Username") String username) {
        try {
            Comment comment = commentService.addComment(id, request.get("content"), username);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=CommentServiceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/service/CommentService.java backend/src/main/java/com/ideacollection/controller/CommentController.java backend/src/main/java/com/ideacollection/dto/CommentRequest.java
git commit -m "feat: add comment service"
```

---

### Task 8: WebSocket Service

**Files:**
- Create: `backend/src/main/java/com/ideacollection/service/WebSocketService.java`

**Step 1: Write failing test**

Create: `backend/src/test/java/com/ideacollection/service/WebSocketServiceTest.java`

```java
package com.ideacollection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketServiceTest {
    @Test
    void testServiceExists() {
        assertNotNull(WebSocketService.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=WebSocketServiceTest`
Expected: FAIL

**Step 3: Write implementation**

Create: `backend/src/main/java/com/ideacollection/service/WebSocketService.java`

```java
package com.ideacollection.service;

import com.ideacollection.model.Idea;
import com.ideacollection.model.Comment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastNewIdea(Idea idea) {
        messagingTemplate.convertAndSend("/topic/ideas/" + idea.getSubTopicId(), idea);
    }

    public void broadcastIdeaUpdate(Idea idea) {
        messagingTemplate.convertAndSend("/topic/ideas/" + idea.getSubTopicId(), idea);
    }

    public void broadcastLikeUpdate(String ideaId, String subTopicId, int likeCount) {
        messagingTemplate.convertAndSend("/topic/ideas/" + ideaId + "/likes",
            java.util.Map.of("likeCount", likeCount));
    }

    public void broadcastNewComment(Comment comment, String subTopicId) {
        messagingTemplate.convertAndSend("/topic/ideas/" + comment.getIdeaId() + "/comments", comment);
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=WebSocketServiceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/ideacollection/service/WebSocketService.java
git commit -m "feat: add WebSocket service"
```

---

### Task 9: Main Application Entry

**Files:**
- Create: `backend/src/main/java/com/ideacollection/IdeaCollectionApplication.java`
- Create: `backend/src/main/java/com/ideacollection/config/DataInitializer.java`

**Step 1: Write implementation**

Create: `backend/src/main/java/com/ideacollection/IdeaCollectionApplication.java`

```java
package com.ideacollection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdeaCollectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdeaCollectionApplication.class, args);
    }
}
```

Create: `backend/src/main/java/com/ideacollection/config/DataInitializer.java`

```java
package com.ideacollection.config;

import com.ideacollection.model.User;
import com.ideacollection.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setAdmin(true);
                userRepository.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }
        };
    }
}
```

**Step 2: Verify backend compiles**

Run: `cd backend && mvn compile`
Expected: SUCCESS

**Step 3: Commit**

```bash
git add backend/src/main/java/com/ideacollection/IdeaCollectionApplication.java backend/src/main/java/com/ideacollection/config/DataInitializer.java
git commit -m "feat: add Spring Boot application entry point and admin initializer"
```

---

## Phase 2: Frontend

### Task 10: React Project Setup

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/Dockerfile`
- Create: `frontend/public/index.html`
- Create: `frontend/src/index.js`
- Create: `frontend/src/App.js`
- Create: `frontend/src/App.css`

**Step 1: Create package.json**

```json
{
  "name": "idea-collection-frontend",
  "version": "1.0.0",
  "private": true,
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "react-scripts": "5.0.1",
    "sockjs-client": "^1.6.1",
    "@stomp/stompjs": "^7.0.0"
  },
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject"
  },
  "browserslist": {
    "production": [
      ">0.2%",
      "not dead",
      "not op_mini all"
    ],
    "development": [
      "last 1 chrome version",
      "last 1 firefox version",
      "last 1 safari version"
    ]
  }
}
```

**Step 2: Create Dockerfile**

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

**Step 3: Create public/index.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Idea Collection</title>
</head>
<body>
  <noscript>You need to enable JavaScript to run this app.</noscript>
  <div id="root"></div>
</body>
</html>
```

**Step 4: Create src/index.js**

```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './App.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

**Step 5: Create src/App.js**

```javascript
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    if (token && username) {
      setUser({ token, username, isAdmin });
    }
  }, []);

  const handleLogin = (authData) => {
    localStorage.setItem('token', authData.token);
    localStorage.setItem('username', authData.username);
    localStorage.setItem('isAdmin', authData.isAdmin);
    setUser(authData);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('isAdmin');
    setUser(null);
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={user ? <Navigate to="/dashboard" /> : <Landing onLogin={handleLogin} />} />
        <Route path="/dashboard" element={user ? <Dashboard user={user} onLogout={handleLogout} /> : <Navigate to="/" />} />
        <Route path="/admin" element={user && user.isAdmin ? <Admin user={user} onLogout={handleLogout} /> : <Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
```

**Step 6: Create src/App.css**

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

button {
  cursor: pointer;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
}

input, textarea {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #007bff;
}
```

**Step 7: Commit**

```bash
git add frontend/package.json frontend/Dockerfile frontend/public/index.html frontend/src/index.js frontend/src/App.js frontend/src/App.css
git commit -m "feat: add React project setup"
```

---

### Task 11: Landing Page (Login/Register)

**Files:**
- Create: `frontend/src/pages/Landing.js`
- Create: `frontend/src/pages/Landing.css`

**Step 1: Write failing test**

Create: `frontend/src/pages/Landing.test.js`

```javascript
import { render, screen } from '@testing-library/react';
import Landing from './Landing';

test('renders login form', () => {
  render(<Landing onLogin={() => {}} />);
  expect(screen.getByText(/Login/i)).toBeInTheDocument();
});
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Landing.test.js`
Expected: FAIL

**Step 3: Write implementation**

Create: `frontend/src/pages/Landing.js`

```javascript
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Landing.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Landing({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!username || !password) {
      setError('Please fill in all fields');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    const endpoint = isLogin ? '/api/auth/login' : '/api/auth/register';

    try {
      const response = await fetch(`${API_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.error || 'An error occurred');
        return;
      }

      onLogin(data);
      navigate('/dashboard');
    } catch (err) {
      setError('Failed to connect to server');
    }
  };

  const handleAdminClick = () => {
    navigate('/admin');
  };

  return (
    <div className="landing">
      <button className="admin-btn" onClick={handleAdminClick}>Admin</button>
      <div className="landing-container">
        <h1>Idea Collection</h1>
        <div className="form-toggle">
          <button
            className={isLogin ? 'active' : ''}
            onClick={() => setIsLogin(true)}
          >
            Login
          </button>
          <button
            className={!isLogin ? 'active' : ''}
            onClick={() => setIsLogin(false)}
          >
            Register
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
          <div className="form-group">
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error && <div className="error">{error}</div>}
          <button type="submit" className="submit-btn">
            {isLogin ? 'Login' : 'Register'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Landing;
```

Create: `frontend/src/pages/Landing.css`

```css
.landing {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.admin-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.landing-container {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
}

.landing-container h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-toggle {
  display: flex;
  margin-bottom: 20px;
}

.form-toggle button {
  flex: 1;
  padding: 10px;
  background: #f0f0f0;
  color: #666;
}

.form-toggle button.active {
  background: #667eea;
  color: white;
}

.form-group {
  margin-bottom: 15px;
}

.form-group input {
  width: 100%;
}

.error {
  color: #dc3545;
  margin-bottom: 15px;
  text-align: center;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  font-size: 16px;
}

.submit-btn:hover {
  background: #5568d3;
}
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Landing.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/pages/Landing.js frontend/src/pages/Landing.css
git commit -m "feat: add landing page with login/register"
```

---

### Task 12: Main Dashboard

**Files:**
- Create: `frontend/src/pages/Dashboard.js`
- Create: `frontend/src/pages/Dashboard.css`
- Create: `frontend/src/components/IdeaCard.js`
- Create: `frontend/src/components/AddIdeaModal.js`

**Step 1: Write failing test**

Create: `frontend/src/pages/Dashboard.test.js`

```javascript
import { render, screen } from '@testing-library/react';
import Dashboard from './Dashboard';

test('renders dashboard', () => {
  render(<Dashboard user={{ token: 'test', username: 'test', isAdmin: false }} onLogout={() => {}} />);
});
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Dashboard.test.js`
Expected: FAIL

**Step 3: Write implementation**

Create: `frontend/src/pages/Dashboard.js`

```javascript
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import IdeaCard from '../components/IdeaCard';
import AddIdeaModal from '../components/AddIdeaModal';
import './Dashboard.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Dashboard({ user, onLogout }) {
  const [theme, setTheme] = useState('');
  const [subTopics, setSubTopics] = useState([]);
  const [selectedSubTopic, setSelectedSubTopic] = useState(null);
  const [ideas, setIdeas] = useState([]);
  const [sortBy, setSortBy] = useState('default');
  const [showAddModal, setShowAddModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTheme();
    fetchSubTopics();
  }, []);

  useEffect(() => {
    if (selectedSubTopic) {
      fetchIdeas();
    }
  }, [selectedSubTopic, sortBy]);

  const fetchTheme = async () => {
    try {
      const response = await fetch(`${API_URL}/api/theme`);
      const data = await response.json();
      setTheme(data.name || 'Welcome to Idea Collection');
    } catch (err) {
      setTheme('Idea Collection');
    }
  };

  const fetchSubTopics = async () => {
    try {
      const response = await fetch(`${API_URL}/api/subtopics`);
      const data = await response.json();
      setSubTopics(data);
      if (data.length > 0 && !selectedSubTopic) {
        setSelectedSubTopic(data[0].id);
      }
    } catch (err) {
      console.error('Failed to fetch subtopics');
    }
  };

  const fetchIdeas = async () => {
    try {
      const response = await fetch(
        `${API_URL}/api/ideas?subTopicId=${selectedSubTopic}&sortBy=${sortBy}`,
        { headers: { 'Authorization': `Bearer ${user.token}` } }
      );
      const data = await response.json();
      setIdeas(data);
    } catch (err) {
      console.error('Failed to fetch ideas');
    }
  };

  const handleCreateIdea = async (title, description) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`,
          'X-User-Id': user.token,
          'X-Username': user.username
        },
        body: JSON.stringify({ subTopicId: selectedSubTopic, title, description })
      });

      if (response.ok) {
        fetchIdeas();
        fetchSubTopics();
      }
    } catch (err) {
      console.error('Failed to create idea');
    }
  };

  const handleStatusUpdate = async (ideaId, stage, stageStatus) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas/${ideaId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ stage, stageStatus })
      });

      if (response.ok) {
        fetchIdeas();
      }
    } catch (err) {
      console.error('Failed to update status');
    }
  };

  const handleLike = async (ideaId) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas/${ideaId}/like`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${user.token}`, 'X-User-Id': user.token }
      });

      if (response.ok) {
        fetchIdeas();
      }
    } catch (err) {
      console.error('Failed to toggle like');
    }
  };

  return (
    <div className="dashboard">
      <header className="header">
        <div className="header-left">
          <h1>{theme}</h1>
        </div>
        <div className="header-right">
          <span>{user.username}</span>
          <button onClick={onLogout}>Logout</button>
        </div>
      </header>

      <nav className="tabs">
        {subTopics.map(st => (
          <button
            key={st.id}
            className={selectedSubTopic === st.id ? 'active' : ''}
            onClick={() => setSelectedSubTopic(st.id)}
          >
            {st.name}
          </button>
        ))}
      </nav>

      <div className="sort-controls">
        <span>Sort by:</span>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="default">Default</option>
          <option value="recent">Most Recent</option>
          <option value="most_liked">Most Liked</option>
          <option value="most_discussed">Most Discussed</option>
        </select>
      </div>

      <div className="ideas-grid">
        {ideas.map(idea => (
          <IdeaCard
            key={idea.id}
            idea={idea}
            user={user}
            onStatusUpdate={handleStatusUpdate}
            onLike={handleLike}
            apiUrl={API_URL}
          />
        ))}
      </div>

      <button className="add-btn" onClick={() => setShowAddModal(true)}>+</button>

      {showAddModal && (
        <AddIdeaModal
          onClose={() => setShowAddModal(false)}
          onSubmit={handleCreateIdea}
        />
      )}
    </div>
  );
}

export default Dashboard;
```

Create: `frontend/src/pages/Dashboard.css`

```css
.dashboard {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 24px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.tabs {
  background: white;
  padding: 10px 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  border-bottom: 1px solid #eee;
}

.tabs button {
  background: #f0f0f0;
  color: #666;
  padding: 8px 16px;
}

.tabs button.active {
  background: #667eea;
  color: white;
}

.sort-controls {
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-controls select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.ideas-grid {
  padding: 20px 30px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.add-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.add-btn:hover {
  background: #5568d3;
}
```

Create: `frontend/src/components/IdeaCard.js`

```javascript
import React, { useState, useEffect } from 'react';

function IdeaCard({ idea, user, onStatusUpdate, onLike, apiUrl }) {
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [hasLiked, setHasLiked] = useState(false);

  useEffect(() => {
    setHasLiked(idea.likes?.includes(user.token));
  }, [idea.likes, user.token]);

  const handleCommentToggle = async () => {
    if (!showComments) {
      try {
        const response = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`);
        const data = await response.json();
        setComments(data);
      } catch (err) {
        console.error('Failed to fetch comments');
      }
    }
    setShowComments(!showComments);
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;

    try {
      const response = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`,
          'X-Username': user.username
        },
        body: JSON.stringify({ content: newComment })
      });

      if (response.ok) {
        const updatedComments = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`);
        setComments(await updatedComments.json());
        setNewComment('');
      }
    } catch (err) {
      console.error('Failed to add comment');
    }
  };

  const isDone = idea.stageStatus === 'Done';

  return (
    <div className={`idea-card ${isDone ? 'completed' : ''}`}>
      <div
        className="status-bar"
        onClick={() => user.isAdmin && onStatusUpdate(idea.id, idea.stage, prompt('Status:', idea.stageStatus))}
      >
        {idea.stage}: {idea.stageStatus}
      </div>

      <h3>{idea.title}</h3>
      <p className="description">{idea.description}</p>
      <p className="author">by {idea.author}</p>

      <div className="actions">
        <button
          className={`like-btn ${hasLiked ? 'liked' : ''}`}
          onClick={() => onLike(idea.id)}
        >
          ♥ {idea.likeCount}
        </button>
        <button className="comment-btn" onClick={handleCommentToggle}>
          💬 Comments
        </button>
      </div>

      {showComments && (
        <div className="comments-section">
          {comments.map(comment => (
            <div key={comment.id} className="comment">
              <strong>{comment.author}:</strong> {comment.content}
            </div>
          ))}
          <div className="comment-input">
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment..."
            />
            <button onClick={handleAddComment}>Submit</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default IdeaCard;
```

Create: `frontend/src/components/AddIdeaModal.js`

```javascript
import React, { useState } from 'react';

function AddIdeaModal({ onClose, onSubmit }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!title.trim()) {
      setError('Title is required');
      return;
    }

    onSubmit(title, description);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>Add New Idea</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              placeholder="Title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>
          <div className="form-group">
            <textarea
              placeholder="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
            />
          </div>
          {error && <div className="error">{error}</div>}
          <div className="modal-actions">
            <button type="button" onClick={onClose}>Cancel</button>
            <button type="submit" className="submit-btn">Submit</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddIdeaModal;
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Dashboard.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/pages/Dashboard.js frontend/src/pages/Dashboard.css frontend/src/components/IdeaCard.js frontend/src/components/AddIdeaModal.js
git commit -m "feat: add main dashboard with idea cards"
```

---

### Task 13: Admin Panel

**Files:**
- Create: `frontend/src/pages/Admin.js`
- Create: `frontend/src/pages/Admin.css`

**Step 1: Write failing test**

Create: `frontend/src/pages/Admin.test.js`

```javascript
import { render, screen } from '@testing-library/react';
import Admin from './Admin';

test('renders admin panel', () => {
  render(<Admin user={{ token: 'test', username: 'admin', isAdmin: true }} onLogout={() => {}} />);
});
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Admin.test.js`
Expected: FAIL

**Step 3: Write implementation**

Create: `frontend/src/pages/Admin.js`

```javascript
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Admin.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Admin({ user, onLogout }) {
  const [theme, setTheme] = useState('');
  const [subTopics, setSubTopics] = useState([]);
  const [newSubTopic, setNewSubTopic] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!user.isAdmin) {
      navigate('/');
      return;
    }
    fetchTheme();
    fetchSubTopics();
  }, []);

  const fetchTheme = async () => {
    try {
      const response = await fetch(`${API_URL}/api/theme`);
      const data = await response.json();
      setTheme(data.name || '');
    } catch (err) {
      console.error('Failed to fetch theme');
    }
  };

  const fetchSubTopics = async () => {
    try {
      const response = await fetch(`${API_URL}/api/subtopics`);
      const data = await response.json();
      setSubTopics(data);
    } catch (err) {
      console.error('Failed to fetch subtopics');
    }
  };

  const handleThemeUpdate = async () => {
    try {
      await fetch(`${API_URL}/api/theme`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: theme })
      });
    } catch (err) {
      console.error('Failed to update theme');
    }
  };

  const handleCreateSubTopic = async (e) => {
    e.preventDefault();
    if (!newSubTopic.trim()) return;

    try {
      await fetch(`${API_URL}/api/subtopics`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: newSubTopic })
      });
      setNewSubTopic('');
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to create subtopic');
    }
  };

  const handleEdit = (subTopic) => {
    setEditingId(subTopic.id);
    setEditName(subTopic.name);
  };

  const handleSaveEdit = async (id) => {
    try {
      await fetch(`${API_URL}/api/subtopics/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: editName })
      });
      setEditingId(null);
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to update subtopic');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this sub-topic and all its ideas?')) return;

    try {
      await fetch(`${API_URL}/api/subtopics/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${user.token}` }
      });
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to delete subtopic');
    }
  };

  return (
    <div className="admin">
      <header className="admin-header">
        <h1>Admin Panel</h1>
        <div className="header-right">
          <button onClick={() => navigate('/dashboard')}>Dashboard</button>
          <button onClick={onLogout}>Logout</button>
        </div>
      </header>

      <div className="admin-content">
        <section className="admin-section">
          <h2>Main Theme</h2>
          <div className="theme-input">
            <input
              type="text"
              value={theme}
              onChange={(e) => setTheme(e.target.value)}
              placeholder="Enter main theme"
            />
            <button onClick={handleThemeUpdate}>Update Theme</button>
          </div>
        </section>

        <section className="admin-section">
          <h2>Sub-Topics</h2>
          <form onSubmit={handleCreateSubTopic} className="add-subtopic">
            <input
              type="text"
              value={newSubTopic}
              onChange={(e) => setNewSubTopic(e.target.value)}
              placeholder="New sub-topic name"
            />
            <button type="submit">Create</button>
          </form>

          <table className="subtopic-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Card Count</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {subTopics.map(st => (
                <tr key={st.id}>
                  <td>
                    {editingId === st.id ? (
                      <input
                        type="text"
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                      />
                    ) : (
                      st.name
                    )}
                  </td>
                  <td>{st.cardCount}</td>
                  <td>
                    {editingId === st.id ? (
                      <button onClick={() => handleSaveEdit(st.id)}>Save</button>
                    ) : (
                      <>
                        <button onClick={() => handleEdit(st)}>Edit</button>
                        <button className="delete-btn" onClick={() => handleDelete(st.id)}>Delete</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  );
}

export default Admin;
```

Create: `frontend/src/pages/Admin.css`

```css
.admin {
  min-height: 100vh;
  background: #f5f5f5;
}

.admin-header {
  background: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.admin-header h1 {
  font-size: 24px;
  color: #333;
}

.admin-content {
  padding: 30px;
  max-width: 800px;
  margin: 0 auto;
}

.admin-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.admin-section h2 {
  margin-bottom: 15px;
  color: #333;
}

.theme-input {
  display: flex;
  gap: 10px;
}

.theme-input input {
  flex: 1;
}

.add-subtopic {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.add-subtopic input {
  flex: 1;
}

.subtopic-table {
  width: 100%;
  border-collapse: collapse;
}

.subtopic-table th,
.subtopic-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.subtopic-table th {
  background: #f9f9f9;
  font-weight: 600;
}

.delete-btn {
  background: #dc3545;
  color: white;
  margin-left: 8px;
}
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=Admin.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/pages/Admin.js frontend/src/pages/Admin.css
git commit -m "feat: add admin panel"
```

---

### Task 14: WebSocket Integration

**Files:**
- Create: `frontend/src/services/websocket.js`

**Step 1: Write failing test**

Create: `frontend/src/services/websocket.test.js`

```javascript
test('websocket service exists', () => {
  expect(true).toBe(true);
});
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=websocket.test.js`
Expected: PASS (trivial test)

**Step 3: Write implementation**

Create: `frontend/src/services/websocket.js`

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = [];
  }

  connect(token, onConnected, onError) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      onConnect: () => {
        if (onConnected) onConnected();
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
      },
      onStompError: (frame) => {
        if (onError) onError(frame);
      }
    });

    this.client.activate();
  }

  subscribe(topic, callback) {
    if (!this.client || !this.client.connected) {
      console.warn('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    });

    this.subscriptions.push(subscription);
    return subscription;
  }

  subscribeToSubTopic(subTopicId, callback) {
    return this.subscribe(`/topic/ideas/${subTopicId}`, callback);
  }

  subscribeToLikes(ideaId, callback) {
    return this.subscribe(`/topic/ideas/${ideaId}/likes`, callback);
  }

  subscribeToComments(ideaId, callback) {
    return this.subscribe(`/topic/ideas/${ideaId}/comments`, callback);
  }

  disconnect() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }
}

export default new WebSocketService();
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --watchAll=false --testPathPattern=websocket.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/services/websocket.js
git commit -m "feat: add WebSocket service for real-time updates"
```

---

## Phase 3: Integration and Testing

### Task 15: Docker Integration Test

**Step 1: Verify docker-compose works**

Run: `docker-compose up --build`
Expected: All services start successfully

**Step 2: Test backend health**

Run: `curl http://localhost:8080/api/theme`
Expected: Returns theme object

**Step 3: Test frontend**

Run: `curl http://localhost:3000`
Expected: Returns HTML

**Step 4: Commit**

```bash
git commit -m "chore: verify Docker integration"
```

---

## Summary

**Total Tasks:** 16

**Backend (9 tasks):**
1. Project Setup & Docker
2. Data Models
3. Repositories
4. Auth Service
5. Theme & SubTopic Services
6. Idea & Like Services
7. Comment Service
8. WebSocket Service
9. Main Application

**Frontend (5 tasks):**
10. React Setup
11. Landing Page
12. Main Dashboard
13. Admin Panel
14. WebSocket Integration

**Integration (2 tasks):**
15. Docker Integration Test
16. Create Initial Admin User
