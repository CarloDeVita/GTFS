package demo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JDialog;

/**
 *
 * @author cdevi
 */
public class SpinnerDialog extends JDialog {
   private Image image;
   
   public SpinnerDialog() throws IOException{
       this.image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("waitGif.gif"));
   }
   
   @Override
  public void paintComponents(Graphics g){
      super.paintComponents(g);
      g.drawImage(image, 0, 0, this);
  }
}
