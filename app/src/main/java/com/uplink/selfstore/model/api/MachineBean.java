package com.uplink.selfstore.model.api;

import java.io.Serializable;
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
    private int cabinetId_1;
    private String cabinetName_1;
    private int[] cabinetRowColLayout_1;
    private int[] cabinetPendantRows_1;
    private boolean isHiddenKind;
    private int kindRowCellSize;
    private List<TerminalPayOptionBean> payOptions;
    private boolean isOpenChkCamera;
    private int maxBuyNumber;

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

    public int getCabinetId_1() {
        return cabinetId_1;
    }

    public void setCabinetId_1(int cabinetId_1) {
        this.cabinetId_1 = cabinetId_1;
    }

    public String getCabinetName_1() {
        return cabinetName_1;
    }

    public void setCabinetName_1(String cabinetName_1) {
        this.cabinetName_1 = cabinetName_1;
    }

    public int[] getCabinetRowColLayout_1() {
        return cabinetRowColLayout_1;
    }

    public void setCabinetRowColLayout_1(int[] cabinetRowColLayout_1) {
        this.cabinetRowColLayout_1 = cabinetRowColLayout_1;
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

    public int[] getCabinetPendantRows_1() {
        return cabinetPendantRows_1;
    }

    public void setCabinetPendantRows_1(int[] cabinetPendantRows_1) {
        this.cabinetPendantRows_1 = cabinetPendantRows_1;
    }
}
