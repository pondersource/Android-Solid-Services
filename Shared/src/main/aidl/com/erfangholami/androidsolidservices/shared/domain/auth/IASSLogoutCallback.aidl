// IASSLogoutCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.auth;

interface IASSLogoutCallback {
     void onResult(boolean granted);
     void onError(int errorCode, String errorMessage);
}