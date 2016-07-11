package dlmj.hideseek.UI.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dlmj.hideseek.Common.Interfaces.OnTouchingLetterChangedListener;
import dlmj.hideseek.Common.Model.DomesticCity;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.DomesticCityTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.PersonListAdapter;
import dlmj.hideseek.UI.Adapter.RegionResultListAdapter;
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CustomLetterListView;

/**
 * Created by Two on 5/10/16.
 */
public class InternalFragment extends Fragment implements AbsListView.OnScrollListener {
    private final static String TAG = "InternalFragment";
    private View rootView;
    private DomesticCityTableManager mDomesticCityTableManager;
    private List<DomesticCity> mAllCityList = new ArrayList<>();
    private List<DomesticCity> mHotCityList = new ArrayList<>();
    private List<DomesticCity> mResultCityList = new ArrayList<>();
    private List<DomesticCity> mRecentCityList = new ArrayList<>();
    private ListView mPersonListView;
    private ListView mResultListView;
    private EditText mSearchEditText;
    private TextView mNoResultTextView;
    private CustomLetterListView mLetterListView;
    private boolean mIsScroll = false;
    private RegionResultListAdapter mRegionResultListAdapter;
    private PersonListAdapter mPersonListAdapter;
    private HashMap<String, Integer> mAlphaIndexer = new HashMap<>();
    private TextView mOverlayTextVew;
    private boolean mReady;
    private OverlayThread mOverlayThread;
    private Intent mIntent;
    private WindowManager mWindowManager;
    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OverlayThread.HIDE_OVERLAY:
                    mOverlayTextVew.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initOverlay();

        if(rootView == null) {
            rootView = inflater.inflate(R.layout.internal, null);
            initializeData();
            findView(rootView);
            setListener();
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    @Override
    public void onDestroyView() {
        mWindowManager.removeView(mOverlayTextVew);

        super.onDestroyView();
    }

    private void initializeData() {
        mIntent = getActivity().getIntent();
        mDomesticCityTableManager = new DomesticCityTableManager(getActivity());
        initCity();
        initHotCity();
        initRecentCity();
        mRegionResultListAdapter = new RegionResultListAdapter(getActivity(), mResultCityList);
        mPersonListAdapter = new PersonListAdapter(getActivity(), mAllCityList, mRecentCityList,
                mHotCityList, mIntent);
        for (int i = 0; i < mAllCityList.size(); i++) {
            String currentStr = PinYinUtil.getAlpha(mAllCityList.get(i).getPinyin());
            String previewStr = (i - 1) >= 0 ?
                    PinYinUtil.getAlpha(mAllCityList.get(i - 1).getPinyin()) : " ";
            if (!previewStr.equals(currentStr)) {
                String name = PinYinUtil.getAlpha(mAllCityList.get(i).getPinyin());
                mAlphaIndexer.put(name, i);
            }
        }

        mLocationClient = new AMapLocationClient(getActivity());
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.startLocation();
        mPersonListAdapter.setLocationClient(mLocationClient);
    }

    private void initRecentCity() {
        mRecentCityList.addAll(mDomesticCityTableManager.getRecentCities());
    }

    private void initHotCity() {
        mHotCityList.add(new DomesticCity("上海", "2"));
        mHotCityList.add(new DomesticCity("北京", "2"));
        mHotCityList.add(new DomesticCity("广州", "2"));
        mHotCityList.add(new DomesticCity("深圳", "2"));
        mHotCityList.add(new DomesticCity("武汉", "2"));
        mHotCityList.add(new DomesticCity("天津", "2"));
        mHotCityList.add(new DomesticCity("西安", "2"));
        mHotCityList.add(new DomesticCity("南京", "2"));
        mHotCityList.add(new DomesticCity("杭州", "2"));
        mHotCityList.add(new DomesticCity("成都", "2"));
        mHotCityList.add(new DomesticCity("重庆", "2"));
    }

    private void initOverlay() {
        mReady = true;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mOverlayTextVew = (TextView) inflater.inflate(R.layout.overlay, null);
        mOverlayTextVew.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mOverlayTextVew, layoutParams);
        mOverlayThread = new OverlayThread(mOverlayTextVew, mHandler);
    }

