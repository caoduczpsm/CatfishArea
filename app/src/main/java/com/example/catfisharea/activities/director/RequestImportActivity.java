package com.example.catfisharea.activities.director;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityRequestImportBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.RequestImportItem;
import com.example.catfisharea.listeners.MaterialsListener;
import com.example.catfisharea.models.Materials;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        mBinding.toolbarRequestImport.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.newItem.setOnClickListener(view -> openDialog());
        mBinding.sendReportBtn.setOnClickListener(view -> sendRequest());

    }

    private void sendRequest() {
        if (preferenceManager.getString(Constants.KEY_CAMPUS_ID) == null) return;
        String name = mBinding.toolbarRequestImport.getTitle().toString();
        DateTime dateTime = DateTime.now();
        String dateCreated = dateTime.getDayOfMonth() + "/" + dateTime.getMonthOfYear() + "/" + dateTime.getYear();
        Map<String, ArrayList<String>> map = new HashMap<>();
        if (materialsList.isEmpty()) {
            Toast.makeText(this, "Nhập danh sách hàng cần nhập", Toast.LENGTH_SHORT).show();
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
                        database.collection(Constants.KEY_COLLECTION_REQUEST).document().set(value).addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Gửi thành công", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        });
                    });
        }


    }

    @SuppressLint("NotifyDataSetChanged")
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
        AppCompatButton btnAdd = dialog.findViewById(R.id.btnAdd);
        AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);
        TextInputEditText note = dialog.findViewById(R.id.edtNote);


        Materials materials = new Materials("", 0);
        ArrayList<String> arrayItem = new ArrayList<>();
        arrayItem.add("Thức ăn");
        arrayItem.add("Thuốc");
        arrayItem.add("Thiết bị");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, arrayItem);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemClickListener((parent, view, position, id) -> materials.setName(arrayItem.get(position)));

        btnAdd.setOnClickListener(view -> {
            int amount = 0;
            if (!Objects.requireNonNull(editText.getText()).toString().isEmpty()) {
                amount = Integer.parseInt(editText.getText().toString().trim());
            }
            materials.setDecription(Objects.requireNonNull(note.getText()).toString().trim());

            if (amount > 0) {
                materials.setAmount(amount);
            }
            if (!materials.getName().isEmpty() && amount > 0) {
                materialsList.add(materials);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
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

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayItem);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(arrayAdapter.getPosition(mtr.getName()));
        editText.setText(mtr.getAmount() + "");
        if (!mtr.getDecription().isEmpty()) {
            note.setText(mtr.getDecription());
        }

        spinner.setOnItemClickListener((parent, view, position, id) -> materials.setName(arrayItem.get(position)));

        btn.setOnClickListener(view -> {
            int amount = 0;
            if (!Objects.requireNonNull(editText.getText()).toString().isEmpty()) {
                amount = Integer.parseInt(editText.getText().toString().trim());
            }
            materials.setDecription(Objects.requireNonNull(note.getText()).toString().trim());

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

    @SuppressLint("NotifyDataSetChanged")
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