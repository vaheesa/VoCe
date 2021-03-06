import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package multicast;

/**
 *
 * @author Thinesh
 */
public class PClient implements Runnable {
   
  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;
  byte tempBuffer[] = new byte[500];
  private DatagramSocket socket = null;
 // byte playBuffer[] = new byte[500];
    private final int mcPort = 55000;
    private InetAddress mcIPAddress = null;


/*private AudioFormat getAudioFormat() {
    float sampleRate = 16000.0F;
    int sampleSizeInBits = 16;
    int channels = 2;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
}*/


    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(" Include the IP address of Peer  : ");
            return;
        }

        try {

            Thread cap = new Thread(new PClient(InetAddress.getByName(args[0])));
            cap.start();

            Thread ply = new Thread((Runnable) new Recieve());
            ply.start();

        } catch (UnknownHostException e) {
        }
    }


    private PClient(InetAddress mcIPAddress) {
      //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      this.mcIPAddress=mcIPAddress;
    }
	
	public PClient(){
		super();
	}
    
  @Override
     public void run() {
        try {
            this.socket = new DatagramSocket(this.mcPort);
            this.captureAudio();
            this.CaptureAndSend();

        } catch (SocketException e) {
        } finally {
            this.socket.close();
        }
    }

    
    
    private void CaptureAndSend(){
        byteArrayOutputStream = new ByteArrayOutputStream();
        stopCapture = false;
        try {
         
            while (!stopCapture) {
                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, mcIPAddress, 55001);
                    this.socket.send(sendPacket);
                   // this.socket.setLoopbackMode(true);
                    byteArrayOutputStream.write(tempBuffer, 0, cnt);
                }
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            System.out.println("CaptureThread::run()" + e);
            System.exit(0);
        }
        
    }
    
               
    public synchronized void captureAudio() {
    
    try {
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();    //get available mixers
        System.out.println("Available mixers:");
        Mixer mixer = null;
        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
            System.out.println(cnt + " " + mixerInfo[cnt].getName());
            mixer = AudioSystem.getMixer(mixerInfo[cnt]);

            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                System.out.println(cnt + " Mic is supported!");
                break;
            }
        }
       Recieve r = new Recieve();
        audioFormat = r.getAudioFormat();     //get the audio format
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start(); 
        //Setting the maximum volume
        //FloatControl control = (FloatControl)sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        //control.setValue(control.getMaximum());

     //   captureAndPlay(); //playing the audio

    } catch (LineUnavailableException e) {
        System.out.println(e);
        System.exit(0);
    }


    
}
}