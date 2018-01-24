package com.example.jahidul.colourdetector;



import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class FindColour extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    final String LOG =this.getClass().getName();
    private ArrayList<color> colorList ;
    private String colorcode;

    private CameraBridgeViewBase mOpenCvCameraView;
    Button btnRenam;

    private  Mat mRgba;

    TextView touch_coordinator;
    TextView touch_colour;
    TextView colorName;
    double x=-1;
    double y=-1;

    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;



    private BaseLoaderCallback mLoaderCallBack=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(FindColour.this);
                }
                break;
                default:{
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_colour);

        touch_coordinator=(TextView)findViewById(R.id.touch_coordinator);
        touch_colour=(TextView)findViewById(R.id.textcolour);
        colorName=(TextView)findViewById(R.id.textViewColorName);
        btnRenam=(Button)findViewById(R.id.buttonRename);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView=(CameraBridgeViewBase)findViewById(R.id.activity_find_colour_camera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        btnRenam.setOnClickListener(this);


    }

    @Override
    public void onPause(){
        super.onPause();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()) {
         OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallBack);
        }
        else
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();

    }
    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba=new Mat();
        mBlobColorRgba=new Scalar(255);
        mBlobColorHsv=new Scalar(255);


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();


    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        return  mRgba;
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols=mRgba.cols();
        int rows=mRgba.rows();

        double yLow=(double)mOpenCvCameraView.getHeight()*0.2401961;
        double yHigh=(double)mOpenCvCameraView.getHeight()*0.7696078;

        double xScale=(double)cols / mOpenCvCameraView.getWidth();
        double yScale=(double)rows / (yHigh-yLow);


        x=event.getX();
        y=event.getY();

        y=y-yLow;

        x=x*xScale;
        y=y*yScale;
        if(( x<0 ) || (y<0)||(x>cols)||(y>rows)) return  false;
        touch_coordinator.setText("X: "+Double.valueOf(x)+",Y: "+Double.valueOf(y));

        Rect touchRect=new Rect();
        touchRect.x=(int)x;
        touchRect.y=(int)y;
        touchRect.width=8;
        touchRect.height=8;

        Mat touchRegionRgba=mRgba.submat(touchRect);
        Mat touchRegionHsv=new Mat();

        Imgproc.cvtColor(touchRegionRgba,touchRegionHsv,Imgproc.COLOR_RGB2HSV_FULL);
        mBlobColorHsv= Core.sumElems(touchRegionHsv);

        int pointCount=touchRect.width*touchRect.height;
        for (int i=0;i<mBlobColorHsv.val.length;i++)
            mBlobColorHsv.val[i] /=pointCount;
        mBlobColorRgba= ConvertsScalarHsv2Rgba(mBlobColorHsv);

        //sow color from database


        HashMap postData =new HashMap();

        String code="#"+String.format("%02X",(int)mBlobColorRgba.val[0])+String.format("%02X",(int)mBlobColorRgba.val[1])+String.format("%02X",(int)mBlobColorRgba.val[2]);
         //int c= Integer.parseInt(code);
        colorcode="#"+ String.format("%02X",(int)mBlobColorRgba.val[0])+String.format("%02X",(int)mBlobColorRgba.val[1])+String.format("%02X",(int)mBlobColorRgba.val[2]);




        postData.put("colorCode", code);

        PostResponseAsyncTask taskWrite = new PostResponseAsyncTask(FindColour.this, postData, new AsyncResponse() {
            @Override
            public void processFinish(String s) {


              // final  String Cname;
                // String Ctimes;


                colorList = new JsonConverter<color>().toArrayList(s,color.class);

                //BindDictionary<color>dictionary = new BindDictionary<color>();


            }
        });

        taskWrite.execute("http://jahidul.netau.net/colorList.php");

       // String [] colorname={"#060001","#060002","#060003","#060004","#060005","#060006","#060007","#060008","#060009",
         //       "#050001","#050002","#050003","#050004","#050005","#050006"};




          //set value


        touch_colour.setText("Color: #"+ String.format("%02X",(int)mBlobColorRgba.val[0])+String.format("%02X",(int)mBlobColorRgba.val[1])+String.format("%02X",(int)mBlobColorRgba.val[2]));

        touch_colour.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],
                (int) mBlobColorRgba.val[2]));

        touch_coordinator.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],
                (int) mBlobColorRgba.val[2]));


        if("#060001".equals(colorcode) || "#060002".equals(colorcode) || "#060003".equals(colorcode)|| "#060004".equals(colorcode)
                || "#060005".equals(colorcode)|| "#060006".equals(colorcode)|| "#060007".equals(colorcode)|| "#050001".equals(colorcode)
                || "#050002".equals(colorcode)|| "#070001".equals(colorcode)|| "#070002".equals(colorcode)){
            colorName.setText("Color Name:Black");

        } else  if("#FFFFFF".equals(colorcode) || "#e5e5e5".equals(colorcode) || "#cccccc".equals(colorcode)|| "#b2b2b2".equals(colorcode)
               ){
            colorName.setText("Color Name:White");
        }else  if("#FF0000".equals(colorcode) || "#FF1414".equals(colorcode) || "#FF3C3C".equals(colorcode)|| "#FF8282".equals(colorcode)
                ){
            colorName.setText("Color Name:Red");
        }else  if("#347C17".equals(colorcode) || "#48902B".equals(colorcode) || "#70B853".equals(colorcode)|| "#206803".equals(colorcode)
                ){
            colorName.setText("Color Name:Green");
        }else if("#000072".equals(colorcode) || "#002DAE".equals(colorcode) || "#3C69EA".equals(colorcode)|| "#507DFE".equals(colorcode)
                ){
            colorName.setText("Color Name:Blue");
        }



        return false;
    }

    private Scalar ConvertsScalarHsv2Rgba(Scalar hsvColor){
        Mat pointMatRgba=new Mat();
        Mat pointMatHsv=new Mat(1,1, CvType.CV_8UC3,hsvColor);
        Imgproc.cvtColor(pointMatHsv,pointMatRgba,Imgproc.COLOR_HSV2RGB_FULL,4);
        return  new Scalar(pointMatRgba.get(0,0));

    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(FindColour.this,EditColor.class);
        intent.putExtra("colorCode",colorcode);
      //  intent.putExtra("country", TailorCountry[position]);
        startActivity(intent);
    }
}
