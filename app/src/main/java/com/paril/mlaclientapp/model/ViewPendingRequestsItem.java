package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewPendingRequestsItem {
    private String user_id;
    private String public_key;
    private String group_id;
    private String group_key;
    private String group_owner_id;
    private String group_name;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_key() {
        return group_key;
    }

    public void setGroup_key(String group_key) {
        this.group_key = group_key;
    }

    public String getGroup_owner_id() {
        return group_owner_id;
    }

    public void setGroup_owner_id(String group_owner_id) {
        this.group_owner_id = group_owner_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public ViewPendingRequestsItem(String user_id, String public_key, String group_id, String group_key, String group_owner_id, String group_name) {

        this.user_id = user_id;
        this.public_key = public_key;
        this.group_id = group_id;
        this.group_key = group_key;
        this.group_owner_id = group_owner_id;
        this.group_name = group_name;
    }
}
