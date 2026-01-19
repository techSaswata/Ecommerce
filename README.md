# 🛒 E-Commerce Backend API

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                   │
│                         (Postman / Frontend App)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                           CONTROLLER LAYER                                                       │          
│  ┌───────────────┐ ┌─────────────────┐ ┌──────────────┐ ┌────────────────┐ ┌──────────────────┐  │
│  │ UserController│ │ProductController│ │CartController│ │OrderController │ │PaymentController │  │
│  └───────────────┘ └─────────────────┘ └──────────────┘ └────────────────┘ └──────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                            SERVICE LAYER                                     │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────────┐ ┌────────────┐ ┌──────────┐│
│  │ UserService │ │ProductService│ │ CartService │ │OrderService│ │PaymentSvc││
│  └─────────────┘ └──────────────┘ └─────────────┘ └────────────┘ └──────────┘│
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                      ┌───────────────┴───────────────┐
                      ▼                               ▼
┌─────────────────────────────────┐   ┌─────────────────────────────────────┐
│        REPOSITORY LAYER         │   │        EXTERNAL SERVICES            │
│  ┌──────────────────────────┐   │   │   ┌─────────────────────────────┐   │
│  │   MongoDB Repositories   │   │   │   │     Razorpay Gateway        │   │
│  │  (User, Product, Cart,   │   │   │   │   (Payment Processing)      │   │
│  │   Order, Payment)        │   │   │   └─────────────────────────────┘   │
│  └──────────────────────────┘   │   │                 │                   │
└─────────────────────────────────┘   │                 ▼                   │
                │                     │   ┌─────────────────────────────┐   │
                ▼                     │   │   Webhook Endpoint          │   │
┌─────────────────────────────────┐   │   │ /api/webhooks/payment       │   │
│           MongoDB               │   │   └─────────────────────────────┘   │
│      (Database Layer)           │   └─────────────────────────────────────┘
└─────────────────────────────────┘
```

### Component Interaction Flow

```
Client → Controllers → Services → Repositories → MongoDB
                ↓
         PaymentService → Razorpay API
                              ↓
                    Webhook → Update Order Status
```

---

## 📁 Project Structure

```
src/main/java/com/ecommerce/
│
├── EcommerceApplication.java          # Main application entry point
│
├── config/                             # Configuration classes
│   ├── WebConfig.java                  # CORS configuration
│   ├── OpenApiConfig.java              # Swagger/OpenAPI config
│   ├── MongoConfig.java                # MongoDB configuration
│   ├── RazorpayConfig.java             # Razorpay client config
│   └── DataSeeder.java                 # Sample data seeder (dev)
│
├── controller/                         # REST Controllers
│   ├── UserController.java             # User management APIs
│   ├── ProductController.java          # Product CRUD APIs
│   ├── CartController.java             # Shopping cart APIs
│   ├── OrderController.java            # Order management APIs
│   └── PaymentController.java          # Payment processing APIs
│
├── webhook/                            # Webhook handlers
│   └── PaymentWebhookController.java   # Razorpay payment webhooks
│
├── service/                            # Business logic layer
│   ├── UserService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   └── PaymentService.java
│
├── repository/                         # Data access layer
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   └── PaymentRepository.java
│
├── model/                              # Entity models
│   ├── User.java
│   ├── Product.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── Payment.java
│
├── dto/                                # Data Transfer Objects
│   ├── request/                        # Request DTOs
│   │   ├── CreateUserRequest.java
│   │   ├── CreateProductRequest.java
│   │   ├── UpdateProductRequest.java
│   │   ├── AddToCartRequest.java
│   │   ├── UpdateCartRequest.java
│   │   ├── CreateOrderRequest.java
│   │   ├── CreatePaymentRequest.java
│   │   └── VerifyPaymentRequest.java
│   └── response/                       # Response DTOs
│       ├── ApiResponse.java
│       ├── UserResponse.java
│       ├── ProductResponse.java
│       ├── CartItemResponse.java
│       ├── CartResponse.java
│       ├── OrderResponse.java
│       └── PaymentResponse.java
│
└── exception/                          # Exception handling
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    ├── PaymentException.java
    └── InsufficientStockException.java

src/main/resources/
├── application.yml                     # Application configuration
└── secrets.properties                  # API keys (DO NOT COMMIT!)
```

---

## 📊 Database Schema

### Entity Relationship Diagram

```
┌────────────────┐         ┌────────────────┐
│     USER       │         │    PRODUCT     │
├────────────────┤         ├────────────────┤
│ id (PK)        │         │ id (PK)        │
│ username       │         │ name           │
│ email          │         │ description    │
│ role           │         │ price          │
│ createdAt      │         │ stock          │
│ updatedAt      │         │ category       │
└───────┬────────┘         │ active         │
        │                  │ createdAt      │
        │                  └───────┬────────┘
        │                          │
        │    ┌─────────────────────┤
        │    │                     │
        ▼    ▼                     │
┌────────────────┐                 │
│   CART_ITEM    │                 │
├────────────────┤                 │
│ id (PK)        │                 │
│ userId (FK)────┼─────────────────┤
│ productId (FK)─┼─────────────────┘
│ quantity       │
│ createdAt      │
└────────────────┘

