package com.utis.warrants;

import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.record.MessagesRecord;
import com.utis.warrants.tables.MessagesTable;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MessageActivity extends FragmentActivity implements InputNameDialogListener, YesNoDialogListener {
	private static final boolean D = true;
	private static final String TAG = "MessageActivity";
	private static final int COMPOSE_MSG = 2;
    private DBSchemaHelper dbSch;
    public Context mContext;
    private TextView senderTitleView, senderNmView, msgDateView, msgDateChView, msgSubjView, msgTextView;
    private ZoomableImageView mAttachmentView;
    private String msgIdStr, msgIdExtStr;
    private int msgId, msgState;
    private long msgIdExt, selfEmpId, msgIdSender;
    private String msgDate, msgSender, msgSubj, msgText;
    private boolean updated;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		mContext = this;
		senderTitleView = (TextView)findViewById(R.id.sender_text);
		senderNmView = (TextView)findViewById(R.id.sender_nm);
		msgDateView = (TextView)findViewById(R.id.msg_date);
		msgDateChView = (TextView)findViewById(R.id.msg_date_ch);
		msgSubjView = (TextView)findViewById(R.id.msg_subj);
		msgTextView = (TextView)findViewById(R.id.msg_text);
		mAttachmentView = (ZoomableImageView)findViewById(R.id.imageViewAttachment);

		dbSch = DBSchemaHelper.getInstance(this);
		selfEmpId = dbSch.getUserId();

		Bundle b = getIntent().getExtras();
		msgIdStr = b.getString("id");
		msgId = Integer.parseInt(msgIdStr);
		msgIdExtStr = b.getString("id_ext");
		msgIdExt = Long.parseLong(msgIdExtStr);
		msgIdSender = Long.parseLong(b.getString("id_sender"));
		msgSender = b.getString("sender");
		msgDate = b.getString("date");
		msgSubj = b.getString("subj");
		msgText = b.getString("msg");
		msgState = Integer.parseInt(b.getString("id_state"));
		
		senderNmView.setText(msgSender);
		msgDateView.setText(msgDate);
		msgSubjView.setText(msgSubj);
		msgTextView.setText(msgText);

		showMessage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_reply_msg);
		if (mi != null && selfEmpId != msgIdSender) {
			mi.setVisible(true);
		}
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_reply_msg:
        	attemtNewMsg();
        	return true;
        case R.id.action_del_msg:
			if (dbSch.removeMsg(msgId, msgIdExt)) {
				updated = true;
				setMsgResult();
				finish();
			}
        	return true;
        }
        return false;
	}
	
	@Override
	public void onFinishYesNoDialog(boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinishInputDialog(String inputText) {
		// TODO Auto-generated method stub
		
	}

	private void setMsgResult() {
        Intent returnIntent = new Intent();
		Bundle b = new Bundle();
		b.putString("updated", Integer.toString((updated) ? 1 : 0));
		returnIntent.putExtras(b); 
        setResult(RESULT_OK, returnIntent);		
	}
	
	@Override
	public void onBackPressed() {
		setMsgResult();
	    super.onBackPressed();
	}
	
	private void attemtNewMsg() {
        Intent msgIntent = new Intent(this, ComposeNewMsgActivity.class);
        Bundle b = new Bundle();
        b.putString("selfEmpId", Long.toString(selfEmpId));
        b.putString("id_recipient", Long.toString(msgIdSender));
        b.putString("recipient", msgSender);
        b.putString("subj", "Re: "+ msgSubj);
        b.putString("msg", msgText);
        msgIntent.putExtras(b);            
        startActivityForResult(msgIntent, COMPOSE_MSG);		
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case COMPOSE_MSG:
                if (data.hasExtra("id")) {
                	long id = Long.parseLong(data.getExtras().getString("id"));
                	if (id > 0)
                		updated = true;
                }
            	break;
    		}
        }
    }   
	
    private void showMessage() {
    	SQLiteDatabase sqdb = dbSch.getWritableDatabase();
    	Cursor c = null;
    	MessagesRecord msg;
    	String query = "SELECT * FROM " + MessagesTable.TABLE_NAME + " WHERE " + MessagesTable.ID + " = " + msgIdStr;
		try {
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {
	    		msg = new MessagesRecord(c);
		    	if (msg.attachment != null && msg.attachment.length > 0) {
			    	Bitmap pic;
			    	pic = BitmapFactory.decodeByteArray(msg.attachment, 0, msg.attachment.length);
			    	mAttachmentView.setImageBitmap(pic);
		    	}
		    	msgDateChView.setText(dbSch.dateFormatMName.format(msg.mDateCh));
				if (msg.id_state == MessagesRecord.MSG_NEW && msg.id_recipient == selfEmpId)
					updated = dbSch.setMsgRead(Integer.parseInt(msgIdStr));
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	if (c != null) c.close();
		}
    }
	

}
