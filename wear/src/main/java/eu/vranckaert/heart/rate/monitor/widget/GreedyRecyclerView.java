package eu.vranckaert.heart.rate.monitor.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
* Date: 29/05/15
* Time: 13:28
*
* @author Dirk Vranckaert
*/
public class GreedyRecyclerView extends RecyclerView {
   private float mStartX;
   private float mStartY;
   private boolean mPossibleVerticalSwipe;
   private boolean mGestureDirectionLocked;
   private final int mTouchSlop;

   public GreedyRecyclerView(Context context) {
       this(context, null);
   }

   public GreedyRecyclerView(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
   }

   public GreedyRecyclerView(Context context, AttributeSet attrs, int defStyle) {

       super(context, attrs, defStyle);
       ViewConfiguration vc = ViewConfiguration.get(context);
       this.mTouchSlop = vc.getScaledTouchSlop();
   }

   public boolean onInterceptTouchEvent(MotionEvent event) {
       if(this.getChildCount() > 0) {
           int action = event.getActionMasked();
           if(action == 0) {
               this.mStartX = event.getX();
               this.mStartY = event.getY();
               this.mPossibleVerticalSwipe = true;
               this.mGestureDirectionLocked = false;
           } else if(action == 2 && this.mPossibleVerticalSwipe) {
               this.handlePossibleVerticalSwipe(event);
           }

           this.getParent().requestDisallowInterceptTouchEvent(this.mPossibleVerticalSwipe);
       }

       return super.onInterceptTouchEvent(event);
   }

   private boolean handlePossibleVerticalSwipe(MotionEvent event) {
       if(this.mGestureDirectionLocked) {
           return this.mPossibleVerticalSwipe;
       } else {
           float deltaX = Math.abs(this.mStartX - event.getX());
           float deltaY = Math.abs(this.mStartY - event.getY());
           float distance = deltaX * deltaX + deltaY * deltaY;
           if(distance > (float)(this.mTouchSlop * this.mTouchSlop)) {
               if(deltaX > deltaY) {
                   this.mPossibleVerticalSwipe = false;
               }

               this.mGestureDirectionLocked = true;
           }

           return this.mPossibleVerticalSwipe;
       }
   }

   public boolean onTouchEvent(MotionEvent event) {
       boolean result = super.onTouchEvent(event);
       if(this.getChildCount() > 0) {
           int action = event.getActionMasked();
           if(action == MotionEvent.ACTION_DOWN) {
               // NA this.handleTouchDown(event);
           } else if(action == MotionEvent.ACTION_UP) {
               // NA this.handleTouchUp(event, scrollState);
               this.getParent().requestDisallowInterceptTouchEvent(false);
           } else if(action == MotionEvent.ACTION_MOVE) {
               result |= this.handlePossibleVerticalSwipe(event);
               this.getParent().requestDisallowInterceptTouchEvent(this.mPossibleVerticalSwipe);
           } else if(action == MotionEvent.ACTION_CANCEL) {
               this.getParent().requestDisallowInterceptTouchEvent(false);
           }
       }

       return result;
   }
}