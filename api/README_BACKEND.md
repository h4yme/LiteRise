# LiteRise Backend API Documentation

## 📋 Overview

This is the PHP backend API for the LiteRise reading application. It provides authentication, student management, and password reset functionality.

## 🚀 Setup Instructions

### 1. Database Setup

Run the SQL migration script to create necessary tables and stored procedures:

```sql
-- Execute in SQL Server Management Studio
USE [LiteRiseDB]
GO

-- Run the database_updates.sql file
-- Location: /api/database_updates.sql
```

This will create:
- `PasswordResetOTP` table for OTP management
- `Schools` table (if not exists)
- Add missing fields to `Students` table (Nickname, Birthday, Gender, SchoolID)
- Stored procedures: `SP_RegisterStudent`, `SP_CreatePasswordResetOTP`, `SP_VerifyPasswordResetOTP`, `SP_ResetPassword`

### 2. Environment Configuration

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Update `.env` with your configuration:
   ```env
   DB_SERVER=your_server_name
   DB_NAME=LiteRiseDB
   DB_USER=your_username
   DB_PASSWORD=your_password
   JWT_SECRET=your-secret-key-here
   DEBUG_MODE=false
   ```

### 3. PHP Configuration

Ensure the following PHP extensions are enabled:
- `pdo_sqlsrv` - SQL Server PDO driver
- `openssl` - For password hashing
- `mbstring` - For string operations
- `json` - For JSON handling

## 📡 API Endpoints

### Base URL
```
http://your-domain.com/api/
```

---

## 1. Registration

### `POST /api/register.php`

Register a new student account.

**Request Body:**
```json
{
  "nickname": "Leo123",
  "first_name": "John",
  "last_name": "Doe",
  "email": "student@example.com",
  "password": "password123",
  "birthday": "2015-05-15",
  "gender": "Male",
  "school_id": 1,
  "grade_level": "1"
}
```

**Required Fields:**
- `nickname` (3-50 characters)
- `first_name` (2-50 characters)
- `last_name` (2-50 characters)
- `email` (valid email format)
- `password` (minimum 6 characters)

**Optional Fields:**
- `birthday` (YYYY-MM-DD format)
- `gender` (Male, Female, or Other)
- `school_id` (integer)
- `grade_level` (1-12, default: 1)

