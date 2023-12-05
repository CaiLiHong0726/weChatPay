package com.hong.wechatpay.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信回调地址，根据自己的项目来，不要直接照搬
 */
@Getter
@AllArgsConstructor
public enum WxNotifyType {

    /**
     * 课程支付通知
     */
    COURSE_NATIVE_NOTIFY("/api/v1/order/wx/course/native"),


    /**
     * 文章支付通知
     */
    ARTICLE_NATIVE_NOTIFY("/api/v1/order/article/wx/callback/article"),


    /**
     * 论文支付通知
     */
    PAPER_NATIVE_NOTIFY("/api/v1/order/paper/wx/callback/paper"),


    /**
     * 退款结果通知
     */
    REFUND_NOTIFY("/api/wx-pay/refunds/notify");

    /**
     * 类型
     */
    private final String type;

}
