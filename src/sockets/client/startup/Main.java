/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.startup;

import sockets.client.view.CmdInterpreter;

/**
 *
 * @author Fredrik
 */
public class Main {
    
    public static void main(String[] args) {
        new CmdInterpreter().start();
    }
}
