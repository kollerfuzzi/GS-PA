/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import beans.PlayerData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koller
 */
public class Client implements Runnable {

    private Socket so;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String name;
    private Receiver recv;

    boolean running = false;

    private List<String> others = new ArrayList<String>();

    public Client(Receiver recv) {
        this.recv = recv;
    }

    public void connect(String address) throws IOException {
        running = true;
        this.so = new Socket(address, 8080);
        this.out = new ObjectOutputStream(this.so.getOutputStream());
        this.in = new ObjectInputStream(this.so.getInputStream());
        Thread t = new Thread(this);
        t.start();
        this.so.setSoTimeout(500);
    }

    public void disconnect() {

    }

    public void send(Object obj) {
        try {
            this.out.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receive() throws IOException, ClassNotFoundException {
        recv.receive(this.in.readObject());
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    receive();
                } catch (SocketTimeoutException ex) {
                    System.out.println("ups");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
        }
    }
}

interface Receiver {

    public void receive(Object obj);
}
