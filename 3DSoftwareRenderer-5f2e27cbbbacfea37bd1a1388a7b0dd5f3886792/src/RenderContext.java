public class RenderContext extends Bitmap
{
	private final int m_scanBuffer[];

	public RenderContext(int width, int height)
	{
		super(width, height);
		m_scanBuffer = new int[height * 2];
	}

	public void DrawScanBuffer(int yCoord, int xMin, int xMax)
	{
		m_scanBuffer[yCoord * 2    ] = xMin;
		m_scanBuffer[yCoord * 2 + 1] = xMax;
	}
	
	public void FillShape(int yMin, int yMax)
	{
		for(int j = yMin; j < yMax; j++)
		{
			int xMin = m_scanBuffer[j * 2];
			int xMax = m_scanBuffer[j * 2 + 1];

			for(int i = xMin; i < xMax; i++)
			{
				DrawPixel(i, j, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF);
			}
		}
	}

	public void FillTriangle(Vertex v1, Vertex v2, Vertex v3, Boolean Wireframe)
	{
		Matrix4f screenSpaceTransform = 
				new Matrix4f().InitScreenSpaceTransform(GetWidth()/2, GetHeight()/2);
		Vertex minYVert = v1.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex midYVert = v2.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex maxYVert = v3.Transform(screenSpaceTransform).PerspectiveDivide();

		if(maxYVert.GetY() < midYVert.GetY())
		{
			Vertex temp = maxYVert;
			maxYVert = midYVert;
			midYVert = temp;
		}

		if(midYVert.GetY() < minYVert.GetY())
		{
			Vertex temp = midYVert;
			midYVert = minYVert;
			minYVert = temp;
		}

		if(maxYVert.GetY() < midYVert.GetY())
		{
			Vertex temp = maxYVert;
			maxYVert = midYVert;
			midYVert = temp;
		}

		float area = minYVert.TriangleAreaTimesTwo(maxYVert, midYVert);
		int handedness = area >= 0 ? 1 : 0;

		ScanConvertTriangle(minYVert, midYVert, maxYVert, handedness);
		if (!Wireframe) {
			FillShape((int)minYVert.GetY(), (int)maxYVert.GetY());}
		else{
			plotLine(minYVert, maxYVert);
			plotLine(minYVert, midYVert);
			plotLine(midYVert, maxYVert);
		}
		
	}
	
	public void FillBezTriangle(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex v5, Vertex v6, Boolean Wireframe)
	{
		Matrix4f screenSpaceTransform = 
				new Matrix4f().InitScreenSpaceTransform(GetWidth()/2, GetHeight()/2);
		Vertex minYVert = v1.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex midYVert = v2.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex maxYVert = v3.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex minControl = v1.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex midControl = v2.Transform(screenSpaceTransform).PerspectiveDivide();
		Vertex maxControl = v3.Transform(screenSpaceTransform).PerspectiveDivide();

		if(maxYVert.GetY() < midYVert.GetY())
		{
			Vertex temp = maxYVert;
			maxYVert = midYVert;
			midYVert = temp;
		}

		if(midYVert.GetY() < minYVert.GetY())
		{
			Vertex temp = midYVert;
			midYVert = minYVert;
			minYVert = temp;
		}

		if(maxYVert.GetY() < midYVert.GetY())
		{
			Vertex temp = maxYVert;
			maxYVert = midYVert;
			midYVert = temp;
		}

		float area = minYVert.TriangleAreaTimesTwo(maxYVert, midYVert);
		int handedness = area >= 0 ? 1 : 0;

		//ScanConvertTriangle(minYVert, midYVert, maxYVert, handedness);
		if (!Wireframe) {
			FillShape((int)minYVert.GetY(), (int)maxYVert.GetY());}
		else{

		}
	}

	public void ScanConvertTriangle(Vertex minYVert, Vertex midYVert, 
			Vertex maxYVert, int handedness)
	{
		ScanConvertLine(minYVert, maxYVert, 0 + handedness);
		ScanConvertLine(minYVert, midYVert, 1 - handedness);
		ScanConvertLine(midYVert, maxYVert, 1 - handedness);
		//plotQuadBezierSeg(maxYVert,10,10, midYVert);
		//plotPoint(minYVert,1);
		plotPoint(maxYVert,8);
		//plotPoint(midYVert,2);

		plotQuadBezierSegs(minYVert, (int)((minYVert.GetX() + maxYVert.GetX())*0.5), (int)((maxYVert.GetY() + maxYVert.GetY())*0.5), maxYVert);
		plotQuadBezierSegs(minYVert,  (int)((minYVert.GetX() + midYVert.GetX())*0.5), (int)((midYVert.GetY() + midYVert.GetY())*0.5), midYVert);
		plotQuadBezierSegs(midYVert,   (int)((midYVert.GetX() + maxYVert.GetX())*0.5), (int)((maxYVert.GetY() + maxYVert.GetY())*0.5), maxYVert);
		Vertex controlmin = new Vertex(1, 0, 1);
		Vertex controlmid = new Vertex(0, 1, 0);
		Vertex controlmax = new Vertex(1, -1, 0);
	}

	private void ScanConvertLine(Vertex minYVert, Vertex maxYVert, int whichSide)
	{
		int yStart = (int)minYVert.GetY();
		int yEnd   = (int)maxYVert.GetY();
		int xStart = (int)minYVert.GetX();
		int xEnd   = (int)maxYVert.GetX();

		int yDist = yEnd - yStart;
		int xDist = xEnd - xStart;

		if(yDist <= 0)
		{
			return;
		}

		float xStep = (float)xDist/(float)yDist;
		float curX = (float)xStart;

		for(int j = yStart; j < yEnd; j++)
		{
			m_scanBuffer[j * 2 + whichSide] = (int)curX;
			curX += xStep;
		}
	}
	
	void plotLine(Vertex minYVert, Vertex maxYVert)
	{
		int y0 = (int)minYVert.GetY();
		int y1   = (int)maxYVert.GetY();
		int x0 = (int)minYVert.GetX();
		int x1   = (int)maxYVert.GetX();

		int dx =  Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
		int dy = -Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
		int err = dx+dy, e2; /* error value e_xy */
	  
		for(;;){  /* loop */
			DrawPixel(x0, y0, 255, 0, 0, 255);
			if (x0==x1 && y0==y1) break;
			e2 = 2*err;
			if (e2 >= dy) { err += dy; x0 += sx; } /* e_xy+e_x > 0 */
			if (e2 <= dx) { err += dx; y0 += sy; } /* e_xy+e_y < 0 */
		}
	}

	void plotQuadBezierSegs(Vertex minYVert, int x1, int y1, Vertex maxYVert)
		{                            
		int x0 = (int)minYVert.GetX();
		int y0 = (int)minYVert.GetY();
		int x2 = (int)maxYVert.GetX();
		int y2 = (int)maxYVert.GetY();

		Vertex a = new Vertex(x2, y2, 0);//point2
		Vertex b = new Vertex(x1, y1, 0); //bez cuve point
		Vertex c = new Vertex(x0, y0, 0);//point1
		//DrawPixel(x0, 0, 255, 255, 0, 255);
		//DrawPixel(x1, 0, 255, 255, 0, 255);
		plotPoint(a, 6);
		plotPoint(b,10);
		plotPoint(c, 9);
		//plotLine();

		int sx = x2-x1, sy = y2-y1;
		long xx = x0-x1, yy = y0-y1, xy;         /* relative values for checks */
		double dx, dy, err, cur = xx*sy-yy*sx;                    /* curvature */

		assert(xx*sx <= 0 && yy*sy <= 0);  /* sign of gradient must not change */

		if (sx*(long)sx+sy*(long)sy > xx*xx+yy*yy) { /* begin with longer part */ 
			x2 = x0; x0 = sx+x1; y2 = y0; y0 = sy+y1; cur = -cur;  /* swap P0 P2 */
		}  
		if (cur != 0) {                                    /* no straight line */
			xx += sx; xx *= sx = x0 < x2 ? 1 : -1;           /* x step direction */
			yy += sy; yy *= sy = y0 < y2 ? 1 : -1;           /* y step direction */
			xy = 2*xx*yy; xx *= xx; yy *= yy;          /* differences 2nd degree */
			if (cur*sx*sy < 0) {                           /* negated curvature? */
			xx = -xx; yy = -yy; xy = -xy; cur = -cur;
			}
			dx = 4.0*sy*cur*(x1-x0)+xx-xy;             /* differences 1st degree */
			dy = 4.0*sx*cur*(y0-y1)+yy-xy;
			xx += xx; yy += yy; err = dx+dy+xy;                /* error 1st step */    
			do {                              
			DrawPixel(x0, y0, 255, 0, 0, 255);

			/* plot curve */
			if (x0 == x2 && y0 == y2) return;  /* last pixel -> curve finished */
			y1 = (2*err < dx) ? 1 : 0;                  /* save value for test of y step */
			if (2*err > dy) { x0 += sx; dx -= xy; err += dy += yy; } /* x step */
			if (    y1 == 1) { y0 += sy; dy -= xy; err += dx += xx; } /* y step */
			} while (dy < dx );           /* gradient negates -> algorithm fails */
		}
			//plotLine(a, c);//plotLine(x0,y0, x2,y2);                  /* plot remaining part to end */

		}  
	private void plotPoint(Vertex minYVert, int r){
		int xm = (int)minYVert.GetX();
		int ym = (int)minYVert.GetY();
		int x = -r, y = 0, err = 2-2*r; /* II. Quadrant */ 
		do {
			DrawPixel(xm-x, ym+y, 255, 0, 255, 255); /*   I. Quadrant */
			DrawPixel(xm-y, ym-x, 255, 0, 255, 255); /*  II. Quadrant */
			DrawPixel(xm+x, ym-y, 255, 0, 255, 255); /* III. Quadrant */
			DrawPixel(xm+y, ym+x, 255, 0, 255, 255); /*  IV. Quadrant */
			r = err;
			if (r <= y) err += ++y*2+1;           /* e_xy+e_y < 0 */
			if (r > x || err > y) err += ++x*2+1; /* e_xy+e_x > 0 or no 2nd y-step */
		} while (x < 0);
		//DrawPixel((int)minYVert.GetX(), (int)minYVert.GetY(), 255, 0, 0, 255);
	}
}
