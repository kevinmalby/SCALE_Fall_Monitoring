package com.ics234.scalefalldetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.LinearGradient;
import android.util.AttributeSet;
import android.widget.TextView;
import android.graphics.Shader;

@SuppressLint("DrawAllocation")
public class GradientTextView extends TextView
{
	protected int startColor;
	protected int finishColor;
	
	public int getStartColor() {
		return startColor;
	}
	public void setStartColor(int startColor) {
		this.startColor = startColor;
	}
	public int getFinishColor() {
		return finishColor;
	}
	public void setFinishColor(int finishColor) {
		this.finishColor = finishColor;
	}
	
    public GradientTextView( Context context )
    {
        super( context, null, -1 );
    }
    public GradientTextView( Context context, 
        AttributeSet attrs )
    {
        super( context, attrs, -1 );
    }
    public GradientTextView( Context context, 
        AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
    }
 
    @Override
    protected void onLayout( boolean changed, 
        int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );
        if(changed)
        {
            getPaint().setShader( new LinearGradient( 
                0, 0, getWidth(), 0, 
                startColor, finishColor, 
                Shader.TileMode.CLAMP ) );
        }
    }
}
