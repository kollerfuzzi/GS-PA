/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;

/**
 *
 * @author alex
 */
public class RoundTime implements Serializable{
    private int timeLeft;
    private Type type;

    public RoundTime(int timeLeft, Type type) {
        this.timeLeft = timeLeft;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public enum Type {
        STARTED, STOPPED, NOTHING
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    
}