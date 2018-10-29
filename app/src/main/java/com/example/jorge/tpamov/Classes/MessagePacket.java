package com.example.jorge.tpamov.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 31/12/2016.
 */
public class MessagePacket implements Serializable {
    static final long serialVersionUID = 1010L;
    private List<String> parameters;
    private String author;
    private String title;
    private String obs;


    public MessagePacket(String author, String title, String obs) {
        this.author = author;
        this.title = title;
        this.obs = obs;
        parameters = new ArrayList<>();
    }

    public String getAuthor(){
        return author;
    }
    public String getTitle(){
        return title;
    }
    public String getObs(){
        return obs;
    }

    @Override
    public String toString() {
        String aux = "";
        aux = "MessagePacket = [";
        for(int i = 0; i < parameters.size(); i++)
            aux += parameters.get(i).toString() + "|";
        return aux;
    }

}
