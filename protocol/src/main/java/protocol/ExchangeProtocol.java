package protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class ExchangeProtocol implements Serializable {

    public Operation type;
    public String login;
    public String password;
    public String fileName;
    public String subDir;
    public ArrayList<String> List;
    public long size;
    public int bufferSize;
    public int sizeOfPart;
    public byte[] partFile;

    public ExchangeProtocol() {
        //по-умолчению
        this.type = Operation.DEFAULT;
        this.bufferSize = 1024;
    }


    public ArrayList<String> getList() {
        return List;
    }

    public void setList(ArrayList<String> list) {
        List = list;
    }

    @Override
    public String toString() {
        return "ExchangeProtocol{" +
                "type=" + type +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", fileName='" + fileName + '\'' +
                ", subDir='" + subDir + '\'' +
                ", List=" + List +
                ", size=" + size +
                ", bufferSize=" + bufferSize +
                ", sizeOfPart=" + sizeOfPart +
                ", partFile=" + Arrays.toString(partFile) +
                '}';
    }

    public Operation getType() {
        return type;
    }

    public void setType(Operation type) {
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getSizeOfPart() {
        return sizeOfPart;
    }

    public void setSizeOfPart(int sizeOfPart) {
        this.sizeOfPart = sizeOfPart;
    }

    public byte[] getPartFile() {
        return partFile;
    }

    public void setPartFile(byte[] partFile) {
        this.partFile = partFile;
    }
}

