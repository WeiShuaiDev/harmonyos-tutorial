package com.waylau.hmos.shortvideo.slice;

import java.util.ArrayList;
import java.util.List;

import com.waylau.hmos.shortvideo.MePageAbility;
import com.waylau.hmos.shortvideo.ResourceTable;
import com.waylau.hmos.shortvideo.bean.UserInfo;
import com.waylau.hmos.shortvideo.bean.VideoInfo;
import com.waylau.hmos.shortvideo.constant.Constants;
import com.waylau.hmos.shortvideo.store.VideoInfoRepository;
import com.waylau.hmos.shortvideo.util.CommonUtil;
import com.waylau.hmos.shortvideo.util.LogUtil;

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Image;
import ohos.agp.components.TextField;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.utils.zson.ZSONArray;

/**
 * 视频发布页面
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-01-26
 */
public class VideoPublishPageAbilitySlice extends AbilitySlice {
    private static final String TAG = VideoPublishPageAbilitySlice.class.getSimpleName();

    // 视频信息列表
    private final List<VideoInfo> videoInfoList = new ArrayList<>();

    private final VideoInfo videoInfo = new VideoInfo();

    private Image imageVideoCover = null; // 封面
    private TextField textVideoContent = null; // 视频内容
    private Button buttonPublish = null; // 发布
    private UserInfo userInfo = new UserInfo();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_video_publish_layout);

        userInfo.setUsername(intent.getStringParam(Constants.LOGIN_USERNAME));
        userInfo.setPortraitPath(intent.getStringParam(Constants.IMAGE_SELECTION));

        // 初始化数据
        initData();

        // 初始化UI组件
        initUi();

        // 初始化事件监听
        initListener();
    }

    private void initData() {
        String resourcePath = "resources/rawfile/videoinfo.json";
        String videosJson = CommonUtil.getJsonFileToString(this, resourcePath);

        // json字符串转成对象集合
        List<VideoInfo> videoInfos = ZSONArray.stringToClassList(videosJson, VideoInfo.class);
        videoInfoList.clear();
        videoInfoList.addAll(videoInfos);
    }

    private void initUi() {
        imageVideoCover = (Image)findComponentById(ResourceTable.Id_image_video_cover);
        textVideoContent = (TextField)findComponentById(ResourceTable.Id_textfield_video_content);
        buttonPublish = (Button)findComponentById(ResourceTable.Id_button_video_publish);
    }

    private void initListener() {
        imageVideoCover.setClickedListener(component -> {
            presentForResult(new VideoSelectionAbilitySlice(), new Intent(), 0);

        });

        buttonPublish.setClickedListener(component -> {
            String videoContent = textVideoContent.getText();
            videoInfo.setContent(videoContent);
            videoInfo.setAuthor(userInfo.getUsername());

            checkPublish(videoInfo);
        });

    }

    private void checkPublish(VideoInfo video) {
        if (video.getCoverPath() == null || video.getVideoPath() == null || video.getCoverPath().isEmpty()
            || video.getVideoPath().isEmpty()) {
            new ToastDialog(getContext()).setText("请选择要发布的视频！").setAlignment(LayoutAlignment.CENTER).show();
        } else if (video.getContent() == null || video.getContent().isEmpty()) {
            new ToastDialog(getContext()).setText("请输入视频的内容！").setAlignment(LayoutAlignment.CENTER).show();
        }

        // 发布
        VideoInfoRepository.insert(video);

        Intent intent = new Intent();
        intent.setParam(Constants.LOGIN_USERNAME, userInfo.getUsername());
        intent.setParam(Constants.IMAGE_SELECTION, userInfo.getPortraitPath());

        Operation operation = new Intent.OperationBuilder().withAbilityName(MePageAbility.class)
                .withBundleName("com.waylau.hmos.shortvideo").build();

        intent.setOperation(operation);

        // 启动Ability
        startAbility(intent);

        terminate();
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onResult(int requestCode, Intent resultIntent) {
        LogUtil.info(TAG, "onResult requestCode:" + requestCode + "; resultIntent:" + resultIntent);
        if (requestCode == 0) {
            videoInfo.setCoverPath(resultIntent.getStringParam(Constants.IMAGE_SELECTION));
            videoInfo.setVideoPath(resultIntent.getStringParam(Constants.VIDEO_SELECTION));
            videoInfo.setPortraitPath(userInfo.getPortraitPath());

            // 刷新视频封面
            imageVideoCover.setPixelMap(CommonUtil.getImageSource(this.getContext(), videoInfo.getCoverPath()));

        } else {
            terminate();
        }
    }

    // 返回
    @Override
    protected void onBackPressed() {
        super.onBackPressed();
    }
}