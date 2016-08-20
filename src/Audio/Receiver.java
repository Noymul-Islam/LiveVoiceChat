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
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Receiver extends Thread {

    private static final int DEFAULT_INTERNAL_BUFSIZ = 20480*4; //Audio Buffer Sizes 
    private static final int DEFAULT_EXTERNAL_BUFSIZ = 20480*4;
    private SourceDataLine m_sourceLine; //Line for Audio Out
    private boolean m_bRecording;
    private int m_nExternalBufferSize;
    static boolean goflag;
    static int PacketsIN;

    public Receiver(AudioFormat format, int nInternalBufferSize, int nExternalBufferSize, //Sets Up Audio System
            String strMixerName) throws LineUnavailableException {
        Mixer mixer = null;
        if (strMixerName != null) {
            Mixer.Info mixerInfo = getMixerInfo(strMixerName);
            mixer = AudioSystem.getMixer(mixerInfo);
        }
        DataLine.Info InfosourceInfo = new DataLine.Info(SourceDataLine.class, format, nInternalBufferSize);
        if (mixer != null) {
            m_sourceLine = (SourceDataLine) mixer.getLine(InfosourceInfo);
        } else {
            m_sourceLine = (SourceDataLine) AudioSystem.getLine(InfosourceInfo);
        }
        m_sourceLine.open(format, nInternalBufferSize);
        m_nExternalBufferSize = nExternalBufferSize;
    }


    public void start() { //Starts the Audio Thread
        System.out.println("okloool555");
        m_sourceLine.start();
        super.start(); //Receiving and Decoding
    }

    public void end() {
        goflag = false;
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(6001); //Opens the Receiving Socket 
            byte[] abBuffer = new byte[322]; //Receiving array
            byte[] decoded  = new byte[322]; //Decompressed Array 
            m_bRecording = true;
            PacketsIN = 0;
            while (m_bRecording & goflag) {
                System.out.println("okloool222");
             //   abBuffer = new byte[82];
                decoded = new byte[322];
                DatagramPacket receivePacket = new DatagramPacket(abBuffer, 322); //Creates the Packet
                serverSocket.receive(receivePacket); //Fills it from Server
                decoded=receivePacket.getData();
               //decoded = Decode(receivePacket.getData()); //Decoded the packet's Data 
                m_sourceLine.write(decoded, 0, decoded.length); //And writes the data to the 
                PacketsIN++;
             System.out.println(goflag +""+ m_bRecording);   
            } //audio out line
        } catch (Exception e) {
            System.out.println("Receiving Error");
            System.exit(1);
        }
    }

    private static Mixer.Info getMixerInfo(String strMixerName) { //Mixer Info for 
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo(); //Setting up the audio system
        for (int i = 0; i < aInfos.length; i++) {
            if (aInfos[i].getName().equals(strMixerName)) {
                return aInfos[i];
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String strMixerName = null;
        float fFrameRate = 8000.0F; //8000 samples per sec
        int nInternalBufferSize = DEFAULT_INTERNAL_BUFSIZ; //sets audio buffers
        int nExternalBufferSize = DEFAULT_EXTERNAL_BUFSIZ;
        goflag = true;
        AudioFormat audioFormat = new AudioFormat(fFrameRate, 16, 1, true, true);//sets audio format
//8000 samples per second on Signed PCM and bigEndian format
        Receiver audioLoop = null;
        long start = System.currentTimeMillis() / 1000;
        try {
            audioLoop = new Receiver(audioFormat, nInternalBufferSize, nExternalBufferSize,//sets the audio system up
                    strMixerName);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        }
        audioLoop.start(); //Starts the Receiving Thread, and Decoding and Audio
        System.out.println("okloool");
        try {
            InputStreamReader input = new InputStreamReader(System.in);
            while (input.read() < 0) {
            }
            audioLoop.end();
            System.out.println("Packets received = " + PacketsIN);
            System.out.println("Samples received = " + (PacketsIN * 161));
            System.out.println("Time Elapsed = " + ((System.currentTimeMillis() / 1000) - start));
            System.out.println("Packets Per Second = " + (PacketsIN / ((System.currentTimeMillis() / 1000) - start)));
        } catch (IOException e) {
        }
        System.exit(1);
    }
}
