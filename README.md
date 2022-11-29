# twitterinteraction
User Recommendation System for Twitter users based on interactions, similarity in interests and topics.


Introduction
When you follow someone on Twitter, Facebook or Instagram, the website may recommend some close friends of this user to you. Your web service will implement a similar functionality based on the Twitter dataset we collected.

Before going into the details, let us understand what a tweet object contains.

Tweet ID is in id or id_str field
Tweet content is in the text field
There are 2 contact tweet types.
If a tweet is a reply (e.g. A replies to B), then the ID of user B is in in_reply_to_user_id or in_reply_to_user_id_str
If a tweet is a retweet, the original tweet object is stored in retweeted_status
Hashtags are stored in entities.hashtags
Sender information is stored as a user object in user
Please refer to the official Twitter API documentation provided in the links above for more information.

Specification
The request of Microservice 3 contains

a user ID
contact tweet type [reply|retweet|both]
percent-encoded phrase
a hashtag
You need to respond with information of every contacted user with a positive ranking score in descending order. The ranking score is defined later in this write-up.

For each contacted user with a positive score, you will return

user ID
latest screen_name
latest description
latest contact tweet of the type specified in the query and with the user in the query.
Request Format
The format is,

GET /twitter?user_id=<ID>&type=<TYPE>&phrase=<PHRASE>&hashtag=<HASHTAG>
An example is,

GET /twitter?user_id=10000123&type=retweet&phrase=hello%20cc&hashtag=cmu
Response Format
<TEAMID>,<TEAM_AWS_ACCOUNT_ID>\n
user_id_1\tscreen_name_1\tdescription_1\tcontact_tweet_text\n
user_id_2\tscreen_name_2\tdescription_2\tcontact_tweet_text
where \n is the newline character, and \t is the tab character. The phrase is percent-encoded. Each line ends with \n except the last one.

For example,

TeamCoolCloud,1234-0000-0001\n
10000124\tAlanTuring\tComputer Scientist\tI propose to consider the question, 'Can machines think?'\n
10000125\tDijkstra\tAlso a computer scientist\tSimplicity is prerequisite for reliability.
For any invalid request, respond with

<TEAMID>,<TEAM_AWS_ACCOUNT_ID>\n
INVALID
Requirements
Malformed JSON Object
If a line cannot be parsed as a valid JSON object, filter it out.

Now each line can be treated as a valid JSON object t.

Malformed Tweets
Tweets satisfying any one of these properties are malformed and should be filtered out. You only need to check the information of the outer tweet, you don't need to check information in t.retweeted_status.

Both t.id and t.id_str are missing or null
Both t.user.id and t.user.id_str are missing or null
t.created_at is missing or null
t.text is missing or null or empty string ""
t.entities.hashtags array is missing or null or of length zero/empty
Language of Tweets
For Microservice 3, we only consider tweets of these languages:

| language   | code in lang field |
| ---------- | ------------------ |
| Arabic     | ar                 |
| English    | en                 |
| French     | fr                 |
| Indonesian | in                 |
| Portuguese | pt                 |
| Spanish    | es                 |
| Turkish    | tr                 |
| Japanese   | ja                 |
Filter out tweets whose t.lang field is NOT listed above.

Duplicate Tweets
For duplicate tweets (those with the same id), retain only one of them.

Contact Tweets
After the filtering, each line from the dataset can be treated as a valid tweet JSON object t.

Definition of Contact Tweet:

A contact tweet is a tweet that is either a reply tweet or a retweet.

A tweet is a reply tweet if t.in_reply_to_user_id is not null.

A tweet is a retweet if t.retweeted_status is not null.

Field names below are for the ease of demonstration. Refer to the Twitter API for the exact field name. Say we have the following tweets:

| tweet_id | user_id | content   | reply_to_id | retweet_to_id |
| -------- | ------- | --------- | ----------- | ------------- |
| 01       | 15618   | cloud     | 15213       | null          |
| 02       | 15640   | computing | null        | 15319         |
| 03       | 15513   | is        | 15213       | null          |
| 04       | 15513   | fun       | null        | null          |
Then we have the following:

