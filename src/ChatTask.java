import java.io.OutputStream;
import java.net.Socket;

public class ChatTask {

    public final OutputStream xo;
    public final String request;
    public final Socket connection;

    public ChatTask(final OutputStream xo,
                    final Socket connection,
                    final String request){
        this.xo = xo;
        this.connection = connection;
        this.request = request;

    }
}
