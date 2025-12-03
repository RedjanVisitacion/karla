import javax.swing.*;
import java.awt.*;

public class TenantFrame extends JFrame {
    public TenantFrame(User user) {
        setTitle("Tenant Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);
        JLabel label = new JLabel("Welcome Tenant: " + user.getUsername(), SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnLogout);
        add(south, BorderLayout.SOUTH);
    }
}
