package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStorageService implements IStorageService {
    private FileInputStream fileIn;
    private FileOutputStream fileOut;

    public FileStorageService() {

    }

    @Override
    public boolean initService(String path, StorageOperation type) {
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
        //пока не нужно
        return false;
    }

    @Override
    public boolean readFileFromStorage(byte[] data) {
        //пока не нужно
        return false;
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
