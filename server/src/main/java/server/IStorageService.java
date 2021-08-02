package server;
//интерфейс для получения файла из хранилища
public interface IStorageService {
    //метод необходит для создания дескрипторов, чтоб не открывать и закрывать лдишний раз
    public boolean initService(String path, StorageOperation type);
    //очищает ресурсы, закрывает соединение и т.п.
    public boolean stop();
    //методы для записи и чтения файла целиком
    public boolean writeFileToStorage(byte[] data);
    public boolean readFileFromStorage(byte[] data);
    //частичная запись в хранилище и частичное чтение
    public boolean writePartOfFileToStorage(byte[] data, int count);
    public boolean readPartOfFileFromStorage(byte[] data, int count);
}