| user_id | contact_tweet_id | contacted_user |
| ------- | ---------------- | -------------- |
| 15213   | 01, 03           | 15618, 15513   |
| 15513   | 03               | 15213          |
| 15319   | 02               | 15640          |
| 15618   | 01               | 15213          |
| 15640   | 02               | 15319          |
User Information
User information is stored in a user object as specified in the Tweet API and it can appear in t and t.retweeted_status objects. Therefore,

For any tweet t, we can find the sender information in t.user
If the tweet t happens to be a retweet, we can additionally find the original poster’s information in t.retweeted_status.user
However, given that our tweet dataset is chronological, some user objects may be outdated and you will need to find the latest information for a user if any. Please always use t.created_at from the outer tweet object t to get the most updated user information. If multiple tweet objects have the same created_at timestamp, break the tie by tweet ID descending in numerical order and choose the first one as the latest user information.

Note: For the user object stored as t.retweeted_status.user, please still use t.created_at instead of t.retweeted_status.created_at as the timestamp. Using screen_name "" and description "" as the default value if they are null or missing.

Score
Going back to a use case of our User Recommendation System, a Twitter user, say Alpha, follows Beta. The system should recommend close friends of Alpha that Beta may be interested in. Our ranking system factors in the closeness between Alpha and his friends as well as the interests of Beta. Specifically, the ranking algorithm consists of three parts: interaction score, hashtag score, and keyword score.

