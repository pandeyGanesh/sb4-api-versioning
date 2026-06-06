# Spring Boot 4 & Spring Framework 7 API Versioning

This project demonstrates and tests the native, first-class API versioning feature introduced in **Spring Framework 7** and **Spring Boot 4**.

---

## The Problem & The Solution

### How API Versioning was Achieved Before Spring Boot 4
Before Spring Boot 4 / Spring Framework 7, there was no standardized framework-level API versioning system. Teams typically resorted to one of these approaches:
1. **URI Path Hardcoding**: Defining paths like `/api/v1/users` and `/api/v2/users` directly in controller annotations. This duplicated request mappings and didn't allow dynamic version resolution.
2. **Manual Request Attribute Filtering**: Using standard Spring features like `headers`, `params`, or `produces` to separate mappings (e.g. `@GetMapping(value = "/users", headers = "X-API-Version=1")`). This lacked support for semantic version comparison, defaults, and deprecation schedules.
3. **Custom `HandlerMapping` and `RequestCondition`**: Writing complex custom code extending `RequestMappingHandlerMapping` and implementing `RequestCondition` (e.g. `ApiVersionRequestCondition`). This was boilerplate-heavy, difficult to maintain, and varied wildly from project to project.

### What Spring Boot 4 API Versioning Provides
Spring Boot 4 introduces first-class versioning support built into Spring Web MVC. This solves the ecosystem fragmentation by providing:
* **Consistency Across Projects**: A single unified framework-level pattern.
* **Reduced Boilerplate**: No need to write custom annotations, registry configurers, or custom path parsers.
* **Native `@RequestMapping` Support**: Version mapping via the new `version` attribute directly on `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
* **Deprecation & Sunset Support**: Built-in support (`ApiVersionDeprecationHandler`) for generating RFC 9745 / RFC 8594 compliance deprecation headers (`Deprecation`, `Sunset`, `Link`).

---

## Under the Hood: Key Framework Classes

Spring Framework 7 implements versioning via a few central components:
1. **`ApiVersionStrategy`**: The central coordinator that holds configured preferences, resolves versions from requests, parses them, and manages validation and deprecations.
2. **`ApiVersionResolver`**: Resolves the raw version string from the request (headers, query params, URL path, etc.).
3. **`ApiVersionParser`**: Parses version strings into comparable objects. By default, `SemanticApiVersionParser` is used, matching standard `major.minor.patch` syntax (e.g. `1.0.0`).
4. **`ApiVersionDeprecationHandler`**: Manages HTTP responses for deprecated versions, automatically appending `Deprecation` and `Sunset` headers.

---

## Configuration & Options

Versioning can be configured either via Java config (`WebMvcConfigurer`) or via `application.properties`/`application.yml`. 

> [!IMPORTANT]
> You must select **exactly one** versioning strategy. Mixing strategies (e.g. attempting to resolve via both headers and path segments) is not supported and will result in conflicts.

### 1. Versioning Strategies

#### A. Request Header Versioning (Used in this project)
Keep URIs clean and specify the target API version via custom headers (e.g., `X-API-Version: 2.0`).

* **YAML Configuration**:
  ```yaml
  spring:
    mvc:
      apiversion:
        supported: 1.0,2.0
        default: 1.0
        use:
          header: X-API-Version
  ```
* **Java Configuration**:
  ```java
  @Override
  public void configureApiVersioning(ApiVersionConfigurer configurer) {
      configurer.useRequestHeader("X-API-Version")
                .addSupportedVersions("1.0", "2.0")
                .setDefaultVersion("1.0");
  }
  ```

#### B. Query Parameter Versioning
Resolve the version using a request parameter (e.g., `GET http://localhost:8100/users?version=2.0`).

* **YAML Configuration**:
  ```yaml
  spring:
    mvc:
      apiversion:
        supported: 1.0,2.0
        default: 1.0
        use:
          query-parameter: version
  ```
* **Java Configuration**:
  ```java
  configurer.useQueryParam("version");
  ```

