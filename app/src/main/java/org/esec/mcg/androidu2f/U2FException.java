package org.esec.mcg.androidu2f;

import org.esec.mcg.androidu2f.client.msg.ErrorCode;

/**
 * Created by yz on 2016/1/14.
 */
@SuppressWarnings("serial")
public class U2FException extends Exception {
    private final ErrorCode errorCode;
    public U2FException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

//    public U2FException(String message, Throwable cause) {
//        super(message, cause);
//    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
