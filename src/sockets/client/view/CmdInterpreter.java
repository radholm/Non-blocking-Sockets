/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.view;

import java.net.InetSocketAddress;
import java.util.Scanner;
import sockets.client.net.ServerConnect;
import sockets.client.net.ServerResponse;

/**
 *
 * @author Fredrik
 */
public class CmdInterpreter implements Runnable {
    public static final String CONNECT = ".connect";
    public static final String QUIT = ".quit";
    public static final String NEWWORD = ".newword";
    public static final String HELP = ".help";
    private static final String PROMPT = "> ";
    private String[] strToken;
    private final Scanner console = new Scanner(System.in);
    private boolean inputCmds = false;
    private ServerConnect server;
    private final ThreadSafeOutput outMgr = new ThreadSafeOutput();

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (inputCmds) {
            return;
        }
        inputCmds = true;
        server = new ServerConnect();
        new Thread(this).start();
    }

    /**
     * Interprets and performs commands.
     */
    @Override
    public void run() {
        outMgr.println("Multithreaded hangman with sockets, .help for commands\n");
        while (inputCmds) {
            try {
                String command = (readNextLine());
                strToken = command.split("\\s+");
                switch (strToken[0]) {
                    case QUIT:
                        inputCmds = false;
                        server.disconnect();
                        break;
                    case CONNECT:
                        server.addListener(new ConsoleOutput());
                        server.connect(strToken[1],
                                      Integer.parseInt(strToken[2]));
                        break;
                    case NEWWORD:
                        server.sendMsgEntry(command);
                        break;
                    case HELP:
                        outMgr.println("\nAvailable Commands:\n" 
                                        + QUIT + "\n" + CONNECT + " <ip adress> <port>" + "\n" 
                                        + NEWWORD + "\n" + HELP + "\n");
                    default:
                        server.sendMsgEntry(command);
                }
            } catch (Exception e) {
                outMgr.println("Operation failed. Try again.");
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    private class ConsoleOutput implements ServerResponse {
        @Override
        public void handleMsg(String msg) {
            outMgr.println((String) msg);
            outMgr.print(PROMPT);
        }

        @Override
        public void connected(InetSocketAddress serverAddress) {
            outMgr.println("Connected to " + serverAddress.getHostName() + ":"
                           + serverAddress.getPort());
            outMgr.print(PROMPT);
        }

        @Override
        public void disconnected() {
            outMgr.println("Disconnected from server.");
            outMgr.print(PROMPT);
        }
    }
}
