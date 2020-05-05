package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewPostItem {
    private String postAuthor;
    private String postContent;
    private String postGroup;
    private String post_key;
    private String group_key;
    private String timestamp;

    public ViewPostItem(String postAuthor, String postContent, String postGroup, String postKey, String groupKey, String timestamp) {
        this.postAuthor = postAuthor;
        this.postContent = postContent;
        this.postGroup = postGroup;
        this.post_key = postKey;
        this.group_key = groupKey;
        this.timestamp = timestamp;
    }

    public String getPostKey() {
        return post_key;
    }

    public String getGroupKey() {
        return group_key;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPostAuthor() {
        return postAuthor;
    }

    public String getPostContent() {
        return postContent;
    }

    public String getPostGroup() {
        return postGroup;
    }
}
