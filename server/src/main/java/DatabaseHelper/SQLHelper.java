package DatabaseHelper;

import AuthService.UserData;
import StorageService.FileInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLHelper {
    private Connection connection;
    private PreparedStatement psGetUserData;
    private PreparedStatement psGetFileInfo;
    private PreparedStatement psGetFileInfoIsNull;
    private PreparedStatement psAddFileInfo;
    private PreparedStatement psUpdateFileInfo;
    private PreparedStatement psGetFilesList;
    private PreparedStatement psDeleteFile;

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
                    "FROM main.files WHERE filename = ? AND owner_id = ? AND sub_dir = ?");
            psGetFileInfoIsNull = conn.prepareStatement("SELECT id, filename, sub_dir, size, owner_id " +
                    "FROM main.files WHERE filename = ? AND owner_id = ? AND sub_dir is null");
            psUpdateFileInfo = conn.prepareStatement("UPDATE main.files SET size = ? WHERE filename = ? " +
                    "AND owner_id = ?");
            psGetFilesList = conn.prepareStatement("SELECT filename, sub_dir FROM main.files WHERE owner_id = ?");
            psDeleteFile = conn.prepareStatement("DELETE FROM main.files WHERE id = ?");

        } catch (SQLException th) {
            th.printStackTrace();
        }
    }

    public boolean deleteFile(long fileId) {
        try {
            psDeleteFile.setLong(1, fileId);
            if (psDeleteFile.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //выборка списка всех файлов и пустой список, если файлов нет
    public List<String> getFilesList(long userId) {
        ArrayList<String> listFiles = new ArrayList<>();
        try {
            psGetFilesList.setLong(1, userId);
            ResultSet rs = psGetFilesList.executeQuery();
            while (rs.next()) {
                String fileName = rs.getString(1);
                String subDir = rs.getString(2);
                if (subDir == null) subDir = "";
                listFiles.add(subDir + fileName);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listFiles;
    }

    //изменяет информацию о файле в БД возвращает true в случае успеха и false в противном случае
    public boolean updateFileInfo(String fileName, long userId, long size) {
        try {
            psUpdateFileInfo.setLong(1, size);
            psUpdateFileInfo.setString(2, fileName);
            psUpdateFileInfo.setLong(3, userId);
            if (psUpdateFileInfo.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
    public FileInfo getFileInfo(String fileName, long ownerId, String subDir) {
        FileInfo file = null;
        if (subDir == null) {
            try {
                psGetFileInfoIsNull.setString(1, fileName);
                psGetFileInfoIsNull.setLong(2, ownerId);
                ResultSet rs = psGetFileInfoIsNull.executeQuery();
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
        } else {
            try {
                psGetFileInfo.setString(1, fileName);
                psGetFileInfo.setLong(2, ownerId);
                psGetFileInfo.setString(3, subDir);
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
            psGetUserData.close();
            psGetFileInfo.close();
            psGetFileInfoIsNull.close();
            psAddFileInfo.close();
            psUpdateFileInfo.close();
            psGetFilesList.close();
            psDeleteFile.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
