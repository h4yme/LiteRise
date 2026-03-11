using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>Simple health/status endpoint – replaces api/index.php</summary>
[ApiController]
[Route("api")]
public class HealthController : ControllerBase
{
    private readonly DatabaseService _db;

    public HealthController(DatabaseService db) => _db = db;

    [HttpGet]
    [HttpGet("health")]
    public async Task<IActionResult> Health()
    {
        bool dbOk = false;
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand("SELECT 1", conn);
            await cmd.ExecuteScalarAsync();
            dbOk = true;
        }
        catch { /* db unreachable */ }

        return Ok(new
        {
            success = true,
            service = "LiteRise API",
            version = "2.0.0",
            runtime = "ASP.NET Core 8",
            database = dbOk ? "connected" : "unreachable",
            timestamp = DateTime.UtcNow.ToString("o")
        });
    }
}
