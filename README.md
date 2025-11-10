# Notes Application

REST API for managing personal notes using Spring Boot and MongoDB.

## 🚀 Technologies

- **Java 21**
- **Spring Boot 3.5.7**
- **MongoDB 7.0**
- **Maven**
- **Docker & Docker Compose**
- **Lombok**
- **Swagger/OpenAPI 3.0** - Interactive API documentation

---

## ✨ Features

- ✅ Create notes with Title, Text, Created Date, and Tags
- ✅ Update and delete notes
- ✅ List notes with pagination
- ✅ Filter by tags (BUSINESS, PERSONAL, IMPORTANT)
- ✅ Sort by date (newest first)
- ✅ Word statistics for note text
- ✅ Validation (Title and Text are required)
- ✅ Interactive API documentation (Swagger UI)

---

## 📚 API Documentation

### Swagger UI (Recommended for Testing)

After starting the application, open in your browser:
```
http://localhost:8080/swagger-ui.html
```

**Swagger UI Features:**
- 🔍 Browse all API endpoints
- ▶️ Test requests directly from browser
- 📖 Detailed documentation for each endpoint
- 📋 Request/Response examples
- ✅ Automatic validation
- 📥 Export OpenAPI specification

### OpenAPI Specification

JSON specification available at:
```
http://localhost:8080/v3/api-docs
```

YAML format:
```
http://localhost:8080/v3/api-docs.yaml
```

---

## 🐳 Running with Docker Compose (Recommended)

### Option 1: Quick Start
```bash
# Start everything at once
docker-compose up --build

# Or in detached mode
docker-compose up -d --build
```

### Option 2: Use Script
```bash
chmod +x quick-start.sh
./quick-start.sh
```

### Application will be available at:
- **API:** http://localhost:8080/api/v1/notes
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **MongoDB:** localhost:27017

### Stop:
```bash
# Stop containers
docker-compose down

# Stop and remove data
docker-compose down -v
```

---

## 💻 Running Locally (Without Docker)

### Requirements
- Java 21
- Maven 3.9+
- MongoDB 7.0+ (running on localhost:27017)

### Steps
```bash
# 1. Start MongoDB (if no Docker Compose)
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# 2. Install dependencies
mvn clean install

# 3. Start application
mvn spring-boot:run
```

---

## 🔌 REST API Endpoints

### 📝 Notes Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/notes` | Create a note |
| `GET` | `/api/v1/notes` | List notes (with pagination) |
| `GET` | `/api/v1/notes/{id}` | Get note by ID |
| `PUT` | `/api/v1/notes/{id}` | Update note |
| `DELETE` | `/api/v1/notes/{id}` | Delete note |
| `GET` | `/api/v1/notes/{id}/stats` | Word statistics |

### 📖 Detailed Documentation

For complete documentation with examples, see **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🧪 Usage Examples

### Via Swagger UI (Easiest)

1. Open http://localhost:8080/swagger-ui.html
2. Select an endpoint (e.g., `POST /api/v1/notes`)
3. Click "Try it out"
4. Fill in the request body
5. Click "Execute"

### Via curl

#### Create a Note
```bash
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Shopping List",
    "text": "Buy milk eggs bread butter",
    "tags": ["PERSONAL"]
  }'
```

#### Get List
```bash
curl "http://localhost:8080/api/v1/notes?page=0&size=10"
```

#### Filter by Tag
```bash
curl "http://localhost:8080/api/v1/notes?tag=BUSINESS&page=0&size=10"
```

#### Get Note
```bash
curl http://localhost:8080/api/v1/notes/{id}
```

#### Word Statistics
```bash
curl http://localhost:8080/api/v1/notes/{id}/stats
```

Example response:
```json
{
  "milk": 2,
  "eggs": 1,
  "bread": 1,
  "butter": 1
}
```

#### Update Note
```bash
curl -X PUT http://localhost:8080/api/v1/notes/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Shopping List",
    "text": "Buy organic milk eggs and whole grain bread",
    "tags": ["PERSONAL", "IMPORTANT"]
  }'
```

#### Delete Note
```bash
curl -X DELETE http://localhost:8080/api/v1/notes/{id}
```

### Via Postman

Import collection: `Notes-API.postman_collection.json`

---

## 🧪 Running Tests
```bash
# All tests (Unit + Integration)
mvn test

# Only Unit tests
mvn test -Dtest=*Test

# Only Integration tests (Docker required)
mvn test -Dtest=*IntegrationTest
```

