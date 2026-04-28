// IASSDataModulesService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.contacts.IASSContactsModuleInterface;
import com.pondersource.shared.domain.datamodule.contact.AddressBook;
import com.pondersource.shared.domain.datamodule.contact.AddressBookList;
import com.pondersource.shared.domain.datamodule.contact.Contact;
import com.pondersource.shared.domain.datamodule.contact.NewContact;
import com.pondersource.shared.domain.datamodule.contact.FullContact;
import com.pondersource.shared.domain.datamodule.contact.Email;
import com.pondersource.shared.domain.datamodule.contact.PhoneNumber;
import com.pondersource.shared.domain.datamodule.contact.Name;
import com.pondersource.shared.domain.datamodule.contact.Group;
import com.pondersource.shared.domain.datamodule.contact.FullGroup;

interface IASSDataModulesService {

    IASSContactsModuleInterface getContactsDataModuleInterface();

    //Declear other data modules here
}