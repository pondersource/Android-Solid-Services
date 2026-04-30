// IASSAuthenticatorService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSLoginCallback;
import com.pondersource.solidandroidclient.IASSLogoutCallback;

interface IASSAuthenticatorService {

    boolean hasLoggedIn();

    boolean isAppAuthorized(String webId);

    void requestLogin(IASSLoginCallback callback);

    void disconnectFromSolid(String webId, IASSLogoutCallback callback);
}