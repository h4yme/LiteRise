# 🚀 LiteRise Complete Setup Guide

This guide will walk you through setting up the entire LiteRise ecosystem from scratch.

---

## 📋 Prerequisites Checklist

Before you begin, ensure you have:

- [ ] **SQL Server 2019+** (or Azure SQL Database)
- [ ] **PHP 8.0+** with extensions:
  - [ ] PDO
  - [ ] PDO_SQLSRV (SQL Server driver)
  - [ ] JSON
  - [ ] OpenSSL
- [ ] **Apache/Nginx** web server
- [ ] **Android Studio** (2023.1 or later)
- [ ] **JDK 17** (for Android development)
- [ ] **.NET 8 SDK** (for teacher dashboard)
- [ ] **Git** for version control

---

## 🗄️ Part 1: Database Setup

### Step 1: Install SQL Server

**Option A: Windows**
```bash
# Download SQL Server 2019 Developer Edition (free)
# https://www.microsoft.com/en-us/sql-server/sql-server-downloads

# Run installer and follow wizard
# Set authentication to Mixed Mode
# Remember your SA password!
```

**Option B: Docker**
```bash
docker pull mcr.microsoft.com/mssql/server:2019-latest
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong@Password" \
   -p 1433:1433 --name literise-sql \
   -d mcr.microsoft.com/mssql/server:2019-latest
```

### Step 2: Create Database

**Using SQL Server Management Studio (SSMS):**
1. Open SSMS
2. Connect to your server
3. Click `File > Open > File`
4. Navigate to `database/schema.sql`
5. Click `Execute` (F5)

**Using sqlcmd:**
```bash
sqlcmd -S localhost -U sa -P YourPassword123 -i database/schema.sql
```

### Step 3: Verify Installation

```sql
USE LiteRiseDB;
GO

-- Check tables
SELECT name FROM sys.tables;

-- Check sample data
SELECT COUNT(*) FROM Students;
SELECT COUNT(*) FROM Items;
SELECT COUNT(*) FROM Badges;
```

You should see:
- 3 students
- ~11 items
- 7 badges

---

## 🔧 Part 2: PHP Backend Setup

### Step 1: Install PHP and Extensions

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install php8.1 php8.1-cli php8.1-common php8.1-curl \
                 php8.1-mbstring php8.1-xml php8.1-json

# Install SQL Server drivers
sudo pecl install sqlsrv pdo_sqlsrv

# Enable extensions
echo "extension=sqlsrv.so" | sudo tee -a /etc/php/8.1/cli/php.ini
echo "extension=pdo_sqlsrv.so" | sudo tee -a /etc/php/8.1/cli/php.ini
```

**Windows:**
1. Download PHP 8.1+ from https://windows.php.net/download/
2. Extract to `C:\php`
3. Download SQL Server drivers: https://docs.microsoft.com/en-us/sql/connect/php/download-drivers-php-sql-server
4. Place DLL files in `C:\php\ext`
5. Edit `php.ini`:
   ```ini
   extension=php_pdo_sqlsrv_81_ts.dll
   extension=php_sqlsrv_81_ts.dll
   ```

### Step 2: Configure Web Server

**Apache (.htaccess already included):**
```bash
sudo apt install apache2
sudo a2enmod rewrite
sudo systemctl restart apache2

# Copy API files to web root
sudo cp -r htdocs/api /var/www/html/
sudo chown -R www-data:www-data /var/www/html/api
sudo chmod 755 /var/www/html/api
```

**Nginx:**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/html;
    index index.php index.html;

    location /api/ {
        try_files $uri $uri/ /api/index.php?$query_string;
    }

    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php8.1-fpm.sock;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        include fastcgi_params;
    }
}
```

### Step 3: Configure Environment

```bash
cd htdocs/api
cp .env.example .env
nano .env
```

Update with your values:
```bash
DB_HOST=localhost
DB_NAME=LiteRiseDB
DB_USER=sa
DB_PASS=YourPassword123
```

### Step 4: Set Permissions

```bash
chmod 644 .env
chmod 755 *.php
chmod 755 src/
chmod 644 src/*.php
```

### Step 5: Test API

```bash
# Test database connection
curl http://localhost/api/test_db.php

# Expected output:
{
  "status": "success",
  "message": "Database connection successful",
  "database": "LiteRiseDB",
  "statistics": {
    "TotalStudents": 3,
    "TotalItems": 11
  }
}
```

---

## 📱 Part 3: Android App Setup

### Step 1: Install Android Studio

1. Download from https://developer.android.com/studio
2. Run installer
3. Open Android Studio Setup Wizard
4. Install:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device

### Step 2: Open Project

```bash
# Clone or navigate to project
cd LiteRise

# Open in Android Studio
# File > Open > Select LiteRise folder
```

Wait for Gradle sync to complete.

### Step 3: Configure API URL

Edit `app/src/main/java/com/example/literise/utils/Constants.java`:

```java
// Change to your server IP
public static final String BASE_URL = "http://192.168.1.100/api/";
```

**Finding your server IP:**
- Windows: `ipconfig`
- Linux/Mac: `ifconfig` or `ip addr`

### Step 4: Update Network Security Config

Already configured in `app/src/main/res/xml/network_security_config.xml`:
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### Step 5: Build and Run

**Option A: Physical Device**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click Run (▶️) in Android Studio

**Option B: Emulator**
1. Click AVD Manager
2. Create Virtual Device
3. Select Pixel 4 (or similar)
4. Download System Image (API 31+)
5. Click Run

### Step 6: Test Login

Use test account:
- Email: `maria.santos@student.com`
- Password: `password123`

