package org.esec.mcg.androidu2f.client.card;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;

/**
 * Created by yz on 2016/3/18.
 */
public class JavaCardTokenReader implements SmardCardReader {

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
    public byte[] transceive(byte[] cmd) {
        byte[] response = null;
        if (mIsoDepTag != null) {
            try {
                response = mIsoDepTag.transceive(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            LogUtils.d(ByteUtil.ByteArrayToHexString(response));
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
}
