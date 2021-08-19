package StorageService;

import AuthService.UserData;

import java.util.List;

//интерфейс для получения файла из хранилища для КОНКРЕТНОГО ПОЛЬЗОВАТЕЛЯ
public interface StorageService {
    //true, если файл есть в хранилище и false в противном случае
    public boolean isFileExist(UserData user, FileInfo file);
    //true если файл есть и удален и false в противном случае
    public boolean deleteFile(UserData user, FileInfo file);
    //возвращает список файлов пользователя
    public List<String> getList(UserData user);
    //метод необходим для создания дескрипторов, чтоб не открывать и закрывать лишний раз
    //на вход подаем имя пользователя и имя файла которое надо достать из хранилища
    public boolean initService(UserData user, FileInfo file, StorageOperation type);
    //очищает ресурсы, закрывает соединение и т.п.
    public boolean stop();
    //частичная запись в хранилище и частичное чтение
    public boolean writePartOfFileToStorage(byte[] data, int count);
    public boolean readPartOfFileFromStorage(byte[] data, int count);

}
