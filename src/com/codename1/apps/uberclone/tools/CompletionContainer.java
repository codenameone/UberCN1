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

import com.codename1.apps.uberclone.forms.CommonCode;
import com.codename1.apps.uberclone.server.LocationService;
import com.codename1.apps.uberclone.server.SearchService;
import com.codename1.components.MultiButton;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.EventDispatcher;

/**
 *
 * @author Shai Almog
 */
public class CompletionContainer {
    private Container result;
    private boolean completionUsed;
    private final EventDispatcher dispatcher = new EventDispatcher();

    void updateCompletion(String text, AutoCompleteAddressInput dest) {
        SearchService.suggestLocations(text, LocationService.getCurrentLocation(), resultList -> {
            if(resultList != null && resultList.size() > 0) {
                result.removeAll();
                completionUsed = true;
                for(SearchService.SuggestionResult r : resultList) {
                    MultiButton mb = CommonCode.createEntry(FontImage.MATERIAL_PLACE, r.getMainText(), r.getSecondaryText());
                    result.add(mb);
                    mb.addActionListener(e -> {
                        dest.setTextNoEvent(r.getFullText());
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
        
    
    public void initCompletionBar() {
        if(!completionUsed) {
            return;
        }
        completionUsed = false;
        result.removeAll();
        initCompletionBarImpl();
    }
    
    private void initCompletionBarImpl() {
        MultiButton addHome = CommonCode.createEntry(FontImage.MATERIAL_HOME, "Add Home");
        MultiButton addWork = CommonCode.createEntry(FontImage.MATERIAL_WORK, "Add Work");
        MultiButton savedPlaces = CommonCode.createEntry(FontImage.MATERIAL_NAVIGATE_NEXT, "Saved Places");
        savedPlaces.setUIIDLine1("WhereToButtonLineNoBorder");
        savedPlaces.setEmblemUIID("WhereToButtonLineNoBorder");
        savedPlaces.setEmblem(FontImage.createMaterial(FontImage.MATERIAL_NAVIGATE_NEXT, savedPlaces.getIconComponent().getUnselectedStyle()));
        
        Label whereSeparator = new Label("", "WhereSeparator");
        whereSeparator.setShowEvenIfBlank(true);
        result.addAll(addHome, addWork, savedPlaces, whereSeparator);

        addHistoryToCompletionBar();
    }

    private void addHistoryToCompletionBar() {
        MultiButton history1 = CommonCode.createEntry(FontImage.MATERIAL_HISTORY, "Mikve Yisrael Str...");
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
