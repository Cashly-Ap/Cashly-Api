# 🧪 Testing Guide - Lexand API

## 📋 Executive Summary

**New**: We now use **real MySQL in Docker** for integration and E2E tests.

**Quick command**:
```bash
./run-tests.sh                        # All tests
./run-tests.sh DifferentialTest       # Specific tests
```

---

## 🎯 Testing Strategy with Real MySQL

### Why Real MySQL in Tests?

**Analogy**: It's like practicing soccer on a real field instead of a paper field. If you play the real match on grass, practice on grass too!

✅ **Advantages**:
- Tests use the **same database as production** (MySQL 8.0)
- Detects MySQL-specific issues that H2 doesn't catch
- Flyway migrations run exactly as in production
- More reliable and realistic tests

### Test Architecture

```
┌─────────────────────────────────────────┐
│  ./run-tests.sh DifferentialTest        │
└──────────────────┬────────────────────┘
                   │
                   ▼
    ┌──────────────────────────────┐
    │  Start mysql-test (tmpfs)     │  ← In RAM, super fast
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Wait for healthcheck (ready) │  ← Ensures MySQL is ready
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Run Flyway migrations        │  ← Same flow as production
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Execute tests (Maven)        │
    │  - Each test cleans DB        │
    │  - Uses fresh data            │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Cleanup: Delete container    │
    │  and tmpfs volume             │
    └──────────────────────────────┘
```

---

## 🚀 Running Tests

### Option 1: Automated Script (Recommended)

```bash
# All tests
./run-tests.sh

# Specific tests
./run-tests.sh DifferentialControllerIntegrationTest
./run-tests.sh "Differential*"
./run-tests.sh "**/*ControllerIntegrationTest"

# With extra Maven arguments
./run-tests.sh MyTest -X  # Debug mode
```

**What the script does**:
1. ✅ Starts MySQL test in Docker (tmpfs)
2. ✅ Waits for MySQL to be fully ready
3. ✅ Runs tests with Maven
4. ✅ Cleans up EVERYTHING automatically on completion (success or failure)

### Option 2: Manual (Development)

```bash
# Start only MySQL test
docker-compose --profile test up -d mysql-test

# Wait until ready
until docker-compose --profile test exec -T mysql-test mysqladmin ping -h localhost -u root -ptestroot --silent; do
    echo "Waiting for MySQL..."
    sleep 2
done

# Run tests
docker-compose --profile test run --rm app-test ./mvnw test

# Cleanup
docker-compose --profile test down -v
```

---

## 🗂️ Test Types

### 1. Unit Tests
**Purpose**: Test individual classes in isolation

**Naming**: `*UnitTest.java`

**Example**:
```java
@DisplayName("DifferentialPercentage Unit Tests")
class DifferentialPercentageUnitTest {
    
    @Test
    @DisplayName("should_CreatePercentage_When_ValidValueProvided")
    void should_CreatePercentage_When_ValidValueProvided() {
        DifferentialPercentage percentage = new DifferentialPercentage(10.5);
        
        assertThat(percentage.value()).isEqualTo(10.5);
    }
    
    @Test
    @DisplayName("should_ThrowException_When_NegativeValue")
    void should_ThrowException_When_NegativeValue() {
        assertThatThrownBy(() -> new DifferentialPercentage(-5.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be negative");
    }
}
```

**Characteristics**:
- ⚡ Fast (milliseconds)
- 🚫 No Spring context
- 🚫 No database
- 🎯 Focused on business logic

---

### 2. Integration Tests
**Purpose**: Test component interaction with real infrastructure

**Naming**: `*IntegrationTest.java`

**Example**:
```java
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Differential Controller Integration Tests")
class DifferentialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void setUp() {
        cleanDatabase();
        registerUserAndGetToken();
    }
    
    private void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE differentials");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
    
    @Test
    @DisplayName("should_CreateDifferential_When_ValidRequest")
    void should_CreateDifferential_When_ValidRequest() throws Exception {
        String requestJson = """
            {
                "name": "VIP Discount",
                "percentage": 15.0,
                "active": true
            }
            """;
        
        mockMvc.perform(post("/api/differentials")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("VIP Discount"))
            .andExpect(jsonPath("$.percentage").value(15.0))
            .andExpect(jsonPath("$.active").value(true));
    }
}
```

**Characteristics**:
- 🐢 Slower (seconds)
- ✅ Spring context loaded
- ✅ Real MySQL database
- ✅ Flyway migrations executed
- ✅ REST endpoints tested
- ✅ JWT authentication required

---

## 🗄️ Database Management in Tests

### Strategy: TRUNCATE Before Each Test

**Why?**
- ✅ Ensures clean state
- ✅ Prevents FK constraint violations
- ✅ Faster than `DELETE` (no transaction log)
- ✅ Resets AUTO_INCREMENT

