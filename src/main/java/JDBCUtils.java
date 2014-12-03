import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * Created by johnson on 12/3/14.
 */
public class JDBCUtils {
    static Logger logger = LogManager.getLogger();
    static String url = "jdbc:mysql://localhost:3306/STUDENTS";
    static String user = "johnson";
    static String password = "";
    static String table = "students";
    static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(url, user, password);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ResultSet executeQuery(String str) throws Exception{
        Statement statement = connection.createStatement();
        return statement.executeQuery(str);
    }

    private static void execute(String str) throws Exception {
        Statement statement = connection.createStatement();
        statement.execute(str);
    }

    public static String get(int id, Columns columns) throws Exception{
        switch (columns) {
            case name:
                return getName(id);
            case sex:
                return getSex(id);
            case picture:
                return getPicturePath(id);
            default:
                return "columns error";
        }
    }

    public static String getName(int id) throws Exception{
        ResultSet resultSet = executeQuery("select name from " + table + " where id = " + id);
        if (resultSet.next()) return resultSet.getString(1);
        else return "id not found";
    }

    public static String getSex(int id) throws Exception{
        ResultSet resultSet = executeQuery("select sex from " + table + " where id = " + id);
        if (resultSet.next()) return resultSet.getString(1);
        else return "id not found";
    }

    public static String getPicturePath(int id) throws Exception {
        ResultSet resultSet = executeQuery("select picture from " + table + " where id = " + id);
        if (resultSet.next()) return resultSet.getString(1);
        else return "id not found";
    }

    public static String getId(String name) throws Exception {
        ResultSet resultSet = executeQuery("select id from " + table + " where name = '" + name + "'");
        if (resultSet.next()) return resultSet.getString(1);
        else return "id not found";
    }

    public static void setSex(int id, SEX sex) throws Exception {
        execute("update " + table + " set sex = '" + sex + "'" + " where id = " + id);
    }

    public static void setPicture(int id, Path path) throws Exception {
        execute("update " + table + " set picture = '" + path.toAbsolutePath()+ "' where id = " + id);
    }

    public static void add(Map<Columns, String> map) throws Exception{
        String name = "null", sex = "null", path = "null", id = "0";
        if (map.containsKey(Columns.id)) id = map.get(Columns.id);
        if (map.containsKey(Columns.name)) name = "'" + map.get(Columns.name) + "'";
        if (map.containsKey(Columns.sex)) sex = "'" + map.get(Columns.sex) + "'";
        if (map.containsKey(Columns.picture)) path = "'" + map.get(Columns.picture) + "'";
        String exec = String.format("insert into %s values (%s, %s, %s, %s)", table, id, name, sex, path);
        execute(exec);
    }

    public static void delete(int id) throws Exception {
        execute("delete from " + table + " where id = " + id);
    }

    public static void edit(int id, Columns columns, String str) throws Exception {
        execute("update " + table + " set " + columns + " = '" + str + "' where id = " + id);
    }

    public static enum SEX {
        male, female
    }
}
