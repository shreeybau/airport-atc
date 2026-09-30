package atc;

import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.table.DefaultTableModel;

public class FlightDAO {
    public static DefaultTableModel list() throws SQLException {
        return DBConnection.query(
            "SELECT f.flight_id, f.flight_no, a.registration_no AS aircraft, f.origin, f.destination, " +
            "f.sched_arrival, f.sched_departure, f.status FROM flights f " +
            "JOIN aircraft a ON a.aircraft_id=f.aircraft_id ORDER BY f.sched_arrival");
    }

    public static DefaultTableModel aircraft() throws SQLException {
        return DBConnection.query("SELECT aircraft_id, registration_no FROM aircraft ORDER BY registration_no");
    }

    public static void add(String no, int aircraftId, String origin, String dest,
                           Timestamp arr, Timestamp dep, String status) throws SQLException {
        DBConnection.update("INSERT INTO flights(flight_no,aircraft_id,origin,destination,sched_arrival," +
                "sched_departure,status) VALUES(?,?,?,?,?,?,?)", no, aircraftId, origin, dest, arr, dep, status);
    }

    public static void update(int id, String no, int aircraftId, String origin, String dest,
                              Timestamp arr, Timestamp dep, String status) throws SQLException {
        DBConnection.update("UPDATE flights SET flight_no=?, aircraft_id=?, origin=?, destination=?, " +
                "sched_arrival=?, sched_departure=?, status=? WHERE flight_id=?",
                no, aircraftId, origin, dest, arr, dep, status, id);
    }

    public static void delete(int id) throws SQLException {
        DBConnection.update("DELETE FROM flights WHERE flight_id=?", id); // allocations cascade
        GateDAO.sync();
    }
}
