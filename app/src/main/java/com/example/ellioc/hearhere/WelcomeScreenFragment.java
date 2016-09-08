package com.example.ellioc.hearhere;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeScreenFragment extends Fragment {


    public WelcomeScreenFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment WelcomeScreenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WelcomeScreenFragment newInstance() {
        return new WelcomeScreenFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.welcome_screen_fragment, container, false);

        return view;
    }

}
