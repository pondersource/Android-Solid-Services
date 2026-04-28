// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;

interface IASSContactModuleAddressBookCallback {
    oneway void valueChanged(in @nullable AddressBook addressBook);
}