package org.esec.mcg.androidu2f.client.card;

import android.os.AsyncTask;

import org.esec.mcg.androidu2f.client.card.APDU.APDUError;
import org.esec.mcg.androidu2f.client.card.APDU.RegisterBuilder;
import org.esec.mcg.androidu2f.client.card.APDU.SelectBuilder;
import org.esec.mcg.androidu2f.client.card.APDU.SignBuilder;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

/**
 * Created by yz on 2016/3/18.
 */
public class ReadCardTask {
    private JavaCardTokenReader mReader;
    private OnCardReadFinishListener mListener;
    private Enum operation;
    private AuthenticationRequest[] mAuthenticationRequests;

    private static final byte[] AID = {(byte)0xa0, 0x00, 0x00, 0x06, 0x47, 0x2f, 0x00, 0x01};
    private static final byte[] U2F_V2 = {'U', '2', 'F', '_', 'V', '2'};

    private byte[] mRawMessage;

    public ReadCardTask(JavaCardTokenReader reader,
                        OnCardReadFinishListener listener,
                        byte[] rawMessage, AuthenticationRequest[] authenticationRequests,
                        Enum op) {
        mReader = reader;
        mListener = listener;
        mRawMessage = rawMessage;
        mAuthenticationRequests = authenticationRequests;
        operation = op;
    }

    public void startExecute() {
        SendAPDUsTask task = new SendAPDUsTask();
        task.execute();
    }

    protected class SendAPDUsTask extends AsyncTask<Boolean, Void, byte[]> {
        private byte[] mSelectToken;
        private byte[] mRegisterCmd;
        private byte[] mSigncmd;
        private APDUError e;

        public SendAPDUsTask() {
            mSelectToken = new SelectBuilder().setAID(AID).build();
        }

        @Override
        protected byte[] doInBackground(Boolean... sign) {
            try {
                byte[] response = mReader.transceive(mSelectToken);
                // TODO: 2016/3/22 Check the AID
                LogUtils.d(ByteUtil.ByteArrayToHexString(response));
                if (operation.equals(U2FTokenIntentType.U2F_OPERATION_REG)) {
                    response = doRegister();
                } else if (operation.equals(U2FTokenIntentType.U2F_OPERATION_SIGN)) {
                    response = doSign();
                }

                return response;
            } catch (APDUError apduError) {
                e = apduError;
                return null;
            } finally {
                mReader.close();
            }
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (e != null) {
                mListener.onCardReadFial(e);
            } else {
                mListener.onCardReadSuccess(result);
            }

            super.onPostExecute(result);
        }

        private byte[] doRegister() throws APDUError {
            mRegisterCmd = new RegisterBuilder().setRawMessage(mRawMessage).build();
            LogUtils.d(ByteUtil.ByteArrayToHexString(mRegisterCmd));
            byte[] response = mReader.transceive(mRegisterCmd);
            LogUtils.d(ByteUtil.ByteArrayToHexString(response));
            return response;
        }

        private byte[] doSign() throws APDUError {
            SignBuilder sb = new SignBuilder().setRawMessage(mRawMessage);
            mSigncmd = sb.build();
            LogUtils.d(ByteUtil.ByteArrayToHexString(mSigncmd));
            byte[] response = mReader.transceive(mSigncmd);
//            LogUtils.d(ByteUtil.ByteArrayToHexString(response));
            return response;
        }
    }

    public static interface OnCardReadFinishListener {
        void onCardReadSuccess(byte[] result);
        void onCardReadFial(APDUError e);
    }
}
