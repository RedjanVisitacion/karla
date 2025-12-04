import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class AdminFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;

    public AdminFrame(User user) {
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

    private int getMonthlyIncome() {
        String q = "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='paid' AND month_key=?";
        String monthKey = java.time.LocalDate.now().toString().substring(0,7);
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBigDecimal(1).intValue(); }
        } catch (SQLException ignored) { }
        return 0;
    }

    private int getPendingPayments() {
        String q = "SELECT COUNT(*) FROM payments WHERE status IN ('unpaid','overdue') AND month_key=?";
        String monthKey = java.time.LocalDate.now().toString().substring(0,7);
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException ignored) { }
        return 0;
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
            "Dashboard", "Rooms", "Payments & Billing",
            "Maintenance Requests", "Announcements"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (int i = 0; i < items.length; i++) {
            final boolean active = (i == 0);
            final Color idleBg = new Color(21, 101, 192);
            final Color hoverBg = sidebarItemActive;

            String label = items[i];
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
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!active) b.setBackground(hoverBg);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!active) b.setBackground(idleBg);
                }
            });

            // navigation handlers
            b.addActionListener(e -> {
                if ("Rooms".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new RoomsFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                } else if ("Dashboard".equals(label)) {
                    // already here; no-op
                } else if ("Tenants".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new TenantsFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                } else if ("Payments & Billing".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new PaymentsFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                } else if ("Maintenance Requests".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new MaintenanceRequestsFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                } else if ("Announcements".equals(label)) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new AnnouncementsFrame(currentUser);
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

        JButton btnExit = new JButton("Exit");
        btnExit.setFocusPainted(false);
        btnExit.setFocusable(false);
        btnExit.setUI(new BasicButtonUI());
        btnExit.setHorizontalAlignment(SwingConstants.CENTER);
        btnExit.setFont(mainFont);
        btnExit.setForeground(Color.WHITE);
        btnExit.setOpaque(true);
        btnExit.setContentAreaFilled(true);
        btnExit.setBorderPainted(false);
        btnExit.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnExit.setBackground(new Color(183, 28, 28));
        btnExit.addActionListener(e -> System.exit(0));

        g.gridy++; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(8, 12, 16, 12);
        side.add(btnExit, g);

        return side;
    }

    private JPanel createMainContent(User user) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(pageBg);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();

        // Header: Admin Dashboard + user avatar/name + Logout
        JLabel header = new JLabel("Admin Dashboard");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 0, 16, 0);
        container.add(header, g);

        JPanel rightHead = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightHead.setOpaque(false);
        rightHead.add(new JLabel(new ImageIcon(new BufferedImage(28, 28, BufferedImage.TYPE_INT_ARGB))));
        rightHead.add(new JLabel("Admin: " + user.getUsername()));
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
        rightHead.add(btnLogout);
        g.gridx = 1; g.gridy = 0; g.gridwidth = 1; g.weightx = 0; g.anchor = GridBagConstraints.EAST;
        container.add(rightHead, g);

        // housekeeping: ensure current month dues exist for all tenants and mark overdue for past months
        ensureMonthAndOverdue();

        // Top summary cards
        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        String[] rc = getRoomCounts();
        int tc = getTenantCount();
        addStatCard(cards, 0, 0, "Total Tenants", String.valueOf(tc));
        addStatCard(cards, 1, 0, "Total Rooms / Available Rooms", rc[0] + " / " + rc[1]);
        addStatCard(cards, 2, 0, "Monthly Income", "\u20B1" + getMonthlyIncome());
        addStatCard(cards, 3, 0, "Pending Payments", String.valueOf(getPendingPayments()));
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(0,0,16,0);
        container.add(cards, g);

        // Middle row: Room Status (left) and Payment Overview (right)
        JPanel roomStatus = createCardPanel();
        roomStatus.setLayout(new GridBagLayout());
        JLabel rsTitle = new JLabel("Room Status"); rsTitle.setFont(titleFont);
        GridBagConstraints rg = new GridBagConstraints();
        rg.gridx = 0; rg.gridy = 0; rg.gridwidth = 3; rg.anchor = GridBagConstraints.WEST; rg.insets = new Insets(0,0,8,0);
        roomStatus.add(rsTitle, rg);
        // dynamic grid of rooms with colored labels (live from DB)
        List<RoomItem> rooms = fetchRooms(Integer.MAX_VALUE);
        int idx = 0;
        Color greenBg = new Color(232,245,233);   // empty
        Color greenFg = new Color(46,125,50);
        Color yellowBg = new Color(255,248,225);  // partially occupied
        Color yellowFg = new Color(183,109,0);
        Color redBg = new Color(255,235,238);     // full
        Color redFg = new Color(183,28,28);
        int cols = 4;
        int rows = (int) Math.ceil(rooms.size() / (double) cols);
        if (rows == 0) rows = 1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String label = idx < rooms.size() ? rooms.get(idx).name : "";
                String status = idx < rooms.size() ? rooms.get(idx).status : "available";
                idx++;
                if (label.isEmpty()) break;
                JLabel pill = new JLabel(label, SwingConstants.CENTER);
                pill.setOpaque(true);
                pill.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                // color by occupancy vs capacity
                if (idx-1 < rooms.size()) {
                    RoomItem it = rooms.get(idx-1);
                    if (it.occupied == 0) {
                        pill.setBackground(greenBg); pill.setForeground(greenFg);
                    } else if (it.occupied < it.capacity) {
                        pill.setBackground(yellowBg); pill.setForeground(yellowFg);
                    } else {
                        pill.setBackground(redBg); pill.setForeground(redFg);
                    }
                } else {
                    pill.setBackground(greenBg); pill.setForeground(greenFg);
                }
                GridBagConstraints cg = new GridBagConstraints();
                cg.gridx = c; cg.gridy = r + 1; cg.insets = new Insets(4,4,4,4); cg.anchor = GridBagConstraints.CENTER;
                roomStatus.add(pill, cg);
            }
        }

        JPanel paymentOverview = createCardPanel();
        paymentOverview.setLayout(new GridBagLayout());
        JLabel poTitle = new JLabel("Payment Overview"); poTitle.setFont(titleFont);
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0; pg.gridy = 0; pg.gridwidth = 2; pg.anchor = GridBagConstraints.WEST; pg.insets = new Insets(0,0,8,0);
        paymentOverview.add(poTitle, pg);
        int[] paySums = getPaymentSums();
        paymentOverview.add(simpleBarChart(paySums[0], paySums[1]), gcAt(0,1,1,1, new Insets(0,0,8,0)));
        paymentOverview.add(simplePieLegend(), gcAt(1,1,1,1, new Insets(0,16,8,0)));

        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.weightx = 0.5; g.insets = new Insets(0, 0, 16, 8); g.fill = GridBagConstraints.BOTH; g.weighty = 0;
        container.add(roomStatus, g);
        g.gridx = 1; g.gridy = 2; g.insets = new Insets(0, 8, 16, 0);
        container.add(paymentOverview, g);

        // Bottom row: Recent Transactions and Recent Maintenance Requests
        JPanel trans = createCardPanel();
        trans.setLayout(new GridBagLayout());
        JLabel tTitle = new JLabel("Recent Transactions"); tTitle.setFont(titleFont);
        GridBagConstraints tg = new GridBagConstraints();
        tg.gridx = 0; tg.gridy = 0; tg.gridwidth = 4; tg.anchor = GridBagConstraints.WEST; tg.insets = new Insets(0,0,8,0);
        trans.add(tTitle, tg);
        addTableRow(trans, 1, "Date", "Tenant", "Amount", pillLabel("Status", Color.DARK_GRAY, new Color(240,240,240)), true);
        // Load recent transactions from payments table (latest paid first, then unpaid/overdue)
        String qrt = "SELECT p.paid_at, u.username, p.amount, p.status FROM payments p " +
                "JOIN users u ON u.id=p.user_id " +
                "ORDER BY (p.paid_at IS NULL), p.paid_at DESC, p.id DESC LIMIT 5";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(qrt); ResultSet rs = ps.executeQuery()) {
            int row = 2; SimpleDateFormat df = new SimpleDateFormat("MMM dd");
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                String date = ts != null ? df.format(ts) : "-";
                String tenant = rs.getString(2);
                String amount = "\u20B1" + rs.getBigDecimal(3).setScale(0);
                String st = rs.getString(4);
                Color fg;
                Color bg;
                if ("paid".equalsIgnoreCase(st)) { fg = new Color(46,125,50); bg = new Color(232,245,233); }
                else if ("overdue".equalsIgnoreCase(st)) { fg = new Color(183,28,28); bg = new Color(255,235,238); }
                else { fg = new Color(183,109,0); bg = new Color(255,248,225); }
                addTableRow(trans, row++, date, tenant, amount, pillLabel(capitalize(st), fg, bg), false);
            }
        } catch (SQLException ignore) { }

        JPanel maint = createCardPanel();
        maint.setLayout(new GridBagLayout());
        JLabel mTitle = new JLabel("Recent Maintenance Requests"); mTitle.setFont(titleFont);
        GridBagConstraints mg = new GridBagConstraints();
        mg.gridx = 0; mg.gridy = 0; mg.gridwidth = 3; mg.anchor = GridBagConstraints.WEST; mg.insets = new Insets(0,0,8,0);
        maint.add(mTitle, mg);
        addTableRow(maint, 1, "Tenant", "Issue", "", pillLabel("Status", Color.DARK_GRAY, new Color(240,240,240)), true);
        String qrm = "SELECT u.username, mr.description, mr.status FROM maintenance_requests mr " +
                "JOIN users u ON u.id=mr.user_id ORDER BY mr.created_at DESC, mr.id DESC LIMIT 5";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(qrm); ResultSet rs = ps.executeQuery()) {
            int row = 2;
            while (rs.next()) {
                String tenant = rs.getString(1);
                String issue = rs.getString(2);
                String st = rs.getString(3);
                Color fg = new Color(183,109,0); // pending
                Color bg = new Color(255,248,225);
                if ("approved".equalsIgnoreCase(st)) { fg = new Color(0,121,107); bg = new Color(224,242,241); }
                if ("resolved".equalsIgnoreCase(st)) { fg = new Color(46,125,50); bg = new Color(232,245,233); }
                addTableRow(maint, row++, tenant, issue, "", pillLabel(capitalize(st), fg, bg), false);
            }
        } catch (SQLException ignore) { }

        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0.5; g.insets = new Insets(0, 0, 0, 8); g.fill = GridBagConstraints.BOTH; g.weighty = 1;
        container.add(trans, g);
        g.gridx = 1; g.gridy = 3; g.insets = new Insets(0, 8, 0, 0);
        container.add(maint, g);

        return container;
    }

    private void addStatCard(JPanel parent, int col, int row, String label, String value) {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        JLabel l = new JLabel(label); l.setFont(mainFont);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0; cg.anchor = GridBagConstraints.WEST; card.add(l, cg);
        cg.gridy = 1; cg.insets = new Insets(4,0,0,0); card.add(v, cg);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = col; g.gridy = row; g.insets = new Insets(0, (col==0?0:8), 0, 8); g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        parent.add(card, g);
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

    private void addTableRow(JPanel parent, int row, String c1, String c2, String c3, JComponent c4, boolean header) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row; g.insets = new Insets(4, 0, 4, 0);
        JLabel l1 = new JLabel(c1); JLabel l2 = new JLabel(c2); JLabel l3 = new JLabel(c3);
        if (header) { l1.setFont(l1.getFont().deriveFont(Font.BOLD)); l2.setFont(l2.getFont().deriveFont(Font.BOLD)); l3.setFont(l3.getFont().deriveFont(Font.BOLD)); }
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; parent.add(l1, g);
        g.gridx = 1; g.insets = new Insets(4, 24, 4, 24); parent.add(l2, g);
        g.gridx = 2; parent.add(l3, g);
        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.EAST; parent.add(c4, g);
    }

    private JComponent pillLabel(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return l;
    }

    private GridBagConstraints gcAt(int x, int y, double wx, double wy, Insets in) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.weightx = wx; g.weighty = wy; g.insets = in; g.anchor = GridBagConstraints.CENTER;
        return g;
    }

    private JComponent simpleBarChart(int paidSum, int unpaidSum) {
        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                int base = h - 28; int bw = Math.max(36, w/6); int gap = bw/2; int x = 24;
                int max = Math.max(1, Math.max(paidSum, unpaidSum));
                // Paid bar
                int bhPaid = (int) ((h - 48) * (paidSum / (double) max));
                g2.setColor(new Color(33,150,243));
                g2.fillRoundRect(x, base - bhPaid, bw, bhPaid, 8, 8);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString("Paid", x + 4, base + 16);
                g2.drawString("\u20B1" + paidSum, x + 4, base - bhPaid - 6);
                // Unpaid bar
                x += bw + gap;
                int bhUnpaid = (int) ((h - 48) * (unpaidSum / (double) max));
                g2.setColor(new Color(183,28,28));
                g2.fillRoundRect(x, base - bhUnpaid, bw, bhUnpaid, 8, 8);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString("Unpaid", x + 4, base + 16);
                g2.drawString("\u20B1" + unpaidSum, x + 4, base - bhUnpaid - 6);
                g2.dispose();
            }
        };
        chart.setPreferredSize(new Dimension(320, 160));
        chart.setOpaque(false);
        return chart;
    }

    private int[] getPaymentSums() {
        int paid = 0, unpaid = 0;
        String q = "SELECT status, COALESCE(SUM(amount),0) FROM payments WHERE month_key=? GROUP BY status";
        String monthKey = java.time.LocalDate.now().toString().substring(0,7);
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String st = rs.getString(1);
                    int sum = rs.getBigDecimal(2).intValue();
                    if ("paid".equalsIgnoreCase(st)) paid = sum;
                    else if ("unpaid".equalsIgnoreCase(st) || "overdue".equalsIgnoreCase(st)) unpaid += sum;
                }
            }
        } catch (SQLException ignored) { }
        return new int[]{paid, unpaid};
    }

    private void ensureMonthAndOverdue() {
        String monthKey = java.time.LocalDate.now().toString().substring(0,7);
        try (Connection c = DBUtil.getConnection()) {
            String upsert = "INSERT INTO payments (user_id, month_key, amount, status) " +
                    "SELECT id, ?, 600, 'unpaid' FROM users WHERE role='tenant' " +
                    "ON DUPLICATE KEY UPDATE amount=VALUES(amount)";
            try (PreparedStatement ps = c.prepareStatement(upsert)) {
                ps.setString(1, monthKey);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE payments SET status='overdue' WHERE status='unpaid' AND month_key < ?")) {
                ps.setString(1, monthKey);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) { }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private JComponent simplePieLegend() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel paid = new JLabel(" "); paid.setOpaque(true); paid.setBackground(new Color(33,150,243)); paid.setPreferredSize(new Dimension(14,14));
        JLabel unpaid = new JLabel(" "); unpaid.setOpaque(true); unpaid.setBackground(new Color(183,28,28)); unpaid.setPreferredSize(new Dimension(14,14));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0,0,6,6); p.add(paid, g);
        g.gridx = 1; p.add(new JLabel("Paid"), g);
        g.gridx = 0; g.gridy = 1; p.add(unpaid, g);
        g.gridx = 1; p.add(new JLabel("Unpaid"), g);
        return p;
    }

    private String[] getRoomCounts() {
        String total = "0", available = "0", occupied = "0";
        String q = "SELECT (SELECT COUNT(*) FROM rooms), (SELECT COUNT(*) FROM rooms WHERE status='available'), (SELECT COUNT(*) FROM rooms WHERE status='occupied')";
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
            if (rs.next()) {
                total = String.valueOf(rs.getInt(1));
                available = String.valueOf(rs.getInt(2));
                occupied = String.valueOf(rs.getInt(3));
            }
        } catch (SQLException ignored) { }
        return new String[]{total, available, occupied};
    }

    private List<RoomItem> fetchRooms(int limit) {
        List<RoomItem> list = new ArrayList<>();
        String qAll = "SELECT r.room_number, r.status, r.capacity, COALESCE(COUNT(ra.user_id),0) AS occ " +
                "FROM rooms r LEFT JOIN room_assignments ra ON ra.room_id=r.id " +
                "GROUP BY r.id, r.room_number, r.status, r.capacity ORDER BY r.room_number";
        String qLim = qAll + " LIMIT ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(limit == Integer.MAX_VALUE ? qAll : qLim)) {
            if (limit != Integer.MAX_VALUE) ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RoomItem(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
                }
            }
        } catch (SQLException ignored) {}
        return list;
    }

    private int getTenantCount() {
        String q = "SELECT COUNT(*) FROM users WHERE role='tenant'";
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) { }
        return 0;
    }

    static class RoomItem {
        final String name; final String status; final int capacity; final int occupied;
        RoomItem(String name, String status, int capacity, int occupied) {
            this.name = name; this.status = status; this.capacity = capacity; this.occupied = occupied;
        }
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
        } catch (Throwable ignored) { }
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
}
