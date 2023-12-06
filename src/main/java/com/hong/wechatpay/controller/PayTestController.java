package com.hong.wechatpay.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hong.wechatpay.common.*;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.*;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

   // @ApiOperation("退款申请测试")
    @GetMapping("/refund/{orderId}")
    public String refund(@PathVariable String orderId) {
        WeChatRefundParam param = new WeChatRefundParam();
        param.setOrderId(orderId);
        String refundOrderId = IdWorker.getIdStr();
        log.info("refundOrderId:{}",refundOrderId);
        param.setRefundOrderId(refundOrderId);
        param.setReason("商品售罄");
        param.setNotify(WxNotifyType.REFUND_NOTIFY);
        param.setRefundMoney(new BigDecimal("0.01"));
        param.setTotalMoney(new BigDecimal("0.01"));
        return  WxPayRefundUtil.refundPay(wxPayConfig,param,wxPayClient);
    }

  //  @ApiOperation("微信退款回调接口")
    @PostMapping("/wx/refund/callback")
    public String refundWechatCallback(HttpServletRequest request, HttpServletResponse response) {
        return WxPayCallbackUtil.wxPayRefundCallback(request, response, verifier, wxPayConfig, new WechatRefundCallback() {
            @Override
            public void success(WxchatCallbackRefundData refundData) {
                // TODO 退款成功的业务逻辑，例如更改订单状态为退款成功等
            }

            @Override
            public void fail(WxchatCallbackRefundData refundData) {
                // TODO 特殊情况下退款失败业务处理，例如银行卡冻结需要人工退款，此时可以邮件或短信提醒管理员，并携带退款单号等关键信息
            }
        });
    }


    //@ApiOperation("转账测试")
    @GetMapping("/transfer/batches")
    public String transferBatches() {
        WechatTransferBatchesParam param = new WechatTransferBatchesParam();
        String batchId = IdWorker.getIdStr();
        log.info("转账Id:{}", batchId);
        param.setBatchId(batchId);
        param.setTitle("转账测试");
        param.setRemark("转账测试");
        param.setTotalMoney(new BigDecimal("0.02"));


        // 批量转账，可以同时转账给多人，但是不能超过3000，我这里只转账给一个人，只用来测试
        List<WechatTransferBatchesParam.transferDetail> detailList = new ArrayList<>();
        WechatTransferBatchesParam.transferDetail detail = new WechatTransferBatchesParam.transferDetail();
        detail.setBatchId(batchId);
        detail.setTotalDetailMoney(new BigDecimal("0.02"));
        detail.setDetailRemark("转账测试详情");
        detail.setOpenid("DF7E6901802E9F75A6FB45137C6D3685");
        detailList.add(detail);


        param.setTransferDetailList(detailList);
        return WxPayTransferBatchesUtils.transferBatches(wxPayConfig,param,wxPayClient);
    }



}
