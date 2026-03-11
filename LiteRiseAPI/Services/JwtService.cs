using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace LiteRiseAPI.Services;

public class JwtService
{
    private readonly IConfiguration _config;
    private readonly SymmetricSecurityKey _signingKey;

    public JwtService(IConfiguration config)
    {
        _config = config;
        var secret = config["Jwt:Secret"]
            ?? throw new InvalidOperationException("Jwt:Secret is not configured.");
        _signingKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
    }

    /// <summary>Generate a JWT token for an authenticated student.</summary>
    public string GenerateToken(int studentId, string email)
    {
        var expiryDays = int.TryParse(_config["Jwt:ExpiryDays"], out var days) ? days : 7;

        var claims = new[]
        {
            new Claim("studentID", studentId.ToString()),
            new Claim("email", email),
            new Claim(JwtRegisteredClaimNames.Sub, studentId.ToString()),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(JwtRegisteredClaimNames.Iat,
                DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString(),
                ClaimValueTypes.Integer64)
        };

        var credentials = new SigningCredentials(_signingKey, SecurityAlgorithms.HmacSha256);

        var token = new JwtSecurityToken(
            issuer: _config["Jwt:Issuer"],
            audience: _config["Jwt:Audience"],
            claims: claims,
            notBefore: DateTime.UtcNow,
            expires: DateTime.UtcNow.AddDays(expiryDays),
            signingCredentials: credentials);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    /// <summary>Validate a token and return the claims principal, or null if invalid.</summary>
    public ClaimsPrincipal? ValidateToken(string token)
    {
        var handler = new JwtSecurityTokenHandler();
        var validationParams = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = _signingKey,
            ValidateIssuer = true,
            ValidIssuer = _config["Jwt:Issuer"],
            ValidateAudience = true,
            ValidAudience = _config["Jwt:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };

        try
        {
            return handler.ValidateToken(token, validationParams, out _);
        }
        catch
        {
            return null;
        }
    }

    /// <summary>Extract the studentID claim from a validated ClaimsPrincipal.</summary>
    public static int? GetStudentId(ClaimsPrincipal principal)
    {
        var claim = principal.FindFirst("studentID") ?? principal.FindFirst(ClaimTypes.NameIdentifier);
        if (claim == null) return null;
        return int.TryParse(claim.Value, out var id) ? id : null;
    }
}
