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

    private final int port;
    private final Map<String, ChatState> stateByName
            = new HashMap<String, ChatState>();

    // added a queue of tasks for the threads to handle
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
        // initializes 8 chatting threads that will handle the requests
        for (int i = 0; i < 7; i++) {
            ChatThread thread = new ChatThread(this);
            thread.start();
        }
        while (true) {
            // accept any incoming connections
            final Socket connection = server.accept();
            handle(connection);
        }
    }

    /**
     * Handles a request from the client. This method already parses HTTP
     * requests and passses the work to a chatting thread.
     */
    private void handle(final Socket connection) throws IOException {
        // parses the connection
        final BufferedReader xi
                = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final OutputStream xo = new BufferedOutputStream(connection.getOutputStream());
        final String request = xi.readLine();
        // lock the task queue before adding a new element task into the queue
        synchronized (tasks) {
            tasks.add(new ChatTask(xo, connection, request));
            // notify 1 waiting thread (if any)
            // which will then wake up and take the work from the queue
            tasks.notify();
        }
    }

    public ChatState getState(final String room) {
        ChatState state = stateByName.get(room);
        if (state == null) {
            state = new ChatState(room);
            stateByName.put(room, state);
        }
        return state;
    }
    
    /**
     * Runs a chat server, with a default port of 8080.
     */
    public static void main(final String[] args) throws IOException {
        final int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        new ChatServer(port).runForever();
    }
}
