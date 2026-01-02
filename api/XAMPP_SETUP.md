# LiteRise API - XAMPP Setup Guide

## 📋 Prerequisites

- XAMPP installed (with Apache and MySQL/MariaDB)
- SQL Server Express (for LiteRise database)
- PHP SQL Server drivers installed

---

## 🚀 Quick Setup Steps

### 1. Copy API Files to XAMPP

Copy the entire `api` folder to your XAMPP `htdocs` directory:

```
C:\xampp\htdocs\api\
├── src/
│   ├── db.php
│   ├── auth.php
│   ├── email.php
│   └── item_formatter.php
├── register.php
├── login.php
├── forgot_password.php
├── verify_otp.php
├── reset_password.php
├── .env.example
└── (other files...)
```

### 2. Configure Environment

1. Copy `.env.example` to `.env`:
   ```
   C:\xampp\htdocs\api\.env
   ```

2. Edit `.env` with your database credentials:
   ```env
   DB_SERVER=DESKTOP-PEM6F9E\SQLEXPRESS
   DB_NAME=LiteRiseDB
   DB_USER=sa
   DB_PASSWORD=p@ssw0rd
   JWT_SECRET=your-secret-key-here
   DEBUG_MODE=true
   ```

### 3. Install PHP SQL Server Drivers

1. Download the Microsoft SQL Server driver for PHP:
   - Visit: https://docs.microsoft.com/en-us/sql/connect/php/download-drivers-php-sql-server
   - Download the appropriate version for your PHP version

2. Extract and copy `.dll` files to `C:\xampp\php\ext\`:
   - `php_sqlsrv_XX_ts.dll`
   - `php_pdo_sqlsrv_XX_ts.dll`

3. Edit `C:\xampp\php\php.ini` and add:
   ```ini
   extension=php_sqlsrv_XX_ts.dll
   extension=php_pdo_sqlsrv_XX_ts.dll
   ```

4. Restart Apache in XAMPP Control Panel

### 4. Run Database Migration

Open SQL Server Management Studio and execute:

```sql
-- Run the database_updates.sql file
-- Location: C:\xampp\htdocs\api\database_updates.sql
```

This will create:
- `PasswordResetOTP` table
- `Schools` table
- Add missing fields to `Students` table
- Create stored procedures

### 5. Verify Setup

1. **Test File Access:**

   Open in browser: `http://localhost/api/test_direct.php`

   You should see:
   - ✓ All files exist
   - ✓ All functions load
   - API endpoint URLs

2. **Test Database Connection:**

   Open in browser: `http://localhost/api/test_db.php`

   You should see: "✅ Database connection successful"

---

## 🧪 Testing the APIs

### Method 1: Using Browser (GET test only)

Visit: `http://localhost/api/test_direct.php`

### Method 2: Using Postman

1. **Test Registration:**
   ```
   POST http://localhost/api/register.php
   Content-Type: application/json

   {
     "nickname": "TestUser",
     "first_name": "Test",
     "last_name": "Student",
     "email": "test@example.com",
     "password": "password123",
     "grade_level": "1"
   }
   ```

2. **Test Login:**
   ```
   POST http://localhost/api/login.php
   Content-Type: application/json

   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```

3. **Test Forgot Password:**
   ```
   POST http://localhost/api/forgot_password.php
   Content-Type: application/json

   {
     "email": "test@example.com"
   }
   ```

### Method 3: Using Command Line

**Option A: Using curl (if installed):**
```bash
curl -X POST http://localhost/api/register.php ^
  -H "Content-Type: application/json" ^
  -d "{\"nickname\":\"TestUser\",\"first_name\":\"Test\",\"last_name\":\"Student\",\"email\":\"test@example.com\",\"password\":\"password123\",\"grade_level\":\"1\"}"
```

**Option B: Using PowerShell:**
```powershell
$body = @{
    nickname = "TestUser"
    first_name = "Test"
    last_name = "Student"
    email = "test@example.com"
    password = "password123"
    grade_level = "1"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost/api/register.php" -Method POST -Body $body -ContentType "application/json"
```

---

