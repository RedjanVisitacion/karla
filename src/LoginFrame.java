import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 16);
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbMessage;
    private final UserDAO userDAO = new UserDAO();
    private ImageIcon eyeIcon;
    private ImageIcon hiddenIcon;
    private char defaultEcho;

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(loadAppIcon());
        eyeIcon = loadIcon("assets/img/eye.png", 18, 18);
        hiddenIcon = loadIcon("assets/img/hidden.png", 18, 18);
        buildUI();
    }

    private void buildUI() {
        JPanel left = createWelcomePanel();
        JPanel right = createLoginCard();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, wrapCenter(right));
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setDividerSize(0);

        setContentPane(split);
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
            if ("admin".equalsIgnoreCase(user.getRole())) {
                JFrame f = new AdminFrame(user);
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                showSingleWindow(f);
            } else {
                JFrame f = new TenantFrame(user);
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                showSingleWindow(f);
            }
        });
    }

    private void openRegister() {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new RegisterFrame();
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            showSingleWindow(f);
        });
    }

    private static void showSingleWindow(JFrame target) {
        float[] alpha = new float[] { 0f };
        try {
            target.setOpacity(0f);
        } catch (Throwable ignored) {
            // setOpacity may not be supported; fallback to instant show
        }
        target.setVisible(true);

        // Dispose others after the target is visible to avoid blank gap
        SwingUtilities.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame && w.isShowing() && w != target) {
                    w.dispose();
                }
            }
        });

        // Smooth fade-in if supported
        javax.swing.Timer t = new javax.swing.Timer(15, e -> {
            alpha[0] += 0.08f;
            float a = Math.min(1f, alpha[0]);
            try { target.setOpacity(a); } catch (Throwable ignored) { ((javax.swing.Timer) e.getSource()).stop(); }
            if (a >= 1f) { ((javax.swing.Timer) e.getSource()).stop(); }
        });
        t.setInitialDelay(10);
        t.start();
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

        JLabel title = new JLabel("Welcome to\n Boarding House Management System");
        title.setForeground(Color.WHITE);
        title.setFont(titleFont);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Please sign in to continue");
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

    private JPanel createLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setOpaque(false);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(220, 225, 230), 16),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        inner.setBackground(Color.WHITE);
        inner.setOpaque(true);

        JLabel header = new JLabel("Login");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lbu = new JLabel("Username"); lbu.setFont(mainFont);
        tfUsername = new JTextField(); tfUsername.setFont(mainFont);
        tfUsername.setPreferredSize(new Dimension(360, 36));

        JLabel lbp = new JLabel("Password"); lbp.setFont(mainFont);
        pfPassword = new JPasswordField(); pfPassword.setFont(mainFont);
        pfPassword.setPreferredSize(new Dimension(360, 36));
        defaultEcho = pfPassword.getEchoChar();
        JPanel pwPanel = createPasswordWithToggle(pfPassword);

        JCheckBox cbRemember = new JCheckBox("Remember me");

        lbMessage = new JLabel(" ", SwingConstants.LEFT); lbMessage.setForeground(Color.RED);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(33, 150, 243));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(160, 40));
        btnLogin.addActionListener(this::handleLogin);
        getRootPane().setDefaultButton(btnLogin);

        JButton btnCreate = new JButton("Create Account"); btnCreate.setFont(mainFont);
        btnCreate.addActionListener(e -> openRegister());

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 0, 16, 0);
        inner.add(header, g);
        g.gridy = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 6, 0);
        inner.add(lbu, g);
        g.gridy = 2; g.weightx = 1.0; g.insets = new Insets(0, 0, 12, 0);
        inner.add(tfUsername, g);
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0); g.weightx = 0;
        inner.add(lbp, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 8, 0); g.weightx = 1.0;
        inner.add(pwPanel, g);
        g.gridy = 5; g.gridwidth = 2; g.weightx = 0; g.insets = new Insets(0, 0, 8, 0); g.anchor = GridBagConstraints.WEST;
        inner.add(cbRemember, g);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(0, 0, 8, 0);
        inner.add(lbMessage, g);
        g.gridy = 7; g.insets = new Insets(0, 0, 8, 0); g.anchor = GridBagConstraints.CENTER;
        inner.add(btnLogin, g);
        g.gridy = 8; g.insets = new Insets(0, 0, 0, 0);
        inner.add(btnCreate, g);

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
        toggle.addActionListener(e -> {
            boolean showing = field.getEchoChar() == 0;
            if (showing) {
                field.setEchoChar(defaultEcho);
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
