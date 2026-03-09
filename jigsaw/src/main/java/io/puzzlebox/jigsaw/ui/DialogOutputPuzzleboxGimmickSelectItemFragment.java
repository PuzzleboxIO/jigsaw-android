package io.puzzlebox.jigsaw.ui;

import android.content.Intent;
import java.util.Locale;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.puzzlebox.jigsaw.R;

public class DialogOutputPuzzleboxGimmickSelectItemFragment extends Fragment {

    private static final String ARG_PARENT_ID = "paramParentId";
    private static final String ARG_INSIGHT_ID = "paramInsightId";
    private static final String ARG_NAME = "paramName";

    private int mParamInsightId;
    private String mParamName;

    private int mId;

    public DialogOutputPuzzleboxGimmickSelectItemFragment() {
        // Required empty public constructor
    }

    public static DialogOutputPuzzleboxGimmickSelectItemFragment newInstance(Integer paramParentId,
                                                                             Integer paramInsightId,
                                                                             String paramName) {
        DialogOutputPuzzleboxGimmickSelectItemFragment fragment = new DialogOutputPuzzleboxGimmickSelectItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARENT_ID, paramParentId);
        args.putInt(ARG_INSIGHT_ID, paramInsightId);
        args.putString(ARG_NAME, paramName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamInsightId = getArguments().getInt(ARG_INSIGHT_ID);
            mParamName = getArguments().getString(ARG_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_output_puzzlebox_gimmick_select_item, container, false);

        mId = mParamInsightId; // we use a separate variable here because parameters may overwrite each other

        TextView textViewSelectGimmickNumber = v.findViewById(R.id.textViewSelectGimmickNumber);
        textViewSelectGimmickNumber.setText(String.format(Locale.US, "#%d: ", mParamInsightId));

        TextView textViewSelectGimmickName = v.findViewById(R.id.textViewSelectGimmickName);
        textViewSelectGimmickName.setText(mParamName);

        LinearLayout layoutSelectGimmick = v.findViewById(R.id.layoutSelectGimmick);
        layoutSelectGimmick.setOnClickListener(view -> {
            Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.puzzlebox.gimmick.status");
            intent.setPackage(requireActivity().getPackageName());
            intent.putExtra("name", "select");
            intent.putExtra("value", mParamName);
            requireActivity().sendBroadcast(intent);
        });

        Button buttonChoose = v.findViewById(R.id.buttonChoose);
        buttonChoose.setOnClickListener(view -> {
            Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.puzzlebox.gimmick.status");
            intent.setPackage(requireActivity().getPackageName());
            intent.putExtra("name", "select");
            intent.putExtra("value", Integer.toString(mId));
            requireActivity().sendBroadcast(intent);
        });
        return v;
    }

}
