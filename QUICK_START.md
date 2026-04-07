# Quick Start Guide - Currency Exchange Application

##  Installation & Setup

### 1. Prerequisites
```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.6+)
mvn -version
```

### 2. Build the Project
```bash
# Navigate to project directory
cd /home/karumakarumakaruma/IdeaProjects/currencyexchange

# Build with Maven
mvn clean install -DskipTests

# Or to run tests as well
mvn clean install
```

### 3. Run the Application
```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using Java directly (after build)
java -jar target/currency-exchange-1.0.0.jar
```

The application will start on: **http://localhost:8080**

---

##  Accessing the Application

### Swagger UI (Interactive API Documentation)
```
http://localhost:8080/swagger-ui.html
```
- View all endpoints
- Try out API calls directly
- See response schemas

### OpenAPI JSON Spec
```
http://localhost:8080/v3/api-docs
```

### H2 Console (In-Memory Database)
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:currencyexchangedb`
- Username: `sa`
- Password: (leave empty)

---

##  Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CurrencyServiceTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

### View Test Results
Tests run from: `src/test/java/com/example/currencyexchange/`

---

##  Using Postman

### Import Collection
1. Open Postman
2. Click **Import**
3. Select **File** tab
4. Choose `Currency_Exchange_API.postman_collection.json`
5. Click **Import**

### Set Base URL Variable (Optional)
1. In Postman, go to **Environments**
2. Create new environment: "Currency Exchange"
3. Add variable:
   - Key: `baseUrl`
   - Value: `http://localhost:8080`
4. Select the environment before making requests

### Test Sample Requests

**Create a Currency:**
```
POST http://localhost:8080/api/currencies
Content-Type: application/json

{
  "code": "USD",
  "name": "United States Dollar",
  "symbol": "$",
  "region": "North America",
  "isActive": true
}
```

**Create an Exchange Rate:**
```
POST http://localhost:8080/api/exchange-rates
Content-Type: application/json

{
  "fromCurrencyCode": "USD",
  "toCurrencyCode": "EUR",
  "rate": 0.92
}
```

**Convert Currency:**
```
POST http://localhost:8080/api/convert
Content-Type: application/json

{
  "fromCurrencyCode": "USD",
  "toCurrencyCode": "EUR",
  "amount": 100
}
```

**Search with Pagination:**
```
GET http://localhost:8080/api/currencies/search?keyword=dollar&page=0&size=10
```

---

## 📊 Project Structure

```
src/
├── main/
│   ├── java/com/example/currencyexchange/
│   │   ├── CurrencyexchangeApplication.java      # Entry point
│   │   ├── aspect/
│   │   │   └── LoggingAspect.java               # AspectJ logging
│   │   ├── config/
│   │   │   ├── JpaConfig.java
│   │   │   ├── RestTemplateConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/                          # REST endpoints
│   │   │   ├── CurrencyController.java
│   │   │   ├── ExchangeRateController.java
│   │   │   └── ConversionController.java
│   │   ├── dto/                                 # Data Transfer Objects
│   │   ├── exception/                           # Exception handling
│   │   ├── model/                               # JPA entities
│   │   ├── repository/                          # Data access
│   │   └── service/                             # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/currencyexchange/       # Unit tests
```

---

## Key Endpoints (Quick Reference)

### Currencies
```
GET    /api/currencies                          # List (paginated)
GET    /api/currencies/{id}                     # Get by UUID
GET    /api/currencies/code/{code}              # Get by code
GET    /api/currencies/search?keyword=...       # Search
POST   /api/currencies                          # Create
PUT    /api/currencies/{id}                     # Update
PATCH  /api/currencies/{uuid}?isActive=true     # Toggle status
DELETE /api/currencies/{id}                     # Delete
```

### Exchange Rates
```
GET    /api/exchange-rates                      # List (paginated)
GET    /api/exchange-rates/{id}                 # Get by UUID
GET    /api/exchange-rates/pair?from=USD&to=EUR # Get pair
GET    /api/exchange-rates/details              # With JOIN query
GET    /api/exchange-rates/search?keyword=...   # Search
POST   /api/exchange-rates                      # Create
PUT    /api/exchange-rates/{id}                 # Update
DELETE /api/exchange-rates/{id}                 # Delete
```

