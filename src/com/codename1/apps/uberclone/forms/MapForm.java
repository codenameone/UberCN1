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
import com.codename1.components.MultiButton;
import com.codename1.components.ScaleImageLabel;
import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Painter;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.FocusListener;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;

/**
 * The main form of the application containing the map code
 *
 * @author Shai Almog
 */
public class MapForm extends Form {
    private static final String MAP_JS_KEY = "AIzaSyDb6aabtmdQPw4Fn-HnWHLFOo72GUj-W_4";
    private Image square;

    private Image dropShadow;
    
    public MapForm() {
        super(new LayeredLayout());
        setScrollableY(false);
        
        Display.getInstance().callSeriallyOnIdle(() -> {
            dropShadow = LoginForm.squareShadow(getDisplayWidth(), 30, convertToPixels(3), 0.40f);
        });
        
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        
        MapContainer mc = new MapContainer(MAP_JS_KEY);
        mc.setShowMyLocation(true);
        add(mc);
        
        Container mapLayer = new Container();
        mapLayer.setLayout(new MapLayout(mc, mapLayer));
        
        Coord telAviv = new Coord(32.072449, 34.778613);
        mc.zoom(telAviv, mc.getMaxZoom() + 1);
        Label car = new Label(Resources.getGlobalResources().getImage("map-vehicle-icon-uberX.png"));
        mapLayer.add(telAviv, car);
        add(mapLayer);
        
        square = Image.createImage(convertToPixels(0.7f), convertToPixels(0.7f), 0xff000000);
        Button whereTo = new Button("Where To?", square, "WhereTo");
        whereTo.setGap(convertToPixels(3));
        add(BoxLayout.encloseY(whereTo));
        
        whereTo.addActionListener(e -> showNavigationToolbar());
        
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
        ScaleImageLabel gradient = new ScaleImageLabel(Resources.getGlobalResources().getImage("gradient-overlay.png"));
        gradient.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(BorderLayout.south(gradient));
        add(BorderLayout.south(FlowLayout.encloseCenter(h1, h2)));
    }

    @Override
    protected void initGlobalToolbar() {
        setToolbar(new Toolbar(true));
        CommonCode.constructSideMenu(getToolbar());
    }
    

    void showNavigationToolbar() {
        final Container layer = getLayeredPane(MapForm.class, true);
        Button back = new Button("", "TitleCommand");
        FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK);

        TextField from = new TextField("", "From", 40, TextField.ANY);
        TextField to = new TextField("", "Where to?", 40, TextField.ANY);

        Image circle = Image.createImage(square.getWidth(), square.getHeight(), 0);
        Graphics g = circle.getGraphics();
        g.setColor(0xa4a4ac);
        g.setAntiAliased(true);
        g.fillArc(0, 0, circle.getWidth(), circle.getHeight(), 0, 360);
        
        final Label fromSelected = new Label(square);
        final Label toSelected = new Label(circle);
        
        from.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(Component cmp) {
                fromSelected.setIcon(square);
                if(layer.getComponentCount() > 1) {
                    Component c = layer.getComponentAt(1);
                    c.setY(getDisplayHeight());
                    layer.animateUnlayout(200, 150, () -> {
                        c.remove();
                        revalidate();
                    });
                }
            }

