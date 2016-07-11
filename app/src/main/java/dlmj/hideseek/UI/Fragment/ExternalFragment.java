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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dlmj.hideseek.Common.Interfaces.OnTouchingLetterChangedListener;
import dlmj.hideseek.Common.Model.ForeignCity;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.ForeignCityTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.ForeignCityListAdapter;
import dlmj.hideseek.UI.Adapter.ForeignCityResultListAdapter;
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CustomLetterListView;

/**
 * Created by Two on 5/25/16.
 */
public class ExternalFragment extends Fragment implements AbsListView.OnScrollListener{
    private final static String TAG = "ExternalFragment";
    private View rootView;
    private TextView mOverlayTextVew;
    private WindowManager mWindowManager;
    private OverlayThread mOverlayThread;
    private boolean mReady;
    private boolean mIsScroll = false;
    private CustomLetterListView mLetterListView;
    private ListView mResultListView;
    private TextView mNoResultTextView;
    private EditText mSearchEditText;
    private HashMap<String, Integer> mAlphaIndexer = new HashMap<>();
    private List<ForeignCity> mAllCityList = new ArrayList<>();
    private List<ForeignCity> mResultCityList = new ArrayList<>();
    private ForeignCityListAdapter mForeignCityListAdapter;
    private ForeignCityResultListAdapter mForeignCityResultListAdapter;
    private ListView mCityListView;
    private ForeignCityTableManager mForeignCityTableManager;
    private Intent mIntent;

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
            rootView = inflater.inflate(R.layout.external, null);
            initData();
            findView(rootView);
            setListener();
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        mWindowManager.removeView(mOverlayTextVew);

        super.onDestroyView();
    }

    private void initData() {
        mIntent = getActivity().getIntent();
        mForeignCityTableManager = new ForeignCityTableManager(getActivity());
        initAllCity();
        mForeignCityListAdapter = new ForeignCityListAdapter(getActivity(), mAllCityList);
        mForeignCityResultListAdapter = new ForeignCityResultListAdapter(getActivity(),
                mResultCityList);
        for (int i = 0; i < mAllCityList.size(); i++) {
            String currentStr = PinYinUtil.getAlpha(mAllCityList.get(i).getName());
            String previewStr = (i - 1) >= 0 ?
                    PinYinUtil.getAlpha(mAllCityList.get(i - 1).getName()) : " ";
            if (!previewStr.equals(currentStr)) {
                String name = PinYinUtil.getAlpha(mAllCityList.get(i).getName());
                mAlphaIndexer.put(name, i);
            }
        }
    }

    private void initAllCity() {
        List<ForeignCity> cityList = mForeignCityTableManager.getAllCities();
        Collections.sort(cityList);
        mAllCityList.addAll(cityList);
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
        mLetterListView = (CustomLetterListView) rootView.findViewById(R.id.letterListView);
        mNoResultTextView = (TextView) rootView.findViewById(R.id.noResultTextView);
        mCityListView = (ListView) rootView.findViewById(R.id.cityListView);
        mCityListView.setAdapter(mForeignCityListAdapter);
        mCityListView.setOnScrollListener(this);
        mSearchEditText = (EditText) rootView.findViewById(R.id.searchEditText);
        mResultListView = (ListView) rootView.findViewById(R.id.resultListView);
        mResultListView.setAdapter(mForeignCityResultListAdapter);
    }

    private void setListener() {
        mLetterListView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                mIsScroll = false;
                if (mAlphaIndexer.get(letter) != null) {
                    int position = mAlphaIndexer.get(letter);
                    mCityListView.setSelection(position);
                    mOverlayTextVew.setText(letter);
                    mOverlayTextVew.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mOverlayThread);
                    mHandler.postDelayed(mOverlayThread, 1000);
                }
            }
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence == null || "".equals(charSequence.toString())) {
                    mLetterListView.setVisibility(View.VISIBLE);
                    mCityListView.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.GONE);
                    mNoResultTextView.setVisibility(View.GONE);
                } else {
                    mResultCityList.clear();
                    mLetterListView.setVisibility(View.GONE);
                    mCityListView.setVisibility(View.GONE);
                    mResultCityList.addAll(mForeignCityTableManager.searchCities(charSequence.toString()));
                    if (mResultCityList.size() <= 0) {
                        mNoResultTextView.setVisibility(View.VISIBLE);
                        mResultListView.setVisibility(View.GONE);
                    } else {
                        mNoResultTextView.setVisibility(View.GONE);
                        mResultListView.setVisibility(View.VISIBLE);
                        mForeignCityResultListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mCityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ForeignCity city = mAllCityList.get(position);

                mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                getActivity().setResult(Activity.RESULT_OK, mIntent);
                getActivity().finish();
            }
        });

        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ForeignCity city = mResultCityList.get(position);

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
            text = name.substring(0, 1).toUpperCase();
            mOverlayTextVew.setText(text);
            mOverlayTextVew.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mOverlayThread);
            mHandler.postDelayed(mOverlayThread, 1000);
        }
    }
}
