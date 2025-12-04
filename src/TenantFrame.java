import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TenantFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;
    private JPanel maintPanel;

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
            final String label = items[i];
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

            b.addActionListener(e -> {
                if ("Maintenance Request".equals(label)) {
                    openMaintenanceRequest();
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

    private void openMaintenanceRequest() {
        JTextArea ta = new JTextArea(5, 30);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        int res = JOptionPane.showConfirmDialog(this, sp, "New Maintenance Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String desc = ta.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description.");
                return;
            }
            String sql = "INSERT INTO maintenance_requests (user_id, description, status) VALUES (?,?, 'pending')";
            try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, currentUser.getId());
                ps.setString(2, desc);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Request submitted.");
                reloadMaintenancePanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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

        JLabel room = (JLabel) pillLabel(roomText, new Color(55,71,79), new Color(236,239,241));
        room.setFont(mainFont);
        Color stFg = new Color(46,125,50); Color stBg = new Color(232,245,233); // default occupied -> green
        if (statusText.toLowerCase().contains("available")) { stFg = new Color(183,109,0); stBg = new Color(255,248,225); }
        JLabel status = (JLabel) pillLabel(statusText, stFg, stBg);
        status.setFont(mainFont);
        // Compute total balance = sum of unpaid payments for this tenant (all months)
        int balance = 0;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM payments WHERE user_id=? AND status='unpaid'")) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) balance = rs.getBigDecimal(1).intValue(); }
        } catch (SQLException ignore) { }
        JLabel amount = (JLabel) pillLabel("Balance: ₱" + balance, new Color(183, 28, 28), new Color(255,235,238));
        amount.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
        // Load real payment rows for current user (latest first). Ensure current month row exists.
        int rowIdx = 2;
        LocalDate now = LocalDate.now();
        String monthKey = now.toString().substring(0,7); // YYYY-MM
        try (Connection c = DBUtil.getConnection()) {
            // Ensure a payment row exists for this tenant for the current month
            String upsert = "INSERT INTO payments (user_id, month_key, amount, status) VALUES (?,?,600,'unpaid') " +
                    "ON DUPLICATE KEY UPDATE amount=VALUES(amount)";
            try (PreparedStatement ps = c.prepareStatement(upsert)) {
                ps.setInt(1, user.getId());
                ps.setString(2, monthKey);
                ps.executeUpdate();
            }

            String q = "SELECT month_key, amount, status FROM payments WHERE user_id=? ORDER BY month_key DESC LIMIT 12";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, user.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String mk = rs.getString(1);
                        LocalDate d = LocalDate.parse(mk + "-01");
                        String date = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + d.getYear();
                        String amountTxt = "\u20B1" + rs.getBigDecimal(2).setScale(0);
                        String st = rs.getString(3);
                        Color fg;
                        Color bg;
                        if ("paid".equalsIgnoreCase(st)) { fg = new Color(46,125,50); bg = new Color(232,245,233); }
                        else if ("overdue".equalsIgnoreCase(st)) { fg = new Color(183,28,28); bg = new Color(255,235,238); }
                        else { fg = new Color(183,109,0); bg = new Color(255,248,225); }
                        addTableRow(payments, rowIdx++, date, amountTxt, pillLabel(capitalize(st), fg, bg), false);
                    }
                }
            }
        } catch (SQLException ignore) { }
        if (rowIdx == 2) {
            addTableRow(payments, rowIdx, "No payments yet.", "", pillLabel("-", Color.DARK_GRAY, new Color(240,240,240)), false);
        }

        // Maintenance Requests card (auto-refreshable)
        maintPanel = createCardPanel();
        maintPanel.setLayout(new GridBagLayout());
        JLabel mTitle = new JLabel("Maintenance Requests"); mTitle.setFont(titleFont);
        GridBagConstraints mg = new GridBagConstraints();
        mg.gridx = 0; mg.gridy = 0; mg.gridwidth = 2; mg.anchor = GridBagConstraints.WEST; mg.insets = new Insets(0,0,8,0);
        maintPanel.add(mTitle, mg);
        reloadMaintenancePanel();

        // Announcements card (from DB)
        JPanel ann = createCardPanel();
        ann.setLayout(new GridBagLayout());
        JLabel aTitle = new JLabel("Announcements"); aTitle.setFont(titleFont);
        GridBagConstraints ag = new GridBagConstraints();
        ag.gridx = 0; ag.gridy = 0; ag.gridwidth = 2; ag.anchor = GridBagConstraints.WEST; ag.insets = new Insets(0,0,8,0);
        ann.add(aTitle, ag);
        int count = 0;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT a.created_at, a.title, a.body, u.username FROM announcements a JOIN users u ON u.id=a.created_by ORDER BY a.created_at DESC, a.id DESC LIMIT 6");
             ResultSet rs = ps.executeQuery()) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM d");
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                String date = ts != null ? ts.toLocalDateTime().toLocalDate().format(df) : "";
                String title = rs.getString(2);
                String body = rs.getString(3);
                String author = rs.getString(4);

                JPanel block = new JPanel(new GridBagLayout());
                block.setOpaque(false);
                GridBagConstraints ib = new GridBagConstraints();
                ib.gridx = 0; ib.gridy = 0; ib.weightx = 1; ib.anchor = GridBagConstraints.WEST; ib.insets = new Insets(0,0,2,0);
                JLabel titleLbl = new JLabel(title); titleLbl.setFont(mainFont.deriveFont(Font.BOLD));
                block.add(titleLbl, ib);
                ib.gridy = 1; ib.insets = new Insets(0,0,6,0);
                JLabel meta = new JLabel(author + "   " + date); meta.setFont(mainFont);
                block.add(meta, ib);
                ib.gridy = 2; ib.insets = new Insets(0,0,0,0);
                JComponent bodyChip = pillLabel(body, new Color(102, 60, 0), new Color(255, 248, 225));
                block.add(bodyChip, ib);

                GridBagConstraints slot = new GridBagConstraints();
                slot.gridx = count % 2; slot.gridy = 1 + (count / 2);
                slot.weightx = 0.5; slot.fill = GridBagConstraints.HORIZONTAL;
                // symmetric gutters: left column has right gutter, right column has left gutter
                int leftGutter = (count % 2 == 1) ? 8 : 0;
                int rightGutter = (count % 2 == 0) ? 8 : 0;
                slot.insets = new Insets(0, leftGutter, 12, rightGutter);
                ann.add(block, slot);
                count++;
            }
            if (count == 0) {
                GridBagConstraints empty = new GridBagConstraints();
                empty.gridx = 0; empty.gridy = 1; empty.gridwidth = 2; empty.anchor = GridBagConstraints.WEST;
                ann.add(new JLabel("No announcements yet."), empty);
            }
        } catch (SQLException ignore) {
            GridBagConstraints err = new GridBagConstraints();
            err.gridx = 0; err.gridy = 1; err.gridwidth = 2; err.anchor = GridBagConstraints.WEST;
            ann.add(new JLabel("Unable to load announcements."), err);
        }

        // Layout grid
        g.gridy = 2; g.gridwidth = 1; g.weightx = 0.5; g.insets = new Insets(0, 0, 16, 8); g.fill = GridBagConstraints.BOTH; g.weighty = 0;
        container.add(payments, g);
        g.gridx = 1; g.insets = new Insets(0, 8, 16, 0);
        container.add(maintPanel, g);

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

    private void reloadMaintenancePanel() {
        if (maintPanel == null) return;
        maintPanel.removeAll();
        maintPanel.setLayout(new GridBagLayout());
        JLabel mTitle = new JLabel("Maintenance Requests"); mTitle.setFont(titleFont);
        GridBagConstraints mg = new GridBagConstraints();
        mg.gridx = 0; mg.gridy = 0; mg.gridwidth = 2; mg.anchor = GridBagConstraints.WEST; mg.insets = new Insets(0,0,8,0);
        maintPanel.add(mTitle, mg);
        addTableRow(maintPanel, 1, "Date", "Description", new JLabel("Status"), new JLabel("Actions"), true);
        int mRow = 2;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, created_at, description, status FROM maintenance_requests WHERE user_id=? ORDER BY created_at DESC, id DESC LIMIT 12")) {
            ps.setInt(1, currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM d");
                while (rs.next()) {
                    int id = rs.getInt(1);
                    Timestamp ts = rs.getTimestamp(2);
                    String date = ts != null ? ts.toLocalDateTime().toLocalDate().format(df) : "";
                    String desc = rs.getString(3);
                    String st = rs.getString(4);
                    Color fg = new Color(183, 109, 0);
                    Color bg = new Color(255, 248, 225);
                    if ("approved".equalsIgnoreCase(st)) { fg = new Color(0,121,107); bg = new Color(224,242,241); }
                    if ("resolved".equalsIgnoreCase(st)) { fg = new Color(46,125,50); bg = new Color(232,245,233); }
                    JComponent statusPill = pillLabel(capitalize(st), fg, bg);
                    JButton btnDel = new JButton("Delete");
                    btnDel.setFocusPainted(false);
                    btnDel.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Delete this request?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
                        if (confirm == JOptionPane.OK_OPTION) {
                            String del = "DELETE FROM maintenance_requests WHERE id=? AND user_id=?";
                            try (Connection dc = DBUtil.getConnection(); PreparedStatement dps = dc.prepareStatement(del)) {
                                dps.setInt(1, id);
                                dps.setInt(2, currentUser.getId());
                                dps.executeUpdate();
                                reloadMaintenancePanel();
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    addTableRow(maintPanel, mRow++, date, desc, statusPill, btnDel, false);
                }
            }
        } catch (SQLException ignore) { }
        if (mRow == 2) {
            addTableRow(maintPanel, mRow, "No requests yet.", "", pillLabel("-", Color.DARK_GRAY, new Color(240,240,240)), new JLabel(""), false);
        }
        maintPanel.revalidate();
        maintPanel.repaint();
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

    private void addTableRow(JPanel parent, int row, String c1, String c2, JComponent c3, JComponent c4, boolean header) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row; g.insets = new Insets(4, 0, 4, 0);
        JLabel l1 = new JLabel(c1); JLabel l2 = new JLabel(c2);
        if (header) { l1.setFont(l1.getFont().deriveFont(Font.BOLD)); l2.setFont(l2.getFont().deriveFont(Font.BOLD));
            if (c3 instanceof JLabel) { c3.setFont(c3.getFont().deriveFont(Font.BOLD)); }
            if (c4 instanceof JLabel) { c4.setFont(c4.getFont().deriveFont(Font.BOLD)); }
        }
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; parent.add(l1, g);
        g.gridx = 1; g.insets = new Insets(4, 24, 4, 24); parent.add(l2, g);
        g.gridx = 2; parent.add(c3, g);
        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.EAST; parent.add(c4, g);
    }

    private String capitalize(String s) { if (s==null||s.isEmpty()) return s; return Character.toUpperCase(s.charAt(0))+s.substring(1); }

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
