<?php
/**
 * LiteRise Database Connection Handler
 * SQL Server Connection using PDO
 */

class Database {
    private $host;
    private $db_name;
    private $username;
    private $password;
    private $conn;

    public function __construct() {
        // Load from environment or use defaults
        $this->host = getenv('DB_HOST') ?: '10.248.215.210';
        $this->db_name = getenv('DB_NAME') ?: 'LiteRiseDB';
        $this->username = getenv('DB_USER') ?: 'sa';
        $this->password = getenv('DB_PASS') ?: 'YourPassword123';
    }

    /**
     * Get database connection
     * @return PDO|null
     */
    public function getConnection() {
        $this->conn = null;

        try {
            // SQL Server connection string
            $dsn = "sqlsrv:Server={$this->host};Database={$this->db_name}";

            $this->conn = new PDO($dsn, $this->username, $this->password);
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->conn->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

        } catch(PDOException $e) {
            error_log("Connection error: " . $e->getMessage());
            return null;
        }

        return $this->conn;
    }

    /**
     * Execute stored procedure
     * @param string $procedureName
     * @param array $params
     * @return array|bool
     */
    public function executeStoredProcedure($procedureName, $params = []) {
        try {
            $conn = $this->getConnection();
            if (!$conn) {
                return false;
            }

            // Build parameter placeholders
            $placeholders = [];
            foreach ($params as $key => $value) {
                $placeholders[] = "@{$key} = :{$key}";
            }
            $paramString = implode(', ', $placeholders);

            // Prepare and execute
            $sql = "EXEC {$procedureName} {$paramString}";
            $stmt = $conn->prepare($sql);

            foreach ($params as $key => &$value) {
                $stmt->bindParam(":{$key}", $value);
            }

            $stmt->execute();
            return $stmt->fetchAll();

        } catch (PDOException $e) {
            error_log("Stored procedure error: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Execute query directly
     * @param string $sql
     * @param array $params
     * @return array|bool
     */
    public function query($sql, $params = []) {
        try {
            $conn = $this->getConnection();
            if (!$conn) {
                return false;
            }

            $stmt = $conn->prepare($sql);
            $stmt->execute($params);
            return $stmt->fetchAll();

        } catch (PDOException $e) {
            error_log("Query error: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Close connection
     */
    public function closeConnection() {
        $this->conn = null;
    }
}
