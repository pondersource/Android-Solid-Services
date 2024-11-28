// IASSAuthenticatorService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSLoginCallback;
import com.pondersource.solidandroidclient.IASSLogoutCallback;

interface IASSAuthenticatorService {

    boolean hasLoggedIn();

    boolean isAppAuthorized();

    void requestLogin(IASSLoginCallback callback);

    void disconnectFromSolid(IASSLogoutCallback callback);
}