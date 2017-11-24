/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.net;

import java.net.InetSocketAddress;

/**
 *
 * @author Fredrik
 */
public interface ServerResponse {
    public void handleMsg(String msg);
    
    public void connected(InetSocketAddress serverAddress);
    
    public void disconnected();
}
