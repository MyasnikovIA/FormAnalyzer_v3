# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ Forms/Schedules/schedules_edit_hp.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Tue May 19 00:03:50 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 13
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_WEEKS
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_WEEKS" activateoncreate="false" compile="true">
		<![CDATA[
		select level week_num,
			@if (:s_type==2) {
               case when level=1 then 'Для нечетных дней' else 'Для четных дней' end week_num_caption
            @} else {
               'Неделя №'||level week_num_caption
            @}
          from DUAL
		 connect by level <= (select ceil(max(DAY_NUMBER) / 7)
	                		    from D_V_SCHEDULESP ss
	                           where ss.PID = :sch_id)
		]]>
		<component cmptype="Variable" name="sch_id" get="sch_id" src="ScheduleId" srctype="var" />
		<component cmptype="Variable" name="s_type" get="s_type" src="schedule_type" srctype="ctrl" />
	</component>
```

**Используемые таблицы/вьюхи:** D_V_SCHEDULESP

---

### Запрос №2

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_DAYS
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_DAYS" activateoncreate="false">
		<![CDATA[
		select level day_num,
		       case level
                     when 1 then 'Пн.'
                     when 2 then 'Вт.'
                     when 3 then 'Ср.'
                     when 4 then 'Чт.'
                     when 5 then 'Пт.'
                     when 6 then 'Сб.'
                     when 7 then 'Вс.'
                end day_name
		  from DUAL
		 connect by level <= 7
		]]>
	</component>
```


---

### Запрос №3

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_DAYS_CHN
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_DAYS_CHN" activateoncreate="false">
		<![CDATA[
		select level day_num,1 week_num,
        		   case level
                  when 1 then 'Нечет.'
                  when 2 then 'Чет.'
               end day_name
          from DUAL
        connect by level <= 2
		]]>
	</component>
```


---

### Запрос №4

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_DAYS_MONTH
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_DAYS_MONTH" activateoncreate="false">
		select sp.DAY_NUMBER DAY_NUM,
               1 WEEK_NUM,
			   sp.ID DAY_ID
		  from D_V_SCHEDULESP sp
		 where sp.PID = :sch_id
		order by sp.day_number
		<component cmptype="Variable" name="sch_id" get="sch_id" src="ScheduleId" srctype="var" />
		<component cmptype="Variable" name="schedule_type" get="schedule_type" src="schedule_type" srctype="ctrl" />
	</component>
```

**Используемые таблицы/вьюхи:** D_V_SCHEDULESP

---

### Запрос №5

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_WEEK
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_WEEK" activateoncreate="false">
            select 1 week_num
            from dual
	</component>
```


---

### Запрос №6

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_DAY_TIMES
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_DAY_TIMES" activateoncreate="false" compile="true">
		select
			@if ( (:schedule_type==4)||(:schedule_type==3)) {
		       sp.day_number
            @} else {
               mod(sp.day_number-1, 7)+1
            @}
               day_num,
            @if ((:schedule_type==4)||(:schedule_type==3)) {
               1
            @} else {
               ceil(sp.day_number/7)
            @}
               week_num,
		       sp.ID DAY_ID,
               nvl(dt.TIME_TYPE, -1) TIME_TYPE,
               nvl(dt.TIME_NAME,'Интервал Квотирования') TIME_NAME,
               dt.ID,
		       to_char(dt.TIME_BEGIN, 'hh24:mi') TIME_BEGIN,
               to_char(dt.TIME_END, 'hh24:mi') TIME_END,
               dt.STEP,
               dt.LIMITS
		  from D_V_SCHEDULESP_TIMES dt
		       join D_V_SCHEDULESP sp on sp.ID = dt.PID
		 where sp.PID = :sch_id
	  order by sp.DAY_NUMBER, TIME_BEGIN
		<component cmptype="Variable" name="sch_id" get="sch_id" src="ScheduleId" srctype="var" />
		<component cmptype="Variable" name="schedule_type" get="schedule_type" src="schedule_type" srctype="ctrl" />
	</component>
```

**Используемые таблицы/вьюхи:** D_V_SCHEDULESP_TIMES, D_V_SCHEDULESP

---

### Запрос №7

**Тип компонента:** M2 Action
**Имя компонента:** Init
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="Init">
		<![CDATA[
			begin
			  begin
			  	select sch.ID,
			  		   sch.NAME,
			  		   sch.CODE,
			  		   to_char(sch.START_DATE, 'dd.mm.yyyy'),
			  		   sch.sch_type,
			  		   sch.QUOTING
			  	  into :sch_id,
			  		   :sch_name,
			  		   :sch_code,
			  		   :sch_start_date,
			  		   :sch_type,
			  		   :QUOTING
			  	  from D_V_SCHEDULE sch
			  	 where sch.ID = :sch_id;
			  exception when NO_DATA_FOUND then
				:sch_name       := '';
				:sch_code       := '';
				:sch_start_date := '';
				:sch_type	    := '';
			  end;

			  if :sch_type=3 then
			    select count(1)
				  into :PERIOD
				  from D_V_schedulesp t
				 where t.pid = :sch_id;
              else
                :PERIOD := null;
              end if;
			end;
		]]>
		<component cmptype="ActionVar" name="sch_id" get="sch_id" src="ScheduleId" srctype="var" />
		<component cmptype="ActionVar" name="sch_name" put="sch_name" len="256" src="schedule_name" srctype="ctrl" />
		<component cmptype="ActionVar" name="sch_code" put="sch_code" len="60" src="schedule_code" srctype="ctrl" />
		<component cmptype="ActionVar" name="sch_start_date" put="sch_start_date" len="10" src="start_date" srctype="ctrl" />
		<component cmptype="ActionVar" name="sch_start_date" put="sch_st_d2" len="10" src="start_date" srctype="var" />
		<component cmptype="ActionVar" name="sch_type" put="sch_type" len="1" src="schedule_type" srctype="ctrl" />
		<component cmptype="ActionVar" name="QUOTING" put="QUOTING" len="1" src="QUOTING" srctype="ctrl" />
		<component cmptype="ActionVar" name="PERIOD" put="PERIOD" len="2" src="PERIOD" srctype="ctrl" />
	</component>
```

**Используемые таблицы/вьюхи:** D_V_SCHEDULE, D_V_SCHEDULESP

---

### Запрос №8

**Тип компонента:** M2 Action
**Имя компонента:** TimeName
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="TimeName" mode="post">
        <![CDATA[
            begin
                :WEEKDAY := D_PKG_DAT_TOOLS.GET_WEEK_DAY_NUM(:pdstart_date);
            end;
        ]]>
        <component cmptype="ActionVar" name="pdstart_date" get="dstart_date" src="start_date" srctype="ctrl" />
        <component cmptype="ActionVar" name="WEEKDAY" put="WEEKDAY" src="WEEKDAY" srctype="var" />
    </component>
```

**Используемые пакеты/функции:** D_PKG_DAT_TOOLS.GET_WEEK_DAY_NUM

---

### Запрос №9

