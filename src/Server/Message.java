package Server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    private final String username;
    private final String text;
    private final LocalTime time;

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public LocalTime getTime() {
        return time;
    }

    public Message(String username, String text) {
        this.username = username;
        this.text = text;
        time = LocalTime.now();
    }

    public Message(String username, String text, LocalTime time) {
        this.username = username;
        this.text = text;
        this.time = time;
    }

    public static Message parseMessage(String message) {
        Pattern pattern = Pattern.compile("(USERNAME|TEXT|TIME): (\".*\"|\\S+)");
        Matcher matcher = pattern.matcher(message);

        var parameters = new HashMap<String, String>();
        while(matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            parameters.put(key, value);
        }

        String from = parameters.get("USERNAME");
        String text = parameters.get("TEXT").replaceAll("(^\\\")|(\\\"$)", "");
        LocalTime time = LocalTime.parse(parameters.get("TIME"));


        return new Message(from, text, time);
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = dtf.format(this.time);
        return  String.format("USERNAME: %s TIME: %s TEXT: \"%s\"", username, time, text);
    }
}

