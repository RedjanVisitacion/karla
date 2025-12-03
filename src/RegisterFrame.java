import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JPasswordField pfConfirm;
    private JLabel lbMessage;
    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame() {
        setTitle("Create Account");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridLayout(6, 1, 8, 8));
        JLabel lbu = new JLabel("Enter Username"); lbu.setFont(mainFont);
        tfUsername = new JTextField(); tfUsername.setFont(mainFont);
        JLabel lbp = new JLabel("Enter Password"); lbp.setFont(mainFont);
        pfPassword = new JPasswordField(); pfPassword.setFont(mainFont);
        JLabel lbc = new JLabel("Confirm your password"); lbc.setFont(mainFont);
        pfConfirm = new JPasswordField(); pfConfirm.setFont(mainFont);
        form.add(lbu); form.add(tfUsername);
        form.add(lbp); form.add(pfPassword);
        form.add(lbc); form.add(pfConfirm);

        JButton btnRegister = new JButton("Create Account"); btnRegister.setFont(mainFont);
        btnRegister.addActionListener(this::handleRegister);
        JButton btnCancel = new JButton("Cancel"); btnCancel.setFont(mainFont);
        btnCancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel); buttons.add(btnRegister);

        lbMessage = new JLabel(" ", SwingConstants.CENTER);
        lbMessage.setForeground(Color.RED);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        root.add(lbMessage, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void handleRegister(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        String confirm = new String(pfConfirm.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lbMessage.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            lbMessage.setText("Passwords do not match.");
            return;
        }
        try {
            boolean ok = userDAO.createUser(username, password, "tenant");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Account created. You can login now.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                lbMessage.setText("Could not create user.");
            }
        } catch (Exception ex) {
            // Handle duplicate username or other SQL errors
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("duplicate")) {
                lbMessage.setText("Username already exists.");
            } else {
                lbMessage.setText("Error: " + msg);
            }
        }
    }
}
