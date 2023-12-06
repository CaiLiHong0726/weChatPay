package com.hong.wechatpay.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cv大魔王
 * @version 1.0
 * @description 微信支付商家转账到零钱请求参数
 * @date 2022/8/4
 */
@Data
public class WechatTransferBatchesParam {

    /**
     * 商户系统内部的商家批次单号，对应 out_batch_no，
     */
    private String batchId;

    /**
     * 该笔批量转账的名称，对应 batch_name，示例值：2019年1月深圳分部报销单
     */
    private String title;

    /**
     * 批次备注
     */
    private String remark;

    /**
     * 转账总金额
     */
    private BigDecimal totalMoney;

    /**
     * 转账明细列表
     */
    private List<transferDetail> transferDetailList;

    @Data
    public static class transferDetail {

        /**
         * 商户系统内部的商家批次单号，对应 out_detail_no，
         */
        private String batchId;

        /**
         * 转账金额
         */
        private BigDecimal totalDetailMoney;

        /**
         * 转账备注
         */
        private String detailRemark;

        /**
         * openid是微信用户在公众号（小程序）appid下的唯一用户标识
         */
        private String openid;
    }
}

