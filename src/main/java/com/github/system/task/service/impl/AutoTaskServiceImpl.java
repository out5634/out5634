package com.github.system.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.system.auth.util.SessionUtils;
import com.github.system.task.dao.AutoTaskDao;
import com.github.system.task.dto.AutoTaskDto;
import com.github.system.task.entity.AutoIndex;
import com.github.system.task.entity.AutoTask;
import com.github.system.task.init.TaskInit;
import com.github.system.task.service.AutoIndexService;
import com.github.system.task.service.AutoTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutoTaskServiceImpl extends ServiceImpl<AutoTaskDao, AutoTask> implements AutoTaskService {

    @Resource
    private AutoIndexService autoIndexService;

    @Override
    public Page taskPage(Page<AutoTask> page, String indexId) throws Exception {
        // 校验indexId是否存在并且启用
        AutoIndex autoIndex = autoIndexService.getOne(new LambdaQueryWrapper<AutoIndex>()
                .eq(AutoIndex::getId, indexId)
                .eq(!SessionUtils.isAdmin(), AutoIndex::getEnable, 1));
        if (autoIndex == null) {
            throw new Exception("未找到该自动任务类型");
        }
        LambdaQueryWrapper<AutoTask> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AutoTask::getId, AutoTask::getCode, AutoTask::getEnable, AutoTask::getName, AutoTask::getLastEndStatus, AutoTask::getLastEndTime, AutoTask::getUserInfos)
                .eq(AutoTask::getIndexId, indexId);
        Page resPage = page(page, lambdaQueryWrapper);
        List list = turnAutoTaskEntity(resPage.getRecords());
        resPage.setRecords(list);
        return resPage;
    }

    @Override
    public List<AutoTaskDto> turnAutoTaskEntity(List<AutoTask> autoTaskList) {
        List<AutoTaskDto> list = new ArrayList<>();
        for (Object record : autoTaskList) {
            Class<?> userInfoClass = TaskInit.userInfosClassesMap.get((String) BeanUtil.getFieldValue(record,"code"));
            AutoTaskDto dto = BeanUtil.toBean(record, AutoTaskDto.class);
            Object bean = JSONUtil.toBean((String) BeanUtil.getFieldValue(record,"userInfos"), userInfoClass);
            dto.setUserInfo(bean);
            list.add(dto);
        }
        return list;
    }

}
