// IASSContactModuleAddressBookListCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact;

import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBookList;

interface IASSContactModuleAddressBookListCallback {
    oneway void valueChanged(in @nullable AddressBookList addressBookList);
}