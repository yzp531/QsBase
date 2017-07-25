package com.qsmaxmin.qsbase.common.aspect;

import android.os.Looper;

import com.qsmaxmin.qsbase.common.exception.QsException;
import com.qsmaxmin.qsbase.common.log.L;
import com.qsmaxmin.qsbase.common.utils.QsHelper;
import com.qsmaxmin.qsbase.mvp.model.QsConstants;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;

/**
 * @CreateBy qsmaxmin
 * @Date 2017/6/28 14:09
 * @Description
 */
@Aspect
public class ThreadAspect {

    private static final String POINTCUT_METHOD_MAIN        = "execution(@com.qsmaxmin.qsbase.common.aspect.ThreadPoint(com.qsmaxmin.qsbase.common.aspect.ThreadType.MAIN) * *(..))";
    private static final String POINTCUT_METHOD_HTTP        = "execution(@com.qsmaxmin.qsbase.common.aspect.ThreadPoint(com.qsmaxmin.qsbase.common.aspect.ThreadType.HTTP) * *(..))";
    private static final String POINTCUT_METHOD_WORK        = "execution(@com.qsmaxmin.qsbase.common.aspect.ThreadPoint(com.qsmaxmin.qsbase.common.aspect.ThreadType.WORK) * *(..))";
    private static final String POINTCUT_METHOD_SINGLE_WORK = "execution(@com.qsmaxmin.qsbase.common.aspect.ThreadPoint(com.qsmaxmin.qsbase.common.aspect.ThreadType.SINGLE_WORK) * *(..))";

    @Pointcut(value = POINTCUT_METHOD_MAIN) public void onMainPoint() {
    }

    @Pointcut(value = POINTCUT_METHOD_HTTP) public void onHttpPoint() {
    }

    @Pointcut(value = POINTCUT_METHOD_WORK) public void onWorkPoint() {
    }

    @Pointcut(value = POINTCUT_METHOD_SINGLE_WORK) public void onSingleWorkPoint() {
    }

    @Around("onMainPoint()") public Object onMainExecutor(final ProceedingJoinPoint joinPoint) throws Throwable {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            return joinPoint.proceed();
        } else {
            QsHelper.getInstance().getThreadHelper().getMainThread().execute(new Runnable() {
                @Override public void run() {
                    L.i("ThreadAspect", joinPoint.toShortString() + " in main thread... ");
                    startOriginalMethod(joinPoint);
                }
            });
        }
        return null;
    }

    @Around("onHttpPoint()") public Object onHttpExecutor(final ProceedingJoinPoint joinPoint) throws Throwable {
        QsHelper.getInstance().getThreadHelper().getHttpThreadPoll().execute(new Runnable() {
            @Override public void run() {
                if (QsHelper.getInstance().getApplication().isTokenAvailable()) {
                    L.i("ThreadAspect", joinPoint.toShortString() + " in http thread... (token is available)");
                    startOriginalMethod(joinPoint);
                } else {
                    synchronized (QsConstants.HTTP_THREAD_LOCKER) {
                        L.e("ThreadAspect", joinPoint.toShortString() + " in http thread... (token is disable, so wait 30s)");
                        try {
                            QsConstants.HTTP_THREAD_LOCKER.wait(30000);
                        } catch (QsException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        startOriginalMethod(joinPoint);
                    }
                }
            }
        });
        return null;
    }

    @Around("onWorkPoint()") public Object onWorkExecutor(final ProceedingJoinPoint joinPoint) throws Throwable {
        QsHelper.getInstance().getThreadHelper().getWorkThreadPoll().execute(new Runnable() {
            @Override public void run() {
                L.i("ThreadAspect", joinPoint.toShortString() + " in work thread... ");
                startOriginalMethod(joinPoint);
            }
        });
        return null;
    }

    @Around("onSingleWorkPoint()") public Object onSingleWorkExecutor(final ProceedingJoinPoint joinPoint) throws Throwable {
        QsHelper.getInstance().getThreadHelper().getSingleThreadPoll().execute(new Runnable() {
            @Override public void run() {
                L.i("ThreadAspect", joinPoint.toShortString() + " in single work thread... ");
                startOriginalMethod(joinPoint);
            }
        });
        return null;
    }

    /**
     * 执行原始方法，将异常映射到{@link com.qsmaxmin.qsbase.mvp.presenter.QsPresenter#methodError(QsException)}
     */
    private void startOriginalMethod(ProceedingJoinPoint joinPoint) {
        try {
            joinPoint.proceed();
        } catch (final QsException e0) {
            try {
                final Object target = joinPoint.getTarget();
                final Method methodError = target.getClass().getMethod("methodError", Throwable.class);
                if (methodError != null) QsHelper.getInstance().getThreadHelper().getMainThread().execute(new Runnable() {
                    @Override public void run() {
                        try {
                            methodError.invoke(target, e0);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
