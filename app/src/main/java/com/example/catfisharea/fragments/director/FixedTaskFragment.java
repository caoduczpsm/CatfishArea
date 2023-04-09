package com.example.catfisharea.fragments.director;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentFixedTaskBinding;
import com.example.catfisharea.adapter.TaskAdapter;
import com.example.catfisharea.bottomsheet.CommentBottomSheetFragment;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FixedTaskFragment extends Fragment implements MultipleListener {

    private FragmentFixedTaskBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private TaskAdapter taskAdapter;
    private List<Task> tasks;

    public FixedTaskFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFixedTaskBinding.inflate(inflater, container, false);

        init();
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
            getAllTasksForDirector();
        } else {
            getAllTasksForWorker();
        }
        if (tasks.size() == 0){
            binding.textMessage.setText("Bạn chưa giao bất kỳ nhiệm vụ cố định nào.");
        }

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void init(){

        //FirebaseFirestore
        database = FirebaseFirestore.getInstance();

        //PreferenceManager
        preferenceManager = new PreferenceManager(requireContext());

        //List
        tasks = new ArrayList<>();

        //Adapter
        taskAdapter = new TaskAdapter(getContext(), tasks, this);
        binding.taskRecyclerView.setAdapter(taskAdapter);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllTasksForDirector(){
        tasks.clear();
        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        Task task1 = new Task();
                        task1.id = queryDocumentSnapshot.getId();
                        task1.title = queryDocumentSnapshot.getString(Constants.KEY_TASK_TITLE);
                        task1.creatorID = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_ID);
                        task1.creatorName = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_NAME);
                        task1.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_PHONE);
                        task1.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_IMAGE);
                        task1.receiverID = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                        task1.receiverName = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_NAME);
                        task1.receiverImage = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_IMAGE);
                        task1.receiverPhone = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_PHONE);
                        task1.receiversCompleted = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                        task1.taskContent = queryDocumentSnapshot.getString(Constants.KEY_TASK_CONTENT);
                        task1.status = queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK);
                        task1.typeOfTask = Constants.KEY_COLLECTION_FIXED_TASK;
                        tasks.add(task1);
                        taskAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void getAllTasksForWorker(){

    }

    @Override
    public void onMultipleUserSelection(Boolean isSelected) {

    }

    @Override
    public void onChangeTeamLeadClicker(User user) {

    }

    @Override
    public void onTaskClicker(Task task) {
        CommentBottomSheetFragment bottomSheetDialog = CommentBottomSheetFragment.newInstance(task);
        bottomSheetDialog.show(requireActivity().getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    @Override
    public void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection) {

    }
}