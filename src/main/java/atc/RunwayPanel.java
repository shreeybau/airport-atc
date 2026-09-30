package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class RunwayPanel extends JPanel implements Refreshable {
    private final JComboBox<String> flight = new JComboBox<>(), runway = new JComboBox<>(),
            op = new JComboBox<>(new String[]{"LAND", "TAKEOFF"});
    private final JTextField slot = new JTextField(14);
    private final JTable runways = Ui.table(), assigns = Ui.table();

    public RunwayPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton assign = new JButton("Assign Slot"), del = new JButton("Delete Selected Assignment");
        assign.addActionListener(e -> assign());
        del.addActionListener(e -> delete());
        flight.addActionListener(e -> autofill());
        op.addActionListener(e -> autofill());
        add(Ui.row(new JLabel("Flight:"), flight, new JLabel("Runway:"), runway, new JLabel("Operation:"), op,
                new JLabel("Slot:"), slot, assign, del), BorderLayout.NORTH);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, Ui.titled("Runways", runways),
                Ui.titled("Landing / takeoff schedule (min. " + RunwayDAO.SEPARATION_MIN + " min separation)", assigns));
        sp.setResizeWeight(0.3);
        add(sp, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            DefaultTableModel f = DBConnection.query("SELECT flight_id, flight_no FROM flights ORDER BY sched_arrival");
            flight.removeAllItems();
            for (int i = 0; i < f.getRowCount(); i++) flight.addItem(f.getValueAt(i, 0) + " - " + f.getValueAt(i, 1));
            DefaultTableModel r = RunwayDAO.runways();
            runway.removeAllItems();
            for (int i = 0; i < r.getRowCount(); i++) runway.addItem(r.getValueAt(i, 0) + " - " + r.getValueAt(i, 1));
            Ui.show(runways, r);
            Ui.show(assigns, RunwayDAO.assignments());
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void autofill() {
        if (flight.getSelectedItem() == null || op.getSelectedItem() == null) return;
        try {
            String col = "LAND".equals(op.getSelectedItem()) ? "sched_arrival" : "sched_departure"; // fixed strings only
            Object v = DBConnection.scalar("SELECT " + col + " FROM flights WHERE flight_id=?", Ui.id(flight.getSelectedItem()));
            if (v != null) slot.setText(v.toString());
        } catch (Exception ignored) { }
    }

    private void assign() {
        try {
            RunwayDAO.assign(Ui.id(flight.getSelectedItem()), Ui.id(runway.getSelectedItem()),
                    (String) op.getSelectedItem(), Ui.ts(slot.getText()));
            refresh();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void delete() {
        try {
            Object id = Ui.sel(assigns, 0);
            if (id == null) throw new IllegalArgumentException("Select an assignment first");
            RunwayDAO.delete(Integer.parseInt(id.toString()));
            refresh();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }
}
