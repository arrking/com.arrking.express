package com.arrking.express.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.arrking.android.database.Properties;
import com.arrking.android.util.HTTPRequestHelper;
import com.arrking.express.MainActivity;
import com.arrking.express.R;
import com.arrking.express.common.Constants;
import com.arrking.express.common.ServerURLHelper;
import com.arrking.express.model.ActivitiTask;
import com.arrking.express.model.ActivitiTasks;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class PendingOrdersFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String CLASSNAME = PendingOrdersFragment.class.getName();
    private LinearLayout root;
    private ListView tab01ListView;
    private List<ContentValues> listContentValues;
    private BaseAdapter adapter;
    private HTTPRequestHelper httpRequestHelper;
    private Properties properties;
    private String userId;
    private String userPass;


    private Handler taskDataRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(CLASSNAME, "get data ...");
            Gson gson = new Gson();
            Bundle data = msg.getData();
            String resp = data.getString("RESPONSE");
            switch (msg.what) {
                case 200:
                    ActivitiTasks activitiTasks = gson.fromJson(resp, ActivitiTasks.class);
                    Log.d(CLASSNAME, "activitiTasks get size " + activitiTasks.getSize());
                    listContentValues = tasks2contentValues(activitiTasks.getData());
                    ((MainActivity) getActivity()).setBadge(1, listContentValues.size());
                    setAdapter(listContentValues);
                    tab01ListView.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).removeLoading();
                    break;
                default:
                    Log.w(CLASSNAME, "taskDataRequestHandler resp:" + resp);
                    break;
            }
        }

        private List<ContentValues> tasks2contentValues(List<ActivitiTask> data) {
            List<ContentValues> t = new ArrayList();
            for (int i = 0; i < data.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(Constants.TASK_ID, data.get(i).getId());
                cv.put(Constants.TASK_ORDER_DATE, data.get(i).getCreateTime());
                cv.put(Constants.TASK_ORDER_LOCATION, "Hello World Cafe");
                t.add(cv);
            }
            return t;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpRequestHelper = new HTTPRequestHelper(taskDataRequestHandler);
        properties = Properties.getInstance(getActivity());
        userId = properties.get(Constants.USER_ID);
        userPass = properties.get(Constants.USER_PASSWORD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        this.root = (LinearLayout) inflater.inflate(R.layout.tab01, container, false);
        initUI();
        refreshList();
        return this.root;
    }

    private void refreshList() {
        ((MainActivity) getActivity()).addLoading();
        this.tab01ListView.setVisibility(View.INVISIBLE);
        requestTaskData();
    }

    private void requestTaskData() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                httpRequestHelper.performPostJSON(ServerURLHelper.queryCashierTasksURL(),
                        userId,
                        userPass,
                        ServerURLHelper.getJSONHeaders(),
                        ServerURLHelper.getQueryCashierTasksBody()
                );
            }
        })).start();
    }

    private void setAdapter(List<ContentValues> lis) {
        this.adapter = new TaskListAdapter(getActivity(), lis);
        this.tab01ListView.setAdapter(this.adapter);
    }

    private void initUI() {
        this.tab01ListView = (ListView) this.root.findViewById(R.id.tab01_listView);
        this.tab01ListView.setOnItemClickListener(this);
        this.tab01ListView.setDivider(getResources().getDrawable(R.drawable.line_image));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    @Override
    public void onPause() {
        super.onPause();
    }


    private class TaskListAdapter extends BaseAdapter {
        private final String TAG = TaskListAdapter.class.getName();
        private LayoutInflater inflater;
        private List<ContentValues> lis;

        public TaskListAdapter(Activity ctx, List<ContentValues> lis) {
            this.inflater = LayoutInflater.from(ctx);
            this.lis = lis;
        }

        @Override
        public int getCount() {
            return this.lis.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "\n ================== \n getView @" + Integer.toString(position));
            PendingOrdersFragment.TaskViewHolder taskViewHolder;
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.task_list_item, null);
                taskViewHolder = new TaskViewHolder();
                taskViewHolder.taskId = (TextView) convertView.findViewById(R.id.task_id);
                taskViewHolder.date = (TextView) convertView.findViewById(R.id.order_date);
                taskViewHolder.orderLocation = (TextView) convertView.findViewById(R.id.order_location);
                convertView.setTag(taskViewHolder);
            } else {
                taskViewHolder = (TaskViewHolder) convertView.getTag();
            }
            ContentValues l = (ContentValues) this.lis.get(position);

            taskViewHolder.date.setText(l.getAsString(Constants.TASK_ORDER_DATE));
            taskViewHolder.orderLocation.setText(l.getAsString(Constants.TASK_ORDER_LOCATION));
            taskViewHolder.taskId.setText(l.getAsString(Constants.TASK_ID));
            return convertView;
        }
    }

    private static class TaskViewHolder {
        ImageView headImage;
        TextView date;
        TextView taskId;
        TextView orderLocation;
    }
}
