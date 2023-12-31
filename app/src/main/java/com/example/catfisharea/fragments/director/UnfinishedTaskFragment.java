package com.example.catfisharea.fragments.director;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.FragmentUnfinishedTaskBinding;
import com.example.catfisharea.adapter.TaskAdapter;
import com.example.catfisharea.bottomsheet.CommentBottomSheetFragment;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UnfinishedTaskFragment extends Fragment implements MultipleListener {

    private FragmentUnfinishedTaskBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private TaskAdapter taskAdapter;
    private List<Task> tasks;
    private int daySelected, monthSelected, yearSelected;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentUnfinishedTaskBinding.inflate(inflater, container, false);
        init();
        setListener();
        getTasks();
        showTaskMessage();
        return mBinding.getRoot();
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
        mBinding.taskRecyclerView.setAdapter(taskAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            mBinding.textMessage.setText("Hôm nay, chưa có nhiệm vụ nào được giao.");

    }

    @SuppressLint("NotifyDataSetChanged")
    private void setListener() {

        // Bấm hoàn thành nhiệm vụ
        mBinding.btnComplete.setOnClickListener(view -> {

            List<Task> selectedTask = taskAdapter.getSelectedTask();

            if (selectedTask.size() == 0){
                showToast("Vui lòng chọn ít nhất một nhiệm vụ cần hoàn thành!");
            } else {
                for (int i = 0; i < selectedTask.size(); i++){
                    int finalI = i;
                    database.collection(Constants.KEY_COLLECTION_TASK)
                            .document(selectedTask.get(i).id)
                            .get()
                            .addOnCompleteListener(task -> {

                                DocumentSnapshot documentSnapshot = task.getResult();
                                List<String> receiverCompleted = (List<String>) documentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                                String completed = documentSnapshot.getString(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED);
                                String receiver = documentSnapshot.getString(Constants.KEY_AMOUNT_RECEIVERS);
                                int amountCompleted = Integer.parseInt(completed);
                                int amountReceiverTask = Integer.parseInt(receiver);

                                amountCompleted++;
                                if (amountReceiverTask != 1){
                                    if (amountCompleted == amountReceiverTask){
                                        HashMap<String, Object> updatedTask = new HashMap<>();
                                        updatedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);
                                        assert receiverCompleted != null;
                                        receiverCompleted.add(preferenceManager.getString(Constants.KEY_USER_ID));
                                        updatedTask.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
                                        updatedTask.put(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED, amountCompleted + "");
                                        database.collection(Constants.KEY_COLLECTION_TASK)
                                                .document(selectedTask.get(finalI).id)
                                                .update(updatedTask)
                                                .addOnSuccessListener(unused -> {
                                                    tasks.remove(selectedTask.get(finalI));
                                                    taskAdapter.notifyDataSetChanged();
                                                });
                                    } else {
                                        assert receiverCompleted != null;
                                        receiverCompleted.add(preferenceManager.getString(Constants.KEY_USER_ID));
                                        HashMap<String, Object> updatedTask = new HashMap<>();
                                        updatedTask.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
                                        updatedTask.put(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED, amountCompleted + "");
                                        database.collection(Constants.KEY_COLLECTION_TASK)
                                                .document(selectedTask.get(finalI).id)
                                                .update(updatedTask)
                                                .addOnSuccessListener(unused -> {
                                                    tasks.remove(selectedTask.get(finalI));
                                                    taskAdapter.notifyDataSetChanged();
                                                });
                                    }
                                } else {
                                    List<String> userId = new ArrayList<>();
                                    userId.add(preferenceManager.getString(Constants.KEY_USER_ID));
                                    HashMap<String, Object> updatedTask = new HashMap<>();
                                    updatedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);
                                    updatedTask.put(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED, amountCompleted + "");
                                    updatedTask.put(Constants.KEY_RECEIVERS_ID_COMPLETED, userId);
                                    database.collection(Constants.KEY_COLLECTION_TASK)
                                            .document(selectedTask.get(finalI).id)
                                            .update(updatedTask)
                                            .addOnSuccessListener(unused -> {
                                                tasks.remove(selectedTask.get(finalI));
                                                taskAdapter.notifyDataSetChanged();
                                            });
                                }

                            });
                }
                database.collection(Constants.KEY_COLLECTION_TASK)
                        .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                        .get()
                        .addOnCompleteListener(task -> {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                                for (int i = 0; i < selectedTask.size(); i++){

                                    if (queryDocumentSnapshot.getId().equals(selectedTask.get(i).id)){

                                        HashMap<String, Object> updatedTask = new HashMap<>();
                                        updatedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);

                                        int finalI = i;
                                        database.collection(Constants.KEY_COLLECTION_TASK)
                                                .document(queryDocumentSnapshot.getId())
                                                .update(updatedTask)
                                                .addOnSuccessListener(unused -> {
                                                    tasks.remove(selectedTask.get(finalI));
                                                    taskAdapter.notifyDataSetChanged();
                                                });

                                    }

                                }

                            }
                        });

            }

        });

    }

    // Hàm lấy các user từ database về và hiển thị ra recyclerview
    @SuppressLint("NotifyDataSetChanged")
    public void getTasks(){

        tasks.clear();

        daySelected = Integer.parseInt(preferenceManager.getString(Constants.KEY_DAY_SELECTED));
        monthSelected = Integer.parseInt(preferenceManager.getString(Constants.KEY_MONTH_SELECTED));
        yearSelected = Integer.parseInt(preferenceManager.getString(Constants.KEY_YEAR_SELECTED));

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
            getCompletedTaskForRegionalAndDirector();
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_WORKER)) {
            getCompletedTaskOfWorkerAndDirector();
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
            if (!preferenceManager.getString(Constants.KEY_DIRECTOR).equals("")){
                if (preferenceManager.getString(Constants.KEY_DIRECTOR).equals(Constants.KEY_MY_DIRECTOR_TASK)){
                    getCompletedTaskOfWorkerAndDirector();
                } else {
                    getCompletedTaskForRegionalAndDirector();
                    mBinding.btnComplete.setVisibility(View.GONE);
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void getCompletedTaskOfWorkerAndDirector(){
        tasks.clear();
        database.collection(Constants.KEY_COLLECTION_TASK)
                .whereEqualTo(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED)
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverIds = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);

                        for (String id : receiverIds){

                            if (id.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                String fullDayOfStart = queryDocumentSnapshot.getString(Constants.KEY_DAY_START_TASK);
                                String fullDayOfEnd = queryDocumentSnapshot.getString(Constants.KEY_DAY_END_TASK);

                                assert fullDayOfStart != null;
                                String dayStart;
                                String monthStart;
                                String yearStart;
                                String[] listDayStart = fullDayOfStart.split("/");

                                dayStart = listDayStart[0];
                                monthStart = listDayStart[1];
                                yearStart = listDayStart[2];


                                assert fullDayOfEnd != null;
                                String dayEnd;
                                String monthEnd;
                                String yearEnd;
                                String[] listDayEnd = fullDayOfEnd.split("/");

                                dayEnd = listDayEnd[0];
                                monthEnd = listDayEnd[1];
                                yearEnd = listDayEnd[2];

                                List<Integer> day = new ArrayList<>();
                                int lengthOfDay;
                                int intOfDayStart = Integer.parseInt(dayStart);
                                int intOfDayEnd = Integer.parseInt(dayEnd);

                                if (intOfDayEnd < intOfDayStart){
                                    if (monthEnd.equals("1") || monthEnd.equals("3") || monthEnd.equals("5") || monthEnd.equals("7") || monthEnd.equals("8")
                                            || monthEnd.equals("10") || monthEnd.equals("12")){
                                        lengthOfDay =  (31 - intOfDayStart + 1) + intOfDayEnd;
                                        int num = intOfDayStart;
                                        while (day.size() != lengthOfDay){
                                            if (num <= 31 && num >= intOfDayStart){
                                                day.add(num);
                                                num++;
                                                if (num >= intOfDayStart){
                                                    num = 1;
                                                }
                                            }

                                            if (num <= intOfDayEnd){
                                                day.add(num);
                                                num++;
                                            }
                                        }

                                    } else if (monthEnd.equals("4") || monthEnd.equals("6") || monthEnd.equals("9") || monthEnd.equals("11")) {
                                        lengthOfDay = (30 - intOfDayStart + 1) + intOfDayEnd;
                                        int num = intOfDayStart;
                                        while (day.size() != lengthOfDay){
                                            if (num <= 30 && num >= intOfDayStart){
                                                day.add(num);
                                                num++;
                                                if (num >= intOfDayStart){
                                                    num = 1;
                                                }
                                            }

                                            if (num <= intOfDayEnd){
                                                day.add(num);
                                                num++;
                                            }
                                        }
                                    }
                                } else {
                                    lengthOfDay = intOfDayEnd - intOfDayStart + 1;
                                    int num = intOfDayStart;
                                    for (int i  = 0; i < lengthOfDay; i++){
                                        if (num >= intOfDayStart && num <= intOfDayEnd){
                                            day.add(num);
                                            num++;
                                        }
                                    }
                                }

                                if (day.contains(daySelected) && (yearSelected == Integer.parseInt(yearStart) || yearSelected == Integer.parseInt(yearEnd))
                                        && (monthSelected == Integer.parseInt(monthStart) || monthSelected == Integer.parseInt(monthEnd))){

                                    boolean isUnCompleted = true;
                                    List<String> receiverCompleted = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                                    for (String receiverId : receiverCompleted){
                                        if (receiverId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                            isUnCompleted = false;
                                        }
                                    }
                                    if (isUnCompleted){
                                        Task task1 = new Task();
                                        task1.id = queryDocumentSnapshot.getId();
                                        task1.creatorID = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_ID);
                                        task1.title = queryDocumentSnapshot.getString(Constants.KEY_TASK_TITLE);
                                        task1.creatorName = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_NAME);
                                        task1.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_PHONE);
                                        task1.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_IMAGE);
                                        task1.receiverID = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                                        task1.receiverName = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_NAME);
                                        task1.receiverImage = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_IMAGE);
                                        task1.receiverPhone = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_PHONE);
                                        task1.receiversCompleted = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                                        task1.taskContent = queryDocumentSnapshot.getString(Constants.KEY_TASK_CONTENT);
                                        task1.title = queryDocumentSnapshot.getString(Constants.KEY_TASK_TITLE);
                                        task1.dayOfStart = queryDocumentSnapshot.getString(Constants.KEY_DAY_START_TASK);
                                        task1.dayOfEnd = queryDocumentSnapshot.getString(Constants.KEY_DAY_END_TASK);
                                        task1.status = queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK);
                                        tasks.add(task1);
                                        taskAdapter.notifyDataSetChanged();
                                    }

                                }
                            }

                        }

                        if (tasks.size() == 0){
                            taskAdapter.notifyDataSetChanged();
                        }

                        showTaskMessage();

                        preferenceManager.remove(Constants.KEY_DAY_SELECTED);
                        preferenceManager.remove(Constants.KEY_MONTH_SELECTED);
                        preferenceManager.remove(Constants.KEY_YEAR_SELECTED);

                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getCompletedTaskForRegionalAndDirector(){
        tasks.clear();
        database.collection(Constants.KEY_COLLECTION_TASK)
                .whereEqualTo(Constants.KEY_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED)
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        String fullDayOfStart = queryDocumentSnapshot.getString(Constants.KEY_DAY_START_TASK);
                        String fullDayOfEnd = queryDocumentSnapshot.getString(Constants.KEY_DAY_END_TASK);

                        assert fullDayOfStart != null;
                        String dayStart;
                        String monthStart;
                        String yearStart;
                        String[] listDayStart = fullDayOfStart.split("/");

                        dayStart = listDayStart[0];
                        monthStart = listDayStart[1];
                        yearStart = listDayStart[2];


                        assert fullDayOfEnd != null;
                        String dayEnd;
                        String monthEnd;
                        String yearEnd;
                        String[] listDayEnd = fullDayOfEnd.split("/");

                        dayEnd = listDayEnd[0];
                        monthEnd = listDayEnd[1];
                        yearEnd = listDayEnd[2];

                        List<Integer> day = new ArrayList<>();
                        int lengthOfDay;
                        int intOfDayStart = Integer.parseInt(dayStart);
                        int intOfDayEnd = Integer.parseInt(dayEnd);

                        if (intOfDayEnd < intOfDayStart){
                            if (monthEnd.equals("1") || monthEnd.equals("3") || monthEnd.equals("5") || monthEnd.equals("7") || monthEnd.equals("8")
                                    || monthEnd.equals("10") || monthEnd.equals("12")){
                                lengthOfDay =  (31 - intOfDayStart + 1) + intOfDayEnd;
                                int num = intOfDayStart;
                                while (day.size() != lengthOfDay){
                                    if (num <= 31 && num >= intOfDayStart){
                                        day.add(num);
                                        num++;
                                        if (num >= intOfDayStart){
                                            num = 1;
                                        }
                                    }

                                    if (num <= intOfDayEnd){
                                        day.add(num);
                                        num++;
                                    }
                                }

                            } else if (monthEnd.equals("4") || monthEnd.equals("6") || monthEnd.equals("9") || monthEnd.equals("11")) {
                                lengthOfDay = (30 - intOfDayStart + 1) + intOfDayEnd;
                                int num = intOfDayStart;
                                while (day.size() != lengthOfDay){
                                    if (num <= 30 && num >= intOfDayStart){
                                        day.add(num);
                                        num++;
                                        if (num >= intOfDayStart){
                                            num = 1;
                                        }
                                    }

                                    if (num <= intOfDayEnd){
                                        day.add(num);
                                        num++;
                                    }
                                }
                            }
                        } else {
                            lengthOfDay = intOfDayEnd - intOfDayStart + 1;
                            int num = intOfDayStart;
                            for (int i  = 0; i < lengthOfDay; i++){
                                if (num >= intOfDayStart && num <= intOfDayEnd){
                                    day.add(num);
                                    num++;
                                }
                            }
                        }

                        if (day.contains(daySelected) && (yearSelected == Integer.parseInt(yearStart) || yearSelected == Integer.parseInt(yearEnd))
                                && (monthSelected == Integer.parseInt(monthStart) || monthSelected == Integer.parseInt(monthEnd))){

                            if (queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK).equals(Constants.KEY_UNCOMPLETED)){
                                Task task1 = new Task();
                                task1.id = queryDocumentSnapshot.getId();
                                task1.creatorID = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_ID);
                                task1.title = queryDocumentSnapshot.getString(Constants.KEY_TASK_TITLE);
                                task1.creatorName = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_NAME);
                                task1.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_PHONE);
                                task1.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_CREATOR_IMAGE);
                                task1.receiverID = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                                task1.receiverName = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_NAME);
                                task1.receiverImage = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_IMAGE);
                                task1.receiverPhone = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_PHONE);
                                task1.receiversCompleted = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                                task1.taskContent = queryDocumentSnapshot.getString(Constants.KEY_TASK_CONTENT);
                                task1.title = queryDocumentSnapshot.getString(Constants.KEY_TASK_TITLE);
                                task1.dayOfStart = queryDocumentSnapshot.getString(Constants.KEY_DAY_START_TASK);
                                task1.dayOfEnd = queryDocumentSnapshot.getString(Constants.KEY_DAY_END_TASK);
                                task1.status = queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK);
                                tasks.add(task1);
                                taskAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                    if (tasks.size() == 0){
                        taskAdapter.notifyDataSetChanged();
                    }

                    showTaskMessage();

                    preferenceManager.remove(Constants.KEY_DAY_SELECTED);
                    preferenceManager.remove(Constants.KEY_MONTH_SELECTED);
                    preferenceManager.remove(Constants.KEY_YEAR_SELECTED);
                });

    }

    public void showTaskMessage(){
        if (tasks.size() == 0){
            mBinding.taskRecyclerView.setVisibility(View.GONE);
            mBinding.btnComplete.setVisibility(View.GONE);
            mBinding.textMessage.setVisibility(View.VISIBLE);
        } else {
            mBinding.taskRecyclerView.setVisibility(View.VISIBLE);
            mBinding.textMessage.setVisibility(View.GONE);
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
                mBinding.btnComplete.setVisibility(View.GONE);
            else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
                if (preferenceManager.getString(Constants.KEY_DIRECTOR).equals(Constants.KEY_MY_DIRECTOR_TASK))
                    mBinding.btnComplete.setVisibility(View.VISIBLE);
                else mBinding.btnComplete.setVisibility(View.GONE);
            } else {
                mBinding.btnComplete.setVisibility(View.VISIBLE);
            }
        }

    }

    public void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
        if (isMultipleSelection){
            taskAdapter.selectedTask();
        }
    }
}