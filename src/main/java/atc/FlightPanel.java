package atc;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class FlightPanel extends JPanel implements Refreshable {
    private final JTextField no = new JTextField(10), origin = new JTextField(12), dest = new JTextField(12),
            arr = new JTextField(14), dep = new JTextField(14);
    private final JComboBox<String> aircraft = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>(
            new String[]{"SCHEDULED", "DELAYED", "LANDED", "DEPARTED", "CANCELLED"});
    private final JTable table = Ui.table();

    public FlightPanel(boolean canEdit) {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        arr.setToolTipText("yyyy-MM-dd HH:mm");
        dep.setToolTipText("yyyy-MM-dd HH:mm");

        JPanel form = Ui.form(
            new String[]{"Flight No", "Aircraft", "Origin", "Destination", "Arrival (yyyy-MM-dd HH:mm)", "Departure", "Status"},
            new JComponent[]{no, aircraft, origin, dest, arr, dep, status}, 4);
        JButton add = new JButton("Add"), upd = new JButton("Update"), del = new JButton("Delete"), clr = new JButton("Clear");
        add.addActionListener(e -> save(false));
        upd.addActionListener(e -> save(true));
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        JPanel north = new JPanel(new BorderLayout());
        if (canEdit) {
            north.add(form, BorderLayout.CENTER);
            north.add(Ui.row(add, upd, del, clr), BorderLayout.SOUTH);
        } else {
            north.add(new JLabel("  Read-only view (only ADMIN can modify flights)"), BorderLayout.CENTER);
        }
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) fill(); });
    }

    @Override
    public void refresh() {
        try {
            aircraft.removeAllItems();
            DefaultTableModel m = FlightDAO.aircraft();
            for (int i = 0; i < m.getRowCount(); i++) aircraft.addItem(m.getValueAt(i, 0) + " - " + m.getValueAt(i, 1));
            Ui.show(table, FlightDAO.list());
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void fill() {
        if (table.getSelectedRow() < 0) return;
        no.setText(String.valueOf(Ui.sel(table, 1)));
        String reg = String.valueOf(Ui.sel(table, 2));
        for (int i = 0; i < aircraft.getItemCount(); i++)
            if (aircraft.getItemAt(i).endsWith(" - " + reg)) aircraft.setSelectedIndex(i);
        origin.setText(String.valueOf(Ui.sel(table, 3)));
        dest.setText(String.valueOf(Ui.sel(table, 4)));
        arr.setText(String.valueOf(Ui.sel(table, 5)));
        dep.setText(String.valueOf(Ui.sel(table, 6)));
        status.setSelectedItem(String.valueOf(Ui.sel(table, 7)));
    }

    private void clear() {
        no.setText(""); origin.setText(""); dest.setText(""); arr.setText(""); dep.setText("");
        table.clearSelection();
    }

    private void save(boolean isUpdate) {
        try {
            if (no.getText().trim().isEmpty() || origin.getText().trim().isEmpty() || dest.getText().trim().isEmpty())
                throw new IllegalArgumentException("Flight no, origin and destination are required");
            java.sql.Timestamp a = Ui.ts(arr.getText()), d = Ui.ts(dep.getText());
            if (!d.after(a)) throw new IllegalArgumentException("Departure must be after arrival");
            int ac = Ui.id(aircraft.getSelectedItem());
            if (isUpdate) {
                Object id = Ui.sel(table, 0);
                if (id == null) throw new IllegalArgumentException("Select a flight to update");
                FlightDAO.update(Integer.parseInt(id.toString()), no.getText().trim(), ac, origin.getText().trim(),
                        dest.getText().trim(), a, d, (String) status.getSelectedItem());
            } else {
                FlightDAO.add(no.getText().trim(), ac, origin.getText().trim(), dest.getText().trim(), a, d,
                        (String) status.getSelectedItem());
            }
            refresh();
            clear();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }

    private void delete() {
        try {
            Object id = Ui.sel(table, 0);
            if (id == null) throw new IllegalArgumentException("Select a flight to delete");
            if (JOptionPane.showConfirmDialog(this, "Delete flight " + Ui.sel(table, 1) + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            FlightDAO.delete(Integer.parseInt(id.toString()));
            refresh();
            clear();
        } catch (Exception e) {
            Ui.err(this, e);
        }
    }
}
