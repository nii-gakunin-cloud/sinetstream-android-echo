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

package jp.ad.sinet.stream.android.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import jp.ad.sinet.stream.android.sample.net.SinetStreamReaderString;
import jp.ad.sinet.stream.android.sample.net.SinetStreamWriterString;
import jp.ad.sinet.stream.android.sample.ui.main.ErrorDialogFragment;
import jp.ad.sinet.stream.android.sample.ui.main.InProgressDialogFragment;
import jp.ad.sinet.stream.android.sample.ui.main.RecvFragment;
import jp.ad.sinet.stream.android.sample.ui.main.SendFragment;
import jp.ad.sinet.stream.android.sample.util.DialogUtil;

// import jp.ad.sinet.stream.android.sample.net.SinetStreamReaderBytes;
// import jp.ad.sinet.stream.android.sample.net.SinetStreamWriterBytes;

public class MainActivity extends AppCompatActivity implements
        InProgressDialogFragment.ProgressDialogListener,
        ErrorDialogFragment.ErrorDialogListener,
        SinetStreamWriterString.SinetStreamWriterStringListener,
        SinetStreamReaderString.SinetStreamReaderStringListener,
        // SinetStreamWriterBytes.SinetStreamWriterBytesListener,
        // SinetStreamReaderBytes.SinetStreamReaderBytesListener,
        SendFragment.SendFragmentListener,
        RecvFragment.RecvFragmentListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private final String SEND_FRAGMENT = SendFragment.class.getSimpleName();
    private final String RECV_FRAGMENT = RecvFragment.class.getSimpleName();

    private InProgressDialogFragment mInProgressDialogFragment = null;
    private boolean mIsWriterAvailable = false;
    private boolean mIsReaderAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.activity_name_main);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true); // onOptionsItemSelected
        }

        if (savedInstanceState == null) {
            /*
             * In case this activity was started with special instructions from an
             * Intent, pass the Intent's extras to the fragment as arguments
             */
            Bundle bundle = null;
            Intent intent = getIntent();
            if (intent != null) {
                bundle = intent.getExtras();
            }

            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            SendFragment sendFragment = new SendFragment();
            sendFragment.setArguments(bundle);
            transaction.replace(R.id.senderContainer, sendFragment, SEND_FRAGMENT);

            RecvFragment recvFragment = new RecvFragment();
            recvFragment.setArguments(bundle);
            transaction.replace(R.id.receiverContainer, recvFragment, RECV_FRAGMENT);

            transaction.commit();
        }
    }

    @Nullable
    private SendFragment lookupSendFragment() {
        SendFragment fragment = (SendFragment) getSupportFragmentManager().
                findFragmentByTag(SEND_FRAGMENT);
        if (fragment == null) {
            Log.e(TAG, SEND_FRAGMENT + " not found?");
        }
        return fragment;
    }

    @Nullable
    private RecvFragment lookupRecvFragment() {
        RecvFragment fragment = (RecvFragment) getSupportFragmentManager().
                findFragmentByTag(RECV_FRAGMENT);
        if (fragment == null) {
            Log.e(TAG, RECV_FRAGMENT + " not found?");
        }
        return fragment;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        SendFragment sendFragment = lookupSendFragment();
        if (sendFragment != null) {
            sendFragment.startWriter();
        }
        RecvFragment recvFragment = lookupRecvFragment();
        if (recvFragment != null) {
            recvFragment.startReader();
        }

        mInProgressDialogFragment =
                new InProgressDialogFragment(
                        getString(R.string.dialog_title_connecting));
        mInProgressDialogFragment.show(
                getSupportFragmentManager(), null);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        SendFragment sendFragment = lookupSendFragment();
        if (sendFragment != null) {
            sendFragment.stopWriter();
        }
        RecvFragment recvFragment = lookupRecvFragment();
        if (recvFragment != null) {
            recvFragment.stopReader();
        }
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Back to the parent activity, as specified in the app manifest */
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReaderStatusChanged(boolean available) {
        /* Implementation of SinetStreamReaderListener.onStatusChanged */
        Log.d(TAG, "onReaderStatusChanged: available=" + available);
        mIsReaderAvailable = available;

        if (available) {
            if (mIsWriterAvailable) {
                if (mInProgressDialogFragment != null) {
                    mInProgressDialogFragment.dismiss();
                    mInProgressDialogFragment = null;
                }
            }

            RecvFragment recvFragment = lookupRecvFragment();
            if (recvFragment != null) {
                recvFragment.clearDisplay();
            }
        }
    }

//    /**
//     * Called when a message has received on any subscribed topic.
//     *
//     * @param topic     a topic where received message came from
//     * @param timestamp message publish date and time, measured in UnixTime format.
//     * @param data      received message contents
//     */
//    @Override
//    public void onMessageReceived(@NonNull String topic,
//                                  long timestamp,
//                                  @NonNull byte[] data) {
//        /* Implementation of SinetStreamReaderListener.onMessageReceived */
//        RecvFragment recvFragment = lookupRecvFragment();
//        if (recvFragment != null) {
//            recvFragment.onDataReceived(topic, timestamp, data);
//        }
//    }

    /**
     * Called when a message has received on any subscribed topic.
     *
     * @param topic     a topic where received message came from
     * @param timestamp message publish date and time, measured in UnixTime format.
     * @param data      received message contents
     */
    @Override
    public void onMessageReceived(@NonNull String topic,
                                  long timestamp,
                                  @NonNull String data) {
        /* Implementation of SinetStreamReaderListener.onMessageReceived */
        RecvFragment recvFragment = lookupRecvFragment();
        if (recvFragment != null) {
            recvFragment.onDataReceived(topic, timestamp, data);
        }
    }

    @Override
    public void onWriterStatusChanged(boolean available) {
        /* Implementation of SinetStreamWriterListener.onStatusChanged */
        Log.d(TAG, "onWriterStatusChanged: available=" + available);
        mIsWriterAvailable = available;

        if (available) {
            if (mIsReaderAvailable) {
                if (mInProgressDialogFragment != null) {
                    mInProgressDialogFragment.dismiss();
                    mInProgressDialogFragment = null;
                }
            }

            SendFragment sendFragment = lookupSendFragment();
            if (sendFragment != null) {
                sendFragment.toggleSendButton(available);
            }
        }
    }

//    /**
//     * Called when {@code publish()} has completed successfully.
//     *
//     * @param message  Original message for publish, not {@code null}.
//     * @param userData User specified opaque object, passed by {@code publish()}.
//     */
//    @Override
//    public void onPublished(@NonNull byte[] message, @Nullable Object userData) {
//        /* Implementation of SinetStreamWriterListener.onPublished */
//        SendFragment sendFragment = lookupSendFragment();
//        if (sendFragment != null) {
//            sendFragment.onPublished(message, userData);
//        }
//    }

    /**
     * Called when {@code publish()} has completed successfully.
     *
     * @param message  Original message for publish, not {@code null}.
     * @param userData User specified opaque object, passed by {@code publish()}.
     */
    @Override
    public void onPublished(@NonNull String message, @Nullable Object userData) {
        SendFragment sendFragment = lookupSendFragment();
        if (sendFragment != null) {
            sendFragment.onPublished(message, userData);
        }
    }

    @Override
    public void onError(@NonNull String message) {
        /* Implementation of SinetStreamWriterListener.onError */
        /* Implementation of SinetStreamReaderListener.onError */
        /* Implementation of SendFragmentListener.onError */
        /* Implementation of RecvFragmentListener.onError */
        Log.e(TAG, "onError: " + message);

        DialogUtil.showErrorDialog(
                this, message, null, true);

        /*
         * If user pressed OK button on the error dialog window,
         * ErrorDialogFragment.onErrorDialogDismissed() will be called.
         */
    }

    @Override
    public void onErrorDialogDismissed(
            @Nullable Parcelable parcelable, boolean isFatal) {
        /* Implementation of ErrorDialogFragment.onErrorDialogDismissed */
        if (mInProgressDialogFragment != null) {
            mInProgressDialogFragment.dismiss();
            mInProgressDialogFragment = null;
        }

        if (isFatal) {
            Log.i(TAG, "Going to finish myself...");
            finish();
        }
    }

    @Override
    public void onCanceled() {
        /* Implementation of InProgressDialogFragment.ProgressDialogListener */
        Log.d(TAG, "onCanceled");
    }
}
