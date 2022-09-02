/**
 * Copyright 2010-present Facebook.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imperial.activemilespro.interface_utility;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookBroadcastReceiver;

public class IntFacebookBroadcastReceiver extends FacebookBroadcastReceiver {

    private final String TAG = "IntFacebookBReceiver";

    @Override
    protected void onSuccessfulAppCall(String appCallId, String action, Bundle extras) {
        Log.d(TAG, "Photo uploaded by call " + appCallId + " succeeded.");
    }

    @Override
    protected void onFailedAppCall(String appCallId, String action, Bundle extras) {
        Log.d(TAG, "Photo uploaded by call " + appCallId + " failed.");
    }
}
