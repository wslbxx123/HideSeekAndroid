package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import dlmj.hideseek.R;

/**
 * 透明提示引导<br/>
 * Created on 2016/10/26
 *
 * @author yekangqi
 */

public class TipsGuideActivity extends Activity implements View.OnClickListener{
    private int clickCount;
    private View guide01,guide02,guide03,guide04,guideArrow,guide05;
    private ImageView mSubmitImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        clickCount=0;
    }

    private void findView() {
        guide01=findViewById(R.id.guide01);
        guide02=findViewById(R.id.guide02);
        guide03=findViewById(R.id.guide03);
        guide04=findViewById(R.id.guide04);
        guideArrow=findViewById(R.id.guideArrow);
        guide05=findViewById(R.id.guide05);
        mSubmitImageView= (ImageView) findViewById(R.id.submitImageView);
    }

    private void setListener() {
        mSubmitImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.submitImageView:
                onGuideClick();
                break;
        }
    }

    private void onGuideClick() {
        switch (clickCount)
        {
            case 0:
                guide01.setVisibility(View.GONE);
                guide02.setVisibility(View.VISIBLE);
                guide03.setVisibility(View.VISIBLE);
                clickCount++;
                break;
            case 1:
                guide02.setVisibility(View.GONE);
                guide03.setVisibility(View.GONE);
                guide04.setVisibility(View.VISIBLE);
                clickCount++;
                break;
            case 2:
                guide04.setVisibility(View.GONE);
                guideArrow.setVisibility(View.VISIBLE);
                guide05.setVisibility(View.VISIBLE);
                mSubmitImageView.setImageResource(R.drawable.guide_start);
                clickCount++;
                break;
            case 3:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }
}
