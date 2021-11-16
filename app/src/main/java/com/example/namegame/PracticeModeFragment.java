package com.example.namegame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PracticeModeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PracticeModeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Toolbar toolbar;

    private ImageView[] images;

    private TextView nameTextView;
    private int correctIndex;
    private int numGuesses;
    private int roundsRemaining;
    private int numCorrectGuesses;

    public PracticeModeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PracticeModeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PracticeModeFragment newInstance(String param1, String param2) {
        PracticeModeFragment fragment = new PracticeModeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_practice_mode, container, false);
        toolbar = view.findViewById(R.id.practiceModeToolbar);
        toolbar.setNavigationIcon(R.drawable.action_back);
        toolbar.setNavigationOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        numGuesses = 0;
        numCorrectGuesses = 0;
        roundsRemaining = 5;

        images = new ImageView[6];
        images[0] = view.findViewById(R.id.practiceImage1);
        images[1] = view.findViewById(R.id.practiceImage2);
        images[2] = view.findViewById(R.id.practiceImage3);
        images[3] = view.findViewById(R.id.practiceImage4);
        images[4] = view.findViewById(R.id.practiceImage5);
        images[5] = view.findViewById(R.id.practiceImage6);
        nameTextView = view.findViewById(R.id.practiceTextView);

        setImages();

        return view;
    }

    public void setImages() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numChild = Math.toIntExact(snapshot.getChildrenCount());
                // gives an array from [0 -> numchild-1]
                int[] arr = new int[numChild];
                for (int i = 1; i < numChild; i++) {
                    arr[i] = i;
                }
                for (int i = numChild-1; i >= numChild-6; i--) {
                    int randomNum = (int)(Math.random() * i);
                    int temp = arr[i];
                    arr[i] = arr[randomNum];
                    arr[randomNum] = temp;
                }
                int[] randomizedNumbers = new int[6];
                for (int i = 0; i < 6; i++) {
                    randomizedNumbers[i] = arr[arr.length-1-i];
                }
                correctIndex = (int)(Math.random() * 6);
                String formattedName = snapshot.child(String.valueOf(randomizedNumbers[correctIndex])).child("firstName").getValue() + " " +
                        snapshot.child(String.valueOf(randomizedNumbers[correctIndex])).child("lastName").getValue();
                nameTextView.setText(formattedName);
                for (int i = 0; i < 6; i++) {
                    int randomizedPerson = randomizedNumbers[i];
                    String url = (String) snapshot.child(String.valueOf(randomizedPerson)).child("headshot").child("url").getValue();
                    Picasso.get()
                            .load(url)
                            .resize(195, 130)
                            .centerCrop()
                            .into(images[i]);
                    images[i].setOnClickListener(PracticeModeFragment.this::onClick);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onClick(View v) {
        int index = -1;
        for (int i = 0; i < 6; i++) {
            if (images[i] == v) {
                index = i;
            }
        }
        assert (index != -1) : "Clicked image is not tracked";
        numGuesses++;
        // correct
        if (v == images[correctIndex]) {
            numCorrectGuesses++;
            roundsRemaining--;
            setImages();
            images[index].getDrawable().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.MULTIPLY);
        } else {
            images[index].getDrawable().setColorFilter(Color.parseColor("#ff0000"), PorterDuff.Mode.MULTIPLY);
        }
        if (roundsRemaining == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Game Over")
                .setMessage("You guessed " + numCorrectGuesses + "/" + numGuesses + ".")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .replace(R.id.fragmentContainerView, new HomeScreenFragment(), "Home")
                                .commit();
                    }
                });
            builder.create().show();
        }

    }


}
