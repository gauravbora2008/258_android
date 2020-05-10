package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/4/2020.
 */

public class ViewPendingRequestsItem {
    private String requester_id;
    private String requester_fullname;
    private String requesters_pub_key;
    private String group_id;
    private String group_key;
    private String group_owner_id;
    private String group_name;
    private String signature;
    private String join_request_hash;
    private String group_owners_pub_key;

    public ViewPendingRequestsItem(String requester_id, String requester_fullname, String requesters_pub_key, String group_id, String group_key, String group_owner_id, String group_name, String signature, String join_request_hash, String group_owners_pub_key) {
        this.requester_id = requester_id;
        this.requester_fullname = requester_fullname;
        this.requesters_pub_key = requesters_pub_key;
        this.group_id = group_id;
        this.group_key = group_key;
        this.group_owner_id = group_owner_id;
        this.group_name = group_name;
        this.signature = signature;
        this.join_request_hash = join_request_hash;
        this.group_owners_pub_key = group_owners_pub_key;
    }

    public String getRequester_id() {
        return requester_id;
    }

    public void setRequester_id(String requester_id) {
        this.requester_id = requester_id;
    }

    public String getRequester_fullname() {
        return requester_fullname;
    }

    public void setRequester_fullname(String requester_fullname) {
        this.requester_fullname = requester_fullname;
    }

    public String getRequesters_pub_key() {
        return requesters_pub_key;
    }

    public void setRequesters_pub_key(String requesters_pub_key) {
        this.requesters_pub_key = requesters_pub_key;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getJoin_request_hash() {
        return join_request_hash;
    }

    public void setJoin_request_hash(String join_request_hash) {
        this.join_request_hash = join_request_hash;
    }

    public String getGroup_owners_pub_key() {
        return group_owners_pub_key;
    }

    public void setGroup_owners_pub_key(String group_owners_pub_key) {
        this.group_owners_pub_key = group_owners_pub_key;
    }
}
