package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;

/**
 * Created by yz on 2016/1/18.
 */
public interface Crypto {
    byte[] computeSha256(String message) throws U2FException;
}
