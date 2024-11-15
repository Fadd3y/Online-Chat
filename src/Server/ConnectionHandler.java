package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.*;


public class ConnectionHandler extends Thread {
    private final ConcurrentHashMap<String, DataOutputStream> USERS;
    private final LinkedBlockingQueue<Message> MESSAGES;
    private static final int PORT = 23456;

    private final ArrayList<Connection> connections = new ArrayList<>();

    ConnectionHandler(ConcurrentHashMap<String, DataOutputStream> users, LinkedBlockingQueue<Message> messages) {
        super("ConnectionHandler");
        this.USERS = users;
        this.MESSAGES = messages;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)
        ) {
            serverSocket.setSoTimeout(1000);
            while (!isInterrupted()) {
                try {
                    Connection connection = new Connection(serverSocket.accept());
                    connection.start();
                    connections.add(connection);

                } catch (Exception e) {
                    //System.out.println("ждем");
                }
            }
            connections.forEach(Thread::interrupt);
            System.out.println("Менеджер подключений завершил работу");
        } catch (IOException e) {

        }
    }

    private class Connection extends Thread {
        Socket client;
        String username;

        public Connection(Socket client) throws SocketException {
            this.client = client;
        }

        @Override
        public void run() {
            try (DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                 DataInputStream inputStream = new DataInputStream(client.getInputStream())
            ) {
                String readUTF2 = readUTFMessage(inputStream);

                if (readUTF2 != null) {
                    initUserByFirstMessage(readUTF2, outputStream);
                } else {
                    disconnectUser();
                    return;
                }

                System.out.println(username + " подключился");

                while (!isInterrupted()) {
                    String readUTF = readUTFMessage(inputStream);
                    if (readUTF != null) {
                        Message message = Message.parseMessage(readUTF);
                        System.out.println(message);
                        sendMessage(message);
                    }
                }
            } catch (Exception e) {
                disconnectUser();
            }
        }

        private void sendMessage(Message message) {
            MESSAGES.add(message);
        }

        private void disconnectUser() {
            USERS.remove(Thread.currentThread().getName());
            Message message = new Message(username, username + " вышел.");
            sendMessage(message);

            System.out.println(username + " вышел");
        }

        private void initUserByFirstMessage(String inputLine, DataOutputStream outputStream) {
            Message message = Message.parseMessage(inputLine);
            System.out.println(message);
            username = message.getUsername();
            currentThread().setName(username);
            USERS.put(username, outputStream);
            sendMessage(message);
        }

        private String readUTFMessage(DataInputStream inputStream) throws ExecutionException, InterruptedException {
            CompletableFuture<String> message = CompletableFuture.supplyAsync(() -> {
                String receivedMessage = null;
                try {
                    receivedMessage = inputStream.readUTF();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
                return receivedMessage;
            });

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    return message.get(5, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    System.out.println("Тайм-аут ожидания, повторная проверка...");
                }
            }
            return null;
        }

    }
}
