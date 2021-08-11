package protocol;

public enum Operation {
    DEFAULT,// по-умолчанию для пустого сообщения
    AUTH,// запрос на авторизацию
    AUTH_OK, // ответ на авторизация, что все в порядке
    AUTH_ERR, // ответ на авторизацию, что такого пользователя нет
    UPLOAD, //загрузка файла на сервер
    UPLOAD_CONFIRM, //готов принять файл
    UPLOAD_PART, //в сообщении часть файла
    UPLOAD_DONE, //загрузка завершена
    DOWNLOAD,
    LIST,
    DELETE
}
