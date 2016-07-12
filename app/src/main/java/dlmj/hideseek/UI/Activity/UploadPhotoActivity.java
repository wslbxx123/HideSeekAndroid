package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.MultipartRequest;
import dlmj.hideseek.BusinessLogic.Network.VolleyQueueController;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleImageView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 5/4/16.
 */
public class UploadPhotoActivity extends Activity {
    private final static String TAG = "UploadPhotoActivity";
    private final String FILE_NAME = "Upload";
    private final int CROP_REQUEST_CODE = 300;
    private final int CHOOSE_REGION = 200;
    private CircleImageView mPhotoCircleNetworkImageView;
    private LinearLayout mPhotoLayout;
    private MultipartRequest mMultipartRequest;
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;
    private String mPhone;
    private String mPassword;
    private String mNickname;
    private LinearLayout mSexLayout;
    private TextView mSexTextView;
    private LinearLayout mRegionLayout;
    private TextView mRegionTextView;
    private Button mRegisterButton;
    private String mImagePath;
    private CustomSuperToast mToast;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_photo);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mPhone = getIntent().getStringExtra(IntentExtraParam.PHONE);
        mPassword = getIntent().getStringExtra(IntentExtraParam.PASSWORD);
        mNickname = getIntent().getStringExtra(IntentExtraParam.NICKNAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch(requestCode) {
                case CROP_REQUEST_CODE:
                    cropImage((Bitmap)data.getParcelableExtra("data"));
                    break;
                case CHOOSE_REGION:
                    String region = data.getStringExtra(IntentExtraParam.REGION_NAME);
                    mRegionTextView.setText(region);
                default:
                    break;
            }
        }
    }

    private void cropImage(Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            mImagePath = BaseInfoUtil.getImagePath(UploadPhotoActivity.this, FILE_NAME);
            LogUtil.d(TAG, mImagePath);
            fileOutputStream = new FileOutputStream(mImagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            mPhotoCircleNetworkImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally{
            if(null != fileOutputStream){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void findView() {
        mPhotoCircleNetworkImageView = (CircleImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleNetworkImageView.setImageResource(R.drawable.default_photo);
        mPhotoCircleNetworkImageView.setImageResource(R.drawable.default_photo);
        mPhotoLayout = (LinearLayout) findViewById(R.id.photoLayout);
        mSexLayout = (LinearLayout) findViewById(R.id.sexLayout);
        mSexTextView = (TextView) findViewById(R.id.sexTextView);
        mRegionLayout = (LinearLayout) findViewById(R.id.regionLayout);
        mRegionTextView = (TextView) findViewById(R.id.regionTextView);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
        mToast = new CustomSuperToast(this);
        mLoadingDialog = new LoadingDialog(this);
    }

    private void setListener() {
        mPhotoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                intent.putExtra("noFaceDetection", false);
                startActivityForResult(intent, CROP_REQUEST_CODE);
            }
        });

        mListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtil.d(TAG, response);
                    JSONObject result = new JSONObject(response);
                    int code = result.getInt("code");
                    String userStr = result.getString("result");

                    UserCache.getInstance().setUser(userStr);

                    if(mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }

                    if(code == CodeParams.SUCCESS) {
                        Intent intent = new Intent(UploadPhotoActivity.this, MatchRoleActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }

                mToast.show(getString(R.string.error_connect_network_failed));
            }
        };

        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadPhotoActivity.this);
                final String[] sexes = {getString(R.string.male), getString(R.string.female),
                        getString(R.string.secret)};
                builder.setItems(sexes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mSexTextView.setText(sexes[which]);
                    }
                });
                builder.show();
            }
        });

        mRegionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(UploadPhotoActivity.this, RegionActivity.class);
                startActivityForResult(intent, CHOOSE_REGION);
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sexStr = mSexTextView.getText().toString();
                String region = mRegionTextView.getText().toString();
                User.SexEnum sex = User.SexEnum.notSet;

                Map<String, String> map = new HashMap<>();
                map.put("phone", mPhone);
                map.put("nickname", mNickname);
                map.put("password", mPassword);

                map.put("role", (int)(Math.random() * 5) + "");

                if(sexStr.equals(getString(R.string.female))) {
                    sex = User.SexEnum.female;
                } else if(sexStr.equals(getString(R.string.male))) {
                    sex = User.SexEnum.male;
                } else if(sexStr.equals(getString(R.string.secret))) {
                    sex = User.SexEnum.secret;
                }

                map.put("sex", sex.getValue() + "");

                if(!region.equals(getString(R.string.not_set))) {
                    map.put("region", region);
                }

                File file = null;
                if(mImagePath != null) {
                    file = new File(mImagePath);
                }

                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.show();
                }
                mMultipartRequest = new MultipartRequest(UrlParams.REGISTER_URL, mListener,
                        mErrorListener, "photo", file, map);
                VolleyQueueController.getInstance(UploadPhotoActivity.this)
                        .getRequestQueue().add(mMultipartRequest);
            }
        });
    }
}
