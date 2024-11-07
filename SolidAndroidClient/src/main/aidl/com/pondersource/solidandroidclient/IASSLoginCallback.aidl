// IASSLoginCallback.aidl
package com.pondersource.solidandroidclient;

// Declare any non-default types here with import statements

oneway interface IASSLoginCallback {

    void onResult(boolean granted);
}