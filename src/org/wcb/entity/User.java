package org.wcb.entity;

/**
 * Created by wybe on 7/1/16.
 */
public class User {
    private String userEmail;
    private String password;

    public User() {}

    public User(String userEmail, String password) {
        this.userEmail = userEmail;
        this.password = password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