**Тип компонента:** M2 Action
**Имя компонента:** AddEditSchedule
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="AddEditSchedule" mode="post">
		<![CDATA[
			declare sholidays  varchar2(7);
			begin
			  sholidays := :MON_HOLIDAY||:TUE_HOLIDAY||:WED_HOLIDAY||:THU_HOLIDAY||:FRI_HOLIDAY||:SAT_HOLIDAY||:SUN_HOLIDAY;
			  begin
			    select sch.id
			      into :ScheduleId
			      from D_V_SCHEDULE sch
			     where sch.id = :updScheduleId;

				D_PKG_SCHEDULE.UPD(pnID         => :updScheduleId,
				  				   pnLPU        => :pnlpu,
				  				   psCODE       => :pscode,
				  				   psNAME       => :psname,
				  		           pdSTART_DATE => :pdstart_date,
                                   pnSCH_TYPE   => :sch_type,
                                   psHOLIDAYS   => sholidays,
                                   pnQUOTING    => :QUOTING);

			  exception when NO_DATA_FOUND then

			  D_PKG_SCHEDULE.ADD(pnD_INSERT_ID => :ScheduleId,
		 		  				 pnLPU         => :pnlpu,
				  				 pnCID         => :pncid,
				  				 psCODE        => :pscode,
				  			     psNAME        => :psname,
				  				 pdSTART_DATE  => :pdstart_date,
				  				 pnSCH_TYPE    => :sch_type,
				  				 psHOLIDAYS    => sholidays,
				  				 pnQUOTING     => :QUOTING,
                                 pnSCH_KIND    => 1);
		      end;
            end;
		]]>
		<component cmptype="ActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="pscode" get="scode" src="schedule_code" srctype="ctrl" />
		<component cmptype="ActionVar" name="psname" get="sname" src="schedule_name" srctype="ctrl" />
		<component cmptype="ActionVar" name="pdstart_date" get="dstart_date" src="start_date" srctype="ctrl" />
		<component cmptype="ActionVar" name="pncid" get="ncid" src="CatalogId" srctype="var" />
		<component cmptype="ActionVar" name="updScheduleId" get="getScheduleId" src="UpdScheduleId" srctype="var" />
		<component cmptype="ActionVar" name="ScheduleId" put="ScheduleId" src="ScheduleId" srctype="var" len="17" />
		<component cmptype="ActionVar" name="sch_type" get="sch_type" src="schedule_type" srctype="ctrl" />
		<component cmptype="ActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="ctrl" />
		<component cmptype="ActionVar" name="MON_HOLIDAY" get="MON_HOLIDAY" src="MON_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="TUE_HOLIDAY" get="TUE_HOLIDAY" src="TUE_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="WED_HOLIDAY" get="WED_HOLIDAY" src="WED_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="THU_HOLIDAY" get="THU_HOLIDAY" src="THU_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="FRI_HOLIDAY" get="FRI_HOLIDAY" src="FRI_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SAT_HOLIDAY" get="SAT_HOLIDAY" src="SAT_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SUN_HOLIDAY" get="SUN_HOLIDAY" src="SUN_HOLIDAY" srctype="ctrl" />
		
		
		<component cmptype="SubAction" name="UpdWeek" groupname="weeks" type="upd">
			<component cmptype="SubActionVar" name="week_num" get="week_num" src="weeks" srctype="ctrl" />
			<component cmptype="SubActionVar" name="ScheduleId" get="ScheduleId" src="ScheduleId" srctype="parent" />
			<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

			<component cmptype="SubAction" name="AddDay" groupname="days" type="upd">
				<![CDATA[
					begin
				  		begin
				   			select sp.id
				   			  into :day_id
				              from D_V_SCHEDULESP sp
				             where sp.pid = :pnsch_id
				               and sp.day_number = (:week_num - 1) * 7 + to_number(:day_num);
				  			exception when NO_DATA_FOUND then
				   			d_pkg_schedulesp.add( pnd_insert_id => :day_id,
				                                          pnlpu => :pnlpu,
				                                          pnpid => :pnsch_id,
				                                   pnday_number => (:week_num - 1) * 7 + to_number(:day_num),
				                                   pdtime_begin => null,
				                                     pdtime_end => null);
				  		end;
					end;
			 	]]>
				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="week_num" get="week_num" put="week_num" src="week_num" srctype="parent" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="days" srctype="ctrl" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

				<component cmptype="SubAction" name="DelTimeType" groupname="time_types" type="del">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="DelTime" groupname="day_times" type="del">
						begin
						  D_PKG_SCHEDULESP_TIMES.DEL(pnID =&gt; :pnid,
						                             pnLPU =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>
				</component>

				<component cmptype="SubAction" name="UpdTimeType" groupname="time_types" type="upd">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="DelTime" groupname="day_times" type="del">
						begin
						  d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
						                            pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime" groupname="day_times" type="upd">
                        <![CDATA[
						declare nTime_type NUMBER(17);
						begin
					   		if :QUOTING=1 then
								nTime_type := null;
							else
								nTime_type := :pntime_type;
						    end if;
							D_PKG_SCHEDULESP_TIMES.UPD_S(pnID        => :pnid,
						                                pnLPU        => :pnlpu,
						                                psTIME_BEGIN => :pstime_begin,
						                                psTIME_END   => :pstime_end,
						                                pnTIME_TYPE  => nTime_type,
						                                pnGEN_ERROR  => 0,
                                                        vAPI_VERSION => 2,
                                                        pnSTEP       => :STEP,
                                                        pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
						<component cmptype="SubActionVar" name="QUOTING" src="QUOTING" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime" groupname="day_times" type="add">
                        <![CDATA[
						declare nTime_type NUMBER(17);
						begin
						 	if :QUOTING=1 then
								nTime_type := null;
							else
								nTime_type:=:pntime_type;
							end if;
						  	D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                                 pnLPU         => :pnlpu,
						                                 pnPID         => :pnpid,
						                                 psTIME_BEGIN  => :pstime_begin,
						                                 psTIME_END    => :pstime_end,
						                                 pnTIME_TYPE   => nTime_type,
						                                 pnGEN_ERROR   => 0,
                                                         pnSTEP        => :STEP,
                                                         pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
						<component cmptype="SubActionVar" name="QUOTING" src="QUOTING" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="day_times" srctype="ctrl" put="" len="17" />
					</component>

				</component>
				<component cmptype="SubAction" name="AddTimeType" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="AddTime" groupname="day_times" type="add">
                        <![CDATA[
						declare nTime_type NUMBER(17);
						begin
						  	if :QUOTING=1 then
								nTime_type := null;
							else
								nTime_type:=:pntime_type;
							end if;
						  	D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                                 pnLPU         => :pnlpu,
						                                 pnPID         => :pnpid,
						                                 psTIME_BEGIN  => :pstime_begin,
						                                 psTIME_END    => :pstime_end,
						                                 pnTIME_TYPE   => nTime_type,
						                                 pnGEN_ERROR   => 0,
                                                         pnSTEP        => :STEP,
                                                         pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
						<component cmptype="SubActionVar" name="QUOTING" src="QUOTING" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
			</component>
		</component>

		
		<component cmptype="SubAction" name="AddWeek" groupname="weeks" type="add">
			<![CDATA[
            declare
            	nDAY_NUMBER NUMBER;
            begin
                select ceil(max(ss.DAY_NUMBER)/7)
                  into nDAY_NUMBER
                  from D_V_SCHEDULESP ss
                 where ss.PID = :SCHEDULEID;

            	if (nDAY_NUMBER is null) then
            		:week_num := 1;
            	else
            		:week_num := nDAY_NUMBER + 1;
            	end if;
            end;
			]]>
			<component cmptype="SubActionVar" name="week_num" put="week_num" src="week_num" srctype="var" len="5" />
			<component cmptype="SubActionVar" name="ScheduleId" get="ScheduleId" src="ScheduleId" srctype="parent" />
			<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

			<component cmptype="SubAction" name="AddDay" groupname="days" type="upd">
				begin
				  	begin
				   		select sp.id
				          into :day_id
				          from d_v_schedulesp sp
				         where sp.pid = :pnsch_id
				           and sp.day_number = (:week_num - 1) * 7 + to_number(:day_num);
				  		exception when NO_DATA_FOUND then
				   			d_pkg_schedulesp.add( pnd_insert_id =&gt; :day_id,
				                                          pnlpu =&gt; :pnlpu,
				                                          pnpid =&gt; :pnsch_id,
				                                   pnday_number =&gt; (:week_num - 1) * 7 + to_number(:day_num),
				                                   pdtime_begin =&gt; null,
				                                     pdtime_end =&gt; null);
				  	end;
				end;
				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="week_num" get="week_num" src="week_num" srctype="parent" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="days" srctype="ctrl" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

			   	<component cmptype="SubAction" name="AddTimeType" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="AddTime" groupname="day_times" type="add">
                        <![CDATA[
						declare nTime_type NUMBER(17);
						begin
						  	if :QUOTING=1 then
								nTime_type := null;
							else
								nTime_type := :pntime_type;
							end if;
						  	D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                                 pnLPU         => :pnlpu,
						                                 pnPID         => :pnpid,
						                                 psTIME_BEGIN  => :pstime_begin,
						                                 psTIME_END    => :pstime_end,
						                                 pnTIME_TYPE   => nTime_type,
						                                 pnGEN_ERROR   => 0,
                                                         pnSTEP        => :STEP,
                                                         pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
						<component cmptype="SubActionVar" name="QUOTING" src="QUOTING" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
			</component>
		</component>

		
		<component cmptype="SubAction" name="DelWeek" groupname="weeks" type="del">
			begin
			  d_pkg_schedule.del_last_week(pnid =&gt; :ScheduleId,
			                              pnlpu =&gt; :pnlpu,
			                           pnnumber =&gt; :week_num);
			end;
			<component cmptype="SubActionVar" name="week_num" src="weeks" get="week_num" srctype="ctrl" />
			<component cmptype="SubActionVar" name="ScheduleId" src="ScheduleId" get="ScheduleId" srctype="parent" />
			<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
		</component>

		
		<component cmptype="SubAction" name="CheckSchedule" mode="execlast">
			  begin
				D_PKG_SCHEDULE.CHECK_INTERVAL(:sch_id, :lpu_id);
			    for schedule in (select c.ID,
			                            c.IS_SUBST,
			                            c.LPU,
			                            c.EMPLOYER,
			                            c.CABLAB,
			                            c.DBEGIN,
			                            c.DEND
			                       from D_V_CLSCHS_BASE c
			                      where c.SCHEDULE = :sch_id
			                        and c.LPU = :lpu_id)
			    loop
			      if schedule.IS_SUBST in (0,2) then
				    for subst in (select cl.ID
			                        from D_V_CLSCHS_BASE cl
			                       where cl.LPU = schedule.LPU
			                         and cl.CABLAB = schedule.CABLAB
			                         and cl.EMPLOYER = schedule.EMPLOYER
			                         and cl.CLSCH_TYPE = 0
			                         and cl.IS_SUBST = 1
			                         and schedule.DEND  between cl.DBEGIN and coalesce(cl.DEND, to_date('31.12.2999', 'DD.MM.YYYY'))
			                     )
			        loop
			          D_PKG_TIMETABLE.CLEAR_TIMETABLE(:lpu_id, subst.ID, 1);
			          D_PKG_TIMETABLE.GEN_TIMETABLE(:lpu_id, subst.ID);
			        end loop;
			        D_PKG_TIMETABLE.CLEAR_TIMETABLE(:lpu_id, schedule.ID, 1);
			        D_PKG_TIMETABLE.GEN_TIMETABLE(:lpu_id, schedule.ID);
			      end if;
			    end loop;
			  end;
			<component cmptype="SubActionVar" name="sch_id" get="ScheduleId" src="ScheduleId" srctype="parent" />
			<component cmptype="SubActionVar" name="lpu_id" get="lpu" src="LPU" srctype="session" />
		</component>
	</component>
```

**Используемые таблицы/вьюхи:** D_V_SCHEDULE
**Используемые пакеты/функции:** D_PKG_SCHEDULE.UPD, D_PKG_SCHEDULE.ADD

---

### Запрос №10

**Тип компонента:** M2 Action
**Имя компонента:** AddEditSchedule_chn
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="AddEditSchedule_chn" mode="post">
		declare
   			sholidays  varchar2(7);
  		begin
			sholidays := :MON_HOLIDAY||:TUE_HOLIDAY||:WED_HOLIDAY||:THU_HOLIDAY||:FRI_HOLIDAY||:SAT_HOLIDAY||:SUN_HOLIDAY;
			begin
				select sch.id
		          into :ScheduleId
				  from d_v_schedule sch
				 where sch.id = :updScheduleId;

			    D_PKG_SCHEDULE.UPD(pnID         =&gt; :updScheduleId,
				  				   pnLPU        =&gt; :pnlpu,
				  				   psCODE       =&gt; :pscode,
				  				   psNAME       =&gt; :psname,
				  		           pdSTART_DATE =&gt; :pdstart_date,
                                   pnSCH_TYPE   =&gt; :sch_type,
                                   psHOLIDAYS   =&gt; sholidays,
                                   pnQUOTING    =&gt; :QUOTING);

				exception when NO_DATA_FOUND then

				  D_PKG_SCHEDULE.ADD(pnD_INSERT_ID =&gt; :ScheduleId,
                                     pnLPU         =&gt; :pnlpu,
                                     pnCID         =&gt; :pncid,
                                     psCODE        =&gt; :pscode,
                                     psNAME        =&gt; :psname,
                                     pdSTART_DATE  =&gt; :pdstart_date,
                                     pnSCH_TYPE    =&gt; :sch_type,
                                     psHOLIDAYS    =&gt; sholidays,
                                     pnQUOTING     =&gt; :QUOTING,
                                     pnSCH_KIND    =&gt; 1);
			end;
		end;

		<component cmptype="ActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="pscode" get="scode" src="schedule_code" srctype="ctrl" />
		<component cmptype="ActionVar" name="psname" get="sname" src="schedule_name" srctype="ctrl" />
		<component cmptype="ActionVar" name="pdstart_date" get="dstart_date" src="start_date" srctype="ctrl" />
		<component cmptype="ActionVar" name="pncid" get="ncid" src="CatalogId" srctype="var" />
		<component cmptype="ActionVar" name="updScheduleId" get="getScheduleId" src="UpdScheduleId" srctype="var" />
		<component cmptype="ActionVar" name="ScheduleId" put="ScheduleId" src="ScheduleId" srctype="var" len="17" />
		<component cmptype="ActionVar" name="sch_type" get="sch_type" src="schedule_type" srctype="ctrl" />
		<component cmptype="ActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="ctrl" />
		<component cmptype="ActionVar" name="MON_HOLIDAY" get="MON_HOLIDAY" src="MON_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="TUE_HOLIDAY" get="TUE_HOLIDAY" src="TUE_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="WED_HOLIDAY" get="WED_HOLIDAY" src="WED_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="THU_HOLIDAY" get="THU_HOLIDAY" src="THU_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="FRI_HOLIDAY" get="FRI_HOLIDAY" src="FRI_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SAT_HOLIDAY" get="SAT_HOLIDAY" src="SAT_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SUN_HOLIDAY" get="SUN_HOLIDAY" src="SUN_HOLIDAY" srctype="ctrl" />
		
		<component cmptype="SubAction" name="ForUpdate_CHN" groupname="week_chn">
			
			<component cmptype="SubActionVar" name="ScheduleId" get="ScheduleId" src="ScheduleId" srctype="parent" />

			<component cmptype="SubAction" name="AddDay_CHN" groupname="days_chn" type="upd">
				begin
			  		begin
			    		select sp.id
				          into :day_id
				          from d_v_schedulesp sp
				         where sp.pid = :pnsch_id
				           and sp.day_number = :day_num;
				   		exception when NO_DATA_FOUND then
				   			d_pkg_schedulesp.add( pnd_insert_id =&gt; :day_id,
				                                          pnlpu =&gt; :pnlpu,
				                                          pnpid =&gt; :pnsch_id,
				                                   pnday_number =&gt; :day_num,
				                                   pdtime_begin =&gt; null,
				                                     pdtime_end =&gt; null);
			  		end;
				end;

				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="days_chn" srctype="ctrl" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />

				<component cmptype="SubAction" name="AddTimeType_CHN" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_CHN" groupname="day_times" type="del">
						begin
					  		d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
					                                  pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" get="nid" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_CHN" groupname="day_times" type="upd">
                        <![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_CHN" groupname="day_times" type="add">
                        <![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                               pnLPU         => :pnlpu,
						                               pnPID         => :pnpid,
						                               psTIME_BEGIN  => :pstime_begin,
						                               psTIME_END    => :pstime_end,
						                               pnTIME_TYPE   => :pntime_type,
						                               pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="UpdTimeType_CHN" groupname="time_types" type="upd">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_CHN" groupname="day_times" type="del">
						begin
						  	d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
													  pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_CHN" groupname="day_times" type="upd">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_CHN" groupname="day_times" type="add">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                               pnLPU         => :pnlpu,
						                               pnPID         => :pnpid,
						                               psTIME_BEGIN  => :pstime_begin,
						                               psTIME_END    => :pstime_end,
						                               pnTIME_TYPE   => :pntime_type,
						                               pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="insert_id" len="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="DelTimeType_CHN" groupname="time_types" type="del">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="DelTime_CHN1" groupname="day_times" type="del">
						begin
						  d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
						                             pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>
				</component>
			</component>
			
			<component cmptype="SubAction" name="CheckSchedule_CHN" mode="execlast">
				  begin
					D_PKG_SCHEDULE.CHECK_INTERVAL(:sch_id, :lpu_id);
				    for schedule in (select c.ID,
				                            c.IS_SUBST,
				                            c.LPU,
				                            c.EMPLOYER,
				                            c.CABLAB,
				                            c.DBEGIN,
				                            c.DEND
				                       from D_V_CLSCHS_BASE c
				                      where c.SCHEDULE = :sch_id
				                        and c.LPU = :lpu_id)
				    loop
				      if schedule.IS_SUBST in (0,2) then
				        for subst in (select cl.ID
				                        from D_V_CLSCHS_BASE cl
				                       where cl.LPU = schedule.LPU
				                         and cl.CABLAB = schedule.CABLAB
				                         and cl.EMPLOYER = schedule.EMPLOYER
				                         and cl.CLSCH_TYPE = 0
				                         and cl.IS_SUBST = 1
				                         and schedule.DEND between cl.DBEGIN and coalesce(cl.DEND, to_date('31.12.2999', 'DD.MM.YYYY'))
				                     )
				        loop
				          D_PKG_TIMETABLE.CLEAR_TIMETABLE(:lpu_id, subst.ID, 1);
				          D_PKG_TIMETABLE.GEN_TIMETABLE(:lpu_id, subst.ID);
				        end loop;
				        D_PKG_TIMETABLE.CLEAR_TIMETABLE(:lpu_id, schedule.ID, 1);
				        D_PKG_TIMETABLE.GEN_TIMETABLE(:lpu_id, schedule.ID);
				      end if;
				    end loop;
				  end;
				<component cmptype="SubActionVar" name="sch_id" get="ScheduleId" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="lpu_id" get="lpu" src="LPU" srctype="session" />
			</component>
		</component>
	</component>
```

**Используемые пакеты/функции:** D_PKG_SCHEDULESP_TIMES.UPD_S

---

### Запрос №11

**Тип компонента:** M2 Action
**Имя компонента:** AddEditScheduleMonth
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="AddEditScheduleMonth" mode="post">
		declare
   			sholidays  varchar2(7);
  		begin
			sholidays := :MON_HOLIDAY||:TUE_HOLIDAY||:WED_HOLIDAY||:THU_HOLIDAY||:FRI_HOLIDAY||:SAT_HOLIDAY||:SUN_HOLIDAY;
			begin
				select sch.id
		          into :ScheduleId
				  from d_v_schedule sch
				 where sch.id = :updScheduleId;

			    D_PKG_SCHEDULE.UPD(pnID         =&gt; :updScheduleId,
				  				   pnLPU        =&gt; :pnlpu,
				  				   psCODE       =&gt; :pscode,
				  				   psNAME       =&gt; :psname,
				  		           pdSTART_DATE =&gt; :pdstart_date,
                                   pnSCH_TYPE   =&gt; :sch_type,
                                   psHOLIDAYS   =&gt; sholidays,
                                   pnQUOTING    =&gt; :QUOTING);

				exception when NO_DATA_FOUND then
				  D_PKG_SCHEDULE.ADD(pnD_INSERT_ID =&gt; :ScheduleId,
                                     pnLPU         =&gt; :pnlpu,
                                     pnCID         =&gt; :pncid,
                                     psCODE        =&gt; :pscode,
                                     psNAME        =&gt; :psname,
                                     pdSTART_DATE  =&gt; :pdstart_date,
                                     pnSCH_TYPE    =&gt; :sch_type,
                                     psHOLIDAYS    =&gt; sholidays,
                                     pnQUOTING     =&gt; :QUOTING,
                                     pnSCH_KIND    =&gt; 1);
			end;
		end;

		<component cmptype="ActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="pscode" get="scode" src="schedule_code" srctype="ctrl" />
		<component cmptype="ActionVar" name="psname" get="sname" src="schedule_name" srctype="ctrl" />
		<component cmptype="ActionVar" name="pdstart_date" get="dstart_date" src="start_date" srctype="ctrl" />
		<component cmptype="ActionVar" name="pncid" get="ncid" src="CatalogId" srctype="var" />
		<component cmptype="ActionVar" name="updScheduleId" get="getScheduleId" src="UpdScheduleId" srctype="var" />
		<component cmptype="ActionVar" name="ScheduleId" put="ScheduleId" src="ScheduleId" srctype="var" len="17" />
		<component cmptype="ActionVar" name="sch_type" get="sch_type" src="schedule_type" srctype="ctrl" />
		<component cmptype="ActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="ctrl" />
		<component cmptype="ActionVar" name="MON_HOLIDAY" get="MON_HOLIDAY" src="MON_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="TUE_HOLIDAY" get="TUE_HOLIDAY" src="TUE_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="WED_HOLIDAY" get="WED_HOLIDAY" src="WED_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="THU_HOLIDAY" get="THU_HOLIDAY" src="THU_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="FRI_HOLIDAY" get="FRI_HOLIDAY" src="FRI_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SAT_HOLIDAY" get="SAT_HOLIDAY" src="SAT_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SUN_HOLIDAY" get="SUN_HOLIDAY" src="SUN_HOLIDAY" srctype="ctrl" />
		
		<component cmptype="SubAction" name="ForUpdate_MON" groupname="week_for_mon">
			
			<component cmptype="SubActionVar" name="ScheduleId" get="ScheduleId" src="ScheduleId" srctype="parent" />

			<component cmptype="SubAction" name="UpdDay_MON" groupname="days_for_mon" type="upd">
				begin
			    	select sp.id
				      into :day_id
				      from d_v_schedulesp sp
				     where sp.pid = :pnsch_id
				       and sp.day_number = :day_old;

				       d_pkg_schedulesp.upd(pnid =&gt; :day_id,
				                         pnlpu =&gt; :pnlpu,
				                 		 pnday_number =&gt; :day_num,
				                 		 pdtime_begin =&gt; null,
				                    	 pdtime_end =&gt; null);
				end;

				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="day_old" get="day_old" src="DAY_NUM_FOR_MON_OLD" srctype="ctrl" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" src="DAY_NUM_FOR_MON" srctype="ctrl" />

				<component cmptype="SubAction" name="AddTimeType_MON" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_CHN" groupname="day_times" type="del">
						begin
					  		d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
					                                  pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_CHN" groupname="day_times" type="upd">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_CHN" groupname="day_times" type="add">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                               pnLPU         => :pnlpu,
						                               pnPID         => :pnpid,
						                               psTIME_BEGIN  => :pstime_begin,
						                               psTIME_END    => :pstime_end,
						                               pnTIME_TYPE   => :pntime_type,
						                               pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="insert_id" len="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="UpdTimeType_MON" groupname="time_types" type="upd">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_MON" groupname="day_times" type="del">
						begin
						  	d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
                                                      pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_MON" groupname="day_times" type="upd">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_MON" groupname="day_times" type="add">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
						                               pnLPU         => :pnlpu,
						                               pnPID         => :pnpid,
						                               psTIME_BEGIN  => :pstime_begin,
						                               psTIME_END    => :pstime_end,
						                               pnTIME_TYPE   => :pntime_type,
						                               pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="DelTimeType_MON" groupname="time_types" type="del">

					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="DelTime_MON1" groupname="day_times" type="del">
						begin
						  	d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
						                              pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>
				</component>
			</component>
			<component cmptype="SubAction" name="AddDay_MON" groupname="days_for_mon" type="add">
				begin
					if :day_num is not null then
						d_pkg_schedulesp.add(pnd_insert_id =&gt; :day_id,
                                                     pnlpu =&gt; :pnlpu,
                                                     pnpid =&gt; :pnsch_id,
                                              pnday_number =&gt; :day_num,
                                              pdtime_begin =&gt; null,
                                                pdtime_end =&gt; null);
				   else
						:day_id:=null;
				   end if;
				end;

				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="DAY_NUM_FOR_MON" srctype="ctrl" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />

				<component cmptype="SubAction" name="AddTimeType_MON" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="AddTime_CHN" groupname="day_times" type="add">
                        <![CDATA[
						begin
							if :pnpid is not null then
								D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
                                                             pnLPU         => :pnlpu,
                                                             pnPID         => :pnpid,
                                                             psTIME_BEGIN  => :pstime_begin,
                                                             psTIME_END    => :pstime_end,
                                                             pnTIME_TYPE   => :pntime_type,
                                                             pnGEN_ERROR   => 0,
                                                             pnSTEP        => :STEP,
                                                             pnLIMITS      => :LIMITS);
							end if;
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
			</component>
			<component cmptype="SubAction" name="DelDay_MON" groupname="days_for_mon" type="del">
				declare day_id NUMBER(17);
				begin
				 	select sp.id
				      into day_id
				      from d_v_schedulesp sp
				     where sp.pid = :pnsch_id
				       and sp.day_number = :day_num;
					d_pkg_schedulesp.del(day_id,:pnlpu);
				end;
				<component cmptype="SubActionVar" name="day_num" get="day_num" src="days_for_mon" srctype="ctrl" len="17" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />
			 </component>
			
			<component cmptype="SubAction" name="CheckSchedule_CHN" mode="execlast">
				begin
					d_pkg_schedule.check_interval(:sch_id, :lpu_id);
				end;
				<component cmptype="SubActionVar" name="sch_id" get="ScheduleId" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="lpu_id" get="lpu" src="LPU" srctype="session" />
			</component>
		</component>
	</component>
```

**Используемые пакеты/функции:** D_PKG_SCHEDULESP_TIMES.UPD_S

---

### Запрос №12

**Тип компонента:** M2 Action
**Имя компонента:** AddEditScheduleSk
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="AddEditScheduleSk" mode="post">
		declare
   			sholidays  varchar2(7);
  		begin
			sholidays := :MON_HOLIDAY||:TUE_HOLIDAY||:WED_HOLIDAY||:THU_HOLIDAY||:FRI_HOLIDAY||:SAT_HOLIDAY||:SUN_HOLIDAY;
			begin
				select sch.id
		          into :ScheduleId
			  	  from d_v_schedule sch
			 	 where sch.id = :updScheduleId;

			   	D_PKG_SCHEDULE.UPD(pnID         =&gt; :updScheduleId,
				  				   pnLPU        =&gt; :pnlpu,
				  				   psCODE       =&gt; :pscode,
				  				   psNAME       =&gt; :psname,
				  		           pdSTART_DATE =&gt; :pdstart_date,
                                   pnSCH_TYPE   =&gt; :sch_type,
                                   psHOLIDAYS   =&gt; sholidays,
                                   pnQUOTING    =&gt; :QUOTING);

				exception when NO_DATA_FOUND then
                  D_PKG_SCHEDULE.ADD(pnD_INSERT_ID =&gt; :ScheduleId,
                                     pnLPU         =&gt; :pnlpu,
                                     pnCID         =&gt; :pncid,
                                     psCODE        =&gt; :pscode,
                                     psNAME        =&gt; :psname,
                                     pdSTART_DATE  =&gt; :pdstart_date,
                                     pnSCH_TYPE    =&gt; :sch_type,
                                     psHOLIDAYS    =&gt; sholidays,
                                     pnQUOTING     =&gt; :QUOTING,
                                     pnSCH_KIND    =&gt; 1);
			end;
		end;

		<component cmptype="ActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="pscode" get="scode" src="schedule_code" srctype="ctrl" />
		<component cmptype="ActionVar" name="psname" get="sname" src="schedule_name" srctype="ctrl" />
		<component cmptype="ActionVar" name="pdstart_date" get="dstart_date" src="start_date" srctype="ctrl" />
		<component cmptype="ActionVar" name="pncid" get="ncid" src="CatalogId" srctype="var" />
		<component cmptype="ActionVar" name="updScheduleId" get="getScheduleId" src="UpdScheduleId" srctype="var" />
		<component cmptype="ActionVar" name="ScheduleId" put="ScheduleId" src="ScheduleId" srctype="var" len="17" />
		<component cmptype="ActionVar" name="sch_type" get="sch_type" src="schedule_type" srctype="ctrl" />
		<component cmptype="ActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="ctrl" />
		<component cmptype="ActionVar" name="MON_HOLIDAY" get="MON_HOLIDAY" src="MON_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="TUE_HOLIDAY" get="TUE_HOLIDAY" src="TUE_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="WED_HOLIDAY" get="WED_HOLIDAY" src="WED_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="THU_HOLIDAY" get="THU_HOLIDAY" src="THU_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="FRI_HOLIDAY" get="FRI_HOLIDAY" src="FRI_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SAT_HOLIDAY" get="SAT_HOLIDAY" src="SAT_HOLIDAY" srctype="ctrl" />
		<component cmptype="ActionVar" name="SUN_HOLIDAY" get="SUN_HOLIDAY" src="SUN_HOLIDAY" srctype="ctrl" />
		
		<component cmptype="SubAction" name="ForUpdate_SK" groupname="week_for_sk">
			
			<component cmptype="SubActionVar" name="ScheduleId" get="ScheduleId" src="ScheduleId" srctype="parent" />

			<component cmptype="SubAction" name="DelDay_SK" groupname="days_for_sk" type="del">
				begin
					d_pkg_schedulesp.del(:day_id,:pnlpu);
				end;
				<component cmptype="SubActionVar" name="day_id" get="day_id" src="days_for_sk" srctype="ctrl" len="17" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />
			 </component>
			<component cmptype="SubAction" name="UpdDay_SK" groupname="days_for_sk" type="upd">
				begin
				   d_pkg_schedulesp.upd(pnid =&gt; :day_id,
				                       pnlpu =&gt; :pnlpu,
				                pnday_number =&gt; :day_num,
				                pdtime_begin =&gt; null,
				                  pdtime_end =&gt; null);
				end;

				<component cmptype="SubActionVar" name="day_id" get="days_for_sk" src="days_for_sk" srctype="ctrl" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="DAY_NUM" srctype="ctrlcaption" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />

				<component cmptype="SubAction" name="AddTimeType_SK" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_SK" groupname="day_times" type="del">
						begin
					  		d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
					                                  pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_SK" groupname="day_times" type="upd">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_SK" groupname="day_times" type="add">
                        <![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
                                                       pnLPU         => :pnlpu,
                                                       pnPID         => :pnpid,
                                                       psTIME_BEGIN  => :pstime_begin,
                                                       psTIME_END    => :pstime_end,
                                                       pnTIME_TYPE   => :pntime_type,
                                                       pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="UpdTimeType_SK" groupname="time_types" type="upd">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="DelTime_MON" groupname="day_times" type="del">
						begin
						  	d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
													  pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>

					<component cmptype="SubAction" name="UpdTime_SK" groupname="day_times" type="upd">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.UPD_S(pnID         => :pnid,
						                               pnLPU        => :pnlpu,
						                               psTIME_BEGIN => :pstime_begin,
						                               psTIME_END   => :pstime_end,
						                               pnTIME_TYPE  => :pntime_type,
						                               pnGEN_ERROR  => 0,
                                                       vAPI_VERSION => 2,
                                                       pnSTEP       => :STEP,
                                                       pnLIMITS     => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
					</component>

					<component cmptype="SubAction" name="AddTime_SK" groupname="day_times" type="add">
						<![CDATA[
						begin
					  	  D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
                                                       pnLPU         => :pnlpu,
                                                       pnPID         => :pnpid,
                                                       psTIME_BEGIN  => :pstime_begin,
                                                       psTIME_END    => :pstime_end,
                                                       pnTIME_TYPE   => :pntime_type,
                                                       pnGEN_ERROR   => 0,
                                                       pnSTEP        => :STEP,
                                                       pnLIMITS      => :LIMITS);
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" en="17" />
					</component>
				</component>
				<component cmptype="SubAction" name="DelTimeType_SK" groupname="time_types" type="del">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />
					<component cmptype="SubActionVar" name="QUOTING" get="QUOTING" src="QUOTING" srctype="parent" />

					<component cmptype="SubAction" name="DelTime_SK1" groupname="day_times" type="del">
						begin
						  d_pkg_schedulesp_times.del(pnid =&gt; :pnid,
						                            pnlpu =&gt; :pnlpu);
						end;
						<component cmptype="SubActionVar" name="pnid" get="nid" src="day_times" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
					</component>
				</component>
			</component>
			<component cmptype="SubAction" name="AddDay_SK" groupname="days_for_sk" type="add">
				begin
					if :day_num is not null then
						d_pkg_schedulesp.add(pnd_insert_id =&gt; :day_id,
                                                     pnlpu =&gt; :pnlpu,
                                                     pnpid =&gt; :pnsch_id,
                                              pnday_number =&gt; :day_num,
                                              pdtime_begin =&gt; null,
                                                pdtime_end =&gt; null);
				   else
						:day_id := null;
				   end if;
				end;

				<component cmptype="SubActionVar" name="day_id" put="day_id" src="day_id" srctype="var" len="17" />
				<component cmptype="SubActionVar" name="day_num" get="day_num" put="day_num" src="DAY_NUM" srctype="ctrlcaption" />
				<component cmptype="SubActionVar" name="pnlpu" get="lpu" src="LPU" srctype="session" />
				<component cmptype="SubActionVar" name="pnsch_id" get="sch_id" src="ScheduleId" srctype="parent" />

				<component cmptype="SubAction" name="AddTimeType_SK" groupname="time_types" type="add">
					<component cmptype="SubActionVar" name="day_id" get="day_id" src="day_id" srctype="parent" />
					<component cmptype="SubActionVar" name="time_type" get="time_type" src="TimeTypeName" srctype="ctrl" />

					<component cmptype="SubAction" name="AddTime_SK" groupname="day_times" type="add">
						<![CDATA[
						begin
							if :pnpid is not null then
								D_PKG_SCHEDULESP_TIMES.ADD_S(pnD_INSERT_ID => :pnd_insert_id,
                                                             pnLPU         => :pnlpu,
                                                             pnPID         => :pnpid,
                                                             psTIME_BEGIN  => :pstime_begin,
                                                             psTIME_END    => :pstime_end,
                                                             pnTIME_TYPE   => :pntime_type,
                                                             pnGEN_ERROR   => 0,
                                                             pnSTEP        => :STEP,
                                                             pnLIMITS      => :LIMITS);
							end if;
						end;
                        ]]>
						<component cmptype="SubActionVar" name="pnlpu" src="LPU" srctype="session" />
						<component cmptype="SubActionVar" name="pnpid" src="day_id" srctype="parent" />
						<component cmptype="SubActionVar" name="pstime_begin" src="TimeBegin" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pstime_end" src="TimeEnd" srctype="ctrl" />
                        <component cmptype="SubActionVar" name="STEP" src="eStep" srctype="ctrl" />
						<component cmptype="SubActionVar" name="LIMITS" src="eLimits" srctype="ctrl" />
						<component cmptype="SubActionVar" name="pntime_type" src="time_type" srctype="parent" />
                        <component cmptype="SubActionVar" name="pnd_insert_id" src="TimeInsertId" srctype="var" put="" len="17" />
					</component>
				</component>
			</component>

			
			<component cmptype="SubAction" name="CheckSchedule_CHN" mode="execlast">
				begin
					d_pkg_schedule.check_interval(:sch_id, :lpu_id);
				end;
				<component cmptype="SubActionVar" name="sch_id" get="ScheduleId" src="ScheduleId" srctype="parent" />
				<component cmptype="SubActionVar" name="lpu_id" get="lpu" src="LPU" srctype="session" />
			</component>
		</component>
	</component>
```

**Используемые пакеты/функции:** D_PKG_SCHEDULESP_TIMES.UPD_S

---

### Запрос №13

**Тип компонента:** M2 Action
**Имя компонента:** checkForWarning
**Источник:** Forms/Schedules/schedules_edit_hp.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\Schedules\schedules_edit_hp.frm

**SQL код:**

```xml
<component cmptype="Action" name="checkForWarning">
        <![CDATA[
        begin
            select case
                       when exists(
                               select null
                                 from D_V_CLSCHS_BASE cb
                                      join D_V_DIRECTION_SERVICES_BASE ds on ds.CABLAB_TO = cb.CABLAB
                                                                         and ds.REC_DATE >= cb.DBEGIN
                                                                         and (ds.REC_DATE <= cb.DEND or cb.DEND is null)
                                                                         and (cb.EMPLOYER is null or cb.EMPLOYER = ds.EMPLOYER_TO)
                                                                         and (cb.SERVICE is null or cb.SERVICE = ds.SERVICE)
                                where cb.SCHEDULE = :SCHEDULE_ID) then 1
                       else 0
                       end SERVICES_COUNT
              into :SERVICES_COUNT
              from dual;
        end;
        ]]>
        <component cmptype="ActionVar" name="SCHEDULE_ID" src="ScheduleId" srctype="var" get="sch_id" />
        <component cmptype="ActionVar" name="SERVICES_COUNT" src="SERVICES_COUNT" srctype="var" put="pSERVICES_COUNT" len="17" />
    </component>
```

**Используемые таблицы/вьюхи:** D_V_CLSCHS_BASE, D_V_DIRECTION_SERVICES_BASE


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 5

---

### Вьюха №1: D_V_SCHEDULESP

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SCHEDULESP
 SELECT id,
    lpu,
    cid,
    pid,
    day_number,
    to_char(time_begin, 'hh24:mi'::text) AS time_begin,
    to_char(time_end, 'hh24:mi'::text) AS time_end
   FROM d_schedulesp sp
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = sp.cid AND ur.unitcode::text = 'SCHEDULESP'::text
         LIMIT 1));
```

---

### Вьюха №2: D_V_SCHEDULESP_TIMES

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SCHEDULESP_TIMES
 SELECT t.id,
    t.lpu,
    t.cid,
    t.pid,
    t.time_begin,
    t.time_end,
    t.time_type,
    t1.time_code,
    t1.time_name,
    to_char(t.time_begin, 'hh24:mi'::text) AS time_begin_s,
    to_char(t.time_end, 'hh24:mi'::text) AS time_end_s,
    t1.er_view,
    t.step,
    t.limits
   FROM d_schedulesp_times t
     LEFT JOIN d_schedule_time_types t1 ON t1.id = t.time_type
  WHERE true = true AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'SCHEDULESP_TIMES'::text
         LIMIT 1));
```

---

### Вьюха №3: D_V_SCHEDULE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SCHEDULE
 SELECT id,
    lpu,
    cid,
    code,
    name,
    start_date,
    sch_type,
        CASE
            WHEN sch_type = 0::numeric THEN 'Обычный'::character varying
            WHEN sch_type = 1::numeric THEN 'Чет/нечет'::character varying
            WHEN sch_type = 2::numeric THEN 'Чет/нечет по дням недели'::character varying
            WHEN sch_type = 3::numeric THEN 'Скользящий'::character varying
            WHEN sch_type = 4::numeric THEN 'По дням месяца'::character varying
            ELSE ''::character varying
        END AS sch_type_mnemo,
    quoting,
    holidays,
        CASE
            WHEN substr2(holidays, 1::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 1::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 1::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 1::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 1::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 1::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS mon_holiday,
        CASE
            WHEN substr2(holidays, 2::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 2::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 2::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 2::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 2::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 2::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS tue_holiday,
        CASE
            WHEN substr2(holidays, 3::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 3::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 3::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 3::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 3::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 3::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS wed_holiday,
        CASE
            WHEN substr2(holidays, 4::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 4::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 4::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 4::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 4::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 4::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS thu_holiday,
        CASE
            WHEN substr2(holidays, 5::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 5::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 5::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 5::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 5::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 5::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS fri_holiday,
        CASE
            WHEN substr2(holidays, 6::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 6::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 6::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 6::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 6::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 6::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS sat_holiday,
        CASE
            WHEN substr2(holidays, 7::numeric, 1::numeric) = NULL::text OR check_null(substr2(holidays, 7::numeric, 1::numeric)::character varying, NULL::character varying) THEN 0
            WHEN substr2(holidays, 7::numeric, 1::numeric) = 0::numeric OR check_null(substr2(holidays, 7::numeric, 1::numeric)::character varying, 0::character varying) THEN 0
            WHEN substr2(holidays, 7::numeric, 1::numeric) = 1::numeric OR check_null(substr2(holidays, 7::numeric, 1::numeric)::character varying, 1::character varying) THEN 1
            ELSE 0
        END AS sun_holiday,
    sch_kind,
        CASE
            WHEN sch_kind = 1::numeric THEN 'Для госпитализации'::character varying
            WHEN sch_kind = 2::numeric THEN 'Маршрутизация'::character varying
            ELSE 'Обычный'::character varying
        END AS sch_kind_mnemo
   FROM d_schedule d
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = d.cid AND ur.unitcode::text = 'SCHEDULE'::text
         LIMIT 1));
```

---

### Вьюха №4: D_V_CLSCHS_BASE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CLSCHS_BASE
 SELECT id,
    lpu,
    cablab,
    employer,
    service,
    schedule,
    dbegin,
    dend,
    rtime_prim,
    rtime_sec,
    rcount,
    rcountmax,
    rcount_cito,
    time_or_count,
    use_cito,
    use_direction,
    schedule_type,
    use_work_calendar,
    clsch_type,
    is_subst,
    is_strictordr,
    sch_start_date,
    sch_resource,
    export_yo,
    profil_yo_id,
    purpose
   FROM d_clschs t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'CLSCHS'::text
         LIMIT 1));
```

---

### Вьюха №5: D_V_DIRECTION_SERVICES_BASE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_DIRECTION_SERVICES_BASE
 SELECT id,
    lpu,
    pid,
    hid,
    rpid,
    is_combined_payment,
    is_necessary,
    service,
    employer_to,
    cablab_to,
    rec_date,
    visit_purpose,
    ref_kind,
    visit_kind,
    diseasecase,
    reg_type,
    serv_status,
    is_primary,
    s_commnet,
    hh_dep,
        CASE
            WHEN serv_status <> 1::numeric THEN 'Записать'::character varying
            ELSE NULL::character varying
        END AS hyperlink,
    rec_type,
    ser_count,
    time_type AS time_type_id,
    irid,
    payment_kind AS payment_kind_id,
    serv_status_reason,
    dc_diagnosis,
    lpu_service AS lpu_service_id,
    rec_duration,
    ticket_n,
    ticket_s,
    rqs_limit,
    ex_system,
    purchase_order,
    is_confirmed,
    confirm_date,
    nurse_user_templates,
    employer_cancel,
    conference,
    serv_desc,
    localization,
    to_char(rec_date, 'HH24:MI'::text) AS rec_time,
    complid,
    compstr,
    important,
    patient,
    guid,
    attendance_state
   FROM d_direction_services t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'DIRECTION_SERVICES'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 5

---

### Вьюха №1: D_V_SCHEDULESP

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SCHEDULESP
select -- Представление для раздела : Графики : состав
       sp.ID,
       sp.LPU,
       sp.CID,
       sp.PID,
       sp.DAY_NUMBER,
       to_char(sp.TIME_BEGIN,'hh24:mi')      TIME_BEGIN,
       to_char(sp.TIME_END,'hh24:mi')        TIME_END
   from D_SCHEDULESP sp   -- Графики : состав
  where exists (select null
                  from D_V_URPRIVS ur
                 where ur.CATALOG = sp.CID
                   and ur.UNITCODE = 'SCHEDULESP'
                   and rownum = 1)
```

---

### Вьюха №2: D_V_SCHEDULESP_TIMES

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SCHEDULESP_TIMES
select --Представление для раздела: Графики : состав дня по временам
       t.ID,
       t.LPU,
       t.CID,
       t.PID,
       t.TIME_BEGIN,
       t.TIME_END,
       t.TIME_TYPE,
       t1.TIME_CODE,
       t1.TIME_NAME,
       to_char(t.TIME_BEGIN,'hh24:mi')      TIME_BEGIN_S,
       to_char(t.TIME_END,'hh24:mi')        TIME_END_S,
       t1.ER_VIEW,
       t.STEP,
       t.LIMITS
  from D_SCHEDULESP_TIMES t,  --Графики : состав дня по временам
       D_SCHEDULE_TIME_TYPES t1
  where t1.ID(+) = t.TIME_TYPE
    and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'SCHEDULESP_TIMES' and rownum = 1)
```

---

### Вьюха №3: D_V_SCHEDULE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SCHEDULE
select --Представление для раздела: Графики
       d.ID,
       d.LPU,
       d.CID,
       d.CODE,
       d.NAME,
       d.START_DATE,
       d.SCH_TYPE,
       case when d.SCH_TYPE = 0 then 'Обычный'
            when d.SCH_TYPE = 1 then 'Чет/нечет'
            when d.SCH_TYPE = 2 then 'Чет/нечет по дням недели'
            when d.SCH_TYPE = 3 then 'Скользящий'
            when d.SCH_TYPE = 4 then 'По дням месяца'
       else '' end SCH_TYPE_MNEMO,
       d.QUOTING,
       d.HOLIDAYS,
       decode(substr(d.HOLIDAYS,1,1),null,0,0,0,1,1,0) MON_HOLIDAY,
       decode(substr(d.HOLIDAYS,2,1),null,0,0,0,1,1,0) TUE_HOLIDAY,
       decode(substr(d.HOLIDAYS,3,1),null,0,0,0,1,1,0) WED_HOLIDAY,
       decode(substr(d.HOLIDAYS,4,1),null,0,0,0,1,1,0) THU_HOLIDAY,
       decode(substr(d.HOLIDAYS,5,1),null,0,0,0,1,1,0) FRI_HOLIDAY,
       decode(substr(d.HOLIDAYS,6,1),null,0,0,0,1,1,0) SAT_HOLIDAY,
       decode(substr(d.HOLIDAYS,7,1),null,0,0,0,1,1,0) SUN_HOLIDAY,
       d.SCH_KIND,
       case when d.SCH_KIND = 1 then 'Для госпитализации'
            when d.SCH_KIND = 2 then 'Маршрутизация'
            else 'Обычный'
       end SCH_KIND_MNEMO
  from D_SCHEDULE d   --Графики
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = d.CID
                  and ur.UNITCODE = 'SCHEDULE'
                  and rownum = 1)
```

---

### Вьюха №4: D_V_CLSCHS_BASE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CLSCHS_BASE
select -- Представление для раздела : Назначенные графики
       t.ID,
       t.LPU,
       t.CABLAB,
       t.EMPLOYER,
       t.SERVICE,
       t.SCHEDULE,
       t.DBEGIN,
       t.DEND,
       t.RTIME_PRIM,
       t.RTIME_SEC,
       t.RCOUNT,
       t.RCOUNTMAX,
       t.RCOUNT_CITO,
       t.TIME_OR_COUNT,
       t.USE_CITO,
       t.USE_DIRECTION,
       t.SCHEDULE_TYPE,
       t.USE_WORK_CALENDAR,
       t.CLSCH_TYPE,
       t.IS_SUBST,
       t.IS_STRICTORDR,
       t.SCH_START_DATE,
       t.SCH_RESOURCE,
       t.EXPORT_YO,
       t.PROFIL_YO_ID,
       t.PURPOSE
  from D_CLSCHS       t
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'CLSCHS' and rownum = 1)
```

---

### Вьюха №5: D_V_DIRECTION_SERVICES_BASE

**Используется в формах:**
- Forms/Schedules/schedules_edit_hp.frm

**DDL определение:**

```sql
-- Oracle View: D_V_DIRECTION_SERVICES_BASE
select --Представление для раздела : Направления : услуги (базовое)
       t.ID,
       t.LPU,
       t.PID,
       t.HID,
       t.RPID,
       t.IS_COMBINED_PAYMENT,
       t.IS_NECESSARY,
       t.SERVICE,
       t.EMPLOYER_TO,
       t.CABLAB_TO,
       t.REC_DATE,
       t.VISIT_PURPOSE,
       t.REF_KIND,
       t.VISIT_KIND,
       t.DISEASECASE,
       t.REG_TYPE,
       t.SERV_STATUS,
       t.IS_PRIMARY,
       t.S_COMMNET,
       t.HH_DEP,
       case when t.SERV_STATUS <> 1 then 'Записать' end HYPERLINK,
       t.REC_TYPE,
       t.SER_COUNT,
       t.TIME_TYPE TIME_TYPE_ID,
       t.IRID,
       t.PAYMENT_KIND PAYMENT_KIND_ID,
       t.SERV_STATUS_REASON,
       t.DC_DIAGNOSIS,
       t.LPU_SERVICE LPU_SERVICE_ID,
       t.REC_DURATION,
       t.TICKET_N,
       t.TICKET_S,
       t.RQS_LIMIT,
       t.EX_SYSTEM,
       t.PURCHASE_ORDER,
       t.IS_CONFIRMED,
       t.CONFIRM_DATE,
       t.NURSE_USER_TEMPLATES,
       t.EMPLOYER_CANCEL,
       t.CONFERENCE,
       t.SERV_DESC,
       t.LOCALIZATION,
       to_char(t.REC_DATE, 'HH24:MI') REC_TIME,
       t.COMPLID,
       t.COMPSTR,
       t.IMPORTANT,
       t.PATIENT,
       t.GUID,
       t.ATTENDANCE_STATE
  from D_DIRECTION_SERVICES t    --Направления : услуги
 where exists(select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'DIRECTION_SERVICES' and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 5
- Всего уникальных таблиц: 6

### Связь вьюх и таблиц

**D_V_SCHEDULESP** использует таблицы:
- D_SCHEDULESP

**D_V_SCHEDULESP_TIMES** использует таблицы:
- D_SCHEDULESP_TIMES
- D_SCHEDULE_TIME_TYPES

**D_V_SCHEDULE** использует таблицы:
- D_SCHEDULE

**D_V_CLSCHS_BASE** использует таблицы:
- D_CLSCHS

**D_V_DIRECTION_SERVICES_BASE** использует таблицы:
- D_DIRECTION_SERVICES

### DDL определения таблиц

---

#### Таблица №1: D_SCHEDULESP

```sql
CREATE TABLE D_SCHEDULESP (
    id bigint,
    pid bigint,
    day_number bigint,
    time_begin timestamp without time zone,
    time_end timestamp without time zone,
    cid bigint,
    lpu bigint
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULESP.id IS 'ID';
COMMENT ON COLUMN D_SCHEDULESP.pid IS 'График';
COMMENT ON COLUMN D_SCHEDULESP.day_number IS 'Номер дня в графике';
COMMENT ON COLUMN D_SCHEDULESP.time_begin IS 'Время начала дня';
COMMENT ON COLUMN D_SCHEDULESP.time_end IS 'Время окончания дня';
COMMENT ON COLUMN D_SCHEDULESP.cid IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULESP.lpu IS 'ЛПУ';

COMMENT ON TABLE D_SCHEDULESP IS 'Графики : состав';
```

---

#### Таблица №2: D_SCHEDULESP_TIMES

```sql
CREATE TABLE D_SCHEDULESP_TIMES (
    id bigint,
    pid bigint,
    time_begin timestamp without time zone,
    time_end timestamp without time zone,
    time_type bigint,
    cid bigint,
    lpu bigint,
    step numeric(4),
    limits numeric(3)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULESP_TIMES.id IS 'ID';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.pid IS 'Состав графика';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.time_begin IS 'Начало';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.time_end IS 'Окончание';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.time_type IS 'Тип интервала';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.cid IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.step IS 'Шаг графика (для госпитализации)';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.limits IS 'Количество записываемых человек (для госпитализации)';

COMMENT ON TABLE D_SCHEDULESP_TIMES IS 'Графики : состав дня по временам';
```

---

#### Таблица №3: D_SCHEDULE_TIME_TYPES

```sql
CREATE TABLE D_SCHEDULE_TIME_TYPES (
    id bigint,
    time_code numeric(3),
    time_name character varying(250),
    version bigint,
    time_color bigint,
    er_view numeric DEFAULT 0,
    use_in_waitlist numeric(1) DEFAULT 0,
    infoboard_name character varying(250),
    is_primary numeric(1),
    fer_view numeric(1) DEFAULT 0,
    ker_view numeric(1) DEFAULT 0,
    other_view numeric(1) DEFAULT 0
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.id IS 'ID';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.time_code IS 'Тип интервала';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.time_name IS 'Содержание интервала';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.version IS 'Версия';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.time_color IS 'Цвет интервала';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.er_view IS 'Выгружать интервал в ЕР: 1 - да, 0-  нет';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.use_in_waitlist IS 'Признак использования интервала для записи пациентов из очереди ожидания: 1 – да, 0 – нет';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.infoboard_name IS 'Наименование интервала для вывода на инфотабло';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.is_primary IS 'Признак первичности: 1 - первичный, 2 - повторный, null - не определен';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.fer_view IS 'Выгружать интервал в ФЭР (Нетрика)';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.ker_view IS 'Выгружать интервал в КЭР (Нетрика)';
COMMENT ON COLUMN D_SCHEDULE_TIME_TYPES.other_view IS 'Выгружать интервал в другие источники (Нетрика)';

COMMENT ON TABLE D_SCHEDULE_TIME_TYPES IS 'Словарь типов интервалов графиков';
```

---

#### Таблица №4: D_SCHEDULE

```sql
CREATE TABLE D_SCHEDULE (
    id bigint,
    code character varying(60),
    name character varying(256),
    start_date timestamp without time zone,
    lpu bigint,
    cid bigint,
    sch_type numeric(1),
    holidays character varying(7),
    quoting numeric(1) DEFAULT 0,
    sch_kind numeric(1)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULE.id IS 'ID';
COMMENT ON COLUMN D_SCHEDULE.code IS 'Код';
COMMENT ON COLUMN D_SCHEDULE.name IS 'Наименование';
COMMENT ON COLUMN D_SCHEDULE.start_date IS 'Дата отсчета';
COMMENT ON COLUMN D_SCHEDULE.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_SCHEDULE.cid IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULE.sch_type IS 'Тип графика:0-обычный,1-чет/нечет,2-чет/нечет по дням недели,3-скользящий,4-по дням месяца';
COMMENT ON COLUMN D_SCHEDULE.holidays IS 'Выходные дни';
COMMENT ON COLUMN D_SCHEDULE.quoting IS 'Для квотирования: 0-нет,1-да';
COMMENT ON COLUMN D_SCHEDULE.sch_kind IS 'Вид графика: NULL - обычный, 1 - для госпитализации, 2 - маршрутизация';

COMMENT ON TABLE D_SCHEDULE IS 'Графики';
```

---

#### Таблица №5: D_CLSCHS

```sql
CREATE TABLE D_CLSCHS (
    id bigint,
    lpu bigint,
    cablab bigint,
    employer bigint,
    service bigint,
    schedule bigint,
    dbegin timestamp without time zone,
    dend timestamp without time zone,
    rtime_sec bigint,
    rcount bigint,
    rcountmax bigint,
    time_or_count numeric(1) DEFAULT 0,
    use_cito numeric(1) DEFAULT 0,
    use_direction numeric(1) DEFAULT 0,
    schedule_type numeric(1) DEFAULT 0,
    use_work_calendar numeric(1) DEFAULT 1,
    rcount_cito bigint,
    clsch_type numeric(1) DEFAULT 0,
    is_subst numeric(1) DEFAULT 0,
    is_strictordr numeric(1),
    sch_start_date timestamp without time zone,
    sch_resource bigint,
    export_yo numeric(1) DEFAULT 0,
    profil_yo_id bigint,
    purpose numeric(1) DEFAULT 0,
    rtime_prim numeric(5)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_CLSCHS.id IS 'ID';
COMMENT ON COLUMN D_CLSCHS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_CLSCHS.cablab IS 'Кабинет';
COMMENT ON COLUMN D_CLSCHS.employer IS 'Сотрудник';
COMMENT ON COLUMN D_CLSCHS.service IS 'Услуга';
COMMENT ON COLUMN D_CLSCHS.schedule IS 'График';
COMMENT ON COLUMN D_CLSCHS.dbegin IS 'Дата начала действия';
COMMENT ON COLUMN D_CLSCHS.dend IS 'Дата окончания';
COMMENT ON COLUMN D_CLSCHS.rtime_sec IS 'Время повторного оказания';
COMMENT ON COLUMN D_CLSCHS.rcount IS 'Количество на время оказания';
COMMENT ON COLUMN D_CLSCHS.rcountmax IS 'Максимальное количество на день';
COMMENT ON COLUMN D_CLSCHS.time_or_count IS 'Тип формирования графика. 0 - по времени оказания, 1 - по количеству, 2 - по количеству с ограничением';
COMMENT ON COLUMN D_CLSCHS.use_cito IS 'Признак возможности записи срочников. 0 - Запрещено, 1 - Разрешено в рабочие дни, 2 - Разрешено при наличии интервала для записи.';
COMMENT ON COLUMN D_CLSCHS.use_direction IS 'Признак возможности создания направлений (назначений) : 0 - нет; 1 - да';
COMMENT ON COLUMN D_CLSCHS.schedule_type IS 'Тип графика: 0 - основной, 1 - по кабинету';
COMMENT ON COLUMN D_CLSCHS.use_work_calendar IS 'Учитывать календарь рабочих\выходных дней:1-да,0-нет';
COMMENT ON COLUMN D_CLSCHS.rcount_cito IS 'Максимальное количество срочников в день';
COMMENT ON COLUMN D_CLSCHS.clsch_type IS 'Тип построения: 0 - обычный, 1 - динамический, 2 - живая очередь';
COMMENT ON COLUMN D_CLSCHS.is_subst IS 'Тип влияния на другие графики: 0 - обычный, 1 - замещение, 2 - дополнение';
COMMENT ON COLUMN D_CLSCHS.is_strictordr IS 'График строгой очередности: null - нет, 1 - да';
COMMENT ON COLUMN D_CLSCHS.sch_start_date IS 'Дата отсчета периода графика (если требуется отличная от настроенной в графике)';
COMMENT ON COLUMN D_CLSCHS.sch_resource IS 'Ресурс расписания';
COMMENT ON COLUMN D_CLSCHS.export_yo IS 'Передавать в сервис УО (1- да, 0 - нет)';
COMMENT ON COLUMN D_CLSCHS.profil_yo_id IS 'ID профиля услуги УО';
COMMENT ON COLUMN D_CLSCHS.purpose IS 'Целевое назначение графика расписания: 0 - для регистратуры, 1 - для лаборатории, 2 - для опер.блока';
COMMENT ON COLUMN D_CLSCHS.rtime_prim IS 'Длительность';

COMMENT ON TABLE D_CLSCHS IS 'Назначенные графики';
```

---

#### Таблица №6: D_DIRECTION_SERVICES

```sql
CREATE TABLE D_DIRECTION_SERVICES (
    id bigint,
    lpu bigint,
    pid bigint,
    hid bigint,
    is_combined_payment numeric(1),
    is_necessary numeric(1),
    service bigint,
    employer_to bigint,
    cablab_to bigint,
    rec_date timestamp without time zone,
    visit_purpose bigint,
    ref_kind bigint,
    visit_kind bigint,
    diseasecase bigint,
    reg_type numeric(1),
    serv_status numeric(1),
    is_primary numeric(1),
    s_commnet character varying(1200),
    hh_dep bigint,
    rec_type numeric(1),
    ser_count numeric(21,2),
    time_type bigint,
    rpid bigint,
    irid bigint,
    payment_kind bigint,
    serv_status_reason character varying(250),
    quota_q bigint,
    uk_hash character varying(75),
    dc_diagnosis bigint,
    lpu_service bigint,
    rec_duration numeric(5),
    ticket_n numeric(5),
    ticket_s character varying(75),
    rqs_limit bigint,
    ex_system bigint,
    purchase_order bigint,
    is_confirmed numeric(1),
    nurse_user_templates bigint,
    confirm_date timestamp without time zone,
    conference_type numeric(1),
    conference character varying(200),
    employer_cancel bigint,
    localization bigint,
    serv_desc character varying(2000),
    complid bigint,
    compstr bigint,
    important numeric(1) DEFAULT 0,
    patient bigint,
    guid character varying(36),
    attendance_state numeric(1) DEFAULT 0
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DIRECTION_SERVICES.id IS 'ID';
COMMENT ON COLUMN D_DIRECTION_SERVICES.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_DIRECTION_SERVICES.pid IS 'Направления';
COMMENT ON COLUMN D_DIRECTION_SERVICES.hid IS 'Иерархия';
COMMENT ON COLUMN D_DIRECTION_SERVICES.is_combined_payment IS 'Используется ли комбинированная оплата : 0 - нет; 1 - да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.is_necessary IS 'Обязательна ли услуга для закрытия направления : 0-нет ; 1-да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.service IS 'Услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.employer_to IS 'Врач, которому назначена услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.cablab_to IS 'Кабинет, которому назначена услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.rec_date IS 'Время назначения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.visit_purpose IS 'Цель посещения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ref_kind IS 'Вид обращения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.visit_kind IS 'Вид посещения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.diseasecase IS 'Случай заболевания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.reg_type IS 'Тип регистрации';
COMMENT ON COLUMN D_DIRECTION_SERVICES.serv_status IS 'Статус услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.is_primary IS 'Услуга первичная : 0- нет; 1- да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.s_commnet IS 'Комментарий';
COMMENT ON COLUMN D_DIRECTION_SERVICES.hh_dep IS 'Истоия болезни : отделения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.rec_type IS 'Тип регистратуры : 0 - врачей, 1 - услуг';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ser_count IS 'Кратность';
COMMENT ON COLUMN D_DIRECTION_SERVICES.time_type IS 'Тип интевала, на который произведена запись';
COMMENT ON COLUMN D_DIRECTION_SERVICES.rpid IS 'Направление на услугу, явл. точкой отсчета';
COMMENT ON COLUMN D_DIRECTION_SERVICES.irid IS 'Направление на услугу, инициирующее внесение результата';
COMMENT ON COLUMN D_DIRECTION_SERVICES.payment_kind IS 'Вид оплаты';
COMMENT ON COLUMN D_DIRECTION_SERVICES.serv_status_reason IS 'Причина смены статуса услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.quota_q IS 'Ограничение по квоте';
COMMENT ON COLUMN D_DIRECTION_SERVICES.uk_hash IS 'Уникальный ключ записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.dc_diagnosis IS 'Диагноз случая заболевания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.lpu_service IS 'Услуга ЛПУ';
COMMENT ON COLUMN D_DIRECTION_SERVICES.rec_duration IS 'Длительность оказания в минутах';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ticket_n IS 'Номер квитка расписания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ticket_s IS 'Номер,описание квитка расписания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.rqs_limit IS 'Ресурс квоты записи в расписание';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ex_system IS 'Источник записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.purchase_order IS 'Заказ-наряд';
COMMENT ON COLUMN D_DIRECTION_SERVICES.is_confirmed IS 'Признак подтверждения записи на прием пациентом: 0 - не подтверждена, 1 - подтверждена, 2 - запись отменена, 3 - запись перенесена, 4 - явка, 5 - не дозвонились';
COMMENT ON COLUMN D_DIRECTION_SERVICES.nurse_user_templates IS 'Шаблон медсестры';
COMMENT ON COLUMN D_DIRECTION_SERVICES.confirm_date IS 'Дата подтверждения записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.conference_type IS 'Выбор из дополнительного словаря - TM_TYPE - Тип консультации';
COMMENT ON COLUMN D_DIRECTION_SERVICES.conference IS 'Консультация';
COMMENT ON COLUMN D_DIRECTION_SERVICES.employer_cancel IS 'Врач, отменивший направление';
COMMENT ON COLUMN D_DIRECTION_SERVICES.localization IS 'Группы локализаций';
COMMENT ON COLUMN D_DIRECTION_SERVICES.serv_desc IS 'Описание услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.complid IS 'Ссылка на запись головной комплексной услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.compstr IS 'Ссылка на настройку состава комплексной услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.important IS 'Важное: 1 - да, 0 - нет';
COMMENT ON COLUMN D_DIRECTION_SERVICES.patient IS 'Пациент';
COMMENT ON COLUMN D_DIRECTION_SERVICES.guid IS 'GUID';
COMMENT ON COLUMN D_DIRECTION_SERVICES.attendance_state IS 'Cостояние посещаемости: 0 – Не определено, 1 – Явка, 2 – Неявка';

COMMENT ON TABLE D_DIRECTION_SERVICES IS 'Направления : услуги';
```


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 5
- Всего уникальных таблиц: 5

### Связь вьюх и таблиц

**D_V_SCHEDULESP** использует таблицы:
- D_SCHEDULESP

**D_V_SCHEDULESP_TIMES** использует таблицы:
- D_SCHEDULESP_TIMES

**D_V_SCHEDULE** использует таблицы:
- D_SCHEDULE

**D_V_CLSCHS_BASE** использует таблицы:
- D_CLSCHS

**D_V_DIRECTION_SERVICES_BASE** использует таблицы:
- D_DIRECTION_SERVICES

### DDL определения таблиц

---

#### Таблица №1: D_SCHEDULESP

```sql
CREATE TABLE D_SCHEDULESP (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    DAY_NUMBER NUMBER(17) NOT NULL,
    TIME_BEGIN DATE,
    TIME_END DATE,
    CID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_SCHEDULESP PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULESP.PID IS 'График';
COMMENT ON COLUMN D_SCHEDULESP.DAY_NUMBER IS 'Номер дня в графике';
COMMENT ON COLUMN D_SCHEDULESP.TIME_BEGIN IS 'Время начала дня';
COMMENT ON COLUMN D_SCHEDULESP.TIME_END IS 'Время окончания дня';
COMMENT ON COLUMN D_SCHEDULESP.CID IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULESP.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_SCHEDULESP.ID IS 'ID';

COMMENT ON TABLE D_SCHEDULESP IS 'Графики : состав';
```

---

#### Таблица №2: D_SCHEDULESP_TIMES

```sql
CREATE TABLE D_SCHEDULESP_TIMES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    TIME_BEGIN DATE NOT NULL,
    TIME_END DATE NOT NULL,
    TIME_TYPE NUMBER(17),
    CID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    STEP NUMBER(4),
    LIMITS NUMBER(3),
    CONSTRAINT PK_D_SCHEDULESP_TIMES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULESP_TIMES.ID IS 'ID';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.PID IS 'Состав графика';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.TIME_BEGIN IS 'Начало';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.TIME_END IS 'Окончание';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.TIME_TYPE IS 'Тип интервала';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.CID IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.STEP IS 'Шаг графика (для госпитализации)';
COMMENT ON COLUMN D_SCHEDULESP_TIMES.LIMITS IS 'Количество записываемых человек (для госпитализации)';

COMMENT ON TABLE D_SCHEDULESP_TIMES IS 'Графики : состав дня по временам';
```

---

#### Таблица №3: D_SCHEDULE

```sql
CREATE TABLE D_SCHEDULE (
    ID NUMBER(17) NOT NULL,
    CODE VARCHAR2(60) NOT NULL,
    NAME VARCHAR2(256) NOT NULL,
    START_DATE DATE,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    SCH_TYPE NUMBER(1),
    HOLIDAYS VARCHAR2(7),
    QUOTING NUMBER(1) NOT NULL,
    SCH_KIND NUMBER(1),
    CONSTRAINT PK_D_SCHEDULE PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_SCHEDULE.ID IS 'ID';
COMMENT ON COLUMN D_SCHEDULE.CODE IS 'Код';
COMMENT ON COLUMN D_SCHEDULE.NAME IS 'Наименование';
COMMENT ON COLUMN D_SCHEDULE.START_DATE IS 'Дата отсчета';
COMMENT ON COLUMN D_SCHEDULE.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_SCHEDULE.CID IS 'Каталог';
COMMENT ON COLUMN D_SCHEDULE.SCH_TYPE IS 'Тип графика:0-обычный,1-чет/нечет,2-чет/нечет по дням недели,3-скользящий,4-по дням месяца';
COMMENT ON COLUMN D_SCHEDULE.HOLIDAYS IS 'Выходные дни';
COMMENT ON COLUMN D_SCHEDULE.QUOTING IS 'Для квотирования: 0-нет,1-да';
COMMENT ON COLUMN D_SCHEDULE.SCH_KIND IS 'Вид графика: NULL - обычный, 1 - для госпитализации, 2 - маршрутизация';

COMMENT ON TABLE D_SCHEDULE IS 'Графики';
```

---

#### Таблица №4: D_CLSCHS

```sql
CREATE TABLE D_CLSCHS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CABLAB NUMBER(17) NOT NULL,
    EMPLOYER NUMBER(17),
    SERVICE NUMBER(17),
    SCHEDULE NUMBER(17) NOT NULL,
    DBEGIN DATE NOT NULL,
    DEND DATE,
    RTIME_SEC NUMBER(17),
    RCOUNT NUMBER(17),
    RCOUNTMAX NUMBER(17),
    TIME_OR_COUNT NUMBER(1),
    USE_CITO NUMBER(1) NOT NULL,
    USE_DIRECTION NUMBER(1) NOT NULL,
    SCHEDULE_TYPE NUMBER(1) NOT NULL,
    USE_WORK_CALENDAR NUMBER(1) NOT NULL,
    RCOUNT_CITO NUMBER(17),
    CLSCH_TYPE NUMBER(1) NOT NULL,
    IS_SUBST NUMBER(1) NOT NULL,
    IS_STRICTORDR NUMBER(1),
    SCH_START_DATE DATE,
    SCH_RESOURCE NUMBER(17) NOT NULL,
    EXPORT_YO NUMBER(1),
    PROFIL_YO_ID NUMBER(17),
    PURPOSE NUMBER(1) NOT NULL,
    RTIME_PRIM NUMBER(5),
    CONSTRAINT PK_D_CLSCHS PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_CLSCHS.ID IS 'ID';
COMMENT ON COLUMN D_CLSCHS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_CLSCHS.CABLAB IS 'Кабинет';
COMMENT ON COLUMN D_CLSCHS.EMPLOYER IS 'Сотрудник';
COMMENT ON COLUMN D_CLSCHS.SERVICE IS 'Услуга';
COMMENT ON COLUMN D_CLSCHS.SCHEDULE IS 'График';
COMMENT ON COLUMN D_CLSCHS.DBEGIN IS 'Дата начала действия';
COMMENT ON COLUMN D_CLSCHS.DEND IS 'Дата окончания';
COMMENT ON COLUMN D_CLSCHS.RTIME_SEC IS 'Время повторного оказания';
COMMENT ON COLUMN D_CLSCHS.RCOUNT IS 'Количество на время оказания';
COMMENT ON COLUMN D_CLSCHS.RCOUNTMAX IS 'Максимальное количество на день';
COMMENT ON COLUMN D_CLSCHS.TIME_OR_COUNT IS 'Тип формирования графика. 0 - по времени оказания, 1 - по количеству, 2 - по количеству с ограничением';
COMMENT ON COLUMN D_CLSCHS.USE_CITO IS 'Признак возможности записи срочников. 0 - Запрещено, 1 - Разрешено в рабочие дни, 2 - Разрешено при наличии интервала для записи.';
COMMENT ON COLUMN D_CLSCHS.USE_DIRECTION IS 'Признак возможности создания направлений (назначений) : 0 - нет; 1 - да';
COMMENT ON COLUMN D_CLSCHS.SCHEDULE_TYPE IS 'Тип графика: 0 - основной, 1 - по кабинету';
COMMENT ON COLUMN D_CLSCHS.USE_WORK_CALENDAR IS 'Учитывать календарь рабочих\выходных дней:1-да,0-нет';
COMMENT ON COLUMN D_CLSCHS.RCOUNT_CITO IS 'Максимальное количество срочников в день';
COMMENT ON COLUMN D_CLSCHS.CLSCH_TYPE IS 'Тип построения: 0 - обычный, 1 - динамический, 2 - живая очередь';
COMMENT ON COLUMN D_CLSCHS.IS_SUBST IS 'Тип влияния на другие графики: 0 - обычный, 1 - замещение, 2 - дополнение';
COMMENT ON COLUMN D_CLSCHS.IS_STRICTORDR IS 'График строгой очередности: null - нет, 1 - да';
COMMENT ON COLUMN D_CLSCHS.SCH_START_DATE IS 'Дата отсчета периода графика (если требуется отличная от настроенной в графике)';
COMMENT ON COLUMN D_CLSCHS.SCH_RESOURCE IS 'Ресурс расписания';
COMMENT ON COLUMN D_CLSCHS.EXPORT_YO IS 'Передавать в сервис УО (1- да, 0 - нет)';
COMMENT ON COLUMN D_CLSCHS.PROFIL_YO_ID IS 'ID профиля услуги УО';
COMMENT ON COLUMN D_CLSCHS.PURPOSE IS 'Целевое назначение графика расписания: 0 - для регистратуры, 1 - для лаборатории, 2 - для опер.блока';
COMMENT ON COLUMN D_CLSCHS.RTIME_PRIM IS 'Длительность';

COMMENT ON TABLE D_CLSCHS IS 'Назначенные графики';
```

---

#### Таблица №5: D_DIRECTION_SERVICES

```sql
CREATE TABLE D_DIRECTION_SERVICES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    HID NUMBER(17),
    IS_COMBINED_PAYMENT NUMBER(1) NOT NULL,
    IS_NECESSARY NUMBER(1) NOT NULL,
    SERVICE NUMBER(17) NOT NULL,
    EMPLOYER_TO NUMBER(17),
    CABLAB_TO NUMBER(17),
    REC_DATE DATE,
    VISIT_PURPOSE NUMBER(17),
    REF_KIND NUMBER(17),
    VISIT_KIND NUMBER(17),
    DISEASECASE NUMBER(17),
    REG_TYPE NUMBER(1) NOT NULL,
    SERV_STATUS NUMBER(1) NOT NULL,
    IS_PRIMARY NUMBER(1) NOT NULL,
    S_COMMNET VARCHAR2(1200),
    HH_DEP NUMBER(17),
    REC_TYPE NUMBER(1),
    SER_COUNT NUMBER(19,2) NOT NULL,
    TIME_TYPE NUMBER(17),
    RPID NUMBER(17),
    IRID NUMBER(17),
    PAYMENT_KIND NUMBER(17),
    SERV_STATUS_REASON VARCHAR2(250),
    QUOTA_Q NUMBER(17),
    UK_HASH VARCHAR2(75) NOT NULL,
    DC_DIAGNOSIS NUMBER(17),
    LPU_SERVICE NUMBER(17),
    REC_DURATION NUMBER(5),
    TICKET_N NUMBER(5),
    TICKET_S VARCHAR2(75),
    RQS_LIMIT NUMBER(17),
    EX_SYSTEM NUMBER(17),
    PURCHASE_ORDER NUMBER(17),
    IS_CONFIRMED NUMBER(1) NOT NULL,
    NURSE_USER_TEMPLATES NUMBER(17),
    CONFIRM_DATE DATE,
    CONFERENCE_TYPE NUMBER(1),
    CONFERENCE VARCHAR2(200),
    EMPLOYER_CANCEL NUMBER(17),
    LOCALIZATION NUMBER(17),
    SERV_DESC VARCHAR2(2000),
    COMPLID NUMBER(17),
    COMPSTR NUMBER(17),
    IMPORTANT NUMBER(1) NOT NULL,
    PATIENT NUMBER(17),
    GUID VARCHAR2(36),
    ATTENDANCE_STATE NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_DIRECTION_SERVICES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DIRECTION_SERVICES.ID IS 'ID';
COMMENT ON COLUMN D_DIRECTION_SERVICES.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_DIRECTION_SERVICES.PID IS 'Направления';
COMMENT ON COLUMN D_DIRECTION_SERVICES.HID IS 'Иерархия';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IS_COMBINED_PAYMENT IS 'Используется ли комбинированная оплата : 0 - нет; 1 - да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IS_NECESSARY IS 'Обязательна ли услуга для закрытия направления : 0-нет ; 1-да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.SERVICE IS 'Услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.EMPLOYER_TO IS 'Врач, которому назначена услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.CABLAB_TO IS 'Кабинет, которому назначена услуга';
COMMENT ON COLUMN D_DIRECTION_SERVICES.REC_DATE IS 'Время назначения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.VISIT_PURPOSE IS 'Цель посещения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.REF_KIND IS 'Вид обращения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.VISIT_KIND IS 'Вид посещения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.DISEASECASE IS 'Случай заболевания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.REG_TYPE IS 'Тип регистрации';
COMMENT ON COLUMN D_DIRECTION_SERVICES.SERV_STATUS IS 'Статус услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IS_PRIMARY IS 'Услуга первичная : 0- нет; 1- да';
COMMENT ON COLUMN D_DIRECTION_SERVICES.S_COMMNET IS 'Комментарий';
COMMENT ON COLUMN D_DIRECTION_SERVICES.HH_DEP IS 'Истоия болезни : отделения';
COMMENT ON COLUMN D_DIRECTION_SERVICES.REC_TYPE IS 'Тип регистратуры : 0 - врачей, 1 - услуг';
COMMENT ON COLUMN D_DIRECTION_SERVICES.SER_COUNT IS 'Кратность';
COMMENT ON COLUMN D_DIRECTION_SERVICES.TIME_TYPE IS 'Тип интевала, на который произведена запись';
COMMENT ON COLUMN D_DIRECTION_SERVICES.RPID IS 'Направление на услугу, явл. точкой отсчета';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IRID IS 'Направление на услугу, инициирующее внесение результата';
COMMENT ON COLUMN D_DIRECTION_SERVICES.PAYMENT_KIND IS 'Вид оплаты';
COMMENT ON COLUMN D_DIRECTION_SERVICES.SERV_STATUS_REASON IS 'Причина смены статуса услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.QUOTA_Q IS 'Ограничение по квоте';
COMMENT ON COLUMN D_DIRECTION_SERVICES.UK_HASH IS 'Уникальный ключ записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.DC_DIAGNOSIS IS 'Диагноз случая заболевания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.LPU_SERVICE IS 'Услуга ЛПУ';
COMMENT ON COLUMN D_DIRECTION_SERVICES.REC_DURATION IS 'Длительность оказания в минутах';
COMMENT ON COLUMN D_DIRECTION_SERVICES.TICKET_N IS 'Номер квитка расписания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.TICKET_S IS 'Номер,описание квитка расписания';
COMMENT ON COLUMN D_DIRECTION_SERVICES.RQS_LIMIT IS 'Ресурс квоты записи в расписание';
COMMENT ON COLUMN D_DIRECTION_SERVICES.EX_SYSTEM IS 'Источник записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.PURCHASE_ORDER IS 'Заказ-наряд';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IS_CONFIRMED IS 'Признак подтверждения записи на прием пациентом: 0 - не подтверждена, 1 - подтверждена, 2 - запись отменена, 3 - запись перенесена, 4 - явка, 5 - не дозвонились';
COMMENT ON COLUMN D_DIRECTION_SERVICES.NURSE_USER_TEMPLATES IS 'Шаблон медсестры';
COMMENT ON COLUMN D_DIRECTION_SERVICES.CONFIRM_DATE IS 'Дата подтверждения записи';
COMMENT ON COLUMN D_DIRECTION_SERVICES.CONFERENCE_TYPE IS 'Выбор из дополнительного словаря - TM_TYPE - Тип консультации';
COMMENT ON COLUMN D_DIRECTION_SERVICES.EMPLOYER_CANCEL IS 'Врач, отменивший направление';
COMMENT ON COLUMN D_DIRECTION_SERVICES.LOCALIZATION IS 'Группы локализаций';
COMMENT ON COLUMN D_DIRECTION_SERVICES.SERV_DESC IS 'Описание услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.CONFERENCE IS 'Консультация';
COMMENT ON COLUMN D_DIRECTION_SERVICES.IMPORTANT IS 'Важное: 1 - да, 0 - нет';
COMMENT ON COLUMN D_DIRECTION_SERVICES.COMPLID IS 'Ссылка на запись головной комплексной услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.COMPSTR IS 'Ссылка на настройку состава комплексной услуги';
COMMENT ON COLUMN D_DIRECTION_SERVICES.GUID IS 'GUID';
COMMENT ON COLUMN D_DIRECTION_SERVICES.ATTENDANCE_STATE IS 'Cостояние посещаемости: 0 – Не определено, 1 – Явка, 2 – Неявка';
COMMENT ON COLUMN D_DIRECTION_SERVICES.PATIENT IS 'Пациент';

COMMENT ON TABLE D_DIRECTION_SERVICES IS 'Направления : услуги';
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 4
- Загружено тел функций: 3

---

### Функция №1: D_PKG_DAT_TOOLS.GET_WEEK_DAY_NUM

```sql
-- Oracle PACKAGE: GET_WEEK_DAY_NUM
-- Возвращает: return NUMBER
--======================================================================
function GET_WEEK_DAY_NUM
(
  pdDATE                               DATE
) return NUMBER 
is
begin
  case trim(lower(to_char(pdDATE,'Day')))
    when 'monday' then return 1;
    when 'понедельник' then return 1;
    when 'tuesday' then return 2;
    when 'вторник' then return 2;
    when 'wednesday' then return 3;
    when 'среда' then return 3;
    when 'thursday' then return 4;
    when 'четверг' then return 4;
    when 'friday' then return 5;
    when 'пятница' then return 5;
    when 'saturday' then return 6;
    when 'суббота' then return 6;
    when 'sunday' then return 7;
    when 'воскресенье' then return 7;
    else return null;
  end case;      
end GET_WEEK_DAY_NUM;
```

---

### Функция №2: D_PKG_SCHEDULE.UPD

```sql
-- Oracle PACKAGE: UPD
--======================================================================
procedure UPD
(
  pnID                                 in NUMBER,          --id
  pnLPU                                in NUMBER,          --МО
  psCODE                               in VARCHAR2,        --код
  psNAME                               in VARCHAR2,        --наименование
  pdSTART_DATE                         in DATE,            --дата отсчета
  pnSCH_TYPE                           in NUMBER,          --тип графика
  psHOLIDAYS                           in VARCHAR2,        --выходные
  pnQUOTING                            in NUMBER,          --для квотирования
  vAPI_VERSION                         in NUMBER default 1, -- Версионность API
  pnSCH_KIND                           in NUMBER default null --Вид графика: NULL - обычный, 1 - для госпитализации, 2 - маршрутизация
)
is
  nCID                  D_PKG_STD.tREF;
  nSCH_KIND             D_SCHEDULE.SCH_KIND%type := pnSCH_KIND;
begin
  -- Поиск каталога --
  EXIST(pnID,pnLPU,nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'SCHEDULE_UPDATE',pnID);
  CHECKS(pnID, pnLPU, pnSCH_TYPE, psNAME, psCODE, 'UPD');
  if vAPI_VERSION < gvAPI_CURRENT_VERSION then
    select case when vAPI_VERSION < 2 then coalesce(nSCH_KIND, t.SCH_KIND) else nSCH_KIND end
      into nSCH_KIND
      from D_SCHEDULE t
     where t.ID = pnID;
  end if;
  begin
    update D_SCHEDULE d
       set d.CODE       = psCODE,
           d.NAME       = psNAME,
           d.START_DATE = trunc(pdSTART_DATE),
           d.SCH_TYPE   = pnSCH_TYPE,
           d.HOLIDAYS   = psHOLIDAYS,
           d.QUOTING    = pnQUOTING,
           d.SCH_KIND   = nSCH_KIND
     where d.ID         = pnID
       and d.LPU        = pnLPU;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'U', sqlcode);
  end;
```

---

### Функция №3: D_PKG_SCHEDULE.ADD

```sql
-- Oracle PACKAGE: ADD
--======================================================================
procedure ADD
(
  pnD_INSERT_ID                        out NUMBER,         --id
  pnLPU                                in NUMBER,          --МО
  pnCID                                in NUMBER,          --Каталог
  psCODE                               in VARCHAR2,        --код
  psNAME                               in VARCHAR2,        --наименование
  pdSTART_DATE                         in DATE,            --дата отсчета
  pnSCH_TYPE                           in NUMBER,          --тип графика
  psHOLIDAYS                           in VARCHAR2,        --выходные
  pnQUOTING                            in NUMBER,          --для квотирования
  pnSCH_KIND                           in NUMBER default null --Вид графика: NULL - обычный, 1 - для госпитализации, 2 - маршрутизация
)
is
begin
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,pnCID,null,'SCHEDULE_INSERT',null);
  CHECKS(null, pnLPU, pnSCH_TYPE, psNAME, psCODE, 'INS');
  begin
    insert into D_SCHEDULE d
    (
      ID,
      CODE,
      NAME,
      START_DATE,
      LPU,
      CID,
      SCH_TYPE,
      HOLIDAYS,
      QUOTING,
      SCH_KIND
    )
      values
    (
      D_GEN_ID,
      psCODE,
      psNAME,
      trunc(pdSTART_DATE),
      pnLPU,
      pnCID,
      pnSCH_TYPE,
      psHOLIDAYS,
      pnQUOTING,
      pnSCH_KIND
    ) returning ID into pnD_INSERT_ID;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'I', sqlcode);
  end;
```

---

### Функция №4: D_PKG_SCHEDULESP_TIMES.UPD_S

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 4
- Загружено тел функций: 4

---

### Функция №1: d_pkg_dat_tools.get_week_day_num

```sql
CREATE OR REPLACE FUNCTION d_pkg_dat_tools.get_week_day_num(pddate timestamp without time zone)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    IF trim(lower(to_char(pddate,'TMDay'))::text) = 'monday' THEN
        return 1;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'понедельник' THEN
        return 1;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'tuesday' THEN
        return 2;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'вторник' THEN
        return 2;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'wednesday' THEN
        return 3;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'среда' THEN
        return 3;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'thursday' THEN
        return 4;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'четверг' THEN
        return 4;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'friday' THEN
        return 5;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'пятница' THEN
        return 5;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'saturday' THEN
        return 6;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'суббота' THEN
        return 6;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'sunday' THEN
        return 7;

    ELSIF trim(lower(to_char(pddate,'TMDay'))::text) = 'воскресенье' THEN
        return 7;

    ELSE
        return null;

    END IF;
END
$function$
```

---

### Функция №2: d_pkg_schedule.upd

```sql
CREATE OR REPLACE PROCEDURE d_pkg_schedule.upd(IN pnid numeric, IN pnlpu numeric, IN pscode character varying, IN psname character varying, IN pdstart_date timestamp without time zone, IN pnsch_type numeric, IN psholidays character varying, IN pnquoting numeric, IN vapi_version numeric DEFAULT 1, IN pnsch_kind numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
    nSCH_KIND d_schedule.sch_kind%TYPE := pnsch_kind;
BEGIN
    CALL d_pkg_schedule.exist(pnid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'SCHEDULE_UPDATE', pnid);
    CALL d_pkg_schedule.checks(pnid, pnlpu, pnsch_type, psname, pscode, 'UPD');
    IF vapi_version < (d_pkg_schedule.GET_gvapi_current_version()) THEN
        SELECT
            (CASE
                WHEN vapi_version < 2 THEN coalesce(nsch_kind,t.sch_kind)
                ELSE nsch_kind
            END)
        INTO STRICT nsch_kind
        FROM
            d_schedule t
        WHERE
            t.id = pnid::bigint;

    END IF;
    BEGIN
        update d_schedule d set code = pscode , "name" = psname , start_date = trunc(pdstart_date) , sch_type = pnsch_type , holidays = psholidays , quoting = pnquoting , sch_kind = nsch_kind where d.id = pnid::bigint
             AND d.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'SCHEDULE');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'SCHEDULE_UPDATE', pnid);
END
$procedure$
```

---

### Функция №3: d_pkg_schedule.add

```sql
CREATE OR REPLACE PROCEDURE d_pkg_schedule.add(INOUT pnd_insert_id numeric, IN pnlpu numeric, IN pncid numeric, IN pscode character varying, IN psname character varying, IN pdstart_date timestamp without time zone, IN pnsch_type numeric, IN psholidays character varying, IN pnquoting numeric, IN pnsch_kind numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
BEGIN
    pnd_insert_id := null;
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, pncid, (null)::numeric, 'SCHEDULE_INSERT', (null)::numeric);
    CALL d_pkg_schedule.checks((null)::numeric, pnlpu, pnsch_type, psname, pscode, 'INS');
    BEGIN
        INSERT INTO d_schedule AS d ( "id" , "code" , "name" , "start_date" , "lpu" , "cid" , "sch_type" , "holidays" , "quoting" , "sch_kind" ) VALUES ( d_gen_id(),pscode,psname,trunc(pdstart_date),pnlpu,pncid,pnsch_type,psholidays,pnquoting,pnsch_kind ) RETURNING id INTO pnd_insert_id;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'I',(SQLSTATE)::varchar);

    END;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, pncid, (null)::numeric, 'SCHEDULE_INSERT', pnd_insert_id);
END
$procedure$
```

---

### Функция №4: d_pkg_schedulesp_times.upd_s

```sql
CREATE OR REPLACE PROCEDURE d_pkg_schedulesp_times.upd_s(IN pnid numeric, IN pnlpu numeric, IN pstime_begin character varying, IN pstime_end character varying, IN pntime_type numeric, IN pngen_error numeric DEFAULT 1, IN vapi_version numeric DEFAULT 1, IN pnstep numeric DEFAULT NULL::numeric, IN pnlimits numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    dTIME_BEGIN timestamp(0);
    dTIME_END timestamp(0);
    nCID numeric(17);
    nPID numeric(17);
    nTIME_TYPE NUMERIC;
    sREGION varchar(50);
    nSTEP d_schedulesp_times.step%TYPE := pnstep;
    nLIMITS d_schedulesp_times.limits%TYPE := pnlimits;
BEGIN
    sregion := d_pkg_options.get(psSO_CODE => 'Region',pnLPU => pnlpu,pnRAISE => 0);
    CALL d_pkg_schedulesp_times.exist(pnid, pnlpu, ncid, npid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'SCHEDULESP_TIMES_UPDATE', pnid);
    dtime_begin := to_timestamp_simple(concat('01.01.0001 ', pstime_begin),d_pkg_std.frm_dt());
    dtime_end := to_timestamp_simple(concat('01.01.0001 ', pstime_end),d_pkg_std.frm_dt());
    IF nullif(sregion,'') IS NOT NULL
     AND sregion = '54' THEN
        IF pntime_type = - 1::numeric THEN
            ntime_type := null;

        ELSE
            ntime_type := pntime_type;

        END IF;

    ELSE
        ntime_type := pntime_type;

    END IF;
    CALL d_pkg_schedulesp_times.checks(pnid, pnlpu, npid, dtime_begin, dtime_end, ntime_type, pngen_error);
    IF vapi_version < (d_pkg_schedulesp_times.GET_gvapi_current_version()) THEN
        SELECT
            (CASE
                WHEN vapi_version < 2 THEN coalesce(nstep,t.step)
                ELSE nstep
            END),
            (CASE
                WHEN vapi_version < 2 THEN coalesce(nlimits,t.limits)
                ELSE nlimits
            END)
        INTO STRICT nstep, nlimits
        FROM
            d_schedulesp_times t
        WHERE
            t.id = pnid::bigint;

    END IF;
    BEGIN
        update d_schedulesp_times t set time_begin = dtime_begin , time_end = dtime_end , time_type = ntime_type , step = nstep , limits = nlimits where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'SCHEDULESP_TIMES');

    END IF;
    CALL d_pkg_schedulesp.remake_interval(npid, pnlpu);
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'SCHEDULESP_TIMES_UPDATE', pnid);
END
$procedure$
```



---

1) все переменные в SQL запросах должны  писаться в верхнем регистре, кроме  первых двух букв, которые обозначают тип переменной pnLPU, psCODE
   не правильно:   cmptype="ActionVar"  name="id"
   правильно   :   cmptype="ActionVar"  name="pnID"
   не правильно:   cmptype="Variable" name="code"
   правильно:      cmptype="Variable" name="psCODE"
   не правильно:   cmptype="ActionVar" name="LPU"
   правильно   :   cmptype="ActionVar" name="pnLPU"

2) Все цифровые переменные  должны быть обернуты в to_number (пример: to_number(:pnLPU) )
4) Все псевдонимы  в SQL запросах "t" должны быть заменены на аббревиатуры тех таблиц, которые они привязаны (пример core.v_job_workers_group t => jwg  или worg)
5) Все переменные в запросах, которые содержать _ID или "ID" точно являются числами и должны быть обернуты to_number
6) Все атрибуты cmpActionVar и cmpDataSetVar должны быить выстроены в одну линию по вертикали. закрывающийся тэг "/>" должен быть без пробелов между последним атрибутом
7) SQL запросы  должны иметь следующий вид :
   ```sql
	<component cmptype="Action" name="AC_get_gw">
        <![CDATA[
        select jwg.ID,
               jwg.CODE,
               jwg.CNT_WORKERS,
          from core.v_job_workers_group jwg
         where jwg.ID = to_number(:id)
        ]]>
		***
	```
8) Пробел после function  не должен быть правильное написание "function() {"
9) в JS функциях после ",",":",";" должен быть пробел
10) атрибуты функций должны начинаться в одну длинию по вертикали , каждый атрибут на новой строке
11) Если в атрибутах cmpActionVar  есть атрибут put , тогда эту переменную необходимо получать в SQL запросе  через INTO (Если этого блока нет, тогда надо добавить его)
   не правильно:
   ```xml
    <component cmptype="Action" name="AC_get_gw">
        <![CDATA[
        select
            t.id,
            t.code
        from core.v_job_workers_group t
        where id = :id
        ]]>
        <component cmptype="ActionVar"  name="id"   src="data"      srctype="var"/>
        <component cmptype="ActionVar"  name="id"   src="tb_Ctrl"   srctype="ctrl" put=""/>
        <component cmptype="ActionVar"  name="code" src="code_Ctrl" srctype="ctrl" put=""/>
    </component>
	```
  правильно:
   ```xml
    <component cmptype="Action" name="AC_get_gw">
        <![CDATA[
        select jwg.id,
               jwg.code
	      into :pnID_TAB,
		       :psCODE,
          from CORE.V_JOB_WORKERS_GROUP jwg
         where id = :pnID
        ]]>
        <component cmptype="ActionVar"  name="pnID"     src="data"     srctype="var"/>
        <component cmptype="ActionVar"  name="pnID_TAB" src="ctrlTb"   srctype="ctrl" put=""/>
        <component cmptype="ActionVar"  name="psCODE"   src="ctrlCode" srctype="ctrl" put=""/>
    </component>
	```
13) Имена инициализированных переменных JS должны писаться в кэмэлкейсе без подчеркивания пример:
    не правильно:let min_workers = +getValue('ctrlMinWorkers');
	правильно : let minWorkers = +getValue('ctrlMinWorkers');
    не правильно: var min_workers = +getValue('ctrlMinWorkers');
	правильно : var minWorkers = +getValue('ctrlMinWorkers');
14) имена таблиц в SQL запросах писать в верхнем регистре пример:
    не правильно:  core.v_job_workers_group
	правильно: CORE.V_JOB_WORKERS_GROUP

16) Удалить все debbuger из JS
17) оставь комментарии без изменения
18) имена JS функций должны быть в кэмэлкейсе  с маленькой буквы пример
    не правильно :   Form.OnCreate
	правильно :      Form.onCreate
19) Условие if в JS должен иметь следующий вид  "if (getVar('action') == 'INSERT') { пример:
    не правильно :   if(getVar('action')=='INSERT'){
	правильно :      if (getVar('action') === 'INSERT') {
20) JS функция Должна иметь следующий шаблон:
```js
    Form.afterSelect = function() {
	    setVar('ID', null);
	    *****
	};
```
21) Все двойные кавычки в JS, в которых пишется текст должен быть заменен на одинарные кавычки:
    не правильно :   setVar("ID", "sdgfsafgsdfgsd";
	правильно :      setVar('CODE', 'текст \' "XXX" ');
22) Вызов функций в JS base().afterSelect заменить на  Form.afterSelect , если он не вызывается в атрибутах компонента
    не правильно :   executeAction('acSelect', base().afterSelect);
	правильно :      executeAction('acSelect', Form.afterSelect);
    правильно : <div cmptype="form" oncreate="base().onCreate();" onclose="base().onClose();" onshow="base().onShow();" style="width:100%;">
23) все конструкции DDL писать в нижнем регистре (coalesce select from where and order by dense_rank);
24) Все таблицы писать в верхнем регистре;
25) все псевдонимы писать с маленькой буквы;
26) все имена полей писать  в верхнем регистре;
27) все SQL функции писать в верхнем регистре;
28) to_number писать в нижнем регистре;
29) ROWNUM писать в нижнем регистре;
30) перемещать атрибуты put="" и len="17" вконец всех атрибутов испоользовать шаблон из блока "правильно:"
     не правильно :
        <component cmptype="ActionVar" name="pnD_INSERT_ID" put="v0" len="17" src="psNEW_ID" srctype="var"/>
        <component cmptype="ActionVar" name="pnLPU"         get="v1"          src="LPU"      srctype="session"/>
        <component cmptype="ActionVar" name="pnPID"         get="v2"          src="pnTEMP_ID" srctype="var"/>
    правильно:
        <component cmptype="ActionVar" name="pnLPU"         src="LPU"       srctype="session"/>
        <component cmptype="ActionVar" name="pnD_INSERT_ID" src="psNEW_ID"  srctype="var"     put="" len="17"/>
        <component cmptype="ActionVar" name="pnPID"         src="pnTEMP_ID" srctype="var"/>


