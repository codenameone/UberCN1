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

import com.codename1.googlemaps.MapContainer;
import com.codename1.location.Location;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Form;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.plaf.Style;

/**
 * The text input field that resides on the map and provides completion 
 * suggestions below or ability to pick from the map
 *
 * @author Shai Almog
 */
public class AutoCompleteAddressInput extends TextField {
    private final Container layers;
    private int firstX = -1, firstY = -1;
    private boolean dragStarted;
    private CompletionContainer completion;
    private ActionListener<ActionEvent> dragListener, releaseListener;
    private Location currentLocation;
    
    private boolean blockChangeEvent;
    
    public AutoCompleteAddressInput(String value, String hint, Container layers, 
            CompletionContainer completion) {
        super(value, hint, 40, TextField.ANY);
        this.completion = completion;
        this.layers = layers;
        getHintLabel().setUIID("FromToTextFieldHint");
        setUIID("FromToTextField");
        addDataChangedListener((i, ii) -> {
            if(blockChangeEvent) {
                return;
            }
            if(!getText().equals(value)) {
                completion.updateCompletion(getText(), this);
            }
        });
    }
    
    public void setTextNoEvent(String text) {
        blockChangeEvent = true;
        setText(text);
        blockChangeEvent = false;
    }

    @Override
    protected void focusGained() {
        completion.initCompletionBar();
    }
    
    @Override
    protected void deinitialize() {
        if(dragListener != null) {
            Form f = getComponentForm();
            f.removePointerDraggedListener(dragListener);
            f.removePointerReleasedListener(dragListener);
        }
        super.deinitialize();
    }
    
    @Override
    protected void initComponent() {
        super.initComponent();
        if(dragListener == null) {
            dragListener = e -> {
                processDragEvent(e);
            };
            getComponentForm().addPointerDraggedListener(dragListener);
            releaseListener = e -> {
                processReleaseEvent(e);
            };
            getComponentForm().addPointerReleasedListener(releaseListener);
        }
    }

    private void processReleaseEvent(ActionEvent e) {
        if(dragStarted) {
            e.consume();
            Component cmp = layers.getComponentAt(1);
            boolean dragUp = layers.getLayout().getComponentConstraint(cmp).equals(SOUTH);
            cmp.remove();
            cmp.setUIID(cmp.getUIID());
            boolean animateDown;
            if(dragUp) {
                animateDown = !(firstY - e.getY() > convertToPixels(8));
            } else {
                animateDown = e.getY() - firstY > convertToPixels(8);
            }
            if(animateDown) {
                layers.add(SOUTH, cmp);
                cmp.setPreferredSize(new Dimension(getDisplayWidth(), getDisplayHeight() / 8));
                Style s = cmp.getUnselectedStyle();
                s.setMarginUnit(Style.UNIT_TYPE_DIPS);
                s.setMarginLeft(3);
                s.setMarginRight(3);
            } else {
                layers.add(CENTER, cmp);
                cmp.setPreferredSize(null);
            }
            layers.animateLayout(200);
            firstX = -1;
            firstY = -1;
            dragStarted = false;
        }
    }

    private void processDragEvent(ActionEvent e) {
        Component cmp = layers.getComponentAt(1);
        boolean dragUp = layers.getLayout().getComponentConstraint(cmp).equals(SOUTH);
        if(dragStarted) {
            e.consume();
            cmp.getUnselectedStyle().setMarginUnit(Style.UNIT_TYPE_PIXELS);
            if(dragUp) {
                cmp.setPreferredSize(new Dimension(getDisplayWidth(), firstY - e.getY() + getDisplayHeight() / 8));
            } else {
                cmp.getUnselectedStyle().setMarginTop(Math.max(0, e.getY() - firstY));
            }
            layers.revalidate();
        } else {
            Component draggedCmp = getComponentForm().getComponentAt(e.getX(), e.getY());
            if(!draggedCmp.isChildOf((Container)cmp)) {
                return;
            }
            
            if(firstX == -1) {
                firstX = e.getX();
                firstY = e.getY();
            }
            if((!dragUp && e.getY() - firstY > convertToPixels(2)) ||
                    (dragUp && firstY - e.getY() > convertToPixels(2))) {
                e.consume();
                dragStarted = true;
            }
        }
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
