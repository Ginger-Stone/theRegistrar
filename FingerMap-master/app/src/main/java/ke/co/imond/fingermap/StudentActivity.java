package ke.co.imond.fingermap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import ke.co.imond.fingermap.utils.ToastUtil;
import ke.co.imond.fingermap.Data.UserItem;

import android.fpi.MtGpio;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.AsyncFingerprint;
import android_serialport_api.AsyncFingerprint.OnGenCharExListener;
import android_serialport_api.AsyncFingerprint.OnGetImageExListener;
import android_serialport_api.AsyncFingerprint.OnRegModelListener;
import android_serialport_api.AsyncFingerprint.OnUpCharListener;
import android_serialport_api.AsyncFingerprint.OnUpImageExListener;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortManager;

public class StudentActivity extends Activity {

    private EditText editText1,editText2,editText6,editText7,editText8,editText9,editText10;
    private TextView text1,text2,text3;
    private ImageView imgPhoto,imgFinger1,imgFinger2;

    private byte[] jpgbytes=null;

    private byte[] model1=new byte[512];
    private byte[] model2=new byte[512];
    private boolean isenrol1=false;
    private boolean isenrol2=false;
    private int savecount=0;

    private ImageView fpImage;
    private TextView  tvFpStatus;
    private AsyncFingerprint vFingerprint;
    private Dialog fpDialog;
    private int	iFinger=0;
    private boolean	bIsUpImage=true;
    private int count;
    private boolean bcheck=false;

    //����
    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private byte[] databuf=new byte[1024];
    private int datasize=0;
    private int soundIda;
    private SoundPool soundPool;

    private Timer TimerBarcode=null;
    private TimerTask TaskBarcode=null;
    private Handler HandlerBarcode;

    //NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;

    UserItem person = new UserItem();
    public String CardSN="";

    private Spinner spin1,spin2;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        editText1=(EditText)findViewById(R.id.editText1);
        editText2=(EditText)findViewById(R.id.editText2);
        editText6=(EditText)findViewById(R.id.editText6);
        editText7=(EditText)findViewById(R.id.editText7);
        editText8=(EditText)findViewById(R.id.editText8);
        editText9=(EditText)findViewById(R.id.editText9);
        editText10=(EditText)findViewById(R.id.editText10);

        text1=(TextView)findViewById(R.id.textView3);
        text2=(TextView)findViewById(R.id.textView4);
        text3=(TextView)findViewById(R.id.textView5);

        imgFinger1=(ImageView)findViewById(R.id.imageView2);
        imgFinger1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FPDialog(1);
            }
        });

        imgFinger2=(ImageView)findViewById(R.id.imageView3);
        imgFinger2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FPDialog(2);
            }
        });

        final ImageView imgBardcode1d=(ImageView)findViewById(R.id.imageView4);
        imgBardcode1d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ToastUtil.showToastTop(StudentActivity.this,"Please sweep Barcode...");
                BarcodeOpen();
            }
        });

        final ImageView imgCard=(ImageView)findViewById(R.id.imageView6);
        imgCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ToastUtil.showToastTop(StudentActivity.this,"Please put the card...");
            }
        });


        //����
        spin1=(Spinner)findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource( this, R.array.us1_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter1);
        spin1.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){
                person.type=pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //nothing to do
            }
        });
        spin1.setSelection(1);

        //ʶ������
        spin2=(Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this, R.array.us2_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);
        spin2.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){
                //person.ident=pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spin2.setSelection(0);


        soundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
        soundIda = soundPool.load(this, R.raw.dong, 1);

        openSerialPort();
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();

        //NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Device does not support NFC!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Enable the NFC function in the system settings!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(resultCode){
            case 1:{
                Bundle bl= data.getExtras();
                String barcode=bl.getString("barcode");
                editText9.setText(barcode);
            }
            break;
            case 2:
                break;
            case 3:{
                Bundle bl= data.getExtras();
                String id=bl.getString("id");
                Toast.makeText(StudentActivity.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
                byte[] photo=bl.getByteArray("photo");
                if(photo!=null){
                    try{
                        Matrix matrix = new Matrix();
                        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                        matrix.preRotate(90);
                        Bitmap nbm=Bitmap.createBitmap(bm ,0,0, bm .getWidth(), bm .getHeight(),matrix,true);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        nbm.compress(Bitmap.CompressFormat.JPEG, 80, out);//��ͼƬѹ��������
                        jpgbytes= out.toByteArray();

                        Bitmap bitmap =BitmapFactory.decodeByteArray(jpgbytes, 0, jpgbytes.length);

                        //Drawable drawable = new BitmapDrawable(bitmap);
                        //imgPhoto.setBackground(drawable);
                        imgPhoto.setImageBitmap(bitmap);

                    }catch(Exception e){
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.enroll, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            SerialPortManager.getInstance().closeSerialPort();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    //ָ�ƵǼ�
    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new Builder(StudentActivity.this);
        builder.setTitle("Registration fingerprint");
        final LayoutInflater inflater = LayoutInflater.from(StudentActivity.this);
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
        vFingerprint.setOnGetImageExListener(new OnGetImageExListener() {
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

        vFingerprint.setOnUpImageExListener(new OnUpImageExListener() {
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

        vFingerprint.setOnGenCharExListener(new OnGenCharExListener() {
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

        vFingerprint.setOnRegModelListener(new OnRegModelListener() {

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

        vFingerprint.setOnUpCharListener(new OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {
                //AdminEditActivity.this.model = model;
                if(iFinger==1){
                    editText6.setText("Registered");
                    System.arraycopy(model, 0, StudentActivity.this.model1,0,512);
                    isenrol1=true;
                    //StudentActivity.this.model1 = model;
                }else{
                    editText7.setText("Registered");
                    System.arraycopy(model, 0, StudentActivity.this.model2,0,512);
                    isenrol2=true;
                    //StudentActivity.this.model2 = model;
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

    private void FPProcess(){

        count = 1;
        //model = null;
        tvFpStatus.setText("Press fingerprint��");
        try {
            Thread.currentThread();
            Thread.sleep(200);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        vFingerprint.FP_GetImageEx();
    }
    //һά����

    //����
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
                    Toast.makeText(StudentActivity.this, "Read barcodes fail", Toast.LENGTH_SHORT).show();
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
                    editText8.setText(new String(tp));
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
        editText10.setText(cardstr);
        CardSN=cardstr;
        //soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
    }

}