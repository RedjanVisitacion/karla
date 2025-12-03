import java.sql.*;

public class DBUtil {
    // TODO: Set your MySQL credentials here
    private static final String URL = "jdbc:mysql://localhost:3306/boarding_house_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    static {
        // Load driver and ensure schema exists
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            ensureSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void ensureSchema() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "role ENUM('admin','tenant') NOT NULL DEFAULT 'tenant', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String seedAdminCheck = "SELECT COUNT(*) FROM users WHERE username=?";
        String seedAdminInsert = "INSERT INTO users (username, password, role) VALUES (?,?,?)";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(createUsers);

            // Ensure existing schema has the correct ENUM values (admin, tenant)
            try (Statement alterSt = conn.createStatement()) {
                alterSt.executeUpdate("ALTER TABLE users MODIFY role ENUM('admin','tenant') NOT NULL DEFAULT 'tenant'");
            } catch (SQLException ignore) {
                // If it already matches, MySQL may throw an error; safe to ignore
            }

            // seed default admin if missing (username: Karla, password: bhms_00;)
            try (PreparedStatement ps = conn.prepareStatement(seedAdminCheck)) {
                ps.setString(1, "Karla");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement ins = conn.prepareStatement(seedAdminInsert)) {
                            ins.setString(1, "Karla");
                            ins.setString(2, "bhms_00;");
                            ins.setString(3, "admin");
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