## 🔧 Troubleshooting

### Issue 1: 404 Error

**Symptoms:** API returns 404 Not Found

**Solutions:**
1. Verify files are in `C:\xampp\htdocs\api\`
2. Check Apache is running in XAMPP
3. Test direct file access: `http://localhost/api/test_direct.php`
4. Check the URL matches your folder structure

### Issue 2: Database Connection Failed

**Symptoms:** "Database connection failed" error

**Solutions:**
1. Verify SQL Server is running
2. Check SQL Server allows remote connections:
   - Open SQL Server Configuration Manager
   - Enable TCP/IP protocol
   - Restart SQL Server service
3. Test connection with `test_db.php`
4. Verify credentials in `.env` file
5. Check PHP SQL Server drivers are installed:
   ```
   php -m | findstr sqlsrv
   ```

### Issue 3: Functions Not Found

**Symptoms:** "Call to undefined function"

**Solutions:**
1. Verify `src/email.php` exists and is readable
2. Check file permissions (should be readable)
3. Clear PHP opcode cache (restart Apache)
4. Test with `test_direct.php`

### Issue 4: Email Not Sending

**Symptoms:** OTP emails not received

**Solutions:**
1. Check `DEBUG_MODE=true` in `.env` - OTP will show in response
2. Configure SMTP in `php.ini`:
   ```ini
   [mail function]
   SMTP = smtp.gmail.com
   smtp_port = 587
   sendmail_from = your-email@gmail.com
   ```
3. Or use a mail service (SendGrid, Mailgun)
4. For testing, check the API response for `debug_otp` field

### Issue 5: CORS Errors (from Android/Web)

**Symptoms:** "Access-Control-Allow-Origin" error

**Solutions:**
1. Headers are already set in `src/db.php`
2. Ensure Apache mod_headers is enabled
3. Restart Apache

---

## 📊 Verify Installation Checklist

- [ ] XAMPP Apache running
- [ ] SQL Server running
- [ ] API files in `C:\xampp\htdocs\api\`
- [ ] `.env` file configured
- [ ] PHP SQL Server drivers installed
- [ ] Database migration executed
- [ ] `test_direct.php` shows all green ✓
- [ ] `test_db.php` connects successfully
- [ ] Postman test returns valid response

---

## 🌐 API Endpoints

Once setup is complete, your APIs will be available at:

| Endpoint | URL |
|----------|-----|
| Registration | `http://localhost/api/register.php` |
| Login | `http://localhost/api/login.php` |
| Forgot Password | `http://localhost/api/forgot_password.php` |
| Verify OTP | `http://localhost/api/verify_otp.php` |
| Reset Password | `http://localhost/api/reset_password.php` |

---

## 📱 Android App Configuration

Update your Android app's `ApiClient.java`:

```java
public class ApiClient {
    // For emulator testing:
    private static final String BASE_URL = "http://10.0.2.2/api/";

    // For physical device on same network:
    // private static final String BASE_URL = "http://192.168.1.XXX/api/";

    // For production:
    // private static final String BASE_URL = "https://yourdomain.com/api/";
}
```

**Note:** Use `10.0.2.2` instead of `localhost` when testing from Android emulator.

---

## 🔒 Security Notes

1. **For Development:**
   - `DEBUG_MODE=true` is okay
   - Use localhost only

2. **For Production:**
   - Set `DEBUG_MODE=false`
   - Change `JWT_SECRET` to a strong random key
   - Use HTTPS
   - Restrict database access
   - Enable firewall rules
   - Remove test files

---

## 📞 Need Help?

1. Check `test_direct.php` output
2. Check Apache error logs: `C:\xampp\apache\logs\error.log`
3. Check PHP error logs: `C:\xampp\php\logs\php_error_log`
4. Enable debug mode in `.env`

---

## ✅ Success Indicators

You'll know everything is working when:

1. ✅ `http://localhost/api/test_direct.php` shows all green checkmarks
2. ✅ Postman POST to `/register.php` returns `{"success": true, ...}`
3. ✅ Database shows new student record
4. ✅ Android app can register and login

---

Good luck! 🚀
