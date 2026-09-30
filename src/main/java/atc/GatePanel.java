package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GatePanel extends JPanel implements Refreshable {
    private final JComboBox<String> flight = new JComboBox<>();
    private final JTable avail = Ui.table(), gates = Ui.table(), allocs = Ui.table();

    public GatePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton find = new JButton("Find Free Gates"), alloc = new JButton("Allocate Selected Gate"),
                rel = new JButton("Release Selected Allocation"), maint = new JButton("Set Maintenance"),
                free = new JButton("Clear Maintenance");
        find.addActionListener(e -> find());
        alloc.addActionListener(e -> allocate());
        rel.addActionListener(e -> release());
        maint.addActionListener(e -> maintenance(true));
        free.addActionListener(e -> maintenance(false));
        add(Ui.row(new JLabel("Flight:"), flight, find, alloc, rel, maint, free), BorderLayout.NORTH);

        JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                Ui.titled("Available gates for selected flight", avail), Ui.titled("All gates (status)", gates));
        top.setResizeWeight(0.5);
        JSplitPane all = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, Ui.titled("Current allocations", allocs));
        all.setResizeWeight(0.55);
        add(all, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            Object keep = flight.getSelectedItem();
            flight.removeAllItems();
            DefaultTableModel m = DBConnection.query("SELECT flight_id, flight_no, origin, destination FROM flights ORDER BY sched_arrival");
            for (int i = 0; i < m.getRowCount(); i++)
                flight.addItem(m.getValueAt(i, 0) + " - " + m.getValueAt(i, 1) + " (" + m.getValueAt(i, 2) + " > " + m.getValueAt(i, 3) + ")");
            if (keep != null) flight.setSelectedItem(keep);
            Ui.show(gates, GateDAO.all());
            Ui.show(allocs, AllocationDAO.list());
            if (flight.getSelectedItem() != null) find();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void find() {
        try {
            Ui.show(avail, GateDAO.available(Ui.id(flight.getSelectedItem())));
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void allocate() {
        try {
            Object gate = Ui.sel(avail, 0);
            if (gate == null) throw new IllegalArgumentException("Select a gate from the available gates table");
            AllocationDAO.allocate(Ui.id(flight.getSelectedItem()), Integer.parseInt(gate.toString()));
            Ui.msg(this, "Gate allocated successfully");
            refresh();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void release() {
        try {
            Object id = Ui.sel(allocs, 0);
            if (id == null) throw new IllegalArgumentException("Select an allocation to release");
            AllocationDAO.release(Integer.parseInt(id.toString()));
            refresh();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void maintenance(boolean on) {
        try {
            Object id = Ui.sel(gates, 0);
            if (id == null) throw new IllegalArgumentException("Select a gate in the 'All gates' table");
            GateDAO.setMaintenance(Integer.parseInt(id.toString()), on);
            refresh();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }
}
