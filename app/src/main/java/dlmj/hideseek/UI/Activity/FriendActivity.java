package dlmj.hideseek.UI.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.FriendCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.OnTouchingLetterChangedListener;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.FriendTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.FriendListAdapter;
import dlmj.hideseek.UI.Adapter.FriendResultListAdapter;
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CustomLetterListView;

/**
 * Created by Two on 6/5/16.
 */
public class FriendActivity extends BaseActivity implements UIDataListener<Bean>, AbsListView.OnScrollListener{
    private final static String TAG = "FriendActivity";
    private String mLastTitle;
    private final static int MSG_REFRESH_LIST = 100;
    private ListView mFriendListView;
    private ListView mResultListView;
    private TextView mNoResultTextView;
    private CustomLetterListView mLetterListView;
    private FriendListAdapter mFriendListAdapter;
    private FriendResultListAdapter mFriendResultListAdapter;
    private List<User> mFriendList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private boolean mIsScroll = false;
    private HashMap<String, Integer> mAlphaIndexer = new HashMap<>();
    private TextView mOverlayTextVew;
    private boolean mReady;
    private WindowManager mWindowManager;
    private OverlayThread mOverlayThread;
    private EditText mSearchEditText;
    private ImageButton mAddFriendButton;
    private LinearLayout mBackLayout;
    private TextView mLastTitleTextView;
    private List<User> mResultFriendList = new ArrayList<>();
    private FriendTableManager mFriendTableManager;
    private ErrorMessageFactory mErrorMessageFactory;

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mFriendListAdapter.notifyDataSetChanged();
                    break;
                case OverlayThread.HIDE_OVERLAY:
                    mOverlayTextVew.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend);
        initData();
        findView();
        setListener();

        mFriendList.clear();
        if(FriendCache.getInstance(this).getList() != null) {
            mFriendList.addAll(FriendCache.getInstance(this).getList());
        }
        setAlphaIndexer();
        mFriendListAdapter.notifyDataSetChanged();
        mResponseCode = 0;
        Map<String, String> params = new HashMap<>();
        params.put("version", mFriendTableManager.getVersion() + "");
        mNetworkHelper.sendPostRequest(UrlParams.GET_FRIEND_URL, params);
    }

    @Override
    public void onDestroy() {
        mWindowManager.removeView(mOverlayTextVew);

        super.onDestroy();
    }

    private void setAlphaIndexer() {
        for (int i = 0; i < mFriendList.size(); i++) {
            String currentStr = PinYinUtil.getAlpha(mFriendList.get(i).getPinyin());
            String previewStr = (i - 1) >= 0 ?
                    PinYinUtil.getAlpha(mFriendList.get(i - 1).getPinyin()) : " ";
            if (!previewStr.equals(currentStr)) {
                String name = PinYinUtil.getAlpha(mFriendList.get(i).getPinyin());
                mAlphaIndexer.put(name, i);
            }
        }

//        mLetterListView.setAlphas((String[]) mAlphaIndexer.keySet().toArray());
//        mLetterListView.invalidate();
    }

    private void initData() {
        mLastTitle = getIntent().getStringExtra(IntentExtraParam.LAST_TITLE);
        mFriendListAdapter = new FriendListAdapter(this, mFriendList);
        mFriendResultListAdapter = new FriendResultListAdapter(this, mResultFriendList);
        mNetworkHelper = new NetworkHelper(this);
        mFriendTableManager = FriendTableManager.getInstance(this);
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mNoResultTextView = (TextView) findViewById(R.id.noResultTextView);
        mFriendListView = (ListView) findViewById(R.id.friendListView);
        mFriendListView.setAdapter(mFriendListAdapter);
        mResultListView = (ListView) findViewById(R.id.resultListView);
        mLetterListView = (CustomLetterListView) findViewById(R.id.letterListView);
        mReady = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        mOverlayTextVew = (TextView) inflater.inflate(R.layout.overlay, null);
        mOverlayTextVew.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mOverlayTextVew, layoutParams);
        mOverlayThread = new OverlayThread(mOverlayTextVew, mUiHandler);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mAddFriendButton = (ImageButton) findViewById(R.id.addFriendBtn);
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
        mLastTitleTextView = (TextView) findViewById(R.id.lastTitleTextView);
        mLastTitleTextView.setText(mLastTitle);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                User friend = mFriendList.get(position);

                friend.setIsFriend(true);
                Intent intent = new Intent(FriendActivity.this, ProfileActivity.class);
                intent.putExtra(IntentExtraParam.LAST_TITLE, FriendActivity.this.getTitle().toString());
                intent.putExtra(IntentExtraParam.PROFILE_INFO, friend);
                startActivity(intent);
            }
        });

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendActivity.this, AddFriendActivity.class);
                intent.putExtra(IntentExtraParam.LAST_TITLE, FriendActivity.this.getTitle().toString());
                startActivity(intent);
            }
        });

        mLetterListView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                mIsScroll = false;
                if (mAlphaIndexer.get(letter) != null) {
                    int position = mAlphaIndexer.get(letter);
                    mFriendListView.setSelection(position);
                    mOverlayTextVew.setText(letter);
                    mOverlayTextVew.setVisibility(View.VISIBLE);
                    mUiHandler.removeCallbacks(mOverlayThread);
                    mUiHandler.postDelayed(mOverlayThread, 1000);
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
                    mFriendListView.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.GONE);
                    mNoResultTextView.setVisibility(View.GONE);
                } else {
                    mResultFriendList.clear();
                    mLetterListView.setVisibility(View.GONE);
                    mFriendListView.setVisibility(View.GONE);
                    mResultFriendList.addAll(mFriendTableManager.searchFriends(charSequence.toString()));
                    if (mResultFriendList.size() <= 0) {
                        mNoResultTextView.setVisibility(View.VISIBLE);
                        mResultListView.setVisibility(View.GONE);
                    } else {
                        mNoResultTextView.setVisibility(View.GONE);
                        mResultListView.setVisibility(View.VISIBLE);
                        mFriendResultListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        mResponseCode = CodeParams.SUCCESS;
        LogUtil.d(TAG, data.getResult());

        FriendCache.getInstance(this).setFriends(data.getResult());
        mFriendList.clear();
        mFriendList.addAll(FriendCache.getInstance(this).getList());
        setAlphaIndexer();
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mResponseCode = errorCode;

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
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
            String name = mFriendList.get(firstVisibleItem).getPinyin();
            text = name.substring(0, 1).toUpperCase();
            mOverlayTextVew.setText(text);
            mOverlayTextVew.setVisibility(View.VISIBLE);
            mUiHandler.removeCallbacks(mOverlayThread);
            mUiHandler.postDelayed(mOverlayThread, 1000);
        }
    }
}
