// IASSLogoutCallback.aidl
package com.pondersource.solidandroidclient;

interface IASSLogoutCallback {
     void onResult(boolean granted);
     void onError(int errorCode, String errorMessage);
}