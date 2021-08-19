package AuthService;

//сервис авторизации
public interface AuthService {
    //проверяет наличие пользователя, возвращает его данные, если такой есть, если его нет, то null
    UserData getUserData(String login, String password);
    //добавляет нового пользователя, возвращает true, если удалось добавить пользователя
    boolean registration(String login, String password);
}
