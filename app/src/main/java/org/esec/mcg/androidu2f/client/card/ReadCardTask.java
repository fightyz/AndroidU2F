package org.esec.mcg.androidu2f.client.card;

import android.os.AsyncTask;

import org.esec.mcg.androidu2f.client.card.APDU.SelectBuilder;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

/**
 * Created by yz on 2016/3/18.
 */
public class ReadCardTask {
    private JavaCardTokenReader mReader;
    private static final byte[] aid = {(byte)0xa0, 0x00, 0x00, 0x06, 0x47, 0x2f, 0x00, 0x01};

    public ReadCardTask(JavaCardTokenReader reader) {
        mReader = reader;
    }

    public void startExecute() {
        SendAPDUsTask task = new SendAPDUsTask();
        task.execute();
    }

    protected class SendAPDUsTask extends AsyncTask<Void, Void, Void> {
        private byte[] mSelectToken;

        public SendAPDUsTask() {
            mSelectToken = new SelectBuilder().setAID(aid).build();
        }

        @Override
        protected Void doInBackground(Void... params) {
            byte[] response = null;
            response = mReader.transceive(mSelectToken);
            LogUtils.d(ByteUtil.ByteArrayToHexString(response));
            return null;
        }
    }
}
