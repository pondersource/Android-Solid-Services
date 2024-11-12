// IASSAuthenticatorService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSLoginCallback;

interface IASSAuthenticatorService {

    boolean hasLoggedIn();

    boolean isAppAuthorized();

    void requestLogin(IASSLoginCallback callback);
}