package com.utis.warrants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.SpecsTable;

public class BarScanActivity extends Activity{
	private static final int SCAN_BARCODE = 3;
	private static final boolean D = true;
	private static final String TAG = "BarScanActivity";
	public Context mContext;
    Button btnUpdate, btnSave;
	private String scanContents = "";
	private String scanFormat = "";
    TextView tvTitle1, tvTitle2;
    private DBSchemaHelper dbSch;
    SpecsRecord specs;
    ConnectionManagerService connectionService;
    private ArrayAdapter<SpecsRecord> mDBArrayAdapter;
    private ArrayAdapter<String> mDBArrayAdapter2;
    private GoodsRecord jobGroup;
    private ListView listName, listSRL;
    String str, su, entId, warrNumDate, s, fnm, strSrl;
    String warrantId, warrIdContract,warrantNum, warrEntAddr , warrEntNm ,warrEntId, warrRemark, warrContragentInfo, subEntName;
    private Long idWarr;
    LinearLayout linearList, linearName, linearSRL, linearINV, linearFisk, linearListSRL;
    EditText etName, etScancode, etSRL, etFisknum;
    Long in, barcode;
    int gds, srlNum, idName;
    static int count = 0;
    boolean boolUpdate = false, boolName = false;
    Drawable originalDrawable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_bar_scan);

        dbSch = DBSchemaHelper.getInstance(this);
        mContext = this;
        count =0;
        boolUpdate = false;
        boolName = false;
        tvTitle1 = (TextView) findViewById(R.id.tvTitle1);
        tvTitle2 = (TextView) findViewById(R.id.tvTitle2);
        linearList = (LinearLayout) findViewById(R.id.linearList);
        linearName = (LinearLayout) findViewById(R.id.linearName);
        linearSRL = (LinearLayout) findViewById(R.id.linearSRL);
        linearINV = (LinearLayout) findViewById(R.id.linearINV);
        linearFisk = (LinearLayout) findViewById(R.id.linearFisk);
        linearListSRL = (LinearLayout) findViewById(R.id.linearListSRL);
        etName = (EditText)findViewById(R.id.etName);
        etScancode = (EditText)findViewById(R.id.etScancode);
        etSRL = (EditText)findViewById(R.id.etSRL);
        etFisknum = (EditText)findViewById(R.id.etFisknum);
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnSave = (Button)findViewById(R.id.btnSave);
        mDBArrayAdapter = new ArrayAdapter<SpecsRecord>(this, R.layout.db_data);
        mDBArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.db_data);

        listName = (ListView) findViewById(R.id.listName);
        listName.setAdapter(mDBArrayAdapter);
        listName.setOnItemClickListener(clickListener);
        listSRL = (ListView) findViewById(R.id.listSRL);
        listSRL.setAdapter(mDBArrayAdapter2);
        listSRL.setOnItemClickListener(clickListenerSRL);


        Bundle b = getIntent().getExtras();
        warrantId = b.getString("id");
        idWarr = Long.parseLong(warrantId);
        warrantNum = b.getString("num");
        warrEntAddr = b.getString("ent_addr");
        warrEntNm = b.getString("ent_name");
        warrIdContract = b.getString("id_contract");
        warrRemark = b.getString("rem");
        warrEntId = b.getString("id_ent");
        warrContragentInfo = b.getString("contragent");
        subEntName = b.getString("sub_ent_nume");
        warrNumDate = b.getString("w_date");

        tvTitle1.setText(warrEntNm+";   "+subEntName );
        tvTitle2.setText(warrEntAddr);




        entId = String.valueOf(warrEntId);
        {
            etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        String strNm = etName.getText().toString();
                        showNameSpecsOne(dbSch, entId, strNm);
                        if (count > 1 && strNm.equals("")) {
                            linearList.setVisibility(View.VISIBLE);
                            linearName.setVisibility(View.GONE);
                            showNameListSpecs(dbSch, entId, strNm);
                            Drawable(5);
                            linearName.setVisibility(View.VISIBLE);
                        }else if (count> 1){
                            etName.setText(specs.eq_name);
                            etFisknum.setText(null);
                            etScancode.setText(null);
                            linearSRL.setVisibility(View.GONE);
                            linearListSRL.setVisibility(View.VISIBLE);
                            showListSRl (dbSch, srlNum);
                        } else if (count == 1){
                            toastShow("ТМЦ найден");
                            boolName = true;
                        }else {
                            linearList.setVisibility(View.GONE);
                            linearName.setVisibility(View.VISIBLE);
                            toastShow("ТМЦ неверен");
                        }
                        count = 0;
                    }
                    return false;
                }
            });
        }

        etScancode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    etName.setText(null);
                    su = etScancode.getText().toString();
                    if(su.equals("")){
                        toastShow("Инвентарный номер не указан");
                    }else {
                        in = Long.parseLong(su);
                        showSPECSGroups(dbSch, in);
                        showJobGroups(dbSch, gds);
                    }
                }
                return false;}
        });
        etSRL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                  String  strSRL = etSRL.getText().toString();
                    if(strSRL.equals("")){
                        toastShow("Серийный номер не указан");
                    }else {
                        showSPECSGroupsSRL(dbSch, strSRL);
                        showJobGroups(dbSch, gds);
                    }
                }
                return false;}});
        etFisknum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String  strFisk = etFisknum.getText().toString();
                    if(strFisk.equals("")){
                        toastShow("Фискальный номер не указан");
                    }else {
                        showSPECSGroupsFisk(dbSch, strFisk);
                        showJobGroups(dbSch, gds);
                    }
                }
                return false;}
        });

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bar_scan, menu);
		return true;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_barcode:
                if(boolUpdate == false){
                //etName.setText(null);
               // etSRL.setText(null);
                etScancode.setText(null);                                                         //-------------
                etFisknum.setText(null);
                etName.setFocusableInTouchMode(true);
                etSRL.setFocusableInTouchMode(true);
                etFisknum.setFocusableInTouchMode(true);
                btnUpdate.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                if (D) Log.d(TAG, "clicked on action_start_scan");
                startScan();
                }else {
                    etName.setFocusableInTouchMode(true);
                    etSRL.setFocusableInTouchMode(true);
                    etFisknum.setFocusableInTouchMode(true);
                    btnUpdate.setVisibility(View.GONE);//-------------------------------------------------
                    btnSave.setVisibility(View.VISIBLE);//------------------------------------------------
                    if (D) Log.d(TAG, "clicked on action_start_scan");
                    startScan();
                }

                return true;
        }
        return false;
    }

    //----- SCAN Activity -----
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_BARCODE) {
            if (resultCode == RESULT_OK) {

                scanContents = intent.getStringExtra("SCAN_RESULT");
                scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (D) Log.d(TAG, "scan successful = "+ scanContents);
                barcode = Long.parseLong(scanContents);
                etScancode.setText(String.valueOf(scanContents));
                    showSPECSGroups(dbSch, barcode);
                    showJobGroups(dbSch, gds);
                    passResult();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            	if (D) Log.d(TAG, "scan failed");
            }
        }
    }

    private void startScan() {
		if (D) Log.d(TAG, "start scan");
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    	intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "PRODUCT_MODE");
    	startActivityForResult(intent, SCAN_BARCODE);
    }
    private void passResult() {
    	Intent returnIntent = new Intent();
    	Bundle b = new Bundle();
    	if (D) Log.d(TAG, "scan pass = "+ scanContents);
		b.putString("SCAN_RESULT", scanContents);
		b.putString("SCAN_RESULT_FORMAT", scanFormat);
		returnIntent.putExtras(b);
		setResult(RESULT_OK, returnIntent);
    }

    @Override
	public void onBackPressed() {
//		passResult();
		if (D) Log.d(TAG, "passResult");
	    super.onBackPressed();
	}

    //----- Find BARCODE -----
    private void showSPECSGroups(DBSchemaHelper sh, Long barcode) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        String querySelect = "SELECT s.*, ";
        String q_eq = "(SELECT g."+ SpecsTable.ID_EXTERNAL +" FROM "+ SpecsTable.TABLE_NAME +
                " g " +")as "+ SpecsRecord.SPECS_EQ_TYPE;
        String query;
        //--------- if query id index ------------
        query = querySelect + q_eq +" FROM " + SpecsTable.TABLE_NAME + " s "+ "WHERE " +"s."+ SpecsTable.INV +
                " LIKE '%"+barcode +"%' ORDER BY s." + SpecsTable.ID_GDS;
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                etSRL.setText(String.valueOf(specs.srl));
                etScancode.setText(String.valueOf(specs.inv));
                etFisknum.setText(specs.fisk_num);
                str = String.valueOf(barcode);
                gds = specs.id_gds;

                cntr++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Count specs = " + cntr);
            if (c != null) c.close();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }
    //----- Name Equipment -----
    private void showJobGroups(DBSchemaHelper sh, int d_gds) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c;
        String q_eq = "(SELECT g."+ GoodsTable.ID_EXTERNAL +" FROM "+ GoodsTable.TABLE_NAME +
                " g " +")as "+ GoodsRecord.GDS_CHILD_CNT;
        String query;
        query = "SELECT d.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " d "+ "WHERE " +"d."+
                GoodsTable.ID_EXTERNAL + " = "+d_gds + " ORDER BY d." + GoodsTable.NM + " ASC";
        c = sqdb.rawQuery(query, null);

        int cntr = 0;

        try {
            while (c.moveToNext()) {
                jobGroup = new GoodsRecord(c);
                fnm = String.valueOf(jobGroup.fnm);
                if(fnm.equals("")){
                }
                else{
                etName.setText(fnm);}
                s = fnm;
                idName = specs.id;
                cntr++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Count name = " + cntr);
                            if(idName>0) {
                    boolName = true;
                            }
            if(cntr == 0){
                toastShow("Нет совпадений");
                if(idName>0){
                btnSave.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.GONE);
                } else{
                    btnSave.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.VISIBLE);
                }
            }else

            if (cntr > 1){
                toastShow("Больше одного совпадения");
                etName.setText(null);
                etSRL.setText(null);
                //etScancode.setText(null);
                etFisknum.setText(null);
            } else {
                btnUpdate.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                Drawable(1);
                boolUpdate = false;
            }
            gds = 0;
            if (c != null) c.close();
        }
    }
    //----- Find FISK -----
    private void showSPECSGroupsFisk(DBSchemaHelper sh, String fisk) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        String querySelect = "SELECT s.*, ";
        String q_eq = "(SELECT g."+ SpecsTable.ID_EXTERNAL +" FROM "+ SpecsTable.TABLE_NAME +
                " g " +")as "+ SpecsRecord.SPECS_EQ_TYPE;
        String query;
        //--------- if query id index ------------
        query = querySelect + q_eq  +" FROM " + SpecsTable.TABLE_NAME + " s "+ "WHERE " +"s."+ SpecsTable.FISK_NUM +
                " LIKE '%"+ fisk + "%' ORDER BY s." + SpecsTable.ID_GDS;
        Log.d(TAG, query);
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                etFisknum.setText(specs.fisk_num);
                etSRL.setText(String.valueOf(specs.srl));
                etScancode.setText(String.valueOf(specs.inv));

                str = String.valueOf(fisk);
                gds = specs.id_gds;
                cntr++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Count = " + cntr);
            equipCount(cntr);
            if (c != null) c.close();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }
    //----- Find SRL -----
    private void showSPECSGroupsSRL(DBSchemaHelper sh, String srlNum) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;

        String querySelect = "SELECT s.*, ";
        String q_eq = "(SELECT g."+ SpecsTable.ID_EXTERNAL +" FROM "+ SpecsTable.TABLE_NAME +
                " g " +")as "+ SpecsRecord.SPECS_EQ_TYPE;
        String query;
        //--------- if query id index ------------
        query = querySelect + q_eq  +" FROM " + SpecsTable.TABLE_NAME + " s "+ "WHERE " +"s."+ SpecsTable.SRL +
                " LIKE '%"+ srlNum + "%' ORDER BY s." + SpecsTable.ID_GDS;
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                etSRL.setText(String.valueOf(specs.srl));
                String strScan = String.valueOf(specs.inv);
                    etScancode.setText(String.valueOf(specs.inv));
                etFisknum.setText(specs.fisk_num);

                str = String.valueOf(srlNum);
                gds = specs.id_gds;
                cntr++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Count = " + cntr);
            equipCount(cntr);
            if (c != null) c.close();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }

    private void showListSRl (DBSchemaHelper sh, int f){
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        String querySelect = "SELECT s.*, ";
        String q_eq = "(SELECT g."+ SpecsTable.ID_EXTERNAL +" FROM "+ SpecsTable.TABLE_NAME +
                " g " +")as "+ SpecsRecord.SPECS_EQ_TYPE;
        String query;
        //--------- if query id index ------------
        query = querySelect + q_eq  +" FROM " + SpecsTable.TABLE_NAME + " s "+ "WHERE " +"s."+ SpecsTable.ID_GDS +
                " = "+ f;
        Log.d(TAG, query);
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter2.clear();
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                idName = specs.id;
                mDBArrayAdapter2.add(specs.srl);

            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "idName = " + String.valueOf(idName));
            if (c != null) c.close();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }

    //----- Name Search where one match -----
    private void showNameSpecsOne(DBSchemaHelper sh, String ent, String name) {
        //select s.*,(select g.fnm from d_gds g where g.id = s.id_d_eqpm) as ttt from d_specs s where s.id_d_ent = 5493;

        String q_eq = "(SELECT g."+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
                " g WHERE "+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ SpecsRecord.SPECS_EQ_NM;
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;

        String query;
        query = "SELECT s.*,"+ q_eq + " from " + SpecsTable.TABLE_NAME + " s WHERE s."+ SpecsTable.ID_ENT + " = " + ent +
                " AND " + SpecsRecord.SPECS_EQ_NM + " LIKE '%" + name + "%'" ;
        int cntr1 = 0;
        count = 0;
        try {
            c = sqdb.rawQuery(query, null);

            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                Drawable(5);
                etName.setText(specs.eq_name);
                etFisknum.setText(specs.fisk_num);
                etSRL.setText(String.valueOf(specs.srl));
                    etScancode.setText(String.valueOf(specs.inv));
                idName = specs.id;
                srlNum = specs.id_gds;
                count++;
                cntr1++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if(cntr1 == 1 || idName>0){
                boolName = true;
            } else {
                boolName = false; }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }
    private void showNameListSpecs(DBSchemaHelper sh, String ent, String name) {
        //select s.*,(select g.fnm from d_gds g where g.id = s.id_d_eqpm) as ttt from d_specs s where s.id_d_ent = 5493;
        //SELECT  * FROM ( SELECT (SELECT g.fnm FROM d_gds g WHERE id = s.id_d_eqpm)as SPECS_EQ_NM from d_specs s WHERE s.id_d_ent = 12898)group by SPECS_EQ_NM;
        Cursor c = null;
        SQLiteDatabase sqdb = sh.getWritableDatabase();


        String q_eq = "(SELECT g."+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
                " g WHERE "+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ SpecsRecord.SPECS_EQ_NM;
        String query;
        query = "SELECT s.*,"+ q_eq + " from " + SpecsTable.TABLE_NAME + " s WHERE s."+ SpecsTable.ID_ENT + " = " + ent +
                " AND " + SpecsRecord.SPECS_EQ_NM + " LIKE '%" + name + "%'" ;
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                srlNum = specs.id_gds;
                mDBArrayAdapter.add(specs);
                cntr++;

            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if(count>1){
                etScancode.setText(null);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }

    //----- ListOnClickListeners -----
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
            specs = mDBArrayAdapter.getItem(position);
            String strNm = specs.eq_name;
            showNameSpecsOne(dbSch, entId, strNm);
            if (count > 1) {
                linearList.setVisibility(View.VISIBLE);
                linearName.setVisibility(View.GONE);
                showNameListSpecs(dbSch, entId, strNm);
                Drawable(5);
                linearName.setVisibility(View.VISIBLE);
                linearList.setVisibility(View.GONE);
                etName.setText(strNm);
                linearListSRL.setVisibility(View.VISIBLE);
                showListSRl (dbSch, srlNum);
                boolName =  false;
            } else if (count ==1) {
                toastShow("ТМЦ найден");
                linearList.setVisibility(View.GONE);
                linearName.setVisibility(View.VISIBLE);
            } else {
                linearList.setVisibility(View.GONE);
                linearName.setVisibility(View.VISIBLE);
                toastShow("Нет совпадений");
            }
            count = 0;
        }
    };

    AdapterView.OnItemClickListener clickListenerSRL = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
            specs.srl = mDBArrayAdapter2.getItem(position);
            String  gdsSRL = specs.srl;
            strSrl = String.valueOf(specs.srl);
            showSPECSGroupsSRL(dbSch, strSrl);
            showJobGroups(dbSch, gds);
            boolName =  true;
            linearListSRL.setVisibility(View.GONE);
            linearSRL.setVisibility(View.VISIBLE);
        }
    };

    //----- Buttons -----
    public void  methodBtnUpdate(View view){
        boolUpdate = true;
        if(boolName == true ){
        etName.setFocusable(false);
        etSRL.setFocusable(false);
        etFisknum.setFocusable(false);
            Drawable(3);
            toastShow("Введите инвентарный номер");
            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            linearListSRL.setVisibility(View.GONE);
        }else{
            toastShow("Введите ТМЦ");
        }
    }
    public void  methodBtnSave(View view){
        String barcode = etScancode.getText().toString();
       if (barcode == null){
           etScancode.setError("Поле не заполнино");
           toastShow("Заполните инвентарный номер");
           barcode = etScancode.getText().toString();
       }else{
           barcode = etScancode.getText().toString();

            dbSch.updateSpecInventoryNum(idName, barcode);

           toastShow("Сохранение");
           Drawable(4);
           Drawable(1);
       }
        btnUpdate.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);
        boolUpdate = false;
    }

    public void backToUpdateForm(View view){

        if (boolUpdate == true){
        boolUpdate = false;
            Drawable(4);
        btnUpdate.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);
            Drawable(1);
        } else {
            Drawable(5);
            etScancode.setText(null);
            Drawable(1);
            linearList.setVisibility(View.GONE);
            linearName.setVisibility(View.VISIBLE);
            linearSRL.setVisibility(View.VISIBLE);
            linearListSRL.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
            boolUpdate = false;
            boolName = false;
            idName = 0;
        }
    }
    public void toastShow(String str){
        Toast toast =  Toast.makeText(BarScanActivity.this, str, Toast.LENGTH_LONG);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(22);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public void equipCount(int i){
        if(i == 0){
            toastShow("Нет совпадений");
        }else if (i > 1){
            toastShow("Больше одного совпадения");
            etName.setText(null);
            etSRL.setText(null);
            //etScancode.setText(null);
            etFisknum.setText(null);
        } else {
            btnUpdate.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            Drawable(1);
            boolUpdate = false;
        }
    }
    public void Drawable(int i){
        if(i == 1){
            linearName.setBackgroundResource(R.drawable.gradient_bg_done_lt);
            linearFisk.setBackgroundResource(R.drawable.gradient_bg_done_lt);
            linearSRL.setBackgroundResource(R.drawable.gradient_bg_done_lt);
        }else if(i == 2){
            linearName.setBackgroundDrawable(originalDrawable);
            linearFisk.setBackgroundDrawable(originalDrawable);
            linearSRL.setBackgroundDrawable(originalDrawable);
        }else if(i == 3){
            linearName.setBackgroundResource(R.drawable.gradient_bg_et);
            linearFisk.setBackgroundResource(R.drawable.gradient_bg_et);
            linearSRL.setBackgroundResource(R.drawable.gradient_bg_et);
        } else if(i == 4){
            etName.setFocusableInTouchMode(true);
            etSRL.setFocusableInTouchMode(true);
            etFisknum.setFocusableInTouchMode(true);
        } else if (i ==5){
            etName.setText(null);
            etSRL.setText(null);
           // etScancode.setText(null);
            etFisknum.setText(null);
        }
    }
}