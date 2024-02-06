package com.sky.service.user.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.user.AddressBookMapper;
import com.sky.service.user.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list(Long userId) {
        return addressBookMapper.listByUserId(userId);
    }

    @Override
    public AddressBook selectById(Long id) {
        return addressBookMapper.selectById(id);
    }

    @Override
    public void add(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.insert(addressBook);
    }

    @Override
    public void edit(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public void setDefaultById(AddressBook addressBook) {
        //先将旧的默认地址取消
        addressBookMapper.disableOldDefaultAddress(BaseContext.getCurrentId());
        //然后再将新地址设置为默认地址
        addressBookMapper.setDefaultById(addressBook.getId());
    }

    @Override
    public AddressBook selectDefault(Long userId) {
        return addressBookMapper.selectDefault(userId);
    }


}
