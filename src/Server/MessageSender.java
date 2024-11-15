package Server;

import java.io.DataOutputStream;
import java.util.concurrent.*;

public class MessageSender extends Thread {

    private final ConcurrentHashMap<String, DataOutputStream> users;
    private final LinkedBlockingQueue<Message> messages;
    private final ExecutorService sendExecutor;

    public MessageSender(ConcurrentHashMap<String, DataOutputStream> users, LinkedBlockingQueue<Message> messages) {
        super("MessageSender");
        this.users = users;
        this.messages = messages;
        sendExecutor = Executors.newFixedThreadPool(2);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Message message = getMessageFromQueue();
                sendMessageToUsers(message);
            } catch (InterruptedException | ExecutionException e){
                Thread.currentThread().interrupt();
            }
        }
        sendExecutor.shutdownNow();
        System.out.println("Отправщик завершил работу");
    }

    private Message getMessageFromQueue() throws InterruptedException , ExecutionException {
        CompletableFuture<Message> getMessage = CompletableFuture.supplyAsync(() -> {
            try {
                return messages.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        Message message = null;
        while (!isInterrupted()) {
            try {
                message = getMessage.get(5, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException e) {
                System.out.println("Ждем сообщения для отправки...");
            }
        }
        return message;
    }

    private void sendMessageToUsers(Message message) {
        var outputList = users.values();
        for (var output : outputList) {
            sendExecutor.submit(new SendMessageThread(output, message));
        }
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
