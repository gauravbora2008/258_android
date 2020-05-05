package com.paril.mlaclientapp.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.ViewUnjoinedGroupsItem;

import java.util.ArrayList;

/**
 * Created by dell on 5/4/2020.
 */

public class viewGroupsAdapter extends RecyclerView.Adapter<viewGroupsAdapter.groupsViewHolder> {
    private ArrayList<ViewUnjoinedGroupsItem> mGroupList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onJoinBtnClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class groupsViewHolder extends RecyclerView.ViewHolder {

        public TextView groupName;
        public TextView groupOwnerFullName;
        public Button joinGroupBtn;

        public groupsViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            groupName = (TextView) itemView.findViewById(R.id.view_groups_grpName);
            groupOwnerFullName = (TextView) itemView.findViewById(R.id.view_group_owner_full_name);
            joinGroupBtn = (Button) itemView.findViewById(R.id.view_group_join_btn);

            joinGroupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onJoinBtnClick(position);
                        }
                    }
                }
            });


        }
    }

    public viewGroupsAdapter(ArrayList<ViewUnjoinedGroupsItem> groupsList) {
        mGroupList = groupsList;
    }

    @Override
    public groupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_item, parent, false);
        groupsViewHolder gvh = new groupsViewHolder(v, mListener);
        return gvh;
    }

    @Override
    public void onBindViewHolder(groupsViewHolder holder, int position) {
        ViewUnjoinedGroupsItem currentGroup = mGroupList.get(position);

        holder.groupName.setText(currentGroup.getGroup_name());
        holder.groupOwnerFullName.setText(currentGroup.getFullname());
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
