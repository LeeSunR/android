package com.el.ariby.ui.main.menu.follow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.el.ariby.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindFollowAdapter extends BaseAdapter implements Filterable {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    ArrayList<FollowItem> FollowItemList = new ArrayList<FollowItem>();
    ArrayList<FollowItem> filteredItemList = FollowItemList;
    DatabaseReference ref, followref;
    FirebaseDatabase database;
    FirebaseUser auth;
    Filter listFilter;
    String user, UsersUid;

    int followCount;
    ArrayList<String> followingUid = new ArrayList<>();

    public FindFollowAdapter(){

    }
    //Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    //position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("USER");
        followref = database.getReference("FRIEND");
        /*Intent intent = ();
        ArrayList<String> ReceiveArr = intent.getStringArrayListExtra("ArrayList");*/
        // "custom_follow_list" Layout을 inflate하여 convertView 참조 획득.

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_user_list,parent, false);
        }
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득

        ImageView iconImageView = convertView.findViewById(R.id.imageView1);
        TextView titleTextView = convertView.findViewById(R.id.textView1);
        final Button addfollow = convertView.findViewById(R.id.add_friend);
        TextView following_num = convertView.findViewById(R.id.following_num);
        TextView followers_num = convertView.findViewById(R.id.followers_num);
        //TextView descTextView = convertView.findViewById(R.id.textView2);
        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득

        FollowItem item = filteredItemList.get(position);
        // 아이템 내 각 위젯에 데이터 반영
        Glide.with(convertView).
                load(item.getIconDrawable()).
                fitCenter().
                into(iconImageView);
        titleTextView.setText(item.getNick());
        following_num.setText(item.getFollwingNum());
        followers_num.setText(item.getFollowerNum());
        addfollow.setTag(position);
        auth = FirebaseAuth.getInstance().getCurrentUser();
        user = auth.getUid();

        doWork(new Callback() {
            @Override
            public void callback(ArrayList<String> data) {
                followingUid = data;

                addfollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                UsersUid = snapshot.getKey();
                                boolean a= true;
                                Log.d("flow","2");
                                for (int i=0; i<followCount; i++){
                                    if(UsersUid.equals(followingUid.get(i)) || user.equals(UsersUid))
                                        a = false;
                                }

                                if(a){
                                    String following = dataSnapshot.child(user).child("following").getValue().toString();
                                    String follower =  dataSnapshot.child(UsersUid).child("follower").getValue().toString();
                                    int followingNum = Integer.parseInt(following);
                                    int followerNum = Integer.parseInt(follower);
                                    followingNum = followingNum+1;
                                    followerNum = followerNum+1;

                                    followref.child("following").child(user).child(UsersUid).setValue("true");
                                    followref.child("follower").child(UsersUid).child(user).setValue("true");
                                    ref.child(user).child("following").setValue(followingNum);
                                    ref.child(UsersUid).child("follower").setValue(followerNum);
                                    addfollow.setText("팔로잉");
                                    Toast.makeText(context, "팔로잉 되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        });
    }
});
        return convertView;

    }
    public void addItem(FollowItem item) {
        FollowItemList.add(item);
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null){
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends  Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length()==0){
                results.values = FollowItemList;
                results.count = FollowItemList.size();
            } else {
                ArrayList<FollowItem> itemList = new ArrayList<FollowItem>();

                for (FollowItem item : FollowItemList) {
                    if(item.getNick().toUpperCase().contains(constraint.toString().toUpperCase()))
                    {
                        itemList.add(item);
                    }
                }
                results.values = itemList;
                results.count = itemList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            filteredItemList = (ArrayList<FollowItem>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
    public interface Callback{
        void callback(ArrayList<String> data);
    }

        public void doWork(final Callback mCallback){
            followref.child("following").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    followCount = (int) dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String list = snapshot.getKey();
                        followingUid.add(list);
                        Log.d("flow","1");
                    }
                    mCallback.callback(followingUid);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
}
