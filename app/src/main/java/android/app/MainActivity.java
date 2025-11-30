package android.app;

import jxapi.SerialPortUtil;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import jxapi.SysControl;
import android.util.DisplayMetrics;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.compose.ui.graphics.Matrix;

/*
 * 本项目为 晶心安卓单板系统 API 测试参考调用方法。
 * 安装方法 ：adb install -t ./app/build/outputs/apk/debug/JXApiTest.apk
 * 启动方法 ：adb shell am start -n android.app/.MainActivity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "JxApiTest";
    private ListView listView;

    // 镜像状态：true=启用，false=禁用（默认禁用）
    private boolean isMirrorEnabled = false;

    private MirrorRootLayout mRootLayout;

    private final String[] data = {"屏幕镜像显示  -----  字体与界面图像通过左右翻转显示在屏幕呈现镜像效果"};
    private final String[] data1 = {"屏幕镜像显示 ----- The Greek alphabet has been used to write the Greek language since the late 9th or early 8th century BC. It was derived from the earlier Phoenician alphabet, and is the earliest known alphabetic script to systematically write vowels as well as consonants.\n"};

    private final String[] data2 = {"Alpha 是希腊字母表中的第一个字母，包括大写字母 \\(A\\) (Alpha) 和小写字母 \\(\\alpha \\) (alpha)。它在希腊语中发音为“阿尔法”，并在数学、科学和其他领域中广泛使用。 The Greek alphabet has been used to write the Greek language since the late 9th or early 8th century BC. It was derived from the earlier Phoenician alphabet, and is the earliest known alphabetic script to systematically write vowels as well as consonants"};
    // 端口
    private String port;     // 串口号
    private int baud;        // 波特率
    private int check;       // 校验位
    private int datatable;   // 数据位
    private int stop;        // 停止位

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);
//        mRootLayout = findViewById(R.id.root_layout);
//
//        // 开启左右镜像
//        mRootLayout.setMirrorType(MirrorRootLayout.MirrorType.LEFT_RIGHT);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AidlUtils.getInstance().bindService(MainActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 延迟执行的操作
                try {
                    // 初始化使能串口ttyAMA1
                    init_ttyAMA1();
                } catch (Exception e) {
                    Log.e(TAG, "串口使能失败");
                }
            }
        }, 50);

        // 界面初始化
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, data2);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        initClick();

        SysControl sysControl = SysControl.GetInstance();

        Log.i(TAG, "system device id is " + sysControl.getDeviceID());
        Log.i(TAG, "system sdk version is " + sysControl.getSDKVersion());
        Log.i(TAG, "system sdk firmware version is " + sysControl.getFirmwareVersion());
        Log.i(TAG, "system sdk getDisplayOrientation is " + sysControl.getDisplayOrientation());
        // sysControl.setDisplayOrientation("portrait");
        Log.i(TAG, "system sdk getDisplayOrientation is " + sysControl.getDisplayOrientation());
        Log.i(TAG, "system sdk getSystemMaxBrightness is " + sysControl.getSystemMaxBrightness());

        // 全局镜像矩阵（水平镜像为例）
        ImageView ivLocal = findViewById(R.id.iv_local);
        // 动态设置图片（替换为其他 PNG 资源）
        ivLocal.setImageResource(R.mipmap.lena_test);

        MirrorRootLayout rootLayout = findViewById(R.id.root_layout);
        MirrorManager.bind(rootLayout);
        MirrorManager.enableLeftRightMirror();
        // 开启硬件加速，提升镜像性能
        getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void openSerial() {
        try {
            getSerialPortSetting();
            SerialPortUtil.openSerialPort(
                    getApplicationContext(),
                    port,
                    baud,
                    check,
                    datatable,
                    stop
            );
        } catch (Exception e) {
            Log.e("JingXin", "iService openSerialPort exception");
        }
    }

    private void getSerialPortSetting() {
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    port = "ttyAMA1";
                    break;
                case 1:
                    baud = 115200;
                    break;
                case 2:
                    check = 0;
                    break;
                case 3:
                    datatable = 8;
                    break;
                case 4:
                    stop = 1;
                    break;
            }
        }
    }

    private void init_ttyAMA1() {
        Log.e(TAG, "使能串口ttyAMA1");
        try {
            AidlUtils.getInstance().enableUart(false);
        } catch (Exception e) {
            Log.e(TAG, "使能串口失败");
            finish();
        } finally {
            System.out.println("auto service  无论是否发生异常，finally 块中的代码都会执行。");
        }
    }

    private void initClick() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
//                        Intent backlight_view = new Intent(MainActivity.this, BacklightView.class);
//                        startActivity(backlight_view);
//                        Log.e(TAG, "item_backlight start!");
//                        mRootLayout = findViewById(R.id.root_layout);
//
//                        // 开启左右镜像
//                        mRootLayout.setMirrorType(MirrorRootLayout.MirrorType.TOP_BOTTOM);
                        if (isMirrorEnabled) {
                            MirrorManager.disableMirror();
                            isMirrorEnabled = false;
                        } else {
                            MirrorManager.enableLeftRightMirror();
                            isMirrorEnabled = true;
                        }
                        break;
                    case 1:
                        Intent intent2 = new Intent();
                        intent2.setClassName(
                                "com.hisilicon.tv.menu",
                                "com.hisilicon.tv.menu.app.TvMenuActivity"
                        );
                        startActivity(intent2);
                        Log.e(TAG, "factoryMenu start!");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy!");
        //解绑服务
        AidlUtils.getInstance().unbindService(MainActivity.this);
    }
}
