package de.darc.dl1xy.sathunter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import com.github.amsacode.predict4java.GroundStationPosition;
import com.github.amsacode.predict4java.TLE;
import de.darc.dl1xy.sathunter.satellite.MetaSatellite;
import de.darc.dl1xy.sathunter.timer.SatTimerManager;
import de.darc.dl1xy.sathunter.timer.TLEZipTimerTask;
import de.darc.dl1xy.sathunter.tle.TLEHandler;
import de.darc.dl1xy.sathunter.util.Debug;
import de.darc.dl1xy.sathunter.util.PropertyReader;

public class SatHunter {

	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static SimpleDateFormat sdf;
	public static Properties props = null;
	public static List<TLE> tleList = null;
	
	private List<MetaSatellite> metaSatList = null;
	private GroundStationPosition gsp;
	private SatTimerManager timerMgr = null;
	
	private Timer globalTimer;
	
	public SatHunter() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {		
	
		Debug.log("Execution started at "+(new Date())+" on "+OS);
		Debug.log("Execution path is: "+System.getProperty("user.dir"));
		
		final SatHunter sh = new SatHunter();
		final PropertyReader pr = new PropertyReader();
		props = pr.readProps(args[0]);
		
	
		sh.init();	
		sh.initTimers();
		
	}
	
	public void init()
	{
		sdf = new SimpleDateFormat(props.getProperty("date_format"));
		final int tleUpdateType = Integer.parseInt(props.getProperty("tle_update_type"));
		
		switch (tleUpdateType)
		{
		case 1:
			final String tleZip = props.getProperty("tle_zip");
			final String tleZipTmp = props.getProperty("tle_tmp_folder");
			Debug.log("Reading TLE from zip:"+tleZip);
			try 
			{
				tleList = TLEHandler.parseTLEFromZip(tleZip, tleZipTmp);
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			
			final String tleFile = props.getProperty("tle_file");
			Debug.log("Reading TLE from file:"+tleFile);
			try 
			{
				tleList = TLEHandler.parseTLEFromFile(tleFile);
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
		final String sats = props.getProperty("sats");
		metaSatList = MetaSatellite.getMetaSatellites(sats, tleList);
		
		final double latitude=Double.parseDouble(props.getProperty("latitude"));
		final double longitude=Double.parseDouble(props.getProperty("longitude"));
		final double heightAMSL=Double.parseDouble(props.getProperty("heightAMSL"));
		
		gsp = new GroundStationPosition(latitude, longitude, heightAMSL); 
		
		//Debug.log(createCmd(metaSatList.get(0), false));
	}
	
	private void initTimers()
	{
		final int tleUpdateHours = Integer.parseInt(props.getProperty("tle_update_hours"));
		final long tleUpdateMilliseconds = tleUpdateHours * 60 * 60 * 1000;
		
		timerMgr = new SatTimerManager();
		timerMgr.createSatTimerTasks(metaSatList, gsp, tleUpdateHours, false, true);
		timerMgr.setStatus(SatTimerManager.STATUS_RUNNING);
		timerMgr.run();		
		
		// set global timer (TLE update)
		globalTimer = new Timer();
		final TLEZipTimerTask tleZipTimerTask = new TLEZipTimerTask(timerMgr, gsp, metaSatList, tleUpdateHours);
		globalTimer.schedule(tleZipTimerTask, tleUpdateMilliseconds, tleUpdateMilliseconds);
	}
	
	public static String createCaptureCmd(MetaSatellite sat, boolean saveToBin)
	{
		final String freq = String.valueOf(sat.downlinkFrequency);
		final String satName = sat.name.replace(" ", "_")+"_"+sdf.format(new Date()); 
		if (OS.indexOf("win") >= 0)
			return "cmd.exe /c run.bat "+System.getProperty("user.dir")+" "+freq+" "+satName;
		return "";
	}
	
	public static String createAptCmd(MetaSatellite sat)
	{
		final String wavName = sat.name.replace(" ", "_")+"_"+sdf.format(new Date())+".wav"; 
		if (OS.indexOf("win") >= 0)
			return "cmd.exe /c apt.bat "+System.getProperty("user.dir")+" "+wavName;
		return "";
	}
}
