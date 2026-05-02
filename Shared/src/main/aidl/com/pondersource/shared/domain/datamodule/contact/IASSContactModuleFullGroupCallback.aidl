// IASSContactModuleFullGroupCallback.aidl
package com.pondersource.shared.domain.datamodule.contact;

import com.pondersource.shared.domain.datamodule.contact.AddressBook;
import com.pondersource.shared.domain.datamodule.contact.FullGroup;

interface IASSContactModuleFullGroupCallback {
    oneway void valueChanged(in @nullable FullGroup fullGroup);
}