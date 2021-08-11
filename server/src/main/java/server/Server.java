package server;

import AuthService.AuthService;
import AuthService.SimpleAuthService;
import DatabaseHelper.SQLHelper;
import StorageService.FileStorageService;
import StorageService.StorageOperation;
import protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket server;
    private static ExecutorService THREAD_POOL;
    private Configuration cfg;
    private AuthService authService;
    private SQLHelper DB;

    public Server() {
        //подключаем файл конфигурации
        cfg = new Configuration();
        //пока берем упрощенный сервер авторизации
        authService = new SimpleAuthService();
        //подлючаем доступ к базе данных
        DB = new SQLHelper("filestorage", "qwerty");
        System.out.println(DB.getUserDataByLogin("login1").toString());
        //задаем кол-во потоков
        int countOfThreads = Integer.parseInt(cfg.getProperty("server.threads"));
        if (countOfThreads == 0)
            countOfThreads = Runtime.getRuntime().availableProcessors()*2;
        // инициализация ресурсов для работы
        //ToDo: caсhedThreadPool нужно ли??
        THREAD_POOL = Executors.newFixedThreadPool(countOfThreads);

    }

    // запуск сервера
    public void start() {
        try {
            server = new ServerSocket(Integer.parseInt(cfg.getProperty("server.port")));
            System.out.println("Сервер запущен");

            while (true) {
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                System.out.println("Клиент: " + socket.getRemoteSocketAddress());
                //каждому новому клиенту поток
                new Thread(() -> {
                    //тестируем протокол
                    ObjectInputStream inObj = null;
                    ObjectOutputStream outObj = null;
                    try {
                        //открываем обработчики сообщений
                        inObj = new ObjectInputStream(socket.getInputStream());
                        outObj = new ObjectOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } //TODO: блок finally

                    //авторизация
                    boolean isNotAuth = true;
                    while(isNotAuth) {
                        //Обрабатываем сообщения от клиента
                        ExchangeProtocol incMessage = null;
                        //исходящее сообщение
                        ExchangeProtocol outMessage = new ExchangeProtocol();
                        try {
                            incMessage = (ExchangeProtocol) inObj.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        System.out.println(incMessage.type);
                        //авторизация пользователя
                        if (incMessage.type == Operation.AUTH) {
                            System.out.println("Проверяем авторизацию пользователя");
                            System.out.println(outMessage.type);
                            System.out.println(incMessage.login);
                            System.out.println(incMessage.password);
                            System.out.println(this.authService.isValidUser(incMessage.login, incMessage.password));
                            if (this.authService.isValidUser(incMessage.login, incMessage.password)) {
                                outMessage.setType(Operation.AUTH_OK);
                                isNotAuth = false;
                            } else {
                                outMessage.setType(Operation.AUTH_ERR);
                            }
                            System.out.println(outMessage.type);
                            try {
                                //пропихиваем ответ назад
                                outObj.writeObject(outMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // цикл после авторизации
                    while (true) {
                        //ждем входящего сообщения
                        ExchangeProtocol incMessage = null;
                        try {
                            incMessage = (ExchangeProtocol) inObj.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (incMessage.type == Operation.UPLOAD){
                            System.out.println(incMessage.type);
                            System.out.println(incMessage.fileName);
                            //ToDo: необходимо сделать проверку на возможность записи файла
                            ExchangeProtocol answer = new ExchangeProtocol();
                            answer.setType(Operation.UPLOAD_CONFIRM);
                            int buffSize = Integer.parseInt(cfg.getProperty("server.bufferSize"));
                            answer.bufferSize = buffSize;
                            try {
                                //пропихиваем ответ назад
                                outObj.writeObject(answer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            this.readFileFromSocket(inObj, incMessage.size, incMessage.fileName, buffSize);
                        }
                    }
                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Стоп");
            this.stop();
        }

    }

    //оставнока сервера и удаление ресурсов
    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        THREAD_POOL.shutdownNow();
    }

    //читаем файл из objectInputStream
    public void readFileFromSocket(ObjectInputStream in, long length, String filename, int bufSize) {
        //создаем сервис хранения файлов
        FileStorageService fss = new FileStorageService(this.cfg);
        fss.initService("login1", filename, StorageOperation.UPLOAD);

        System.out.println("Reading from socket " + filename);

        long readCount = 0;
        int count = 0;

        while(readCount < length) {
            ExchangeProtocol partOfFile = null;
            try {
                partOfFile = (ExchangeProtocol) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
           // System.out.println(partOfFile.toString());
            if (partOfFile.type == Operation.UPLOAD_PART) {
                count = partOfFile.sizeOfPart;
                fss.writePartOfFileToStorage(partOfFile.partFile, count);
                readCount += count;
            }
        }
        fss.stop();
    }

    public void writeFileToSocket(Socket socket, int length, String filename, int bufSize) {
        OutputStream out;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        FileInputStream fileIn;
        try {
            fileIn = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Writing to socket " + filename);
        byte[] buffer = new byte[bufSize];
        int writeCount = 0;
        int count = 0;
        while(writeCount < length) {
            try {
                    count = fileIn.read(buffer);
                    out.write(buffer,0, count);
                    writeCount += count;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fileIn.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    //исходящее сообщение
//                        for (Operation o: Operation.values()) {
//                            ExchangeProtocol outMessage = new ExchangeProtocol();
//                            outMessage.type = o;
//                            try {
//                                outObj.writeObject(outMessage);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }

//                    System.out.println("reading...." + socket.getRemoteSocketAddress());
//                    this.readFileFromSocket(socket, SP.size, SP.fileName, Integer.parseInt(cfg.getProperty("server.bufferSize")));
//                    System.out.println("stop...." + socket.getRemoteSocketAddress());

//                new Thread(() -> {
//                    this.writeFileToSocket(socket, 10_000_000, "client/src/main/resources/test.txt", 1024);
//                    System.out.println("read3");
//
//                }).start();
