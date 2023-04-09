package com.example.catfisharea.activities.director;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityRequestImportBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.RequestImportItem;
import com.example.catfisharea.listeners.MaterialsListener;
import com.example.catfisharea.models.Materials;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestImportActivity extends BaseActivity implements MaterialsListener {
    private ActivityRequestImportBinding mBinding;
    private List<Materials> materialsList;
    private RequestImportItem adapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRequestImportBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(RequestImportActivity.this);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        materialsList = new ArrayList<>();
        adapter = new RequestImportItem(this, materialsList, this);
        mBinding.recyclerViewImport.setAdapter(adapter);

        mBinding.toolbarRequestImport.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.newItem.setOnClickListener(view -> {
            openDialog();
        });
        mBinding.sendReportBtn.setOnClickListener(view -> {
            sendRequest();
        });

    }

    private void sendRequest() {
//        materialsList = adapter.getMaterials();
//        adapter.notifyDataSetChanged();
        String name = mBinding.toolbarRequestImport.getTitle().toString();
        DateTime dateTime = DateTime.now();
        String dateCreated = dateTime.getDayOfMonth() + "/" + dateTime.getMonthOfYear() + "/" + dateTime.getYear();
        Map<String, ArrayList<String>> map = new HashMap<>();
        if (materialsList.isEmpty()) {
            return;
        }
        for (Materials mtr : materialsList) {
            ArrayList<String> item = new ArrayList<>();
            item.add(mtr.getAmount() + "");
            item.add(mtr.getDecription());
            map.put(mtr.getName(), item);
        }
        Map<String, Object> value = new HashMap<>();
        value.put(Constants.KEY_NAME, name);
        value.put(Constants.KEY_DATE_CREATED_REQUEST, dateCreated);
        value.put(Constants.KEY_REQUESTER, preferenceManager.getString(Constants.KEY_USER_ID));
        value.put(Constants.KEY_MATERIALSLIST, map);
        value.put(Constants.KEY_STATUS_TASK, Constants.KEY_PENDING);
        value.put(Constants.KEY_TYPE_REQUEST, Constants.KEY_IMPORT_REQUEST);
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            database.collection(Constants.KEY_COLLECTION_CAMPUS).document(preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                    .get().addOnSuccessListener(documentSnapshot -> {
                        String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                        value.put(Constants.KEY_RECEIVER_ID, areaId);
                        database.collection(Constants.KEY_COLLECTION_REQUEST).document().set(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                finish();
                            }
                        });
                    });
        }


    }

    private void openDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_add_request);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        AutoCompleteTextView spinner = dialog.findViewById(R.id.nameItem);
        TextInputEditText editText = dialog.findViewById(R.id.amountItem);
        Button btn = dialog.findViewById(R.id.btnAdd);
        TextInputEditText note = dialog.findViewById(R.id.edtNote);


        Materials materials = new Materials("", 0);
        ArrayList<String> arrayItem = new ArrayList<>();
        arrayItem.add("Thức ăn");
        arrayItem.add("Thuốc");
        arrayItem.add("Thiết bị");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayItem);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        String item;
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                materials.setName(arrayItem.get(position));
                Log.d("nhaphang", arrayItem.get(position));
            }
        });

        btn.setOnClickListener(view -> {
            int amount = 0;
            if (!editText.getText().toString().isEmpty()) {
                amount = Integer.parseInt(editText.getText().toString().trim());
            }
            materials.setDecription(note.getText().toString().trim());

            if (amount > 0) {
                materials.setAmount(amount);
            }
            Log.d("nhaphang", materials.getName());
            if (!materials.getName().isEmpty() && amount > 0) {
                materialsList.add(materials);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

        });
        dialog.show();
    }

    private void openDialog(Materials mtr) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_add_request);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        AutoCompleteTextView spinner = dialog.findViewById(R.id.nameItem);
        TextInputEditText editText = dialog.findViewById(R.id.amountItem);
        Button btn = dialog.findViewById(R.id.btnAdd);
        TextInputEditText note = dialog.findViewById(R.id.edtNote);


        Materials materials = new Materials("", 0);
        ArrayList<String> arrayItem = new ArrayList<>();
        arrayItem.add("Thức ăn");
        arrayItem.add("Thuốc");
        arrayItem.add("Thiết bị");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayItem);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(arrayAdapter.getPosition(mtr.getName()));
        editText.setText(mtr.getAmount() + "");
        if (!mtr.getDecription().isEmpty()) {
            note.setText(mtr.getDecription());
        }

        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                materials.setName(arrayItem.get(position));
                Log.d("nhaphang", arrayItem.get(position));
            }
        });

        btn.setOnClickListener(view -> {
            int amount = 0;
            if (!editText.getText().toString().isEmpty()) {
                amount = Integer.parseInt(editText.getText().toString().trim());
            }
            materials.setDecription(note.getText().toString().trim());

            if (amount > 0) {
                materials.setAmount(amount);
            }

            if (!materials.getName().isEmpty() && amount > 0) {
                materialsList.add(materials);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

        });
        dialog.show();
    }

    @Override
    public void delete(Materials mtr) {
        materialsList.remove(mtr);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void edit(Materials mtr) {
        openDialog(mtr);
    }
}