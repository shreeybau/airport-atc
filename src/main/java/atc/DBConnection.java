package atc;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/** Central JDBC helper: connection + PreparedStatement-based query/update utilities. */
public class DBConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver missing", e);
        }
    }

    // Settings can be overridden with environment variables (ATC_DB_URL, ATC_DB_USER, ATC_DB_PASS)
    private static final String URL = System.getenv().getOrDefault("ATC_DB_URL",
            "jdbc:mysql://localhost:3306/atc_db?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false");
    private static final String USER = System.getenv().getOrDefault("ATC_DB_USER", "root");
    private static final String PASS = System.getenv().getOrDefault("ATC_DB_PASS", "root"); // placeholder

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    static void bind(PreparedStatement ps, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }

    /** Converts date-time values to "yyyy-MM-dd HH:mm" strings for display. */
    private static Object norm(Object v) {
        if (v instanceof LocalDateTime) return ((LocalDateTime) v).format(FMT);
        if (v instanceof Timestamp) return ((Timestamp) v).toLocalDateTime().format(FMT);
        return v;
    }

    public static DefaultTableModel query(String sql, Object... p) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData m = rs.getMetaData();
                int n = m.getColumnCount();
                Vector<String> cols = new Vector<>();
                for (int i = 1; i <= n; i++) cols.add(m.getColumnLabel(i));
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= n; i++) row.add(norm(rs.getObject(i)));
                    model.addRow(row);
                }
                return model;
            }
        }
    }

    public static int update(String sql, Object... p) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        }
    }

    /** Returns the first column of the first row, or null. */
    public static Object scalar(String sql, Object... p) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? norm(rs.getObject(1)) : null;
            }
        }
    }
}