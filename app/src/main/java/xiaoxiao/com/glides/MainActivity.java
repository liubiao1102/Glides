package xiaoxiao.com.glides;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kongzue.stacklabelview.StackLabel;
import com.kongzue.stacklabelview.interfaces.OnLabelClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView ig_view;
    private TextView tv_time;
    private StackLabel stack;
    private ArrayList<String> strings;
    private RecyclerView recycle_view;
    private long exitTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ig_view = findViewById(R.id.ig_view);
        tv_time = findViewById(R.id.tv_time);
        stack = findViewById(R.id.stack);
        recycle_view = findViewById(R.id.recycle_view);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String format = simpleDateFormat.format(System.currentTimeMillis());
        tv_time.setText(format);
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("哈哈哈oooooooooooooooooooo" + i);
        }
        stack.setLabels(strings);
        stack.setDeleteButton(true);
        stack.setOnLabelClickListener(new OnLabelClickListener() {
            @Override
            public void onClick(int index, View v, String s) {
                if (stack.isDeleteButton()) {
                    strings.remove(index);
                    stack.setLabels(strings);
                }
            }
        });
        findViewById(R.id.but).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* String url = "http://cn.bing.com/az/hprichbg/rb/Dongdaemun_ZH-CN10736487148_1920x1080.jpg";
                Glide
                        .with(MainActivity.this)
                        .load("http://p1.pstatp.com/large/166200019850062839d3")
                        //.asBitmap()//只允许加载静态图
                        .asGif()//只允许加载gif动图
                        .override(50, 50)
                        .placeholder(R.drawable.ic_launcher_background)//占位图
                        .diskCacheStrategy(DiskCacheStrategy.NONE)//禁用glide缓存功能
                        .error(R.drawable.ic_launcher_foreground)//加载失败占位图
                        .into(ig_view);*/
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(),
                    "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}