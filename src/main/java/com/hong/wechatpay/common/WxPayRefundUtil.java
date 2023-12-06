package com.hong.wechatpay.common;

import com.google.gson.Gson;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.DefaultException;
import com.hong.wechatpay.entity.WeChatRefundParam;
import com.hong.wechatpay.entity.WxApiType;
import com.hong.wechatpay.entity.WxchatCallbackSuccessData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WxPayRefundUtil {

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


    /**
     * 封装微信支付申请退款请求参数
     * @param param 微信支付申请退款请求参数
     * @return 封装后的map微信支付申请退款请求参数对象
     */
    @SneakyThrows
    private static Map<String, Object> getRefundParams(WxPayConfig wxPayConfig, WeChatRefundParam param) {
        Map<String, Object> paramsMap = new HashMap<>();
        if (StringUtils.isNoneBlank(param.getTransactionId())) {
            paramsMap.put("transaction_id", param.getTransactionId());
        } else if (StringUtils.isNoneBlank(param.getOrderId())) {
            paramsMap.put("out_trade_no", param.getOrderId());
        } else {
            throw new DefaultException("微信支付订单号和商户订单号必须填写一个");
        }
        paramsMap.put("out_refund_no", param.getRefundOrderId());
        if (StringUtils.isNoneBlank(param.getReason())) {
            paramsMap.put("reason", param.getReason());
        }
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(param.getNotify().getType()));
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("refund", param.getRefundMoney().multiply(new BigDecimal("100")).intValue());
        amountMap.put("total", param.getTotalMoney().multiply(new BigDecimal("100")).intValue());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);

        return paramsMap;
    }

    /**
     * 发起微信退款申请
     *
     * @param wxPayConfig 微信配置信息
     * @param param       微信支付申请退款请求参数
     * @param wxPayClient 微信请求客户端（）
     * @return 微信支付二维码地址
     */
    @SneakyThrows
    public static String refundPay(WxPayConfig wxPayConfig, WeChatRefundParam param, CloseableHttpClient wxPayClient) {
        // 1.获取请求参数的Map格式
        Map<String, Object> paramsMap = getRefundParams(wxPayConfig, param);

        // 2.获取请求对象
        HttpPost httpPost = getHttpPost(wxPayConfig, WxApiType.DOMESTIC_REFUNDS, paramsMap);

        // 3.完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultException("微信支付请求失败");
        }

        // 4.解析response对象
        HashMap<String, String> resultMap = refundPayResolverResponse(response);
        log.info("发起退款参数：{}", resultMap);
        if (resultMap != null) {
            // 返回微信支付退款单号
            return resultMap.get("refund_id");
        }
        return null;

    }

    /**
     * 解析响应数据
     *
     * @param response 发送请求成功后，返回的数据
     * @return 微信返回的参数
     */
    @SneakyThrows
    private static HashMap<String, String> refundPayResolverResponse(CloseableHttpResponse response) {
        try {
            // 1.获取请求码
            int statusCode = response.getStatusLine().getStatusCode();
            // 2.获取返回值 String 格式
            final String bodyAsString = EntityUtils.toString(response.getEntity());

            Gson gson = new Gson();
            if (statusCode == 200) {
                // 3.如果请求成功则解析成Map对象返回
                HashMap<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
                return resultMap;
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

    /**
     * 获取请求对象（Post请求）
     *
     * @param wxPayConfig 微信配置类
     * @param apiType     接口请求地址
     * @param paramsMap   请求参数
     * @return Post请求对象
     */
    private static HttpPost getHttpPost(WxPayConfig wxPayConfig, WxApiType apiType, Map<String, Object> paramsMap) {
        // 1.设置请求地址
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(apiType.getType()));

        // 2.设置请求数据
        Gson gson = new Gson();
        String jsonParams = gson.toJson(paramsMap);

        // 3.设置请求信息
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        return httpPost;
    }






}
