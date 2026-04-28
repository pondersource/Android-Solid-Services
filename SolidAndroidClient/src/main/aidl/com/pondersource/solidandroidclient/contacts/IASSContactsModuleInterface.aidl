// IASSContactsModuleInterface.aidl
package com.pondersource.solidandroidclient.contacts;

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
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookListCallback;
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookCallback;
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullContactCallback;
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullGroupCallback;

interface IASSContactsModuleInterface {

    void getAddressBooks(IASSContactModuleAddressBookListCallback callback);

    void createAddressBook(
            String title,
            boolean isPrivate,
            IASSContactModuleAddressBookCallback callback,
            @nullable String storage,
            @nullable String ownerWebId,
            @nullable String container
    );

    void getAddressBook(String uri, IASSContactModuleAddressBookCallback callback);

    void deleteAddressBook(
        String uri,
        @nullable String ownerWebId,
        IASSContactModuleAddressBookCallback callback
    );

    void createNewContact(
        String addressBookUri,
        in NewContact newContact,
        in List<String> groupUris,
        IASSContactModuleFullContactCallback callback
    );

    void getContact(
        String contactUri,
        IASSContactModuleFullContactCallback callback
    );

    void renameContact(
         String contactUri,
         String newName,
         IASSContactModuleFullContactCallback callback
    );

    void addNewPhoneNumber(
        String contactUri,
        String newPhoneNumber,
        IASSContactModuleFullContactCallback callback
    );

    void addNewEmailAddress(
        String contactUri,
        String newEmailAddress,
        IASSContactModuleFullContactCallback callback
    );

    void removePhoneNumber(
        String contactUri,
        String phoneNumber,
        IASSContactModuleFullContactCallback callback
    );

    void removeEmailAddress(
        String contactUri,
        String emailAddress,
        IASSContactModuleFullContactCallback callback
    );

    void deleteContact(
        String addressBookUri,
        String contactUri,
        IASSContactModuleFullContactCallback callback
    );

    void createNewGroup(
         String addressBookUri,
         String title,
         in List<String> contactUris,
         IASSContactModuleFullGroupCallback callback
    );

    void getGroup(
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void deleteGroup(
        String addressBookUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void addContactToGroup(
        String contactUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void removeContactFromGroup(
        String contactUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );
}