---

## 🖥️ Part 4: Teacher Dashboard Setup

### Step 1: Install .NET SDK

**Windows:**
```bash
# Download from https://dotnet.microsoft.com/download/dotnet/8.0
# Run installer
```

**Ubuntu/Debian:**
```bash
wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb
sudo apt update
sudo apt install -y dotnet-sdk-8.0
```

**Verify installation:**
```bash
dotnet --version
# Should show: 8.0.x
```

### Step 2: Configure Connection String

Edit `web-dashboard/LiteRiseDashboard/appsettings.json`:

```json
{
  "ConnectionStrings": {
    "LiteRiseDB": "Server=localhost;Database=LiteRiseDB;User Id=sa;Password=YourPassword123;TrustServerCertificate=True;"
  }
}
```

### Step 3: Restore Dependencies

```bash
cd web-dashboard/LiteRiseDashboard
dotnet restore
```

### Step 4: Run Application

```bash
dotnet run

# Or for hot reload during development:
dotnet watch run
```

### Step 5: Access Dashboard

Open browser:
```
https://localhost:5001
```

Login with teacher account:
- Email: `elena.torres@teacher.com`
- Password: `password123`

---

## 🧪 Part 5: Testing the Complete System

### End-to-End Test Flow

1. **Student Login (Android App)**
   - Open app
   - Login as `maria.santos@student.com`
   - Verify successful authentication

2. **Take Pre-Assessment**
   - Start placement test
   - Answer 20 questions
   - Submit responses
   - Verify ability score is calculated

3. **Check Progress (Teacher Dashboard)**
   - Login to web dashboard
   - Navigate to Students section
   - Find Maria Santos
   - Verify session appears with ability score

4. **Play Game (Android App)**
   - Select Sentence Scramble or Timed Trail
   - Complete game
   - Verify XP and streaks update

5. **View Analytics (Teacher Dashboard)**
   - Check student performance graphs
   - View ability growth over time
   - Export reports

---

## 🔒 Part 6: Security Hardening

### Production Checklist

- [ ] Change all default passwords
- [ ] Use `password_hash()` for new accounts
- [ ] Enable HTTPS (Let's Encrypt)
- [ ] Configure firewall rules
- [ ] Disable PHP error display
- [ ] Set up regular database backups
- [ ] Implement rate limiting on API
- [ ] Use environment variables for secrets
- [ ] Enable SQL Server encryption
- [ ] Set up monitoring and logging

### Update Passwords

```sql
-- Update student passwords (use proper hashing in production)
UPDATE Students
SET Password = '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE Email = 'maria.santos@student.com';
```

In PHP:
```php
$hashedPassword = password_hash('new_password', PASSWORD_BCRYPT);
```

---

## 🐛 Troubleshooting

### API Issues

**Problem:** "Database connection failed"
```bash
# Check SQL Server is running
sudo systemctl status mssql-server  # Linux
# Or check Services in Windows

# Test connection
sqlcmd -S localhost -U sa -P YourPassword123 -Q "SELECT @@VERSION"
```

**Problem:** "Call to undefined function sqlsrv_connect()"
```bash
# Install SQL Server drivers
sudo pecl install sqlsrv pdo_sqlsrv
php -m | grep sqlsrv  # Should show sqlsrv and pdo_sqlsrv
```

### Android App Issues

**Problem:** "Unable to connect to server"
- Check if server IP is correct
- Ping server: `ping 192.168.1.100`
- Verify firewall allows port 80/443
- Check `network_security_config.xml` allows cleartext traffic
- Ensure phone and server are on same network

**Problem:** "Build failed: SDK not found"
```bash
# In Android Studio:
Tools > SDK Manager
# Install:
- Android SDK Platform 34
- Android SDK Build-Tools
```

### Teacher Dashboard Issues

**Problem:** "Unable to connect to database"
```bash
# Test connection string
dotnet run --urls="http://localhost:5000"

# Check SQL Server allows remote connections
# SQL Server Configuration Manager > SQL Server Network Configuration
# > Protocols for MSSQLSERVER > TCP/IP > Enabled
```

---

## 📊 Sample Data Guide

### Adding More Students

```sql
INSERT INTO Students (FirstName, LastName, Email, Password, GradeLevel, Section)
VALUES ('Pedro', 'Garcia', 'pedro.garcia@student.com', 'password123', 4, 'B');
```

### Adding Assessment Items

```sql
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam,
                   DiscriminationParam, GuessingParam, CorrectAnswer,
                   AnswerChoices, GradeLevel)
VALUES ('The cat _ on the mat.', 'Grammar', 'Easy', -0.5, 1.3, 0.33, 'B',
        '["sit", "sits", "sitting"]', 4);
```

---

## 📞 Getting Help

- **Documentation**: Check README.md
- **API Reference**: htdocs/api/README.md
- **Database Schema**: database/schema.sql (has detailed comments)
- **Issues**: Create GitHub issue with:
  - Error message
  - Steps to reproduce
  - System information

---

## ✅ Post-Setup Checklist

- [ ] Database created and populated
- [ ] PHP API responding to requests
- [ ] Android app connects and authenticates
- [ ] Teacher dashboard loads student data
- [ ] Test account can login and take assessment
- [ ] IRT calculations working (theta updates)
- [ ] Games functional
- [ ] Badges system working
- [ ] All passwords changed from defaults

---

**Congratulations! 🎉 LiteRise is now fully set up!**

Next steps:
- Customize branding and colors
- Add more assessment items
- Invite teachers and students
- Monitor system performance
- Schedule regular backups
