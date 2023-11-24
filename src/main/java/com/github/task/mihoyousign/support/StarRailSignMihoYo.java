package com.github.task.mihoyousign.support;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.system.base.dto.r.R;
import com.github.system.task.dto.TaskLog;
import com.github.system.task.dto.TaskResult;
import com.github.task.mihoyousign.constant.MihoyouSignConstant;
import com.github.task.mihoyousign.support.model.SignUserInfo;
import com.github.task.mihoyousign.support.pojo.Award;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lijinglin
 */
public class StarRailSignMihoYo extends MiHoYoAbstractSign{

    private static final Logger log = LogManager.getLogger(StarRailSignMihoYo.class.getName());

    public StarRailSignMihoYo(String cookie) {
        super(cookie);
        setClientType(MihoyouSignConstant.SIGN_CLIENT_TYPE);
        setAppVersion(MihoyouSignConstant.APP_VERSION);
        setSalt(MihoyouSignConstant.SIGN_SALT);
    }


    @Override
    public TaskResult doSign(TaskLog log) {
        R<List<SignUserInfo>> userInfo = getUserInfo(log);
        if (userInfo.ok()) {
            signUserInfoList = userInfo.getData();
            for (SignUserInfo signUserInfo : signUserInfoList) {

                String doSign = doSign(signUserInfo.getUid(), signUserInfo.getRegion());
                String hubSign = hubSign(signUserInfo.getUid(), signUserInfo.getRegion());
                uidMap.put("msg", uidMap.get("msg") + "\n" + doSign + "\n" + hubSign);
            }
        } else {
            return TaskResult.doError("获取用户信息失败：" + userInfo.getMsg());
        }
    }

    /**
     * 签到（重载doSign,主要用来本地测试）
     * @param uid 游戏角色uid
     * @param region 游戏服务器标识符
     * @return 签到信息
     */
    public String doSign(String uid, String region) {

        Map<String, Object> data = new HashMap<>();

        data.put("act_id", MiHoYoConfig.XQTD_SIGN_ACT_ID);
        data.put("region", region);
        data.put("uid", uid);

        JSONObject signResult = HttpUtils.doPost(MiHoYoConfig.XQTD_SIGN_URL, getHeaders(""), data);

        if (signResult.getInt("retcode") == 0) {
            log.info("星穹铁道签到福利成功：{}", signResult.get("message"));
            return "星穹铁道签到福利成功：" + signResult.get("message");
        } else {
            log.info("星穹铁道签到福利签到失败：{}", signResult.get("message"));
            return "星穹铁道签到福利签到失败：" + signResult.get("message");
        }
    }

    /**
     * 获取今天奖励详情
     *
     * @param day
     * @return
     */
    public Award getAwardInfo(int day) {

        JSONObject awardResult = HttpUtils.doGet(MiHoYoConfig.XQTD_AWARD_URL, getHeaders(""));
        JSONArray jsonArray = awardResult.getJSONObject("data").getJSONArray("awards");

        List<Award> awards = JSONUtil.toList(jsonArray, Award.class);
        return awards.get(day - 1);
    }

    /**
     * 社区签到并查询当天奖励
     * @param uid 游戏角色uid
     * @param region 游戏服务器标识符
     * @return 签到信息
     */
    public String hubSign(String uid, String region) {
        Map<String, Object> data = new HashMap<>();

        data.put("act_id", MiHoYoConfig.XQTD_SIGN_ACT_ID);
        data.put("region", region);
        data.put("uid", uid);

        JSONObject signInfoResult = HttpUtils.doGet(MiHoYoConfig.XQTD_INFO_URL, getHeaders(""), data);
        if (signInfoResult == null || signInfoResult.getJSONObject("data") == null){
            return null;
        }

        LocalDateTime time = LocalDateTime.now();
        Boolean isSign = signInfoResult.getJSONObject("data").getBool("is_sign");
        Integer totalSignDay = signInfoResult.getJSONObject("data").getInt("total_sign_day");
        int day = isSign ? totalSignDay : totalSignDay + 1;

        Award award = getAwardInfo(day);

        StringBuilder msg = new StringBuilder();
        msg.append(time.getMonth().getValue()).append("月已签到").append(totalSignDay).append("\n");
        msg.append(signInfoResult.getJSONObject("data").get("today")).append("签到获取").append(award.getCnt()).append(award.getName());

        log.info("{}月已签到{}天", time.getMonth().getValue(), totalSignDay);
        log.info("{}签到获取{}{}", signInfoResult.getJSONObject("data").get("today"), award.getCnt(), award.getName());

        return msg.toString();
    }
}