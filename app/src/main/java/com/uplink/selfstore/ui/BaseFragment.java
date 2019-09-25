package com.uplink.selfstore.ui;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.uplink.selfstore.utils.ToastUtil;

public class BaseFragment extends Fragment {

    public void showToast(String text) {
        ToastUtil.showMessage(getActivity(), text + "", Toast.LENGTH_SHORT);
    }

}
