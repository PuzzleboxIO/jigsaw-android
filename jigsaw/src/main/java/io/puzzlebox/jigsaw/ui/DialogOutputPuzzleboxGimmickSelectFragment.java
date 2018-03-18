package io.puzzlebox.jigsaw.ui;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;

public class DialogOutputPuzzleboxGimmickSelectFragment extends DialogFragment {

    private final String TAG = DialogOutputPuzzleboxGimmickSelectFragment.class.getSimpleName();

    LinearLayout dynamicLayout;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void onSelectGimmickRefresh();
        void onSelectGimmickItem(Integer deviceNumber);
    }

    public DialogOutputPuzzleboxGimmickSelectFragment() {
        // Required empty public constructor
    }

    public static DialogOutputPuzzleboxGimmickSelectFragment newInstance(String param1, String param2) {
        DialogOutputPuzzleboxGimmickSelectFragment fragment = new DialogOutputPuzzleboxGimmickSelectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_output_puzzlebox_gimmick_select, null);

        Button buttonRefresh = v.findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonChoose onClick(): " + -1);
                if (mListener != null) {
                    mListener.onSelectGimmickItem(-1);
                }
            }

        });


        dynamicLayout = v.findViewById(R.id.dynamicLayoutSelectGimmick);

//        for (int i = 0; i < DeviceEmotivInsightSingleton.getInstance().detectedDevices.size(); i++) {
//
//            String deviceName = DeviceEmotivInsightSingleton.getInstance().detectedDevices.get(i);
//
//            Log.d(TAG, "Adding Insight: " + i + ": \"" + deviceName + "\" ");
//
//            if (savedInstanceState == null) {
//
//                DialogOutputPuzzleboxGimmickSelectItemFragment item = io.puzzlebox.jigsaw.ui.DialogOutputPuzzleboxGimmickSelectItemFragment.newInstance(
//                        R.id.dynamicLayoutSelectGimmick,
//                        i,
//                        deviceName);
//
//                try {
//                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//                    ft.add(R.id.dynamicLayoutSelectGimmick, item, "device" + i);
//                    ft.commit();
//                } catch (Exception e) {
//                    Log.e(TAG, "ft Exception:" + e.toString());
//                }
//
//            }
//
//        }

        displayDevicesFound(savedInstanceState);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void displayDevicesFound(Bundle savedInstanceState) {

        Log.e(TAG, "displayDevicesFound: " + DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.size());

        ArrayList<String> deviceNames = new ArrayList<>();

        for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
            if (sr.getDevice().getName() != null) {
//				deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "]");
                deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "] [Rssi: " + sr.getRssi() + "]");
            }
        }

        if (deviceNames.isEmpty()) {
            deviceNames.add(getResources().getString(R.string.scan_default_list));
        }

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_list_item_1, deviceNames);
//        setListAdapter(adapter);




        for (int i = 0; i < deviceNames.size(); i++) {

            String deviceName = deviceNames.get(i);

            Log.d(TAG, "Adding Gimmick: " + i + ": \"" + deviceName + "\" ");

            if (savedInstanceState == null) {

                DialogOutputPuzzleboxGimmickSelectItemFragment item = io.puzzlebox.jigsaw.ui.DialogOutputPuzzleboxGimmickSelectItemFragment.newInstance(
                        R.id.dynamicLayoutSelectGimmick,
                        i,
                        deviceName);

                try {
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    ft.add(R.id.dynamicLayoutSelectGimmick, item, "device" + i);
                    ft.commit();
                } catch (Exception e) {
                    Log.e(TAG, "ft Exception:" + e.toString());
                }

            }

        }





    }

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//
//        String deviceSelection = (String) getListAdapter().getItem(position);
//        Toast.makeText(getActivity(), deviceSelection, Toast.LENGTH_SHORT).show();
//
//        Log.i(TAG, "Device selected: " + deviceSelection);
//
//        textViewScanTitle.setText(getResources().getString(R.string.scan_connecting));
//
//        broadcastCommandBluetooth("connect", deviceSelection);
//
//    }



}