### Test Coverage:
- **Unit Tests:** 13 tests (NoteServiceTest)
-   **CRUD operations** (8 tests): create, update, delete, read
-   **Word statistics** (1 parameterized test × 16 scenarios):
      - Latin and Cyrillic scripts
      - Case insensitivity
      - Edge cases
- **Error handling** (4 tests): NoteNotFoundException in various operations
- **Controller Tests:** 10 tests (NoteControllerTest)
- **Integration Tests:** 21 tests (NoteIntegrationTest)
- **Total:** 37 tests

---

## 📁 Project Structure
```
src/main/java/com/example/noteapplication/
├── NoteApplication.java          # Main application class
├── controller/
│   └── NoteController.java       # REST API endpoints
├── service/
│   ├── NoteService.java          # Service interface
│   └── NoteServiceImpl.java      # Business logic
├── repository/
│   └── NoteRepository.java       # MongoDB repository
├── model/
│   ├── Note.java                 # Entity
│   └── Tag.java                  # Enum
├── dto/
│   ├── NoteCreateRequest.java    # Request DTOs
│   ├── NoteUpdateRequest.java
│   ├── NoteListResponse.java     # Response DTOs
│   └── NoteDetailResponse.java
├── exception/
│   ├── NoteNotFoundException.java
│   └── GlobalExceptionHandler.java
├── config/
│   ├── MongoConfig.java
│   └── OpenApiConfig.java        # Swagger configuration
└── mapper/
    └── NoteMapper.java

src/test/java/com/example/noteapplication/
├── NoteIntegrationTest.java      # E2E tests with Testcontainers
├── service/
│   └── NoteServiceTest.java      # Unit tests
└── controller/
│   └── NoteControllerTest.java   # Controller tests
└── NoteIntegrationTest.java   # Controller tests
   ```

---

## ⚙️ Configuration

### application.yml
```yaml
spring:
  application:
    name: note-application
  data:
    mongodb:
      host: localhost
      port: 27017
      database: notesdb

server:
  port: 8080
```

### Docker Profile (application-docker.yml)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://mongodb:27017/notesdb
```

---

## 🎯 Validation

### Required Fields:
- ✅ `title` - cannot be empty
- ✅ `text` - cannot be empty

### Optional Fields:
- ⚪ `tags` - can be empty or null

### Allowed Tags:
- `BUSINESS`
- `PERSONAL`
- `IMPORTANT`

### Validation Error Examples:
```json
{
  "status": 400,
  "errors": {
    "title": "Title is required",
    "text": "Text is required"
  },
  "timestamp": "2024-11-10T00:00:00"
}
```

---

## 🚨 Error Handling

| Status | Description |
|--------|-------------|
| `200 OK` | Successful request |
| `201 Created` | Note created |
| `204 No Content` | Note deleted |
| `400 Bad Request` | Validation error |
| `404 Not Found` | Note not found |
| `500 Internal Server Error` | Internal error |

---

## 📊 Word Statistics

The `/api/v1/notes/{id}/stats` endpoint returns word frequency:

**Features:**
- Case-insensitive (Java = JAVA = java)
- Cyrillic support (Україна, Київ)
- Sorted by frequency (most frequent first)
- Filters special characters and numbers
- Only letters are counted as words

**Example:**

Input text: `"note is just a note"`

Output:
```json
{
  "note": 2,
  "is": 1,
  "just": 1,
  "a": 1
}
```

---

## 🔧 Troubleshooting

### Issue: MongoDB connection refused

**Solution:**
```bash
# Check if MongoDB is running
docker ps | grep mongo

# Or start MongoDB
docker-compose up -d mongodb
```

### Issue: Port 8080 already in use

**Solution:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Issue: Tests fail (Testcontainers)

**Solution:**
```bash
# Check if Docker is running
docker --version

# Start Docker Desktop
```

---

## 📝 TODO / Possible Improvements

- [ ] Add authentication (Spring Security + JWT)
- [ ] Add full-text search (MongoDB full-text search)
- [ ] Export notes to PDF/Markdown
- [ ] Add note categories
- [ ] Add file attachments
- [ ] Rate limiting for API
- [ ] Caching (Redis)
- [ ] Metrics and monitoring (Actuator, Prometheus)

---

## 👤 Author

Test Task - Dmytro Oliinyk

---

## 📄 License

This project was created for a test assignment.