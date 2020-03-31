package de.darc.dl1xy.sathunter.satellite;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.github.amsacode.predict4java.Satellite;
import com.github.amsacode.predict4java.SatelliteFactory;
import com.github.amsacode.predict4java.TLE;
import de.darc.dl1xy.sathunter.tle.TLEHandler;

public class MetaSatellite {

	public String name;
	public long downlinkFrequency;
	public long uplinkFrequency;
	public Satellite sat;
	
	public MetaSatellite() 
	{
		
	}
		
	public String toString()
	{
		return "MetaSatellite name:"+name+" with freq:"+downlinkFrequency;
	}
	
	public static List<MetaSatellite> getMetaSatellites(final String propertyString, final List<TLE> tleData)
	{
		final List<MetaSatellite> result = new ArrayList<MetaSatellite>();
		final String[] segs = propertyString.split( Pattern.quote( "," ) );
		
		for (String segment:segs)
		{
			final String[] subSeq = segment.split( Pattern.quote( ":" ) );
			final String satName = subSeq[0];
			String downFreq = "0";
			String upFreq = "0";
			if (subSeq.length > 1)
				downFreq = subSeq[1];
			if (subSeq.length > 2)
				upFreq = subSeq[2];
			final TLE satTLE = TLEHandler.findTLEByName(tleData, satName);
			if (satTLE != null)
			{
				final MetaSatellite metaSat = new MetaSatellite();
				metaSat.name = satName;
				metaSat.downlinkFrequency = Long.parseLong(downFreq);
				metaSat.uplinkFrequency = Long.parseLong(upFreq);
				metaSat.sat = SatelliteFactory.createSatellite(satTLE);
				result.add(metaSat);
				System.out.println(metaSat+" added !");
				
			}
		}
		
		return result;
	}
}
