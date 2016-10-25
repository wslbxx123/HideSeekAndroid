package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Fragment.ExchangeOrderFragment;
import dlmj.hideseek.UI.Fragment.PurchaseOrderFragment;

/**
 * 创建者     ZPL
 * 创建时间   2016/8/1 22:31
 * 描述	    订单详情页
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MyOrderActivity extends BaseFragmentActivity implements View.OnClickListener{
    private String mLastTitle;
    private TextView mLeft;
    private TextView mRight;
    private ViewPager mViewPager;
    private TextView mLastTitleTextView;
    private LinearLayout mBackLayout;
    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_order);
        initData();
        findView();
        setListener();
    }

    private void setListener() {
        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
        mLeft.setSelected(true);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mLeft.setSelected(true);
                        mRight.setSelected(false);
                        break;
                    case 1:
                        mLeft.setSelected(false);
                        mRight.setSelected(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initData() {
        mLastTitle = getIntent().getStringExtra(IntentExtraParam.LAST_TITLE);
    }

    private void findView() {
        mFragmentList.add(new PurchaseOrderFragment());
        mFragmentList.add(new ExchangeOrderFragment());
        mLeft = (TextView) findViewById(R.id.tv_left);
        mRight = (TextView) findViewById(R.id.tv_right);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }
        });
        mLastTitleTextView = (TextView) findViewById(R.id.lastTitleTextView);
        mLastTitleTextView.setText(mLastTitle);
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left:
                mLeft.setSelected(true);
                mRight.setSelected(false);
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_right:
                mLeft.setSelected(false);
                mRight.setSelected(true);
                mViewPager.setCurrentItem(1);
                break;
        }
    }
}
