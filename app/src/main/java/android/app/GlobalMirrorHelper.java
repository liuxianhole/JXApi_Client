//package android.app;
//import android.app.Activity;
//import android.app.Application;
//import android.graphics.Canvas;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.annotation.NonNull;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 全局镜像工具类（支持左右/上下镜像，一键开启/关闭）
// */
//public class GlobalMirrorHelper {
//    private static GlobalMirrorHelper sInstance;
//    private final Application mApp;
//    // 存储每个Activity的镜像代理（避免内存泄漏）
//    private final Map<Activity, MirrorDrawProxy> mProxyMap = new HashMap<>();
//    // 镜像类型：LEFT_RIGHT（左右）、TOP_BOTTOM（上下）、NONE（关闭）
//    private MirrorType mMirrorType = MirrorType.NONE;
//
//    public enum MirrorType {
//        NONE, LEFT_RIGHT, TOP_BOTTOM
//    }
//
//    private GlobalMirrorHelper(Application app) {
//        this.mApp = app;
//        registerActivityLifecycle();
//    }
//
//    // 单例初始化（在Application中调用）
//    public static void init(Application app) {
//        if (sInstance == null) {
//            synchronized (GlobalMirrorHelper.class) {
//                if (sInstance == null) {
//                    sInstance = new GlobalMirrorHelper(app);
//                }
//            }
//        }
//    }
//
//    public static GlobalMirrorHelper getInstance() {
//        if (sInstance == null) {
//            throw new IllegalStateException("请先调用 GlobalMirrorHelper.init(Application)");
//        }
//        return sInstance;
//    }
//
//    // 开启/关闭镜像
//    public void setMirrorType(MirrorType type) {
//        mMirrorType = type;
//        // 刷新所有已启动的Activity
//        for (Map.Entry<Activity, MirrorDrawProxy> entry : mProxyMap.entrySet()) {
//            Activity activity = entry.getKey();
//            if (!activity.isFinishing() && !activity.isDestroyed()) {
//                entry.getValue().updateMirrorType(type);
//                activity.getWindow().getDecorView().invalidate(); // 触发重绘
//            }
//        }
//    }
//
//    // 监听所有Activity的生命周期，添加镜像代理
//    private void registerActivityLifecycle() {
//        mApp.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
//            @Override
//            public void onActivityCreated(@NonNull Activity activity, android.os.Bundle savedInstanceState) {
//                // 给Activity的根视图（DecorView）添加镜像代理
//                View decorView = activity.getWindow().getDecorView();
//                MirrorDrawProxy proxy = new MirrorDrawProxy(decorView, mMirrorType);
//                mProxyMap.put(activity, proxy);
//            }
//
//            @Override
//            public void onActivityDestroyed(@NonNull Activity activity) {
//                // 移除代理，避免内存泄漏
//                mProxyMap.remove(activity);
//            }
//
//            // 其他生命周期方法空实现
//            @Override public void onActivityStarted(@NonNull Activity activity) {}
//            @Override public void onActivityResumed(@NonNull Activity activity) {}
//            @Override public void onActivityPaused(@NonNull Activity activity) {}
//            @Override public void onActivityStopped(@NonNull Activity activity) {}
//            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {}
//        });
//    }
//
//    /**
//     * 镜像绘制代理：通过包装DecorView，修改其dispatchDraw的Canvas矩阵
//     */
//    private static class MirrorDrawProxy {
//        private final View mTargetView; // 目标视图（DecorView）
//        private MirrorType mCurrentType;
//
//        public MirrorDrawProxy(View targetView, MirrorType type) {
//            this.mTargetView = targetView;
//            this.mCurrentType = type;
//            // 关键：开启视图的绘制（DecorView默认关闭onDraw，需手动开启）
//            if (mTargetView instanceof ViewGroup) {
//                ((ViewGroup) mTargetView).setWillNotDraw(false);
//            }
//            hookDispatchDraw();
//        }
//
//        public void updateMirrorType(MirrorType type) {
//            mCurrentType = type;
//        }
//
//        // Hook DecorView的dispatchDraw方法，修改Canvas
//        private void hookDispatchDraw() {
//            // 通过反射替换DecorView的dispatchDraw（兼容所有Android版本）
//            try {
//                Class<?> viewClass = View.class;
//                java.lang.reflect.Method dispatchDrawMethod = viewClass.getDeclaredMethod("dispatchDraw", Canvas.class);
//                dispatchDrawMethod.setAccessible(true);
//
//                // 动态代理dispatchDraw
//                java.lang.reflect.InvocationHandler handler = (proxy, method, args) -> {
//                    if (args != null && args.length > 0 && args[0] instanceof Canvas) {
//                        Canvas canvas = (Canvas) args[0];
//                        applyMirror(canvas); // 应用镜像矩阵
//                    }
//                    // 执行原dispatchDraw方法（保证页面正常绘制）
//                    return dispatchDrawMethod.invoke(mTargetView, args);
//                };
//
//                // 替换原方法（简化反射逻辑，实际可使用更稳定的Hook框架如Xposed，但非必需）
//                // 此处用更简单的方式：重写DecorView的dispatchDraw（通过自定义ViewGroup包装）
//                if (mTargetView instanceof ViewGroup) {
//                    ((ViewGroup) mTargetView).setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
//                        @Override
//                        public void onChildViewAdded(View parent, View child) {}
//
//                        @Override
//                        public void onChildViewRemoved(View parent, View child) {}
//                    });
//
//                    // 直接重写ViewGroup的dispatchDraw（更简单，无需反射）
//                    ViewGroup targetViewGroup = (ViewGroup) mTargetView;
//                    targetViewGroup.setDispatchDrawListener(canvas -> {
//                        applyMirror(canvas);
//                        targetViewGroup.superDispatchDraw(canvas); // 调用原方法
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        // 应用镜像矩阵到Canvas
//        private void applyMirror(Canvas canvas) {
//            if (mCurrentType == MirrorType.NONE) return;
//
//            int width = mTargetView.getWidth();
//            int height = mTargetView.getHeight();
//            float centerX = width / 2f;
//            float centerY = height / 2f;
//
//            // 保存Canvas当前状态（绘制后恢复，避免影响其他视图）
//            canvas.save();
//
//            switch (mCurrentType) {
//                case LEFT_RIGHT:
//                    // 左右镜像：X轴缩放-1，中心点为屏幕中心
//                    canvas.scale(-1, 1, centerX, centerY);
//                    break;
//                case TOP_BOTTOM:
//                    // 上下镜像：Y轴缩放-1，中心点为屏幕中心
//                    canvas.scale(1, -1, centerX, centerY);
//                    break;
//            }
//        }
//    }
//
//    // 为ViewGroup添加dispatchDraw监听（简化重写逻辑）
//    private interface DispatchDrawListener {
//        void onDispatchDraw(Canvas canvas);
//    }
//
//    private static class ViewGroupWrapper extends ViewGroup {
//        private DispatchDrawListener mListener;
//
//        public ViewGroupWrapper(Context context) {
//            super(context);
//        }
//
//        public void setDispatchDrawListener(DispatchDrawListener listener) {
//            mListener = listener;
//        }
//
//        @Override
//        protected void dispatchDraw(Canvas canvas) {
//            if (mListener != null) {
//                mListener.onDispatchDraw(canvas);
//            } else {
//                super.dispatchDraw(canvas);
//            }
//        }
//
//        @Override
//        protected void onLayout(boolean changed, int l, int t, int r, int b) {}
//    }
//
//    // 给ViewGroup添加superDispatchDraw方法（调用父类dispatchDraw）
//    private static class ViewGroupProxy extends ViewGroup {
//        private final ViewGroup mTarget;
//
//        public ViewGroupProxy(Context context, ViewGroup target) {
//            super(context);
//            this.mTarget = target;
//        }
//
//        public void superDispatchDraw(Canvas canvas) {
//            mTarget.superDispatchDraw(canvas);
//        }
//
//        @Override
//        protected void dispatchDraw(Canvas canvas) {
//            super.dispatchDraw(canvas);
//        }
//
//        @Override
//        protected void onLayout(boolean changed, int l, int t, int r, int b) {}
//    }
//}