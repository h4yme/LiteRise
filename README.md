# 📚 LiteRise - Adaptive Literacy Assessment Platform

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Web-green.svg)
![Status](https://img.shields.io/badge/status-Active-success.svg)

LiteRise is a comprehensive adaptive literacy assessment and learning platform for elementary students (Grades 4-6) in the Philippines. It uses Item Response Theory (IRT) to provide personalized learning experiences and accurate ability measurements.

---

## 🎯 Project Overview

### Key Features

#### 📱 **Student Mobile App (Android)**
- **Adaptive Pre-Assessment**: IRT-based placement testing
- **Personalized Lessons**: Content matched to student ability level
- **Gamification**:
  - Sentence Scramble (Syntax practice)
  - Timed Trail (Speed reading & comprehension)
- **Real-time Progress Tracking**: XP, streaks, and badges
- **Multi-skill Assessment**: Reading, Grammar, Pronunciation, Spelling, Syntax

#### 🖥️ **Teacher Dashboard (ASP.NET)**
- Student performance monitoring
- Analytics and reporting
- Content management (questions, lessons)
- Badge and achievement tracking

#### 🔧 **REST API Backend (PHP)**
- Secure authentication
- IRT calculations (3-parameter logistic model)
- Session management
- Real-time ability updates

#### 💾 **SQL Server Database**
- Comprehensive schema with stored procedures
- Performance-optimized indexes
- Sample data for testing

---

## 📂 Project Structure

```
LiteRise/
├── htdocs/api/                    # 🔌 PHP Backend API
│   ├── src/
│   │   └── db.php                 # Database connection handler
│   ├── irt.php                    # IRT calculation engine
│   ├── login.php                  # Student authentication
│   ├── create_session.php         # Test session creation
│   ├── get_preassessment_items.php  # Fetch assessment questions
│   ├── submit_responses.php       # Process and save responses
│   ├── update_ability.php         # Update student theta
│   ├── get_student_progress.php   # Fetch progress data
│   ├── get_lessons.php            # Fetch personalized lessons
│   ├── get_game_data.php          # Fetch game questions
│   ├── save_game_result.php       # Save game scores
│   ├── test_db.php                # Database connectivity test
│   ├── .htaccess                  # Apache configuration
│   └── .env.example               # Environment variables template
│
├── app/                           # 📱 Android Application
│   ├── src/main/java/com/example/literise/
│   │   ├── activities/            # UI screens
│   │   │   ├── SplashActivity.java
│   │   │   ├── LoginActivity.java
│   │   │   ├── PreAssessmentActivity.java
│   │   │   └── MainActivity.java  # Dashboard (to be completed)
│   │   ├── api/                   # Retrofit API client
│   │   │   ├── ApiClient.java
│   │   │   └── ApiService.java
│   │   ├── models/                # Data models
│   │   │   ├── Students.java
│   │   │   ├── Question.java
│   │   │   ├── ResponseModel.java
│   │   │   └── SubmitRequest.java
│   │   ├── utils/                 # Utilities
│   │   │   ├── IRTCalculator.java  # Client-side IRT
│   │   │   ├── Constants.java      # App configuration
│   │   │   └── CustomToast.java    # Custom notifications
│   │   ├── database/
│   │   │   └── SessionManager.java # Session persistence
│   │   └── adapters/              # RecyclerView adapters
│   └── res/                       # Resources (layouts, drawables, etc.)
│
├── web-dashboard/                 # 🖥️ ASP.NET Teacher Dashboard
│   └── LiteRiseDashboard/
│       ├── Controllers/
│       │   └── DashboardController.cs
│       ├── Models/
│       │   └── DashboardViewModel.cs
│       ├── Views/
│       ├── wwwroot/               # Static files
│       ├── Program.cs
│       ├── appsettings.json
│       └── LiteRiseDashboard.csproj
│
└── database/
    └── schema.sql                 # Complete database schema
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (2023.1 or later) for mobile app
- **PHP 8.0+** with PDO SQL Server extension
- **SQL Server 2019+** or Azure SQL Database
- **.NET 8 SDK** for teacher dashboard
- **Apache/Nginx** web server

---

## 📦 Installation

### 1️⃣ Database Setup

```sql
-- Run the schema creation script
sqlcmd -S your_server -U sa -P your_password -i database/schema.sql
```

Or use SQL Server Management Studio to execute `database/schema.sql`.

### 2️⃣ PHP Backend Setup

```bash
# Navigate to API directory
cd htdocs/api

# Copy environment file
cp .env.example .env

# Edit .env with your database credentials
nano .env

# Set proper permissions
chmod 644 .env
chmod 755 *.php

# Test database connection
curl http://your-server/api/test_db.php
```

**Update `src/db.php` with your connection details:**
```php
$this->host = 'your_server';
$this->db_name = 'LiteRiseDB';
$this->username = 'sa';
$this->password = 'your_password';
```

### 3️⃣ Android App Setup

```bash
# Open project in Android Studio
# Update API base URL in Constants.java
# File: app/src/main/java/com/example/literise/utils/Constants.java

public static final String BASE_URL = "http://your-server-ip/api/";

# Build and run
./gradlew assembleDebug
# or use Android Studio's Run button
```

### 4️⃣ Teacher Dashboard Setup

```bash
cd web-dashboard/LiteRiseDashboard

# Restore dependencies
dotnet restore

# Update connection string in appsettings.json
nano appsettings.json

# Run the application
dotnet run

# Navigate to https://localhost:5001
```

---

## 🧪 Testing

### Test Accounts

**Students:**
- Email: `maria.santos@student.com`
- Email: `juan.delacruz@student.com`
- Email: `ana.reyes@student.com`
- Password: `password123` (for all)

**Teachers:**
- Email: `elena.torres@teacher.com`
- Email: `carlos.mendoza@teacher.com`
- Password: `password123` (for all)

⚠️ **Note**: Change these passwords in production!

---

## 🔬 IRT Implementation

### 3-Parameter Logistic (3PL) Model

```
P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))
```

**Parameters:**
- **θ (theta)**: Student ability (-4 to +4 scale)
- **a**: Item discrimination (how well it differentiates ability levels)
- **b**: Item difficulty
- **c**: Guessing parameter (pseudo-chance level)

### Ability Estimation

- **Method**: Maximum Likelihood Estimation (MLE) via Newton-Raphson
- **Iterations**: Max 20 with convergence tolerance of 0.001
- **Alternative**: Expected A Posteriori (EAP) for extreme scores

### Adaptive Testing

Items are selected using the **Maximum Information Criterion**:
- Next item = item that provides most information at current θ estimate
- Ensures efficient and accurate ability measurement

---

## 📊 API Documentation

### Base URL
```
http://your-server/api/
```

### Endpoints

#### Authentication
```http
POST /login.php
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "password123"
}

