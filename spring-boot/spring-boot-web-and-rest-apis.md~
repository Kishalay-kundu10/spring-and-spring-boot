# Spring Boot Web And Rest APIs

official reference : [spring mvc](https://docs.spring.io/spring-framework/reference/web/webmvc.html)

### How any web request goes through spring mvc ?  : 

```
HTTP Request
    │
    ▼
DispatcherServlet        ← single entry point for ALL requests (Front Controller pattern)
    │
    ▼
HandlerMapping           ← figures out WHICH controller method matches this URL + HTTP verb
    │
    ▼
HandlerAdapter           ← calls the controller method, resolves @PathVariable, @RequestBody etc
    │
    ▼
Controller method        ← your code runs here
    │
    ▼
HttpMessageConverter     ← converts return value to JSON using Jackson
    │
    ▼
HTTP Response (JSON)

```
*DispatcherServlet* is auto-configured by *spring-boot-starter-web*. You never touch it directly — but knowing it exists explains why all your requests go through one place and why things like filters and interceptors work globally.

---

### Building API Rest endpoints : 

*Controllers* are the entry point for all requests. They are annotated with *RestController* and return a *ResponseBody* (or *ResponseEntity*).

```java
@RestController                    // @Controller + @ResponseBody on every method
@RequestMapping("/api/v1/students") // base path for all methods in this class
public class StudentController {

    @GetMapping                    // GET /api/v1/students
    public List<Student> getAll() { }

    @GetMapping("/{id}")           // GET /api/v1/students/1
    public Student getById(@PathVariable Long id) { }

    @PostMapping                   // POST /api/v1/students
    public Student create(@RequestBody Student student) { }

    @PutMapping("/{id}")           // PUT /api/v1/students/1
    public Student update(@PathVariable Long id, @RequestBody Student student) { }

    @PatchMapping("/{id}")         // PATCH /api/v1/students/1  (partial update)
    public Student patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) { }

    @DeleteMapping("/{id}")        // DELETE /api/v1/students/1
    public void delete(@PathVariable Long id) { }
}

```
*PUT* vs *PATCH* : -
- PUT replaces the entire resource — you send the full object
- PATCH updates partial fields — you send only what changed.

---

### Parameter extraction Annotation : 

```java 
// @PathVariable — from the URL path
@GetMapping("/{id}")
public Student getById(@PathVariable Long id) { }
// GET /api/v1/students/42  →  id = 42

// @RequestParam — from query string
@GetMapping
public List<Student> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String grade
) { }
// GET /api/v1/students?page=0&size=10&grade=A

// @RequestBody — deserializes JSON body into a Java object
@PostMapping
public Student create(@RequestBody Student student) { }
// Body: {"name":"John","email":"john@example.com","grade":"A"}

// @RequestHeader — reads an HTTP header
@GetMapping("/profile")
public Student getProfile(@RequestHeader("Authorization") String token) { }
```
---

### *ResponseEntity* - Controlling the HTTP response properly:

Right now your controller returns raw objects. That's fine for happy path, but in real APIs you need to control the HTTP status code and headers. ResponseEntity gives you that control:

```java 
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        Student student = studentService.getById(id);
        return ResponseEntity.ok(student);  // 200 OK with body
    }

    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student student) {
        Student created = studentService.create(student);
        return ResponseEntity
            .status(HttpStatus.CREATED)  // 201 Created
            .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
```
**Http Status Codes we must know** : 

| **Code**                  | **Meaning**          | **When to use**       |
|---------------------------|----------------------|-----------------------|
| 200 OK                    | Success              | GET, PUT responses    |
| 201 Created               | Resource created     | POST responses        |
| 204 No Content            | Resource deleted     | DELETE responses      |
| 400 Bad Request           | Client error         | Validation failures   |
| 401 Unauthorized          | Authentication error | Missing/invalid token |
| 403 Forbidden             | Authorization error  | Wrong role            |
| 404 Not Found             | Resource not found   | ID not found          |
| 500 Internal Server error | Server Bug           | Unhandled exceptions  |
---

### Validation with Bean Validation (Jakarta Validation) :

Never trust data coming from the client. Validate it at the boundary.
First add the dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
Annotate your domain model:

```java
public class Student {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Grade is required")
    @Pattern(regexp = "^[A-F]$", message = "Grade must be A through F")
    private String grade;
}
```
Trigger validation in the controller with *@Valid* : 

```java
@PostMapping
public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
    // if validation fails, Spring throws MethodArgumentNotValidException
    // before your method body even runs
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(studentService.create(student));
}
```
Common Validation annotation : 

| **Annotation**   | **Meaning**                            |
|------------------|----------------------------------------|
| @NotBlank        | Not empty                              |
| @Size            | Length                                 |
| @Email           | Valid email                            |
| @Pattern         | Regex match                            |
| @Positive        | Value must be positive                 |
| @Min             | Value must be at least X               |
| @Max             | Value must be at most X                |
| @DecimalMin      | Value must be at least X               |
| @DecimalMax      | Value must be at most X                |
| @Digits          | Value must be X digits long            |
| @Future          | Value must be in the future            |
| @Past            | Value must be in the past              |
| @PastOrPresent   | Value must be in the past or present   |
| @FutureOrPresent | Value must be in the future or present |

---

### Exception Handling - The professional way : 

Right now if a student isn't found, Spring returns a *500 Internal Server Error* with a stack trace. That's terrible for an API. The industry standard is *@RestControllerAdvice* :

```java
// First — a clean error response structure
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    // getters
}
```
```java 
// Custom exception
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(Long id) {
        super("Student not found with id: " + id);
    }
}
```
```java 
// Global exception handler — handles exceptions from ALL controllers
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(StudentNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(Exception.class)  // catch-all safety net
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "An unexpected error occurred"));
    }
}
```
Now when student is not found, Spring returns a *404 Not Found* with a clean error message.

```json
{
  "status": 404,
  "message": "Student not found with id: 99",
  "timestamp": "2025-03-29T10:30:00"
}
```
---

### DTOs - the patter every production API needs : 

Right now you're exposing your Student model directly. This is an anti-pattern in production for two reasons — you might expose fields you don't want to (like a password), and your API contract becomes coupled to your database schema.

The solution is Data Transfer Objects (DTOs):

```java
// What the client SENDS to create a student (no id — server generates it)
public class CreateStudentRequest {
    @NotBlank private String name;
    @Email    private String email;
    @NotBlank private String grade;
    // getters/setters
}

// What the server SENDS back to the client (no sensitive fields)
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private String grade;
    // getters/setters
}
```

