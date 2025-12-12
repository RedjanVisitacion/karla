import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AnnouncementsFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;

    private JTable table;
    private DefaultTableModel model;

    public AnnouncementsFrame(User user) {
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
                "Dashboard", "Rooms", "Payments & Billing", "Maintenance Requests", "Announcements"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (String label : items) {
            final boolean active = "Announcements".equals(label);
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

        JLabel header = new JLabel("Announcements"); header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0,0,16,0);
        container.add(header, g);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(33, 150, 243));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            SwingUtilities.invokeLater(() -> {
                JFrame f = new LoginFrame();
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                showSingleWindow(f);
            });
        });
        g.gridx = 1; g.gridy = 0; g.gridwidth = 1; g.weightx = 0; g.anchor = GridBagConstraints.EAST;
        container.add(btnLogout, g);

        // Compose panel: form on top, table below
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        JLabel lblTitle = new JLabel("Title:"); lblTitle.setFont(mainFont);
        JTextField tfTitle = new JTextField(24);
        JLabel lblBody = new JLabel("Body:"); lblBody.setFont(mainFont);
        JTextArea taBody = new JTextArea(4, 24); taBody.setLineWrap(true); taBody.setWrapStyleWord(true);
        JScrollPane spBody = new JScrollPane(taBody);
        JButton btnPost = new JButton("Post Announcement"); btnPost.setBackground(new Color(33,150,243)); btnPost.setForeground(Color.WHITE); btnPost.setFocusPainted(false);
        btnPost.addActionListener(e -> {
            String t = tfTitle.getText().trim(); String b = taBody.getText().trim();
            if (t.isEmpty() || b.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter a title and body."); return; }
            String ins = "INSERT INTO announcements (title, body, created_by) VALUES (?,?,?)";
            try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(ins)) {
                ps.setString(1, t); ps.setString(2, b); ps.setInt(3, currentUser.getId());
                ps.executeUpdate();
                tfTitle.setText(""); taBody.setText("");
                reload();
                JOptionPane.showMessageDialog(this, "Announcement posted.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        GridBagConstraints fg = new GridBagConstraints();
        fg.gridx = 0; fg.gridy = 0; fg.insets = new Insets(0,0,6,8); fg.anchor = GridBagConstraints.WEST; form.add(lblTitle, fg);
        fg.gridx = 1; form.add(tfTitle, fg);
        fg.gridx = 0; fg.gridy = 1; form.add(lblBody, fg);
        fg.gridx = 1; form.add(spBody, fg);
        fg.gridx = 1; fg.gridy = 2; fg.insets = new Insets(8,0,0,0); fg.anchor = GridBagConstraints.EAST; form.add(btnPost, fg);

        JLabel listTitle = new JLabel("Recent Announcements"); listTitle.setFont(titleFont);
        JPanel listTop = new JPanel(new BorderLayout()); listTop.setOpaque(false);
        listTop.add(listTitle, BorderLayout.WEST);
        JPanel listActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); listActions.setOpaque(false);
        JButton btnEdit = new JButton("Edit Selected"); btnEdit.addActionListener(e -> editSelected());
        JButton btnRefresh = new JButton("Refresh"); btnRefresh.addActionListener(e -> reload());
        listActions.add(btnEdit); listActions.add(btnRefresh);
        listTop.add(listActions, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(form, BorderLayout.WEST);
        top.add(listTop, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Date", "Title", "Author", "Body"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.weightx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        container.add(card, g);

        reload();
        return container;
    }

    private void reload() {
        model.setRowCount(0);
        String q = "SELECT a.id, a.created_at, a.title, u.username, a.body FROM announcements a JOIN users u ON u.id=a.created_by ORDER BY a.created_at DESC, a.id DESC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd");
            while (rs.next()) {
                int id = rs.getInt(1);
                Timestamp ts = rs.getTimestamp(2);
                String date = ts != null ? df.format(ts) : "-";
                model.addRow(new Object[]{id, date, rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an announcement first."); return; }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        String currentTitle = String.valueOf(model.getValueAt(row, 2));
        String currentBody = String.valueOf(model.getValueAt(row, 4));

        JTextField tfTitle = new JTextField(currentTitle, 24);
        JTextArea taBody = new JTextArea(currentBody, 6, 24); taBody.setLineWrap(true); taBody.setWrapStyleWord(true);
        JScrollPane spBody = new JScrollPane(taBody);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0,0,6,8); g.anchor = GridBagConstraints.WEST; p.add(new JLabel("Title:"), g);
        g.gridx = 1; p.add(tfTitle, g);
        g.gridx = 0; g.gridy = 1; p.add(new JLabel("Body:"), g);
        g.gridx = 1; p.add(spBody, g);

        int res = JOptionPane.showConfirmDialog(this, p, "Edit Announcement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String newTitle = tfTitle.getText().trim();
            String newBody = taBody.getText().trim();
            if (newTitle.isEmpty() || newBody.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter a title and body."); return; }
            String upd = "UPDATE announcements SET title=?, body=? WHERE id=?";
            try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, newTitle); ps.setString(2, newBody); ps.setInt(3, id);
                ps.executeUpdate();
                reload();
                JOptionPane.showMessageDialog(this, "Announcement updated.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
