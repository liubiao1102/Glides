package xiaoxiao.com.glides;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {
    private Main3Activity mContext;

    public static void PasteActivityStart(Context context)
    {
        Intent intent = new Intent();
        intent.setClass(context,Main3Activity.class);
        context.startActivity(intent);
    }
    private TextView txt_paste;
    private Button btn_getData;
    private ClipboardManager mClipboardManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mContext = this;
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

        initControls();


    }

    public void tankuang(View view) {
        CreatDialog();
    }
    private void CreatDialog() {
        BaseDialog.Builder builder = new BaseDialog.Builder(mContext);
        BaseDialog mDialog = builder.setViewId(R.layout.dialog)
                //设置dialogpadding
                .setPaddingdp(0, 0, 0, 0)
                //设置显示位置
                .setGravity(Gravity.CENTER)
                //设置动画

                //设置dialog的宽高
                .setWidthHeightpx(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                //设置触摸dialog外围是否关闭
                .isOnTouchCanceled(true)
                //设置监听事件
                .builder();
        mDialog.show();

    }

    /**
     * 初始化控件
     */
    private void initControls() {
        txt_paste = (TextView)findViewById(R.id.txt_paste);

        btn_getData = (Button)findViewById(R.id.btn_getData);
        btn_getData.setOnClickListener(this);
/**
 * 将剪贴板上的文字信息取出来放到文本框中
 */
       /* ClipData mClipData = mClipboardManager.getPrimaryClip();
        ClipData.Item item = mClipData.getItemAt(0);
        if (TextUtils.isEmpty(item.getText().toString())){
            Toast.makeText(Main3Activity.this,"无内容",Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(Main3Activity.this,"有内容",Toast.LENGTH_LONG).show();
            txt_paste.setText(item.getText().toString()+"我是刘彪");
           // CreatDialog();

        }*/
        /**
         * 判断当前剪贴板上存在Copy，获取按钮可用，如果
         *//*
        if(mClipboardManager.hasPrimaryClip())
        {
            btn_getData.setEnabled(true);
        }
        else
        {

            btn_getData.setEnabled(false);
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_getData:



                break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
