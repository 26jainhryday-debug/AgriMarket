import dao.*;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import model.*;
import util.DBConnection;

/**
 * AgriMarket - Java Swing Frontend
 * Drop this file into your src/ directory alongside Main.java.
 * Run: javac -cp .:sqlite-jdbc.jar $(find . -name "*.java") && java -cp .:sqlite-jdbc.jar AgriApp
 */
public class AgriApp {

    // ── Palette ──────────────────────────────────────────────────────────────
    static final Color BG        = new Color(0xF5F0E8);
    static final Color PANEL_BG  = new Color(0xFFFDF7);
    static final Color ACCENT    = new Color(0x3D7A3A);   // deep forest green
    static final Color ACCENT2   = new Color(0xE8A020);   // harvest amber
    static final Color SIDEBAR   = new Color(0x2A4A27);   // dark bark
    static final Color TEXT      = new Color(0x1A2E18);
    static final Color TEXT_MUTED= new Color(0x7A8C78);
    static final Color ROW_ALT   = new Color(0xEFF6EE);
    static final Color DANGER    = new Color(0xC0392B);
    static final Color BORDER    = new Color(0xD4C9B0);

    static FarmerDAO  fdao;
    static ProductDAO pdao;
    static UserDAO    udao;
    static String     currentRole;
    static JLabel farmersCountLbl;
    static JLabel productsCountLbl;
    static JLabel lowStockCountLbl;

    public static void main(String[] args) {
        // Init DAOs & tables
        fdao = new FarmerDAO();
        pdao = new ProductDAO();
        udao = new UserDAO();
        fdao.createTable();
        pdao.createTable();
        udao.createTable();

        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(AgriApp::showLogin);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIN SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    static void showLogin() {
        JFrame f = new JFrame("AgriMarket");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(480, 600);
        f.setLocationRelativeTo(null);
        f.setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // subtle grain background
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // top decorative band
                g2.setColor(SIDEBAR);
                g2.fillRect(0, 0, getWidth(), 220);
                // leaf motif
                g2.setColor(new Color(0x3D7A3A, true));
                for (int i = 0; i < 6; i++) {
                    g2.setColor(new Color(255,255,255, 15));
                    g2.fillOval(-40 + i*90, -40 + i*30, 120, 120);
                }
            }
        };

