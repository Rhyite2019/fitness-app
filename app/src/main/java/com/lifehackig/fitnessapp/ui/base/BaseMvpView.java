package com.lifehackig.fitnessapp.ui.base;

/**
 * Created by Sheena on 3/6/17.
 */

public interface BaseMvpView {
    boolean displayLoadingAnimation();
    void hideLoadingAnimation();
    void setAppBarTitle(String title);
    void initBottomNav();
    void setBottomNavChecked(int position);
    void hideBottomNav();
    void navigateToMain();
}
