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
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import jp.ad.sinet.stream.android.api.SinetStreamReaderString;
import jp.ad.sinet.stream.android.sample.MainActivity;
import jp.ad.sinet.stream.android.sample.R;
import jp.ad.sinet.stream.android.sample.constants.BundleKeys;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecvFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecvFragment extends Fragment {
    private final String TAG = RecvFragment.class.getSimpleName();

    private String mServiceName = "";
    private SinetStreamReaderString mSinetStreamReader = null;
    //private SinetStreamReaderBytes mSinetStreamReader = null;
    private RecvFragment.RecvFragmentListener mListener;
    private LinearLayoutManager mLinearLayoutManager;
    // private MainViewModel mViewModel;

    public static RecvFragment newInstance() {
        return new RecvFragment();
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context The Context which implements RecvFragmentListener
     */
    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);

        if (context instanceof RecvFragmentListener) {
            mListener = (RecvFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    " must implement RecvFragmentListener");
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        mListener = null;
        super.onDetach();
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     *
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     *
     * <p>Any restored child fragments will be created before the base
     * <code>Fragment.onCreate</code> method returns.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Here we assume that calling Activity will provide the
         * service name for this MQTT session.
         */
        Bundle bundle = getArguments();
        if (bundle != null) {
            String serviceName = bundle.getString(BundleKeys.BUNDLE_KEY_SERVICE_NAME);
            if (serviceName != null) {
                mServiceName = serviceName;
            }
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>A default View can be returned by calling {@link #Fragment} in your
     * constructor. Otherwise, this method returns null.
     *
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: savedInstanceState=" + savedInstanceState);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_receiver, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.timeLine);
        if (recyclerView != null) {
            // Set the adapter
            Context context = recyclerView.getContext();

            /*
             * LinearLayoutManager.ReverseLayout: false (default)
             *   1    |
             *   2    | stack downwards
             *   3    V
             *
             * LinearLayoutManager.ReverseLayout: true
             *   3    A
             *   2    | stack upwards
             *   1    |
             */
            mLinearLayoutManager = new LinearLayoutManager(context);
            mLinearLayoutManager.setReverseLayout(true);

            recyclerView.setLayoutManager(mLinearLayoutManager);
            recyclerView.setAdapter(new MessageAdapter());
        } else {
            Log.w(TAG, "onCreateView: RecyclerView (R.id.timeLine) not found?");
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: savedInstanceState=" + savedInstanceState);
        super.onActivityCreated(savedInstanceState);
        // mViewModel = ViewModelProvider.of(this).get(MainViewModel.class);

        Activity activity = getActivity();
        if (activity != null) {
            mSinetStreamReader = new SinetStreamReaderString(activity);
            // mSinetStreamReader = new SinetStreamReaderBytes(activity);
        } else {
            // This case must not happen.
            mListener.onError(TAG + "Cannot get Activity?");
        }
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        /*
         * Make following part as a separate method startReader().
         *
        if (mSinetStreamReader != null) {
            mSinetStreamReader.initialize(mServiceName);
        }
        */
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to Activity.onStop of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        /*
         * Make following part as a separate method stopReader().
         *
        if (mSinetStreamReader != null) {
            mSinetStreamReader.terminate();
        }
        */
        super.onStop();
    }

    public void startReader(@Nullable String alias) {
        if (mSinetStreamReader != null) {
            mSinetStreamReader.initialize(mServiceName, alias);
        }
    }

    public void stopReader() {
        if (mSinetStreamReader != null) {
            mSinetStreamReader.terminate();
        }
    }

    public void onDataReceived(@NonNull String topic,
                               long timestamp,
                               @NonNull Object data) {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            RecyclerView recyclerView =
                    activity.findViewById(R.id.timeLine);
            if (recyclerView != null) {
                MessageAdapter messageAdapter = (MessageAdapter) recyclerView.getAdapter();
                if (messageAdapter != null) {
                    /* Let MessageAdapter handle the received data presentation */
                    messageAdapter.addMessage(topic, timestamp, data);
                    scrollToBottom();
                } else {
                    mListener.onError(TAG + ": MessageAdapter not yet set?");
                }
            }
        }
    }

    public void clearDisplay() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            RecyclerView recyclerView =
                    activity.findViewById(R.id.timeLine);
            if (recyclerView != null) {
                MessageAdapter messageAdapter = (MessageAdapter) recyclerView.getAdapter();
                if (messageAdapter != null) {
                    messageAdapter.clearAllMessage();
                } else {
                    mListener.onError(TAG + ": MessageAdapter not yet set?");
                }
            }
        }
    }

    public void scrollToBottom() {
        /* Perform auto-scroll to the bottom */
        Activity activity = getActivity();
        if (activity != null) {
            RecyclerView recyclerView =
                    activity.findViewById(R.id.timeLine);
            if (recyclerView != null) {
                //LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                LinearLayoutManager llm = mLinearLayoutManager;
                int lastVisibleItemPosition = llm.findLastVisibleItemPosition();
                int lastItemPosition = llm.getItemCount() - 1;

                if (lastVisibleItemPosition < lastItemPosition) {
                    recyclerView.scrollToPosition(lastItemPosition);
                }
            }
        } else {
            Log.e(TAG, "scrollToBottom: Activity not found?");
        }
    }

    public void onServerDisconnected() {
        Log.d(TAG, "onServerDisconnected");
    }

    /**
     * Communicate with other fragments
     * http://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface RecvFragmentListener {
        void onError(@NonNull String errorMessage);
    }
}
