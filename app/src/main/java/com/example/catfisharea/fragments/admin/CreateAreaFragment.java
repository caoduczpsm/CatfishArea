package com.example.catfisharea.fragments.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentCreateBinding;
import com.example.catfisharea.adapter.UserPickerAdapter;
import com.example.catfisharea.listeners.PickUserListener;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
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
import com.mapbox.turf.TurfJoins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateAreaFragment extends Fragment implements PermissionsListener, OnMapReadyCallback, PickUserListener {

    private FragmentCreateBinding mBinding;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;
    Projection projection;
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
    private UserPickerAdapter mAdapter;
    private ArrayList<User> mUsers;
    private Dialog dialog;
    private User magager;
    private String action;
    private String idItem;
    private User mUserEdit;
    private int fillColor = Color.argb(100, 20, 137, 238);
    private boolean isStarted = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        mBinding = FragmentCreateBinding.inflate(inflater, container, false);
//        View view = inflater.inflate(R.layout.fragment_create, container, false);
        mBinding.mapView.onCreate(savedInstanceState);
        mBinding.mapView.getMapAsync(this);
//        setListener();
        preferenceManager = new PreferenceManager(getContext());
        mBinding.toolbarManageArea.setNavigationOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        //FireStore
        database = FirebaseFirestore.getInstance();
        action = getArguments().getString("action");

        return mBinding.getRoot();
    }

    private void getAraes() {
        database.collection(Constants.KEY_COLLECTION_AREA)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString(Constants.KEY_NAME);
                        ArrayList<GeoPoint> geo = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                        assert geo != null;

                        List<LatLng> listpoint = new ArrayList<>();
                        List<Point> boundingBox = new ArrayList<>();
                        for (GeoPoint point : geo) {
                            listpoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            boundingBox.add(Point.fromLngLat(point.getLatitude(), point.getLongitude()));
                        }

                        List<List<Point>> boundingBoxList = new ArrayList<>();
                        boundingBoxList.add(boundingBox);
                        if (idItem != null && idItem.equals(doc.getId())) {
                            polygon = mapboxMap.addPolygon(new PolygonOptions()
                                    .addAll(listpoint)
                                    .fillColor(Color.argb(100, 20, 137, 238)));
                        } else {
                            Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
                                    .addAll(listpoint)
                                    .fillColor(Color.argb(100, 255, 80, 80)));
                        }

                        listpoint.add(listpoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listpoint)
                                .color(Color.BLACK)
                                .width(3);

                        if (idItem != null && idItem.equals(doc.getId())) {
                            line = mapboxMap.addPolyline(rectOptions);
                        } else {
                            Polyline line = mapboxMap.addPolyline(rectOptions);

                        }

