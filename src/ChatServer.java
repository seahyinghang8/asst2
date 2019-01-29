// ChatServer

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatServer {
    private static final Charset utf8 = Charset.forName("UTF-8");

    private static final String OK = "200 OK";
    private static final String NOT_FOUND = "404 NOT FOUND";
    private static final String HTML = "text/html";
    private static final String TEXT = "text/plain";
    private static final String DEFAULT_ROOM = "DEFAULT";

    private static final Pattern PAGE_REQUEST
            = Pattern.compile("GET /([\\p{Alnum}]*/?) HTTP.*");
    private static final Pattern PULL_REQUEST
            = Pattern.compile("POST /([\\p{Alnum}]*)/?pull\\?last=([0-9]+) HTTP.*");
    private static final Pattern PUSH_REQUEST
            = Pattern.compile("POST /([\\p{Alnum}]*)/?push\\?msg=([^ ]*) HTTP.*");

    private static final String CHAT_HTML;

    static {
        try {
            CHAT_HTML = getFileAsString("../index.html");
        } catch (final IOException xx) {
            throw new Error("unable to start server", xx);
        }
    }

    private final int port;
    private final Map<String, ChatState> stateByName
            = new HashMap<String, ChatState>();

    public final Queue<ChatTask> tasks = new LinkedList<>();

    /**
     * Constructs a new {@link ChatServer} that will service requests
     * on the specified <code>port</code>. <code>state</code> will be
     * used to hold the current state of the chat.
     */
    public ChatServer(final int port) throws IOException {
        this.port = port;
    }

    /**
     * Starts the server. You want to add any multithreading server
     * startup code here.
     */
    public void runForever() throws IOException {
        @SuppressWarnings("resource") final ServerSocket server = new ServerSocket(port);
        for (int i = 0; i < 7; i++) {
            ChatThread thread = new ChatThread(this);
        }
        while (true) {
            final Socket connection = server.accept();

            handle(connection);
        }
    }

    private static String replaceEmptyWithDefaultRoom(final String room) {
        if (room.isEmpty()) {
            return DEFAULT_ROOM;
        }
        return room;
    }

    /**
     * Handles a request from the client. This method already parses HTTP
     * requests and calls the corresponding ChatState methods for you.
     */
    private void handle(final Socket connection) throws IOException {
        final BufferedReader xi
                = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final OutputStream xo = new BufferedOutputStream(connection.getOutputStream());

        final String request = xi.readLine();

        synchronized (tasks) {
            System.out.println(request);
            tasks.add(new ChatTask(xo, connection, request));
            tasks.notify();
        }
        System.out.println("Main is out!");
    }

    public ChatState getState(final String room) {
        ChatState state;
        synchronized (stateByName) {
            state = stateByName.get(room);
            if (state == null) {
                state = new ChatState(room);
                stateByName.put(room, state);
            }
        }
        return state;
    }

    /**
     * Reads the resource with the specified path as a string, and
     * then returns the string.
     */
    private static String getFileAsString(final String path)
            throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(path));
        return new String(fileBytes, utf8);
    }

    /**
     * Runs a chat server, with a default port of 8080.
     */
    public static void main(final String[] args) throws IOException {
        final int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        new ChatServer(port).runForever();
    }
}
