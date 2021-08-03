package AuthService;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements IAuthService{

    private class UserData {
        String login;
        String password;

        public UserData(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("login1", "12345"));
        users.add(new UserData("login2", "qwerty"));
    }

    @Override
    public boolean isValidUser(String login, String password) {
        for (UserData user : users) {
            if(user.login.equals(login) && user.password.equals(password)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean registration(String login, String password) {
        //заглушка, пока не нужен
        return false;
    }
}
