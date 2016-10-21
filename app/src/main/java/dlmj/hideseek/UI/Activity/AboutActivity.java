package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.R;

/**
 * 关于页面<br/>
 * Created on 2016/10/19
 *
 * @author yekangqi
 */

public class AboutActivity extends Activity implements View.OnClickListener {

    private View mBackImageView;
    private View mBackTextView;
    private TextView mAppNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        findView();
        setListener();
    }

    private void findView() {
        mBackImageView =  findViewById(R.id.backImageView);
        mBackTextView =  findViewById(R.id.backTextView);
        mAppNameTextView = (TextView) findViewById(R.id.appNameTextView);

        mAppNameTextView.setText(getString(R.string.app_name)+" "+BaseInfoUtil.getPackageInfo(this).versionName);
    }

    private void setListener() {
        mBackTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImageView:
            case R.id.backTextView:
                finish();
                break;
        }
    }
}
