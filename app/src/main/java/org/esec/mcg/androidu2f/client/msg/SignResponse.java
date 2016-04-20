package org.esec.mcg.androidu2f.client.msg;

/**
 * Created by yz on 2016/4/15.
 */
public class SignResponse implements ResponseData {
    private String keyHandle;
    private String signatureData;
    private String clientData;
}
