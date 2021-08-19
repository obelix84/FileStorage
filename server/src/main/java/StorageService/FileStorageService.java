package StorageService;

import AuthService.UserData;
import server.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

//сервис хранения файлов на сервере в файлах
public class FileStorageService implements StorageService {
    private FileInputStream fileIn;
    private FileOutputStream fileOut;
    private Configuration configuration;

    //DI
    public FileStorageService(Configuration conf) {
        this.configuration = conf;
    }

    @Override
    public boolean isFileExist(UserData user, FileInfo file) {
        //заглушка
        return false;
    }

    @Override
    public boolean deleteFile(UserData user, FileInfo file) {
        //заглушка
        return false;
    }

    @Override
    public List<String> getList(UserData user) {
        //заглушка
        return null;
    }

    @Override
    public boolean initService(UserData user, FileInfo file, StorageOperation type) {
        //формируем путь к фалу пользователя
        String path = this.configuration.getProperty("server.storageDirectory")+ "/"+ user.getLogin() + "/" + file.getFileName();
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
    public boolean writePartOfFileToStorage(byte[] data, int count) {
        if (fileOut != null) {
            try {
                fileOut.write(data, 0, count);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean readPartOfFileFromStorage(byte[] data, int count) {
        if (fileIn != null) {
            try {
                int c = fileIn.read(data, 0, count);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
