# 🚀 LiteRise Complete Setup Guide

Complete step-by-step guide to set up the LiteRise adaptive literacy assessment system.

## 📋 Table of Contents

1. [System Requirements](#system-requirements)
2. [Database Setup](#database-setup)
3. [API Setup (XAMPP)](#api-setup-xampp)
4. [Android App Setup](#android-app-setup)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)

---

## 1. System Requirements

### Required Software

- **SQL Server 2019+** (Developer or Express Edition)
- **XAMPP 8.0+** (PHP 7.4+ with Apache)
- **Android Studio** (latest version)
- **Microsoft SQL Server PDO Driver for PHP**

### Operating System
- Windows 10/11 (recommended)
- macOS or Linux (with SQL Server Docker)

---

## 2. Database Setup

### Step 1: Install SQL Server

1. Download SQL Server 2019 Developer Edition:
   - https://www.microsoft.com/en-us/sql-server/sql-server-downloads

2. Install with default settings:
   - Mixed Mode Authentication
   - Remember your `sa` password (e.g., `p@ssw0rd`)

3. Install SQL Server Management Studio (SSMS):
   - https://docs.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms

### Step 2: Create Database

1. Open SSMS and connect to your server:
   ```
   Server: DESKTOP-PEM6F9E\SQLEXPRESS (or your instance name)
   Authentication: SQL Server Authentication
   Login: sa
   Password: p@ssw0rd
   ```

2. Open a New Query window

3. Copy and paste the entire `database/schema.sql` file

4. Execute the script (F5)

5. Verify database creation:
   ```sql
   USE LiteRiseDB;
   GO

   SELECT COUNT(*) FROM Students; -- Should return 3
   SELECT COUNT(*) FROM Items;    -- Should return 13
   SELECT COUNT(*) FROM Badges;   -- Should return 7
   ```

### Step 3: Create Test Student (Optional)

If you want to test with a specific account:

```sql
USE LiteRiseDB;
GO

-- Password: "password123"
INSERT INTO Students (FirstName, LastName, Email, Password, GradeLevel, Section)
VALUES ('Test', 'Student', 'test@student.com',
        '$2y$12$LQv3c1yqBWVHxkd0LHAkCOYz6TfZ.q8fTZ7wSp.kZP9Hq.hJpG6Fu', 5, 'A');
```

---

## 3. API Setup (XAMPP)

### Step 1: Install XAMPP

1. Download XAMPP from:
   - https://www.apachefriends.org/

2. Install to default location: `C:\xampp`

3. Start Apache from XAMPP Control Panel

### Step 2: Install SQL Server PDO Driver

1. Download Microsoft Drivers for PHP for SQL Server:
   - https://docs.microsoft.com/en-us/sql/connect/php/download-drivers-php-sql-server

2. Extract and copy these files to `C:\xampp\php\ext\`:
   ```
   php_pdo_sqlsrv_74_ts_x64.dll
   php_sqlsrv_74_ts_x64.dll
   ```
   (Note: Choose files matching your PHP version)

3. Edit `C:\xampp\php\php.ini` and add these lines:
   ```ini
   extension=php_pdo_sqlsrv_74_ts_x64.dll
   extension=php_sqlsrv_74_ts_x64.dll
   ```

4. Restart Apache from XAMPP Control Panel

5. Verify installation:
   ```bash
   # Open browser: http://localhost/dashboard/phpinfo.php
   # Search for "sqlsrv" - you should see the extension loaded
   ```

### Step 3: Copy API Files

1. Copy the API directory:
   ```bash
   # From your project:
   cp -r htdocs/api C:/xampp/htdocs/api
   ```

2. Update `.env` file with your database credentials:
   ```bash
   cd C:/xampp/htdocs/api
   notepad .env
   ```

   Update these values:
   ```env
   DB_SERVER=DESKTOP-PEM6F9E\SQLEXPRESS
   DB_NAME=LiteRiseDB
   DB_USER=sa
   DB_PASSWORD=p@ssw0rd

   JWT_SECRET=your_secret_random_string_here
   ```

3. Set permissions (Windows):
   - Right-click `api` folder → Properties
   - Security tab → Edit → Add `IUSR` and `IIS_IUSRS` with Read & Execute permissions

### Step 4: Test API

1. Open browser and visit:
   ```
   http://localhost/api/test_db.php
   ```

2. You should see:
   ```json
   {
     "success": true,
     "message": "Database connection successful",
     "tests": {
       "connection": "✅ Connected",
       "students_table": "✅ 3 students found"
     }
   }
   ```

3. Test login endpoint:
   ```bash
   curl -X POST http://localhost/api/login.php \
     -H "Content-Type: application/json" \
     -d "{\"email\":\"maria.santos@student.com\",\"password\":\"password123\"}"
   ```

---

## 4. Android App Setup

### Step 1: Open Project in Android Studio

1. Open Android Studio

2. File → Open → Select the `LiteRise` folder

3. Wait for Gradle sync to complete

### Step 2: Configure API URL

1. Get your computer's IP address:

   **Windows:**
   ```cmd
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)

   **Mac/Linux:**
   ```bash
   ifconfig | grep "inet "
   ```

2. Update `ApiClient.java`:
   ```java
   // For Android Emulator (localhost):
   private static final String BASE_URL = "http://10.0.2.2/api/";

   // For Physical Device (use your PC's IP):
   private static final String BASE_URL = "http://192.168.1.100/api/";
   ```

3. If using physical device, make sure:
   - Phone and PC are on the same Wi-Fi network
   - Windows Firewall allows Apache (port 80)

### Step 3: Build and Run

1. Connect Android device or start emulator

2. Click Run (green play button)

3. Select your device

4. Wait for app to install and launch

---

## 5. Testing

### Test Login

1. Open app

2. Use test credentials:
   ```
   Email: maria.santos@student.com
   Password: password123
   ```

3. Should see "Welcome Maria Santos!" message

4. Should navigate to PreAssessment screen

### Test Pre-Assessment

1. After login, you should see assessment questions

2. Answer all questions

3. Submit responses

4. Check that ability score is calculated

### Verify Data in Database

```sql
USE LiteRiseDB;
GO

-- Check sessions
SELECT * FROM TestSessions ORDER BY StartTime DESC;

-- Check responses
SELECT * FROM Responses ORDER BY Timestamp DESC;

-- Check updated ability
SELECT StudentID, FirstName, LastName, CurrentAbility
FROM Students
WHERE Email = 'maria.santos@student.com';
```

---

## 6. Troubleshooting

### Problem: "Database connection failed"

**Solution:**
1. Verify SQL Server is running:
   ```
   services.msc → SQL Server (SQLEXPRESS) → Start
   ```

2. Check credentials in `.env` file

3. Test connection in SSMS first

4. Check firewall allows SQL Server (port 1433)

### Problem: "Call to undefined function sqlsrv_connect"

**Solution:**
1. PDO drivers not installed or not enabled
2. Re-check Step 2 in API Setup
3. Verify in `phpinfo()` that sqlsrv extensions are loaded
4. Restart Apache

### Problem: "Connection refused" in Android app

**Solution:**

**For Emulator:**
- Use `http://10.0.2.2/api/` (not `localhost`)

**For Physical Device:**
- Check PC and phone on same Wi-Fi
- Use PC's IP address: `http://192.168.1.100/api/`
- Check Windows Firewall:
  ```
  Control Panel → Firewall → Allow app → Apache HTTP Server
  ```

### Problem: "Invalid credentials" on login

**Solution:**
1. Check database has sample students:
   ```sql
   SELECT * FROM Students;
   ```

2. Password hashing issue - Update student password:
   ```sql
   -- Password: "password123"
   UPDATE Students
   SET Password = '$2y$12$LQv3c1yqBWVHxkd0LHAkCOYz6TfZ.q8fTZ7wSp.kZP9Hq.hJpG6Fu'
   WHERE Email = 'maria.santos@student.com';
   ```

3. Check password in login.php is being verified correctly

### Problem: "Stored procedure not found"

**Solution:**
1. Re-run the database schema script
2. Verify procedures exist:
   ```sql
   SELECT * FROM sys.procedures WHERE name LIKE 'SP_%';
   ```

### Problem: App crashes on launch

**Solution:**
1. Check Logcat in Android Studio
2. Common issues:
   - Network permissions in AndroidManifest.xml
   - Cleartext traffic not allowed (add network security config)
   - API URL format incorrect

### Problem: JWT token expired

**Solution:**
1. Token expires after 7 days
2. User needs to login again
3. Or increase token expiry in `auth.php`:
   ```php
   $expire = $issuedAt + (60 * 60 * 24 * 30); // 30 days
   ```

---

## 📞 Additional Help

### Useful Commands

**Check PHP version:**
```bash
php -v
```

**Check Apache status:**
```bash
# XAMPP Control Panel → Apache → Status
```

**View Apache error logs:**
```
C:\xampp\apache\logs\error.log
```

**View PHP error logs:**
```
C:\xampp\php\logs\php_error_log.txt
```

### Testing Tools

**Postman:**
- Import API collection
- Test all endpoints
- Download: https://www.postman.com/

**cURL:**
- Command-line API testing
- Included in Windows 10/11

**Android Device Monitor:**
- View app logs
- Android Studio → View → Tool Windows → Logcat

---

## 🎉 Next Steps

After successful setup:

1. ✅ Test all API endpoints
2. ✅ Complete a full assessment flow
3. ✅ Check IRT calculations are working
4. ✅ Test game modules
5. ✅ Review security settings before production
6. ✅ Create additional test accounts
7. ✅ Customize UI/UX as needed

---

## 📝 Notes

- Always use HTTPS in production
- Change JWT_SECRET before deploying
- Never commit `.env` file to Git
- Regularly backup the database
- Monitor API logs for errors

---

**Happy coding! 🚀**
