/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import sockets.common.MessageHandler;

import sockets.server.controller.Controller;

/**
 *
 * @author Fredrik
 */
public class GameServer {
    private static final int LINGER_TIME = 5000;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final Controller contr = new Controller();
    private int portNo = 8080;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;
    private volatile boolean sent = false;
    
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.parseArguments(args);
        server.serve();
    }
    
    void sendMsg(String msg) {
        sent = true;
        ByteBuffer completeMsg = createMessage(msg);
        messagesToSend.add(completeMsg);
        selector.wakeup();
    }
    
    private ByteBuffer createMessage(String msg) {
        String messageWithLengthHeader = MessageHandler.prependLengthHeader(msg);
        return ByteBuffer.wrap(messageWithLengthHeader.getBytes());
    }    
    
    private void serve() {
        try {
            startSelector();
            startListeningSocketChannel();
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        startHandler(key);
                    } else if (key.isReadable()) {
                        clientRecieve(key);
                    } else if (key.isWritable()) {
                        clientSend(key);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server failed.");
        }
    }
    
    private void startHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        ClientHandler handler = new ClientHandler(this, clientChannel);
        contr.createWords();
        clientChannel.register(selector, SelectionKey.OP_WRITE, new Client(handler, contr.getWord(), contr.getTries()));
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }
    
    private void clientRecieve(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.handler.recvMsg();
        } catch (IOException clientHasClosedConnection) {
            removeClient(key);
        }
    }

    private void clientSend(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.send();
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException clientHasClosedConnection) {
            removeClient(key);
        }
    }

    private void removeClient(SelectionKey clientKey) throws IOException {
        Client client = (Client) clientKey.attachment();
        client.handler.disconnectClient();
        clientKey.cancel();
    }

    private void startSelector() throws IOException {
        selector = Selector.open();
    }

    private void startListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    private void parseArguments(String[] arguments) {
        if (arguments.length > 0) {
            try {
                portNo = Integer.parseInt(arguments[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default.");
            }
        }
    }
    
    private class Client {
        private final ClientHandler handler;
        private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();

        private Client(ClientHandler handler, String word, int tries) {
            this.handler = handler;
            messagesToSend.add(createMessage(word.replaceAll(".", "-")));
        }

        private void send() throws IOException {
            ByteBuffer msg = null;
            while ((msg = messagesToSend.peek()) != null) {
                handler.sendMsg(msg);
                messagesToSend.remove();
            }
        }
    }
}

