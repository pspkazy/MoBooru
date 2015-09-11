package com.example.arthurl.mobooru;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Settings_subs extends Activity {


    ArrayList<Sub> subsList;
    CustomAdapter adp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_subs);
        subsList = (ArrayList<Sub>) getIntent().getSerializableExtra("arylst");
        displayList();
        buttonPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayList() {
        adp = new CustomAdapter(this, R.layout.activity_settings_subs_checkboxes, subsList);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adp);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                Sub sub = (Sub) parent.getItemAtPosition(p);
                Toast.makeText(getApplicationContext(), " Selected " + sub.subname, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class CustomAdapter extends ArrayAdapter<Sub> {

        private ArrayList<Sub> subsList;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<Sub> subsList) {
            super(context, textViewResourceId, subsList);
            this.subsList = new ArrayList<Sub>();
            this.subsList.addAll(subsList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.activity_settings_subs_checkboxes, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Sub sub = (Sub) cb.getTag();
                        Toast.makeText(getApplicationContext(),
                                "Clicked on Checkbox: " + cb.getText() +
                                        " is " + cb.isChecked(),
                                Toast.LENGTH_LONG).show();
                        sub.selected = cb.isChecked();
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Sub sb = subsList.get(position);
            holder.code.setText(" (" +  sb.subID + ")");
            holder.name.setText(sb.subname);
            holder.name.setChecked(sb.selected);
            holder.name.setTag(sb);

            return convertView;

        }

    }

    private void buttonPressed() {
        Button back = (Button) findViewById(R.id.updateSelection);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer response = new StringBuffer();
                response.append(" Selected: \n");
                ArrayList<Sub> s = adp.subsList;
                for (Sub d : s) {
                    if (d.selected) {
                        response.append("\n " + d.subname);
                    }
                }

                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        });
    }
}