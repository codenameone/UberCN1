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
import static com.codename1.ui.CN.*;
import com.codename1.ui.util.UITimer;
import com.codename1.util.EasyThread;
import com.codename1.util.LazyValue;
import com.codename1.util.SuccessCallback;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
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

    private static final long MAX_UPDATE_FREQUENCY = 3000;
    private Location lastKnownLocation;
    private SocketConnection server;
    private CarAdded carCallback;
    private SuccessCallback<Location> locationCallback;
    private Map<Long, User> cars = new HashMap<>();
        
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
            super(Globals.SERVER_SOCKET_URL);
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
                if(ll == lon && lt == lat && dir == direction) {
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
                    String token = Preferences.get("token", null);
                    dos.writeShort(token.length());
                    for(int iter = 0 ; iter < token.length() ; iter++) {
                        dos.writeByte((byte)token.charAt(iter));
                    }
                    
                    dos.writeDouble(lat);
                    dos.writeDouble(lon);
                    dos.writeFloat(dir);
                    
                    // 1km search radius
                    dos.writeDouble(1);
                    
                    // we are not hailing a taxi right now
                    dos.writeByte(0);
                    
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
                int size = dis.readInt();
                for(int iter = 0 ; iter < size ; iter++) {
                    long id = dis.readLong();
                    User car = cars.get(id);
                    if(car == null) {
                        car = new User().
                                id.set(id).
                                latitude.set(dis.readDouble()).
                                longitude.set(dis.readDouble()).
                                direction.set(dis.readFloat());
                        cars.put(id, car);
                        User finalCar = car;
                        callSerially(() -> carCallback.carAdded(finalCar));
                    } else {
                        car.latitude.set(dis.readDouble()).
                            longitude.set(dis.readDouble()).
                            direction.set(dis.readFloat());
                    }
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
    
    private LocationService() {}
    
    public static void bind(CarAdded carCallback, SuccessCallback<Location> locationUpdate) {
        new LocationService().bindImpl(carCallback, locationUpdate);
    }
    
    public static interface CarAdded {
        void carAdded(User driver);
    }
}