        // ── Logo area ─────────────────────────────────────────────────────────
        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(480, 220));

        JLabel icon = new JLabel("🌾");
        icon.setFont(new Font("Serif", Font.PLAIN, 56));
        JLabel title = new JLabel("AgriMarket");
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Farm Management System");
        sub.setFont(new Font("Georgia", Font.ITALIC, 13));
        sub.setForeground(new Color(0xAAC9A8));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0,0,4,0);
        top.add(icon, gc);
        gc.gridy = 1;
        top.add(title, gc);
        gc.gridy = 2;
        top.add(sub, gc);

        // ── Form card ─────────────────────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(36, 40, 36, 40));

        JLabel heading = new JLabel("Sign in");
        heading.setFont(new Font("Georgia", Font.BOLD, 22));
        heading.setForeground(TEXT);

        JTextField userField = styledField("Username");
        JPasswordField passField = styledPassField("Password");
        JButton loginBtn = styledButton("Sign In", ACCENT, Color.WHITE);
        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errLabel.setForeground(DANGER);
        errLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.gridx = 0; c.insets = new Insets(0,0,16,0);

        c.gridy = 0; card.add(heading, c);
        c.gridy = 1; c.insets = new Insets(0,0,10,0); card.add(label("Username"), c);
        c.gridy = 2; c.insets = new Insets(0,0,16,0); card.add(userField, c);
        c.gridy = 3; c.insets = new Insets(0,0,10,0); card.add(label("Password"), c);
        c.gridy = 4; c.insets = new Insets(0,0,24,0); card.add(passField, c);
        c.gridy = 5; c.insets = new Insets(0,0,8,0);  card.add(loginBtn, c);
        c.gridy = 6; c.insets = new Insets(0,0,0,0);  card.add(errLabel, c);

        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            String role = udao.login(u, p);
            if (role == null) {
                errLabel.setText("Invalid username or password.");
                shake(card);
            } else {
                currentRole = role;
                f.dispose();
                showDashboard(u);
            }
        });

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        GridBagConstraints oc = new GridBagConstraints();
        oc.insets = new Insets(0, 28, 28, 28); oc.fill = GridBagConstraints.BOTH;
        oc.weightx = 1; oc.weighty = 1;
        outer.add(card, oc);

        root.add(top, BorderLayout.NORTH);
        root.add(outer, BorderLayout.CENTER);

        f.setContentPane(root);
        f.setVisible(true);

        // Press Enter to login
        f.getRootPane().setDefaultButton(loginBtn);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MAIN DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    static void showDashboard(String username) {
        JFrame f = new JFrame("AgriMarket — Dashboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1100, 700);
        f.setLocationRelativeTo(null);

        // ── Sidebar ───────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel navTop = new JPanel();
        navTop.setOpaque(false);
        navTop.setLayout(new BoxLayout(navTop, BoxLayout.Y_AXIS));
        navTop.setBorder(new EmptyBorder(24, 0, 0, 0));

        JLabel logo = new JLabel("🌾 AgriMarket");
        logo.setFont(new Font("Georgia", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(0, 20, 24, 20));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTop.add(logo);

        JLabel userInfo = new JLabel("  " + username + " · " + currentRole);
        userInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        userInfo.setForeground(new Color(0x88AA86));
        userInfo.setBorder(new EmptyBorder(0, 20, 20, 20));
        userInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTop.add(userInfo);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xFFFFFF, true));
        sep.setMaximumSize(new Dimension(220, 1));
        navTop.add(sep);
        navTop.add(Box.createRigidArea(new Dimension(0, 8)));

        sidebar.add(navTop, BorderLayout.NORTH);

        // Content area (CardLayout)
        JPanel content = new JPanel(new CardLayout());
        content.setBackground(BG);

        // Nav buttons
        String[] pages = {"Dashboard", "Farmers", "Products"};
        String[] icons = {"📊", "👨‍🌾", "🛒"};
        ButtonGroup bg = new ButtonGroup();
        JPanel navMid = new JPanel();
        navMid.setOpaque(false);
        navMid.setLayout(new BoxLayout(navMid, BoxLayout.Y_AXIS));

        JPanel dashPanel   = buildDashPanel();
        JPanel farmerPanel = buildFarmerPanel();
        JPanel productPanel= buildProductPanel();

        content.add(dashPanel,    "Dashboard");
        content.add(farmerPanel,  "Farmers");
        content.add(productPanel, "Products");

        for (int i = 0; i < pages.length; i++) {
            String page = pages[i];
            JToggleButton btn = navButton(icons[i] + "  " + page);
            bg.add(btn);
            if (i == 0) btn.setSelected(true);
            btn.addActionListener(e -> {
                ((CardLayout) content.getLayout()).show(content, page);
            });
            navMid.add(btn);
            navMid.add(Box.createRigidArea(new Dimension(0, 2)));
        }
        sidebar.add(navMid, BorderLayout.CENTER);

        // Logout
        JButton logoutBtn = new JButton("⟵  Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(0xFFAAAA));
        logoutBtn.setBackground(new Color(0x1A3018));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(220, 44));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBorder(new EmptyBorder(0, 20, 0, 0));
        logoutBtn.addActionListener(e -> { f.dispose(); showLogin(); });
        JPanel navBot = new JPanel(new BorderLayout());
        navBot.setOpaque(false);
        navBot.setBorder(new EmptyBorder(0, 0, 16, 0));
        navBot.add(logoutBtn, BorderLayout.CENTER);
        sidebar.add(navBot, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, content);
        split.setDividerSize(0);
        split.setEnabled(false);

        f.setContentPane(split);
        f.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DASHBOARD PANEL (summary stats)
    // ══════════════════════════════════════════════════════════════════════════
    static JPanel buildDashPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(32, 32, 32, 32));

        JLabel title = new JLabel("Overview");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);

        cards.add(statCard("Total Farmers", countTable("farmers"), "👨‍🌾", ACCENT));
        cards.add(statCard("Total Products", countTable("products"), "🛒", ACCENT2));
        cards.add(statCard("Low Stock Items", countLowStock(), "⚠️", DANGER));

        p.add(title, BorderLayout.NORTH);
        p.add(cards, BorderLayout.CENTER);
        return p;
    }

    static JPanel statCard(String label, int value, String emoji, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL_BG);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                // accent bar top
                g2.fillRoundRect(0,0,getWidth(),8,4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(200, 140));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;
        JLabel em = new JLabel(emoji);
        em.setFont(new Font("Serif", Font.PLAIN, 30));
        card.add(em, gc);

        gc.gridy = 1; gc.insets = new Insets(8,0,4,0);
        JLabel val = new JLabel(String.valueOf(value));

        if (label.equals("Total Farmers")) farmersCountLbl = val;
        if (label.equals("Total Products")) productsCountLbl = val;
        if (label.equals("Low Stock Items")) lowStockCountLbl = val;

        val.setFont(new Font("Georgia", Font.BOLD, 38));
        val.setForeground(accent);
        card.add(val, gc);

        gc.gridy = 2; gc.insets = new Insets(0,0,0,0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        card.add(lbl, gc);

        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FARMERS PANEL
    // ══════════════════════════════════════════════════════════════════════════
    static JPanel buildFarmerPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(32, 32, 32, 32));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Farmers");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT);

        JButton addBtn = styledButton("+ Add Farmer", ACCENT, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(140, 38));
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Name", "Age", "Location"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        loadFarmers(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(styledPanelBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        addBtn.addActionListener(e -> {
            showAddFarmerDialog(model);
        });

        p.add(header, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    static void showAddFarmerDialog(DefaultTableModel model) {
        JDialog dlg = new JDialog((Frame)null, "Add Farmer", true);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(null);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JTextField nameF = styledField("e.g. Ramesh Kumar");
        JTextField ageF  = styledField("e.g. 42");
        JTextField locF  = styledField("e.g. Pune");
        JButton    save  = styledButton("Save Farmer", ACCENT, Color.WHITE);
        JLabel     err   = new JLabel(" ");
        err.setForeground(DANGER);
        err.setFont(new Font("SansSerif", Font.PLAIN, 11));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0;
        c.insets = new Insets(0,0,8,0);

        c.gridy = 0; panel.add(label("Full Name"), c);
        c.gridy = 1; panel.add(nameF, c);
        c.gridy = 2; panel.add(label("Age"), c);
        c.gridy = 3; panel.add(ageF, c);
        c.gridy = 4; panel.add(label("Location"), c);
        c.gridy = 5; panel.add(locF, c);
        c.gridy = 6; c.insets = new Insets(16,0,6,0); panel.add(save, c);
        c.gridy = 7; c.insets = new Insets(0,0,0,0);  panel.add(err, c);

        save.addActionListener(e -> {
            try {
                String name = nameF.getText().trim();
                int age = Integer.parseInt(ageF.getText().trim());
                String loc = locF.getText().trim();
                if (name.isEmpty() || loc.isEmpty()) { err.setText("All fields required."); return; }
                fdao.add(new Farmer(0, name, age, loc));
                loadFarmers(model);
                refreshDashboard();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                err.setText("Age must be a number.");
            }
        });

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    static void loadFarmers(DefaultTableModel model) {
        model.setRowCount(0);

        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM farmers")
        ) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("location")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRODUCTS PANEL
    // ══════════════════════════════════════════════════════════════════════════
    static JPanel buildProductPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(32, 32, 32, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Products");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT);

        JButton addBtn = styledButton("+ Add Product", ACCENT2, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(150, 38));
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        String[] cols = {"ID", "Farmer ID", "Name", "Quantity", "Price (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                if ("⚠ Low Stock".equals(val)) lbl.setForeground(DANGER);
                else lbl.setForeground(ACCENT);
                return lbl;
            }
        });
        loadProducts(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(styledPanelBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        addBtn.addActionListener(e -> showAddProductDialog(model));

        p.add(header, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    static void showAddProductDialog(DefaultTableModel model) {
        JDialog dlg = new JDialog((Frame)null, "Add Product", true);
        dlg.setSize(400, 380);
        dlg.setLocationRelativeTo(null);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JTextField farmerIdF  = styledField("Farmer ID");
        JTextField nameF      = styledField("e.g. Wheat");
        JTextField quantityF  = styledField("e.g. 100");
        JTextField priceF     = styledField("e.g. 25.50");
        JButton    save       = styledButton("Save Product", ACCENT2, Color.WHITE);
        JLabel     err        = new JLabel(" ");
        err.setForeground(DANGER);
        err.setFont(new Font("SansSerif", Font.PLAIN, 11));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0;
        c.insets = new Insets(0,0,8,0);

        c.gridy = 0; panel.add(label("Farmer ID"), c);
        c.gridy = 1; panel.add(farmerIdF, c);
        c.gridy = 2; panel.add(label("Product Name"), c);
        c.gridy = 3; panel.add(nameF, c);
        c.gridy = 4; panel.add(label("Quantity"), c);
        c.gridy = 5; panel.add(quantityF, c);
        c.gridy = 6; panel.add(label("Price per unit (₹)"), c);
        c.gridy = 7; panel.add(priceF, c);
        c.gridy = 8; c.insets = new Insets(16,0,6,0); panel.add(save, c);
        c.gridy = 9; c.insets = new Insets(0,0,0,0);  panel.add(err, c);

        save.addActionListener(e -> {
            try {
                int fid = Integer.parseInt(farmerIdF.getText().trim());
                String name = nameF.getText().trim();
                int qty = Integer.parseInt(quantityF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                if (name.isEmpty()) { err.setText("Product name required."); return; }
                pdao.add(new Product(0, fid, name, qty, price));
                loadProducts(model);
                refreshDashboard();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                err.setText("Check numeric fields.");
            }
        });

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    static void loadProducts(DefaultTableModel model) {
        model.setRowCount(0);

        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM products")
        ) {
            while (rs.next()) {
                int qty = rs.getInt("quantity");

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("farmer_id"),
                    rs.getString("name"),
                    qty,
                    String.format("₹%.2f", rs.getDouble("price")),
                    qty < 50 ? "⚠ Low Stock" : "✓ OK"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DB HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    static int countTable(String table) {
        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static int countLowStock() {
        try (
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products WHERE quantity < 50")
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static void refreshDashboard() {
        if (farmersCountLbl != null)
            farmersCountLbl.setText(String.valueOf(countTable("farmers")));

        if (productsCountLbl != null)
            productsCountLbl.setText(String.valueOf(countTable("products")));

        if (lowStockCountLbl != null)
            lowStockCountLbl.setText(String.valueOf(countLowStock()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI COMPONENT HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight()/2 + 5);
                }
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(new Color(0xF8F3E8));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        f.setPreferredSize(new Dimension(0, 40));
        return f;
    }

    static JPasswordField styledPassField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(new Color(0xF8F3E8));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        f.setPreferredSize(new Dimension(0, 40));
        return f;
    }

    static JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) g2.setColor(bg.darker());
                else g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 42));
        return b;
    }

    static JToggleButton navButton(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(0x3D7A3A));
                    g2.fillRoundRect(12, 2, getWidth()-24, getHeight()-4, 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0xFFFFFF, true));
                    g2.fillRoundRect(12, 2, getWidth()-24, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setForeground(new Color(0xCCDDCA));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(220, 44));
        b.setPreferredSize(new Dimension(220, 44));
        b.setBorder(new EmptyBorder(0, 20, 0, 20));
        return b;
    }

    static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? PANEL_BG : ROW_ALT);
                else c.setBackground(new Color(0xC5E0C4));
                c.setForeground(TEXT);
                return c;
            }
        };
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(SIDEBAR);
        table.getTableHeader().setForeground(new Color(0xAAC9A8));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(new Color(0xC5E0C4));
        table.setFocusable(false);
        return table;
    }

    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    static Border styledPanelBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(0,0,0,0)
        );
    }

    static void shake(JComponent c) {
        Point orig = c.getLocation();
        Timer t = new Timer(30, null);
        int[] seq = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        int[] idx = {0};
        t.addActionListener(e -> {
            if (idx[0] < seq.length) {
                c.setLocation(orig.x + seq[idx[0]++], orig.y);
            } else {
                c.setLocation(orig);
                t.stop();
            }
        });
        t.start();
    }
}