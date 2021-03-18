/*
 * Copyright (c) 2020 National Institute of Informatics
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package jp.ad.sinet.stream.android.sample.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import jp.ad.sinet.stream.android.sample.R;
import jp.ad.sinet.stream.android.sample.constants.BundleKeys;

public class ErrorDialogFragment extends DialogFragment {
    private String mErrorMessage;

    private final Activity mActivity;
    private ErrorDialogListener mListener = null;
    private Parcelable mCallbackParcelable = null;
    private boolean mIsFatal = false;

    public ErrorDialogFragment(Activity activity) {
        // Required empty public constructor
        mActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = mActivity;
        if (context instanceof ErrorDialogListener) {
            mListener = (ErrorDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ErrorDialogListener");
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            mErrorMessage = bundle.getString(BundleKeys.BUNDLE_KEY_ERROR_MESSAGE);
            mCallbackParcelable = bundle.getParcelable(BundleKeys.BUNDLE_KEY_PARCELABLE);
            mIsFatal = bundle.getBoolean(BundleKeys.BUNDLE_KEY_ERROR_FATAL);
        } else {
            mErrorMessage = "N/A";
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // return super.onCreateDialog(savedInstanceState);

        // Use the Builder class for convenient dialog construction
        // Use the default AppTheme for the AlertDialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setTitle(getString(R.string.dialog_title_error));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(mErrorMessage)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         * Let calling Activity do the rest of tasks.
                         */
                        if (mListener != null) {
                            mListener.onErrorDialogDismissed(mCallbackParcelable, mIsFatal);
                        }
                    }
                });

        // Create an AlertDialog object and return it
        Dialog alertDialog = builder.create();

        // Don't allow user to cancel by touching outside of the dialog.
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    public interface ErrorDialogListener {
        void onErrorDialogDismissed(@Nullable Parcelable parcelable, boolean isFatal);

        //void onNegativeButtonPressed();
    }
}
