package com.example.openglsome;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private Button mCheckSupportBtn;
    private Button mOpenglRenderBtn;
    private Button mRotateBtn;
    private Button mTranslateBtn;
    private Button mScaleBtn;
    private boolean support = false;
    private GLSurfaceView glSurfaceView;
    private GLSurfaceView glSurfaceViewStl;
    private MyRender myRender;
    private float rotateDegreen = 0;
    float box[] = new float[] {
            // FRONT
            -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            // BACK
            -0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,
            // LEFT
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            // RIGHT
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            // TOP
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,
            // BOTTOM
            -0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            rotate(rotateDegreen);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView);
        mCheckSupportBtn = (Button)findViewById(R.id.checkSupportBtn);
        mCheckSupportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
                support = (configurationInfo.reqGlEsVersion >= 0x2000);
                if (support) {
                    Toast.makeText(MainActivity.this,"yes，you got it",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"sorry,no support",Toast.LENGTH_LONG).show();
                }
            }
        });
        mOpenglRenderBtn = (Button)findViewById(R.id.openglRenderBtn);
        mOpenglRenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(support){
                    glSurfaceView.setVisibility(View.VISIBLE);
//                    glSurfaceView = new GLSurfaceView(MainActivity.this);
                    glSurfaceView.setRenderer(new GLRender());
//                    setContentView(glSurfaceView);
                }
            }
        });
        mRotateBtn = (Button)findViewById(R.id.rotateBtn);
        mRotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GLSurfaceView glSurfaceView1 = new GLSurfaceView(MainActivity.this);
                glSurfaceView1.setRenderer(new TriangleRender());
                setContentView(glSurfaceView1);
            }
        });
        mTranslateBtn = (Button)findViewById(R.id.translateBtn);
        mTranslateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                glSurfaceViewStl = new GLSurfaceView(MainActivity.this);
                myRender = new MyRender(MainActivity.this);
                glSurfaceViewStl.setRenderer(myRender);
                setContentView(glSurfaceViewStl);
                new Thread(){
                    @Override
                    public void run() {
                        while (true){
                            try {
                                sleep(100);
                                rotateDegreen += 5;
                                mHandler.sendEmptyMessage(0x001);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        });
        mScaleBtn = (Button)findViewById(R.id.scaleBtn);
        mScaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
    public void rotate(float degree) {
        Log.i("wanlijun",degree+"");
        myRender.rotate(degree);
        glSurfaceViewStl.invalidate();
    }

    class GLRender implements GLSurfaceView.Renderer{
        private FloatBuffer floatBuffer;
        float xrot = 0.0f;
        float yrot = 0.0f;
        public GLRender(){
            floatBuffer = float2NioBuffer(box);
        }
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            //设置清屏颜色（RGBA，黑色）
            gl10.glClearColor(1.0f,1.0f,1.0f,0f);
            gl10.glEnable(GL10.GL_DEPTH_TEST); //启用深度缓存
            gl10.glEnable(GL10.GL_CULL_FACE);  //启用背面剪裁
            gl10.glClearDepthf(1.0f);    // 设置深度缓存值
            gl10.glDepthFunc(GL10.GL_LEQUAL);  // 设置深度缓存比较函数，GL_LEQUAL表示新的像素的深度缓存值小于等于当前像素的深度缓存值（通过gl.glClearDepthf(1.0f)设置）时通过深度测试
            gl10.glShadeModel(GL10.GL_SMOOTH);// 设置阴影模式GL_SMOOTH
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {
            gl10.glViewport(0,0,i,i1);
            gl10.glMatrixMode(GL10.GL_PROJECTION); // 设置投影矩阵
            //将当前变换矩阵复位变为单位矩阵
            gl10.glLoadIdentity();
//            GLU.gluPerspective(gl10, 45.0f, ((float) i) / i1, 0.1f, 10f);//设置透视范围
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            //用glClearColor设置的清屏颜色来清屏，于是画板的背景色就变成了绿色
            //清除深度缓存
            gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            //声明使用模型视图变换
            gl10.glMatrixMode(GL10.GL_MODELVIEW);
            //将当前变换矩阵复位变为单位矩阵
            gl10.glLoadIdentity();
//            GLU.gluLookAt(gl10, 0, 0, 3, 0, 0, 0, 0, 1, 0);//设置视点和模型中心位置
            //绕（1,0,0）向量旋转30度
//            gl10.glRotatef(30, 1, 0, 0);
            //沿x轴方向移动1个单位
//            gl10.glTranslatef(1, 0, 0);
            //x，y，z方向放缩0.1倍
//            gl10.glScalef(0.1f, 0.1f, 0.1f);
            // 设置三角形
            gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            //设置GL10.GL_FIXED时画不出模型，设置为GL10.GL_FLOAT时可以画
            gl10.glVertexPointer(3, GL10.GL_FLOAT, 0, floatBuffer);

            //旋转规则：glRotatef(GLfloat angle,GLfloat x,GLfloat y,GLfloat z)
            //angle表示旋转角度,x,y,z相当于一个布尔值，0表示假，非零表示真
            //如果你想让当前的几何图形围绕着z轴旋转，那么x和y都设为0，而z设为非零值即可
            //如果这里的x,y,z的值都设置为0.0，那么将围绕着x轴旋转
            //如果设置的旋转值（x,y,z的值）为正数，那么旋转的方向是逆时针的，如果旋转值是负数，那么旋转的方向是顺时针的
            //OpenGL是右手原则,大拇指指向（0,0,0）到（1,0,0）方向，另外四个手指的弯曲方向即为模型旋转方向
            gl10.glRotatef(xrot, 1, 0, 0);  //绕着(1,0,0)即x轴旋转
            gl10.glRotatef(yrot, 0, 1, 0);

            //六个面一个面绘制一种颜色
            gl10.glColor4f(1.0f, 0, 0, 1.0f);   //设置颜色，红色
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);  //绘制正方型FRONT面
            gl10.glColor4f(1.0f, 1.0f, 0, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);

            gl10.glColor4f(0, 1.0f, 0, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
            gl10.glColor4f(0, 1.0f, 1.0f, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);

            gl10.glColor4f(0, 0, 1.0f, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
            gl10.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);

            xrot += 1.0f;
            yrot += 0.5f;
//            gl10.glFinish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(glSurfaceView != null && glSurfaceView.getVisibility() == View.VISIBLE){
            glSurfaceView.onResume();
        }
        if(glSurfaceViewStl != null){
            glSurfaceViewStl.onResume();
            //如果在oncreate
//           new Thread(){
//               @Override
//               public void run() {
//                  while (true){
//                      try {
//                          sleep(100);
//                          rotateDegreen += 5;
//                          mHandler.sendEmptyMessage(0x001);
//                      } catch (Exception e) {
//                          e.printStackTrace();
//                      }
//                  }
//               }
//           }.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(glSurfaceView != null && glSurfaceView.getVisibility() == View.VISIBLE){
            glSurfaceView.onPause();
        }
        if(glSurfaceViewStl != null){
            glSurfaceViewStl.onPause();
        }
    }
    private FloatBuffer float2NioBuffer(float[] trianglePoint){
        //先初始化buffer，数组的长度*4，因为一个float占4个字节
        //报错：java.lang.IllegalArgumentException: Must use a native order direct Buffer
        //是因为我用的ByteBuffer.allocate，应该要用ByteBuffer.allocateDirect
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(trianglePoint.length * 4);
        //以本机字节顺序来修改此缓冲区的字节顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer fb = byteBuffer.asFloatBuffer();
        //将给定float[]数据从当前位置开始，依次写入此缓冲区
        fb.put(trianglePoint);
        //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
        fb.position(0);
        return fb;
    }

    class TriangleRender implements GLSurfaceView.Renderer{
        private float[] mTriangleArray = {0f,1f,0f,-1f,-1f,0f,1f,-1f,0f};
//        private float[] mColor = new float[]{
//                1, 1, 0, 1,
//                0, 1, 1, 1,
//                1, 0, 1, 1
//        };
private float[] mColor = new float[]{
        1, 0, 0, 1,
        0, 1, 0, 1,
        0, 0, 1, 1
};
        public TriangleRender(){
        }
        @Override
        public void onDrawFrame(GL10 gl10) {
            gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
            gl10.glMatrixMode(GL10.GL_MODELVIEW);
            gl10.glLoadIdentity();
            gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl10.glEnableClientState(GL10.GL_COLOR_ARRAY);
//            gl10.glTranslatef(0.0f,0.0f,-2.0f);
            gl10.glVertexPointer(3,GL10.GL_FLOAT,0,float2NioBuffer(mTriangleArray));
            gl10.glColorPointer(4, GL10.GL_FLOAT, 0, float2NioBuffer(mColor));
            gl10.glDrawArrays(GL10.GL_TRIANGLES,0,3);
            gl10.glFinish();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {
            float ration = (float)i/i1;
            gl10.glViewport(0,0,i,i1);
            gl10.glMatrixMode(GL10.GL_PROJECTION);
            gl10.glLoadIdentity();
//            gl10.glFrustumf(ration, ration, -1, 1, 1, 10);
//            gl10.glMatrixMode(GL10.GL_MODELVIEW);
//            gl10.glLoadIdentity();
        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            gl10.glClearColor(1.0f,1.0f,1.0f,0.0f);
        }
    }


//    private void loadTexture(GL10 gl, Model model, boolean isAssets) {
//        Log.d("GLRenderer", "绑定纹理：" + model.getPictureName());
//        Bitmap bitmap = null;
//        try {
//            // 打开图片资源
//            if (isAssets) {//如果是从assets中读取
//                bitmap = BitmapFactory.decodeStream(getAssets().open(model.getPictureName()));
//            } else {//否则就是从SD卡里面读取
//                bitmap = BitmapFactory.decodeFile(model.getPictureName());
//            }
//            // 生成一个纹理对象，并将其ID保存到成员变量 texture 中
//            int[] textures = new int[1];
//            gl.glGenTextures(1, textures, 0);
//            model.setTextureIds(textures);
//
//            // 将生成的空纹理绑定到当前2D纹理通道
//            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
//
//            // 设置2D纹理通道当前绑定的纹理的属性
//            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//                    GL10.GL_NEAREST);
//            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
//                    GL10.GL_LINEAR);
//
//            // 将bitmap应用到2D纹理通道当前绑定的纹理中
//            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
//            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//
//            if (bitmap != null)
//                bitmap.recycle();
//
//        }
//    }


}
