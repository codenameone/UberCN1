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

package com.codename1.apps.uberclone.dataobj;

import com.codename1.properties.BooleanProperty;
import com.codename1.properties.DoubleProperty;
import com.codename1.properties.FloatProperty;
import com.codename1.properties.LongProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * Property object representing a user
 *
 * @author Shai Almog
 */
public class User implements PropertyBusinessObject {
    public final LongProperty<User> id = new LongProperty<>("id");
    public final Property<String, User> givenName = new Property<>("givenName");
    public final Property<String, User> surname = new Property<>("surname");
    public final Property<String, User> phone = new Property<>("phone");
    public final Property<String, User> email = new Property<>("email");
    public final Property<String, User> facebookId = new Property<>("facebookId");
    public final Property<String, User> googleId = new Property<>("googleId");
    public final BooleanProperty<User> driver = new BooleanProperty<>("driver");
    public final Property<String, User> car = new Property<>("givenName");
    public final FloatProperty<User> currentRating = new FloatProperty<>("currentRating");
    public final DoubleProperty<User> latitude = new DoubleProperty<>("latitude");
    public final DoubleProperty<User> longitude = new DoubleProperty<>("longitude");
    public final FloatProperty<User> direction = new FloatProperty<>("direction");
    public final Property<String, User> authToken = new Property<>("authToken");
    public final Property<String, User> password = new Property<>("password");
    public final Property<String, User> pushToken = new Property<>("pushToken");
    public final LongProperty<User> currentRide = new LongProperty<>("currentRide");

    private final PropertyIndex idx = new PropertyIndex(this, "User", id, givenName, 
            surname, phone, email, facebookId, googleId, driver, car, currentRating,
            latitude, longitude, direction, authToken, password, pushToken, currentRide);
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

}
