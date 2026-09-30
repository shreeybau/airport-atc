package atc;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ReportPanel extends JPanel implements Refreshable {
    private final Map<String, String> reports = new LinkedHashMap<>();
    private final JComboBox<String> choice;
    private final JTable table = Ui.table();

    public ReportPanel() {
        reports.put("Flight schedule",
            "SELECT f.flight_no, al.name AS airline, f.origin, f.destination, f.sched_arrival, f.sched_departure, f.status " +
            "FROM flights f JOIN aircraft a ON a.aircraft_id=f.aircraft_id JOIN airlines al ON al.airline_id=a.airline_id " +
            "ORDER BY f.sched_arrival");
        reports.put("Gate occupancy",
            "SELECT g.terminal, g.gate_no, g.size_capacity AS capacity, g.status, COUNT(a.alloc_id) AS bookings " +
            "FROM gates g LEFT JOIN gate_allocations a ON a.gate_id=g.gate_id " +
            "GROUP BY g.gate_id, g.terminal, g.gate_no, g.size_capacity, g.status ORDER BY g.terminal, g.gate_no");
        reports.put("Flights without a gate",
            "SELECT f.flight_no, f.origin, f.destination, f.sched_arrival, f.sched_departure, f.status FROM flights f " +
            "WHERE NOT EXISTS (SELECT 1 FROM gate_allocations a WHERE a.flight_id=f.flight_id) ORDER BY f.sched_arrival");
        reports.put("Runway usage",
            "SELECT r.name AS runway, r.status, ra.operation, COUNT(ra.assign_id) AS movements " +
            "FROM runways r LEFT JOIN runway_assignments ra ON ra.runway_id=r.runway_id " +
            "GROUP BY r.runway_id, r.name, r.status, ra.operation ORDER BY r.name");
        choice = new JComboBox<>(reports.keySet().toArray(new String[0]));

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton run = new JButton("Run Report");
        run.addActionListener(e -> refresh());
        add(Ui.row(new JLabel("Report:"), choice, run), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            Ui.show(table, DBConnection.query(reports.get((String) choice.getSelectedItem())));
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }
}
