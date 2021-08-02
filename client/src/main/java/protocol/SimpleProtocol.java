package protocol;

import java.io.Serializable;

public class SimpleProtocol implements Serializable {
    public Operation type;
    public String fileName;
    public int size;

    public SimpleProtocol(Operation type, String fileName, int size) {
        this.type = type;
        this.fileName = fileName;
        this.size = size;
    }
}

