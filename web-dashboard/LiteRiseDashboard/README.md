# LiteRise Teacher Dashboard

ASP.NET MVC web application for teachers to monitor student progress, manage content, and view analytics.

## Features

- **Student Management**: View all students, their progress, and performance metrics
- **Performance Analytics**: Charts and graphs showing student ability growth
- **Content Management**: Add, edit, and manage assessment items and lessons
- **Reports**: Generate detailed reports on student performance
- **Badge Management**: Track and award badges to students

## Setup

1. Install .NET 8 SDK
2. Update connection string in `appsettings.json`
3. Run the application:
   ```bash
   dotnet restore
   dotnet run
   ```

## Project Structure

- `/Controllers` - MVC Controllers
- `/Models` - Data models
- `/Views` - Razor views
- `/wwwroot` - Static files (CSS, JS, images)
- `/Data` - Database access layer

## Default Route

Navigate to `https://localhost:5001` to access the teacher dashboard.

## Login Credentials (Sample)

- Email: elena.torres@teacher.com
- Password: password123 (change in production!)
