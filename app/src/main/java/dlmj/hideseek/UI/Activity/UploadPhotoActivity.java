package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.MultiPartJsonRequest;
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
    private static final String TAG = "UploadPhotoActivity";
    private final String FILE_NAME = "Upload";
    private final int CROP_REQUEST_CODE = 300;
    private final int CHOOSE_REGION = 200;
    private CircleImageView mPhotoCircleNetworkImageView;
    private LinearLayout mPhotoLayout;
    private MultiPartJsonRequest mMultiPartRequest;
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
    private Uri mImageUri;
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
                    cropImage();
                    break;
                case CHOOSE_REGION:
                    String region = data.getStringExtra(IntentExtraParam.REGION_NAME);
                    mRegionTextView.setText(region);
                default:
                    break;
            }
        }
    }

    private void cropImage() {
        try {
            if(mImageUri != null){
                LogUtil.d(TAG, mImageUri.toString());
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(mImageUri));
                mPhotoCircleNetworkImageView.setImageBitmap(bitmap);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
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
        mLoadingDialog = new LoadingDialog(this, this.getString(R.string.loading));
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
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                String imagePath = BaseInfoUtil.getImagePath(UploadPhotoActivity.this, FILE_NAME, true);
                File imageFile = new File(imagePath);
                mImageUri = Uri.fromFile(imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
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
                final String[] sexes = { getString(R.string.female),getString(R.string.male),
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
                try {
                    String sexStr = mSexTextView.getText().toString();
                    String region = mRegionTextView.getText().toString();
                    User.SexEnum sex = User.SexEnum.notSet;

                    Map<String, String> map = new HashMap<>();
                    map.put("phone", mPhone);
                    map.put("nickname", mNickname);
                    map.put("password", mPassword);
                    map.put("app_platform", "1");

                    map.put("role", (int) (Math.random() * 5) + "");

                    if (sexStr.equals(getString(R.string.female))) {
                        sex = User.SexEnum.female;
                    } else if (sexStr.equals(getString(R.string.male))) {
                        sex = User.SexEnum.male;
                    } else if (sexStr.equals(getString(R.string.secret))) {
                        sex = User.SexEnum.secret;
                    }

                    map.put("sex", sex.getValue() + "");

                    if (!region.equals(getString(R.string.not_set))) {
                        map.put("region", region);
                    }

                    File file = null;
                    if (mImageUri != null) {
                        file = new File(new URI(mImageUri.toString()));
                    }

                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.show();
                    }
                    mMultiPartRequest = new MultiPartJsonRequest(UrlParams.REGISTER_URL, mListener,
                            mErrorListener, "photo", file, map);
                    VolleyQueueController.getInstance(UploadPhotoActivity.this)
                            .getRequestQueue().add(mMultiPartRequest);
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
