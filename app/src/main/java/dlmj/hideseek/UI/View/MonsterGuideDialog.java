package dlmj.hideseek.UI.View;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import dlmj.hideseek.Common.Factory.GoalImageFactory;
import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.R;

/**
 * Created by Two on 21/10/2016.
 */
public class MonsterGuideDialog extends Dialog {
    private Context mContext;
    private ImageView mGoalImageView;
    private ImageView mFirstStarImageView;
    private ImageView mSecondStarImageView;
    private ImageView mThirdStarImageView;
    private ImageView mFourthStarImageView;
    private ImageView mFifthStarImageView;
    private TextView mBattleRoleTextView;
    private TextView mIntroductionTextView;
    private ImageButton mCloseBtn;
    private GoalImageFactory mGoalImageFactory;

    public MonsterGuideDialog(Context context) {
        super(context, R.style.MonsterGuideDialog);
        this.mContext = context;
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mGoalImageFactory = new GoalImageFactory(mContext);
    }

    public void findView() {
        View guideView = LayoutInflater.from(mContext).inflate(R.layout.monster_guide, null);
        setContentView(guideView);
        mGoalImageView = (ImageView) guideView.findViewById(R.id.goalImageView);
        mFirstStarImageView = (ImageView) guideView.findViewById(R.id.firstStarImageView);
        mSecondStarImageView = (ImageView) guideView.findViewById(R.id.secondStarImageView);
        mThirdStarImageView = (ImageView) guideView.findViewById(R.id.thirdStarImageView);
        mFourthStarImageView = (ImageView) guideView.findViewById(R.id.fourthStarImageView);
        mFifthStarImageView = (ImageView) guideView.findViewById(R.id.fifthStarImageView);
        mBattleRoleTextView = (TextView) guideView.findViewById(R.id.battleRoleTextView);
        mIntroductionTextView = (TextView) guideView.findViewById(R.id.introductionTextView);
        mCloseBtn = (ImageButton) guideView.findViewById(R.id.closeBtn);
    }

    public void setListener() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setEndGoal(Goal goal) {
        mGoalImageView.setImageResource(mGoalImageFactory.get(goal.getType(), goal.getShowTypeName()));
        mIntroductionTextView.setText(goal.getIntroduction());
        initStar(goal.getScore());
        
        if(goal.getUnionType() == 1) {
            if(goal.getScore() < 0) {
                mBattleRoleTextView.setText(mContext.getString(R.string.none));
            } else {
                mBattleRoleTextView.setText(
                        String.format(mContext.getString(R.string.league_race), goal.getUnionType()));
            }
        } else if(goal.getUnionType() > 1) {
            mBattleRoleTextView.setText(
                    String.format(mContext.getString(R.string.league_races), goal.getUnionType()));
        }

    }

    public void initStar(int num) {
        int tempNum = Math.abs(num);
        mFirstStarImageView.setImageResource(R.drawable.star);
        mSecondStarImageView.setImageResource(R.drawable.star);
        mThirdStarImageView.setImageResource(R.drawable.star);
        mFourthStarImageView.setImageResource(R.drawable.star);
        mFifthStarImageView.setImageResource(R.drawable.star);

        if(tempNum >= 1) {
            mFirstStarImageView.setImageResource(R.drawable.star_selected);
        }

        if(tempNum >= 2) {
            mSecondStarImageView.setImageResource(R.drawable.star_selected);
        }

        if(tempNum >= 3) {
            mThirdStarImageView.setImageResource(R.drawable.star_selected);
        }

        if(tempNum >= 4) {
            mFourthStarImageView.setImageResource(R.drawable.star_selected);
        }

        if(tempNum >= 5) {
            mFifthStarImageView.setImageResource(R.drawable.star_selected);
        }
    }
}
