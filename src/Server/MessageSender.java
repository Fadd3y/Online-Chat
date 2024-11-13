package Server;

import java.io.DataOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageSender extends Thread {

    private final ConcurrentHashMap<String, DataOutputStream> users;
    private final ConcurrentLinkedQueue<Message> messages;
    private final ExecutorService sendExecutor;

    public MessageSender(ConcurrentHashMap<String, DataOutputStream> users, ConcurrentLinkedQueue<Message> messages) {
        super("MessageSender");
        this.users = users;
        this.messages = messages;
        sendExecutor = Executors.newFixedThreadPool(2);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "здесь");
        while (!isInterrupted()) {
            try {
                Thread.sleep(10);
                if (!messages.isEmpty()) {
                    Message message = messages.peek();
                    users.values().forEach(user -> sendExecutor.submit(new SendMessageThread(user, message)));
                    messages.remove();
                }
            } catch (Exception e) {
                System.out.println("Отправщик завершил работу в блоке кэтч");
                sendExecutor.shutdown();
            }

        }
        sendExecutor.shutdown();
        System.out.println("Отправщик завершил работу");
    }

    public static class SendMessageThread extends Thread {
        Message message;
        DataOutputStream user;

        public SendMessageThread(DataOutputStream user, Message message) {
            this.message = message;
            this.user = user;
        }

        @Override
        public void run() {
            try {
                user.writeUTF(message.toString());
                System.out.println(Thread.currentThread().getName() + "здесь");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void sendUTF(String text) {

        }
    }
}
