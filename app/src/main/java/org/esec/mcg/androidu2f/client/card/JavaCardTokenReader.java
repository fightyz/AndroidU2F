package org.esec.mcg.androidu2f.client.card;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import org.esec.mcg.androidu2f.client.card.APDU.APDUError;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;

/**
 * Created by yz on 2016/3/18.
 */
public class JavaCardTokenReader implements SmardCardReader {

    private static final byte[] GET_RESPONSE_COMMAND = {0x00, (byte) 0xc0, 0x00, 0x00, (byte) 0xff};

    private static JavaCardTokenReader INSTANCE = null;

    private IsoDep mIsoDepTag;

    public static JavaCardTokenReader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JavaCardTokenReader();
        }
        return INSTANCE;
    }

    private JavaCardTokenReader() {
        mIsoDepTag = null;
    }

    @Override
    public void init(Tag tag) {
        if (tag != null) {
            mIsoDepTag = IsoDep.get(tag);
            mIsoDepTag.setTimeout(5000);
        }
    }

    @Override
    public void connect() {
        if (mIsoDepTag != null) {
            try {
                mIsoDepTag.connect();
                LogUtils.d("connected!!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] transceive(byte[] cmd) throws APDUError {
        int status = 0x6100;
        byte[] response = new byte[0];
        if (mIsoDepTag != null) {
            while ((status &0xff00) == 0x6100) {
                byte[] resp = new byte[0];
                try {
                    resp = mIsoDepTag.transceive(cmd);
                    LogUtils.d(ByteUtil.ByteArrayToHexString(resp));
                    status = ((0xff & resp[resp.length - 2]) << 8) | (0xff & resp[resp.length - 1]);
                    response = concat(response, resp, resp.length - 2);
                    cmd = GET_RESPONSE_COMMAND;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (status != 0x9000) {
                throw new APDUError(status);
            }
        }
        return response;
    }

    @Override
    public boolean isConnected() {
        if (mIsoDepTag == null) {
            return false;
        } else {
            return mIsoDepTag.isConnected();
        }
    }

    @Override
    public void close() {
        if (mIsoDepTag != null) {
            try {
                mIsoDepTag.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] concat(byte[] a, byte[] b, int length) {
        byte[] res = new byte[a.length + length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, length);
        return res;
    }
}
