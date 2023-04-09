package com.example.catfisharea.bottomsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentCommentBottomSheetBinding;
import com.example.catfisharea.adapter.CommentAdapter;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Comment;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment implements UserListener {

    private FragmentCommentBottomSheetBinding mBinding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Task taskComment;
    private List<Comment> comments;
    private List<User> users;
    private CommentAdapter commentAdapter;
    private UsersAdapter usersAdapter;
    private TextView textMessage;

    public static CommentBottomSheetFragment newInstance(Task task) {
        CommentBottomSheetFragment commentBottomSheetFragment = new CommentBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_COLLECTION_TASK, task);
        commentBottomSheetFragment.setArguments(bundle);
        return commentBottomSheetFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentCommentBottomSheetBinding.inflate(inflater, container, false);
        init();
        setListeners();
        readComments();
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.ChatBottomSheet);
        @SuppressLint("InflateParams") View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.fragment_comment_bottom_sheet, null);
        bottomSheetDialog.setContentView(viewDialog);
        return bottomSheetDialog;
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        preferenceManager = new PreferenceManager(requireContext());
        database = FirebaseFirestore.getInstance();
        Bundle bundleReceive = getArguments();
        if (bundleReceive != null) {
            taskComment = (Task) bundleReceive.get(Constants.KEY_COLLECTION_TASK);
        }

        comments = new ArrayList<>();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        commentAdapter = new CommentAdapter(comments);
        mBinding.commentRecyclerView.setAdapter(commentAdapter);

        mBinding.textTitle.setText(taskComment.title);
        mBinding.textContent.setText(taskComment.taskContent);
        mBinding.textDate.setText(taskComment.dayOfStart + " - " + taskComment.dayOfEnd);
        if (taskComment.status.equals("uncompleted") && !taskComment.receiversCompleted.contains(preferenceManager.getString(Constants.KEY_USER_ID))) {
            mBinding.textStatus.setText("Chưa hoàn thành");
            mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
            mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
            setDrawableTint(Color.parseColor("#ed444f"));
        } else if (taskComment.status.equals("completed") || taskComment.receiversCompleted.contains(preferenceManager.getString(Constants.KEY_USER_ID))) {
            mBinding.textStatus.setText("Hoàn thành");
            mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
            mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
            setDrawableTint(Color.parseColor("#51b155"));
        }

        if (comments.size() == 0) {
            mBinding.notComment.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        mBinding.inputeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setBtnVisible(!mBinding.inputeMessage.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBinding.layoutSend.setOnClickListener(v -> sendComment());

        mBinding.textCompletedTask.setOnClickListener(view -> openShowUserCompletedDialog());

        mBinding.textUnCompletedTask.setOnClickListener(view -> openShowUserUnCompletedDialog());
    }

    private void readComments() {
        database.collection(Constants.KEY_COLLECTION_COMMENT_TASK)
                .whereEqualTo(Constants.KEY_TASK_COMMENT_ID, taskComment.id)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Comment comment = new Comment();
                    comment.userCommentId = documentChange.getDocument().getString(Constants.KEY_USER_COMMENT_ID);
                    comment.userCommentName = documentChange.getDocument().getString(Constants.KEY_USER_COMMENT_NAME);
                    comment.userCommentImage = documentChange.getDocument().getString(Constants.KEY_USER_COMMENT_IMAGE);
                    comment.userCommentPosition = documentChange.getDocument().getString(Constants.KEY_USER_COMMENT_POSITION);
                    comment.taskId = documentChange.getDocument().getString(Constants.KEY_TASK_COMMENT_ID);
                    comment.content = documentChange.getDocument().getString(Constants.KEY_COMMENT_CONTENT);
                    comment.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_COMMENT_TIMESTAMP));
                    comment.dataObject = documentChange.getDocument().getDate(Constants.KEY_COMMENT_TIMESTAMP);
                    comments.add(comment);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(comments, Comparator.comparing(obj -> obj.dataObject));
            }
            if (comments.size() == 0) {
                mBinding.notComment.setVisibility(View.VISIBLE);
                commentAdapter.notifyDataSetChanged();
            } else {
                mBinding.notComment.setVisibility(View.GONE);
                commentAdapter.notifyItemRangeInserted(comments.size(), comments.size());
                mBinding.commentRecyclerView.smoothScrollToPosition(comments.size() - 1);
            }

        }
    };

    private void sendComment() {
        HashMap<String, Object> comment = new HashMap<>();
        comment.put(Constants.KEY_USER_COMMENT_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        comment.put(Constants.KEY_USER_COMMENT_NAME, preferenceManager.getString(Constants.KEY_NAME));
        comment.put(Constants.KEY_USER_COMMENT_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        comment.put(Constants.KEY_USER_COMMENT_POSITION, preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT));
        comment.put(Constants.KEY_TASK_COMMENT_ID, taskComment.id);
        comment.put(Constants.KEY_COMMENT_CONTENT, mBinding.inputeMessage.getText().toString());
        comment.put(Constants.KEY_COMMENT_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_COMMENT_TASK).add(comment);

        try {


            JSONObject data = new JSONObject();
            data.put(Constants.KEY_USER_COMMENT_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_USER_COMMENT_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.KEY_USER_COMMENT_IMAGE, preferenceManager.getString(Constants.KEY_USER_COMMENT_IMAGE));
            data.put(Constants.KEY_USER_COMMENT_POSITION, preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT));
            data.put(Constants.KEY_TASK_COMMENT_ID, taskComment.id);
            data.put(Constants.KEY_COMMENT_CONTENT, mBinding.inputeMessage.getText().toString());
            data.put(Constants.KEY_COMMENT_TIMESTAMP, new Date());
            data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mBinding.inputeMessage.setText(null);
    }

    private void openShowUserUnCompletedDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_show_user_uncompleted);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        textMessage = dialog.findViewById(R.id.textMessage);
        RecyclerView uncompletedUserRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        uncompletedUserRecyclerView.setAdapter(usersAdapter);
        getUnCompletedUser();
        if (users.size() == 0) {
            textMessage.setVisibility(View.VISIBLE);
        } else textMessage.setVisibility(View.GONE);
        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void openShowUserCompletedDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_show_user_completed);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        textMessage = dialog.findViewById(R.id.textMessage);
        RecyclerView completedUserRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        completedUserRecyclerView.setAdapter(usersAdapter);
        getCompletedUser();
        if (users.size() == 0) {
            textMessage.setVisibility(View.VISIBLE);
        } else textMessage.setVisibility(View.GONE);
        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUnCompletedUser() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_TASK)
                .document(taskComment.id)
                .get()
                .addOnCompleteListener(task -> {

                    DocumentSnapshot documentSnapshot = task.getResult();

                    List<String> completedUser = (List<String>) documentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                    List<String> receiverID = (List<String>) documentSnapshot.get(Constants.KEY_RECEIVER_ID);
                    assert receiverID != null;
                    for (String id : receiverID) {
                        assert completedUser != null;
                        if (completedUser.contains(id))
                            continue;
                        else {
                            database.collection(Constants.KEY_COLLECTION_USER)
                                    .document(id)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        DocumentSnapshot documentSnapshot1 = task1.getResult();
                                        User user = new User();
                                        user.name = documentSnapshot1.getString(Constants.KEY_NAME);
                                        user.phone = documentSnapshot1.getString(Constants.KEY_PHONE);
                                        user.position = documentSnapshot1.getString(Constants.KEY_TYPE_ACCOUNT);
                                        user.image = documentSnapshot1.getString(Constants.KEY_IMAGE);
                                        user.token = documentSnapshot1.getString(Constants.KEY_FCM_TOKEN);
                                        user.id = documentSnapshot1.getId();
                                        users.add(user);
                                        usersAdapter.notifyDataSetChanged();
                                        if (users.size() == 0) {
                                            textMessage.setVisibility(View.VISIBLE);
                                        } else textMessage.setVisibility(View.GONE);
                                    });
                        }
                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getCompletedUser() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_TASK)
                .document(taskComment.id)
                .get()
                .addOnCompleteListener(task -> {

                    DocumentSnapshot documentSnapshot = task.getResult();

                    List<String> completedUser = (List<String>) documentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);

                    for (int i = 0; i < Objects.requireNonNull(completedUser).size(); i++) {

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .document(completedUser.get(i))
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    DocumentSnapshot documentSnapshot1 = task1.getResult();
                                    User user = new User();
                                    user.name = documentSnapshot1.getString(Constants.KEY_NAME);
                                    user.phone = documentSnapshot1.getString(Constants.KEY_PHONE);
                                    user.position = documentSnapshot1.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.image = documentSnapshot1.getString(Constants.KEY_IMAGE);
                                    user.token = documentSnapshot1.getString(Constants.KEY_FCM_TOKEN);
                                    user.id = documentSnapshot1.getId();
                                    users.add(user);
                                    usersAdapter.notifyDataSetChanged();
                                    if (users.size() == 0) {
                                        textMessage.setVisibility(View.VISIBLE);
                                    } else textMessage.setVisibility(View.GONE);
                                });

                    }

                });
    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void setDrawableTint(int color) {
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = requireContext().getResources().getDrawable(R.drawable.ic_access_time);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        mBinding.textStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    private void setBtnVisible(boolean visible) {
        if (visible) {
            mBinding.layoutImage.setVisibility(View.INVISIBLE);
            mBinding.layoutAttact.setVisibility(View.INVISIBLE);
            mBinding.layoutSend.setVisibility(View.VISIBLE);
        } else {
            mBinding.layoutImage.setVisibility(View.VISIBLE);
            mBinding.layoutAttact.setVisibility(View.VISIBLE);
            mBinding.layoutSend.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicker(User user) {

    }
}