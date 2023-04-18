package com.example.catfisharea.fragments.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentCreatePondBinding;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.SpinnerAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class CreatePondFragment extends Fragment implements PermissionsListener, OnMapReadyCallback, MultipleListener {
    private FragmentCreatePondBinding mBinding;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;
    private Projection projection;
    public double latitude;
    public double longitude;
    boolean available = false;
    private List<LatLng> arraylistoflatlng;
    private List<Polyline> polylineList;
    private Boolean Is_MAP_Moveable = false;
    private Polyline line;
    private Polygon polygon;
    private Boolean isZoomOut = false;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Area areaSelected;
    private Campus campusSelected;
    private Marker marker;
    private MultipleUserSelectionAdapter usersAdapter;
    private ArrayList<User> mUsers;
    private List<User> mUsersSelected;
    private Icon icon;
    private String action;
    private String idItem;
    private List<User> mUsersEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        mBinding = FragmentCreatePondBinding.inflate(inflater, container, false);
        mBinding.mapView.onCreate(savedInstanceState);
        mBinding.mapView.getMapAsync(this);
        preferenceManager = new PreferenceManager(getContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

        IconFactory iconFactory = IconFactory.getInstance(getContext());
        icon = iconFactory.fromResource(R.drawable.ic_pond_marker);

        action = getArguments().getString("action");

        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mBinding.mapView.onStart();
        setListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        if (mapboxMap == null) {
            Log.d("Mapboxmap null", "map is null");
        }
        mBinding.edtNameArea.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setDefaultUI();
                }
            }
        });

        mBinding.edtAcreage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setDefaultUI();
                }
            }
        });

        mUsers = new ArrayList<>();
        mUsersSelected = new ArrayList<>();
        mBinding.edtWorker.setOnClickListener(view -> {
            openPickUserDialog();
            setDefaultUI();
        });
        mBinding.centerLoc.setOnClickListener(view -> {
            setDefaultUI();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                    .zoom(18).build();
            mapboxMap.setCameraPosition(cameraPosition);
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);
        });

        mBinding.freeHandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Is_MAP_Moveable = !Is_MAP_Moveable;
            }
        });

        arraylistoflatlng = new ArrayList<>();
        polylineList = new ArrayList<>();

        mBinding.mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Is_MAP_Moveable) {
                    setDefaultUI();
                    float x = event.getX();
                    float y = event.getY();

                    int x_co = Math.round(x);
                    int y_co = Math.round(y);

                    projection = mapboxMap.getProjection();
                    PointF x_y_points = new PointF(x_co, y_co);

                    LatLng latLng = mapboxMap.getProjection().fromScreenLocation(x_y_points);
                    latitude = latLng.getLatitude();
                    longitude = latLng.getLongitude();
                    int eventaction = event.getAction();

                    if (available) {
                        mapboxMap.removeMarker(marker);
                        available = false;
                    }

                    switch (eventaction) {
                        case MotionEvent.ACTION_DOWN:
                            if (action.equals("edit")) {
                                if (mapboxMap.getMarkers().size() > 0)
                                    mapboxMap.removeMarker(mapboxMap.getMarkers().get(0));
                            }
                            // finger touches the screen
//                            arraylistoflatlng.add(new LatLng(latitude, longitude));
                            MarkerOptions options = new MarkerOptions();
                            options.setIcon(icon);
                            options.position(new LatLng(latitude, longitude));
                            marker = mapboxMap.addMarker(options);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            // finger leaves the screen
                            available = true;
                            break;
                    }
                }
                return Is_MAP_Moveable;
            }
        });

        mBinding.deleteBtn.setOnClickListener(view -> {
            setDefaultUI();
            if (available) {
                //clear the previous polygon first. Write code here
                if (marker != null) {
//                    fillColor = Color.argb(100, 20, 137, 238);
                    mapboxMap.removeMarker(marker);
                }
                available = false;
            }
        });
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.layoutMaps.getLayoutParams();
        mBinding.zoomOutBtn.setOnClickListener(view -> {
            if (!isZoomOut) {
                mBinding.layoutMaps.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mBinding.zoomOutBtn.setImageResource(R.drawable.baseline_zoom_in_map_24);
            } else {
                mBinding.layoutMaps.setLayoutParams(params);
                mBinding.zoomOutBtn.setImageResource(R.drawable.ic_zoom_out_map);
            }
            isZoomOut = !isZoomOut;
        });

        mBinding.saveBtnCreate.setOnClickListener(view -> {
            savePond();
        });

    }

    private void openPickUserDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_pick_user);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button save_btn = dialog.findViewById(R.id.btnSave);
        save_btn.setVisibility(View.VISIBLE);

        //ConstrainLayout trong dialog
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerPickUserDialog);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        usersAdapter = new MultipleUserSelectionAdapter(mUsers, this);
        recyclerView.setAdapter(usersAdapter);
        getUsers();
        no_btn.setOnClickListener(view -> dialog.dismiss());
        mUsersSelected.clear();
        save_btn.setOnClickListener(view -> {
            mUsersSelected.addAll(usersAdapter.getSelectedUser());
            String textUser = "";
            for (User user : mUsersSelected) {
                textUser += user.name + ", ";
            }
            mBinding.edtWorker.setText(textUser);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void getUsers() {
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mUsers.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String pondId = doc.getString(Constants.KEY_POND_ID);
                        if (pondId == null || pondId.isEmpty()) {
                            User user = new User();
                            user.name = doc.getString(Constants.KEY_NAME);
                            user.phone = doc.getString(Constants.KEY_PHONE);
                            user.image = doc.getString(Constants.KEY_IMAGE);
                            user.position = doc.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = doc.getString(Constants.KEY_FCM_TOKEN);
                            Log.d("CreatePond", "getUser" + mUsersSelected.size());
                            if (mUsersSelected.size() > 0 && mUsersSelected.contains(user))
                                user.isSelected = true;
                            user.id = doc.getId();
                            mUsers.add(user);

                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                });

    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(getContext());
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

    private void savePond() {
        boolean isFinish = true;
        String name = mBinding.edtNameArea.getText().toString();
        String acreage = mBinding.edtAcreage.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Hãy nhập tên ao", Toast.LENGTH_SHORT).show();
//            mBinding.edtNameArea.setHint("Hãy nhập tên ao");
//
            isFinish = false;
        }
        if (areaSelected == null) {
            Toast.makeText(getContext(), "Hãy chọn vùng", Toast.LENGTH_SHORT).show();
//            mBinding.spinnerArea.setBackgroundResource(R.drawable.edit_text_error);
            isFinish = false;
        }
        if (campusSelected == null) {
            Toast.makeText(getContext(), "Hãy chọn khu", Toast.LENGTH_SHORT).show();
//            mBinding.spinnerCampus.setBackgroundResource(R.drawable.edit_text_error);
            isFinish = false;
        }
        if (mUsersSelected.size() == 0) {
            Toast.makeText(getContext(), "Hãy chọn công nhân", Toast.LENGTH_SHORT).show();
//            mBinding.edtWorker.setBackgroundResource(R.drawable.edit_text_error);
//            mBinding.edtNameArea.setHint("Hãy chọn công nhân");
//            mBinding.edtNameArea.setHintTextColor(getContext().getResources().getColor(R.color.red));
            isFinish = false;
        }
        if (marker == null) {
            Toast.makeText(getContext(), "Hãy vẽ lên bản đồ", Toast.LENGTH_SHORT).show();
//            mBinding.layoutMaps.setBackgroundResource(R.drawable.edit_text_error);
            isFinish = false;
        }
        if (acreage.isEmpty()) {
            Toast.makeText(getContext(), "Hãy nhập diện tích", Toast.LENGTH_SHORT).show();
//            mBinding.edtAcreage.setHint("Hãy nhập diện tích ao");
//            mBinding.edtAcreage.setHintTextColor(getContext().getResources().getColor(R.color.red));
//            mBinding.edtAcreage.setBackgroundResource(R.drawable.edit_text_error);
            isFinish = false;
        }
        if (isFinish) {
            if (action.equals("create")) {
                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                        .whereEqualTo(Constants.KEY_NAME, name).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> value = new HashMap<>();
                                value.put(Constants.KEY_NAME, name);
                                value.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                                value.put(Constants.KEY_CAMPUS_ID, campusSelected.getId());
                                GeoPoint geo = new GeoPoint(latitude, longitude);
                                value.put(Constants.KEY_MAP, geo);
                                value.put(Constants.KEY_ACREAGE, acreage);
                                HashMap<String, Object> parameters = new HashMap<>();
                                parameters.put(Constants.KEY_SPECIFICATION_PH, "0");
                                parameters.put(Constants.KEY_SPECIFICATION_SALINITY, "0");
                                parameters.put(Constants.KEY_SPECIFICATION_ALKALINITY, "0");
                                parameters.put(Constants.KEY_SPECIFICATION_TEMPERATE, "0");
                                parameters.put(Constants.KEY_SPECIFICATION_H2S, "0");
                                parameters.put(Constants.KEY_SPECIFICATION_NH3, "0");

                                value.put(Constants.KEY_SPECIFICATIONS_MEASURED, parameters);
                                value.put(Constants.KEY_NUM_OF_FEEDING_LIST, Arrays.asList("0", "0", "0", "0", "0", "0", "0", "0"));
                                value.put(Constants.KEY_AMOUNT_FED, Arrays.asList("0", "0", "0", "0", "0", "0", "0", "0"));
                                value.put(Constants.KEY_SPECIFICATIONS_TO_MEASURE, Arrays.asList("0", "0", "0", "0", "0"));

                                DocumentReference doc = database.collection(Constants.KEY_COLLECTION_POND).document();
                                doc.set(value).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> pondId = new HashMap<>();
                                        pondId.put(Constants.KEY_POND_ID, doc.getId());
                                        pondId.put(Constants.KEY_CAMPUS_ID, campusSelected.getId());
                                        pondId.put(Constants.KEY_AREA_ID, areaSelected.getId());
                                        if (mUsersSelected.size() <= 0) Log.d("CreatePond", "size 0");
                                        for (User user : mUsersSelected) {
                                            database.collection(Constants.KEY_COLLECTION_USER).document(user.id)
                                                    .update(pondId);
                                        }
                                        doc.update(Constants.KEY_AREA_ID, areaSelected.getId());


                                        Toast.makeText(getContext(), "Thêm ao mới thành công", Toast.LENGTH_SHORT).show();
                                        mBinding.edtNameArea.setText("");
                                        mBinding.edtAcreage.setText("");
                                        mUsersSelected.clear();
                                        getPonds(campusSelected.getId());
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Tên vùng đã tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                        .whereEqualTo(Constants.KEY_NAME, name).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.getDocuments().isEmpty()
                                    || queryDocumentSnapshots.getDocuments().get(0).getId().equals(idItem)) {
                                HashMap<String, Object> value = new HashMap<>();
                                value.put(Constants.KEY_NAME, name);

                                GeoPoint geo = new GeoPoint(latitude, longitude);
                                value.put(Constants.KEY_MAP, geo);
                                value.put(Constants.KEY_ACREAGE, acreage);
                                DocumentReference doc = database.collection(Constants.KEY_COLLECTION_POND).document(idItem);
                                doc.update(value).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> pondId = new HashMap<>();
                                        pondId.put(Constants.KEY_POND_ID, idItem);

                                        for (User user : mUsersSelected) {
                                            database.collection(Constants.KEY_COLLECTION_USER).document(user.id)
                                                    .update(pondId);
                                        }
                                        Toast.makeText(getContext(), "Thêm ao mới thành công", Toast.LENGTH_SHORT).show();
                                        getPonds(campusSelected.getId());
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Tên vùng đã tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        }
    }

    @SuppressLint("NewApi")
    private void getPonds(String campusId) {
        mapboxMap.getMarkers().forEach(it -> {
            mapboxMap.removeMarker(it);
        });
        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        GeoPoint geo = (GeoPoint) doc.get(Constants.KEY_MAP);
                        MarkerOptions options = new MarkerOptions();
                        options.setIcon(icon);
                        options.position(new LatLng(geo.getLatitude(), geo.getLongitude()));
                        mapboxMap.addMarker(options);
                    }
                });
    }

    private void setDataAreaSpinner() {
        List<RegionModel> mArae = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this.getContext(), R.layout.layout_spinner_item, mArae);
        mBinding.spinnerArea.setAdapter(spinnerAdapter);
        if (action.equals("create")) {
            database.collection(Constants.KEY_COLLECTION_AREA)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String name = doc.getString(Constants.KEY_NAME);
                            ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                            Area area = new Area(id, name, geoList);
                            mArae.add(area);
                        }
                        spinnerAdapter.notifyDataSetChanged();
                    });
            mBinding.spinnerArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Area area = (Area) parent.getItemAtPosition(position);
