package com.hong.wechatpay.entity;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Slf4j
public class WxchatCallbackRefundData {

    /**
     * 商户订单号
     */
    private String orderId;


    /**
     * 商户退款单号,out_refund_no
     */
    private String refundId;

    /**
     * 微信支付系统生成的订单号
     */
    private String transactionId;

    /**
     * 微信支付系统生成的退款订单号
     */
    private String transactionRefundId;

    /**
     * 退款渠道
     * ORIGINAL：原路退款
     * BALANCE：退回到余额
     * OTHER_BALANCE：原账户异常退到其他余额账户
     * OTHER_BANKCARD：原银行卡异常退到其他银行卡
     */
    private String 	channel;

    /**
     * 退款成功时间
     * 当前退款成功时才有此返回值
     */
    private Date successTime;

    /**
     * 退款状态
     * 退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往商户平台-交易中心，手动处理此笔退款。
     * SUCCESS：退款成功
     * CLOSED：退款关闭
     * PROCESSING：退款处理中
     * ABNORMAL：退款异常
     */
    private String 	status;

    /**
     * 退款金额
     */
    private BigDecimal refundMoney;


    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(String successTime) {
        // Hutool工具包的方法，自动识别一些常用格式的日期字符串
        this.successTime = DateUtil.parse(successTime);
    }
}

