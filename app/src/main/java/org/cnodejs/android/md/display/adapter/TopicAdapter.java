package org.cnodejs.android.md.display.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cnodejs.android.md.R;
import org.cnodejs.android.md.display.activity.LoginActivity;
import org.cnodejs.android.md.display.activity.UserDetailActivity;
import org.cnodejs.android.md.display.util.ActivityUtils;
import org.cnodejs.android.md.display.util.ToastUtils;
import org.cnodejs.android.md.display.view.ITopicItemReplyView;
import org.cnodejs.android.md.display.view.ITopicReplyView;
import org.cnodejs.android.md.display.widget.ContentWebView;
import org.cnodejs.android.md.model.entity.Reply;
import org.cnodejs.android.md.model.storage.LoginShared;
import org.cnodejs.android.md.presenter.contract.ITopicItemReplyPresenter;
import org.cnodejs.android.md.presenter.implement.TopicItemReplyPresenter;
import org.cnodejs.android.md.util.FormatUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopicAdapter extends BaseAdapter {

    private final Activity activity;
    private final LayoutInflater inflater;
    private final List<Reply> replyList;
    private final Map<String, Integer> positionMap;
    private final ITopicReplyView topicReplyView;

    public TopicAdapter(@NonNull Activity activity, @NonNull List<Reply> replyList, @NonNull Map<String, Integer> positionMap, @NonNull ITopicReplyView topicReplyView) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        this.replyList = replyList;
        this.positionMap = positionMap;
        this.topicReplyView = topicReplyView;
    }

    @Override
    public int getCount() {
        return replyList.size();
    }

    @Override
    public Object getItem(int position) {
        return replyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_topic_item_reply, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.update(position);
        return convertView;
    }

    public class ViewHolder implements ITopicItemReplyView {

        @BindView(R.id.topic_item_reply_img_avatar)
        protected ImageView imgAvatar;

        @BindView(R.id.topic_item_reply_tv_login_name)
        protected TextView tvLoginName;

        @BindView(R.id.topic_item_reply_tv_index)
        protected TextView tvIndex;

        @BindView(R.id.topic_item_reply_tv_create_time)
        protected TextView tvCreateTime;

        @BindView(R.id.topic_item_reply_btn_ups)
        protected TextView btnUps;

        @BindView(R.id.topic_item_reply_tv_target_position)
        protected TextView tvTargetPosition;

        @BindView(R.id.topic_item_reply_web_content)
        protected ContentWebView webContent;

        @BindView(R.id.topic_item_reply_icon_deep_line)
        protected View iconDeepLine;

        @BindView(R.id.topic_item_reply_icon_shadow_gap)
        protected View iconShadowGap;

        private final ITopicItemReplyPresenter topicItemReplyPresenter;

        private Reply reply;
        private int position = -1;

        public ViewHolder(@NonNull View itemView) {
            ButterKnife.bind(this, itemView);
            topicItemReplyPresenter = new TopicItemReplyPresenter(activity, this);
        }

        public void update(int position) {
            this.position = position;
            reply = replyList.get(position);
            updateReplyViews(reply, position, positionMap.get(reply.getReplyId()));
        }

        @OnClick(R.id.topic_item_reply_img_avatar)
        protected void onBtnAvatarClick() {
            UserDetailActivity.startWithTransitionAnimation(activity, reply.getAuthor().getLoginName(), imgAvatar, reply.getAuthor().getAvatarUrl());
        }

        @OnClick(R.id.topic_item_reply_btn_ups)
        protected void onBtnUpsClick() {
            if (LoginActivity.startForResultWithAccessTokenCheck(activity)) {
                if (reply.getAuthor().getLoginName().equals(LoginShared.getLoginName(activity))) {
                    ToastUtils.with(activity).show(R.string.can_not_up_yourself_reply);
                } else {
                    topicItemReplyPresenter.upReplyAsyncTask(reply, position);
                }
            }
        }

        @OnClick(R.id.topic_item_reply_btn_at)
        protected void onBtnAtClick() {
            if (LoginActivity.startForResultWithAccessTokenCheck(activity)) {
                topicReplyView.onAt(reply, positionMap.get(reply.getId()));
            }
        }

        @Override
        public void updateReplyViews(@NonNull Reply reply, int position, @Nullable Integer targetPosition) {
            Glide.with(activity).load(reply.getAuthor().getAvatarUrl()).placeholder(R.drawable.image_placeholder).dontAnimate().into(imgAvatar);
            tvLoginName.setText(reply.getAuthor().getLoginName());
            tvIndex.setText(position + 1 + "楼");
            tvCreateTime.setText(FormatUtils.getRecentlyTimeText(reply.getCreateAt()));
            updateUpViews(reply);
            if (targetPosition == null) {
                tvTargetPosition.setVisibility(View.GONE);
            } else {
                tvTargetPosition.setVisibility(View.VISIBLE);
                tvTargetPosition.setText("回复：" + (targetPosition + 1) + "楼");
            }

            // 这里直接使用WebView，有性能问题
            webContent.loadRenderedContent(reply.getRenderedContent());

            iconDeepLine.setVisibility(position == replyList.size() - 1 ? View.GONE : View.VISIBLE);
            iconShadowGap.setVisibility(position == replyList.size() - 1 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void updateUpViews(@NonNull Reply reply) {
            btnUps.setText(String.valueOf(reply.getUpList().size()));
            btnUps.setCompoundDrawablesWithIntrinsicBounds(reply.getUpList().contains(LoginShared.getId(activity)) ? R.drawable.ic_thumb_up_theme_24dp : R.drawable.ic_thumb_up_grey600_24dp, 0, 0, 0);
        }

        @Override
        public boolean onUpReplyResultOk(@NonNull Reply reply, int position) {
            if (ActivityUtils.isAlive(activity)) {
                if (position == this.position) {
                    updateUpViews(reply);
                }
                return false;
            } else {
                return true;
            }
        }

    }

}
