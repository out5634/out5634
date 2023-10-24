package com.github.system.task.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.system.task.entity.AutoIndex;
import com.github.system.task.service.AutoIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "任务")
@RestController
@RequestMapping("/auto/index")
public class AutoIndexController {

    @Resource
    private AutoIndexService autoIndexService;

    @ApiOperation("列表")
    @RequestMapping("/list")
    public List<AutoIndex> list() {
        LambdaQueryWrapper<AutoIndex> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AutoIndex::getId, AutoIndex::getCode, AutoIndex::getName)
                .eq(AutoIndex::getEnable, 1);
        return autoIndexService.list(lambdaQueryWrapper);
    }

    @ApiOperation("管理员分页列表")
    @RequestMapping("/admin/page")
    @SaCheckRole("ADMIN")
    public Page<AutoIndex> adminPage(Page<AutoIndex> page) {
        LambdaQueryWrapper<AutoIndex> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        return autoIndexService.page(page, lambdaQueryWrapper);
    }


}