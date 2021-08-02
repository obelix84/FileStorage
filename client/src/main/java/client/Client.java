package client;

import protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    //private Socket socket;
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(5);

    public Client() {
        this.connect();
    }

    public void connect() {
        for (int i = 0; i < 1; i++) {
            THREAD_POOL.execute(() -> {
                try {
                    Socket socket2 = new Socket("localhost", 10000);
                    System.out.println("connected");
                    //посылаем протокол
                    SimpleProtocol message2 = new SimpleProtocol(Operation.UPLOAD,
                            "server/src/main/resources/test1_serv.txt", 10_000_000);

                    ObjectOutputStream outObj2 = new ObjectOutputStream(socket2.getOutputStream());

                    System.out.println(message2.fileName);
                    System.out.println(message2.size);
                    System.out.println(message2.type);

                    System.out.println("Пишем сообщение");
                    outObj2.writeObject(message2);
                    System.out.println("Кидаем файл");
                    System.out.println("-");
                    //outObj.close();
                    System.out.println("--");
                    OutputStream out2 = socket2.getOutputStream();
                    System.out.println("---");
                    System.out.println(System.getProperty("user.dir"));
                    FileInputStream fin2 = new FileInputStream("client/src/main/resources/test1.txt");
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = fin2.read(buffer)) > 0 ) {
                        System.out.println("читаем 1 " + socket2.getRemoteSocketAddress());
                        out2.write(buffer, 0, count);
                    }
                    socket2.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.disconnect();
                }
            });
            THREAD_POOL.execute(() -> {
                try {
                    Socket socket1 = new Socket("localhost", 10000);
                    System.out.println("connected");
                    //посылаем протокол
                    SimpleProtocol message1 = new SimpleProtocol(Operation.UPLOAD,
                            "server/src/main/resources/test2_serv.txt", 6504);

                    ObjectOutputStream outObj1 = new ObjectOutputStream(socket1.getOutputStream());

                    System.out.println(message1.fileName);
                    System.out.println(message1.size);
                    System.out.println(message1.type);

                    System.out.println("Пишем сообщение");
                    outObj1.writeObject(message1);
                    System.out.println("Кидаем файл");
                    System.out.println("-");
                    //outObj.close();
                    System.out.println("--");
                    OutputStream out1 = socket1.getOutputStream();
                    System.out.println("---");
                    System.out.println(System.getProperty("user.dir"));
                    FileInputStream fin1 = new FileInputStream("client/src/main/resources/test2.txt");
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = fin1.read(buffer)) > 0 ) {
                        System.out.println("читаем 2" + socket1.getRemoteSocketAddress());
                        out1.write(buffer, 0, count);
                    }
                    socket1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    this.disconnect();
                }
            });

            THREAD_POOL.execute(() -> {
                try {
                    Socket socket1 = new Socket("localhost", 10000);
                    System.out.println("connected");
                    //посылаем протокол
                    SimpleProtocol message1 = new SimpleProtocol(Operation.UPLOAD,
                            "server/src/main/resources/test3_serv.txt", 1533);

                    ObjectOutputStream outObj1 = new ObjectOutputStream(socket1.getOutputStream());

                    System.out.println(message1.fileName);
                    System.out.println(message1.size);
                    System.out.println(message1.type);

                    System.out.println("Пишем сообщение");
                    outObj1.writeObject(message1);
                    System.out.println("Кидаем файл");
                    System.out.println("-");
                    //outObj.close();
                    System.out.println("--");
                    OutputStream out1 = socket1.getOutputStream();
                    System.out.println("---");
                    System.out.println(System.getProperty("user.dir"));
                    FileInputStream fin1 = new FileInputStream("client/src/main/resources/test3.txt");
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = fin1.read(buffer)) > 0 ) {
                        System.out.println("читаем 2" + socket1.getRemoteSocketAddress());
                        out1.write(buffer, 0, count);
                    }
                    socket1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    this.disconnect();
                }
            });

//            THREAD_POOL.execute(() -> {
//                try {
//                    socket = new Socket("localhost", 10000);
//                    InputStream in = socket.getInputStream();
//                    System.out.println("connected");
//                    System.out.println(System.getProperty("user.dir"));
//                    FileOutputStream fout = new FileOutputStream("client/src/main/resources/test_client.txt");
//                    byte[] buffer = new byte[1024];
//                    int count = 0;
//                    while ((count = in.read(buffer)) > 0 ) {
//                        System.out.println("читаем ");
//                        fout.write(buffer, 0, count);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    this.disconnect();
//                }
//            });
            THREAD_POOL.shutdown();
        }

    }

    public void disconnect() {
//        try {
//            //socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("disconnect");
        //THREAD_POOL.shutdown();
    }

}
