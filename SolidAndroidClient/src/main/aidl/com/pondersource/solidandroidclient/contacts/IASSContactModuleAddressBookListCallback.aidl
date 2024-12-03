// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.data.datamodule.contact.AddressBook;
import com.pondersource.shared.data.datamodule.contact.AddressBookList;

interface IASSContactModuleAddressBookListCallback {
    oneway void valueChanged(in @nullable AddressBookList addressBookList);
}