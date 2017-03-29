package com.company.ui.jfx.login;

import javafx.util.Pair;

/**
 * Created by Александр on 29.03.2017.
 */
public interface AuthenticationCallback {

    void onAuth(boolean isAuthenticated, User user);
}
