package org.esec.mcg.androidu2f.token;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.esec.mcg.androidu2f.R;

public class U2FTokenActivity extends AppCompatActivity {

    private TextView operationTextView;
    private TextView operationMessageTextView;

    private String u2fIntentType;
    private String message;

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
//        message = extras.getString("message");
        operationTextView.setText(u2fIntentType);
//        operationMessageTextView.setText(message);
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void localProceed(View view) {
        u2fToken = new LocalU2FToken();
        // TODO
    }
}
