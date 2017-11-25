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

package com.codename1.apps.uberclone.forms;

import com.codename1.components.MultiButton;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.Style;

/**
 * Common code for construction and initialization of various classes e.g. the side menu logic etc.
 *
 * @author Shai Almog
 */
public class CommonCode {
    private static Image avatar;
    public static Image getAvatar() {
        if(avatar == null) {
            int size = convertToPixels(10);
            Image temp = Image.createImage(size, size, 0xff000000);
            Graphics g = temp.getGraphics();
            g.setAntiAliased(true);
            g.setColor(0xffffff);
            g.fillArc(0, 0, size, size, 0, 360);
            Object mask = temp.createMask();
            Style s = new Style();
            s.setFgColor(0xc2c2c2);
            s.setBgTransparency(255);
            s.setBgColor(0xe9e9e9);
            FontImage x = FontImage.createMaterial(FontImage.MATERIAL_PERSON, s, size);
            avatar = x.fill(size, size);
            if(avatar instanceof FontImage) {
                avatar = ((FontImage)avatar).toImage();
            }
            avatar = avatar.applyMask(mask);
        }
        return avatar;
    }
    
    public static void constructSideMenu(Toolbar tb) {
        Label userAndAvatar = new Label("Shai Almog", getAvatar(), "AvatarBlock");
        userAndAvatar.setGap(convertToPixels(3));
        tb.addComponentToSideMenu(userAndAvatar);
        
        MultiButton uberForBusiness = new MultiButton("Do you Uber for business?");
        uberForBusiness.setTextLine2("Tap to create your business profile");
        uberForBusiness.setUIID("UberForBusinessBackground");
        uberForBusiness.setUIIDLine1("UberForBusinessLine1");
        uberForBusiness.setUIIDLine2("UberForBusinessLine2");
        tb.addComponentToSideMenu(uberForBusiness);
        
        tb.addCommandToSideMenu("Payment", null, e -> {});
        tb.addCommandToSideMenu("Your Trips", null, e -> {});
        tb.addCommandToSideMenu("Help", null, e -> {});
        tb.addCommandToSideMenu("Free Rides", null, e -> {});
        tb.addCommandToSideMenu("Settings", null, e -> {});
        
        Button legalButton = new Button("Legal", "Legal");
        Container legal = BorderLayout.centerCenterEastWest(null, new Label("v4.178.1001", "VersionNumber"), legalButton);
        legal.setLeadComponent(legalButton);
        legal.setUIID("SideNavigationPanel");
        tb.setComponentToSideMenuSouth(legal);
    } 
}
