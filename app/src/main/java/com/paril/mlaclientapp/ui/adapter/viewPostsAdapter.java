package com.paril.mlaclientapp.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.ViewPostItem;

import java.util.ArrayList;

/**
 * Created by dell on 5/4/2020.
 */

public class viewPostsAdapter extends RecyclerView.Adapter<viewPostsAdapter.postsViewHolder> {
    ArrayList<ViewPostItem> mPostList;

    public static class postsViewHolder extends RecyclerView.ViewHolder {

        public TextView postAuthor;
        public TextView postContent;
        public TextView postGroup;

        public postsViewHolder(View itemView) {
            super(itemView);
            postAuthor = (TextView) itemView.findViewById(R.id.view_post_author_TV);
            postContent = (TextView) itemView.findViewById(R.id.view_post_content_TV);
            postGroup = (TextView) itemView.findViewById(R.id.view_post_group_name_TV);
        }
    }

    public viewPostsAdapter(ArrayList<ViewPostItem> postsList) {
        mPostList = postsList;
    }

    @Override
    public postsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post_item, parent, false);
        postsViewHolder pvh = new postsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(postsViewHolder holder, int position) {
        ViewPostItem currentPost = mPostList.get(position);

        holder.postAuthor.setText(currentPost.getPostAuthor());
        holder.postContent.setText(currentPost.getPostContent());
        holder.postGroup.setText(currentPost.getPostGroup());
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }
}
