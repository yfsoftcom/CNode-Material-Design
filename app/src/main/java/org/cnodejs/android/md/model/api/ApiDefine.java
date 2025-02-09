package org.cnodejs.android.md.model.api;

public final class ApiDefine {

    private ApiDefine() {}

    public static final String HOST = "https://cnodejs.org";
    public static final String API_BASE_URL = HOST + "/api/v1/";
    public static final String USER_PATH_PREFIX = "/user/";
    public static final String USER_LINK_URL_PREFIX = HOST + USER_PATH_PREFIX;
    public static final String TOPIC_PATH_PREFIX = "/topic/";
    public static final String TOPIC_LINK_URL_PREFIX = HOST + TOPIC_PATH_PREFIX;

    public static final boolean MD_RENDER = true; // 使用服务端Markdown渲染可以轻微提升性能

}
