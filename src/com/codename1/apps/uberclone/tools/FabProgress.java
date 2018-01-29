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

package com.codename1.apps.uberclone.tools;

import com.codename1.components.FloatingActionButton;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Stroke;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.UITimer;

/**
 *
 * @author Shai Almog
 */
public class FabProgress {
    private UITimer timer;
    private Motion angle;
    private String originalUiid;
    private Stroke stroke;
    
    private FabProgress(FloatingActionButton fab) {
        originalUiid = fab.getUIID();
        stroke = new Stroke(convertToPixels(0.5f), Stroke.CAP_SQUARE, Stroke.JOIN_MITER, 1);
        angle = Motion.createEaseInMotion(0, 360, 1500);
        angle.start();
        timer = UITimer.timer(30, true, fab.getComponentForm(), () -> {
            int ang = angle.getValue();
            if(angle.isFinished()) {
                angle = Motion.createEaseInMotion(0, 360, 1500);
                angle.start();
            }
            updateFabStyle(fab.getUnselectedStyle(), ang);
            updateFabStyle(fab.getSelectedStyle(), ang);
            updateFabStyle(fab.getPressedStyle(), ang);
            fab.repaint();
        });
    }
    
    private void updateFabStyle(Style s, int angle) {
        RoundBorder rb = (RoundBorder)s.getBorder();
        s.setBorder(rb.stroke(stroke).
                strokeColor(0x297aa7).
                strokeOpacity(255).
                strokeAngle(angle));
    }
    
    public static void bind(FloatingActionButton fab) {
        FabProgress ff = new FabProgress(fab);
        fab.putClientProperty("$internFabProgress", ff);
    }
    
    public static void stop(FloatingActionButton fab) {
        FabProgress fp = (FabProgress)fab.getClientProperty("$internFabProgress");
        if(fp != null) {
            fp.timer.cancel();
            fab.setUIID(fp.originalUiid);
            fab.repaint();
        }
    }
}
