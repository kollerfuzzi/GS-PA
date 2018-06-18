/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author alex
 */
public class RoundInfo implements Serializable{
    private LocalDateTime predictedStarttime; //the time when the round is supposed to start, if it is not a forced round it should be 5 seconds after at least there are 2 plyer online.
    private String mapname; //there is only one map, but if we decide to add another this should be in this class
    private String roundtype; // same as aboth
    private long roundlength = 300; //length of round in seconds, default is 300, or 5 minutes
    private int playerHelth = 10; //helth of every player in this round, default is 10

    public RoundInfo(LocalDateTime predictedStarttime, long roundlegth, int playerHelth, String mapname, String roundtype) {
        this.predictedStarttime = predictedStarttime;
        this.roundlength = roundlegth;
        this.playerHelth = playerHelth;
        
        this.mapname = mapname;
        this.roundtype = roundtype;
    }
    
    public RoundInfo(LocalDateTime predictedStarttime) {
        this.predictedStarttime = predictedStarttime;
        this.mapname = "default";
        this.roundtype = "default";
    } 

    public RoundInfo(LocalDateTime predictedStarttime, String mapname, String roundtype, long roundlength, int playerHelth) {
        this.predictedStarttime = predictedStarttime;
        this.mapname = mapname;
        this.roundtype = roundtype;
        this.roundlength = roundlength;
        this.playerHelth = playerHelth;
    }

    public LocalDateTime getPredictedStarttime() {
        return predictedStarttime;
    }

    public void setPredictedStarttime(LocalDateTime predictedStarttime) {
        this.predictedStarttime = predictedStarttime;
    }

    public String getMapname() {
        return mapname;
    }

    public void setMapname(String mapname) {
        this.mapname = mapname;
    }

    public String getRoundtype() {
        return roundtype;
    }

    public void setRoundtype(String roundtype) {
        this.roundtype = roundtype;
    }

    public long getRoundlength() {
        return roundlength;
    }

    public void setRoundlength(long roundlength) {
        this.roundlength = roundlength;
    }

    public int getPlayerHelth() {
        return playerHelth;
    }

    public void setPlayerHelth(int playerHelth) {
        this.playerHelth = playerHelth;
    }
    
}