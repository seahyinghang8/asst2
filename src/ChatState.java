// ChatState

import java.util.LinkedList;

public class ChatState {
    private static final int MAX_HISTORY = 32;

    private final String name;
    private final LinkedList<String> history = new LinkedList<String>();
    private long lastID = System.currentTimeMillis();

    public ChatState(final String name) {
        this.name = name;
        history.addLast("Hello " + name + "!");
    }

    /**
     * Returns the name of the chat room.
     */
    public String getName() {
        return name;
    }

    /**
     * This method adds a new message to the chat room history, and
     * increments the <code>lastID</code> variable to track how many
     * messages have been posted. If the history is longer than
     * <code>MAX_HISTORY</code> messages, it also removes the oldest
     * method.
     *
     * TODO: Modify this method to be thread-safe. Note that this
     * method is supposed to wake up any blocked calls to
     * {@link #recentMessages(long)} so that they can return the newly posted
     * messages.
     */
    public void addMessage(final String msg) {
        // access to the chat history of a single room has to be locked
        // to prevent any other threads from adding new messages / deleting messages
        synchronized (history) {
            history.addLast(msg);
            // since lastID is only accessed when history is being accessed,
            // it can be updated without the need for a lock
            ++lastID;
            if (history.size() > MAX_HISTORY) {
                history.removeFirst();
            }
            // wake up all the other threads waiting to update their clients
            // with the most recent messages
            history.notifyAll();
        }
    }

    /**
     * A helper method which returns the number of outstanding
     * messages.
     */
    private int messagesToSend(final long mostRecentSeenID) {
        final long count = lastID - mostRecentSeenID;
        if (count < 0 || count > history.size()) {
            return history.size();
        } else {
            return (int) count;
        }
    }

    /**
     * This method checks to see if there are new messages in the chat
     * room. If yes, then it returns immediately with those
     * messages. If no, then it waits up to 15 seconds for new
     * messages for new messages to arrive, and then returns.
     *
     * TODO: The starter code uses <code>Thread.sleep()</code> to wait
     * 15 seconds for new messages. Change this to use proper
     * synchronization instead. Note that this method is supposed to
     * return the instant new messages are available, not continue to
     * wait even after messages have been posted.
     */
    public String recentMessages(long mostRecentSeenID) {
        final StringBuffer buf = new StringBuffer();
        // First, acquire a lock to history (shared object among threads)
        synchronized (history) {
            // messagesToSend will inspect the lastID, which should only
            // be updated once the history lock is acquired
            int count = messagesToSend(mostRecentSeenID);
            if (count == 0) {
                try {
                    // wait till a new message is ready to be updated
                    history.wait(15000);
                } catch (final InterruptedException xx) {
                    throw new Error("unexpected", xx);
                }
                count = messagesToSend(mostRecentSeenID);
            }


            // If count == 1, then id should be lastID on the first
            // iteration.
            long id = lastID - count + 1;
            for (String msg : history.subList(history.size() - count, history.size())) {
                buf.append(id).append(": ").append(msg).append('\n');
                ++id;
            }
        }
        return buf.toString();
    }
}
