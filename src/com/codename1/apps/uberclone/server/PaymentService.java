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

import com.codename1.braintree.Purchase;
import com.codename1.io.rest.Rest;
import static com.codename1.apps.uberclone.server.Globals.*;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.io.rest.Response;
import com.codename1.util.Callback;
 
/**
 *
 * @author Shai Almog
 */
public class PaymentService implements Purchase.Callback {
    private String rideId;
    
    private PaymentService(String rideId) {
        this.rideId = rideId;
    }
    
    public static void sendPaymentAuthorization(String rideId) {
        Purchase.startOrder(new PaymentService(rideId));
    }
    
    @Override
    public String fetchToken() {
        return Rest.get(SERVER_URL + "pay/token").
                        acceptJson().
                        getAsString().
                        getResponseData();
    }

    @Override
    public void onPurchaseSuccess(String nonce) {
        Log.p("Payment succeeded we got a nonce: " + nonce);
        Rest.get(SERVER_URL + "pay/token").
                acceptJson().
                queryParam("ride", rideId).
                queryParam("nonce", nonce).
                getAsStringAsync(new Callback<Response<String>>() {
            @Override
            public void onSucess(Response<String> value) {
            }

            @Override
            public void onError(Object sender, Throwable err, int errorCode, String errorMessage) {
                Log.p("Payment server error!");
                ToastBar.showErrorMessage("There was an error processing your payment in the server: " + errorMessage);
            }
        });
    }

    @Override
    public void onPurchaseFail(String a) {
        Log.p("Payment failed!");
        ToastBar.showErrorMessage("There was an error processing your payment: " + a);
    }

    @Override
    public void onPurchaseCancel() {
        Log.p("Purchase Canceled");
    }
}
