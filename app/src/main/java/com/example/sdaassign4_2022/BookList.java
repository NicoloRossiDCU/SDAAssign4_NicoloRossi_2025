package com.example.sdaassign4_2022;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * Images used are sourced from Public Domain Day 2019.
 * by Duke Law School's Center for the Study of the Public Domain
 * is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * A simple {@link Fragment} subclass.
 * @author Chris Coughlan
 */
public class BookList extends Fragment {


    public BookList() {
        // Required empty public constructor
    }

    ViewPageAdapter adapter;
    FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_book_list, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.bookView_view);

        // ref: https://firebase.google.com/docs/firestore/query-data/get-data

        ArrayList<Book> mBooks = new ArrayList<>();

        // get the list of books.
        // TODO: add listener for updates to each document so we get realtime updates
        db.collection("drawable")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //date are saved as timestamp in firestore so need to be converted to LocalDateTime
                               LocalDateTime lastBorrow = convertToLocalDateTime((com.google.firebase.Timestamp) Objects.requireNonNull(document.get("DateLastBorrow")));

                                mBooks.add(new Book(
                                        (String) document.get("title"),
                                        (String) document.get("author"),
                                        (String) document.get("Url"),
                                        // eventually there should be a validation here or conversion
                                        // to string and then to bool
                                        (boolean) Objects.requireNonNull(document.get("Available")),
                                        lastBorrow,
                                        (String) document.get("ID")
                                        ));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        LibraryViewAdapter recyclerViewAdapter = new LibraryViewAdapter(getContext(), mBooks);

                        recyclerView.setAdapter(recyclerViewAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    }
                });

            // I tried to add a listener that would update the list of books but did not work
/*        mBooks.forEach(book -> {
            DocumentReference docRef = db.collection("drawable").document(book.ID);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {

                    Log.e(TAG, snapshot.get("title").toString());
                }
            });
        }); */


        return root;
    }
private LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
    // Ref: https://code.luasoftware.com/tutorials/google-cloud-firestore/understanding-date-in-firestore
    long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
    ZoneId tz = ZoneId.systemDefault();
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), tz);
    }
}
