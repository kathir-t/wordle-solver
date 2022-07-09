import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PopulateDB {

    public static void main(String[] args) {
        try
        {
            long startMillis = System.currentTimeMillis();
            System.out.println("Words population started");

            File file = new File("wordle-all-allowed-guesses.txt");

            createTable();

            populateTable(file);

            System.out.println("Completed in " + (System.currentTimeMillis() - startMillis) + " ms");
        }
        catch (SQLException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private static void createTable() throws SQLException {
        final String createQuery = "CREATE TABLE `NYT_Wordle` (\n" +
                "    `word` char(5) NOT NULL,\n" +
                "    A TINYINT DEFAULT 0,\n" +
                "    B TINYINT DEFAULT 0,\n" +
                "    C TINYINT DEFAULT 0,\n" +
                "    D TINYINT DEFAULT 0,\n" +
                "    E TINYINT DEFAULT 0,\n" +
                "    F TINYINT DEFAULT 0,\n" +
                "    G TINYINT DEFAULT 0,\n" +
                "    H TINYINT DEFAULT 0,\n" +
                "    I TINYINT DEFAULT 0,\n" +
                "    J TINYINT DEFAULT 0,\n" +
                "    K TINYINT DEFAULT 0,\n" +
                "    L TINYINT DEFAULT 0,\n" +
                "    M TINYINT DEFAULT 0,\n" +
                "    N TINYINT DEFAULT 0,\n" +
                "    O TINYINT DEFAULT 0,\n" +
                "    P TINYINT DEFAULT 0,\n" +
                "    Q TINYINT DEFAULT 0,\n" +
                "    R TINYINT DEFAULT 0,\n" +
                "    S TINYINT DEFAULT 0,\n" +
                "    T TINYINT DEFAULT 0,\n" +
                "    U TINYINT DEFAULT 0,\n" +
                "    V TINYINT DEFAULT 0,\n" +
                "    W TINYINT DEFAULT 0,\n" +
                "    X TINYINT DEFAULT 0,\n" +
                "    Y TINYINT DEFAULT 0,\n" +
                "    Z TINYINT DEFAULT 0\n" +
                ") ENGINE = InnoDB DEFAULT CHARSET = utf8;";
        DB.executeUpdate(createQuery);
    }

    private static void populateTable(File file) throws SQLException, FileNotFoundException {

        Scanner myReader = new Scanner(file);

        while (myReader.hasNextLine())
        {
            String word = myReader.nextLine().toUpperCase();

            if (word.length() == 5)
            {
                Map<Character, Integer> lettersPositionMap = getLettersPositionMap(word);

                StringBuilder sqlQueryBuilder = new StringBuilder();
                sqlQueryBuilder.append("INSERT INTO NYT_Wordle (word");
                lettersPositionMap.keySet().forEach(c -> sqlQueryBuilder.append(", ").append(c));
                sqlQueryBuilder.append(") VALUES ('");
                sqlQueryBuilder.append(word).append("'");
                lettersPositionMap.values().forEach(n -> sqlQueryBuilder.append(", ").append(n));
                sqlQueryBuilder.append(");");

                DB.executeUpdate(sqlQueryBuilder.toString());
            }
        }
        myReader.close();
    }

    private static Map<Character, Integer> getLettersPositionMap(String word) {
        Map<Character,Integer> lettersPosMap = new HashMap<>(6,1);
        for (int i=0; i<5; i++)
        {
            char c = word.charAt(i);
            lettersPosMap.put(c, (int) (lettersPosMap.getOrDefault(c, 0) + Math.pow(2,4-i)));
        }
        return lettersPosMap;
    }
}
