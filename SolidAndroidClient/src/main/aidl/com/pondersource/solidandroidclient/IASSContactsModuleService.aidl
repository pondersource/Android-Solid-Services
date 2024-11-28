// IASSContactsModuleService.aidl
package com.pondersource.solidandroidclient;

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

interface IASSContactsModuleService {

    AddressBookList getAddressBooks();

    String createAddressBook(
            String title,
            String storage,
            String ownerWebId,
            String container
    );

    AddressBook getAddressBook(String uri);


    String createNewContact(
        String addressBookUri,
        in NewContact newContact,
        in List<String> groupUris
    );

    FullContact getContact(
        String contactUri
    );

    void renameContact(
        String contactUri,
         String newName
    );

    FullContact addNewPhoneNumber(
        String contactUri,
        String newPhoneNumber
    );

    FullContact addNewEmailAddress(
        String contactUri,
        String newEmailAddress
    );

    boolean removePhoneNumber(
        String contactUri,
        String phoneNumber
    );

    boolean removeEmailAddress(
        String contactUri,
        String emailAddress
    );

    String createNewGroup(
        String addressBookUri,
         String title
    );

    FullGroup getGroup(
        String groupUri
    );

    boolean removeGroup(
        String addressBookUri,
        String groupUri
    );

    void addContactToGroup(
        String contactUri,
        String groupUri
    );

    boolean removeContactFromGroup(
        String contactUri,
        String groupUri
    );
}