// IASSAuthenticatorService.aidl
package com.erfangholami.androidsolidservices.shared;

import com.erfangholami.androidsolidservices.shared.domain.auth.IASSLoginCallback;
import com.erfangholami.androidsolidservices.shared.domain.auth.IASSLogoutCallback;

interface IASSAuthenticatorService {

    boolean hasLoggedIn();
    boolean isAppAuthorized(String webId);
    void requestLogin(IASSLoginCallback callback);
    void disconnectFromSolid(String webId, IASSLogoutCallback callback);
}