package com.example.jorge.tpamov.Classes;

/**
 * Created by Jorge on 19/12/2016.
 */

public class classBudgetNote {
    int id;
    String title;
    String gender;
    Double value;
    String date;

    public classBudgetNote(int id, String title, Double value){
        this.id = id;
        this.title = title;
        this.value = value;
    }

    public classBudgetNote(int id, String title,String gender, Double value, String date){
        this.id = id;
        this.gender = gender;
        this.title = title;
        this.value = value;
        this.date = date;
    }

    public int getId(){
        return this.id;
    }
    public String getTitle(){
        return this.title;
    }
    public String getGender(){
        return this.gender;
    }
    public Double getValue(){
        return this.value;
    }
    public String getDate(){
        return this.date;
    }

    @Override
    public String toString() {
        return "classBudgetNote{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", date='" + date + '\'' +
                '}';
    }
}
