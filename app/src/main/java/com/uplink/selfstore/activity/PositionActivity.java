package com.uplink.selfstore.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uplink.selfstore.R;


public class PositionActivity extends Activity {

    public class CellIDInfo {

        public int cellId;
        public String mobileCountryCode;
        public String mobileNetworkCode;
        public int locationAreaCode;
        public String radioType;
        public CellIDInfo(){}
    }

    private CdmaCellLocation location = null;
    private Button btnGetInfo = null;
    private static final int TYPE = 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);
        btnGetInfo = (Button)findViewById(R.id.btnGet);
        btnGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tv = (EditText)findViewById(R.id.editText1);
                // TODO Auto-generated method stub
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                int type = tm.getNetworkType();
                //���й����ƶ���2G��EGDE����ͨ��2GΪGPRS�����ŵ�2GΪCDMA�����ŵ�3GΪEVDO
                //String OperatorName = tm.getNetworkOperatorName();
                Location loc = null;
                ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();
                //�й�����ΪCTC
                //NETWORK_TYPE_EVDO_A���й�����3G��getNetworkType
                //NETWORK_TYPE_CDMA����2G��CDMA
                if (type == TelephonyManager.NETWORK_TYPE_EVDO_A || type == TelephonyManager.NETWORK_TYPE_CDMA || type ==TelephonyManager.NETWORK_TYPE_1xRTT)
                {
                    location = (CdmaCellLocation) tm.getCellLocation();
                    int cellIDs = location.getBaseStationId();
                    int networkID = location.getNetworkId();
                    StringBuilder nsb = new StringBuilder();
                    nsb.append(location.getSystemId());
                    CellIDInfo info = new CellIDInfo();
                    info.cellId = cellIDs;
                    info.locationAreaCode = networkID; //ok
                    info.mobileNetworkCode = nsb.toString();
                    info.mobileCountryCode = tm.getNetworkOperator().substring(0, 3);
                    info.radioType = "cdma";
                    CellID.add(info);
                }
                //�ƶ�2G�� + CMCC + 2
                //type = NETWORK_TYPE_EDGE
                else if(type == TelephonyManager.NETWORK_TYPE_EDGE)
                {
                    GsmCellLocation location = (GsmCellLocation)tm.getCellLocation();
                    int cellIDs = location.getCid();
                    int lac = location.getLac();
                    CellIDInfo info = new CellIDInfo();
                    info.cellId = cellIDs;
                    info.locationAreaCode = lac;
                    info.mobileNetworkCode = tm.getNetworkOperator().substring(3, 5);
                    info.mobileCountryCode = tm.getNetworkOperator().substring(0, 3);
                    info.radioType = "gsm";
                    CellID.add(info);
                }
                //��ͨ��2G�������� China Unicom   1 NETWORK_TYPE_GPRS
                else if(type == TelephonyManager.NETWORK_TYPE_GPRS)
                {
                    GsmCellLocation location = (GsmCellLocation)tm.getCellLocation();
                    int cellIDs = location.getCid();
                    int lac = location.getLac();
                    CellIDInfo info = new CellIDInfo();
                    info.cellId = cellIDs;
                    info.locationAreaCode = lac;
                    //�������ԣ���ȡ��ͨ�����������б���ȥ�����������ִ��󣬴�������ΪJSON Parsing Error
                    //info.mobileNetworkCode = tm.getNetworkOperator().substring(3, 5);
                    //info.mobileCountryCode = tm.getNetworkOperator().substring(0, 3);
                    info.radioType = "gsm";
                    CellID.add(info);
                }
                else
                {
                    tv.setText("Current Not Support This Type.");
                }

                loc = callGear(CellID);

                if(loc != null)
                {
                    try {

                        StringBuilder sb = new StringBuilder();
                        String pos = getLocation(loc);
                        sb.append("CellID:");
                        sb.append(CellID.get(0).cellId);
                        sb.append("+\n");

                        sb.append("home_mobile_country_code:");
                        sb.append(CellID.get(0).mobileCountryCode);
                        sb.append("++\n");

                        sb.append("mobileNetworkCode:");
                        sb.append(CellID.get(0).mobileNetworkCode);
                        sb.append("++\n");

                        sb.append("locationAreaCode:");
                        sb.append(CellID.get(0).locationAreaCode);
                        sb.append("++\n");
                        sb.append(pos);

                        tv.setText(sb.toString());

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    private Location callGear(ArrayList<CellIDInfo> cellID) {
//        if (cellID == null) return null;
//        DefaultHttpClient client = new DefaultHttpClient();
//        HttpPost post = new HttpPost(
//                "http://www.google.com/loc/json");
//        JSONObject holder = new JSONObject();
//        try {
//            holder.put("version", "1.1.0");
//            holder.put("host", "maps.google.com");
//            holder.put("home_mobile_country_code", cellID.get(0).mobileCountryCode);
//            holder.put("home_mobile_network_code", cellID.get(0).mobileNetworkCode);
//            holder.put("radio_type", cellID.get(0).radioType);
//            holder.put("request_address", true);
//            if ("460".equals(cellID.get(0).mobileCountryCode))
//                holder.put("address_language", "zh_CN");
//            else
//                holder.put("address_language", "en_US");
//            JSONObject data,current_data;
//            JSONArray array = new JSONArray();
//            current_data = new JSONObject();
//            current_data.put("cell_id", cellID.get(0).cellId);
//            current_data.put("location_area_code", cellID.get(0).locationAreaCode);
//            current_data.put("mobile_country_code", cellID.get(0).mobileCountryCode);
//            current_data.put("mobile_network_code", cellID.get(0).mobileNetworkCode);
//            current_data.put("age", 0);
//            array.put(current_data);
//            if (cellID.size() > 2) {
//                for (int i = 1; i < cellID.size(); i++) {
//                    data = new JSONObject();
//                    data.put("cell_id", cellID.get(i).cellId);
//                    data.put("location_area_code", cellID.get(i).locationAreaCode);
//                    data.put("mobile_country_code", cellID.get(i).mobileCountryCode);
//                    data.put("mobile_network_code", cellID.get(i).mobileNetworkCode);
//                    data.put("age", 0);
//                    array.put(data);
//                }
//            }
//            holder.put("cell_towers", array);
//            StringEntity se = new StringEntity(holder.toString());
//            Log.e("Location send", holder.toString());
//            post.setEntity(se);
//            HttpResponse resp = client.execute(post);
//            HttpEntity entity = resp.getEntity();
//
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(entity.getContent()));
//            StringBuffer sb = new StringBuffer();
//            String result = br.readLine();
//            while (result != null) {
//                Log.e("Locaiton receive", result);
//                sb.append(result);
//                result = br.readLine();
//            }
//            if(sb.length() <= 1)
//                return null;
//            data = new JSONObject(sb.toString());
//            data = (JSONObject) data.get("location");
//
//            Location loc = new Location(LocationManager.NETWORK_PROVIDER);
//            loc.setLatitude((Double) data.get("latitude"));
//            loc.setLongitude((Double) data.get("longitude"));
//            loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
//            loc.setTime(GetUTCTime());
//            return loc;
//        } catch (JSONException e) {
//            return null;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    /**
     * ��ȡ����λ��
     *
     * @throws Exception
     */
    private String getLocation(Location itude) throws Exception {
        String resultString = "";

        /** �������get������ֱ�ӽ������ӵ�URL�� */
        String urlString = String.format("http://maps.google.cn/maps/geo?key=abcdefg&q=%s,%s", itude.getLatitude(), itude.getLongitude());
        Log.i("URL", urlString);

//        /** �½�HttpClient */
//        HttpClient client = new DefaultHttpClient();
//        /** ����GET���� */
//        HttpGet get = new HttpGet(urlString);
//        try {
//            /** ����GET���󲢻�÷������� */
//            HttpResponse response = client.execute(get);
//            HttpEntity entity = response.getEntity();
//            BufferedReader buffReader = new BufferedReader(new InputStreamReader(entity.getContent()));
//            StringBuffer strBuff = new StringBuffer();
//            String result = null;
//            while ((result = buffReader.readLine()) != null) {
//                strBuff.append(result);
//            }
//            resultString = strBuff.toString();
//
//            /** ����JSON���ݣ���������ַ */
//            if (resultString != null && resultString.length() > 0) {
//                JSONObject jsonobject = new JSONObject(resultString);
//                JSONArray jsonArray = new JSONArray(jsonobject.get("Placemark").toString());
//                resultString = "";
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    resultString = jsonArray.getJSONObject(i).getString("address");
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("��ȡ����λ�ó��ִ���:" + e.getMessage());
//        } finally {
//            get.abort();
//            client = null;
//        }

        return resultString;
    }

    public long GetUTCTime() {
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }
}
