package com.cloudcomputing.twitter;

public class DecoderMatrix {
  public Boolean [][] large_matrix;
  public PreprocessedMatrix inner_matrix;
  public int version;

  public void populate_outer_mat(String input_str) {


    byte [] byte_8 = new byte[32*32/4]; // each element contains a word, not a byte
    int bytearr_ind = 0;

    int i = 0;
    while(i<input_str.length()) {
//			System.out.println(i+" "+input_str.charAt(i));
      if( (i+1)!= input_str.length() && input_str.charAt(i)=='0' && input_str.charAt(i+1)=='x') {
//				System.out.println((i+1)+" "+input_str.charAt(i+1));
        i+=2;
        int chars = 0;
        while( (i+chars+1)!= input_str.length() ) {
          if(input_str.charAt(i+chars)=='0' && input_str.charAt(i+chars+1)=='x') {
            break;
          }
          else {
//						System.out.println("    "+i+" "+input_str.charAt(i+chars));
            chars += 1;
          }
        }
//				System.out.println(input_str.charAt(i-2)+input_str.charAt(i-1));
//				System.out.print(i+" "+chars+" ");
        for(int j=0; j < 8-chars; j++) {
          byte_8[j+bytearr_ind] = 0;
//					System.out.print(0);
        }
//				System.out.println(bytearr_ind);
        for(int j=8-chars; j<8; j++) {
//					System.out.println("!!!!"+j+" "+input_str.charAt(i+j));
          byte_8[j+bytearr_ind] = (byte) Integer.parseInt(Character.toString(input_str.charAt(i)), 16);
          i+=1;
//					System.out.print(String.format("%01x",byte_8[j+bytearr_ind]));
        }
//				System.out.println();
        bytearr_ind += 8;

      } else {
        i += 1;
      }
    }

//		print_matrix();
    byte [] byte_8_merged = new byte[32*32/8];
    for(int j=0; j<32*32/8; j++) {
      byte_8_merged[j] = (byte) ((byte) (byte_8[2*j]<<4) + byte_8[2*j+1]);
//			System.out.println(String.format("%02x",byte_8_merged[j]));
    }

    LogisticMap map = new LogisticMap(32*32/8, 3);
    byte_8_merged = map.encrypt(byte_8_merged);

    //now the byte array has been populated, we can put it into the matrix
    int x=0, y=0;
    for(int j=0; j<32*32/8; j++) {
      for(int pos = 7; pos>=0; pos--) {
        if ((byte_8_merged[j] & (1<<pos)) != 0) {
          large_matrix[x][y] = true;
        }else {
          large_matrix[x][y] = false;
        }

        y += 1;
        if(y==32) {
          y = 0;
          x+=1;
        }
      }
    }

//		print_matrix();
  }

  public void print_matrix() {
    System.out.println();
    int count = 0;
    for (int i=0; i<32; i++) {
      for (int j=0; j<32; j++) {
        count += 1;
        if (large_matrix[i][j] == true)
          System.out.print("=");
        else
          System.out.print("_");
        System.out.print(" ");
//				if(count % 8 == 0)
//					System.out.print(count/8);
//				else
//					System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();
    System.out.println();
  }

  public Boolean populate_inner_mat(int input_version) {
    PreprocessedMatrix mat = new PreprocessedMatrix(input_version);

    for(int rot=0; rot<360; rot+=90) {
      for(int i=0; i<32-mat.size; i++) {
        for(int j=0; j<32-mat.size; j++) {
          int mismatch = 0;

          for(int k = 0; k<mat.mask_bits_size; k++) {
            int x = mat.mask_bits[k][0];
            int y = mat.mask_bits[k][1];

            if (mat.qrcode[x][y] != large_matrix[x+i][y+j]) {
              mismatch = 1;
              break;
            }
          }
          if (mismatch == 0) {
//						System.out.println("Found matrix with rotation "+rot+" at coords "+i+","+j);

            for(int x=0; x<mat.size; x++) {
              for(int y=0; y<mat.size; y++) {
                mat.qrcode[x][y] = large_matrix[x+i][y+j];
              }
            }
            mat.restore_rotation();
            inner_matrix = mat;
            return true;
          }
        }
      }

      mat.rotate_matrix();
    }

    return false;
  }

  public DecoderMatrix(String input_str) {
    large_matrix = new Boolean [32][32];
    populate_outer_mat(input_str);
    if (populate_inner_mat(1) == true) {
      version=1;
    } else if (populate_inner_mat(2) == true) {
      version=2;
    } else {
      System.out.println("Could not recognise matrix");
    }

  }
}
