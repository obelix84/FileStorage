package DatabaseHelper;

import AuthService.UserData;

import java.sql.*;
import java.util.Properties;

public class SQLHelper {
    private Connection connection;
    private PreparedStatement psGetUserData;
    private PreparedStatement psRegistration;

    public SQLHelper(String user, String password) {
        String url = "jdbc:postgresql://localhost/filestorage";
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("ssl","false");
        try {
            Connection conn = DriverManager.getConnection(url, props);
            psGetUserData = conn.prepareStatement("SELECT id, login, password," +
                    " default_dir, read, write, management FROM main.users WHERE login = ?;");
        } catch (SQLException th) {
            th.printStackTrace();
        }
    }

    public UserData getUserDataByLogin(String login) {
        UserData UD = null;
        try {
            psGetUserData.setString(1, login);
            ResultSet rs = psGetUserData.executeQuery();
            if (rs.next()) {
                UD = new UserData(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getBoolean(5),
                        rs.getBoolean(6),
                        rs.getBoolean(7));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return UD;
    }

    public void disconnect() {
        try {
            psRegistration.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