    private void findView(View rootView) {
        mPersonListView = (ListView) rootView.findViewById(R.id.personListView);
        mPersonListView.setAdapter(mPersonListAdapter);
        mPersonListView.setOnScrollListener(this);
        mResultListView = (ListView) rootView.findViewById(R.id.resultListView);
        mResultListView.setAdapter(mRegionResultListAdapter);
        mSearchEditText = (EditText) rootView.findViewById(R.id.searchEditText);
        mNoResultTextView = (TextView) rootView.findViewById(R.id.noResultTextView);
        mLetterListView = (CustomLetterListView) rootView.findViewById(R.id.letterListView);
    }

    private void setListener() {
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence == null || "".equals(charSequence.toString())) {
                    mLetterListView.setVisibility(View.VISIBLE);
                    mPersonListView.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.GONE);
                    mNoResultTextView.setVisibility(View.GONE);
                } else {
                    mResultCityList.clear();
                    mLetterListView.setVisibility(View.GONE);
                    mPersonListView.setVisibility(View.GONE);
                    mResultCityList.addAll(mDomesticCityTableManager.searchCities(charSequence.toString()));
                    if (mResultCityList.size() <= 0) {
                        mNoResultTextView.setVisibility(View.VISIBLE);
                        mResultListView.setVisibility(View.GONE);
                    } else {
                        mNoResultTextView.setVisibility(View.GONE);
                        mResultListView.setVisibility(View.VISIBLE);
                        mRegionResultListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mLetterListView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                mIsScroll = false;
                if (mAlphaIndexer.get(letter) != null) {
                    int position = mAlphaIndexer.get(letter);
                    mPersonListView.setSelection(position);
                    mOverlayTextVew.setText(letter);
                    mOverlayTextVew.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mOverlayThread);
                    mHandler.postDelayed(mOverlayThread, 1000);
                }
            }
        });

        mPersonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 4) {
                    DomesticCity city = mAllCityList.get(position);
                    mDomesticCityTableManager.insertRecentCity(city);

                    mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                    getActivity().setResult(Activity.RESULT_OK, mIntent);
                    getActivity().finish();
                }
            }
        });

        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                DomesticCity city = mResultCityList.get(position);
                mDomesticCityTableManager.insertRecentCity(city);

                mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                getActivity().setResult(Activity.RESULT_OK, mIntent);
                getActivity().finish();
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            mIsScroll = scrollState == SCROLL_STATE_TOUCH_SCROLL
                    || scrollState == SCROLL_STATE_FLING;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if (!mIsScroll) {
            return;
        }

        if (mReady) {
            String text;
            String name = mAllCityList.get(firstVisibleItem).getName();
            String pinyin = mAllCityList.get(firstVisibleItem).getPinyin();
            if (firstVisibleItem < 4) {
                text = name;
            } else {
                text = PinYinUtil.converterToFirstSpell(pinyin)
                        .substring(0, 1).toUpperCase();
            }
            mOverlayTextVew.setText(text);
            mOverlayTextVew.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mOverlayThread);
            // 延迟一秒后执行，让overlay为不可见
            mHandler.postDelayed(mOverlayThread, 1000);
        }
    }

    private void initCity() {
        DomesticCity city = new DomesticCity("定位", "0"); // 当前定位城市
        mAllCityList.add(city);
        city = new DomesticCity("最近", "1"); // 最近访问的城市
        mAllCityList.add(city);
        city = new DomesticCity("热门", "2"); // 热门城市
        mAllCityList.add(city);
        city = new DomesticCity("全部", "3"); // 全部城市
        mAllCityList.add(city);
        List<DomesticCity> cityList = mDomesticCityTableManager.getAllCities();
        Collections.sort(cityList);
        mAllCityList.addAll(cityList);
    }
}
