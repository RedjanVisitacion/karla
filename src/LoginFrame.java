import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbMessage;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 260);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        JLabel lbu = new JLabel("Username"); lbu.setFont(mainFont);
        tfUsername = new JTextField(); tfUsername.setFont(mainFont);
        JLabel lbp = new JLabel("Password"); lbp.setFont(mainFont);
        pfPassword = new JPasswordField(); pfPassword.setFont(mainFont);
        form.add(lbu); form.add(tfUsername); form.add(lbp); form.add(pfPassword);

        JButton btnLogin = new JButton("Login"); btnLogin.setFont(mainFont);
        btnLogin.addActionListener(this::handleLogin);
        JButton btnCreate = new JButton("Create Account"); btnCreate.setFont(mainFont);
        btnCreate.addActionListener(e -> openRegister());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCreate);
        buttons.add(btnLogin);

        lbMessage = new JLabel(" ", SwingConstants.CENTER); lbMessage.setForeground(Color.RED);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        root.add(lbMessage, BorderLayout.NORTH);

        setContentPane(root);
    }

    private void handleLogin(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            lbMessage.setText("Please enter username and password.");
            return;
        }
        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                lbMessage.setText("Invalid credentials.");
                return;
            }
            lbMessage.setText(" ");
            routeByRole(user);
        } catch (Exception ex) {
            ex.printStackTrace();
            lbMessage.setText("Error: " + ex.getMessage());
        }
    }

    private void routeByRole(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            if ("admin".equalsIgnoreCase(user.getRole())) {
                new AdminFrame(user).setVisible(true);
            } else {
                new TenantFrame(user).setVisible(true);
            }
        });
    }

    private void openRegister() {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}