31) Если на форме нет cmpFetch или <component cmptype="Fetch" тогда все атрибуты get="" и put="" должны быть пустыми значениями в cmpActionVar и cmpDataSetVar
    Если есть атрибут только get , без атрибута put , тогда этот атрибут удаляется

33) в SQL запросах в конструкция  "case when",  "else" переноситься на новую строчку под "case" + 2 пробела
     не правильно :
                select case when (ds.REC_DATE = trunc(ds.REC_DATE) and ds.REC_DATE is not null) then v.VISIT_DATE
                            else ds.REC_DATE
                       end
    правильно:
                select case when (ds.REC_DATE = trunc(ds.REC_DATE) and ds.REC_DATE is not null) then v.VISIT_DATE
                         else ds.REC_DATE
                       end
34) В SQL запросах, когда встречается однобуквенный псевдоним, необходимо его заменить на более осмысленную версия, которая ассоциируется с  таблицей, на которую ссылается псевдоним:
     не правильно :
          D_V_VISITS v
          D_V_DIRECTION_SERVICES d
    правильно:
          D_V_VISITS vis
          D_V_DIRECTION_SERVICES ds

36) После каждой JS функции нужно ставить ";" пример:
     не правильно :
	Form.onCreate = function() {
            ***
        }
    правильно:
	Form.onCreate = function() {
            ***
        };
