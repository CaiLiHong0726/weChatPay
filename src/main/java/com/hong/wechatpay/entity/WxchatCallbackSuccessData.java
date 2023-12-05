package com.hong.wechatpay.entity;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @author cv大魔王
 * @version 1.0
 * @description 微信支付成功回调返回的数据
 * @date 2022/8/4
 */
@Data
@Slf4j
public class WxchatCallbackSuccessData {

    /**
     * 商户订单号
     */
    private String orderId;

    /**
     * 微信支付系统生成的订单号
     */
    private String transactionId;

    /**
     * 交易状态
     * SUCCESS：支付成功
     * REFUND：转入退款
     * NOTPAY：未支付
     * CLOSED：已关闭
     * REVOKED：已撤销（付款码支付）
     * USERPAYING：用户支付中（付款码支付）
     * PAYERROR：支付失败(其他原因，如银行返回失败)
     */
    private String tradestate;

    /**
     * 支付完成时间
     */
    private Date successTime;

    /**
     * 交易类型
     * JSAPI：公众号支付
     * NATIVE：扫码支付
     * APP：APP支付
     * MICROPAY：付款码支付
     * MWEB：H5支付
     * FACEPAY：刷脸支付
     */
    private String 	tradetype;

    /**
     * 订单总金额
     */
    private BigDecimal totalMoney;


    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(String successTime) {
        // Hutool工具包的方法，自动识别一些常用格式的日期字符串
        this.successTime = DateUtil.parse(successTime);
    }
}

