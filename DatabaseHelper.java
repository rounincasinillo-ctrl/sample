import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;


public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/sample_db";
    private static final String USER = "root";
    private static final String PASS = "09810288525Rounin";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    // "C-0005" or "PF-0003" -> 5 or 3
    private static int numberOf(String id) {
        return Integer.parseInt(id.substring(id.indexOf('-') + 1));
    }

    public static void save(Billable b) throws SQLException {
        if (b instanceof Consumer) {
            Consumer c = (Consumer) b;
            String type = c instanceof Residential ? "Residential" : "Commercial";
            String sql = "INSERT INTO consumers (consumer_name, consumer_address, type) VALUES (?, ?, ?)";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getConsumerName());
                ps.setString(2, c.getConsumerAddress());
                ps.setString(3, type);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) c.setConsumerId(String.format("C-%04d", keys.getInt(1)));
                }
            }
        } else {
            PublicFacility f = (PublicFacility) b;
            String sql = "INSERT INTO public_facilities (facility_name, agency) VALUES (?, ?)";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, f.getFacilityName());
                ps.setString(2, f.getAgency());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) f.setFacilityId(String.format("PF-%04d", keys.getInt(1)));
                }
            }
        }
    }
    public static void updateDetail(Billable b) throws SQLException {
        if (b instanceof Consumer) {
            Consumer c = (Consumer) b;
            String type = c instanceof Residential ? "Residential" : "Commercial";
            String sql = "UPDATE consumers SET consumer_name = ?, consumer_address = ?, type = ? WHERE consumer_id = ?";
            try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getConsumerName());
                ps.setString(2, c.getConsumerAddress());
                ps.setString(3, type);
                ps.setInt(4, numberOf(c.getConsumerId()));
                ps.executeUpdate();
            }
        } else {
            PublicFacility f = (PublicFacility) b;
            String sql = "UPDATE public_facilities SET facility_name = ?, agency = ? WHERE facility_id = ?";
            try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, f.getFacilityName());
                ps.setString(2, f.getAgency());
                ps.setInt(3, numberOf(f.getFacilityId()));
                ps.executeUpdate();
            }
        }
    }

    public static void delete(Billable b) throws SQLException {
        String sql;
        int id;
        if (b instanceof Consumer) {
            sql = "DELETE FROM consumers WHERE consumer_id = ?";
            id = numberOf(((Consumer) b).getConsumerId());
        } else {
            sql = "DELETE FROM public_facilities WHERE facility_id = ?";
            id = numberOf(((PublicFacility) b).getFacilityId());
        }
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }


    // Save a computed bill (one row per reading, history is kept)
    public static void saveReading(Billable b, MeterReading r, double totalBill) throws SQLException {
        String sql;
        int id;
        if (b instanceof Consumer) {
            sql = "INSERT INTO meter_readings (consumer_id, previous_reading, current_reading, reading_date, total_bill) VALUES (?, ?, ?, ?, ?)";
            id = numberOf(((Consumer) b).getConsumerId());
        } else {
            sql = "INSERT INTO meter_readings (facility_id, previous_reading, current_reading, reading_date, total_bill) VALUES (?, ?, ?, ?, ?)";
            id = numberOf(((PublicFacility) b).getFacilityId());
        }
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setDouble(2, r.getPreviousReading());
            ps.setDouble(3, r.getCurrentReading());
            ps.setDate(4, java.sql.Date.valueOf(LocalDate.parse(r.getReadingDate().strip())));
            ps.setDouble(5, totalBill);
            ps.executeUpdate();
        }
    }


    // Latest reading of every account, keyed by account ID ("C-0001" / "PF-0001")
    public static HashMap<String, MeterReading> loadLatestReadings() throws SQLException {
        HashMap<String, MeterReading> map = new HashMap<>();
        String sql = "SELECT consumer_id, facility_id, previous_reading, current_reading, reading_date "
                + "FROM meter_readings ORDER BY reading_id";   // later rows replace earlier ones
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int consumerId = rs.getInt("consumer_id");
                String key = rs.wasNull()
                        ? String.format("PF-%04d", rs.getInt("facility_id"))
                        : String.format("C-%04d", consumerId);
                map.put(key, new MeterReading(
                        rs.getDouble("previous_reading"),
                        rs.getDouble("current_reading"),
                        rs.getString("reading_date")));
            }
        }
        return map;
    }


    public static ArrayList<Billable> loadAll() throws SQLException {
        ArrayList<Billable> list = new ArrayList<>();
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                    "SELECT consumer_id, consumer_name, consumer_address, type FROM consumers ORDER BY consumer_id")) {
                while (rs.next()) {
                    String id = String.format("C-%04d", rs.getInt("consumer_id"));
                    String name = rs.getString("consumer_name");
                    String address = rs.getString("consumer_address");
                    if (rs.getString("type").equals("Residential")) {
                        list.add(new Residential(id, name, address));
                    } else {
                        list.add(new Commercial(id, name, address));
                    }
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT facility_id, facility_name, agency FROM public_facilities ORDER BY facility_id")) {
                while (rs.next()) {
                    PublicFacility f = new PublicFacility(rs.getString("facility_name"), rs.getString("agency"));
                    f.setFacilityId(String.format("PF-%04d", rs.getInt("facility_id")));
                    list.add(f);
                }
            }
        }
        return list;
    }
}

