package AuthService;

public class DBAuthService implements AuthService{

    @Override
    public boolean isValidUser(String login, String password) {

        return false;
    }

    @Override
    public boolean registration(String login, String password) {
        //пока заглушка
        return false;
    }
}
