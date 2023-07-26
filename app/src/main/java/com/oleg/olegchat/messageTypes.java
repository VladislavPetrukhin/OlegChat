package com.oleg.olegchat;


import java.util.ArrayList;

public class messageTypes {
    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    public void addKeyValue(String key, String value) {
        keys.add(key);
        values.add(value);
    }

    public String getValueByKey(String key) {
        int index = keys.indexOf(key);
        return (index != -1) ? values.get(index) : null;
    }

    public boolean containsKey(String key) {
        return keys.contains(key);
    }
}

