package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.CustomListTaskBinding;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MultipleTaskSelectionViewHolder> {

    private List<Task> tasks;
    private final MultipleListener multipleListener;
    private final Context context;
    private boolean isMultipleSelection = false;
    private CustomListTaskBinding customListTaskBinding;
    private int taskPosition;

    public TaskAdapter(Context context, List<Task> tasks, MultipleListener multipleListener) {
        this.tasks = tasks;
        this.multipleListener = multipleListener;
        this.context = context;
    }

    // Đánh dấu chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllSelected(List<Task> tasks){
        this.tasks = tasks;
        for (Task task : tasks)
            if (!task.isSelected)
                task.isSelected = true;
        notifyDataSetChanged();
    }

    // Bỏ chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllUnSelected(List<Task> tasks){
        this.tasks = tasks;
        for (Task task : tasks)
            if (task.isSelected)
                task.isSelected = false;
        notifyDataSetChanged();
    }

    // Bỏ chọn một số tài khoản khi đã thực thi xong yêu cầu và thay đổi quyền tài khoản sau khi người dùng bấm thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void setTaskUnSelected(List<Task> tasks){
        for (Task task : tasks){
            if (task.isSelected)
                task.isSelected = false;
        }
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void selectedTask(){
        Task task = tasks.get(taskPosition);
        customListTaskBinding.getRoot().setOnClickListener(view -> {
            if (task.isSelected){
                customListTaskBinding.viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                customListTaskBinding.imageSelected.setVisibility(View.GONE);
                task.isSelected = false;
                if (getSelectedTask().size() == 0){
                    multipleListener.onTaskSelectedClicker(false, isMultipleSelection);
                }
            } else{
                customListTaskBinding.viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                customListTaskBinding.imageSelected.setVisibility(View.VISIBLE);
                task.isSelected = true;
                multipleListener.onTaskSelectedClicker(true, isMultipleSelection);
            }
        });

    }

    @NonNull
    @Override
    public MultipleTaskSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        customListTaskBinding = CustomListTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MultipleTaskSelectionViewHolder(customListTaskBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleTaskSelectionViewHolder holder, int position) {
        holder.bindTaskSelection(tasks.get(position));
        taskPosition = holder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public List<Task> getSelectedTask(){
        List<Task> selectedTask = new ArrayList<>();
        for (Task task : tasks){
            if (task.isSelected){
                selectedTask.add(task);
            }
        }
        return selectedTask;
    }


    public class MultipleTaskSelectionViewHolder extends RecyclerView.ViewHolder{

        private final CustomListTaskBinding mBinding;
        private final PreferenceManager preferenceManager;

        MultipleTaskSelectionViewHolder(CustomListTaskBinding mBinding){
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            customListTaskBinding = this.mBinding;
            preferenceManager = new PreferenceManager(context);
        }

        @SuppressLint("SetTextI18n")
        void bindTaskSelection(final Task task){
            mBinding.nameTask.setText(task.title);
            mBinding.noteTask.setText(task.taskContent);
            if (task.typeOfTask == null){
                mBinding.imageLeave.setVisibility(View.VISIBLE);
                mBinding.dateTask.setText(task.dayOfStart +" - " + task.dayOfEnd);
            }
            if (task.status.equals("uncompleted") && !task.receiversCompleted.contains(preferenceManager.getString(Constants.KEY_USER_ID))) {
                mBinding.textStatus.setText("Chưa hoàn thành");
                mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
                mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
                setDrawableTint(Color.parseColor("#ed444f"));
            } else if (task.status.equals("completed") || task.receiversCompleted.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                mBinding.textStatus.setText("Hoàn thành");
                mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
                mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
                setDrawableTint(Color.parseColor("#51b155"));
            }

            //PreferenceManager preferenceManager = new PreferenceManager(itemView.getContext());
            if (task.isSelected){
                mBinding.viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                mBinding.imageSelected.setVisibility(View.VISIBLE);
            } else {
                mBinding.viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                mBinding.imageSelected.setVisibility(View.GONE);
            }
            ArrayList<User> users = new ArrayList<>();
            for (int i = 0; i < task.receiverID.size(); i++) {
                User user = new User();
                user.id = task.receiverID.get(i);
                user.name = task.receiverName.get(i);
                user.image = task.receiverImage.get(i);
                users.add(user);
            }
            for (int i = 0; i < task.receiversCompleted.size(); i++) {
                for (User user: users) {
                    if (task.receiversCompleted.get(i).equals(user.id)) {
                        user.isSelected = true;
                    }
                }
            }
            if (users.size() > 0) {
                if (users.get(0).isSelected) {
                    mBinding.imageUser1.setBorderColor(Color.parseColor("#51b155"));
                } else {
                    mBinding.imageUser1.setBorderColor(Color.parseColor("#ed444f"));
                }
                mBinding.imageUser1.setImageBitmap(getUserImage(task.receiverImage.get(0)));
                if (users.size() > 1) {
                    mBinding.imageUser2.setImageBitmap(getUserImage(task.receiverImage.get(1)));
                    if (users.get(1).isSelected) {
                        mBinding.imageUser2.setBorderColor(Color.parseColor("#51b155"));
                    } else {
                        mBinding.imageUser2.setBorderColor(Color.parseColor("#ed444f"));
                    }
                }
                if (users.size() > 2) {
                    mBinding.imageUser3.setImageBitmap(getUserImage(task.receiverImage.get(2)));
                    if (users.get(2).isSelected) {
                        mBinding.imageUser3.setBorderColor(Color.parseColor("#51b155"));
                    } else {
                        mBinding.imageUser3.setBorderColor(Color.parseColor("#ed444f"));
                    }
                }
                if (users.size() > 3) {
                    mBinding.imageUser4.setImageBitmap(getUserImage(task.receiverImage.get(3)));
                    if (users.get(3).isSelected) {
                        mBinding.imageUser4.setBorderColor(Color.parseColor("#51b155"));
                    } else {
                        mBinding.imageUser4.setBorderColor(Color.parseColor("#ed444f"));
                    }
                }
                if (users.size() > 4) {
                    mBinding.imageUser5.setImageBitmap(getUserImage(task.receiverImage.get(4)));
                    if (users.get(4).isSelected) {
                        mBinding.imageUser5.setBorderColor(Color.parseColor("#51b155"));
                    } else {
                        mBinding.imageUser5.setBorderColor(Color.parseColor("#ed444f"));
                    }
                }
            }

            if (task.typeOfTask != null){
                if (task.status.equals(Constants.KEY_UNCOMPLETED)){
                    mBinding.viewBackground.setBackgroundResource(R.drawable.background_task_uncompleted);
                } else {
                    mBinding.viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                }
            }

            mBinding.layoutUserSelection.setOnLongClickListener(view -> {
                if (task.typeOfTask == null) {
                    if (task.isSelected){
                        mBinding.viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                        mBinding.imageSelected.setVisibility(View.GONE);
                        task.isSelected = false;
                        if (getSelectedTask().size() == 0){
                            multipleListener.onMultipleUserSelection(false);
                        }
                    } else{
                        mBinding.viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                        mBinding.imageSelected.setVisibility(View.VISIBLE);
                        task.isSelected = true;
                        multipleListener.onMultipleUserSelection(true);
                    }
                    isMultipleSelection = true;
                }
                return true;
            });

            mBinding.layoutUserSelection.setOnClickListener(view -> multipleListener.onTaskClicker(task));

        }

        private Bitmap getUserImage(String encodedImage){
            byte[] bytes = new byte[0];
            if (encodedImage != null){
                bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            }

            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }

        private void setDrawableTint(int color) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(R.drawable.ic_access_time);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, color);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            mBinding.textStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

    }
}

