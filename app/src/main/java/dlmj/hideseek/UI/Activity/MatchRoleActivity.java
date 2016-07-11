package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/17/16.
 */
public class MatchRoleActivity extends Activity {
    private RelativeLayout mRoleLayout;
    private AnimationDrawable mAnimationDrawable;
    private TextView mRoleTextView;
    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_role);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                User user = UserCache.getInstance().getUser();
                mAnimationDrawable.stop();
                mRoleLayout.setBackgroundResource(user.getRoleDrawableId());
                mRoleTextView.setText(user.getRole().toString(MatchRoleActivity.this));
            }
        }, 3000);
    }

    private void findView() {
        mRoleLayout = (RelativeLayout) findViewById(R.id.roleLayout);
        mAnimationDrawable = (AnimationDrawable) mRoleLayout.getBackground();
        mAnimationDrawable.start();
        mRoleTextView = (TextView) findViewById(R.id.roleTextView);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
    }

    private void setListener() {
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatchRoleActivity.this.finish();
            }
        });
    }
}
