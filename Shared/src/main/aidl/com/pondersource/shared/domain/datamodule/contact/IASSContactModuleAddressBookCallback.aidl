// IASSContactModuleAddressBookListCallback.aidl
package com.pondersource.shared.domain.datamodule.contact;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;

interface IASSContactModuleAddressBookCallback {
    oneway void valueChanged(in @nullable AddressBook addressBook);
}