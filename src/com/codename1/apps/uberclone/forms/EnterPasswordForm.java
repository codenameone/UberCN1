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

import com.codename1.components.FloatingActionButton;
import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;

/**
 * Implements the enter password form
 *
 * @author Shai Almog
 */
public class EnterPasswordForm extends Form {
    public EnterPasswordForm() {
        super(new BorderLayout());
        Form previous = getCurrentForm();
        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());
        
        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        
        box.add(new SpanLabel("Welcome back, signin to continue", "FlagButton"));
        TextField password = new TextField("", "Enter your password", 80, TextField.PASSWORD);
        setEditOnShow(password);
        box.add(password);

        SpanLabel error = new SpanLabel("Password error", "ErrorLabel");
        error.setVisible(false);
        box.add(error);
        
        add(CENTER, box);
        
        Button forgot = new Button("I forgot my password", "ForgotPassword");
        Button account = new Button("I don't have an account", "ForgotPassword");
        add(SOUTH, BoxLayout.encloseY(forgot, account));

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);
        
        fab.addActionListener(e -> {
            new MapForm().show();
        });
    }
    
    
}
