package com.oleg.olegchat;

public class AwesomeMessage {

    private String messageType;
    private String text;
    private String name;
    private String url;
    private String sender;
    private String recipient;
    private String message_id;
    private boolean isMine;
    public AwesomeMessage(){

    }

    public AwesomeMessage(String messageType, String text, String name, String url, String sender,
                          String recipient, String message_id, boolean isMine) {
        this.messageType = messageType;
        this.text = text;
        this.name = name;
        this.url = url;
        this.sender = sender;
        this.recipient = recipient;
        this.message_id = message_id;
        this.isMine = isMine;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
