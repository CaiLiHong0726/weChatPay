package com.hong.wechatpay.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;

@Data
public class WeChatBasePayData {

    /**
     * 商品描述
     */
    private String title;

    /**
     * 商家订单号，对应 out_trade_no
     */
    private String orderId;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 回调地址
     */
    private WxNotifyType notify;

}