Response:
{
  "StudentID": 1,
  "FullName": "Maria Santos",
  "email": "maria.santos@student.com",
  "GradeLevel": 4,
  "AbilityScore": 0.0,
  "XP": 0,
  "CurrentStreak": 0
}
```

#### Create Session
```http
POST /create_session.php

{
  "StudentID": 1,
  "SessionType": "PreAssessment"
}

Response:
{
  "SessionID": 1,
  "StudentID": 1,
  "SessionType": "PreAssessment",
  "InitialTheta": 0.0,
  "StartTime": "2024-01-01T10:00:00"
}
```

#### Get Assessment Items
```http
POST /get_preassessment_items.php

Response: Array of 20 questions
[
  {
    "ItemID": 1,
    "QuestionText": "She _ to school every day.",
    "OptionA": "go",
    "OptionB": "goes",
    "OptionC": "going",
    "OptionD": "",
    "CorrectOption": "B",
    "Difficulty": -0.7,
    "Discrimination": 1.4
  }
]
```

#### Submit Responses
```http
POST /submit_responses.php

{
  "StudentID": 1,
  "SessionID": 1,
  "Responses": [
    {
      "ItemID": 1,
      "SelectedOption": "B",
      "Correct": true,
      "TimeTakenSec": 15.5
    }
  ]
}

