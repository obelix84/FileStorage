package protocol;

import java.io.Serializable;
import java.util.Arrays;

public class ExchangeProtocol implements Serializable {

    public Operation type;
    public String login;
    public String password;
    public String fileName;
    public long size;
    public int bufferSize;

    @Override
    public String toString() {
        return "ExchangeProtocol{" +
                "type=" + type +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", fileName='" + fileName + '\'' +
                ", size=" + size +
                ", bufferSize=" + bufferSize +
                ", sizeOfPart=" + sizeOfPart +
                ", partFile=" + Arrays.toString(partFile) +
                '}';
    }

    public int sizeOfPart;
    public byte[] partFile;

    public long getSize() {
        return size;
    }

    public ExchangeProtocol() {
        //по-умолчению
        this.type = Operation.DEFAULT;
        this.bufferSize = 1024;
    }

    public void setType(Operation type) {
        this.type = type;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSize(long size) {
        this.size = size;
    }


}

