package xiaoxiao.com.glides;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private ClipboardManager mClipboardManager;
    private EditText edit_txt;
    private Button btn_copy;
    private Button btn_nextPage;
    private ClipData mClipData;
    private Button jt;
    private ImageView ig_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //获取管理剪切板的一个类
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        initControls();

    }

    private void initControls() {
        edit_txt = (EditText)findViewById(R.id.edit_txt);

        btn_copy = (Button)findViewById(R.id.btn_copy);
        ig_view = findViewById(R.id.ig_view);
        jt = (Button)findViewById(R.id.jt);
        btn_copy.setOnClickListener(this);
        jt.setOnClickListener(this);

        btn_nextPage = (Button)findViewById(R.id.btn_nextPage);
        btn_nextPage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.jt:
                Bitmap bitmap = JtMethod();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] bytes=outputStream.toByteArray();
                Glide.with(this)
                        .load(bytes)
                        .centerCrop()
//                    .thumbnail(0.1f)   //缩略图为原来的十分之一
                      //  .override(Utils.px2dip(mContext,130),Utils.px2dip(mContext,130)) //设置大小
                       // .placeholder(me.iwf.photopicker.R.drawable.__picker_ic_photo_black_48dp)
                      //  .error(me.iwf.photopicker.R.drawable.__picker_ic_broken_image_black_48dp)
                        .into(ig_view);


            case R.id.btn_copy:

                /**
                 * 将文字信息放到剪贴板上
                 */

                mClipData = ClipData.newPlainText("text",edit_txt.getText().toString());
                mClipboardManager.setPrimaryClip(mClipData);
                break;
            case R.id.btn_nextPage:
Main3Activity.PasteActivityStart(this);
                break;

        }

    }

    private Bitmap JtMethod() {
        // 获取windows中最顶层的view
        View view = getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();
        Log.i("liubiao", "宽"+widths+"高"+heights);
        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 10, bos);

        byte[] bytes = bos.toByteArray();
        bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;

    }
}
