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

package com.codename1.apps.uberclone.server;

import com.codename1.apps.uberclone.dataobj.User;
import com.codename1.components.ToastBar;
import com.codename1.io.Preferences;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.properties.PreferencesObject;
import com.codename1.sms.twilio.TwilioSMS;
import com.codename1.util.Callback;
import com.codename1.util.FailureCallback;
import com.codename1.util.SuccessCallback;
import java.util.Map;
import java.util.Random;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Shai Almog
 */
public class UserService {
    private static User me;
    
    public static void loadUser() {
        me = new User();
        PreferencesObject.create(me).bind();        
    }
    
    public static void logout() {
        Preferences.set("token", null);
    }
    
    public static boolean isLoggedIn() {
        return Preferences.get("token", null) != null;
    }
    
    public static void sendSMSActivationCode(String phoneNumber) {
        TwilioSMS tw = TwilioSMS.create("ACa4f4809e10981e60db18ff61adcc36fa", "1d8596aea94201da6830f14b20dda463", "+14159149077");

        Random r = new Random();
        String val = "";
        for(int iter = 0 ; iter < 4 ; iter++) {
            val += r.nextInt(10);
        }
        Preferences.set("phoneVerification", val);
        
        tw.sendSmsAsync(phoneNumber, val);
    }
    
    public static boolean validateSMSActivationCode(String code) {
        String val = Preferences.get("phoneVerification", null);
        return code.indexOf(val) > -1 && code.length() < 80;
    }
    
    public static boolean userExists(String phoneNumber) {
        Response<byte[]> b = Rest.get(Globals.SERVER_URL + "user/exists").
                acceptJson().
                queryParam("phone", phoneNumber).getAsBytes();
        if(b.getResponseCode() == 200) {
            // the t from true
            return b.getResponseData()[0] == (byte)'t';
        }
        return false;
    }
    
    public static boolean addNewUser(User u) {
        Response<String> token = Rest.post(Globals.SERVER_URL + "user/add").
                jsonContent().
                body(u.getPropertyIndex().toJSON()).getAsString();
        if(token.getResponseCode() != 200) {
            return false;
        }
        Preferences.set("token", token.getResponseData());
        return true;
    }
    
    public static void loginWithPhone(String phoneNumber, String password, final SuccessCallback<User> onSuccess, final FailureCallback<Object> onError) {
        Rest.get(Globals.SERVER_URL + "user/login").
                acceptJson().
                queryParam("password", password).
                queryParam("phone", phoneNumber).
                getAsJsonMapAsync(new Callback<Response<Map>>() {
            @Override
            public void onSucess(Response<Map> value) {
                me = new User();
                me.getPropertyIndex().populateFromMap(value.getResponseData());
                Preferences.set("token", me.authToken.get());
                PreferencesObject.create(me).bind();
                onSuccess.onSucess(me);
            }

            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                onError.onError(null, err, errorCode, errorMessage);
            }
        });
    }
}
