package dlmj.hideseek.UI.Activity;

import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperToast;

import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.Common.Interfaces.OnUserLoginListener;
import dlmj.hideseek.Common.Interfaces.OnUserRegisterListener;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoginDialog;
import dlmj.hideseek.UI.View.RegisterDialog;

/**
 * Created by Two on 13/10/2016.
 */
public class BaseFragmentActivity extends FragmentActivity implements OnUserLoginListener, OnUserRegisterListener {
    protected LoginDialog mLoginDialog;
    private RegisterDialog mRegisterDialog;
    protected CustomSuperToast mToast;
    protected int mResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoginDialog = new LoginDialog(this);
        mRegisterDialog = new RegisterDialog(this);
        mLoginDialog.setOnUserLoginListener(this);
        mRegisterDialog.setOnUserRegisterListener(this);
        mToast = new CustomSuperToast(this);
        mToast.setListener(new SuperToast.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                if(mResponseCode == CodeParams.ERROR_SESSION_INVALID) {
                    UserInfoManager.getInstance().logout(BaseFragmentActivity.this);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mLoginDialog.isShowing()) {
            mLoginDialog.dismiss();
        }

        if(mRegisterDialog.isShowing()) {
            mRegisterDialog.dismiss();
        }

        super.onDestroy();
    }

    public LoginDialog getLoginDialog() {
        return mLoginDialog;
    }

    @Override
    public void showRegisterDialog() {
        mRegisterDialog.resetData();
        mRegisterDialog.show();
    }

    @Override
    public void goToUploadPhoto(String registerPhone, String registerPassword, String nickname) {
        Intent intent = new Intent();
        intent.setClass(this, UploadPhotoActivity.class);
        intent.putExtra(IntentExtraParam.PHONE, registerPhone);
        intent.putExtra(IntentExtraParam.PASSWORD, registerPassword);
        intent.putExtra(IntentExtraParam.NICKNAME, nickname);
        startActivityForResult(intent, IntroduceActivity.REGISTER_CODE);
        mLoginDialog.dismiss();
    }
}
