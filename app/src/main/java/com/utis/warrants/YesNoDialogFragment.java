package com.utis.warrants;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class YesNoDialogFragment extends DialogFragment {
	TextView txtName;
	Button btnYes, btnNo;
	static String dialogTitle;
	static String dialogText;
	
	//---Interface containing methods to be implemented by calling activity---
	public interface YesNoDialogListener {
		void onFinishYesNoDialog(boolean state);
	}
	
	public YesNoDialogFragment() {
		//---empty constructor required---
	}
	
	//---set the title of the dialog window---
	public void setDialogTitle(String title) {
		dialogTitle = title;
	}
	
	public void setDialogText(String text) {
		dialogText = text;
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_yes_no_dialog, container);
		//---get the Button views--- 
		txtName = (TextView) view.findViewById(R.id.txtAsk);
		txtName.setText(dialogText);
		btnYes = (Button) view.findViewById(R.id.btnYes);
		btnNo = (Button) view.findViewById(R.id.btnNo);
		btnYes.setOnClickListener(btnListener);
		btnNo.setOnClickListener(btnListener);
		//---set the title for the dialog---
		getDialog().setTitle(dialogTitle);
		return view;
	}
	
	//---create an anonymous class to act as a button click	listener---
	private OnClickListener btnListener = new OnClickListener()	{
		public void onClick(View v)	{
			//---gets the calling activity---
			YesNoDialogListener activity = (YesNoDialogListener) getActivity(); 
			boolean state = ((Button) v).getText().toString().equals(getString(R.string.btn_yes)) ? true : false;
			activity.onFinishYesNoDialog(state);
			//---dismiss the alert---
			dismiss(); 
		}
	};
}
