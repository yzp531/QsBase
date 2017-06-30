package com.qsmaxmin.qsbase.common.threadpoll;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @CreateBy qsmaxmin
 * @Date 2017/3/21 20:14
 * @Description
 */

public class HttpThreadPoll extends ThreadPoolExecutor {

    HttpThreadPoll(int threadCount) {
        super(threadCount, threadCount, 0, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(), ThreadUtils.generateThread("HttpThreadPoll", true));
    }
}
