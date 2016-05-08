package com.frkn.ygahack;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by erkam on 8.05.2016.
 */
public class OCR {
    private static OCR ocr;
    private File file;

    //Returns cloudsight object
    public static OCR getInstance(){
        if(ocr==null)
            return new OCR();
        else
            return ocr;
    }

    public JSONObject postImage(File file){
        this.file = file;
        JSONObject object=null;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        SSLSocketFactory sf = null;
        try {
            sf = new SSLSocketFactory(
                    SSLContext.getDefault(),
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Scheme sch = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        HttpPost httppost = new HttpPost("https://api.ocr.space/parse/image");
        //httppost.addHeader("apikey", "1007e8976188957");
        //httppost.addHeader("language", "tur");
        //httppost.addHeader("isOverlayRequired", "false");

        JSONObject json=null;

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file,"image/jpeg"); //image should be a String
            builder.addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA, file.getName());
            builder.addTextBody("apikey", "1007e8976188957");
            builder.addTextBody("language","tur");
            builder.addTextBody("isOverlayRequired","false");
            httppost.setEntity(builder.build());

           /* // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("image_request[remote_image_url]", "12345"));
            nameValuePairs.add(new BasicNameValuePair("image_request[language]", "tr-TR"));
            nameValuePairs.add(new BasicNameValuePair("image_request[locale]", "tr-TR"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
*/

            String responseBody;

            try {
                responseBody = EntityUtils.toString(httpclient.execute(httppost).getEntity(), "UTF-8");
                Log.d("INFO",responseBody);
                object = new JSONObject(responseBody);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpclient.getConnectionManager().shutdown();
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
        return object;
    }
}
