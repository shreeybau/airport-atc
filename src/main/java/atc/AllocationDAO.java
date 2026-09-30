package atc;

import java.sql.*;
import javax.swing.table.DefaultTableModel;

/** Gate allocation with a real JDBC transaction (commit / rollback). */
public class AllocationDAO {
    public static DefaultTableModel list() throws SQLException {
        return DBConnection.query(
            "SELECT a.alloc_id, f.flight_no, CONCAT(g.terminal,'-',g.gate_no) AS gate, a.start_time, a.end_time " +
            "FROM gate_allocations a JOIN flights f ON f.flight_id=a.flight_id " +
            "JOIN gates g ON g.gate_id=a.gate_id ORDER BY a.start_time");
    }

    public static void allocate(int flightId, int gateId) throws SQLException {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                String gateStatus, capacity, size;
                Timestamp arr, dep;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status, size_capacity FROM gates WHERE gate_id=? FOR UPDATE")) { // lock row
                    ps.setInt(1, gateId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Gate not found");
                        gateStatus = rs.getString(1);
                        capacity = rs.getString(2);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT f.sched_arrival, f.sched_departure, a.size_category FROM flights f " +
                        "JOIN aircraft a ON a.aircraft_id=f.aircraft_id WHERE f.flight_id=?")) {
                    ps.setInt(1, flightId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Flight not found");
                        arr = rs.getTimestamp(1);
                        dep = rs.getTimestamp(2);
                        size = rs.getString(3);
                    }
                }
                if ("MAINTENANCE".equals(gateStatus)) throw new SQLException("Gate is under maintenance");
                if ("SML".indexOf(capacity) < "SML".indexOf(size))
                    throw new SQLException("Gate is too small for this aircraft (" + size + " > " + capacity + ")");
                if (count(c, "SELECT COUNT(*) FROM gate_allocations WHERE flight_id=?", flightId) > 0)
                    throw new SQLException("This flight already has a gate. Release it first.");
                if (count(c, "SELECT COUNT(*) FROM gate_allocations WHERE gate_id=? AND start_time<? AND end_time>?",
                        gateId, dep, arr) > 0)
                    throw new SQLException("Gate is already booked during this flight's time window");

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO gate_allocations(flight_id,gate_id,start_time,end_time) VALUES(?,?,?,?)")) {
                    DBConnection.bind(ps, flightId, gateId, arr, dep);
                    ps.executeUpdate();
                }
                GateDAO.sync(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static void release(int allocId) throws SQLException {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM gate_allocations WHERE alloc_id=?")) {
                ps.setInt(1, allocId);
                ps.executeUpdate();
                GateDAO.sync(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private static long count(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            DBConnection.bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
