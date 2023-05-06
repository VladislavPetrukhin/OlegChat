package com.oleg.olegchat;

public class User {

    private String name;
    private String email;
    private String id;
    private String photoUrl;
    private String contacts;

    public User() {
    }

    public User(String name, String email, String id, String photoUrl, String contacts) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.photoUrl = photoUrl;
        this.contacts = contacts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }
}
