#FUNNEL WORKER

The Funnel worker is responsible for updating the users belonging to each segment. This is done by connecting to the data platform tables (Initially Redshift, now Hive) to run queries associated with each segment and get the data.

###Flow -
The segment update jobs are stored in redis in a queue data structure with scored values and timestamp for the job to be run at.
An infinite for loop picks the jobs once the time stamp is reached and executes the queries to fetch the data.
This data is stored in redis as a first level of data store and is also stored in mysql as a second level of data store.
The requests for segment data is served from redis as all data is available.


###Types of segments -
__Static__ - These segments are one time updated segments. The segments are created to be run at an appropriate time by specifying a query. This query runs at the specified time to get the users and no further update is made to the segment users.

___Current Implementation___ - Once the job is created, it is added to a queue in redis with the time stamp. An infinite for loop keeps querying data from this queue to check if there are any segments  that have to be created. Once the current time crosses the segment time, this is added to another queue which immediately sets the worker running to query the tables to fetch data.

___Proposed Implementation___ - When a static segment is created, schedule a clock-scheduler job for the static segment time. This makes an API call to funnel-worker at the specified time, which triggers the segment update query to populate data.



__Dynamic__ - These segments run at a specified time every day as a cron job to get updated segments. The segments are created by specifying a query. This query runs at a fixed time every day to get the latest updated users. This is run at 9AM and 6PM every day.

___Current Implementation___ - The jobs are stored in a delayed queue with the time stamp at which it has to be next (9AM or 6PM) run at. Once it crosses, it is moved into a main queue. An infinite for loop picks it up and schedules a segment update job.

___Proposed Implementation___ - Use clock scheduler cron job to make an API call to funnel-worker at 9AM and 6PM. This pulls all the segments for which the update has to be run (dynamic) and runs updates one after the other till it finishes.

___Note___ - If the time stamp overshoots to the next cron job, all those which have already run before the new cron time will be updated again and not those that are executed after the new cron time.



__Proxy__ -  These segments are used to get real time data about the users depending on a few fixed tag values.
Tags - New user, Old User, Order count 
