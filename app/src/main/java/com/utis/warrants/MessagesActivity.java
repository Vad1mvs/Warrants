package com.utis.warrants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.record.EmpRecord;
import com.utis.warrants.record.MessagesRecord;
import com.utis.warrants.tables.MessagesTable;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
//import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MessagesActivity extends FragmentActivity implements OnClickListener, OnScrollListener,
		SwipeRefreshLayout.OnRefreshListener, InputNameDialogListener, YesNoDialogListener {
	private static final boolean D = true;
	private static final String TAG = "MessagesActivity";
	private static final int MSG_SHOW = 1;
	private static final int COMPOSE_MSG = 2;
	private DBSchemaHelper dbSch;
	private SwipeListView mDBSwipeListView;
	private ListView mDBListView;
	private CustomMsgAdapter mDBArrayAdapter;
	public Context mContext;
	private TextView captionView;
	private long selfEmpId = 0, mEmpId = 0;
	private String empId, empNm, empWhere = "";
	private boolean updated;
	private View viewContainer;
	private SwipeRefreshLayout swipeLayout;
	private EditText textFilter;

	private static class CustomMsgAdapter extends ArrayAdapter<MessagesRecord> {
		ArrayList<MessagesRecord> msgArray, msgFiltered;
		MsgFilter msgFilter;
		String preConstraint = "";
		private long selfUserId = 0;
		private Context context;

		static class MsgViewHolder {
			public View backView;
			public View frontView;
			public TextView textSender;
			public TextView textDate;
			public TextView textSubj;
			public TextView textMsg;
			public CheckBox checkBox;
			public ImageView imgMsg;
			public Button btn1;
			public Button btn2;
			public Button btn3;
		}

		private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

		public void setNewSelection(int position, boolean value) {
			mSelection.put(position, value);
			notifyDataSetChanged();
		}

		public boolean isPositionChecked(int position) {
			Boolean result = mSelection.get(position);
			return result == null ? false : result;
		}

		public Set<Integer> getCurrentCheckedPosition() {
			return mSelection.keySet();
		}

		public void removeSelection(int position) {
			mSelection.remove(position);
			notifyDataSetChanged();
		}

		public void clearSelection() {
			mSelection = new HashMap<Integer, Boolean>();
			notifyDataSetChanged();
		}

		public CustomMsgAdapter(Context context, int layout, int resId,
				MessagesRecord[] items) {
			// Call through to ArrayAdapter implementation
			super(context, layout, resId, items);
			this.context = context;
		}

		public CustomMsgAdapter(Context context, int textViewResourceId, long userId) {
			// Call through to ArrayAdapter implementation
			super(context, textViewResourceId);
			selfUserId = userId;
			this.context = context;
	        this.msgArray = new ArrayList<MessagesRecord>();
	        this.msgFiltered = new ArrayList<MessagesRecord>();    		
		}

		public void clear() {
			super.clear();
			msgArray.clear();
			// msgFiltered.clear();
		}

		public void add(MessagesRecord item) {
			super.add(item);
			msgArray.add(item);
			// if (preConstraint != null && preConstraint.length() > 0)
			// getFilter().filter(preConstraint);
			// else
			// getFilter().filter("");
		}

		public void addAll(ArrayList<MessagesRecord> items) {
			super.addAll(items);
			msgArray.clear();
			msgArray.addAll(items);
			msgFiltered.clear();
			msgFiltered.addAll(items);
		}

		public MessagesRecord getItem(int position) {
			return msgFiltered.get(position);
		}
	 
		public long getItemId(int position) {
			return position;
		}		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgViewHolder viewHolder;
			View row = convertView;
			// Inflate a new row if one isn't recycled
			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.msg_row, null);
				// row =
				// LayoutInflater.from(getContext()).inflate(R.layout.msg_row,
				// parent, false);
				// configure view viewHolder
				viewHolder = new MsgViewHolder();
				viewHolder.backView = row.findViewById(R.id.back);
				viewHolder.frontView = row.findViewById(R.id.front);
				viewHolder.textSender = (TextView) row.findViewById(R.id.lineSender);
				viewHolder.textDate = (TextView) row.findViewById(R.id.lineDate);
				viewHolder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
				viewHolder.imgMsg = (ImageView) row.findViewById(R.id.imageViewAttachment);
				viewHolder.textSubj = (TextView) row.findViewById(R.id.lineSubj);
				viewHolder.textMsg = (TextView) row.findViewById(R.id.lineMsg);
				viewHolder.btn1 = (Button) row.findViewById(R.id.swipe_button1);
				viewHolder.btn2 = (Button) row.findViewById(R.id.swipe_button2);
				viewHolder.btn3 = (Button) row.findViewById(R.id.swipe_button3);
				row.setTag(viewHolder);
			} else {
				viewHolder = (MsgViewHolder) row.getTag();
			}
			final MessagesRecord item = getItem(position);
			// fill data
			viewHolder.backView.setVisibility(View.GONE);
			if (item.id_sender != selfUserId)
				viewHolder.textSender.setText(/* item.senderName + */" ->>");
			else
				viewHolder.textSender.setText(/* item.recipientName + */" <<-");
			viewHolder.textDate.setText(item.getDate());
			viewHolder.textSubj.setText(item.subj);
			viewHolder.textMsg.setText(item.msg);
			if (item.attachment != null && item.attachment.length > 0) {
				Bitmap pic;
				// BitmapFactory.Options bitmapOptions = new
				// BitmapFactory.Options();
				// bitmapOptions.inJustDecodeBounds = true;
				// pic = BitmapFactory.decodeByteArray(item.attachment, 0,
				// item.attachment.length, bitmapOptions);
				// int imageWidth = bitmapOptions.outWidth;
				// int imageHeight = bitmapOptions.outHeight;
				pic = BitmapFactory.decodeByteArray(item.attachment, 0,
						item.attachment.length);
				viewHolder.imgMsg.setImageBitmap(CommonClass.getResizedBitmap(
						pic, 48, 48));
			} else {
				viewHolder.imgMsg.setImageDrawable(null);
			}

			if (item.status == MessagesRecord.MSG_IMPORTANT) {
				viewHolder.textSender.setTypeface(Typeface.DEFAULT_BOLD);
				viewHolder.textDate.setTypeface(Typeface.DEFAULT_BOLD);
			} else {
				viewHolder.textSender.setTypeface(Typeface.DEFAULT);
				viewHolder.textDate.setTypeface(Typeface.DEFAULT);
			}
			if (item.id_state == MessagesRecord.MSG_NEW) {
				if (item.id_sender != selfUserId) {
					if (item.status == MessagesRecord.MSG_IMPORTANT)
						viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_new_imp_msg);
					else
						viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_new_msg);
				} else {
					viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_done);
				}
			} else if (item.modified == 1 || item.id_external == 0)
				viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_modified);
			else if (item.id_sender == selfUserId) {
				viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_done_lt);
			} else
				viewHolder.frontView.setBackgroundResource(R.drawable.list_selector);

			if (mSelection.get(position) != null) {
				viewHolder.frontView.setBackgroundResource(R.drawable.list_selector_selected);
			}

			viewHolder.checkBox.setVisibility(View.INVISIBLE);
			viewHolder.checkBox.setChecked(item.selected);
			viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						item.selected = true;
					} else {
						item.selected = false;
					}
					// if (Build.VERSION.SDK_INT >=
					// Build.VERSION_CODES.HONEYCOMB) {
					// invalidateOptionsMenu();
					// }
				}
			});

			viewHolder.btn1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "Button 1 Clicked",
							Toast.LENGTH_SHORT).show();
				}
			});

			viewHolder.btn2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "Button 2 Clicked",
							Toast.LENGTH_SHORT).show();
				}
			});

			viewHolder.btn3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "Button 3 Clicked", Toast.LENGTH_SHORT);
				}
			});
			return row;
		}

		@Override
		public Filter getFilter() {
			if (msgFilter == null) {
				msgFilter = new MsgFilter();
			}
			return msgFilter;
		}

        @Override
        public int getCount() {
            return msgFiltered.size();
        }
        		
		private class MsgFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String filterString = constraint.toString().toLowerCase();
				FilterResults results = new FilterResults();
				ArrayList<MessagesRecord> list;

				// if (D) Log.d(TAG, "filter1");
				if (constraint == null || constraint.length() == 0) {
					results.values = msgArray;
					results.count = msgArray.size();
				} else {
					if (preConstraint == null || preConstraint.length() == 0
							|| preConstraint.length() > constraint.length())
						list = msgArray;
					else
						list = msgFiltered;
					if (msgArray.size() > 0)
						preConstraint = constraint.toString();
					List<MessagesRecord> nEntList = new ArrayList<MessagesRecord>();
					for (MessagesRecord msg : list) {
						if (msg.msg != null
								&& msg.msg.toLowerCase().contains(filterString)
								|| (msg.subj != null && msg.subj.toLowerCase()
										.contains(filterString)))
							nEntList.add(msg);
					}
					// if (D) Log.d(TAG, "filter2");
					results.values = nEntList;
					results.count = nEntList.size();
				}
				// if (D) Log.d(TAG, "filter exit");
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				msgFiltered = (ArrayList<MessagesRecord>) results.values;
				notifyDataSetChanged();
				if (D)
					Log.d(TAG, "filter3");
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messages);
		mContext = this;
		captionView = (TextView) findViewById(R.id.title_text);
		// dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		selfEmpId = dbSch.getUserId();
		Bundle b = getIntent().getExtras();
		empId = b.getString("id_emp");
		mEmpId = Long.parseLong(empId);
		empNm = b.getString("empNm");
		mDBArrayAdapter = new CustomMsgAdapter(this, R.layout.msg_row, selfEmpId);
		mDBListView = (ListView) findViewById(R.id.listViewMessages);
		mDBListView.setAdapter(mDBArrayAdapter);
		mDBListView.setOnItemClickListener(mMessagesClickListener);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			mDBListView.setMultiChoiceModeListener(new ModeCallback());
		}
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		textFilter = (EditText) findViewById(R.id.editTextFilter);
		textFilter.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				MessagesActivity.this.mDBArrayAdapter.getFilter().filter(cs,
						new Filter.FilterListener() {
							public void onFilterComplete(int count) {
								showCounter(count);
							}
						});
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		refreshMessagesList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messages, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_del_msg);
		if (mi != null) {
			mi.setEnabled(false);
			mi.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			if (D)
				Log.d(TAG, "onOptions Search");
			return true;
		case R.id.action_new_msg:
			if (D)
				Log.d(TAG, "onOptions action_new_msg");
			Intent msgIntent = new Intent(this, ComposeNewMsgActivity.class);
			Bundle b = new Bundle();
			b.putString("selfEmpId", Long.toString(selfEmpId));
			b.putString("id_recipient", empId);
			b.putString("recipient", empNm);
			b.putString("subj", "");
			b.putString("msg", "");
			msgIntent.putExtras(b);
			startActivityForResult(msgIntent, COMPOSE_MSG);
			return true;
		case R.id.action_del_msg:
			if (D)
				Log.d(TAG, "onOptions action_del_msg");
			deleteChecked();
			return true;
		case R.id.action_warr_online:
			if (D)
				Log.d(TAG, "onOptions action_warr_online");
			CommonClass.openOnlineWarrants(mContext);
			return true;
		case R.id.action_mark_msg_as_read:
			if (D)
				Log.d(TAG, "onOptions action_mark_msg_as_read");
			markRead();
			return true;
		}
		return false;
	}

	public void onClickUndo(View view) {
		Toast.makeText(this, "Deletion undone", Toast.LENGTH_LONG).show();
		viewContainer.setVisibility(View.GONE);
	}
	private OnItemClickListener mMessagesClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			showMessage(arg2);
		}
	};

	private void showMessage(int itemPosition) {
		MessagesRecord msg;
		msg = mDBArrayAdapter.getItem(itemPosition);
		if (D)
			Log.d(TAG, "id=" + msg.id);
		Intent intent = new Intent(mContext, MessageActivity.class);
		Bundle b = new Bundle();
		b.putString("id", Long.toString(msg.id));
		b.putString("id_ext", Long.toString(msg.id_external));
		b.putString("id_sender", Long.toString(msg.id_sender));
		b.putString("sender", msg.senderName);
		b.putString("date", msg.getDate());
		b.putString("subj", msg.subj);
		b.putString("msg", msg.msg);
		b.putString("id_state", Integer.toString(msg.id_state));
		intent.putExtras(b);
		startActivityForResult(intent, MSG_SHOW);
        String d = String.valueOf(msg.getDate());
        Toast.makeText(this, d,Toast.LENGTH_LONG).show();
	}

	private void deleteItem(int itemPosition) {
		MessagesRecord item = mDBArrayAdapter.getItem(itemPosition);
		if (dbSch.removeMsg(item.id, item.id_external)) {
			mDBArrayAdapter.remove(item);
			showCounter(mDBArrayAdapter.getCount());
		}
	}

	private void deleteChecked() {
		boolean needUpdate = false;
		for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
			MessagesRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				if (dbSch.removeMsg(item.id, item.id_external))
					needUpdate = true;
			}
		}
		if (needUpdate) {
			refreshMessagesList();
			updated = true;
		}
	}

	private void markRead() {
		boolean needUpdate = false;
		for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
			MessagesRecord item = mDBArrayAdapter.getItem(i);
			if (item.id_state == MessagesRecord.MSG_NEW) {
				if (dbSch.setMsgRead(item.id))
					needUpdate = true;
			}
		}
		if (needUpdate) {
			refreshMessagesList();
			updated = true;
		}
	}

	private void refreshMessagesList() {
		empWhere = "";
		if (empId != null) {
			if (mEmpId == selfEmpId) {
				empWhere = MessagesTable.ID_SENDER + " = " + empId + " AND "
						+ MessagesTable.ID_RECIPIENT + " = " + empId;
			} else {
				empWhere = MessagesTable.ID_SENDER + " = " + empId + " OR "
						+ MessagesTable.ID_RECIPIENT + " = " + empId;
			}
		}
		if (empWhere != "")
			empWhere = "(" + empWhere + ") AND ";
		empWhere += MessagesTable.REC_STAT
				+ DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		showMessages(empWhere);
	}

	@Override
	public void onFinishYesNoDialog(boolean state) {
		// TODO Auto-generated method stub

	}

	// ===Input Name Dialog===
	private void showInputNameDialog() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
		inputNameDialog.setCancelable(false);
		inputNameDialog.setDialogTitle("Написать сообщение");
		inputNameDialog.show(fragmentManager, "input dialog");
	}

	@Override
	public void onFinishInputDialog(String inputText) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Returned from dialog: " + inputText,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	private void showMessages(String sWhere) {
		SQLiteDatabase sqdb = dbSch.getWritableDatabase();
		Cursor c = null;
		Cursor cEmp;
		MessagesRecord msg;
		EmpRecord empRecord;
		String query;
		if (sWhere.length() == 0) {
			query = "SELECT * FROM " + MessagesTable.TABLE_NAME + " ORDER BY "
					+ MessagesTable.ID + " DESC";
		} else {
			query = "SELECT * FROM " + MessagesTable.TABLE_NAME + " WHERE "
					+ sWhere + " ORDER BY " + MessagesTable.ID + " DESC";
		}
		int cntr = 0;
		try {
			c = sqdb.rawQuery(query, null);
			mDBListView.setAdapter(null);
			mDBArrayAdapter.clear();			
			while (c.moveToNext()) {
				cntr++;
				msg = new MessagesRecord(c);
				cEmp = dbSch.getEmpName(msg.id_sender);
				if (cEmp != null) {
					empRecord = new EmpRecord(cEmp);
					msg.senderName = empRecord.toString();
					msg.senderPhoto = empRecord.photo;
					cEmp.close();
				}
				cEmp = dbSch.getEmpName(msg.id_recipient);
				if (cEmp != null) {
					empRecord = new EmpRecord(cEmp);
					msg.recipientName = empRecord.toString();
					msg.recipientPhoto = empRecord.photo;
					cEmp.close();
				}
				if (D)
					Log.d(TAG, "Msg = " + msg.msg);
				mDBArrayAdapter.add(msg);
			}
		} catch (Exception e) {
			if (D)
				Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
			if (D)
				Log.d(TAG, "Msgs Count = " + cntr);
	        String filter = textFilter.getText().toString(); 
	        if (D) Log.d(TAG, "filter = " + filter);
	        if (filter != null && filter.length() >= 0) {
	        	MessagesActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
            	    public void onFilterComplete(int count) {
            	         showCounter(count);
            	    }
            	});	        	
	        } else {
	        }
	        mDBListView.setAdapter(mDBArrayAdapter);
			if (c != null)
				c.close();
		}
	}

	private void showCounter(int cntr) {
		captionView.setText(empNm + "; " + getString(R.string.action_msg_cntrs)
				+ cntr);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case MSG_SHOW:
				if (data.hasExtra("updated")) {
					int updated = Integer.parseInt(data.getExtras().getString(
							"updated"));
					if (updated > 0)
						showMessages(empWhere);
				}
				break;
			case COMPOSE_MSG:
				if (data.hasExtra("id")) {
					long id = Long.parseLong(data.getExtras().getString("id"));
					if (id > 0)
						showMessages(empWhere);
				}
				break;
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	private class ModeCallback implements ListView.MultiChoiceModeListener {
		MenuItem miDelete;

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.messages_rowselection, menu);
			mode.setTitle(mContext.getString(R.string.selected));
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			miDelete = menu.findItem(R.id.action_delete_msg);
			updateMenuItems();
			return true;
		}

		private void updateMenuItems() {
			int msgState = getSelectedMsgState();
			if (miDelete != null) {
				miDelete.setEnabled(msgState == MessagesRecord.MSG_READ);
				miDelete.setVisible(msgState == MessagesRecord.MSG_READ);
			}
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_delete_msg:
				deleteSelected();
				// the Action was executed, close the CAB
				mode.finish();
				return true;
			case R.id.action_mark_as_read:
				setMsgReadState();
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		public void onDestroyActionMode(ActionMode mode) {
			mDBArrayAdapter.clearSelection();
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			final int checkedCount = mDBListView.getCheckedItemCount();
			if (checked)
				mDBArrayAdapter.setNewSelection(position, checked);
			else
				mDBArrayAdapter.removeSelection(position);

			switch (checkedCount) {
			case 0:
				mode.setSubtitle(null);
				break;
			default:
				mode.setSubtitle("" + checkedCount);
				break;
			}
			updateMenuItems();
		}

		private int getSelectedMsgState() {
			int res = MessagesRecord.MSG_NEW;
			for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
				if (mDBArrayAdapter.isPositionChecked(i)) {
					MessagesRecord item = mDBArrayAdapter.getItem(i);
					if (item.id_sender == selfEmpId)
						res = MessagesRecord.MSG_READ;
					else
						res = item.id_state;
				}
			}
			return res;
		}

		private void setMsgReadState() {
			boolean needUpdate = false;
			for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
				if (mDBArrayAdapter.isPositionChecked(i)) {
					MessagesRecord item = mDBArrayAdapter.getItem(i);
					if (item.id_state == MessagesRecord.MSG_NEW
							&& item.id_recipient == selfEmpId) {
						if (dbSch.setMsgRead(item.id)) {
							item.id_state = MessagesRecord.MSG_READ;
							item.modified = 1;
							needUpdate = true;
						}
					}
				}
			}
			if (needUpdate) {
				mDBArrayAdapter.clearSelection();
				mDBArrayAdapter.notifyDataSetChanged();
				updated = true;
			}
		}

		private void deleteSelected() {
			boolean needUpdate = false;
			for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
				if (mDBArrayAdapter.isPositionChecked(i)) {
					MessagesRecord item = mDBArrayAdapter.getItem(i);
					if (item.id_state == MessagesRecord.MSG_READ || item.id_sender == selfEmpId) {
						if (dbSch.removeMsg(item.id, item.id_external)) {
							needUpdate = true;
							mDBArrayAdapter.remove(item);
						}
					}
				}
			}
			mDBArrayAdapter.clearSelection();
			mDBArrayAdapter.notifyDataSetChanged();
			refreshMessagesList();
			invalidateOptionsMenu();
//			showCounter(mDBArrayAdapter.getCount());
		}

	}

	@Override
	public void onRefresh() {
		refreshMessagesList();
		swipeLayout.setRefreshing(false);
	}

}