**Success Response (201):**
```json
{
  "success": true,
  "message": "Registration successful! Welcome to LiteRise!",
  "student": {
    "StudentID": 123,
    "Nickname": "Leo123",
    "FirstName": "John",
    "LastName": "Doe",
    "FullName": "John Doe",
    "Email": "student@example.com",
    "Birthday": "2015-05-15",
    "Gender": "Male",
    "GradeLevel": 1,
    "SchoolID": 1,
    "CurrentAbility": 0.0,
    "TotalXP": 0,
    "CurrentStreak": 0,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**
- `400` - Validation errors (missing fields, invalid format)
- `409` - Email already registered
- `500` - Server error

---

## 2. Login

### `POST /api/login.php`

Authenticate a student.

**Request Body:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "StudentID": 123,
  "FullName": "John Doe",
  "FirstName": "John",
  "LastName": "Doe",
  "email": "student@example.com",
  "GradeLevel": 1,
  "CurrentAbility": 0.5,
  "TotalXP": 1200,
  "CurrentStreak": 5,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `400` - Missing fields
- `401` - Invalid credentials
- `500` - Server error

---

## 3. Forgot Password (Request OTP)

### `POST /api/forgot_password.php`

Request a password reset OTP to be sent via email.

**Request Body:**
```json
{
  "email": "student@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset code sent to your email",
  "email": "s***@example.com",
  "expires_in_minutes": 10,
  "note": "Please check your email for the 6-digit verification code"
}
```

**Error Responses:**
- `400` - Invalid email format
- `403` - Account inactive
- `500` - Server error

**Security Note:** For security, the API always returns success even if the email doesn't exist, to prevent email enumeration attacks.

---

## 4. Verify OTP

### `POST /api/verify_otp.php`

Verify the OTP code before resetting password.

**Request Body:**
```json
{
  "email": "student@example.com",
  "otp_code": "123456"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "valid": true,
  "message": "OTP verified successfully",
  "note": "You can now reset your password"
}
```

**Error Responses (400):**
```json
{
  "success": false,
  "valid": false,
  "error": "Invalid or expired OTP"
}
```

---

## 5. Reset Password

### `POST /api/reset_password.php`

Reset password using verified OTP.

**Request Body:**
```json
{
  "email": "student@example.com",
  "otp_code": "123456",
  "new_password": "newpassword123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset successfully! You can now login with your new password.",
  "note": "Please login with your new credentials"
}
```

**Error Responses:**
- `400` - Invalid OTP, expired OTP, or validation errors
- `500` - Server error

---

## 🔐 Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

The token is valid for 7 days by default and contains:
- Student ID
- Email
- Issued at timestamp
- Expiration timestamp

---

## 📧 Email Functionality

The API sends emails for:

1. **Welcome Email** - Sent after successful registration
2. **OTP Email** - Sent for password reset

### Email Templates

Both emails use professional HTML templates with:
- Responsive design
- LiteRise branding
- Clear call-to-action
- Security warnings (for OTP)

### Email Configuration

Update `.env` file:
```env
EMAIL_FROM=noreply@literise.com
EMAIL_FROM_NAME=LiteRise
```

**For Production:** Consider using a dedicated email service like:
- SendGrid
- Amazon SES
- Mailgun
- SMTP server

---

## 🔒 Security Features

### Password Security
- Passwords hashed using `bcrypt` (cost: 12)
- Minimum 6 characters (configurable)
- Never stored in plain text
- Never returned in API responses

### OTP Security
- 6-digit random codes
- 10-minute expiration
- One-time use (marked as used after verification)
- Invalidates previous OTPs when new one is requested
- Logs IP address for security auditing

### JWT Security
- HS256 algorithm
- 7-day expiration
- Contains minimal user data
- Secret key configurable via environment

### Email Enumeration Protection
- Forgot password always returns success
- Doesn't reveal if email exists
- Consistent response times

### SQL Injection Protection
- PDO prepared statements
- Parameter binding
- Input validation and sanitization

---

## 🧪 Testing

### Using Postman / Insomnia

1. **Register a new student:**
   ```
   POST http://localhost/literise/api/register.php
   Content-Type: application/json

   {
     "nickname": "TestStudent",
     "first_name": "Test",
     "last_name": "User",
     "email": "test@example.com",
     "password": "password123",
     "grade_level": "1"
   }
   ```

2. **Login:**
   ```
   POST http://localhost/literise/api/login.php
   Content-Type: application/json

   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```

3. **Request password reset:**
   ```
   POST http://localhost/literise/api/forgot_password.php
   Content-Type: application/json

   {
     "email": "test@example.com"
   }
   ```

4. **Verify OTP:**
   ```
   POST http://localhost/literise/api/verify_otp.php
   Content-Type: application/json

   {
     "email": "test@example.com",
     "otp_code": "123456"
   }
   ```

5. **Reset password:**
   ```
   POST http://localhost/literise/api/reset_password.php
   Content-Type: application/json

   {
     "email": "test@example.com",
     "otp_code": "123456",
     "new_password": "newpassword123"
   }
   ```

---

## 📊 Database Schema Changes

### Students Table - New Fields
```sql
[Nickname] NVARCHAR(50) NULL
[Birthday] DATE NULL
[Gender] NVARCHAR(20) NULL
[SchoolID] INT NULL
```

### Schools Table (New)
```sql
[SchoolID] INT IDENTITY(1,1) PRIMARY KEY
[SchoolName] NVARCHAR(200) NOT NULL
[District] NVARCHAR(100) NULL
[Address] NVARCHAR(300) NULL
[City] NVARCHAR(100) NULL
[Province] NVARCHAR(100) NULL
[IsActive] BIT DEFAULT 1
[DateCreated] DATETIME DEFAULT GETDATE()
```

### PasswordResetOTP Table (New)
```sql
[OTPID] INT IDENTITY(1,1) PRIMARY KEY
[Email] NVARCHAR(100) NOT NULL
[OTPCode] NVARCHAR(6) NOT NULL
[CreatedAt] DATETIME DEFAULT GETDATE()
[ExpiresAt] DATETIME NOT NULL
[IsUsed] BIT DEFAULT 0
[UsedAt] DATETIME NULL
[IPAddress] NVARCHAR(50) NULL
```

---

## 🐛 Troubleshooting

### Common Issues

**1. Database connection failed**
- Check SQL Server is running
- Verify connection string in `.env`
- Ensure PDO SQL Server driver is installed
- Check firewall settings

**2. Email not sending**
- Check PHP `mail()` function is configured
- Verify SMTP settings if using external service
- Check email logs in server error logs
- In DEBUG_MODE, OTP is returned in API response

**3. OTP not working**
- Check system time is correct (for expiration)
- Verify OTP hasn't been used already
- Ensure OTP is exactly 6 digits
- Check PasswordResetOTP table for records

**4. JWT token issues**
- Verify JWT_SECRET is set in `.env`
- Check token format: `Bearer <token>`
- Ensure token hasn't expired (7 days)
- Validate token signature

---

## 📝 Error Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created (Registration successful) |
| 400 | Bad Request (Validation error) |
| 401 | Unauthorized (Invalid credentials) |
| 403 | Forbidden (Account inactive) |
| 409 | Conflict (Email already exists) |
| 500 | Internal Server Error |

---

## 🔄 API Response Format

All API responses follow this format:

**Success:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "error": "Error message here"
}
```

In DEBUG_MODE, additional `details` field may be included.

---

## 📞 Support

For issues or questions:
- Check error logs in `php_error.log`
- Enable `DEBUG_MODE=true` for detailed errors
- Review database stored procedure logs
- Contact: support@literise.com

---

## 📜 License

Copyright © 2025 LiteRise. All rights reserved.
