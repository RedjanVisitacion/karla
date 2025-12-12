import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 16);
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JPasswordField pfConfirm;
    private JLabel lbMessage;
    private final UserDAO userDAO = new UserDAO();
    private ImageIcon eyeIcon;
    private ImageIcon hiddenIcon;

    public RegisterFrame() {
        setTitle("Create Account");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(loadAppIcon());
        eyeIcon = loadIcon("assets/img/eye.png", 18, 18);
        hiddenIcon = loadIcon("assets/img/hidden.png", 18, 18);
        buildUI();
    }

    private void buildUI() {
        JPanel left = createWelcomePanel();
        JPanel right = createRegisterCard();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, wrapCenter(right));
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setDividerSize(0);
        setContentPane(split);
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
        if (!isStrongPassword(username, password)) {
            lbMessage.setText("Password too weak. Use at least 8 characters with uppercase, lowercase, and a number.");
            return;
        }
        try {
            boolean ok = userDAO.createUser(username, password, "tenant");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Account created. You can login now.", "Success", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.invokeLater(() -> {
                    JFrame f = new LoginFrame();
                    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    showSingleWindow(f);
                });
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

    private boolean isStrongPassword(String username, String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        String lower = password.toLowerCase();
        if (lower.equals("password") || lower.equals("123456") || lower.equals("12345678") || lower.equals("qwerty") || lower.equals("abc123") || lower.equals("111111")) return false;
        if (username != null && !username.isEmpty() && username.equalsIgnoreCase(password)) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(33, 150, 243);
                Color c2 = new Color(13, 71, 161);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        Image iconImg = loadAppIcon();
        JLabel iconLabel = null;
        if (iconImg != null) {
            Image scaled = iconImg.getScaledInstance(72, 72, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaled));
        }

        JLabel title = new JLabel("Create your account");
        title.setForeground(Color.WHITE);
        title.setFont(titleFont);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Boarding House Management System");
        subtitle.setForeground(new Color(230, 240, 255));
        subtitle.setFont(subtitleFont);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;
        if (iconLabel != null) {
            gc.insets = new Insets(0, 0, 16, 0);
            panel.add(iconLabel, gc);
            gc.gridy++;
        }
        gc.insets = new Insets(0, 0, 12, 0);
        panel.add(title, gc);
        gc.gridy = gc.gridy + 1; gc.insets = new Insets(0, 0, 0, 0);
        panel.add(subtitle, gc);
        return panel;
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setOpaque(false);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(220, 225, 230), 16),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        inner.setBackground(Color.WHITE);
        inner.setOpaque(true);

        JLabel header = new JLabel("Create Account");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lbu = new JLabel("Enter Username"); lbu.setFont(mainFont);
        tfUsername = new JTextField(); tfUsername.setFont(mainFont);
        tfUsername.setPreferredSize(new Dimension(360, 36));

        JLabel lbp = new JLabel("Enter Password"); lbp.setFont(mainFont);
        pfPassword = new JPasswordField(); pfPassword.setFont(mainFont);
        pfPassword.setPreferredSize(new Dimension(360, 36));

        JLabel lbc = new JLabel("Confirm your password"); lbc.setFont(mainFont);
        pfConfirm = new JPasswordField(); pfConfirm.setFont(mainFont);
        pfConfirm.setPreferredSize(new Dimension(360, 36));

        JPanel pwPanel = createPasswordWithToggle(pfPassword);
        JPanel cfPanel = createPasswordWithToggle(pfConfirm);

        lbMessage = new JLabel(" ", SwingConstants.LEFT);
        lbMessage.setForeground(Color.RED);

        JButton btnRegister = new JButton("Create Account"); btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRegister.setBackground(new Color(33, 150, 243));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(200, 40));
        btnRegister.addActionListener(this::handleRegister);

        JButton btnLogin = new JButton("Login"); btnLogin.setFont(mainFont);
        btnLogin.addActionListener(e -> {
            JFrame f = new LoginFrame();
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            showSingleWindow(f);
        });

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 0, 16, 0);
        inner.add(header, g);
        g.gridy = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 6, 0);
        inner.add(lbu, g);
        g.gridy = 2; g.weightx = 1.0; g.insets = new Insets(0, 0, 12, 0);
        inner.add(tfUsername, g);
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0); g.weightx = 0;
        inner.add(lbp, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0); g.weightx = 1.0;
        inner.add(pwPanel, g);
        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0); g.weightx = 0;
        inner.add(lbc, g);
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0); g.weightx = 1.0;
        inner.add(cfPanel, g);
        g.gridy = 7; g.gridwidth = 2; g.insets = new Insets(0, 0, 8, 0);
        inner.add(lbMessage, g);
        g.gridy = 8; g.gridwidth = 1; g.insets = new Insets(0, 0, 0, 8);
        inner.add(btnLogin, g);
        g.gridx = 1; g.insets = new Insets(0, 0, 0, 0); g.anchor = GridBagConstraints.EAST;
        inner.add(btnRegister, g);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0; outer.gridy = 0; outer.weightx = 1; outer.weighty = 1; outer.anchor = GridBagConstraints.CENTER;
        card.add(inner, outer);
        return card;
    }

    private JPanel wrapCenter(JComponent comp) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(new Color(245, 247, 250));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.weighty = 1; gc.anchor = GridBagConstraints.CENTER;
        wrap.add(comp, gc);
        return wrap;
    }

    private static void showSingleWindow(JFrame target) {
        float[] alpha = new float[] { 0f };
        try {
            target.setOpacity(0f);
        } catch (Throwable ignored) {
            // setOpacity may not be supported; fallback to instant show
        }
        target.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame && w.isShowing() && w != target) {
                    w.dispose();
                }
            }
        });

        javax.swing.Timer t = new javax.swing.Timer(15, e -> {
            alpha[0] += 0.08f;
            float a = Math.min(1f, alpha[0]);
            try { target.setOpacity(a); } catch (Throwable ignored) { ((javax.swing.Timer) e.getSource()).stop(); }
            if (a >= 1f) { ((javax.swing.Timer) e.getSource()).stop(); }
        });
        t.setInitialDelay(10);
        t.start();
    }

    static class RoundedBorder extends AbstractBorder {
        private final Color color; private final int arc;
        RoundedBorder(Color color, int arc) { this.color = color; this.arc = arc; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
        @Override
        public Insets getBorderInsets(Component c, Insets insets) { insets.set(1,1,1,1); return insets; }
    }

    private Image loadAppIcon() {
        try {
            return new ImageIcon("assets/img/bhIcon.png").getImage();
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            Image img = new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel createPasswordWithToggle(JPasswordField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);

        JButton toggle = new JButton();
        toggle.setIcon(eyeIcon);
        toggle.setFocusable(false);
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setMargin(new Insets(0,0,0,0));
        toggle.setPreferredSize(new Dimension(36, 36));
        final char def = field.getEchoChar();
        toggle.addActionListener(e -> {
            boolean showing = field.getEchoChar() == 0;
            if (showing) {
                field.setEchoChar(def);
                if (eyeIcon != null) toggle.setIcon(eyeIcon);
            } else {
                field.setEchoChar((char) 0);
                if (hiddenIcon != null) toggle.setIcon(hiddenIcon);
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(toggle);
        p.add(right, BorderLayout.EAST);
        p.setPreferredSize(new Dimension(360, 36));
        return p;
    }
}
