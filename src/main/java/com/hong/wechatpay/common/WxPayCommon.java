package com.hong.wechatpay.common;

import com.google.gson.Gson;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.DefaultException;
import com.hong.wechatpay.entity.WeChatBasePayData;
import com.hong.wechatpay.entity.WxApiType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WxPayCommon {

    /**
     * 封装基础通用请求数据
     * @param wxPayConfig 微信的配置文件
     * @param basePayData 微信支付基础请求数据
     * @return 封装后的map对象
     */
    private static Map<String, Object> getBasePayParams(WxPayConfig wxPayConfig, WeChatBasePayData basePayData) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        // 如果商品名称过长则截取
        String title = basePayData.getTitle().length() > 62 ? basePayData.getTitle().substring(0, 62) : basePayData.getTitle();
        paramsMap.put("description",title);
        paramsMap.put("out_trade_no", basePayData.getOrderId());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(basePayData.getNotify().getType()));
        Map<String, Integer> amountMap = new HashMap<>();
        amountMap.put("total", basePayData.getPrice().multiply(new BigDecimal("100")).intValue());
        paramsMap.put("amount", amountMap);
        return paramsMap;
    }

    /**
     * 获取请求对象（Post请求）
     * @param wxPayConfig 微信配置类
     * @param apiType 接口请求地址
     * @param paramsMap 请求参数
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

    /**
     * 解析响应数据
     * @param response 发送请求成功后，返回的数据
     * @return 微信返回的参数
     */
    @SneakyThrows
    private static HashMap<String, String> resolverResponse(CloseableHttpResponse response) {
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
                if (StringUtils.isNotBlank(bodyAsString)) {
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
     * 创建微信支付订单-Native方式
     *
     * @param wxPayConfig 微信配置信息
     * @param basePayData 基础请求信息，商品标题、商家订单id、订单价格
     * @param wxPayClient 微信请求客户端（）
     * @return 微信支付二维码地址
     */
    @SneakyThrows
    public static String wxNativePay(WxPayConfig wxPayConfig, WeChatBasePayData basePayData, CloseableHttpClient wxPayClient) {
        // 1.获取请求参数的Map格式
        Map<String, Object> paramsMap = getBasePayParams(wxPayConfig, basePayData);

        // 2.获取请求对象
        HttpPost httpPost = getHttpPost(wxPayConfig, WxApiType.NATIVE_PAY, paramsMap);

        // 3.完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultException("微信支付请求失败");
        }

        // 4.解析response对象
        HashMap<String, String> resultMap = resolverResponse(response);
        if (resultMap != null) {
            // native请求返回的是二维码链接，前端将链接转换成二维码即可
            return resultMap.get("code_url");
        }
        return null;
    }




}
