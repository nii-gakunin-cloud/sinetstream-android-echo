<!--
Copyright (C) 2020-2021 National Institute of Informatics

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
--->

[English](README.en.md)

## 概要

本アプリケーションは、Pub/Subメッセージングモデルにおける送受信
クライアントである`Publisher`機能(`Writer`)、および`Subscriber`
機能(`Reader`)の双方を実装する。  
`Writer`は、GUI操作でユーザが入力した任意文字列をAndroid版の
[SINETStreamライブラリ](https://www.sinetstream.net/docs/userguide/android.html)
を経由して`SINETStreamメッセージ`を対向`Broker`に送信する。
一方`Reader`は、同ライブラリ経由で対向`Broker`からの`SINETStream
メッセージ`の内容を受け取り、GUIに表示する。

```
     Android Application
    +-------------------------------+
    |  +----------+   +----------+  |
    |  |  Writer  |   |  Reader  |  |
    |  +----------+   +----------+  |
    |  +-------------------------+  |
    |  | SINETStream for Android |  |                      Backend
    |  +-------------------------+  |                      Server
    +------|-----------A------------+                     +----------+
           |           |                 (         )      |          |
           |           |   [message]     (         )      |          |
           |           +-----------------( Network )<-----| +------+ |
           +---------------------------->(         )----->| |Broker| |
               [message]                 (         )      | +------+ |
                                                          +----------+
```

## 動作環境

* Android 8.0 (APIレベル26) 以上
* 対向`Broker`機能が稼働するサーバ
* 上記サーバへのIP疎通の確保


## 使用例

別紙
[Android版クイックスタートガイド](https://www.sinetstream.net/docs/tutorial-android/)
のうち、`チュートリアル - STEP1: テキスト送受信アプリの実行(sinetstream-android-echo)`
の項を参照のこと。


## ライセンス

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

