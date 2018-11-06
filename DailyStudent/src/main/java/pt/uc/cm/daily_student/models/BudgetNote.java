package pt.uc.cm.daily_student.models;

import android.support.annotation.NonNull;

public class BudgetNote {
    private int id;
    private String title;
    private String gender;
    private Double value;
    private String date;

    public BudgetNote(int id, String title, Double value) {
        this.id = id;
        this.title = title;
        this.value = value;
    }

    public BudgetNote(int id, String title, String gender, Double value, String date) {
        this.id = id;
        this.gender = gender;
        this.title = title;
        this.value = value;
        this.date = date;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getGender() {
        return this.gender;
    }

    public Double getValue() {
        return this.value;
    }

    public String getDate() {
        return this.date;
    }

    @NonNull
    @Override
    public String toString() {
        return "BudgetNote{" + "id=" + id + ", title='" + title + '\'' + ", value=" + value +
                ", date='" + date + '\'' + '}';
    }
}