            @Override
            public void focusLost(Component cmp) {
                fromSelected.setIcon(circle);
            }
        });
        to.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(Component cmp) {
                fromSelected.setIcon(circle);
                toSelected.setIcon(square);
                showToNavigationBar(layer);
            }

            @Override
            public void focusLost(Component cmp) {
                toSelected.setIcon(circle);
            }
        });
        
        from.getHintLabel().setUIID("FromToTextFieldHint");
        from.setUIID("FromToTextField");
        to.getHintLabel().setUIID("FromToTextFieldHint");
        to.setUIID("FromToTextField");

        Container navigationToolbar = BoxLayout.encloseY(back, 
                BorderLayout.centerCenterEastWest(from, null, fromSelected), 
                BorderLayout.centerCenterEastWest(to, null, toSelected)
        );
        navigationToolbar.setUIID("WhereToToolbar");
        navigationToolbar.getUnselectedStyle().setBgPainter((g1, rect) -> {
            g1.setAlpha(255);
            g1.setColor(0xffffff);
            if(dropShadow != null) {
                if(layer.getComponentCount() > 1) {
                    g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                } 
                g1.drawImage(dropShadow, rect.getX(), rect.getY() + rect.getHeight() - dropShadow.getHeight(), rect.getWidth(), dropShadow.getHeight());
                g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() - dropShadow.getHeight() / 2);
            } else {
                g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            }
            g1.setColor(0xa4a4ac);
            g1.setAntiAliased(true);
            
            int x = fromSelected.getAbsoluteX() + fromSelected.getWidth() / 2 - 1;
            int y = fromSelected.getAbsoluteY() + fromSelected.getHeight() / 2 + circle.getHeight() / 2;
            g1.fillRect(x, y, 2, toSelected.getAbsoluteY() - y + toSelected.getHeight() / 2 - circle.getHeight() / 2);
        });
        
        back.addActionListener(e -> {
            navigationToolbar.setY(-navigationToolbar.getHeight());
            if(layer.getComponentCount() > 1) {
                // the second component is the to navigation bar
                layer.getComponentAt(1).setY(getDisplayHeight());
            }
            navigationToolbar.getParent().animateUnlayout(200, 120, () -> {
                    layer.removeAll();
                    revalidate();
            });
        });
        layer.setLayout(new BorderLayout());
        layer.add(NORTH, navigationToolbar);
        navigationToolbar.setWidth(getDisplayWidth());
        navigationToolbar.setHeight(getPreferredH());
        navigationToolbar.setY(-navigationToolbar.getHeight());
        layer.animateLayout(200);
    }
    
    private void showToNavigationBar(Container parentLayer) {
        MultiButton addHome = new MultiButton("Add Home");
        addHome.setUIID("Container");
        addHome.setUIIDLine1("WhereToButtonLine1");
        addHome.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(addHome, FontImage.MATERIAL_HOME);
        MultiButton addWork = new MultiButton("Add Work");
        addWork.setUIID("Container");
        addWork.setUIIDLine1("WhereToButtonLine1");
        addWork.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(addWork, FontImage.MATERIAL_WORK);
        MultiButton savedPlaces = new MultiButton("Saved Places");
        savedPlaces.setUIID("Container");
        savedPlaces.setUIIDLine1("WhereToButtonLineNoBorder");
        savedPlaces.setEmblemUIID("WhereToButtonLineNoBorder");
        savedPlaces.setIconUIID("WhereToButtonIcon");
        savedPlaces.setEmblem(FontImage.createMaterial(FontImage.MATERIAL_NAVIGATE_NEXT, savedPlaces.getIconComponent().getUnselectedStyle()));
        FontImage.setMaterialIcon(savedPlaces, FontImage.MATERIAL_STAR_BORDER);
        
        Label whereSeparator = new Label("", "WhereSeparator");
        whereSeparator.setShowEvenIfBlank(true);
        
        MultiButton history1 = new MultiButton("Mikve Yisrael Str...");
        history1.setUIID("Container");
        history1.setUIIDLine1("WhereToButtonLine1");
        history1.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(history1, FontImage.MATERIAL_HISTORY);
        
        Container result = BoxLayout.encloseY(addHome, addWork, savedPlaces, whereSeparator, history1);
        result.setUIID("Form");
        result.setScrollableY(true);
        result.setScrollVisible(false);
        result.setY(getDisplayHeight());
        result.setWidth(getDisplayWidth());
        result.setHeight(result.getPreferredH());
        parentLayer.add(CENTER, result);
        parentLayer.animateLayout(200);
    } 
}
