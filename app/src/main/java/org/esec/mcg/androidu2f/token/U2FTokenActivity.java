package org.esec.mcg.androidu2f.token;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;
import org.esec.mcg.utils.ByteUtil;

public class U2FTokenActivity extends AppCompatActivity {

    private TextView operationTextView;
    private TextView operationMessageTextView;

    private String u2fIntentType;
    private byte[] message;

    private U2FToken u2fToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u2f_token);

        operationTextView = (TextView) findViewById(R.id.operation_tv);
        operationMessageTextView = (TextView) findViewById(R.id.operation_message_tv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        u2fIntentType = extras.getString("U2FIntentType");
        message = extras.getByteArray("message");
        operationTextView.setText(u2fIntentType);
        operationMessageTextView.setText(ByteUtil.ByteArrayToHexString(message));
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void localProceed(View view) {
        u2fToken = new LocalU2FToken(this);
        if (u2fIntentType.equals(U2FIntentType.U2F_OPERATION_REG.name())) {
            RegisterRequest registerRequest;
            RegisterResponse registerResponse;
            try {
                registerRequest = RawMessageCodec.decodeRegisterRequest(message);
                registerResponse = u2fToken.register(registerRequest);
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                Bundle data = new Bundle();
                data.putByteArray("message", RawMessageCodec.encodeRegisterResponse(registerResponse));
                data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG_RESULT.name());
                i.putExtras(data);
                setResult(RESULT_OK, i);
                finish();
            } catch (U2FException e) {
                e.printStackTrace();
            }
        }
    }
}
