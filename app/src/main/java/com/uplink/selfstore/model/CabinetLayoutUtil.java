package com.uplink.selfstore.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class CabinetLayoutUtil {

    public static List<List<String>> getRowsByDs(int[] rowColLayout) {

        List<List<String>> rows = new ArrayList<>();



        int rowLength = rowColLayout.length;

        int num = 1;
        for (int i = rowLength - 1; i > 0; i--) {

            List<String> cols = new ArrayList<>();

            int colLength = rowColLayout[i];

            for (int j = colLength - 1; j >= 0; j--) {

                String slotId = (i - 1) + "-" + j + "-" + num;
                cols.add(slotId);
                num++;
            }

            rows.add(cols);

        }

        return rows;

    }
}
