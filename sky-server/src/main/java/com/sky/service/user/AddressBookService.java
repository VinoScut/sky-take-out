package com.sky.service.user;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    List<AddressBook> list(Long currentId);

    AddressBook selectById(Long id);

    void add(AddressBook addressBook);

    void edit(AddressBook addressBook);

    void deleteById(Long id);

    void setDefaultById(AddressBook addressBook);

    AddressBook selectDefault(Long userId);
}
