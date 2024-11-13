package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static boolean isExit = false;
    private static final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, DataOutputStream> users = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread connectionHandler = new ConnectionHandler(users, messages);
        connectionHandler.start();

        Thread messageSender = new MessageSender(users, messages);
        messageSender.start();

        Scanner scanner = new Scanner(System.in);

        while (!isExit) {
            if (scanner.nextLine().equalsIgnoreCase("exit")) {
                connectionHandler.interrupt();
                messageSender.interrupt();
                isExit = true;
            }
        }

        Thread.sleep(5000);

        System.out.println("Активные потоки перед завершением:");
        Thread.getAllStackTraces().keySet().forEach(thread -> {
            System.out.println("Поток: " + thread.getName() + ", Состояние: " + thread.getState());
        });
    }
}