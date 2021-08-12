package DatabaseHelper;

import AuthService.UserData;
import StorageService.FileInfo;

import java.sql.*;
import java.util.Properties;

public class SQLHelper {
    private Connection connection;
    private PreparedStatement psGetUserData;
    private PreparedStatement psGetFileInfo;
    private PreparedStatement psAddFileInfo;
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
            psAddFileInfo = conn.prepareStatement("INSERT INTO main.files (filename, sub_dir, size, owner_id) " +
                    "VALUES (?, ?, ?, ?)");
            psGetFileInfo = conn.prepareStatement("SELECT id, filename, sub_dir, size, owner_id " +
                    "FROM main.files WHERE filename = ? AND owner_id = ?");
        } catch (SQLException th) {
            th.printStackTrace();
        }
    }

    //добавляет файл в базу возвращает true в случае успеха и false в противном случае
    public boolean insertFileInfo(String fileName, String subDir, long size, UserData UD) {
        try {
            psAddFileInfo.setString(1, fileName);
            psAddFileInfo.setString(2, subDir);
            psAddFileInfo.setLong(3, size);
            psAddFileInfo.setLong(4, UD.getUserId());
            if (psAddFileInfo.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Возвращает информацию о файле или Null, если файла такого нет
    public FileInfo getFileInfo(String fileName, long ownerId) {
        FileInfo file = null;
        try {
            psGetFileInfo.setString(1, fileName);
            psGetFileInfo.setLong(2, ownerId);
            ResultSet rs = psGetFileInfo.executeQuery();
            if (rs.next()) {
                file = new FileInfo(rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getLong(4),
                            rs.getLong(5));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return file;
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
