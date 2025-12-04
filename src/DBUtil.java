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

        String createRooms = "CREATE TABLE IF NOT EXISTS rooms (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "room_number VARCHAR(20) NOT NULL UNIQUE, " +
                "capacity INT NOT NULL DEFAULT 4, " +
                "status ENUM('available','occupied') NOT NULL DEFAULT 'available', " +
                "assigned_user_id INT NULL, " +
                "CONSTRAINT fk_rooms_user FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String createAssignments = "CREATE TABLE IF NOT EXISTS room_assignments (" +
                "room_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (room_id, user_id), " +
                "UNIQUE KEY uq_user_id (user_id), " +
                "CONSTRAINT fk_ra_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String createPayments = "CREATE TABLE IF NOT EXISTS payments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "month_key CHAR(7) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL DEFAULT 0, " +
                "status ENUM('paid','unpaid') NOT NULL DEFAULT 'unpaid', " +
                "paid_at TIMESTAMP NULL DEFAULT NULL, " +
                "UNIQUE KEY uq_user_month (user_id, month_key), " +
                "CONSTRAINT fk_pay_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String seedAdminCheck = "SELECT COUNT(*) FROM users WHERE username=?";
        String seedAdminInsert = "INSERT INTO users (username, password, role) VALUES (?,?,?)";
        String seedRoomsCount = "SELECT COUNT(*) FROM rooms";
        String seedRoomsInsert = "INSERT INTO rooms (room_number, capacity, status) VALUES (?,?, 'available')";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(createUsers);
            st.executeUpdate(createRooms);
            st.executeUpdate(createAssignments);
            st.executeUpdate(createPayments);

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

            // seed some rooms if none exist
            try (Statement cs = conn.createStatement(); ResultSet rsc = cs.executeQuery(seedRoomsCount)) {
                if (rsc.next() && rsc.getInt(1) == 0) {
                    try (PreparedStatement insRoom = conn.prepareStatement(seedRoomsInsert)) {
                        for (int i = 1; i <= 20; i++) {
                            insRoom.setString(1, "Room " + i);
                            insRoom.setInt(2, (i == 16 ? 2 : 4));
                            insRoom.addBatch();
                        }
                        insRoom.executeBatch();
                    }
                }
            }

            // normalize capacities for existing rooms according to rules
            try (Statement norm = conn.createStatement()) {
                norm.executeUpdate("UPDATE rooms SET capacity=2 WHERE room_number='Room 16' AND capacity<>2");
                norm.executeUpdate("UPDATE rooms SET capacity=4 WHERE room_number<>'Room 16' AND capacity<>4");
            } catch (SQLException ignore) { }

            // migrate legacy single assignment to room_assignments
            try (Statement sm = conn.createStatement();
                 ResultSet rs = sm.executeQuery("SELECT id, assigned_user_id FROM rooms WHERE assigned_user_id IS NOT NULL")) {
                while (rs.next()) {
                    int roomId = rs.getInt(1);
                    int userId = rs.getInt(2);
                    try (PreparedStatement ins = conn.prepareStatement("INSERT IGNORE INTO room_assignments (room_id, user_id) VALUES (?,?)")) {
                        ins.setInt(1, roomId);
                        ins.setInt(2, userId);
                        ins.executeUpdate();
                    }
                }
                // clear legacy column to avoid double-source of truth
                try (Statement clr = conn.createStatement()) {
                    clr.executeUpdate("UPDATE rooms SET assigned_user_id=NULL WHERE assigned_user_id IS NOT NULL");
                }
            } catch (SQLException ignore) { }

            // recalc room statuses by occupancy vs capacity
            try (Statement rsu = conn.createStatement()) {
                rsu.executeUpdate(
                        "UPDATE rooms r SET r.status = CASE " +
                                "WHEN (SELECT COUNT(*) FROM room_assignments ra WHERE ra.room_id=r.id) >= r.capacity THEN 'occupied' " +
                                "ELSE 'available' END");
            } catch (SQLException ignore) { }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