37) После первого открывающегося тэга нет смещения на 4 символа , первый вложенный уровеньв ложения без пробелов  пишется слева
38) если в атрибуте "name" имени cmpActionVar, cmpDataSetVar, <component cmptype="ActionVar", <component cmptype="Variable"   первый символ "~", тогда имя конвертируется  как и раньше но после этого в начало имени возарвщвется символ "~"


40) необходимо все запросы на форме поместить в блоки Router которые имеют следующий вид.
Для Action :
```
	<cmpAction name="ACT_SEL">
        <cmpActionRouter condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
                begin
                    -- сюда необходимо поместить код для ORACLE (код на форме)
                end;
            ]]>
        </cmpActionRouter>
        <cmpActionRouter  condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
                begin
                    -- сюда необходимо поместить код для POSTGRESQL (с преобразованием типов)
                end;
            ]]>
        </cmpActionRouter>
        <cmpActionVar name="LPU" src="LPU" srctype="session" type="integer"/>
        <cmpActionVar name="RES" src="RES" srctype="var"/>
        <cmpActionVar name="ACTION_RESULT" src="ACTION_RESULT:caption" srctype="ctrl" put="" len="4000"/>
    </cmpAction>
```
Для DataSet :
```
	<cmpDataSet name="DS_SEL2" activateoncreate="false">
        <cmpDataSetRouter  condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
                -- сюда необходимо поместить код для ORACLE (код на форме)
            ]]>
        </cmpDataSetRouter>
        <cmpDataSetRouter  condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
                -- сюда необходимо поместить код для POSTGRESQL (с преобразованием типов)
            ]]>
        </cmpDataSetRouter>
        <cmpDataSetVar name="pnLPU" src="LPU" srctype="session"/>
        <cmpDataSetVar name="pnPID" src="ID"  srctype="var"/>
    </cmpDataSet>
```
Все атрибуты "src" остаются без изменений, а вот "name" нужно переименовывать согласно правилам , которе описаны в пункте 1

41) Всевызываемые функции или процедуры должнывызываться через именованные атрибуты и на против каждого атрибута необходимо указать комментарий, за что отвечает этот атрибут. Это необходимо делать для всех видов зароса и блоков PLSQL в синтаксисе PostgreSQL и OracleSQL

42) Если в JS коде нсть сравнение с числом, тогда второй аргумент нужно принудительно привести к число через "+"Исходный пример:
```
                            if (getVar('ModalResult') == 1) {
                                refreshDataSet('dsHpkMkbs');
                            }
```
Переработанный код:
```
                            if (+getVar('ModalResult') === 1) {
                                refreshDataSet('dsHpkMkbs');
                            }

```

