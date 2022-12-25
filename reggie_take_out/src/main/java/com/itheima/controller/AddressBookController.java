package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.AddressBook;
import com.itheima.exception.CustmException;
import com.itheima.mapper.AddressBookMapper;
import com.itheima.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查找所有已存入的收货地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> addressBook() {
        List<AddressBook> books = addressBookMapper.getAll();
        return R.success(books);
    }

    /**
     * 新增收货地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> addRess(@RequestBody AddressBook addressBook) {
        //调用MP自动获取唯一id的值，并赋值
        addressBook.setId(IdWorker.getId());
        addressBook.setUserId(IdWorker.getId());
        addressBookService.save(addressBook);
        return R.success("收货地址保存成功^_^");
    }

    /**
     * 修改收货地址数据回显
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> selectOne(@PathVariable Long id){
        AddressBook book = addressBookService.getById(id);
        return R.success(book);
    }

    /**
     * 修改收货地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook){
        if (addressBookService.updateById(addressBook)) {
            return R.success("收货地址修改成功^_^");
        }
        return R.error("收货地址修改失败");
    }

    /**
     * 更新默认收货地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> alterDefault(@RequestBody AddressBook addressBook){
        //将原来的默认收货地址重置
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(addressBook!=null,AddressBook::getIsDefault,1).set(AddressBook::getIsDefault,0);
        addressBookService.update(updateWrapper);
        //将id指定的收货地址更新为默认收货地址
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(addressBook!=null,AddressBook::getId,addressBook.getId()).set(AddressBook::getIsDefault,1);
        addressBookService.update(wrapper);
        return R.success("默认地址修改成功");
    }
    @GetMapping("/default")
    public R<AddressBook> getAddress(){
        //条件构造器
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        //查询默认收货地址为1的数据
        wrapper.eq(AddressBook::getIsDefault,1);
        //将查询到的默认地址数据返回
        AddressBook addressBook = addressBookService.getOne(wrapper);
        if (addressBook==null){
            throw new CustmException("默认地址为空，请先填写收货地址!");
        }
        return R.success(addressBook);
    }
}
