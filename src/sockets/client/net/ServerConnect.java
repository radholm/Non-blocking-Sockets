/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author Fredrik
 */
public class ServerConnect implements Runnable {
    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(100);
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final List<ServerResponse> listeners = new ArrayList<>();
    private SocketChannel socketChannel;
    private InetSocketAddress serverAddress;
    private Selector selector;
    private boolean connected;
    private volatile boolean sent = false;
    
    @Override
    public void run() {
        try {
            startConnection();
            startSelector();
            
            while (connected || !messagesToSend.isEmpty()) {
                if(sent) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    sent = false;
                }
                
                selector.select();
                for(SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if(!key.isValid()) {
                        continue;
                    }
                    if(key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        serverRecieve(key);
                    } else if (key.isWritable()) {
                        serverSend(key);
                    }
                }
            }
        } catch(Exception e) {
            System.err.println("Disconnected.");
        }
        try {
            doDisconnect();
        } catch(IOException ex) {
            System.err.println("Disconnection failed, aborts...");
        }
    }
    
    public void addListener(ServerResponse listener) {
        listeners.add(listener);
    }
    
    public void connect(String host, int port) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
    }
    
    private void startSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
    
    private void startConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }
    
    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            notifyConnectionDone(remoteAddress);
        } catch(IOException couldNotGetRemAddrUsingDefaultInstead) {
            notifyConnectionDone(serverAddress);
        }
    }
    
    public void disconnect() throws IOException {
        connected = false;
        sendMsgEntry("Client is Disconnected");
    }
    
    private void doDisconnect() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
        notifyDisconnectionDone();
    }

    public void sendMsgEntry(String msg) {
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(msg.getBytes()));
        }
        sent = true;
        selector.wakeup();
    }
    
    private void serverSend(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void serverRecieve(SelectionKey key) throws IOException {
        msgFromServer.clear();
        int numOfReadBytes = socketChannel.read(msgFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException("Lost Connection");
        }
        String recvdString = extractMessageFromBuffer();
        notifyMsgReceived(recvdString);
    }

    private String extractMessageFromBuffer() {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        return new String(bytes);
    }
    
    private void notifyConnectionDone(InetSocketAddress connectedAddress) {
        Executor pool = ForkJoinPool.commonPool();
        for (ServerResponse listener : listeners) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.connected(connectedAddress);
                }
            });
        }
    }
    
    private void notifyDisconnectionDone() {
        Executor pool = ForkJoinPool.commonPool();
        for (ServerResponse listener : listeners) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.disconnected();
                }
            });
        }
    }
    
    private void notifyMsgReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        for (ServerResponse listener : listeners) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.handleMsg(msg);
                }
            });
        }
    }
}
