package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/4/2020.
 */

public class AddRequestsItem {
    private String group_id;
    private String group_name;
    private String group_key;
    private String member_id;
    private String member_public_key;
    private String member_fullname;

    public AddRequestsItem(String group_id, String group_name, String group_key, String member_id, String member_public_key, String member_fullname) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_key = group_key;
        this.member_id = member_id;
        this.member_public_key = member_public_key;
        this.member_fullname = member_fullname;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_key() {
        return group_key;
    }

    public void setGroup_key(String group_key) {
        this.group_key = group_key;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getMember_public_key() {
        return member_public_key;
    }

    public void setMember_public_key(String member_public_key) {
        this.member_public_key = member_public_key;
    }

    public String getMember_fullname() {
        return member_fullname;
    }

    public void setMember_fullname(String member_fullname) {
        this.member_fullname = member_fullname;
    }
}
