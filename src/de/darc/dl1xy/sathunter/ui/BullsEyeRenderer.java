package de.darc.dl1xy.sathunter.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.github.amsacode.predict4java.GroundStationPosition;
import com.github.amsacode.predict4java.PassPredictor;
import com.github.amsacode.predict4java.SatNotFoundException;
import com.github.amsacode.predict4java.SatPassTime;
import com.github.amsacode.predict4java.SatPos;
import de.darc.dl1xy.sathunter.SatHunter;
import de.darc.dl1xy.sathunter.satellite.MetaSatellite;
import de.darc.dl1xy.sathunter.util.Coordinate;

public class BullsEyeRenderer {

	private static final double ZOOM_FACTOR = 5.5;
	// gfx stuff
	private static final int IMG_WIDTH=512;
	private static final int IMG_HEIGHT=512;
	private static final int IMG_WIDTH_HALF = IMG_WIDTH / 2;
	private static final int IMG_HEIGHT_HALF = IMG_HEIGHT / 2;
	private static final int SPOT_SIZE = 8;
	private static final Color BG_COLOR = Color.WHITE;
	private static final Color FG_COLOR = Color.BLACK;
	private static final  BasicStroke STROKE = new BasicStroke(2.0f);
	
	private BufferedImage img;
	private Graphics2D gfx;
	
	// sat stuff
	private MetaSatellite metaSat;
	private GroundStationPosition gsp;
	private SatPassTime spt;
	private PassPredictor passPredictor;
	private double minElevation = 0;
	private static final String NL = "\n";
	
	public BullsEyeRenderer() {
		
		
	}

