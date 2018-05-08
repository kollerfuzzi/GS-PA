/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koller
 */
public class Client implements Runnable{
    private Socket so;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client() {
        
    }
    
    public void connect(String address) throws IOException {
        this.so = new Socket(address, 18259);
        this.out = new ObjectOutputStream(this.so.getOutputStream());
        this.in = new ObjectInputStream(this.so.getInputStream());
        Thread t = new Thread(this);
        t.start();
    }
    
    public void send(Object obj) {
        try {
            this.out.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void receive() {
        
    }

    @Override
    public void run() {
        receive();
    }
}

interface Receiver {
    public void receive(Object obj);
}