// IASSDataModulesService.aidl
package com.erfangholami.androidsolidservices.shared;

import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactsModuleInterface;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBookList;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.Contact;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.NewContact;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullContact;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.Email;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.PhoneNumber;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.Name;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.Group;
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullGroup;

interface IASSDataModulesService {

    IASSContactsModuleInterface getContactsDataModuleInterface();

    //Declear other data modules here
}