Interaction score calculates the frequency of interactions between two users (Or Alpha and his friends in the above narratives).
Hashtag score calculates the common interest shared by two users (Or Alpha and his friends in the above narratives), as reflected by the hashtags in the tweets they posted.
Keyword score factors in the queried key phrase and hashtag (Or Beta's interests in the above narratives).
Note: You don't need to deal with nested tweets. For example, we are analyzing a nested tweet object from the outermost level. A tweet A is a retweet of another tweet B, and the tweet B is a reply tweet to C. We only need to analyze the information in tweet A and tweet B. That is to say, we only consider the two outermost tweets when meeting a nested tweet.

1. Interaction Score
There are two types of interactions: reply and retweet. The more replies and retweets between two users, the higher their interaction score. Interaction score is calculated as log(1 + 2 * reply_count + retweet_count)

Some examples are below:

A replied B 4 times; B retweeted A 3 times log(1 + 2*4 + 1*3) = 2.485
A replied B twice; B replied A once log(1 + 2*(2+1) + 1*0) = 1.946
A retweeted B once log(1 + 2*0 + 1*1) = 0.693
no replies/retweets between A and B log(1 + 2*0 + 1*0) = 0
You may spot cases where the reply or retweet is to the same user of the original tweet (self-reply or self-retweet). We will actually count those contact tweets and hence someone may have an interaction score with oneself. It is interesting to see if a user ends up having a higher score than the user's friends.

2. Hashtag Score
The hashtag score is based on the frequency of the same hashtags (case insensitive) posted by users among all the valid tweets. By posting, we mean that it is the user who is stored in t.user object of a tweet object. Don't consider t.retweeted_status.user.

Very popular hashtags may not really demonstrate common interests between two users. (Why? You need to discuss the reason for this in the report). As such, we do not consider them when calculating the hashtag score. We provide a list of hashtag (case insensitive) to be excluded. (Please set your browser, parser or text editor to be encoding-aware when reading this list!) Note that for the excluded hashtags, we do not filter out those tweets. We just ignore those hashtags in the hashtag score calculation.

Here are a few examples. Assume the hashtag zipcode is a very popular hashtag that we exclude.

| sender_uid | hashtags            |
| ---------- | ------------------- |
| 15619      | Aws, azure, ZIPCODE |
| 15619      | Cloud, Azure        |
| 15619      | Cloud, GCP          |
| 15619      | cloud, aws          |
| 15319      | cmu, us             |
| 15319      | AZure               |
| 15319      | Cloud, GCP          |
| 15319      | aWs, zipcode, CLOUD |
| 15513      | cmu, us             |
| 15513      | haha, ZIPcode       |
| 15513      | zipcode             |
Given all the tweets above, the hashtag score of the user pairs below are:

| uid_1 | uid_2 | same_tag_count | explanation                    |
| ----- | ----- | -------------- | ------------------------------ |
| 15619 | 15319 | 13             | aws=3, cloud=5, azure=3, GCP=2 |
| 15619 | 15513 | 0              | no match                       |
| 15319 | 15513 | 4              | cmu=2, us=2                    |
The final hashtag_score is calculated as follows.

If same_tag_count > 10, then hashtag_score = 1 + log(1 + same_tag_count - 10).
Else, hashtag_score = 1
Note: For the cases of self-reply or self-retweet (the reply or retweet is to the same user of the original tweet), the hashtag score will always be 1.

Hint: the rules of converting characters into lower case differ between different programming languages.

Java: use toLowerCase(Locale.ENGLISH) in String Class
Python use import locale; locale.setlocale(locale.LC_ALL,'en_US.UTF-8'); before calling lower()
3. Keywords Score
Keywords score is calculated by counting the total number of matches of phrase and also hashtag (both provided in the query) across the contact tweets of a specific type. The type is given in the query, and valid values are [reply|retweet|both]. Going back to our use case and say Beta is a fan of cloud computing, hence we may want to examine the content and hashtags of the contact tweets between Alpha and its friends to see with whom Alpha discusses cloud computing the most and recommend it to Beta.

When calculating the keywords score, we count all the hashtags including those in the filtering list. In the query,

if type equals reply, then the score is calculated only based on reply tweets
if type equals retweet, then the score is calculated on retweets only
if type equals both, then the score is calculated based on all contacted tweets
Matching rule for the phrase: case sensitive match.

For example, for phrase life

lifeisgood has a match
it's my Lifelife has a match
that's my lifelife lesson has two matches
Another example, for phrase haha

hahaha has 2 matches (overlapping matches are possible)
haHaha has no matches
Haha bahaha has 1 match
Matching rule for the hashtag: case insensitive exact match.

For example, if hashtag in the request is cloud, and a tweet has hashtags #Cloud #CLOUD #CLOUD #cmu (note that duplicate tags are allowed), then this tweet will add 3 to number_of_matches.

Between two users, if there are no contact tweets of the type specified in the query, then keywords_score = 0.

Otherwise, keywords_score = 1 + log(number_of_matches + 1).

Keywords score example
Given request & tweet: GET /twitter?user_id=15513&type=retweet&phrase=hello%20cc&hashtag=cmu

| tweet_id | sender_uid | content       | reply_to_uid | retweet_to_uid | hashtags    |
| -------- | ---------- | ------------- | ------------ | -------------- | ----------- |
| 01       | 15618      | hello cc, fun | 15513        | null           | cmu, pitt   |
| 02       | 15213      | computing     | null         | 15513          | CMU         |
| 03       | 15513      | is            | 15213        | null           | PA, US, CMU |
| 04       | 15513      | hello cc!!    | null         | 15640          | cMu, cmu    |
Result:

| user_id | phrase_match | hashtag_match | keywords_score |
| ------- | ------------ | ------------- | -------------- |
| 15618   | null         | null          | 0              |
| 15213   | 0            | 1             | 1 + log(1+1)   |
| 15640   | 1            | 2             | 1 + log(3+1)   |
Final Score
The final ranking score between two users is calculated as final_score = interaction_score * hashtag_score * keywords_score

For the calculations above, log base is e, and keep 5 decimal points of precision for the final score rounding half up. We will ignore user pairs with a final score of 0.

Hint: please think about the scoring criteria and the implication of threshold before starting your ETL pipeline!

Ordering
Your web service should return the most up-to-date information of users and their latest contact tweet with the user in the query ordered by the score calculated above in descending order. Break ties by user ID in descending numerical order.

For the latest contact tweets, break the tie by tweet ID in descending numerical order if they have the same timestamp.
