/**
 * 
 */
package cm.aptoide.summerinternship2011;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * @author rafael
 *
 */
public class GifView extends ImageView{
    	
    	private Movie movie;
    	private InputStream is=null;
    	private long movieStart;
    	private boolean startAnimation;
		
		public GifView(Context context, int resourceGif) {
			super(context);
			is=context.getResources().openRawResource(resourceGif);
			movie=Movie.decodeStream(is);
			movieStart=0;
			startAnimation = true;
		}

		public GifView(Context context) {
			super(context);
			is=null;
			movie=null;
			movieStart=0;
			startAnimation = false;
		}
		
		public GifView(Context context, AttributeSet attrs) {
			super(context, attrs);
			is=null;
			movie=null;
			movieStart=0;
			startAnimation = false;
		}
		
		public GifView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			is=null;
			movie=null;
			movieStart=0;
			startAnimation = false;
		}
		
		public synchronized  void startAnimation(int resourceGif){
			is = super.getContext().getResources().openRawResource(resourceGif);
			movie = Movie.decodeStream(is);
			movieStart = 0;
			if(!startAnimation)
				startAnimation = true;
		}
		
    	@Override
    	protected synchronized void onDraw(Canvas canvas) {
    		if(startAnimation){
    			canvas.drawRGB(0, 0, 0);
	    		super.onDraw(canvas);
	    		long now=android.os.SystemClock.uptimeMillis();
	    		 if (movieStart == 0) { // first time
	                 movieStart = now;
	             }
	    		 int relTime = (int)((now - movieStart) % movie.duration()) ;
	             movie.setTime(relTime);
	             movie.draw(canvas,10,10);
	             this.invalidate();
    		}
    	}
    	
    	public synchronized void stopAnimation() { startAnimation = false; }
    	
    	public synchronized void startAnimation() { startAnimation = true; }
    	
}
