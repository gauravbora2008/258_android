package com.paril.mlaclientapp.model;

import com.paril.mlaclientapp.model.GetGroupsModel;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewUnjoinedGroupsItem {
    private String group_owner_id;
    private String group_id;
    private String group_name;
    private String group_key;
    private String signature;
    private String public_key;
    private String fullname;

    public String getGroup_owner_id() {
        return group_owner_id;
    }

    public void setGroup_owner_id(String group_owner_id) {
        this.group_owner_id = group_owner_id;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public ViewUnjoinedGroupsItem(String group_owner_id, String group_id, String group_name, String group_key, String signature, String public_key, String fullname) {

        this.group_owner_id = group_owner_id;
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_key = group_key;
        this.signature = signature;
        this.public_key = public_key;
        this.fullname = fullname;
    }
}
