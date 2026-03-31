package com.eventwise.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Comment;
import com.eventwise.ProfileType;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    public interface OnDeleteClickListener{
        void onDeleteClicked(Comment comment);
    }


    private ProfileType profileType;
    private OnDeleteClickListener onDeleteClickListener;

    private List<Comment> comments;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_comment, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        //Copied from NotificationAdapter
        holder.timestamp.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date(comment.getTimestamp() * 1000)));

        //Dont know how to feel about this as it pulls from the database each time creating a small delay when updating...
        EntrantDatabaseManager entrantDBManager = new EntrantDatabaseManager();
        entrantDBManager.getEntrantFromId(comment.getAuthorId())
                        .addOnSuccessListener( entrant -> {
                                    if (entrant == null) {
                                        holder.username.setText("Unknown User");
                                        return;
                                    } else {
                                        holder.username.setText(entrant.getName());
                                    }
                        }).addOnFailureListener(e->
                        {
                            holder.username.setText("Unknown User");
                            Log.d("CommentAdapter", "Error getting entrant: " + e.getMessage());
                        });
        holder.content.setText(comment.getText());
        holder.profile_type.setText(comment.getProfileType().name());
        if (profileType == ProfileType.ADMIN || (profileType == ProfileType.ORGANIZER && comment.getProfileType() == ProfileType.ENTRANT)){
            holder.delete_button.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClicked(comment);
                }
            });
        }
        else{
            holder.delete_button.setVisibility(View.GONE);
        }
    }

    public CommentAdapter(List<Comment> comments,  ProfileType profileType, OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
        this.comments = comments;
        this.profileType = profileType;
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp;
        TextView username;
        TextView content;

        TextView profile_type;

        ImageButton delete_button;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
            username = itemView.findViewById(R.id.comment_username);
            content = itemView.findViewById(R.id.comment_body);
            delete_button = itemView.findViewById(R.id.delete_comment_button);
            profile_type = itemView.findViewById(R.id.profile_type_string);
        }
    }

}