**Pattern**:
```java
@BeforeEach
void setUp() {
    cleanDatabase();
    // ... test setup
}

private void cleanDatabase() {
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
    jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens");
    jdbcTemplate.execute("TRUNCATE TABLE users");
    jdbcTemplate.execute("TRUNCATE TABLE differentials");
    jdbcTemplate.execute("TRUNCATE TABLE warehouse_stock");
    jdbcTemplate.execute("TRUNCATE TABLE warehouses");
    jdbcTemplate.execute("TRUNCATE TABLE customer_addresses");
    jdbcTemplate.execute("TRUNCATE TABLE customer_phones");
    jdbcTemplate.execute("TRUNCATE TABLE customer_emails");
    jdbcTemplate.execute("TRUNCATE TABLE customers");
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
}
```

**Important**: Always truncate in correct order (child tables before parent tables), or disable FK checks temporarily.

---

## 🔐 Authentication in Controller Tests

All controller endpoints require JWT authentication. Pattern:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        cleanDatabase();
        registerUserAndGetToken();
    }
    
    private void registerUserAndGetToken() {
        try {
            // Register user
            String registerJson = """
                {
                    "email": "test@example.com",
                    "userName": "Test User",
                    "password": "Test1234!"
                }
                """;
            
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerJson))
                .andExpect(status().isCreated());
            
            // Login to get token
            String loginJson = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!"
                }
                """;
            
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
            
            String responseBody = result.getResponse().getContentAsString();
            JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
            this.jwtToken = jsonNode.get("accessToken").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup authentication", e);
        }
    }
    
    @Test
    void should_AccessProtectedEndpoint_When_Authenticated() throws Exception {
        mockMvc.perform(get("/api/differentials")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk());
    }
}
```

---

## 📊 H2 vs MySQL Comparison

| Aspect | H2 (Before) | MySQL (Now) |
|--------|-------------|-------------|
| **Database** | In-memory Java | Real MySQL 8.0 in Docker |
| **Flyway** | Disabled | ✅ Enabled |
| **Speed** | ⚡ Very fast | 🐢 Slower (but tmpfs helps) |
| **Realism** | ❌ Low | ✅ High (same as production) |
| **SQL Syntax** | H2-specific | MySQL-specific |
| **Constraints** | Partial | ✅ Full MySQL constraints |
| **Indexes** | Different behavior | ✅ Real MySQL indexes |
| **Transactions** | Different isolation | ✅ MySQL InnoDB isolation |
| **Charset** | Default UTF-8 | ✅ `utf8mb4_unicode_ci` |

**Why We Switched**:
- Production uses MySQL, tests should too
- Prevents bugs caused by SQL dialect differences
- Validates migrations work correctly
- More confidence in production behavior

---

## 🛠️ Utility Scripts

### `run-tests.sh`
**Purpose**: Automated test execution with MySQL

**Usage**:
```bash
./run-tests.sh                           # All tests
./run-tests.sh DifferentialTest          # Specific test
./run-tests.sh "**/*IntegrationTest"     # Pattern matching
```

**What it does**:
1. Starts `mysql-test` container with tmpfs
2. Waits for healthcheck (MySQL ready)
3. Runs Maven tests
4. Cleans up containers and volumes

**Features**:
- ✅ Automatic cleanup on success/failure
- ✅ Colored output (green/red)
- ✅ Healthcheck validation
- ✅ Supports test filters

---

### `clean-test-db.sh`
**Purpose**: Manual database reset

**Usage**:
```bash
./clean-test-db.sh
```

**What it does**:
1. Runs `flyway:clean` (drops all tables)
2. Runs `flyway:migrate` (recreates schema)
3. Useful for debugging migration issues

**Warning**: Only for test database!

---

## 🐛 Troubleshooting

### Problem: Tests fail with "Connection refused"

**Cause**: MySQL not ready when tests start

**Solution**: Script already handles this with healthcheck. If running manually:
```bash
# Wait for MySQL
until docker-compose --profile test exec -T mysql-test mysqladmin ping -h localhost -u root -ptestroot --silent; do
    echo "Waiting for MySQL..."
    sleep 2
done
```

---

### Problem: "Table doesn't exist"

**Cause**: Flyway migrations not executed

**Solution**:
```bash
# Clean and re-migrate
./clean-test-db.sh

# Or manually
docker-compose --profile test exec app-test ./mvnw flyway:clean flyway:migrate
```

---

### Problem: "Duplicate entry" errors

**Cause**: Database not cleaned between tests

**Solution**: Ensure `cleanDatabase()` is called in `@BeforeEach`:
```java
@BeforeEach
void setUp() {
    cleanDatabase();  // ← This must be called
    // ... rest of setup
}
```

---

### Problem: LazyInitializationException in tests

**Cause**: Hibernate session closed before accessing lazy collections

**Solution**: Add `@Transactional` to test class:
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // ← Add this
class MyRepositoryTest {
    // ...
}
```

**Note**: This is only needed for repository tests that access lazy collections. Controller tests don't need it.

---

### Problem: Tests are very slow

**Check**:
1. Is `mysql-test` using tmpfs? (Check docker-compose.yaml)
2. Are you running in Docker Desktop with enough RAM allocated?

**Optimize**:
```yaml
# docker-compose.yaml
mysql-test:
  tmpfs:
    - /var/lib/mysql  # ← Database in RAM (much faster)
```

---

## 🔄 CI/CD Integration

### GitLab CI Example

```yaml
test:
  stage: test
  image: docker:latest
  services:
    - docker:dind
  before_script:
    - apk add --no-cache bash
    - chmod +x run-tests.sh
  script:
    - ./run-tests.sh
  artifacts:
    when: always
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports/
  only:
    - main
    - develop
    - merge_requests
```

### GitHub Actions Example

```yaml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set execute permissions
        run: chmod +x run-tests.sh
      
      - name: Run tests
        run: ./run-tests.sh
      
      - name: Publish test results
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Maven Tests
          path: target/surefire-reports/TEST-*.xml
          reporter: java-junit
```

---

## 📏 Test Naming Convention

We follow the **Given-When-Then** pattern:

```java
@Test
@DisplayName("should_[Expected]_When_[Condition]")
void should_ReturnProduct_When_ValidIdProvided() {
    // Given
    ProductId id = ProductId.generate();
    Product product = createTestProduct(id);
    repository.save(product);
    
    // When
    Optional<Product> result = repository.findById(id);
    
    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
}
```

**Format**: `should_[ExpectedResult]_When_[Condition]`

**Examples**:
- ✅ `should_CreateDifferential_When_ValidDataProvided`
- ✅ `should_ThrowException_When_NegativePercentage`
- ✅ `should_ReturnEmptyList_When_NoDifferentialsExist`
- ❌ `testCreate` (too vague)
- ❌ `test_differential_creation` (not descriptive)

---

## 🎯 Best Practices

### 1. Database Cleanup
Always clean database in `@BeforeEach`, not `@AfterEach`:
```java
// ✅ GOOD
@BeforeEach
void setUp() {
    cleanDatabase();  // Clean BEFORE test
}

// ❌ BAD
@AfterEach
void tearDown() {
    cleanDatabase();  // Don't clean AFTER (hides test data)
}
```

**Why?** If test fails, you can inspect database state for debugging.

---

### 2. Use Test Profiles
Always activate `test` profile:
```java
@SpringBootTest
@ActiveProfiles("test")  // ← Always use test profile
class MyTest {
    // ...
}
```

This ensures `application-test.properties` is loaded.

---

### 3. Avoid @Transactional Unless Needed
```java
// ✅ GOOD - Controller tests (no @Transactional)
@SpringBootTest
@AutoConfigureMockMvc
class DifferentialControllerIntegrationTest {
    // Tests real transaction behavior
}

// ✅ GOOD - Repository tests with lazy loading
@SpringBootTest
@Transactional  // Needed for lazy collections
class JpaCustomerRepositoryTest {
    // Keeps session open
}
```

---

### 4. Clear Test Data
Use descriptive, isolated test data:
```java
// ✅ GOOD
Customer customer = Customer.create(
    CustomerCode.of(1001L),
    new LegalName("Test Corporation Ltd"),
    CustomerType.BUSINESS,
    new Rif("J-12345678-9"),
    new CustomerAlias("TestCorp")
);

// ❌ BAD
Customer customer = new Customer("John", "Doe");  // Too generic
```

---

### 5. Test One Thing
```java
// ✅ GOOD - Single assertion focus
@Test
void should_ValidatePercentage_When_NegativeValue() {
    assertThatThrownBy(() -> new DifferentialPercentage(-5.0))
        .isInstanceOf(IllegalArgumentException.class);
}

// ❌ BAD - Multiple unrelated assertions
@Test
void testDifferential() {
    // Creates differential
    // Updates differential
    // Deletes differential
    // All in one test (too much!)
}
```

---

## 📚 Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Testcontainers](https://www.testcontainers.org/) (alternative approach)

---

## 🎓 Summary

**Key Takeaways**:
1. ✅ We use **real MySQL** in tests (same as production)
2. ✅ Tests run in **Docker** with tmpfs (fast)
3. ✅ Use `./run-tests.sh` for automated execution
4. ✅ Clean database **before** each test, not after
5. ✅ Integration tests require **JWT authentication**
6. ✅ Follow **naming conventions** for clarity
7. ✅ Flyway migrations run in tests (validates migrations)

**Quick Commands**:
```bash
# Run all tests
./run-tests.sh

# Run specific module
./run-tests.sh DifferentialControllerIntegrationTest

# Reset database manually
./clean-test-db.sh
```

**Happy Testing!** 🚀
