import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// The different types of tasks
enum Tasks {
    NULL, PAGE, PULL, PUSH, EXCEPTION
}

public class ChatThread extends Thread {
    private final ChatServer server;
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

    /**
     * Writes a minimal but valid HTTP response to
     * <code>output</code>.
     */
    static void sendResponse(final OutputStream xo,
                             final String status,
                             final String contentType,
                             final String content) throws IOException {
        final byte[] data = content.getBytes(utf8);
        final String headers =
                "HTTP/1.0 " + status + "\r\n" +
                        "Content-Type: " + contentType + "; charset=utf-8\r\n" +
                        "Content-Length: " + data.length + "\r\n\r\n";

        xo.write(headers.getBytes(utf8));
        xo.write(data);
        xo.flush();

        System.out.println(currentThread() + ": replied with " + data.length + " bytes");
    }

    // initializing the chat thread which extends from the thread class
    public ChatThread(ChatServer server) {
        super();
        this.server = server;
    }

    public void run() {
        // threads that are created will loop indefinitely
        while (true) {
            // create a local task variable
            ChatTask task;
            // get exclusive access to the task queue
            synchronized (this.server.tasks) {
                // wait till the task queue is not empty
                while (this.server.tasks.isEmpty()) {
                    try {
                        this.server.tasks.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // dequeue the task
                task = server.tasks.remove();
                System.out.println(task.request);
            }

            // process the task
            try {
                System.out.println("Got a request from a chat client for " + Thread.currentThread() + ": " + task.request);

                Matcher m;
                if (task.request == null) {
                    ChatThread.sendResponse(task.xo, NOT_FOUND, TEXT, "Empty request.");
                } else if (PAGE_REQUEST.matcher(task.request).matches()) {
                    ChatThread.sendResponse(task.xo, OK, HTML, CHAT_HTML);
                } else if ((m = PULL_REQUEST.matcher(task.request)).matches()) {
                    String room = replaceEmptyWithDefaultRoom(m.group(1));
                    final long last = Long.valueOf(m.group(2));
                    ChatThread.sendResponse(task.xo, OK, TEXT, this.server.getState(room).recentMessages(last));
                } else if ((m = PUSH_REQUEST.matcher(task.request)).matches()) {
                    String room = replaceEmptyWithDefaultRoom(m.group(1));
                    final String msg = m.group(2);
                    this.server.getState(room).addMessage(msg);
                    ChatThread.sendResponse(task.xo, OK, TEXT, "ack");
                } else {
                    ChatThread.sendResponse(task.xo, NOT_FOUND, TEXT, "Malformed request.");
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            } finally {
                try {
                    task.connection.close();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
        }
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

    private static String replaceEmptyWithDefaultRoom(final String room) {
        if (room.isEmpty()) {
            return DEFAULT_ROOM;
        }
        return room;
    }
}
