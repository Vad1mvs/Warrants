package com.utis.warrants;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.warrants.record.EmpRecord;
import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.record.WarrContRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.EmpTable;
import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.SpecsTable;
import com.utis.warrants.tables.StateTable;
import com.utis.warrants.tables.WarrContTable;
import com.utis.warrants.tables.WarrantTable;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ATestActivity extends Activity {
    private static final boolean D = true;
    private static final String TAG = "ATestActivity";
    public Context mContext;
    private DBSchemaHelper dbSch;

    private ListView mDBListView, list, list2, list3, listWarrants;
    private ArrayAdapter<GoodsRecord> mDBArrayAdapter;
    private GoodsRecord jobGroup;

    private ArrayAdapter<String> mDBArrayAdapter2;

    private WarrContRecord warrCont;
    private EmpRecord emp;

    private ArrayAdapter<String> mDBArrayAdapter3;
    private ArrayAdapter<EmpRecord> mDBArrayAdapter4;
    private ArrayAdapter<String> mDBArrayAdapter5;

    SpecsRecord specs;

    private WarrantRecord warr;
    private int arraySize;
    //private String typeCaption, excludedJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atest);
        dbSch = DBSchemaHelper.getInstance(this);
        mContext = this;

        mDBArrayAdapter = new ArrayAdapter<GoodsRecord>(this, R.layout.db_data);
        mDBListView = (ListView) findViewById(R.id.listViewJob);
        mDBListView.setAdapter(mDBArrayAdapter);

        mDBArrayAdapter3 = new ArrayAdapter<String>(this, R.layout.db_data);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(mDBArrayAdapter3);

        mDBArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.db_data);
        list2 = (ListView) findViewById(R.id.list2);
        list2.setAdapter(mDBArrayAdapter2);

        mDBArrayAdapter4 = new ArrayAdapter<EmpRecord>(this, R.layout.db_data);
        list3 = (ListView) findViewById(R.id.list3);
        list3.setAdapter(mDBArrayAdapter4);

        mDBArrayAdapter5 = new ArrayAdapter<String>(this, R.layout.db_data);
        listWarrants = (ListView) findViewById(R.id.listWarrants);
        listWarrants.setAdapter(mDBArrayAdapter5);


        showJobGroups(dbSch);
        showSPECSGroups(dbSch);
        //showGDSCont(dbSch);
        showWarrCont(dbSch);
        showEmp(dbSch);
        showWarrants(dbSch);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String selected = list.getItemAtPosition(arg2).toString();
                String str = "18359001";
                Toast.makeText(ATestActivity.this,selected,Toast.LENGTH_SHORT).show();
                if(selected.contains(str)){
                    Toast.makeText(ATestActivity.this,"Perfect",Toast.LENGTH_SHORT).show();
                }else{}
            }
        });
        String str = "18359001";
    }

    private void showJobGroups(DBSchemaHelper sh) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c;
        String q_eq = "(SELECT g."+ GoodsTable.ID_EXTERNAL +" FROM "+ GoodsTable.TABLE_NAME +
                " g " +")as "+
                GoodsRecord.GDS_CHILD_CNT;
        String query;
            query = "SELECT d.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " d "+ "ORDER BY d." +
                    GoodsTable.NM + " ASC";

        c = sqdb.rawQuery(query, null);

        int cntr = 0;
        try {
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {
                cntr++;
                jobGroup = new GoodsRecord(c);
                mDBArrayAdapter.add(jobGroup);
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Count = " + cntr);
            //  showCounter(cntr);
            if (c != null) c.close();
        }
    }

    private void showSPECSGroups(DBSchemaHelper sh) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        String querySelect = "SELECT s.*, ";
        String q_eqp = "(SELECT e."+ SpecsTable.ID_CONTRACT +" FROM "+ SpecsTable.TABLE_NAME +
                " e " +")as "
                + SpecsRecord.SPECS_EQ_NM_2
                ;
        String query;
            query = querySelect + q_eqp +" FROM " + SpecsTable.TABLE_NAME + " s ORDER BY s." + SpecsTable.ID_GDS;

        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter3.clear();
            while (c.moveToNext()) {
                specs = new SpecsRecord(c);

                mDBArrayAdapter3.add("  "+specs.id_external + "      | id_srl: " +specs.srl+"| id_ent: "+specs.id_ent+"| fisk: "+specs.fisk_num
                        +"| id_contract: " +specs.id_contract

+ "| temp: " +specs.temp+"| ip_modem: "+specs.ipModem+"| ip_cash: "+specs.ipCash+"| num_cash: " +specs.numCash +"| factory_seal: "+specs.factorySeal
                        +"| modified: " +specs.modified + " |place" + specs.place);
                cntr++;
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "job Count = " + cntr);
            if (c != null) c.close();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }
    private int showWarrCont(DBSchemaHelper sh) {
        String q_eq = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
                " WHERE "+ StateTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_STATUS +")as "+ WarrContRecord.EQ_STATE_NM;
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;

        String query;
            query = "SELECT *,"+ q_eq + " from " + WarrContTable.TABLE_NAME +
                    " ORDER BY " + WarrContTable.ID_EXTERNAL + " DESC";
       int checkedCntr = 0;
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter2.clear();
            while (c.moveToNext()) {
                warrCont = new WarrContRecord(c);
                mDBArrayAdapter2.add(String.valueOf(
"eq_inv: "+ warrCont.eq_inv + " / eq_srl:  "+ warrCont.eq_srl + " /id_state:  "+warrCont.eq_spec
        + " /job_status:  "+warrCont.job_status + " /id_subent:  "+warrCont.id_job + " /eq_status:  "+warrCont.eq_status+ " / eq_spec:  "+ warrCont.eq_spec + " /remark:  "
        +warrCont.remark + " /modified:  "+warrCont.modified + " /rec_stat:  "+warrCont.rec_stat + " /tbl_mode:  "+warrCont.tbl_mode));

            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "WarrCont Count = " + arraySize);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        return arraySize;
    }

    private void showEmp(DBSchemaHelper sh) {
        SQLiteDatabase sqdb = sh.getWritableDatabase();
        Cursor c = null;
        String query;

            query = "SELECT * from " + EmpTable.TABLE_NAME + " ORDER BY " + EmpTable.SURNAME + " ASC";
        int cntr = 0;
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter4.clear();
            while (c.moveToNext()) {
                cntr++;
                emp = new EmpRecord(c);
                if (D) Log.d(TAG, "Emp = " + emp.id_external);
                mDBArrayAdapter4.add(emp);
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "emp Count = " + cntr);
            if (c != null) c.close();
        }
    }

    private void showWarrants(DBSchemaHelper sh) {
//        SQLiteDatabase sqdb = sh.getWritableDatabase();
//        Cursor c = null;
//        Cursor cEnt = null;
//        String w_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
//                " WHERE "+ StateTable.ID_EXTERNAL +" = w."+ WarrantTable.IDSTATE +")as "+
//                WarrantRecord.WARR_STATE_NM;
//
//       // String markQuery = 	"(SELECT MAX(" + EmpWorkTimeTable.ID + ") FROM " + EmpWorkTimeTable.TABLE_NAME +
//       //         " WHERE " + EmpWorkTimeTable.ID_WARRANT + "= w.id_external)AS DTBE";
//        String query;
//        query = "SELECT w.*, "+  w_state +" FROM " + WarrantTable.TABLE_NAME  +" w WHERE " + WarrantTable.REGL + " = " + 0;
//int i = 0;
//        try {
//            c = sqdb.rawQuery(query, null);
//            mDBArrayAdapter5.clear();
//            while (c.moveToNext()) {
//                warr = new WarrantRecord(c);
//mDBArrayAdapter5.add(String.valueOf(
//
//         "id_ent: "+ warr.id_ent + " / id_owner:  "+ warr.id_owner + " /id_state:  "+warr.id_state + " /id:  "+warr.id_external
//                        + " /id_subent:  "+warr.id_subent+" num: "+ warr.num + " / id_contract:  "+ warr.id_contract + " /id_user_sign:  "+warr.id_user_sign + " /d_open:  "+warr.d_open
//                + " /remark:  "+warr.remark+" modified: "+ warr.modified + " / to_sign:  "+ warr.to_sign + " /d_sign:  "+warr.d_sign + " /local_state:  "+warr.localState + "REGL: "+warr.regl));
//                Log.e(TAG,String.valueOf(i++) );
//            }
//        } catch (Exception e) {
//            if (D) Log.e(TAG, "Exception: " + e.getMessage());
//        } finally {
//            if (D) Log.d(TAG, "WarrCont Count = " + arraySize);
//
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            invalidateOptionsMenu();
//        }

    }
}