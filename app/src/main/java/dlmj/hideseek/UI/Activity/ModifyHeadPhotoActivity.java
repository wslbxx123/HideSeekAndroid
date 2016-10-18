package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * 修改头像<br/>
 * Created on 2016/10/18
 *
 * @author yekangqi
 */

public class ModifyHeadPhotoActivity extends BaseActivity {
    private static String TAG="ModifyHeadPhotoActivity";
    private static final int LOADING_END = 1;
    private static final int REQUEST_CODE_PROFILE=3;//修改头像
    private final String FILE_NAME = "Modify";
    private View mBackTextView;
    private View mSettingsLayout;
    private NetworkImageView mPhotoNetworkImageView;

    private ImageLoader mImageLoader;
    private Uri mImageUri;
    private LoadingDialog mLoadingDialog;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;
    private NetworkHelper mUploadPhotoHelper;
    private User mUser;

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
        setContentView(R.layout.photo);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mUploadPhotoHelper=new NetworkHelper(this);
        mUser = UserCache.getInstance().getUser();
        mImageLoader = ImageCacheManager.getInstance(getApplicationContext()).getImageLoader();
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mBackTextView =  findViewById(R.id.backTextView);
        mSettingsLayout =  findViewById(R.id.settingsLayout);
        mPhotoNetworkImageView = (NetworkImageView) findViewById(R.id.photoNetworkImageView);
        mPhotoNetworkImageView.setDefaultImageResId(R.drawable.default_photo);
        mPhotoNetworkImageView.setImageUrl(mUser.getPhotoUrl(), mImageLoader);

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);
    }

    private void setListener() {
        findViewById(R.id.backImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击修改
        mSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                String imagePath = BaseInfoUtil.getImagePath(ModifyHeadPhotoActivity.this, FILE_NAME, true);
                File imageFile = new File(imagePath);
                mImageUri = Uri.fromFile(imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                intent.putExtra("noFaceDetection", false);
                startActivityForResult(intent, REQUEST_CODE_PROFILE);
            }
        });

        //头像监听
        mUploadPhotoHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                mResponseCode = CodeParams.SUCCESS;
                mHandler.sendEmptyMessage(LOADING_END);
                try {
                    LogUtil.d(TAG, data.getResult());
                    JSONObject result = new JSONObject(data.getResult());
                    String bigphoto = result.getString("photo_url");
                    String small_photo_url=result.getString("small_photo_url");

                    mUser.setSmallPhotoUrl(small_photo_url);
                    mUser.setPhotoUrl(bigphoto);
                    UserCache.getInstance().update(mUser,"small_photo_url",small_photo_url);
                    UserCache.getInstance().update(mUser,"photo_url",bigphoto);
                    mPhotoNetworkImageView.setDefaultImageResId(0);
                    mPhotoNetworkImageView.setImageUrl(bigphoto,mImageLoader);
                    setResult(Activity.RESULT_OK);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                if(mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
                mResponseCode = errorCode;
                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
                mToast.show(getString(R.string.error_connect_network_failed));
            }
        });
    }

    private void cropImage() {
        try {
            if(mImageUri != null) {
                //执行上传图片
                File file = new File(new URI(mImageUri.toString()));
                if (file.exists() && file.isFile()) {
                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.show();
                    }
                    mUploadPhotoHelper.getRequestForPostWitFile(UrlParams.UPDATEPHOTOURL_URL, "photo", file, new HashMap<String, String>());
                    mImageUri=null;
                }
            }

        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_PROFILE)
        {
            cropImage();
        }
    }
}
