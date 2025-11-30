package android.app;
import android.annotation.SuppressLint;

public class MirrorManager {
    @SuppressLint("StaticFieldLeak")
    private static MirrorRootLayout sCurrentRootLayout;

    // 绑定当前Activity的镜像布局
    public static void bind(MirrorRootLayout rootLayout) {
        sCurrentRootLayout = rootLayout;
    }

    // 全局开启左右镜像
    public static void enableLeftRightMirror() {
        if (sCurrentRootLayout != null) {
            sCurrentRootLayout.setMirrorType(MirrorRootLayout.MirrorType.LEFT_RIGHT);
        }
    }

    // 全局关闭镜像
    public static void disableMirror() {
        if (sCurrentRootLayout != null) {
            sCurrentRootLayout.setMirrorType(MirrorRootLayout.MirrorType.NONE);
        }
    }
}
