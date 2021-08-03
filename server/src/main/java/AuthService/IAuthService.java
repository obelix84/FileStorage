package AuthService;

//сервис авторизации
public interface IAuthService {
    //проверяет наличие пользователя, возвращает true, если такой есть
    boolean isValidUser(String login, String password);
    //добавляет нового пользователя, возвращает true, если удалось добавить пользователя
    boolean registration(String login, String password);
}
