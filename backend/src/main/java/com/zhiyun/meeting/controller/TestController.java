package com.zhiyun.meeting.controller;

import com.zhiyun.meeting.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/hello")
    public Result<String> hello() {
        return Result.success("智云会议后端启动成功");
    }
}