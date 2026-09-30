package atc;

import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class GateDAO {
    public static DefaultTableModel all() throws SQLException {
        return DBConnection.query("SELECT gate_id, terminal, gate_no, size_capacity AS capacity, status " +
                "FROM gates ORDER BY terminal, gate_no");
    }

    /** Gates that are not in maintenance, big enough for the aircraft, and free during the flight's window. */
    public static DefaultTableModel available(int flightId) throws SQLException {
        DefaultTableModel f = DBConnection.query(
            "SELECT f.sched_arrival, f.sched_departure, a.size_category FROM flights f " +
            "JOIN aircraft a ON a.aircraft_id=f.aircraft_id WHERE f.flight_id=?", flightId);
        if (f.getRowCount() == 0) throw new SQLException("Flight not found");
        Timestamp arr = Timestamp.valueOf(f.getValueAt(0, 0).toString() + ":00");
        Timestamp dep = Timestamp.valueOf(f.getValueAt(0, 1).toString() + ":00");
        String size = f.getValueAt(0, 2).toString();
        return DBConnection.query(
            "SELECT g.gate_id, g.terminal, g.gate_no, g.size_capacity AS capacity, g.status FROM gates g " +
            "WHERE g.status<>'MAINTENANCE' AND FIELD(g.size_capacity,'S','M','L')>=FIELD(?,'S','M','L') " +
            "AND NOT EXISTS (SELECT 1 FROM gate_allocations a WHERE a.gate_id=g.gate_id " +
            "AND a.start_time < ? AND a.end_time > ?) ORDER BY g.terminal, g.gate_no", size, dep, arr);
    }

    public static void setMaintenance(int gateId, boolean maintenance) throws SQLException {
        if (maintenance) {
            Number n = (Number) DBConnection.scalar("SELECT COUNT(*) FROM gate_allocations WHERE gate_id=?", gateId);
            if (n.longValue() > 0) throw new SQLException("Release this gate's allocations before maintenance");
        }
        DBConnection.update("UPDATE gates SET status=? WHERE gate_id=?", maintenance ? "MAINTENANCE" : "FREE", gateId);
        sync();
    }

    /** Recomputes FREE/OCCUPIED for every gate that is not under maintenance. */
    public static void sync() throws SQLException {
        try (Connection c = DBConnection.get()) { sync(c); }
    }

    static void sync(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("UPDATE gates g SET g.status = IF(EXISTS(SELECT 1 FROM gate_allocations a " +
                    "WHERE a.gate_id=g.gate_id),'OCCUPIED','FREE') WHERE g.status<>'MAINTENANCE'");
        }
    }
}
