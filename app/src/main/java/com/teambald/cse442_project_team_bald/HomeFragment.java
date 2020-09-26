package com.teambald.cse442_project_team_bald;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT: ";

    private ImageButton recordButton;
    private SignInButton loginButton;

    private HomeFragment homeFragObj;

    private GoogleSignInClient mGoogleSignInClient;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance() {
        HomeFragment hfrag = new HomeFragment();
        hfrag.setHomeFragObj(hfrag);
        return hfrag;
    }
    public void setHomeFragObj(HomeFragment hfo)
    {
        homeFragObj = hfo;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getActivity());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(homeFragObj.getActivity(), gso);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_fragment, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recordButton = view.findViewById(R.id.recorder_button);
        recordButton.setOnClickListener(new recordClickListener());
        loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new loginClickListener());
    }
    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            //TODO: @Xuanhua: Implement record function here.
            Log.i(TAG, "Record button is clicked.");
        }
    }
    private class loginClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 1);
        }
    }
}