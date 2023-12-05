package com.hong.wechatpay.common;

import com.google.gson.Gson;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.DefaultException;
import com.hong.wechatpay.entity.WxchatCallbackSuccessData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashMap;

@Slf4j
public class WxPaySearchOrderUtil {


    /**
     * 根据微信支付系统生成的订单号查询订单详情
     * @param wxPayConfig 微信支付配置信息
     * @param transactionId 微信支付系统生成的订单号
     * @param wxPayClient 微信支付客户端请求对象
     * @return 微信订单对象
     */
    @SneakyThrows
    public static WxchatCallbackSuccessData searchByTransactionId(WxPayConfig wxPayConfig, String transactionId, CloseableHttpClient wxPayClient) {
        // 1.请求路径和对象
        String url = wxPayConfig.getDomain().concat("/v3/pay/transactions/id/").concat(transactionId).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 2.完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultException("微信支付请求失败");
        }

        // 3.解析返回的数据
        WxchatCallbackSuccessData callbackData = resolverResponse(response);
        log.info("callbackData:{}",callbackData);
        return callbackData;
    }

    /**
     * 根据微信支付系统生成的订单号查询订单详情
     * @param wxPayConfig 微信支付配置信息
     * @param orderId 我们自己的订单id
     * @param wxPayClient 微信支付客户端请求对象
     * @return 微信订单对象
     */
    @SneakyThrows
    public static WxchatCallbackSuccessData searchByOrderId(WxPayConfig wxPayConfig, String orderId, CloseableHttpClient wxPayClient) {
        // 1.请求路径和对象
        String url = wxPayConfig.getDomain().concat("/v3/pay/transactions/out-trade-no/").concat(orderId).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 2.完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultException("微信支付请求失败");
        }

        // 3.解析返回的数据
        WxchatCallbackSuccessData callbackData = resolverResponse(response);
        log.info("callbackData:{}",callbackData);
        return callbackData;
    }


    /**
     * 解析响应数据
     * @param response 发送请求成功后，返回的数据
     * @return 微信返回的参数
     */
    @SneakyThrows
    private static WxchatCallbackSuccessData resolverResponse(CloseableHttpResponse response) {
        try {
            // 1.获取请求码
            int statusCode = response.getStatusLine().getStatusCode();
            // 2.获取返回值 String 格式
            final String bodyAsString = EntityUtils.toString(response.getEntity());

            Gson gson = new Gson();
            if (statusCode == 200) {
                // 3.如果请求成功则解析成Map对象返回
                HashMap<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);

                // 4.封装成我们需要的数据
                WxchatCallbackSuccessData callbackData = new WxchatCallbackSuccessData();
                callbackData.setSuccessTime(String.valueOf(resultMap.get("success_time")));
                callbackData.setOrderId(String.valueOf(resultMap.get("out_trade_no")));
                callbackData.setTransactionId(String.valueOf(resultMap.get("transaction_id")));
                callbackData.setTradestate(String.valueOf(resultMap.get("trade_state")));
                callbackData.setTradetype(String.valueOf(resultMap.get("trade_type")));
                String amount = String.valueOf(resultMap.get("amount"));
                HashMap<String,Object> amountMap = gson.fromJson(amount, HashMap.class);
                String total = String.valueOf(amountMap.get("total"));
                callbackData.setTotalMoney(new BigDecimal(total).movePointLeft(2));
                return callbackData;
            } else {
                if (StringUtils.isNoneBlank(bodyAsString)) {
                    log.error("微信支付请求失败，提示信息:{}", bodyAsString);
                    // 4.请求码显示失败，则尝试获取提示信息
                    HashMap<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
                    throw new DefaultException(resultMap.get("message"));
                }
                log.error("微信支付请求失败，未查询到原因，提示信息:{}", response);
                // 其他异常，微信也没有返回数据，这就需要具体排查了
                throw new IOException("request failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefaultException(e.getMessage());
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