43) в POSTGRE необходимо принудительно конвертировать входящие переменные через "::"
Пример:
```
                    select hmkb.id,
                           hmkb.mkb,
                           hmkb.mkb_handle,
                           hmkb.mkb_own,
                           hmkb.max_count
                      from d_v_hpk_mkbs hmkb
                     where hmkb.lpu = to_number(:pnLPU)
                       and hmkb.pid = to_number(:pnHPK_ID)
                       and hmkb.mkb_handle = :psHPK_ID
```

переработанный код:
```
                    select hmkb.id,
                           hmkb.mkb,
                           hmkb.mkb_handle,
                           hmkb.mkb_own,
                           hmkb.max_count
                      from d_v_hpk_mkbs hmkb
                     where hmkb.lpu = (:pnLPU)::numeric
                       and hmkb.pid = (:pnHPK_ID)::numeric
                       and hmkb.mkb_handle = (:psHPK_ID)::text
```
44) Все таблицы должны писаться в верхнем регистре, все имена полуй пишуться в верхним регистре, все псевданимы таблиц пишутся с маленькой буквы
Пример:
```
                    select hmkb.id,
                           hmkb.mkb,
                           hmkb.mkb_handle,
                           hmkb.mkb_own,
                           hmkb.max_count
                      from d_v_hpk_mkbs hmkb
                     where hmkb.lpu = to_number(:pnLPU)
                       and hmkb.pid = to_number(:pnHPK_ID)
```
Переработанный запрос:
```
                    select hmkb.ID,
                           hmkb.MKB,
                           hmkb.MKB_HANDLE,
                           hmkb.MKB_OWN,
                           hmkb.MAX_COUNT
                      from D_V_HPK_MKBS hmkb
                     where hmkb.LPU = to_number(:pnLPU)
                       and hmkb.PID = to_number(:pnHPK_ID)
```

45) SQL запрос в DataSet начинаеся сразу же под "<![CDATA[" начала. select  должно быть на одной линии под "<![CDATA[" по вертикали
Пример исходника :
```
            <cmpDataSetRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                    -- POSTGRESQL: выборка диагнозов для вида плана госпитализации
                    -- pnLPU - идентификатор ЛПУ из сессии
                    -- pnHPK_ID - идентификатор вида плана госпитализации
                    select hmkb.ID,
                           hmkb.MKB,
                           hmkb.MKB_HANDLE,
                           hmkb.MKB_OWN,
                           hmkb.MAX_COUNT
                      from D_V_HPK_MKBS hmkb
                     where hmkb.LPU = (:pnLPU)::numeric
                       and hmkb.PID = (:pnHPK_ID)::numeric
                ]]>
            </cmpDataSetRouter>
```
Исправленый вариант:
```
            <cmpDataSetRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                -- POSTGRESQL: выборка диагнозов для вида плана госпитализации
                -- pnLPU - идентификатор ЛПУ из сессии
                -- pnHPK_ID - идентификатор вида плана госпитализации
                select hmkb.ID,
                       hmkb.MKB,
                       hmkb.MKB_HANDLE,
                       hmkb.MKB_OWN,
                       hmkb.MAX_COUNT
                  from D_V_HPK_MKBS hmkb
                 where hmkb.LPU = (:pnLPU)::numeric
                   and hmkb.PID = (:pnHPK_ID)::numeric
                ]]>
            </cmpDataSetRouter>
```

46) необходимо в SQL запросах написать на против каждого поля комментарий "--" в котором описано что это за поле в запросе

47)
## ПРАВИЛО ПРЕОБРАЗОВАНИЯ ACTION С UNIT/ACTION ИЛИ ПРЯМЫМ ACTION

### Тип 1: Action с атрибутами unit и action

**Исходный вид:**
```xml
<component cmptype="Action" name="addHpkMkbs" unit="HPK_MKBS" action="INSERT">
    <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="ActionVar" name="pnID" src="newid" srctype="var" put="v1" len="17"/>
</component>
```

**Необходимо преобразовать в:**
```xml
<cmpAction name="addHpkMkbs">
    <cmpActionRouter condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
    <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnID,   -- Выходной ID записи
                                  pnLPU         => :pnLPU); -- ЛПУ пользователя
        end;
        ]]>
    </cmpActionRouter>
    <cmpActionVar name="pnLPU" src="LPU" srctype="session" type="integer"/>
    <cmpActionVar name="pnID" src="newid" srctype="var" put="" len="17"/>
</cmpAction>
```

**Исходный вид:**
```xml
<component cmptype="Action" name="addHpkMkbs" unit="HPK_MKBS" action="INSERT">
    <component cmptype="ActionVar" name="pnLPU"        src="LPU"        srctype="session"/>
    <component cmptype="Actionvar" name="pnID"         src="newid"      srctype="var"     put="v1" len="17"/>
    <component cmptype="ActionVar" name="pnPID"        src="HPK_ID"     srctype="var" />
    <component cmptype="ActionVar" name="pnMKB"        src="MKB"        srctype="var" />
    <component cmptype="ActionVar" name="psMKB_HANDLE" src="MKB_HANDLE" srctype="var" />
    <component cmptype="ActionVar" name="pnMKB_OWN"    src="MKB_OWN"    srctype="var" />
    <component cmptype="ActionVar" name="pnMKB_COUNT"  src="MKB_COUNT"  srctype="var" />
</component>
```

Необходимо преобразовать в:
```xml
<cmpAction name="addHpkMkbs">
    <cmpActionRouter condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
    <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnID,             -- Выходной ID записи
                                  pnLPU         => :pnLPU,            -- ЛПУ пользователя
                                  pnPID         => :pnPID,            -- Идентификатор родительской записи
                                  pnMKB         => :pnMKB,            -- Код МКБ
                                  psMKB_HANDLE  => :psMKB_HANDLE,     -- Признак основного МКБ
                                  pnMKB_OWN     => :pnMKB_OWN,        -- Собственный код МКБ
                                  pnMKB_COUNT   => :pnMKB_COUNT,      -- Максимальное количество
                                  pnCID         => :pnCID);           -- Каталог
        end;
        ]]>
    </cmpActionRouter>
    <cmpActionVar name="pnLPU"        src="LPU"        srctype="session"/>
    <cmpActionVar name="pnID"         src="newid"      srctype="var"     put="" len="17"/>
    <cmpActionVar name="pnPID"        src="HPK_ID"     srctype="var"/>
    <cmpActionVar name="pnMKB"        src="MKB"        srctype="var"/>
    <cmpActionVar name="psMKB_HANDLE" src="MKB_HANDLE" srctype="var"/>
    <cmpActionVar name="pnMKB_OWN"    src="MKB_OWN"    srctype="var"/>
    <cmpActionVar name="pnMKB_COUNT"  src="MKB_COUNT"  srctype="var"/>
    <cmpActionVar name="pnCID"        src="CID"        srctype="var"/>
</cmpAction>
```

### Тип 2: Action с прямым указанием функции в атрибуте action

**Исходный вид:**
```xml
<cmpAction name="ZnoRiskContrRes1DelAction" repeatername="RptZnoRiskContrRes" execon="del"
          action="D_PKG_R_ZNO_RISK_CONTR_RES.DEL">
    <cmpActionVar name="pnLPU" src="LPU" srctype="session"/>
    <cmpActionVar name="pnID" src="_clonedata_" srctype="var" property="ID" get="gID"/>
</cmpAction>
```

**Необходимо преобразовать в:**
```xml
<cmpAction name="ZnoRiskContrRes1DelAction">
    <cmpActionRouter condition="TYPE_DATABASE=ORACLE" action="D_PKG_R_ZNO_RISK_CONTR_RES.DEL"/>
    <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          call D_PKG_R_ZNO_RISK_CONTR_RES.DEL(pnLPU => :pnLPU,  -- ЛПУ пользователя
                                              pnID  => :pnID ); -- Идентификатор записи

        end;
        ]]>
    </cmpActionRouter>
    <cmpActionVar name="pnLPU" src="LPU" srctype="session" type="integer"/>
    <cmpActionVar name="pnID" src="_clonedata_" srctype="var"/>
</cmpAction>
```

**Правила преобразования для обоих типов:**

47.1 Удалить атрибуты `unit` и `action` из основного тэга (для типа 1), или только `action` (для типа 2)
47.2 Добавить `<cmpActionRouter condition="TYPE_DATABASE=ORACLE">` с исходными атрибутами
47.3 Добавить `<cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">` с CDATA блоком
47.4 В CDATA блоке вызвать функцию из `execproc` (для типа 1) или из `action` (для типа 2)
47.5 Все параметры передавать через именованные атрибуты с комментариями
47.6 Преобразовать имена переменных: `pnLPU`, `pnID` и т.д. (первые две буквы указывают тип: pn - число, ps - строка, pd - дата)
47.7 Атрибуты `put=""` и `len` переместить в конец
47.8 Удалить атрибуты `get=""` (если нет Fetch компонента на форме)


48) Отступы внутри блоков CDATA для SQL/PLSQL

**Правило:** Внутри блоков `CDATA` для **SQL запросов** (`cmpActionRouter` и `cmpDataSetRouter`) все вложенные конструкции должны иметь отступ **2 пробела** от начала строки. Для всех остальных блоков (например, JavaScript в `Script` компоненте) сохраняется отступ **4 пробела**.

**Пример для PL/SQL блока в Action:**
```xml
<!-- Правильно (2 пробела) - только для SQL/PLSQL -->
<cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
    <![CDATA[
    begin
      -- комментарий с 2 пробелами
      call PROCEDURE_NAME(param => :value);
    end;
    ]]>
</cmpActionRouter>
```

**Пример для SQL запроса в DataSet:**
```xml
<!-- Правильно (2 пробела) - только для SQL -->
<cmpDataSetRouter condition="TYPE_DATABASE=ORACLE">
    <![CDATA[
    select t.ID,
           t.NAME
      from TABLE t
     where t.ID = :pnID
    ]]>
</cmpDataSetRouter>
```

**Пример для JavaScript блока (сохраняем 4 пробела):**
```xml
<!-- Правильно (4 пробела) - для JavaScript -->
<component cmptype="Script">
    <![CDATA[
        Form.onCreate = function() {
            setVar('HPK_ID', getVar('HPK_ID', 1));
        };

        Form.onShow = function() {
            // код с отступом 4 пробела
        };
    ]]>
</component>
```

49) Выравнивание параметров в вызовах процедур/функций (только для SQL)

**Правило:** При вызове процедуры с именованными параметрами внутри SQL/PLSQL блока, если параметры переносятся на новую строку, отступ должен быть **на 2 пробела больше** чем у `call`:

```sql
-- Правильно (для SQL/PLSQL блоков)
call PROCEDURE_NAME(
  pnPARAM1 => :pnPARAM1,   -- отступ 2 пробела
  pnPARAM2 => :pnPARAM2,   -- отступ 2 пробела
  psPARAM3 => :psPARAM3    -- отступ 2 пробела
);

-- Альтернативный вариант (одна строка, если параметров мало)
call PROCEDURE_NAME(pnPARAM1 => :pnPARAM1, pnPARAM2 => :pnPARAM2);
```

50) Отступы для многострочных комментариев в SQL/PLSQL блоках

**Правило:** Комментарии внутри блоков CDATA для SQL/PLSQL должны иметь отступ **2 пробела**:

```xml
<![CDATA[
begin
  -- Это правильный комментарий (2 пробела)
  call PROC(
    pnID => :pnID);  -- Комментарий в конце строки (1 пробел до комментария)
end;
]]>
```

51) Структура begin/end блоков в SQL/PLSQL

**Правило:** Ключевые слова `begin` и `end` должны быть на отдельных строках и не иметь отступа внутри CDATA (выровнены по левому краю относительно содержимого CDATA). Весь SQL/PLSQL код внутри блока имеет отступ **2 пробела**:

```xml
<![CDATA[
begin
  -- код с отступом 2 пробела
  if condition then
    -- вложенный код с отступом 4 пробела (2 + 2)
    do_something;
  end if;
end;
]]>
```

### 52) Смешанные блоки (SQL внутри JS не допускаются)

**Правило:** Блоки SQL/PLSQL могут находиться только в компонентах `cmpActionRouter` и `cmpDataSetRouter`. В компоненте `Script` может быть только JavaScript код с отступом **4 пробела**. Не допускается смешивание SQL кода в JavaScript блоках.

### 53) Общее правило отступов

| Тип блока | Компонент | Отступ внутри CDATA |
|-----------|-----------|---------------------|
| SQL/PLSQL | `cmpActionRouter` | 2 пробела |
| SQL/PLSQL | `cmpDataSetRouter` | 2 пробела |
| JavaScript | `Script` | 4 пробела |
| JavaScript | атрибуты событий (onclick, oncreate и т.д.) | 4 пробела |

### 54) Форматирование SELECT запросов в DataSet

**Правило:** В блоках `cmpDataSetRouter` SQL запрос должен начинаться сразу после `CDATA[` без дополнительных отступов. Ключевые слова SQL (`select`, `from`, `where`, `and`, `order by`) должны быть выровнены по вертикали:

```xml
<cmpDataSetRouter condition="TYPE_DATABASE=ORACLE">
    <![CDATA[
    select hmkb.ID,                         -- Уникальный идентификатор записи
           hmkb.MKB,                        -- Код диагноза
           hmkb.MKB_HANDLE                  -- Ручной ввод
      from D_V_HPK_MKBS hmkb                 -- Представление диагнозов
     where hmkb.LPU = to_number(:pnLPU)     -- Фильтр по ЛПУ
       and hmkb.PID = to_number(:pnHPK_ID)  -- Фильтр по виду плана
    ]]>
</cmpDataSetRouter>
```


55) Форматирование вызова процедур/функций в одну строку (предпочтительный вариант)

**Правило:** Если количество параметров не превышает 3-х, вызов процедуры/функции должен быть оформлен **в одну строку**. Параметры разделяются запятой с пробелом после запятой. Комментарий к параметру ставится после `--` с одним пробелом.

**Пример:**
```sql
-- Правильно (3 параметра в одну строку)
call D_PKG_HPK_MKBS.ADD(pnLPU         => :pnLPU,          -- ЛПУ пользователя
                        pnPID         => :pnPID,          -- Идентификатор вида плана
                        pnD_INSERT_ID => :pnD_INSERT_ID); -- Выходной ID записи
```

```sql
-- Правильно (2 параметра в одну строку)
call D_PKG_HPK_MKBS.DELETE(pnID  => :pnID,   -- Идентификатор записи
                           pnLPU => :pnLPU); -- ЛПУ пользователя
```

```sql
-- Правильно (1 параметр)
call D_PKG_HPK_MKBS.GET(pnID => :pnID); -- Получить запись по ID
```

56) Форматирование вызова процедур/функций с переносом строк (для 4+ параметров)

**Правило:** Если количество параметров 4 и более, вызов процедуры/функции оформляется с переносом строки после открывающей скобки. Каждый параметр на новой строке с отступом **2 пробела** от уровня `call`. Закрывающая скобка и точка с запятой на отдельной строке с отступом **2 пробела**.

**Пример:**
```sql
-- Правильно (4+ параметров с переносом)
call D_PKG_HPK_MKBS.ADD(
  pnLPU         => :pnLPU,          -- ЛПУ пользователя
  pnPID         => :pnPID,          -- Идентификатор вида плана
  pnMKB         => :pnMKB,          -- Код МКБ
  pnMAX_COUNT   => :pnMAX_COUNT,    -- Максимальное количество
  pnCID         => :pnCID           -- Каталог для проверки прав
);
```

 57) Выравнивание параметров при вызове в одну строку

**Правило:** При вызове в одну строку все параметры должны быть выровнены по вертикали относительно первого параметра. Отступ для последующих строк - **на 20 пробелов больше** чем у `call` (или выравнивание по открывающей скобке).

```sql
-- Правильно (выравнивание параметров)
call PROCEDURE_NAME(pnPARAM1 => :pnPARAM1,      -- Комментарий
                    pnPARAM2 => :pnPARAM2,      -- Комментарий
                    psPARAM3 => :psPARAM3);     -- Комментарий
```

58) Пробелы вокруг оператора `=>`

**Правило:** Вокруг оператора `=>` всегда должны быть пробелы: пробел до и пробел после.

```sql
-- Правильно
call PROC(pnID => :pnID);

-- Неправильно
call PROC(pnID=>:pnID);
call PROC(pnID =>:pnID);
call PROC(pnID=> :pnID);
```

59) Комментарии к параметрам

**Правило:** Комментарий к параметру ставится после запятой (или после значения параметра) через `--` с одним пробелом. Комментарий должен описывать назначение параметра на русском языке.

```sql
-- Правильно
call PROC(pnID   => :pnID,    -- Идентификатор записи
          pnLPU  => :pnLPU,   -- ЛПУ пользователя
          psCODE => :psCODE); -- Код услуги

-- Неправильно (нет пробела после --)
call PROC(pnID => :pnID,--Идентификатор записи
          pnLPU => :pnLPU,--ЛПУ пользователя);
```

60) Точка с запятой в конце вызова

**Правило:** Вызов процедуры/функции всегда заканчивается точкой с запятой (`;`). При переносе строки точка с запятой ставится после закрывающей скобки на отдельной строке.

```sql
-- Правильно (одна строка)
call PROC(pnID => :pnID);  -- точка с запятой в конце

-- Правильно (многострочный)
call PROC(
  pnID  => :pnID,
  pnLPU => :pnLPU
);  -- точка с запятой после закрывающей скобки
```

61) Пустые строки между секциями

**Правило:** Между вызовом процедуры и закрывающим `end;` должна быть одна пустая строка для визуального разделения.

```xml
<![CDATA[
begin
  -- POSTGRESQL: комментарий
  call D_PKG_HPK_MKBS.ADD(pnLPU => :pnLPU,   -- ЛПУ пользователя
                          pnPID => :pnPID);   -- Идентификатор вида плана

end;
]]>
```

62) Приоритет форматирования вызова

**Правило:** Следовать следующему приоритету при форматировании:

1. **1-3 параметра** → Одна строка с выравниванием параметров
2. **4+ параметров** → Многострочный формат с переносом
3. **Длинные значения параметров** (>50 символов) → Многострочный формат независимо от количества параметров

63) Именованные параметры обязательны

**Правило:** При вызове процедур/функций всегда использовать **именованные параметры** (синтаксис `name => value`). Позиционная передача параметров запрещена.

```sql
-- Правильно (именованные параметры)
call PROC(pnID => :pnID, pnLPU => :pnLPU);

-- Неправильно (позиционная передача)
call PROC(:pnID, :pnLPU);
```

64) Регистр имен процедур и параметров

**Правило:** Имена процедур/функций пишутся в **верхнем регистре**. Имена параметров в вызове пишутся в **нижнем регистре** с префиксом типа (`pn`, `ps`, `pd`).

```sql
-- Правильно
call D_PKG_HPK_MKBS.ADD(pnLPU => :pnLPU, pnPID => :pnPID);

-- Неправильно (процедура в нижнем регистре)
call d_pkg_hpk_mkbs.add(pnLPU => :pnLPU, pnPID => :pnPID);

-- Неправильно (параметр в верхнем регистре)
call D_PKG_HPK_MKBS.ADD(PNLPU => :pnLPU, PNPID => :pnPID);
```

65) Примеры правильного форматирования

**Пример 1: 2 параметра (короткий вызов)**
```sql
call D_PKG_HPK_MKBS.DELETE(pnID  => :pnID,   -- Идентификатор записи
                           pnLPU => :pnLPU); -- ЛПУ пользователя
```

**Пример 2: 3 параметра (средний вызов)**
```sql
call D_PKG_HPK_MKBS.UPDATE(pnID    => :pnID,        -- Идентификатор записи
                           pnLPU   => :pnLPU,       -- ЛПУ пользователя
                           psNAME  => :psNAME);     -- Наименование
```

**Пример 3: 5 параметров (многострочный вызов)**
```sql
call D_PKG_HPK_MKBS.ADD(
  pnLPU         => :pnLPU,          -- ЛПУ пользователя
  pnPID         => :pnPID,          -- Идентификатор вида плана
  pnMKB         => :pnMKB,          -- Код МКБ
  pnMAX_COUNT   => :pnMAX_COUNT,    -- Максимальное количество
  pnCID         => :pnCID           -- Каталог для проверки прав
);
```

**Пример 4: Длинное значение параметра (многострочный)**
```sql
call D_PKG_HPK_MKBS.SET_DESCRIPTION(
  pnID    => :pnID,                  -- Идентификатор записи
  psTEXT  => :psVERY_LONG_TEXT       -- Очень длинное описание параметра
            || ' continued on next line'
);
```
# ДОПОЛНЕНИЕ ПРАВИЛА №66: СТИЛЬ КОМПОНЕНТОВ ДЛЯ M2
## Правило №66: Определение стиля компонентов на основе версии платформы

### 66.1 Определение версии формы

Перед преобразованием формы необходимо определить её версию на основе:
1. **Исходного кода** - если в форме используются `cmptype="DataSet"` без префикса `cmp` - это M2
2. **Контекста задачи** - явное указание "форма является M2"
3. **Расширения файла** - `.frm` в пути `mis_MEDDEV-151210` указывает на M2

### 66.2 Стиль компонентов для M2

**Если форма является M2, используются следующие компоненты:**

| Тип компонента | M2 синтаксис | D3 синтаксис (запрещен) |
|----------------|--------------|------------------------|
| **Action** | `<component cmptype="Action">` | `<cmpAction>` |
| **DataSet** | `<component cmptype="DataSet">` | `<cmpDataSet>` |
| **ActionRouter** | `<component cmptype="ActionRouter">` | `<cmpActionRouter>` |
| **DataSetRouter** | `<component cmptype="DataSetRouter">` | `<cmpDataSetRouter>` |
| **ActionVar** | `<component cmptype="ActionVar">` | `<cmpActionVar>` |
| **DataSetVar** | `<component cmptype="Variable">` (в DataSet) | `<cmpDataSetVar>` |
| **Grid** | `<component cmptype="Grid">` | `<cmpGrid>` |

### 66.3 Пример для M2 Action с Router

**Правильный M2 синтаксис:**
```xml
<!-- M2 стиль: component cmptype="Action" -->
<component cmptype="Action" name="acAddHpkMkbs">
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Выходной ID записи
                                  pnLPU         => :pnLPU,           -- ЛПУ пользователя
                                  pnPID         => :pnPID);          -- Идентификатор родительской записи
        end;
        ]]>
    </component>
    <component cmptype="ActionVar" name="pnLPU"         src="LPU"         srctype="session"/>
    <component cmptype="ActionVar" name="pnD_INSERT_ID" src="newid"       srctype="var"     put="" len="17"/>
    <component cmptype="ActionVar" name="pnPID"         src="HPK_ID"      srctype="var"/>
</component>
```

### 66.4 Пример для M2 DataSet с Router

**Правильный M2 синтаксис:**
```xml
<!-- M2 стиль: component cmptype="DataSet" -->
<component cmptype="DataSet" name="dsHpkMkbs" mode="Range">
    <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
        <![CDATA[
        select hmkb.ID,                         -- Уникальный идентификатор записи
               hmkb.MKB,                        -- Код диагноза МКБ
               hmkb.MKB_HANDLE,                 -- Признак ручного ввода
               hmkb.MKB_OWN,                    -- Собственный код МКБ
               hmkb.MAX_COUNT                   -- Максимальное количество записей
          from D_V_HPK_MKBS hmkb
         where hmkb.LPU = to_number(:pnLPU)
           and hmkb.PID = to_number(:pnHPK_ID)
        ]]>
    </component>
    <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        select hmkb.ID,
               hmkb.MKB,
               hmkb.MKB_HANDLE,
               hmkb.MKB_OWN,
               hmkb.MAX_COUNT
          from D_V_HPK_MKBS hmkb
         where hmkb.LPU = (:pnLPU)::numeric
           and hmkb.PID = (:pnHPK_ID)::numeric
        ]]>
    </component>
    <component cmptype="Variable" name="pnLPU"    src="LPU"     srctype="session"/>
    <component cmptype="Variable" name="pnHPK_ID" src="HPK_ID"  srctype="var"/>
    <component cmptype="Variable" name="r1c"      src="r1c"     srctype="var"    default="10"/>
    <component cmptype="Variable" name="r1s"      src="r1s"     srctype="var"    default="1"/>
</component>
```

### 66.5 Сравнение M2 и D3 стилей

| Элемент | M2 | D3 |
|---------|-----|-----|
| **Action** | `<component cmptype="Action">` | `<cmpAction>` |
| **ActionRouter** | `<component cmptype="ActionRouter">` | `<cmpActionRouter>` |
| **ActionVar** | `<component cmptype="ActionVar">` | `<cmpActionVar>` |
| **DataSet** | `<component cmptype="DataSet">` | `<cmpDataSet>` |
| **DataSetRouter** | `<component cmptype="DataSetRouter">` | `<cmpDataSetRouter>` |
| **Variable (в DataSet)** | `<component cmptype="Variable">` | `<cmpDataSetVar>` |
| **Grid** | `<component cmptype="Grid">` | `<cmpGrid>` |

### 66.6 Полный пример M2 формы

```xml
<div cmptype="form" oncreate="base().onCreate();" onshow="base().onShow();" window_size="750x600">
    <component cmptype="Script">
        <![CDATA[
            Form.onCreate = function() {
                setVar('HPK_ID', getVar('HPK_ID', 1));
            };

            Form.addHpkMkbs = function() {
                setVar('PRIMARY', null);
                setVar('PARENT', getVar('HPK_ID'));
                openWindow('HospitPlanning/hpk_mkbs_edit', true)
                    .addListener('onafterclose', function() {
                        if (+getVar('ModalResult') === 1) {
                            refreshDataSet('dsHpkMkbs');
                        }
                    });
            };
        ]]>
    </component>

    <component cmptype="Action" name="acAddHpkMkbs">
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            begin
              call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Выходной ID записи
                                      pnLPU         => :pnLPU,           -- ЛПУ пользователя
                                      pnPID         => :pnPID);          -- Идентификатор родительской записи
            end;
            ]]>
        </component>
        <component cmptype="ActionVar" name="pnLPU"         src="LPU"         srctype="session"/>
        <component cmptype="ActionVar" name="pnD_INSERT_ID" src="newid"       srctype="var"     put="" len="17"/>
        <component cmptype="ActionVar" name="pnPID"         src="HPK_ID"      srctype="var"/>
    </component>

    <component cmptype="DataSet" name="dsHpkMkbs" mode="Range">
        <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            select hmkb.ID,                         -- Уникальный идентификатор записи
                   hmkb.MKB,                        -- Код диагноза МКБ
                   hmkb.MKB_HANDLE,                 -- Признак ручного ввода
                   hmkb.MKB_OWN,                    -- Собственный код МКБ
                   hmkb.MAX_COUNT                   -- Максимальное количество записей
              from D_V_HPK_MKBS hmkb
             where hmkb.LPU = (:pnLPU)::numeric
               and hmkb.PID = (:pnHPK_ID)::numeric
            ]]>
        </component>
        <component cmptype="Variable" name="pnLPU"    src="LPU"     srctype="session"/>
        <component cmptype="Variable" name="pnHPK_ID" src="HPK_ID"  srctype="var"/>
        <component cmptype="Variable" name="r1c"      src="r1c"     srctype="var"    default="10"/>
        <component cmptype="Variable" name="r1s"      src="r1s"     srctype="var"    default="1"/>
    </component>

    <component cmptype="Grid" name="gridHpkMkbs" dataset="dsHpkMkbs" field="ID" selectlist="ID" grid_caption="Диагнозы">
        <component cmptype="Column" caption="Диагноз" field="MKB"/>
        <component cmptype="Column" caption="Диагноз: ручной ввод" field="MKB_HANDLE"/>
        <component cmptype="Column" caption="Ограничение записи" field="MAX_COUNT"/>
    </component>
</div>
```

