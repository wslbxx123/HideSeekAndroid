package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperToast;

import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.UI.View.CustomSuperToast;

/**
 * Created by Two on 13/10/2016.
 */
public class BaseFragment extends Fragment {
    protected CustomSuperToast mToast;
    protected int mResponseCode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToast = new CustomSuperToast(getActivity());
        mToast.setListener(new SuperToast.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                if(mResponseCode == CodeParams.ERROR_SESSION_INVALID) {
                    UserInfoManager.getInstance().logout(getActivity());
                }
            }
        });
    }
}
