package com.qsmaxmin.qsbase.mvp.model;

/**
 * @CreateBy qsmaxmin
 * @Date 2017/6/21 15:19
 * @Description
 */

public class QsConstants {
    private QsConstants() {
    }

    /**
     * 请求token的http线程锁
     */
    public static final Object HTTP_THREAD_LOCKER = new Object();
    /**
     * View状态布局
     */
    public static final int    VIEW_STATE_LOADING = 0;
    public static final int    VIEW_STATE_CONTENT = 1;
    public static final int    VIEW_STATE_EMPTY   = 2;
    public static final int    VIEW_STATE_ERROR   = 3;

}