//                        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
//                            @Override
//                            public boolean onMapClick(@NonNull LatLng point) {
//                                if (TurfJoins.inside(Point.fromLngLat(point.getLatitude(), point.getLongitude()),
//                                        com.mapbox.geojson.Polygon.fromLngLats(boundingBoxList))) {
//                                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
//                                }
//                                return false;
//                            }
//                        });
                    }

                });
        drawCampus();
    }

    private boolean checkInside(Point point) {
        List<Point> boundingBox = new ArrayList<>();
        List<List<Point>> boundingBoxList = new ArrayList<>();

        if (!mapboxMap.getPolygons().isEmpty()) {
            for (Polygon polygon : mapboxMap.getPolygons()) {
                boundingBox.clear();
                boundingBoxList.clear();
                for (LatLng latLng : polygon.getPoints()) {
                    boundingBox.add(Point.fromLngLat(latLng.getLatitude(), latLng.getLongitude()));
                }
                boundingBoxList.add(boundingBox);
                if (TurfJoins.inside(point, com.mapbox.geojson.Polygon.fromLngLats(boundingBoxList))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkBound() {
        List<Point> boundingBox = new ArrayList<>();
        List<List<Point>> boundingBoxList = new ArrayList<>();
        for (LatLng latLng : polygon.getPoints()) {
            boundingBox.add(Point.fromLngLat(latLng.getLatitude(), latLng.getLongitude()));

        }
        boundingBoxList.add(boundingBox);

        AtomicBoolean isBound = new AtomicBoolean();
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, idItem)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        ArrayList<GeoPoint> geoPoints = (ArrayList<GeoPoint>) documentSnapshot.get(Constants.KEY_MAP);

                        Point point = Point.fromLngLat(geoPoints.get(0).getLatitude(), geoPoints.get(0).getLongitude());

                        if (!TurfJoins.inside(point, com.mapbox.geojson.Polygon.fromLngLats(boundingBoxList))) {

                            isBound.set(false);
                        }
                    }
                });
        return isBound.get();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        mBinding.toolbarManageArea.setNavigationOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        mBinding.layoutManager.setOnClickListener(view -> {
            openPickUserDialog();
        });

        mBinding.centerLoc.setOnClickListener(view -> {
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new
//                            LatLng(10.301464, 105.808371))
//                    .zoom(13).build();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                    .zoom(13).build();
            mapboxMap.setCameraPosition(cameraPosition);
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);
        });

        mBinding.freeHandBtn.setOnClickListener(v -> {
            Is_MAP_Moveable = !Is_MAP_Moveable;
            if (Is_MAP_Moveable) {
                mBinding.freeHandBtn.setImageResource(R.drawable.baseline_done_24);
            } else {
                mBinding.freeHandBtn.setImageResource(R.drawable.baseline_draw_24);
            }
        });

        arraylistoflatlng = new ArrayList<>();
        polylineList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mAdapter = new UserPickerAdapter(mUsers, this);

        mBinding.mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Is_MAP_Moveable) {
                    if (action.equals("edit") && !isStarted) {
                        if (mapboxMap.getPolygons().size() > 0 && mapboxMap.getPolylines().size() > 0) {
                            mapboxMap.removePolyline(line);
                            mapboxMap.removePolygon(polygon);
                            polygon.remove();
                            line.remove();
                        }
                        isStarted = true;
                    }
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

                    switch (eventaction) {
                        case MotionEvent.ACTION_DOWN:
                            // finger touches the screen
                            arraylistoflatlng.add(new LatLng(latitude, longitude));
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // finger moves on the screen
                            arraylistoflatlng.add(new LatLng(latitude, longitude));
                            PolylineOptions rectOptions = new PolylineOptions()
                                    .addAll(arraylistoflatlng)
                                    .color(Color.BLACK)
                                    .width(5);
                            line = mapboxMap.addPolyline(rectOptions);
                            polylineList.add(line);
//                        line.setJointType(JointType.ROUND);

                            if (checkInside(Point.fromLngLat(latitude, longitude))) {
                                fillColor = Color.argb(100, 255, 80, 80);
                            }

                            break;
                        case MotionEvent.ACTION_UP:
                            // finger leaves the screen
                            if (polygon != null) {
                                polygon.remove();
                            }

                            polygon = mapboxMap.addPolygon(new PolygonOptions()
                                    .addAll(arraylistoflatlng)
                                    .fillColor(fillColor));
                            available = true;
                            break;
                    }
                }
                return Is_MAP_Moveable;
            }
        });

        mBinding.deleteBtn.setOnClickListener(view -> {
            if (available) {
                //clear the previous polygon first. Write code here
                if (polygon != null) {
                    fillColor = Color.argb(100, 20, 137, 238);
                    if (mapboxMap != null) {
                        deletePolygon();
                    }
                }
                available = false;
            }
        });
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.layoutMaps.getLayoutParams();

        mBinding.zoomOutBtn.setOnClickListener(view -> {
            if (!isZoomOut) {
                mBinding.layoutMaps.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mBinding.zoomOutBtn.setImageResource(R.drawable.baseline_zoom_in_map_24);
                mBinding.saveBtnCreate.setVisibility(View.INVISIBLE);
                mBinding.cardInfo.setVisibility(View.GONE);
            } else {
                mBinding.layoutMaps.setLayoutParams(params);
                mBinding.zoomOutBtn.setImageResource(R.drawable.ic_zoom_out_map);
                mBinding.saveBtnCreate.setVisibility(View.VISIBLE);
                mBinding.cardInfo.setVisibility(View.VISIBLE);
            }
            isZoomOut = !isZoomOut;
        });
        mBinding.saveBtnCreate.setOnClickListener(view -> {
            saveArea();
        });
    }

    private void drawCampus() {
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        ArrayList<GeoPoint> geo = (ArrayList<GeoPoint>) documentSnapshot.get(Constants.KEY_MAP);
                        assert geo != null;
                        List<LatLng> listpoint = new ArrayList<>();
                        for (GeoPoint point : geo) {
                            listpoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }

                        Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
                                .addAll(listpoint)
                                .fillColor(Color.argb(100, 255, 80, 80)));

                        listpoint.add(listpoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listpoint)
                                .color(Color.BLACK)
                                .width(3);
                        Polyline line = mapboxMap.addPolyline(rectOptions);
                    }
                });
    }


    private void deletePolygon() {
        if (polygon != null) {
            if (line != null) {
                mapboxMap.removePolyline(line);
                line.remove();
            }

            mapboxMap.removePolygon(polygon);
            polygon.remove();
            if (polylineList != null && polylineList.size() > 0){
                for (Polyline l : polylineList) {
                    l.remove();
                }
            }

            polylineList.clear();
            arraylistoflatlng.clear();
        }
    }


    //  Lưu lại vùng đã tạo
    private void saveArea() {
        String name = mBinding.edtNameArea.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Nhập tên vùng", Toast.LENGTH_SHORT).show();
        } else if (magager == null) {
            Toast.makeText(getContext(), "Chọn trưởng vùng", Toast.LENGTH_SHORT).show();
        } else if (Is_MAP_Moveable || arraylistoflatlng.isEmpty() || polygon == null || polygon.getPoints().isEmpty()) {
            Toast.makeText(getContext(), "Vẽ vùng trên bản đồ", Toast.LENGTH_SHORT).show();
        } else {
//            Kiểm tra tên vùng đã tồn tại chưa
            if (action.equals("create")) {
                database.collection(Constants.KEY_COLLECTION_AREA)
                        .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                        .whereEqualTo(Constants.KEY_NAME, name).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> value = new HashMap<>();
                                ArrayList<GeoPoint> geoList = new ArrayList<>();
                                value.put(Constants.KEY_NAME, name);
                                value.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                                for (LatLng lt : arraylistoflatlng) {
                                    GeoPoint geo = new GeoPoint(lt.getLatitude(), lt.getLongitude());
                                    geoList.add(geo);
                                }
                                value.put(Constants.KEY_MAP, geoList);
                                DocumentReference doc = database.collection(Constants.KEY_COLLECTION_AREA).document();
                                doc.set(value).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("AAAA", doc.getId());
                                        HashMap<String, Object> areaId = new HashMap<>();
                                        areaId.put(Constants.KEY_AREA_ID, doc.getId());
                                        database.collection(Constants.KEY_COLLECTION_USER).document(magager.id)
                                                .update(areaId).addOnSuccessListener(task1 -> {
                                                    Toast.makeText(getContext(), "Thêm vùng mới thành công", Toast.LENGTH_SHORT).show();
                                                    mBinding.edtNameArea.setText("");
                                                    magager = null;
                                                    deletePolygon();
                                                    getAraes();
                                                });
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Tên vùng đã tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                database.collection(Constants.KEY_COLLECTION_AREA)
                        .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                        .whereEqualTo(Constants.KEY_NAME, name).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.getDocuments().isEmpty()
                                    || queryDocumentSnapshots.getDocuments().get(0).getId().equals(idItem)) {
                                HashMap<String, Object> value = new HashMap<>();
                                ArrayList<GeoPoint> geoList = new ArrayList<>();
                                value.put(Constants.KEY_NAME, name);

                                for (LatLng lt : arraylistoflatlng) {
                                    GeoPoint geo = new GeoPoint(lt.getLatitude(), lt.getLongitude());
                                    geoList.add(geo);
                                }
                                value.put(Constants.KEY_MAP, geoList);
                                DocumentReference doc = database.collection(Constants.KEY_COLLECTION_AREA).document(idItem);
                                doc.update(value).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        HashMap<String, Object> areaId = new HashMap<>();
                                        areaId.put(Constants.KEY_AREA_ID, doc.getId());
                                        database.collection(Constants.KEY_COLLECTION_USER).document(mUserEdit.id)
                                                .update(Constants.KEY_AREA_ID, FieldValue.delete());
                                        database.collection(Constants.KEY_COLLECTION_USER).document(magager.id)
                                                .update(areaId).addOnSuccessListener(task1 -> {
                                                    Toast.makeText(getContext(), "Sửa thành công", Toast.LENGTH_SHORT).show();

                                                });
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Tên vùng đã tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }


        }

    }

    //   Mở dialog chọn trưởng vùng
    private void openPickUserDialog() {
        dialog = openDialog(R.layout.layout_dialog_pick_user);
        assert dialog != null;

        //Button trong dialog
        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);

        //ConstrainLayout trong dialog
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerPickUserDialog);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        getUser();
        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void getUser() {
        mUsers.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot qr : queryDocumentSnapshots.getDocuments()) {
                        String id = qr.getId();
                        String name = qr.getString(Constants.KEY_NAME);
                        String image = qr.getString(Constants.KEY_IMAGE);
                        String phone = qr.getString(Constants.KEY_PHONE);
                        String type = qr.getString(Constants.KEY_TYPE_ACCOUNT);
                        String areaId = qr.getString(Constants.KEY_AREA_ID);
                        String disable = qr.getString("disable");
                        if ((areaId == null || areaId.isEmpty()) && (disable == null || disable.equals("0"))) {
                            User user = new User();
                            user.name = name;
                            user.image = image;
                            user.id = id;
                            user.phone = phone;
                            user.position = type;
                            mUsers.add(user);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
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

    @Override
    public void onStart() {
        super.onStart();
        mBinding.mapView.onStart();
        setListener();
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
            getAraes();
        } else if (action.equals("edit")) {
            mBinding.saveBtnCreate.setText("Sửa");
            mBinding.toolbarManageArea.setTitle("Chỉnh sửa vùng");
            idItem = getArguments().getString("idItem");
            getAraes();
            setData(idItem);
        }
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
    public void onClickUser(User user) {
        magager = user;
        mBinding.imageManager.setImageBitmap(getUserImage(user.image));
        if (user.name.equals("")) {
            mBinding.textNameManager.setText("Chưa cập nhật tên!");
        } else {
            mBinding.textNameManager.setText(user.name);
        }
        dialog.dismiss();

    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = new byte[0];
        if (encodedImage != null) {
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void setData(String idArea) {
        database.collection(Constants.KEY_COLLECTION_AREA).document(idArea)
                .get().addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString(Constants.KEY_NAME);
                    ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) documentSnapshot.get(Constants.KEY_MAP);
                    mBinding.edtNameArea.setText(name);
//                  Vẽ vùng lên bản đồ

                    for (GeoPoint point : geoList) {
                        arraylistoflatlng.add(new LatLng(point.getLatitude(), point.getLongitude()));
                    }

//                    polygon = mapboxMap.addPolygon(new PolygonOptions()
//                            .addAll(arraylistoflatlng)
//                            .fillColor(Color.argb(100, 255, 80, 80)));
                    arraylistoflatlng.add(arraylistoflatlng.get(0));
//                    PolylineOptions rectOptions = new PolylineOptions()
//                            .addAll(arraylistoflatlng)
//                            .color(Color.BLACK)
//                            .width(3);
//                    Polyline line = mapboxMap.addPolyline(rectOptions);
                    LatLng center = getPolygonCenterPoint((ArrayList<LatLng>) arraylistoflatlng);
                    CameraPosition areaPosition = new CameraPosition.Builder()
                            .target(center)
                            .zoom(13).build();
                    mapboxMap.setCameraPosition(areaPosition);
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);
                });
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF)
                .whereEqualTo(Constants.KEY_AREA_ID, idArea).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        String name = queryDocumentSnapshots.getDocuments().get(0).getString(Constants.KEY_NAME);
                        String avt = queryDocumentSnapshots.getDocuments().get(0).getString(Constants.KEY_IMAGE);
                        mBinding.textNameManager.setText(name);
                        mBinding.imageManager.setImageBitmap(getUserImage(avt));
                        mUserEdit = new User();
                        mUserEdit.name = name;
                        mUserEdit.id = queryDocumentSnapshots.getDocuments().get(0).getId();
                        magager = mUserEdit;
                    }
                });

    }

    //   Lấy trung điểm polygon
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


}