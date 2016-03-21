package org.esec.mcg.androidu2f.client.card;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.client.card.APDU.APDUError;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

public class JavaCardTokenActivity extends AppCompatActivity implements ReadCardTask.OnCardReadFinishListener {

    private NfcAdapter mAdapter;
    private PendingIntent mPendingintent;
    private IntentFilter[] mFilters;
    private String[][] mTechList;

    private JavaCardTokenReader mReader;

    private String u2fTokenIntentType;
    private byte[] rawMessage;

    private TextView mJavaCardMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_card_token);
        mJavaCardMessage = (TextView)findViewById(R.id.java_card_message);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingintent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mFilters = new IntentFilter[] {ndef};
        mTechList = new String[][] { new String[] {IsoDep.class.getName()},
                                     new String[] {NfcA.class.getName()}};
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingintent, mFilters, mTechList);
        }

        Bundle extras = getIntent().getExtras();
        u2fTokenIntentType = extras.getString("U2FTokenIntentType");
        rawMessage = extras.getByteArray("RawMessage");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            mReader = JavaCardTokenReader.getInstance();
            mReader.init(tag);
            mReader.connect();
//            enum operation_type;
//            if (u2fTokenIntentType.equals(U2FTokenIntentType.U2F_OPERATION_REG.name())) {
//                operation_type = U2FTokenIntentType.U2F_OPERATION_REG;
//            }
            // TODO: 2016/3/18  
            startReadTask();
        } else {
            LogUtils.d("tag is null");
        }
    }

    private void startReadTask() {
        // TODO: 2016/3/18 post a handler.
        Enum operation = null;
        if (u2fTokenIntentType.equals(U2FTokenIntentType.U2F_OPERATION_REG.name())) {
            operation = U2FTokenIntentType.U2F_OPERATION_REG;
        } else if (u2fTokenIntentType.equals(U2FTokenIntentType.U2F_OPERATION_SIGN.name())) {
            operation = U2FTokenIntentType.U2F_OPERATION_SIGN;
            // because rawMessage's first byte is control byte
            System.arraycopy(rawMessage, 1, rawMessage, 0, rawMessage.length - 1);
        }
        ReadCardTask task = new ReadCardTask(mReader, this, rawMessage, operation);
        task.startExecute();
    }

    @Override
    public void onCardReadSuccess(byte[] result) {
        mJavaCardMessage.setText(ByteUtil.ByteArrayToHexString(result));
        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        Bundle data = new Bundle();
        data.putByteArray("RawMessage", result);
        i.putExtras(data);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onCardReadFial(APDUError e) {
        // TODO: 2016/3/21 Handle this
        setResult(RESULT_CANCELED);
        finish();
    }

}
