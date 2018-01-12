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

import com.codename1.apps.uberclone.dataobj.Ride;
import com.codename1.io.rest.Rest;
import static com.codename1.apps.uberclone.server.Globals.*;
import com.codename1.io.rest.Response;
import com.codename1.util.SuccessCallback;
import java.util.Map;

/**
 *
 * @author Shai Almog
 */
public class DriverService {
    private static String currentRide;
    public static void fetchRideDetails(long id, SuccessCallback<Ride> rideDetails) {
        Rest.get(SERVER_URL + "ride/get").
                acceptJson().queryParam("id", "" + id).
                getAsJsonMap(response -> {
                    Map data = response.getResponseData();
                    if(data != null) {
                        Ride r = new Ride();
                        r.getPropertyIndex().populateFromMap(data);
                        rideDetails.onSucess(r);
                    }
                });
    }
    
    public static boolean acceptRide(long id) {
        Response<String> response = Rest.get(SERVER_URL + "ride/accept").
                acceptJson().
                queryParam("token", UserService.getToken()).
                queryParam("userId", "" + id).
                getAsString();
        if(response.getResponseCode() == 200) {
            currentRide = response.getResponseData();
            return true;
        }
        return false;
    }
    
    public static void startRide() {
        Rest.post(SERVER_URL + "ride/start").
                acceptJson().
                queryParam("id", currentRide).
                getAsString();
    }

    public static void finishRide() {
        Rest.post(SERVER_URL + "ride/finish").
                acceptJson().
                queryParam("id", currentRide).
                getAsString();
    }
}

