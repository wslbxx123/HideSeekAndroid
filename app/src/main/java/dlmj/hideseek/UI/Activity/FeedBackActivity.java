package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * 意见反馈<br/>
 * Created on 2016/10/19
 *
 * @author yekangqi
 */

public class FeedBackActivity extends BaseActivity implements View.OnClickListener, UIDataListener<Bean>, TextWatcher {
    private static final String TAG="FeedBackActivity";
    private static final int LOADING_END = 1;
    private String type = "0";//type 0（建议），1（咨询），2（投诉）
    private View mBackImageView;
    private View mBackTextView;
    private Button mAdviceButton;
    private Button mConsultationButton;
    private Button mComplaintsButton;
    private Button mSubmitButton;
    private EditText mContactsEditText;
    private EditText mContentEditText;

    private NetworkHelper mNetworkHelper;
    private LoadingDialog mLoadingDialog;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mBackImageView =  findViewById(R.id.backImageView);
        mBackTextView =  findViewById(R.id.backTextView);
        mAdviceButton = (Button) findViewById(R.id.adviceButton);
        mConsultationButton = (Button) findViewById(R.id.consultationButton);
        mComplaintsButton = (Button) findViewById(R.id.complaintsButton);
        mSubmitButton = (Button) findViewById(R.id.submitButton);
        mContactsEditText = (EditText) findViewById(R.id.contactsEditText);
        mContentEditText = (EditText) findViewById(R.id.contentEditText);

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);

        mContentEditText.clearFocus();
        mContactsEditText.clearFocus();
    }

    private void setListener() {
        mBackTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mAdviceButton.setOnClickListener(this);
        mConsultationButton.setOnClickListener(this);
        mComplaintsButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);

        mContentEditText.addTextChangedListener(this);
        mContactsEditText.addTextChangedListener(this);
        mNetworkHelper.setUiDataListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImageView:
            case R.id.backTextView:
                finish();
                break;
            case R.id.adviceButton://意见
                type="0";
                mAdviceButton.setBackgroundResource(R.drawable.feedback_type_back_press);
                mConsultationButton.setBackgroundResource(R.drawable.feedback_type_back);
                mComplaintsButton.setBackgroundResource(R.drawable.feedback_type_back);
                break;
            case R.id.consultationButton://咨询
                type="1";
                mAdviceButton.setBackgroundResource(R.drawable.feedback_type_back);
                mConsultationButton.setBackgroundResource(R.drawable.feedback_type_back_press);
                mComplaintsButton.setBackgroundResource(R.drawable.feedback_type_back);
                break;
            case R.id.complaintsButton://投诉
                type="2";
                mAdviceButton.setBackgroundResource(R.drawable.feedback_type_back);
                mConsultationButton.setBackgroundResource(R.drawable.feedback_type_back);
                mComplaintsButton.setBackgroundResource(R.drawable.feedback_type_back_press);
                break;
            case R.id.submitButton://提交
                submit();
                break;
        }
    }

    private void submit(){
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("type",type);
        params.put("content",mContentEditText.getText().toString());
        params.put("contact",mContactsEditText.getText().toString());
        mNetworkHelper.sendPostRequest(UrlParams.ADDFEEDBACK_URL,params);
    }

    @Override
    public void onDataChanged(Bean data) {
        mResponseCode = CodeParams.SUCCESS;
        mHandler.sendEmptyMessage(LOADING_END);
        mContactsEditText.setText("");
        mContentEditText.setText("");
        mToast.show(getString(R.string.submit_success), CustomSuperToast.MessageType.success);
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mHandler.sendEmptyMessage(LOADING_END);
        mResponseCode = errorCode;
        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(mContentEditText.getText().toString()) || TextUtils.isEmpty(mContactsEditText.getText().toString()))
        {
            mSubmitButton.setEnabled(false);
        }else
        {
            mSubmitButton.setEnabled(true);
        }
    }
}
