# Milestone 11: Testing

## Overview
Comprehensive test suite with 78 tests covering unit tests, controller tests, and security tests. Uses Mockito with subclass mock maker for Java 26 compatibility.

## Test Infrastructure

### Dependencies (pom.xml)
- spring-boot-starter-test (includes JUnit 5, Mockito, AssertJ)
- spring-security-test (security test utilities)
- H2 database (in-memory test DB)
- Testcontainers (PostgreSQL for integration tests)
- Mockito 5.14.2 (subclass mock maker for Java 26)

### Test Configuration
- `src/test/resources/application-test.yml` - H2 in-memory DB, simple cache, test JWT secrets
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` - subclass mock maker

### Test Data Factory
`src/test/java/com/ailisting/config/TestDataFactory.java` - Reusable factory methods:
- `createUser(id, username, email, role)` - Create user with any role
- `createAdminUser()` / `createRegularUser()` - Pre-configured users
- `createListing(user, productName)` / `createPublishedListing(user, productName)`
- `createListingWithId(id, user, productName)` - Listing with specific ID

## Unit Tests

### JwtTokenProviderTest (12 tests)
Location: `src/test/java/com/ailisting/security/JwtTokenProviderTest.java`

| Test | Description |
|------|-------------|
| generateToken_ValidUsername_ReturnsToken | JWT access token generation |
| generateRefreshToken_ValidUsername_ReturnsToken | JWT refresh token generation |
| validateToken_ValidAccessToken_ReturnsTrue | Access token validation |
| validateToken_ValidRefreshToken_ReturnsTrue | Refresh token validation |
| validateToken_InvalidToken_ReturnsFalse | Invalid token rejection |
| validateToken_ExpiredToken_ReturnsFalse | Expired token detection |
| getUsernameFromToken_ValidToken_ReturnsUsername | Username extraction |
| getTokenType_AccessToken_ReturnsAccess | Token type claim for access |
| getTokenType_RefreshToken_ReturnsRefresh | Token type claim for refresh |
| getExpirationMs_ReturnsCorrectValue | Expiration config |
| getRefreshExpirationSeconds_ReturnsCorrectValue | Refresh expiration config |
| isTokenExpired_FreshToken_ReturnsFalse | Fresh token check |

### AuthServiceImplTest (16 tests)
Location: `src/test/java/com/ailisting/service/AuthServiceImplTest.java`

Tests all AuthService methods with mocked UserRepository, RefreshTokenRepository, PasswordEncoder, AuthenticationManager, JwtTokenProvider.

Covers: register (valid, duplicate username, duplicate email), login (valid, disabled user), refresh token (valid, expired, invalid), logout (valid, invalid), forgot password (valid, nonexistent email), reset password (valid, invalid token), verify email (valid, invalid token).

### ListingServiceImplTest (18 tests)
Location: `src/test/java/com/ailisting/service/ListingServiceImplTest.java`

Tests all ListingService methods with mocked ListingRepository, UserRepository, StorageService, AiGenerationService.

Covers: create (valid, invalid user), read (valid, invalid ID), update (valid), delete (draft soft-delete, with image cleanup), duplicate, status transitions (valid DRAFT->PUBLISHED, invalid ARCHIVED->PUBLISHED, invalid PUBLISHED->DRAFT), list/search/filter, stats, AI content generation.

### AiGenerationServiceImplTest (8 tests)
Location: `src/test/java/com/ailisting/service/AiGenerationServiceImplTest.java`

Tests AI generation with mocked AiProviderFactory, AiGenerationLogRepository, UserRepository.

Covers: generation (valid, user not found, provider failure, invalid JSON response), AI availability checks (available, no provider, provider throws, provider not available).

## Controller Tests

### AuthControllerTest (8 tests)
Location: `src/test/java/com/ailisting/controller/AuthControllerTest.java`

Standalone MockMvc setup (no Spring context). Tests all /auth endpoints: register, login, refresh, logout, forgot-password, reset-password, verify-email.

### ListingControllerTest (10 tests)
Location: `src/test/java/com/ailisting/controller/ListingControllerTest.java`

Standalone MockMvc with principal injection. Tests: create, get, list, update, delete, duplicate, status update, search, stats, unauthorized access.

### AdminControllerTest (6 tests)
Location: `src/test/java/com/ailisting/controller/AdminControllerTest.java`

Standalone MockMvc with ROLE_ADMIN authority. Tests: getAllUsers, getUserById, toggleUserStatus, analytics overview, generation stats, health check.

## Running Tests

```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceImplTest

# Run specific test method
mvn test -Dtest=AuthServiceImplTest#register_ValidRequest_ReturnsAuthResponse

# Run integration tests (requires Docker)
mvn verify -DskipTests

# Run with coverage
mvn test jacoco:report
```

## Architecture Decisions
- Subclass mock maker for Java 26 compatibility (avoids byte-buddy module issues)
- Standalone MockMvc for controller tests (no Spring context = faster)
- H2 in-memory DB for test data persistence scenarios
- Test data factory for consistent, maintainable test fixtures
- Surefire excludes *IntegrationTest.java and *IT.java from unit test runs
