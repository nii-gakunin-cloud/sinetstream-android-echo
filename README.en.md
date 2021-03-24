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

[日本語](README.md)

## Overview

This application implements both `Writer` and `Reader` functionality,
which refers to `Publisher` and `Subscriber` clients defined in
Pub/Sub messaging model.  
When user types in arbitrary string on GUI, `Writer` sends it as a
`SINETStream message` to the peer `Broker` via
[SINETStream for Android](https://www.sinetstream.net/docs/userguide/android.html)
library. On the other hand, `Reader` receives a `SINETStream message`
from the `Broker` via the library and shows its content on GUI.

```
     Android Application
    +-------------------------------+
    |  +----------+   +----------+  |
    |  |  Writer  |   |  Reader  |  |
    |  +----------+   +----------+  |
    |  +-------------------------+  |
    |  | SINETStream for Android |  |                      Backend
    |  +-------------------------+  |                      Server
    +------|-----------A------------+                     +------------+
           |           |                 (         )      |            |
           |           |   [message]     (         )      | +--------+ |
           |           +-----------------( IP      )<-----| | Broker | |
           +---------------------------->( Network )----->| |        | |
               [message]                 (         )      | +--------+ |
                                                          +------------+
```

## Operating Environment

* Android 8.0 (API level 26) or higher
* A server which hosts `Broker` functionality
* IP reacherbility between Android and the server


## How to use

See section `TUTORIAL - STEP1: Run send/recv text (sinetstream-android-echo)` in
[Quick Start Guide (Android)](https://www.sinetstream.net/docs/tutorial-android/)
for details.


## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

