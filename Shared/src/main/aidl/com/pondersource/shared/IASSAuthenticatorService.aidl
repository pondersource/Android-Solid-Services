// IASSAuthenticatorService.aidl
package com.pondersource.shared;

import com.pondersource.shared.domain.auth.IASSLoginCallback;
import com.pondersource.shared.domain.auth.IASSLogoutCallback;

interface IASSAuthenticatorService {

    boolean hasLoggedIn();
    boolean isAppAuthorized(String webId);
    void requestLogin(IASSLoginCallback callback);
    void disconnectFromSolid(String webId, IASSLogoutCallback callback);
}