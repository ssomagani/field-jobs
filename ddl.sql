drop procedure NewJob;
drop procedure NewAgent;
drop procedure NewZone;
drop procedure NewRegion;
drop procedure LatestJobs;
drop procedure GetRegionsUnderManager;
drop procedure GetZonesUnderRegion;
drop procedure GetJobsUnderZone;
drop procedure GetJobsByAgent;
drop procedure GetAgentsByZone;
drop procedure GetEachJobByAgent;
drop procedure GetAgentsByRegion;
drop procedure InsertJobsIntoStream;
drop procedure FinishJobs;

drop view JOB_RES_TIME;
drop view JOB_CREATED_BY_SEC;
drop view jobs_by_agent;
drop view jobs_by_region;

drop table agents;
drop table jobs;
drop table zones;
drop table regions;



create table agents (id integer not null, name varchar(30) not null, zone integer not null, created timestamp not null);
partition table agents on column id;
create table zones (id integer not null, manager integer, region integer not null);
create table regions (id integer not null, manager integer);
create table jobs (id integer not null, type tinyint not null, zone integer not null, agent_id integer not null, created timestamp not null, finished timestamp);
partition table jobs on column agent_id;
create index idx_jobs_agent on jobs(agent_id);
create index idx_jobs_zone on jobs(zone);
create index idx_region on zones(region);

create stream JOBS_REPORT 
export to target ManagerReport
partition on column agent_id (
	job_id integer not null,
	type integer not null,
	zone integer not null,
	agent_id integer not null,
	created timestamp,
	finished timestamp
);


create procedure NewJob partition on table jobs column agent_id parameter 3 as insert into jobs values ?, ?, ?, ?, NOW, null;
create procedure NewAgent partition on table agents column id as insert into agents values ?, ?, ?, NOW;
create procedure NewZone as insert into zones values ?, ?, ?;
create procedure NewRegion as insert into regions values ?, ?;
create procedure LatestJobs as select count(*) from jobs where agent_id=? and type=? and DATEADD(MINUTE, 1, created) > NOW();
create procedure GetRegionsUnderManager as select id, manager from regions where manager = ?;
create procedure GetZonesUnderRegion as select id, region from zones where region = ?;
create procedure GetJobsUnderZone as select job_count, zone from jobs_by_zone where zone = ?;
create procedure GetJobsByAgent as select job_count, agent_id from jobs_by_agent where agent_id = ?;
create procedure GetAgentsByZone as select id from agents where zone = ?;
create procedure GetAgentsByRegion as select id from agents where zone in (select id from zones where region = ?);
create procedure GetEachJobByAgent partition on table jobs column agent_id as select id, type, zone, agent_id, created, finished from jobs where agent_id = ?;
create procedure InsertJobsIntoStream partition on table jobs column agent_id as insert into JOBS_REPORT  select * from jobs where agent_id = ?;
create procedure from class com.volt.example.ExportReport;
create procedure FinishJobs partition on table jobs column agent_id as update jobs set FINISHED=now where agent_id = ?;

create view JOB_RES_TIME as select agent_id, SINCE_EPOCH(SECOND, FINISHED) - SINCE_EPOCH(SECOND, CREATED) as RES_TIME from jobs group by agent_id, RES_TIME;
create view JOB_CREATED_BY_SEC as select agent_id, TRUNCATE(SECOND, CREATED) as sec, count(*) from jobs group by agent_id, sec; 
create view jobs_by_agent as SELECT AGENT_ID, zone, COUNT(*) AS JOB_COUNT FROM JOBS GROUP BY AGENT_ID, zone;
create view jobs_by_zone as select zone, count(*) as JOB_COUNT from jobs group by zone;
create view jobs_by_region as select zones.region, count(*) from zones, jobs where zones.id = jobs.zone group by zones.region;
