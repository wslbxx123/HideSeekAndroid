package dlmj.hideseek.UI.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

import static com.tencent.android.tpush.service.XGWatchdog.TAG;

/**
 * 修改性别<br/>
 * Created on 2016/10/17
 *
 * @author yekangqi
 */

public class ModifySexActivity extends BaseActivity implements UIDataListener<Bean> {
    private static final int LOADING_END = 1;
    private static final int CHANGE_SEX_SUCCESS = 2;
    private View mCancelTextView;
    private View mSubmitTextView;
    private View mSexLayout;
    private TextView mSexTextView;
    private User mUser;
    private NetworkHelper mNetworkHelper;
    private LoadingDialog mLoadingDialog;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;
    private String selectSex;//选择的性别

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
                case CHANGE_SEX_SUCCESS:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    mSexTextView.setText(msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sex);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mUser = UserCache.getInstance().getUser();
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mCancelTextView = findViewById(R.id.cancelTextView);
        mSubmitTextView = findViewById(R.id.submitTextView);
        mSexLayout = findViewById(R.id.sexLayout);
        mSexTextView = (TextView) findViewById(R.id.sexTextView);
        mSexTextView.setText(mUser.getSex().toString(this));

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);
    }

    private void setListener() {
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSubmitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
                params.put("sex",selectSex);
                mNetworkHelper.sendPostRequest(UrlParams.UPDATE_SEX_URL,params);
            }
        });
        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //性别
                AlertDialog.Builder builder = new AlertDialog.Builder(ModifySexActivity.this);
                String[] sexes = {getString(R.string.female), getString(R.string.male),
                        getString(R.string.secret)};
                builder.setItems(sexes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (!mLoadingDialog.isShowing()) {
                            mLoadingDialog.show();
                        }
                        //更新后的性别 0：未设置，1：女，2：男，3：秘密
                        switch (which)
                        {
                            case 0://弹窗里面第一个是女
                                selectSex="1";
                                break;
                            case 1:
                                selectSex="2";
                                break;
                            case 2:
                                selectSex="3";
                                break;
                        }
                        mSubmitTextView.setEnabled(true);
                    }
                });
                builder.show();
            }
        });
        mNetworkHelper.setUiDataListener(this);
    }

    @Override
    public void onDataChanged(Bean data) {
        mResponseCode = CodeParams.SUCCESS;
        try {
            //改变UI
            JSONObject result=new JSONObject(data.getResult());
            Message m=new Message();
            int updateSexInt=result.optInt("sex");
            User.SexEnum disPlaySex=User.SexEnum.valueOf(updateSexInt);
            m.obj= disPlaySex.toString(this);
            m.what=CHANGE_SEX_SUCCESS;
            mHandler.sendMessage(m);
            //更新缓存
            mUser.setSex(disPlaySex);
            UserCache.getInstance().update(mUser,"sex",updateSexInt);
            setResult(MyProfileActivity.RESULT_CODE_SEX_SUCCESS);
            finish();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mHandler.sendEmptyMessage(LOADING_END);
        mResponseCode = errorCode;
        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }
}
