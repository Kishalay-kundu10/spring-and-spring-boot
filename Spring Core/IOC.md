# Spring Core : IOC and DI

Official Reference : [Spring framework official Docs](https://docs.spring.io/spring-framework/reference/core.html)

---
### Why Spring exist at all?

Earlier Java enterprise code looked like this -- 

```java

public class OrderService {
// Tightly coupled — OrderService creates its own dependency
private PaymentService paymentService = new PaymentService();
private EmailService emailService = new EmailService();
}

```
This causes three immediate problems. First, OrderService is tightly coupled — you can't swap PaymentService for a mock in tests without changing the class. Second, object creation is scattered everywhere — no central control. Third, managing lifecycles (open DB connection, close it, etc.) becomes your problem.
Spring solves all three with one core idea:

Don't create your objects. Describe them. Let the framework create, wire, and manage them.

**This is called Inversion of Control (IoC)** — you invert the control of object creation from your code to the framework.

#### The IOC Container :- 

Spring's IoC container is represented by two interfaces. You need to know both.
- **BeanFactory** — the basic container. Lazily initializes beans (creates them only when asked). Lightweight, rarely used directly.

- **ApplicationContext** — extends BeanFactory. This is what you'll use 100% of the time in real projects. It eagerly initializes beans at startup, supports events, i18n, AOP integration, and much more.

```java

// The two most common ApplicationContext implementations
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class); // Java config
ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml"); 

```
In Spring Boot you never instantiate this yourself — Boot creates and manages it for you. But understanding what it does is fundamental.

>- **ApplicationContext vs BeanFactory** ?

>  BeanFactory is Lazy initialization.
   ApplicationContext is eager initialization, and that ApplicationContext extends BeanFactory.
   The reason is that ApplicationContext supports features that are essential for a real application — @PostConstruct / @PreDestroy lifecycle hooks, Spring AOP, event publishing (ApplicationEvent), internationalization, and most critically — the entire auto-configuration machinery that Spring Boot is built on. BeanFactory supports none of these. Boot needs ApplicationContext to function. 
---
#### What is a bean ? 

A Bean is simply any object that is managed by the Spring IoC container. That's it. When Spring creates and manages an object's lifecycle, that object is a bean.

---

#### What is dependency injection ?

Dependency Injection (DI) is the mechanism Spring uses to implement IoC. Instead of a class creating its dependencies, Spring injects them. There are three styles:

1. Style 1 : **Constructor injection**
```java
@Service
public class OrderService {

    private final PaymentService paymentService;
    private final EmailService emailService;

    // Spring sees one constructor and automatically injects
    public OrderService(PaymentService paymentService, EmailService emailService) {
        this.paymentService = paymentService;
        this.emailService = emailService;
    }
}
```
*Why this is best* : dependencies are final (immutable), the object is always in a valid state, and it's trivially testable — just call new OrderService(mockPayment, mockEmail) in tests.

2. Style 2 : **Setter Injection** (use for optional dependencies only)

```java
@Service
public class OrderService {
    private DiscountService discountService; // optional

    @Autowired // required here since it's not a constructor
    public void setDiscountService(DiscountService discountService) {
        this.discountService = discountService;
    }
}
```
3. Style 3 : **Field Injection** ❌ (avoid in production code)

```java
@Service
public class OrderService {
    @Autowired // Spring injects directly into the field via reflection
    private PaymentService paymentService;
}
```
This looks clean but hides dependencies, makes testing harder (you need reflection or Spring context to inject mocks), and is flagged by static analysis tools. The Spring team themselves discourage it in official docs.


>- **Constructor vs Field Injection**? 

>  ✅ constructor injection makes dependencies mandatory and avoids null states.

>  ✅ field injection hides dependencies and hurts testability.

>  ✅ The single biggest advantage of constructor injection — final. When a field is final, the object is immutable and thread-safe by construction. You can never accidentally reassign a dependency. This is the reason the Spring team specifically recommends it in their official docs.

>  ✅ field injection uses reflection to bypass normal Java access rules — which is why it's slow, fragile, and flagged by tools like SonarQube.
---

#### Annotations used for defining Beans

Spring uses the following annotations for defining beans:

```java
@Component      // Generic Spring-managed bean
@Service        // Semantic alias for @Component — marks service layer
@Repository     // Semantic alias — marks data layer, adds exception translation
@Controller     // Semantic alias — marks web layer (MVC)
@RestController // @Controller + @ResponseBody — for REST APIs
@Configuration  // Marks a class as a source of @Bean definitions

```
All of @Service, @Repository, @Controller are just @Component with extra meaning. Spring scans your classpath and registers all of them as beans

```java
@Configuration
public class AppConfig {

    @Bean // Explicit bean definition — return value becomes a Spring bean
    public PaymentService paymentService() {
        return new StripePaymentService();
    }

    @Bean
    public OrderService orderService() {
        return new OrderService(paymentService()); // Spring intercepts this call!
    }
}
```

---

#### Bean Scope 

By default, every Spring bean is a Singleton — one instance per ApplicationContext, shared everywhere. This is almost always what you want for stateless services.

| Scope     | Annotations                 | Meaning                         | 
|-----------|-----------------------------|---------------------------------|
| Singleton | @Scope("singleton") Default | One instance, shared everywhere |
| Prototype | @Scope("prototype")         | One instance per request        |
| Request   | @Scope("request")           | One instance per request        |
| Session   | @Scope("session")           | One instance per session        |

---

