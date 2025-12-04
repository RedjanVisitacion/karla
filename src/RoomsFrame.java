import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomsFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);

    private final User currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JCheckBox cbAvailableOnly;
    private JLabel lbTotal; private JLabel lbAvailable; private JLabel lbOccupied;

    public RoomsFrame(User user) {
        setTitle("");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());
        this.currentUser = user;

        JPanel sidebar = createSidebar();
        JPanel main = createMainContent();

        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void removeRoom() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        int id = (int) model.getValueAt(row, 0);
        // check occupants
        int[] info = loadRoomOccupancyAndCapacity(id);
        int occ = info[0];
        if (occ > 0) { JOptionPane.showMessageDialog(this, "Cannot remove: room has occupants."); return; }
        int res = JOptionPane.showConfirmDialog(this, "Delete this room?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        String del = "DELETE FROM rooms WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(del)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            reloadRooms();
            reloadStats();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStatCard(JPanel parent, int col, String label, JLabel valueLabel) {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        JLabel l = new JLabel(label); l.setFont(mainFont);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0; cg.anchor = GridBagConstraints.WEST; card.add(l, cg);
        cg.gridy = 1; cg.insets = new Insets(4,0,0,0); card.add(valueLabel, cg);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = col; g.gridy = 0; g.insets = new Insets(0, (col==0?0:8), 0, 8); g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        parent.add(card, g);
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(33, 150, 243));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String val = String.valueOf(value);
            JLabel l = (JLabel) c;
            l.setOpaque(true);
            Color bg = table.getBackground();
            Color fg = table.getForeground();
            if ("available".equalsIgnoreCase(val)) {
                bg = new Color(232,245,233); fg = new Color(46,125,50);
            } else if ("occupied".equalsIgnoreCase(val)) {
                bg = new Color(255,235,238); fg = new Color(183,28,28);
            }
            if (isSelected) {
                // Keep selection highlight but tint text
                l.setBackground(table.getSelectionBackground());
                l.setForeground(table.getSelectionForeground());
            } else {
                l.setBackground(bg);
                l.setForeground(fg);
            }
            l.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            return l;
        }
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
                "Dashboard", "Tenants", "Rooms", "Payments & Billing",
                "Maintenance Requests", "Announcements", "Reports"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (int i = 0; i < items.length; i++) {
            final boolean active = ("Rooms".equals(items[i]));
            final Color idleBg = new Color(21, 101, 192);
            final Color hoverBg = sidebarItemActive;

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

            if ("Dashboard".equals(items[i])) {
                b.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> {
                        JFrame f = new AdminFrame(currentUser);
                        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        showSingleWindow(f);
                    });
                });
            }

            g.gridy++;
            g.insets = new Insets(4, 12, 4, 12);
            side.add(b, g);
        }

        g.gridy++; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        side.add(Box.createVerticalGlue(), g);

        return side;
    }

    private JPanel createMainContent() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(pageBg);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();

        JLabel header = new JLabel("Rooms");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 0, 16, 0);
        container.add(header, g);

        // Stats cards row
        JPanel stats = new JPanel(new GridBagLayout());
        stats.setOpaque(false);
        addStatCard(stats, 0, "Total Rooms", lbTotal = new JLabel("0"));
        addStatCard(stats, 1, "Available", lbAvailable = new JLabel("0"));
        addStatCard(stats, 2, "Occupied", lbOccupied = new JLabel("0"));
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(0,0,16,0);
        container.add(stats, g);

        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());

        cbAvailableOnly = new JCheckBox("Show available only");
        cbAvailableOnly.setSelected(true);
        cbAvailableOnly.addActionListener(e -> reloadRooms());

        JButton btnRefresh = primaryButton("Refresh");
        btnRefresh.addActionListener(e -> reloadRooms());

        JButton btnAdd = primaryButton("Add Room");
        btnAdd.addActionListener(e -> addRoom());

        JButton btnUpdate = primaryButton("Update");
        btnUpdate.addActionListener(e -> updateRoom());

        JButton btnAssign = primaryButton("Assign Tenant");
        btnAssign.addActionListener(e -> assignTenant());

        JButton btnUnassign = new JButton("Unassign");
        btnUnassign.setBackground(new Color(183, 28, 28));
        btnUnassign.setForeground(Color.WHITE);
        btnUnassign.setFocusPainted(false);
        btnUnassign.addActionListener(e -> unassignTenant());

        JButton btnRemove = new JButton("Remove Room");
        btnRemove.setBackground(new Color(183, 28, 28));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> removeRoom());

        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0; cg.insets = new Insets(0,0,8,8); cg.anchor = GridBagConstraints.WEST;
        card.add(cbAvailableOnly, cg);
        cg.gridx = 1; card.add(btnRefresh, cg);
        cg.gridx = 2; card.add(btnAdd, cg);
        cg.gridx = 3; card.add(btnUpdate, cg);
        cg.gridx = 4; card.add(btnAssign, cg);
        cg.gridx = 5; card.add(btnUnassign, cg);
        cg.gridx = 6; card.add(btnRemove, cg);

        model = new DefaultTableModel(new Object[]{"ID", "Room Number", "Capacity", "Occupied", "Status", "Occupants"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        // Status colored renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        // Compact ID column
        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane sp = new JScrollPane(table);

        cg.gridx = 0; cg.gridy = 1; cg.gridwidth = 7; cg.weightx = 1; cg.weighty = 1; cg.fill = GridBagConstraints.BOTH;
        card.add(sp, cg);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.fill = GridBagConstraints.BOTH; g.weightx = 1; g.weighty = 1;
        container.add(card, g);

        reloadRooms();
        reloadStats();
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

    private void reloadRooms() {
        model.setRowCount(0);
        String base = "SELECT r.id, r.room_number, r.capacity, r.status, " +
                "COUNT(ra.user_id) AS occupied, " +
                "GROUP_CONCAT(u.username ORDER BY u.username SEPARATOR ', ') AS occupants " +
                "FROM rooms r " +
                "LEFT JOIN room_assignments ra ON ra.room_id = r.id " +
                "LEFT JOIN users u ON u.id = ra.user_id " +
                "GROUP BY r.id, r.room_number, r.capacity, r.status ";
        String q = base + (cbAvailableOnly.isSelected() ? "HAVING occupied < r.capacity " : "") + "ORDER BY r.room_number";
        try (Connection c = DBUtil.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String rn = rs.getString(2);
                int cap = rs.getInt(3);
                String status = rs.getString(4);
                int occupied = rs.getInt(5);
                String occupants = rs.getString(6);
                if (occupants == null) occupants = "";
                model.addRow(new Object[]{id, rn, cap, occupied + "/" + cap, status, occupants});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadStats() {
        String q = "SELECT "+
                "(SELECT COUNT(*) FROM rooms) AS total, "+
                "(SELECT COUNT(*) FROM rooms WHERE status='available') AS available, "+
                "(SELECT COUNT(*) FROM rooms WHERE status='occupied') AS occupied";
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
            if (rs.next()) {
                lbTotal.setText(String.valueOf(rs.getInt(1)));
                lbAvailable.setText(String.valueOf(rs.getInt(2)));
                lbOccupied.setText(String.valueOf(rs.getInt(3)));
            }
        } catch (SQLException ignored) { }
    }

    private void addRoom() {
        JTextField tfNumber = new JTextField();
        JTextField tfCap = new JTextField("1");
        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        p.add(new JLabel("Room Number"));
        p.add(tfNumber);
        p.add(new JLabel("Capacity"));
        p.add(tfCap);
        int res = JOptionPane.showConfirmDialog(this, p, "Add Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String num = tfNumber.getText().trim();
        int cap;
        try { cap = Integer.parseInt(tfCap.getText().trim()); } catch (Exception e) { cap = 1; }
        if (num.isEmpty()) return;
        String sql = "INSERT INTO rooms (room_number, capacity, status) VALUES (?,?, 'available')";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, num);
            ps.setInt(2, cap);
            ps.executeUpdate();
            reloadRooms();
            reloadStats();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String currentNumber = String.valueOf(model.getValueAt(row, 1));
        int currentCap = 1;
        try { currentCap = Integer.parseInt(String.valueOf(model.getValueAt(row, 2))); } catch (Exception ignored) {}

        JTextField tfNumber = new JTextField(currentNumber);
        JTextField tfCap = new JTextField(String.valueOf(currentCap));
        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        p.add(new JLabel("Room Number"));
        p.add(tfNumber);
        p.add(new JLabel("Capacity"));
        p.add(tfCap);
        int res = JOptionPane.showConfirmDialog(this, p, "Update Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String num = tfNumber.getText().trim();
        int cap;
        try { cap = Integer.parseInt(tfCap.getText().trim()); } catch (Exception e) { cap = currentCap; }
        if (num.isEmpty()) return;
        String sql = "UPDATE rooms SET room_number=?, capacity=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, num);
            ps.setInt(2, cap);
            ps.setInt(3, id);
            ps.executeUpdate();
            reloadRooms();
            reloadStats();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignTenant() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        int id = (int) model.getValueAt(row, 0);
        // check occupancy vs capacity
        int[] info = loadRoomOccupancyAndCapacity(id);
        int occ = info[0], cap = info[1];
        if (occ >= cap) { JOptionPane.showMessageDialog(this, "Room is full."); return; }
        List<UserItem> tenants = loadUnassignedTenants();
        if (tenants.isEmpty()) { JOptionPane.showMessageDialog(this, "No tenants found."); return; }
        JComboBox<UserItem> combo = new JComboBox<>(tenants.toArray(new UserItem[0]));
        int res = JOptionPane.showConfirmDialog(this, combo, "Assign to tenant", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        UserItem selected = (UserItem) combo.getSelectedItem();
        if (selected == null) return;
        String ins = "INSERT INTO room_assignments (room_id, user_id) VALUES (?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(ins)) {
            ps.setInt(1, id);
            ps.setInt(2, selected.id);
            ps.executeUpdate();
            recalcRoomStatus(c, id);
            reloadRooms();
            reloadStats();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unassignTenant() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        int id = (int) model.getValueAt(row, 0);
        List<UserItem> occupants = loadRoomOccupants(id);
        if (occupants.isEmpty()) { JOptionPane.showMessageDialog(this, "No occupants to unassign."); return; }
        JComboBox<UserItem> combo = new JComboBox<>(occupants.toArray(new UserItem[0]));
        int res = JOptionPane.showConfirmDialog(this, combo, "Select tenant to unassign", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        UserItem sel = (UserItem) combo.getSelectedItem();
        if (sel == null) return;
        String del = "DELETE FROM room_assignments WHERE room_id=? AND user_id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(del)) {
            ps.setInt(1, id);
            ps.setInt(2, sel.id);
            ps.executeUpdate();
            recalcRoomStatus(c, id);
            reloadRooms();
            reloadStats();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<UserItem> loadUnassignedTenants() {
        List<UserItem> list = new ArrayList<>();
        String q = "SELECT u.id, u.username FROM users u WHERE u.role='tenant' " +
                "AND u.id NOT IN (SELECT user_id FROM room_assignments) ORDER BY u.username";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UserItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ignored) { }
        return list;
    }

    private List<UserItem> loadRoomOccupants(int roomId) {
        List<UserItem> list = new ArrayList<>();
        String q = "SELECT u.id, u.username FROM room_assignments ra JOIN users u ON u.id=ra.user_id WHERE ra.room_id=? ORDER BY u.username";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserItem(rs.getInt(1), rs.getString(2)));
                }
            }
        } catch (SQLException ignored) { }
        return list;
    }

    private int[] loadRoomOccupancyAndCapacity(int roomId) {
        String q = "SELECT (SELECT COUNT(*) FROM room_assignments WHERE room_id=?), (SELECT capacity FROM rooms WHERE id=?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, roomId);
            ps.setInt(2, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new int[]{rs.getInt(1), rs.getInt(2)};
            }
        } catch (SQLException ignored) { }
        return new int[]{0, 0};
    }

    private void recalcRoomStatus(Connection c, int roomId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE rooms r SET r.status = CASE " +
                        "WHEN (SELECT COUNT(*) FROM room_assignments ra WHERE ra.room_id=r.id) >= r.capacity THEN 'occupied' " +
                        "ELSE 'available' END WHERE r.id=?")) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }

    static class UserItem {
        final int id; final String name;
        UserItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
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
