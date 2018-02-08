package xiaoyalllove.xyeturnplatedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private LuckyTurnplate lucky;
    private int index = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lucky = (LuckyTurnplate) findViewById(R.id.lucky);

//        Typeface iconfont = Typeface.createFromAsset(getAssets(), "iconfont.ttf");
//        TextView textview = (TextView) findViewById(R.id.like);
//        textview.setTypeface(iconfont);
    }

    public void startLucky(View view) {
        if (!lucky.isStart()) {
            if (index == 2) {
                index = 5;
            } else {
                index = 2;
            }
            lucky.luckyStart(index);
            new Thread(sendable).start();
        } else {
//            //判断是否停止了旋转
//            if (!lucky.isShouldEnd()) {
//                lucky.luckyEnd();
//            }
        }
    }

    Runnable sendable = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(3);
                //判断是否停止了旋转
                if (!lucky.isShouldEnd()) {
                    lucky.luckyEnd();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
