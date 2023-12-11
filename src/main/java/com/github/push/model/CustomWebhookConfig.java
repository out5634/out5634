package com.github.push.model;

import com.github.push.base.annotation.PushEntity;
import com.github.push.base.annotation.PushProperty;
import com.github.push.base.annotation.PushPropertyOptions;
import com.github.push.base.model.PushBaseConfig;
import com.github.push.constant.PushTypeConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@PushEntity(value = PushTypeConstant.CUSTOM, order = 999)
public class CustomWebhookConfig extends PushBaseConfig {

    @PushProperty(value = "请求地址")
    @NotBlank
    private String url;

    @PushProperty(value = "请求方式", options = {
            @PushPropertyOptions(num = 0, name = "GET"),
            @PushPropertyOptions(num = 1, name = "POST JSON"),
            @PushPropertyOptions(num = 2, name = "POST Form-data"),
            @PushPropertyOptions(num = 3, name = "POST x-www-UrlEncode")
    })
    private Integer requestType = 0;

    @PushProperty(value = "成功标识", desc = "填写成功标识")
    private String successFlag;//jsonObj

    /**
     * 这俩玩意是特例，需要前端做下键值对形式提交，后台当成json去解析填充
     */
    @PushProperty(value = "请求参数", desc = "自定义的请求参数，JSON提交形式支持直接将JSON路径作为键填写，例如data.userInfo[0].user")
    private String params;//jsonArray

    @PushProperty(value = "请求头配置", desc = "自定义的请求头")
    private String headers;//jsonArray
}
