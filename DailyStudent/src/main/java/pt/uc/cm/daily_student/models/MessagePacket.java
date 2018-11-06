package pt.uc.cm.daily_student.models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessagePacket implements Serializable {
    private static final long serialVersionUID = 1010L;
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

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getObs() {
        return obs;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder aux;
        aux = new StringBuilder("MessagePacket = [");
        for (int i = 0; i < parameters.size(); i++)
            aux.append(parameters.get(i)).append("|");
        return aux.toString();
    }
}
