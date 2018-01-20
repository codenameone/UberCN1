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
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.io.websocket.WebSocket;
import com.codename1.location.Location;
import com.codename1.location.LocationListener;
import com.codename1.location.LocationManager;
import com.codename1.push.Push;
import static com.codename1.ui.CN.*;
import static com.codename1.apps.uberclone.server.Globals.*;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.util.UITimer;
import com.codename1.util.EasyThread;
import com.codename1.util.LazyValue;
import com.codename1.util.SuccessCallback;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connects to the server and updates every time we move using the websocket API
 *
 * @author Shai Almog
 */
public class LocationService {
    private static final short MESSAGE_TYPE_LOCATION_UPDATE = 1;
    private static final short MESSAGE_TYPE_DRIVER_POSITIONS = 2;
    private static final short MESSAGE_TYPE_AVAILBLE_DRIVER_POSITIONS = 3;
    private static final short MESSAGE_TYPE_DRIVER_FOUND = 4;
    
    private static final short HAILING_OFF = 0;
    private static final short HAILING_ON = 1;
    private static final short HAILING_TURN_OFF = 2;
    
    private static LocationService instance;
    private static final long MAX_UPDATE_FREQUENCY = 3000;
    private static Location lastKnownLocation;
    private SocketConnection server;
    private CarAdded carCallback;
    private SuccessCallback<Location> locationCallback;
    private Map<Long, User> cars = new HashMap<>();
    private int hailing;
    private Motion halingRadius;
    private String from;
    private String to;
    private List<String> notificationList;
    
    private long driverId;
    private CarAdded driverFound;
        
    public static Location getCurrentLocation() {
        return lastKnownLocation;
    }
    
    private void bindImpl(CarAdded carCallback, SuccessCallback<Location> locationUpdate) {
        this.carCallback  = carCallback;
        locationCallback = locationUpdate;
        LocationManager.getLocationManager().setLocationListener(new LocationListener() {
            @Override
            public void locationUpdated(Location location) {
                lastKnownLocation = location;
                if(location.getStatus() == LocationManager.AVAILABLE && locationCallback != null) {
                    SuccessCallback<Location>  c = locationCallback;
                    locationCallback = null;
                    c.onSucess(location);
                }
                if(server != null) {
                    server.sendLocationUpdate();
                }
            }

            @Override
            public void providerStateChanged(int newState) {
            }
        });        
        new SocketConnection().connect();
    }
    
    class SocketConnection extends WebSocket {
        private double lat, lon;
        private float direction;
        private long lastUpdateTime;
        private EasyThread et;
        
        public SocketConnection() {
            super(SERVER_SOCKET_URL);
            et = EasyThread.start("Websocket");
        }
        
        @Override
        protected void onOpen() {
            // the server isn't ready for updates until this is invoked
            server = this;
            
            sendLocationUpdate();
        }
        
        public void sendLocationUpdate() {
            // we don't want to use the main thread for networking
            if(!et.isThisIt()) {
                et.run(() -> sendLocationUpdate());
                return;
            }
            if(lastKnownLocation != null) {
                double lt = lastKnownLocation.getLatitude();
                double ll = lastKnownLocation.getLongitude();
                float dir = lastKnownLocation.getDirection();
                if(ll == lon && lt == lat && dir == direction && hailing == HAILING_OFF) {
                    // no need to do an update
                    return;
                }
                
                long time = System.currentTimeMillis();
                if(time - lastUpdateTime < MAX_UPDATE_FREQUENCY) {
                    return;
                }
                lastUpdateTime = time;
                lon = ll;
                lat = lt;
                direction = dir;
                
                try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(bos);) {
                    
                    dos.writeShort(MESSAGE_TYPE_LOCATION_UPDATE);
                    String token = UserService.getToken();
                    dos.writeShort(token.length());
                    for(int iter = 0 ; iter < token.length() ; iter++) {
                        dos.writeByte((byte)token.charAt(iter));
                    }
                    
                    dos.writeDouble(lat);
                    dos.writeDouble(lon);
                    dos.writeFloat(dir);
                    
                    if(hailing == HAILING_ON) {
                        dos.writeDouble(((double)halingRadius.getValue()) / 1000.0);
                        dos.writeByte(HAILING_ON);
                        byte[] fromBytes = from.getBytes("UTF-8");
                        byte[] toBytes = to.getBytes("UTF-8");
                        dos.writeShort(fromBytes.length);
                        dos.write(fromBytes);
                        dos.writeShort(toBytes.length);
                        dos.write(toBytes);
                    } else {
                        // 1km search radius
                        dos.writeDouble(1);
                        dos.writeByte(hailing);
                        if(hailing == HAILING_TURN_OFF) {
                            hailing = HAILING_OFF;
                        }
                    }
                    
                    dos.flush();
                    send(bos.toByteArray());
                } catch(IOException err) {
                    // this isn't likely as this is a RAM based stream
                    Log.e(err);
                }
            }
        }