### 66.7 Важные замечания для M2

1. **ActionVar** в M2 всегда имеет атрибуты `get` или `put` при необходимости
2. **Variable** в DataSet (M2) не имеет префикса `pn` в имени, но должен иметь по правилу №1
3. **ActionRouter** и **DataSetRouter** - это полноценные компоненты с атрибутом `condition`
4. В M2 **не используется** синтаксис `cmpAction`, `cmpDataSet` и т.д. - это ошибка
5. Закрывающий тэг `/>` должен быть без пробелов между последним атрибутом

### 66.8 Обработка существующей формы hosp_plan_mkbs.frm

Учитывая, что исходная форма использует:
- `<component cmptype="Action">` (не `<cmpAction>`)
- `<component cmptype="DataSet">` (не `<cmpDataSet>`)
- `<component cmptype="Variable">` (не `<cmpDataSetVar>`)

Форма **однозначно является M2**, следовательно:
- ✅ Используем `<component cmptype="Action">`
- ✅ Используем `<component cmptype="ActionRouter">`
- ✅ Используем `<component cmptype="DataSet">`
- ✅ Используем `<component cmptype="DataSetRouter">`
- ✅ Используем `<component cmptype="Variable">`
- ❌ НЕ используем `<cmpAction>`, `<cmpDataSet>`, `<cmpActionRouter>` и т.д.
# ДОПОЛНЕНИЕ ПРАВИЛА №66: СТИЛЬ КОМПОНЕНТОВ ДЛЯ D3
## Правило №66: Определение стиля компонентов на основе версии платформы (продолжение)
### 66.9 Стиль компонентов для D3
**Если форма является D3, используются следующие компоненты:**

| Тип компонента | D3 синтаксис | M2 синтаксис (запрещен) |
|----------------|--------------|------------------------|
| **Action** | `<cmpAction>` | `<component cmptype="Action">` |
| **DataSet** | `<cmpDataSet>` | `<component cmptype="DataSet">` |
| **ActionRouter** | `<cmpActionRouter>` | `<component cmptype="ActionRouter">` |
| **DataSetRouter** | `<cmpDataSetRouter>` | `<component cmptype="DataSetRouter">` |
| **ActionVar** | `<cmpActionVar>` | `<component cmptype="ActionVar">` |
| **DataSetVar** | `<cmpDataSetVar>` | `<component cmptype="Variable">` |
| **Grid** | `<cmpGrid>` | `<component cmptype="Grid">` |

### 66.10 Пример для D3 Action с Router

**Правильный D3 синтаксис:**
```xml
<!-- D3 стиль: cmpAction -->
<cmpAction name="acAddHpkMkbs">
    <cmpActionRouter condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
    <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Выходной ID записи
                                  pnLPU         => :pnLPU,           -- ЛПУ пользователя
                                  pnPID         => :pnPID);          -- Идентификатор родительской записи
        end;
        ]]>
    </cmpActionRouter>
    <cmpActionVar name="pnLPU"         src="LPU"         srctype="session"/>
    <cmpActionVar name="pnD_INSERT_ID" src="newid"       srctype="var"     put="" len="17"/>
    <cmpActionVar name="pnPID"         src="HPK_ID"      srctype="var"/>
</cmpAction>
```

### 66.11 Пример для D3 DataSet с Router

**Правильный D3 синтаксис:**
```xml
<!-- D3 стиль: cmpDataSet -->
<cmpDataSet name="dsHpkMkbs" mode="Range">
    <cmpDataSetRouter condition="TYPE_DATABASE=ORACLE">
        <![CDATA[
        select hmkb.ID,                         -- Уникальный идентификатор записи
               hmkb.MKB,                        -- Код диагноза МКБ
               hmkb.MKB_HANDLE,                 -- Признак ручного ввода
               hmkb.MKB_OWN,                    -- Собственный код МКБ
               hmkb.MAX_COUNT                   -- Максимальное количество записей
          from D_V_HPK_MKBS hmkb
         where hmkb.LPU = to_number(:pnLPU)
           and hmkb.PID = to_number(:pnHPK_ID)
        ]]>
    </cmpDataSetRouter>
    <cmpDataSetRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        select hmkb.ID,
               hmkb.MKB,
               hmkb.MKB_HANDLE,
               hmkb.MKB_OWN,
               hmkb.MAX_COUNT
          from D_V_HPK_MKBS hmkb
         where hmkb.LPU = (:pnLPU)::numeric
           and hmkb.PID = (:pnHPK_ID)::numeric
        ]]>
    </cmpDataSetRouter>
    <cmpDataSetVar name="pnLPU"    src="LPU"     srctype="session"/>
    <cmpDataSetVar name="pnHPK_ID" src="HPK_ID"  srctype="var"/>
    <cmpDataSetVar name="r1c"      src="r1c"     srctype="var"    default="10"/>
    <cmpDataSetVar name="r1s"      src="r1s"     srctype="var"    default="1"/>
</cmpDataSet>
```

### 66.12 Полный пример D3 формы

```xml
<div cmptype="form" oncreate="base().onCreate();" onshow="base().onShow();" window_size="750x600">
    <component cmptype="Script">
        <![CDATA[
            Form.onCreate = function() {
                setVar('HPK_ID', getVar('HPK_ID', 1));
            };

            Form.addHpkMkbs = function() {
                setVar('PRIMARY', null);
                setVar('PARENT', getVar('HPK_ID'));
                openWindow('HospitPlanning/hpk_mkbs_edit', true)
                    .addListener('onafterclose', function() {
                        if (+getVar('ModalResult') === 1) {
                            refreshDataSet('dsHpkMkbs');
                        }
                    });
            };
        ]]>
    </component>

    <cmpAction name="acAddHpkMkbs">
        <cmpActionRouter condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
        <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            begin
              call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Выходной ID записи
                                      pnLPU         => :pnLPU,           -- ЛПУ пользователя
                                      pnPID         => :pnPID);          -- Идентификатор родительской записи
            end;
            ]]>
        </cmpActionRouter>
        <cmpActionVar name="pnLPU"         src="LPU"         srctype="session"/>
        <cmpActionVar name="pnD_INSERT_ID" src="newid"       srctype="var"     put="" len="17"/>
        <cmpActionVar name="pnPID"         src="HPK_ID"      srctype="var"/>
    </cmpAction>

    <cmpAction name="acDeleteHpkMkbs">
        <cmpActionRouter condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="DELETE"/>
        <cmpActionRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            begin
              call D_PKG_HPK_MKBS.DEL(pnID  => :pnID,    -- Идентификатор удаляемой записи
                                      pnLPU => :pnLPU);  -- ЛПУ пользователя
            end;
            ]]>
        </cmpActionRouter>
        <cmpActionVar name="pnLPU" src="LPU"         srctype="session"/>
        <cmpActionVar name="pnID"  src="HPK_MKBS"    srctype="var"/>
    </cmpAction>

    <cmpDataSet name="dsHpkMkbs" mode="Range">
        <cmpDataSetRouter condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            select hmkb.ID,                         -- Уникальный идентификатор записи
                   hmkb.MKB,                        -- Код диагноза МКБ
                   hmkb.MKB_HANDLE,                 -- Признак ручного ввода
                   hmkb.MKB_OWN,                    -- Собственный код МКБ
                   hmkb.MAX_COUNT                   -- Максимальное количество записей
              from D_V_HPK_MKBS hmkb
             where hmkb.LPU = (:pnLPU)::numeric
               and hmkb.PID = (:pnHPK_ID)::numeric
            ]]>
        </cmpDataSetRouter>
        <cmpDataSetVar name="pnLPU"    src="LPU"     srctype="session"/>
        <cmpDataSetVar name="pnHPK_ID" src="HPK_ID"  srctype="var"/>
        <cmpDataSetVar name="r1c"      src="r1c"     srctype="var"    default="10"/>
        <cmpDataSetVar name="r1s"      src="r1s"     srctype="var"    default="1"/>
    </cmpDataSet>

    <cmpGrid name="gridHpkMkbs" dataset="dsHpkMkbs" field="ID" selectlist="ID" grid_caption="Диагнозы">
        <component cmptype="Column" caption="Диагноз" field="MKB"/>
        <component cmptype="Column" caption="Диагноз: ручной ввод" field="MKB_HANDLE"/>
        <component cmptype="Column" caption="Ограничение записи" field="MAX_COUNT"/>
        <component cmptype="GridFooter" separate="false">
            <component count="10" cmptype="Range" varstart="r1s" varcount="r1c" valuecount="10" valuestart="1"/>
        </component>
    </cmpGrid>
</div>
```

### 66.13 Дополнительные особенности D3


### 66.14 Сравнительная таблица M2 vs D3

| Характеристика | M2 | D3 |
|----------------|-----|-----|
| **Action** | `<component cmptype="Action">` | `<cmpAction>` |
| **DataSet** | `<component cmptype="DataSet">` | `<cmpDataSet>` |
| **Router Action** | `<component cmptype="ActionRouter">` | `<cmpActionRouter>` |
| **Router DataSet** | `<component cmptype="DataSetRouter">` | `<cmpDataSetRouter>` |
| **Параметры Action** | `<component cmptype="ActionVar">` | `<cmpActionVar>` |
| **Параметры DataSet** | `<component cmptype="Variable">` | `<cmpDataSetVar>` |
| **Grid** | `<component cmptype="Grid">` | `<cmpGrid>` |
| **Поддержка GraphQL** | Нет | Да |
| **Async/await** | Нет (колбэки) | Да |
| **Реактивность** | Нет | Да |
| **Service вызовы** | Нет | Да |
| **Жизненный цикл** | onCreate, onShow, onClose | onInit, onMount, onRender, onDestroy |

### 66.15 Определение версии формы (алгоритм)

```javascript
function determineFormVersion(formCode) {
    // Признаки M2
    const m2Patterns = [
        /<component\s+cmptype="Action"\s+name=/,
        /<component\s+cmptype="DataSet"\s+name=/,
        /<component\s+cmptype="ActionRouter"/,
        /<component\s+cmptype="DataSetRouter"/,
        /<component\s+cmptype="Variable"\s+name=/
    ];

    // Признаки D3
    const d3Patterns = [
        /<cmpAction\s+name=/,
        /<cmpDataSet\s+name=/,
        /<cmpActionRouter\s+condition=/,
        /<cmpDataSetRouter\s+condition=/,
        /<cmpDataSetVar\s+name=/
    ];

    let m2Score = m2Patterns.filter(pattern => pattern.test(formCode)).length;
    let d3Score = d3Patterns.filter(pattern => pattern.test(formCode)).length;

    if (m2Score > d3Score) return 'M2';
    if (d3Score > m2Score) return 'D3';

    // По умолчанию или по контексту
    return 'M2'; // или 'D3' в зависимости от контекста
}
```

### 66.16 Важные замечания для D3

1. **cmpActionVar** в D3 использует атрибуты `get` и `put` аналогично M2
2. **cmpDataSetVar** в D3 имеет префикс `pn` в имени (согласно правилу №1)
3. **cmpActionRouter** и **cmpDataSetRouter** - это сокращенная форма компонентов
4. В D3 **не используется** синтаксис `<component cmptype="...">` для Action и DataSet - это ошибка
5. Закрывающий тэг `/>` должен быть без пробелов между последним атрибутом
6. D3 поддерживает смешанный стиль: для Grid можно использовать как `<cmpGrid>`, так и `<component cmptype="Grid">` (обратная совместимость)



## Исправление правила №67 - Блок комментария формы

Вы абсолютно правы! Форма в стиле M2 не может открываться методом D3, и наоборот. Блок комментария должен содержать **только один пример вызова**, соответствующий стилю самой формы.

### Правило №67 (исправленное): Добавление блока документации в начало формы

#### 67.1 Определение стиля формы для примера вызова

**Для M2 формы** - пример вызова использует `openWindow()`
**Для D3 формы** - пример вызова использует `openD3Form()`

#### 67.2 Структура блока комментария (упрощенная)

```xml
<!--
    ========================================================================
    НАЗВАНИЕ ФОРМЫ: <Имя файла формы>
    ОПИСАНИЕ: <Краткое описание назначения формы>

    ВХОДНЫЕ ПАРАМЕТРЫ:
    - <Имя переменной> : <Тип> - <Описание>

    ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
    - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
    - return_id : number - Идентификатор добавленной/измененной записи (при наличии)

    ПРИМЕР ВЫЗОВА:
    <Пример вызова в стиле формы (M2 или D3)>
    ========================================================================
-->
```

#### 67.3 Пример для M2 формы

```xml
<!--
    ========================================================================
    НАЗВАНИЕ ФОРМЫ: HospitPlanning/hosp_plan_mkbs.frm
    ОПИСАНИЕ: Форма редактирования диагнозов (МКБ) для видов планов госпитализации

    ВХОДНЫЕ ПАРАМЕТРЫ:
    - HPK_ID : number - Идентификатор вида плана госпитализации (HPK)

    ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
    - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
    - return_id : number - Идентификатор добавленной/измененной записи (при сохранении)

    ПРИМЕР ВЫЗОВА:
    openWindow({
        name: 'HospitPlanning/hosp_plan_mkbs',
        vars: {
            HPK_ID: getVar('HPK_ID')
        }
    }, true, 750, 600)
        .addListener('onafterclose', function() {
            if (getVar('ModalResult') == 1) {
                refreshDataSet('dsHpkList');
            }
        });
    ========================================================================
-->
```

#### 67.4 Пример для D3 формы

```xml
<!--
    ========================================================================
    НАЗВАНИЕ ФОРМЫ: HospitPlanning/hosp_plan_mkbs.frm
    ОПИСАНИЕ: Форма редактирования диагнозов (МКБ) для видов планов госпитализации

    ВХОДНЫЕ ПАРАМЕТРЫ:
    - HPK_ID : number - Идентификатор вида плана госпитализации (HPK)

    ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
    - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
    - return_id : number - Идентификатор добавленной/измененной записи (при сохранении)

    ПРИМЕР ВЫЗОВА:
    openD3Form('HospitPlanning/hosp_plan_mkbs', true, {
        width: '750px',
        height: '600px',
        vars: {
            HPK_ID: getVar('HPK_ID')
        },
        onclose: (mod) => {
            if (+mod?.ModalResult !== 1) return;
            refreshDataSet('dsHpkList');
        }
    });
    ========================================================================
-->
```

#### 67.5 Алгоритм определения стиля формы

```javascript
function getFormStyle(formCode) {
    // Признаки M2 формы
    const m2Patterns = [
        /<component\s+cmptype="Action"\s+name=/,
        /<component\s+cmptype="DataSet"\s+name=/,
        /<component\s+cmptype="ActionRouter"/,
        /<component\s+cmptype="DataSetRouter"/,
        /<component\s+cmptype="Variable"\s+name=/
    ];

    // Признаки D3 формы
    const d3Patterns = [
        /<cmpAction\s+name=/,
        /<cmpDataSet\s+name=/,
        /<cmpActionRouter\s+condition=/,
        /<cmpDataSetRouter\s+condition=/,
        /<cmpDataSetVar\s+name=/
    ];

    let m2Score = m2Patterns.filter(pattern => pattern.test(formCode)).length;
    let d3Score = d3Patterns.filter(pattern => pattern.test(formCode)).length;

    if (m2Score > d3Score) return 'M2';
    if (d3Score > m2Score) return 'D3';

    // Если не удалось определить - по умолчанию M2 (как в исходной задаче)
    return 'M2';
}
```

#### 67.6 Полный пример M2 формы с блоком комментария

```xml
<div cmptype="form" oncreate="base().onCreate();" onshow="base().onShow();" window_size="750x600">
    <!--
        ========================================================================
        НАЗВАНИЕ ФОРМЫ: HospitPlanning/hosp_plan_mkbs.frm
        ОПИСАНИЕ: Форма редактирования диагнозов (МКБ) для видов планов госпитализации

        ВХОДНЫЕ ПАРАМЕТРЫ:
        - HPK_ID : number - Идентификатор вида плана госпитализации (HPK)

        ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
        - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
        - return_id : number - Идентификатор добавленной/измененной записи (при сохранении)

        ПРИМЕР ВЫЗОВА:
        openWindow({
            name: 'HospitPlanning/hosp_plan_mkbs',
            vars: {
                HPK_ID: getVar('HPK_ID')
            }
        }, true, 750, 600)
            .addListener('onafterclose', function() {
                if (getVar('ModalResult') == 1) {
                    refreshDataSet('dsHpkList');
                }
            });
        ========================================================================
    -->
    <!-- ... остальной код формы ... -->
</div>
```

#### 67.7 Шаблоны для быстрого добавления

**Для M2 формы:**
```xml
<!--
    ========================================================================
    НАЗВАНИЕ ФОРМЫ: <Путь к форме>
    ОПИСАНИЕ: <Описание формы>

    ВХОДНЫЕ ПАРАМЕТРЫ:
    - <VAR_NAME> : <type> - <description>

    ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
    - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
    - return_id : number - Идентификатор добавленной/измененной записи (при наличии)

    ПРИМЕР ВЫЗОВА:
    openWindow({
        name: '<Путь к форме>',
        vars: {
            <VAR_NAME>: getVar('<VAR_NAME>')
        }
    }, true, <width>, <height>)
        .addListener('onafterclose', function() {
            if (getVar('ModalResult') == 1) {
                // Действия после закрытия
            }
        });
    ========================================================================
-->
```

**Для D3 формы:**
```xml
<!--
    ========================================================================
    НАЗВАНИЕ ФОРМЫ: <Путь к форме>
    ОПИСАНИЕ: <Описание формы>

    ВХОДНЫЕ ПАРАМЕТРЫ:
    - <VAR_NAME> : <type> - <description>

    ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ:
    - ModalResult : number - Результат модального окна (1 - сохранено, 0 - отменено)
    - return_id : number - Идентификатор добавленной/измененной записи (при наличии)

    ПРИМЕР ВЫЗОВА:
    openD3Form('<Путь к форме>', true, {
        width: '<width>px',
        height: '<height>px',
        vars: {
            <VAR_NAME>: getVar('<VAR_NAME>')
        },
        onclose: (mod) => {
            if (+mod?.ModalResult !== 1) return;
            // Действия после закрытия
        }
    });
    ========================================================================
-->
```

#### 67.8 Важные замечания

1. **Единый стиль:** Пример вызова должен строго соответствовать стилю формы (M2 или D3)
2. **Не смешивать:** Нельзя указывать оба примера или пример для другого стиля
3. **Обязательность:** Блок комментария добавляется в каждую форму
4. **Позиция:** Сразу после открывающего тега `<div cmptype="form">`
5. **Актуальность:** Параметры должны соответствовать фактической логике формы
6. **Язык:** Комментарии на русском языке

## Дополнение правил: Правило №68 - Форматирование кода в компоненте Script

### Правило №68: Отступы в компоненте Script

Весь JavaScript код внутри компонента `<component cmptype="Script">` должен быть выровнен относительно открывающего тега `<![CDATA[` без дополнительных пробелов в начале строк. Т.е. код начинается сразу после перевода строки после `<![CDATA[`.

#### 68.1 Правильное форматирование

**Правильно (без лишних отступов):**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('pnHPK_ID', getVar('HPK_ID', 1));
    };

    Form.onShow = function() {
        refreshDataSet('dsHpkMkbs');
    };
    ]]>
</component>
```

**Неправильно (с лишними отступами):**
```xml
<component cmptype="Script">
    <![CDATA[
                Form.onCreate = function() {
                    setVar('pnHPK_ID', getVar('HPK_ID', 1));
                };
            ]]>
</component>
```

#### 68.2 Детальные правила форматирования

1. **Отступ для `<![CDATA[`:** Должен быть на одном уровне с открывающим тегом `<component cmptype="Script">` (обычно 4 пробела)
2. **Отсутствие отступа для первой строки кода:** Первая строка JavaScript кода не должна иметь дополнительного отступа относительно `<![CDATA[`
3. **Внутренние отступы в JS:** Внутри JavaScript кода сохраняются стандартные отступы (4 пробела для вложенных блоков)
4. **Закрывающий `]]>`:** Должен быть на одной линии с последней строкой кода (без дополнительных отступов)

#### 68.3 Примеры форматирования

**Пример 1: Простая форма**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('HPK_ID', getVar('HPK_ID', 1));
    };

    Form.addRecord = function() {
        setVar('PRIMARY', null);
        setVar('PARENT', getVar('HPK_ID'));
        openWindow('HospitPlanning/hpk_mkbs_edit', true)
            .addListener('onafterclose', function() {
                if (+getVar('ModalResult') === 1) {
                    refreshDataSet('dsHpkMkbs');
                }
            });
    };
    ]]>
</component>
```

**Пример 2: С вложенными конструкциями**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.validate = function() {
        if (empty(getValue('ctrlCode'))) {
            message('ERROR', 'Не заполнен код');
            return false;
        }

        if (getValue('ctrlCount') < 0) {
            message('ERROR', 'Количество не может быть отрицательным');
            return false;
        }

        return true;
    };

    Form.onShow = function() {
        if (getVar('action') === 'INSERT') {
            setValue('ctrlDate', new Date());
        }
    };
    ]]>
</component>
```

**Пример 3: С большим количеством функций**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('pnLPU', getVar('LPU'));
        setVar('pnHPK_ID', getVar('HPK_ID', 1));
    };

    Form.onShow = function() {
        refreshDataSet('dsHpkMkbs');
    };

    Form.addHpkMkbs = function() {
        setVar('PRIMARY', null);
        setVar('PARENT', getVar('HPK_ID'));
        openWindow('HospitPlanning/hpk_mkbs_edit', true)
            .addListener('onafterclose', function() {
                if (+getVar('ModalResult') === 1) {
                    refreshDataSet('dsHpkMkbs');
                }
            });
    };

    Form.editHpkMkbs = function() {
        if (empty(getValue('ctrlHpkMkbs'))) {
            message('WARNING', 'Не выбрана запись для редактирования');
            return;
        }
        setVar('PRIMARY', getValue('ctrlHpkMkbs'));
        setVar('PARENT', getVar('HPK_ID'));
        openWindow('HospitPlanning/hpk_mkbs_edit', true)
            .addListener('onafterclose', function() {
                if (+getVar('ModalResult') === 1) {
                    refreshDataSet('dsHpkMkbs');
                }
            });
    };

    Form.delHpkMkbs = function() {
        if (!confirm('Вы действительно хотите удалить запись(и)?')) {
            return;
        }

        var selectList = getValue('ctrlHpkMkbsSelectList');
        if (empty(selectList)) {
            setVar('pnHPK_MKBS', getValue('ctrlHpkMkbs'));
        } else {
            setVar('pnHPK_MKBS', selectList);
        }

        executeAction('acDeleteHpkMkbs', function() {
            refreshDataSet('dsHpkMkbs');
            setValue('ctrlHpkMkbsSelectList', null);
        });
    };
    ]]>
</component>
```

#### 68.4 Сравнение правильного и неправильного форматирования

| Элемент | Правильно | Неправильно |
|---------|-----------|--------------|
| **Отступ перед `<![CDATA[`** | 4 пробела | 4 пробела |
| **Отступ первой строки JS** | 0 пробелов (относительно CDATA) | 8-16 пробелов |
| **Отступ вложенных блоков JS** | 4 пробела | 8-20 пробелов |
| **Отступ перед `]]>`** | 0 пробелов | 8-16 пробелов |

#### 68.5 Визуальное представление

**Правильная структура отступов:**
```
[4 пробела]<component cmptype="Script">
[4 пробела]    <![CDATA[
[4 пробела]    Form.onCreate = function() {
[4 пробела]        // код с отступом 4 пробела
[4 пробела]    };
[4 пробела]    ]]>
[4 пробела]</component>
```

**Неправильная структура отступов:**
```
[4 пробела]<component cmptype="Script">
[4 пробела]    <![CDATA[
[8 пробелов]        Form.onCreate = function() {
[12 пробелов]            // слишком большой отступ
[8 пробелов]        };
[8 пробелов]    ]]>
[4 пробела]</component>
```

#### 68.6 Исключения

Правило не применяется к:
1. **SQL запросам** в `cmpDataSetRouter` и `cmpActionRouter` (там свои правила отступов)
2. **Атрибутам событий** (onclick, onchange и т.д.) внутри HTML-подобных тегов

#### 68.7 Применение к форме hosp_plan_mkbs.frm

**Исходный код с ошибкой:**
```xml
<component cmptype="Script">
    <![CDATA[
                Form.onCreate = function()
                {
                    setVar('HPK_ID', getVar('HPK_ID', 1));
                }

                Form.onShow = function()
                {

                }
    ]]>
</component>
```

**Исправленный код:**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('pnHPK_ID', getVar('HPK_ID', 1));
    };

    Form.onShow = function() {
        // код выполняется при показе формы
    };
    ]]>
</component>
```

#### 68.8 Автоматическое применение правила

При переработке формы необходимо:
1. Удалить все лишние пробелы в начале каждой строки JavaScript кода
2. Оставить только стандартные отступы внутри JS кода (4 пробела)
3. Убедиться, что первая строка JS кода начинается сразу после перевода строки после `<![CDATA[`
4. Закрывающий `]]>` должен быть на отдельной строке без пробелов в начале

## Правило №69: Отступы внутри блоков $$() и _$$()

### Правило №69: Форматирование кода между $$() и _$$()

Весь код, расположенный между вызовами `$$(clone);` и `_$$();`, должен иметь отступ **на 4 пробела больше**, чем окружающий код.

#### Пример:

**Неправильно:**
```javascript
$$(clone);
setValue('checkIsActive', data.IS_ACTIVE);
setValue('checkIsPrior', data.IS_PRIORITY);
_$$();
```

**Правильно:**
```javascript
$$(clone);
    setValue('checkIsActive', data.IS_ACTIVE);
    setValue('checkIsPrior', data.IS_PRIORITY);
_$$();
```

#### Общее правило:
- `$$(clone);` - без дополнительного отступа
- Внутренний код - отступ **+4 пробела** относительно внешнего блока
- `_$$();` - без дополнительного отступа (на одном уровне с `$$(clone);`)


## Правило №70: Вызов процедур в PostgreSQL с использованием CALL

### Правило №70: Использование CALL для вызова процедур в PostgreSQL

В блоках `cmpActionRouter` для PostgreSQL необходимо использовать ключевое слово `CALL` для вызова хранимых процедур (функций, возвращающих void).

#### 70.1 Синтаксис вызова

**Правильный синтаксис PostgreSQL:**
```sql
CALL schema_name.procedure_name(parameter1 => value1, parameter2 => value2);
```

**Неправильный синтаксис (Oracle-style):**
```sql
-- Не использовать SELECT для вызова процедур
SELECT schema_name.procedure_name(parameter1, parameter2);

-- Не использовать EXEC
EXEC schema_name.procedure_name(parameter1, parameter2);

-- Не использовать BEGIN ... END без CALL
BEGIN
    schema_name.procedure_name(parameter1, parameter2);
