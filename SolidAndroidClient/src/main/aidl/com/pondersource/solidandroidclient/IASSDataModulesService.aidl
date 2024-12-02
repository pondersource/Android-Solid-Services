// IASSDataModulesService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSContactsModuleInterface;
import com.pondersource.shared.data.datamodule.contact.AddressBook;
import com.pondersource.shared.data.datamodule.contact.AddressBookList;
import com.pondersource.shared.data.datamodule.contact.Contact;
import com.pondersource.shared.data.datamodule.contact.NewContact;
import com.pondersource.shared.data.datamodule.contact.FullContact;
import com.pondersource.shared.data.datamodule.contact.Email;
import com.pondersource.shared.data.datamodule.contact.PhoneNumber;
import com.pondersource.shared.data.datamodule.contact.Name;
import com.pondersource.shared.data.datamodule.contact.Group;
import com.pondersource.shared.data.datamodule.contact.FullGroup;

interface IASSDataModulesService {

    IASSContactsModuleInterface getContactsDataModuleInterface();

    //Declear other data modules here
}