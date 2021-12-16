/*
 * Copyright (c) 2021 National Institute of Informatics
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

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

public class EchoViewModel extends ViewModel {
    private String mPrivateKeyAlias = null;

    @Nullable
    public String getPrivateKeyAlias() {
        return mPrivateKeyAlias;
    }

    public void setPrivateKeyAlias(@Nullable String alias) {
        this.mPrivateKeyAlias = alias;
    }
}
