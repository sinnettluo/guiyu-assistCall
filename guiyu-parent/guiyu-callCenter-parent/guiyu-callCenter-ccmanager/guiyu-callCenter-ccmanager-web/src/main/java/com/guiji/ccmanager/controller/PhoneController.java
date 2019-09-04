package com.guiji.ccmanager.controller;

import com.guiji.ccmanager.constant.Constant;
import com.guiji.ccmanager.service.PhoneService;
import com.guiji.ccmanager.vo.PhoneAreaVo;
import com.guiji.component.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

/**
 * author:liyang
 * Date:2019/6/28 19:19
 * Description:
 */
@RestController
@Slf4j
public class PhoneController {

    @Autowired
    PhoneService phoneService;

    @GetMapping("/getPhoneBySeqId")
    public Result.ReturnData getPhoneBySeqId(@RequestParam("seqId") String seqId) {

        log.info("getPhoneBySeqId收到参数 ,seqId[{}]",seqId);

        String callId;
        Integer orgId;
        BigInteger bigInteCallId = null;
        try {
            String[] arr = seqId.split(Constant.UUID_SEPARATE);
            callId = arr[0];
            orgId = Integer.valueOf(arr[1]);
            bigInteCallId = new BigInteger(callId);
        }catch (Exception e){
            return Result.error("0305016");
        }

        PhoneAreaVo phoneAreaVo = phoneService.getPhoneBySeqId(bigInteCallId,orgId);

        log.info("getPhoneBySeqId返回结果 ,phoneAreaVo[{}]",phoneAreaVo);

        return Result.ok(phoneAreaVo);

    }

}
