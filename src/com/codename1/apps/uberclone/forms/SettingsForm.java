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

import static com.codename1.apps.uberclone.forms.CommonCode.getAvatar;
import com.codename1.apps.uberclone.server.UserService;
import com.codename1.components.MultiButton;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;

/**
 * Settings form UI
 *
 * @author Shai Almog
 */
public class SettingsForm extends Form {
    public SettingsForm() {
        super(BoxLayout.y());
        CommonCode.initBlackTitleForm(this, "Account Settings", null);
        
        Button userAndAvatar = new Button("Shai Almog", "Label");
        userAndAvatar.setIcon(CommonCode.getAvatar(i -> userAndAvatar.setIcon(i)));
        userAndAvatar.setGap(convertToPixels(3));
        userAndAvatar.addActionListener(e -> new EditAccountForm().show());
        
        MultiButton addHome = CommonCode.createEntry(FontImage.MATERIAL_HOME, "Add Home");
        MultiButton addWork = CommonCode.createEntry(FontImage.MATERIAL_WORK, "Add Work");

        Button moreSavedPlaces = new Button("More Saved Places", "ConnectWithSocialButton");
        
        Button signOut = new Button("Sign Out", "Label");
        
        signOut.addActionListener(e -> {
            if(Dialog.show("Sign Out", "Are you sure?", "Sign Out", "Cancel")) {
                UserService.logout();
                new LoginForm().show();
            }
        });
        
        
        addAll(userAndAvatar, 
                CommonCode.createSeparator(), 
                new Label("Favorites"), 
                addHome, 
                addWork,
                moreSavedPlaces, 
                signOut);        
    }

    @Override
    protected void initGlobalToolbar() {
        super.initGlobalToolbar();
        getToolbar().setUIID("BlackToolbar");
    }
}
