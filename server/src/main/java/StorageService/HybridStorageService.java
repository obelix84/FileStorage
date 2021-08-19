package StorageService;

import AuthService.UserData;
import DatabaseHelper.SQLHelper;
import server.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> getList(UserData user) {
        ArrayList<String> listFiles = (ArrayList<String>) this.DB.getFilesList(user.getUserId());
        return listFiles;
    }

    public boolean isFileExist(UserData user, FileInfo file) {
        //проверяем наличие файла в БД
        FileInfo fileInDB = this.DB.getFileInfo(file.getFileName(), user.getUserId(), file.getSubDir());
        if (fileInDB != null) return true;
        return false;
    }

    @Override
    public boolean deleteFile(UserData user, FileInfo file) {
        FileInfo fileToDelete = this.DB.getFileInfo(file.getFileName(), user.getUserId(),file.getSubDir());
        if (fileToDelete != null) {
            //удаляем файл в файловой системе
            File fileFS = new File(this.configuration.getProperty("server.storageDirectory") + "/"
                    + (user.getDefaultDir() == null? user.getLogin():"") + "/" + fileToDelete.getId());
            if(fileFS.delete()){
                //удаляем запись из базы
                this.DB.deleteFile(fileToDelete.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean initService(UserData user, FileInfo file, StorageOperation type) {
        this.currentType = type;
        //проверяем наличие файла в БД
        FileInfo fileInDB = this.DB.getFileInfo(file.getFileName(), user.getUserId(), file.getSubDir());
        //если такой файл есть, то просто обновляем размер файла
        if (fileInDB != null) {
            this.DB.updateFileInfo(file.getFileName(), user.getUserId(), file.getSize());
        } else {
            //если файла нет, то вставляем новый и получаем идентификатор нового файла
            this.DB.insertFileInfo(file.getFileName(), file.getSubDir(), file.getSize(), user);
            fileInDB = this.DB.getFileInfo(file.getFileName(), user.getUserId(), file.getSubDir());
        }
        //формируем путь к папке пользователя
        String path;
        if (user.getDefaultDir() == null) path = user.getLogin();
        else path = user.getDefaultDir();
        path = this.configuration.getProperty("server.storageDirectory") + "/" + path + "/" + fileInDB.getId();
        System.out.println(path);
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
        //просто перезаписывает файл
        if (fileOut != null) {
            try {
                fileOut.write(data, 0, count);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean readPartOfFileFromStorage(byte[] data, int count) {
        return false;
    }
}
