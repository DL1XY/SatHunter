package de.darc.dl1xy.sathunter.timer;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import uk.me.g4dpz.satellite.GroundStationPosition;
import de.darc.dl1xy.sathunter.SatHunter;
import de.darc.dl1xy.sathunter.satellite.MetaSatellite;
import de.darc.dl1xy.sathunter.tle.TLEHandler;

public class TLEZipTimerTask extends TimerTask {

	private List<MetaSatellite> metaSatList;
	private GroundStationPosition gsp;
	private SatTimerManager timerMgr;
	private int hours;
	
	public TLEZipTimerTask(final SatTimerManager timerManager, final GroundStationPosition gsp, final List<MetaSatellite> metaSatList, final int hours) {
		this.timerMgr = timerManager;
		this.gsp = gsp;
		this.metaSatList = metaSatList;
		this.hours = hours;
	}

	@Override
	public void run() {
		System.out.println("TLEZipTimerTask updates TLE");
		timerMgr.stopAll();
		
		final String tleZip = SatHunter.props.getProperty("tle_zip");
		final String tleZipTmp = SatHunter.props.getProperty("tle_tmp_folder");
		System.out.println("Reading TLE from zip:"+tleZip);
		try 
		{
			SatHunter.tleList = TLEHandler.parseTLEFromZip(tleZip, tleZipTmp);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		timerMgr.createSatTimerTasks(metaSatList, gsp, hours, false, true);
		timerMgr.setStatus(SatTimerManager.STATUS_RUNNING);
	}

}