	public void createImage(final MetaSatellite metaSat, final GroundStationPosition gsp, final SatPassTime spt, final double minElevation)
	{
		this.metaSat = metaSat;
		this.gsp = gsp;
		this.spt = spt;
		this.minElevation = minElevation;
		
		try {
			this.passPredictor = new PassPredictor(metaSat.sat.getTLE(), gsp);
		} catch (IllegalArgumentException | SatNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.img = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		this.gfx = img.createGraphics();
				
		gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gfx.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gfx.setBackground(BG_COLOR);
		gfx.clearRect(0, 0, img.getWidth(), img.getHeight());
		this.createBullsEye(gfx);
		this.createPasses(gfx); 
		// rotate here
		this.createInfo(gfx);		
		this.createDesc();
		this.writeImg(img);		
	}
	
	private void createBullsEye(Graphics2D gfx)
	{
		gfx.setPaint(FG_COLOR);
		gfx.setStroke(STROKE);
		
		// Lines
		gfx.draw(new Line2D.Double(IMG_WIDTH_HALF-STROKE.getLineWidth()/2, IMG_WIDTH/64+16, IMG_WIDTH_HALF-STROKE.getLineWidth()/2, IMG_HEIGHT-IMG_WIDTH/64-16)); // vertical
		gfx.draw(new Line2D.Double(IMG_WIDTH/64+16, IMG_HEIGHT_HALF-STROKE.getLineWidth()/2, IMG_WIDTH-IMG_WIDTH/64-16, IMG_HEIGHT_HALF-STROKE.getLineWidth()/2)); // horizontal
		
		// circles
		gfx.drawOval(IMG_WIDTH/16+16,IMG_HEIGHT/16+16,IMG_WIDTH-IMG_WIDTH/8-32,IMG_HEIGHT-IMG_HEIGHT/8-32);
		gfx.drawOval(IMG_WIDTH/4+16,IMG_HEIGHT/4+16,IMG_WIDTH-IMG_WIDTH_HALF-32,IMG_HEIGHT-IMG_HEIGHT_HALF-32);		
	}
	
	private void createDesc()
	{
		// North
		final Font font = new Font("Arial", Font.BOLD, 18);
		gfx.setFont(font);
		final FontMetrics fontMetrics = gfx.getFontMetrics();
		
		final String northMsg = "N";
		int stringWidth = fontMetrics.stringWidth(northMsg);
		int stringHeight = fontMetrics.getAscent();
		gfx.drawString(northMsg, (IMG_WIDTH - stringWidth) / 2, 8 + stringHeight/2);
		
		final String southMsg = "S";
		stringWidth = fontMetrics.stringWidth(southMsg);
		gfx.drawString(southMsg, (IMG_WIDTH - stringWidth) / 2, IMG_HEIGHT + 8 - stringHeight/2);
		
		final String westMsg = "W";
		stringWidth = fontMetrics.stringWidth(westMsg);
		gfx.drawString(westMsg, stringWidth /4-4, IMG_HEIGHT/2+stringHeight/4);
		
		final String eastMsg = "E";
		stringWidth = fontMetrics.stringWidth(eastMsg);
		gfx.drawString(eastMsg, IMG_WIDTH-stringWidth, IMG_HEIGHT/2+stringHeight/4);
	}
	
	private void createInfo(final Graphics2D gfx)
	{
		final Font font = new Font("Arial", Font.PLAIN, 10);
		gfx.setFont(font);
		String txt = metaSat.name+"\nAOS: "+spt.getStartTime()+NL;
		txt += "LOS: "+spt.getEndTime()+NL;
		txt += "TCA: "+spt.getTCA()+NL;
		txt += "Max El: "+(int)spt.getMaxEl()+NL;
		txt += "Min Def El: "+minElevation +NL;
		txt += "AOS Az: "+spt.getAosAzimuth()+NL;
		txt += "LOS Az: "+spt.getLosAzimuth();
		
		drawString(gfx, txt, 4, 4);
		/*
		String txt2 = "Coordinates:"+gsp.getLatitude()+":"+gsp.getLongitude();
		drawString(gfx, txt2, 16, IMG_HEIGHT-16);
		*/
	}
	
	private void createPasses(final Graphics2D gfx)
	{
		final BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imageGraphics = image.createGraphics();
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		final Date startDate = spt.getStartTime();
		final Date endDate = spt.getEndTime();
		
		final Point2D.Double myPos = this.coordinateToPoint(new Coordinate(gsp.getLongitude(), gsp.getLatitude()));
		
		final List<SatPos> betweenPos = getSatPosBetweenDates (startDate, endDate, 8 );
		final List<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		for (SatPos p:betweenPos)
		{
			final Point2D.Double pPos = this.coordinateToPoint(new Coordinate(p.getLongitude() / (Math.PI * 2.0) * 360 , p.getLatitude()/ (Math.PI * 2.0) * 360 ));
			final Point2D.Double pPosNorm = new Point2D.Double(pPos.x - myPos.x, pPos.y - myPos.y);
			final Point2D.Double pPosNormFinal = new Point2D.Double(pPosNorm.x * ZOOM_FACTOR, pPosNorm.y * ZOOM_FACTOR);
		
			//Debug.log("pPosNorm:"+pPosNorm+" pPosNormFinal:"+pPosNormFinal);
			
			Waypoint w = new Waypoint();
			w.coord = pPosNormFinal;
			w.elevation = p.getElevation();
			w.time = p.getTime();
			
			try {
				if (metaSat.downlinkFrequency > 0 && passPredictor != null)
					w.downlinkFrequency = passPredictor.getDownlinkFreq(metaSat.downlinkFrequency , p.getTime());
				if (metaSat.uplinkFrequency > 0 && passPredictor != null)
					w.uplinkFrequency = passPredictor.getDownlinkFreq(metaSat.uplinkFrequency , p.getTime());
			} catch (SatNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			waypoints.add(w);
		}
       
		int numWaypoints = waypoints.size();
		for (int i=0; i < numWaypoints; i++)
		{
			imageGraphics.setPaint(Color.BLACK);
			
			if (i==0)
				imageGraphics.setPaint(Color.BLUE);
			
			final Waypoint w = waypoints.get(i);
			
			if (Math.toDegrees(w.elevation) >=  minElevation)
				imageGraphics.setPaint(Color.RED);
			
			if (i==numWaypoints-1)
				imageGraphics.setPaint(Color.BLUE);
			
			imageGraphics.drawArc((int)w.coord.x + IMG_WIDTH_HALF -(SPOT_SIZE/2), (int)w.coord.y + IMG_HEIGHT_HALF -(SPOT_SIZE/2), SPOT_SIZE, SPOT_SIZE, 0, 360);
			
			final String date = dateFormat.format(w.time);
			final Font font = new Font("Arial", Font.PLAIN, 9);
			imageGraphics.setFont(font);
			
			final AffineTransform orig = imageGraphics.getTransform();
			
	        double angle = Math.toRadians(90.0);
	        imageGraphics.rotate(angle, (int)w.coord.x + IMG_WIDTH_HALF, (int)w.coord.y + IMG_HEIGHT_HALF);
	        String info = date;
	        if (w.downlinkFrequency > 0f)
	        	info+=" DOWN: "+String.format(Locale.US, "%.4f", (w.downlinkFrequency / 1000000))+"MHz";
	        if (w.uplinkFrequency > 0f)
	        	info+=" UP: "+String.format(Locale.US, "%.4f", (w.uplinkFrequency / 1000000))+"MHz";
	        imageGraphics.drawString(info, (int)w.coord.x + IMG_WIDTH_HALF - 32 , (int)w.coord.y + IMG_HEIGHT_HALF + 16);
	        imageGraphics.setTransform(orig);
		}
		imageGraphics.dispose();
		
		final AffineTransform orig = gfx.getTransform();
		final double angle = Math.toRadians(270.0);
		
		gfx.rotate(angle,IMG_WIDTH_HALF,IMG_HEIGHT_HALF);
		gfx.drawImage(image, 0,0,IMG_WIDTH,IMG_HEIGHT,null);
		gfx.setTransform(orig);
	}
	
	private void writeImg(BufferedImage img)
	{
		final String startTime = SatHunter.sdf.format(spt.getStartTime());
		final String endTime = SatHunter.sdf.format(spt.getEndTime());
		try {
			ImageIO.write(img, "PNG", new File(System.getProperty("user.dir")+"\\output\\predict\\"+metaSat.name.replace(" ", "_")+"_"+startTime+"_"+endTime+".PNG"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 private void drawString(Graphics g, String text, int x, int y) {
         for (String line : text.split("\n"))
             g.drawString(line, x, y += g.getFontMetrics().getHeight());
     }
	 
	 private List<SatPos> getSatPosBetweenDates (Date startDate, Date endDate, int iterations)
	 {
		 long startDateMS = startDate.getTime();
		 
		 final List<SatPos> result = new ArrayList<SatPos>(); 
		 final long duration = endDate.getTime() - startDateMS;
		 final long interval = duration / iterations;
		 
		 for (int i=0; i < iterations; i++)
		 {			
			 final SatPos satPos = metaSat.sat.getPosition(gsp, new Date(startDateMS));
			 result.add(satPos);
			 startDateMS += interval;
		 }
		 return result;
	 }
	
	 private Point2D.Double coordinateToPoint(Coordinate coord)
	 {
		 return new Point2D.Double(coord.getLon(), coord.getLat());
	 }
	 
	 class Waypoint
	 {
	 	 public Point2D.Double coord;
	 	 public double elevation;
	 	 public Date time;
	 	 public double uplinkFrequency;
	 	 public double downlinkFrequency;
	 }
}


