import Server.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String username;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client(String username, DataInputStream inputStream, DataOutputStream outputStream) {
        this.username = username;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public static void main(String[] args) throws IOException {
        try(Socket socket = new Socket("127.0.0.1", 23456);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
        ) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите имя пользователя:");
            String username = scanner.nextLine();


            Client client = new Client(username, inputStream, outputStream);

            Thread readInput = new Thread(() -> {
                try {
                    while (true) {
                        System.out.println(client.read());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            readInput.start();

            while (true) {
                String text = scanner.nextLine();
                client.send(text);
            }
        }
    }

    private void send(String message) throws IOException {
        var messageToSend = new Message(username, message);
        outputStream.writeUTF(messageToSend.toString());
    }

    private Message read() throws IOException {
        return Message.parseMessage(inputStream.readUTF());
    }
}
