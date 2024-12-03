// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.data.datamodule.contact.AddressBook;
import com.pondersource.shared.data.datamodule.contact.FullGroup;

interface IASSContactModuleFullGroupCallback {
    oneway void valueChanged(in @nullable FullGroup fullGroup);
}