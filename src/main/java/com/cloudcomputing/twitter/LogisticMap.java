package com.cloudcomputing.twitter;

public class LogisticMap {
  public byte [] map;

  public LogisticMap(int mapsize, int version) {
//		int mapsize = qrcode.mapsize;
    double [] dbl_map = new double [mapsize];
    dbl_map[0] = 0.1;
    double r = 4.0;
    double max = dbl_map[0];
    for(int i=1; i<mapsize; i++) {
      dbl_map[i] = r*dbl_map[i-1]*(1-dbl_map[i-1]);
      if (max < dbl_map[i])
        max = dbl_map[i];
//			System.out.println(dbl_map[i]);
    }

    map = new byte[mapsize];
    for(int i=0; i<mapsize; i++) {
      dbl_map[i] = dbl_map[i]*255;
      if(dbl_map[i]>=255)
        System.out.print("PROBLEM");
      map[i] = (byte) Math.floor(dbl_map[i]);
    }

    if (version == 1) {
      map[52] = 0;
      map[53] = (byte) 7;
      map[54] = (byte) 155;
      map[55] = (byte) 85;
    }

    if(version == 2) {
      map[76] = (byte) 128;
      map[77] = (byte) 6;
      map[78] = (byte) 26;
    }

  }

  public byte reverse_bits(byte input) {
    byte ret = 0;
    for(int i=0; i<8; i++) {
      if ((input & (1<<i)) != 0) {
        ret = (byte) (ret | (1<<(7-i)));
      }
    }

//		System.out.println(String.format("%02x", ret)+" "+String.format("%02x", input));
    return ret;
  }


  public byte [] encrypt(byte [] qr_arr) {
    byte [] result = new byte[map.length];

    for(int i=0; i<map.length; i++) {
//			byte qr_byte = qr_arr[i];
//			if (version == 2 && )
//			System.out.print("XORing "+i+" "+String.format("%02x",qr_arr[i])+" "+String.format("%02x",map[i]));
      result[i] = (byte) (reverse_bits(map[i])^qr_arr[i]);
//			System.out.print("("+String.format("%02x",reverse_bits(map[i])));
//			System.out.print(") "+String.format("%02x", result[i]));
//			System.out.println();
    }
    return result;
  }

}
