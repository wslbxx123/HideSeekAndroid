package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import dlmj.hideseek.R;
import dlmj.hideseek.UI.Fragment.ExternalFragment;
import dlmj.hideseek.UI.Fragment.InternalFragment;

/**
 * Created by Two on 5/10/16.
 */
public class RegionActivity extends FragmentActivity {
    private FragmentTabHost mFragmentTabHost;
    private int mTabImage[] = {R.drawable.internal_back, R.drawable.external_back};
    private int mTabText[] = {R.string.internal, R.string.external};
    private Class mFragmentArray[] = { InternalFragment.class, ExternalFragment.class};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.region);
        findView();
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

    private View getView(int index) {
        View view = View.inflate(this, R.layout.tab_content, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView textView = (TextView)view.findViewById(R.id.text);

        imageView.setImageResource(mTabImage[index]);
        textView.setText(mTabText[index]);

        return view;
    }
}