        @Override
        protected void onClose(int i, String string) {
            Log.p("Connection closed! Error... trying to reconnect in 5 seconds");
            UITimer.timer(5000, false, () -> connect());
        }

        @Override
        protected void onMessage(String string) {
        }

        @Override
        protected void onMessage(byte[] bytes) {
            try {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
                short response = dis.readShort();
                
                if(response == MESSAGE_TYPE_DRIVER_FOUND) {
                    driverId = dis.readLong();
                    User u = cars.get(driverId);
                    if(u == null) {
                        u = new User().id.set(driverId);
                        cars.put(driverId, u);
                    }
                    u.car.set(dis.readUTF()).
                            givenName.set(dis.readUTF()).
                            surname.set(dis.readUTF()).
                            currentRating.set(dis.readFloat()).
                            currentRide.set(dis.readLong());
                    final User finalUser = u;
                    callSerially(() -> driverFound.carAdded(finalUser));
                    return;
                }
                
                int size = dis.readInt();
                List<String> sendPush = null;
                for(int iter = 0 ; iter < size ; iter++) {
                    long id = dis.readLong();
                    User car = cars.get(id);
                    if(car == null) {
                        car = new User().
                                id.set(id).
                                latitude.set(dis.readDouble()).
                                longitude.set(dis.readDouble()).
                                direction.set(dis.readFloat()).
                                pushToken.set(dis.readUTF());
                        cars.put(id, car);
                        User finalCar = car;
                        callSerially(() -> carCallback.carAdded(finalCar));
                    } else {
                        car.latitude.set(dis.readDouble()).
                            longitude.set(dis.readDouble()).
                            direction.set(dis.readFloat()).
                            pushToken.set(dis.readUTF());
                    }
                    if(hailing == HAILING_ON && response == MESSAGE_TYPE_AVAILBLE_DRIVER_POSITIONS) {
                        if(!notificationList.contains(car.pushToken.get())) {
                            notificationList.add(car.pushToken.get());
                            if(sendPush == null) {
                                sendPush = new ArrayList<>();
                            }
                            sendPush.add(car.pushToken.get());
                        }
                    }
                }
                
                if(sendPush != null) {
                    String[] devices = new String[sendPush.size()];
                    sendPush.toArray(devices);
                    String apnsCert = APNS_DEV_PUSH_CERT;
                    String apnsPass = APNS_DEV_PUSH_PASS;
                    if(APNS_PRODUCTION) {
                        apnsCert = APNS_PROD_PUSH_CERT;
                        apnsPass = APNS_PROD_PUSH_PASS;
                    }
                    new Push(CODENAME_ONE_PUSH_KEY, 
                            "#" + UserService.getUser().id.getLong() + 
                                    ";Ride pending from: " + from + " to: " + to, 
                            devices).
                            pushType(3).
                            apnsAuth(apnsCert, apnsPass, APNS_PRODUCTION).
                            gcmAuth(GOOGLE_PUSH_AUTH_KEY).sendAsync();
                }
            } catch(IOException err) {
                // won't happen as this is in RAM
                Log.e(err);
            }
        }

        @Override
        protected void onError(Exception e) {
            Log.e(e);
        }
        
    }
    
    public static void hailRide(String from, String to, CarAdded callback) {
        instance.driverFound = callback;
        instance.from = from;
        instance.to = to;
        instance.notificationList = new ArrayList<>();
        instance.halingRadius = Motion.createLinearMotion(500, 2000, 30000);
        instance.halingRadius.start();
        instance.hailing = HAILING_ON;
        instance.server.sendLocationUpdate();
    }
    
    private LocationService() {}
    
    public static void bind(CarAdded carCallback, SuccessCallback<Location> locationUpdate) {
        instance = new LocationService();
        instance.bindImpl(carCallback, locationUpdate);
    }
    
    public static interface CarAdded {
        void carAdded(User driver);
    }
}
