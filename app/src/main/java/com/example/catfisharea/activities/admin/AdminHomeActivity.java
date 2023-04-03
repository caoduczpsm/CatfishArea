package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.catfisharea.R;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.databinding.ActivityAdminHomeBinding;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends BaseActivity {

    private ActivityAdminHomeBinding mBinding;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setListener();
    }

    private void setListener() {
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes);
        mBinding.recyclerViewAdminHome.setAdapter(homeAdapter);
        ItemHome item1 = new ItemHome();
        Campus re1 = new Campus("1", "Khu 01", null, "1");
        item1.setRegionModel(re1);

        Pond p1 = new Pond("1", "Ao 01", null, "1", "1000");
        Pond p2 = new Pond("2", "Ao 02", null, "1", "1000");
        Pond p3 = new Pond("3", "Ao 03", null, "1", "1000");
        List<RegionModel> l1 = new ArrayList<>();
        l1.add(p1);
        l1.add(p2);
        l1.add(p3);

        item1.setReginonList(l1);
        itemHomes.add(item1);
        itemHomes.add(item1);
        itemHomes.add(item1);
        homeAdapter.notifyDataSetChanged();
    }


}