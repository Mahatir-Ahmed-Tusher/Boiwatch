package com.tusher.boiwatch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.tusher.boiwatch.R;

public class LoginBottomSheetFragment extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_login_bottom_sheet, container, false);

        MaterialButton btnSubmit = view.findViewById(R.id.btn_login_submit);
        MaterialButton btnCancel = view.findViewById(R.id.btn_login_cancel);

        btnSubmit.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Demo Login Successful! Welcome to BoiWatch.", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}
