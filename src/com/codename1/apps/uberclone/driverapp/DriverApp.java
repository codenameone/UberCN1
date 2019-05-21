/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */

package com.codename1.apps.uberclone.driverapp;

import com.codename1.apps.uberclone.UberClone;
import com.codename1.apps.uberclone.forms.MapForm;
import com.codename1.apps.uberclone.server.UserService;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.push.Push;
import com.codename1.push.PushCallback;
import static com.codename1.ui.CN.*;
import com.codename1.ui.FontImage;

/**
 *
 * @author Shai Almog
 */
public class DriverApp extends UberClone implements PushCallback {
    private long lastId;
            
    @Override
    protected boolean driverMode() {
        return true;
    }
    
    @Override
    public void init(Object context) {
        Log.p("Loaded Driver App");
        super.init(context);
    }

    @Override
    public void start() {
        super.start();
        callSerially(() -> {
            if(UserService.isLoggedIn()) {
                registerPush();
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void push(String value) {
        Log.p("Received push callback: " + value);
        if(value.startsWith("#")) {
            lastId = Long.parseLong(value.substring(1));
        } else {
            ToastBar.showMessage(value, FontImage.MATERIAL_INFO, 6000, e -> {
                MapForm map = MapForm.get();
                if(map != getCurrentForm()) {
                    map.show();
                }
                map.showRide(lastId);
            });
        }
    }

    @Override
    public void registeredForPush(String deviceId) {
        Log.p("Registered for push device key: " + Push.getPushKey());
        UserService.registerPushToken(Push.getPushKey());
    }

    @Override
    public void pushRegistrationError(String error, int errorCode) {
        Log.p("Error registering for push: " + error);
    }
}