//                boundingBox.clear();
//                boundingBoxList.clear();
                    areaSelected = area;
                    List<LatLng> listpoint = new ArrayList<>();
                    listpoint = area.getListLatLng();
                    listpoint.add(listpoint.get(0));
                    LatLng center = getPolygonCenterPoint((ArrayList<LatLng>) listpoint);

//                for (LatLng lt: listpoint){
//                    boundingBox.add(Point.fromLngLat(lt.getLatitude(), lt.getLongitude()));
//                }
                    mapboxMap.getPolygons().forEach(plg -> {
                        mapboxMap.removePolygon(plg);
                    });

                    mapboxMap.getPolylines().forEach(plg -> {
                        mapboxMap.removePolyline(plg);
                    });

//                boundingBoxList.add(boundingBox);

                    PolylineOptions rectOptions = new PolylineOptions()
                            .addAll(listpoint)
                            .color(Color.BLACK)
                            .width(3);
                    Polyline line = mapboxMap.addPolyline(rectOptions);

                    CameraPosition areaPosition = new CameraPosition.Builder()
                            .target(center)
                            .zoom(19).build();
                    mapboxMap.setCameraPosition(areaPosition);
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);
                    setDataCampusSpinner();
                }
            });
        } else {
            database.collection(Constants.KEY_COLLECTION_POND).document(idItem)
                    .get().addOnSuccessListener(docPond -> {
                        String idArea = docPond.getString(Constants.KEY_AREA_ID);
                        database.collection(Constants.KEY_COLLECTION_AREA).document(idArea)
                                .get().addOnSuccessListener(docArea -> {
                                    String id = docArea.getId();
                                    String name = docArea.getString(Constants.KEY_NAME);
                                    ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) docArea.get(Constants.KEY_MAP);
                                    Area area = new Area(id, name, geoList);
                                    areaSelected = area;
                                    setDataCampusSpinner();
                                    mArae.add(area);
                                    spinnerAdapter.notifyDataSetChanged();
                                    mBinding.spinnerArea.setListSelection(1);
                                    mBinding.spinnerArea.setText(name);

                                    for (GeoPoint point : geoList) {
                                        arraylistoflatlng.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                    }

                                    arraylistoflatlng.add(arraylistoflatlng.get(0));

                                    PolylineOptions rectOptions = new PolylineOptions()
                                            .addAll(arraylistoflatlng)
                                            .color(Color.BLACK)
                                            .width(3);
                                    Polyline line = mapboxMap.addPolyline(rectOptions);

                                    LatLng center = getPolygonCenterPoint((ArrayList<LatLng>) arraylistoflatlng);
                                    CameraPosition areaPosition = new CameraPosition.Builder()
                                            .target(center)
                                            .zoom(19).build();
                                    mapboxMap.setCameraPosition(areaPosition);
                                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);


                                });
                    });
        }
    }

    //    lấy dữ liệu cho spinner khu và vẽ khu đã chọn lên bản đồ
    private void setDataCampusSpinner() {
        setDefaultUI();
        ArrayList<RegionModel> mCampus = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this.getContext(), R.layout.layout_spinner_item, mCampus);
        mBinding.spinnerCampus.setAdapter(spinnerAdapter);
        if (action.equals("create")) {
            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .whereEqualTo(Constants.KEY_AREA_ID, areaSelected.getId())
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String areaid = areaSelected.getId();
                            String name = doc.getString(Constants.KEY_NAME);
                            ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                            Campus campus = new Campus(id, name, geoList, areaid);
                            mCampus.add(campus);
                        }
                        spinnerAdapter.notifyDataSetChanged();
                    });

            mBinding.spinnerCampus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setDefaultUI();
                    Campus campus = (Campus) parent.getItemAtPosition(position);
                    campusSelected = campus;
                    List<LatLng> listpoint = new ArrayList<>();
                    listpoint = campus.getListLatLng();
                    listpoint.add(listpoint.get(0));
                    LatLng center = getPolygonCenterPoint((ArrayList<LatLng>) listpoint);
                    if (!campus.getId().isEmpty()) {
                        getPonds(campus.getId());
                    }

                    PolylineOptions rectOptions = new PolylineOptions()
                            .addAll(listpoint)
                            .color(Color.BLACK)
                            .width(3);
                    if (mapboxMap.getPolylines().size() > 1) {
                        mapboxMap.removePolyline(mapboxMap.getPolylines().get(1));
                    }
                    Polyline line = mapboxMap.addPolyline(rectOptions);

                    CameraPosition areaPosition = new CameraPosition.Builder()
                            .target(center)
                            .zoom(20).build();
                    mapboxMap.setCameraPosition(areaPosition);
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);

                }
            });
        } else {
            database.collection(Constants.KEY_COLLECTION_POND).document(idItem)
                    .get().addOnSuccessListener(documentSnap -> {
                        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                                .document(documentSnap.getString(Constants.KEY_CAMPUS_ID))
                                .get().addOnSuccessListener(documentSnapshot -> {
                                    String id = documentSnapshot.getId();
                                    String name = documentSnapshot.getString(Constants.KEY_NAME);
                                    ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) documentSnapshot.get(Constants.KEY_MAP);
                                    Campus campus = new Campus(id, name, geoList, "");
                                    campusSelected = campus;
                                    mCampus.add(campus);
                                    spinnerAdapter.notifyDataSetChanged();
                                    mBinding.spinnerCampus.setListSelection(0);
                                    mBinding.spinnerCampus.setText(name);

                                    List<LatLng> points = new ArrayList<>();
                                    for (GeoPoint point : geoList) {
                                        points.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                    }

                                    points.add(points.get(0));

                                    PolylineOptions rectOptions = new PolylineOptions()
                                            .addAll(points)
                                            .color(Color.BLACK)
                                            .width(3);
                                    Polyline line = mapboxMap.addPolyline(rectOptions);
                                });
                    });
        }

    }

    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList) {
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng lt : polygonPointsList) {
            builder.include(lt);
        }
        LatLngBounds bounds = builder.build();
        centerLatLng = bounds.getCenter();
        return centerLatLng;
    }

    private void deletePolygon() {
        if (polygon != null) {
            mapboxMap.removePolyline(line);
            mapboxMap.removePolygon(polygon);
            polygon.remove();
            for (Polyline l : polylineList) {
                l.remove();
            }
            polylineList.clear();
            arraylistoflatlng.clear();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mBinding.mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mBinding.mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.mapView.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if (action.equals("create")) {

        } else if (action.equals("edit")) {
            mBinding.toolbarManagePond.setTitle("Chỉnh sửa ao");
            idItem = getArguments().getString("idItem");
            setData(idItem);
        }
        setDataAreaSpinner();

        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableComponent(style);
            }
        });
    }

    private void enableComponent(Style loadedMapStyle) {
        try {
            if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
                locationComponent = mapboxMap.getLocationComponent();

                locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build()
                );

                if (ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationComponent.setLocationComponentEnabled(true);

                locationComponent.setCameraMode(CameraMode.TRACKING);

                locationComponent.setRenderMode(RenderMode.COMPASS);

            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(getActivity());
            }
        } catch (Exception e) {
            Log.e("ERR_LOAD_MAP", e.getMessage());
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> list) {

    }

    @Override
    public void onPermissionResult(boolean b) {
        if (b) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableComponent(style);
                }
            });
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void setData(String idPond) {
        mUsersEdit = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_POND).document(idPond)
                .get().addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString(Constants.KEY_NAME);
                    GeoPoint geo = (GeoPoint) documentSnapshot.get(Constants.KEY_MAP);
                    String acreage = documentSnapshot.getString(Constants.KEY_ACREAGE);
                    mBinding.edtNameArea.setText(name);
                    mBinding.edtAcreage.setText(acreage);
                    latitude = geo.getLatitude();
                    longitude = geo.getLongitude();
