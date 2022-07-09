import java.sql.SQLException;
import java.util.*;

public class Wordle {

    private static final String TABLE_NAME = "NYT_Wordle";

    private static class Char {
        private static final Map<Character, Char> charMap = new HashMap<>();

        private final char c;
        private boolean isPresent = true;
        private Set<Integer> possiblePos = null;
        private Set<Integer> notPossiblePos = null;
        private Set<Integer> mustBePos = null;

        Char(char c) {
            this.c = c;
        }

        static Char get(char character) {
            return charMap.computeIfAbsent(character, Char::new);
        }

        void isAbsent() {
            if (mustBePos != null || notPossiblePos != null || possiblePos != null)
            {
                throw new RuntimeException("Can't mark absence after filling possible OR not-possible positions");
            }
            this.isPresent = false;
        }

        Char canBeAt(int... pos) {
            validate(true, pos);
            if (possiblePos == null)
            {
                possiblePos = new HashSet<>();
            }
            Arrays.stream(pos).forEach(p -> possiblePos.add(p));
            return this;
        }

        Char canNotBeAt(int... pos) {
            validate(false, pos);
            if (notPossiblePos == null)
            {
                notPossiblePos = new HashSet<>();
            }
            Arrays.stream(pos).forEach(p -> notPossiblePos.add(p));
            return this;
        }

        Char mustBeAt(int... pos) {
            validate(true, pos);
            if (mustBePos == null)
            {
                mustBePos = new HashSet<>();
            }
            Arrays.stream(pos).forEach(p -> mustBePos.add(p));
            return this;
        }

        private void validate(boolean possible, int... pos) {
            if (!isPresent)
            {
                throw new RuntimeException("Char has been declared as absent already. Can not declare positions!");
            }

            for (int p : pos)
            {
                if (p < 1 || p > 5)
                {
                    throw new RuntimeException("Position has to be between 1 and 5");
                }

                if (possible && notPossiblePos != null && notPossiblePos.contains(p))
                {
                    throw new RuntimeException("Position already described as not-possible");
                }
                else if (!possible && ((possiblePos != null && possiblePos.contains(p)) || (mustBePos != null && mustBePos.contains(p))))
                {
                    throw new RuntimeException("Position already described as possible");
                }
            }
        }

        String getCriteria() {
            if (!this.isPresent)
            {
                return c + " = 0";
            }
            else
            {
                String criteria = "(" + this.c + " | 0 <> 0)";
                if (mustBePos != null)
                {
                    int mustBeMask = mustBePos.stream().mapToInt(i -> (int) Math.pow(2, 5 - i)).sum();
                    criteria += " AND (" + this.c + " & " + mustBeMask + " = " + mustBeMask + ")";
                }

                if (possiblePos != null)
                {
                    int possibleMask = possiblePos.stream().mapToInt(i -> (int) Math.pow(2, 5 - i)).sum();
                    criteria += " AND (" + this.c + " & " + possibleMask + " <> 0)";
                }

                if (notPossiblePos != null)
                {
                    int notPossibleMask = notPossiblePos.stream().mapToInt(i -> (int) Math.pow(2, 5 - i)).sum();
                    criteria += " AND (" + this.c + " & " + notPossibleMask + " = 0)";
                }

                return criteria;
            }
        }

        static String getQuery(boolean onlyCount) {
            StringBuilder sqlBuilder = new StringBuilder();
            if (onlyCount)
            {
                sqlBuilder.append("SELECT COUNT(*) FROM ").append(TABLE_NAME);
            }
            else
            {
                sqlBuilder.append("SELECT * FROM ").append(TABLE_NAME);
            }

            if (charMap.isEmpty())
            {
                return sqlBuilder.toString();
            }

            sqlBuilder.append("\nWHERE ");
            charMap.values().forEach(ch -> sqlBuilder.append(ch.getCriteria()).append("\nAND "));
            sqlBuilder.setLength(sqlBuilder.length()-5);
            return sqlBuilder.toString();
        }

        static int getCount() {
            String query = getQuery(true);
            try {
                return Integer.parseInt(DB.executeQuery(query).get(0));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static List<String> getWords() {
            String query = getQuery(false);
            try {
                return DB.executeQuery(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Char.get('A').canNotBeAt(1,2,3);
        Char.get('D').canNotBeAt(1,2,3);
        Char.get('I').isAbsent();
        Char.get('E').canNotBeAt(4,5,2);
        Char.get('U').isAbsent();
        Char.get('B').isAbsent();
        Char.get('G').isAbsent();
        Char.get('L').isAbsent();
        Char.get('S').canNotBeAt(5);


        List<String> words = Char.getWords();
        System.out.println(words.size() + " possible matches");
        if (words.size() > 20)
        {
            System.out.println("Printing first 20 possibilities");
        }
        for (int i=0; i < words.size() && i < 20; i++)
        {
            System.out.println(words.get(i));
        }

    }
}