END;
```

#### 70.2 Пример для Action (PostgreSQL)

**Правильно:**
```xml
<component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
    <![CDATA[
    begin
      -- PostgreSQL: вызов процедуры добавления записи
      call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Выходной ID записи
                              pnLPU         => :pnLPU,           -- ЛПУ пользователя
                              pnPID         => :pnPID);          -- Идентификатор родителя
    end;
    ]]>
</component>
```

**Неправильно:**
```xml
<component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
    <![CDATA[
    begin
      -- Ошибка: SELECT вместо CALL
      select D_PKG_HPK_MKBS.ADD(:pnD_INSERT_ID, :pnLPU, :pnPID);
    end;
    ]]>
</component>
```

#### 70.3 Полный пример Action с двумя маршрутами

```xml
<component cmptype="Action" name="acAddHpkMkbs">
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="INSERT"/>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          -- PostgreSQL: вызов процедуры добавления
          call D_PKG_HPK_MKBS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- ID новой записи
                                  pnLPU         => :pnLPU,           -- ЛПУ пользователя
                                  pnPID         => :pnPID,           -- ID родителя
                                  pnMKB         => :pnMKB,           -- Код МКБ
                                  psMKB_HANDLE  => :psMKB_HANDLE,    -- Признак ручного ввода
                                  pnMKB_OWN     => :pnMKB_OWN,       -- Собственный код МКБ
                                  pnMAX_COUNT   => :pnMAX_COUNT);    -- Максимальное количество
        end;
        ]]>
    </component>
    <component cmptype="ActionVar" name="pnLPU"         src="LPU"         srctype="session"/>
    <component cmptype="ActionVar" name="pnD_INSERT_ID" src="newid"       srctype="var" put="" len="17"/>
    <component cmptype="ActionVar" name="pnPID"         src="HPK_ID"      srctype="var"/>
    <component cmptype="ActionVar" name="pnMKB"         src="MKB"         srctype="var"/>
    <component cmptype="ActionVar" name="psMKB_HANDLE"  src="MKB_HANDLE"  srctype="var"/>
    <component cmptype="ActionVar" name="pnMKB_OWN"     src="MKB_OWN"     srctype="var"/>
    <component cmptype="ActionVar" name="pnMAX_COUNT"   src="MAX_COUNT"   srctype="var"/>
</component>
```

#### 70.4 Пример для DELETE операции

```xml
<component cmptype="Action" name="acDeleteHpkMkbs">
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="DELETE"/>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          -- PostgreSQL: вызов процедуры удаления
          call D_PKG_HPK_MKBS.DEL(pnID  => :pnID,    -- ID удаляемой записи
                                  pnLPU => :pnLPU);  -- ЛПУ пользователя
        end;
        ]]>
    </component>
    <component cmptype="ActionVar" name="pnLPU" src="LPU"     srctype="session"/>
    <component cmptype="ActionVar" name="pnID"  src="HPK_ID"  srctype="var"/>
</component>
```

#### 70.5 Основные требования

1. **Обязательное использование `CALL`** - для всех вызовов процедур в PostgreSQL
2. **Именованные параметры** - всегда использовать синтаксис `name => value`
3. **Блок `begin ... end`** - обязательный для нескольких операторов
4. **Точка с запятой** - обязательная в конце каждого оператора и после `end`
5. **Комментарии** - для каждого параметра после `--`

#### 70.6 Сравнение Oracle и PostgreSQL

| Элемент | Oracle | PostgreSQL |
|---------|--------|------------|
| **Ключевое слово** | `BEGIN` (без CALL) | `CALL` |
| **Синтаксис** | `procedure_name(params);` | `CALL procedure_name(params);` |
| **Пакеты** | `PKG_NAME.PROC_NAME` | `PKG_NAME.PROC_NAME` |
| **Параметры** | Именованные или позиционные | Именованные (рекомендуется) |

#### 70.7 Ошибки, которых следует избегать

```sql
-- Ошибка 1: Отсутствие CALL
begin
  D_PKG_HPK_MKBS.ADD(:pnID, :pnLPU);
end;

-- Ошибка 2: Использование SELECT
begin
  SELECT D_PKG_HPK_MKBS.ADD(:pnID, :pnLPU);
end;

-- Ошибка 3: Позиционные параметры (не рекомендуется)
begin
  call D_PKG_HPK_MKBS.ADD(:pnID, :pnLPU, :pnPID);
end;

-- Ошибка 4: Отсутствие блока begin...end для нескольких операторов
call D_PKG_HPK_MKBS.ADD(:pnID, :pnLPU);
call D_PKG_HPK_MKBS.LOG(:pnID);  -- Без begin...end будет ошибка
```

#### 70.8 Шаблон для быстрого использования

```xml
<component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
    <![CDATA[
    begin
      -- PostgreSQL: комментарий действия
      call SCHEMA.PROCEDURE_NAME(pnPARAM1 => :pnPARAM1,   -- Комментарий параметра 1
                                 pnPARAM2 => :pnPARAM2,   -- Комментарий параметра 2
                                 psPARAM3 => :psPARAM3);  -- Комментарий параметра 3
    end;
    ]]>
</component>
```
## Правило №71: Сохранение имен переменных в JS и атрибутах src

### Правило №71: Не переименовывать переменные в JS и атрибутах src

При переработке формы **запрещается** изменять имена переменных, которые используются в:
1. JavaScript коде (`setVar`, `getVar`, `getValue`, `setValue`)
2. Атрибутах `src` компонентов (`ActionVar`, `Variable`)

**Менять можно только** атрибуты `name` в компонентах (`Action`, `DataSet`, `ActionVar`, `Variable`).

#### 71.1 Что НЕЛЬЗЯ менять

**JavaScript переменные:**
```javascript
// НЕ МЕНЯТЬ - имена переменных в JS
setVar('HPK_ID', getVar('HPK_ID', 1));
setVar('PLAN_DAY_ENG', data.PLAN_DAY);
getVar('ModalResult');
getValue('ctrlHpkMkbs');
```

**Атрибуты src:**
```xml
<!-- НЕ МЕНЯТЬ - значения атрибутов src -->
<component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
<component cmptype="ActionVar" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
<component cmptype="Variable" name="pnLPU" src="LPU" srctype="session"/>
```

#### 71.2 Что МОЖНО менять

**Атрибуты name в компонентах:**
```xml
<!-- МОЖНО МЕНЯТЬ - name компонентов -->
<!-- Было -->
<component cmptype="Action" name="addHpkMkbs">
<component cmptype="DataSet" name="DS_HPK_MKBS">
<component cmptype="ActionVar" name="~pnID">

<!-- Стало -->
<component cmptype="Action" name="acAddHpkMkbs">
<component cmptype="DataSet" name="dsHpkMkbs">
<component cmptype="ActionVar" name="~pnID">  <!-- ~ сохраняется -->
```

#### 71.3 Пример правильной переработки

**Исходный код:**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('HPK_ID', getVar('HPK_ID', 1));
    }

    Form.delHpkMkbs = function() {
        if(confirm('Вы действительно хотите удалить запись(и)?')) {
            if(empty(getValue('HPK_MKBS_SelectList')))
                setVar('HPK_MKBS', getValue('HPK_MKBS'));
            else
                setVar('HPK_MKBS', getValue('HPK_MKBS_SelectList'));
            executeAction('deleteHpkMkbs', function(){refreshDataSet('DS_HPK_MKBS');});
        }
    }
    ]]>
</component>

<component cmptype="Action" name="deleteHpkMkbs" unit="HPK_MKBS" action="DELETE">
    <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="ActionVar" name="~pnID" src="HPK_MKBS" srctype="var" get="v1"/>
</component>

<component cmptype="DataSet" name="DS_HPK_MKBS">
    <![CDATA[
    select t.ID from d_v_hpk_mkbs t where t.LPU = :LPU and t.PID = :HPK
    ]]>
    <component cmptype="Variable" name="LPU" src="LPU" srctype="session"/>
    <component cmptype="Variable" name="HPK" src="HPK_ID" srctype="var"/>
</component>
```

**Правильно переработанный код (JS переменные и src сохранены):**
```xml
<component cmptype="Script">
    <![CDATA[
    Form.onCreate = function() {
        setVar('HPK_ID', getVar('HPK_ID', 1));  // Переменная HPK_ID сохранена
    };

    Form.delHpkMkbs = function() {
        if (confirm('Вы действительно хотите удалить запись(и)?')) {
            if (empty(getValue('HPK_MKBS_SelectList'))) {  // Переменная сохранена
                setVar('HPK_MKBS', getValue('HPK_MKBS'));   // Переменная сохранена
            } else {
                setVar('HPK_MKBS', getValue('HPK_MKBS_SelectList')); // Переменная сохранена
            }
            executeAction('acDeleteHpkMkbs', function() {
                refreshDataSet('dsHpkMkbs');
            });
        }
    };
    ]]>
</component>

<component cmptype="Action" name="acDeleteHpkMkbs">  <!-- name изменен -->
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_MKBS" action="DELETE"/>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
            call D_PKG_HPK_MKBS.DEL(pnID  => :pnID,    -- ID записи
                                    pnLPU => :pnLPU);  -- ЛПУ пользователя
        end;
        ]]>
    </component>
    <!-- src значения сохранены: LPU, HPK_MKBS -->
    <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="ActionVar" name="~pnID" src="HPK_MKBS" srctype="var"/>  <!-- ~ сохранен, get удален -->
</component>

<component cmptype="DataSet" name="dsHpkMkbs" mode="Range">  <!-- name изменен -->
    <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        -- src значения сохранены: LPU, HPK_ID
        select hmkb.ID
          from D_V_HPK_MKBS hmkb
         where hmkb.LPU = (:pnLPU)::numeric
           and hmkb.PID = (:pnHPK_ID)::numeric
        ]]>
    </component>
    <!-- src значения сохранены: LPU, HPK_ID -->
    <component cmptype="Variable" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
</component>
```
## Правило №71 (дополнение): Сохранение имен переменных в JS и атрибутах src

### 71.6 Особые требования для переменных с srctype="var"

**КРИТИЧЕСКОЕ ПРАВИЛО:** Переменные, которые имеют `srctype="var"` в компонентах `ActionVar` или `Variable`, **ЗАПРЕЩЕНО переименовывать** как в JS коде, так и в атрибутах `src`.

Это связано с тем, что такие переменные используются для передачи данных между:
- Различными Action компонентами на форме
- Родительскими и дочерними формами (через openWindow)
- Мастер-детейл связями между DataSet

**Пример НЕПРАВИЛЬНОГО переименования:**
```xml
<!-- Было (исходный код) -->
<component cmptype="Variable" name="LPU" src="LPU" srctype="session"/>
<component cmptype="Variable" name="HPK_ID" src="HPK_ID" srctype="var"/>

<!-- НЕЛЬЗЯ ТАК ДЕЛАТЬ! Переименована переменная с srctype="var" -->
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID_CHANGED" srctype="var"/>
```

**Пример ПРАВИЛЬНОГО подхода:**
```xml
<!-- Было -->
<component cmptype="Variable" name="HPK_ID" src="HPK_ID" srctype="var"/>

<!-- Можно изменить только name, но src ОСТАЕТСЯ неизменным -->
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
```

### 71.7 Иерархия приоритетов для переименования

| Тип переменной | Можно переименовывать JS | Можно менять src | Приоритет |
|----------------|-------------------------|------------------|-----------|
| `srctype="session"` | ✅ Да | ✅ Да (если соответствует сессии) | Низкий |
| `srctype="ctrl"` | ✅ Да | ❌ Нет (привязана к контролу) | Средний |
| `srctype="var"` | ❌ **НЕТ** | ❌ **НЕТ** | **ВЫСОКИЙ** |
| `srctype="data"` | ❌ **НЕТ** | ❌ **НЕТ** | **ВЫСОКИЙ** |

### 71.8 Почему нельзя переименовывать переменные с srctype="var"

1. **Межкомпонентные связи:** Переменные с `srctype="var"` используются для передачи данных между Action компонентами через `getVar`/`setVar`

2. **Родительско-дочерние формы:** При вызове `openWindow` переменные передаются по имени:
   ```javascript
   // В вызывающей форме
   openWindow('HospitPlanning/hosp_plan_mkbs', true, {
       vars: {
           HPK_ID: getValue('HOSP_PLAN_KINDS')  // Имя переменной важно!
       }
   });
   ```

3. **Мастер-детейл связи:** DataSet используют переменные для фильтрации:
   ```xml
   <!-- Детейл DataSet ожидает переменную с ТОЧНЫМ именем -->
   <component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
   ```

### 71.9 Разрешенные изменения для srctype="var"

**Можно изменять ТОЛЬКО атрибут `name` компонента:**
```xml
<!-- Разрешено -->
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
<!-- Было name="HPK_ID", стало name="pnHPK_ID" - src НЕ ИЗМЕНИЛСЯ -->
```

**Запрещено изменять:**
- Атрибут `src` у переменной с `srctype="var"`
- Имя переменной в `getVar()`, `setVar()`, `getValue()`, `setValue()`
- Имя переменной в атрибуте `src` у `ActionVar` или `Variable`

### 71.10 Пример правильной переработки формы

**Исходный код:**
```xml
<component cmptype="Action" name="showMkbsHospPlanKind">
    <component cmptype="ActionVar" name="HPK_ID" src="HPK_ID" srctype="var"/>
</component>

<component cmptype="DataSet" name="DS_HPK_MKBS">
    <component cmptype="Variable" name="HPK_ID" src="HPK_ID" srctype="var"/>
</component>

<component cmptype="Script">
    <![CDATA[
    Form.showMkbsHospPlanKind = function() {
        setVar('HPK_ID', getValue('HOSP_PLAN_KINDS'));
        openWindow('HospitPlanning/hosp_plan_mkbs', true);
    };
    ]]>
</component>
```

**Правильно переработанный код:**
```xml
<component cmptype="Action" name="acShowMkbsHospPlanKind">
    <!-- name изменен на acShowMkbsHospPlanKind, но src ОСТАЛСЯ HPK_ID -->
    <component cmptype="ActionVar" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
</component>

<component cmptype="DataSet" name="dsHpkMkbs">
    <!-- name изменен на pnHPK_ID, но src ОСТАЛСЯ HPK_ID -->
    <component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
</component>

<component cmptype="Script">
    <![CDATA[
    Form.showMkbsHospPlanKind = function() {
        // Имя переменной в JS НЕ ИЗМЕНЯЕТСЯ
        setVar('HPK_ID', getValue('HOSP_PLAN_KINDS'));
        openWindow('HospitPlanning/hosp_plan_mkbs', true);
    };
    ]]>
</component>
```

### 71.11 Проверочный список (дополненный)

При переработке формы необходимо проверить:

- [ ] Все `getVar('XXX')` остались с исходными `XXX`
- [ ] Все `setVar('XXX')` остались с исходными `XXX`
- [ ] Все `getValue('XXX')` остались с исходными `XXX`
- [ ] Все `setValue('XXX')` остались с исходными `XXX`
- [ ] Все `src="XXX"` в `ActionVar` остались с исходными `XXX`
- [ ] Все `src="XXX"` в `Variable` остались с исходными `XXX`
- [ ] **Для `srctype="var"` и `srctype="data"` - src НЕ ИЗМЕНЯЕТСЯ НИКОГДА**
- [ ] **Для `srctype="var"` и `srctype="data"` - имена в JS НЕ ИЗМЕНЯЮТСЯ**
- [ ] Изменены только атрибуты `name` компонентов
- [ ] Символ `~` в начале имени переменной сохранен

### 71.12 Типичные ошибки при переработке

**ОШИБКА 1: Переименование src у var переменной**
```xml
<!-- ОШИБКА! Нельзя менять src -->
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID_NEW" srctype="var"/>
```

**ОШИБКА 2: Переименование переменной в JS**
```javascript
// ОШИБКА! Нельзя менять имя переменной
setVar('HPK_ID_NEW', getValue('HOSP_PLAN_KINDS'));
```

**ОШИБКА 3: Изменение имени переменной в data передаче**
```javascript
// ОШИБКА! Дочерняя форма ожидает HPK_ID
openWindow('HospitPlanning/hosp_plan_mkbs', true, {
    vars: {
        HPK_ID_NEW: getValue('HOSP_PLAN_KINDS')  // Должно быть HPK_ID
    }
});
```

### 71.13 Исключения (требуют согласования)

**Единственный случай, когда можно менять имя переменной с srctype="var":**

Если в исходном коде есть **явное несоответствие** между JS и src, и это подтверждено анализом всей системы:

```javascript
// Исходный код с ошибкой в самой системе
setVar('PLAN_DAY', data.day);  // В JS используется PLAN_DAY
// В src указано PLAN_DAY_ENG
<component cmptype="Variable" name="pnPLAN_DAY" src="PLAN_DAY_ENG" srctype="var"/>
```

В этом случае необходимо:
1. Согласовать с командой разработки
2. Изменить JS код на `setVar('PLAN_DAY_ENG', data.day)`
3. Изменить src на `PLAN_DAY_ENG`
4. Протестировать все вызовы формы



## Правило №71 (продолжение): Сохранение имен переменных в JS и атрибутах src

### 71.14 Дополнительные примеры правильной переработки

**Пример 1: Переменная с srctype="var" в Action (удаление)**
```xml
<!-- Исходный код -->
<component cmptype="Action" name="delHpkPlan" unit="HPK_PLANS" action="DELETE">
    <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="ActionVar" name="pnID" src="HPK_PLANS" srctype="ctrl" get="v1"/>
</component>

<!-- Правильно переработанный код -->
<component cmptype="Action" name="acDelHpkPlan">
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_PLANS" action="DELETE"/>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        begin
          -- POSTGRESQL: Удаление плана госпитализации
          call D_PKG_HPK_PLANS.DEL(pnID  => (:pnID)::numeric,   -- Идентификатор записи
                                   pnLPU => (:pnLPU)::numeric); -- ЛПУ пользователя
        end;
        ]]>
    </component>
    <!-- src="LPU" и src="HPK_PLANS" НЕ ИЗМЕНИЛИСЬ -->
    <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session"/>
    <component cmptype="ActionVar" name="pnID"  src="HPK_PLANS" srctype="ctrl"/>
</component>
```

**Пример 2: Переменная с srctype="var" в DataSet (фильтрация)**
```xml
<!-- Исходный код -->
<component cmptype="DataSet" name="DS_HPK_PLANS">
    <component cmptype="Variable" name="ID" src="HOSP_PLAN_KINDS" srctype="ctrl" get="v1"/>
    <component cmptype="Variable" name="PLAN_DATE_FROM" src="PLAN_DATE_FROM" srctype="ctrl" get="v2"/>
</component>

<!-- Правильно переработанный код -->
<component cmptype="DataSet" name="dsHpkPlans" activateoncreate="false" mode="Range">
    <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
        <![CDATA[
        -- ORACLE: Выборка планов госпитализации
        select hp.ID,                                  -- Уникальный идентификатор
               trunc(hp.PLAN_DATE) as PLAN_DATE,       -- Дата плана
               hp.PLAN_DAY_RUS,                        -- День недели (русский)
               trim(hp.PLAN_DAY_ENG) as PLAN_DAY_ENG,  -- День недели (английский)
               hp.GEN_COUNT_S                          -- Общее количество мест
          from D_V_HPK_PLANS hp
         where hp.PID = to_number(:pnHOSP_PLAN_KINDS)   -- Фильтр по виду плана
           and (hp.PLAN_DATE >= to_date(:pdPLAN_DATE_FROM, 'DD.MM.YYYY') or :pdPLAN_DATE_FROM is null)
           and (hp.PLAN_DATE <= to_date(:pdPLAN_DATE_TO, 'DD.MM.YYYY') or :pdPLAN_DATE_TO is null)
        ]]>
    </component>
    <!-- src НЕ ИЗМЕНИЛИСЬ: HOSP_PLAN_KINDS, PLAN_DATE_FROM, PLAN_DATE_TO -->
    <component cmptype="Variable" name="pnHOSP_PLAN_KINDS" src="HOSP_PLAN_KINDS" srctype="ctrl"/>
    <component cmptype="Variable" name="pdPLAN_DATE_FROM"  src="PLAN_DATE_FROM"  srctype="ctrl"/>
    <component cmptype="Variable" name="pdPLAN_DATE_TO"    src="PLAN_DATE_TO"    srctype="ctrl"/>
</component>
```

### 71.15 Что делать, если переменная не найдена в целевой форме

**Ситуация:** При переработке формы обнаруживается, что переменная с `srctype="var"` или `srctype="data"` используется в Action или DataSet, но не определена в JS коде текущей формы.

**Решение:**
1. **НЕ УДАЛЯТЬ** такие переменные
2. **НЕ ПЕРЕИМЕНОВЫВАТЬ** их src
3. Оставить как есть - они могут использоваться:
   - При вызове из родительской формы (передаются через vars)
   - В других Action на форме
   - В мастер-детейл связях

**Пример:**
```xml
<!-- Переменная HPK_ID может не использоваться в JS текущей формы, -->
<!-- но она передается из родительской формы через openWindow -->
<component cmptype="Variable" name="pnHPK_ID" src="HPK_ID" srctype="var"/>
```

### 71.16 Обоснование правила (дополнение)

4. **Целостность данных:** Переменные с `srctype="var"` и `srctype="data"` участвуют в обмене данными между компонентами

5. **Невидимые связи:** Существуют связи, которые не видны в коде формы:
   - Связи через `openWindow` (передача vars)
   - Связи через `setControlProperty` с параметром `locate`
   - Связи через `executeAction` с передачей параметров

6. **Динамические вызовы:** Некоторые Action могут вызываться динамически по имени, и ожидают конкретные имена переменных

## Правило №72: Оптимизация вызовов функций и процедур при конвертации Oracle → PostgreSQL

### 72.1 Вызов функций (возвращают значение)

**Oracle (медленно в PostgreSQL):**
```sql
:var := FUNCTION_NAME(params);
```

**PostgreSQL (быстро):**
```sql
SELECT FUNCTION_NAME(params) INTO :var;
```

**Пример:**
```sql
-- ❌ Медленно в PostgreSQL
:SHOW_CSE := D_PKG_URPRIVS.CHECK_BPPRIV(:LPU, 'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS', null, 0);

-- ✅ Быстро в PostgreSQL
select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS', null, 0)
  into :pnSHOW_CSE;
```

### 72.2 Вызов процедур (RETURNS VOID)

**Oracle (медленно в PostgreSQL):**
```sql
PROCEDURE_NAME(params);
```

**PostgreSQL (быстро):**
```sql
CALL PROCEDURE_NAME(params);
```

**Пример:**
```sql
-- ❌ Медленно в PostgreSQL
D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, :LPU, 'HOSP_PLAN_KINDS', :CID);

-- ✅ Быстро в PostgreSQL
call D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, :pnLPU, 'HOSP_PLAN_KINDS', :pnCID);
```

### 72.3 Присваивание констант и выражений

**Oracle и PostgreSQL (оставляем как есть - быстро):**
```sql
:var := '01.' || to_char(current_date, 'MM.YYYY');
:var := to_char(current_date, 'DD.MM.YYYY');
:var := 100 + 50;
:var := text1 || text2;
```

### 72.4 Функции дат

| Oracle | PostgreSQL (быстро) |
|--------|---------------------|
| `sysdate` | `current_date` |
| `ADD_MONTHS(date, months)` | `date + (months \|\| ' months')::interval` |
| `last_day(date)` | `date_trunc('month', date) + interval '1 month - 1 day'` |
| `trunc(date)` | `date_trunc('day', date)` |

**Пример:**
```sql
-- ❌ Медленно в PostgreSQL (эмуляция Oracle функций)
nNEW_DATE := ADD_MONTHS(to_date(:PLAN_DATE_FROM_S, 'dd.mm.yyyy'), :SEARCH_DIRECTION);

-- ✅ Быстро в PostgreSQL (нативные операции)
ndNEW_DATE := (:pdPLAN_DATE_FROM_S)::date + ((:pnSEARCH_DIRECTION)::integer || ' months')::interval;
```

### 72.5 Условные операторы

**Оставляем как есть (быстро в обеих СУБД):**
```sql
if to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY') < to_date(:pdDATE_TO, 'DD.MM.YYYY') then
  D_P_EXC('Текст ошибки');
end if;
```

### 72.6 Полный пример конвертации

**Исходный Oracle:**
```sql
nNEW_DATE := ADD_MONTHS(to_date(:PLAN_DATE_FROM_S, 'dd.mm.yyyy'),:SEARCH_DIRECTION);
:PLAN_DATE_FROM := '01.'||to_char(nNEW_DATE,'mm.yyyy');
:PLAN_DATE_TO := trunc(last_day(nNEW_DATE));
:SYS_DATE := to_char(sysdate,'DD.MM.YYYY');
:SHOW_CSE := D_PKG_URPRIVS.CHECK_BPPRIV(:LPU, 'ACCESS', null, 0);
D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, :LPU, 'TABLE', :CID);
```

**PostgreSQL оптимизированный:**
```sql
declare
  ndNEW_DATE date;
begin
  ndNEW_DATE := (:pdPLAN_DATE_FROM_S)::date + ((:pnSEARCH_DIRECTION)::integer || ' months')::interval;
  :pdPLAN_DATE_FROM := '01.' || to_char(ndNEW_DATE, 'MM.YYYY');
  :pdPLAN_DATE_TO := to_char(date_trunc('month', ndNEW_DATE) + interval '1 month - 1 day', 'DD.MM.YYYY');
  :pdSYS_DATE := to_char(current_date, 'DD.MM.YYYY');

  select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'ACCESS', null, 0)
    into :pnSHOW_CSE;

  call D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, :pnLPU, 'TABLE', :pnCID);
end;
```

### 72.7 Таблица быстрых решений

| Конструкция Oracle | PostgreSQL (быстро) |
|-------------------|---------------------|
| `var := func()` | `SELECT func() INTO var` |
| `proc()` | `CALL proc()` |
| `ADD_MONTHS(d, m)` | `d + (m \|\| ' months')::interval` |
| `last_day(d)` | `date_trunc('month', d) + interval '1 month - 1 day'` |
| `sysdate` | `current_date` |
| `var := expr` | `var := expr` (оставить) |

### 72.8 Исключения из правил

**Когда НЕ нужно менять конструкцию:**

1. **Встроенные функции PostgreSQL** - работают быстро в любом виде
2. **Присваивание простых типов** - `:=` оптимален
3. **Арифметические операции** - `:=` оптимален
4. **Конкатенация строк** - `:=` оптимален

### 72.9 Проверочный список при конвертации

- [ ] Все вызовы функций заменены на `SELECT ... INTO`
- [ ] Все вызовы процедур заменены на `CALL`
- [ ] `sysdate` заменен на `current_date`
- [ ] `ADD_MONTHS` заменен на интервальную арифметику
- [ ] `last_day` заменен на `date_trunc` с интервалом
- [ ] Присваивания констант оставлены через `:=`

## Правило №73: Форматирование вызовов функций и процедур в PostgreSQL

### 73.1 Вызов функций (однострочный - до 3 параметров)

**Правило:** Если у функции до 3 параметров, вызов оформляется в одну строку.

```sql
-- 1 параметр
select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'ACCESS', null, 0) into :pnRESULT;

-- 2-3 параметра с выравниванием
select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric,   -- ЛПУ пользователя
                                  'ACCESS',            -- Код доступа
                                  null,                -- Доп. параметр
                                  0)
  into :pnRESULT;   -- Флаг
```

