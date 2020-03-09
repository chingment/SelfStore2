package com.uplink.selfstore.model.common;

/**
 * Created by chingment on 2017/12/18.
 */



public class NineGridItemBean {

    private String title;
    private Object icon;
    private int type;
    private String action;
    private Object tag;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Object getIcon() {
        return icon;
    }

    public void setIcon(Object icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public  NineGridItemBean()
    {

    }

    public  NineGridItemBean(String title,int type,String action, Object icon)
    {
        this.title=title;
        this.type=type;
        this.icon=icon;
        this.action=action;
    }

    public  NineGridItemBean(String title,int type,String action, Object icon,Object tag)
    {
        this.title=title;
        this.type=type;
        this.icon=icon;
        this.action=action;
        this.tag=tag;
    }
}
