package ke.co.imond.fingermap;

import android.fpi.MtGpio;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog.Builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortManager;
import ke.co.imond.fingermap.Data.StuDBHelper;
import ke.co.imond.fingermap.utils.ExtApi;

public class EnrollActivity extends AppCompatActivity {

    StuDBHelper mydb;

    EditText reg_no, first_name, last_name, parent_phone, class_level, enrolledThumb, enrolledindex;
    Button buttonEnroll, buttonView;
    ImageButton thumbFP, indexFP;

    private byte[] jpgbytes=null;

    private byte[] model1=new byte[512];
    private byte[] model2=new byte[512];
    private boolean isenrol1=false;
    private boolean isenrol2=false;
    private int savecount=0;

    private Timer TimerBarcode=null;
    private TimerTask TaskBarcode=null;
    private Handler HandlerBarcode;

    private ImageView fpImage;
    private TextView tvFpStatus;
    private AsyncFingerprint vFingerprint;
    private Dialog fpDialog;
    private int	iFinger=0;
    private boolean	bIsUpImage=true;
    private int count;
    private boolean bcheck=false;
    private ReadThread mReadThread;
    //����
    private SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private byte[] databuf=new byte[1024];
    private int datasize=0;
    private int soundIda;
    private SoundPool soundPool;

    public String CardSN="";
    public String finger1="";
    public String finger2="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mydb = new StuDBHelper(this);

        enrolledindex = (EditText)findViewById(R.id.enrolledIndex);
        enrolledThumb = (EditText)findViewById(R.id.enrolledThumb);

