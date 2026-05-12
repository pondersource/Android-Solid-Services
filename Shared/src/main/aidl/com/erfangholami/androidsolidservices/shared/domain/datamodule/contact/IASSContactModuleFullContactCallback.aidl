// IASSContactModuleAddressBookListCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact;

import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullContact;

interface IASSContactModuleFullContactCallback {
    oneway void valueChanged(in @nullable FullContact fullContact);
}