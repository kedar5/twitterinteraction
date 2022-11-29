package com.cloudcomputing.twitter;

public class PreprocessedMatrix {
  public int version;
  public Boolean [][] qrcode;
  public Boolean [][] qrcode_mask;
  public int size;
  public int mapsize;
  public int mask_bits_size;
  public int [][] mask_bits;
  public int rotation ;

  public PreprocessedMatrix(int input_version) {
    version = input_version;
    if (version == 1) {
      size = 21;
    } else {
      size = 25;
    }
    mapsize = (size*size)/8+1;
    create_base_matrix();
    create_mask_matrix();
    create_mask_bits();
    rotation = 0;
  }

  public void print_matrix() {
    System.out.println();
    int count = 0;
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
        count += 1;
        if (qrcode[i][j] == true)
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

  public void create_base_matrix(){


    qrcode = new Boolean [size][size];

    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
        qrcode[i][j] = false;
      }
    }

    //timing patterns
    for(int i=6; i<size; i+=2) {
      qrcode[i][6] = true;
      qrcode[6][i] = true;
    }

    //position detection
    int[] start_x = {0, size-7, 0};
    int[] start_y = {0, 0, size-7};

    for(int i=0; i<3; i++) {
      for(int x=start_x[i]; x<start_x[i]+6; x++) {
        qrcode[x][start_y[i]] = true;
        qrcode[x][start_y[i]+6] = true;
      }

      for(int y=start_y[i]; y<start_y[i]+6; y++) {
        qrcode[start_x[i]][y] = true;
        qrcode[start_x[i]+6][y] = true;
      }

      for(int x=start_x[i]+2; x<start_x[i]+5; x++) {
        qrcode[x][start_y[i]+2] = true;
        qrcode[x][start_y[i]+4] = true;
      }

      for(int y=start_y[i]+2; y<start_y[i]+5; y++) {
        qrcode[start_x[i]+2][y] = true;
        qrcode[start_x[i]+4][y] = true;
      }

      qrcode[start_x[i]+3][start_y[i]+3] = true;

    }

    //alignment patterns
    if (version==1) {
      start_x = new int[]{};
      start_y = new int[]{};
    }
    else {
      start_x = new int[]{size-9};
      start_y = new int[]{size-9};
    }

    for(int i=0; i<start_x.length; i++) {
      for(int x=start_x[i]; x<start_x[i]+5; x++) {
        qrcode[x][start_y[i]] = true;
        qrcode[x][start_y[i]+4] = true;
      }

      for(int y=start_y[i]; y<start_y[i]+5; y++) {
        qrcode[start_x[i]][y] = true;
        qrcode[start_x[i]+4][y] = true;
      }

      qrcode[start_x[i]+2][start_y[i]+2] = true;

    }
  }

  public void create_mask_matrix(){

    qrcode_mask = new Boolean [size][size];
    mask_bits_size = 0;

    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
        qrcode_mask[i][j] = false;
      }
    }

    //timing patterns
    for(int i=6; i<size; i+=1) {
      qrcode_mask[i][6] = true;
      qrcode_mask[6][i] = true;
      mask_bits_size += 2;
    }

    //position detection
    int[] start_x = {0, size-8, 0};
    int[] start_y = {0, 0, size-8};

    for(int i=0; i<3; i++) {
      for(int x=start_x[i]; x<start_x[i]+8; x++) {
        for(int y=start_y[i]; y<start_y[i]+8; y++) {
          qrcode_mask[x][y] = true;
          mask_bits_size += 1;
        }
      }
    }

    //alignment patterns
    if (version==1) {
      start_x = new int[]{};
      start_y = new int[]{};
    }
    else {
      start_x = new int[]{size-9};
      start_y = new int[]{size-9};
    }

    for(int i=0; i<start_x.length; i++) {
      for(int x=start_x[i]; x<start_x[i]+5; x++) {
        for(int y=start_y[i]; y<start_y[i]+5; y++) {
          qrcode_mask[x][y] = true;
          mask_bits_size += 1;
        }
      }
    }
  }

  public void create_mask_bits() {
    mask_bits = new int [mask_bits_size][2];
    int mb_ind = 0;
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
        if (qrcode_mask[i][j] == true) {
          mask_bits[mb_ind][0] = i;
          mask_bits[mb_ind][1] = j;
          mb_ind += 1;
        }
      }
    }
  }

  //rotates matrix by 90
  public void rotate_matrix() {
    rotation = (rotation + 90)%360;

    Boolean [][] qrcode_new = new Boolean[size][size];
    Boolean [][] qrcode_mask_new = new Boolean[size][size];

    for (int i=0; i<size; i++) {
      for(int j=0; j<size; j++) {
        qrcode_new[j][size-i-1] = qrcode[i][j];
        qrcode_mask_new[j][size-i-1] = qrcode_mask[i][j];
      }
    }

    for(int i=0; i<mask_bits_size; i++) {
      int x = mask_bits[i][0];
      int y = mask_bits[i][1];

      mask_bits[i][0] = y;
      mask_bits[i][1] = size-x-1;
//			System.out.println();
    }

    qrcode = qrcode_new;
    qrcode_mask = qrcode_mask_new;
//		print_matrix();
  }

  public void restore_rotation() {
    while (rotation!=0) rotate_matrix();
  }
}
