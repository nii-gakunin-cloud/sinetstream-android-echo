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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.Date;

import jp.ad.sinet.stream.android.api.SinetStreamReaderString;
import jp.ad.sinet.stream.android.api.SinetStreamWriterString;
import jp.ad.sinet.stream.android.config.remote.ConfigServerSettings;
import jp.ad.sinet.stream.android.net.cert.KeyChainHandler;
import jp.ad.sinet.stream.android.sample.constants.ActivityCodes;
import jp.ad.sinet.stream.android.sample.constants.BundleKeys;
import jp.ad.sinet.stream.android.sample.ui.configserver.SharedPrefsAccessKey;
import jp.ad.sinet.stream.android.sample.ui.configserver.SharedPrefsConfigServer;
import jp.ad.sinet.stream.android.sample.ui.dialogs.ErrorDialogFragment;
import jp.ad.sinet.stream.android.sample.ui.main.EchoViewModel;
import jp.ad.sinet.stream.android.sample.ui.main.RecvFragment;
import jp.ad.sinet.stream.android.sample.ui.main.SendFragment;
import jp.ad.sinet.stream.android.sample.util.DialogUtil;

public class MainActivity extends AppCompatActivity implements
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

    private EchoViewModel mViewModel = null;
    private boolean mIsWriterAvailable = false;
    private boolean mIsReaderAvailable = false;

    private SharedPreferences mSharedPreferences;

    /* Parameters to be required for the remote configuration server access */
    private boolean mUseConfigServer = false;
    private boolean mIsProtocolDebug = false;
    private ConfigServerSettings mConfigServerSettings = null;
    private String mServerUrl = null;
    private String mAccount = null;
    private String mSecretKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        /*
         * Keep some attributes beyond Activity's lifecycle.
         */
        mViewModel = new ViewModelProvider(this).
                get(EchoViewModel.class);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.activity_name_main);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true); // onOptionsItemSelected
        }

        if (savedInstanceState != null) {
            /* Avoid creating the same fragment sets more than once. */
            Log.d(TAG, "onCreate: After RESTART");
        } else {
            /*
             * In case this activity was started with special instructions from an
             * Intent, pass the Intent's extras to the fragment as arguments
             */
            Bundle bundle = null;
            Intent intent = getIntent();
            if (intent != null) {
                bundle = intent.getExtras();

                mUseConfigServer = bundle.getBoolean(
                        BundleKeys.BUNDLE_KEY_USE_CONFIG_SERVER, false);
                mIsProtocolDebug = bundle.getBoolean(
                        BundleKeys.BUNDLE_KEY_PROTOCOL_DEBUG, false);

                if (mUseConfigServer) {
                    /*
                     * Let user pickup an AccessToken which must have downloaded
                     * on this device.
                     * Note that series of dialogs may appear during the remote
                     * configuration processes, if there are multiple choices in
                     * the SINETStream configuration set.
                     */
                    setupRemoteConfiguration();
                } else {
                    Log.d(TAG, "Use manually chosen SINETStream configurations");
                }
            }

            Bundle bundle2 = ((bundle != null) ? bundle : new Bundle());
            bundle2.putBoolean(BundleKeys.BUNDLE_KEY_PROTOCOL_DEBUG, mIsProtocolDebug);

            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            SendFragment sendFragment = new SendFragment();
            sendFragment.setArguments(bundle2);
            transaction.replace(R.id.senderContainer, sendFragment, SEND_FRAGMENT);

            RecvFragment recvFragment = new RecvFragment();
            recvFragment.setArguments(bundle2);
            transaction.replace(R.id.receiverContainer, recvFragment, RECV_FRAGMENT);

            transaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        clearRemoteConfiguration();
        super.onDestroy();
    }

    private void setupRemoteConfiguration() {
        /* If preloaded AccessToken exists, use it. */
        SharedPrefsAccessKey sharedPrefsAccessKey =
                new SharedPrefsAccessKey(this);

        if (sharedPrefsAccessKey.isAccessTokenEmpty()) {
            Log.d(TAG, "Preloaded AccessToken does not exist");
        } else {
            Log.d(TAG, "Going to use preloaded AccessToken");
            if (sharedPrefsAccessKey.isAccessTokenExpired()) {
                onError(getString(R.string.auth_json_expired));
                return;
            }
            mServerUrl = sharedPrefsAccessKey.readAccessTokenServerUrl();
            mAccount = sharedPrefsAccessKey.readAccessTokenAccount();
            mSecretKey = sharedPrefsAccessKey.readAccessTokenSecretKey();
            return;
        }

        /* Load an AccessToken interactively */
        mConfigServerSettings = new ConfigServerSettings(this,
                new ConfigServerSettings.ConfigServerSettingsListener() {
                    /**
                     * Called when user-specified settings file (auth.json) contains valid
                     * parameter values.
                     *
                     * @param serverUrl      The URL of the configuration server.
                     * @param account        The login account for the configuration server.
                     * @param secretKey      The API key published by the configuration server.
                     * @param expirationDate Expiration date of the secretKey.
                     */
                    @Override
                    public void onParsed(@NonNull String serverUrl,
                                         @NonNull String account,
                                         @NonNull String secretKey,
                                         @NonNull Date expirationDate) {
                        mServerUrl = serverUrl;
                        mAccount = account;
                        mSecretKey = secretKey;
                        /*
                         * Don't care expirationDate here.
                         * Let onExpired() handle the event instead.
                         */

                        buildFragments(null);
                    }

                    @Override
                    public void onExpired() {
                        MainActivity.this.onError(
                                getString(R.string.auth_json_expired));
                    }

                    @Override
                    public void onError(@NonNull String description) {
                        MainActivity.this.onError(description);
                    }
                });
        mConfigServerSettings.launchDocumentPicker();
    }

    private void clearRemoteConfiguration() {
        Log.d(TAG, "clearRemoteConfiguration");
        if (mConfigServerSettings != null) {
            mConfigServerSettings.clearDocumentPicker();
            mConfigServerSettings = null;
        }
    }

    private boolean getPrefsToggleSslTls() {
        String key = getString(R.string.pref_key_toggle_tls);
        return mSharedPreferences.getBoolean(key, false);
    }

    private boolean getPrefsServerCertificates() {
        String key = getString(R.string.pref_key_tls_server_certs);
        return mSharedPreferences.getBoolean(key, false);
    }

    private boolean getPrefsClientCertificates() {
        String key = getString(R.string.pref_key_tls_client_certs);
        return mSharedPreferences.getBoolean(key, false);
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

        if (mUseConfigServer) {
            if (mConfigServerSettings != null) {
                Log.d(TAG, "Wait for the ConfigServerSettings callback");
            } else {
                buildFragments(null);
            }
            return;
        }
        if (getPrefsToggleSslTls()) {
            /* Use SSL/TLS */
            if (getPrefsClientCertificates()) {
                /* Use Client Certificate */
                String alias = mViewModel.getPrivateKeyAlias();
                if (alias != null) {
                    Log.d(TAG, "Re-use certificate alias: " + alias);
                    buildFragments(alias);
                } else {
                    /* Let user select the client certificate */
                    KeyChainHandler kch = new KeyChainHandler();
                    kch.checkCertificate(
                            MainActivity.this,
                            new KeyChainHandler.KeyChainListener() {
                                @Override
                                public void onPrivateKeyAlias(
                                        @Nullable String alias) {
                                    Log.d(TAG, "onPrivateKeyAlias: " + alias);
                                    mViewModel.setPrivateKeyAlias(alias);

                                    if (alias != null) {
                                        buildFragments(alias);
                                    } else {
                                        onError(getString(R.string.keychain_alias_unspecified));
                                    }
                                }
                            });
                }
            } else {
                /* Don't use Client Certificate */
                buildFragments(null);
            }
        } else {
            /* Don't use SSL/TLS */
            buildFragments(null);
        }
    }

    private void buildFragments(@Nullable String alias) {
        Log.d(TAG, "buildFragments: alias(" + alias + ")");

        SendFragment sendFragment = lookupSendFragment();
        RecvFragment recvFragment = lookupRecvFragment();

        if (mUseConfigServer) {
            /*
             * Automatic list item selection:
             * During the configuration server sessions, there may be a case
             * in which multiple choices are being presented depending on
             * the configuration content.
             * Usually, user will have to choose the desired item on the fly,
             * but also user can skip such interventions by specifying the
             * selection items beforehand.
             */
            SharedPrefsConfigServer sharedPrefsConfigServer =
                    new SharedPrefsConfigServer(this);
            String dataStream = sharedPrefsConfigServer.getDataStream();
            String serviceName = sharedPrefsConfigServer.getServiceName();

            if (sendFragment != null) {
                if (mServerUrl != null && mAccount != null && mSecretKey != null) {
                    /* Download SINETStream settings from configuration server */
                    sendFragment.setRemoteConfig(
                            mServerUrl, mAccount, mSecretKey);
                }
                if (dataStream != null && serviceName != null) {
                    /* Specify multiple-choice items for automatic selection */
                    sendFragment.setPredefinedParameters(dataStream, serviceName);
                }
            }
            if (recvFragment != null) {
                if (mServerUrl != null && mAccount != null && mSecretKey != null) {
                    /* Download SINETStream settings from configuration server */
                    recvFragment.setRemoteConfig(
                            mServerUrl, mAccount, mSecretKey);
                }
                if (dataStream != null && serviceName != null) {
                    /* Specify multiple-choice items for automatic selection */
                    recvFragment.setPredefinedParameters(dataStream, serviceName);
                }
            }
        }
        if (sendFragment != null) {
            sendFragment.initializeWriter(alias);
        }
        if (recvFragment != null) {
            recvFragment.initializeReader(alias);
        }
        toggleProgressBar(true);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        SendFragment sendFragment = lookupSendFragment();
        if (sendFragment != null) {
            /* Prevent timing-dependent NullPointerException */
            if (mIsWriterAvailable) {
                sendFragment.terminateWriter();
            }
            mIsWriterAvailable = false; /* Prevent race condition */
        }
        RecvFragment recvFragment = lookupRecvFragment();
        if (recvFragment != null) {
            /* Prevent timing-dependent NullPointerException */
            if (mIsReaderAvailable) {
                recvFragment.terminateReader();
            }
            mIsReaderAvailable = false; /* Prevent race condition */
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

    /**
     * Called when initialization process, including configuration loading,
     * has finished.
     * Now user can call "SinetStreamReader<T>.setup()" next.
     */
    @Override
    public void onReaderConfigLoaded() {
        RecvFragment recvFragment = lookupRecvFragment();
        if (recvFragment != null) {
            recvFragment.setupReader();
        }
    }

    @Override
    public void onReaderStatusChanged(boolean available) {
        /* Implementation of SinetStreamReaderListener.onStatusChanged */
        Log.d(TAG, "onReaderStatusChanged: available=" + available);
        mIsReaderAvailable = available;

        if (available) {
            if (mIsWriterAvailable) {
                /* Both Writer and Reader are ready. Stop running progress bar */
                toggleProgressBar(false);
            }

            /* See corresponding comment onWriterStatusChanged(). */
        }
    }

    /**
     * Called when the broker connection has lost and auto-reconnect
     * procedure is in progress.
     */
    @Override
    public void onReaderReconnectInProgress() {
        /* Implementation of SinetStreamReaderListener.onReaderReconnectInProgress */
        Log.d(TAG, "onReaderReconnectInProgress");
        mIsReaderAvailable = false;
        toggleProgressBar(true);
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

    /**
     * Called when initialization process, including configuration loading,
     * has finished.
     * Now user can call "SinetStreamWriter<T>.setup()" next.
     */
    @Override
    public void onWriterConfigLoaded() {
        SendFragment sendFragment = lookupSendFragment();
        if (sendFragment != null) {
            sendFragment.setupWriter();
        }
    }

    @Override
    public void onWriterStatusChanged(boolean available) {
        /* Implementation of SinetStreamWriterListener.onStatusChanged */
        Log.d(TAG, "onWriterStatusChanged: available=" + available);
        mIsWriterAvailable = available;

        if (available) {
            if (mIsReaderAvailable) {
                /* Both Writer and Reader are ready. Stop running progress bar */
                toggleProgressBar(false);
            }

            /*
             * Mqtt operations such like connect() and disconnect() are asynchronous
             * requests and take some time until completion.
             * On Application RESTART case, MainActivity issues disconnect() and
             * connect() requests without waiting for those completions.
             * That is, MainActivity may receive a disconnect-complete notification
             * for the previous disconnect(), followed by a connect-complete
             * notification for the new connect().
             *
             * [Before RESTART]
             *     ...
             *     onStop()
             *       sendFragment.stopWriter()
             *         disconnect()
             * ===========================================
             * [After RESTART]
             *     onCreate()
             *     onStart()
             *                      <-- disconnect-complete
             *       sendFragment.startWriter()
             *         connect()
             *                      <-- connect-complete
             */
            SendFragment sendFragment = lookupSendFragment();
            if (sendFragment != null) {
                sendFragment.toggleSendButton(available);
            }
        }
    }

    /**
     * Called when the broker connection has lost and auto-reconnect
     * procedure is in progress.
     */
    @Override
    public void onWriterReconnectInProgress() {
        /* Implementation of SinetStreamWriterListener.onWriterReconnectInProgress */
        Log.d(TAG, "onWriterReconnectInProgress");
        mIsWriterAvailable = false;
        toggleProgressBar(true);
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
    public void onError(@NonNull String description) {
        /* Implementation of SinetStreamWriterListener.onError */
        /* Implementation of SinetStreamReaderListener.onError */
        /* Implementation of SendFragmentListener.onError */
        /* Implementation of RecvFragmentListener.onError */
        Log.e(TAG, "onError: " + description);

        toggleProgressBar(false);
        DialogUtil.showErrorDialog(
                this, description, null, true);

        /*
         * If user pressed OK button on the error dialog window,
         * ErrorDialogFragment.onErrorDialogDismissed() will be called.
         */
    }

    @Override
    public void onErrorDialogDismissed(boolean isFatal) {
        /* Implementation of ErrorDialogFragment.onErrorDialogDismissed */

        toggleProgressBar(false);
        if (isFatal) {
            onBackPressed();
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        /*
         * It seems strange, but calling super.onBackPressed() seems to
         * nullify the setResult() effect.
         *
        super.onBackPressed();
         */
        Log.i(TAG, "Going to finish myself...");
        Intent intent = new Intent();
        intent.putExtra(ActivityCodes.KEY, ActivityCodes.ACTIVITY_CODE_MAIN);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void toggleProgressBar(boolean enabled) {
        /*
         * NB:
         * This method might be called from a non-main thread.
         * Explicitly specify a looper for the constructor of Handler().
         *
         * > ViewRoot$CalledFromWrongThreadException:
         * > Only the original thread that created a view hierarchy
         * > can touch its views.
         */
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                /*
                 * Swap ProgressBar and Sender/Receiver container
                 */
                LinearLayout progressBar = findViewById(R.id.progressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
                }
                FrameLayout senderContainer = findViewById(R.id.senderContainer);
                if (senderContainer != null) {
                    senderContainer.setVisibility(enabled ? View.GONE : View.VISIBLE);
                }
                FrameLayout receiverContainer = findViewById(R.id.receiverContainer);
                if (receiverContainer != null) {
                    receiverContainer.setVisibility(enabled ? View.GONE : View.VISIBLE);
                }
            }
        });
    }
}
