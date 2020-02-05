package com.example.project2;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new LifeFragment();
    }
}