### Conversion
```
POST   /api/convert                             # Convert (JSON body)
GET    /api/convert?from=USD&to=EUR&amount=100  # Quick convert
```

---

##  Troubleshooting

### Port Already in Use
```bash
# Kill process on port 8080
lsof -i :8080
kill -9 <PID>

# Or use different port
java -jar target/currency-exchange-1.0.0.jar --server.port=8081
```

### Maven Build Issues
```bash
# Clear cache
mvn clean

# Update dependencies
mvn dependency:resolve

# Check for issues
mvn validate
```

### Application Won't Start
1. Check logs for errors
2. Ensure Java 17+ is installed
3. Verify port 8080 is available
4. Check application.properties is in classpath

### H2 Console Connection Error
1. Ensure application is running
2. Use exact JDBC URL: `jdbc:h2:mem:currencyexchangedb`
3. Username: `sa` (password is empty)
4. Click **Connect**

---

##  Documentation Files

| File | Purpose |
|---|---|
| `README.md` | Complete project documentation |
| `PROJECT_COMPLETION_SUMMARY.md` | Implementation details of all requirements |
| `GITHUB_SETUP.md` | Step-by-step GitHub upload guide |
| `QUICK_START.md` | This file - quick reference |

---

##  Next Steps

### 1. Explore the API
- Open Swagger UI: http://localhost:8080/swagger-ui.html
- Try out different endpoints
- Review request/response schemas

### 2. Check the Logs
- Look at the console output
- Observe AspectJ logging for requests/responses
- Check service method timings

### 3. Database Exploration
- Open H2 Console: http://localhost:8080/h2-console
- Run SQL queries on the tables
- Check the schema and data

### 4. Review the Code
- Open in your IDE
- Understand the layer separation
- Review AspectJ implementation
- Check test cases

### 5. Upload to GitHub
- Follow instructions in `GITHUB_SETUP.md`
- Push to your GitHub account
- Share the repository link

---

## Tips & Tricks

### Use curl for Quick Testing
```bash
# Get all currencies
curl http://localhost:8080/api/currencies

# Create currency
curl -X POST http://localhost:8080/api/currencies \
  -H "Content-Type: application/json" \
  -d '{"code":"USD","name":"Dollar","symbol":"$","region":"Americas","isActive":true}'

# Search
curl "http://localhost:8080/api/currencies/search?keyword=dollar"
```

### Check Application Properties
All configuration in: `src/main/resources/application.properties`
- Database: H2 in-memory
- Logging levels
- JPA settings
- External API URL

### View AspectJ Logs
Add to application.properties:
```properties
logging.level.com.example.currencyexchange.aspect=DEBUG
```

### Modify Pagination Defaults
In controller methods, change:
```java
@PageableDefault(size = 10, sort = "code")
```

---

##  Common Issues & Solutions

| Issue | Solution |
|---|---|
| `Port 8080 already in use` | Use different port or kill existing process |
| `Java version mismatch` | Ensure Java 17+ is installed and JAVA_HOME is set |
| `Maven not found` | Add Maven to PATH or use `mvn` wrapper scripts |
| `Tests failing` | Run `mvn clean install` to rebuild everything |
| `H2 console won't connect` | Check JDBC URL spelling and app is running |

---

##  Logging Format

All logs appear in console with format:
```
[REQUEST ] ClassName.methodName() | args: [...]
[RESPONSE] ClassName.methodName() | duration: XXms | result: ...
[SERVICE ENTER] ClassName.methodName() | args: [...]
[SERVICE EXIT] ClassName.methodName() | returned: ...
[SERVICE ERROR] ClassName.methodName() | exception: ExceptionType - message
```

---

##  Learning Resources

This project demonstrates:
- ✅ Spring Boot REST API development
- ✅ JPA/Hibernate database operations
- ✅ AspectJ AOP logging
- ✅ Unit testing with JUnit 5, Mockito
- ✅ Pagination with Spring Data
- ✅ External API integration
- ✅ Exception handling
- ✅ Input validation
- ✅ Layered architecture

Perfect for learning enterprise Java development patterns!

---

**Last Updated:** April 6, 2026  
**Version:** 1.0.0  
**Status:** ✅ Production Ready

