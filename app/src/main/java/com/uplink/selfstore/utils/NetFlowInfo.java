package com.uplink.selfstore.utils;

import android.graphics.drawable.Drawable;

public class NetFlowInfo {



        private String packname;
        private Drawable icon;
        private String appname;
        private long upKb;
        private long downKb;

        public String getPackname() {
            return packname;
        }

        public void setPackname(String packname) {
            this.packname = packname;
        }


        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public String getAppname() {
            return appname;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public long getUpKb() {
            return upKb;
        }

        public void setUpKb(long upKb) {
            this.upKb = upKb;
        }

        public long getDownKb() {
            return downKb;
        }

        public void setDownKb(long downKb) {
            this.downKb = downKb;
        }

}
