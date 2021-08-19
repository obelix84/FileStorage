package server;

import AuthService.*;
import DatabaseHelper.SQLHelper;
import StorageService.*;
import StorageService.StorageOperation;
import StorageService.StorageService;
import protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        //подлючаем доступ к базе данных
        DB = new SQLHelper("filestorage", "qwerty");
        //пока берем упрощенный сервер авторизации
        authService = new DBAuthService(DB);
        UserData UD = DB.getUserDataByLogin("login2");
        //System.out.println(UD);
        //System.out.println(DB.getFileInfo("02.avi", 1).toString());
        //717477888
        //System.out.println(DB.updateFileInfo(0, 717477888));
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
                THREAD_POOL.execute(() -> {
                    //тестируем протокол
                    ObjectInputStream inObj = null;
                    ObjectOutputStream outObj = null;
                    //пользователь работающий в данном потоке
                    UserData user = null;
                    try {
                        //открываем обработчики сообщений
                        inObj = new ObjectInputStream(socket.getInputStream());
                        outObj = new ObjectOutputStream(socket.getOutputStream());
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
                                System.out.println(this.authService.getUserData(incMessage.login, incMessage.password));
                                user = this.authService.getUserData(incMessage.login, incMessage.password);
                                if (user != null) {
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
                                System.out.println(incMessage);

                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //проверяем наличие файла в хранилище
                                FileInfo file = new FileInfo(-1, incMessage.getFileName(), incMessage.getSubDir(),
                                        incMessage.getSize(), user.getUserId());
                                System.out.println(file);
                                //проверка на существование файла в хранилище
                                if (SS.isFileExist(user ,file)) {
                                    ExchangeProtocol answer = new ExchangeProtocol();
                                    answer.setType(Operation.UPLOAD_EXIST);
                                    try {
                                        //пропихиваем ответ назад
                                        outObj.writeObject(answer);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    continue;
                                }
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
                                this.readFileFromSocket(inObj, user, file, SS,buffSize);
                                SS.stop();
                            }
                            if (incMessage.type == Operation.UPLOAD_OVERWRITE) {

                                System.out.println(incMessage.type);
                                System.out.println(incMessage.fileName);
                                System.out.println(incMessage);

                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //проверяем наличие файла в хранилище
                                FileInfo file = new FileInfo(-1, incMessage.getFileName(), incMessage.getSubDir(),
                                        incMessage.getSize(), user.getUserId());
                                System.out.println(file);

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
                                this.readFileFromSocket(inObj, user, file, SS,buffSize);
                                SS.stop();
                            }
                            if (incMessage.type == Operation.LIST) {
                                System.out.println(incMessage);
                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //получаем список файлов из хранилища
                                ArrayList<String> listFiles = (ArrayList<String>) SS.getList(user);
                                Collections.sort(listFiles);
                                //формируем ответ
                                ExchangeProtocol listAnswer = new ExchangeProtocol();
                                listAnswer.setType(Operation.LIST);
                                listAnswer.setList(listFiles);
                                try {
                                    //пропихиваем ответ назад
                                    outObj.writeObject(listAnswer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                SS.stop();
                            }
                            if (incMessage.type == Operation.EXIST) {
                                System.out.println(incMessage);
                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //проверяем наличие файла в хранилище
                                FileInfo file = new FileInfo(-1, incMessage.getFileName(), incMessage.getSubDir(),
                                        incMessage.getSize(), user.getUserId());
                                System.out.println(file);
                                //проверка на существование файла в хранилище
                                ExchangeProtocol existAnswer = new ExchangeProtocol();
                                if (SS.isFileExist(user, file)) {
                                    existAnswer.setType(Operation.EXIST_TRUE);
                                } else {
                                    existAnswer.setType(Operation.EXIST_FALSE);
                                }
                                try {
                                    //пропихиваем ответ назад
                                    outObj.writeObject(existAnswer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                SS.stop();
                            }
                            if (incMessage.type == Operation.DELETE) {
                                System.out.println(incMessage);
                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //проверяем наличие файла в хранилище
                                FileInfo file = new FileInfo(-1, incMessage.getFileName(), incMessage.getSubDir(),
                                        incMessage.getSize(), user.getUserId());
                                SS.deleteFile(user, file);
                                SS.stop();
                            }
                            if (incMessage.type == Operation.DOWNLOAD) {
                                System.out.println(incMessage);
                                //открываем доступ к хранилищу
                                StorageService SS = new HybridStorageService(this.cfg, this.DB);
                                //проверяем наличие файла в хранилище
                                FileInfo file = new FileInfo(-1, incMessage.getFileName(), incMessage.getSubDir(),
                                        incMessage.getSize(), user.getUserId());
                                //SS.deleteFile(user, file);
                                SS.stop();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (outObj != null) {
                                outObj.close();
                            }
                            if (inObj != null) {
                                inObj.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
        this.DB.disconnect();
        THREAD_POOL.shutdownNow();
    }

    //читаем файл из objectInputStream
    public void readFileFromSocket(ObjectInputStream in, UserData UD, FileInfo FI, StorageService SS, int bufSize) {
        //создаем сервис хранения файлов с БД

        SS.initService(UD, FI, StorageOperation.UPLOAD);

        System.out.println("Reading from socket " + FI.getFileName());

        long readCount = 0;
        int count = 0;

        while(readCount < FI.getSize()) {
            ExchangeProtocol partOfFile = null;
            try {
                partOfFile = (ExchangeProtocol) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (partOfFile.type == Operation.UPLOAD_PART) {
                count = partOfFile.sizeOfPart;
                SS.writePartOfFileToStorage(partOfFile.partFile, count);
                readCount += count;
            }
        }
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
