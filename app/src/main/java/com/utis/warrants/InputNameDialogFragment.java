package com.utis.warrants;

import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Patterns;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputNameDialogFragment extends DialogFragment {
	EditText txtName; 
	TextView txtComment;
	Button btn;
	static String dialogTitle;
	static String dialogText;
	static String dialogComment = null;
	static int textInputType = InputType.TYPE_CLASS_TEXT;
	static boolean setIpFilter = false;

	//---Interface containing methods to be implemented by calling activity---
	public interface InputNameDialogListener {
		void onFinishInputDialog(String inputText);
	}
	
	public InputNameDialogFragment() {
		//---empty constructor required---
	}

	//---set the title of the dialog window---
	public void setDialogTitle(String title) {
		dialogTitle = title;
	}
	
	public void setDialogText(String text) {
		dialogText = text;
	}
	
	public void setDialogComment(String text) {
		dialogComment = text;
	}
	
	public void setTextInputType(int textType) {
		textInputType = textType;
	}
	
	public void setIPAddrFilter(boolean setFilter) {
		setIpFilter = setFilter;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inputname_dialog, container);
		//---get the EditText and Button views---
		txtName = (EditText) view.findViewById(R.id.txtName);
		txtName.setText(dialogText);
		txtName.setInputType(textInputType);
		txtComment = (TextView) view.findViewById(R.id.txtComment);
		if (dialogComment != null)
			txtComment.setText(dialogComment);
		if (setIpFilter) {
//			txtName.addTextChangedListener(new TextWatcher() {                       
//			    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}            
//			    @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}            
//
//			    private String mPreviousText = "";          
//			    @Override
//			    public void afterTextChanged(Editable s) {          
//			        if( Patterns.IP_ADDRESS.matcher(s).matches()) {
//			            mPreviousText = s.toString();
//			        } else {
//			            s.replace(0, s.length(), mPreviousText);
//			        }
//			    }
//			});
			InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start,
                        int end, Spanned dest, int dstart, int dend) {
                    if (end > start) {
                        String destTxt = dest.toString();
                        String resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) +
                        destTxt.substring(dend);
                        if (!resultingTxt.matches ("^\\d{1,3}(\\." +
                                "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) { 
                            return "";
                        } else {
                            String[] splits = resultingTxt.split("\\.");
                            for (int i = 0; i < splits.length; i++) {
                                if (Integer.valueOf(splits[i]) > 255) {
                                    return "";
                                }
                            }
                        }
                    }
                return null;
                }
            };
            txtName.setFilters(filters);
		} else {
			txtName.setFilters(new InputFilter[] {});
		}
		btn = (Button) view.findViewById(R.id.btnDone);
		//---event handler for the button---
		btn.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View view) {
				//---gets the calling activity---
				InputNameDialogListener activity = (InputNameDialogListener) getActivity();
				activity.onFinishInputDialog(txtName.getText().toString());
				//---dismiss the alert---
				dismiss(); 
			}
		}); 
		
		//---show the keyboard automatically---txtName.requestFocus();
		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		//---set the title for the dialog---
		getDialog().setTitle(dialogTitle);
		return view;
	}
	
}