//                  Vẽ ao lên bản đồ
                    MarkerOptions options = new MarkerOptions();
                    options.setIcon(icon);
                    options.position(new LatLng(geo.getLatitude(), geo.getLongitude()));
                    mapboxMap.addMarker(options);

                });
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, idPond).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString(Constants.KEY_NAME);
                        String avt = doc.getString(Constants.KEY_IMAGE);

                        User user = new User();
                        user.name = name;
                        user.id = queryDocumentSnapshots.getDocuments().get(0).getId();
                        mUsersEdit.add(user);
                        mUsersSelected = mUsersEdit;
                        if (mBinding.edtWorker.getText().toString().isEmpty()) {
                            mBinding.edtWorker.setText(name);
                        } else {
                            mBinding.edtWorker.setText(mBinding.edtWorker.getText() + ", " + name);
                        }
                    }
                });
    }

    private void setDefaultUI() {
//        mBinding.edtNameArea.setHintTextColor(getContext().getResources().getColor(R.color.textGray));
//        mBinding.edtNameArea.setBackgroundResource(R.drawable.background_edit_text);
//
//        mBinding.edtAcreage.setHintTextColor(getContext().getResources().getColor(R.color.textGray));
//        mBinding.edtAcreage.setBackgroundResource(R.drawable.background_edit_text);
//
//        mBinding.edtWorker.setHintTextColor(getContext().getResources().getColor(R.color.textGray));
//        mBinding.edtWorker.setBackgroundResource(R.drawable.background_edit_text);
//        mBinding.spinnerCampus.setBackgroundResource(R.drawable.background_edit_text);
//        mBinding.spinnerArea.setBackgroundResource(R.drawable.background_edit_text);
//        mBinding.layoutMaps.setBackgroundResource(R.drawable.background_edit_text);
    }
}