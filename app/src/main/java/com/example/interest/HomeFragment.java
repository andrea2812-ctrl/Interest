package com.example.interest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listViewPosts;
    private PostsAdapter adapter;
    private List<Post> postList;

    private DatabaseReference postsRef;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inizializza Firebase
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Inizializza ListView
        listViewPosts = view.findViewById(R.id.listViewPosts);
        postList = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), postList);
        listViewPosts.setAdapter(adapter);

        // Recupera i post dal database ordinati per timestamp decrescente
        postsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    postList.add(post);
                }

                // Ordina la lista in ordine decrescente per timestamp
                Collections.sort(postList, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                adapter.notifyDataSetChanged();  // Notifica l'adapter che i dati sono cambiati
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gestione errori
            }
        });

        return view;
    }
}
