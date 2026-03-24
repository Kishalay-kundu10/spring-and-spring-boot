# Spring Boot 

official reference : [Spring boot official docs](https://docs.spring.io/spring-boot/)

## What is Spring Boot ?

Spring Boot helps you to create stand-alone, production-grade Spring-based applications that you can run. We take an opinionated view of the Spring platform and third-party libraries, so that you can get started with minimum fuss. Most Spring Boot applications need very little Spring configuration.

Spring Boot is not a replacement for Spring Framework. It is a tool built on top of Spring that eliminates boilerplate configuration. Before Spring Boot, setting up a Spring web application required hundreds of lines of XML or Java config — configuring a DispatcherServlet, a DataSource, a ViewResolver, transaction managers, and so on.

Spring Boot gives us three things -

-  **Auto-configuration** — detects what's on your classpath and configures it automatically
-  **Starters** — curated dependency bundles so you don't hunt for compatible versions
-  **Embedded server** — Tomcat/Jetty/Undertow baked in, no external server needed.

### *@SpringBootApplication* - What it actually does?

*@SpringBootApplication* mainly consists of three annotations :- 

```java
@SpringBootApplication
// is exactly equivalent to:
@SpringBootConfiguration   // marks this as a configuration class (like @Configuration)
@EnableAutoConfiguration   // turns on auto-configuration
@ComponentScan             // scans this package + all sub-packages for beans
public class StudentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentApiApplication.class, args);
    }
}
```
SpringApplication.run() does the following in order — 
- boots the JVM
- creates the ApplicationContext
- triggers component scanning
- fires auto-configuration
- starts the embedded Tomcat
- and then your app is live.

This is why your main class package matters. If StudentApiApplication is in *com.student.student_api* then Spring scans *com.student.student_api* and everything under it. If you put a controller in *com.other*, Spring never finds it.

### *@AutoConfiguration* - The magic Annotation 

This is the most important concept in Spring Boot to understand deeply. When you add spring-boot-starter-web to your pom.xml, Spring Boot's auto-configuration system:

1. Detects that spring-webmvc is on the classpath
2. Automatically configures a DispatcherServlet
3. Configures Jackson for JSON serialization
4. Starts an embedded Tomcat on port 8080

How does it know what to configure? Spring Boot ships with hundreds of @Configuration classes inside the jar, each guarded by conditions:

```java
// This is simplified from Spring Boot's actual source code
@Configuration
@ConditionalOnClass(DataSource.class)      // only if DataSource is on classpath
@ConditionalOnMissingBean(DataSource.class) // only if YOU haven't defined your own
public class DataSourceAutoConfiguration {

    @Bean
    public DataSource dataSource() {
        // creates a default HikariCP DataSource
    }
}
```
The key annotations od @AutoConfiguration : 

| Annotation                | Description                                     |
|---------------------------|-------------------------------------------------|
| @ConditionalOnClass       | Detects if a certain class is on the classpath. |
| @ConditionalOnMissingBean | Detects if a certain bean is missing.           |
| @ConditionalOnProperty    | Detects if a certain property is set.           |

- **The Golden Rule** : Your own beans always take priority over auto-configured ones. If you define a DataSource bean yourself, Spring Boot's auto-configured one backs off. This is how you customize Boot's defaults.

---

### Starters 

Starters are Maven/Gradle dependencies that pull in everything you need for a feature — the libraries, their correct versions, and the auto-configuration to wire them up.

```xml
<!-- This one dependency gives you: Spring MVC + embedded Tomcat + Jackson -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- This gives you: Hibernate + Spring Data JPA + HikariCP connection pool -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- This gives you: Spring Security with sensible defaults -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- This gives you: JUnit 5 + Mockito + AssertJ + Spring Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
- **There are no version numbers** : Spring Boot's parent POM manages all versions and guarantees they are compatible with each other. This is one of the biggest practical benefits of Boot.

---

### Application.properties or application.yml 

This is your central configuration file. Everything about your app — server port, database URL, logging level, custom settings — lives here.
Spring Boot supports two formats. Both do the same thing, pick one and be consistent. Industry currently prefers yml.

```properties
# application.properties style
server.port=8080
spring.application.name=student-api
spring.datasource.url=jdbc:postgresql://localhost:5432/studentdb
spring.datasource.username=postgres
spring.datasource.password=secret
logging.level.com.student=DEBUG
```
```yml
# application.yml style — same thing, cleaner for nested config
server:
  port: 8080

spring:
  application:
    name: student-api
  datasource:
    url: jdbc:postgresql://localhost:5432/studentdb
    username: postgres
    password: secret

logging:
  level:
    com.student: DEBUG
```

### Profiles : Current Industry-critical feature 

Real applications behave differently in different environments. You never want to connect your local dev environment to the production database. Profiles solve this.

```yml
# application.yml — shared config for all environments
spring:
  application:
    name: student-api

---
# application-dev.yml — only active when profile = dev
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb  # in-memory DB for dev
logging:
  level:
    com.student: DEBUG

---
# application-prod.yml — only active when profile = prod
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://prod-server:5432/studentdb
    username: ${DB_USER}      # reads from environment variable
    password: ${DB_PASSWORD}  # never hardcode prod secrets
logging:
  level:
    com.student: WARN
```

Activating profile in three ways : 

```properties
# Option 1 — in application.properties (for default local dev)
spring.profiles.active=dev
```

```bash
# Option 2 — JVM argument (for deployments)
java -jar student-api.jar --spring.profiles.active=prod

# Option 3 — environment variable (most common in cloud/Docker)
SPRING_PROFILES_ACTIVE=prod
```
You can also annotate beans to only exist in certain profiles:

```java
@Service
@Profile("dev")  // this bean only exists when profile is 'dev'
public class MockEmailService implements EmailService {
    public void send(String email) {
        System.out.println("MOCK: would send to " + email);
    }
}

@Service
@Profile("prod")  // this bean only exists when profile is 'prod'
public class SmtpEmailService implements EmailService {
    public void send(String email) {
        // actually send via SMTP
    }
}
```
---

###  Binding properties to a class with *@ConfigurationProperties*

Injecting individual properties with *@Value* works for one or two values, but for any real configuration group, use *@ConfigurationProperties*:

```yml
# application.yml
app:
  student:
    max-per-class: 30
    allowed-grades: A,B,C,D,F
    default-grade: B
```
```java
@ConfigurationProperties(prefix = "app.student")
@Component
public class StudentProperties {
    private int maxPerClass;
    private List<String> allowedGrades;
    private String defaultGrade;

    // getters and setters required
}
```
```java
// Use it anywhere via injection
@Service
public class StudentService {
    private final StudentProperties props;

    public StudentService(StudentProperties props) {
        this.props = props;
    }

    public void createStudent(Student student) {
        if (count() >= props.getMaxPerClass()) {
            throw new RuntimeException("Class is full");
        }
    }
}
```

This is the professional way to handle app configuration. It's type-safe, testable, and IDE-autocomplete-friendly.

---

###### 2.8 — What happens at startup — the full picture
```
java -jar student-api.jar
    │
    ├── SpringApplication.run() called
    ├── Environment created — loads application.yml, env vars, JVM args
    ├── Active profiles determined
    ├── ApplicationContext created
    ├── @ComponentScan runs — finds all @Component/@Service/@Repository
    ├── Auto-configuration runs — @ConditionalOn* checks happen
    ├── All beans instantiated and wired
    ├── @PostConstruct methods called
    ├── Embedded Tomcat starts on configured port
    └── ApplicationReadyEvent fired — app is live
```
