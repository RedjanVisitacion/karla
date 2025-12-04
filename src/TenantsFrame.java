import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TenantsFrame extends JFrame {
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Color sidebarBg = new Color(13, 71, 161);
    private final Color sidebarItemActive = new Color(33, 150, 243);
    private final Color pageBg = new Color(245, 247, 250);
    private final Color cardBorder = new Color(220, 225, 230);
    private final User currentUser;

    private JTable table;
    private DefaultTableModel model;

    public TenantsFrame(User user) {
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
                "Dashboard", "Tenants", "Rooms"
        };

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(16, 16, 16, 16);
        side.add(brand, g);

        for (String label : items) {
            final boolean active = "Tenants".equals(label);
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

        JLabel header = new JLabel("Tenants"); header.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        JLabel title = new JLabel("Tenant List"); title.setFont(titleFont);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> reloadTenants());
        top.add(title, BorderLayout.WEST); top.add(refresh, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Tenant", "Room"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.weightx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        container.add(card, g);

        reloadTenants();
        return container;
    }

    private void reloadTenants() {
        model.setRowCount(0);
        String q = "SELECT u.id, u.username, COALESCE(r.room_number, 'Not assigned') " +
                "FROM users u " +
                "LEFT JOIN room_assignments ra ON ra.user_id = u.id " +
                "LEFT JOIN rooms r ON r.id = ra.room_id " +
                "WHERE u.role='tenant' ORDER BY u.username";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
