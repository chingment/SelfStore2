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
    private boolean imIsUse;
    private String imPartner;
    private String imUserName;
    private String imPassword;

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

    public boolean isImIsUse() {
        return imIsUse;
    }

    public void setImIsUse(boolean imIsUse) {
        this.imIsUse = imIsUse;
    }

    public String getImPartner() {
        return imPartner;
    }

    public void setImPartner(String imPartner) {
        this.imPartner = imPartner;
    }

    public String getImUserName() {
        return imUserName;
    }

    public void setImUserName(String imUserName) {
        this.imUserName = imUserName;
    }

    public String getImPassword() {
        return imPassword;
    }

    public void setImPassword(String imPassword) {
        this.imPassword = imPassword;
    }
}
