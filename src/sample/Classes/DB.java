package sample.Classes;

import java.io.IOException;
import java.sql.*;

public class DB {
    private static Connection connection;
    private static Statement statement;

    public static void setConnection(String database) {
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"+database);
            createTables(connection);
        }catch (ClassNotFoundException | SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    private static void createTables(Connection con){
        try {
            DatabaseMetaData metadata = con.getMetaData();
            statement = con.createStatement();
            ResultSet resultSet = metadata.getTables(null, null, "subjects", null);
            if (!resultSet.next())
                statement.executeUpdate("CREATE TABLE subjects (idSub INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nameSub TEXT)");
            resultSet = metadata.getTables(null,null,"topics", null);
            if (!resultSet.next())
                statement.executeUpdate("CREATE TABLE topics " +
                        "(idTopic INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idSub INTEGER NOT NULL, " +
                        "nameTopic TEXT(50), " +
                        "FOREIGN KEY (idSub) REFERENCES subjects (idSub))");
            resultSet = metadata.getTables(null,null,"questions",null);
            if (!resultSet.next())
                statement.executeUpdate("CREATE TABLE questions " +
                        "(idQuestion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idTopic INTEGER NOT NULL," +
                        "nameQuestion TEXT, correctAnswer TEXT, " +
                        "FOREIGN KEY (idTopic) REFERENCES topics(idTopic))");
            resultSet = metadata.getTables(null,null,"answers",null);
            if (!resultSet.next())
                statement.executeUpdate("CREATE TABLE answers " +
                        "(idAnswer INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idQuestion INTEGER NOT NULL, " +
                        "nameAnswer TEXT, " +
                        "isCorrect INTEGER, " +
                        "FOREIGN KEY (idQuestion) REFERENCES questions (idQuestion))");
            resultSet = metadata.getTables(null, null, "tasks", null);
            if (!resultSet.next())
                statement.executeUpdate("CREATE TABLE tasks " +
                        "(idTask INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "idTopic INTEGER NOT NULL, "+
                        "nameTask TEXT, "+
                        "answer TEXT, " +
                        "FOREIGN KEY (idTopic) REFERENCES topics (idTopic))");
        } catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    public static ResultSet Select(String tableName, String condition){
        String query = "SELECT * FROM ";
        try {
            ResultSet rst;
            if (condition !=null) {
                query += tableName+" WHERE "+condition;
            } else
                query += tableName;
            return statement.executeQuery(query);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
            return null;
        }
    }
    public static void Update(String tableName, String updates, String condition){
        try {
            String query = "UPDATE "+tableName+" SET "+updates+" WHERE "+condition;
            statement.executeUpdate(query);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }
    public static void Insert(String tableName, String fieldName, String values){
        try{
            String query = "INSERT INTO "+tableName+" ("+fieldName+") VALUES (\""+values+"\")";
            statement.executeUpdate(query);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }
    public static void Delete (String table, String condition){
        String query = "DELETE FROM "+table+" WHERE "+condition;
        try{
            statement.executeUpdate(query);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }
    public static ResultSet SelectMax (String field, String table){
        try{
            return statement.executeQuery("SELECT MAX ("+field+") FROM "+table);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
            return null;
        }
    }
    public static void Query(String sql){
        try{
            statement.execute(sql);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }
}
