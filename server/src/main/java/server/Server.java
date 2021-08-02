package server;

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

    public Server() {
        //подключаем файл конфигурации
        cfg = new Configuration();
        //задаем кол-во потоков
        int countOfThreads = Integer.parseInt(cfg.getProperty("thread.count"));
        if (countOfThreads == 0)
            countOfThreads = Runtime.getRuntime().availableProcessors()*2;
        // инициализация ресурсов для работы
        //ToDo: caсhedThreadPool нужно ли??
        THREAD_POOL = Executors.newFixedThreadPool(countOfThreads);

    }

    // запуск сервера
    public void start() {
        try {
            server = new ServerSocket(10000);
            System.out.println("Server started");

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                //каждому новому клиенту поток
                new Thread(() -> {
                    //тестируем протокол
                    //Обрабатываем сообщения от клиента
                    SimpleProtocol SP = null;
                    ObjectInputStream inObj = null;
                    try {
                        inObj = new ObjectInputStream(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        SP = (SimpleProtocol) inObj.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println(SP.fileName);
                    System.out.println(SP.size);
                    System.out.println(SP.type);
                    System.out.println("reading...." + socket.getRemoteSocketAddress());
                    this.readFileFromSocket(socket, SP.size, SP.fileName, Integer.parseInt(cfg.getProperty("buffer.size")));
                    System.out.println("stop...." + socket.getRemoteSocketAddress());


                }).start();
//                new Thread(() -> {
//                    this.writeFileToSocket(socket, 10_000_000, "client/src/main/resources/test.txt", 1024);
//                    System.out.println("read3");
//
//                }).start();
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
        THREAD_POOL.shutdownNow();
    }
    //читаем файл из сокета
    public void readFileFromSocket(Socket socket, int length, String filename, int bufSize) {
        InputStream in;
        try {
            in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FileStorageService fss = new FileStorageService();
        fss.initService(filename, StorageOperation.UPLOAD);

        System.out.println("Reading from socket " + filename);
        byte[] buffer = new byte[bufSize];
        int readCount = 0;
        int count = 0;
        while(readCount < length) {
            try {
                if ((count = in.read(buffer)) > 0) {
                    fss.writePartOfFileToStorage(buffer, count);
                    readCount += count;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fss.stop();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
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
