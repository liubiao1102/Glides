package xiaoxiao.com.glides;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 该类的工作:建立和管理蓝牙连接。
 * 共有三个线程。mAcceptThread线程用来监听socket连接（服务端使用）.
 * mConnectThread线程用来连接serversocket（客户端使用）。
 * mConnectedThread线程用来处理socket发送、接收数据。（客户端和服务端共用）
 */
public class BluetoothChatUtil {
    private static final String TAG = "BluetoothChatService";

    // 服务名 SDP
    private static final String SERVICE_NAME = "BluetoothChat";
    // uuid SDP
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 蓝牙适配器
    private final BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private static BluetoothChatUtil mBluetoothChatUtil;
    private BluetoothDevice mConnectedBluetoothDevice;
    //常数，指示当前的连接状态
    public static final int STATE_NONE = 0;       // 当前没有可用的连接
    public static final int STATE_LISTEN = 1;     // 现在侦听传入的连接
    public static final int STATE_CONNECTING = 2; // 现在开始连接
    public static final int STATE_CONNECTED = 3;  // 现在连接到远程设备
    public static final int STATAE_CONNECT_FAILURE = 4; //连接失败

    public static final int MESSAGE_DISCONNECTED = 5;
    public static final int STATE_CHANGE = 6;
    public static final String DEVICE_NAME = "device_name";
    public static final int MESSAGE_READ = 7;
    public static final int MESSAGE_WRITE = 8;
    public static final String READ_MSG = "read_msg";

    /**
     * 构造函数。准备一个新的bluetoothchat会话。
     *
     * @param context
     */
    private BluetoothChatUtil(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    public static BluetoothChatUtil getInstance(Context c) {
        if (null == mBluetoothChatUtil) {
            mBluetoothChatUtil = new BluetoothChatUtil(c);
        }
        return mBluetoothChatUtil;
    }

    public void registerHandler(Handler handler) {
        mHandler = handler;
    }

    public void unregisterHandler() {
        mHandler = null;
    }

    /**
     * 设置当前状态的聊天连接
     *
     * @param state 整数定义当前连接状态
     */
    private synchronized void setState(int state) {
        mState = state;
        // 给新状态的处理程序，界面活性可以更新
        mHandler.obtainMessage(STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * 返回当前的连接状态。
     */
    public synchronized int getState() {
        return mState;
    }

    public BluetoothDevice getConnectedDevice() {
        return mConnectedBluetoothDevice;
    }

    /**
     * 开始聊天服务。特别acceptthread开始
     * 开始服务器模式。
     */
    public synchronized void startListen() {
        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 启动线程来监听一个bluetoothserversocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * 开始connectthread启动连接到远程设备。
     *
     * @param device 连接的蓝牙设备
     */
    public synchronized void connect(BluetoothDevice device) {
        // 取消任何线程试图建立连接
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        //启动线程连接到远程设备
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 开始ConnectedThread开始管理一个蓝牙连接,传输、接收数据.
     *
     * @param socket socket连接
     * @param device 已连接的蓝牙设备
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        //取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        //把连接设备的名字传到 ui Activity
        mConnectedBluetoothDevice = device;
        Message msg = mHandler.obtainMessage(STATE_CONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    /**
     * 停止所有的线程
     */
    public synchronized void disconnect() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void bluetoothWrite(JSONArray array) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }
        try {
            r.analyzeJSONPrintText(array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        //创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // 执行写同步
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // 发送失败的信息带回活动
        Message msg = mHandler.obtainMessage(STATAE_CONNECT_FAILURE);
        mHandler.sendMessage(msg);
        mConnectedBluetoothDevice = null;
        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // 发送失败的信息带回Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DISCONNECTED);
        mHandler.sendMessage(msg);
        mConnectedBluetoothDevice = null;
        setState(STATE_NONE);
    }

    /**
     * 本线程 侦听传入的连接。
     * 它运行直到连接被接受（或取消）。
     */
    private class AcceptThread extends Thread {
        // 本地服务器套接字
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // 创建一个新的侦听服务器套接字
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(
                        SERVICE_NAME, SERVICE_UUID);
                //tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        public void run() {

            setName("AcceptThread");
            BluetoothSocket socket = null;
            // 循环，直到连接成功
            while (mState != STATE_CONNECTED) {
                try {
                    // 这是一个阻塞调用 返回成功的连接
                    // mServerSocket.close()在另一个线程中调用，可以中止该阻塞
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                // 如果连接被接受
                if (socket != null) {
                    synchronized (BluetoothChatUtil.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // 正常情况。启动ConnectedThread。
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // 没有准备或已连接。新连接终止。
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }

        }

        public void cancel() {

            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 本线程用来连接设备
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // 得到一个bluetoothsocket
            try {
                mmSocket = device.createRfcommSocketToServiceRecord
                        (SERVICE_UUID);
            } catch (IOException e) {
                mmSocket = null;
            }
        }

        public void run() {
            try {
                // socket 连接,该调用会阻塞，直到连接成功或失败
                mmSocket.connect();

            } catch (IOException connectException) {

                try {
                    Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (Exception e) {

                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException ie) {
                    }
                }

                return;
            }
            // 启动连接线程
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 本线程server 和client共用.
     * 它处理所有传入和传出的数据。
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // 获得bluetoothsocket输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // 监听输入流
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    // 读取输入流
                    int bytes = mmInStream.read(buffer);
                    // 发送获得的字节的ui activity
                    Message msg = mHandler.obtainMessage(MESSAGE_READ);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(READ_MSG, buffer);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 向外发送。
         *
         * @param buffer 发送的数据
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // 分享发送的信息到Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //解析出JSON存储的打印命令
        public void analyzeJSONPrintText(JSONArray array) throws JSONException {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                switch (jsonObject.getInt("operateType")) {
                    case 1:
                        try {
                            updatePrinterStatus(jsonObject.getInt("alignment"), (float) jsonObject.getDouble("fontSize"), jsonObject.getBoolean("isThick"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            write(jsonObject.getString("content").getBytes("gbk"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        String lineWarp = "\n";
                        for (int j = 0; j < Integer.parseInt(jsonObject.getString("content")); j++) {
                            write(lineWarp.getBytes());
                        }
                        break;
                    case 4:
                        break;
                    default:
                        break;
                }
            }
        }

        //变更打印机打印参数 对齐 字号 加粗
        public void updatePrinterStatus(int alignment, float fontSize, boolean isThick) throws IOException {
            switch (alignment) {
                case 0:
                    mmOutStream.write(new byte[]{0x1b, 0x61, 0x00});
                    break;
                case 1:
                    mmOutStream.write(new byte[]{0x1b, 0x61, 0x01});
                    break;
                case 2:
                    mmOutStream.write(new byte[]{0x1b, 0x61, 0x02});
                    break;
                default:
                    break;
            }
            if (fontSize == 36) {
                mmOutStream.write(new byte[]{0x1d, 0x21, 0x11});
            } else if (fontSize == 24) {
                mmOutStream.write(new byte[]{0x1d, 0x21, 0x00});
            }
            if (isThick) {
                mmOutStream.write(new byte[]{0x1b, 0x45, 0x00});
            } else if (!isThick) {
                mmOutStream.write(new byte[]{0x1b, 0x45, 0x01});
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}