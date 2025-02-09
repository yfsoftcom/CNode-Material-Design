package org.cnodejs.android.md.display.viewholder;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cnodejs.android.md.R;
import org.cnodejs.android.md.display.activity.LoginActivity;
import org.cnodejs.android.md.display.activity.UserDetailActivity;
import org.cnodejs.android.md.display.util.ActivityUtils;
import org.cnodejs.android.md.display.view.ITopicHeaderView;
import org.cnodejs.android.md.display.widget.ContentWebView;
import org.cnodejs.android.md.model.entity.Result;
import org.cnodejs.android.md.model.entity.Topic;
import org.cnodejs.android.md.model.entity.TopicWithReply;
import org.cnodejs.android.md.presenter.contract.ITopicHeaderPresenter;
import org.cnodejs.android.md.presenter.implement.TopicHeaderPresenter;
import org.cnodejs.android.md.util.FormatUtils;
import org.cnodejs.android.md.util.ResUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopicHeaderViewHolder implements ITopicHeaderView {

    @BindView(R.id.topic_header_layout_content)
    protected ViewGroup layoutContent;

    @BindView(R.id.topic_header_icon_good)
    protected View iconGood;

    @BindView(R.id.topic_header_tv_title)
    protected TextView tvTitle;

    @BindView(R.id.topic_header_img_avatar)
    protected ImageView imgAvatar;

    @BindView(R.id.topic_header_tv_tab)
    protected TextView tvTab;

    @BindView(R.id.topic_header_tv_login_name)
    protected TextView tvLoginName;

    @BindView(R.id.topic_header_tv_create_time)
    protected TextView tvCreateTime;

    @BindView(R.id.topic_header_tv_visit_count)
    protected TextView tvVisitCount;

    @BindView(R.id.topic_header_btn_favorite)
    protected ImageView btnFavorite;

    @BindView(R.id.topic_header_web_content)
    protected ContentWebView webContent;

    @BindView(R.id.topic_header_layout_no_reply)
    protected ViewGroup layoutNoReply;

    @BindView(R.id.topic_header_layout_reply_count)
    protected ViewGroup layoutReplyCount;

    @BindView(R.id.topic_header_tv_reply_count)
    protected TextView tvReplyCount;

    private final Activity activity;
    private Topic topic;
    private boolean isCollect;

    private final ITopicHeaderPresenter topicHeaderPresenter;

    public TopicHeaderViewHolder(@NonNull Activity activity, @NonNull ListView listView) {
        this.activity = activity;
        LayoutInflater inflater = LayoutInflater.from(activity);
        View headerView = inflater.inflate(R.layout.activity_topic_header, listView, false);
        ButterKnife.bind(this, headerView);
        listView.addHeaderView(headerView, null, false);
        this.topicHeaderPresenter = new TopicHeaderPresenter(activity, this);
    }

    @OnClick(R.id.topic_header_img_avatar)
    protected void onBtnAvatarClick() {
        UserDetailActivity.startWithTransitionAnimation(activity, topic.getAuthor().getLoginName(), imgAvatar, topic.getAuthor().getAvatarUrl());
    }

    @OnClick(R.id.topic_header_btn_favorite)
    protected void onBtnFavoriteClick() {
        if (topic != null) {
            if (LoginActivity.startForResultWithAccessTokenCheck(activity)) {
                if (isCollect) {
                    topicHeaderPresenter.decollectTopicAsyncTask(topic.getId());
                } else {
                    topicHeaderPresenter.collectTopicAsyncTask(topic.getId());
                }
            }
        }
    }

    @Override
    public void updateViews(@Nullable Topic topic, boolean isCollect, int replyCount) {
        this.topic = topic;
        this.isCollect = isCollect;
        if (topic != null) {
            layoutContent.setVisibility(View.VISIBLE);
            iconGood.setVisibility(topic.isGood() ? View.VISIBLE : View.GONE);

            tvTitle.setText(topic.getTitle());
            Glide.with(activity).load(topic.getAuthor().getAvatarUrl()).placeholder(R.drawable.image_placeholder).dontAnimate().into(imgAvatar);
            tvTab.setText(topic.isTop() ? R.string.tab_top : topic.getTab().getNameId());
            tvTab.setBackgroundDrawable(ResUtils.getThemeAttrDrawable(activity, topic.isTop() ? R.attr.referenceBackgroundAccent : R.attr.referenceBackgroundNormal));
            tvTab.setTextColor(topic.isTop() ? Color.WHITE : ResUtils.getThemeAttrColor(activity, android.R.attr.textColorSecondary));
            tvLoginName.setText(topic.getAuthor().getLoginName());
            tvCreateTime.setText(FormatUtils.getRecentlyTimeText(topic.getCreateAt()) + "创建");
            tvVisitCount.setText(topic.getVisitCount() + "次浏览");
            btnFavorite.setImageResource(isCollect ? R.drawable.ic_favorite_theme_24dp : R.drawable.ic_favorite_outline_grey600_24dp);

            // 这里直接使用WebView，有性能问题
            webContent.loadRenderedContent(topic.getRenderedContent());

            updateReplyCount(replyCount);
        } else {
            layoutContent.setVisibility(View.GONE);
            iconGood.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateViews(@NonNull TopicWithReply topic) {
        updateViews(topic, topic.isCollect(), topic.getReplyList().size());
    }

    @Override
    public void updateReplyCount(int replyCount) {
        layoutNoReply.setVisibility(replyCount > 0 ? View.GONE : View.VISIBLE);
        layoutReplyCount.setVisibility(replyCount > 0 ? View.VISIBLE : View.GONE);
        tvReplyCount.setText(replyCount + "条回复");
    }

    @Override
    public boolean onCollectTopicResultOk(Result result) {
        if (ActivityUtils.isAlive(activity)) {
            isCollect = true;
            btnFavorite.setImageResource(R.drawable.ic_favorite_theme_24dp);
        }
        return false;
    }

    @Override
    public boolean onDecollectTopicResultOk(Result result) {
        if (ActivityUtils.isAlive(activity)) {
            isCollect = false;
            btnFavorite.setImageResource(R.drawable.ic_favorite_outline_grey600_24dp);
        }
        return false;
    }

}
