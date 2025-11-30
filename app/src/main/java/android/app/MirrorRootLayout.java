package android.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 镜像父布局：所有 Activity 的根布局使用该布局
 */
public class MirrorRootLayout extends FrameLayout {
    public enum MirrorType {
        NONE,
        LEFT_RIGHT,
        TOP_BOTTOM
    }
    private MirrorType mMirrorType = MirrorType.NONE;

    public MirrorRootLayout(Context context) {
        super(context);
        // 开启绘制
        setWillNotDraw(false);
    }

    public MirrorRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public MirrorRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    // 设置镜像类型
    public void setMirrorType(MirrorType type) {
        mMirrorType = type;
        invalidate(); // 触发重绘
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mMirrorType != MirrorType.NONE) {
            int width = getWidth();
            int height = getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;

            // 保存状态
            canvas.save();

            // 应用镜像矩阵
            if (mMirrorType == MirrorType.LEFT_RIGHT) {
                canvas.scale(-1, 1, centerX, centerY);
            } else if (mMirrorType == MirrorType.TOP_BOTTOM) {
                canvas.scale(1, -1, centerX, centerY);
            }
        }

        // 分发子 View 绘制
        super.dispatchDraw(canvas);

        if (mMirrorType != MirrorType.NONE) {
            // 恢复 Canvas 状态
            canvas.restore();
        }
    }
}