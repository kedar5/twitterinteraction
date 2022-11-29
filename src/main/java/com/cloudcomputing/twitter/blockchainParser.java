package com.cloudcomputing.twitter;

import org.json.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class blockchainParser {

  public int isvalid;
  public JSONObject responseJSON;
  public String msg;
  private long n;
  private long e;
  private long d;

  long power(long x_ip, long y_ip, long mod_ip)
  {
    //System.out.println("x="+x_ip+", y="+y_ip+", mod="+mod_ip);
    BigInteger x = BigInteger.valueOf(x_ip);
    BigInteger y = BigInteger.valueOf(y_ip);
    BigInteger mod = BigInteger.valueOf(mod_ip);
    BigInteger res = x.modPow(y,mod);

    return res.longValue();
  }

  public blockchainParser() {
    isvalid = 1;
    n = Long.parseLong("1561906343821");
    e = Long.parseLong("1097844002039");
    d = Long.parseLong("343710770439");
    msg = "no msg";
  }

  public void parse_json(JSONObject json_ip) {

    JSONArray chains;
    JSONArray new_tx;
    int new_id;
    JSONArray existing_chains;
    String new_target;
    try {
      chains = (JSONArray) json_ip.get("chain");
      new_tx = (JSONArray) json_ip.get("new_tx");
      new_id = ((JSONArray) json_ip.get("chain")).length();
      existing_chains = (JSONArray) json_ip.get("chain");
      new_target = json_ip.getString("new_target");
    } catch (JSONException e) {
      e.printStackTrace();
      isvalid = 0;
      msg = "Parse error 1";
      return;
    }


    //VALIDATING the existing chains
    BigInteger time = new BigInteger("0");
    String prev_hash = "00000000";
    for(int i=0; i<existing_chains.length(); i++) {
      //System.out.println("Looking at transaction "+i);
      JSONArray curr_txs;
      JSONObject curr_chain;
      String curr_hash, curr_pow, curr_target;
      int curr_id;
      try {
        curr_chain = (JSONObject) existing_chains.get(i);
        curr_txs = (JSONArray) curr_chain.get("all_tx");
        curr_hash = curr_chain.getString("hash");
        curr_pow = curr_chain.getString("pow");
        curr_target = curr_chain.getString("target");
        curr_id = curr_chain.getInt("id");

        if (curr_id != i) {
          isvalid = 0;
          msg = "block id is incorrect";
          return;
        }
      } catch (JSONException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
        isvalid = 0;
        msg = "Parse error 2";
        return;
      }

      String tx_hashes = "";

      // validating internal tx
      for(int j=0; j<curr_txs.length(); j++) {
        try {
          JSONObject curr_tx = (JSONObject) curr_txs.get(j);

          //if the time order is incorrect
          BigInteger newTime = new BigInteger((String) curr_tx.get("time"));
          if (time.compareTo(newTime) == 1){
            isvalid = 0;
            msg = "Times of curr tx are not inc";
            return;
          } else {
            time = newTime;
          }

          if (j != curr_txs.length()-1)
            validate_tx(curr_tx, false);
          else
            validate_miner_block(curr_tx, curr_id, false);

          tx_hashes += "|" + curr_tx.getString("hash");

        } catch (JSONException e) {
          e.printStackTrace();
          isvalid = 0;
          msg = "Parse error 3";
          return;
        }
      }

      String inner_string = Integer.toString(curr_id) + "|"
        + prev_hash + tx_hashes;
      //System.out.println(inner_string);
      String calc_hash =
        get_cchash(
          get_SHA(inner_string,-1) + curr_pow
        );

      if (curr_hash.equals(calc_hash)) {
        ;
      } else {
        isvalid = 0;
        msg = "Hash of the block is incorrect";
        return;
      }

      prev_hash = calc_hash;

      //the hash should be lexicographically smaller than the target
      if (calc_hash.compareTo(curr_target) >= 0) {
        isvalid = 0;
        msg = "hash and target do not correspond";
        return;
      }
    }

    BigInteger last_block_creation = time;

    //System.out.println("Now adding new transactions");
    // ADDING new transactions to a new chain
    time = last_block_creation; // all the new times must come after any other logged transactions
    JSONArray new_block_tx = new JSONArray();
    String tx_hashes = "";
    for(int i=0; i<new_tx.length(); i++) {
      try {
        JSONObject curr_tx = (JSONObject) new_tx.get(i);

        //if the time order is incorrect
        BigInteger newTime = new BigInteger((String) curr_tx.get("time"));
        if (time.compareTo(newTime) == 1){
          isvalid = 0;
          msg = "Times of new tx are not inc";
          return;
        } else {
          time = newTime;
        }

        //checks for valid sig and hash, and adds if new transaction
        curr_tx = validate_tx(curr_tx, true);
        new_block_tx.put(curr_tx);
        tx_hashes += "|" + curr_tx.getString("hash");

      } catch (JSONException e) {
        e.printStackTrace();
        isvalid = 0;
        msg = "Parse error 4";
        return;
      }

    }

    // add miner's reward to new chain
    BigInteger timenew = BigInteger.valueOf((long) 6e+11);

    JSONObject miner_reward = new JSONObject();
    try {

      miner_reward.put("recv", e);
      miner_reward.put("time", last_block_creation.add(timenew).toString());
      miner_reward.put("amt", get_reward_from_id(new_id));
      miner_reward = validate_miner_block(miner_reward, new_id, true);
      tx_hashes += "|" + miner_reward.getString("hash");
      new_block_tx.put(miner_reward);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      msg = "Parse Error 4.5";
      return;
    }

    String inner_string = Integer.toString(new_id) + "|"
      + prev_hash + tx_hashes;
    //System.out.println(inner_string);

    long curr_pow = 0;
    String calc_hash;
    while(true) {
      calc_hash =
        get_cchash(
          get_SHA(inner_string,-1) + curr_pow
        );
      if (calc_hash.compareTo(new_target) < 0) {
        break;
      } else {
        curr_pow += 1;
      }
    }

    JSONObject new_chain = new JSONObject();
    try {
      new_chain.put("all_tx", new_block_tx);
      new_chain.put("pow", Long.toString(curr_pow));
      new_chain.put("id", new_id);
      new_chain.put("hash", calc_hash);
      new_chain.put("target", new_target);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      isvalid = 0;
      msg = "Parse error 5";
      return;
    }

    existing_chains.put(new_chain);

    responseJSON = new JSONObject();
    try {
      responseJSON.put("chain", existing_chains);

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      isvalid = 0;
      msg = "literally the last thing";
    }
  }

  private long get_reward_from_id(int id) {
    return (long) (500000000/ Math.pow(2,Math.floor(id/2)));
  }

  /*
   * Checks if block is a valid miner block.
   * In case it is new, adds the hash value.
   * Also checks if amount is correct as per id
   **/
  public JSONObject validate_miner_block(JSONObject miner_reward, int chain_id, Boolean is_new) throws JSONException {
    String hash = get_cchash(
      miner_reward.getString("time") + "|"
        + "|"
        + Long.toString(miner_reward.getLong("recv"))  + "|"
        + Long.toString(miner_reward.getLong("amt")) + "|"

    );

    if (is_new) {
      miner_reward.put("hash", hash);
    } else {
      if (hash.equals(miner_reward.getString("hash")) &&
        miner_reward.getLong("amt") == get_reward_from_id(chain_id)
      ) {
        ;
      }
      else {
        isvalid = 0;
        msg = "hash or amt of existing miner block does not match";
        return null;
      }
    }
    //System.out.println("Miner block:");
    //System.out.println(miner_reward);
    return miner_reward;
  }

  /*
   * Validates all blocks except for miner reward. Miner reward should not be passed to this.
   * In case it is new, adds the hash, sig, fee, send
   **/
  public JSONObject validate_tx(JSONObject tx, Boolean can_be_new) throws JSONException {
    //System.out.println("\nThe object is:");
    //System.out.println(tx);
    if (tx.has("time") &&
      tx.has("send") &&
      tx.has("recv") &&
      tx.has("amt") &&
      tx.has("fee") &&
      tx.has("hash") &&
      tx.has("sig")
    ) {
//			//System.out.println("Existing object hash is:"+ tx.getString("hash"));
    }
    else if(can_be_new == true &&
      tx.has("time") &&
      tx.has("recv") &&
      tx.has("amt")
    ) {
      tx.put("send", e);
      tx.put("fee", 0);
    }
    else {
      isvalid = 0;
      msg = "the block does not have all required fields";
      return null;
    }


    //hash calculation
    String hash = get_cchash(
      tx.getString("time") + "|"
        + tx.getLong("send") + "|"
        + tx.getLong("recv")  + "|"
        + tx.getLong("amt") + "|"
        + tx.getLong("fee")
    );
//		//System.out.println("Calculated object hash is:"+ hash);
    long hashlong = Long.parseLong(hash, 16);
//		//System.out.println("Calculated object hash (long) is:"+ hashlong);

    // that means we need to
    if (tx.has("hash")) {
      //this validation is messing up: TODO
      long pk = tx.getLong("send");
      long decrypt_hash = power(tx.getLong("sig"),pk,n);
//			//System.out.println("Decrypted sig as long is:"+ decrypt_hash+" "+Long.toHexString(decrypt_hash));

      if(hash.equals(tx.get("hash")) && decrypt_hash == hashlong) {
//				//System.out.println("MATCH");
      }
      else {
        isvalid = 0;
        return null;
      }
    }
    else {
      long sig = power(Long.parseLong(hash, 16),d,n);
//			//System.out.println("Calculated sig is:"+ sig);

      tx.put("hash", hash);
      tx.put("sig", sig);
    }

    return tx;
  }

  public String get_cchash(String ip)  {
    return get_SHA(ip, 4);
  }

  //https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
  public String get_SHA(String ip, int limit) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    byte[] encodedhash = digest.digest(
      ip.getBytes(StandardCharsets.UTF_8));

    StringBuilder hexString = new StringBuilder();
    if (limit == -1) limit = encodedhash.length;
    for (int i = 0; i < limit; i++) {
      String hex = Integer.toHexString(0xff & encodedhash[i]);
      if(hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

}
