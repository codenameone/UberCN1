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

import com.codename1.apps.uberclone.UberClone;
import com.codename1.apps.uberclone.dataobj.User;
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
import static com.codename1.apps.uberclone.server.Globals.*;
import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.Log; 
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import java.io.IOException;

/**
 * A generic service class that handles login/creation etc.
 *
 * @author Shai Almog
 */
public class UserService {
    private static User me;
    
    public static User getUser() {
        return me;
    }
    
    public static String getToken() {
        if(UberClone.isDriverMode()) {
            return Preferences.get("driver-token", null);
        } else {
            return Preferences.get("token", null);
        }
    }
    
    public static void loadUser() {
        me = new User();
        if(UberClone.isDriverMode()) {
            PreferencesObject.create(me).setPrefix("driver").bind();        
        } else {
            PreferencesObject.create(me).bind();        
        }
        if(Display.getInstance().isSimulator()) {
            Log.p("User details: " + me.getPropertyIndex().toString());
        }
    }
    
    public static void logout() {
        if(UberClone.isDriverMode()) {
            Preferences.set("driver-token", null);
        } else {
            Preferences.set("token", null);
        }
    }
    
    public static boolean isLoggedIn() {
        return getToken() != null;
    }
    
    public static void sendSMSActivationCode(String phoneNumber) {
        TwilioSMS tw = TwilioSMS.create(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_PHONE);

        Random r = new Random();
        String val = "";
        for(int iter = 0 ; iter < 4 ; iter++) {
            val += r.nextInt(10);
        }
        Preferences.set("phoneVerification", val);
        if(Display.getInstance().isSimulator() || DEBUG) {
            Log.p("Debug verification code: " + val + " sent to phone " + phoneNumber);
        }
        tw.sendSmsAsync(phoneNumber, val);
    }
    
    public static void resendSMSActivationCode(String phoneNumber) {
        TwilioSMS tw = TwilioSMS.create(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_PHONE);
        tw.sendSmsAsync(phoneNumber, Preferences.get("phoneVerification", null));
    }

    public static boolean validateSMSActivationCode(String code) {
        String val = Preferences.get("phoneVerification", null);
        return code.indexOf(val) > -1 && code.length() < 80;
    }

    public static boolean userExists(String phoneNumber, String facebookId, String googleId) {
        if(phoneNumber != null) {
            return userExistsPhone(phoneNumber);
        }
        if(facebookId != null) {
            return userExistsFacebook(facebookId);
        }
        return userExistsGoogle(googleId);
    }
    
    public static boolean userExistsPhone(String phoneNumber) {
        return userExistsImpl("user/exists", phoneNumber);
    }

    public static boolean userExistsFacebook(String phoneNumber) {
        return userExistsImpl("user/existsFacebook", phoneNumber);
    }

    public static boolean userExistsGoogle(String phoneNumber) {
        return userExistsImpl("user/existsGoogle", phoneNumber);
    }

    private static boolean userExistsImpl(String url, String val) {
        Response<byte[]> b = Rest.get(SERVER_URL + url).
                acceptJson().
                queryParam("v", val).getAsBytes();
        if(b.getResponseCode() == 200) {
            // the t from true
            return b.getResponseData()[0] == (byte)'t';
        }
        return false;
    }

    public static void registerPushToken(String pushToken) {
        Rest.get(SERVER_URL + "user/setPushToken").
                queryParam("token", getToken()).
                queryParam("pushToken", pushToken).getAsStringAsync(new Callback<Response<String>>() {
            @Override
            public void onSucess(Response<String> value) {
            }

            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
            }
        });
    }
    
    public static boolean addNewUser(User u) {
        Response<String> token = Rest.post(SERVER_URL + "user/add").
                jsonContent().
                body(u.getPropertyIndex().toJSON()).getAsString();
        if(token.getResponseCode() != 200) {
            return false;
        }
        if(UberClone.isDriverMode()) {
            Preferences.set("driver-token", token.getResponseData());
            registerPush();
        } else {
            Preferences.set("token", token.getResponseData());
        }
        return true;
    }

    public static void editUser(User u) {
        Rest.post(SERVER_URL + "user/add").
                jsonContent().
                body(u.getPropertyIndex().toJSON()).getAsStringAsync(new Callback<Response<String>>() {
            @Override
            public void onSucess(Response<String> value) {
            }

            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
            }
        });
    }
    
    public static void login(String phoneNumber, String facebookId, String googleId, String password, final SuccessCallback<User> onSuccess, final FailureCallback<Object> onError) {
        Rest.get(SERVER_URL + "user/login").
                acceptJson().
                queryParam("password", password).
                queryParam("phone", phoneNumber).
                queryParam("facebookId", facebookId).
                queryParam("googleId", googleId).
                getAsJsonMapAsync(new Callback<Response<Map>>() {
            @Override
            public void onSucess(Response<Map> value) {
                me = new User();
                me.getPropertyIndex().populateFromMap(value.getResponseData());
                if(UberClone.isDriverMode()) {
                    Preferences.set("driver-token", me.authToken.get());
                    PreferencesObject.create(me).setPrefix("driver").bind();
                } else {
                    Preferences.set("token", me.authToken.get());
                    PreferencesObject.create(me).bind();
                }
                onSuccess.onSucess(me);
            }

            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                onError.onError(null, err, errorCode, errorMessage);
            }
        });
    }
    
    public static void fetchAvatar(SuccessCallback<Image> callback) {
        fetchAvatar(me.id.getLong(), callback);
    }
    
    public static void fetchAvatar(long id, SuccessCallback<Image> callback) {
        ConnectionRequest cr = new ConnectionRequest(SERVER_URL + "user/avatar/" + id, false);
        cr.setFailSilently(true);
        cr.downloadImageToStorage("avatarImage-" + id, callback);
    }
    
    
    public static void setAvatar(String imageFile) {
        try {
            MultipartRequest mp = new MultipartRequest();
            mp.setUrl(SERVER_URL + "user/updateAvatar/" + getToken());
            mp.addData("img", imageFile, "image/jpeg");
            addToQueue(mp);
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error uploading avatar file: " + err);
        }
    }
}
