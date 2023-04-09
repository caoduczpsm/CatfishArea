package com.example.catfisharea.fragments.director;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverviewTaskFragment extends Fragment {

    private PieChart pieChart;
    private PieDataSet pieDataSet;

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    private ImageView imageAddTask;
    private TextView textAddTask, textNumCompletedTask, textNumUncompletedTask;

    private AdditionTaskFragment additionTaskFragment;
    private ConstraintLayout layoutOverview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview_task, container, false);

        pieChart = view.findViewById(R.id.pieChart);

        //ImageView
        imageAddTask = view.findViewById(R.id.imageAddTask);

        //TextView
        textAddTask = view.findViewById(R.id.textAddTask);
        textNumCompletedTask = view.findViewById(R.id.textNumCompletedTask);
        textNumUncompletedTask = view.findViewById(R.id.textNumUncompletedTask);

        //FrameLayout
        layoutOverview = view.findViewById(R.id.layoutOverview);

        init();

        // Tạo biểu đồ tròn cho hiển thị nhiệm vụ
        createPieChart();
        setListener();

        return view;
    }

    public OverviewTaskFragment() {
        // Required empty public constructor
    }


    private void init() {

        //PreferenceManager
        preferenceManager = new PreferenceManager(requireContext());

        //FirebaseFireStore
        database = FirebaseFirestore.getInstance();

        //Fragment
        additionTaskFragment = new AdditionTaskFragment();

        if (!preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_WORKER))
            textAddTask.setVisibility(View.VISIBLE);


    }

    private void setListener() {

        textAddTask.setOnClickListener(view -> replaceFragments(additionTaskFragment));

        imageAddTask.setOnClickListener(view -> replaceFragments(additionTaskFragment));

        //imageAddTask.setOnClickListener(view -> addTaskDialog());

    }

    public void replaceFragments(Fragment fragment) {
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        assert fragment != null;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.overviewFragment, fragment);
        layoutOverview.setVisibility(View.GONE);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("SetTextI18n")
    public void createPieChart() {

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
            getTaskForRegionChiefAndDirector();
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
            if (preferenceManager.getString(Constants.KEY_DIRECTOR).equals(Constants.KEY_MY_DIRECTOR_TASK)){
                getTaskOfWorkerAndDirector();
            } else {
                getTaskForRegionChiefAndDirector();
            }
        } else {
            getTaskOfWorkerAndDirector();
        }

    }

    @SuppressLint("SetTextI18n")
    private void getTaskForRegionChiefAndDirector(){
        ArrayList<PieEntry> dataVal = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_TASK)
                .whereEqualTo(Constants.KEY_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {

                    // Biến đếm số nhiệm vụ đã hoàn thành và chưa hoàn thành
                    int countAmountCompleted = 0;
                    int countAmountUncompleted = 0;

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK), Constants.KEY_COMPLETED))
                            countAmountCompleted++;
                        else countAmountUncompleted++;

                    }

                    textNumCompletedTask.setText(countAmountCompleted + "");
                    textNumUncompletedTask.setText(countAmountUncompleted + "");

                    dataVal.add(new PieEntry(countAmountCompleted, "Hoàn thành"));
                    dataVal.add(new PieEntry(countAmountUncompleted, "Chưa hoàn thành"));
                    pieDataSet = new PieDataSet(dataVal, "");

                    List<Integer> colors = new ArrayList<>();
                    colors.add(Color.rgb(0,129, 201));
                    colors.add(Color.rgb(239,163, 157));

                    pieDataSet.setColors(colors);

                    pieDataSet.setValueTextSize(18f);
                    pieDataSet.setValueTextColor(Color.WHITE);

                    PieData pieData = new PieData(pieDataSet);

                    pieChart.setDrawEntryLabels(false);
                    pieChart.getDescription().setText("Các khoản chi tiêu ");
                    pieChart.getDescription().setTextSize(16f);
                    pieChart.setUsePercentValues(true);
                    pieChart.setEntryLabelTextSize(18f);
                    pieChart.setCenterTextRadiusPercent(50);
                    pieChart.setHoleRadius(30);
                    pieChart.setTransparentCircleRadius(40);
                    pieChart.setTransparentCircleColor(Color.RED);
                    pieChart.setTransparentCircleAlpha(50);
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                    pieChart.getDescription().setEnabled(false);
                    pieChart.animateX(2000);

                });

    }

    @SuppressLint("SetTextI18n")
    private void getTaskOfWorkerAndDirector(){
        ArrayList<PieEntry> dataVal = new ArrayList<>();

        database.collection(Constants.KEY_COLLECTION_TASK)
                .get()
                .addOnCompleteListener(task -> {

                    // Biến đếm số nhiệm vụ đã hoàn thành và chưa hoàn thành
                    int countAmountCompleted = 0;
                    int countAmountUncompleted = 0;

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverIds = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);

                        for (String id : receiverIds){
                            if (id.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                boolean isCompleted = false;
                                List<String> receiverCompleted = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVERS_ID_COMPLETED);
                                for (String receiverId : receiverCompleted){
                                    if (receiverId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                        isCompleted = true;
                                        break;
                                    }
                                }

                                if (isCompleted){
                                    countAmountCompleted++;
                                } else {
                                    countAmountUncompleted++;
                                }
                                break;
                            }

                        }
                    }

                    textNumCompletedTask.setText(countAmountCompleted + "");
                    textNumUncompletedTask.setText(countAmountUncompleted + "");

                    dataVal.add(new PieEntry(countAmountCompleted, "Hoàn thành"));
                    dataVal.add(new PieEntry(countAmountUncompleted, "Chưa hoàn thành"));
                    pieDataSet = new PieDataSet(dataVal, "");

                    List<Integer> colors = new ArrayList<>();
                    colors.add(Color.rgb(0,129, 201));
                    colors.add(Color.rgb(239,163, 157));

                    pieDataSet.setColors(colors);

                    pieDataSet.setValueTextSize(18f);
                    pieDataSet.setValueTextColor(Color.WHITE);

                    PieData pieData = new PieData(pieDataSet);

                    pieChart.setDrawEntryLabels(false);
                    pieChart.getDescription().setText("Các khoản chi tiêu ");
                    pieChart.getDescription().setTextSize(16f);
                    pieChart.setUsePercentValues(true);
                    pieChart.setEntryLabelTextSize(18f);
                    pieChart.setCenterTextRadiusPercent(50);
                    pieChart.setHoleRadius(30);
                    pieChart.setTransparentCircleRadius(40);
                    pieChart.setTransparentCircleColor(Color.RED);
                    pieChart.setTransparentCircleAlpha(50);
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                    pieChart.getDescription().setEnabled(false);
                    pieChart.animateX(2000);

                });

    }

}