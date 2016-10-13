package dlmj.hideseek.UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Fragment.MeFragment;
import dlmj.hideseek.UI.Fragment.RaceGroupFragment;
import dlmj.hideseek.UI.Fragment.RecordFragment;
import dlmj.hideseek.UI.Fragment.SearchFragment;

//首页
public class IntroduceActivity extends BaseFragmentActivity{
    private final String TAG = "Introduce Activity";
    public final static int GO_TO_WARNING = 100;
    public final static int REGISTER_CODE = 300;
    private FragmentTabHost mFragmentTabHost;
    private int mTabImage[] = {R.drawable.home_back, R.drawable.record_back,
            R.drawable.friend_group_back, R.drawable.me_back};
    private int mTabText[] = {R.string.home, R.string.record, R.string.race_group, R.string.me};
    private Class mFragmentArray[] = {SearchFragment.class, RecordFragment.class,
            RaceGroupFragment.class, MeFragment.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduce);
        findView();
        setListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch(requestCode) {
                case REGISTER_CODE:

                    break;
                case GO_TO_WARNING:
                    SearchFragment searchFragment = (SearchFragment)getSupportFragmentManager()
                            .findFragmentById(0);
                    long goalId = data.getLongExtra(IntentExtraParam.GOAL_ID, 0);
                    searchFragment.updateEndGoal(goalId);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findView() {
        mFragmentTabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        mFragmentTabHost.setup(this, getSupportFragmentManager(),
                R.id.mainContent);

        for(int i = 0; i < mTabText.length; i++) {
            TabHost.TabSpec spec = mFragmentTabHost
                    .newTabSpec(this.getString(mTabText[i]))
                    .setIndicator(getView(i));
            mFragmentTabHost.addTab(spec, mFragmentArray[i], null);
            mFragmentTabHost.getTabWidget().getChildAt(i);
        }
    }

    private void setListener() {

    }

    private View getView(int index) {
        View view = View.inflate(this, R.layout.tab_content, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView textView = (TextView)view.findViewById(R.id.text);

        imageView.setImageResource(mTabImage[index]);
        textView.setText(mTabText[index]);

        return view;
    }

    public FragmentTabHost getFragmentTabHost() {
        return mFragmentTabHost;
    }

}
