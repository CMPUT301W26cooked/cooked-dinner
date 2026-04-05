package com.eventwise;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.adapters.CommentAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/**
 * A {@link BottomSheetDialogFragment} that provides a user interface for viewing, posting,
 * and managing comments associated with a specific event.
 * <p>
 * This class handles the retrieval of comments from the database, displays them in a
 * {@link RecyclerView}, and allows users to add new comments. It also supports comment
 * deletion via a callback in the {@link CommentAdapter}. UI elements for posting comments
 * are dynamically hidden if the current user has an {@link ProfileType#ADMIN} profile.
 * </p>
 *
 * @see BottomSheetDialogFragment
 * @see CommentAdapter
 * @see EntrantDatabaseManager
 */
public class CommentBottomSheet extends BottomSheetDialogFragment {

    public interface OnEnterClickListener{
        void onEnterClicked(Comment comment);
    }

    private EditText commentEditText;
    private CommentAdapter adapter;

    private RecyclerView recyclerView;

    private String eventId;
    private ProfileType profileType;

    private ImageButton post_comment_button;
    private ImageButton dismiss_button;

    private ArrayList<Comment> comments = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.bottom_sheet_comments, container, false);

        eventId = getArguments().getString("eventId");
        profileType = ProfileType.valueOf(getArguments().getString("profileType"));



        commentEditText = view.findViewById(R.id.add_comment_edit_text);

        recyclerView = view.findViewById(R.id.event_comment_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        BottomSheetBehavior behavior = dialog.getBehavior();

        behavior.setDraggable(false); // Disable dragging
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);





        //Set up post comment button and its callback
        post_comment_button = view.findViewById(R.id.post_comment_button);
        post_comment_button.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString();
            if (!commentText.isEmpty()) {
                SessionStore store = new SessionStore(requireContext());
                String entrantId = store.getOrCreateDeviceId();


                long timestamp = System.currentTimeMillis() / 1000L;

                Comment newComment = new Comment(commentText, entrantId, timestamp, profileType);
                //Add to beginning of list
                comments.add(0,newComment);

                //Upload to Firebase
                EntrantDatabaseManager entrantDBMan = new EntrantDatabaseManager();
                entrantDBMan.addCommentToEvent(newComment, eventId)
                    .addOnSuccessListener(unused -> {
                        Log.d("Comment", "Added comment to Firebase");
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Comment", "Failed to add comment to Firebase", e);
                    });

            }
            adapter.notifyDataSetChanged();

        });
        //Dismiss button
        dismiss_button = view.findViewById(R.id.dismiss_button);
        dismiss_button.setOnClickListener(v -> {
            dismiss();
        });



        //Hide edit text when profile is admin
        if (profileType == ProfileType.ADMIN){
            commentEditText.setVisibility(View.GONE);
            post_comment_button.setVisibility(View.GONE);
        }

        //Comment adapter with callback
        EntrantDatabaseManager entrantDBMan = new EntrantDatabaseManager();
        adapter = new CommentAdapter(comments, profileType, comment -> {
            //Delete comment from list
            comments.remove(comment);
            adapter.notifyDataSetChanged();

            //Delete from Firebase
            entrantDBMan.removeCommentFromEvent(comment, eventId)
                    .addOnFailureListener(e->{
                        Log.d("Comment", "Failed to remove comment from Firebase", e);
                    })
                    .addOnSuccessListener(unused -> {
                        Log.d("Comment", "Removed comment from Firebase");
                    });
        });
        recyclerView.setAdapter(adapter);

        //Get comments from Firebase
        entrantDBMan.getCommentsFromEventId(eventId)
                .addOnSuccessListener(comments -> {
                    this.comments.clear();
                    this.comments.addAll(comments);
                    this.comments.sort((b, a) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
                    Log.d("Comment", "Got comments from Firebase");
                    Log.d("Comment", "Num of comments " + comments.size());
                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Log.d("Comment", "Failed to get comments from Firebase", e);
                });


        return view;

    }








}
