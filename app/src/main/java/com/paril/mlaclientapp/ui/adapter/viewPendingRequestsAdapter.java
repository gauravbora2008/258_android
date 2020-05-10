package com.paril.mlaclientapp.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.ViewPendingRequestsItem;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by dell on 5/4/2020.
 */

public class viewPendingRequestsAdapter extends RecyclerView.Adapter<viewPendingRequestsAdapter.requestViewHolder> {
    private ArrayList<ViewPendingRequestsItem> mGroupList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onApproveBtnClick(int position) throws Exception;

        void onDenyBtnClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class requestViewHolder extends RecyclerView.ViewHolder {

        public TextView pendingRequestsTV;
        public Button approveRequestBtn;
        public Button denyRequestBtn;

        public requestViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            pendingRequestsTV = (TextView) itemView.findViewById(R.id.pending_requests_text);
            approveRequestBtn = (Button) itemView.findViewById(R.id.approve_request_btn);
            denyRequestBtn = (Button) itemView.findViewById(R.id.deny_request_btn);

            approveRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                listener.onApproveBtnClick(position);
                            } catch (IOException | InvalidKeySpecException | IllegalBlockSizeException | NoSuchProviderException | KeyStoreException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | UnrecoverableEntryException | InvalidKeyException | NoSuchAlgorithmException | CertificateException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            denyRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDenyBtnClick(position);
                        }
                    }
                }
            });


        }
    }

    public viewPendingRequestsAdapter(ArrayList<ViewPendingRequestsItem> groupsList) {
        mGroupList = groupsList;
    }

    @Override
    public requestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_request_item, parent, false);
        requestViewHolder gvh = new requestViewHolder(v, mListener);
        return gvh;
    }

    @Override
    public void onBindViewHolder(requestViewHolder holder, int position) {

        ViewPendingRequestsItem currentGroup = mGroupList.get(position);
        holder.pendingRequestsTV.setText(
                currentGroup.getRequester_fullname()
                + " (Requesters ID: "
                + currentGroup.getRequester_id()
                + ") wants to join "
                + currentGroup.getGroup_name()
                + " (Group ID: "
                + currentGroup.getGroup_id()
                + ")");

    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }
}
