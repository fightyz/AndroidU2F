package org.esec.mcg.androidu2f.client.msg;

/**
 * Created by yz on 2016/1/21.
 */
public class RegisterResponse {
    /** websafe-base64(raw registration response message) */
    private final String registrationData;

    /** websafe-base64(UTF8(stringified(client data))) */
    private final String clientData;

    public RegisterResponse(String registrationData, String clientData) {
        this.registrationData = registrationData;
        this.clientData = clientData;
    }

    public String getRegistrationData() {
        return registrationData;
    }

    public String getClientData() {
        return clientData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((registrationData == null) ? 0 : registrationData.hashCode());
        result = prime * result + ((clientData == null) ? 0 : clientData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        RegisterResponse other = (RegisterResponse) o;
        if (registrationData == null) {
            if (other.registrationData != null)
                return false;
        } else if (!registrationData.equals(other.registrationData))
            return false;
        if (clientData == null) {
            if (other.clientData != null)
                return false;
        } else if (!clientData.equals(other.clientData))
            return false;
        return true;
    }
}
