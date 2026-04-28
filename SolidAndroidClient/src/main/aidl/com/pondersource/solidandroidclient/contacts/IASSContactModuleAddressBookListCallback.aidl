// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;
import com.pondersource.shared.domain.datamodule.contact.AddressBookList;

interface IASSContactModuleAddressBookListCallback {
    oneway void valueChanged(in @nullable AddressBookList addressBookList);
}