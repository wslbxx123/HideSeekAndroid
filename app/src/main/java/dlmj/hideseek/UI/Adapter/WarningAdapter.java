package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dlmj.hideseek.Common.Factory.GoalImageFactory;
import dlmj.hideseek.Common.Model.Warning;
import dlmj.hideseek.R;

/**
 * Created by Two on 11/10/2016.
 */
public class WarningAdapter extends BaseAdapter {
    private List<Warning> mWarningList;
    private Context mContext;
    private GoalImageFactory mGoalImageFactory;
    private GetBtnOnClickedListener mGetBtnOnClickedListener;

    public WarningAdapter(Context context, List<Warning> warningList) {
        mContext = context;
        mWarningList = warningList;
        mGoalImageFactory = new GoalImageFactory(mContext);
    }

    public void setGetBtnOnClickedListener(GetBtnOnClickedListener getBtnOnClickedListener) {
        this.mGetBtnOnClickedListener = getBtnOnClickedListener;
    }

    @Override
    public int getCount() {
        if (mWarningList != null) {
            return mWarningList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mWarningList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.warning_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mGoalImageView = (ImageView) convertView.findViewById(R.id.goalImageView);
            viewHolder.mMessageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            viewHolder.mGetBtn = (Button) convertView.findViewById(R.id.getButton);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Warning warning = (Warning)getItem(position);
        viewHolder.mGoalImageView.setImageResource(
                mGoalImageFactory.get(warning.getGoal().getType(), warning.getGoal().getShowTypeName()));
        viewHolder.mMessageTextView.setText(
                String.format(mContext.getString(R.string.watched_by_monster),
                        warning.getGoal().getGoalName(mContext)));
        viewHolder.mGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetBtnOnClickedListener.getBtnOnClicked(warning.getGoal().getPkId());
            }
        });

        return convertView;
    }

    class ViewHolder {
        ImageView mGoalImageView;
        TextView mMessageTextView;
        Button mGetBtn;
    }

    public interface GetBtnOnClickedListener {
        void getBtnOnClicked(long goalId);
    }
}
