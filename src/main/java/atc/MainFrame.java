package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    public MainFrame(String user, String role) {
        super("Airport ATC & Gate Allocation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);

        boolean admin = role.equals("ADMIN");
        boolean atc = admin || role.equals("ATC");
        boolean gate = admin || role.equals("GATE_MANAGER");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", new DashboardPanel());
        tabs.addTab("Flights", new FlightPanel(admin));
        if (gate) tabs.addTab("Gate Allocation", new GatePanel());
        if (atc) tabs.addTab("Runway / ATC", new RunwayPanel());
        tabs.addTab("Reports", new ReportPanel());
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });

        JLabel who = new JLabel("Logged in as " + user + "  [" + role + "]");
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8, 12, 8, 12));
        top.add(who, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);
        top.setBackground(new Color(30, 58, 95));
        who.setForeground(Color.WHITE);
        who.setFont(who.getFont().deriveFont(Font.BOLD, 15f));

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        ((Refreshable) tabs.getComponentAt(0)).refresh();
    }
}