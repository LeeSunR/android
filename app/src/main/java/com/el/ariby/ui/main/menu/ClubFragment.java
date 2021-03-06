package com.el.ariby.ui.main.menu;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.el.ariby.R;
import com.el.ariby.ui.main.menu.club.ClubCreateActivity;
import com.el.ariby.ui.main.menu.club.ClubDetailActivity;
import com.el.ariby.ui.main.menu.club.ClubItem;
import com.el.ariby.ui.main.menu.club.ClubSearchActivity;
import com.el.ariby.ui.main.menu.club.CustomClub;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ClubFragment extends Fragment {
    Button btnCreate;
    Button btnFind;
    ListView listClub;
    ClubAdapter adapter;
    DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_club, container, false);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("CLUB");
        final SwipeRefreshLayout mSwipeRefreshLayout = v.findViewById(R.id.swipe_layout);

        btnCreate = v.findViewById(R.id.btn_create);
        btnFind = v.findViewById(R.id.btn_find);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ClubCreateActivity.class);
                startActivity(intent);
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ClubSearchActivity.class);
                startActivity(intent);
            }
        });

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("clubImageURL").getValue().toString();
                    String club = snapshot.getKey();
                    String nick = snapshot.child("leaderNick").getValue().toString();
                    long num = snapshot.child("member").getChildrenCount();
                    String location = snapshot.child("location").getValue().toString();
                    adapter.addItem(new ClubItem(url, club, nick, num, location));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        listClub = v.findViewById(R.id.list_club);
        adapter = new ClubAdapter();

        listClub.setAdapter(adapter);

        listClub.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title=((ClubItem)adapter.getItem(position)).getTitle();
                String nick=((ClubItem)adapter.getItem(position)).getNick();
                String logo=((ClubItem)adapter.getItem(position)).getMainLogo();
                long num=((ClubItem)adapter.getItem(position)).getNumber();
                String map=((ClubItem)adapter.getItem(position)).getMap();
                Intent intent = new Intent(getActivity(), ClubDetailActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("nick", nick);
                intent.putExtra("logo", logo);
                intent.putExtra("num", num);
                intent.putExtra("map", map);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.anim_slide_in_right, R.anim.not_move_activity);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.clearItem();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String url = snapshot.child("clubImageURL").getValue().toString();
                            String club = snapshot.getKey();
                            String nick = snapshot.child("leaderNick").getValue().toString();
                            long num = snapshot.child("member").getChildrenCount();
                            String location = snapshot.child("location").getValue().toString();
                            adapter.addItem(new ClubItem(url, club, nick, num, location));
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clearItem();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("clubImageURL").getValue().toString();
                    String club = snapshot.getKey();
                    String nick = snapshot.child("leaderNick").getValue().toString();
                    long num = snapshot.child("member").getChildrenCount();
                    String location = snapshot.child("location").getValue().toString();
                    adapter.addItem(new ClubItem(url, club, nick, num, location));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class ClubAdapter extends BaseAdapter {
        ArrayList<ClubItem> items = new ArrayList<>();

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Object getItem(int position) {
            return items.get(position);
        }

        public void addItem(ClubItem item) {
            items.add(item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomClub view = new CustomClub(getActivity());
            ClubItem item = items.get(position);
            view.setImgNickMain(item.getMainLogo());
            view.setTxtTitle(item.getTitle());
            view.setTxtNickname(item.getNick());
            view.setTxtNumber(String.valueOf(item.getNumber()));
            view.setTxtMap(item.getMap());
            return view;
        }

        public void clearItem() {
            items.clear();
        }
    }
}