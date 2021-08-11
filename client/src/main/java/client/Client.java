package client;


import protocol.*;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
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
                System.out.print("Логин: \n> ");
                String login = console.nextLine();
                if(login.equals("")) {
                    System.out.println("Логин не может быть пустым, попробуйте еще раз!");
                    continue;
                }
                System.out.print("Пароль: \n> ");
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
                        //загрузка файла на сервер
                        //Загрузить файл на сервер UPLOAD source_path
                        String[] parts = command.split("\\s");
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);
                        //смотри есть ли такой файл
                        File file = new File(parts[1]);
                        long length = file.length();
                        if(length == 0) {
                            System.out.println("Файл не найден!");
                            break;
                        }
                        //формируем объекта для запроса на сервер
                        ExchangeProtocol uploadRequest = new ExchangeProtocol();
                        uploadRequest.setType(Operation.UPLOAD);
                        //избавляемся от пути
                        String [] fileParts = parts[1].split("\\/");
                        System.out.println(fileParts[fileParts.length - 1]);
                        uploadRequest.setFileName(fileParts[fileParts.length - 1]);
                        uploadRequest.setSize(length);
                        //upload C:/Users/obelix/test31.txt
                        //upload C:/Users/obelix/test1.txt
                        //upload C:/Users/obelix/02.avi
                        if (writeMessage(outObj, uploadRequest)) {
                            System.out.println("Ожидание ответа от сервера...");
                            ExchangeProtocol incoming = readMessage(inObj);
                            //если все норм и сервер готов принять файл
                            if (incoming.type == Operation.UPLOAD_CONFIRM) {
                                //открываем файл и кусками отправляем на сервер
                                FileInputStream fin = new FileInputStream(parts[1]);
                                OutputStream out = socket.getOutputStream();
                                int count = 0;
                                //отправляем на сервер
                                System.out.println("Отправляем на север");
                                while (true) {
                                    ExchangeProtocol partFile = new ExchangeProtocol();
                                    partFile.setType(Operation.UPLOAD_PART);
                                    partFile.partFile = new byte[incoming.bufferSize];
                                    count = fin.read(partFile.partFile);
                                    if (count <= 0) break;
                                    partFile.sizeOfPart = count;
                                    outObj.writeObject(partFile);
                                    //сбрасывает буффер, меньше жрет память, но замедляет процесс
                                    outObj.reset();
                                    System.out.print(".");
                                }
                                System.out.println(" готово!");
                                fin.close();
                            }
                        } else {
                            System.out.println("Ошибка отправки запрос на сервер! Попробуйте снова.");
                        }

                    } else if (command.startsWith("download")) {
                        //загрузка файла на сервер
                        //DOWNLOAD server_path
                        String[] parts = command.split("\\s");
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);
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

