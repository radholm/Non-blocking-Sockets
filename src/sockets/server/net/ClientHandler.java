/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.common.MessageHandler;
import sockets.server.controller.Controller;

/**
 *
 * @author Fredrik
 */
class ClientHandler implements Runnable {
    private final Controller contr = new Controller();
    private final GameServer server;
    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(100);
    private final MessageHandler msgHandler = new MessageHandler();
    private static final String QUIT = ".quit";
    private static final String NEWWORD = ".newword";
    private boolean connected;
    private String secretWord;
    private int tries, score;
    private String wordToDisplay;
    final Set<String> correctChars;
    final Set<String> incorrectChars;
    
    ClientHandler(GameServer server, SocketChannel clientChannel) {
        this.incorrectChars = new HashSet<>();
        this.correctChars = new HashSet<>();
        this.server = server;
        this.clientChannel = clientChannel;
        connected = true;
    }

    @Override
    public void run() {
        while (msgHandler.hasNext()) {
            try {
                String msg = (msgHandler.nextMsg());
                switch (msg) {
                    case QUIT:
                        break;
                    case NEWWORD:
                        newRound();
                        break;
                    default:
                        msgInterpreter(msg);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    void msgInterpreter(String msg) throws IOException {
        if(msg.equals(".newword")) {
            server.sendMsg("You started a new round\n");
            newRound();
        } else if(msg.contains(".")) {
            //command
        } else if(msg.matches(".*([ \\t]).*")) {
            server.sendMsg("You can't guess with a whitespace\n");
        } else {
            guessHandler(msg);
        }
    }
    
    void guessHandler(String guess) throws IOException {
        String res = contr.guessHandler(guess, secretWord);
        if (correctChars.contains(guess) || incorrectChars.contains(guess)) {
            server.sendMsg("You guessed that already!");
        } else if(res.equals("t")) {
            correctChars.add(guess);
        } else if(res.equals("f")) {
            incorrectChars.add(guess);
            --tries;
        } else if(res.equals("true")) {
            server.sendMsg("You were right!");
            correctChars.add(guess);
        } else {
            server.sendMsg("You were wrong!");
            incorrectChars.add(guess);
            --tries;
        }
        wordToDisplay = contr.maskWord(correctChars, secretWord);
        server.sendMsg(wordToDisplay + " " + "lives: " + tries + " Score: " + score);
        
        if (wordToDisplay.equals(secretWord)) {
            server.sendMsg("You won!\n");
            score++;
            newRound();
        } else if (incorrectChars.size() == secretWord.length()) {
            server.sendMsg("No tries left, you lost! Word was: " + secretWord + "\n");
            score--;
            newRound();
        }
    }
    
    void newRound() throws IOException {
        incorrectChars.clear();
        correctChars.clear();
        contr.createWords();
        secretWord = contr.getWord();
        tries = contr.getTries();
    }
    
    void sendMsg(ByteBuffer msg) throws IOException {
        clientChannel.write(msg);
    }
    
    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }
    
    void recvMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        ForkJoinPool.commonPool().execute(this);
    }
    
    void disconnectClient() throws IOException {
        clientChannel.close();
    }
}
