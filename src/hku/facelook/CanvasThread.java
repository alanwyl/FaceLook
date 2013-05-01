package hku.facelook;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class CanvasThread extends Thread{
	private SurfaceHolder _surfaceHolder;
    private CameraPreview _panel;
    private boolean _run = false;


    public CanvasThread (SurfaceHolder surfaceHolder, CameraPreview panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }


    public void setRunning(boolean run) { //Allow us to stop the thread
        _run = run;
    }

    @Override
    public void run() {
        Canvas c;
        while (_run) {     //When setRunning(false) occurs, _run is 
            c = null;      //set to false and loop ends, stopping thread

            try {
            	c = _surfaceHolder.lockCanvas();
            	if (c != null) {
	                synchronized (_surfaceHolder) {
						 //Insert methods to modify positions of items in onDraw()
	                	_panel.onDraw(c);
	                }
                }
            } finally {
            	if (c == null) {
            		Log.e("NULL", "null canvas");
            		setRunning(false);
            	} else {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
