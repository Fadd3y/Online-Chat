package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static boolean isExit = false;
    private static final LinkedBlockingQueue<Message> MESSAGES = new LinkedBlockingQueue<>();
    private static final ConcurrentHashMap<String, DataOutputStream> USERS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        Thread connectionHandler = new ConnectionHandler(USERS, MESSAGES);
        connectionHandler.start();

        Thread messageSender = new MessageSender(USERS, MESSAGES);
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