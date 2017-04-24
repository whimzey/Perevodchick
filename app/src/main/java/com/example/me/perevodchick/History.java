package com.example.me.perevodchick;

import io.realm.RealmObject;

//объект истории в базе
public class History extends RealmObject {

    private String name;
    private String value;
    private String lang;

    public History() {
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