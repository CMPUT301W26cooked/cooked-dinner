package com.eventwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Comment;
import com.eventwise.R;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    private final List<Comment> comments;

    public CommentListAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_comment, parent, false);
        return new ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.username.setText(comment.getAuthorId());
//        holder.timestamp.setText(Integer.toString(comment.getTimestamp()));
        holder.comment.setText(comment.getText());

        }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    /**
     * Holds one comment row view.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView timestamp;
        TextView comment;

        ViewHolder(@NonNull View itemView){
            super(itemView);
            username = itemView.findViewById(R.id.comment_username);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
            comment = itemView.findViewById(R.id.comment_body);
        }
    }

}
