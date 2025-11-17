# 🚀 LiteRise Quick Start Guide

Get your LiteRise system up and running in **30 minutes**!

---

## 📋 What You'll Build

By the end of this guide, you'll have:
- ✅ SQL Server database with sample data
- ✅ PHP Backend API running and tested
- ✅ Android app connected and functional
- ✅ Complete login → assessment → results flow working

---

## ⏱️ Step 1: Database Setup (5 minutes)

### Using SQL Server Management Studio (SSMS):

1. **Open SSMS** and connect to your server
2. **Open** the file: `database/schema.sql`
3. **Execute** (Press F5 or click Execute)
4. **Verify**:
   ```sql
   USE LiteRiseDB;
   SELECT COUNT(*) FROM Students;  -- Should show 3
   SELECT COUNT(*) FROM Items;     -- Should show 11
   ```

### Using Command Line:

```bash
sqlcmd -S localhost -U sa -P YourPassword123 -i database/schema.sql
```

✅ **You should see**: "Database schema created successfully!"

---

## ⏱️ Step 2: PHP Backend Setup (10 minutes)

### A. Install PHP SQL Server Drivers

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install php php-cli php-pdo
sudo pecl install sqlsrv pdo_sqlsrv

# Enable extensions
echo "extension=sqlsrv.so" | sudo tee -a /etc/php/8.1/cli/php.ini
echo "extension=pdo_sqlsrv.so" | sudo tee -a /etc/php/8.1/cli/php.ini
```

**Windows:**
1. Download drivers from: https://docs.microsoft.com/en-us/sql/connect/php/
2. Place DLLs in `C:\php\ext`
3. Add to `php.ini`:
   ```ini
   extension=php_pdo_sqlsrv_81_ts.dll
   extension=php_sqlsrv_81_ts.dll
   ```

### B. Configure Database Connection

1. **Edit** `htdocs/api/src/db.php`:
   ```php
   $this->host = 'localhost';        // Your SQL Server address
   $this->db_name = 'LiteRiseDB';
   $this->username = 'sa';            // Your SQL Server username
   $this->password = 'YourPassword123'; // Your SQL Server password
   ```

2. **Copy** environment template:
   ```bash
   cd htdocs/api
   cp .env.example .env
   nano .env  # Edit with your credentials
   ```

### C. Deploy API Files

**Apache (Linux):**
```bash
sudo cp -r htdocs/api /var/www/html/
sudo chown -R www-data:www-data /var/www/html/api
sudo systemctl restart apache2
```

**Windows (XAMPP):**
```bash
# Copy htdocs/api folder to C:\xampp\htdocs\
# Restart Apache from XAMPP Control Panel
```

### D. Test API

```bash
# Test database connection
curl http://localhost/api/test_db.php

# Should return:
# {"status":"success","message":"Database connection successful", ...}
```

✅ **If you see "success"**, your API is working!

---

## ⏱️ Step 3: Android App Setup (10 minutes)

### A. Open Project in Android Studio

1. **Launch** Android Studio
2. **Open** → Navigate to `LiteRise` folder
3. **Wait** for Gradle sync to complete

### B. Configure API URL

1. Open `app/src/main/java/com/example/literise/utils/Constants.java`
2. Update BASE_URL:
   ```java
   public static final String BASE_URL = "http://YOUR_IP_HERE/api/";
   ```

**Find your IP:**
- Windows: `ipconfig` → Look for IPv4 Address
- Linux/Mac: `ifconfig` or `ip addr`
- Example: `http://192.168.1.100/api/`

⚠️ **Important**: Use your computer's IP, not `localhost` (unless using emulator on same machine)

### C. Build and Run

1. **Connect** your Android device via USB (or start an emulator)
2. **Enable** USB Debugging on your device:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable USB Debugging
3. **Click** Run (▶️) in Android Studio
4. **Select** your device from the list

---

## ⏱️ Step 4: Test Complete Flow (5 minutes)

### Test Login

1. **Launch** the app
2. **Enter** credentials:
   - Email: `maria.santos@student.com`
   - Password: `password123`
3. **Tap** "Let's Read!"

✅ **Expected**: Toast message "Welcome Maria Santos!" and navigate to assessment

### Test Assessment

1. **Answer** a few questions
2. **Tap** "Continue" after each answer
3. **Complete** all 20 questions (or skip to end)
4. **See** results with:
   - Final Ability Score (θ)
   - Accuracy percentage
   - Number correct/total

✅ **Expected**: Assessment submits successfully, ability score calculated

---

## 🧪 Automated Testing

### Run API Test Script

```bash
chmod +x test_api.sh
./test_api.sh
```

This will test:
1. Database connection
2. Student login
3. Session creation
4. Question retrieval
5. Response submission
6. Progress tracking
7. Lesson recommendations

✅ **Expected**: All 7 tests pass with green checkmarks

---

## 🎯 What's Working Now

After completing this guide, you have:

