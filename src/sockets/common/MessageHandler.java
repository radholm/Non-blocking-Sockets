/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.common;

/**
 *
 * @author Fredrik
 */
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;

/**
 * Handles receiving partial or multiple messages. Received data is sent to this class, and
 * individual messages are extracted. All methods are thread safe.
 */
public class MessageHandler {
    private StringBuilder recvdChars = new StringBuilder();
    private final Queue<String> messages = new ArrayDeque<>();

    /**
     * @return The first received message that has not previously been returned, or
     *         <code>null</code> if there is no complete message available.
     */
    public synchronized String nextMsg() {
        return messages.poll();
    }

    /**
     * @return <code>true</code> if there is at least one unread complete message,
     *         <code>false</code> if there is not.
     */
    public synchronized boolean hasNext() {
        return !messages.isEmpty();
    }

    /**
     * Prepends a length header to the specified message. This method should be used by senders. The
     * returned message can be handled by instances of this class when the message is received.
     *
     * @param msgWithoutHeader A message with no length header
     * @return The specified message, with the appropriate length header prepended.
     */
    public static String prependLengthHeader(String msgWithoutHeader) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(Integer.toString(msgWithoutHeader.length()));
        joiner.add(msgWithoutHeader);
        return joiner.toString();
    }
}
