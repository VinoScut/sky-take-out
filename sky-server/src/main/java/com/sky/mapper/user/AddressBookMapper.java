package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressBookMapper extends BaseMapper<AddressBook> {

    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> listByUserId(Long userId);

    @Update("update address_book set is_default = 1 where id = #{id}")
    void setDefaultById(Long id);

    @Select("select * from address_book where user_id = #{userId} and is_default = 1")
    AddressBook selectDefault(Long userId);

    @Update("update address_book set is_default = 0 where user_id = #{userId} and is_default = 1")
    void disableOldDefaultAddress(Long userId);

    @Select("select detail from address_book where id = #{addressBookId}")
    String getAddressById(Long addressBookId);

    @Select("select consignee from address_book where id = #{addressBookId}")
    String getConsigneeById(Long addressBookId);

    @Select("select phone from address_book where id = #{addressBookId}")
    String getPhoneById(Long addressBookId);
}