### ✅ Backend API (100%)
- Student authentication
- Session management
- Item retrieval (20 adaptive questions)
- Response submission with IRT
- Ability calculation (θ estimation)
- Progress tracking

### ✅ Android App (85%)
- Login screen
- Pre-assessment flow
- Question display
- Answer submission
- Time tracking
- Results display

### ✅ Database (100%)
- All tables created
- Sample data loaded
- Stored procedures working
- Indexes applied

---

## 📱 Testing Different Accounts

Try these sample accounts:

**Students:**
| Email | Password | Grade | Description |
|-------|----------|-------|-------------|
| maria.santos@student.com | password123 | 4 | Default test account |
| juan.delacruz@student.com | password123 | 5 | Grade 5 student |
| ana.reyes@student.com | password123 | 6 | Grade 6 student |

**Teachers:**
| Email | Password | Role |
|-------|----------|------|
| elena.torres@teacher.com | password123 | Elementary |
| carlos.mendoza@teacher.com | password123 | Elementary |

---

## 🔧 Troubleshooting

### API Returns "Database connection failed"

**Check**:
```bash
# Verify SQL Server is running
sqlcmd -S localhost -U sa -P YourPassword123

# Test query
sqlcmd -S localhost -U sa -P YourPassword123 -Q "SELECT @@VERSION"
```

**Fix**: Update credentials in `htdocs/api/src/db.php`

### Android App Shows "Connection error"

**Check**:
1. Phone and computer on same Wi-Fi network
2. Firewall allows port 80/443
3. API URL uses computer's IP, not `localhost`
4. Test API from phone's browser: `http://YOUR_IP/api/test_db.php`

**Fix**: Update `Constants.BASE_URL` with correct IP

### "No questions available"

**Check**:
```sql
USE LiteRiseDB;
SELECT COUNT(*) FROM Items WHERE IsActive = 1;
```

**Fix**: Run `database/schema.sql` again to insert sample data

### App crashes on assessment

**Check** Android Studio Logcat for errors

**Common fixes**:
- Clear app data: Settings → Apps → LiteRise → Clear Data
- Rebuild project: Build → Clean Project → Rebuild Project

---

## 📈 Next Steps

Now that your core system is working, you can:

1. **Add More Questions**: Insert items into database
2. **Build Dashboard**: Complete `MainActivity.java` with stats
3. **Implement Games**: Sentence Scramble & Timed Trail
4. **Teacher Dashboard**: Set up ASP.NET web portal
5. **Customize**: Change colors, branding, content

---

## 💡 Understanding the Flow

Here's what happens when a student takes an assessment:

```
1. Login
   ↓
   API: /login.php → Returns StudentID, FullName, Current θ
   ↓
2. Create Session
   ↓
   API: /create_session.php → Returns SessionID, Initial θ
   ↓
3. Get Questions
   ↓
   API: /get_preassessment_items.php → Returns 20 items
   ↓
4. Student Answers
   ↓
   App: Tracks time, correctness for each response
   ↓
5. Submit All Responses
   ↓
   API: /submit_responses.php →
     • Calculates Final θ using IRT (Newton-Raphson MLE)
     • Updates database
     • Returns accuracy, reliability, θ change
   ↓
6. Show Results
   ↓
   App: Displays score, ability level, recommendations
```

---

## 🎓 Understanding IRT Results

When you see results like:
- **θ = 0.5**: Slightly above average ability
- **Accuracy = 75%**: Answered 75% correctly
- **Reliability = 0.85**: High confidence in measurement

**Theta (θ) Scale:**
- **-2.0 to -1.0**: Beginner
- **-1.0 to 0.5**: Developing
- **0.5 to 1.5**: Intermediate (grade-appropriate)
- **1.5 to 2.5**: Advanced
- **> 2.5**: Expert

---

## 📞 Getting Help

If you get stuck:

1. **Check Logs**:
   - PHP: `/var/log/apache2/error.log` (Linux)
   - Android: Android Studio → Logcat
   - SQL: SQL Server Error Logs

2. **Test Each Component**:
   - Database: `sqlcmd -S localhost -U sa -P YourPassword123`
   - API: `curl http://localhost/api/test_db.php`
   - App: Check Logcat for connection errors

3. **Review Documentation**:
   - `README.md` - Full project overview
   - `SETUP_GUIDE.md` - Detailed installation
   - `htdocs/api/README.md` - API documentation

---

## ✅ Success Checklist

- [ ] Database created with sample data
- [ ] API test returns `"status":"success"`
- [ ] Android app connects to API
- [ ] Can login with test account
- [ ] Assessment loads 20 questions
- [ ] Can submit responses
- [ ] Results show ability score
- [ ] Automated test script passes all tests

---

**Congratulations! 🎉 Your LiteRise system is now operational!**

You have a working adaptive literacy assessment platform with:
- IRT-based ability measurement
- Real-time theta calculations
- Multi-tier architecture (Android + PHP + SQL Server)
- Sample data for testing

**Estimated setup time**: 20-30 minutes
**Status**: Core system functional, ready for expansion

---

**Happy coding! 🚀**
