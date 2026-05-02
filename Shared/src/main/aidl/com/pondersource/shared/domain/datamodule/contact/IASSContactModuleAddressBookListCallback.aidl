// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.shared.domain.datamodule.contact;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;
import com.pondersource.shared.domain.datamodule.contact.AddressBookList;

interface IASSContactModuleAddressBookListCallback {
    oneway void valueChanged(in @nullable AddressBookList addressBookList);
}