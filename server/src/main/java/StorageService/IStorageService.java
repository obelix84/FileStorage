package StorageService;

//интерфейс для получения файла из хранилища для КОНКРЕТНОГО ПОЛЬЗОВАТЕЛЯ
public interface IStorageService {
    //метод необходим для создания дескрипторов, чтоб не открывать и закрывать лишний раз
    //на вход подаем имя пользователя и имя файла которое надо достать из хранилища
    public boolean initService(String user, String fileName, StorageOperation type);
    //очищает ресурсы, закрывает соединение и т.п.
    public boolean stop();
    //методы для записи и чтения файла целиком
    public boolean writeFileToStorage(byte[] data);
    public boolean readFileFromStorage(byte[] data);
    //частичная запись в хранилище и частичное чтение
    public boolean writePartOfFileToStorage(byte[] data, int count);
    public boolean readPartOfFileFromStorage(byte[] data, int count);
}
