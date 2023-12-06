package com.hong.wechatpay.common;

import com.hong.wechatpay.entity.WxchatCallbackRefundData;

/**
 * 退款处理接口，为了防止项目开发人员，不手动判断退款失败的情况
 * 退款失败：退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往商户平台-交易中心，手动处理此笔退款
 */
public interface WechatRefundCallback {

    /**
     * 退款成功处理情况
     */
    void success(WxchatCallbackRefundData refundData);

    /**
     * 退款失败处理情况
     */
    void fail(WxchatCallbackRefundData refundData);
}

