package com.paril.mlaclientapp.model;

/**
 * Created by dell on 4/30/2020.
 */

public class SNUser {

    private String username;
    private String password;
    private String fullname;
    private String publicKey;

    public SNUser(String username, String password, String fullname, String publicKey) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
