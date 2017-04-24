package com.example.me.perevodchick;
import io.realm.RealmObject;

//объект избранного в базе
 public class Favorite extends RealmObject {

    private String name;
    private String value;
    private String lang;

    public Favorite() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

     String getValue() {
        return value;
    }

     void setValue(String value) {
        this.value = value;
    }

    String getLang() {
        return lang;
    }

    void setLang(String lang) {
        this.lang = lang;
    }



}