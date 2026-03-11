using Microsoft.Data.SqlClient;

namespace LiteRiseAPI.Services;

/// <summary>
/// Provides ADO.NET connections to the Azure SQL database.
/// Uses <see cref="SqlConnection"/> (Microsoft.Data.SqlClient) – the same driver
/// recommended for Azure SQL.
/// </summary>
public class DatabaseService
{
    private readonly string _connectionString;

    public DatabaseService(IConfiguration config)
    {
        _connectionString = config.GetConnectionString("DefaultConnection")
            ?? throw new InvalidOperationException("DefaultConnection is not configured.");
    }

    /// <summary>Open and return a new SQL connection (caller must dispose).</summary>
    public SqlConnection GetConnection()
    {
        var conn = new SqlConnection(_connectionString);
        conn.Open();
        return conn;
    }

    /// <summary>Open and return a new SQL connection asynchronously (caller must dispose).</summary>
    public async Task<SqlConnection> GetConnectionAsync()
    {
        var conn = new SqlConnection(_connectionString);
        await conn.OpenAsync();
        return conn;
    }

    /// <summary>
    /// Insert an activity-log row. Failures are swallowed and logged so that
    /// a logging failure never breaks the main request.
    /// </summary>
    public async Task LogActivityAsync(int studentId, string activityType, string details = "")
    {
        try
        {
            await using var conn = await GetConnectionAsync();
            await using var cmd = new SqlCommand(
                "INSERT INTO ActivityLog (StudentID, ActivityType, ActivityDetails) VALUES (@s, @t, @d)",
                conn);
            cmd.Parameters.AddWithValue("@s", studentId);
            cmd.Parameters.AddWithValue("@t", activityType);
            cmd.Parameters.AddWithValue("@d", details);
            await cmd.ExecuteNonQueryAsync();
        }
        catch (Exception ex)
        {
            // Non-critical – log but don't throw
            Console.Error.WriteLine($"[LogActivity] {ex.Message}");
        }
    }
}
