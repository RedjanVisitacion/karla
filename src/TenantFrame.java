import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.sql.*;

public class TenantFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;

    public TenantFrame(User user) {
        setTitle("");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());
        this.currentUser = user;

        JPanel sidebar = createSidebar();
        JPanel main = createMainContent(user);

        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setBackground(sidebarBg);
        side.setLayout(new GridBagLayout());
        side.setPreferredSize(new Dimension(240, 0));

        JLabel brand = new JLabel("  Boarding House");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String[] items = new String[]{
                "Dashboard", "Maintenance Request"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (int i = 0; i < items.length; i++) {
            final boolean active = (i == 0);
            final Color idleBg = new Color(21, 101, 192); // solid mid blue for idle state
            final Color hoverBg = sidebarItemActive;      // brighter blue on hover

            JButton b = new JButton(items[i]);
            b.setFocusPainted(false);
            b.setFocusable(false);
            b.setUI(new BasicButtonUI());
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(mainFont);
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorderPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            b.setBackground(active ? hoverBg : idleBg);

            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!active) b.setBackground(hoverBg);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!active) b.setBackground(idleBg);
                }
            });

            // No additional navigation actions for tenant sidebar currently

            g.gridy++;
            g.insets = new Insets(4, 12, 4, 12);
            side.add(b, g);
        }

        g.gridy++; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        side.add(Box.createVerticalGlue(), g);

        return side;
    }

    private void openMyRoom() {
        String title = "My Room";
        String roomText = "Room: Not yet assigned";
        String statusText = "Status: Not assigned";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.room_number, r.status FROM rooms r " +
                             "JOIN room_assignments ra ON ra.room_id = r.id " +
                             "WHERE ra.user_id = ? LIMIT 1")) {
            ps.setInt(1, currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rn = rs.getString(1);
                    String st = rs.getString(2);
                    if (rn != null && !rn.isEmpty()) roomText = "Room No: " + rn;
                    if (st != null && !st.isEmpty()) statusText = "Status: " + Character.toUpperCase(st.charAt(0)) + st.substring(1);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        p.add(new JLabel(roomText));
        p.add(new JLabel(statusText));
        JOptionPane.showMessageDialog(this, p, title, JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createMainContent(User user) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(pageBg);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header: show "Tenant: <username>"
        GridBagConstraints g = new GridBagConstraints();
        JLabel header = new JLabel("Tenant: " + user.getUsername());
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 0, 16, 0);
        container.add(header, g);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(33, 150, 243));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                JFrame f = new LoginFrame();
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                showSingleWindow(f);
            });
        });
        g.gridx = 1; g.gridy = 0; g.gridwidth = 1; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.insets = new Insets(0, 0, 16, 0);
        container.add(btnLogout, g);

        // Summary card
        JPanel summary = createCardPanel();
        summary.setLayout(new GridBagLayout());
        // Determine tenant's assigned room and status
        String roomText = "Room: Not yet assigned";
        String statusText = "Status: Not assigned";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.room_number, r.status FROM rooms r " +
                             "JOIN room_assignments ra ON ra.room_id = r.id " +
                             "WHERE ra.user_id = ? LIMIT 1")) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rn = rs.getString(1);
                    String st = rs.getString(2);
                    if (rn != null && !rn.isEmpty()) roomText = "Room No: " + rn;
                    if (st != null && !st.isEmpty()) {
                        statusText = "Status: " + Character.toUpperCase(st.charAt(0)) + st.substring(1);
                    }
                }
            }
        } catch (SQLException ignore) { }

        JLabel room = new JLabel(roomText); room.setFont(mainFont);
        JLabel status = new JLabel(statusText); status.setFont(mainFont);
        JLabel amount = new JLabel("₱5000"); amount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        GridBagConstraints sg = new GridBagConstraints();
        sg.gridx = 0; sg.gridy = 0; sg.insets = new Insets(0, 0, 4, 24); sg.anchor = GridBagConstraints.WEST;
        summary.add(room, sg);
        sg.gridx = 1; summary.add(status, sg);
        sg.gridx = 2; sg.weightx = 1; sg.anchor = GridBagConstraints.EAST; summary.add(amount, sg);
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(0, 0, 16, 0);
        container.add(summary, g);

        // Payment History card
        JPanel payments = createCardPanel();
        payments.setLayout(new GridBagLayout());
        JLabel phTitle = new JLabel("Payment History"); phTitle.setFont(titleFont);
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0; pg.gridy = 0; pg.gridwidth = 3; pg.anchor = GridBagConstraints.WEST; pg.insets = new Insets(0,0,8,0);
        payments.add(phTitle, pg);
        addTableRow(payments, 1, "Date", "Amount", "Status", true);
        addTableRow(payments, 2, "Jan 01", "₱5000", pillLabel("Paid", new Color(46, 125, 50), new Color(232, 245, 233)), false);
        addTableRow(payments, 3, "Dec 01", "₱5000", pillLabel("Paid", new Color(46, 125, 50), new Color(232, 245, 233)), false);
        addTableRow(payments, 4, "Nov 01", "₱4500", pillLabel("Overdue", new Color(183, 28, 28), new Color(255, 235, 238)), false);
        addTableRow(payments, 5, "Oct 01", "₱4500", pillLabel("Paid", new Color(46, 125, 50), new Color(232, 245, 233)), false);

        // Maintenance Requests card
        JPanel maint = createCardPanel();
        maint.setLayout(new GridBagLayout());
        JLabel mTitle = new JLabel("Maintenance Requests"); mTitle.setFont(titleFont);
        GridBagConstraints mg = new GridBagConstraints();
        mg.gridx = 0; mg.gridy = 0; mg.gridwidth = 2; mg.anchor = GridBagConstraints.WEST; mg.insets = new Insets(0,0,8,0);
        maint.add(mTitle, mg);
        addTableRow(maint, 1, "Date", "Status", "", true);
        addTableRow(maint, 2, "Jan 01", "Leaky faucet", pillLabel("Pending", new Color(183, 109, 0), new Color(255, 248, 225)), false);
        addTableRow(maint, 3, "Nov 1", "Broken window", pillLabel("Approved", new Color(0, 121, 107), new Color(224, 242, 241)), false);

        // Announcements card
        JPanel ann = createCardPanel();
        ann.setLayout(new GridBagLayout());
        JLabel aTitle = new JLabel("Announcements"); aTitle.setFont(titleFont);
        JLabel by = new JLabel("Admin   Apr 20"); by.setFont(mainFont);
        JLabel msg = new JLabel("Please keep the noise level down in the evenings"); msg.setFont(mainFont);
        GridBagConstraints ag = new GridBagConstraints();
        ag.gridx = 0; ag.gridy = 0; ag.anchor = GridBagConstraints.WEST; ag.insets = new Insets(0,0,6,0);
        ann.add(aTitle, ag);
        ag.gridy = 1; ann.add(by, ag);
        ag.gridy = 2; ann.add(msg, ag);

        // Layout grid
        g.gridy = 2; g.gridwidth = 1; g.weightx = 0.5; g.insets = new Insets(0, 0, 16, 8); g.fill = GridBagConstraints.BOTH; g.weighty = 0;
        container.add(payments, g);
        g.gridx = 1; g.insets = new Insets(0, 8, 16, 0);
        container.add(maint, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.insets = new Insets(0, 0, 0, 0); g.weighty = 1;
        container.add(ann, g);

        return container;
    }

    private JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(cardBorder, 12),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    private void addTableRow(JPanel parent, int row, String c1, String c2, String c3, boolean header) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row; g.insets = new Insets(4, 0, 4, 0);
        JLabel l1 = new JLabel(c1); JLabel l2 = new JLabel(c2); JLabel l3 = new JLabel(c3);
        if (header) { l1.setFont(l1.getFont().deriveFont(Font.BOLD)); l2.setFont(l2.getFont().deriveFont(Font.BOLD)); l3.setFont(l3.getFont().deriveFont(Font.BOLD)); }
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; parent.add(l1, g);
        g.gridx = 1; g.insets = new Insets(4, 24, 4, 24); parent.add(l2, g);
        g.gridx = 2; g.weightx = 1; g.anchor = GridBagConstraints.EAST; parent.add(l3, g);
    }

    private void addTableRow(JPanel parent, int row, String c1, String c2, JComponent c3, boolean header) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row; g.insets = new Insets(4, 0, 4, 0);
        JLabel l1 = new JLabel(c1); JLabel l2 = new JLabel(c2);
        if (header) { l1.setFont(l1.getFont().deriveFont(Font.BOLD)); l2.setFont(l2.getFont().deriveFont(Font.BOLD)); }
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; parent.add(l1, g);
        g.gridx = 1; g.insets = new Insets(4, 24, 4, 24); parent.add(l2, g);
        g.gridx = 2; g.weightx = 1; g.anchor = GridBagConstraints.EAST; parent.add(c3, g);
    }

    private JComponent pillLabel(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return l;
    }

    static class RoundedBorder extends javax.swing.border.AbstractBorder {
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
}
