package de.darc.dl1xy.sathunter.tle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import uk.me.g4dpz.satellite.TLE;

public class TLEHandler {

	public TLEHandler() {
		// TODO Auto-generated constructor stub
	}

	public static java.util.List<TLE> parseTLEFromURL (final String urlString) throws URISyntaxException, IOException
	{
		URL url = new URL(urlString);
		
		return TLE.importSat(url.openStream());
	}
	
	public static java.util.List<TLE> parseTLEFromFile (final String fileName) throws FileNotFoundException, IOException 
	{	
		
		return TLE.importSat(new FileInputStream(System.getProperty("user.dir")+fileName));
	}
	
	public static java.util.List<TLE> parseTLEFromFile (final File file) throws FileNotFoundException, IOException 
	{		
		return TLE.importSat(new FileInputStream(file));
	}
	
	public static java.util.List<TLE> parseTLEFromZip (final String urlString, final String tmpFolder) throws IOException
	{
		final URL url = new URL(urlString);
		File newFile = null;
		byte[] buffer = new byte[1024];
		 
	     
    	//create output directory is not exists
		final File folder = new File(tmpFolder);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
 
    	final ZipInputStream zis = new ZipInputStream(url.openStream());
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
 
    	while(ze != null)
    	{
 
    		final String fileName = ze.getName();
    		newFile = new File(tmpFolder + File.separator + fileName);
 
    		System.out.println("Unzip file "+ newFile.getAbsoluteFile());
 
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();
 
            final FileOutputStream fos = new FileOutputStream(newFile);             
 
            int len;
            while ((len = zis.read(buffer)) > 0) {
            	fos.write(buffer, 0, len);
            }
 
            fos.close();   
            ze = zis.getNextEntry();
    	}
 
        zis.closeEntry();
    	zis.close();
	
		return TLEHandler.parseTLEFromFile(newFile);
	}
	
	public static TLE findTLEByName(final List<TLE> tleList, final String tleName)
	{
		for (TLE tle:tleList)
		{
			if (tle.getName().equals(tleName))
				return tle;
		}
		return null;
	}
}
