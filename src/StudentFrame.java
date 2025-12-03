import javax.swing.*;
import java.awt.*;

public class StudentFrame extends JFrame {
    public StudentFrame(User user) {
        setTitle("Tenant Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);
        JLabel label = new JLabel("Welcome Tenant: " + user.getUsername(), SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);
    }
}
