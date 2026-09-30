package atc;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/** Shared GUI helpers: dialogs, tables with colour-coded status, form layout. */
public class Ui {
    static final Color GREEN = new Color(198, 239, 206), RED = new Color(255, 199, 206), ORANGE = new Color(255, 224, 170);

    public static void err(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void msg(Component parent, String m) {
        JOptionPane.showMessageDialog(parent, m, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Parses "yyyy-MM-dd HH:mm". */
    public static Timestamp ts(String s) {
        try {
            return Timestamp.valueOf(LocalDateTime.parse(s.trim().replace(' ', 'T')));
        } catch (Exception e) {
            throw new IllegalArgumentException("Date/time must look like 2026-10-01 14:30");
        }
    }

    /** Leading integer id from strings like "3 - AI101". */
    public static int id(Object comboItem) {
        if (comboItem == null) throw new IllegalArgumentException("Please make a selection");
        return Integer.parseInt(comboItem.toString().split(" - ")[0].trim());
    }

    public static JTable table() {
        JTable t = new JTable();
        t.setRowHeight(30);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        t.getTableHeader().setPreferredSize(new Dimension(0, 34));
        return t;
    }

    public static void show(JTable t, DefaultTableModel m) {
        t.setModel(m);
        for (int i = 0; i < t.getColumnCount(); i++)
            if ("status".equalsIgnoreCase(t.getColumnName(i)))
                t.getColumnModel().getColumn(i).setCellRenderer(new StatusRenderer());
    }

    /** Value in column {@code col} of the selected row (model index), or null. */
    public static Object sel(JTable t, int col) {
        int r = t.getSelectedRow();
        return r < 0 ? null : t.getModel().getValueAt(t.convertRowIndexToModel(r), col);
    }

    public static JScrollPane titled(String title, JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new TitledBorder(title));
        return sp;
    }

    public static JPanel form(String[] labels, JComponent[] fields, int perRow) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = (i % perRow) * 2;
            g.gridy = i / perRow;
            g.weightx = 0;
            p.add(new JLabel(labels[i]), g);
            g.gridx++;
            g.weightx = 1;
            p.add(fields[i], g);
        }
        return p;
    }

    public static JPanel row(Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (Component c : items) p.add(c);
        return p;
    }

    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            if (!sel) {
                switch (String.valueOf(v)) {
                    case "FREE": case "ACTIVE": case "SCHEDULED": case "LANDED": case "DEPARTED":
                        setBackground(GREEN); break;
                    case "OCCUPIED": case "CLOSED": case "CANCELLED":
                        setBackground(RED); break;
                    case "MAINTENANCE": case "DELAYED":
                        setBackground(ORANGE); break;
                    default:
                        setBackground(t.getBackground());
                }
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
