package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 20/10/2016.
 */
public class NewFriendAdapter extends BaseAdapter {
    private Context mContext;
    private List<User> mNewFriendList;
    private ImageLoader mImageLoader;
    private AcceptBtnOnClickedListener mAcceptBtnOnClickedListener;

    public NewFriendAdapter(Context context, List<User> newFriendList) {
        this.mContext = context;
        this.mNewFriendList = newFriendList;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    public void setAcceptBtnOnClickedListener(AcceptBtnOnClickedListener acceptBtnOnClickedListener) {
        this.mAcceptBtnOnClickedListener = acceptBtnOnClickedListener;
    }

    @Override
    public int getCount() {
        if(mNewFriendList != null) {
            return mNewFriendList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mNewFriendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.new_friend_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mFriendImageView = (CircleNetworkImageView) convertView.findViewById(R.id.friendImageView);
            viewHolder.mFriendNameTextView = (TextView) convertView.findViewById(R.id.friendNameTextView);
            viewHolder.mMessageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            viewHolder.mAcceptBtn = (Button) convertView.findViewById(R.id.acceptBtn);
            viewHolder.mStatusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final User user = (User)getItem(position);
        viewHolder.mFriendImageView.setDefaultImageResId(R.drawable.default_photo);
        viewHolder.mFriendImageView.setImageUrl(user.getSmallPhotoUrl(), mImageLoader);
        viewHolder.mFriendNameTextView.setText(user.getNickname());
        viewHolder.mMessageTextView.setText(user.getRequestMessage());

        if(user.getIsFriend()) {
            viewHolder.mAcceptBtn.setVisibility(View.GONE);
            viewHolder.mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mAcceptBtn.setVisibility(View.VISIBLE);
            viewHolder.mStatusTextView.setVisibility(View.GONE);
        }

        viewHolder.mAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAcceptBtnOnClickedListener.acceptBtnOnClicked(user.getPKId());
            }
        });

        return convertView;
    }

    private class ViewHolder {
        CircleNetworkImageView mFriendImageView;
        TextView mFriendNameTextView;
        TextView mMessageTextView;
        Button mAcceptBtn;
        TextView mStatusTextView;
    }

    public interface AcceptBtnOnClickedListener {
        void acceptBtnOnClicked(long friendId);
    }
}
