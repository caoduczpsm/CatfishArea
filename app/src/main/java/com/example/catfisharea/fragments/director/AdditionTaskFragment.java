package com.example.catfisharea.fragments.director;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AdditionTaskFragment extends Fragment implements MultipleListener {

    private Calendar myCal;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private List<User> users, selectedDirector;
    private FirebaseFirestore database;
    private Button btnCreate, btnDone;
    private ProgressBar progressBar;
    private RecyclerView directorRecyclerView;
    private TextView textSelectUser, textDayOfStart, textDayOfEnd;
    private ImageView imageSelectDirector;
    private EditText edtContent, edtTitle;
    private RadioButton radioFixedTask, radioMomentarilyTask;
    private Spinner spinnerTypeOfFixedTask;

    public AdditionTaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_addition_task, container, false);

        //RecyclerView
        directorRecyclerView = view.findViewById(R.id.directorRecyclerView);

        //Button
        btnCreate = view.findViewById(R.id.btnCreate);
        btnDone = view.findViewById(R.id.btnDone);

        //ProgressBar
        progressBar = view.findViewById(R.id.progressBar);

        //TextView
        textSelectUser = view.findViewById(R.id.textSelectDirector);
        textDayOfStart = view.findViewById(R.id.textDayOfStart);
        textDayOfEnd = view.findViewById(R.id.textDayOfEnd);

        //ImageView
        imageSelectDirector = view.findViewById(R.id.imageSelectDirector);

        //EditText
        edtContent = view.findViewById(R.id.edtContent);
        edtTitle = view.findViewById(R.id.edtTitle);

        //RadioButton
        radioFixedTask = view.findViewById(R.id.radioFixedTask);
        radioMomentarilyTask = view.findViewById(R.id.radioMomentarilyTask);

        //Spinner
        spinnerTypeOfFixedTask = view.findViewById(R.id.spinnerTypeOfTask);

        radioFixedTask.setChecked(true);

        init();
        getUsers();
        setListener();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void init(){

        //PreferenceManager
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        //FirebaseFireStore
        database = FirebaseFirestore.getInstance();

        //Calendar
        myCal = Calendar.getInstance();

        //List
        users = new ArrayList<>();
        selectedDirector = new ArrayList<>();
        List<String> typeOfFixedTask = new ArrayList<>();

        typeOfFixedTask.add(Constants.KEY_FIXED_TASK_FEED_FISH);
        typeOfFixedTask.add(Constants.KEY_FIXED_TASK_MEASURE_WATER);

        //ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.custom_layout_spinner, typeOfFixedTask);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeOfFixedTask.setAdapter(adapter);

        //Adapter
        usersAdapter = new MultipleUserSelectionAdapter(users, this);
        directorRecyclerView.setAdapter(usersAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            textSelectUser.setText("Chọn trưởng khu");
        else textSelectUser.setText("Chọn công nhân");

    }

    @SuppressLint("SetTextI18n")
    private void setListener() {

        // Hiển thị hoặc ẩn danh sách trưởng khu
        textSelectUser.setOnClickListener(view -> {
            if (directorRecyclerView.getVisibility() == View.VISIBLE){
                directorRecyclerView.setVisibility(View.GONE);
                imageSelectDirector.setImageResource(R.drawable.ic_up);
            } else {
                directorRecyclerView.setVisibility(View.VISIBLE);
                imageSelectDirector.setImageResource(R.drawable.ic_down);
            }
        });

        // Hiển thị hoặc ẩn danh sách trưởng khu
        imageSelectDirector.setOnClickListener(view -> {
            if (directorRecyclerView.getVisibility() == View.VISIBLE){
                directorRecyclerView.setVisibility(View.GONE);
                imageSelectDirector.setImageResource(R.drawable.ic_up);
            } else {
                directorRecyclerView.setVisibility(View.VISIBLE);
                imageSelectDirector.setImageResource(R.drawable.ic_down);
            }
        });

        // Chọn ngày bắt đầu
        textDayOfStart.setOnClickListener(view -> openDatePicker(Constants.KEY_DAY_START_TASK));

        // Chọn ngày kết thúc
        textDayOfEnd.setOnClickListener(view -> openDatePicker(Constants.KEY_DAY_END_TASK));

        // Tạo nhiệm vụ mới
        btnCreate.setOnClickListener(view -> {
            if (radioMomentarilyTask.isChecked())
                createMomentarilyTask();
            if (radioFixedTask.isChecked())
                createFixedTask();
        });

        radioFixedTask.setOnClickListener(view -> setVisibilityTextOfDay(false));

        radioMomentarilyTask.setOnClickListener(view -> setVisibilityTextOfDay(true));

        btnDone.setOnClickListener(view -> replaceFragments(new OverviewTaskFragment()));
    }

    @SuppressLint("SetTextI18n")
    private void createFixedTask(){
        selectedDirector.clear();
        selectedDirector = usersAdapter.getSelectedUser();
        if (isValidSignUpDetailsForFixedTask()){
            loading(true);

            List<String> receiverIds = new ArrayList<>();
            List<String> receiverNames = new ArrayList<>();
            List<String> receiverImages = new ArrayList<>();
            List<String> receiverPhones = new ArrayList<>();
            List<String> receiverCompleted = new ArrayList<>();

            // Duyệt qua các trưởng vùng mà người dùng chọn để lấy tên và id
            for (User user : selectedDirector) {
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
            task.put(Constants.KEY_AMOUNT_RECEIVERS, selectedDirector.size() + "");
            task.put(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED, "0");
            task.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
            task.put(Constants.KEY_TASK_CONTENT, edtContent.getText().toString());
            task.put(Constants.KEY_TASK_TITLE, spinnerTypeOfFixedTask.getSelectedItem().toString());
            task.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);

            // Tạo nhiệm vụ lên cơ sở dữ liệu
            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                    .add(task)
                    .addOnCompleteListener(task1 -> {

                        loading(false);

                        // Cập nhật lại tình trạng ban đầu cho các ô dữ liệu
                        edtContent.setText("");
                        edtTitle.setText("");
                        usersAdapter.setUserUnSelected(users);

                        // Hiển hoặc ẩn danh sách trưởng khu
                        if (directorRecyclerView.getVisibility() == View.VISIBLE){
                            directorRecyclerView.setVisibility(View.GONE);
                            imageSelectDirector.setImageResource(R.drawable.ic_up);
                        } else {
                            directorRecyclerView.setVisibility(View.VISIBLE);
                            imageSelectDirector.setImageResource(R.drawable.ic_down);
                        }


                    }).addOnFailureListener(e -> showToast(e.getMessage()));

        }
    }

    @SuppressLint("SetTextI18n")
    private void createMomentarilyTask(){
        selectedDirector.clear();
        selectedDirector = usersAdapter.getSelectedUser();
        if (isValidSignUpDetailsForMomentarilyTask()){
            loading(true);

            List<String> receiverIds = new ArrayList<>();
            List<String> receiverNames = new ArrayList<>();
            List<String> receiverImages = new ArrayList<>();
            List<String> receiverPhones = new ArrayList<>();
            List<String> receiverCompleted = new ArrayList<>();

            // Duyệt qua các trưởng vùng mà người dùng chọn để lấy tên và id
            for (User user : selectedDirector) {
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
            task.put(Constants.KEY_AMOUNT_RECEIVERS, selectedDirector.size() + "");
            task.put(Constants.KEY_AMOUNT_RECEIVERS_COMPLETED, "0");
            task.put(Constants.KEY_RECEIVERS_ID_COMPLETED, receiverCompleted);
            task.put(Constants.KEY_DAY_START_TASK, textDayOfStart.getText().toString());
            task.put(Constants.KEY_DAY_END_TASK, textDayOfEnd.getText().toString());
            task.put(Constants.KEY_TASK_CONTENT, edtContent.getText().toString());
            task.put(Constants.KEY_TASK_TITLE, edtTitle.getText().toString());
            task.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);

            // Tạo nhiệm vụ lên cơ sở dữ liệu
            database.collection(Constants.KEY_COLLECTION_TASK)
                    .add(task)
                    .addOnCompleteListener(task1 -> {

                        loading(false);

                        // Cập nhật lại tình trạng ban đầu cho các ô dữ liệu
                        textDayOfStart.setText("Chọn ngày bắt đầu");
                        textDayOfEnd.setText("Chọn ngày kết thúc");
                        edtContent.setText("");
                        edtTitle.setText("");
                        usersAdapter.setUserUnSelected(users);

                        // Hiển hoặc ẩn danh sách trưởng khu
                        if (directorRecyclerView.getVisibility() == View.VISIBLE){
                            directorRecyclerView.setVisibility(View.GONE);
                            imageSelectDirector.setImageResource(R.drawable.ic_up);
                        } else {
                            directorRecyclerView.setVisibility(View.VISIBLE);
                            imageSelectDirector.setImageResource(R.drawable.ic_down);
                        }


                    }).addOnFailureListener(e -> showToast(e.getMessage()));

        }

    }

    private void setVisibilityTextOfDay(boolean isVisible){
        if (isVisible){
            textDayOfEnd.setVisibility(View.VISIBLE);
            textDayOfStart.setVisibility(View.VISIBLE);
            edtTitle.setVisibility(View.VISIBLE);
            spinnerTypeOfFixedTask.setVisibility(View.GONE);
        } else {
            textDayOfEnd.setVisibility(View.GONE);
            textDayOfStart.setVisibility(View.GONE);
            edtTitle.setVisibility(View.GONE);
            spinnerTypeOfFixedTask.setVisibility(View.VISIBLE);
        }
    }

    // Hàm lấy các user từ database về và hiển thị ra recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private void getUsers(){

        if (Constants.KEY_REGIONAL_CHIEF.equals(preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT))){
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
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
                                users.add(user);
                                usersAdapter.notifyDataSetChanged();

                            }

                        }

                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
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
                                users.add(user);
                                usersAdapter.notifyDataSetChanged();

                            }

                        }

                    });
        }

    }

    public void replaceFragments(Fragment fragment) {
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        assert fragment != null;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.overviewFragment, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
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

    // Hàm kiểm tra người dùng đã nhập đủ dữ liệu chưa
    private Boolean isValidSignUpDetailsForMomentarilyTask(){
        if(selectedDirector.size() == 0){
            showToast("Chọn ít nhất một trưởng vùng!");
            return false;
        } else if(textDayOfStart.getText().toString().trim().isEmpty()){
            showToast("Bạn chưa chọn ngày bắt đầu!");
            return false;
        } else if(textDayOfEnd.getText().toString().trim().isEmpty()){
            showToast("Bạn chưa chọn ngày kết thúc");
            return false;
        } else if(edtContent.getText().toString().trim().isEmpty()){
            showToast("Bạn chưa nhập nội dung công việc!");
            return false;
        } else if(edtTitle.getText().toString().trim().isEmpty()){
            showToast("Bạn chưa nhập tiêu đề công việc!");
            return false;
        } else{
            return true;
        }
    }

    // Hàm kiểm tra người dùng đã nhập đủ dữ liệu chưa
    private Boolean isValidSignUpDetailsForFixedTask(){
        if(selectedDirector.size() == 0){
            showToast("Chọn ít nhất một trưởng vùng!");
            return false;
        } else if(edtContent.getText().toString().trim().isEmpty()){
            showToast("Bạn chưa nhập nội dung công việc!");
            return false;
        } else{
            return true;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    // Hàm giả lập trạng thái loading và ẩn lúc tạo
    private void loading(Boolean isLoading){
        if(isLoading){
            btnCreate.setVisibility(View.INVISIBLE);
            btnDone.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else{
            btnCreate.setVisibility(View.VISIBLE);
            btnDone.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
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