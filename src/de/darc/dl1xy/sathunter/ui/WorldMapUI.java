package de.darc.dl1xy.sathunter.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WorldMapUI extends JPanel{

	private final static String IMG_PATH="resources/land_shallow_topo_2048.tif";
	private BufferedImage image;
	private JLabel picLabel;
	
    public WorldMapUI() {
       try {                
    	   image = ImageIO.read(new File(IMG_PATH));
    	   picLabel = new JLabel(new ImageIcon(image));
    	   add(picLabel);
       } catch (IOException ex) {
            // handle exception...
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
    }

}
