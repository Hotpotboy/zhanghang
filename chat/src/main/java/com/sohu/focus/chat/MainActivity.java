package com.sohu.focus.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.FriendData;
import com.sohu.focus.chat.data.FriendListData;
import com.sohu.focus.chat.data.SessionIdResponseData;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;

import java.util.ArrayList;


public class MainActivity extends Activity implements Response.ErrorListener,AdapterView.OnItemClickListener {
    private ListView mFriendList;
    /**好友列表适配器*/
    private FriendListAdapter mFriendListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFriendList = (ListView)findViewById(R.id.friend_list);
        mFriendList.setOnItemClickListener(this);
        getFriendList();
    }
    private void getFriendList(){
        String url = Const.URL_GET_FRIENDS+"?userId="+Const.currentId;
        StringRequest friendListRequest = new StringRequest(url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    FriendListData  friendList = objectMapper.readValue(response,FriendListData.class);
                    if (friendList != null && friendList.getErrorCode()==0) {
                        ArrayList<FriendData> friendDatas = friendList.getData();
                        if(mFriendListAdapter==null){
                            mFriendListAdapter = new FriendListAdapter(MainActivity.this,friendDatas);
                            mFriendList.setAdapter(mFriendListAdapter);
                        }else{
                            mFriendListAdapter.setDatas(friendDatas);
                        }
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },this);
        VolleyUtils.requestNet(friendListRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int friendId = Const.otherId;
        String url = Const.URL_GET_SESSION_ID+"?userId="+Const.currentId+"&friendId="+friendId;
        StringRequest friendListRequest = new StringRequest(url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    SessionIdResponseData sessionIdData = objectMapper.readValue(response,SessionIdResponseData.class);
                    if (sessionIdData != null && sessionIdData.getErrorCode()==0) {
                        long sessionId = sessionIdData.getData();
                        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                        intent.putExtra(Const.INTENT_KEY_OTHER_ID,friendId);
                        intent.putExtra(Const.INTENT_KEY_SESSION_ID,sessionId);
                        startActivity(intent);
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },this);
        VolleyUtils.requestNet(friendListRequest);
    }
}
