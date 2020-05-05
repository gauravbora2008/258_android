package com.paril.mlaclientapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dell on 5/3/2020.
 */

public class NewPostModel {
    public String author_id;
    public String group_id;
    public String post_key;
    public String post_data;
    public String timestamp;

    public NewPostModel(String post_key, String group_id, String post_data, String author_id) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        this.post_key = post_key;
        this.group_id = group_id;
        this.post_data = post_data;
        this.author_id = author_id;
        this.timestamp = formatter.format(date);
    }

    public String getPost_key() {
        return post_key;
    }

    public void setPost_key(String post_key) {
        this.post_key = post_key;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getPost_data() {
        return post_data;
    }

    public void setPost_data(String post_data) {
        this.post_data = post_data;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
