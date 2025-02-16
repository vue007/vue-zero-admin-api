package com.zero.admin.controller.system;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    public String add(Object user) {
        return "success";
    }
}
