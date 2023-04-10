package com.example.catfisharea.bottomsheet;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentCommentBottomSheetBinding;
import com.example.catfisharea.adapter.CommentAdapter;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.MultipleListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment implements UserListener, MultipleListener {

    private FragmentCommentBottomSheetBinding mBinding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Task taskComment;
    private List<Comment> comments;
    private List<User> users, selectedUser;
    private CommentAdapter commentAdapter;
    private UsersAdapter usersAdapter;
    private MultipleUserSelectionAdapter multipleUserSelectionAdapter;
    private TextView textMessage;
    private TextView textDayOfStart, textDayOfEnd;
    private Calendar myCal;

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

        //Calendar
        myCal = Calendar.getInstance();

        comments = new ArrayList<>();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        commentAdapter = new CommentAdapter(comments);
        selectedUser = new ArrayList<>();
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

        if (taskComment.typeOfTask == null){
            mBinding.textCompletedTask.setVisibility(View.VISIBLE);
            mBinding.textUnCompletedTask.setVisibility(View.VISIBLE);
            mBinding.textAssignmentFixedTask.setVisibility(View.GONE);
            mBinding.textDate.setVisibility(View.VISIBLE);
            mBinding.textLeave.setVisibility(View.VISIBLE);
            mBinding.imageLeave.setVisibility(View.VISIBLE);
        } else {
            mBinding.textCompletedTask.setVisibility(View.GONE);
            mBinding.textUnCompletedTask.setVisibility(View.GONE);
            mBinding.textAssignmentFixedTask.setVisibility(View.VISIBLE);
            mBinding.textDate.setVisibility(View.GONE);
            mBinding.textLeave.setVisibility(View.GONE);
            mBinding.imageLeave.setVisibility(View.GONE);
        }

        if (comments.size() == 0) {
            mBinding.notComment.setVisibility(View.VISIBLE);
        }

        if (taskComment.status.equals(Constants.KEY_UNCOMPLETED)){
            if (taskComment.creatorID.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                mBinding.layoutEditTask.setVisibility(View.VISIBLE);
            }
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

        mBinding.textCompletedTask.setOnClickListener(view -> showUserCompletedDialog());

        mBinding.textUnCompletedTask.setOnClickListener(view -> showUserUnCompletedDialog());

        mBinding.textAssignmentFixedTask.setOnClickListener(view -> showUserAssignFixedTask());

        mBinding.imageDelete.setOnClickListener(view -> openConfirmDeleteTaskDialog());

        mBinding.imageEdit.setOnClickListener(view -> {
            if (taskComment.typeOfTask == null){
                openEditionForMomentarilyTaskDialog();
            } else {
                openEditionForFixedTaskDialog();
            }
        });
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

    private void showUserUnCompletedDialog() {
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

    private void showUserAssignFixedTask() {
        Dialog dialog = openDialog(R.layout.layout_dialog_show_user_assign_fixed_task);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        RecyclerView assignedUserRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        assignedUserRecyclerView.setAdapter(usersAdapter);
        getAssignmentUserForFixedTask();
        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void showUserCompletedDialog() {
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

    @SuppressLint("SetTextI18n")
    private void openConfirmDeleteTaskDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_task);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(view -> {
            deleteTask();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openEditionForMomentarilyTaskDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_edit_momentarily_task);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        TextView textSelectUser;
        EditText edtTitle, edtContent;
        RecyclerView userRecyclerView;
        ImageView imageSelectUser;

        textSelectUser = dialog.findViewById(R.id.textSelectUser);
        textDayOfStart = dialog.findViewById(R.id.textDayOfStart);
        textDayOfEnd = dialog.findViewById(R.id.textDayOfEnd);
        edtTitle = dialog.findViewById(R.id.edtTitle);
        edtContent = dialog.findViewById(R.id.edtContent);
        userRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        imageSelectUser = dialog.findViewById(R.id.imageSelectUser);

        //Adapter
        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        userRecyclerView.setAdapter(multipleUserSelectionAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            textSelectUser.setText("Chọn trưởng khu");
        else textSelectUser.setText("Chọn công nhân");

        textDayOfStart.setText(taskComment.dayOfStart);
        textDayOfEnd.setText(taskComment.dayOfEnd);
        edtTitle.setText(taskComment.title);
        edtContent.setText(taskComment.taskContent);

        if (Constants.KEY_REGIONAL_CHIEF.equals(preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT))){
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR)
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            users.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                if (taskComment.receiverID.contains(user.id)){
                                    user.isSelected = true;
                                }
                                users.add(user);
                                multipleUserSelectionAdapter.notifyDataSetChanged();
                            }

                        }

                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            users.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                if (taskComment.receiverID.contains(user.id)){
                                    user.isSelected = true;
                                }
                                users.add(user);
                                usersAdapter.notifyDataSetChanged();

                            }

                        }

                    });
        }

        // Hiển thị hoặc ẩn danh sách trưởng khu
        textSelectUser.setOnClickListener(view -> {
            if (userRecyclerView.getVisibility() == View.VISIBLE){
                userRecyclerView.setVisibility(View.GONE);
                imageSelectUser.setImageResource(R.drawable.ic_up);
            } else {
                userRecyclerView.setVisibility(View.VISIBLE);
                imageSelectUser.setImageResource(R.drawable.ic_down);
            }
        });

        // Hiển thị hoặc ẩn danh sách trưởng khu
        imageSelectUser.setOnClickListener(view -> {
            if (userRecyclerView.getVisibility() == View.VISIBLE){
                userRecyclerView.setVisibility(View.GONE);
                imageSelectUser.setImageResource(R.drawable.ic_up);
            } else {
                userRecyclerView.setVisibility(View.VISIBLE);
                imageSelectUser.setImageResource(R.drawable.ic_down);
            }
        });

        // Chọn ngày bắt đầu
        textDayOfStart.setOnClickListener(view -> openDatePicker(Constants.KEY_DAY_START_TASK));

        // Chọn ngày kết thúc
        textDayOfEnd.setOnClickListener(view -> openDatePicker(Constants.KEY_DAY_END_TASK));

        btnSave.setOnClickListener(view -> {
            selectedUser.clear();
            selectedUser = multipleUserSelectionAdapter.getSelectedUser();
            if(selectedUser.size() == 0){
                showToast("Chọn ít nhất một trưởng vùng!" + selectedUser.size());
            } else if(textDayOfStart.getText().toString().trim().isEmpty()){
                showToast("Bạn chưa chọn ngày bắt đầu!");
            } else if(textDayOfEnd.getText().toString().trim().isEmpty()){
                showToast("Bạn chưa chọn ngày kết thúc");
            } else if(edtContent.getText().toString().trim().isEmpty()){
                showToast("Bạn chưa nhập nội dung công việc!");
            } else if(edtTitle.getText().toString().trim().isEmpty()){
                showToast("Bạn chưa nhập tiêu đề công việc!");
            } else {

                List<String> receiverIds = new ArrayList<>();
                List<String> receiverNames = new ArrayList<>();
                List<String> receiverImages = new ArrayList<>();
                List<String> receiverPhones = new ArrayList<>();
                List<String> receiverCompleted = new ArrayList<>();

                // Duyệt qua các trưởng vùng mà người dùng chọn để lấy tên và id
                for (User user : selectedUser) {
                    receiverIds.add(user.id);
                    receiverNames.add(user.name);
                    receiverImages.add(user.image);
                    receiverPhones.add(user.phone);
                }

                // Tạo các trường dữ liệu cho bảng task
                HashMap<String, Object> task = new HashMap<>();
                task.put(Constants.KEY_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                task.put(Constants.KEY_CREATOR_NAME, preferenceManager.getString(Constants.KEY_NAME));
                task.put(Constants.KEY_CREATOR_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
                task.put(Constants.KEY_CREATOR_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                task.put(Constants.KEY_RECEIVER_ID, receiverIds);
                task.put(Constants.KEY_RECEIVER_NAME, receiverNames);
                task.put(Constants.KEY_RECEIVER_IMAGE, receiverImages);
                task.put(Constants.KEY_RECEIVER_PHONE, receiverPhones);
                task.put(Constants.KEY_AMOUNT_RECEIVERS, selectedUser.size() + "");
                task.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
                task.put(Constants.KEY_DAY_START_TASK, textDayOfStart.getText().toString());
                task.put(Constants.KEY_DAY_END_TASK, textDayOfEnd.getText().toString());
                task.put(Constants.KEY_TASK_CONTENT, edtContent.getText().toString());
                task.put(Constants.KEY_TASK_TITLE, edtTitle.getText().toString());
                task.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);

                // Tạo nhiệm vụ lên cơ sở dữ liệu
                database.collection(Constants.KEY_COLLECTION_TASK)
                        .document(taskComment.id)
                        .update(task)
                        .addOnCompleteListener(task1 -> {

                            // Cập nhật lại tình trạng ban đầu cho các ô dữ liệu
                            textDayOfStart.setText("Chọn ngày bắt đầu");
                            textDayOfEnd.setText("Chọn ngày kết thúc");
                            edtContent.setText("");
                            edtTitle.setText("");
                            multipleUserSelectionAdapter.setUserUnSelected(users);

                            // Hiển hoặc ẩn danh sách trưởng khu
                            if (userRecyclerView.getVisibility() == View.VISIBLE){
                                userRecyclerView.setVisibility(View.GONE);
                                imageSelectUser.setImageResource(R.drawable.ic_up);
                            } else {
                                userRecyclerView.setVisibility(View.VISIBLE);
                                imageSelectUser.setImageResource(R.drawable.ic_down);
                            }


                        }).addOnFailureListener(e -> showToast(e.getMessage()));
                showToast("Đã cập nhật nhiệm vụ thành công!");
                dialog.dismiss();
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void openEditionForFixedTaskDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_edit_fixed_task);
        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        TextView textSelectUser;
        ImageView imageSelectUser;
        Spinner spinnerTypeOfTask;
        EditText edtContent;
        RecyclerView userRecyclerView;

        textSelectUser = dialog.findViewById(R.id.textSelectUser);
        imageSelectUser = dialog.findViewById(R.id.imageSelectUser);
        spinnerTypeOfTask = dialog.findViewById(R.id.spinnerTypeOfTask);
        userRecyclerView = dialog.findViewById(R.id.userRecyclerView);

        //Adapter
        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        userRecyclerView.setAdapter(multipleUserSelectionAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            textSelectUser.setText("Chọn trưởng khu");
        else textSelectUser.setText("Chọn công nhân");

        List<String> typeOfFixedTask = new ArrayList<>();

        typeOfFixedTask.add(Constants.KEY_FIXED_TASK_FEED_FISH);
        typeOfFixedTask.add(Constants.KEY_FIXED_TASK_MEASURE_WATER);

        //ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_layout_spinner, typeOfFixedTask);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeOfTask.setAdapter(adapter);

        if (taskComment.typeOfTask != null){
            int index = typeOfFixedTask.indexOf(taskComment.title);
            spinnerTypeOfTask.setSelection(index);
        }

        edtContent = dialog.findViewById(R.id.edtContent);
        edtContent.setText(taskComment.taskContent);

        if (Constants.KEY_REGIONAL_CHIEF.equals(preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT))){
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR)
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            users.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                if (taskComment.receiverID.contains(user.id)){
                                    user.isSelected = true;
                                }
                                users.add(user);
                                multipleUserSelectionAdapter.notifyDataSetChanged();
                            }

                        }

                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            users.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                if (taskComment.receiverID.contains(user.id)){
                                    user.isSelected = true;
                                }
                                users.add(user);
                                usersAdapter.notifyDataSetChanged();

                            }

                        }

                    });
        }

        // Hiển thị hoặc ẩn danh sách trưởng khu
        textSelectUser.setOnClickListener(view -> {
            if (userRecyclerView.getVisibility() == View.VISIBLE){
                userRecyclerView.setVisibility(View.GONE);
                imageSelectUser.setImageResource(R.drawable.ic_up);
            } else {
                userRecyclerView.setVisibility(View.VISIBLE);
                imageSelectUser.setImageResource(R.drawable.ic_down);
            }
        });

        // Hiển thị hoặc ẩn danh sách trưởng khu
        imageSelectUser.setOnClickListener(view -> {
            if (userRecyclerView.getVisibility() == View.VISIBLE){
                userRecyclerView.setVisibility(View.GONE);
                imageSelectUser.setImageResource(R.drawable.ic_up);
            } else {
                userRecyclerView.setVisibility(View.VISIBLE);
                imageSelectUser.setImageResource(R.drawable.ic_down);
            }
        });

        btnSave.setOnClickListener(view -> {
            selectedUser.clear();
            selectedUser = multipleUserSelectionAdapter.getSelectedUser();
            if(selectedUser.size() == 0){
                showToast("Chọn ít nhất một trưởng vùng!");
            } else if(edtContent.getText().toString().trim().isEmpty()){
                showToast("Bạn chưa nhập nội dung công việc!");
            } else{
                List<String> receiverIds = new ArrayList<>();
                List<String> receiverNames = new ArrayList<>();
                List<String> receiverImages = new ArrayList<>();
                List<String> receiverPhones = new ArrayList<>();
                List<String> receiverCompleted = new ArrayList<>();

                // Duyệt qua các trưởng vùng mà người dùng chọn để lấy tên và id
                for (User user : selectedUser) {
                    receiverIds.add(user.id);
                    receiverNames.add(user.name);
                    receiverImages.add(user.image);
                    receiverPhones.add(user.phone);
                }

                // Tạo các trường dữ liệu cho bảng task
                HashMap<String, Object> task = new HashMap<>();
                task.put(Constants.KEY_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                task.put(Constants.KEY_CREATOR_NAME, preferenceManager.getString(Constants.KEY_NAME));
                task.put(Constants.KEY_CREATOR_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
                task.put(Constants.KEY_CREATOR_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                task.put(Constants.KEY_RECEIVER_ID, receiverIds);
                task.put(Constants.KEY_RECEIVER_NAME, receiverNames);
                task.put(Constants.KEY_RECEIVER_IMAGE, receiverImages);
                task.put(Constants.KEY_RECEIVER_PHONE, receiverPhones);
                task.put(Constants.KEY_AMOUNT_RECEIVERS, selectedUser.size() + "");
                task.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
                task.put(Constants.KEY_TASK_CONTENT, edtContent.getText().toString());
                task.put(Constants.KEY_TASK_TITLE, spinnerTypeOfTask.getSelectedItem().toString());
                task.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);

                // Tạo nhiệm vụ lên cơ sở dữ liệu
                database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                        .document(taskComment.id)
                        .update(task)
                        .addOnCompleteListener(task1 -> {

                            // Cập nhật lại tình trạng ban đầu cho các ô dữ liệu
                            edtContent.setText("");
                            multipleUserSelectionAdapter.setUserUnSelected(users);

                            // Hiển hoặc ẩn danh sách trưởng khu
                            if (userRecyclerView.getVisibility() == View.VISIBLE){
                                userRecyclerView.setVisibility(View.GONE);
                                imageSelectUser.setImageResource(R.drawable.ic_up);
                            } else {
                                userRecyclerView.setVisibility(View.VISIBLE);
                                imageSelectUser.setImageResource(R.drawable.ic_down);
                            }

                        }).addOnFailureListener(e -> showToast(e.getMessage()));
                showToast("Đã cập nhật nhiệm vụ cố định thành công!");
                dialog.dismiss();

            }

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    // Mở hộp thoại chọn ngày
    private void openDatePicker(String type) {
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH,month);
            myCal.set(Calendar.DAY_OF_MONTH,day);
            if (type.equals(Constants.KEY_DAY_START_TASK))
                textDayOfStart.setText((myCal.get(Calendar.DAY_OF_MONTH) + "/"
                        + (myCal.get(Calendar.MONTH) + 1) + "/"
                        + myCal.get(Calendar.YEAR)));
            else textDayOfEnd.setText((myCal.get(Calendar.DAY_OF_MONTH) + "/"
                    + (myCal.get(Calendar.MONTH) + 1) + "/"
                    + myCal.get(Calendar.YEAR)));
        };

        DatePickerDialog dialog = new DatePickerDialog(
                requireActivity(), dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
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

    @SuppressLint("NotifyDataSetChanged")
    private void getAssignmentUserForFixedTask() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .document(taskComment.id)
                .get()
                .addOnCompleteListener(task -> {

                    DocumentSnapshot documentSnapshot = task.getResult();
                    List<String> receiverID = (List<String>) documentSnapshot.get(Constants.KEY_RECEIVER_ID);
                    assert receiverID != null;
                    for (String id : receiverID) {
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
                                });
                    }

                });
    }

    private void deleteTask(){
        if (taskComment.typeOfTask == null){
            database.collection(Constants.KEY_COLLECTION_TASK)
                    .document(taskComment.id)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        showToast("Đã xóa nhiệm vụ thành công!");
                        dismiss();
                    })
                    .addOnFailureListener(runnable -> showToast("Xóa nhiệm vụ không thành công!"));
        }
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

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicker(User user) {

    }

    @Override
    public void onMultipleUserSelection(Boolean isSelected) {

    }

    @Override
    public void onChangeTeamLeadClicker(User user) {

    }

    @Override
    public void onTaskClicker(Task task) {

    }

    @Override
    public void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection) {

    }
}