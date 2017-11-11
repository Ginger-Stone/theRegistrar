package android.fpi;


import android.util.Log;

/**
 * @hide
 * @author 1
 *
 */
public class MtGpio {

    private boolean mOpen = false;
    private static MtGpio mMe = null;
    private MtGpio() {
        mOpen = openDev()>=0?true:false;
        Log.d("MtGpio","openDev->ret:"+mOpen);
    }

    public static MtGpio getInstance(){
        if (mMe == null){
            mMe = new MtGpio();
        }
        return mMe;
    }

    public void BCPowerSwitch(boolean bonoff){
        if(bonoff){
            //Barcode Power
            sGpioMode(12,0);
            sGpioDir(12,1);
            sGpioOut(12,1);
        }else{
            sGpioMode(12,0);
            sGpioDir(12,1);
            sGpioOut(12,0);
        }
    }

    public void BCReadSwitch(boolean bonoff){
        if(bonoff){
            //Barcode TRIG
            sGpioMode(13,0);
            sGpioDir(13,1);
            sGpioOut(13,1);
        }else{
            sGpioMode(13,0);
            sGpioDir(13,1);
            sGpioOut(13,0);
        }
    }

    public void FPPowerSwitch(boolean bonoff){
        if(bonoff){
            //FP Power
            sGpioMode(119,0);
            sGpioDir(119,1);
            sGpioOut(119,1);
        }else{
            sGpioMode(119,0);
            sGpioDir(119,1);
            sGpioOut(119,0);
        }
    }


    public boolean isOpen(){
        return mOpen;
    }

    public void sGpioDir(int pin, int dir){
        setGpioDir(pin,dir);
    }

    public void sGpioOut(int pin, int out){
        setGpioOut(pin,out);
    }

    public int getGpioPinState(int pin){
        return getGpioIn(pin);
    }

    public void sGpioMode(int pin, int mode){
        setGpioMode(pin, mode);
    }

    // JNI
    private native int openDev();
    public native void closeDev();
    private native int setGpioMode(int pin, int mode);
    private native int setGpioDir(int pin, int dir);
    private native int setGpioPullEnable(int pin, int enable);
    private native int setGpioPullSelect(int pin, int select);
    public native  int setGpioOut(int pin, int out);
    private native int getGpioIn(int pin);
    static {
        System.loadLibrary("mtgpio");
    }
}
