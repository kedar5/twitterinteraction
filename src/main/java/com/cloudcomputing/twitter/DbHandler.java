package com.cloudcomputing.twitter;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import java.util.*;

import static io.netty.util.internal.StringUtil.length;
import static java.lang.Math.log;
public class DbHandler {
  List<String> hashtag_list = List.of("mtvhottest","gameinsight","iheartawards","android","androidgames","videomtv2016","rtã—ãŸäººå…¨å“¡ãƒ•ã‚©ãƒ­ãƒ¼ã™ã‚‹","bestfanarmy","rt","now2016","teamfollowback","retweet","nowplaying","ipad","ipadgames","followback","openfollow","Ø§Ù„Ø³Ø¹ÙˆØ¯ÙŠØ©","sougofollow","nblnabilavoto","love","ç›¸äº’ãƒ•ã‚©ãƒ­ãƒ¼","Ø§Ù„Ù‡Ù„Ø§Ù„","follow","kca","followtrick","news","followme","Ø§Ù„Ø±ÙŠØ§Ø¶","Ø±ØªÙˆÙŠØª","iphone","win","job","æ‹¡æ•£å¸Œæœ›","ØªØ·Ø¨ÙŠÙ‚_Ù‚Ø±Ø¢Ù†Ù‰","quran","np","nowonedirection","Ø§Ù„Ù†ØµØ±","porn","ãƒˆãƒ¬ã‚¯ãƒ«","tuitutil","hadith","teambts","sex","ï·º","jobs","Ø¢Ø³Ø±Ø¹_Ø±ØªÙˆÙŠØª","ç›¸äº’å¸Œæœ›","lovatics","bestmusicvideo","nbavote","videoveranomtv","ff","ã¾ã¨ã‚","free","soompiawards","mgwv","Ø§Ù„Ø§ØªØ­Ø§Ø¯","followmejp","music","Ø³ÙƒØ³","nowfifthharmony","teamexo","soundcloud","teamsuperjunior","giveaway","beliebers","mufc","epicmobclothing","littlemonsters","quote","Ø±ÙŠØªÙˆÙŠØª","onedirection","amazon","selfie","heatonedirection","hiring","mpn","tfbjp","trecru","iphonegames","gain","youtube","izmirescort","video","Ø§Ù„ÙƒÙˆÙŠØª","fashion","tfb","travel","bigolive","Ø¬Ø¯Ø©","anotherfollowtrain","teamgot7","xxx","ksa","Ù†Ø´Ø±_Ø³ÙŠØ±ØªÙ‡","saudi","vote1duk","hot","sexy","harmonizers","ãƒ¢ãƒ³ã‚¹ãƒˆ","Ù‚Ø·Ø±","ØºØ±Ø¯_Ø¨ØµÙˆØ±Ø©","teen","soma","å‹•ç”»","Ø¯Ø¹Ø§Ø¡","follow2befollowed","f4f","art","countkun","wmaonedirection","Ù…ØµØ±","halamadrid","photography","belieber","marketing","remajaindonesia","wmabritneyspears","mileyformmva","goldenglobes","lfc","Ø§Ù„Ø§Ù‡Ù„ÙŠ","Ù…ÙƒØ©","venezuela","tcfollowtrain","cge","pussy","selenators","tbt","heatjustinbieber","selenaformmva","ãƒ¤ãƒ•ã‚ªã‚¯","competition","tcot","libra","virgo","akubutuhsentuhanlelaki","usa","Ø§Ù„Ø¨Ø­Ø±ÙŠÙ†","bringbackourgirls","bestcollaboration","ad","tech","socialmedia","ØªØ§Ù…Ù„Ø§Øª_Ø§ÙŠÙ…Ø§Ù†ÙŠØ©","aries","leo","cancer","followers","business","umrei","nsfw","Ø§Ù„Ø¯Ù…Ø§Ù…","Ù†ÙŠÙƒ","lovelive","trump","bot","quotes","bts","me","ØºØ±Ø¯_Ø¨Ø§Ù„Ø®ÙŠØ±","Ø¹Ø§Ø¬Ù„","Ø§Ù„Ø´Ø¨Ø§Ø¨","movie","Ø³ÙˆØ±ÙŠØ§","Ø§Ù„Ø§Ù…Ø§Ø±Ø§Øª","inspiration","timber","bestcover","Ø¯Ø¨ÙŠ","new","mtvsummerstar","fxch","ãƒ‘ã‚ºãƒ‰ãƒ©","Ø´Ø¹Ø±","got7","teamretweet","gemini","rt2gain","votetris","votethewanteduk","facetoface","dragmedown","cfc","Ø¨ÙˆØ­","fanarmy","siguemeytesigo","bahrain","itunes","Ø§ÙÙ„Ø§Ù…_Ø³ÙƒØ³","quotesalieakbaryks","fatego","akb48","taurus","iran","lrt","boobs","è‰¦ã“ã‚Œ","etsy","bestmoviesong","votekatniss","health","Ù‚Ø­Ø¨Ù‡","nothuman","kangenkakrianaantoinette","ãƒªãƒ•ã‚©ãƒ­ãƒ¼","photo","autofollow","noticias","1dongma","åè¨€","listenlive","workfromhome","pixiv","teenchoice","mh370","maga","lol","uae","ç¥ã£ã¦ãã‚Œã‚‹äººrt","pisces","hiphop","ass","5sosfam","believeinmagicball","wmajustinbieber","style","exo","sidetoside","scorpio","followdaibosyu","lineãƒžãƒ³ã‚¬","milf","ë°©íƒ„ì†Œë…„ë‹¨","aquarius","twitter","kcamexico","agqr","Ø³Ø§Ù„Ø¨","mpoints","cantstopthefeeling","syria","Ù…ØºÙ„ÙŠÙ‡Ø§_Ø§Ù„Ø²ÙŠÙ†_ØªØ¬Ø±ÙŠØ¨ÙŠ","london","ØµÙˆØ±Ø©","happynewyear","oomf","naked","ã‚¢ãƒžã‚¾ãƒ³","naverã¾ã¨ã‚","seo","pjnet","pltl","nude","christmas","instagram","refollow","asmsg","sexdate","food","sobatindonesia","auspol","jewelry","fgo","asyamsulzakaria","football","yespimpmysummerball","nowjustinbieber","ØªØ·Ø¨ÙŠÙ‚_Ø§Ø°ÙƒØ§Ø±","happy","Ø¹Ø¬Ù„Ø§Ù†_ÙˆØ§Ø®ÙˆØ§Ù†Ù‡","ÙƒØ³","adult","fb","fitness","Ø§Ù„ÙŠÙ…Ù†","anal","Ø·ÙŠØ²","sheskindahotmusicvideo","nba","tweetkepo","2ch","åŠ£åŒ–ã‚³ãƒ”ãƒ¼","uk","twimaker","tits","snapchat","leadership","careerarc","capricorn","Ø±ÙˆØ§Ø¨Ø·_Ø³ÙƒØ³","beauty","fact","Ù„Ø²ÙŠØ§Ø¯Ø©_Ø¹Ø¯Ø¯_Ù…ØªØ§Ø¨Ø¹ÙŠÙ†","ã‚ãƒ¼ãƒ¼ãƒ¼ãƒ¼ã‚¸ãƒ£ãƒ‹ã‚ªã‚¿ã•ã‚“ã¨ç¹‹ãŒã‚‹ãŠæ™‚é–“ãŒã¾ã„ã‚Šã¾ã—ãŸã„ã£ã±ã„ç¹‹ãŒã‚Šã¾ã—ã‚‡","lt","ØªØ·Ø¨ÙŠÙ‚_ØªØ§Ù…Ù„Ø§Øª_Ø§ÙŠÙ…Ø§Ù†ÙŠØ©","wcw","pillowtalk","nyc","4musiclfsdirectioners","repost","amateur","facebook","periscope","fav","startup","raw","prayforsouthkorea","design","Ø±ØªÙˆÙŠØª_Ø¨Ù„Ø¢_ØªÙˆÙ‚Ù","sagittarius","ehemehem","maunyaapaan","verifydjzoodel","nature","escortizmir","takutadaapaapa","åŠ è—¤ç”±ç¾Žå­","erotic","nashsnewvideo","kindle","Ø¯Ø±Ø±","deals","f1","nowladygaga","spinnrtaylorswift","wmaexo","ç„¡æ–™","Ø§Ø³ØªØºÙØ§Ø±","ãƒ¢ãƒ³ã‚¹ãƒˆã‚„ã‚‹ãªã‚ˆ","å…±æ„Ÿã—ãŸã‚‰rt","shoes","smap","cetiga","chibicybercommunity","bitcoin","nfl","perfectday","empleo","okuga","Ø§Ù„Ø¹Ø±Ø§Ù‚","Ø±ØªÙˆÙŠØª_Ù„Ø²ÙŠØ§Ø¯Ø©","meetthevamily","ukraine","wedding","Ø²Ø¨","Ø§Ù„Ø¥Ù…Ø§Ø±Ø§Øª","mplusrewards","islam","Ø§Ø¶ØºØ·_ÙÙˆÙ„Ùˆ","life","Ù…Ù‚Ø§Ø·Ø¹_Ø³ÙƒØ³","cuba","tweetbatt","russia","mobile","live","romance","ç›¸äº’","vintage","obamafarewell","Ø§Ù„Ø£Ù‡Ù„ÙŠ","respect","ã‚³ãƒ©ãƒœã‚­ãƒ£ã‚¹","followngain","apple","Ø§Ù„Ù‚ØµÙŠÙ…","retweets","breaking","teamautofollow","teenwolf","porno","yesallwomen","spring","ebook","money","Ø§ÙÙ„Ø§Ù…","Ù…Ø­Ø§Ø±Ù…","Ø²ÙŠØ§Ø¯Ø©_Ù…ØªØ§Ø¨Ø¹ÙŠÙ†","instantfollow","Ø³ÙƒØ³_ÙˆØ±Ø¹Ø§Ù†","ã‚¨ãƒ­","summer","exsandohs","takipedenitakipederim","nhk","ãƒ©ãƒ–ãƒ©ã‚¤ãƒãƒ¼ã¯rt","sougo","å£°å„ªç·é¸æŒ™","entrepreneur","rtã—ãŸäººã«ã‚„ã‚‹","pll","Ø¯Ø§Ø¹Ø´","c91","Ù†Ø¬Ø±Ø§Ù†","obama","chasingcameron","ã‚¢ãƒ€ãƒ«ãƒˆ","2ne1","madrid","sherlock","cute","èŒãˆã‚‹ã‚·ãƒãƒ¥ã‚¨ãƒ¼ã‚·ãƒ§ãƒ³ã«åŒæ„ãªã‚‰rt","best","Ø§Ù„Ø³Ø¹ÙˆØ¯ÙŠÙ‡","truth","ynwa","india","blog","Ø§ÙƒØ´Ù†_ÙŠØ§_Ø¯ÙˆØ±ÙŠ","gossip","books","blessed","ps4share","freebiefriday","beautiful","mustfollow","500aday","mplusplaces","ã‚«ã‚²ãƒ—ãƒ­å¥½ããªäººrt","followall","handmade","afc","æ‹¡æ•£","mendesarmy","arsenal","çµå©š","ãƒ©ãƒ–ãƒ©ã‚¤ãƒ–","otraindianapolis","dubai","pics","admindirectpopularpenipu","funny","pakistan","family","omspiktanya","teamfollow","4musiclfsbeliebers","ØµØ¨Ø§Ø­_Ø§Ù„Ø®ÙŠØ±","paris","women","æ‹æ„›","follow4follow","download","uber","ä¹ƒæœ¨å‚46","Ø­Ù‚ÙŠÙ‚Ø©","yahooãƒ‹ãƒ¥ãƒ¼ã‚¹","forex","friends","ã³ã‚ˆãƒ¼ã‚“","Ø³Ø§Ù…ÙŠ_Ø§Ù„Ø¬Ø§Ø¨Ø±","ãƒã‚¤ã‚­ãƒ¥ãƒ¼ã‚¯ãƒ©ã‚¹ã‚¿ã•ã‚“ã¨ç¹‹ãŒã‚ŠãŸã„","etsymnt","csrclassics","ã‚¢ãƒ¡ãƒ–ãƒ­","shopping","shestheone","rip","bigbang","theresistance","ÙˆØ±Ø¹Ø§Ù†","a","radio","Ù†Ø¬ÙˆÙ…_Ø§Ù„Ø±ØªÙˆÙŠØª","ufc190","ÙÙˆÙ„ÙˆØ¨Ø§Ùƒ","architecture","sale","gameofthrones","contest","csrracing","Ø­Ù‚ÙŠÙ‚Ù‡","iartg","rtã—ãŸäººã§æ°—ã«ãªã£ãŸäººãƒ•ã‚©ãƒ­ãƒ¼ã™ã‚‹","work","babes","ä»Šã®å°å­¦ç”Ÿã¯çŸ¥ã‚‰ãªã„","dating","education","wwe","google","sports","kasabi","technology","success","demilovato","nhl15bergeron","fun","çµµæãã•ã‚“ã¨ç¹‹ãŒã‚ŠãŸã„","mcfc","Ø¨ÙŠØ¹_Ù…ØªØ§Ø¨Ø¹ÙŠÙ†","Ø§Ù„Ø§Ø­Ø³Ø§Ø¡","starwars","iot","nblalinavoto","caracas","selenagomez","æ ¼è¨€","motivation","god","trabajo","realmadrid","nigeria","sales","unfalert","nasigudegmeruya","Ø¹Ù…Ø§Ù†","Ù„Ø²ÙŠØ§Ø¯Ø©","girls","Ø§Ù†ØªØ®Ø¨ÙˆØ§_Ø§Ù„Ø¹Ø±Øµ","nhkç´…ç™½","home","digital","ãƒ•ã‚©ãƒ­ãƒ¼è¿”ã—","gantenggantengserigalasctv","å¯æ„›ã„ã¨æ€ã£ãŸã‚‰rt","egypt","japan","i","ãƒ‹ãƒ¥ãƒ¼ã‚¹","israel","bestlyrics","nct127","shoutout","cool","china","concours","niggernavy","followpyramid","gay","kesombonganalieakbar","wishbdaybywelfare","olsenwpmoychallenge","beach","indonesia","djkingassassin","fcblive","nw","åœ°éœ‡","entertainment","ces2017","interiordesign","realestate","rakutenichiba","smurfsvillage","weather","ã‚¨ãƒ­å‹•ç”»","è‰¦ã“ã‚Œç‰ˆæ·±å¤œã®çœŸå‰£ãŠçµµæã60åˆ†ä¸€æœ¬å‹è² ","ç›¸äº’é™å®š","ãƒ©ãƒ–ãƒ©ã‚¤ãƒãƒ¼ã¨ç¹‹ãŒã‚ŠãŸã„","chicago","girl","ØªØ³Ø¯ÙŠØ¯_Ù‚Ø±ÙˆØ¶","rbooks","Ø§Ø°ÙƒØ§Ø±","ØºØ±Ø¯_Ø¨Ø°ÙƒØ±_Ø§Ù„Ù„Ù‡","mlb","lesanges6","p2","book","pojoksatu","Ù…ØªØµØ¯Ø±_Ù„Ø§ØªÙƒÙ„Ù…Ù†ÙŠ","toronto","app","mexico","ãƒ‰ãƒªãƒ•ãƒˆã‚¹ãƒ”ãƒªãƒƒãƒ„","Ø¨Ø±Ø´Ù„ÙˆÙ†Ø©","jesus","rtã—ãŸã‚ã‚‰ã—ã£ãå…¨å“¡ãƒ•ã‚©ãƒ­ãƒ¼ã™ã‚‹","amwriting","Ø±ØªÙˆÙŠØª_Ù‚ÙˆÙŠ_ÙˆØ³Ø±ÙŠØ¹","bokep","tokyomx","kcacolombia","ebay","å±…é…’å±‹","Ø§Ù„Ù…Ø¯ÙŠÙ†Ø©","edm","share","smpn12yksuksesun","Ù…ÙƒÙˆÙ‡","Ø³ÙƒØ³_Ø¹Ø±Ø¨ÙŠ","cat","ufc207","mcm","hindkanapakkojawab","Ù…ØªØ¶Ø±Ø±ÙŠ_Ù…Ø§Ù†_Ø¯ÙŠÙØ§Ù†","filmtania","çŒ«","us","Ø¬Ù†Ø³_Ø³ÙƒØ³_Ù†ÙŠÙƒ_Ø§ØºØªØµØ§Ø¨_Ø·ÙŠØ²_Ø³Ø§Ø®Ù†_sex_Ù‡ÙŠÙØ§Ø¡_ÙˆÙ‡Ø¨ÙŠ_Ù„Ù„ÙƒØ¨Ø§Ø±_ÙÙ‚Ø·","ÙÙˆÙ„ÙˆÙ…ÙŠ","choicetwit","lyft","å‡ºä¼šã„","celebrities","c91ã‚³ã‚¹ãƒ—ãƒ¬","Ø­Ø³Ø§Ø¨_ÙŠØ³ØªØ­Ù‚_Ø§Ù„Ù…ØªØ§Ø¨Ø¹Ù‡","mÃ©xico","Ø­Ø§Ø¦Ù„","comebackhome","goharshahi","ØªØ³Ø¨ÙŠØ­","shawnfollowme","dog","Ø¬Ø§Ø²Ø§Ù†","favcounter","sexting","instantfollowback","bairavaa","cats","throwback","momoclo","Ø¨Ø±_Ø§Ù„ÙˆØ§Ù„Ø¯ÙŠÙ†","nbljosephinevoto","ãƒ¢ãƒ¼ãƒ‹ãƒ³ã‚°å¨˜","makeup","nblaleydavoto","gh2015","Ù…Ù…Ø­ÙˆÙ†Ù‡","bigdata","here","tityfollowtrain","aaa","piscis","followbackseguro","2a","trndnl","gamedev","online","isis","gÃ¼nkÃ¶mÃ¼rkarasÄ±","canada","Ø§Ø¨Ù‡Ø§","webeliveinyoukris","turkey","goodmorning","albert_stanlie","biafra","Ù…ØªØ§Ø¨Ø¹ÙŠÙ†Ùƒ","info","rtã—ãŸäººãƒ•ã‚©ãƒ­ãƒ¼ã™ã‚‹","swifties","ã‚²ãƒ¼ãƒ ","thf","tlrp","barcelona","mothersday","newyork","Ø¬Ù†Ø³","salud","france","arfahmdtampan","Ø¯ÙŠÙˆØ«","niconews","ÙØ±ØµÙ€Ù€ØªÙƒ","Ù‚Ø±ÙˆØ¨_Ø§Ù„Ø³Ø¹ÙˆØ¯ÙŠØ©","movies","tauro","semihvaroitayfaunfsuzkazandirtiyor","ã‚ãƒ¼ãƒ¼ãƒ¼ãƒ¼ãƒ¼ãƒ¼ã‚¸ãƒ£ãƒ‹ã‚ªã‚¿ã•ã‚“ã¨ç¹‹ãŒã‚‹ãŠæ™‚é–“ãŒã¾ã„ã‚Šã¾ã—ãŸãªã®ã§ã„ã£ã±ã„ç¹‹ãŒã‚Šã¾ã—ã‚‡ãã—ã¦æ¿ƒãçµ¡ã‚“ã§å…ƒæ°—ãªã£ã¡ã‚ƒã„ã¾ã—ã‚‡rtã—ã¦ãã‚ŒãŸæ–¹ã§æ°—ã«ãªã£ãŸæ–¹ãŠè¿Žãˆã§ã™","ê°“ì„¸ë¸","ÙØ­Ù„","gÃ©minis","tntweeters","world","valencia","å€Ÿé‡‘ã‚ã‚‹ã‹ã‚‰ã‚®ãƒ£ãƒ³ãƒ–ãƒ«ã—ã¦ãã‚‹","hair","Ø§Ù„ÙØªØ­","Ø§Ù„Ù…Ù„ÙƒÙŠ","ÙÙŠØ¯ÙŠÙˆ","mlkday","bizitalk","Ø§Ù„Ø³ÙŠØ³ÙŠ","stocks","cÃ¡ncer","smile","breakingnews","healthcare","ãƒ•ã‚©ãƒ­ãƒ¼","giants","tbs","throwbackthursday","escreveai","Ø°ÙƒØ±","gift","uclfinal","qatar","asian","capricornio","Ø¹Ø¬Ù„Ø§Ù†","heat5sos","Ø³Ù…Ù‡","åµ");
  Vertx vertx = Vertx.vertx();
  MySQLConnectOptions connectOptions = new MySQLConnectOptions()
    .setPort(3306)
    .setHost("m3db.c2dez1aoybbc.us-east-1.rds.amazonaws.com")
    .setDatabase("m3")
    .setUser("admin")
    .setPassword("yyp31234");

