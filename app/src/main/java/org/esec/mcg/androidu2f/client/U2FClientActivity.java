package org.esec.mcg.androidu2f.client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.client.model.U2FClient;
import org.esec.mcg.androidu2f.client.model.U2FClientImpl;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.token.LocalU2FToken;
import org.esec.mcg.androidu2f.token.U2FToken;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.utils.logger.LogUtils;

public class U2FClientActivity extends AppCompatActivity {

    private static final int REG_ACTIVITY_RES_1 = 1;

    private TextView operationTextView;
    private TextView operationMessageTextView;
    private Button swipeCardButton;
    private Button localTokenButton;

    private String u2fIntentType;
    private String message;

    private U2FToken u2fToken;
    private U2FClient u2fClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_u2f_token);
//        operationTextView = (TextView) findViewById(R.id.operation_tv);
//        operationMessageTextView = (TextView) findViewById(R.id.operation_message_tv);
//        swipeCardButton = (Button) findViewById(R.id.swipe_card_btn);
//        localTokenButton = (Button) findViewById(R.id.local_token_btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        u2fIntentType = extras.getString("U2FIntentType");
        message = extras.getString("message");
//        operationTextView.setText(u2fIntentType);
//        operationMessageTextView.setText(message);

        try {
            // The caller's appid. In this case, the caller is self.
            u2fClient = new U2FClientImpl(this.getPackageManager().getPackageInfo(this.getPackageName(), this.getPackageManager().GET_SIGNATURES));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (u2fIntentType.equals(U2FIntentType.U2F_OPERATION_REG.name())) {
            try {
                RegisterRequest registerRequest;
                registerRequest = u2fClient.register(message);
                if ( registerRequest != null ) {
                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                    i.addCategory("android.intent.category.DEFAULT");
                    i.setType("application/fido.u2f_token+json");
                    Bundle data = new Bundle();
//                    data.put

                    data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG.name());
                    i.putExtras(data);
                    startActivityForResult(i, REG_ACTIVITY_RES_1);
                }
            } catch (U2FException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            LogUtils.d("resultCode = " + resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
        setResult(RESULT_OK);
        finish();
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Listener to local token button
     * @param view
     */
//    public void localProceed(View view) {
//        u2fToken = new LocalU2FToken();
//        try {
//            // The caller's appid. In this case, the caller is self.
//            u2fClient = new U2FClientImpl(u2fToken, this.getPackageManager().getPackageInfo(this.getPackageName(), this.getPackageManager().GET_SIGNATURES));
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (u2fIntentType.equals(U2FIntentType.U2F_OPERATION_REG.name())) {
//            try {
//                u2fClient.register(message);
//            } catch (U2FException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//    }
}
