package com.codename1.apps.uberclone;


import com.codename1.apps.uberclone.forms.LoginForm;
import com.codename1.apps.uberclone.forms.MapForm;
import com.codename1.apps.uberclone.server.UserService;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Toolbar;
import java.io.IOException;
import com.codename1.ui.layouts.BoxLayout;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class UberClone {
    private static boolean driverMode;
    private Form current;
    private Resources theme;

    protected boolean driverMode() {
        return false;
    }
    
    public static boolean isDriverMode() {
        return driverMode;
    }
    
    public void init(Object context) {
        driverMode = driverMode();
        NetworkManager.getInstance().updateThreadCount(2);
        theme = UIManager.initFirstTheme("/theme");
        Toolbar.setGlobalToolbar(true);
        Log.bindCrashProtection(true);
        Display.getInstance().lockOrientation(true);
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        
        if(UserService.isLoggedIn()) {
            UserService.loadUser();
            MapForm.get().show();
        } else {
            new LoginForm().show();
        }
    }

    public void stop() {
        current = getCurrentForm();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = getCurrentForm();
        }
    }
    
    public void destroy() {
    }

}
