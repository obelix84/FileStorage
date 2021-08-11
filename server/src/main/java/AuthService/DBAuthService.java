package AuthService;

import DatabaseHelper.SQLHelper;

public class DBAuthService implements AuthService{
    private SQLHelper DB;

    public DBAuthService(SQLHelper DB) {
        this.DB = DB;
    }

    @Override
    public UserData getUserData(String login, String password) {
        UserData UD = this.DB.getUserDataByLogin(login);
        //проверяем логин и пароль
        if (UD.getPassword().equals(password)) {
            return UD;
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password) {
        //пока заглушка
        return false;
    }
}
