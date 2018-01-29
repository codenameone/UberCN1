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

import com.codename1.apps.uberclone.dataobj.User;
import com.codename1.apps.uberclone.server.UserService;
import com.codename1.capture.Capture;
import com.codename1.components.FloatingActionButton;
import com.codename1.properties.UiBinding;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;

/**
 * Settings form UI
 *
 * @author Shai Almog
 */
public class EditAccountForm extends Form {
    public EditAccountForm() {
        super(BoxLayout.y());
        CommonCode.initBlackTitleForm(this, "Edit Account", null);
        
        Button avatar = new Button("", "Label");
        avatar.setIcon(CommonCode.getAvatar(i -> avatar.setIcon(i)));
        avatar.addActionListener(e -> {
            String file = Capture.capturePhoto(512, -1);
            if(file != null) {
                avatar.setIcon(CommonCode.setAvatar(file));
                UserService.setAvatar(file);
            }
        });

        Label edit = new Label("", "Container");
        Style s = edit.getUnselectedStyle();
        s.setMarginUnit(Style.UNIT_TYPE_DIPS);
        s.setPaddingUnit(Style.UNIT_TYPE_DIPS);
        s.setMargin(3, 3, 3, 3);
        s.setPadding(1, 1, 1, 1);
        s.setFgColor(0xffffff);
        s.setBgTransparency(0);
        FontImage.setMaterialIcon(edit, FontImage.MATERIAL_EDIT, 2f);
        s.setBorder(RoundBorder.create().
                color(0).
                opacity(255).
                rectangle(false).
                shadowOpacity(0));
                
        Container avatarContainer = LayeredLayout.encloseIn(avatar, 
                FlowLayout.encloseBottom(edit));
        
        User user = UserService.getUser();
        UiBinding uib = new UiBinding();

        String userString = user.getPropertyIndex().toString();
        
        TextField firstName = new TextField("", "", 80, TextField.ANY);
        uib.bind(user.givenName, firstName);
        firstName.setUIID("Label");
        
        TextField surname = new TextField("", "", 80, TextField.ANY);
        uib.bind(user.surname, surname);
        surname.setUIID("Label");
        
        TextField email = new TextField("", "", 80, TextField.EMAILADDR);
        uib.bind(user.email, email);
        email.setUIID("Label");
                
        addAll(avatarContainer,
                CommonCode.createSeparator(),
                new Label("First Name","GrayLabel"),
                firstName,
                new Label("Last Name","GrayLabel"),
                surname,
                new Label("E-Mail","GrayLabel"),
                email
        );
        
        final Form previous = getCurrentForm();
        previous.addShowListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                previous.removeShowListener(this);
                UiBinding.unbind(user);
                String newUserString = user.getPropertyIndex().toString();
                if(!newUserString.equals(userString)) {
                    UserService.editUser(user);
                }
            }
        });
    }

    @Override
    protected void initGlobalToolbar() {
        super.initGlobalToolbar();
        getToolbar().setUIID("BlackToolbar");
    }
}
