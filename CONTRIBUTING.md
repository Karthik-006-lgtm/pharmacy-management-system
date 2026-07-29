# 🤝 Contributing to Online Pharmacy Management System

First off, thank you for considering contributing to the Online Pharmacy Management System! It's people like you that make this project such a great tool.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Guidelines](#coding-guidelines)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)

---

## 📜 Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

### Our Standards

- ✅ Using welcoming and inclusive language
- ✅ Being respectful of differing viewpoints
- ✅ Gracefully accepting constructive criticism
- ✅ Focusing on what is best for the community
- ✅ Showing empathy towards other community members

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
```bash
git clone https://github.com/YOUR_USERNAME/pharmacy-management-system.git
cd pharmacy-management-system
```

3. Add the upstream repository:
```bash
git remote add upstream https://github.com/Karthik-006-lgtm/pharmacy-management-system.git
```

---

## 🎯 How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the behavior
- **Expected behavior**
- **Actual behavior**
- **Screenshots** (if applicable)
- **Environment details** (OS, Java version, browser)

### 💡 Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title and description**
- **Use case** - Why is this enhancement needed?
- **Proposed solution**
- **Alternative solutions** you've considered
- **Mockups or examples** (if applicable)

### 🔧 Code Contributions

1. **Find an issue** to work on or create a new one
2. **Comment on the issue** expressing your interest
3. **Wait for assignment** to avoid duplicate work
4. **Create a branch** for your changes
5. **Make your changes** following our guidelines
6. **Submit a pull request**

---

## 💻 Development Setup

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

### 3. Access the Application

- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

### 4. Run Tests

```bash
mvn test
```

---

## 🔄 Pull Request Process

### Before Submitting

- [ ] Code follows our coding guidelines
- [ ] Tests have been added/updated
- [ ] All tests pass locally
- [ ] Documentation has been updated
- [ ] Commit messages follow our guidelines
- [ ] No merge conflicts with main branch

### Submission Steps

1. **Update your fork**:
```bash
git fetch upstream
git rebase upstream/main
```

2. **Push to your fork**:
```bash
git push origin feature/your-feature-name
```

3. **Create Pull Request** on GitHub

4. **Fill out the PR template** completely

5. **Wait for review** - Maintainers will review your PR

### PR Review Process

- ✅ Code review by at least one maintainer
- ✅ All automated checks must pass
- ✅ No unresolved review comments
- ✅ Approved by maintainer

---

## 📝 Coding Guidelines

### Java Code Standards

#### General Principles

- Follow **SOLID principles**
- Write **clean, readable code**
- Use **meaningful variable names**
- Keep methods **small and focused**
- **DRY** - Don't Repeat Yourself

#### Naming Conventions

```java
// Classes: PascalCase
public class MedicineService { }

// Methods: camelCase
public void saveMedicine() { }

// Constants: UPPER_SNAKE_CASE
public static final int MAX_STOCK = 1000;

// Variables: camelCase
private String medicineName;
```

#### Code Structure

```java
// Controller Example
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    
    private final MedicineService medicineService;
    
    // Constructor injection (preferred)
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicine(@PathVariable Long id) {
        // Implementation
    }
}
```

#### Service Layer

```java
@Service
@Transactional
public class MedicineService {
    
    private final MedicineRepository medicineRepository;
    
    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }
    
    // Business logic methods
}
```

### Frontend Standards

#### HTML/Thymeleaf

- Use **semantic HTML5** elements
- Proper **indentation** (2 or 4 spaces)
- **Thymeleaf syntax** for dynamic content
- **Bootstrap classes** for styling

#### CSS

- Use **Bootstrap 5** utilities where possible
- Custom CSS in separate files
- Follow **BEM naming** for custom classes
- Mobile-first approach

---

## 🧪 Testing Guidelines

### Unit Tests

```java
@Test
public void testSaveMedicine() {
    // Arrange
    Medicine medicine = new Medicine();
    medicine.setName("Test Medicine");
    
    // Act
    Medicine saved = medicineService.save(medicine);
    
    // Assert
    assertNotNull(saved.getId());
    assertEquals("Test Medicine", saved.getName());
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class MedicineControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetMedicine() throws Exception {
        mockMvc.perform(get("/api/medicines/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").exists());
    }
}
```

### Test Coverage

- Aim for **80%+ code coverage**
- Test **happy paths** and **edge cases**
- Test **error scenarios**
- Mock external dependencies

---

## 📝 Commit Message Guidelines

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples

```bash
# Good commit messages
feat(medicine): add medicine search functionality
fix(cart): resolve quantity update issue
docs(readme): update installation instructions
refactor(service): improve medicine service performance

# Bad commit messages
update
fixed bug
changes
wip
```

### Detailed Example

```
feat(invoice): implement automatic invoice generation

- Add InvoiceService for invoice management
- Implement PDF generation using iText
- Add tax calculation (18% GST)
- Create invoice download endpoint
- Update order service to trigger invoice creation

Closes #123
```

---

## 🏷️ Branch Naming

### Convention

```
<type>/<short-description>

Examples:
feature/medicine-search
bugfix/cart-quantity-issue
hotfix/security-vulnerability
docs/api-documentation
```

---

## 🔍 Code Review Checklist

### For Contributors

- [ ] Code is self-explanatory
- [ ] No commented-out code
- [ ] No debug statements (System.out.println)
- [ ] Proper error handling
- [ ] Input validation where needed
- [ ] Tests included and passing
- [ ] Documentation updated

### For Reviewers

- [ ] Code follows project standards
- [ ] Logic is correct and efficient
- [ ] Security considerations addressed
- [ ] Performance implications considered
- [ ] Tests are comprehensive
- [ ] Documentation is clear

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Bootstrap Documentation](https://getbootstrap.com/docs/)

---

## 🎯 Areas Needing Contribution

### High Priority

- 🧪 **Unit Tests** - Increase test coverage
- 🧪 **Integration Tests** - End-to-end testing
- 📧 **Email Notifications** - Order confirmations
- 💳 **Payment Gateway** - Razorpay/Stripe integration

### Medium Priority

- 📊 **Analytics Dashboard** - Charts and graphs
- 📄 **PDF Generation** - Professional invoices
- 🌐 **API Documentation** - Swagger/OpenAPI
- 🎨 **UI Improvements** - Enhanced user experience

### Future Enhancements

- 📱 **Mobile App** - React Native/Flutter
- 🤖 **AI Integration** - Medicine recommendations
- 🌍 **i18n** - Multi-language support
- 🔔 **Push Notifications** - Real-time updates

---

## ❓ Questions?

Feel free to:
- Open an [issue](https://github.com/Karthik-006-lgtm/pharmacy-management-system/issues)
- Start a [discussion](https://github.com/Karthik-006-lgtm/pharmacy-management-system/discussions)
- Contact the maintainers

---

## 🙏 Thank You!

Your contributions make this project better for everyone. We appreciate your time and effort!

---

<div align="center">

**Happy Contributing!** 🎉

[⬆ Back to Top](#-contributing-to-online-pharmacy-management-system)

</div>
