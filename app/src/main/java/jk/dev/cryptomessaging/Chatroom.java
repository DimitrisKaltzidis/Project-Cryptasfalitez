package jk.dev.cryptomessaging;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jk.dev.cryptomessaging.Utilities.MessagesListAdapter;

public class Chatroom extends AppCompatActivity {


    private Button btnSend;
    private EditText inputMsg;

    private MessagesListAdapter adapter;
    private List<Message> listMessages;
    private ListView listViewMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);

        listMessages = new ArrayList<Message>();

        adapter = new MessagesListAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message("JK", "1,2,test", false);
                Message msg1 = new Message("KJ", "3,4,test", true);
                listMessages.add(msg);

                adapter.notifyDataSetChanged();
                listMessages.add(msg1);
                adapter.notifyDataSetChanged();
                playSound();
            }
        });

    }

    public void playSound() {
        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
