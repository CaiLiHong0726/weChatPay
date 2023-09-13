package com.hong.wechatpay.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hong.wechatpay.common.WxPayCommon;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.WeChatBasePayData;
import com.hong.wechatpay.entity.WxNotifyType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/pay/test")
public class PayTestController {
    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private CloseableHttpClient wxPayClient;

   // @ApiOperation("1.支付测试接口")
    @GetMapping("/pay/test")
    public Map<String, String> getCheckNum() {
        WeChatBasePayData payData = new WeChatBasePayData();
        payData.setTitle("支付测试商品");
        payData.setOrderId(IdWorker.getIdStr()); //测试时随机生成一个，代表订单号
        payData.setPrice(new BigDecimal("0.01"));
        payData.setNotify(WxNotifyType.REFUND_NOTIFY);

        String path = WxPayCommon.wxNativePay(wxPayConfig, payData, wxPayClient);
        Map<String, String> map = new HashMap();
        map.put("path", path);
        return map;
    }

}
