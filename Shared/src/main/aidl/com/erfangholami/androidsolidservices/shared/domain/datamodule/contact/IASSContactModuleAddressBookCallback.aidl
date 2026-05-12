// IASSContactModuleAddressBookListCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact;

import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook;

interface IASSContactModuleAddressBookCallback {
    oneway void valueChanged(in @nullable AddressBook addressBook);
}