package com.utis.warrants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.SpecsTable;
import com.utis.warrants.tables.StateTable;
import com.utis.warrants.tables.WarrantTable;

import java.util.ArrayList;
import java.util.List;

public class BarScanWarrantsActivity extends Activity {
    private static final int SCAN_BARCODE = 3;
    private static final int WARR_CONT_SHOW = 1;
    private static final boolean D = true;
    private static final String TAG = "BarScanWarrantsActivity";
    public Context mContext;
    private String scanContents = "";
    private String scanFormat = "";
    private DBSchemaHelper dbSch;
    SpecsRecord specs;
    ConnectionManagerService connectionService;

    private ArrayAdapter<SpecsRecord> specsRecordArrayAdapter;
    private ArrayAdapter<String> mDBArrayAdapterSRL;
    private ArrayAdapter<WarrantRecord> mDBArrayAdapterWarr;
    private GoodsRecord jobGroup;
    WarrantRecord warrant;
    private ListView  listSRL, listWarrants ;
    String str, su, entId, warrNumDate, s, fnm, strSrl;
    String warrEntId;
    private Long idWarr;
    LinearLayout linearName, linearSRL, linearINV, linearFisk, linearListSRL, linearWarr;
    ScrollView scrollView;
    EditText etName, etScancode, etSRL, etFisknum;
    Long in, barcode;
    int gds, srlNum, idName, idEnt;
    static int count = 0;
    int countWarr;
    boolean boolUpdate = false, boolName = false;
    Drawable originalDrawable;
    private ListView mDBListView;
    private CustomWarrantAdapter mDBArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_bar_scan_warrants);

        dbSch = DBSchemaHelper.getInstance(this);
        mDBArrayAdapter = new CustomWarrantAdapter(this, R.layout.warrant_row);
        mDBListView = (ListView) findViewById(R.id.listViewWarrants);
        mDBListView.setAdapter(mDBArrayAdapter);
        mContext = this;
        count =0;
        idEnt = 0;
        boolUpdate = false;
        boolName = false;

        linearName = (LinearLayout) findViewById(R.id.linearName);
        linearSRL = (LinearLayout) findViewById(R.id.linearSRL);
        linearINV = (LinearLayout) findViewById(R.id.linearINV);
        linearFisk = (LinearLayout) findViewById(R.id.linearFisk);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        linearWarr = (LinearLayout) findViewById(R.id.linearWarr);
        linearListSRL = (LinearLayout) findViewById(R.id.linearListSRL);
        etName = (EditText)findViewById(R.id.etName);
        etScancode = (EditText)findViewById(R.id.etScancode);
        etSRL = (EditText)findViewById(R.id.etSRL);
        etFisknum = (EditText)findViewById(R.id.etFisknum);
        specsRecordArrayAdapter = new ArrayAdapter<SpecsRecord>(this, R.layout.db_data);
        mDBArrayAdapterSRL = new ArrayAdapter<String>(this, R.layout.db_data);

        listSRL = (ListView) findViewById(R.id.listSRL);
        listSRL.setAdapter(mDBArrayAdapterSRL);
        listSRL.setOnItemClickListener(clickListenerSRL);
        mDBArrayAdapterWarr = new ArrayAdapter<WarrantRecord>(this, R.layout.db_data);
        mDBArrayAdapter = new CustomWarrantAdapter(this, R.layout.warrant_row2);
        mDBListView = (ListView) findViewById(R.id.listViewWarrants);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mWarrantsClickListener);
        entId = String.valueOf(warrEntId);
        {
            etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        String strNm = etName.getText().toString();
                        showNameSpecsOne(dbSch, strNm);
                        if (strNm.equals("")) {
                            etName.setText(null);
                            toastShow("ТМЦ номер не указан");
                        }else if (count> 1){
                            etName.setText(specs.eq_name);
                            etFisknum.setText(null);
                            etScancode.setText(null);
                            linearSRL.setVisibility(View.GONE);
                            linearListSRL.setVisibility(View.VISIBLE);
                            showListSRl (dbSch, srlNum);
                            if(idEnt>0){
                                showWarrants(dbSch, idEnt);
                            }
                        } else if (count == 1){
                            if(idEnt>0){
                                showWarrants(dbSch, idEnt);
                            }
                            boolName = true;
                        }else {
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
                        if(idEnt>0){
                            showWarrants(dbSch, idEnt);}
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
                        if(idEnt>0){
                            showWarrants(dbSch, idEnt);
                        }
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
                        if(idEnt>0){
                            showWarrants(dbSch, idEnt);
                        }
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
                    etScancode.setText(null);
                    etFisknum.setText(null);
                    etName.setFocusableInTouchMode(true);
                    etSRL.setFocusableInTouchMode(true);
                    etFisknum.setFocusableInTouchMode(true);

                    if (D) Log.d(TAG, "clicked on action_start_scan");
                    startScan();
                }else {
                    etName.setFocusableInTouchMode(true);
                    etSRL.setFocusableInTouchMode(true);
                    etFisknum.setFocusableInTouchMode(true);

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
                if(idEnt>0){
                showWarrants(dbSch, idEnt);}
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
                idEnt = specs.id_ent;

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
                    etName.setText(fnm);
                }
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
                } else{
                }
            }else

            if (cntr > 1){
                toastShow("Больше одного совпадения");
                etName.setText(null);
                etSRL.setText(null);
                //etScancode.setText(null);
                etFisknum.setText(null);
            } else {
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
                idEnt = specs.id_ent;
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
                idEnt = specs.id_ent;
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
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapterSRL.clear();
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);
                idName = specs.id;
                mDBArrayAdapterSRL.add(specs.srl);

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
    private void showNameSpecsOne(DBSchemaHelper sh, String name) {
        //select s.*,(select g.fnm from d_gds g where g.id = s.id_d_eqpm) as ttt from d_specs s where s.id_d_ent = 5493;

        String q_eq = "(SELECT g."+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
                " g WHERE "+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ SpecsRecord.SPECS_EQ_NM;
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;

        String query;
//        query = "SELECT s.*,"+ q_eq + " from " + SpecsTable.TABLE_NAME + " s WHERE s."+ SpecsTable.ID_ENT + " = " + ent +
//                " AND " + SpecsRecord.SPECS_EQ_NM + " LIKE '%" + name + "%'" ;
        query = "SELECT s.*,"+ q_eq + " from " + SpecsTable.TABLE_NAME + " s WHERE " + SpecsRecord.SPECS_EQ_NM + " LIKE '%" + name + "%'" ;
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
                idEnt = specs.id_ent;
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

    //----- Warrants List -----
    private void showWarrants(DBSchemaHelper sh, int subent) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        Cursor cEnt = null;
        String w_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
                " WHERE "+ StateTable.ID_EXTERNAL +" = w."+ WarrantTable.IDSTATE +")as "+
                WarrantRecord.WARR_STATE_NM;

        String markQuery = 	"(SELECT MAX(" + EmpWorkTimeTable.ID +
                ") FROM " + EmpWorkTimeTable.TABLE_NAME +
                " WHERE " + EmpWorkTimeTable.ID_WARRANT + "= w.id_external)AS DTBE";


        String query;
        query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
                " w WHERE "+WarrantTable.ID_SUBENT +" = " + subent +" ORDER BY DTBE DESC," + WarrantTable.ID_EXTERNAL + " DESC";

        countWarr = 0; int colid, scrollPos = 0;
        try {
            scrollPos = mDBListView.getFirstVisiblePosition();
            c = sqdb.rawQuery(query, null);
            mDBListView.setAdapter(null);
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {

                warrant = new WarrantRecord(c);
                cEnt = dbSch.getEntName(warrant.id_ent);
                if (cEnt != null) {
                    colid = cEnt.getColumnIndex(EntTable.NM);
                    warrant.entName = cEnt.getString(colid);
                    if (warrant.id_subent > 0) {
                        cEnt.close();
                        cEnt = dbSch.getEntName(warrant.id_subent);
                        if (cEnt != null) {
                            colid = cEnt.getColumnIndex(EntTable.NM);
                            warrant.subEntName = cEnt.getString(colid);
                        }
                    }
                }
                mDBArrayAdapter.add(warrant);
                countWarr++;
            }
        } catch (Exception e) {

        } finally {

            if (countWarr > 1){
                linearWarr.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
            } else if (countWarr == 1){
                intentAuto();
            }else {
                toastShow("Наряды не найдены");
            }
            String filter = "";
            BarScanWarrantsActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                public void onFilterComplete(int count) {


                }
            });
            mDBListView.setAdapter(mDBArrayAdapter);
        }
    }

    AdapterView.OnItemClickListener clickListenerSRL = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
            specs.srl = mDBArrayAdapterSRL.getItem(position);
            String  gdsSRL = specs.srl;
            strSrl = String.valueOf(specs.srl);
            showSPECSGroupsSRL(dbSch, strSrl);
            showJobGroups(dbSch, gds);
            boolName =  true;
            linearListSRL.setVisibility(View.GONE);
            linearSRL.setVisibility(View.VISIBLE);
        }
    };

    private AdapterView.OnItemClickListener mWarrantsClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            WarrantRecord warrant;
            warrant = mDBArrayAdapter.getItem(arg2);

                Intent intent = new Intent(mContext, WarrContActivity.class);
                Bundle b = new Bundle();
                b.putString("id", Long.toString(warrant.id_external)); //Your id
                b.putString("id_contract", Long.toString(warrant.id_contract));
                b.putString("id_state", Integer.toString(warrant.id_state));
                b.putString("to_sign", Integer.toString((warrant.id_user_sign == 0) ? warrant.to_sign : 0));
                b.putString("local_state", Integer.toString(warrant.localState));
                b.putString("contragent", String.format("%s; %s;\n%s", warrant.entName, warrant.subEntName, warrant.entAddress));
                b.putString("ent_addr", warrant.entAddress);
                b.putString("ent_name", warrant.entName);
                b.putString("ent_lat", Double.toString(warrant.entLat));
                b.putString("ent_lng", Double.toString(warrant.entLng));
                b.putString("num", warrant.getNumDate());
                b.putString("w_num", Integer.toString(warrant.num));
                b.putString("rem", warrant.remark);
                b.putString("w_date", warrant.d_open_str);
                b.putString("sub_ent_nume", warrant.subEntName);
                int ent = warrant.id_subent;
                if (ent == 0) ent = warrant.id_ent;
                b.putString("id_ent", Integer.toString(ent));
                intent.putExtras(b);
                startActivityForResult(intent, WARR_CONT_SHOW);
            }

    };

    public void backToUpdateForm(View view){

        if (boolUpdate == true){
            boolUpdate = false;
            Drawable(4);
            Drawable(1);
        } else {
            Drawable(5);
            etScancode.setText(null);
            Drawable(1);
            linearName.setVisibility(View.VISIBLE);
            linearSRL.setVisibility(View.VISIBLE);
            linearListSRL.setVisibility(View.GONE);
            linearWarr.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            boolUpdate = false;
            boolName = false;
            idName = 0;
            idEnt = 0;
        }
    }
    public void toastShow(String str){
        Toast toast =  Toast.makeText(BarScanWarrantsActivity.this, str, Toast.LENGTH_LONG);
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
        } else if(i == 4){
            etName.setFocusableInTouchMode(true);
            etSRL.setFocusableInTouchMode(true);
            etFisknum.setFocusableInTouchMode(true);
        } else if (i ==5){
            etName.setText(null);
            etSRL.setText(null);
            etFisknum.setText(null);
        }
    }
    public void intentAuto(){
        Intent intent = new Intent(mContext, WarrContActivity.class);
        Bundle b = new Bundle();
        b.putString("id", Long.toString(warrant.id_external)); //Your id
        b.putString("id_contract", Long.toString(warrant.id_contract));
        b.putString("id_state", Integer.toString(warrant.id_state));
        b.putString("to_sign", Integer.toString((warrant.id_user_sign == 0) ? warrant.to_sign : 0));
        b.putString("local_state", Integer.toString(warrant.localState));
        b.putString("contragent", String.format("%s; %s;\n%s", warrant.entName, warrant.subEntName, warrant.entAddress));
        b.putString("ent_addr", warrant.entAddress);
        b.putString("ent_name", warrant.entName);
        b.putString("ent_lat", Double.toString(warrant.entLat));
        b.putString("ent_lng", Double.toString(warrant.entLng));
        b.putString("num", warrant.getNumDate());
        b.putString("w_num", Integer.toString(warrant.num));
        b.putString("rem", warrant.remark);
        b.putString("w_date", warrant.d_open_str);
        b.putString("sub_ent_nume", warrant.subEntName);
        int ent = warrant.id_subent;
        if (ent == 0) ent = warrant.id_ent;
        b.putString("id_ent", Integer.toString(ent));
        intent.putExtras(b);
        startActivityForResult(intent, WARR_CONT_SHOW);
    }

    private static class CustomWarrantAdapter extends ArrayAdapter<WarrantRecord>{
        ArrayList<WarrantRecord> warrArray, warrFiltered;
        String preConstraint = "";
        Context context;
        static class WarrantViewHolder {
            public TextView textNo;
            public TextView textStatus;
            public TextView textDate;
            public TextView textName;
            public TextView textRem;
        }
        public CustomWarrantAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
            this.warrArray = new ArrayList<WarrantRecord>();
            this.warrFiltered = new ArrayList<WarrantRecord>();
        }
        public void clear() {
            super.clear();
            warrArray.clear();
        }

        public void add(WarrantRecord item) {
            super.add(item);
            warrArray.add(item);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WarrantViewHolder viewHolder;
            View row = convertView;
            if(row == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.warrant_row2, null);
                viewHolder = new WarrantViewHolder();
                viewHolder.textNo = (TextView)row.findViewById(R.id.lineNo);
                viewHolder.textStatus = (TextView)row.findViewById(R.id.lineState);
                viewHolder.textDate = (TextView)row.findViewById(R.id.lineDate);
                viewHolder.textName = (TextView)row.findViewById(R.id.lineName);
                viewHolder.textRem = (TextView)row.findViewById(R.id.lineRemark);
                row.setTag(viewHolder);
            } else {
                viewHolder = (WarrantViewHolder) row.getTag();
            }
            WarrantRecord item = getItem(position);
            viewHolder.textNo.setText("№" + item.num);
            if (item.jobMarkStarted) viewHolder.textNo.setTextColor(Color.RED);
            else if (item.jobMarkModified) viewHolder.textNo.setTextColor(Color.BLUE);
            else viewHolder.textNo.setTextColor(Color.BLACK);
            viewHolder.textStatus.setText(item.warrStateName);
            viewHolder.textDate.setText(item.getDate());
            viewHolder.textName.setText(item.entName + "; " + item.subEntName);
            viewHolder.textRem.setText(item.remark);

            return row;
        }
    }
}