┌────────────────┐         ┌────────────────┐         ┌────────────────┐
│     ORDER      │         │   ORDER_ITEM   │         │    PAYMENT     │ 
├────────────────┤         ├────────────────┤         ├────────────────┤
│ id (PK)        │◄────────│ orderId (FK)   │         │ id (PK)        │
│ userId (FK)    │         │ productId      │         │ orderId (FK)───┼──►
│ totalAmount    │         │ productName    │         │ amount         │
│ status         │         │ quantity       │         │ currency       │
│ items[]        │         │ price          │         │ status         │
│ shippingAddr   │         │ subtotal       │         │ razorpayOrderId│
│ razorpayOrderId│         └────────────────┘         │ razorpayPayId  │
│ createdAt      │                                    │ createdAt      │
└────────────────┘                                    └────────────────┘
```

### Order Status Flow

```
CREATED → PENDING_PAYMENT → PAID → PROCESSING → SHIPPED → DELIVERED
                ↓
              FAILED
                ↓
            CANCELLED
```

### Payment Status Flow

```
PENDING → AUTHORIZED → CAPTURED → SUCCESS
             ↓
           FAILED
             ↓
          REFUNDED
```

---

## 📌 API Endpoints

### Base URL: `http://localhost:8080/api`

### 👤 User APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users` | Create a new user |
| GET | `/users/{userId}` | Get user by ID |
| GET | `/users/username/{username}` | Get user by username |
| GET | `/users` | Get all users |

### 📦 Product APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/products` | Create a new product |
| GET | `/products` | Get all products |
| GET | `/products/{productId}` | Get product by ID |
| GET | `/products/active` | Get active products |
| GET | `/products/category/{category}` | Get products by category |
| GET | `/products/search?q={keyword}` | Search products |
| PUT | `/products/{productId}` | Update a product |
| DELETE | `/products/{productId}` | Delete a product |

### 🛒 Cart APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/cart/add` | Add item to cart |
| GET | `/cart/{userId}` | Get user's cart |
| PUT | `/cart/{userId}/items/{productId}` | Update cart item |
| DELETE | `/cart/{userId}/items/{productId}` | Remove item from cart |
| DELETE | `/cart/{userId}/clear` | Clear user's cart |

### 📋 Order APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Create order from cart |
| GET | `/orders/{orderId}` | Get order by ID |
| GET | `/orders/user/{userId}` | Get user's order history |
| GET | `/orders` | Get all orders (Admin) |
| POST | `/orders/{orderId}/cancel` | Cancel an order |

### 💳 Payment APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payments/create` | Create payment for order |
| POST | `/payments/verify` | Verify payment signature |
| GET | `/payments/order/{orderId}` | Get payment by order ID |
| GET | `/payments/{paymentId}` | Get payment by ID |
| GET | `/payments/config` | Get Razorpay key for frontend |

### 🔔 Webhook APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/webhooks/payment` | Razorpay payment webhook |
| POST | `/webhooks/payment/mock` | Mock webhook for testing |

---

## 🔄 Order Flow Pipeline

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            ORDER PIPELINE                                    │
└──────────────────────────────────────────────────────────────────────────────┘

Step 1: BROWSE PRODUCTS
         │
         ▼
    ┌─────────────┐
    │  Products   │ ◄─── GET /api/products
    │   Catalog   │
    └─────────────┘
         │
         ▼
Step 2: ADD TO CART
         │
         ▼
    ┌─────────────┐
    │  Shopping   │ ◄─── POST /api/cart/add
    │    Cart     │
    └─────────────┘
         │
         ▼
Step 3: CHECKOUT
         │
         ▼
    ┌─────────────┐
    │   Create    │ ◄─── POST /api/orders
    │   Order     │      • Validates cart
    │             │      • Calculates total
    └─────────────┘      • Reserves stock
         │               • Clears cart
         ▼
Step 4: PAYMENT
         │
         ▼
    ┌─────────────┐
    │   Create    │ ◄─── POST /api/payments/create
    │  Razorpay   │      • Creates Razorpay order
    │   Order     │      • Returns order ID
    └─────────────┘
         │
         ▼
    ┌─────────────┐
    │  Razorpay   │ ◄─── User completes payment
    │  Checkout   │      on Razorpay page
    └─────────────┘
         │
         ▼
    ┌─────────────┐
    │   Verify    │ ◄─── POST /api/payments/verify
    │  Signature  │      OR Webhook callback
    └─────────────┘
         │
         ├─── Success ───► Order Status: PAID
         │
         └─── Failure ───► Order Status: FAILED
                           Stock: Restored
         │
         ▼
Step 5: ORDER FULFILLMENT
         │
         ▼
    ┌─────────────┐
    │  Order      │
    │  Status:    │
    │  PROCESSING │ → SHIPPED → DELIVERED
    └─────────────┘
```

---

## API Postman Collection

**Postman Collection:** [https://college-bytes-s-team.postman.co/workspace/Team-Workspace~24d4dcc0-5f16-4eab-80de-a437e617b755/request/47962954-b2039d61-4bd4-4144-83cf-b6ce2b0629e3?action=share&creator=47962954&ctx=documentation](https://college-bytes-s-team.postman.co/workspace/Team-Workspace~24d4dcc0-5f16-4eab-80de-a437e617b755/request/47962954-b2039d61-4bd4-4144-83cf-b6ce2b0629e3?action=share&creator=47962954&ctx=documentation)

**API Responses:** See `Ecommerce Backend.postman_collection.json` for complete request/response.

---

**Prepared with ❤️ by techsas**
