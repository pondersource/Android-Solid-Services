// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.shared.domain.datamodule.contact;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;
import com.pondersource.shared.domain.datamodule.contact.FullContact;

interface IASSContactModuleFullContactCallback {
    oneway void valueChanged(in @nullable FullContact fullContact);
}