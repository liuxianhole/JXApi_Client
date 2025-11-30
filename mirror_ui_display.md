# 屏幕镜像显示方案
## 海思352 屏幕显示方向相关设置
当前晶心海思方案提供4种屏幕显示方向设置，分别为横屏、竖屏、180度、270度。并未提供 android 屏幕镜像显示方向设置。

```c++
    // 显示-设置屏幕方向 传入 0 90 180 270
    // setprop persist.prop.screenorientation landscape ---------横屏
    // setprop persist.prop.screenorientation portrait  ---------竖屏
    // setprop persist.prop.screenorientation seascape  ---------180度
    // setprop persist.prop.screenorientation upsideDown --------270度
    public void setDisplayOrientation(String rotation) {
        if (Objects.equals(rotation, getDisplayOrientation())) {
            Log.e("当前方向", "与旋转方向一致，不执行");
            return;
        }
        try {
            setSystemProperty("persist.prop.screenorientation", rotation);
        } catch (Exception exc) {
            Log.e(TAG, "set rotation err!");
        }
    }

    // 显示-获取屏幕方向        1
    // landscape ---------横屏
    // portrait  ---------竖屏
    // seascape  ---------180度
    // upsideDown --------270度
    public String getDisplayOrientation() {
        try {
            return getSystemProperty("persist.prop.screenorientation", "landscape");
        } catch (Exception e) {
            Log.e(TAG, "getDisplayOrientation 失败!");
            return "";
        }
    }
```
而且当前屏幕镜像显示方向设置的为系统属性，需要将 app 重启才能生效，而且屏幕显示方法变化，整个系统的显示的方向也发生了变化。

## 屏幕镜像显示方案
目前常见的屏幕镜像显示方案有以下几种：
1. 通过设计一个全局父布局，所有 Activity 的布局都继承该布局，通过重写 dispatchDraw() 实现所有 View 镜像显示。
设置的全局父布局类如下：
```java
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
```

2. 项目当中需要显示镜像的 Activity 布局，继承自 MirrorRootLayout 类。如下：
```xml
<?xml version="1.0" encoding="utf-8"?>
<android.app.MirrorRootLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:app="http://schemas.android.com/apk/res-auto"
	xmlns:tools="http://schemas.android.com/tools"
	android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

<LinearLayout
	android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">


    <RelativeLayout
        android:id="@+id/relativeLayout1"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:layout_centerInParent="true"
        android:gravity="center">


		<ListView
			android:id="@+id/list_view"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			android:layout_below="@+id/TextView"
			android:alpha="0.9"
			android:background="@android:color/white" />

        <TextView
                android:id="@+id/TextView"
                android:layout_width="match_parent"
                android:layout_height="@dimen/syh_60"
                android:background="@color/daoqi"
                android:focusable="false"
                android:focusableInTouchMode="false"
                android:gravity="center"
                android:text="@string/app_name"
		android:textStyle="bold"
                android:textColor="@android:color/white"
	        android:textSize="38.0sp"
        />
    </RelativeLayout>

    <ImageView
        android:id="@+id/iv_local"
        android:layout_width="match_parent"
        android:layout_height="0dp"
	android:layout_weight="1"
        android:src="@mipmap/lena_test"
	android:scaleType="fitXY"
	android:contentDescription="@null" 
	android:transitionName="image_transition"
    />
</LinearLayout>
</android.app.MirrorRootLayout>
```

如上图所示，所有需要显示镜像的 Activity 布局，都继承自 MirrorRootLayout 类，并且设置 layout_width 为 match_parent。 layout_height 也设置为 match_parent。

3. 设置一个全局的 MirrorManager, 用于管理所有 Activity 的镜像显示。
```java
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

```

4. 在具体的 Activity 中，需要显示镜像的布局，在当前 Activity 的 onCreate() 方法中调用 MirrorManager.bind() 方法绑定镜像布局。如下：
```java
public class MainActivity extends AppCompatActivity {
    private MirrorRootLayout mRootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootLayout = findViewById(R.id.root_layout);
        MirrorManager.bind(mRootLayout);
    }
}
```

5. 在具体的需要调用镜像显示的地方，调用 MirrorManager.enableLeftRightMirror() 方法开启左右镜像显示，调用 MirrorManager.disableMirror() 方法关闭镜像显示，如下切换镜像与非镜像显示：
```java
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (isMirrorEnabled) {
                            MirrorManager.disableMirror();
                            isMirrorEnabled = false;
                        } else {
                            MirrorManager.enableLeftRightMirror();
                            isMirrorEnabled = true;
                        }
                        break;
                    ...................................................
                    ...................................................
                }
            }     
```

6. 本地 demo 测试效果
正常的显示效果如下：
![正常显示效果](app/src/main/res/mipmap-hdpi/normal.png)

镜像显示的效果如下：
![镜像显示效果](app/src/main/res/mipmap-hdpi/mirror.png)


7. 注意事项
1. 镜像显示只在当前 Activity 中生效，其他 Activity 不会受到影响，需要设置镜像显示的 Activity 布局为 MirrorRootLayout 类。
2. 镜像显示当前只是针对当前 Activity 的布局生效，系统的其他 app，已经状态栏，导航栏等，显示效果都不会受到影响，如果需要全局显示镜像效果，可以在 Application 中设置 app 全屏显示。
