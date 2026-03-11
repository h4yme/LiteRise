using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BCrypt.Net;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Models.Responses;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly JwtService _jwt;
    private readonly ILogger<AuthController> _logger;

    public AuthController(DatabaseService db, JwtService jwt, ILogger<AuthController> logger)
    {
        _db = db;
        _jwt = jwt;
        _logger = logger;
    }

    /// <summary>Student login – mirrors login.php</summary>
    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(
                "EXEC SP_StudentLogin @Email = @Email, @Password = @Password", conn);
            cmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            cmd.Parameters.AddWithValue("@Password", req.Password);

            await using var reader = await cmd.ExecuteReaderAsync();

            if (!await reader.ReadAsync())
            {
                _logger.LogWarning("Login failed: no student found for {Email}", req.Email);
                return Unauthorized(new ErrorResponse { Error = "Invalid email or password." });
            }

            var hashedPassword = reader["Password"]?.ToString() ?? "";
            if (!BCrypt.Net.BCrypt.Verify(req.Password, hashedPassword))
            {
                _logger.LogWarning("Login failed: password mismatch for {Email}", req.Email);
                return Unauthorized(new ErrorResponse { Error = "Invalid email or password." });
            }

            var studentId = Convert.ToInt32(reader["StudentID"]);
            var email = reader["Email"]?.ToString() ?? req.Email;
            var token = _jwt.GenerateToken(studentId, email);

            var response = new LoginResponse
            {
                StudentID = studentId,
                FullName = $"{reader["FirstName"]} {reader["LastName"]}",
                FirstName = reader["FirstName"]?.ToString() ?? "",
                LastName = reader["LastName"]?.ToString() ?? "",
                Nickname = reader["Nickname"]?.ToString() ?? "",
                Email = email,
                GradeLevel = reader["GradeLevel"] != DBNull.Value ? Convert.ToInt32(reader["GradeLevel"]) : 0,
                Section = reader["Section"]?.ToString(),
                CurrentAbility = reader["CurrentAbility"] != DBNull.Value ? Convert.ToDouble(reader["CurrentAbility"]) : 0,
                AbilityScore = reader["CurrentAbility"] != DBNull.Value ? Convert.ToDouble(reader["CurrentAbility"]) : 0,
                TotalXP = reader["TotalXP"] != DBNull.Value ? Convert.ToInt32(reader["TotalXP"]) : 0,
                XP = reader["TotalXP"] != DBNull.Value ? Convert.ToInt32(reader["TotalXP"]) : 0,
                CurrentStreak = reader["CurrentStreak"] != DBNull.Value ? Convert.ToInt32(reader["CurrentStreak"]) : 0,
                LongestStreak = reader["LongestStreak"] != DBNull.Value ? Convert.ToInt32(reader["LongestStreak"]) : 0,
                LastLogin = reader["LastLogin"]?.ToString(),
                PreAssessmentCompleted = reader["PreAssessmentCompleted"] != DBNull.Value && Convert.ToBoolean(reader["PreAssessmentCompleted"]),
                AssessmentStatus = reader["AssessmentStatus"]?.ToString() ?? "Not Started",
                Cat1_PhonicsWordStudy = reader["Cat1_PhonicsWordStudy"] != DBNull.Value ? Convert.ToInt32(reader["Cat1_PhonicsWordStudy"]) : 0,
                Cat2_VocabularyWordKnowledge = reader["Cat2_VocabularyWordKnowledge"] != DBNull.Value ? Convert.ToInt32(reader["Cat2_VocabularyWordKnowledge"]) : 0,
                Cat3_GrammarAwareness = reader["Cat3_GrammarAwareness"] != DBNull.Value ? Convert.ToInt32(reader["Cat3_GrammarAwareness"]) : 0,
                Cat4_ComprehendingText = reader["Cat4_ComprehendingText"] != DBNull.Value ? Convert.ToInt32(reader["Cat4_ComprehendingText"]) : 0,
                Cat5_CreatingComposing = reader["Cat5_CreatingComposing"] != DBNull.Value ? Convert.ToInt32(reader["Cat5_CreatingComposing"]) : 0,
                Token = token
            };

            await _db.LogActivityAsync(studentId, "Login", "User logged in successfully");
            return Ok(response);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Login error for {Email}", req.Email);
            return StatusCode(500, new ErrorResponse { Error = "Login failed." });
        }
    }

    /// <summary>Student registration – mirrors register.php</summary>
    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        if (req.GradeLevel < 1 || req.GradeLevel > 12)
            return BadRequest(new ErrorResponse { Error = "Grade level must be between 1 and 12." });

        if (req.Gender != null && !new[] { "male", "female", "other" }.Contains(req.Gender.ToLower()))
            return BadRequest(new ErrorResponse { Error = "Gender must be 'Male', 'Female', or 'Other'." });

        try
        {
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(req.Password, workFactor: 12);

            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                EXEC SP_RegisterStudent
                    @Nickname = @Nickname,
                    @FirstName = @FirstName,
                    @LastName = @LastName,
                    @Email = @Email,
                    @Password = @Password,
                    @Birthday = @Birthday,
                    @Gender = @Gender,
                    @GradeLevel = @GradeLevel,
                    @SchoolID = @SchoolID,
                    @Section = NULL", conn);

            cmd.Parameters.AddWithValue("@Nickname", req.Nickname.Trim());
            cmd.Parameters.AddWithValue("@FirstName", req.FirstName.Trim());
            cmd.Parameters.AddWithValue("@LastName", req.LastName.Trim());
            cmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            cmd.Parameters.AddWithValue("@Password", hashedPassword);
            cmd.Parameters.AddWithValue("@Birthday", (object?)req.Birthday ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@Gender", (object?)req.Gender ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@GradeLevel", req.GradeLevel);
            cmd.Parameters.AddWithValue("@SchoolID", (object?)req.SchoolId ?? DBNull.Value);

            await using var reader = await cmd.ExecuteReaderAsync();

            if (!await reader.ReadAsync())
                return BadRequest(new ErrorResponse { Error = "Registration failed." });

            var studentId = Convert.ToInt32(reader["StudentID"]);
            if (studentId == -1)
            {
                var errorMsg = reader["ErrorMessage"]?.ToString() ?? "Registration failed.";
                var status = errorMsg.Contains("already registered") ? 409 : 400;
                return StatusCode(status, new ErrorResponse { Error = errorMsg });
            }

            var email = reader["Email"]?.ToString() ?? req.Email;
            var token = _jwt.GenerateToken(studentId, email);

            var student = new StudentInfo
            {
                StudentID = studentId,
                Nickname = reader["Nickname"]?.ToString() ?? req.Nickname,
                FirstName = reader["FirstName"]?.ToString() ?? req.FirstName,
                LastName = reader["LastName"]?.ToString() ?? req.LastName,
                FullName = $"{reader["FirstName"]} {reader["LastName"]}",
                Email = email,
                Birthday = reader["Birthday"]?.ToString(),
                Gender = reader["Gender"]?.ToString(),
                GradeLevel = reader["GradeLevel"] != DBNull.Value ? Convert.ToInt32(reader["GradeLevel"]) : req.GradeLevel,
                SchoolID = reader["SchoolID"] != DBNull.Value ? Convert.ToInt32(reader["SchoolID"]) : null,
                Section = reader["Section"]?.ToString(),
                CurrentAbility = reader["CurrentAbility"] != DBNull.Value ? Convert.ToDouble(reader["CurrentAbility"]) : 0,
                AbilityScore = reader["CurrentAbility"] != DBNull.Value ? Convert.ToDouble(reader["CurrentAbility"]) : 0,
                TotalXP = 0,
                XP = 0,
                CurrentStreak = 0,
                LongestStreak = 0,
                DateCreated = reader["DateCreated"]?.ToString(),
                IsActive = reader["IsActive"] != DBNull.Value && Convert.ToBoolean(reader["IsActive"])
            };

            await _db.LogActivityAsync(studentId, "Registration", "New student registered");

            return StatusCode(201, new RegisterResponse
            {
                Message = "Registration successful! Welcome to LiteRise!",
                Student = student,
                Token = token
            });
        }
        catch (SqlException ex) when (ex.Message.Contains("UNIQUE") || ex.Message.Contains("duplicate"))
        {
            return StatusCode(409, new ErrorResponse { Error = "This email is already registered." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Registration error for {Email}", req.Email);
            return StatusCode(500, new ErrorResponse { Error = "Registration failed. Please try again." });
        }
    }

    /// <summary>Request a password-reset OTP – mirrors forgot_password.php</summary>
    [HttpPost("forgot-password")]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // Check if email exists
            await using var checkCmd = new SqlCommand(
                "SELECT StudentID FROM Students WHERE Email = @Email AND IsActive = 1", conn);
            checkCmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            var result = await checkCmd.ExecuteScalarAsync();

            // Always return success to prevent email enumeration
            if (result == null)
                return Ok(new SimpleSuccessResponse { Message = "If that email is registered, you will receive a reset code." });

            var studentId = Convert.ToInt32(result);
            var otp = new Random().Next(100000, 999999).ToString();
            var expiry = DateTime.UtcNow.AddMinutes(15);

            await using var saveCmd = new SqlCommand(@"
                UPDATE Students
                SET ResetToken = @Otp, ResetTokenExpiry = @Expiry
                WHERE StudentID = @StudentID", conn);
            saveCmd.Parameters.AddWithValue("@Otp", otp);
            saveCmd.Parameters.AddWithValue("@Expiry", expiry);
            saveCmd.Parameters.AddWithValue("@StudentID", studentId);
            await saveCmd.ExecuteNonQueryAsync();

            // TODO: send OTP via email using email service
            _logger.LogInformation("OTP {Otp} generated for {Email}", otp, req.Email);

            return Ok(new SimpleSuccessResponse { Message = "If that email is registered, you will receive a reset code." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "ForgotPassword error for {Email}", req.Email);
            return StatusCode(500, new ErrorResponse { Error = "An error occurred." });
        }
    }

    /// <summary>Verify OTP – mirrors verify_otp.php</summary>
    [HttpPost("verify-otp")]
    public async Task<IActionResult> VerifyOtp([FromBody] VerifyOtpRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT StudentID FROM Students
                WHERE Email = @Email
                  AND ResetToken = @Otp
                  AND ResetTokenExpiry > GETUTCDATE()
                  AND IsActive = 1", conn);
            cmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            cmd.Parameters.AddWithValue("@Otp", req.Otp.Trim());

            var result = await cmd.ExecuteScalarAsync();
            if (result == null)
                return BadRequest(new ErrorResponse { Error = "Invalid or expired OTP." });

            return Ok(new SimpleSuccessResponse { Message = "OTP verified successfully." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "VerifyOtp error");
            return StatusCode(500, new ErrorResponse { Error = "An error occurred." });
        }
    }

    /// <summary>Reset password – mirrors reset_password.php</summary>
    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // Validate token
            await using var validateCmd = new SqlCommand(@"
                SELECT StudentID FROM Students
                WHERE Email = @Email
                  AND ResetToken = @Token
                  AND ResetTokenExpiry > GETUTCDATE()
                  AND IsActive = 1", conn);
            validateCmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            validateCmd.Parameters.AddWithValue("@Token", req.Token.Trim());

            var result = await validateCmd.ExecuteScalarAsync();
            if (result == null)
                return BadRequest(new ErrorResponse { Error = "Invalid or expired reset token." });

            var studentId = Convert.ToInt32(result);
            var newHash = BCrypt.Net.BCrypt.HashPassword(req.NewPassword, workFactor: 12);

            await using var updateCmd = new SqlCommand(@"
                UPDATE Students
                SET Password = @Password,
                    ResetToken = NULL,
                    ResetTokenExpiry = NULL
                WHERE StudentID = @StudentID", conn);
            updateCmd.Parameters.AddWithValue("@Password", newHash);
            updateCmd.Parameters.AddWithValue("@StudentID", studentId);
            await updateCmd.ExecuteNonQueryAsync();

            return Ok(new SimpleSuccessResponse { Message = "Password reset successfully." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "ResetPassword error");
            return StatusCode(500, new ErrorResponse { Error = "An error occurred." });
        }
    }

    /// <summary>Portal login for admin/teacher accounts – mirrors portal_login.php</summary>
    [HttpPost("portal-login")]
    public async Task<IActionResult> PortalLogin([FromBody] PortalLoginRequest req)
    {
        if (!ModelState.IsValid)
            return BadRequest(new ErrorResponse { Error = "Invalid request data." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            var role = req.Role.ToLower() == "teacher" ? "teacher" : "admin";
            var spName = role == "admin" ? "SP_AdminLogin" : "SP_TeacherLogin";
            await using var cmd = new SqlCommand(
                $"EXEC {spName} @Email = @Email, @Password = @Password", conn);
            cmd.Parameters.AddWithValue("@Email", req.Email.Trim());
            cmd.Parameters.AddWithValue("@Password", req.Password);

            await using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync())
                return Unauthorized(new ErrorResponse { Error = "Invalid email or password." });

            var userId   = Convert.ToInt32(reader["UserID"]);
            var fullName = reader["FullName"]?.ToString() ?? "";
            await reader.CloseAsync();

            var token = _jwt.GenerateToken(userId, req.Email);

            // Fetch extra fields needed by the portal
            int? schoolId = null;
            if (role == "teacher")
            {
                await using var extraCmd = new SqlCommand(
                    "SELECT SchoolID FROM Teachers WHERE TeacherID = @ID", conn);
                extraCmd.Parameters.AddWithValue("@ID", userId);
                var sid = await extraCmd.ExecuteScalarAsync();
                if (sid != null && sid != DBNull.Value) schoolId = Convert.ToInt32(sid);
            }

            return Ok(new
            {
                success  = true,
                user_id  = userId,
                name     = fullName,
                email    = req.Email.Trim(),
                role,
                school_id = schoolId,
                token,
                message  = "Login successful."
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "PortalLogin error for {Email}", req.Email);
            return StatusCode(500, new ErrorResponse { Error = "Login failed." });
        }
    }
}
