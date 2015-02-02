package de.darc.dl1xy.sathunter.timer;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;

import uk.me.g4dpz.satellite.SatPassTime;
import de.darc.dl1xy.sathunter.SatHunter;
import de.darc.dl1xy.sathunter.satellite.MetaSatellite;
import de.darc.dl1xy.sathunter.util.Debug;

public class SatTimerTask extends TimerTask {

	public static final int STATUS_WAITING = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_STOPPED = 2;
	
	protected MetaSatellite metaSat;
	protected SatPassTime passTime;
	protected long duration;
	protected int status;

	public SatTimerTask(final MetaSatellite metaSat, final SatPassTime passTime) {
		
		this.metaSat = metaSat;
		this.passTime = passTime;
		this.duration = passTime.getEndTime().getTime() - passTime.getStartTime().getTime();
		this.status = SatTimerTask.STATUS_WAITING;
	}


	@Override
	public void run() {
		this.setStatus(STATUS_RUNNING);
				
		File log = new File("log_"+SatHunter.sdf.format(passTime.getStartTime())+".txt");
		
		this.processCaptureCmd(log);
		this.processAptCmd(log);
	}
	
	private void processCaptureCmd(File log)
	{
		final String cmd = SatHunter.createCaptureCmd(metaSat, false);
		//final String cmd = SatHunter.createCaptureCmd(metaSat, false);
		Debug.log("SatTimerTask for Sat "+this.metaSat.name+" started at "+(new Date())+", official AOS:"+this.passTime.getStartTime()+" EOS:"+this.passTime.getEndTime()+" duration:"+duration);
		//System.out.println("Capture Command:"+cmd);
		
		try {
			String[] splitCmd = cmd.split(" ");
			ProcessBuilder pb = new ProcessBuilder(splitCmd);
			Map<String, String> env = pb.environment();
			if (env != null)
				env.putAll(env);			 
			
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(log));
			
			Process proc = pb.start();
			Thread.sleep(this.duration);
			proc.destroy();
			// HACK to kill the process
			if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) 
				Runtime.getRuntime().exec("taskkill /F /IM rtl_fm.exe");
			   else
				   Runtime.getRuntime().exec("kill -9 rtl_fm");
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processAptCmd(File log)
	{
		final String cmd = SatHunter.createAptCmd(metaSat);
		//final String cmd = SatHunter.createCaptureCmd(metaSat, false);
		
		Debug.log("Command:"+cmd);
		
		try {
			String[] splitCmd = cmd.split(" ");
			ProcessBuilder pb = new ProcessBuilder(splitCmd);
			Map<String, String> env = pb.environment();
			if (env != null)
				env.putAll(env);			 
			
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(log));
			
			Process procRtlFm = pb.start();
			procRtlFm.waitFor();
			procRtlFm.destroy();
			/*
			if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) 
				Runtime.getRuntime().exec("taskkill /F /IM rtl_fm.exe");
			   else
				   Runtime.getRuntime().exec("kill -9 rtl_fm");
			*/
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long getDuration()
	{
		return duration;
	}
	
	public MetaSatellite getMetaSatellite()
	{
		return metaSat;
	}
	
	public SatPassTime getSatPassTime()
	{
		return passTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