        reg_no = (EditText)findViewById(R.id.reg_no);
        first_name = (EditText)findViewById(R.id.first_name);
        last_name = (EditText)findViewById(R.id.last_name);
        parent_phone = (EditText)findViewById(R.id.parent_phone);
        class_level = (EditText)findViewById(R.id.class_level);
        buttonEnroll = (Button)findViewById(R.id.buttonEnroll);
        buttonView = (Button)findViewById(R.id.buttonView);
        thumbFP=(ImageButton) findViewById(R.id.thumbFP);
        thumbFP.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FPDialog(1);
            }
        });

        indexFP=(ImageButton) findViewById(R.id.indexFP);
        indexFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FPDialog(2);
            }
        });
        addData();
        viewData();
        openSerialPort();
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();


    }

    private void viewData() {
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor res = mydb.getData();
                if(res.getCount() == 0)
                    showMessage("Error", "No data");

                StringBuffer buffer = new StringBuffer();
                while(res.moveToNext()) {
                    buffer.append("ID"+res.getString(0)+"\n");
                    buffer.append("First Name"+res.getString(1)+"\n");
                    buffer.append("Last Name"+res.getString(2)+"\n");
                    buffer.append("Parent Phone"+res.getString(3)+"\n");
                    buffer.append("Class Level"+res.getString(4)+"\n");
                    buffer.append("Finger 1"+res.getString(6)+"\n\n");
                }

                showMessage("Data", buffer.toString());



            }
        });
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private void addData() {
        buttonEnroll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isenrol1)
                    finger1 = ExtApi.BytesToBase64(model1,model1.length);
                if(isenrol2)
                    finger2 =ExtApi.BytesToBase64(model2,model2.length);
                boolean isInserted = mydb.addNewStudent(reg_no.getText().toString(),
                        first_name.getText().toString(),
                        last_name.getText().toString(),
                        parent_phone.getText().toString(),
                        class_level.getText().toString(), finger1, finger2);
                if (isInserted = true)
                    Toast.makeText(EnrollActivity.this, "Inserted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(EnrollActivity.this, "Not Inserted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void FPDialog(int i){
        iFinger=i;
        Builder builder = new Builder(EnrollActivity.this);
        builder.setTitle("Fingerprint Registration");
        final LayoutInflater inflater = LayoutInflater.from(EnrollActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrollfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus= (TextView) vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });

        fpDialog = builder.create();
        fpDialog.setCanceledOnTouchOutside(false);
        fpDialog.show();

        FPProcess();
    }

    private void FPInit(){
        //ָ�ƴ���
        vFingerprint.setOnGetImageExListener(new AsyncFingerprint.OnGetImageExListener() {
            @Override
            public void onGetImageExSuccess() {
                if(bcheck){
                    vFingerprint.FP_GetImageEx();
                }else{
                    if(bIsUpImage){
                        vFingerprint.FP_UpImageEx();
                        tvFpStatus.setText("Fingerprint image being displayed...");
                    }else{
                        tvFpStatus.setText("Being processed...");
                        vFingerprint.FP_GenCharEx(count);
                    }
                }
            }

            @Override
            public void onGetImageExFail() {
                if(bcheck){
                    bcheck=false;
                    tvFpStatus.setText("Please press fingerprint��");
                    vFingerprint.FP_GetImageEx();
                    count++;
                }else{
                    vFingerprint.FP_GetImageEx();
                }
            }
        });

        vFingerprint.setOnUpImageExListener(new AsyncFingerprint.OnUpImageExListener() {
            @Override
            public void onUpImageExSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                fpImage.setImageBitmap(image);
                //fpImage.setBackgroundDrawable(new BitmapDrawable(image));
                vFingerprint.FP_GenCharEx(count);
                tvFpStatus.setText("Being processed...");
            }

            @Override
            public void onUpImageExFail() {
            }
        });

        vFingerprint.setOnGenCharExListener(new AsyncFingerprint.OnGenCharExListener() {
            @Override
            public void onGenCharExSuccess(int bufferId) {
                //vFingerprint.FP_Search(1, 1, 256);
                //tvFpStatus.setText("Being identified...");
                if (bufferId == 1) {
                    bcheck=true;
                    tvFpStatus.setText("Please lift your finger��");
                    vFingerprint.FP_GetImageEx();

                    //tvFpStatus.setText("Please press fingerprint��");
                    //vFingerprint.FP_GetImageEx();
                    //count++;
                } else if (bufferId == 2) {
                    vFingerprint.FP_RegModel();
                }
            }

            @Override
            public void onGenCharExFail() {
                tvFpStatus.setText("Failed to generate eigenvalues��");
            }
        });

        vFingerprint.setOnRegModelListener(new AsyncFingerprint.OnRegModelListener() {

            @Override
            public void onRegModelSuccess() {
                vFingerprint.FP_UpChar();
                tvFpStatus.setText("Synthetic template success��");
            }

            @Override
            public void onRegModelFail() {
                tvFpStatus.setText("Synthetic template failure��");
            }
        });

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {
                //AdminEditActivity.this.model = model;
                if(iFinger==1){
                    enrolledThumb.setText("Registered");
                    System.arraycopy(model, 0, EnrollActivity.this.model1,0,512);
                    isenrol1=true;
                    //EnrollActivity.this.model1 = model;
                }else{
                    enrolledindex.setText("Registered");
                    System.arraycopy(model, 0, EnrollActivity.this.model2,0,512);
                    isenrol2=true;
                    //EnrollActivity.this.model2 = model;
                }
                tvFpStatus.setText("Successful registration��");

                //SerialPortManager.getInstance().closeSerialPort();
                fpDialog.cancel();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText("Registration Failed��");
            }
        });

    }

    private void FPProcess() {

        count = 1;
        //model = null;
        tvFpStatus.setText("Press fingerprint��");
        try {
            Thread.currentThread();
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        vFingerprint.FP_GetImageEx();
    }

    public void BarcodeOpen(){
        MtGpio.getInstance().BCPowerSwitch(true);
        MtGpio.getInstance().BCReadSwitch(true);
        try {
            Thread.currentThread();
            Thread.sleep(200);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        datasize=0;
        MtGpio.getInstance().BCReadSwitch(false);
        //DialogFactory.showProgressDialog(RoomCheckActivity.this,"Read barcodes...");
    }

    public void BarcodeClose(){
        if (mReadThread != null)
            mReadThread.interrupt();
        closeSerialPort();
        mSerialPort = null;
        MtGpio.getInstance().BCReadSwitch(true);
        MtGpio.getInstance().BCPowerSwitch(false);
        //DialogFactory.cancleProgressDialog();
    }

    public void openSerialPort(){
        try {
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
        } catch (IOException e) {
        } catch (InvalidParameterException e) {
        }
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {

        if (mSerialPort == null) {
            String path = "/dev/ttyMT1";
            int baudrate = 9600;
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()/*true*/) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EnrollActivity.this, "Read barcodes fail", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                System.arraycopy(buffer, 0, databuf,datasize,size);
                datasize=datasize+size;
                if(TimerBarcode==null){
                    TimerBarcodeStart();
                }
            }
        });
    }

    public void TimerBarcodeStart() {
        TimerBarcode = new Timer();
        HandlerBarcode = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TimerBarcodeStop();
                if(datasize>0){
                    byte tp[]=new byte[datasize];
                    System.arraycopy(databuf, 0, tp,0,datasize);
                    enrolledindex.setText(new String(tp));
                    soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
                    datasize=0;
                }
                super.handleMessage(msg);
            }
        };
        TaskBarcode = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                HandlerBarcode.sendMessage(message);
            }
        };
        TimerBarcode.schedule(TaskBarcode, 1000, 1000);
    }

    public void TimerBarcodeStop() {
        if (TimerBarcode!=null) {
            TimerBarcode.cancel();
            TimerBarcode = null;
            TaskBarcode.cancel();
            TaskBarcode=null;
        }
    }

    //NFC

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){
        byte[] sn = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        String cardstr=/*Integer.toString(count)+":"+*/
                Integer.toHexString(sn[0]&0xFF).toUpperCase()+
                        Integer.toHexString(sn[1]&0xFF).toUpperCase()+
                        Integer.toHexString(sn[2]&0xFF).toUpperCase()+
                        Integer.toHexString(sn[3]&0xFF).toUpperCase();
        enrolledindex.setText(cardstr);
        CardSN=cardstr;
        //soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
    }

}
