// IASSContactsModuleInterface.aidl
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

interface IASSContactsModuleInterface {

    @nullable
    AddressBookList getAddressBooks();

    @nullable
    AddressBook createAddressBook(
            String title,
            String storage,
            String ownerWebId,
            String container
    );

    @nullable
    AddressBook getAddressBook(String uri);

    @nullable
    FullContact createNewContact(
        String addressBookUri,
        in NewContact newContact,
        in List<String> groupUris
    );

    @nullable
    FullContact getContact(
        String contactUri
    );

    @nullable
    FullContact renameContact(
        String contactUri,
         String newName
    );

    @nullable
    FullContact addNewPhoneNumber(
        String contactUri,
        String newPhoneNumber
    );

    @nullable
    FullContact addNewEmailAddress(
        String contactUri,
        String newEmailAddress
    );

    @nullable
    FullContact removePhoneNumber(
        String contactUri,
        String phoneNumber
    );

    @nullable
    FullContact removeEmailAddress(
        String contactUri,
        String emailAddress
    );

    @nullable
    FullGroup createNewGroup(
        String addressBookUri,
         String title
    );

    @nullable
    FullGroup getGroup(
        String groupUri
    );

    @nullable
    FullGroup removeGroup(
        String addressBookUri,
        String groupUri
    );

    @nullable
    FullGroup addContactToGroup(
        String contactUri,
        String groupUri
    );

    @nullable
    FullGroup removeContactFromGroup(
        String contactUri,
        String groupUri
    );
}