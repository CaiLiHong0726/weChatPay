package com.hong.wechatpay.common;

import com.google.gson.Gson;
import com.hong.wechatpay.config.WxPayConfig;
import com.hong.wechatpay.entity.DefaultException;
import com.hong.wechatpay.entity.WechatTransferBatchesParam;
import com.hong.wechatpay.entity.WxApiType;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WxPayTransferBatchesUtils {

    /**
     * 发起商家转账，支持批量转账
     *
     * @param wxPayConfig 微信配置信息
     * @param param 转账请求参数
     * @param wxPayClient 微信请求客户端（）
     * @return 微信支付二维码地址
     */
    @SneakyThrows
    public static String transferBatches(WxPayConfig wxPayConfig, WechatTransferBatchesParam param, CloseableHttpClient wxPayClient) {
        // 1.获取请求参数的Map格式
        Map<String, Object> paramsMap = getParams(wxPayConfig, param);

        // 2.获取请求对象，WxApiType.TRANSFER_BATCHES="/v3/transfer/batches"
        HttpPost httpPost = getHttpPost(wxPayConfig,WxApiType.TRANSFER_BATCHES, paramsMap);

        // 3.完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultException("商家转账请求失败");
        }

        // 4.解析response对象
        HashMap<String, String> resultMap = resolverResponse(response);
        if (resultMap != null) {
            // batch_id微信批次单号，微信商家转账系统返回的唯一标识
            return resultMap.get("batch_id");
        }
        return null;
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
                if (StringUtils.isNoneBlank(bodyAsString)) {
                    log.error("商户转账请求失败，提示信息:{}", bodyAsString);
                    // 4.请求码显示失败，则尝试获取提示信息
                    HashMap<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
                    throw new DefaultException(resultMap.get("message"));
                }
                log.error("商户转账请求失败，未查询到原因，提示信息:{}", response);
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

    /**
     * 封装转账请求参数
     *
     * @param wxPayConfig 微信的配置文件
     * @param param 批量转账请求数据
     * @return 封装后的map对象
     */
    @SneakyThrows
    private static Map<String, Object> getParams(WxPayConfig wxPayConfig, WechatTransferBatchesParam param) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("out_batch_no", param.getBatchId());
        paramsMap.put("batch_name", param.getTitle());
        paramsMap.put("batch_remark", param.getRemark());
        paramsMap.put("total_amount", param.getTotalMoney().multiply(new BigDecimal("100")).intValue());
        paramsMap.put("total_num", param.getTransferDetailList().size());

        // 存储转账明细，一次最多三千笔
        if (param.getTransferDetailList().size() > 3000) {
            throw new DefaultException("发起批量转账一次最多三千笔");
        }
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (WechatTransferBatchesParam.transferDetail detail : param.getTransferDetailList()) {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("out_detail_no",detail.getBatchId());
            detailMap.put("transfer_amount",detail.getTotalDetailMoney().multiply(new BigDecimal("100")).intValue());
            detailMap.put("transfer_remark",detail.getDetailRemark());
            detailMap.put("openid",detail.getOpenid());
            detailList.add(detailMap);
        }
        paramsMap.put("transfer_detail_list", detailList);
        return paramsMap;
    }
}

