package client;


import protocol.*;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    //private Socket socket;
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(5);
    private Scanner console = new Scanner(System.in);
    private Socket socket;
    ObjectOutputStream outObj;
    ObjectInputStream inObj;

    public Client() {
        this.start();

    }

    public void start() {
        System.out.println("Добро пожаловать!");
        System.out.println("Консольный клиент файл-серверного хранилища ver 0.001");
        System.out.print("Для соединения с сервером нажимте Enter..");
        String enter = console.nextLine();
        System.out.println("\n" + "");
        try {
            this.socket = new Socket("localhost", 10001);
            //открываем обертку для отправки объектов через сокет
            outObj = new ObjectOutputStream(socket.getOutputStream());
            //и для получения
            inObj = new ObjectInputStream(socket.getInputStream());
            //цикл авторизации
            while(true) {
                System.out.println("Соединение установлено");
                System.out.print("Логин: ");
                String login = console.nextLine();
                if(login.equals("")) {
                    System.out.println("Логин не может быть пустым, попробуйте еще раз!");
                    continue;
                }
                System.out.print("Пароль: ");
                String password = console.nextLine();
                if(password.equals("")) {
                    System.out.println("Пароль не может быть пустым, попробуйте еще раз!");
                    continue;
                }
                //отправляем пароль на сервер для проверки
                //формируем протокол взаимодействия
                ExchangeProtocol outMessage = new ExchangeProtocol();
                outMessage.setType(Operation.AUTH);
                outMessage.setLogin(login);
                outMessage.setPassword(password);
                //отправляем запрос
                outObj.writeObject(outMessage);
                ExchangeProtocol incMessage = null;
                try {
                    //получаем назад
                    incMessage = (ExchangeProtocol) inObj.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println(incMessage.type);
                if (incMessage.type == Operation.AUTH_OK) {
                    System.out.println("Авторизация успешна!");
                    break;
                } else if (incMessage.type == Operation.AUTH_ERR){
                    System.out.println("Такого пользователя не существует!");
                }

            }
            //основная работа
            while(true) {
                //обрабатыаем консоль
                System.out.print("Для продолжения работы введите команду \n");
                while(true) {
                    System.out.print(">");
                    String command = console.nextLine();
                    //выход из клиента EXIT
                    if(command.equals("exit")) {
                        break;
                    } else if (command.startsWith("upload")) {
                        //Загрузить файл на сервер UPLOAD source_path [destination_folder]
                        String[] parts = command.split("\\s");
                        //смотри есть ли такой файл
                        File uploadFile = new File(parts[1]);
                        System.out.println(parts[1]);
                        long length = uploadFile.length();
                        if(length == 0) {
                            System.out.println("Файл не найден!");
                            continue;
                        }

                        //формируем объекта для запроса на сервер
                        ExchangeProtocol uploadRequest = new ExchangeProtocol();
                        uploadRequest.setType(Operation.UPLOAD);
                        //избавляемся от пути
                        String[] fileParts = parts[1].split("\\/");
                        uploadRequest.setFileName(fileParts[fileParts.length - 1]);
                        uploadRequest.setSize(length);
                        if (parts.length > 2) {
                            if (parts[2].substring(parts[2].length() - 1) == "/")
                                uploadRequest.setSubDir(parts[2]);
                            else
                                uploadRequest.setSubDir(parts[2] + "/");
                        } else {
                            uploadRequest.setSubDir(null);
                        }
                        //отправляем сообщение на сервер
                        if (writeMessage(outObj, uploadRequest)) {
                            System.out.println("Ожидание ответа от сервера...");
                            //цикл обработки сообщений от сервера
                            while (true) {
                                ExchangeProtocol incoming = readMessage(inObj);
                                if (incoming.type == Operation.UPLOAD_CONFIRM) {
                                    //отправляем файл
                                    sendFile(outObj, parts[1], incoming.getBufferSize());
                                    break;
                                }
                                if (incoming.type == Operation.UPLOAD_EXIST) {
                                    //перезаписать файл или нет?
                                    System.out.println("Файл на сервере существует, перезаписать?");
                                    System.out.print("[да/нет] > ");
                                    command = console.nextLine();
                                    if (command.equals("нет")) {
                                        break;
                                    } else if (command.equals("да")) {
                                        //формируем объекта для запроса на сервер
                                        ExchangeProtocol overwriteRequest = new ExchangeProtocol();
                                        overwriteRequest.setType(Operation.UPLOAD_OVERWRITE);
                                        overwriteRequest.setFileName(fileParts[fileParts.length - 1]);
                                        overwriteRequest.setSize(length);
                                        if (parts.length > 2) {
                                            if (parts[2].substring(parts[2].length() - 1) == "/")
                                                overwriteRequest.setSubDir(parts[2]);
                                            else
                                                overwriteRequest.setSubDir(parts[2] + "/");
                                        } else {
                                            overwriteRequest.setSubDir(null);
                                        }
                                        //отправить на сервер
                                        if (writeMessage(outObj, overwriteRequest)) {
                                            continue;
                                        } else {
                                            System.out.println("Ошибка отправки на сервер сообщения!");
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            System.out.println("Ошибка отправки запроса на сервер! Попробуйте снова.");
                        }

                    } else if (command.startsWith("download")) {
                        //загрузка файла на сервер
                        //upload C:/Users/obelix/test31.txt lalala/bbb
                        //upload C:/Users/obelix/test1.txt
                        //upload C:/Users/obelix/02.avi
                        //DOWNLOAD server_path
                        String[] parts = command.split("\\s");
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);
                        //вторая часть состоит из пути и имени файла
                        String [] fileParts = parts[1].split("\\/");
                        String subDir = "";
                        if (fileParts.length > 1) {
                            for (int i = 0; i < fileParts.length - 1; i++) {
                                String filePart = fileParts[i];
                                subDir += filePart + "/";
                            }
                        }
                        System.out.println(subDir);

                        //формируем объект для запроса на сервер
                        ExchangeProtocol downRequest = new ExchangeProtocol();
                        downRequest.setType(Operation.DOWNLOAD);
                        downRequest.setSubDir(subDir);
                        downRequest.setFileName(fileParts[fileParts.length - 1]);

                    } else if (command.startsWith("list")) {
                        //получение списка файлов

                        ExchangeProtocol listRequest = new ExchangeProtocol();
                        listRequest.setType(Operation.LIST);
                        //отправляем сообщение на сервер
                        if (writeMessage(outObj, listRequest)) {
                            System.out.println("Ожидание ответа от сервера...");
                            ExchangeProtocol incomingList = readMessage(inObj);
                            //выводим список на экран
                            ArrayList<String> filesList = incomingList.getList();
                            for (String s : filesList) {
                                System.out.println(s);
                            }
                            //сбросим кэш
                            outObj.reset();
                        }

                    } else if (command.startsWith("delete")) {
                        //Удалить файл с сервера DELETE path
                        String[] parts = command.split("\\s");
                        //смотри есть ли такой файл
                        ExchangeProtocol exRequest = new ExchangeProtocol();
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);

                        exRequest.setType(Operation.EXIST);
                        String [] pathParts = parts[1].split("\\/");
                        exRequest.setFileName(pathParts[pathParts.length - 1]);
                        System.out.println(pathParts[pathParts.length - 1]);
                        String subDir = "";
                        for (int i = 0; i < pathParts.length - 1; i++) {
                            String pathPart = pathParts[i];
                            subDir += pathParts[i] + "/";
                        }
                        System.out.println("Sub Dir: " + subDir);
                        exRequest.setSubDir(subDir.equals("")? null: subDir);
                        //отправляем сообщение на сервер
                        if (writeMessage(outObj, exRequest)) {
                            System.out.println("Ожидание ответа от сервера...");
                            ExchangeProtocol incomingEx = readMessage(inObj);
                            if (incomingEx.getType() == Operation.EXIST_FALSE) {
                                System.out.println("Такого файла на сервере нет!");
                                continue;
                            } else if (incomingEx.getType() == Operation.EXIST_TRUE) {
                                System.out.println("Вы уверены, что хотите удалить файл?");
                                System.out.print("[да/нет] > ");
                                command = console.nextLine();
                                if(command.equals("да")) {
                                    ExchangeProtocol deleteFile = new ExchangeProtocol();
                                    deleteFile.setType(Operation.DELETE);
                                    deleteFile.setFileName(pathParts[pathParts.length - 1]);
                                    deleteFile.setSubDir(subDir.equals("")? null: subDir);
                                    if (writeMessage(outObj, deleteFile)) {
                                        System.out.println("Файл удален!");
                                        continue;
                                    }
                                } else {
                                    System.out.println("Операция прервана");
                                    continue;
                                }
                            }
                            //сбросим кэш
                            outObj.reset();
                        }

                    }
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            this.stop();
        }

    }

    private ExchangeProtocol readMessage(ObjectInputStream in){
        ExchangeProtocol incMessage = null;
        try {
            incMessage = (ExchangeProtocol) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return incMessage;
    }

    private boolean writeMessage(ObjectOutputStream out, ExchangeProtocol outMessage){
        try {
            out.writeObject(outMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    private void uploadFile () {
//        //загрузка файла на сервер
//        //Загрузить файл на сервер UPLOAD source_path [destination_folder]
//        String[] parts = command.split("\\s");
//        //смотри есть ли такой файл
//        File file = new File(parts[1]);
//        long length = file.length();
//        if(length == 0) {
//            System.out.println("Файл не найден!");
//            break;
//        }
//
//        //формируем объекта для запроса на сервер
//        ExchangeProtocol uploadRequest = new ExchangeProtocol();
//        uploadRequest.setType(Operation.UPLOAD);
//        //избавляемся от пути
//        String[] fileParts = parts[1].split("\\/");
//        System.out.println(fileParts[fileParts.length - 1]);
//        uploadRequest.setFileName(fileParts[fileParts.length - 1]);
//        uploadRequest.setSize(length);
//        if (parts.length > 2) {
//            if (parts[2].substring(parts[2].length() - 1) == "/")
//                uploadRequest.setSubDir(parts[2]);
//            else
//                uploadRequest.setSubDir(parts[2] + "/");
//        } else {
//            uploadRequest.setSubDir(null);
//        }
//        //upload C:/Users/obelix/test31.txt lalala/bbb
//        //upload C:/Users/obelix/test1.txt
//        //upload C:/Users/obelix/02.avi
//        if (writeMessage(outObj, uploadRequest)) {
//            System.out.println("Ожидание ответа от сервера...");
//            ExchangeProtocol incoming = readMessage(inObj);
//            //файл в хранилище существует, перезаписать?
//            if (incoming.type == Operation.UPLOAD_EXIST) {
//                System.out.println("Файл на сервере существует, перезаписать? да/нет");
//                System.out.print(">");
//                command = console.nextLine();
//                if (command.equals("нет")) {
//                    continue;
//                } else if (command.equals("да")) {
//                    //формируем объекта для запроса на сервер
//                    ExchangeProtocol overwriteRequest = new ExchangeProtocol();
//                    overwriteRequest.setType(Operation.UPLOAD_OVERWRITE);
//                    //отправить на сервер
//                    if (writeMessage(outObj, overwriteRequest)) {
//                        System.out.println("Ошибка отправки на сервер сообщения!");
//                        break;
//                    }
//                }
//
//            }
//            //если все норм и сервер готов принять файл
//            if (incoming.type == Operation.UPLOAD_CONFIRM) {
//                //открываем файл и кусками отправляем на сервер
//                FileInputStream fin = new FileInputStream(parts[1]);
//                OutputStream out = socket.getOutputStream();
//                int count = 0;
//                //отправляем на сервер
//                System.out.println("Отправляем на север");
//                while (true) {
//                    ExchangeProtocol partFile = new ExchangeProtocol();
//                    partFile.setType(Operation.UPLOAD_PART);
//                    partFile.partFile = new byte[incoming.bufferSize];
//                    count = fin.read(partFile.partFile);
//                    if (count <= 0) break;
//                    partFile.sizeOfPart = count;
//                    outObj.writeObject(partFile);
//                    //сбрасывает буффер, меньше жрет память, но замедляет процесс
//                    outObj.reset();
//                }
//                System.out.println(" готово!");
//                fin.close();
//
//            }
//
//
//        } else {
//            System.out.println("Ошибка отправки запрос на сервер! Попробуйте снова.");
//        }
//
//    }

    private void sendFile(ObjectOutputStream outObj, String path, int bufferSize) throws IOException {
        FileInputStream fin = new FileInputStream(path);
        int count = 0;
        //отправляем на сервер
        System.out.println("Отправляем на север");
        while (true) {
            ExchangeProtocol partFile = new ExchangeProtocol();
            partFile.setType(Operation.UPLOAD_PART);
            partFile.partFile = new byte[bufferSize];
            count = fin.read(partFile.partFile);
            if (count <= 0) break;
            partFile.sizeOfPart = count;
            outObj.writeObject(partFile);
            //сбрасывает буффер, меньше жрет память, но замедляет процесс
            outObj.reset();
        }
        System.out.println("Готово!");
        fin.close();
    }

    public void stop() {
        try {
           inObj.close();
           outObj.close();
           socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        console.close();
        System.out.println("disconnect");
        //THREAD_POOL.shutdown();
    }

}

//
//    public void connect() {
//        for (int i = 0; i < 1; i++) {
//            THREAD_POOL.execute(() -> {
//                try {
//                    Socket socket2 = new Socket("localhost", 10000);
//                    System.out.println("connected");
//                    //посылаем протокол
//                    ExchangeProtocol message2 = new ExchangeProtocol();
//                    //ExchangeProtocol message2 = new ExchangeProtocol(Operation.UPLOAD,
//                    //        "test1_serv.txt", 10_000_000);
//
//                    ObjectOutputStream outObj2 = new ObjectOutputStream(socket2.getOutputStream());
//
//                    System.out.println(message2.fileName);
//                    System.out.println(message2.size);
//                    System.out.println(message2.type);
//
//                    System.out.println("Пишем сообщение");
//                    outObj2.writeObject(message2);
//                    System.out.println("Кидаем файл");
//                    System.out.println("-");
//                    //outObj.close();
//                    System.out.println("--");
//                    OutputStream out2 = socket2.getOutputStream();
//                    System.out.println("---");
//                    System.out.println(System.getProperty("user.dir"));
//                    FileInputStream fin2 = new FileInputStream("client/src/main/resources/test1.txt");
//                    byte[] buffer = new byte[1024];
//                    int count = 0;
//                    while ((count = fin2.read(buffer)) > 0 ) {
//                        System.out.println("читаем 1 " + socket2.getRemoteSocketAddress());
//                        out2.write(buffer, 0, count);
//                    }
//                    socket2.close();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    this.stop();
//                }
//            });
//            THREAD_POOL.execute(() -> {
//                try {
//                    Socket socket1 = new Socket("localhost", 10000);
//                    System.out.println("connected");
//                    //посылаем протокол
//                    ExchangeProtocol message1 = new ExchangeProtocol();
//                    //ExchangeProtocol message1 = new ExchangeProtocol(Operation.UPLOAD,
//                    //        "test2_serv.txt", 6504);
//
//                    ObjectOutputStream outObj1 = new ObjectOutputStream(socket1.getOutputStream());
//
//                    System.out.println(message1.fileName);
//                    System.out.println(message1.size);
//                    System.out.println(message1.type);
//
//                    System.out.println("Пишем сообщение");
//                    outObj1.writeObject(message1);
//                    System.out.println("Кидаем файл");
//                    System.out.println("-");
//                    //outObj.close();
//                    System.out.println("--");
//                    OutputStream out1 = socket1.getOutputStream();
//                    System.out.println("---");
//                    System.out.println(System.getProperty("user.dir"));
//                    FileInputStream fin1 = new FileInputStream("client/src/main/resources/test2.txt");
//                    byte[] buffer = new byte[1024];
//                    int count = 0;
//                    while ((count = fin1.read(buffer)) > 0 ) {
//                        System.out.println("читаем 2" + socket1.getRemoteSocketAddress());
//                        out1.write(buffer, 0, count);
//                    }
//                    socket1.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//
//                    this.stop();
//                }
//            });
//
//            THREAD_POOL.execute(() -> {
//                try {
//                    Socket socket1 = new Socket("localhost", 10000);
//                    System.out.println("connected");
//                    //посылаем протокол
//                    ExchangeProtocol message1 = new ExchangeProtocol();
//                    //ExchangeProtocol message1 = new ExchangeProtocol(Operation.UPLOAD,
//                     //       "test3_serv.txt", 1533);
//
//                    ObjectOutputStream outObj1 = new ObjectOutputStream(socket1.getOutputStream());
//
//                    System.out.println(message1.fileName);
//                    System.out.println(message1.size);
//                    System.out.println(message1.type);
//
//                    System.out.println("Пишем сообщение");
//                    outObj1.writeObject(message1);
//                    System.out.println("Кидаем файл");
//                    System.out.println("-");
//                    //outObj.close();
//                    System.out.println("--");
//                    OutputStream out1 = socket1.getOutputStream();
//                    System.out.println("---");
//                    System.out.println(System.getProperty("user.dir"));
//                    FileInputStream fin1 = new FileInputStream("client/src/main/resources/test3.txt");
//                    byte[] buffer = new byte[1024];
//                    int count = 0;
//                    while ((count = fin1.read(buffer)) > 0 ) {
//                        System.out.println("читаем 2" + socket1.getRemoteSocketAddress());
//                        out1.write(buffer, 0, count);
//                    }
//                    socket1.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//
//                    this.stop();
//                }
//            });
//
////            THREAD_POOL.execute(() -> {
////                try {
////                    socket = new Socket("localhost", 10000);
////                    InputStream in = socket.getInputStream();
////                    System.out.println("connected");
////                    System.out.println(System.getProperty("user.dir"));
////                    FileOutputStream fout = new FileOutputStream("client/src/main/resources/test_client.txt");
////                    byte[] buffer = new byte[1024];
////                    int count = 0;
////                    while ((count = in.read(buffer)) > 0 ) {
////                        System.out.println("читаем ");
////                        fout.write(buffer, 0, count);
////                    }
////                } catch (IOException e) {
////                    e.printStackTrace();
////                } finally {
////                    this.disconnect();
////                }
////            });
//            THREAD_POOL.shutdown();
//        }
//
//    }

