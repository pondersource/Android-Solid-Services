// IASSLogoutCallback.aidl
package com.pondersource.shared.domain.auth;

interface IASSLogoutCallback {
     void onResult(boolean granted);
     void onError(int errorCode, String errorMessage);
}