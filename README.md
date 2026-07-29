# Online Pharmacy Management System

A production-ready web-based pharmacy management system built with Java Spring Boot, providing comprehensive features for online medicine ordering, prescription management, and inventory control with advanced admin capabilities.

## Tech Stack

### Backend
- Java 17+
- Spring Boot 3.2.0
- Spring MVC
- Spring Data JPA
- Hibernate
- Spring Security (BCrypt password encoding)
- Maven 3.6+

### Frontend
- Thymeleaf Template Engine
- HTML5, CSS3, JavaScript
- Bootstrap 5
- Bootstrap Icons
- AJAX for dynamic features

### Database
- MySQL 8.0+ (Production)
- H2 Database (Development/Testing)

## Features

### Customer Features
- User Registration & Login with validation
- Browse & Search Medicines (advanced search)
- Filter by Category & Prescription Requirement
- Sort by Price, Name, Date
- View Medicine Details with expiry alerts
- **Wishlist Management** - Save favorite medicines
- Shopping Cart Management
- Place Orders with prescription upload
- **Download Invoice** (PDF format)
- Track Order Status in real-time
- View Order History
- Manage Profile

### Admin Features
- **Advanced Dashboard** with 8 key metrics
  - Total Medicines, Orders, Customers, Revenue
  - Today's Orders, Pending/Delivered Orders
  - Low Stock & Expired Medicines
- **Medicine Management** with image upload (AJAX)
- **Low Stock Alerts** - Configurable threshold
- **Expiry Alert System** - Color-coded (7/15/30 days)
- Manage Categories (CRUD)
- Manage Inventory with stock tracking
- View Customer List
- **Order Management** with status updates
- **Audit Log System** - Track all admin actions
- Verify/Reject Prescriptions
- Sales Analytics foundation

## Project Structure

```
online-pharmacy-management/
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/
│   │   │   ├── PharmacyApplication.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── WishlistController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── InvoiceController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── AdminDashboardController.java
│   │   │   │   ├── AdminMedicineController.java
│   │   │   │   ├── AdminCategoryController.java
│   │   │   │   ├── AdminOrderController.java
│   │   │   │   ├── AdminCustomerController.java
│   │   │   │   ├── AdminPrescriptionController.java
│   │   │   │   └── AdminAuditController.java
│   │   │   ├── dto/
│   │   │   │   ├── UserRegistrationDto.java
│   │   │   │   ├── MedicineDto.java
│   │   │   │   └── CategoryDto.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Medicine.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Cart.java
│   │   │   │   ├── Wishlist.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Prescription.java
│   │   │   │   └── AuditLog.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   └── InsufficientStockException.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── MedicineRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   ├── WishlistRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderItemRepository.java
│   │   │   │   ├── PrescriptionRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── MedicineService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── CartService.java
│   │   │   │   ├── WishlistService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── PrescriptionService.java
│   │   │   │   ├── PDFService.java
│   │   │   │   └── AuditLogService.java
│   │   │   └── util/
│   │   │       └── SecurityUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-h2.properties
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   └── js/
│   │       │       └── main.js
│   │       └── templates/
│   │           ├── landing.html
│   │           ├── home.html
│   │           ├── auth/
│   │           ├── medicines/
│   │           ├── cart/
│   │           ├── wishlist/
│   │           ├── orders/
│   │           ├── profile/
│   │           ├── admin/
│   │           ├── fragments/
│   │           └── error/
├── uploads/                # Medicine images
├── .gitignore
├── pom.xml
└── README.md
```

## Installation & Setup

### Prerequisites
- JDK 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Database Setup

1. **For Development (H2 Database)**:
   - H2 is configured by default
   - No setup required
   - Access H2 console at: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:pharmacy_db`
   - Username: `sa`, Password: (empty)

2. **For Production (MySQL)**:
   - Create MySQL database:
   ```sql
   CREATE DATABASE pharmacy_db;
   ```
   - Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/pharmacy_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

### Running the Application

1. Clone or download the repository

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Access the application:
```
http://localhost:8080
```

5. (Optional) Access H2 Console:
```
http://localhost:8080/h2-console
```

## Default User Accounts

### Admin Account
- Email: `admin@pharmacy.com`
- Password: `admin123`

### Customer Account
- Email: `john@example.com`
- Password: `john123`

## Database Schema

### Tables
- **users** - User account information
- **roles** - User roles (ADMIN, CUSTOMER)
- **user_roles** - User-Role mapping
- **categories** - Medicine categories
- **medicines** - Medicine inventory with images
- **cart** - Shopping cart items
- **wishlist** - Customer wishlist items
- **orders** - Customer orders
- **order_items** - Order line items
- **prescriptions** - Uploaded prescription files
- **audit_logs** - Admin action tracking

## Security Features

- BCrypt password encryption
- Role-based access control (RBAC)
- CSRF protection
- Session management
- Secure password storage
- Protected admin routes

## Order Status Workflow

1. **PENDING** - Order placed
2. **PRESCRIPTION_VERIFICATION** - Awaiting prescription approval
3. **APPROVED** - Prescription approved
4. **PACKED** - Order packed
5. **SHIPPED** - Order shipped
6. **DELIVERED** - Order delivered
7. **CANCELLED** - Order cancelled

## Prescription Workflow

1. Customer uploads prescription for orders containing prescription-required medicines
2. Admin reviews the uploaded prescription
3. Admin approves or rejects the prescription
4. Order status updates accordingly

## Key Validations

- Email uniqueness check
- Stock availability check before adding to cart
- Prescription requirement validation
- Form input validation with real-time feedback
- Expiry date validation with color-coded alerts
- Stock level validation with low stock warnings
- File upload validation (image formats, size limits)
- Audit trail for all critical operations

## Future Enhancements

- Email/SMS notifications for order updates
- Payment gateway integration (Razorpay, Stripe)
- Advanced analytics with Chart.js visualizations
- Export reports (Excel, PDF)
- Medicine review and ratings system
- Multi-language support (i18n)
- Promotional discounts and coupon system
- Real-time chat support
- Mobile app integration (REST API)
- Advanced inventory forecasting

## Development Guidelines

### Architecture
- Clean layered architecture (Controller → Service → Repository → Database)
- SOLID principles
- MVC pattern
- RESTful API design

### Code Standards
- Constructor-based dependency injection
- Proper exception handling
- Validation at all layers
- Transaction management
- Comprehensive logging

## License

This project is developed for educational and demonstration purposes.

## Support

For issues or questions, please create an issue in the repository.

---

**Note**: This is a demonstration project. For production deployment, additional security measures, testing, and infrastructure setup are recommended.
