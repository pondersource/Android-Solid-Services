// IASSContactModuleFullGroupCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact;

import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullGroup;

interface IASSContactModuleFullGroupCallback {
    oneway void valueChanged(in @nullable FullGroup fullGroup);
}