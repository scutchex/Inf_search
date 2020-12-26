package dataset;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Accessor {
    private Connection connection;

    private static final Map<String, String> FIELDS = initFields();
    private static final Map<String, String> MEANINGFUL_FIELDS = meaningfulFields();

    public Accessor(String dbPath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public Set<Map<String, String>> selectAll() throws SQLException {
        String selectSql = "SELECT * FROM news";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSql);

        Set<Map<String, String>> dataset = new HashSet<>();
        while (resultSet.next()) {
            HashMap<String, String> article = new HashMap<>();
            article.put("id", Integer.toString(resultSet.getInt("id")));
            for (String field : FIELDS.keySet()) {
                article.put(field, resultSet.getString(FIELDS.get(field)));
            }
            dataset.add(article);
        }
        return dataset;
    }

    public static String[] getFields() {
        return FIELDS.keySet().toArray(new String[0]);
    }

    public static String[] getMeaningfulFields() {
        return MEANINGFUL_FIELDS.keySet().toArray(new String[0]);
    }

    public static String getDbField(String initField) {
        return FIELDS.get(initField);
    }

    private static Map<String, String> initFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("Ссылка", "url");
        fields.put("Заголовок", "title");
        fields.put("Категория", "category");
        fields.put("Время", "date");
        fields.put("Описание", "description");
        fields.put("Контент", "content");
        fields.put("Журналист", "journalist");
        fields.put("Теги", "tags");
        return fields;
    }

    private static  Map<String, String> meaningfulFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("Заголовок", "title");
        fields.put("Категория", "category");
        fields.put("Описание", "description");
        fields.put("Контент", "content");
        fields.put("Теги", "tags");
        return fields;
    }
}
