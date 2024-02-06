package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.user.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    AddressBookService addressBookService;
    @Autowired
    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        List<AddressBook> addressBookList = addressBookService.list(BaseContext.getCurrentId());
        return Result.success(addressBookList);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> selectById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.selectById(id);
        return Result.success(addressBook);
    }

    @PostMapping
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook) {
        addressBookService.add(addressBook);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("/根据id修改地址")
    public Result edit(@RequestBody AddressBook addressBook) {
        addressBookService.edit(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("/根据id删除地址")
    public Result deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultById(@RequestBody AddressBook addressBook) {
        addressBookService.setDefaultById(addressBook);
        return Result.success();
    }

    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> selectDefault() {
        AddressBook defaultAddressBook = addressBookService.selectDefault(BaseContext.getCurrentId());
        return Result.success(defaultAddressBook);
    }

}
