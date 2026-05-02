// IASSContactsModuleInterface.aidl
package com.pondersource.shared.domain.datamodule.contact;

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
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleAddressBookListCallback;
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleAddressBookCallback;
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleFullContactCallback;
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleFullGroupCallback;

interface IASSContactsModuleInterface {

    void getAddressBooks(String webId, IASSContactModuleAddressBookListCallback callback);

    void createAddressBook(
            String webId,
            String title,
            boolean isPrivate,
            IASSContactModuleAddressBookCallback callback,
            @nullable String storage,
            @nullable String ownerWebId,
            @nullable String container
    );

    void getAddressBook(String webId, String uri, IASSContactModuleAddressBookCallback callback);

    void deleteAddressBook(
        String webId,
        String uri,
        @nullable String ownerWebId,
        IASSContactModuleAddressBookCallback callback
    );

    void createNewContact(
        String webId,
        String addressBookUri,
        in NewContact newContact,
        in List<String> groupUris,
        IASSContactModuleFullContactCallback callback
    );

    void getContact(
        String webId,
        String contactUri,
        IASSContactModuleFullContactCallback callback
    );

    void renameContact(
         String webId,
         String contactUri,
         String newName,
         IASSContactModuleFullContactCallback callback
    );

    void addNewPhoneNumber(
        String webId,
        String contactUri,
        String newPhoneNumber,
        IASSContactModuleFullContactCallback callback
    );

    void addNewEmailAddress(
        String webId,
        String contactUri,
        String newEmailAddress,
        IASSContactModuleFullContactCallback callback
    );

    void removePhoneNumber(
        String webId,
        String contactUri,
        String phoneNumber,
        IASSContactModuleFullContactCallback callback
    );

    void removeEmailAddress(
        String webId,
        String contactUri,
        String emailAddress,
        IASSContactModuleFullContactCallback callback
    );

    void deleteContact(
        String webId,
        String addressBookUri,
        String contactUri,
        IASSContactModuleFullContactCallback callback
    );

    void createNewGroup(
         String webId,
         String addressBookUri,
         String title,
         in List<String> contactUris,
         IASSContactModuleFullGroupCallback callback
    );

    void getGroup(
        String webId,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void deleteGroup(
        String webId,
        String addressBookUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void addContactToGroup(
        String webId,
        String contactUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );

    void removeContactFromGroup(
        String webId,
        String contactUri,
        String groupUri,
        IASSContactModuleFullGroupCallback callback
    );
}