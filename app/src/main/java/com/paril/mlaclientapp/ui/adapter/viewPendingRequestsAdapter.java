package com.paril.mlaclientapp.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.ViewPendingRequestsItem;

import java.util.ArrayList;

/**
 * Created by dell on 5/4/2020.
 */

public class viewPendingRequestsAdapter extends RecyclerView.Adapter<viewPendingRequestsAdapter.requestViewHolder> {
    private ArrayList<ViewPendingRequestsItem> mGroupList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onApproveBtnClick(int position);
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
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onApproveBtnClick(position);
                        }
                    }
                }
            });

            denyRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_item, parent, false);
        requestViewHolder gvh = new requestViewHolder(v, mListener);
        return gvh;
    }

    @Override
    public void onBindViewHolder(requestViewHolder holder, int position) {
        ViewPendingRequestsItem currentGroup = mGroupList.get(position);

//        holder.pendingRequestsTV.setText(currentGroup.getGroup_name());
//        holder.groupOwnerFullName.setText(currentGroup.getFullname());
//        holder.joinGroupBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println(v.getId());
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }
}
