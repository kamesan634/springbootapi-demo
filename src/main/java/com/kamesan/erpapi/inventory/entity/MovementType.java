package com.kamesan.erpapi.inventory.entity;

/**
 * 庫存異動類型枚舉
 *
 * <p>定義系統中所有可能的庫存異動類型，包括：</p>
 * <ul>
 *   <li>進貨入庫 (PURCHASE_IN)</li>
 *   <li>銷售出庫 (SALES_OUT)</li>
 *   <li>退貨入庫 (RETURN_IN) - 客戶退貨</li>
 *   <li>退貨出庫 (RETURN_OUT) - 退給供應商</li>
 *   <li>調撥入庫 (TRANSFER_IN)</li>
 *   <li>調撥出庫 (TRANSFER_OUT)</li>
 *   <li>盤盈入庫 (ADJUST_IN)</li>
 *   <li>盤虧出庫 (ADJUST_OUT)</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
public enum MovementType {

    /**
     * 進貨入庫
     * <p>從供應商採購商品入庫</p>
     */
    PURCHASE_IN("進貨入庫", true),

    /**
     * 銷售出庫
     * <p>商品銷售給客戶出庫</p>
     */
    SALES_OUT("銷售出庫", false),

    /**
     * 退貨入庫
     * <p>客戶退回商品入庫</p>
     */
    RETURN_IN("退貨入庫", true),

    /**
     * 退貨出庫
     * <p>將商品退還給供應商</p>
     */
    RETURN_OUT("退貨出庫", false),

    /**
     * 調撥入庫
     * <p>從其他倉庫調入商品</p>
     */
    TRANSFER_IN("調撥入庫", true),

    /**
     * 調撥出庫
     * <p>將商品調撥至其他倉庫</p>
     */
    TRANSFER_OUT("調撥出庫", false),

    /**
     * 盤盈入庫
     * <p>盤點發現實際數量多於帳面數量</p>
     */
    ADJUST_IN("盤盈入庫", true),

    /**
     * 盤虧出庫
     * <p>盤點發現實際數量少於帳面數量</p>
     */
    ADJUST_OUT("盤虧出庫", false),

    /**
     * 盤點入庫
     * <p>盤點時發現庫存增加</p>
     */
    COUNT_IN("盤點入庫", true),

    /**
     * 盤點出庫
     * <p>盤點時發現庫存減少</p>
     */
    COUNT_OUT("盤點出庫", false);

    /**
     * 異動類型的中文描述
     */
    private final String description;

    /**
     * 是否為入庫操作
     * <p>true: 增加庫存; false: 減少庫存</p>
     */
    private final boolean inbound;

    /**
     * 建構子
     *
     * @param description 中文描述
     * @param inbound     是否為入庫操作
     */
    MovementType(String description, boolean inbound) {
        this.description = description;
        this.inbound = inbound;
    }

    /**
     * 取得異動類型的中文描述
     *
     * @return 中文描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判斷是否為入庫操作
     *
     * @return true 表示入庫（增加庫存），false 表示出庫（減少庫存）
     */
    public boolean isInbound() {
        return inbound;
    }

    /**
     * 取得庫存變化量的正負符號
     *
     * <p>入庫操作返回 +1，出庫操作返回 -1</p>
     *
     * @return 庫存變化符號（+1 或 -1）
     */
    public int getQuantitySign() {
        return inbound ? 1 : -1;
    }
}
