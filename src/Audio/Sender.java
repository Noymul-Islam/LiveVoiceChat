/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Audio;

/**
 *
 * @author Noymul Islam
 */
import java.io.*;
import javax.sound.sampled.*;
import java.net.*;
import java.util.*;

public class Sender extends Thread {

    private static final int DEFAULT_INTERNAL_BUFSIZ = 20480*4; //Audio Buffer Sizes
    private static final int DEFAULT_EXTERNAL_BUFSIZ = 20480*4;
    private TargetDataLine m_targetLine; //Audio In Line
    private boolean m_bRecording;
    private int m_nExternalBufferSize;
    static int PacketsOUT;
    static boolean goflag;
   

    public Sender(AudioFormat format, int nInternalBufferSize, int nExternalBufferSize, //Gets Lines and Mixers
            String strMixerName) throws LineUnavailableException { //Ready

        Mixer mixer = null;
        if (strMixerName != null) {
            Mixer.Info mixerInfo = getMixerInfo(strMixerName);
            mixer = AudioSystem.getMixer(mixerInfo);
        }
        DataLine.Info InfotargetInfo = new DataLine.Info(TargetDataLine.class, format, nInternalBufferSize);
        if (mixer != null) {
            m_targetLine = (TargetDataLine) mixer.getLine(InfotargetInfo);
        } else {
            m_targetLine = (TargetDataLine) AudioSystem.getLine(InfotargetInfo);
        }
        m_targetLine.open(format, nInternalBufferSize);
        m_nExternalBufferSize = nExternalBufferSize;
    }

    public void start() { //Starts the Audio In Thread
        System.out.println("ok");
        m_targetLine.start();
        super.start();
    }

    public void end() {
        goflag = false;
    }

    public void run() {
        try {
             System.out.println("okokok");
            byte[] abBuffer = new byte[322]; //buffer size of 161 2-byte samples 
            byte[] sendData = new byte[82]; //buffer size of 160 4-bit samples and 1 16-bit sample
            m_bRecording = true;
            DatagramSocket clientSocket = new DatagramSocket();//Starts the Sending Socket
            InetAddress IPAddress = InetAddress.getByName("192.168.0.114"); //Sets The IP ADDRESS 
            PacketsOUT = 0;
            while (m_bRecording && goflag) {
                abBuffer = new byte[322];
                sendData = new byte[82];
                m_targetLine.read(abBuffer, 0, 322); //Read in the samples 
              //  sendData = Encode(abBuffer); //Encode the samples
                
                    
                //for(int  i=0;i<sendData.length;i++)
                  //  System.out.println("--->"+sendData[i]);
                    DatagramPacket sendPacket = new DatagramPacket(abBuffer, 322, IPAddress, 6001); //Send the Encoded Samples
                clientSocket.send(sendPacket); //161 samples as 82 bytes 
                PacketsOUT++;
                Thread.sleep(20); //Wait 20 milliseconds before sending again 

            }
            clientSocket.close(); //Close Socket
        } catch (Exception e) { //Try and Catch required for Thread.sleep()
            System.out.println("error");
        }
    }

    private static Mixer.Info getMixerInfo(String strMixerName) { //Returns Mixer info 
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo(); //Used for setting up the Audio System
        for (int i = 0; i < aInfos.length; i++) {
            if (aInfos[i].getName().equals(strMixerName)) {
                return aInfos[i];
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String strMixerName = null;
        float fFrameRate = 8000.0F; //8000 samples per second
        int nInternalBufferSize = DEFAULT_INTERNAL_BUFSIZ;
        int nExternalBufferSize = DEFAULT_EXTERNAL_BUFSIZ;
        AudioFormat audioFormat = new AudioFormat(fFrameRate, 16, 1, true, true);
        Sender audioLoop = null;
        goflag = true;
        long start = System.currentTimeMillis() / 1000;
        try {
            audioLoop = new Sender(audioFormat, nInternalBufferSize, nExternalBufferSize, //Sets up the Audio System
                    strMixerName);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        audioLoop.start(); //Starts the Thread
        
        try {
            
            InputStreamReader input = new InputStreamReader(System.in);
                     
            while (input.read() < 0) {
                System.out.println("okloool");
            }
            
            audioLoop.end();
            System.out.println("Packets sent = " + PacketsOUT);
            System.out.println("Samples sent = " + (PacketsOUT * 161));
            System.out.println("Time Elapsed = " + ((System.currentTimeMillis() / 1000) - start));
            System.out.println("Packets Per Second = " + (PacketsOUT / ((System.currentTimeMillis() / 1000) - start)));

        } catch (IOException e) {

        }
        System.exit(1);

    }

}
