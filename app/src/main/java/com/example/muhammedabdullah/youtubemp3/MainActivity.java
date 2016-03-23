package com.example.muhammedabdullah.youtubemp3;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    String t = "musc.mp3";
    static String videoUrl = "";
    static boolean titleIsReady= false;
    static String title = "music.mp3";
    static String url = ""; //https://www.youtube.com/watch?v=K7z3590-Mt0
    static String downloadUrl = "http://www.youtubeinmp3.com/fetch/?video=";
    static String parseUrl = "http://www.youtubeinmp3.com/fetch/?format=text&video=";
    public TextView tv = null;
    private EditText et = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.textView);
        et = (EditText)findViewById(R.id.editText);
        et.setHint("Video Url");
    }

    public void download(View view){
        FetchTittle ft = null;
        url = et.getText().toString();
        if(url.equals("") || !url.contains("youtu")){
            tv.setText("You should enter a valid url.");
            return;
        }

        String tempStr = url;
        tempStr = tempStr.split("be",2)[1];
        if(!tempStr.substring(0,4).equals(".com")){ // youtu.be to youtube.com
            String ext = url.split("/",4)[3];
            url = "https://www.youtube.com/watch?v="+ext;
        }

        downloadUrl = "http://www.youtubeinmp3.com/fetch/?video="+url;
        parseUrl = "http://www.youtubeinmp3.com/fetch/?format=text&video="+url;
        try {
            ft = new FetchTittle();
            ft.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(MainActivity.title.equals("music.mp3"))
        {
            tv.setText("The video is protected by copyright.");
            return;
        }
        tv.setText(MainActivity.title+" is downloaded");
    }


   private class FetchTittle extends AsyncTask<Void,Void,Void>{

       String cont ="";
       @Override
       protected void onPreExecute(){
           super.onPreExecute();
           progressDialog = new ProgressDialog(MainActivity.this);
           progressDialog.setTitle("State:");
           progressDialog.setMessage("Title is getting..");
           progressDialog.setIndeterminate(false);
           progressDialog.show();
       }


        @Override
        protected Void doInBackground(Void... params) {
            try {

                Document p = Jsoup.connect(parseUrl).get();
                String tmp = p.body().text();
                if(!tmp.contains("Title") || !tmp.contains("Link")){
                    return null;
                }
                cont = tmp;
                cont = cont.split("Title: ",2)[1];
                cont = cont.split(" Length: ",2)[0];
                cont = cont + ".mp3";

                tmp = tmp.split("Link: ",2)[1];
                downloadUrl = tmp;

                MainActivity.title = cont;
                String str = Context.DOWNLOAD_SERVICE;
                DownloadManager dm;
                dm = (DownloadManager)getSystemService(str);
                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request rq = new DownloadManager.Request(uri);
                rq.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, MainActivity.title);
                rq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Long ref = dm.enqueue(rq);

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
    }

       @Override
       protected void onPostExecute(Void aVoid) {
           progressDialog.dismiss();
           MainActivity.title = "music.mp3";
       }
   }
}
