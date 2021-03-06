package netdb.course.softwarestudio.askapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netdb.course.softwarestudio.askapp.model.Comment;
import netdb.course.softwarestudio.askapp.model.Definition;
import netdb.course.softwarestudio.askapp.service.rest.RestManager;


public class MainActivity extends ActionBarActivity {
    private ArrayList<Comment> cCommentList = new ArrayList<Comment>();
    private RestManager restMgr;

    public static EditText keywordEdt;
    private EditText definitionEdt;
    private EditText commentEdt;
    private Button searchBtn;
    private Button definitionBtn;
    private Button commentBtn;
    private TextView titleTxt;
    private TextView descriptionTxt;
    private ProgressBar progressBar;
    private ListView cListView;
    private MessageAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restMgr = RestManager.getInstance(this);

        keywordEdt = (EditText) findViewById(R.id.edt_keyword);
        definitionEdt = (EditText) findViewById(R.id.edt_definition);
        commentEdt = (EditText) findViewById(R.id.edt_comment);
        searchBtn = (Button) findViewById(R.id.btn_search);
        definitionBtn = (Button) findViewById(R.id.btn_definition);
        commentBtn = (Button) findViewById(R.id.btn_comment);
        titleTxt = (TextView) findViewById(R.id.txt_title);
        descriptionTxt = (TextView) findViewById(R.id.txt_description);
        progressBar = (ProgressBar) findViewById(R.id.pgsb_loading);
        cListView = (ListView) findViewById(R.id.list_comments);

        mMessageAdapter = new MessageAdapter(this, cCommentList);
        cListView.setAdapter(mMessageAdapter);

        searchBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordEdt.getText().toString().replaceAll("\\s", "");
                if (keyword.length() != 0) {
                    // send a search request
                    progressBar.setVisibility(View.VISIBLE);
                    searchKeyword(keyword);

                    getComments(keyword);
                } else {
                    // clear results
                    titleTxt.setText("");
                    descriptionTxt.setText("");
                }
            }
        });

        definitionBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = keywordEdt.getText().toString().replaceAll("\\s", "");
                String description = definitionEdt.getText().toString().replaceAll("\\s", "");
                if (description.length() != 0 && title.length() != 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    addDefinition(title, description);
                    // searchKeyword(title);
                    getComments(title);
                }
            }
        });

        commentBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                String title = keywordEdt.getText().toString().replaceAll("\\s", "");
                String content = commentEdt.getText().toString().replaceAll("\\s", "");
                if (content.length() != 0 && title.length() != 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    addComment(title, content);
                    getComments(title);
                }
            }
        });

    }

    private void getComments(String title) {

        Map<String, String> params = new HashMap<String, String>();

        restMgr.listResource(Comment.class, params, new RestManager.ListResourceListener<Comment>() {
            @Override
            public void onResponse(int code, Map<String, String> headers,
                                   List<Comment> resources) {
                if (resources != null) {

                    Log.d("TAG", "CODEIS : " + code );
                    Log.d("TAG", "HEADER : " +  headers.toString());
                    Log.d("TAG", "YA~ GET : " + resources);

                    cCommentList.clear();
                    for (Comment m : resources) {
                        cCommentList.add(m);
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {
                onError(null, null, code, headers);
            }

            @Override
            public void onError(String message, Throwable cause, int code,
                                Map<String, String> headers) {
                Log.d(this.getClass().getSimpleName(), "" + code + ": " + message);
            }
        }, null);

    /*
        String URL = getResources().getString(R.string.rest_server_url);
        URL = URL + "comments/" + title;
        Log.d("TAG", URL );

            // defaultHttpClient
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpPost = new HttpGet(URL);
        Log.d("URI", httpPost.getURI().toString());
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost, localContext);
            Log.d("MON","I'm HERE~");
            HttpEntity httpEntity = httpResponse.getEntity();
            Log.d("TAG", httpEntity.getContent().toString());

            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       */
    }

    private void addComment(String title, String content){
        final Comment cmt = new Comment();
        cmt.setTitle(title);
        cmt.setContent(content);

        restMgr.postResource(Comment.class, cmt, new RestManager.PostResourceListener<Comment>() {
            @Override
            public void onResponse(int code, Map<String, String> headers) {
                searchKeyword( cmt.getTitle() );
                return;
                // TODO: Finish this activity

            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {
                onError(null, null, code, headers);
            }

            @Override
            public void onError(String message, Throwable cause, int code, Map<String, String> headers) {
                Log.d(this.getClass().getSimpleName(), "" + code + ": " + message);

                setResult(RESULT_CANCELED);
                finish();
            }
        }, null);
    }

    private void addDefinition(String title, String description) {
        final Definition def = new Definition();
        def.setTitle(title);
        def.setDescription(description);

        restMgr.postResource(Definition.class, def, new RestManager.PostResourceListener<Definition>() {
            @Override
            public void onResponse(int code, Map<String, String> headers) {
                searchKeyword( def.getTitle() );
                return;
                // TODO: Finish this activity

            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {
                onError(null, null, code, headers);
            }

            @Override
            public void onError(String message, Throwable cause, int code, Map<String, String> headers) {
                Log.d(this.getClass().getSimpleName(), "" + code + ": " + message);

                setResult(RESULT_CANCELED);
                finish();
            }
        }, null);
    }


    private void searchKeyword(String keyword) {

        // Set header
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");

        try {
            keyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
            Log.d("TAG", keyword);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restMgr.getResource(Definition.class, keyword, null, header, new RestManager.GetResourceListener<Definition>() {
            @Override
            public void onResponse(int code, Map<String, String> headers, Definition resource) {

                progressBar.setVisibility(View.INVISIBLE);

                titleTxt.setText(resource.getTitle());
                descriptionTxt.setText(resource.getDescription());
            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {

            }

            @Override
            public void onError(String message, Throwable cause, int code, Map<String, String> headers) {

                progressBar.setVisibility(View.INVISIBLE);

                if (code == 404) {
                    Toast.makeText(MainActivity.this, getString(R.string.info_not_found),
                            Toast.LENGTH_SHORT).show();

                }
            }
        }, null);
    }
}
