package org.esec.mcg.androidu2f.token;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wepayplugin.nfcstd.WepayPlugin;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2f.token.msg.AuthenticationResponse;
import org.esec.mcg.androidu2f.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2f.token.msg.RegistrationResponse;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONObject;

import java.util.Random;

public class U2FTokenActivity extends AppCompatActivity {

    private TextView operationTextView;
    private TextView operationMessageTextView;

    private String u2fIntentType;
    private byte[] rawMessage;

    private U2FToken u2fToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_u2f_token);
//
//        operationTextView = (TextView) findViewById(R.id.operation_tv);
//        operationMessageTextView = (TextView) findViewById(R.id.operation_message_tv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        u2fIntentType = extras.getString("U2FIntentType");
        rawMessage = extras.getByteArray("RawMessage");

        if (LocalU2FToken.USER_PRESECE) {
            localProceed();
            return;
        }
        // user presence with bank card
        JSONObject jsonm = new JSONObject();
        try {
        jsonm.put(WepayPlugin.merchantCode, "1000000200");
        jsonm.put(WepayPlugin.outOrderId, getRandomNum(12));
        jsonm.put(WepayPlugin.nonceStr, getRandomNum(32));
        jsonm.put(WepayPlugin.noticeUrl, "http://192.168.6.34:10000/merchant/telcharge_notice.jsp");
        /********MD5签名*********/
        String signmd5Src = MD5Encrypt.signJsonStringSort(jsonm.toString());
        String signmd5 = MD5Encrypt.sign(signmd5Src, "123456ADSEF");
        jsonm.put(WepayPlugin.sign, signmd5);
        } catch (Exception e) {
        e.printStackTrace();
        }
        WepayPlugin.getInstance().genWepayQueryRequestJar(this, jsonm.toString(), true);

//        localProceed();
//        operationTextView.setText(u2fIntentType);
//        operationMessageTextView.setText(ByteUtil.ByteArrayToHexString(rawMessage));
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Token simulator
     */
    public void localProceed() {
        u2fToken = new LocalU2FToken(this);
        if (u2fIntentType.equals(U2FIntentType.U2F_OPERATION_REG.name())) { // register
            try {
                RegistrationRequest registrationRequest = RawMessageCodec.decodeRegistrationRequest(rawMessage);
                RegistrationResponse registrationResponse = u2fToken.register(registrationRequest);
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                Bundle data = new Bundle();
                data.putByteArray("RawMessage", RawMessageCodec.encodeRegistrationResponse(registrationResponse));
                i.putExtras(data);
                setResult(RESULT_OK, i);
                finish();
            } catch (U2FException e) {
                // TODO: 2016/3/10 How to handle the exception?
                setResult(RESULT_CANCELED);
                finish();
                e.printStackTrace();
                return;
            }
        } else if (u2fIntentType.equals(U2FIntentType.U2F_OPERATION_SIGN.name())) { // sign
            try {
                AuthenticationRequest authenticationRequest = RawMessageCodec.decodeAuthenticationRequest(rawMessage);
                AuthenticationResponse authenticationResponse = u2fToken.authenticate(authenticationRequest);
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                Bundle data = new Bundle();
                data.putByteArray("RawMessage", RawMessageCodec.encodeAuthenticationResponse(authenticationResponse));
                i.putExtras(data);
                setResult(RESULT_OK, i);
                finish();
            } catch (U2FException e) {
                setResult(RESULT_CANCELED);
                finish();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WepayPlugin.reqCod) {
            if (data != null) {
                Bundle mbundle = data.getExtras();
                /**
                 * 支付取消
                 */
                int REAULT_CANCEL_CODE = 24;
                /**
                 * 支付成功
                 */
                int REAULT_SUCCESS_CODE = 25;
                /**
                 * 支付出错
                 */
                int REAULT_ERROR_CODE = 26;
                if (mbundle.getBoolean("isPay")) //支付
                {
                    if (mbundle.getInt("code") == REAULT_SUCCESS_CODE) {
                        Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
                        Log.i("Nfc-Pay:", mbundle.getString("data"));
                    } else if (mbundle.getInt("code") == REAULT_ERROR_CODE)
                        Toast.makeText(this, "支付失败", Toast.LENGTH_SHORT).show();
                    else if (mbundle.getInt("code") == REAULT_CANCEL_CODE)
                        Toast.makeText(this, "支付已取消", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();

                } else //余额查询
                {
                    if (mbundle.getInt("code") == REAULT_SUCCESS_CODE) {
                        Toast.makeText(this, " 余额查询成功", Toast.LENGTH_SHORT).show();
                        Log.i("Nfc-Query:", mbundle.getString("data"));
                        LocalU2FToken.USER_PRESECE = true;

                    } else if (mbundle.getInt("code") == REAULT_ERROR_CODE)
                        Toast.makeText(this, " 余额查询失败", Toast.LENGTH_SHORT).show();
                    else if (mbundle.getInt("code") == REAULT_CANCEL_CODE)
                        Toast.makeText(this, " 余额查询已取消", Toast.LENGTH_SHORT).show();

                }

            } else {
                Toast.makeText(this, "出错啦", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == WepayPlugin.cardNoCod){
            if (data != null) {
                Bundle mbundle = data.getExtras();
                /**
                 * 支付取消
                 */
                int REAULT_CANCEL_CODE = 24;
                /**
                 * 支付成功
                 */
                int REAULT_SUCCESS_CODE = 25;
                /**
                 * 支付出错
                 */
                int REAULT_ERROR_CODE = 26;

                if (mbundle.getInt("code") == REAULT_SUCCESS_CODE) {
                    Toast.makeText(this, "获取卡号成功", Toast.LENGTH_SHORT).show();
                    Log.i("Nfc-CardNo:", mbundle.getString("data"));
                } else if (mbundle.getInt("code") == REAULT_ERROR_CODE) {
                    Toast.makeText(this, "获取卡号失败", Toast.LENGTH_SHORT).show();
                } else if (mbundle.getInt("code") == REAULT_CANCEL_CODE) {
                    Toast.makeText(this, "获取卡号取消", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "出错啦", Toast.LENGTH_SHORT).show();
            }
        }

        if (LocalU2FToken.USER_PRESECE) {
            localProceed();
        }
    }

    /**
     * 获取随机字符串
     *
     * @param len 长度
     * @return 随机字符串
     */
    public static String getRandomNum(int len) {
        String[] arr = {"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9"};
        String s = "";
        if (len <= 0) {
            return s;
            }
        Random ra = new Random();
        int arrLen = arr.length;
        for (int i = 0; i < len; i++) {
            s += arr[ra.nextInt(arrLen)];
            }
        return s;
    }
}
