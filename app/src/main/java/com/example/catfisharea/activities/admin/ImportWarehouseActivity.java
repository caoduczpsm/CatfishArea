package com.example.catfisharea.activities.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.ActivityImportWarehouseBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ImportAdapter;
import com.example.catfisharea.listeners.ImportWarehouseListener;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ImportWarehouseActivity extends BaseActivity implements ImportWarehouseListener {
    private ActivityImportWarehouseBinding mBinding;
    private String encodedImage;
    private Calendar myCal;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String warehouseID;
    private String areaID;
    private List<Category> mList;
    private ImportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityImportWarehouseBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        warehouseID = getIntent().getStringExtra(Constants.KEY_WAREHOUSE_ID);
        areaID = getIntent().getStringExtra(Constants.KEY_AREA_ID);

        setListener();
    }

    @SuppressLint("SetTextI18n")
    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        myCal = Calendar.getInstance();
        mBinding.imageBack.setOnClickListener(view -> onBackPressed());
        mBinding.edtDate.setText(myCal.get(Calendar.DAY_OF_MONTH) + "/"
                + (myCal.get(Calendar.MONTH) + 1) + "/" + myCal.get(Calendar.YEAR));
        mBinding.imageImport.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Animatoo.animateSlideLeft(this);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        mBinding.edtDate.setOnClickListener(view -> openDatePicker());

        mList = new ArrayList<>();
        adapter = new ImportAdapter(this, mList, this);
        mBinding.recyclerViewImport.setAdapter(adapter);

        setDataRecyclerView();

        getNameWarehose();

        mBinding.saveBtn.setOnClickListener(view -> saveCategory());
    }

    private void getNameWarehose() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .document(warehouseID).get().addOnSuccessListener(nameDoc -> {
                   String name = nameDoc.getString(Constants.KEY_NAME);
                   mBinding.textName.setText(name);
                });
    }

    private void saveCategory() {
        List<Category> result = adapter.getResult();
        for (Category item : result) {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                    .document(warehouseID).collection(Constants.KEY_CATEGORY_OF_WAREHOUSE)
                    .document(item.getId());

            documentReference.get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> data = new HashMap<>();
                data.put(Constants.KEY_NAME, item.getName());
                data.put(Constants.KEY_UNIT, item.getUnit());
                data.put(Constants.KEY_EFFECT, item.getEffect());
                data.put(Constants.KEY_PRODUCER, item.getProducer());
                if (documentSnapshot.get(Constants.KEY_PRICE) != null) {
                    String priceOld = documentSnapshot.getString(Constants.KEY_PRICE);
                    String amountOld = documentSnapshot.getString(Constants.KEY_AMOUNT);
                    data.put(Constants.KEY_PRICE, priceCaculation(priceOld, amountOld, item.getPrice(), item.getAmount()));
//                    if (detail.containsKey(String.valueOf(item.getPrice()))) {
//                        detail.put(String.valueOf(item.getPrice()), item.getAmount() + Integer.parseInt(detail.get(String.valueOf(item.getPrice())).toString()));
//                    } else {
//                        detail.put(String.valueOf(item.getPrice()), item.getAmount());
//                    }
                } else {
                    data.put(Constants.KEY_PRICE, (item.getPrice()));
                }

//                data.put(Constants.KEY_DETAIL, detail);
                if (documentSnapshot.exists()) {
                    long amount = Long.parseLong(item.getAmount()) +
                            Long.parseLong(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_AMOUNT)));
                    data.put(Constants.KEY_AMOUNT, String.valueOf(amount));
                    documentReference.update(data).addOnSuccessListener(command -> {

                        Toast.makeText(this, "Nhập hàng thành công", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    });
                } else {
                    data.put(Constants.KEY_AMOUNT, String.valueOf(item.getAmount()));
                    documentReference.set(data).addOnSuccessListener(command -> {
                        Toast.makeText(this, "Nhập hàng thành công", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    });
                }
            });
        }
        saveHistory(result);
    }

    private String priceCaculation(String priceOld, String amountOld, String priceNew, String amountNew) {
        double totalOld = Integer.parseInt(amountOld) * Double.parseDouble(priceOld);
        double totalNew = Integer.parseInt(amountNew) * Double.parseDouble(priceNew);
        double price = (totalOld + totalNew) / (Integer.parseInt(amountOld) + Integer.parseInt(amountNew));
        return String.valueOf(price);
    }

    private void saveHistory(List<Category> result) {
        Map<String, Object> data = new HashMap<>();

        Timestamp timestamp = new Timestamp(myCal.getTime());
        data.put(Constants.KEY_TIMESTAMP, timestamp);
        data.put(Constants.KEY_WAREHOUSE_ID, warehouseID);
        data.put(Constants.KEY_AREA_ID, areaID);
        data.put(Constants.KEY_TOTAL_MONEY, Objects.requireNonNull(mBinding.edtMoney.getText()).toString());

        List<Map<String, Object>> product = new ArrayList<>();

        for (Category item: result) {
            Map<String, Object> productitem = new HashMap<>();
            productitem.put(Constants.KEY_NAME, item.getName() + " - " + item.getProducer());
            productitem.put(Constants.KEY_AMOUNT, item.getAmount());
            productitem.put(Constants.KEY_UNIT, item.getUnit());
            product.add(productitem);
        }
        data.put(Constants.KEY_MATERIALSLIST, product);

        database.collection(Constants.KEY_COLLECTION_WAREHOUSE_HISTORY).document().set(data);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setDataRecyclerView() {
        database.collection(Constants.KEY_COLLECTION_CATEGORY)
                .whereEqualTo(Constants.KEY_AREA_ID, areaID).get()
                .addOnSuccessListener(categoryQuery -> {
                    for (DocumentSnapshot doc : categoryQuery.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString(Constants.KEY_NAME);
                        String unit = doc.getString(Constants.KEY_UNIT);
                        String producer = doc.getString(Constants.KEY_PRODUCER);
                        String effect = doc.getString(Constants.KEY_EFFECT);

                        Category category = new Category(id, name);
                        category.setUnit(unit);
                        category.setProducer(producer);
                        category.setEffect(effect);
                        mList.add(category);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void openDatePicker() {
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);
            mBinding.edtDate.setText(day + "/" + (month + 1) + "/" + year);
        };

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mBinding.imageImport.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    @Override
    public void changeMoney(List<Category> list) {
        List<Category> result = adapter.getResult();
        long total = 0;
        boolean finish = true;
        for (Category item : result) {
            total += Long.parseLong(item.getAmount()) * Long.parseLong(item.getPrice());
            if ((item == null || item.getId() == null) || item.getId().isEmpty() &&
                    Objects.equals(item.getAmount(), "0") && Objects.equals(item.getPrice(), "0")) {
                finish = false;
            }
        }
        if (!finish) {
            mBinding.saveBtn.setClickable(false);
            mBinding.saveBtn.setEnabled(false);
        } else {
            mBinding.saveBtn.setClickable(true);
            mBinding.saveBtn.setEnabled(true);
        }
        mBinding.edtMoney.setText(DecimalHelper.formatText(total));
    }
}