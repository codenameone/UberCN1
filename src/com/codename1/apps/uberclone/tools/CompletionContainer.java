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

import com.codename1.apps.uberclone.server.LocationService;
import com.codename1.apps.uberclone.server.SearchService;
import com.codename1.components.MultiButton;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.EventDispatcher;

/**
 *
 * @author Shai Almog
 */
public class CompletionContainer {
    private final Container layers;
    private Container result;
    private boolean completionUsed;
    private EventDispatcher dispatcher = new EventDispatcher();
    
    public CompletionContainer(Container layers) {
        this.layers = layers;
    }

    void updateCompletion(String text, AutoCompleteAddressInput dest) {
        if(dest.getClientProperty("LOCKED") != null) {
            return;
        }
        SearchService.suggestLocations(text, LocationService.getCurrentLocation(), resultList -> {
            if(resultList != null && resultList.size() > 0) {
                result.removeAll();
                completionUsed = true;
                for(SearchService.SuggestionResult r : resultList) {
                    MultiButton mb = createEntry(FontImage.MATERIAL_PLACE, r.getMainText(), r.getSecondaryText());
                    result.add(mb);
                    mb.addActionListener(e -> {
                        dest.putClientProperty("LOCKED", Boolean.TRUE);
                        dest.setText(r.getFullText());
                        dest.putClientProperty("LOCKED", null);
                        r.getLocation(l -> {
                            dest.setCurrentLocation(l);
                            dispatcher.fireActionEvent(e);
                        });
                    });
                }
                result.animateLayout(150);
            }
        });
    }
    
    void foldCompletion() {
        Component cmp = layers.getComponentAt(1);
        if(layers.getLayout().getComponentConstraint(cmp).equals(SOUTH)) {
            return;
        }
        cmp.remove();
        cmp.setUIID(cmp.getUIID());
        layers.add(SOUTH, cmp);
        cmp.setPreferredSize(new Dimension(getDisplayWidth(), getDisplayHeight() / 8));
        Style s = cmp.getUnselectedStyle();
        s.setMarginUnit(Style.UNIT_TYPE_DIPS);
        s.setMarginLeft(3);
        s.setMarginRight(3);
        layers.animateLayout(200);
    }
    
    void expandCompletion() {
        Component cmp = layers.getComponentAt(1);
        if(!layers.getLayout().getComponentConstraint(cmp).equals(SOUTH)) {
            return;
        }
        cmp.remove();
        cmp.setUIID(cmp.getUIID());
        layers.add(CENTER, cmp);
        cmp.setPreferredSize(null);
        layers.animateLayout(200);        
    }
    
    private MultiButton createEntry(char icon, String title) {
        MultiButton b = new MultiButton(title);
        b.setUIID("Container");
        b.setUIIDLine1("WhereToButtonLine1");
        b.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(b, icon);
        return b;
    }
    
    private MultiButton createEntry(char icon, String title, String subtitle) {
        MultiButton b = new MultiButton(title);
        b.setTextLine2(subtitle);
        b.setUIID("Container");
        b.setUIIDLine1("WhereToButtonLineNoBorder");
        b.setUIIDLine2("WhereToButtonLine2");
        b.setIconUIID("WhereToButtonIcon");
        FontImage.setMaterialIcon(b, icon);
        return b;
    }
    
    public void initCompletionBar() {
        if(!completionUsed) {
            return;
        }
        completionUsed = false;
        result.removeAll();
        initCompletionBarImpl();
    }
    
    private void initCompletionBarImpl() {
        MultiButton addHome = createEntry(FontImage.MATERIAL_HOME, "Add Home");
        MultiButton addWork = createEntry(FontImage.MATERIAL_WORK, "Add Work");
        MultiButton savedPlaces = createEntry(FontImage.MATERIAL_NAVIGATE_NEXT, "Saved Places");
        savedPlaces.setUIIDLine1("WhereToButtonLineNoBorder");
        savedPlaces.setEmblemUIID("WhereToButtonLineNoBorder");
        savedPlaces.setEmblem(FontImage.createMaterial(FontImage.MATERIAL_NAVIGATE_NEXT, savedPlaces.getIconComponent().getUnselectedStyle()));
        
        Label whereSeparator = new Label("", "WhereSeparator");
        whereSeparator.setShowEvenIfBlank(true);
        result.addAll(addHome, addWork, savedPlaces, whereSeparator);

        addHistoryToCompletionBar();
    }

    private void addHistoryToCompletionBar() {
        MultiButton history1 = createEntry(FontImage.MATERIAL_HISTORY, "Mikve Yisrael Str...");
        result.add(history1);
    }
    
    public void showCompletionBar(Container parentLayer) {
        result = new Container(BoxLayout.y());
        initCompletionBarImpl();
        
        result.setUIID("Form");
        result.setScrollableY(true);
        result.setScrollVisible(false);
        Container enclose = BorderLayout.center(result);
        enclose.setY(getDisplayHeight());
        enclose.setWidth(getDisplayWidth());
        enclose.setHeight(result.getPreferredH());
        parentLayer.add(CENTER, enclose);
        parentLayer.animateLayout(200);
    } 

    public void addCompletionListener(ActionListener<ActionEvent> a) {
        dispatcher.addListener(a);
    }

    public void removeCompletionListener(ActionListener<ActionEvent> a) {
        dispatcher.removeListener(a);
    }
    
}
