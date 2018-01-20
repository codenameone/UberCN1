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

import com.codename1.apps.uberclone.UberClone;
import com.codename1.apps.uberclone.dataobj.User;
import com.codename1.apps.uberclone.server.UserService;
import com.codename1.components.FloatingActionButton;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.util.FailureCallback;
import com.codename1.util.SuccessCallback;

/**
 * Implements the enter password form
 *
 * @author Shai Almog
 */
public class EnterPasswordForm extends Form {
    public EnterPasswordForm(String phoneNumber, String facebookId, String googleId) {
        super(new BorderLayout());
        Form previous = getCurrentForm();
        
        InfiniteProgress ip = new InfiniteProgress();
        Dialog dlg = ip.showInifiniteBlocking();
        boolean exists = UserService.userExists(phoneNumber, facebookId, googleId);
        dlg.dispose();
        
        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());
        
        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        
        if(exists) {
            box.add(new SpanLabel("Welcome back, signin to continue", "FlagButton"));
        } else {
            box.add(new SpanLabel("Please enter a new password", "FlagButton"));
        }
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
            Dialog ipDlg = new InfiniteProgress().showInifiniteBlocking();
            
            if(exists) {
                UserService.login(phoneNumber, facebookId, googleId, password.getText(), (value) -> {
                    MapForm.get().show();
                }, (sender, err, errorCode, errorMessage) -> {
                    ipDlg.dispose();
                    error.setText("Login error");
                    error.setVisible(true);
                    revalidate();
                });
            } else {                
                if(UserService.addNewUser(new User().
                        phone.set(phoneNumber).
                        facebookId.set(facebookId).
                        googleId.set(googleId).
                        password.set(password.getText()).
                        driver.set(UberClone.isDriverMode()))) {
                    MapForm.get().show();
                } else {
                    ipDlg.dispose();
                    error.setText("Signup error");
                    error.setVisible(true);
                    revalidate();
                }
            }
        });
    }
    
    
}