#### C. Path Segment Versioning
Embed the version directly in the request path (e.g. `/1.0/users` or `/api/1.0/users`). The configuration value represents the 0-based index of the path segment containing the version.
* **0-based Indexing**:
  - `index 0` is the first segment (e.g. in `/{version}/users`, `version` is at index 0).
  - `index 1` is the second segment (e.g. in `/api/{version}/users`, `version` is at index 1).

* **YAML Configuration**:
  ```yaml
  spring:
    mvc:
      apiversion:
        supported: 1.0,2.0
        default: 1.0
        use:
          path-segment: 1
  ```
* **Java Configuration**:
  ```java
  configurer.usePathSegment(1);
  ```

#### D. Media Type Parameter Versioning (Content Negotiation)
Pass the API version inside the standard `Accept` header (e.g., `Accept: application/json;version=2.0`).

> [!WARNING]
> Because Spring Boot's properties binder uses relaxed binding, using standard slashes in map keys (like `application/json`) directly inside YAML files will strip the `/` and cause a startup failure. To prevent this, map keys with slashes must be wrapped in double quotes and square brackets.

* **YAML Configuration**:
  ```yaml
  spring:
    mvc:
      apiversion:
        supported: 1.0,2.0
        default: 1.0
        use:
          media-type-parameter:
            "[application/json]": version
  ```
* **Java Configuration**:
  ```java
  configurer.useMediaTypeParameter(MediaType.APPLICATION_JSON, "version");
  ```

---

## Important Versioning Behaviors & Pitfalls

### Default Version Lookup
If you do not specify the `version` attribute on your old mapping method, but specify it on the new method, Spring will handle the routing dynamically:
* If a default version is specified in configuration (`spring.mvc.apiversion.default=1.0`), requesting without a version will automatically pick the default version and execute the matching mapping.

### The "Missing Version" 400 Bad Request Pitfall
A common point of confusion arises when defining supported versions without a default fallback:
* **Scenario**: You register `2.0` in `supported` versions, but **do not** specify a default version.
* **Expectation**: Requests *with* the version header `2.0` should invoke the new endpoint, while requests *without* the version header should fall back to the unversioned method.
* **Reality**: Making a request without the version header returns a **`400 Bad Request`** error (`MissingApiVersionException`) instead of routing to the unversioned method. When API versioning is enabled, Spring requires a valid version string unless a default version is configured or versioning is marked optional.

---

## Project Setup & Code Structure

The project maps a `User` domain package leveraging the versioning features to return V1 and V2 representations of a User DTO:
* **[User.java](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/java/in/ganeshpandey/sb4_api_versioning/user/User.java)**: Core domain entity record containing standard fields like `fullName` and conversion methods mapping to DTOs.
* **[UserV1.java](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/java/in/ganeshpandey/sb4_api_versioning/user/UserV1.java)**: Version 1 DTO returning `fullName`, `age`, `bloodGroup`, and `email` (no database id).
* **[UserV2.java](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/java/in/ganeshpandey/sb4_api_versioning/user/UserV2.java)**: Version 2 DTO returning separated name fields `firstName` and `lastName`, `bloodGroup`, `email`, and `id` (no `age`).
* **[UserRepository.java](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/java/in/ganeshpandey/sb4_api_versioning/user/UserRepository.java)**: In-memory repository seeded with test users.
* **[UserController.java](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/java/in/ganeshpandey/sb4_api_versioning/user/UserController.java)**: Leverages Spring's native API versioning annotation parameters to map versioned requests:
  ```java
  @GetMapping(version = "1.0")
  public List<UserV1> getUsers() {
      return userRepository.getAll().stream().map(User::toV1).toList();
  }

  @GetMapping(version = "2.0")
  public List<UserV2> getUsersV2() {
      return userRepository.getAll().stream().map(User::toV2).toList();
  }
  ```

### API Verification
You can reference and execute the HTTP test cases located inside **[sb4-api-versioning.http](file:///Users/ganesh/Documents/Peronal/sb4-api-versioning/src/main/resources/requests/sb4-api-versioning.http)**:
```http
### Get V1 Users
GET http://localhost:8100/users
X-API-Version: 1.0

### Get V2 Users
GET http://localhost:8100/users
X-API-Version: 2.0
```
