package atc;

import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.table.DefaultTableModel;

public class RunwayDAO {
    public static final int SEPARATION_MIN = 5;

    public static DefaultTableModel runways() throws SQLException {
        return DBConnection.query("SELECT runway_id, name, length_m, status FROM runways ORDER BY name");
    }

    public static DefaultTableModel assignments() throws SQLException {
        return DBConnection.query(
            "SELECT ra.assign_id, f.flight_no, r.name AS runway, ra.operation, ra.slot_time " +
            "FROM runway_assignments ra JOIN flights f ON f.flight_id=ra.flight_id " +
            "JOIN runways r ON r.runway_id=ra.runway_id ORDER BY ra.slot_time");
    }

    public static void assign(int flightId, int runwayId, String op, Timestamp slot) throws SQLException {
        Object st = DBConnection.scalar("SELECT status FROM runways WHERE runway_id=?", runwayId);
        if (st == null || !"ACTIVE".equals(st.toString())) throw new SQLException("Runway is not ACTIVE");
        Number clash = (Number) DBConnection.scalar(
            "SELECT COUNT(*) FROM runway_assignments WHERE runway_id=? AND ABS(TIMESTAMPDIFF(MINUTE, slot_time, ?)) < ?",
            runwayId, slot, SEPARATION_MIN);
        if (clash.longValue() > 0)
            throw new SQLException("Slot conflict: runway already used within " + SEPARATION_MIN + " minutes");
        DBConnection.update("INSERT INTO runway_assignments(flight_id,runway_id,operation,slot_time) VALUES(?,?,?,?)",
                flightId, runwayId, op, slot);
    }

    public static void delete(int assignId) throws SQLException {
        DBConnection.update("DELETE FROM runway_assignments WHERE assign_id=?", assignId);
    }
}
