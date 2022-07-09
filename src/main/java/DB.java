import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/words";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void main(String[] args) {

    }

    public static List<String > executeQuery(String query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
        ) {
            List<String> list = new ArrayList<>();

            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next())
            {
                list.add(resultSet.getString(1));
            }
            return list;
        }
    }

    public static int executeUpdate(String query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
        ) {
            return stmt.executeUpdate(query);
        }
    }
}