### 73.2 Вызов функций (многострочный - 4+ параметров)

**Правило:** Если у функции 4 и более параметров, каждый параметр на новой строке с отступом 2 пробела.

```sql
select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric,                  -- ЛПУ пользователя
                                  'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS',  -- Код доступа
                                  null,                               -- Дополнительный параметр
                                  0)                                  -- Флаг проверки

  into :pnSHOW_CSE;
```

### 73.3 Вызов процедур (однострочный - до 3 параметров)

```sql
-- 1 параметр
call D_PKG_HPK_SCHEDULE.DEL((:pnID)::numeric);

-- 2-3 параметра
call D_PKG_HPK_SCHEDULE.DEL((:pnID)::numeric,   -- Идентификатор записи
                            (:pnLPU)::numeric);  -- ЛПУ пользователя
```

### 73.4 Вызов процедур (многострочный - 4+ параметров)

```sql
call D_PKG_CATALOGS.FIND_ROOT_CATALOG(
  1,                              -- Уровень каталога (1 - корневой)
  (:pnLPU)::numeric,              -- ЛПУ пользователя
  'HOSP_PLAN_KINDS',              -- Код таблицы/юнита
  :pnCID                          -- Выходной ID каталога
);
```

### 73.5 Приведение типов для параметров

**Правило:** В PostgreSQL все входящие параметры необходимо явно приводить к нужному типу.

| Тип в Oracle | Тип в PostgreSQL | Приведение |
|-------------|-----------------|------------|
| NUMBER | numeric | `(:pnVAR)::numeric` |
| VARCHAR2 | text | `(:psVAR)::text` |
| DATE | date | `(:pdVAR)::date` |
| INTEGER | integer | `(:pnVAR)::integer` |

**Пример:**
```sql
-- Правильное приведение типов
call D_PKG_HPK_PLANS.SET_PLAN_FOR_DAY(pnLPU        => (:pnLPU)::numeric,        -- NUMBER → numeric
                                      pnPLAN       => (:pnHPK_PLANS)::numeric,  -- NUMBER → numeric
                                      psDAY        => (:psPLAN_DAY_ENG)::text,  -- VARCHAR2 → text
                                      pdSTART_DATE => (:pdDATE_FROM)::date,     -- DATE → date
                                      pdEND_DATE   => (:pdDATE_TO)::date);      -- DATE → date
```

### 73.6 Именованные параметры (обязательно)

**Правило:** Всегда использовать именованные параметры при вызове процедур.

```sql
-- ✅ Правильно (именованные параметры)
call D_PKG_HPK_PLANS.DEL(pnID  => (:pnID)::numeric,
                         pnLPU => (:pnLPU)::numeric);

-- ❌ Неправильно (позиционные параметры)
call D_PKG_HPK_PLANS.DEL((:pnID)::numeric, (:pnLPU)::numeric);
```

### 73.7 Проверочный список для PostgreSQL блоков

- [ ] Функции возвращающие значение вызываются через `SELECT ... INTO`
- [ ] Процедуры вызываются через `CALL`
- [ ] Все входящие параметры приведены к типу через `::`
- [ ] Использованы именованные параметры для процедур
- [ ] `sysdate` заменен на `current_date`
- [ ] `ADD_MONTHS` заменен на интервальную арифметику
- [ ] `last_day` заменен на `date_trunc + interval`
## Правило №74: Вызов процедур с INOUT/OUT параметрами в PostgreSQL

### 74.1 Проблема

В PostgreSQL при вызове хранимой процедуры, имеющей параметры типа `INOUT` или `OUT` (которые возвращают значения), **нельзя напрямую использовать переменную ActionVar** (например `:pnCID`) в качестве аргумента. Это приводит к ошибке несоответствия типов, так как платформа M2 не может правильно связать переменную для возврата значения.

### 74.2 Решение

Необходимо создать **локальную переменную** в блоке `DECLARE`, передать её в процедуру (для `INOUT` - с предварительным присвоением значения), а затем присвоить результат обратно в ActionVar переменную.

### 74.3 Типы параметров и их обработка

| Тип параметра | Описание | Действие до вызова | Действие после вызова |
|---------------|----------|-------------------|----------------------|
| `IN` | Только входной | Присвоить значение | Не требуется |
| `OUT` | Только выходной | Не требуется | Присвоить результат |
| `INOUT` | Входной и выходной | Присвоить значение | Присвоить результат |

### 74.4 Шаблоны решений

#### Для параметра типа `INOUT`:

```sql
declare
  nLOCAL_VAR numeric;  -- Локальная переменная того же типа
begin
  -- 1. Присваиваем значение из ActionVar в локальную переменную
  nLOCAL_VAR := :pnCID;

  -- 2. Вызываем процедуру с локальной переменной
  call D_PKG_CATALOGS.FIND_ROOT_CATALOG(pnRAISE    => 1,
                                        pnLPU      => (:pnLPU)::numeric,
                                        psUNITCODE => 'HOSP_PLAN_KINDS',
                                        pnCATALOG  => nLOCAL_VAR);  -- INOUT параметр

  -- 3. Присваиваем результат обратно в ActionVar
  :pnCID := nLOCAL_VAR;
end;
```

#### Для параметра типа `OUT`:

```sql
declare
  nLOCAL_VAR numeric;  -- Локальная переменная для получения результата
begin
  -- 1. Вызываем процедуру с локальной переменной
  call D_PKG_SOME_PACKAGE.GET_DATA(pnLPU     => (:pnLPU)::numeric,
                                   pnID      => (:pnID)::numeric,
                                   pnRESULT  => nLOCAL_VAR);  -- OUT параметр

  -- 2. Присваиваем результат обратно в ActionVar
  :pnRESULT := nLOCAL_VAR;
end;
```

### 74.5 Полный пример для `acGetDefParams`

```xml
<component cmptype="Action" name="acGetDefParams">
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
        <![CDATA[
        begin
          :pdSYS_DATE := to_char(sysdate, 'DD.MM.YYYY');
          :pdFILTER_DATE_FROM := '01.' || to_char(sysdate, 'MM.YYYY');
          :pdFILTER_DATE_TO := to_char(last_day(sysdate), 'DD.MM.YYYY');

          select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS', null, 0)
            into :pnSHOW_CSE
            from dual;

          D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, :pnLPU, 'HOSP_PLAN_KINDS', :pnCID);

          select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'HPK_SCHEDULE_INSERT', null, 0)
            into :pnSCH_ADD
            from dual;

          select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'HPK_SCHEDULE_UPDATE', null, 0)
            into :pnSCH_UPD
            from dual;

          select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'HPK_SCHEDULE_DELETE', null, 0)
            into :pnSCH_DEL
            from dual;
        end;
        ]]>
    </component>
    <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
        <![CDATA[
        declare
          nCID numeric;  -- Локальная переменная для INOUT параметра
        begin
          :pdSYS_DATE := to_char(current_date, 'DD.MM.YYYY');
          :pdFILTER_DATE_FROM := '01.' || to_char(current_date, 'MM.YYYY');
          :pdFILTER_DATE_TO := to_char(date_trunc('month', current_date) + interval '1 month - 1 day', 'DD.MM.YYYY');

          select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS', null, 0)
            into :pnSHOW_CSE;

          -- Присваиваем значение из ActionVar в локальную переменную
          nCID := :pnCID;

          -- Вызываем процедуру с локальной переменной (INOUT параметр)
          call D_PKG_CATALOGS.FIND_ROOT_CATALOG(pnRAISE    => 1,                       -- Отображать сообщение при ошибке
                                                pnLPU      => (:pnLPU)::numeric,       -- ЛПУ пользователя
                                                psUNITCODE => 'HOSP_PLAN_KINDS',       -- Код раздела
                                                pnCATALOG  => nCID);                   -- INOUT: ID каталога

          -- Возвращаем результат обратно в ActionVar
          :pnCID := nCID;

          select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'HPK_SCHEDULE_INSERT', null, 0)
            into :pnSCH_ADD;

          select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'HPK_SCHEDULE_UPDATE', null, 0)
            into :pnSCH_UPD;

          select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'HPK_SCHEDULE_DELETE', null, 0)
            into :pnSCH_DEL;
        end;
        ]]>
    </component>
    <component cmptype="ActionVar" name="pnLPU"              src="LPU"            srctype="session"/>
    <component cmptype="ActionVar" name="pdFILTER_DATE_FROM" src="PLAN_DATE_FROM" srctype="ctrl" put="" len="15"/>
    <component cmptype="ActionVar" name="pdFILTER_DATE_TO"   src="PLAN_DATE_TO"   srctype="ctrl" put="" len="15"/>
    <component cmptype="ActionVar" name="pdFILTER_DATE_FROM" src="deSchDateFrom"  srctype="ctrl" put="" len="15"/>
    <component cmptype="ActionVar" name="pdFILTER_DATE_TO"   src="deSchDateTo"    srctype="ctrl" put="" len="15"/>
    <component cmptype="ActionVar" name="pnSHOW_CSE"         src="SHOW_CSE"       srctype="var"  put="" len="10"/>
    <component cmptype="ActionVar" name="pnCID"              src="MAIN_CID"       srctype="var"  put="" len="17"/>
    <component cmptype="ActionVar" name="pdSYS_DATE"         src="SYS_DATE"       srctype="var"  put="" len="15"/>
    <component cmptype="ActionVar" name="pnSCH_ADD"          src="SCH_ADD"        srctype="var"  put="" len="1"/>
    <component cmptype="ActionVar" name="pnSCH_UPD"          src="SCH_UPD"        srctype="var"  put="" len="1"/>
    <component cmptype="ActionVar" name="pnSCH_DEL"          src="SCH_DEL"        srctype="var"  put="" len="1"/>
</component>
```

### 74.6 Пример для процедуры с несколькими OUT параметрами

```sql
declare
  nOUT1 numeric;
  nOUT2 numeric;
  sOUT3 varchar;
begin
  -- Вызов процедуры с OUT параметрами
  call D_PKG_SOME.GET_VALUES(pnID    => (:pnID)::numeric,
                             pnOUT1  => nOUT1,   -- OUT параметр
                             pnOUT2  => nOUT2,   -- OUT параметр
                             psOUT3  => sOUT3);  -- OUT параметр

  -- Присваиваем результаты в ActionVar
  :pnRESULT1 := nOUT1;
  :pnRESULT2 := nOUT2;
  :psRESULT3 := sOUT3;
end;
```

### 74.7 Важные замечания

1. **Локальная переменная обязательна** - нельзя использовать `:pnCID` напрямую
2. **Тип локальной переменной** должен соответствовать типу параметра процедуры
3. **Для INOUT параметров** - обязательно присвоить начальное значение до вызова
4. **Для OUT параметров** - начальное значение не требуется
5. **Всегда присваивать результат** обратно в ActionVar после вызова
6. **Имя локальной переменной** рекомендуется начинать с префикса `n` (число) или `s` (строка), `d` (дата)

```sql
declare
  nNEW_ID numeric;  -- Локальная переменная
begin
  -- Проверка даты
  if (:pdCLOSE_DATE_PLAN)::date < (:pdPLAN_DATE)::date then
    PERFORM D_P_EXC('Дата плана не может быть больше...');
  end if;

  -- Присваиваем начальное значение (для INOUT параметра)
  nNEW_ID := (:pnD_INSERT_ID)::numeric;

  -- Вызываем процедуру с локальной переменной
  call D_PKG_HPK_PLANS.ADD(pnD_INSERT_ID => nNEW_ID,   -- INOUT параметр
                           pnLPU         => (:pnLPU)::numeric,
                           pnPID         => (:pnPID)::numeric,
                           pdPLAN_DATE   => (:pdPLAN_DATE)::date,
                           pnMALE_COUNT  => (:pnMALE_COUNT)::numeric,
                           pnOPER_COUNT  => (:pnOPER_COUNT)::numeric,
                           pnGEN_COUNT   => (:pnGEN_COUNT)::numeric);

  -- Возвращаем результат обратно в ActionVar
  :pnD_INSERT_ID := nNEW_ID;
end;
```

### Уточнение Правило №74:
**Для PostgreSQL при вызове процедур с параметрами `INOUT` или `OUT`:**
1. **Всегда** создавайте локальную переменную в блоке `DECLARE`
2. **Для `INOUT`** - присвойте значение из ActionVar ДО вызова
3. **Для `OUT`** - не требуется присвоения до вызова
4. **После вызова** - обязательно присвойте результат обратно в ActionVar
5. **Исключение:** Если процедура НЕ содержит `INOUT`/`OUT` параметров, можно вызывать без `DECLARE`

### Уточняющие признаки для определения необходимости `DECLARE`:

| Признак | Нужен DECLARE |
|---------|---------------|
| Параметр используется в `INTO` или `RETURNING` | ✅ Да |
| В Oracle версии есть `RETURNING ID INTO` | ✅ Да |
| Параметр имеет тип `OUT` или `INOUT` в сигнатуре | ✅ Да |
| Параметр только `IN` | ❌ Нет |
| Переменная используется только для передачи значения В ПРОЦЕДУРУ | ❌ Нет |
| Переменная используется для получения значения ИЗ ПРОЦЕДУРЫ | ✅ Да |

### Как определить в вашем случае:

В исходном Oracle коде:
```sql
D_PKG_HPK_PLANS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID, ...)
```
Стрелка `=>` указывает на именованный параметр. Если в PostgreSQL сигнатуре этот параметр `INOUT` или процедура использует `RETURNING`, то нужен `DECLARE`.

**Вывод:** Правило нужно дополнить явным указанием, что **любой параметр, который возвращает значение (включая `pnD_INSERT_ID`), требует использования `DECLARE` с локальной переменной.**

75) `PERFORM` надо писать в нихнем регистре `perform`
## Правило №76: Порядок группировки компонентов в форме (исправленное)

### 76.1 Общая структура формы

Форма должна иметь следующую структуру компонентов (сверху вниз):

1. **Блок комментария формы** (документация)
2. **Script компонент** (весь JavaScript код)
3. **Action компоненты** (все SQL/PLSQL блоки)
4. **DataSet компоненты** (все SQL/PLSQL блоки)
5. **HTML/таблицы и визуальные компоненты** (Grid, Edit, Button, и т.д.)
6. **Все остальные компоненты** (MaskInspector, DepControls, SubForm и др.) - могут находиться в любом месте

### 76.2 Важные замечания

- **SubForm**, **MaskInspector**, **DepControls** и другие вспомогательные компоненты **не подчиняются** данному правилу группировки и могут находиться в любом месте формы
- **Комментарий формы** всегда должен быть первым элементом после открывающего тега `<div cmptype="form">`

### 76.3 Детальная схема группировки

```xml
<div cmptype="form" ...>
    <!-- ====================================================================
        1. БЛОК КОММЕНТАРИЯ ФОРМЫ (ДОКУМЕНТАЦИЯ)
        ==================================================================== -->
    <!--
        НАЗВАНИЕ ФОРМЫ: ...
        ОПИСАНИЕ: ...
        ВХОДНЫЕ ПАРАМЕТРЫ: ...
        ВОЗВРАЩАЕМЫЙ РЕЗУЛЬТАТ: ...
        ПРИМЕР ВЫЗОВА: ...
    -->

        <!-- ================================================================
            2. SCRIPT КОМПОНЕНТ (весь JavaScript код)
            ================================================================ -->
        <component cmptype="Script">
            <![CDATA[
            // Все JS функции формы
            ]]>
        </component>


        <!-- ================================================================
            3. ACTION КОМПОНЕНТЫ (SQL/PLSQL блоки)
            Группируются по функциональному назначению:
            - Сначала Action для получения/выборки данных (acSelect, acGet...)
            - Затем Action для вставки/обновления (acInsert, acUpdate, acSave)
            - Затем Action для удаления (acDelete)
            - Затем вспомогательные Action (acCheck, acValidate)
            ================================================================ -->
        <component cmptype="Action" name="acSelect">
            <!-- ... -->
        </component>

        <component cmptype="Action" name="acInsert">
            <!-- ... -->
        </component>

        <component cmptype="Action" name="acUpdate">
            <!-- ... -->
        </component>

        <component cmptype="Action" name="acDelete">
            <!-- ... -->
        </component>

        <!-- ================================================================
            4. DATASET КОМПОНЕНТЫ (SQL/PLSQL блоки)
            Группируются по функциональному назначению
            ================================================================ -->
        <component cmptype="DataSet" name="dsMain">
            <!-- ... -->
        </component>

        <component cmptype="DataSet" name="dsDict">
            <!-- ... -->
        </component>

        <!-- ================================================================
            6. HTML/ТАБЛИЦЫ И ВИЗУАЛЬНЫЕ КОМПОНЕНТЫ
            ================================================================ -->
        <table style="width: 100%;">
            <!-- визуальные компоненты -->
        </table>

        <!-- ================================================================
            7. ОСТАЛЬНЫЕ КОМПОНЕНТЫ (могут быть в любом месте)
            - SubForm
            - MaskInspector
            - DepControls
            - и другие вспомогательные компоненты
            ================================================================ -->
        <component cmptype="SubForm" path="CheckCseAccess"/>
        <component cmptype="MaskInspector" controls="..." effectControls="..."/>
        <component cmptype="DepControls" requireds="..." dependents="..."/>

    </component>
</div>
```

### 76.4 Пример правильной группировки

```xml
<div cmptype="form" oncreate="base().onCreate();" onshow="base().onShow();" window_size="150x225">
    <!--
        ========================================================================
        НАЗВАНИЕ ФОРМЫ: HospPlan/hospplanperiod_edit.frm
        ОПИСАНИЕ: Форма редактирования периодов планов госпитализации
        ========================================================================
    -->
    <!-- ===== SCRIPT ===== -->
    <component cmptype="Script">
        <![CDATA[
        Form.onCreate = function() { ... };
        Form.onShow = function() { ... };
        Form.onButtonOKClick = function() { ... };
        ]]>
    </component>

    <!-- ===== ACTION КОМПОНЕНТЫ ===== -->
    <component cmptype="Action" name="acSelect">
        <!-- ... -->
    </component>

    <component cmptype="Action" name="acInsert">
        <!-- ... -->
    </component>

    <component cmptype="Action" name="acUpdate">
        <!-- ... -->
    </component>

    <component cmptype="Action" name="acGetCurrentLPU">
        <!-- ... -->
    </component>


    <!-- ===== ВИЗУАЛЬНЫЕ КОМПОНЕНТЫ ===== -->
    <table style="width: 100%;">
        <tr>
            <td><component cmptype="label" caption="Дата:"/></td>
            <td><component cmptype="DateEdit" name="PLAN_DATE"/></td>
        </tr>
        <!-- ... -->
    </table>

    <!-- ===== ОСТАЛЬНЫЕ КОМПОНЕНТЫ (могут быть в любом месте) ===== -->
    <component cmptype="SubForm" path="CheckCseAccess"/>
    <component cmptype="MaskInspector" controls="PLAN_DATE;GEN_COUNT" effectControls="ButtonOk"/>
</div>
```

### 76.5 Порядок Action компонентов внутри группы

| Порядок | Тип Action | Пример имени | Описание |
|---------|------------|--------------|----------|
| 1 | Select/Get | `acSelect`, `acGetData`, `acLoad` | Получение данных |
| 2 | Insert/Add | `acInsert`, `acAdd`, `acCreate` | Добавление записей |
| 3 | Update | `acUpdate`, `acEdit`, `acModify` | Изменение записей |
| 4 | Save | `acSave`, `acStore` | Сохранение (комбинированный) |
| 5 | Delete | `acDelete`, `acRemove`, `acDel` | Удаление записей |
| 6 | Check/Validate | `acCheck`, `acValidate`, `acVerify` | Проверки и валидация |
| 7 | Вспомогательные | `acGetCurrentLPU`, `acCalc` | Прочие Action |

### 76.6 Порядок DataSet компонентов внутри группы

| Порядок | Тип DataSet | Пример имени | Описание |
|---------|-------------|--------------|----------|
| 1 | Основной | `dsMain`, `dsData`, `dsMaster` | Главный DataSet формы |
| 2 | Детальный | `dsDetail`, `dsItems`, `dsChild` | Дочерний DataSet |
| 3 | Справочный | `dsDict`, `dsList`, `dsReference` | Справочные данные |
| 4 | Вспомогательный | `dsTemp`, `dsAux` | Временные данные |

### 76.7 Компоненты, которые НЕ подчиняются правилу группировки

Следующие компоненты могут находиться **в любом месте** формы (обычно после визуальных компонентов, но это не обязательно):

| Компонент | Описание |
|-----------|----------|
| `SubForm` | Подключаемые подформы |
| `MaskInspector` | Контроллер масок ввода |
| `DepControls` | Контроллер зависимостей полей |
| `CheckCseAccess` | Компонент проверки прав доступа |
| Любые другие вспомогательные компоненты | Не влияющие на основной поток данных |

### 76.8 Проверочный список группировки

- [ ] Блок комментария в начале формы
- [ ] Script компонент расположен после всех SQL/PLSQL компонентов
- [ ] Все Action компоненты сгруппированы вместе
- [ ] Action отсортированы по функциональному назначению (Select → Insert → Update → Delete → Check)
- [ ] Все DataSet компоненты сгруппированы вместе после Action
- [ ] Визуальные компоненты (таблицы) расположены после Script
- [ ] SubForm, MaskInspector, DepControls могут быть в любом месте (не требуют перемещения)

Правило №77: Приведение имен функций и процедур к верхнему регистру в PostgreSQL
Во всех блоках PostgreSQL (cmpActionRouter и cmpDataSetRouter) имена функций и процедур ДОЛЖНЫ быть приведены к ВЕРХНЕМУ РЕГИСТРУ.

#### 77.1 Область применения

Правило применяется ко всем вызовам функций и процедур:
- `CALL D_PKG_NAME.PROC_NAME(...)`
- `SELECT D_PKG_NAME.FUNC_NAME(...) INTO ...`
- `FROM D_PKG_NAME.TABLE_FUNC(...)`
- `PERFORM D_PKG_NAME.PROC_NAME(...)`
- `SELECT D_PKG_NAME.FUNC_NAME(...)`

#### 77.2 Примеры правильного форматирования

```sql
-- ✅ Правильно: процедура в верхнем регистре
call D_PKG_HPK_PLANS.ADD(pnID => :pnID);

-- ✅ Правильно: функция в верхнем регистре
select D_PKG_URPRIVS.CHECK_BPPRIV(:pnLPU, 'ACCESS') into :pnRESULT;

-- ✅ Правильно: table function в верхнем регистре
from D_PKG_CSE_ACCESSES.GET_ALL_RIGHTS(:pnLPU, :pnCSE_ACCS, 'UNIT', :pnUNIT_ID) as t

-- ✅ Правильно: PERFORM в верхнем регистре
perform D_PKG_MSG.RECORD_NOT_FOUND(1, :pnID, 'TABLE');

-- ✅ Правильно: SELECT с вызовом функции
select D_PKG_CSE_ACCESSES.CHECK_RIGHT(...) into nRES_ACCESS;
```

```sql
-- ❌ Неправильно: нижний регистр
call d_pkg_hpk_plans.add(pnID => :pnID);
select d_pkg_urprivs.check_bppriv(:pnLPU, 'ACCESS') into :pnRESULT;
from d_pkg_cse_accesses.get_all_rights(:pnLPU, :pnCSE_ACCS, 'UNIT', :pnUNIT_ID) as t
select d_pkg_cse_accesses.check_right(...) into nRES_ACCESS;
```

#### 77.4 Алгоритм применения

При обработке PostgreSQL блоков необходимо:

1. Найти все вызовы функций и процедур в PostgreSQL синтаксисе
2. Паттерны для поиска:
   - `call [a-z_][a-z_]*\.[a-z_][a-z_]*\(`
   - `select [a-z_][a-z_]*\.[a-z_][a-z_]*\(`
   - `from [a-z_][a-z_]*\.[a-z_][a-z_]*\(`
   - `perform [a-z_][a-z_]*\.[a-z_][a-z_]*\(`
   - `[a-z_][a-z_]*\.[a-z_][a-z_]*\(` в контексте присваивания или INTO
3. Преобразовать имя пакета и имя функции/процедуры в **верхний регистр**
4. Сохранить регистр параметров (они должны быть в нижнем регистре с префиксами)

#### 77.5 Регулярные выражения для поиска и замены

**Поиск вызовов процедур:**
```regex
call ([a-z_]+)\.([a-z_]+)\(
```

**Замена:**
```regex
call \U$1\E.\U$2\E(
```

**Поиск вызовов функций в SELECT:**
```regex
select ([a-z_]+)\.([a-z_]+)\(
```

**Замена:**
```regex
select \U$1\E.\U$2\E(
```

**Поиск table functions в FROM:**
```regex
from ([a-z_]+)\.([a-z_]+)\(
```

**Замена:**
```regex
from \U$1\E.\U$2\E(
```

**Поиск вызовов функций в PERFORM:**
```regex
perform ([a-z_]+)\.([a-z_]+)\(
```

**Замена:**
```regex
perform \U$1\E.\U$2\E(
```

#### 77.11 Исключения из правила (не требуют изменения)

```sql
-- ✅ PostgreSQL встроенные функции - остаются в нижнем регистре
-- (они не являются функциями проекта D_PKG_*)
select current_date;
select now();
select array_append(result, rrow);

-- ✅ Псевдонимы таблиц - в нижнем регистре (правило №25)
from D_V_CSE_ACS_ALL t  -- псевдоним "t" в нижнем регистре

-- ✅ Параметры функций - в нижнем регистре (правило №1)
call PROC(pnID => :pnID)  -- параметр pnID в нижнем регистре
```

#### 77.12 Пример полного PostgreSQL блока с соблюдением правила №77

```sql
begin
  -- Проверка прав доступа через таблицу всех доступных прав
  select case when D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnRAISE          => 0,
                                                  pnLPU             => (:pnLPU)::numeric,
                                                  psUNITCODE        => 'HOSP_PLAN_KINDS',
                                                  pnUNIT_ID         => (:pnUNIT_ID)::numeric,
                                                  psRIGHT           => t.NRIGHT_CODE,
                                                  pnSOURCE_ID       => (:pnEMPLOYER)::numeric,
                                                  pnSI_TYPE         => 0,
                                                  pnCHECK_EXCLUSIVE => 0) = 1 then 1
             else 0
           end
    into nRES_ACCESS
    from D_PKG_CSE_ACCESSES.GET_ALL_RIGHTS((:pnLPU)::numeric,
                                           (
                                            select t.ID
                                              from D_V_CSE_ACS_ALL t
                                             where t.WHO_ID    = (:pnEMPLOYER)::numeric
                                               and t.WHO_CODE  = 'EMPLOYERS'
                                               and t.UNITCODE  = :psUNITCODE
                                               and t.UNIT_ID   = (:pnUNIT_ID)::numeric
                                            ),
                                           'HOSP_PLAN_KINDS',
                                           (:pnUNIT_ID)::numeric) as t(RIGHT_ID, NRIGHT_CODE, RIGHT_NAME)
   where instr(';' || :psCHECK_CODE || ';', ';' || t.NRIGHT_CODE || ';') > 0;

  -- Присвоение результата выходной переменной
  :pnRES_ACCESS := nRES_ACCESS;
end;
```

Исходник формы, который необходимо переработать:
```

```
Покажи только текст  переработанный  формы
