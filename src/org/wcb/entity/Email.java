package org.wcb.entity;

/**
 * Created by wybe on 7/1/16.
 */
public class Email {
    private String sender;
    private String receiver;
    private String subject;
    private String body;
    private String date;

    public Email() {}

    public Email(String sender, String receiver, String subject, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "From:" + sender +
                "\nTo:" + receiver +
                "\nDate:" + date +
                "\nSubject:" + subject +
                "\nBody:\n\t" + body;
    }
}