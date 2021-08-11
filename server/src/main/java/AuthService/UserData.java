package AuthService;

public class UserData {
    private long userId;
    private String login;
    private String password;
    private String defaultDir = null;
    private boolean read;
    private boolean write;
    private boolean management;

    public UserData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public UserData(long userId, String login, String password, String defaultDir, boolean read, boolean write, boolean management) {
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.defaultDir = defaultDir;
        this.read = read;
        this.write = write;
        this.management = management;
    }

    public long getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getDefaultDir() {
        return defaultDir;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isWrite() {
        return write;
    }

    public boolean isManagement() {
        return management;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "userId=" + userId +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", defaultDir='" + defaultDir + '\'' +
                ", read=" + read +
                ", write=" + write +
                ", management=" + management +
                '}';
    }
}