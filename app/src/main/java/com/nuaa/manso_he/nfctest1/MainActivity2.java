package com.nuaa.manso_he.nfctest1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;


public class MainActivity2 extends Activity {

    private static final String Tag_ASSIST = "[ReadTest]";
    private Context mContext;
    //NFC Declarations
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mNfcPendingIntent = null;
    //UI
    private TextView mTitle = null;
    private TextView mPayload = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        //创建一个PendingIntent对象，以便Android系统能够在扫描到NFC标签时，用它来封装NFC标签的详细信息
//        PendingIntent pdIntent = PendingIntent.getActivity(
//                this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
//        IntentFilter ndef =new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        try{
//            ndef.addDataType("*/*");
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            throw new RuntimeException("fail",e);
//        }
//        IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef,};
//        //建立一个应用程序希望处理的NFC标签技术的数组
//        String[][] techListsArray = new String[][]{new String[]{NfcF.class.getName()}};
        //TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into onCreat");
        mContext=this;

        //NFC Check(未实现)
        checkNFCFunction();
        //Init UI
        intiUI();
        //Init NFC
        initNFC();
    }


    private void intiUI() {
        //TODO Auto-generated method stub
        mPayload = (TextView) findViewById(R.id.tvACT2_1);
        mTitle = (TextView) findViewById(R.id.tvACT2);
    }

    private void initNFC() {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into initNFC");
        //getting the default NFC adapter
        mNfcAdapter =NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent =PendingIntent.getActivity(
                this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        //FLAG_ACTIVITY_SINGLE_TOP:not creating multiple instances of the same application.

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    protected void onResume() {
        //TODO Auto-generated method stub
        super.onResume();
        enableForegroundDispatch();
        //消息判别
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_NDEF_DISCOVERED");
            resolveIntent(getIntent());
        }
        else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST+"ACTION_TECT_DISCOVERED");
        }
        else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST+"ACTION_TAG_DISCOVERED");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForeGroundDispatch();
    }

    //获取NDEF消息
    private void resolveIntent(Intent intent) {
        LogUtil.i(MyConstant.Tag, Tag_ASSIST+"into resolveIntent");
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_NDEF_DISCOVERED");
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMsgs !=null)
            {
                messages = new NdefMessage[rawMsgs.length];
                for(int i=0;i<rawMsgs.length;i++)
                {
                    messages[i]=(NdefMessage)rawMsgs[i];
                    LogUtil.i(MyConstant.Tag,Tag_ASSIST+"messages[i] = "+messages[i]);
                }
            }
            else
            {
                //Unknown tag type
                byte[] empty =new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,empty,empty,empty);
                NdefMessage msg= new NdefMessage(new NdefRecord[]{record});
                messages = new NdefMessage[]{msg};
            }
            //Setup the views
            setTitle(R.string.title_scanned_tag);
            //process NDEF Msg
            processNDEFMsg(messages);
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
        {
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"ACTION_TECT_DISCOVERED");
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            LogUtil.i(MyConstant.Tag, Tag_ASSIST+"ACTION_TAG_DISCOVERED");
        }
        else
        {
            LogUtil.e(MyConstant.Tag, Tag_ASSIST+"Unknow intent "+intent);
            finish();
            return;
        }
    }

    //获取待解析的NDEFMessage
    private void processNDEFMsg(NdefMessage[] messages) {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into processNDEFMsg");
        if(messages == null || messages.length==0)
        {
            LogUtil.e(MyConstant.Tag,Tag_ASSIST+"NdefMessgae is null");
            return;
        }
        for(int i=0;i<messages.length;i++)
        {
            int length =messages[i].getRecords().length;
            LogUtil.i(MyConstant.Tag,Tag_ASSIST+"Message "+(i+1)+","+"length="+length);
            NdefRecord[] records= messages[i].getRecords();
            for(int j=0;j<length;j++)  //几个记录
            {
                for(NdefRecord record:records)
                {
                    parseUriRecord(record);
                }
            }
        }
    }

    //解析NdefMessage
    private void parseUriRecord(NdefRecord record) {
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseUriRecord");
        short tnf = record.getTnf();
        if(tnf == NdefRecord.TNF_WELL_KNOWN)
        {
            parseWellKnownRecord(record);
        }else if (tnf == NdefRecord.TNF_ABSOLUTE_URI)
        {
            parseAbsoluteUriRecord(record);
        }else
        {
            LogUtil.e(MyConstant.Tag,Tag_ASSIST+ "Unkonwn TNF"+ tnf);
        }
    }

    private void parseAbsoluteUriRecord(NdefRecord record) {

        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseAbsolute");
        byte[] payload = record.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        //1
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Tnf:"+record.getTnf()+"\n");
        //T
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Type:"+new String(record.getType())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record ID:"+ new String(record.getId())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record payload:"+uri+"\n");
        mPayload.setText("REV:"+uri);
    }

    private void parseWellKnownRecord(NdefRecord record) {

        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"into parseWellKnown");
        Preconditions.checkArgument(Arrays.equals(record.getType(),NdefRecord.RTD_URI));
        byte[] payload = record.getPayload();
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the prefix: "+ prefix +"\n");
        byte[] fullUri = concat(prefix.getBytes(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1, payload.length));
        Uri uri = Uri.parse(new String(fullUri,Charset.forName("UTF-8")));
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Tnf:"+record.getTnf()+"\n");
        //T
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record Type:"+new String(record.getType())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record ID:"+ new String(record.getId())+"\n");
        LogUtil.i(MyConstant.Tag,Tag_ASSIST+"the Record payload:"+uri+"\n");
        mPayload.setText("REV:"+uri);
    }

    private byte[] concat(byte[] bytes, byte[] bytes1) {
        byte[] bufret=null;
        int len1=0;
        int len2=0;
        if(bytes!=null)
            len1=bytes.length;
        if(bytes1!=null)
            len2=bytes1.length;
        if(len1+len2>0)
            bufret=new byte[len1+len2];
        if(len1>0)
            System.arraycopy(bytes,0,bufret,0,len1);
        if(len2>0)
            System.arraycopy(bytes1,0,bufret,len1,len2);
        return bufret;
    }

    public static boolean isUri(NdefRecord record)
    {
        if(record.getTnf()==NdefRecord.TNF_WELL_KNOWN)
        {
            if(Arrays.equals(record.getType(),NdefRecord.RTD_URI))
            {
                return true;
            }else
            {
                return false;
            }
        }else if(record.getTnf()==NdefRecord.TNF_ABSOLUTE_URI)
        {
            return true;
        }else
        {
            return false;
        }
    }

    /*
    NFC Forum "URI Record Type Definition"

    This is a mapping of "URI Identifier Codes" to URI string prefix,
    per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static final BiMap<Byte,String> URI_PREFIX_MAP = ImmutableBiMap.<Byte,String>builder()
            .put((byte)0x00,"").put((byte)0x01,"http://www.").put((byte)0x02,"https://www.")
            .put((byte)0x03,"http://").put((byte)0x04,"https://").put((byte)0x05,"tel:")
            .put((byte)0x06,"mailto:").put((byte)0x07,"ftp://anonymous:anonymous@").put((byte)0x08,"ftp://ftp.")
            .put((byte)0x09,"ftps://").put((byte)0x0A,"sftp://").put((byte)0x0B,"smb://")
            .put((byte)0x0C,"nfs://").put((byte)0x0D,"ftp://").put((byte)0x0E,"dav://")
            .put((byte)0x0F,"news:").put((byte)0x10,"telnet://").put((byte)0x11,"imap:")
            .put((byte)0x12,"rtsp://").put((byte)0x13,"urn:").put((byte)0x14,"pop:")
            .put((byte)0x15,"sip:").put((byte)0x16,"sips:").put((byte)0x17,"tftp:")
            .put((byte)0x18,"btspp://").put((byte)0x19,"bt12cap://").put((byte)0x1A,"btgoep://")
            .put((byte)0x1B,"tcpodex://").put((byte)0x1C,"irdaobex://").put((byte)0x1D,"file://")
            .put((byte)0x1E,"urn:epc:id:").put((byte)0x1F,"urn:epc:tag:").put((byte)0x20,"urn:epc:pat:")
            .put((byte)0x21,"urn:epx:raw:").put((byte)0x22,"urn:epc:").put((byte)0x23,"urn:nfc:").build();


    private void enableForegroundDispatch() {
        if(mNfcAdapter!=null)
        {
            mNfcAdapter.enableForegroundDispatch(this,mNfcPendingIntent,null,null);
        }
    }

    private void disableForeGroundDispatch() {
        if(mNfcAdapter!=null)
        {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void checkNFCFunction() {
        //TODO Auto-generated method stub
        //getting the default NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //check the NFC adapter first
        if(mNfcAdapter == null)
        {
            //mTextView.setText("NFC adapter is not available");

            AlertDialog.Builder customBuilder;
            customBuilder = new AlertDialog.Builder(mContext);
            final Dialog dialog =customBuilder.create();
            customBuilder.setTitle(getString(R.string.inquire)).setMessage(getString(R.string.nfc_notice2))
                    .setNegativeButton(getString(R.string.no),new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setPositiveButton(getString(R.string.yes),new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            SetDialogWidth(dialog).show();
            return;
        }else
        {
            if(!mNfcAdapter.isEnabled())
            {

                AlertDialog.Builder customBuilder = new AlertDialog.Builder(mContext);
                final Dialog dialog= customBuilder.create();
                customBuilder.setTitle(getString(R.string.inquire)).setMessage(getString(R.string.nfc_notice3))
                        .setNegativeButton(getString(R.string.no),new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setPositiveButton(getString(R.string.yes),new
                        DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(setnfc);
                    }
                });

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                SetDialogWidth(dialog).show();
                return;
            }
        }
    }

    private Dialog SetDialogWidth(Dialog dialog) {
        DisplayMetrics dm=new DisplayMetrics();
        //取得窗口属性
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //取得窗口的宽度
        int screenWidth = dm.widthPixels;
        //窗口高度
        int screenHeight = dm.heightPixels;
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if(screenWidth>screenHeight)
        {
            params.width = (int)(((float)screenHeight)*0.875);
        }else
        {
            params.width = (int)(((float)screenWidth)*0.875);
        }
        dialog.getWindow().setAttributes(params);
        return dialog;
    }


}
