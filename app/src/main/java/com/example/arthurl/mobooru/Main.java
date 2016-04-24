package com.example.arthurl.mobooru;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;

import org.json.*;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main extends Activity {

    int pageSize = 30;
    private StaggeredGridView sgv;
    private DataAdapter adapter;
    public ArrayList<Data> datas = new ArrayList<Data>();


    final String verstring = "MoBooru v. 0.2a";

    String mainsite = "http://redditbooru.com";
    URL url1;

    Display display;

    private static Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Legacy support
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    int screenWidth = 0;
    int screenHeight = 0;
    int bitmapWidth = 0;
    int bitmapHeight = 0;

    // DEFAULT SETTINGS
    String favstring = "";
    Boolean showNsfw = false;

    String s1 = "http://redditbooru.com/images/?sources=" + favstring + "&afterDate=";
    long lastTime;
    Document doc;
    Elements redditSubs;
    ArrayList<Sub> subsList = new ArrayList<Sub>();
    String catJSONs = "";
    JSONArray catJSONa;
    LoadJSONasyncInit runner;
    JSONArray jsonObjs;
    LoadMorePhotos lm;

    int current_page = 1;
    int currentScrollPos = 0;
    Boolean loadingMore = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        favstring = prefs.getString("FAV_SUBS", "" + R.string.defaultsub).replaceAll(",", "%2C");
        showNsfw = prefs.getBoolean("SHOW_NSFW", false);

        display = getWindowManager().getDefaultDisplay();
        screenWidth = getDisplaySize(display).x;
        screenHeight = getDisplaySize(display).y;

        System.out.println(favstring);
        s1 = "http://redditbooru.com/images/?sources=" + favstring;
        runner = new LoadJSONasyncInit();

        try {
            JSONArray jsonObjs = new JSONArray();
            jsonObjs = runner.execute(jsonObjs).get();
            ArrayList<Data> tmp = addToArry(jsonObjs);
            adapter = new DataAdapter(this, R.layout.staggered, tmp, showNsfw);
            setTitle(verstring);
            sgv = (StaggeredGridView) findViewById(R.id.gridView);
            sgv.setAdapter(adapter);
            sgv.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    lm = new LoadMorePhotos();
                    lm.execute();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("jsonparsefailed");
        }

        sgv.setOnItemClickListener(new StaggeredGridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Toast.makeText(getActivity(), "Item Clicked: " + position, Toast.LENGTH_SHORT).show();
                ImageView imView = new ImageView(getActivity());
                try{

                    Bitmap img = new DownloadImage(imView).execute(datas.get(position).imgUrl).get();
                    bitmapWidth = img.getWidth();
                    bitmapHeight = img.getHeight();

                    BitmapDrawable resizedBitmap = new BitmapDrawable(getActivity().getResources(), Bitmap.createScaledBitmap(img, bitmapWidth, bitmapHeight, false));
                    while(bitmapHeight > (screenHeight - 250) || bitmapWidth > (screenWidth - 250)) {
                        bitmapHeight = bitmapHeight / 2;
                        bitmapWidth = bitmapWidth / 2;
                    }

                    Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.popup_imgview);

                    ImageView image = (ImageView) dialog.findViewById(R.id.imageview);

                    image.setBackground(resizedBitmap);

                    dialog.getWindow().setBackgroundDrawable(null);
                    dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                    dialog.show();

                } catch (Exception e){
                    e.printStackTrace();
                }

//                finish();

            }
        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }


    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the offset value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter
    }

    public Context getActivity() {
        return this;
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ProgressDialog pDialog;

        protected void onPreExecute(){
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Downloading...");
            pDialog.show();
        }

        public DownloadImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            System.out.println(urls[0]);
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            pDialog.dismiss();
            bmImage.setImageBitmap(result);
        }
    }

    private class LoadJSONasyncInit extends AsyncTask<JSONArray, Void, JSONArray> {

        protected JSONArray doInBackground(JSONArray... urls) {
            try {
                doc = Jsoup.connect(mainsite)
                        .header("Accept-Encoding", "gzip, deflate")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .maxBodySize(0)
                        .timeout(6000000)
                        .get();
                redditSubs = doc.select("script");
                int i = 0;
                for (Element sub : redditSubs) {
                    String at = sub.toString();
                    if (i == 2) {
                        catJSONs = at;
                    }
                    i++;
                }

                catJSONs = catJSONs.substring(catJSONs.indexOf("["));
                catJSONs = catJSONs.substring(0, catJSONs.indexOf("]") + 1);

                catJSONa = new JSONArray(catJSONs);

                for (int j = 0; j < catJSONa.length(); j++) {
                    subsList.add(new Sub(catJSONa.getJSONObject(j).getString("name"), catJSONa.getJSONObject(j).getInt("value")));
                }
//                System.out.println(subsList.size());

            } catch (Exception e) {
                System.out.println("connection failed");
                e.printStackTrace();
            }

            try {
                url1 = new URL(s1 + lastTime);

                Scanner scan = new Scanner(url1.openStream());
                String str = "";
                while (scan.hasNext())
                    str += scan.nextLine();
                scan.close();

                jsonObjs = new JSONArray(str);

            } catch (Exception e) {
                System.out.println("JSON parse failed");
                e.printStackTrace();
            }

            return jsonObjs;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            jsonObjs = result;
        }
    }

    private class LoadMorePhotos extends AsyncTask<Void, Void, Void> {

        JSONArray tmp;

        @Override
        protected Void doInBackground(Void... arg0) {

            // SET LOADING MORE "TRUE"
            loadingMore = true;

            // INCREMENT CURRENT PAGE
            current_page += 1;

            try {
                s1 = "http://redditbooru.com/images/?sources=" + favstring + "&afterDate=";
                url1 = new URL(s1 + lastTime);
                System.out.println(s1 + lastTime);

                Scanner scan = new Scanner(url1.openStream());
                String str = "";
                while (scan.hasNext())
                    str += scan.nextLine();
                scan.close();

                tmp = new JSONArray(str);
            } catch (Exception e) {
                System.out.println("JSON parse failed");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // get listview current position - used to maintain scroll position
            int currentPosition = sgv.getFirstVisiblePosition();

            // APPEND NEW DATA TO THE ARRAYLIST AND SET THE ADAPTER TO THE
            // LISTVIEW
            datas = addToArry(tmp);
            adapter.datas = datas;
            adapter.notifyDataSetChanged();


            // SET LOADINGMORE "FALSE" AFTER ADDING NEW FEEDS TO THE EXISTING
            // LIST
            loadingMore = false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public ArrayList<Data> addToArry(JSONArray ja) {
        if (loadingMore) {

            //IMPLEMENT STOP LOADING ONCE ARRAYSIZE < PAGESIZE
            if (ja == null) {
                loadingMore = false;
            } else {
                if (ja.length() < pageSize) {
                    pageSize = ja.length();
                }
                for (int i = 0; i < pageSize; i++) {
                    Data data = new Data();
                    try {
                        data.thumbImgUrl = "http://redditbooru.com" + ja.getJSONObject(i).getString("thumb") + "_300_300.jpg";
                        data.imgUrl = ja.getJSONObject(i).getString("cdnUrl");
                        data.width = ja.getJSONObject(i).getInt("width");
                        data.height = ja.getJSONObject(i).getInt("height");
                        data.nsfw = ja.getJSONObject(i).getBoolean("nsfw");
                        data.title = ja.getJSONObject(i).getString("title");
                        data.desc = ja.getJSONObject(i).getString("sourceUrl");
                        data.rat = data.width / data.height;
                        if (i == pageSize - 1) {
                            lastTime = Long.parseLong(ja.getJSONObject(i).getString("dateCreated"));
                            System.out.println(lastTime);
                        }
                    } catch (Exception e) {
                        System.out.println("JSON parse failed2");
                        e.printStackTrace();
                    }
                    if (data.desc.equals("null")) {
                        data.desc = "";
                    }
                    if (data.thumbImgUrl.equals("null")) {
                        data.thumbImgUrl = "";
                    }
                    datas.add(data);
                }
            }
        }
        return datas;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(Main.this, Settings_subs.class).putExtra("arylst", subsList));
                return true;
            case R.id.action_about:
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage("Author: pspkazy\nSite: http://github.com/pspkazy\n2014-2015")
                        .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
