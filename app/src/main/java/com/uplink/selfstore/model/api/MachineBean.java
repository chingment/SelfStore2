package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/1.
 */

public class MachineBean implements Serializable {

    private String machineId;
    public  String name;
    private String merchName;
    private String storeName;
    private String shopName;
    private String shopAddress;
    private String logoImgUrl;
    private String currency;
    private String currencySymbol;
    private String csrQrCode;
    private String csrPhoneNumber;
    private String csrHelpTip;
    private HashMap<String, CabinetBean> cabinets;
    private boolean isHiddenKind;
    private int kindRowCellSize;
    private List<TerminalPayOptionBean> payOptions;
    private boolean cameraByChkIsUse;
    private boolean cameraByRlIsUse;
    private boolean cameraByJgIsUse;
    private int maxBuyNumber;
    private boolean exIsHas;
    private String ostVern;
    private String mstVern;
    private FingerVeinnerBean fingerVeinner;
    private ScannerBean scanner;
    private ImBean im;
    private MqttBean mqtt;
    private int picInSampleSize=8;

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


    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
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

    public HashMap<String, CabinetBean> getCabinets() {
        return cabinets;
    }

    public void setCabinets(HashMap<String, CabinetBean> cabinets) {
        this.cabinets = cabinets;
    }


    public String getOstVern() {
        return ostVern;
    }

    public void setOstVern(String ostVern) {
        this.ostVern = ostVern;
    }

    public String getMstVern() {
        return mstVern;
    }

    public void setMstVern(String mstVern) {
        this.mstVern = mstVern;
    }

    public FingerVeinnerBean getFingerVeinner() {
        return fingerVeinner;
    }

    public void setFingerVeinner(FingerVeinnerBean fingerVeinner) {
        this.fingerVeinner = fingerVeinner;
    }

    public ScannerBean getScanner() {
        return scanner;
    }

    public void setScanner(ScannerBean scanner) {
        this.scanner = scanner;
    }

    public boolean isCameraByChkIsUse() {
        return cameraByChkIsUse;
    }

    public void setCameraByChkIsUse(boolean cameraByChkIsUse) {
        this.cameraByChkIsUse = cameraByChkIsUse;
    }

    public boolean isCameraByRlIsUse() {
        return cameraByRlIsUse;
    }

    public void setCameraByRlIsUse(boolean cameraByRlIsUse) {
        this.cameraByRlIsUse = cameraByRlIsUse;
    }

    public boolean isCameraByJgIsUse() {
        return cameraByJgIsUse;
    }

    public void setCameraByJgIsUse(boolean cameraByJgIsUse) {
        this.cameraByJgIsUse = cameraByJgIsUse;
    }

    public ImBean getIm() {
        return im;
    }

    public void setIm(ImBean im) {
        this.im = im;
    }

    public MqttBean getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttBean mqtt) {
        this.mqtt = mqtt;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public int getPicInSampleSize() {
        return picInSampleSize;
    }

    public void setPicInSampleSize(int picInSampleSize) {
        this.picInSampleSize = picInSampleSize;
    }
}
