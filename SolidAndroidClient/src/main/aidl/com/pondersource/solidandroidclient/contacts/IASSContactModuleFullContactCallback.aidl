// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.solidandroidclient.contacts;

import com.pondersource.shared.data.datamodule.contact.AddressBook;
import com.pondersource.shared.data.datamodule.contact.FullContact;

interface IASSContactModuleFullContactCallback {
    oneway void valueChanged(in @nullable FullContact fullContact);
}