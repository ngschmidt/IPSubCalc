package ipsubcalc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IP Subnet Calculating Algorithms
 * @author Nicholas Schmidt
 */
public class IPSubCalc {

    private static byte[] bitwiseAnd(byte[] one, byte[] two) {
        byte[] tmp = new byte[one.length];
        for(int i = 0; i < two.length; i++) tmp[i] = two[i];
        for(int i = 0; i < one.length; i++) {
            one[i] &= tmp[i];
        }
        return one;
    }
    private static byte[] bitwiseXOR(byte[] one, byte[] two) {
        for(int i = 0; i < one.length; i++) {
            one[i] ^= two[i];
        }
        return one;
    }
    private static byte[] bitwiseOR(byte[] one, byte[] two) {
        for(int i = 0; i < one.length; i++) {
            one[i] |= two[i];
        }
        return one;
    }
    private static byte[] bitwiseInvert(byte[] in) {
        for(int i = 0; i < in.length; i++) {
            in[i] ^= 0xFF;
        }
        return in;        
    }
    private static byte[] bitwiseInvert(byte[] in, int len) {
        byte[] ret = new byte[len];
        for(int i = 0; i < in.length; i++) ret[i] = in[i];
        for(int i = 0; i < ret.length; i++) ret[i] ^= 0xFF;
        return ret;        
    }    
    private static byte[] bitwiseSubtract(byte[] one, byte[] two){
        if (one.length == two.length) {
            for(int i = 0; i < one.length; i++) one[i] -= two[i];
        }
        return one;
    } 
    private static long bitwiseHostAddr(byte[] one, byte[] two){
        byte[] tmp = bitwiseSubtract(one,two);
        printBytes(tmp);
        long temp = 0;
        for(int i = 0; i < tmp.length; i++) temp += tmp[i] & 0xFF  << (tmp.length - 1 - i) * 8 ;
        return temp - 1;
    }
    private static long bitwiseNetAddrs(byte[] one, byte[] two){
        byte[] tmp = bitwiseInvert(bitwiseSubtract(one,two));
        long temp = 0;
        for(int i = 0; i < tmp.length; i++) temp += tmp[i] & 0xFF  << (tmp.length - 1 - i) * 8 ;
        return Math.abs(temp);        
    }
    private static String join(String[] s, String delim) {
      if (s.length==0) return null;
      String out= s[0];
      for (int x=1;x<s.length;++x){
          out += delim;
          out +=s[x];
      }
      return out;
    }    
    private static byte writeByByte(int a) {
        switch(a){
            case 1:
                return (byte) 128;
            case 2:
                return (byte) 192;
            case 3:
                return (byte) 224;
            case 4:
                return (byte) 240;
            case 5:
                return (byte) 248;
            case 6:
                return (byte) 252;
            case 7:
                return (byte) 254;
            case 8:
                return (byte) 255;
            default: 
                return (byte) 0;
        }
    }
    private static int writeFromByte(byte a) {
        switch((int)a&0xFF){
            case 128:
                return 1;
            case 192:
                return 2;
            case 224:
                return 3;
            case 240:
                return 4;
            case 248:
                return 5;
            case 252:
                return 6;
            case 254:
                return 7;
            case 255:
                return 8;
            default:
                return 0;
        }
    }
    private static byte[] writeByBits(int a) {
        int retLen = (int) Math.ceil((double)a/(double)8);
        byte[] ret = new byte[retLen];
        for(int i = 0; i < retLen; i++){
            if(a >= 8) {
                ret[i] = writeByByte(8);
                a -=8;
            } else ret[i] = writeByByte(a%8);
        }
        return ret;
    }
    private static int countBits(byte[] arr) {
        int ret = 0;
        for(int i=0; i < arr.length; i++){
            ret += writeFromByte(arr[i]);
        }
        return ret;
    }
    private static void printBytes(byte[] a) {
        for(int i = 0; i < a.length; i++) System.out.print(a[i]&0xFF);
        System.out.print("\n");
    }
    private static void printBytesHex(byte[] a) {
        for(int i = 0; i < a.length; i++) System.out.print(Integer.toHexString(a[i]&0xFF));   
        System.out.print("\n");        
    }

