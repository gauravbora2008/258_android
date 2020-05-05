package com.paril.mlaclientapp.model;

import com.paril.mlaclientapp.model.GetGroupsModel;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewUnjoinedGroupsItem {
    private String group_id;
    private String group_owner_id;
    private String fullname;
    private String group_name;
    private String group_key;

    public String getGroup_id() {
        return group_id;
    }

    public String getOwner_id() {
        return group_owner_id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getGroup_key() {
        return group_key;
    }

    public ViewUnjoinedGroupsItem(String group_id, String group_owner_id, String fullname, String group_name, String group_key) {

        this.group_id = group_id;
        this.group_owner_id = group_owner_id;
        this.fullname = fullname;
        this.group_name = group_name;
        this.group_key = group_key;
    }

    public void setGroup_id(String group_id) {

        this.group_id = group_id;
    }

    public void setOwner_id(String owner_id) {
        this.group_owner_id = owner_id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public void setGroup_owner_id(String group_owner_id) {
        this.group_name = group_owner_id;
    }
}
