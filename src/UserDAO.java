import java.sql.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UserDAO {
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, role, password FROM users WHERE username=? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String uname = rs.getString("username");
                    String role = rs.getString("role");
                    String stored = rs.getString("password");
                    boolean ok = verifyPassword(password, stored);
                    if (ok) {
                        // Auto-upgrade plaintext passwords to hashed format
                        if (!isHashedFormat(stored)) {
                            try {
                                String newHash = hashPassword(password);
                                try (PreparedStatement up = conn.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                                    up.setString(1, newHash);
                                    up.setInt(2, id);
                                    up.executeUpdate();
                                }
                            } catch (Exception ignore) { }
                        }
                        return new User(id, uname, role);
                    }
                }
            }
        }
        return null;
    }

    public boolean createUser(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashed;
            try {
                hashed = hashPassword(password);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new SQLException("Failed to hash password", e);
            }
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, role);
            return ps.executeUpdate() == 1;
        }
    }

    // Password hashing helpers (PBKDF2 with HMAC-SHA256)
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    private static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        String sSalt = Base64.getEncoder().encodeToString(salt);
        String sHash = Base64.getEncoder().encodeToString(hash);
        return "PBKDF2$" + PBKDF2_ITERATIONS + "$" + sSalt + "$" + sHash;
    }

    private static boolean isHashedFormat(String stored) {
        return stored != null && stored.startsWith("PBKDF2$");
    }

    private static boolean verifyPassword(String password, String stored) {
        if (stored == null) return false;
        if (isHashedFormat(stored)) {
            try {
                String[] parts = stored.split("\\$");
                if (parts.length != 4) return false;
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expected.length * 8);
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] actual = skf.generateSecret(spec).getEncoded();
                return constantTimeEquals(actual, expected);
            } catch (Exception e) {
                return false;
            }
        }
        // Fallback for legacy plaintext rows
        return password.equals(stored);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int res = 0;
        for (int i = 0; i < a.length; i++) res |= a[i] ^ b[i];
        return res == 0;
    }
}
