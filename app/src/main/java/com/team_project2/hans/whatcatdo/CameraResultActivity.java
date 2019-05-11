package com.team_project2.hans.whatcatdo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;

import com.team_project2.hans.whatcatdo.Classifier.Recognition;

import org.tensorflow.lite.Tensor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraResultActivity extends AppCompatActivity {
    private static final String  TAG = "CAMERA RESULT ACTIVITY";

    /*layout component*/
    private VideoView videoView;
    private TextView text_result_camera;

    /*tensorflow*/
    private TensorFlowImageClassifier classifier;

    /*동영상 프레임 단위로 자르기*/
    private File videoFile;
    private Uri videoFileUri;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private ArrayList<Bitmap> bitmapArrayList;
    private MediaPlayer mediaPlayer;
    private Bitmap bitmap;
    private Thread thread;

    Long id;
    String string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);
        getSupportActionBar().hide();

        videoView = findViewById(R.id.videoView);
        text_result_camera = findViewById(R.id.text_result_camera);

        new CheckTypesTask().execute();
    }

    void convertVideoToImage(){
        if(!getIntent().getStringExtra("videoPath").isEmpty()){
            String path = getIntent().getStringExtra("videoPath");
            videoView.setVideoURI(Uri.parse(path));
            videoView.start();

            videoFile = new File(path);
            videoFileUri = Uri.parse(videoFile.toString());
            mediaMetadataRetriever = new MediaMetadataRetriever();
            bitmapArrayList = new ArrayList<>();
            mediaMetadataRetriever.setDataSource(videoFile.toString());
            mediaPlayer = MediaPlayer.create(getBaseContext(),videoFileUri);

            for(int i=0;i<mediaPlayer.getDuration(); i += 500){
                bitmap = mediaMetadataRetriever.getFrameAtTime(i*1000,MediaMetadataRetriever.OPTION_CLOSEST);
                bitmapArrayList.add(bitmap);
            }
            mediaMetadataRetriever.release();
            saveFrames();
        }
    }

    public void classifyImages(ArrayList<Bitmap> bitmaps){
        ArrayList<List<Classifier.Recognition>> recognitions = new ArrayList<>();
        TensorFlowImageClassifier tensorFlowImageClassifier = TensorFlowImageClassifier.getTensorFlowClassifier();
        for(Bitmap bitmap : bitmaps){
            bitmap = BitmapConverter.ConvertBitmap(bitmap,Common.INPUT_SIZE);
            List<Classifier.Recognition> results = tensorFlowImageClassifier.recognizeImage(bitmap);
            recognitions.add(results);
        }
        string = "";
        for(List<Classifier.Recognition> r : recognitions){
            Log.d(TAG,r.toString());
            string += (r.toString()+'\n');
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_result_camera.setText(string);
            }
        });
    }




    public void saveFrames(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    saveFrames(bitmapArrayList);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void saveFrames(ArrayList<Bitmap> saveBitmap) throws IOException{
        String folder = Environment.getExternalStorageDirectory().toString();
        id = System.currentTimeMillis();
        File saveFolder = new File(folder + Common.IMAGE_PATH);
        if(!saveFolder.exists()){
            saveFolder.mkdirs();
        }
        int i = 0;
        for (Bitmap b : saveBitmap){
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, Common.IMAGE_QUALITY, bytes);
            File file = new File(saveFolder,("wcd_image_"+id+"_"+i+".jpg"));

            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());

            fo.flush();
            fo.close();
            i++;
        }
        thread.interrupt();
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                CameraResultActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("감정분석 중이에요~");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                convertVideoToImage();
                classifyImages(bitmapArrayList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }



}
