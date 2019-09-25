package com.procurement.procurement_server.model.user_level;

import java.util.List;

/**
 * added a list to get all the paidPurchases done by Finance employee
 * removed payid, ordid because this class cannot handle them.
 *
 * paid purchases by this user
 *
 ***/

public class FinanceEmployee extends Staff {

    private List<String> paidPurchaseList;


    public List<String> getPaidPurchaseList() {
        return paidPurchaseList;
    }

    public void setPaidPurchaseList(List<String> paidPurchaseList) {
        this.paidPurchaseList = paidPurchaseList;
    }
}
