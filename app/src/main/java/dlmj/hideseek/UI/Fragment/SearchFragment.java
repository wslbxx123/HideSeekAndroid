package dlmj.hideseek.UI.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.google.gson.JsonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.DisplayUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Hardware.CameraInterface;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.MapActivity;
import dlmj.hideseek.UI.Activity.NavigationActivity;
import dlmj.hideseek.UI.Activity.StoreActivity;
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CameraSurfaceView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.GameView;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 4/29/16.
 */
public class SearchFragment extends Fragment implements CameraInterface.CamOpenOverCallback,
        CameraSurfaceView.OnCreateListener, LocationSource, AMapLocationListener,
        UIDataListener<Bean>, SensorEventListener, AMap.OnMapLoadedListener, View.OnClickListener {
    private final static int REFRESH_MAP_INTERVAL = 5000;
    private final static String TAG = "Search Fragment";
    private float mPreviewRate = -1f;
    private CameraSurfaceView mSurfaceView = null;
    private MapView mMapView;
    private AMap mMap;
    private LocationSource.OnLocationChangedListener mLocationListener;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private View rootView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetGoalNetworkHelper;
    private NetworkHelper mHitMonsterNetworkHelper;
    private NetworkHelper mSeeMonsterNetworkHelper;
    private Button mNavigateButton;
    private double mLatitude;
    private double mLongitude;
    private NaviLatLng mStartLatLng = new NaviLatLng();
    private NaviLatLng mEndLatLng = new NaviLatLng();
    private Goal mEndGoal;
    private TextView mDistanceTextView;
    private int mOrientation;
    private double mDistance;
    private SensorManager mSensorManager;
    private boolean mIfRegisteredSensor;
    private Button mGetButton;
    private Hashtable<Long, Marker> mMarkerHashTable = new Hashtable<>();
    private LinearLayout mDistanceLayout;
    private CustomSuperToast mErrorSuperToast;
    private ErrorMessageFactory mErrorMessageFactory;
    private TextView mOverlayTextVew;
    private WindowManager mWindowManager;
    private OverlayThread mOverlayThread;
    private GameView mGameView;
    private ImageView mRoleImageView;
    private TextView mHintTextView;
    private Handler mUiHandler = new Handler();
    private Handler mRefreshMapHandler = new Handler();
    private boolean mLocationFlag = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMap();
            mRefreshMapHandler.postDelayed(mRunnable, REFRESH_MAP_INTERVAL);
        }
    };
    private ImageButton mRefreshBtn;
    private RelativeLayout mSetBombLayout;
    private ImageView mMonster;
    private TextView mBombNum;
    private LoadingDialog mLoadingDialog;
    private boolean mIfSeeGoal = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initOverlay();

        if(rootView == null) {
            rootView = inflater.inflate(R.layout.search, null);
            initData();
            findView(rootView);
            setListener();
            mMapView.onCreate(savedInstanceState);
            setLayer("MAP_TYPE_NORMAL");
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        return rootView;
    }

    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mOverlayTextVew = (TextView) inflater.inflate(R.layout.score_overlay, null);
        Typeface fontFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/score_font.ttf");
        mOverlayTextVew.setTypeface(fontFace);
        mOverlayTextVew.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0, -150,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mOverlayTextVew, layoutParams);
        mOverlayThread = new OverlayThread(mOverlayTextVew, mUiHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
        mMapView.onResume();
        LogUtil.d(TAG, "Map View is resumed");

        if(GoalCache.getInstance().getIfNeedClearMap()) {
            refresh();
        }

        mRefreshMapHandler.postDelayed(mRunnable, REFRESH_MAP_INTERVAL);

//        List<Goal> goals = GoalCache.getInstance().getList();
//
//        setGoalsOnMap(goals);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        if (sensors.size() > 0){
            Sensor sensor = sensors.get(0);
            mIfRegisteredSensor = mSensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            mOrientation = -1;
        }
        setSearchView();
    }

    private void refreshMap() {
        if(mLongitude == 0 || mLatitude == 0) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("latitude", mLatitude + "");
        params.put("longitude", mLongitude + "");

        mStartLatLng.setLatitude(mLatitude);
        mStartLatLng.setLongitude(mLongitude);

        long version = GoalCache.getInstance().getVersion();
        params.put("version", version + "");

        User user = UserCache.getInstance().getUser();
        if(UserCache.getInstance().ifLogin()) {
            params.put("account_role", user.getRole().getValue() + "");
        }

        if (!mLoadingDialog.isShowing() && GoalCache.getInstance().getVersion() == 0) {
            mLoadingDialog.show();
        }

        mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_MAP_URL, params);
    }

    private void setEndGoal() {
        List<Goal> list = new LinkedList<>();

        if(mEndGoal != null) {
            list.add(mEndGoal);
            list.add(GoalCache.getInstance().getSelectedGoal());
        }
        setGoalsOnMap(list);

        mEndGoal = GoalCache.getInstance().getSelectedGoal();

        if(mEndGoal != null) {
            mGameView.setGoal(mEndGoal);
            mEndLatLng.setLatitude(mEndGoal.getLatitude());
            mEndLatLng.setLongitude(mEndGoal.getLongitude());
            refreshDistance();
            checkIfGoalDisplayed();
        }
    }

    @Override
    public void onDestroyView() {
        mWindowManager.removeView(mOverlayTextVew);
        mRefreshMapHandler.removeCallbacks(mRunnable);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        LogUtil.d(TAG, "Map View is destroyed");
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        LogUtil.d(TAG, "Map View is paused");
        CameraInterface.getInstance(getActivity()).doStopCamera();

        if (mIfRegisteredSensor) {
            mSensorManager.unregisterListener(this);
            mIfRegisteredSensor = false;
        }
    }

    public void refreshDistance() {
        double distance = AMapUtils.calculateLineDistance(
                new LatLng(mStartLatLng.getLatitude(), mStartLatLng.getLongitude()),
                new LatLng(mEndLatLng.getLatitude(), mEndLatLng.getLongitude()));
        mDistance = distance < 15 ? 0 : distance - 15;
        mDistanceTextView.setText((int)mDistance + "m");
        LogUtil.d(TAG, "distance: " + (int)mDistance + "m");
    }

    public void openCamera() {
        Thread openThread = new Thread() {
            @Override
            public void run() {
                try{
                    CameraInterface.getInstance(getActivity()).doOpenCamera(SearchFragment.this);
                } catch(RuntimeException ex) {
                    LogUtil.e(TAG, ex.getMessage());
                }
            }
        };
        openThread.start();
    }

    public void initData() {
        mPreviewRate = DisplayUtil.getScreenRate(getActivity());
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetGoalNetworkHelper = new NetworkHelper(getActivity());
        mHitMonsterNetworkHelper = new NetworkHelper(getActivity());
        mSeeMonsterNetworkHelper = new NetworkHelper(getActivity());

        mSensorManager =  (SensorManager) getActivity().
                getSystemService(Context.SENSOR_SERVICE);

        mErrorSuperToast = new CustomSuperToast(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView(View view) {
        mSurfaceView = (CameraSurfaceView) view.findViewById(R.id.cameraSurfaceView);
        mSurfaceView.setOnCreateListener(this);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMap = mMapView.getMap();

        mNavigateButton = (Button) view.findViewById(R.id.navigateButton);
        mDistanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        mGetButton = (Button) view.findViewById(R.id.getButton);
        mDistanceLayout = (LinearLayout) view.findViewById(R.id.distanceLayout);
        mGameView = (GameView) view.findViewById(R.id.gameView);
        mRoleImageView = (ImageView) view.findViewById(R.id.roleImageView);
        mHintTextView = (TextView) view.findViewById(R.id.hintTextView);

        //右上角图标
        mRefreshBtn = (ImageButton) view.findViewById(R.id.refreshBtn);
        mSetBombLayout = (RelativeLayout) view.findViewById(R.id.setBombLayout);
        mMonster = (ImageView) view.findViewById(R.id.monster_handbook);
        mBombNum = (TextView) view.findViewById(R.id.bomb_num);

        mLoadingDialog = new LoadingDialog(getActivity(), getContext().getString(R.string.refresh_map_hint));
    }

    private void setSearchView() {
        if (UserCache.getInstance().ifLogin()) {
            mSetBombLayout.setVisibility(View.VISIBLE);
            mMonster.setVisibility(View.VISIBLE);

            User user = UserCache.getInstance().getUser();
            mRoleImageView.setImageResource(user.getRoleImageDrawableId());
            mBombNum.setText(user.getBombNum() + "");
        } else {
            mSetBombLayout.setVisibility(View.GONE);
            mMonster.setVisibility(View.GONE);
        }
    }

    private void setLayer(String layerName) {
        if (layerName.equals("MAP_TYPE_NORMAL")) {
            mMapView.getMap().setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
        } else if (layerName.equals("MAP_TYPE_SATELLITE")) {
            mMapView.getMap().setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        }
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);
        mSetBombLayout.setOnClickListener(this);
        mMonster.setOnClickListener(this);

        mGameView.setOnBombGotListener(new GameView.OnBombGotListener() {
            @Override
            public void onBombGot() {
                if(mEndGoal.getType() == Goal.GoalTypeEnum.bomb) {
                    getGoal();
                }
            }
        });

        UIDataListener<Bean> getGoalUIDataListener = new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                mOverlayTextVew.setText(getScoreStr(mEndGoal.getScore()));

                mOverlayTextVew.setVisibility(View.VISIBLE);
                mUiHandler.removeCallbacks(mOverlayThread);
                mUiHandler.postDelayed(mOverlayThread, 1000);
                updateEndGoal();
                LogUtil.d(TAG, data.getMessage());
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                String message = mErrorMessageFactory.get(errorCode);
                mErrorSuperToast.show(message);

                mGameView.goalContinue();
            }
        };

        mGetGoalNetworkHelper.setUiDataListener(getGoalUIDataListener);

        UIDataListener<Bean> hitMonsterUIDataListener = new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, "Hit monster: " + data.getResult());

                try {
                    String result = data.getResult();
                    JSONObject jsonObject = new JSONObject(result);

                    if(jsonObject.get("score_sum") != null &&
                            !(jsonObject.get("score_sum") instanceof JsonNull) &&
                            !jsonObject.getString("score_sum").equals("null")) {
                        mOverlayTextVew.setText("+" + mEndGoal.getScore());
                        mOverlayTextVew.setVisibility(View.VISIBLE);
                        mUiHandler.removeCallbacks(mOverlayThread);
                        mUiHandler.postDelayed(mOverlayThread, 1000);

                        if(UserCache.getInstance().ifLogin()) {
                            UserCache.getInstance().getUser().setRecord(jsonObject.getInt("score_sum"));
                            updateEndGoal();
                        }
                    } else {
                        int canSuccess = jsonObject.getInt("if_can_success");

                        if(canSuccess == 0) {
                            mHintTextView.setText(R.string.not_meet_condition);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mHitMonsterNetworkHelper.closeLock();
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                String message = mErrorMessageFactory.get(errorCode);
                mErrorSuperToast.show(message);

                if(errorCode == CodeParams.ERROR_GOAL_DISAPPEAR) {
                    updateEndGoal();
                }

                mHitMonsterNetworkHelper.closeLock();
            }
        };

        mHitMonsterNetworkHelper.setUiDataListener(hitMonsterUIDataListener);

        UIDataListener<Bean> seeMonsterUIDataListener = new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, "See monster: " + data.getResult());
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                String message = mErrorMessageFactory.get(errorCode);
                mErrorSuperToast.show(message);

                if(errorCode == CodeParams.ERROR_SESSION_INVALID) {
                    UserInfoManager.getInstance().checkIfGoToLogin(getActivity());
                }
            }
        };

        mSeeMonsterNetworkHelper.setUiDataListener(seeMonsterUIDataListener);

        mNavigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NavigationActivity.class);
                intent.putExtra(IntentExtraParam.START_POINT, mStartLatLng);
                intent.putExtra(IntentExtraParam.END_POINT, mEndLatLng);

                startActivity(intent);
            }
        });

        mMap.setOnMapLoadedListener(this);


        mDistanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Map view is clicked");

                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGameView.getGoalDisplayed()) {
                    switch(mEndGoal.getType()) {
                        case mushroom:
                            getGoal();
                            break;
                        case monster:
                            mGameView.hitMonster();
                            Map<String, String> params = new HashMap<>();
                            params.put("goal_id", mEndGoal.getPkId() + "");
                            User user = UserCache.getInstance().getUser();
                            params.put("account_role", user.getRole().getValue() + "");
                            mHitMonsterNetworkHelper.sendPostRequest(
                                    UrlParams.HIT_MONSTER_URL, params);
                            mHitMonsterNetworkHelper.openLock();
                            break;
                    }
                }
            }
        });
    }

    private String getScoreStr(int score) {
        if(score > 0) {
            return "+" + score;
        } else {
            return score + "";
        }
    }

    private void getGoal() {
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", mEndGoal.getPkId() + "");
        params.put("goal_type", mEndGoal.getType().getValue() + "");
        mGetGoalNetworkHelper.sendPostRequest(UrlParams.GET_GOAL_URL, params);
    }

    private void refresh() {
        mMap.clear();
        setUpMap();
        mEndGoal = null;
        GoalCache.getInstance().reset();
        mMarkerHashTable.clear();
    }

    private void setUpMap() {
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location));
        mMap.setMyLocationStyle(locationStyle);
        mMap.setLocationSource(this);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(19));
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(mLocationListener != null) {
            if(aMapLocation.getErrorCode() == 0) {
                mLocationListener.onLocationChanged(aMapLocation);

                mLatitude = aMapLocation.getLatitude();
                mLongitude = aMapLocation.getLongitude();

                if(!mLocationFlag) {
                    refreshMap();
                    mLocationFlag = true;
                }
                LogUtil.d(TAG, "Latitude: " + mLatitude +
                        ";Longitude: " + mLongitude);

                refreshDistance();
                checkIfGoalDisplayed();
            } else{
                String errText = "定位失败，" + aMapLocation.getErrorCode() + ": " +
                        aMapLocation.getErrorInfo();
                LogUtil.d(TAG, errText);
            }
        }
    }

    @Override
    public void cameraHasOpened() {
        LogUtil.d(TAG, "Camera has been opened.");
        checkCamera();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationListener = onLocationChangedListener;

        if(mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            mLocationClient.setLocationListener(this);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationClient.setLocationOption(mLocationOption);

            mLocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onSurfaceViewCreated() {
        LogUtil.d(TAG, "Surface view has been created.");
        checkCamera();
    }

    private void checkCamera() {
        final SurfaceHolder holder = mSurfaceView.getSurfaceHolder();
        if(holder != null) {
            CameraInterface.getInstance(getActivity()).doStartPreview(holder, mPreviewRate);
            mMapView.onResume();
        }
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        GoalCache.getInstance().setGoals(data.getResult(), mLatitude, mLongitude);

        List<Goal> updateGoals = GoalCache.getInstance().getUpdateList();

        if(mLoadingDialog.isShowing()) {
            GoalCache.getInstance().refreshClosestGoal(mLatitude, mLongitude);
            setEndGoal();
        }

        setGoalsOnMap(updateGoals);

        if(mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onMapLoaded() {
        setUpMap();
    }

    private void setGoalsOnMap(List<Goal> goals) {
        for(Goal goal : goals) {
            MarkerOptions markerOptions;
            if(goal.getIsSelected()) {
                markerOptions = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box_selected_marker))
                        .draggable(true);
            } else{
                markerOptions = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box_marker))
                        .draggable(true);
            }

            Marker marker;
            if(mMarkerHashTable.containsKey(goal.getPkId())) {
                marker = mMarkerHashTable.get(goal.getPkId());
                marker.setIcon(markerOptions.getIcon());
            } else {
                marker = mMap.addMarker(markerOptions);
                mMarkerHashTable.put(goal.getPkId(), marker);
            }

            if(goal.getValid()) {
                marker.setVisible(true);
            } else{
                marker.setVisible(false);
            }

            marker.setPosition(new LatLng(goal.getLatitude(), goal.getLongitude()));

        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        if(mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private void checkIfGoalDisplayed() {
        if(mEndGoal != null) {
            LogUtil.d(TAG, "End Goal: Latitude=" + mEndGoal.getLatitude() + ", " +
                    "Longitude=" + mEndGoal.getLongitude());
            if((mEndGoal.getOrientation() == mOrientation || mOrientation == -1) &&
                    mDistance < 10 && mEndGoal.getValid()) {
                LogUtil.d(TAG, "Goal is displayed");

                mGameView.showGoal();

                if(!mIfSeeGoal && UserCache.getInstance().ifLogin()) {
                    mIfSeeGoal = true;
                    if(mEndGoal.getType() == Goal.GoalTypeEnum.monster) {
                        seeMonster();
                    }
                }
            } else {
                LogUtil.d(TAG, "Goal is hidden");
                mGameView.hideGoal();
            }
        }
    }

    private void seeMonster() {
        long pkId = mEndGoal.getPkId();
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", pkId + "");
        mSeeMonsterNetworkHelper.sendPostRequest(UrlParams.SEE_MONSTER_URL, params);
    }

    private void updateEndGoal() {
        mGameView.hideGoal();
        mEndGoal.setValid(false);
        mEndGoal.setIsSelected(false);

        GoalCache goalCache = GoalCache.getInstance();
        goalCache.setSelectedGoal(null);
        goalCache.refreshClosestGoal(mLatitude, mLongitude);

        setEndGoal();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];

        mOrientation = (((int)x + 45) / 90 * 90) % 360;
        LogUtil.d("TAG", "Orientation: " + mOrientation);

        checkIfGoalDisplayed();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setBombLayout:
                //跳转到商店
                forActivity();
                break;
            case R.id.monster_handbook:
                forActivity();
                break;
        }
    }

    private void forActivity() {
        Intent intent = new Intent(getActivity(), StoreActivity.class);
        startActivity(intent);
    }
}
