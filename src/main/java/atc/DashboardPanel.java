package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class DashboardPanel extends JPanel implements Refreshable {
    private final String[] titles = {"Flights", "Free Gates", "Occupied Gates", "Active Runways", "Gate Allocations"};
    private final String[] sql = {
        "SELECT COUNT(*) FROM flights",
        "SELECT COUNT(*) FROM gates WHERE status='FREE'",
        "SELECT COUNT(*) FROM gates WHERE status='OCCUPIED'",
        "SELECT COUNT(*) FROM runways WHERE status='ACTIVE'",
        "SELECT COUNT(*) FROM gate_allocations"};
    private final JLabel[] values = new JLabel[5];
    private final JTable table = Ui.table();

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel cards = new JPanel(new GridLayout(1, 5, 10, 0));
        Color[] colors = {new Color(220, 235, 255), Ui.GREEN, Ui.RED, new Color(225, 245, 254), Ui.ORANGE};
        for (int i = 0; i < 5; i++) {
            values[i] = new JLabel("-", SwingConstants.CENTER);
            values[i].setFont(values[i].getFont().deriveFont(Font.BOLD, 32f));
            JLabel t = new JLabel(titles[i], SwingConstants.CENTER);
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(colors[i]);
            card.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(14, 10, 14, 10)));
            card.add(values[i], BorderLayout.CENTER);
            card.add(t, BorderLayout.SOUTH);
            cards.add(card);
        }
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(cards, BorderLayout.CENTER);
        top.add(Ui.row(refresh), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(Ui.titled("Flights and assigned gates", table), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            for (int i = 0; i < 5; i++) values[i].setText(String.valueOf(((Number) DBConnection.scalar(sql[i])).longValue()));
            Ui.show(table, DBConnection.query(
                "SELECT f.flight_no, f.origin, f.destination, f.sched_arrival, f.sched_departure, f.status, " +
                "CONCAT(g.terminal,'-',g.gate_no) AS gate FROM flights f " +
                "LEFT JOIN gate_allocations a ON a.flight_id=f.flight_id LEFT JOIN gates g ON g.gate_id=a.gate_id " +
                "ORDER BY f.sched_arrival LIMIT 20"));
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }
}
