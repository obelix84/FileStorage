package StorageService;

import AuthService.UserData;
import DatabaseHelper.SQLHelper;
import protocol.Operation;
import server.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//гибридное файловое хранилище, файлы хранит по папкам пользователей, а информацию о файле хранит в БД
public class HybridStorageService implements StorageService{
    private FileInputStream fileIn;
    private FileOutputStream fileOut;
    private Configuration configuration;
    private SQLHelper DB;
    private StorageOperation currentType;

    public HybridStorageService(Configuration configuration, SQLHelper DB) {
        this.configuration = configuration;
        this.DB = DB;
    }

    @Override
    public boolean initService(UserData user, String fileName, StorageOperation type) {
        this.currentType = type;
        //формируем путь к папке пользователя
        String path;
        if (user.getDefaultDir() == null) path = user.getLogin();
        else path = user.getDefaultDir();
        path = this.configuration.getProperty("server.storageDirectory") + "/" + path + "/";
        try {
            if (type == StorageOperation.UPLOAD) {
                fileOut = new FileOutputStream(path);
            } else if (type == StorageOperation.DOWNLOAD) {
                fileIn = new FileInputStream(path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (fileIn != null) {
            try {
                fileIn.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean writeFileToStorage(byte[] data) {
        return false;
    }

    @Override
    public boolean readFileFromStorage(byte[] data) {
        return false;
    }

    @Override
    public boolean writePartOfFileToStorage(byte[] data, int count) {
        return false;
    }

    @Override
    public boolean readPartOfFileFromStorage(byte[] data, int count) {
        return false;
    }
}
