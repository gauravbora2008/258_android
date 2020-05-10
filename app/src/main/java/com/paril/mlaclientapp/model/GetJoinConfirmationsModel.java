package com.paril.mlaclientapp.model;

/**
 * Created by dell on 5/3/2020.
 */

public class GetJoinConfirmationsModel {
    public String group_owner_id;
    public String requester_id;
    public String encryptedGroupKey;
    public String group_id;
    public String signature;
    public String public_key;

    public GetJoinConfirmationsModel(String group_owner_id, String requester_id, String encryptedGroupKey, String group_id, String signature, String public_key) {
        this.group_owner_id = group_owner_id;
        this.requester_id = requester_id;
        this.encryptedGroupKey = encryptedGroupKey;
        this.group_id = group_id;
        this.signature = signature;
        this.public_key = public_key;
    }
}
