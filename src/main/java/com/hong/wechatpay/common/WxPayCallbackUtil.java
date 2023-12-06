package com.hong.wechatpay.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.hong.wechatpay.common.WechatPayValidatorForRequest;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.DefaultException;
import com.hong.wechatpay.entity.WxchatCallbackRefundData;
import com.hong.wechatpay.entity.WxchatCallbackSuccessData;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
public class WxPayCallbackUtil{


    /**
     * 微信支付创建订单回调方法
     * @param verifier 证书
     * @param wxPayConfig 微信配置
     * @param businessCallback 回调方法，用于处理业务逻辑
     * @return json格式的string数据，直接返回给微信
     */
    public static String wxPaySuccessCallback(HttpServletRequest request, HttpServletResponse response, Verifier verifier, WxPayConfig wxPayConfig, Consumer<WxchatCallbackSuccessData> businessCallback) {
        Gson gson = new Gson();

        // 1.处理通知参数
        final String body = HttpUtils.readData(request);
        HashMap<String, Object> bodyMap = gson.fromJson(body, HashMap.class);

        // 2.签名验证
        WechatPayValidatorForRequest wechatForRequest = new WechatPayValidatorForRequest(verifier, body, (String) bodyMap.get("id"));
        try {
            if (!wechatForRequest.validate(request)) {
                // 通知验签失败
                response.setStatus(500);
                final HashMap<String, Object> map = new HashMap<>();
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 3.获取明文数据
        String plainText = decryptFromResource(bodyMap,wxPayConfig);
        HashMap<String,Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        log.info("plainTextMap:{}",plainTextMap);
        // 4.封装微信返回的数据
        WxchatCallbackSuccessData callbackData = new WxchatCallbackSuccessData();
        callbackData.setSuccessTime(String.valueOf(plainTextMap.get("success_time")));
        callbackData.setOrderId(String.valueOf(plainTextMap.get("out_trade_no")));
        callbackData.setTransactionId(String.valueOf(plainTextMap.get("transaction_id")));
        callbackData.setTradestate(String.valueOf(plainTextMap.get("trade_state")));
        callbackData.setTradetype(String.valueOf(plainTextMap.get("trade_type")));
        String amount = String.valueOf(plainTextMap.get("amount"));
        HashMap<String,Object> amountMap = gson.fromJson(amount, HashMap.class);
        String total = String.valueOf(amountMap.get("total"));
        callbackData.setTotalMoney(new BigDecimal(total).movePointLeft(2));
        log.info("callbackData:{}",callbackData);

        if ("SUCCESS".equals(callbackData.getTradestate())) {
            // 执行业务逻辑
            businessCallback.accept(callbackData);
        }

        // 5.成功应答
        response.setStatus(200);
        final HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", "SUCCESS");
        resultMap.put("message", "成功");
        return gson.toJson(resultMap);
    }

    /**
     * 对称解密
     */
    @SneakyThrows
    private static String decryptFromResource(HashMap<String, Object> bodyMap, WxPayConfig wxPayConfig) {
        // 通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        // 数据密文
        String ciphertext = resourceMap.get("ciphertext");
        // 随机串
        String nonce = resourceMap.get("nonce");
        // 附加数据
        String associateData = resourceMap.get("associated_data");
        AesUtil aesUtil = new AesUtil(wxPayConfig.getKey().getBytes(StandardCharsets.UTF_8));
        try {
            return aesUtil.decryptToString(associateData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new DefaultException("解密失败");
        }
    }

    /**
     * 微信支付申请退款回调方法
     *
     * @param verifier       证书
     * @param wxPayConfig    微信配置
     * @param refundCallback 回调方法，用于处理业务逻辑，包含退款成功处理于退款失败处理
     * @return json格式的string数据，直接返回给微信
     */
    public static String wxPayRefundCallback(HttpServletRequest request, HttpServletResponse response, Verifier verifier, WxPayConfig wxPayConfig, WechatRefundCallback refundCallback) {
        Gson gson = new Gson();

        // 1.处理通知参数
        final String body = HttpUtils.readData(request);
        HashMap<String, Object> bodyMap = gson.fromJson(body, HashMap.class);

        // 2.签名验证
        WechatPayValidatorForRequest wechatForRequest = new WechatPayValidatorForRequest(verifier, body, (String) bodyMap.get("id"));
        try {
            if (!wechatForRequest.validate(request)) {
                // 通知验签失败
                response.setStatus(500);
                final HashMap<String, Object> map = new HashMap<>();
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 3.获取明文数据
        String plainText = decryptFromResource(bodyMap, wxPayConfig);
        HashMap<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        log.info("退款plainTextMap:{}", plainTextMap);
        // 4.封装微信返回的数据
        WxchatCallbackRefundData refundData = getRefundCallbackData(plainTextMap);


        if ("SUCCESS".equals(refundData.getStatus())) {
            // 执行业务逻辑
            refundCallback.success(refundData);
        } else {
            // 特殊情况退款失败业务处理，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往商户平台-交易中心，手动处理此笔退款
            refundCallback.fail(refundData);
        }

        // 5.成功应答
        response.setStatus(200);
        final HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", "SUCCESS");
        resultMap.put("message", "成功");
        return gson.toJson(resultMap);
    }
    private static WxchatCallbackRefundData getRefundCallbackData(HashMap<String, Object> plainTextMap) {
        Gson gson = new Gson();
        WxchatCallbackRefundData refundData = new WxchatCallbackRefundData();
        String successTime = String.valueOf(plainTextMap.get("success_time"));
        if (StringUtils.isNoneBlank(successTime)) {
            refundData.setSuccessTime(successTime);
        }
        refundData.setOrderId(String.valueOf(plainTextMap.get("out_trade_no")));
        refundData.setRefundId(String.valueOf(plainTextMap.get("out_refund_no")));
        refundData.setTransactionId(String.valueOf(plainTextMap.get("transaction_id")));
        refundData.setTransactionRefundId(String.valueOf(plainTextMap.get("refund_id")));
        refundData.setChannel(String.valueOf(plainTextMap.get("channel")));
        final String status = String.valueOf(plainTextMap.get("refund_status"));
        refundData.setStatus(status);
        String amount = String.valueOf(plainTextMap.get("amount"));
        HashMap<String, Object> amountMap = gson.fromJson(amount, HashMap.class);
        String refundMoney = String.valueOf(amountMap.get("refund"));
        refundData.setRefundMoney(new BigDecimal(refundMoney).movePointLeft(2));
        log.info("refundData:{}", refundData);
        return refundData;
    }

}
