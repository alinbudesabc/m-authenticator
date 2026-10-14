package com.alinbudesabc.authenticator.exc;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ContactMapper {

    public static void main(String[] args) {
        ContactDomain c1 = new ContactDomain("Work", "phone", "email", "", "");
        ContactDomain c3 = new ContactDomain("Other", "phone", "email", "", "");
        ContactDomain c2 = new ContactDomain("Home", "phone1", "email1", "", "");
        ContactDto contactDto = new ContactMapper().mapContact3(List.of(c1, c2, c3));
        System.out.println(contactDto);
    }


    public ContactDto mapContact(List<ContactDomain> contactList)  {

        Optional<ContactDto> home = contactList.stream()
                .filter(contactDomain -> "Home".equals(contactDomain.type()))
                .map(contactDomain -> new ContactDto(contactDomain.type(), contactDomain.mobile(), contactDomain.companyEmail()))
                .findFirst();

        Optional<ContactDto> work = contactList.stream()
                .filter(contactDomain -> "Work".equals(contactDomain.type()))
                .map(contactDomain -> new ContactDto(contactDomain.type(), contactDomain.mobile(), contactDomain.companyEmail()))
                .findFirst();

        if(home.isPresent()){
            return home.get();
        }

        return work.orElse(null);

    }

    public ContactDto mapContact3(List<ContactDomain> domains){
        Optional<ContactDto> contact = domains.stream()
                .filter(contactDomain -> "Work".equals(contactDomain.type()) || "Home".equals(contactDomain.type()))
                .sorted(Comparator.comparing(c ->
                        switch (c.type()) {
                            case "Home" -> 0;
                            case "Work" -> 1;
                            default -> 3;
                        }
                ))
                .map(contactDomain -> new ContactDto(contactDomain.type(), contactDomain.telephone(), contactDomain.personalEmail() ))
                .findFirst();

        return contact.orElse(null);
    }

    public ContactDto mapContact2(List<ContactDomain> contactList) {
        return contactList.stream()
                .filter(c -> "Home".equals(c.type()) || "Work".equals(c.type()))
                .sorted(Comparator.comparing(c -> "Home".equals(c.type()) ? 0 : 1)) // Home first
                .map(c -> new ContactDto(c.type(), c.mobile(), c.companyEmail()))
                .findFirst()
                .orElse(null);
    }

}



record ContactDomain(String type,

                     String telephone,

                     String mobile,

                     String personalEmail,

                     String companyEmail) {

}

record ContactDto(String type, String phone, String email) {

}


//TODO: You should return contact type "Home", if doesn't exist return "Work" type, if doesn't exist return null;

//TODO: Contact should be mapped to DTO

//TODO: It should be done using Java stream, don't use "for" and "if"

//TODO: * Write tests
