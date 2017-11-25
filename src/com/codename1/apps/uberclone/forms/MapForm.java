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

import com.codename1.apps.uberclone.tools.MapLayout;
import com.codename1.components.FloatingActionButton;
import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;

/**
 * The main form of the application containing the map code
 *
 * @author Shai Almog
 */
public class MapForm extends Form {
    private static final String MAP_JS_KEY = "AIzaSyDb6aabtmdQPw4Fn-HnWHLFOo72GUj-W_4";
    
    public MapForm() {
        super(new LayeredLayout());
        setScrollableY(false);
        MapContainer mc = new MapContainer(MAP_JS_KEY);
        add(mc);
        
        Container mapLayer = new Container();
        mapLayer.setLayout(new MapLayout(mc, mapLayer));
        
        Coord telAviv = new Coord(32.109333, 34.855499);
        mc.zoom(telAviv, mc.getMaxZoom() - 2);
        Label car = new Label(Resources.getGlobalResources().getImage("map-vehicle-icon-uberX.png"));
        mapLayer.add(telAviv, car);
        add(mapLayer);
        
        Button whereTo = new Button("Where To?", Image.createImage(convertToPixels(1f), convertToPixels(1f), 0xff000000), "WhereTo");
        whereTo.setGap(convertToPixels(2));
        add(BoxLayout.encloseY(whereTo));
        
        FloatingActionButton history1 = FloatingActionButton.createFAB(FontImage.MATERIAL_HISTORY, "History");
        FloatingActionButton history2 = FloatingActionButton.createFAB(FontImage.MATERIAL_HISTORY, "History");
        TextArea history1Label = new TextArea("Mikve Yisrael Str...", 3, 4);
        TextArea history2Label = new TextArea("Burgeranch", 3, 4);
        history1Label.setUIID("HistoryLabel");
        history2Label.setUIID("HistoryLabel");
        history1Label.setEditable(false);
        history1Label.setGrowByContent(false);
        history2Label.setEditable(false);
        history2Label.setGrowByContent(false);
        Container h1 = BoxLayout.encloseY(history1, history1Label);
        Container h2 = BoxLayout.encloseY(history2, history2Label);
        h1.setLeadComponent(history1);
        h2.setLeadComponent(history2);
        add(BorderLayout.south(FlowLayout.encloseCenter(h1, h2)));
    }

    @Override
    protected void initGlobalToolbar() {
        setToolbar(new Toolbar(true));
        CommonCode.constructSideMenu(getToolbar());
    }
    
    
}
