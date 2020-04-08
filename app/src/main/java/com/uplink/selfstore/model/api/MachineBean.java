package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/1.
 */

public class MachineBean implements Serializable {

    private String id;
    public  String name;
    private String merchName;
    private String storeName;
    private String logoImgUrl;
    private String currency;
    private String currencySymbol;
    private String csrQrCode;
    private String csrPhoneNumber;
    private String csrHelpTip;
    private String ostCtrl;
    private String mstCtrl;

    private HashMap<String, CabinetBean> cabinets;

    public HashMap<String, CabinetBean> getCabinets() {
        return cabinets;
    }

    public void setCabinets(HashMap<String, CabinetBean> cabinets) {
        this.cabinets = cabinets;
    }

    private boolean isHiddenKind;
    private int kindRowCellSize;
    private List<TerminalPayOptionBean> payOptions;
    private boolean isOpenChkCamera;
    private int maxBuyNumber;
    private boolean exIsHas;

    public String getCsrHelpTip() {
        return csrHelpTip;
    }

    public void setCsrHelpTip(String csrHelpTip) {
        this.csrHelpTip = csrHelpTip;
    }

    public boolean isExIsHas() {
        return exIsHas;
    }

    public void setExIsHas(boolean exIsHas) {
        this.exIsHas = exIsHas;
    }

    public int getMaxBuyNumber() {
        return maxBuyNumber;
    }

    public void setMaxBuyNumber(int maxBuyNumber) {
        this.maxBuyNumber = maxBuyNumber;
    }

    public boolean isOpenChkCamera() {
        return isOpenChkCamera;
    }

    public void setOpenChkCamera(boolean openChkCamera) {
        isOpenChkCamera = openChkCamera;
    }

    public String getCsrQrCode() {
        return csrQrCode;
    }

    public void setCsrQrCode(String csrQrCode) {
        this.csrQrCode = csrQrCode;
    }

    public String getLogoImgUrl() {
        return logoImgUrl;
    }

    public void setLogoImgUrl(String logoImgUrl) {
        this.logoImgUrl = logoImgUrl;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMerchName() {
        return merchName;
    }

    public void setMerchName(String merchName) {
        this.merchName = merchName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public boolean isHiddenKind() {
        return isHiddenKind;
    }

    public void setHiddenKind(boolean hiddenKind) {
        isHiddenKind = hiddenKind;
    }

    public int getKindRowCellSize() {
        return kindRowCellSize;
    }

    public void setKindRowCellSize(int kindRowCellSize) {
        this.kindRowCellSize = kindRowCellSize;
    }

    public List<TerminalPayOptionBean> getPayOptions() {
        return payOptions;
    }

    public void setPayOptions(List<TerminalPayOptionBean> payOptions) {
        this.payOptions = payOptions;
    }

    public String getCsrPhoneNumber() {
        return csrPhoneNumber;
    }

    public void setCsrPhoneNumber(String csrPhoneNumber) {
        this.csrPhoneNumber = csrPhoneNumber;
    }

    public String getOstCtrl() {
        return ostCtrl;
    }

    public void setOstCtrl(String ostCtrl) {
        this.ostCtrl = ostCtrl;
    }

    public String getMstCtrl() {
        return mstCtrl;
    }

    public void setMstCtrl(String mstCtrl) {
        this.mstCtrl = mstCtrl;
    }
}
