package com.hong.wechatpay.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeChatRefundParam {

    /**
     * 微信支付订单号，微信支付订单号和商家订单号二选一
     */
    private String transactionId;

    /**
     * 商家订单号，对应 out_trade_no，
     */
    private String orderId;

    /**
     * 商户退款单号，对应out_refund_no
     */
    private String refundOrderId;

    /**
     * 退款原因，选填
     */
    private String reason;

    /**
     * 回调地址
     */
    private WxNotifyType notify;

    /**
     * 退款金额
     */
    private BigDecimal refundMoney;

    /**
     * 原订单金额，必填
     */
    private BigDecimal totalMoney;
}

