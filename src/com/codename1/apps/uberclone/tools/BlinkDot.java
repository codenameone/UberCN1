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

import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import com.codename1.ui.Graphics;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.GeneralPath;

/**
 *
 * @author Shai Almog
 */
public class BlinkDot extends Component {
    private int value;
    private Motion growth;
    public BlinkDot() {
        setUIID("Label");
    }

    @Override
    protected void initComponent() {
        super.initComponent();
        getComponentForm().registerAnimated(this);
    }

    @Override
    protected void deinitialize() {
        getComponentForm().deregisterAnimated(this);
        super.deinitialize();
    }

    @Override
    public boolean animate() {
        if(growth == null || growth.isFinished()) {
            growth = Motion.createEaseInOutMotion(3, getWidth() / 2, 1000);
            growth.start();
        } 
        int newValue = growth.getValue();
        if(newValue != value) {
            value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {
        g.setAlpha(255);
        g.setColor(0x297aa7);
        int s = convertToPixels(2);
        g.setAntiAliased(true);
        g.fillArc(getX() + getWidth() / 2 - s / 2, getY() + getHeight() / 2 - s / 2, s, s, 0, 360);
        g.drawArc(getX() + getWidth() / 2 - value, getY() + getHeight() / 2 - value, value * 2, value * 2, 0, 360);
        g.drawArc(getX() + getWidth() / 2 - value - 1, getY() + getHeight() / 2 - value - 1, value * 2 + 1, value * 2 + 1, 0, 360);
    }

    @Override
    protected Dimension calcPreferredSize() {
        int s = convertToPixels(15);
        return new Dimension(s, s);
    }
    
    @Override
    protected void paintBackground(Graphics g) {
    }    
}
