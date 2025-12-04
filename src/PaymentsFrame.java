import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class PaymentsFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;

    private JTable table;
    private DefaultTableModel model;

    public PaymentsFrame(User user) {
        setTitle("");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());
        this.currentUser = user;

        add(createSidebar(), BorderLayout.WEST);
        add(createMain(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setBackground(sidebarBg);
        side.setPreferredSize(new Dimension(240, 0));

        JLabel brand = new JLabel("  Boarding House");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String[] items = new String[]{
                "Dashboard", "Rooms", "Payments & Billing"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (String label : items) {
            final boolean active = "Payments & Billing".equals(label);
            final Color idleBg = new Color(21, 101, 192);
            final Color hoverBg = sidebarItemActive;

            JButton b = new JButton(label);
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
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (!active) b.setBackground(hoverBg); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { if (!active) b.setBackground(idleBg); }
            });

            b.addActionListener(e -> {
                if ("Dashboard".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new AdminFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                } else if ("Rooms".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new RoomsFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                }
            });

            g.gridy++;
            g.insets = new Insets(4, 12, 4, 12);
            side.add(b, g);
        }

        g.gridy++; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        side.add(Box.createVerticalGlue(), g);
        return side;
    }

    private JPanel createMain() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(pageBg);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();

        JLabel header = new JLabel("Payments & Billing"); header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0,0,16,0);
        container.add(header, g);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(33, 150, 243));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JFrame f = new LoginFrame();
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            showSingleWindow(f);
        }));
        g.gridx = 1; g.gridy = 0; g.gridwidth = 1; g.weightx = 0; g.anchor = GridBagConstraints.EAST;
        container.add(btnLogout, g);

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        JLabel title = new JLabel("Monthly Dues (\u20B1600 per tenant)"); title.setFont(titleFont);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnPaid = new JButton("Mark Paid");
        btnPaid.setBackground(new Color(46, 125, 50)); btnPaid.setForeground(Color.WHITE); btnPaid.setFocusPainted(false);
        btnPaid.addActionListener(e -> markSelected("paid"));
        JButton btnUnpaid = new JButton("Mark Unpaid");
        btnUnpaid.setBackground(new Color(183, 28, 28)); btnUnpaid.setForeground(Color.WHITE); btnUnpaid.setFocusPainted(false);
        btnUnpaid.addActionListener(e -> markSelected("unpaid"));
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> reloadPayments());
        actions.add(btnPaid); actions.add(btnUnpaid); actions.add(refresh);
        top.add(title, BorderLayout.WEST); top.add(actions, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Tenant", "Room", "Month", "Amount", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.weightx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        container.add(card, g);

        reloadPayments();
        return container;
    }

    private void reloadPayments() {
        model.setRowCount(0);
        LocalDate now = LocalDate.now();
        String monthKey = now.toString().substring(0,7); // YYYY-MM
        String monthName = now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + now.getYear();
        try (Connection c = DBUtil.getConnection()) {
            // ensure a payment row exists for each tenant for the current month
            String upsert = "INSERT INTO payments (user_id, month_key, amount, status) " +
                    "SELECT id, ?, 600, 'unpaid' FROM users WHERE role='tenant' " +
                    "ON DUPLICATE KEY UPDATE amount=VALUES(amount)";
            try (PreparedStatement ps = c.prepareStatement(upsert)) {
                ps.setString(1, monthKey);
                ps.executeUpdate();
            }

            String q = "SELECT u.username, COALESCE(r.room_number, 'Not assigned') AS room, p.amount, p.status " +
                    "FROM payments p " +
                    "JOIN users u ON u.id=p.user_id " +
                    "LEFT JOIN room_assignments ra ON ra.user_id=u.id " +
                    "LEFT JOIN rooms r ON r.id=ra.room_id " +
                    "WHERE p.month_key=? ORDER BY u.username";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, monthKey);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String status = rs.getString(4);
                        String amount = "\u20B1" + rs.getBigDecimal(3).setScale(0);
                        model.addRow(new Object[]{rs.getString(1), rs.getString(2), monthName, amount, capitalize(status)});
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markSelected(String status) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a tenant row first."); return; }
        String username = String.valueOf(model.getValueAt(row, 0));
        LocalDate now = LocalDate.now();
        String monthKey = now.toString().substring(0,7);
        String sql = "UPDATE payments p JOIN users u ON u.id=p.user_id SET p.status=?, p.paid_at=CASE WHEN ?='paid' THEN CURRENT_TIMESTAMP ELSE NULL END WHERE p.month_key=? AND u.username=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, monthKey);
            ps.setString(4, username);
            int n = ps.executeUpdate();
            if (n == 0) { JOptionPane.showMessageDialog(this, "No matching payment row."); }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        reloadPayments();
    }

    private String capitalize(String s) { if (s==null||s.isEmpty()) return s; return Character.toUpperCase(s.charAt(0))+s.substring(1); }

    private JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(cardBorder, 12),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color; private final int arc;
        RoundedBorder(Color color, int arc) { this.color = color; this.arc = arc; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
        @Override public Insets getBorderInsets(Component c, Insets insets) { insets.set(1,1,1,1); return insets; }
    }

    private static void showSingleWindow(JFrame target) {
        try { target.setOpacity(0f); } catch (Throwable ignored) {}
        target.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame && w.isShowing() && w != target) { w.dispose(); }
            }
        });
        final float[] a = {0f};
        javax.swing.Timer t = new javax.swing.Timer(15, e -> {
            a[0] += 0.08f; float alpha = Math.min(1f, a[0]);
            try { target.setOpacity(alpha); } catch (Throwable ignored) { ((javax.swing.Timer)e.getSource()).stop(); }
            if (alpha >= 1f) { ((javax.swing.Timer)e.getSource()).stop(); }
        });
        t.setInitialDelay(10);
        t.start();
    }
}
