package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import dlmj.hideseek.R;
import dlmj.hideseek.UI.Fragment.ExchangeFragment;
import dlmj.hideseek.UI.Fragment.ShopFragment;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 15:55
 * 描述	     商店页
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class StoreActivity extends FragmentActivity implements View.OnClickListener {


    private TextView mLeft;
    private TextView mRight;
    private FragmentManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
    }

    private void initData() {
        mLeft.setSelected(true);
        mManager = getSupportFragmentManager();
        mManager.beginTransaction().add(R.id.frameLayout,new ShopFragment()).commit();
    }

    private void initView() {
        mLeft = (TextView) findViewById(R.id.tv_left);
        mRight = (TextView) findViewById(R.id.tv_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left:
                mLeft.setSelected(true);
                mRight.setSelected(false);
                mManager.beginTransaction().replace(R.id.frameLayout,new ShopFragment()).commit();
                break;
            case R.id.tv_right:
                mLeft.setSelected(false);
                mRight.setSelected(true);
                mManager.beginTransaction().replace(R.id.frameLayout,new ExchangeFragment()).commit();
                break;
        }
    }
}
