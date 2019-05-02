drop table yelp_analysis_ks.oldest_10yelpers;
drop table yelp_analysis_ks.top_users_avg_stars;
drop table yelp_analysis_ks.top10_best_rated_hotels;

DROP KEYSPACE yelp_analysis_ks;
CREATE KEYSPACE yelp_analysis_ks
WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}
AND DURABLE_WRITES = false;

CREATE TABLE yelp_analysis_ks.oldest_10yelpers (
    user_id text PRIMARY KEY,
    yelping_since text
) ;

CREATE TABLE yelp_analysis_ks.top_users_avg_stars (
    name text PRIMARY KEY,
    category text,
    avgstars bigint
);

CREATE TABLE yelp_analysis_ks.top10_best_rated_hotels (
    name text PRIMARY KEY,
    state text,
    city text
);