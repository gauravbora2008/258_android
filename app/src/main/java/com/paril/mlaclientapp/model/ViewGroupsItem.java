package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewGroupsItem {
    private String group_id;
    private String owner_id;
    private String fullname;
    private String group_name;

    public String getGroup_id() {
        return group_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getGroup_name() {
        return group_name;
    }

    public ViewGroupsItem(String group_id, String owner_id, String fullname, String group_name) {

        this.group_id = group_id;
        this.owner_id = owner_id;
        this.fullname = fullname;
        this.group_name = group_name;
    }

    public void setGroup_id(String group_id) {

        this.group_id = group_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }
}