    private static byte[] getNetAddressBytes(byte[] addr, byte[] mask) {
        return bitwiseAnd(addr, mask);
    }
    public static InetAddress getNetAddress(byte[] addr, byte[] mask) {
        try {
            return InetAddress.getByAddress(bitwiseAnd(addr, mask));
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return InetAddress.getLoopbackAddress();
    }    
    public static InetAddress getNetAddress(InetAddress addr, byte[] mask) {
        return getNetAddress(addr.getAddress(), mask);        
    }
    public static InetAddress getNetAddress(InetAddress addr, int mask) {        
        return getNetAddress(addr.getAddress(), writeByBits(mask));        
    }
    public static int getNetMaskBits(String mask) {
        String[] arr = mask.split("\\.");
        int[] arrint = new int[arr.length];
        for(int i = 0; i < arr.length; i++) arrint[i] = Integer.parseInt(arr[i]);
        byte[] arrbyte = new byte[arr.length];
        for(int i = 0; i < arr.length; i++) arrbyte[i] = (byte) arrint[i];
        return countBits(arrbyte);
    }
    public static String genNetMask4(int bits){
        byte[] mask = writeByBits(bits);
        System.out.println(mask.length);
        int[] maskint = new int[mask.length];
        for(int i = 0; i < mask.length; i++) maskint[i] = (int)mask[i]&0xFF;
        String[] maskstr = new String[mask.length];
        for(int i = 0; i < mask.length; i++) maskstr[i] = Integer.toString(maskint[i]);  
        return join(maskstr, ".");        
    }
    public static String genNetMaskHex(int bits){
        byte[] mask = writeByBits(bits);
        int[] maskint = new int[mask.length];
        for(int i = 0; i < mask.length; i++) maskint[i] = (int)mask[i]&0xFF;
        String[] maskstr = new String[mask.length];
        for(int i = 0; i < mask.length; i++) maskstr[i] = Integer.toHexString(maskint[i]);  
        return join(maskstr, ":");         
    }
    public static byte[] genNetMask(int bits) {
        return writeByBits(bits);      
    }
    public static byte[] getNetBroadcastByteWise(byte[] addr, byte[] mask) {
        byte[] ret = getNetAddressBytes(addr, mask);
        byte[] cmp = bitwiseInvert(mask, ret.length);
        return bitwiseOR(ret, cmp);
    }
    public static InetAddress getNetBroadcast(byte[] addr, byte[] mask) {
        try {
            return InetAddress.getByAddress(getNetBroadcastByteWise(addr, mask));
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return InetAddress.getLoopbackAddress();
    }
    public static InetAddress getNetBroadcast(InetAddress addr, byte[] mask) {
        return getNetBroadcast(addr.getAddress(), mask);
    }
    public static InetAddress getNetBroadcast(InetAddress addr, int mask) {
        return getNetBroadcast(addr.getAddress(), writeByBits(mask));
    }
    public static byte[] getNetHostsBytesWise(byte[] addr, byte[] mask) {
        return bitwiseSubtract(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetHosts(byte[] addr, byte[] mask) {
        return bitwiseHostAddr(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetHosts(InetAddress addr, byte[] mask) {
        return bitwiseHostAddr(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetHosts(InetAddress addr, int mask) {
        return bitwiseHostAddr(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetAddrs(byte[] addr, byte[] mask) {
        return bitwiseNetAddrs(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetAddrs(InetAddress addr, byte[] mask) {
        return bitwiseNetAddrs(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }
    public static long getNetAddrs(InetAddress addr, int mask) {
        return bitwiseNetAddrs(getNetBroadcast(addr, mask).getAddress(),getNetAddress(addr, mask).getAddress());
    }    
    public static void main(String[] args) {
        try {
            System.out.println(getNetAddrs(InetAddress.getByName("192.168.0.67"), 16));
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
