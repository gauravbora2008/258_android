package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/1/2020.
 */

public class SNRegisterNewUser {
    private String username;
    private String password;
    private String publicKeyStr;
    private String fullname;
    private String encryptedGroupKey;

    public SNRegisterNewUser(String username, String password, String publicKeyStr, String fullname, String encryptedGroupKey) {
        this.username = username;
        this.password = password;
        this.publicKeyStr = publicKeyStr;
        this.fullname = fullname;
        this.encryptedGroupKey = encryptedGroupKey;
    }
}
