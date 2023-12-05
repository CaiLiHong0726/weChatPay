package com.hong.wechatpay.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hong.wechatpay.common.WxPayCallbackUtil;
import com.hong.wechatpay.common.WxPayCommon;
import com.hong.wechatpay.common.WxPaySearchOrderUtil;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.WeChatBasePayData;
import com.hong.wechatpay.entity.WxNotifyType;
import com.hong.wechatpay.entity.WxchatCallbackSuccessData;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private Verifier verifier;

   // @ApiOperation("1.支付测试接口")
    @GetMapping("/pay/wxNativeTest")
    public Map<String, String> getNativeCheckNum() {
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

    @GetMapping("/pay/JsApiTest")
    public Map<String, String> getJsApiCheckNum() {
        WeChatBasePayData payData = new WeChatBasePayData();
        payData.setTitle("支付测试商品");
        payData.setOrderId(IdWorker.getIdStr()); //测试时随机生成一个
        payData.setPrice(new BigDecimal("0.01"));
        payData.setNotify(WxNotifyType.REFUND_NOTIFY);

        // String path = WxPayCommon.wxNativePay(wxPayConfig, payData, wxPayClient);
        // 每个账户所关联的openId都是不一样的，你也别拿我的试，需要看你们项目的环境
        String path = WxPayCommon.wxJsApiPay(wxPayConfig, payData, "oS3tA4zspa1DYMK5zBYkZv9XMIqw", wxPayClient);
        Map<String, String> map = new HashMap();
        map.put("path", path);
        return map;
    }


    @GetMapping("/pay/AppTest")
    public Map<String, String> getAppCheckNum() {
        WeChatBasePayData payData = new WeChatBasePayData();
        payData.setTitle("支付测试商品");
        payData.setOrderId(IdWorker.getIdStr()); //测试时随机生成一个，代表订单号
        payData.setPrice(new BigDecimal("0.01"));
        payData.setNotify(WxNotifyType.REFUND_NOTIFY);

        String path = WxPayCommon.wxAppPay(wxPayConfig, payData, wxPayClient);
        Map<String, String> map = new HashMap();
        map.put("path", path);
        return map;
    }


    @GetMapping("/pay/wxH5Test")
    public Map<String, String> getH5CheckNum() {
        WeChatBasePayData payData = new WeChatBasePayData();
        payData.setTitle("支付测试商品");
        payData.setOrderId(IdWorker.getIdStr()); //测试时随机生成一个，代表订单号
        payData.setPrice(new BigDecimal("0.01"));
        payData.setNotify(WxNotifyType.REFUND_NOTIFY);

        String path = WxPayCommon.wxH5Pay(wxPayConfig, payData, wxPayClient);
        Map<String, String> map = new HashMap();
        map.put("path", path);
        return map;
    }

   //"微信支付回调接口"
    @PostMapping("/wx/callback")
    public String courseNative(HttpServletRequest request, HttpServletResponse response) {
        return WxPayCallbackUtil.wxPaySuccessCallback(request, response, verifier, wxPayConfig, callbackData -> {
            // TODO 处理你的业务逻辑，下面说一下一般业务逻辑处理方法
            log.info("微信支付返回的信息：{}", callbackData);
            // 1.根据订单id获取订单信息

            // 2.判断金额是否相符，如果不相符则调用退款接口，并取消该订单，通知客户支付金额不符

            // 3.查询订单状态是否是未支付，如果是未支付则改为已支付，填充其他逻辑，

            // 4.如果是其他状态综合你的业务逻辑来处理

            // 5.如果是虚拟物品，则对应充值，等等其他逻辑
        });
    }


   // @ApiOperation("根据微信订单号查询订单")
    @PostMapping("/search/order/transaction/{transactionId}")
    public WxchatCallbackSuccessData searchByTransactionId(@PathVariable String transactionId) {
        return  WxPaySearchOrderUtil.searchByTransactionId(wxPayConfig,transactionId,wxPayClient);
    }

   // @ApiOperation("根据商户订单号查询")
    @PostMapping("/search/order/{orderId}")
    public WxchatCallbackSuccessData searchByOrderId(@PathVariable String orderId) {
        return  WxPaySearchOrderUtil.searchByOrderId(wxPayConfig,orderId,wxPayClient);
    }


}
