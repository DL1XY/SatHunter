package de.darc.dl1xy.sathunter.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import de.darc.dl1xy.sathunter.SatHunter;
import de.darc.dl1xy.sathunter.satellite.MetaSatellite;
import de.darc.dl1xy.sathunter.ui.BullsEyeRenderer;

public class SatTimerManager implements Runnable{

	public final static int STATUS_INIT = 0;
	public final static int STATUS_RUNNING = 1;
	
	private List<SatTimerTask> satTimerTasks;
	
	private long intervall = 0;
	private long lastExec = 0;
	private Timer timer;
	private int status = STATUS_INIT;
	private double minElevation = 0;
	
	public SatTimerManager() {
		this.minElevation = Double.parseDouble(SatHunter.props.getProperty("min_elevation"));
		this.intervall = Long.parseLong(SatHunter.props.getProperty("timer_intervall"));
		this.timer = new Timer();
	}


	public void createSatTimerTasks(final List<MetaSatellite> metaSatList, final GroundStationPosition gsp, final int hours, final boolean overlapping, final boolean onlyStrongSignals)
	{		
		satTimerTasks = new ArrayList<SatTimerTask>();
		BullsEyeRenderer ber = new BullsEyeRenderer();
		for (MetaSatellite metaSat:metaSatList)
		{
			
			try 
			{
				final PassPredictor pp = new PassPredictor(metaSat.sat.getTLE(), gsp);
				final List<SatPassTime> spts = pp.getPasses(new Date(), hours, false);
				for (SatPassTime spt:spts)
				{
					final SatTimerTask satTimerTask = new SatTimerTask(metaSat, spt);
					satTimerTasks.add(satTimerTask);		
					
				}
			} 
			catch (IllegalArgumentException | InvalidTleException
					| SatNotFoundException e) 
			{
			
				e.printStackTrace();
			}			
		}
		// cleanup
		if (onlyStrongSignals)
			this.removeWeakTimerTasks();
		
		if (!overlapping)
			this.removeOverlappingTimerTasks();
			
		
		// create schedulers & imgs
		for (SatTimerTask task:satTimerTasks)
		{
			final SatPassTime spt = task.getSatPassTime();
			if (!onlyStrongSignals)
				ber.createImage(task.getMetaSatellite(), gsp, spt, 0f);
			else
				ber.createImage(task.getMetaSatellite(), gsp, spt, minElevation);
			System.out.println("Add TimerTask for Satellite "+task.getMetaSatellite().name+" AOS:"+task.getSatPassTime().getStartTime());
			/*
			System.out.println(spt);
			System.out.println("###");
			*/
			timer.schedule(task, spt.getStartTime());
		}
		
		System.out.println(satTimerTasks.size() + " passes added !");
	}
	
	// kick overlapping and weak signal passes
	private void removeOverlappingTimerTasks()
	{
		final List<SatTimerTask> tmpTimers = new ArrayList<SatTimerTask>();
		Date oldEndDate = null;
		for (SatTimerTask task:satTimerTasks)
		{
			if (oldEndDate == null) // first iteration
			{
				oldEndDate = task.passTime.getEndTime();
				tmpTimers.add(task);
				continue;
			}
			
			if (task.passTime.getStartTime().compareTo(oldEndDate) < 0) // overlapping, startDate is < oldEndDate
			{
				continue;
			}
			else
			{
				oldEndDate = task.passTime.getEndTime();
				tmpTimers.add(task);
			}
		}
		satTimerTasks.clear();
		satTimerTasks = tmpTimers;
	}
	
	private void removeWeakTimerTasks()
	{
		
		//System.out.println("minElev:"+minElev);
		final List<SatTimerTask> tmpTimers = new ArrayList<SatTimerTask>();
		for (SatTimerTask task:satTimerTasks)
		{
			final SatPassTime passTime = task.passTime;			
			
			if (passTime.getMaxEl() >= minElevation)
			{
				tmpTimers.add(task);
			}
		}
		satTimerTasks.clear();
		satTimerTasks = tmpTimers;
	}

	@Override
	public void run() 
	{
		while(true)// endless loop
		{
			if (status != STATUS_INIT)
			{
				final Date now = new Date();
				if (lastExec + intervall < now.getTime())
				{
					lastExec = now.getTime();
					
					for (SatTimerTask task:satTimerTasks)
					{
						switch (task.getStatus())
						{
							case SatTimerTask.STATUS_STOPPED:
							{
								continue;
							}						
							case SatTimerTask.STATUS_RUNNING:
							{
								if (now.compareTo(task.passTime.getEndTime()) >= 0) // done
								{
									System.out.println("Task of "+task.metaSat.name+" stopped "+now);
									task.setStatus(SatTimerTask.STATUS_STOPPED);
									task.cancel();
								}
							}
							break;
							case SatTimerTask.STATUS_WAITING:
							{
								continue;
							}
							
						}
					}
				}
			}
		}
	}
	
	public Timer getTimer()
	{
		return timer;
	}
	
	public void stopAll()
	{
		int runningTasks = 0;
		int waitingTasks = 0;
		int stoppedTasks = 0;
		for (SatTimerTask task:satTimerTasks)
		{
			if (task.getStatus() == SatTimerTask.STATUS_RUNNING)
			{
				runningTasks++;
			}
			if (task.getStatus() == SatTimerTask.STATUS_WAITING)
			{
				waitingTasks++;
			}
			if (task.getStatus() == SatTimerTask.STATUS_STOPPED)
			{
				stoppedTasks++;
			}
			task.cancel();
			task = null;
		}
		this.status = STATUS_INIT;
		System.out.println("SatTimerManager cancelled "+runningTasks+" runningTasks, "+waitingTasks+" waitingTasks and "+stoppedTasks+" stoppedTasks");
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
}
