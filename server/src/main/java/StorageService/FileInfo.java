package StorageService;

public class FileInfo {
    private long id;
    private String fileName;
    private String subDir;
    private long size;
    private long userId;

    public FileInfo(long id, String fileName, String subDir, long size, long userId) {
        this.id = id;
        this.fileName = fileName;
        this.subDir = subDir;
        this.size = size;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", subDir='" + subDir + '\'' +
                ", size=" + size +
                ", userId=" + userId +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
