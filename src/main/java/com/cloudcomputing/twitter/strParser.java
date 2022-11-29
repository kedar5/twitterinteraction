package com.cloudcomputing.twitter;

import org.json.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class strParser {

  public String parse_input(String input) {
    byte[] decodedBytes = Base64.getUrlDecoder().decode(input);

//		String actualURLString = nw String(actualByte);
    String decompressedString = decompressToString(decodedBytes);

    //System.out.println(decompressedString);
    JSONObject json;
    try {
      json = new JSONObject(decompressedString);
    } catch (JSONException e) {
      return "INVALID";
    }

    blockchainParser bp = new blockchainParser();

    // put a try catch here
    bp.parse_json(json);

    if (bp.isvalid == 0) {
      //System.out.println(bp.msg);
      return "INVALID"+bp.msg;
    } else {
      byte[] compressedString = compress(bp.responseJSON.toString());
      return Base64.getUrlEncoder()
        .encodeToString(compressedString);
    }
  }

  //Following code taken from https://thiscouldbebetter.wordpress.com/2011/08/26/compressing-and-uncompressing-data-in-java-using-zlib/
  public byte[] compress(byte[] bytesToCompress)
  {
    Deflater deflater = new Deflater();
    deflater.setInput(bytesToCompress);
    deflater.finish();

    byte[] bytesCompressed = new byte[Short.MAX_VALUE];

    int numberOfBytesAfterCompression = deflater.deflate(bytesCompressed);

    byte[] returnValues = new byte[numberOfBytesAfterCompression];

    System.arraycopy
      (
        bytesCompressed,
        0,
        returnValues,
        0,
        numberOfBytesAfterCompression
      );

    return returnValues;
  }

  public byte[] compress(String stringToCompress)
  {
    byte[] returnValues = null;

    try
    {

      returnValues = this.compress
        (
          stringToCompress.getBytes("UTF-8")
        );
    }
    catch (UnsupportedEncodingException uee)
    {
      uee.printStackTrace();
    }

    return returnValues;
  }

  public byte[] decompress(byte[] bytesToDecompress)
  {
    byte[] returnValues = null;

    Inflater inflater = new Inflater();

    int numberOfBytesToDecompress = bytesToDecompress.length;

    inflater.setInput
      (
        bytesToDecompress,
        0,
        numberOfBytesToDecompress
      );

    int bufferSizeInBytes = numberOfBytesToDecompress;

    int numberOfBytesDecompressedSoFar = 0;
    List<Byte> bytesDecompressedSoFar = new ArrayList<Byte>();

    try
    {
      while (inflater.needsInput() == false)
      {
        byte[] bytesDecompressedBuffer = new byte[bufferSizeInBytes];

        int numberOfBytesDecompressedThisTime = inflater.inflate
          (
            bytesDecompressedBuffer
          );

        numberOfBytesDecompressedSoFar += numberOfBytesDecompressedThisTime;

        for (int b = 0; b < numberOfBytesDecompressedThisTime; b++)
        {
          bytesDecompressedSoFar.add(bytesDecompressedBuffer[b]);
        }
      }

      returnValues = new byte[bytesDecompressedSoFar.size()];
      for (int b = 0; b < returnValues.length; b++)
      {
        returnValues[b] = (byte)(bytesDecompressedSoFar.get(b));
      }

    }
    catch (DataFormatException dfe)
    {
      dfe.printStackTrace();
    }

    inflater.end();

    return returnValues;
  }

  public String decompressToString(byte[] bytesToDecompress)
  {
    byte[] bytesDecompressed = this.decompress
      (
        bytesToDecompress
      );

    String returnValue = null;

    try
    {
      returnValue = new String
        (
          bytesDecompressed,
          0,
          bytesDecompressed.length,
          "UTF-8"
        );
    }
    catch (UnsupportedEncodingException uee)
    {
      uee.printStackTrace();
    }

    return returnValue;
  }

}
