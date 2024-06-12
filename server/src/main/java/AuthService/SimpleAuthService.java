package AuthService;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{
    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("login1", "12345"));
        users.add(new UserData("login2", "qwerty"));
    }

    @Override
    public UserData getUserData(String login, String password) {
        for (UserData user : users) {
            if(user.getLogin().equals(login) && user.getPassword().equals(password)){
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password) {
        //заглушка, пока не нужен
        return false;
    }
}
