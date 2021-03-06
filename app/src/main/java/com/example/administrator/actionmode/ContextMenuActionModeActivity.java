package com.example.administrator.actionmode;

import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.MultiChoiceModeListener;
import java.util.ArrayList;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ContextMenuActionModeActivity extends AppCompatActivity {

    private ListView contact_list_view;
    private ProgressDialog mDialog;

    private MyContactAdapter mAdpater;
    private MultiModeCallback mCallback;

    // 模拟数据
    private ArrayList<Contact> contacts;
    private String[] userNames = new String[] { "Jack", "Rose", "Matt", "Adam", "Xtina", "Blake", "Tupac", "Biggie",
            "T.I", "Eminem" };
    private String[] phoneNumbers = new String[] { "138-0000-0001", "138-0000-0002", "138-0000-0003", "138-0000-0004",
            "138-0000-0005", "138-0000-0006", "138-0000-0007", "138-0000-0008", "138-0000-0009", "138-0000-0010" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context_menu_action_mode);

        initView();

        new ContactsDownloadTask().execute();

    }

    private void initView() {
        contact_list_view = (ListView) this.findViewById(R.id.context_menu_listView);

        mDialog = new ProgressDialog(this);

        mDialog.setTitle("提示信息");
        mDialog.setMessage("下载联系人列表中...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        mCallback = new MultiModeCallback();
        contact_list_view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        contact_list_view.setMultiChoiceModeListener(mCallback);
    }

    private void downloadContactsFromServer() {
        if (contacts == null) {
            contacts = new ArrayList<Contact>();
        }

        for (int i = 0; i < userNames.length; i++) {
            contacts.add(new Contact(userNames[i], phoneNumbers[i]));
        }
    }

    private class ContactsDownloadTask extends AsyncTask<Void, Integer, Void> {

        private int currentlyProgressValue;

        @Override
        protected void onPreExecute() {
            mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (currentlyProgressValue < 100) {
                publishProgress(++currentlyProgressValue);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // download data from server
            downloadContactsFromServer();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mAdpater = new MyContactAdapter(ContextMenuActionModeActivity.this, contacts);
            contact_list_view.setAdapter(mAdpater);

            mDialog.dismiss();
        }

    }
        //MultiChoiceModeListener，多选处理
    private class MultiModeCallback implements MultiChoiceModeListener {
        private View mMultiSelectActionBarView;
        private TextView mSelectedCount;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //在Activity类中有一个getMenuInflater()的函数用来返回这个Activity的MenuInflater，
            // 并通过MenuInflater对象来设置menu XML里的menu作为该Activity的菜单
            //inflate就相当于将一个xml中定义的布局找出来
            mode.getMenuInflater().inflate(R.menu.multi_acitonmode_menu, menu);

            mAdpater.setItemMultiCheckable(true);
            //notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容
            mAdpater.notifyDataSetChanged();

            if (mMultiSelectActionBarView == null) {
               //通过LayoutInflater的inflate方法映射一个自己定义的Layout布局xml加载或从xxxView中创建
                mMultiSelectActionBarView = LayoutInflater.from(ContextMenuActionModeActivity.this)
                        .inflate(R.layout.list_multi_select_actionbar, null);
                mSelectedCount = (TextView) mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
            }
            mode.setCustomView(mMultiSelectActionBarView);
            ((TextView) mMultiSelectActionBarView.findViewById(R.id.title)).setText("您已选中");


            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancle:
                    mAdpater.setItemMultiCheckable(false);
                    mAdpater.clearSeletedContacts();
                    mAdpater.notifyDataSetChanged();
                    mode.finish();
                    break;
                case R.id.menu_delete:
                    mAdpater.deleteSeletedContacts();
                    mAdpater.notifyDataSetChanged();
                    mode.invalidate();
                    mode.finish();
                    break;

                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdpater.setItemMultiCheckable(false);
            mAdpater.clearSeletedContacts();
            mAdpater.notifyDataSetChanged();

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (checked) {
                mAdpater.addSelectedContact(position);
            } else {
                mAdpater.cancelSeletedContact(position);
            }

            mAdpater.notifyDataSetChanged();

            updateSeletedCount();
            mode.invalidate();

        }

        public void updateSeletedCount() {
            mSelectedCount.setText(Integer.toString(contact_list_view.getCheckedItemCount()) + "条");
        }

    }

}