Response:
{
  "success": true,
  "SessionID": 1,
  "FinalTheta": 0.234,
  "InitialTheta": 0.0,
  "ThetaChange": 0.234,
  "Accuracy": 85.5,
  "Reliability": 0.89
}
```

[View complete API documentation](htdocs/api/README.md)

---

## 🎮 Game Modules

### 1. Sentence Scramble
- **Type**: Syntax practice
- **Mechanic**: Arrange scrambled words to form correct sentences
- **XP Rewards**: Based on speed and accuracy

### 2. Timed Trail
- **Type**: Speed reading & comprehension
- **Mechanic**: Answer as many questions correctly within time limit
- **Includes**: Spelling, Grammar, Pronunciation items

---

## 🏆 Gamification System

### XP (Experience Points)
- Base XP per correct answer: **10 XP**
- Accuracy bonus (≥90%): **+50 XP**
- Accuracy bonus (≥75%): **+25 XP**
- Speed bonus (< 30s): **+30 XP**
- Speed bonus (< 60s): **+15 XP**

### Badges
1. **First Steps**: Complete first pre-assessment
2. **Syntax Master**: Perfect 10 Sentence Scrambles
3. **Clear Speaker**: 95%+ pronunciation accuracy on 20 words
4. **Word Master**: 15 correct word unscrambles
5. **Speed Reader**: 3 Timed Trails under 45 seconds
6. **Streak Champion**: 10-question correct streak
7. **Fluency Pro**: Reach θ > 2.0

---

## 🔒 Security Features

- Password hashing with bcrypt (PHP: `password_hash()`)
- SQL injection prevention (parameterized queries)
- CORS configuration for API access
- Session timeout (30 minutes)
- HTTPS recommended for production
- Input validation on all endpoints

---

## 📈 Performance Optimizations

### Database
- Indexed columns: StudentID, Email, SessionID, ItemID
- Stored procedures for complex queries
- Connection pooling in PHP

### API
- Response caching where appropriate
- Gzip compression enabled
- 30-second timeouts

### Android
- Retrofit with OkHttp for efficient networking
- RecyclerView for large lists
- ViewBinding for type-safe views

---

## 🛠️ Development Roadmap

### ✅ Completed
- [x] Database schema with IRT parameters
- [x] PHP backend API with IRT implementation
- [x] Android login and pre-assessment
- [x] Session management
- [x] Response submission and ability update
- [x] Basic teacher dashboard structure

### 🚧 In Progress
- [ ] Android MainActivity dashboard
- [ ] Game modules (Sentence Scramble, Timed Trail)
- [ ] Pronunciation assessment with speech recognition

### 📅 Planned
- [ ] Lesson delivery system
- [ ] Teacher dashboard analytics
- [ ] Parent/guardian portal
- [ ] Offline mode with sync
- [ ] Multi-language support (Filipino, English)
- [ ] Advanced reporting and exports

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Development Team** - Initial work

---

## 🙏 Acknowledgments

- Item Response Theory implementation based on academic research
- Material Design guidelines for Android UI
- PHP PDO SQL Server documentation
- ASP.NET Core MVC framework

---

## 📞 Support

For support, email support@literise.ph or create an issue in this repository.

---

## 📚 Documentation

- [API Documentation](htdocs/api/README.md)
- [Android App Guide](app/README.md)
- [Teacher Dashboard Guide](web-dashboard/LiteRiseDashboard/README.md)
- [Database Schema](database/README.md)

---

**Built with ❤️ for Filipino learners**
