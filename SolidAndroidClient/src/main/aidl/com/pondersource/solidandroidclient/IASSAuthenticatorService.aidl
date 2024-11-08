// IASSAuthenticatorService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSLoginCallback;

// Declare any non-default types here with import statements

interface IASSAuthenticatorService {

    boolean hasLoggedIn();

    boolean isAppAuthorized(String appPackageName);

    void requestLogin(String appPackageName, String appName, IASSLoginCallback callback);
}