  // Pool options
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  // Create the client pool
  SqlClient client = MySQLPool.pool(vertx, connectOptions, poolOptions);
  public String parse_search(String user_id,String type1, String phrase, String hashtag) throws NullPointerException {
    String values = (user_id + "," + type1 + "," + phrase + "," + hashtag);
    System.out.println(values);
    HashMap<String, Double> hashing_score_map = new HashMap<>();
    HashMap<String, Double> interaction_score_map = new HashMap<>();
    HashMap<String, Double> keyword_score_map = new HashMap<>();
    HashMap<String, Double> final_score_map = new HashMap<>();
    HashMap<String, List<String>> outputmap = new HashMap<>();
    String sql ="SELECT * FROM Data WHERE uid2=  "+user_id+" or uid1= "+user_id+" ;";
    Future<RowSet<Row>> res1 = client.query(sql).execute();
    res1.onComplete(ar ->{
      if (ar.succeeded()){
        System.out.println(ar.result());
        RowSet<Row> result = ar.result();
        hashing_score_map.put("b",4.0);
      }
    });

    if (res1.isComplete()){
      System.out.println("BROOOOOOOOOOOOOOOOOOOO");
      RowSet<Row> result = res1.result();
      hashing_score_map.put("k",2.0);
    }

//    ArrayList<String> alluserids = new ArrayList<String>();
//    RowSet<Row> result = res1.result();
//    for (Row row : result) {
//      int uid1 = row.getInteger(0);
//      int uid2 = row.getInteger(1);
//      if (String.valueOf(uid1).equals(user_id)){
//        alluserids.add(String.valueOf(uid2));
//      }
//      else if (String.valueOf(uid2).equals(user_id)){
//        alluserids.add(String.valueOf(uid1));
//      }
//    }
    System.out.println("Size :"+Arrays.asList(hashing_score_map));
    //System.out.println("Size :"+ alluserids.size());
    return "Invalid";

  }
}