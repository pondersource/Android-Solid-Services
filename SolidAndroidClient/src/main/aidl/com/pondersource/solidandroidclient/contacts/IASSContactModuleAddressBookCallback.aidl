// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.data.datamodule.contact.AddressBook;

interface IASSContactModuleAddressBookCallback {
    oneway void valueChanged(in @nullable AddressBook addressBook);
}