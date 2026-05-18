# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ /Forms/HospPlan/hospplan.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Mon May 18 10:18:17 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 40
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_SI_ICONS
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="DataSet" activateoncreate="false" name="DS_SI_ICONS">
            <![CDATA[
            select j.ID,
                   j.ID || hh.ID UNIQ_N,
                   D_PKG_SIGNAL_INFO_SETS.GET_FULL_SIGNAL_INFORMATION(fnLPU                => to_number(:LPU),
                                                                      fnSI_PLACE           => 4,
                                                                      fnPATIENT            => j.PATIENT,
                                                                      fnDIRECTION_SERVICES => null,
                                                                      fnDISEASECASE        => j.DISEASECASE) SI_ICON
              from D_V_HPK_PLAN_JOURNALS_BASE j
                   left join D_V_HOSP_HISTORIES_BASE hh
                          on hh.HPK_PLAN_JOURNAL = j.ID
                         and hh.RELATIVE_HH is null
             where j.ID in (select  t.COLUMN_VALUE
                              from table(cast(:DS_IDS as D_CL_ID)) t)
            ]]>
            <component cmptype="Variable" name="LPU" src="LPU" srctype="session" />
            <component cmptype="Variable" name="DS_IDS" src="DS_IDS" srctype="var" type="collection" tdo="D_CL_ID" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_PLAN_JOURNALS_BASE, D_V_HOSP_HISTORIES_BASE
**Используемые пакеты/функции:** D_PKG_SIGNAL_INFO_SETS.GET_FULL_SIGNAL_INFORMATION

---

### Запрос №2

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_HOSP_PLAN_KINDS
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_HOSP_PLAN_KINDS" compile="true">
  		    <![CDATA[
				select hpk.ID,
				       hpk.HP_NAME,
  		               hpk.JOURNAL_TYPE
				  from D_V_HOSP_PLAN_KINDS hpk
				       join table(cast(D_PKG_CSE_ACCESSES.GET_ID_WITH_RIGHTS(:PNLPU,'HOSP_PLAN_KINDS','3', :CABLAB) AS D_C_ID )) t1 on t1.COLUMN_VALUE = hpk.ID
				@if (:isVmpWaitingList) {
				 where hpk.JOURNAL_TYPE = 2
				@}
  		      order by hpk.HP_NAME
		 	]]>
  		  	<component cmptype="Variable" name="PNLPU" src="LPU" srctype="var" get="var1" />
  		  	<component cmptype="Variable" name="CABLAB" src="CABLAB" srctype="var" get="var0" />
			<component cmptype="Variable" name="isVmpWaitingList" src="isVmpWaitingList" srctype="var" />
  		</component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_PLAN_KINDS
**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.GET_ID_WITH_RIGHTS

---

### Запрос №3

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_HPK_PLAN_DAY
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_HPK_PLAN_DAY" activateoncreate="false" mode="Range" compile="true">
			<![CDATA[
				select row_number() over (
                          @if (:JOURNAL_TYPE == 2 && :GenDirNumbToDirKinds == 1){
                              @if (:fltr_DIRECTION_KIND_ID){
                                  partition by j.DIRECTION_KIND_ID
                              @}
                                  order by j.REGISTER_DATE
                          @}else{
                                  order by j.ID
                          @}
                       ) ROW_NUM,
                       j.ID,
		               j.UNIQ_N,
		               j.LPU,
		               j.HPK_PLAN THISPLANID,
                       j.PATIENT_ADDRESS,
                       j.PATIENT_SNILS,
                       j.RECORD_STATUS,
                       j.RECORD_STATUS_MNEMO,
		               j.HOSP_PLAN_KIND,
		               j.HOSP_PLAN_KIND_NAME,
		               j.PATIENT_ID,
		               j.PATIENT_AGENT,
		               j.PATIENT_CARD_NUMB CARD_NUMB,
		               j.HOSP_MKB.STR1 || ' ' || j.HOSP_MKB.STR2 HOSP_MKB,
		               j.IS_CANCELED,
                       j.PATIENT,
                       coalesce(j.PATIENT_ACTUAL, j.PATIENT) PATIENT_ACTUAL,
		               trunc(j.PATIENT_BIRTHDATE) PATIENT_BIRTHDATE,
		               j.DIRECTED_BY_ID,
		               j.DIRECTED_BY,
		               j.DIRECTED_TO_ID,
		               j.DIRECTED_TO,
		               j.REGISTERED_BY_ID,
		               j.REGISTERED_BY,
	  	               to_char(j.REGISTER_DATE, D_PKG_STD.FRM_DT) REGISTER_DATE,
		               to_char(j.DATE_IN, D_PKG_STD.FRM_DT) DATE_IN,
                     @if (:ADMISSION_HOSP == '1' || :ADMISSION_HOSP == '2') {
                       j.DATE_REC,
                     @}
		               trunc(j.DATE_IN) DATE_IN_TRUNC,
		               j.HOSP_IN_DEP,
					   j.MARKER,
		               to_char(j.DATE_OUT, D_PKG_STD.FRM_DT) DATE_OUT,
		               j.HAS_PRIVILEGES,
		               j.SHAS_PRIVILEGES,
		               j.OPERATION_ID,
		               j.OPERATION.STR2 OPERATION,
		               j.DIRECTION,
		               j.PAYMENT_KIND_ID,
		               j.PAYMENT_KIND,
		               j.CONTRACT CONTRACT_ID,
		               coalesce(j.HOSP_PAYMENT_KIND, j.PAYMENT_KIND) PAYMENT_KIND_NAME,
		               j.IS_READY,
		               j.HH_DIRECTION_DATE,
		               j.IS_OPER,
		               j.IS_OPER_MNEMO,
		               j.IS_READY_MNEMO,
		               j.HP_NAME,
		               D_PKG_HPK_PLAN_JOURNALS.GET_HOSP_HISTORY_STATUS(pnLPU              => j.LPU,
		                                                               pnHPK_PLAN_JOURNAL => j.ID,
		                                                               pnHOSP_HISTORIES   => j.HOSP_HISTORY) BEDS,
		               j.HOSP_HISTORY,
		               j.HOSP_HISTORY_DS,
		               to_char(j.PLAN_DATE, D_PKG_STD.FRM_D) PLAN_DATE,
		               j.COMMS COMMENTS,
		               j.DEPBED,
		               j.HOSP_PLAN_KIND_NAME DEP,
		               j.DEP_ID,
		               j.DIAGNOSIS_FROM,
		               j.DIAGNOSIS_FROM DIAGNOSIS_FROM_ORDER,
                       j.HPK_JOURNAL_TYPE JT,
                       j.DIR_COMMENT DIR_COMMENTS,
		               j.PATIENT_POLIS,
		               j.COMMENTS PATIENT_CONTACTS,
		               j.DISEASECASE,
                       j.HPK_JOURNAL_TYPE,
                       j.RECORD_NUMB || '-' || j.RECORD_PREF RECORD_PREF_NUMB,
                       j.DIR_PREF || '/' || j.DIRECTION_KIND_SHORT_NAME || '/' || j.DIR_NUMB DIR_PREF_NUMB,
                       j.HH_TYPE,
                       j.DIRECTION_KIND_ID,
                       j.DIRECTION_KIND_NAME,
                       j.OUTER_DIRECTION_NUMB OD_NUMB,
                       j.OUTER_DIRECTION_ID,
                       j.CANC_REASON_NAME,
                       j.HOSP_TYPE HOSPITALIZATION_TYPE_NAME,
                       j.MED_THERAPY_SCHEME_CODE,
                       j.RELATIVE_PATIENT_ID,
                       j.RELATIVE_PATIENT,
                       j.ALCOHOL_DATE,
                       case when j.ALCOHOL_RES = 1 then 'Отрицательно'
                            when j.ALCOHOL_RES = 2 then 'Положительно'
                            else ''
                       end ALCOHOL_RES,
                       j.DRUG_DATE,
                       case when j.DRUG_RES = 1 then 'Отрицательно'
                            when j.DRUG_RES = 2 then 'Положительно'
                            else ''
                       end DRUG_RES,
                       j.RELATIVE_HOSP_HISTORY,
                       j.RELATIVE_DISEASECASE,
			           j.CABLAB_NAME,
                       j.RELATIVE_PAYMENT_KIND_ID,
                    @if (:SHOW_FLG == 1){
                       case when exists (select null
                                           from D_V_AGENT_FLU_BASE a
                                          where a.PID = j.PATIENT_AGENT)
                              then 1
                            else 0
                       end AGENT_FLU,
                       case when exists (select null
                                           from D_V_AGENT_FLU_PMC_LAST af
                                          where af.PID = j.PATIENT_AGENT
                                            and j.REGISTER_DATE > af.NEXT_DATE
                                            and af.FLU_PURPOSE = 1
                                            and rownum = 1)
                              then 1
                            else 0
                       end PMC_FLU,
                       case when exists (select null
                                           from D_V_AGENT_FLU_PMC_LAST af
                                          where af.PID = j.PATIENT_AGENT
                                            and af.FLU_PURPOSE in (1, 2)
                                            and af.FLU_CONCLUSION = 2
                                            and rownum = 1)
                              then 1
                            else 0
                       end PMC_FLU_PATALOGY,
                       D_PKG_DAT_TOOLS.FULL_YEARS(sysdate, j.PATIENT_BIRTHDATE) AGN_YEARS,
                    @}
                    @if (:USE_QUEUE74 == 1){
                       wlr.ID          WLR_ID,
                       wlr.PREF_NUMB   WLR_PREFNUMB,
                       wlr.STATUS      WLR_STATUS,
                    @}
                       trunc(j.REGISTER_DATE) REGISTER_DATE_TRUNC,
                       j.HH_CANC_REASON
                  from D_V_HPK_PLAN_JOURNALS_GRID j
                    @if (:USE_QUEUE74 == 1){
                       left join (select wl.ID,
                                         wl.PREF_NUMB,
                                         wl.STATUS,
                                         wl.AGENT_ID,
                                         row_number() over (partition by wl.AGENT_ID order by wl.REG_DATE desc) RN
                                    from D_V_WL_RECORDS74 wl
                                   where wl.LPU = to_number(:PNLPU)
                                     and wl.TICKET_TYPE = 0
                                     and wl.REG_DATE    > trunc(sysdate)
                                     and wl.SERVICE     = :SERV_QUEUE74) wlr on wlr.AGENT_ID = j.PATIENT_AGENT
                                                                            and wlr.RN = 1

                    @}
                 where j.LPU = to_number(:PNLPU)
                   and to_date(:START_DATE, D_PKG_STD.FRM_D) <= coalesce (j.HOSP_PLAN_KIND_CLOSE_DATE, to_date(:START_DATE, D_PKG_STD.FRM_D))
               @if(:PERSMEDCARD){
                   and j.PATIENT_ID = to_number(:PERSMEDCARD)
               @}
               @if (:JOURNAL_TYPE == 0 && :PLANIDD <> -1){
                   and j.HOSP_PLAN_KIND = to_number(:PLANIDD)
                   and to_number(:CHECK_HPK) = 1
               @} else if (:JOURNAL_TYPE){
                   and j.HOSP_PLAN_KIND = to_number(:PLANIDD)
                   and to_number(:CHECK_HPK) = 1
               @}else if (:PLANIDD==-1){
                   and (select D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => to_number(:PNLPU),
                                                              psUNITCODE => 'HOSP_PLAN_KINDS',
                                                              pnUNIT_ID  => j.HOSP_PLAN_KIND,
                                                              psRIGHT    => 3,
                                                              pnCABLAB   => to_number(:CABLAB),
                                                              pnSERVICE  => null)
                          from dual) = 1
               @}else {
                   and j.HPK_PLAN_KIND is null
               @}
               @if(:CH_HH_ANNUL==1){
                   and (j.HOSP_HISTORY_DS is null or j.HOSP_HISTORY_DS != 1)
               @}
               @if (:JOURNAL_TYPE==0){
                   and j.PLAN_DATE = to_date(:START_DATE, D_PKG_STD.FRM_D)
               @}
			]]>
			<component cmptype="Variable" name="PNLPU" src="LPU" srctype="var" get="var0" />
			<component cmptype="Variable" name="CABLAB" src="CABLAB" srctype="session" />
			<component cmptype="Variable" name="PLANIDD" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" get="var3" />
			<component cmptype="Variable" name="START_DATE" src="DDATE" srctype="ctrl" get="var2" />
        	<component cmptype="Variable" name="JOURNAL_TYPE" src="JOURNAL_TYPE" srctype="var" get="var4" />
			<component cmptype="Variable" name="CABLAB" src="CABLAB" srctype="var" get="var5" />
			<component cmptype="Variable" name="CHECK_HPK" src="CHECK_HPK" srctype="var" get="var6" />
			<component cmptype="Variable" name="GenDirNumbToDirKinds" src="GenDirNumbToDirKinds" srctype="var" get="var7" />
			<component cmptype="Variable" name="CH_HH_ANNUL" src="CH_HH_ANNUL" srctype="ctrl" get="var8" />
			<component cmptype="Variable" name="PERSMEDCARD" src="PERSMEDCARD" srctype="ctrl" get="var9" />
			<component cmptype="Variable" name="SHOW_FLG" src="SHOW_FLG" srctype="var" get="gSHOW_FLG" />
			<component cmptype="Variable" name="ADMISSION_HOSP" src="ADMISSION_HOSP" srctype="var" />
			<component cmptype="Variable" type="count" src="ds1count" srctype="var" default="5" />
			<component cmptype="Variable" type="start" src="ds1start" srctype="var" default="1" />
            <component cmptype="Variable" name="USE_QUEUE74" src="0" srctype="const" get="pUSE_QUEUE74" />
            <component cmptype="Variable" name="SERV_QUEUE74" src="0" srctype="const" get="pSERV_QUEUE74" />
  		</component>
```

**Используемые таблицы/вьюхи:** D_V_AGENT_FLU_BASE, D_V_AGENT_FLU_PMC_LAST, D_V_HPK_PLAN_JOURNALS_GRID, D_V_WL_RECORDS74
**Используемые пакеты/функции:** D_PKG_STD.FRM_DT, D_PKG_HPK_PLAN_JOURNALS.GET_HOSP_HISTORY_STATUS, D_PKG_STD.FRM_D, D_PKG_DAT_TOOLS.FULL_YEARS, D_PKG_CSE_ACCESSES.CHECK_RIGHT

---

### Запрос №4

**Тип компонента:** M2 Action
**Имя компонента:** CreateNewPlan
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="CreateNewPlan">
            <![CDATA[
            begin
              D_PKG_HPK_PLANS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,
                                  pnLPU         => to_number(:pnLPU),
                                  pnPID         => to_number(:pnPID),
                                  pdPLAN_DATE   => to_date(:pdPLAN_DATE, 'DD.MM.YYYY'),
                                  pnMALE_COUNT  => null,
                                  pnOPER_COUNT  => null,
                                  pnGEN_COUNT   => null);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pdPLAN_DATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnPID" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnD_INSERT_ID" src="NewID" srctype="var" put="" len="17" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.ADD

---

### Запрос №5

**Тип компонента:** M2 Action
**Имя компонента:** SetPKConstr
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="SetPKConstr">
            <![CDATA[
            begin
              select hpk.HAS_PAYMENT_CONSTRAINTS
                into :pnHAS_PAYMENT_CONSTRAINTS
                from D_V_HOSP_PLAN_KINDS hpk
               where hpk.ID = to_number(:pnHPK_ID);
            exception
              when no_data_found then
                :pnHAS_PAYMENT_CONSTRAINTS := 0;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnHPK_ID" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnHAS_PAYMENT_CONSTRAINTS" src="HAS_PK_CONSTR" srctype="var" put="" len="2" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_PLAN_KINDS

---

### Запрос №6

**Тип компонента:** M2 Action
**Имя компонента:** CheckRightsIB
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="CheckRightsIB">
            <![CDATA[
            begin
              :pnCHECK_IB := D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => to_number(:pnLPU),
                                                            psUNITCODE => 'HOSP_PLAN_KINDS',
                                                            pnUNIT_ID  => to_number(:pnPLANID),
                                                            psRIGHT    => '8',
                                                            pnCABLAB   => to_number(:pnCABLAB),
                                                            pnSERVICE  => null);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnCABLAB" src="CABLAB" srctype="var" />
            <component cmptype="ActionVar" name="pnPLANID" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnCHECK_IB" src="CHIB" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.CHECK_RIGHT

---

### Запрос №7

**Тип компонента:** M2 Action
**Имя компонента:** CheckRightsREC
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="CheckRightsREC">
            <![CDATA[
            begin
              :pnCHECK_REC := D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => to_number(:pnLPU),
                                                             psUNITCODE => 'HOSP_PLAN_KINDS',
                                                             pnUNIT_ID  => to_number(:pnPLANID),
                                                             psRIGHT    => '7',
                                                             pnCABLAB   => to_number(:pnCABLAB),
                                                             pnSERVICE  => null);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnCABLAB" src="CABLAB" srctype="var" />
            <component cmptype="ActionVar" name="pnPLANID" src="C_HOSP_PLAN_KINDS" srctype="var" />
            <component cmptype="ActionVar" name="pnCHECK_REC" src="CHREC" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.CHECK_RIGHT

---

### Запрос №8

**Тип компонента:** M2 Action
**Имя компонента:** ActionDelPlanDay
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ActionDelPlanDay">
            <![CDATA[
            declare
              nLID          NUMBER(17);
              nLVMP_APPL    NUMBER(17);
              nLVMP_TALON   NUMBER(17);
            begin
              begin
                select vmp_l.ID,
                       vmp_l.VMP_APPL,
                       vmp_l.VMP_TALON
                  into nLID,
                       nLVMP_APPL,
                       nLVMP_TALON
                  from D_V_VMP_LINKS vmp_l
                       left join D_V_HPK_PLAN_JOURNALS hpk_pj on vmp_l.DIRECTION = hpk_pj.DIRECTION
                 where hpk_pj.ID = to_number(:pnID);

                D_PKG_VMP_LINKS.UPD(pnID        => nLID,
                                    pnLPU       => to_number(:pnLPU),
                                    pnVMP_APPL  => nLVMP_APPL,
                                    pnVMP_TALON => nLVMP_TALON,
                                    pnDIRECTION => NULL);
              exception
                when no_data_found then
                  null;
              end;

              D_PKG_HPK_PLAN_JOURNALS.DEL(pnID  => to_number(:pnID),
                                          pnLPU => to_number(:pnLPU));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnID" src="HPK_PL_DAY_VAR" srctype="var" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_VMP_LINKS, D_V_HPK_PLAN_JOURNALS
**Используемые пакеты/функции:** D_PKG_VMP_LINKS.UPD, D_PKG_HPK_PLAN_JOURNALS.DEL

---

### Запрос №9

**Тип компонента:** M2 Action
**Имя компонента:** prevDay
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="prevDay">
			<![CDATA[
			begin
				:START_DATE := to_char(to_date(:CURR_DATE)-1,'dd.mm.yyyy');
			end;
			]]>
			<component cmptype="ActionVar" name="START_DATE" src="DDATE" srctype="ctrl" put="var1" len="11" />
			<component cmptype="ActionVar" name="CURR_DATE" src="DDATE" srctype="ctrl" get="var3" />
	  	</component>
```


---

### Запрос №10

**Тип компонента:** M2 Action
**Имя компонента:** nextDay
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="nextDay">
			<![CDATA[
			begin
				:START_DATE := to_char(to_date(:CURR_DATE)+1,'dd.mm.yyyy');
			end;
			]]>
			<component cmptype="ActionVar" name="START_DATE" src="DDATE" srctype="ctrl" put="var1" len="11" />
			<component cmptype="ActionVar" name="CURR_DATE" src="DDATE" srctype="ctrl" get="var3" />
	  	</component>
```


---

### Запрос №11

**Тип компонента:** M2 Action
**Имя компонента:** SearchPatient
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="SearchPatient">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_RECORD(pnLPU       => to_number(:pnLPU),
                                                 pnPATIENT   => to_number(:pnPATIENT),
                                                 pnEXIST     => :pnEXIST,
                                                 pdDATE      => :pdDATE,
                                                 pnPLAN_KIND => :pnPLAN_KIND,
                                                 pnHAVE_NEXT => :pnHAVE_NEXT,
                                                 pnHAVE_PREV => :pnHAVE_PREV);

              if :pnEXIST = 0 then
                :pnHAVE_NEXT := 0;
                :pnHAVE_PREV := 0;
                :pnPLAN_KIND := to_number(:pnPLAN_KIND_IN);
              end if;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPATIENT" src="PERSMEDCARD" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnPLAN_KIND_IN" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnEXIST" src="EXIST" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pdDATE" src="DDATE" srctype="ctrl" put="" len="11" />
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="HAVE_NEXT" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnHAVE_PREV" src="HAVE_PREV" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_RECORD

---

### Запрос №12

**Тип компонента:** M2 Action
**Имя компонента:** startSearchDirection
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="startSearchDirection">
            <![CDATA[
            begin
              begin
                select t.PATIENT,
                       t.ID
                  into :pnPMC,
                       :pnDIR
                  from D_V_DIRECTIONS_BASE t
                 where (t.DIR_PREF = :psDIR_PREF or (:psDIR_PREF is null and t.DIR_PREF is null))
                   and (t.DIR_NUMB = :psDIR_NUMB or (:psDIR_NUMB is null and t.DIR_NUMB is null))
                   and t.DIR_TYPE  = 1
                   and t.LPU       = to_number(:pnLPU);
              exception
                when no_data_found then
                  :pnPMC := null;
                  :pnDIR := null;
                  :pnDIR_RESULT := -1;
                when too_many_rows then
                  :pnPMC := null;
                  :pnDIR := null;
                  :pnDIR_RESULT := -2;
              end;

              if :pnDIR is not null and :pnPMC is not null then
                D_PKG_HPK_PLAN_JOURNALS.SET_RECORD(pnLPU       => to_number(:pnLPU),
                                                   pnPATIENT   => to_number(:pnPMC),
                                                   pnEXIST     => :pnEXIST,
                                                   pdDATE      => :pdDATE,
                                                   pnPLAN_KIND => :pnPLAN_KIND,
                                                   pnHAVE_NEXT => :pnHAVE_NEXT,
                                                   pnHAVE_PREV => :pnHAVE_PREV,
                                                   pnDIRECTION => to_number(:pnDIR));
                :pnDIR_RESULT := 0;
              end if;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="psDIR_PREF" src="DIR_PREF" srctype="ctrl" />
            <component cmptype="ActionVar" name="psDIR_NUMB" src="DIR_NUMB" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnDIR_RESULT" src="DIR_RESULT" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnPMC" src="PMC_DIR" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="pnDIR" src="DIR_DIR" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="pdDATE" src="DDATE" srctype="ctrl" put="" len="25" />
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="DIR_HAVE_NEXT" srctype="var" put="" len="3" />
            <component cmptype="ActionVar" name="pnHAVE_PREV" src="DIR_HAVE_PREV" srctype="var" put="" len="3" />
            <component cmptype="ActionVar" name="pnEXIST" src="EXIST" srctype="var" put="" len="2" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_DIRECTIONS_BASE
**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_RECORD

---

### Запрос №13

**Тип компонента:** M2 Action
**Имя компонента:** checkNextExists
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="checkNextExists">
            <![CDATA[
            declare
              dNEXT_DATE      DATE;
              nNEXT_PLAN_KIND NUMBER(17);
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD(pnLPU            => to_number(:pnLPU),
                                                      pnPATIENT        => to_number(:pnPATIENT),
                                                      pdDATE           => to_date(:pdDATE, 'DD.MM.YYYY') - 1,
                                                      pnPLAN_KIND      => to_number(:pnPLAN_KIND),
                                                      pdNEXT_DATE      => dNEXT_DATE,
                                                      pnNEXT_PLAN_KIND => nNEXT_PLAN_KIND,
                                                      pnHAVE_NEXT      => :pnHAVE_NEXT);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPATIENT" src="PERSMEDCARD" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="HAVE_NEXTT" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD

---

### Запрос №14

**Тип компонента:** M2 Action
**Имя компонента:** SearchPatientNext
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="SearchPatientNext">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD(pnLPU            => to_number(:pnLPU),
                                                      pnPATIENT        => to_number(:pnPATIENT),
                                                      pdDATE           => to_date(:pdDATE, 'DD.MM.YYYY'),
                                                      pnPLAN_KIND      => to_number(:pnPLAN_KIND),
                                                      pdNEXT_DATE      => :pdNEXT_DATE,
                                                      pnNEXT_PLAN_KIND => :pnNEXT_PLAN_KIND,
                                                      pnHAVE_NEXT      => :pnHAVE_NEXT);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPATIENT" src="PERSMEDCARD" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdNEXT_DATE" src="DDATE" srctype="ctrl" put="" len="11" />
            <component cmptype="ActionVar" name="pnNEXT_PLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="HAVE_NEXTT" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD

---

### Запрос №15

**Тип компонента:** M2 Action
**Имя компонента:** searchDirNext
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="searchDirNext">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD(pnLPU            => to_number(:pnLPU),
                                                      pnPATIENT        => to_number(:pnPMC),
                                                      pdDATE           => to_date(:pdDATE_IN, 'DD.MM.YYYY'),
                                                      pnPLAN_KIND      => to_number(:pnHPK_IN),
                                                      pdNEXT_DATE      => :pdDATE_OUT,
                                                      pnNEXT_PLAN_KIND => :pnHPK_OUT,
                                                      pnHAVE_NEXT      => :pnHAVE_NEXT,
                                                      pnDIRECTION      => to_number(:pnDIR));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPMC" src="PMC_DIR" srctype="var" />
            <component cmptype="ActionVar" name="pnDIR" src="DIR_DIR" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_IN" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnHPK_IN" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE_OUT" src="DDATE" srctype="ctrl" put="" len="25" />
            <component cmptype="ActionVar" name="pnHPK_OUT" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="DIR_HAVE_NEXTT" srctype="var" put="" len="3" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD

---

### Запрос №16

**Тип компонента:** M2 Action
**Имя компонента:** searchDirPrev
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="searchDirPrev">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD(pnLPU            => to_number(:pnLPU),
                                                      pnPATIENT        => to_number(:pnPMC),
                                                      pdDATE           => to_date(:pdDATE_IN, 'DD.MM.YYYY'),
                                                      pnPLAN_KIND      => to_number(:pnHPK_IN),
                                                      pdPREV_DATE      => :pdDATE_OUT,
                                                      pnPREV_PLAN_KIND => :pnHPK_OUT,
                                                      pnHAVE_PREV      => :pnHAVE_PREV,
                                                      pnDIRECTION      => to_number(:pnDIR));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPMC" src="PMC_DIR" srctype="var" />
            <component cmptype="ActionVar" name="pnDIR" src="DIR_DIR" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_IN" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnHPK_IN" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE_OUT" src="DDATE" srctype="ctrl" put="" len="25" />
            <component cmptype="ActionVar" name="pnHPK_OUT" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_PREV" src="DIR_HAVE_PREVV" srctype="var" put="" len="3" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD

---

### Запрос №17

**Тип компонента:** M2 Action
**Имя компонента:** SearchPatientPrev
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="SearchPatientPrev">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD(pnLPU            => to_number(:pnLPU),
                                                      pnPATIENT        => to_number(:pnPATIENT),
                                                      pdDATE           => to_date(:pdDATE, 'DD.MM.YYYY'),
                                                      pnPLAN_KIND      => to_number(:pnPLAN_KIND),
                                                      pdPREV_DATE      => :pdPREV_DATE,
                                                      pnPREV_PLAN_KIND => :pnPREV_PLAN_KIND,
                                                      pnHAVE_PREV      => :pnHAVE_PREV);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPATIENT" src="PERSMEDCARD" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdPREV_DATE" src="DDATE" srctype="ctrl" put="" len="11" />
            <component cmptype="ActionVar" name="pnPREV_PLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnHAVE_PREV" src="HAVE_PREVV" srctype="var" put="" len="2" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD

---

### Запрос №18

**Тип компонента:** M2 Action
**Имя компонента:** ActionIB
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ActionIB">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_HH_DIRECTION_DATE(pnID => to_number(:pnID));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnID" src="HPK_PL_DAY_VAR" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_HH_DIRECTION_DATE

---

### Запрос №19

**Тип компонента:** M2 Action
**Имя компонента:** ActionMax_Prior
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ActionMax_Prior">
            <![CDATA[
            begin
              begin
                select case when t.MAX_PRIOR is null then ''
                            else ' Предварительная запись: ' || t.MAX_PRIOR || ' дней.'
                       end,
                       case when t.MIN_AGE is null then ''
                            else ' Минимальный возраст: ' || t.MIN_AGE || '.'
                       end,
                       case when t.MAX_AGE is null then ''
                            else ' Максимальный возраст: ' || t.MAX_AGE || '.'
                       end,
                       case when t.HAS_MKB_CONSTRAINTS = 1 then ' Имеются ограничения по диагнозам.'
                            else ''
                       end,
                       case when t.HAS_MKB_CONSTRAINTS = 1 then ''
                            else ' Имеются ограничения по видам оплаты.'
                       end,
                       case when t.HAS_LIMITS = 1 then ''
                            else ' Неограниченная запись.'
                       end
                  into :psMAX_PRIOR,
                       :psMIN_AGE,
                       :psMAX_AGE,
                       :psHAS_MKB_CONSTRAINTS,
                       :psPAYMENT_KIND,
                       :psHAS_LIMITS
                  from D_V_HOSP_PLAN_KINDS t
                 where t.ID = to_number(:pnHPKID);
              exception
                when no_data_found then
                  :psMAX_PRIOR := null;
                  :psMIN_AGE := null;
                  :psMAX_AGE := null;
                  :psHAS_MKB_CONSTRAINTS := null;
                  :psPAYMENT_KIND := null;
                  :psHAS_LIMITS := null;
              end;

              begin
                select ' Мест занято(всего): ' || h.GEN_COUNT_S || ',',
                       ' мужских: ' || h.MALE_COUNT_S || ',',
                       ' женских: ' || h.FEMALE_COUNT_S,
                       ' из них оперативных: ' || h.OPER_COUNT_S || ',',
                       ' консервативных: ' || h.CON_COUNT_S || ','
                  into :psGEN_COUNT_S,
                       :psMALE_COUNT_S,
                       :psFEMALE_COUNT_S,
                       :psOPER_COUNT_S,
                       :psCON_COUNT_S
                  from D_V_HPK_PLANS h
                 where h.PID = to_number(:pnHPKID)
                   and h.PLAN_DATE = to_date(:psSTART_DATE, 'DD.MM.YYYY');
              exception
                when no_data_found then
                  :psGEN_COUNT_S := null;
                  :psMALE_COUNT_S := null;
                  :psFEMALE_COUNT_S := null;
                  :psOPER_COUNT_S := null;
                  :psCON_COUNT_S := null;
              end;

              begin
                select hpk.HAS_PAYMENT_CONSTRAINTS
                  into :pnHAS_PAYMENT_CONSTRAINTS
                  from D_V_HOSP_PLAN_KINDS hpk
                 where hpk.ID = to_number(:pnHPKID);
              exception
                when no_data_found then
                  :pnHAS_PAYMENT_CONSTRAINTS := 0;
              end;

              begin
                if to_date(:psSTART_DATE, 'dd.mm.yyyy') < trunc(sysdate) then
                  :pnAVAIL_ADD := 0;
                else
                  :pnAVAIL_ADD := 1;
                end if;
              end;

              begin
                select t.ID
                  into :pnIDD
                  from D_V_HPK_PLANS_BASE t
                 where t.PID = to_number(:pnHPKID)
                   and t.PLAN_DATE = to_date(:psSTART_DATE, D_PKG_STD.FRM_D)
                   and t.LPU = to_number(:pnLPU);
              exception
                when no_data_found then
                  :pnIDD := null;
              end;

              begin
                select t.JOURNAL_TYPE
                  into :pnJOURNAL_TYPE
                  from D_V_HOSP_PLAN_KINDS t
                 where t.ID = to_number(:pnHPKID);
              exception
                when no_data_found then
                  :pnJOURNAL_TYPE := null;
              end;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnHPKID" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="psSTART_DATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="psMAX_PRIOR" src="max_reg_date" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psMIN_AGE" src="min_reg_age" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psMAX_AGE" src="max_reg_age" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psHAS_MKB_CONSTRAINTS" src="has_mkb_ogr" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psPAYMENT_KIND" src="pay_reg_kind" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psHAS_LIMITS" src="has_reg_lim" srctype="ctrlcaption" put="" len="60" />
            <component cmptype="ActionVar" name="psGEN_COUNT_S" src="Place_all" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psMALE_COUNT_S" src="Place_male" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psFEMALE_COUNT_S" src="Place_female" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psOPER_COUNT_S" src="Place_oper" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psCON_COUNT_S" src="Place_cons" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="pnAVAIL_ADD" src="AVAIL_ADD" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnHAS_PAYMENT_CONSTRAINTS" src="HAS_PK_CONSTR" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnIDD" src="THISPLANID" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="pnJOURNAL_TYPE" src="JOURNAL_TYPE" srctype="var" put="" len="1" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_PLAN_KINDS, D_V_HPK_PLANS, D_V_HPK_PLANS_BASE
**Используемые пакеты/функции:** D_PKG_STD.FRM_D

---

### Запрос №20

**Тип компонента:** M2 Action
**Имя компонента:** NearestDaySearch
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="NearestDaySearch">
            <![CDATA[
            begin
              :pdRESULT := D_PKG_HPK_PLAN_JOURNALS.NEAREST_DATE_SEARCH(pnLPU        => to_number(:pnLPU),
                                                                       pnHPK        => to_number(:pnHPK),
                                                                       pdSTART_DATE => to_date(:pdSTART_DATE, 'DD.MM.YYYY'),
                                                                       pnSEX        => to_number(:pnSEX),
                                                                       pnOPER_TYPE  => to_number(:pnOPER_TYPE));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnHPK" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdSTART_DATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnSEX" src="C_HOSP_READY" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnOPER_TYPE" src="C_HOSP_KIND" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdRESULT" src="DDATE" srctype="ctrl" put="" len="11" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.NEAREST_DATE_SEARCH

---

### Запрос №21

**Тип компонента:** M2 Action
**Имя компонента:** getQuantBeds
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getQuantBeds">
            <![CDATA[
            begin
              :pnQUANT_BEDS := D_PKG_HPK_PLAN_JOURNALS.GET_QUANT_BEDS_PROFILES_NEW(pnHPK_PLAN => to_number(:pnHPK_PLAN),
                                                                                   pnLPU      => to_number(:pnLPU));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnHPK_PLAN" src="THISPLANID" srctype="var" />
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnQUANT_BEDS" src="quant_beds" srctype="var" put="" len="700" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.GET_QUANT_BEDS_PROFILES_NEW

---

### Запрос №22

**Тип компонента:** M2 Action
**Имя компонента:** ActionPlaces
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ActionPlaces">
            <![CDATA[
            begin
              select ' Мест занято(всего): ' || h.GEN_COUNT_S || ',',
                     ' мужских: ' || h.MALE_COUNT_S || ',',
                     ' женских: ' || h.FEMALE_COUNT_S,
                     ' из них оперативных: ' || h.OPER_COUNT_S || ',',
                     ' консервативных: ' || h.CON_COUNT_S || ','
                into :psGEN_COUNT_S,
                     :psMALE_COUNT_S,
                     :psFEMALE_COUNT_S,
                     :psOPER_COUNT_S,
                     :psCON_COUNT_S
                from D_V_HPK_PLANS h
               where h.PID = to_number(:pnHPKID)
                 and h.PLAN_DATE = to_date(:psSTART_DATE, D_PKG_STD.FRM_D)
                 and h.LPU = to_number(:pnLPU);
            exception
              when no_data_found then
                :psGEN_COUNT_S := null;
                :psMALE_COUNT_S := null;
                :psFEMALE_COUNT_S := null;
                :psOPER_COUNT_S := null;
                :psCON_COUNT_S := null;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnHPKID" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="ActionVar" name="psSTART_DATE" src="DDATE" srctype="ctrl" />
            <component cmptype="ActionVar" name="psGEN_COUNT_S" src="Place_all" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psMALE_COUNT_S" src="Place_male" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psFEMALE_COUNT_S" src="Place_female" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psOPER_COUNT_S" src="Place_oper" srctype="ctrlcaption" put="" len="82" />
            <component cmptype="ActionVar" name="psCON_COUNT_S" src="Place_cons" srctype="ctrlcaption" put="" len="82" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_PLANS
**Используемые пакеты/функции:** D_PKG_STD.FRM_D

---

### Запрос №23

**Тип компонента:** M2 Action
**Имя компонента:** GetRight
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="GetRight">
          <![CDATA[
          begin
            :pnCHECK_HPK := D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => to_number(:pnLPU),
                                                           psUNITCODE => 'HOSP_PLAN_KINDS',
                                                           pnUNIT_ID  => to_number(:pnPLANIDD),
                                                           psRIGHT    => 3,
                                                           pnCABLAB   => to_number(:pnCABLAB),
                                                           pnSERVICE  => null);
          end;
          ]]>
          <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
          <component cmptype="ActionVar" name="pnPLANIDD" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" />
          <component cmptype="ActionVar" name="pnCABLAB" src="CABLAB" srctype="var" />
          <component cmptype="ActionVar" name="pnCHECK_HPK" src="CHECK_HPK" srctype="var" put="" len="1" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.CHECK_RIGHT

---

### Запрос №24

**Тип компонента:** M2 Action
**Имя компонента:** GetCardNumbAndStandartRights
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="GetCardNumbAndStandartRights" compile="true">
            <![CDATA[
            declare
              nCID NUMBER(17);
            begin
              :pnADMISSION_HOSP := coalesce(D_PKG_OPTIONS.GET('ADMISSION_HOSP', to_number(:pnLPU), 0), 0);
              :pnOPTION_THERAPY_SCHEMES_FIELD := coalesce(D_PKG_OPTIONS.GET('TherapySchemesField', to_number(:pnLPU), 0), 0);
              :pnGEN_DIR_NUMB_TO_DIR_KINDS := coalesce(D_PKG_OPTIONS.GET('GenDirNumbToDirKinds', to_number(:pnLPU), 0), 0);

              @if (:pnPMC) {
                  begin
                      select p2.ID,
                             p2.CARD_NUMB
                        into :pnPMC_ID,
                             :psCARDN
                        from D_V_PERSMEDCARD p1
                             join D_V_PERSMEDCARD p2 on p1.AGENT = p2.AGENT
                       where p1.ID = to_number(:pnPMC)
                         and p2.LPU = to_number(:pnLPU);
                  exception
                      when no_data_found then
                          :pnPMC_ID := null;
                          :psCARDN := null;
                  end;
              @} else {
                  :pnPMC_ID := null;
                  :psCARDN := null;
              @}

              D_PKG_CATALOGS.FIND_ROOT_CATALOG(1, to_number(:pnLPU), 'HPK_PLAN_JOURNALS', nCID);
              D_PKG_URPRIVS.GET_STANDART_PRIVS(pnLPU      => to_number(:pnLPU),
                                               psUNITCODE => 'HPK_PLAN_JOURNALS',
                                               pnCID      => nCID,
                                               pnINSERT   => :pnINSERT,
                                               pnUPDATE   => :pnUPDATE,
                                               pnDELETE   => :pnDELETE,
                                               pnMOVE_OUT => :pnMOVE_OUT);
              :pnSHOW_FLG := D_PKG_OPTION_SPECS.GET('ActualFLG', to_number(:pnLPU));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnPMC" src="PMC_ID" srctype="var" />
            <component cmptype="ActionVar" name="pnSHOW_FLG" src="SHOW_FLG" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnINSERT" src="PINSS" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnUPDATE" src="PUPDD" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnDELETE" src="PDELL" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="pnMOVE_OUT" src="PMOVV" srctype="var" put="" len="2" />
            <component cmptype="ActionVar" name="psCARDN" src="PERSMEDCARD" srctype="ctrlcaption" put="" len="26" />
            <component cmptype="ActionVar" name="pnPMC_ID" src="PERSMEDCARD" srctype="ctrl" put="" len="17" />
            <component cmptype="ActionVar" name="pnADMISSION_HOSP" src="ADMISSION_HOSP" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnOPTION_THERAPY_SCHEMES_FIELD" src="optionTherapySchemesField" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnGEN_DIR_NUMB_TO_DIR_KINDS" src="GenDirNumbToDirKinds" srctype="var" put="" len="1" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_PERSMEDCARD
**Используемые пакеты/функции:** D_PKG_CATALOGS.FIND_ROOT_CATALOG, D_PKG_URPRIVS.GET_STANDART_PRIVS

---

### Запрос №25

**Тип компонента:** M2 Action
**Имя компонента:** changeAnnStatus
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="changeAnnStatus">
            <![CDATA[
            begin
              if :pnHOSP_HISTORY_DS = 0 then
                D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS(to_number(:pnHOSP_HISTORY), to_number(:pnLPU), 2);
              else
                D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS(to_number(:pnHOSP_HISTORY), to_number(:pnLPU), 1);
              end if;
              D_PKG_WLH_REQUESTS.UPD_BY_HH_SET_DISCARD(pnHH_ID => to_number(:pnHOSP_HISTORY),
                                                       pnLPU   => to_number(:pnLPU));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" />
            <component cmptype="ActionVar" name="pnHOSP_HISTORY" src="HOSP_HISTORY" srctype="var" />
            <component cmptype="ActionVar" name="pnHOSP_HISTORY_DS" src="HOSP_HISTORY_DS" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS, D_PKG_WLH_REQUESTS.UPD_BY_HH_SET_DISCARD

---

### Запрос №26

**Тип компонента:** M2 Action
**Имя компонента:** rollbackAnnIB
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="rollbackAnnIB">
			<![CDATA[
			begin
				D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS(to_number(:HOSP_HISTORY), to_number(:LPU), 0);
			    D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE_DISCARD(pnHH_ID => to_number(:HOSP_HISTORY),
                                                            pnLPU   => to_number(:LPU));
			end;
			]]>
			<component cmptype="ActionVar" name="LPU" get="LPU" src="LPU" srctype="var" />
			<component cmptype="ActionVar" name="HOSP_HISTORY" get="HOSP_HISTORY" src="HOSP_HISTORY" srctype="var" />
		</component>
```

**Используемые пакеты/функции:** D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS, D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE_DISCARD

---

### Запрос №27

**Тип компонента:** M2 Action
**Имя компонента:** getOuterLPUCablab
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getOuterLPUCablab">
            <![CDATA[
            begin
              select t.ID
                into :pnLPU
                from D_V_LPU t
               where t.LPUDICT_ID = to_number(:pnOUTER_LPUDICT);

              :psERR := null;
              begin
                select t.ID
                  into :pnCABLAB
                  from D_V_CABLAB t
                       join D_V_LPU t2 on t.LPU = t2.ID
                 where t.CL_CODE = (select D_PKG_OPTION_SPECS.GET('CABLAB_OUTER_DIRECTIONS', t2.ID) from dual)
                   and t2.LPUDICT_ID = to_number(:pnOUTER_LPUDICT);
              exception
                when no_data_found then
                  :psERR := 'Не найден кабинет для внешних направлений. Обратитесь к администратору';
              end;

              if to_number(:pnDDIR_ID) is not null then
                begin
                  select ID
                    into :pnDDIR_IDN
                    from D_V_DIRECTIONS d2
                   where d2.ID = to_number(:pnDDIR_ID)
                     and d2.LPU = to_number(:pnLPU);
                exception
                  when no_data_found then
                    select d2.ID
                      into :pnDDIR_IDN
                      from D_V_OUTER_DIRECTIONS od
                           join D_V_DIRECTIONS d2 on d2.OUTER_DIRECTION_ID = od.ID
                     where od.REPRESENT_DIRECTION = to_number(:pnDDIR_ID);
                end;
              end if;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnOUTER_LPUDICT" src="OUTER_LPUDICT" srctype="var" />
            <component cmptype="ActionVar" name="pnDDIR_ID" src="DDIR_ID" srctype="var" />
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="pnCABLAB" src="CABLAB" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="psERR" src="ERR" srctype="var" put="" len="100" />
            <component cmptype="ActionVar" name="pnDDIR_IDN" src="DDIR_ID" srctype="var" put="" len="17" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_LPU, D_V_CABLAB, D_V_DIRECTIONS, D_V_OUTER_DIRECTIONS

---

### Запрос №28

**Тип компонента:** M2 Action
**Имя компонента:** getCurrentHHDep
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getCurrentHHDep">
            <![CDATA[
            begin
              select hhd.ID
                into :pnHH_DEP_ID
                from D_V_HOSP_HISTORY_DEPS hhd
                     join D_V_HOSP_HISTORIES hh on hh.ID = hhd.PID
               where hh.ID = to_number(:pnHH_ID)
                 and hhd.IS_LAST = 1;
            exception
              when no_data_found then
                :pnHH_DEP_ID := null;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnHH_ID" src="HH_ID" srctype="var" />
            <component cmptype="ActionVar" name="pnHH_DEP_ID" src="HH_DEP_ID" srctype="var" put="" len="17" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_HISTORY_DEPS, D_V_HOSP_HISTORIES

---

### Запрос №29

**Тип компонента:** M2 Action
**Имя компонента:** ACT_CHECK_IF_HH_IS_SINGLE
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ACT_CHECK_IF_HH_IS_SINGLE">
            <![CDATA[
            begin
              begin
                select dir.IS_CANCELED
                  into :pnIS_CANCELED
                  from D_V_HPK_PLAN_JOURNALS_BASE jor
                       left join D_V_DIRECTIONS_BASE dir on dir.ID = jor.DIRECTION
                 where jor.ID = to_number(:pnHPK_PLAN_JOURNAL);
              exception
                when no_data_found then
                  :pnIS_CANCELED := 0;
              end;

              D_PKG_HOSP_HISTORIES.CHECK_HOSP_ONE_TIME(pnLPU      => to_number(:pnLPU),
                                                       pnPATIENT  => to_number(:pnPATIENT),
                                                       pdDATE_IN  => to_date(:psDATE_IN, 'DD.MM.YYYY HH24:MI'),
                                                       pdDATE_OUT => to_date(:psDATE_OUT, 'DD.MM.YYYY HH24:MI'),
                                                       psERR      => :psERR,
                                                       psWARN     => :psWARN);
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnPATIENT" src="PATIENT_ID" srctype="var" />
            <component cmptype="ActionVar" name="pnHPK_PLAN_JOURNAL" src="ID" srctype="var" />
            <component cmptype="ActionVar" name="psDATE_IN" src="DATE_IN" srctype="var" />
            <component cmptype="ActionVar" name="psDATE_OUT" src="DATE_OUT" srctype="var" />
            <component cmptype="ActionVar" name="pnIS_CANCELED" src="IS_CANCELED" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="psERR" src="ERR" srctype="var" put="" len="4000" />
            <component cmptype="ActionVar" name="psWARN" src="WARN" srctype="var" put="" len="4000" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_PLAN_JOURNALS_BASE, D_V_DIRECTIONS_BASE
**Используемые пакеты/функции:** D_PKG_HOSP_HISTORIES.CHECK_HOSP_ONE_TIME

---

### Запрос №30

**Тип компонента:** M2 Action
**Имя компонента:** updateWLH
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="updateWLH">
            <![CDATA[
			begin
              D_PKG_WLH_REQUESTS.UPD_BY_HH_ADD(pnHH_ID => to_number(:pnHH_ID),
                                               pnLPU   => to_number(:pnLPU));
			end;
			]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnHH_ID" src="HH_ID" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_WLH_REQUESTS.UPD_BY_HH_ADD

---

### Запрос №31

**Тип компонента:** M2 Action
**Имя компонента:** updateWLHByHospCancel
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="updateWLHByHospCancel">
            <![CDATA[
			begin
              D_PKG_WLH_REQUESTS.UPD_BY_HH_CANCEL(pnHPK_PLAN_JOURNAL => to_number(:HPK_ID),
                                                  pnLPU              => to_number(:pnLPU));
			end;
			]]>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="HPK_ID" src="HPK_ID" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_WLH_REQUESTS.UPD_BY_HH_CANCEL

---

### Запрос №32

**Тип компонента:** M2 Action
**Имя компонента:** delInfoHpkScheduleReg
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="delInfoHpkScheduleReg">
			<![CDATA[
            begin
              D_PKG_HPK_SCHEDULE_REG.DEL(pnID  => :pnHPK_SCHEDULE_REG,
                                         pnLPU => :pnLPU_TO_ID);
            end;
            ]]>
			<component cmptype="ActionVar" name="pnHPK_SCHEDULE_REG" src="HPK_SCHEDULE_REG" srctype="var" />
			<component cmptype="ActionVar" name="pnOUTER_DIRECTION_ID" src="OUTER_DIRECTION_ID" srctype="var" />
			<component cmptype="ActionVar" name="pnLPU_TO_ID" src="LPU_TO" srctype="var" />
		</component>
```

**Используемые пакеты/функции:** D_PKG_HPK_SCHEDULE_REG.DEL

---

### Запрос №33

**Тип компонента:** M2 Action
**Имя компонента:** getInfoHpkScheduleReg
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getInfoHpkScheduleReg">
			<![CDATA[
            begin
              select hsr.ID
                into :pnHPK_SCHEDULE_REG
                from D_V_HPK_SCHEDULE_REG hsr
               where hsr.DIRECTION = :pnDIRECTION
                 and rownum = 1
               order by hsr.ID desc;
            exception when no_data_found then
              :pnHPK_SCHEDULE_REG := null;
            end;
            ]]>
			<component cmptype="ActionVar" name="pnDIRECTION" src="DIRECTION" srctype="var" />
			<component cmptype="ActionVar" name="pnHPK_SCHEDULE_REG" src="HPK_SCHEDULE_REG" srctype="var" put="" len="17" />
		</component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_SCHEDULE_REG

---

### Запрос №34

**Тип компонента:** M2 Action
**Имя компонента:** getInfoFromHHDeps
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getInfoFromHHDeps">
			<![CDATA[
			declare hhID number := :HH_ID;
			begin
			  if hhID is not null then
			    begin
				  select t.PAYMENT_KIND
				    into :HH_PAYMENT_KIND
				    from (select hhd.PAYMENT_KIND,
				  			   hhd.DATE_OUT,
				  			   max(coalesce(hhd.date_out, sysdate)) over (partition by hhd.PID) MX
				  		  from D_V_HOSP_HISTORY_DEPS_BASE hhd
				  		 where hhd.PID = :HH_ID
				  	   ) t
				   where coalesce(t.DATE_OUT, sysdate) = t.MX;
				exception when no_data_found then
				  hhID := null;
				end;
			  end if;

			  if hhID is null or :PAYMENT_KIND_ID = :HH_PAYMENT_KIND then
			    begin
			      select c.DOC_PREF || '/' || c.DOC_NUMB || ' от ' || trunc(c.DOC_DATE)
			    	into :CONTRACT_CAPTION
			    	from D_V_CONTRACTS_BASE c
			       where c.ID = :CONTRACT;
			    exception when no_data_found then null;
			    end;
			  end if;
			end;
			]]>
			<component cmptype="ActionVar" name="LPU" src="LPU" srctype="session" get="g1" />
			<component cmptype="ActionVar" name="HH_ID" src="HH_ID" srctype="var" get="gHH_ID" />
			<component cmptype="ActionVar" name="CONTRACT" src="CONTRACT" srctype="var" get="gCONTRACT" />
			<component cmptype="ActionVar" name="PAYMENT_KIND_ID" src="PAYMENT_KIND_ID" srctype="var" get="gPAYMENT_KIND_ID" />
			<component cmptype="ActionVar" name="CONTRACT_CAPTION" src="CONTRACT_CAPTION" srctype="var" put="gCONTRACT_CAPTION" len="1000" />
			<component cmptype="ActionVar" name="HH_PAYMENT_KIND" src="HH_PAYMENT_KIND" srctype="var" put="gHH_PAYMENT_KIND" len="100" />
		</component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_HISTORY_DEPS_BASE, D_V_CONTRACTS_BASE

---

### Запрос №35

**Тип компонента:** M2 Action
**Имя компонента:** ACancelHosp
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ACancelHosp">
			<![CDATA[
			begin
			  if :SMP_CALL_EXS_IDS is not null then
                for r in (select sces.ID,
                                 sces.HOSP_STATUS,
                                 sces.HPK_ID,
                                 sces.REFUSE_REASON
                            from D_V_SMP_CALL_EX_SYSTEM sces
                           where sces.ID in (select  ids.COLUMN_VALUE ID
                                               from table(D_PKG_TOOLS.STR_SEPARATE_TO_IDS(:SMP_CALL_EXS_IDS)) ids))
                loop
                  D_PKG_SMP_CALL_EX_SYSTEM.SET_HOSP_HISTORY(pnID     => r.ID,
                                                            pnLPU    => to_number(:LPU),
                                                            pnHH_ID  => null,
                                                            pnHPK_ID => r.HPK_ID);
                end loop;
              end if;
              D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE(pnHH_ID => to_number(:HH_ID),
                                                  pnLPU   => to_number(:LPU));
			  D_PKG_HOSP_HISTORIES.DEL(pnID  => to_number(:HH_ID),
			                           pnLPU => to_number(:LPU));
			end;
			]]>
			<component cmptype="ActionVar" name="SMP_CALL_EXS_IDS" src="SMP_CALL_EXS_IDS" srctype="var" />
			<component cmptype="ActionVar" name="HH_ID" src="HH_ID_DEL" srctype="var" />
			<component cmptype="ActionVar" name="LPU" src="LPU" srctype="session" />
		</component>
```

**Используемые таблицы/вьюхи:** D_V_SMP_CALL_EX_SYSTEM
**Используемые пакеты/функции:** D_PKG_TOOLS.STR_SEPARATE_TO_IDS, D_PKG_SMP_CALL_EX_SYSTEM.SET_HOSP_HISTORY, D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE, D_PKG_HOSP_HISTORIES.DEL

---

### Запрос №36

**Тип компонента:** M2 Action
**Имя компонента:** ACancelCheck
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="ACancelCheck">
            <![CDATA[
			begin
              select D_STRAGG_EX(D_TP_STRAGG_REC(sces.ID, ';', null, null, 1))
                into :SMP_CALL_EXS_IDS
                from D_V_HOSP_HISTORIES_BASE hh
                     join D_V_LPU_BASE l on l.ID = hh.LPU
                     join D_V_SMP_CALL_EX_SYSTEM_BASE sces
                       on hh.ID = sces.HH_ID
                      and l.CODE_LPU = sces.LPU_CODE
               where hh.ID = to_number(:HH_ID);
			end;
			]]>
            <component cmptype="ActionVar" name="HH_ID" src="HH_ID_DEL" srctype="var" />
            <component cmptype="ActionVar" name="SMP_CALL_EXS_IDS" src="SMP_CALL_EXS_IDS" srctype="var" put="" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_HISTORIES_BASE, D_V_LPU_BASE, D_V_SMP_CALL_EX_SYSTEM_BASE

---

### Запрос №37

**Тип компонента:** M2 Action
**Имя компонента:** reverseCancelHosp
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="reverseCancelHosp">
			<![CDATA[
			begin
              D_PKG_WLH_REQUESTS.UPD_BY_HH_REVERSE_CANCEL(pnHPK_PLAN_JOURNAL => to_number(:HPK_ID),
                                                          pnLPU              => to_number(:LPU));
              D_PKG_DIRECTIONS.SET_CANCELED(pnID            => to_number(:HOSP_DIR),
                                            pnLPU           => to_number(:LPU),
                                            pnIS_CANCELED   => 0,
                                            pnCANC_REASON   => null,
                                            pnCANC_EMPLOYER => null,
                                            pdCANC_DATE     => null);
			end;
			]]>
			<component cmptype="ActionVar" name="HOSP_DIR" src="HOSP_DIR" srctype="var" get="g0" />
			<component cmptype="ActionVar" name="HPK_ID" src="HPK_ID" srctype="var" get="g1" />
			<component cmptype="ActionVar" name="LPU" src="LPU" srctype="session" get="g2" />
			<component cmptype="SubAction" mode="execlast">
				<![CDATA[
				begin
					D_PKG_HPK_PLAN_JOURNALS.SET_RECORD_STATUS(:HPK_ID, :LPU, 0);
				end;
				]]>
				<component cmptype="SubActionVar" name="HPK_ID" get="g3" src="HPK_ID" srctype="parent" />
				<component cmptype="SubActionVar" name="LPU" get="g4" src="LPU" srctype="parent" />
			</component>
		</component>
```

**Используемые пакеты/функции:** D_PKG_WLH_REQUESTS.UPD_BY_HH_REVERSE_CANCEL, D_PKG_DIRECTIONS.SET_CANCELED

---

### Запрос №38

**Тип компонента:** M2 Action
**Имя компонента:** getDefDepFromOption
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="getDefDepFromOption">
			<![CDATA[
			begin
				:DP_NAME := D_PKG_OPTION_SPECS.GET('SchRegDepDefault', :LPU);
			end;
			]]>
			<component cmptype="ActionVar" name="LPU" src="LPU" srctype="var" get="v0" />
			<component cmptype="ActionVar" name="DP_NAME" src="DP_NAME_FOR_REG_SHORT" srctype="var" put="DPN" len="4000" />
		</component>
```


---

### Запрос №39

**Тип компонента:** M2 Action
**Имя компонента:** A_hosp_ready_rights
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="A_hosp_ready_rights">
			<![CDATA[
				BEGIN
					:RIGHTS := D_PKG_CSE_ACCESSES.check_employer_right(:pnlpu,d_pkg_employers.get_id(:pnlpu),'HOSP_PLAN_KINDS',:unit_id,8);
				END;
			]]>
			<component cmptype="ActionVar" name="pnlpu" src="LPU" srctype="var" get="lpu" />
			<component cmptype="ActionVar" name="unit_id" src="HOSP_PLAN_KIND" srctype="var" get="unit_id" />
			<component cmptype="ActionVar" name="RIGHTS" src="hosp_ready_rights" srctype="var" put="hosp_ready_rights" len="1" />
		</component>
```

**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.CHECK_EMPLOYER_RIGHT, D_PKG_EMPLOYERS.GET_ID

---

### Запрос №40

**Тип компонента:** M2 Action
**Имя компонента:** setIsReady
**Источник:** /Forms/HospPlan/hospplan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\HospPlan\hospplan.frm

**SQL код:**

```xml
<component cmptype="Action" name="setIsReady">
			<![CDATA[
			BEGIN
				D_PKG_HPK_PLAN_JOURNALS.SET_IS_READY(pnID => :ID,
				                                    pnLPU => :LPU,
				                               pnIS_READY => :IS_READY);
			END;
			]]>
			<component cmptype="ActionVar" name="ID" src="HPK_PLAN_DAY_ID" srctype="var" get="id" />
			<component cmptype="ActionVar" name="LPU" src="LPU" srctype="var" get="lpu" />
			<component cmptype="ActionVar" name="IS_READY" src="IS_READY" srctype="var" get="is_ready" />
		</component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLAN_JOURNALS.SET_IS_READY


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 25

---

### Вьюха №1: D_V_HPK_PLAN_JOURNALS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_PLAN_JOURNALS_BASE
 SELECT id,
    lpu,
    hpk_plan,
    hpk,
    patient,
    directed_by,
    directed_to,
    registered_by,
    register_date,
    has_privileges,
    operation AS operation_id,
    cid,
    direction,
    payment_kind AS payment_kind_id,
    is_ready,
    hh_direction_date,
    is_oper,
    diseasecase,
        CASE
            WHEN is_oper = 0::numeric OR check_null(is_oper::character varying, 0::character varying) THEN 'Консервативный'::character varying
            WHEN is_oper = 1::numeric OR check_null(is_oper::character varying, 1::character varying) THEN 'Оперативный'::character varying
            ELSE NULL::character varying
        END AS is_oper_mnemo,
        CASE
            WHEN is_ready = 0::numeric OR check_null(is_ready::character varying, 0::character varying) THEN 'Не готов'::character varying
            WHEN is_ready = 1::numeric OR check_null(is_ready::character varying, 1::character varying) THEN 'Готов'::character varying
            ELSE NULL::character varying
        END AS is_ready_mnemo,
    comments,
    quota_q,
    record_status,
        CASE
            WHEN record_status = 0::numeric THEN 'Не отработана'::character varying
            ELSE 'Отработана'::character varying
        END AS record_status_mnemo,
    record_numb,
    record_pref,
    concat(record_pref, '-', record_numb) AS record_pref_numb,
    rl_record,
    sch_resource,
    employment_status,
    date_actual,
    date_end_serv,
    contract,
    alcohol_date,
    alcohol_res,
    drug_date,
    drug_res,
    direction_status
   FROM d_hpk_plan_journals t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'HPK_PLAN_JOURNALS'::text));
```

---

### Вьюха №2: D_V_HOSP_HISTORIES_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HOSP_HISTORIES_BASE
 SELECT id,
    lpu,
    hpk_plan_journal,
    patient,
    hh_pref,
    hh_numb,
    hh_numb_type,
    hh_numb_altern,
    hosp_reason,
    reception_emp,
    date_in,
    plan_date_out,
    date_out,
    hospitalization_type,
    transportation_kind,
    lpu_from,
    mkb_send,
    mkb_send_exact,
    mkb_clinic,
    mkb_clinic_exact,
    mkb_clinic_date,
    mkb_final,
    mkb_final_exact,
    mkb_fin_comp,
    mkb_fin_comp_exact,
    mkb_fin_add,
    mkb_fin_add_exact,
    hosp_times,
    hosp_result,
    mkb_receive,
    mkb_receive_exact,
    relative,
    diseasecase,
    discard_status,
    is_well_timed_hosp,
    is_enough_volume,
    is_correct_healing,
    is_same_diagn,
    arch_date,
    arch_dep,
    arch_comment,
    arch_numb,
    hh_type,
    hosp_hour,
    hosp_outcome,
    transfer_lpu,
    transfer_reason,
    hh_numb_full,
    hh_numb_mask,
    other_therapy,
    ability_status,
    features,
    abandonment,
    arrive_order,
    death_came,
    hosp_income,
    hosp_is_first,
    direction_hosp,
    date_departure,
    relative_hh,
    judge_decision,
    seized_items,
    novor_num,
    attending_empl_id,
    department_id
   FROM d_hosp_histories t
  WHERE discard_status = 0::numeric AND relative_hh IS NULL AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'HOSP_HISTORIES'::text
         LIMIT 1));
```

---

### Вьюха №3: D_V_HOSP_PLAN_KINDS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HOSP_PLAN_KINDS
 SELECT t.id,
    t.hp_code,
    t.hp_name,
    t.max_prior,
    t.lpu,
    t.cid,
    d_pkg_hosp_plan_deps.get_deps(t.id::numeric) AS deps,
    t.min_age,
    t.max_age,
    t.has_mkb_constraints,
        CASE
            WHEN t.has_mkb_constraints = 0::numeric OR check_null(t.has_mkb_constraints::character varying, 0::character varying) THEN 'Нет'::character varying
            WHEN t.has_mkb_constraints = 1::numeric OR check_null(t.has_mkb_constraints::character varying, 1::character varying) THEN 'Да'::character varying
            ELSE ''::character varying
        END AS shas_mkb_constraints,
    t.has_limits,
        CASE
            WHEN t.has_limits = 0::numeric OR check_null(t.has_limits::character varying, 0::character varying) THEN 'Нет'::character varying
            WHEN t.has_limits = 1::numeric OR check_null(t.has_limits::character varying, 1::character varying) THEN 'Да'::character varying
            ELSE ''::character varying
        END AS shas_limits,
    t.has_payment_constraints,
        CASE
            WHEN t.has_payment_constraints = 0::numeric OR check_null(t.has_payment_constraints::character varying, 0::character varying) THEN 'Нет'::character varying
            WHEN t.has_payment_constraints = 1::numeric OR check_null(t.has_payment_constraints::character varying, 1::character varying) THEN 'Да'::character varying
            ELSE ''::character varying
        END AS shas_payment_constraints,
    t.numb_group,
    t.journal_type,
    t1.jt_name AS journal_type_mnemo,
    t.is_oper,
        CASE
            WHEN t.is_oper = 0::numeric THEN 'Консервативный'::character varying
            WHEN t.is_oper = 1::numeric THEN 'Оперативный'::character varying
            ELSE NULL::character varying
        END AS oper_mnemo,
    t.hp_block,
        CASE
            WHEN t.hp_block = 1::numeric OR check_null(t.hp_block::character varying, 1::character varying) THEN 'Журнал заблокирован'::character varying
            ELSE NULL::character varying
        END AS hp_block_mnemo,
    t.open_date,
    t.close_date
   FROM d_hosp_plan_kinds t
     CROSS JOIN d_hpk_journal_types t1
  WHERE t1.jt_code = t.journal_type AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'HOSP_PLAN_KINDS'::text));
```

---

### Вьюха №4: D_V_AGENT_FLU_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_AGENT_FLU_BASE
 SELECT id,
    pid,
    cid,
    version,
    flu_lpu AS flu_lpu_id,
    flu_lpu_handle,
    flu_diagnosis,
    flu_date,
    flu_conclusion,
    flu_rejection AS flu_rejection_id,
    is_last,
    rad_dose,
    rad_measure
   FROM d_agent_flu t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'AGENT_FLU'::text
         LIMIT 1));
```

---

### Вьюха №5: D_V_AGENT_FLU_PMC_LAST

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_AGENT_FLU_PMC_LAST
 SELECT t.id,
    t.pid,
    t.cid,
    t.version,
    t.flu_lpu AS flu_lpu_id,
    t.flu_lpu_handle,
    t.flu_diagnosis,
    t.flu_date,
    t.flu_conclusion,
    t1.flu_purpose,
    t1.next_date,
    t1.flu_method
   FROM d_agent_flu t
     LEFT JOIN d_pmc_flu t1 ON t1.agent_flu = t.id
  WHERE t.is_last = 1::numeric AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'AGENT_FLU'::text));
```

---

### Вьюха №6: D_V_HPK_PLAN_JOURNALS_GRID

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_PLAN_JOURNALS_GRID
 SELECT j.id,
    concat(j.id, hh.id) AS uniq_n,
    j.hpk_plan,
    j.lpu,
    j.hpk AS hosp_plan_kind,
    hpk.hp_name AS hosp_plan_kind_name,
    agn.id AS patient_agent,
    d_pkg_str_tools.fio(fssurname => agn.surname::character varying, fsname => agn.firstname::character varying, fspatrname => agn.lastname::character varying, fsreturn_full => 1::character varying) AS patient,
    TRIM(BOTH FROM d_pkg_agent_names.get_actual_on_date(fnagent => agn.id::numeric, fddate => COALESCE(hh.date_in, j.register_date)::timestamp without time zone, fsfield => 'SURNAME FIRSTNAME LASTNAME'::character varying)::text) AS patient_actual,
    j.patient AS patient_id,
    pmc.card_numb AS patient_card_numb,
    j.directed_by AS directed_by_id,
    ( SELECT d_pkg_str_tools.fio(dir_by_agn.surname::character varying, dir_by_agn.firstname::character varying, dir_by_agn.lastname::character varying) AS fio
           FROM d_employers dir_by_emp
             JOIN d_agents dir_by_agn ON dir_by_agn.id = dir_by_emp.agent
          WHERE dir_by_emp.id = j.directed_by) AS directed_by,
    j.directed_to AS directed_to_id,
    ( SELECT d_pkg_str_tools.fio(dir_to_agn.surname::character varying, dir_to_agn.firstname::character varying, dir_to_agn.lastname::character varying) AS fio
           FROM d_employers dir_to_emp
             JOIN d_agents dir_to_agn ON dir_to_agn.id = dir_to_emp.agent
          WHERE dir_to_emp.id = j.directed_to) AS directed_to,
    j.registered_by AS registered_by_id,
    ( SELECT d_pkg_str_tools.fio(reg_by_agn.surname::character varying, reg_by_agn.firstname::character varying, reg_by_agn.lastname::character varying) AS fio
           FROM d_employers reg_by_emp
             JOIN d_agents reg_by_agn ON reg_by_agn.id = reg_by_emp.agent
          WHERE reg_by_emp.id = j.registered_by) AS registered_by,
    j.register_date,
    j.has_privileges,
    vs.vs_name AS shas_privileges,
    j.operation AS operation_id,
    ( SELECT ROW(srv.se_code::character varying(4000), srv.se_name::character varying(4000))::d_tp_ss AS "row"
           FROM d_services srv
          WHERE srv.id = j.operation) AS operation,
    j.cid,
    j.direction,
    j.payment_kind AS payment_kind_id,
    ( SELECT pay_kind.pk_name
           FROM d_payment_kind pay_kind
          WHERE pay_kind.id = j.payment_kind) AS payment_kind,
    ( SELECT sch_r.date_rec
           FROM d_hpk_schedule_reg sch_r
          WHERE sch_r.direction = j.direction) AS date_rec,
    j.sch_resource,
    ( SELECT c.cl_name
           FROM d_sch_resources sr
             JOIN d_cablab c ON c.id = sr.cablab
          WHERE sr.id = j.sch_resource) AS cablab_name,
        CASE
            WHEN hh.id IS NOT NULL THEN ( SELECT max(pk.pk_name::text) AS max
               FROM d_hosp_history_deps hhd1
                 JOIN d_payment_kind pk ON pk.id = hhd1.payment_kind
              WHERE hhd1.pid = hh.id
              GROUP BY hhd1.date_in
              ORDER BY hhd1.date_in
             LIMIT 1)
            ELSE NULL::text
        END AS hosp_payment_kind,
    j.is_ready,
    j.hh_direction_date,
    j.is_oper,
        CASE j.is_oper
            WHEN 0 THEN 'Консервативный'::text
            WHEN 1 THEN 'Оперативный'::text
            ELSE NULL::text
        END AS is_oper_mnemo,
        CASE j.is_ready
            WHEN 0 THEN 'Не готов'::text
            WHEN 1 THEN 'Готов'::text
            ELSE NULL::text
        END AS is_ready_mnemo,
    hpk.hp_name,
    j.comments AS comms,
    j.diseasecase,
    j.contract,
    hh.hh_type,
    hh.discard_status AS hosp_history_ds,
    hh.id AS hosp_history,
    hh.relative AS hh_agent_relative,
        CASE
            WHEN hh.id IS NOT NULL THEN concat(hh.hh_numb_full, ( SELECT concat(
                    CASE
                        WHEN db1.pid IS NOT NULL THEN concat(', Койка: ', db1.db_code)::character varying
                        ELSE NULL::character varying
                    END, ', Палата: ',
                    CASE
                        WHEN db1.pid IS NULL THEN db1.db_code
                        ELSE ( SELECT db2.db_code
                           FROM d_dep_beds db2
                          WHERE db2.id = db1.pid)
                    END) AS concat
               FROM d_hosp_history_deps q
                 JOIN d_hh_dep_beds d ON d.pid = q.id
                 JOIN d_dep_beds db1 ON db1.id = d.dep_bed
              WHERE q.pid = hh.id AND q.date_out IS NULL AND d.date_out IS NULL))::character varying
            ELSE NULL::character varying
        END AS depbed,
        CASE
            WHEN hh.id IS NOT NULL THEN ( SELECT dep.dp_name
               FROM d_hosp_history_deps hhd1
                 JOIN d_deps dep ON dep.id = hhd1.dep
              WHERE hhd1.pid = hh.id AND hhd1.prvsid IS NULL)
            ELSE NULL::character varying
        END AS dep,
        CASE
            WHEN hh.id IS NOT NULL THEN ( SELECT hhd1.dep
               FROM d_hosp_history_deps hhd1
              WHERE hhd1.pid = hh.id AND hhd1.prvsid IS NULL)
            ELSE NULL::bigint
        END AS dep_id,
        CASE
            WHEN hh.hosp_result IS NULL OR (EXISTS ( SELECT NULL::text AS "null"
               FROM d_hosp_results hr
              WHERE hr.id = hh.hosp_result AND hr.r_code::text <> '6'::text)) THEN ( SELECT dep.dp_name
               FROM d_hosp_history_deps hhd
                 JOIN d_deps dep ON dep.id = hhd.dep
              WHERE hhd.pid = hh.id AND hhd.prvsid IS NULL)
            ELSE NULL::character varying
        END AS hosp_in_dep,
    ( SELECT string_agg(concat(m.m_name, '/', sc.grad_from, '/', sc.grad_to), ';'::text) AS string_agg
           FROM d_pmc_markers pm
             JOIN d_markers m ON m.id = pm.marker
             JOIN d_schedule_colors sc ON sc.id = m.color
          WHERE pm.pid = j.patient AND sysdate() >= pm.begin_date AND sysdate() <= COALESCE(pm.end_date, to_timestamp_simple('31.12.2999'::text, d_pkg_std.frm_d()::text)) AND (( SELECT d_pkg_cse_accesses.check_right(pnlpu => j.lpu::numeric, psunitcode => 'MARKERS'::character varying, pnunit_id => pm.marker::numeric, psright => 1::character varying, pncablab => NULLIF(current_setting(('MED'::text || '.'::text) || 'CABLAB'::text, true), ''::text)::numeric) AS check_right)) <> 1::numeric) AS marker,
    ( SELECT concat('Тел.: ', d_stragg_ex(ROW(concat(t.contact,
                CASE
                    WHEN NULLIF(t.note::text, ''::text) IS NOT NULL THEN concat(' ', t.note)::character varying
                    ELSE NULL::character varying
                END)::character varying(4000), concat(';', chr(10))::character varying(10), 'ASC'::character varying(8), NULL::character varying(4000), NULL::numeric(1000,0))::d_tp_stragg_rec)) AS concat
           FROM d_agent_contacts t
             JOIN d_contact_types ct ON ct.id = t.contact_type
          WHERE t.pid = agn.id AND (ct.ct_code::text = ANY (ARRAY['1'::character varying, '2'::character varying]::text[]))) AS comments,
    ( SELECT aa.raion
           FROM ( SELECT a.pid,
                    a.raion,
                    a.begin_date,
                    a.end_date,
                    row_number() OVER (PARTITION BY a.pid ORDER BY a.begin_date DESC) AS rn
                   FROM d_agent_addrs a
                  WHERE a.is_reg = 1::numeric) aa
          WHERE aa.pid = agn.id AND aa.begin_date <= COALESCE(hp.plan_date, sysdate()) AND (aa.end_date >= COALESCE(hp.plan_date, sysdate()) OR aa.end_date IS NULL) AND aa.rn::numeric = 1::numeric) AS raion_id,
    hp.plan_date,
    hosp_type.hk_name AS hosp_type,
    hosp_type.hk_code,
    ld.lpu_fullname AS lpu_from,
        CASE
            WHEN (COALESCE(length(mkb.mkb_code::text), 0) + COALESCE(length(mkb.mkb_name::text), 0) + COALESCE(length(hh.mkb_receive_exact::text), 0)) <= 3998 THEN concat(mkb.mkb_code, ' ', mkb.mkb_name, ' ', hh.mkb_receive_exact)::character varying
            ELSE concat(mkb.mkb_code, ' ', mkb.mkb_name, ' ', substr2(hh.mkb_receive_exact, 1::numeric, (3995 - COALESCE(length(mkb.mkb_code::text), 0) - COALESCE(length(mkb.mkb_name::text), 0))::numeric), '...')::character varying
        END AS diagnosis_from,
    agn.birthdate AS patient_birthdate,
    hh.date_in,
    hh.date_out,
    ( SELECT s.soc_code
           FROM d_agent_social_states pss
             JOIN d_socialstates s ON s.id = pss.social_state
          WHERE pss.pid = pmc.agent AND pss.begin_date <= sysdate() AND (pss.end_date >= trunc(sysdate()) OR pss.end_date IS NULL)) AS patient_social_state,
    ( SELECT
                CASE
                    WHEN pwp.work_place IS NULL THEN pwp.work_place_hand
                    ELSE ( SELECT ag.agn_name
                       FROM d_agents ag
                      WHERE ag.id = pwp.work_place)
                END AS agn_name
           FROM d_agent_work_places pwp
          WHERE pwp.pid = pmc.agent AND pwp.is_main = 1::numeric AND pwp.begin_date <= sysdate() AND (pwp.end_date >= trunc(sysdate()) OR pwp.end_date IS NULL)) AS patient_work_place,
    ( SELECT ct.cat_code
           FROM d_agent_categories pct
             JOIN d_categories ct ON ct.id = pct.category
          WHERE pct.pid = pmc.agent AND pct.date_b <= sysdate() AND (pct.date_e >= trunc(sysdate()) OR pct.date_e IS NULL)
         LIMIT 1) AS patient_categories,
    d_pkg_agent_polis.get_actual_on_date(fnpatient => agn.id::numeric, fddate => sysdate(), fntype => 0::numeric, fsfield => 'P_SER P_NUM'::character varying) AS patient_polis,
    ( SELECT concat(apd.pd_ser, ' ', apd.pd_numb) AS concat
           FROM d_agent_persdocs apd
          WHERE apd.pid = agn.id AND apd.is_main = 1::numeric AND apd.period_begin <= sysdate() AND (apd.period_end >= trunc(sysdate()) OR apd.period_end IS NULL)) AS patient_persdoc,
    ( SELECT d_pkg_agent_addrs.get_short_address_by_id(aa1.id::numeric) AS get_short_address_by_id
           FROM ( SELECT a.id,
                    a.pid,
                    a.begin_date,
                    a.end_date,
                    row_number() OVER (PARTITION BY a.pid ORDER BY a.begin_date DESC) AS rn
                   FROM d_agent_addrs a
                  WHERE a.is_reg = 1::numeric) aa1
          WHERE aa1.pid = agn.id AND aa1.begin_date <= sysdate() AND (aa1.end_date >= trunc(sysdate()) OR aa1.end_date IS NULL) AND aa1.rn::numeric = 1::numeric) AS patient_address,
    dir.dir_pref,
    dir.dir_numb,
    dir_kind.id AS direction_kind_id,
    dir_kind.dk_code AS direction_kind,
    dir_kind.dk_name AS direction_kind_name,
    dir_kind.short_name AS direction_kind_short_name,
    j.record_status,
        CASE
            WHEN j.record_status = 0::numeric THEN 'Не отработана'::character varying
            ELSE 'Отработана'::character varying
        END AS record_status_mnemo,
    d_pkg_doc_tools.snils_from_str(agn.snils::character varying) AS patient_snils,
    dir.is_canceled,
    dir.canc_reason AS canc_reason_id,
    dcr.dcr_code AS canc_reason,
    dcr.dcr_name AS canc_reason_name,
    hpk.journal_type AS hpk_journal_type,
    j.record_pref,
    j.record_numb,
    dir.outer_direction AS outer_direction_id,
    ( SELECT od.d_numb
           FROM d_outer_directions od
          WHERE od.id = dir.outer_direction) AS outer_direction_numb,
    dir.dir_comment,
    COALESCE(( SELECT string_agg(ts.code::text, ';'::text ORDER BY dts.id) AS string_agg
           FROM d_dir_therapy_schemes dts
             JOIN d_med_therapy_schemes_desc tsd ON tsd.id = dts.med_therapy_schemes
             JOIN d_med_therapy_schemes ts ON ts.id = tsd.pid
          WHERE dts.pid = j.direction), (( SELECT mts.code
           FROM d_directions d
             JOIN d_med_therapy_schemes mts ON d.therapy_schemes = mts.id
          WHERE d.id = j.direction))::text) AS med_therapy_scheme_code,
    dir.hosp_mkb AS hosp_mkb_id,
    ( SELECT ROW(hosp_mkb.mkb_code::character varying(4000), hosp_mkb.mkb_name::character varying(4000))::d_tp_ss AS "row"
           FROM d_mkb10 hosp_mkb
          WHERE hosp_mkb.id = dir.hosp_mkb) AS hosp_mkb,
    dir.reg_visit,
    dir.hosp_dep AS dir_hosp_dep_id,
    ( SELECT dhd.dp_name
           FROM d_deps dhd
          WHERE dhd.id = dir.hosp_dep) AS dir_hosp_dep_name,
    j.alcohol_date,
    j.alcohol_res,
    j.drug_date,
    j.drug_res,
    hpk.close_date AS hosp_plan_kind_close_date,
    hosp_rel.patient AS relative_patient_id,
    ( SELECT TRIM(BOTH FROM concat(rel_ag.surname, ' ', rel_ag.firstname, ' ', rel_ag.lastname)) AS btrim
           FROM d_persmedcard rel_pat
             JOIN d_agents rel_ag ON rel_ag.id = rel_pat.agent
          WHERE rel_pat.id = hosp_rel.patient) AS relative_patient,
    hosp_rel.id AS relative_hosp_history,
    hosp_rel.diseasecase AS relative_diseasecase,
    ( SELECT hpk_rel.payment_kind
           FROM d_hpk_plan_journals hpk_rel
          WHERE hpk_rel.id = hosp_rel.hpk_plan_journal) AS relative_payment_kind_id,
        CASE
            WHEN NULLIF(crg.name::text, ''::text) IS NOT NULL AND NULLIF(hcr.comm::text, ''::text) IS NOT NULL THEN concat(crg.name, ';', hcr.comm)::character varying
            ELSE COALESCE(crg.name, hcr.comm)
        END AS hh_canc_reason
   FROM d_hpk_plan_journals j
     LEFT JOIN d_hpk_plans hp ON hp.id = j.hpk_plan AND hp.lpu = j.lpu
     JOIN d_hosp_plan_kinds hpk ON hpk.id = j.hpk AND hpk.lpu = j.lpu
     JOIN d_persmedcard pmc ON pmc.id = j.patient AND pmc.lpu = j.lpu
     JOIN d_agents agn ON agn.id = pmc.agent
     LEFT JOIN (d_hosp_histories hh
     JOIN d_hospitalization_types hosp_type ON hosp_type.id = hh.hospitalization_type
     LEFT JOIN d_lpudict ld ON ld.id = hh.lpu_from
     LEFT JOIN d_mkb10 mkb ON mkb.id = hh.mkb_receive
     LEFT JOIN d_hosp_histories hosp_rel ON hosp_rel.relative_hh = hh.id AND (hosp_rel.date_out IS NULL OR hosp_rel.date_out = hh.date_out)
     LEFT JOIN d_hh_canc_reason hcr ON hcr.pid = hh.id
     LEFT JOIN d_canc_reason_guid crg ON crg.id = hcr.guid) ON hh.hpk_plan_journal = j.id AND hh.relative_hh IS NULL
     JOIN d_hpkpj_vmp_states vs ON vs.vs_code = j.has_privileges
     LEFT JOIN (d_directions dir
     LEFT JOIN d_direction_kinds dir_kind ON dir_kind.id = dir.direction_kind
     LEFT JOIN d_dir_canc_reasons dcr ON dcr.id = dir.canc_reason) ON dir.id = j.direction
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = j.cid AND ur.unitcode::text = 'HPK_PLAN_JOURNALS'::text));
```

---

### Вьюха №7: D_V_WL_RECORDS74

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_WL_RECORDS74
 SELECT t.id,
    t.lpu,
    t.pid,
    t.hid,
    t.agent AS agent_id,
    d_pkg_str_tools.fio(t1.surname::character varying, t1.firstname::character varying, t1.lastname::character varying) AS agent,
    t.pref,
    t.numb,
    concat(t.pref, t.numb) AS pref_numb,
    t.ticket_type,
    t.is_ill,
        CASE
            WHEN t.is_ill = 0::numeric THEN 'Здоровый'::character varying
            ELSE 'Больной'::character varying
        END AS is_ill_name,
    t.service AS service_id,
    t2.se_code AS service,
    t2.se_name AS service_name,
    t.dir_serv AS dir_serv_id,
    t3.uk_hash AS dir_serv,
    t.payment_kind AS payment_kind_id,
    t4.pk_code AS payment_kind,
    t4.pk_name AS payment_kind_name,
    t.reg_date,
    t.call_date,
    t.status,
        CASE t.status
            WHEN 0 THEN 'Находится в очереди'::text
            WHEN 1 THEN 'Вызван из очереди'::text
            WHEN 2 THEN 'Неявка'::text
            WHEN 3 THEN 'Услуга оказана'::text
            ELSE NULL::text
        END AS status_name,
    t.call_numb,
    t.checkup_date,
    t.visit_date,
    t.employer AS employer_id,
    d_pkg_str_tools.fio(t6.surname::character varying, t6.firstname::character varying, t6.lastname::character varying) AS employer,
    t7.id AS persmedcard,
    t.cablab,
    t.profcard
   FROM d_wl_records74 t
     LEFT JOIN d_agents t1 ON t1.id = t.agent
     JOIN d_services t2 ON t2.id = t.service
     LEFT JOIN d_direction_services t3 ON t3.id = t.dir_serv
     LEFT JOIN d_payment_kind t4 ON t4.id = t.payment_kind
     LEFT JOIN d_employers t5 ON t5.id = t.employer
     LEFT JOIN d_agents t6 ON t6.id = t5.agent
     LEFT JOIN d_persmedcard t7 ON t7.agent = t.agent AND t7.lpu = t.lpu
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'WL_RECORDS74'::text
         LIMIT 1));
```

---

### Вьюха №8: D_V_VMP_LINKS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_VMP_LINKS
 SELECT id,
    version,
    vmp_appl,
    vmp_talon,
    direction
   FROM d_vmp_links t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.version = t.version AND ur.unitcode::text = 'VMP_LINKS'::text
         LIMIT 1));
```

---

### Вьюха №9: D_V_HPK_PLAN_JOURNALS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_PLAN_JOURNALS
 SELECT t.id,
    t.lpu,
    t.hpk_plan,
    t.hpk AS hosp_plan_kind,
    t2.hp_name AS hosp_plan_kind_name,
    t.patient AS patient_id,
    t3.agent AS patient_agent,
    concat(a3.surname, ' ', a3.firstname, ' ', a3.lastname) AS patient,
    a3.sex,
    a3.birthdate AS patient_birthdate,
    t.directed_by AS directed_by_id,
    d_pkg_str_tools.fio(a5.surname::character varying, a5.firstname::character varying, a5.lastname::character varying) AS directed_by,
    t.directed_to AS directed_to_id,
    d_pkg_str_tools.fio(a6.surname::character varying, a6.firstname::character varying, a6.lastname::character varying) AS directed_to,
    t.registered_by AS registered_by_id,
    d_pkg_str_tools.fio(a7.surname::character varying, a7.firstname::character varying, a7.lastname::character varying) AS registered_by,
    t.register_date,
    t.has_privileges,
    t10.vs_name AS shas_privileges,
    t.operation AS operation_id,
    t4.se_name AS operation,
    t.cid,
    t.direction,
    t.payment_kind AS payment_kind_id,
    concat(t8.pk_code, ' ', t8.pk_name) AS payment_kind,
    t8.pk_name AS payment_kind_name,
    t8.pk_code AS payment_kind_code,
    t8.is_commerc AS payment_kind_is_commerc,
    t.is_ready,
    t.hh_direction_date,
    t.is_oper,
    t.diseasecase,
        CASE
            WHEN t.is_oper = 0::numeric OR check_null(t.is_oper::character varying, 0::character varying) THEN 'Консервативный'::character varying
            WHEN t.is_oper = 1::numeric OR check_null(t.is_oper::character varying, 1::character varying) THEN 'Оперативный'::character varying
            ELSE NULL::character varying
        END AS is_oper_mnemo,
        CASE
            WHEN t.is_ready = 0::numeric OR check_null(t.is_ready::character varying, 0::character varying) THEN 'Не готов'::character varying
            WHEN t.is_ready = 1::numeric OR check_null(t.is_ready::character varying, 1::character varying) THEN 'Готов'::character varying
            ELSE NULL::character varying
        END AS is_ready_mnemo,
    t2.hp_name,
    t.comments,
    d_pkg_agent_addrs.get_reg_raion(a3.id::numeric, t1.plan_date::timestamp without time zone, 0::numeric) AS raion_id,
    d_pkg_agent_addrs.get_reg_raion(a3.id::numeric, t1.plan_date::timestamp without time zone, 2::numeric) AS raion_name,
    t9.id AS hosp_history,
    t9.discard_status AS hosp_history_ds,
    t1.plan_date,
    t9.hh_pref AS hosp_history_pref,
    t9.hh_numb AS hosp_history_numb,
    t9.hh_numb_altern AS hosp_history_numb_alt,
    t9.hh_numb_full AS hosp_history_numb_full,
    t9.mkb_send AS hosp_history_mkb_send,
    t9.mkb_send_exact AS hosp_history_mkb_send_exact,
    t9.date_in AS hosp_history_date_in,
    t9.hospitalization_type AS hosp_history_hosp_type,
    t.quota_q,
    t.hpk,
    t.record_status,
        CASE
            WHEN t.record_status = 0::numeric THEN 'Не отработана'::character varying
            ELSE 'Отработана'::character varying
        END AS record_status_mnemo,
    t.record_numb,
    t.record_pref,
    concat(t.record_pref, '-', t.record_numb) AS record_pref_numb,
    t.rl_record,
    t.sch_resource,
    t2.hp_code AS hosp_plan_kind_code,
    t11.hosp_plan_date,
    t12.id AS dep_id,
    t12.dp_code AS dep_code,
    t12.dp_name AS dep,
    t13.id AS employment_status_id,
    t13.es_code AS employment_status,
    t13.es_name AS employment_status_name,
    t.date_actual,
    t.date_end_serv,
    t.rc_record,
    t.contract,
    t.alcohol_date,
    t.alcohol_res,
    t.drug_date,
    t.drug_res,
    t.direction_status
   FROM d_hpk_plan_journals t
     CROSS JOIN d_hosp_plan_kinds t2
     CROSS JOIN d_persmedcard t3
     CROSS JOIN d_agents a3
     CROSS JOIN d_employers t7
     CROSS JOIN d_hpkpj_vmp_states t10
     CROSS JOIN d_directions t11
     LEFT JOIN d_hpk_plans t1 ON t1.id = t.hpk_plan
     LEFT JOIN d_services t4 ON t4.id = t.operation
     LEFT JOIN d_employers t5 ON t5.id = t.directed_by
     LEFT JOIN d_agents a5 ON a5.id = t5.agent
     LEFT JOIN d_employers t6 ON t6.id = t.directed_to
     LEFT JOIN d_agents a6 ON a6.id = t6.agent
     LEFT JOIN d_agents a7 ON a7.id = t7.agent
     LEFT JOIN d_payment_kind t8 ON t8.id = t.payment_kind
     LEFT JOIN ( SELECT hh.id,
            hh.discard_status,
            hh.hh_pref,
            hh.hh_numb,
            hh.hh_numb_altern,
            hh.hpk_plan_journal,
            hh.mkb_send,
            hh.mkb_send_exact,
            hh.hh_numb_full,
            hh.hospitalization_type,
            hh.date_in
           FROM d_hosp_histories hh
          WHERE hh.discard_status = 0::numeric AND hh.relative_hh IS NULL) t9 ON t.id = t9.hpk_plan_journal
     LEFT JOIN d_deps t12 ON t12.id = t11.hosp_dep
     LEFT JOIN d_employment_status t13 ON t13.id = t.employment_status
  WHERE true = true AND t2.id = t.hpk AND t3.id = t.patient AND a3.id = t3.agent AND true = true AND true = true AND true = true AND true = true AND true = true AND t7.id = t.registered_by AND true = true AND true = true AND true = true AND t10.vs_code = t.has_privileges AND t11.id = t.direction AND true = true AND true = true AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'HPK_PLAN_JOURNALS'::text));
```

---

### Вьюха №10: D_V_DIRECTIONS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_DIRECTIONS_BASE
 SELECT id,
    lpu,
    outer_direction,
    lpu_to,
    lpu_to_handle,
    patient,
    reg_visit,
    reg_employer,
    reg_date,
    dir_comment,
    reg_type,
    dir_type,
    hosp_mkb,
    hosp_kind,
    dir_numb,
    speciality,
    ex_cause_mkb,
    injure_kind,
    injure_time,
    direction_kind,
    hosp_dep,
    dir_pref,
    hosp_bed_type,
    mes,
    reg_hpkpj,
    hosp_reason,
    is_canceled,
    canc_reason,
    canc_employer,
    canc_employer_fio,
    canc_date,
    reg_dir_serv,
    hosp_plan_date,
    date_tr,
    hosp_depdict,
    doc_comment,
    hosp_type,
    talon_vmp_date,
    hosp_direct_type,
    hosp_reason_streetkids,
    transportation_kind,
    hosp_hour,
    reg_dep,
    hosp_mkb_exact,
    talon_vmp_num,
    is_onko,
    realised_days,
    dir_form,
    therapy_schemes,
    direction_form,
    type_med_help,
    direction_condition,
    direction_reason,
    vmp
   FROM d_directions t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'DIRECTIONS'::text
         LIMIT 1));
```

---

### Вьюха №11: D_V_HPK_PLANS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_PLANS
 SELECT t.id,
    t.lpu,
    t.pid,
    t1.hp_name AS plan_kind,
    t.plan_date,
    to_char(t.plan_date, 'DAY'::text, 'NLS_DATE_LANGUAGE=AMERICAN'::text) AS plan_day_eng,
    initcap(to_char(t.plan_date, 'DAY'::text, 'NLS_DATE_LANGUAGE=RUSSIAN'::text)) AS plan_day_rus,
    t.male_count,
        CASE
            WHEN t.male_count = NULL::numeric OR check_null(t.male_count::character varying, NULL::character varying) THEN NULL::numeric
            ELSE t.gen_count - t.male_count
        END AS female_count,
    t.oper_count,
        CASE
            WHEN t.oper_count = NULL::numeric OR check_null(t.oper_count::character varying, NULL::character varying) THEN NULL::numeric
            ELSE t.gen_count - t.oper_count
        END AS cons_count,
    t.gen_count,
        CASE
            WHEN t.gen_count = NULL::numeric OR check_null(t.gen_count::character varying, NULL::character varying) THEN concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 0::numeric), '(неогр.)')::character varying
            ELSE concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 0::numeric), '(', t.gen_count, ')')::character varying
        END AS gen_count_s,
        CASE
            WHEN t.male_count = NULL::numeric OR check_null(t.male_count::character varying, NULL::character varying) THEN concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 1::numeric), '(неогр.)')::character varying
            ELSE concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 1::numeric), '(', t.male_count, ')')::character varying
        END AS male_count_s,
        CASE
            WHEN t.male_count = NULL::numeric OR check_null(t.male_count::character varying, NULL::character varying) THEN concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 2::numeric), '(неогр.)')::character varying
            ELSE concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 2::numeric), '(', t.gen_count - t.male_count, ')')::character varying
        END AS female_count_s,
        CASE
            WHEN t.oper_count = NULL::numeric OR check_null(t.oper_count::character varying, NULL::character varying) THEN concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 3::numeric), '(неогр.)')::character varying
            ELSE concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 3::numeric), '(', t.oper_count, ')')::character varying
        END AS oper_count_s,
        CASE
            WHEN t.oper_count = NULL::numeric OR check_null(t.oper_count::character varying, NULL::character varying) THEN concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 4::numeric), '(неогр.)')::character varying
            ELSE concat(d_pkg_hpk_plan_journals.get_info(t.lpu::numeric, t1.id::numeric, t.plan_date::timestamp without time zone, 1::numeric, 4::numeric), '(', t.gen_count - t.oper_count, ')')::character varying
        END AS con_count_s,
    t.cid
   FROM d_hpk_plans t
     CROSS JOIN d_hosp_plan_kinds t1
  WHERE t1.id = t.pid AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'HPK_PLANS'::text));
```

---

### Вьюха №12: D_V_HPK_PLANS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_PLANS_BASE
 SELECT t.id,
    t.lpu,
    t.pid,
    t1.hp_name AS plan_kind,
    t.plan_date,
    to_char(t.plan_date, 'DAY'::text, 'NLS_DATE_LANGUAGE=AMERICAN'::text) AS plan_day_eng,
    initcap(to_char(t.plan_date, 'DAY'::text, 'NLS_DATE_LANGUAGE=RUSSIAN'::text)) AS plan_day_rus,
    t.male_count,
        CASE
            WHEN t.male_count = NULL::numeric OR check_null(t.male_count::character varying, NULL::character varying) THEN NULL::numeric
            ELSE t.gen_count - t.male_count
        END AS female_count,
    t.oper_count,
        CASE
            WHEN t.oper_count = NULL::numeric OR check_null(t.oper_count::character varying, NULL::character varying) THEN NULL::numeric
            ELSE t.gen_count - t.oper_count
        END AS cons_count,
    t.gen_count,
    t.cid
   FROM d_hpk_plans t
     JOIN d_hosp_plan_kinds t1 ON t1.id = t.pid
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'HPK_PLANS'::text));
```

---

### Вьюха №13: D_V_PERSMEDCARD

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_PERSMEDCARD
 SELECT p.id,
    p.cid,
    p.lpu,
    p.card_numb,
    p.agent,
    a.firstname,
    a.surname,
    a.lastname,
    a.birthdate,
    a.deathdate,
        CASE
            WHEN a.sex = 0::numeric OR check_null(a.sex::character varying, 0::character varying) THEN 'Женский'::character varying
            WHEN a.sex = 1::numeric OR check_null(a.sex::character varying, 1::character varying) THEN 'Мужской'::character varying
            ELSE ''::character varying
        END AS sex,
    a.sex AS nsex,
    a.birthplace,
    p.createdate,
    p.moddate,
    p.note,
    r.rh_name AS rhesus,
    p.rhesus AS nrhesus,
    p.bloodgroupe AS bloodgroupe_id,
    b.bg_code AS bloodgroupe,
    p.ecolor,
    a.snils,
    p.reg_division AS reg_division_id,
    d.div_code AS reg_division,
    p.tp_printed,
        CASE
            WHEN p.tp_printed = 0::numeric THEN 'Нет'::character varying
            WHEN p.tp_printed = 1::numeric THEN 'Да'::character varying
            ELSE NULL::character varying
        END AS tp_printed_mnemo,
    p.pmc_type,
        CASE
            WHEN p.pmc_type = 1::numeric THEN 'аноним'::character varying
            WHEN p.pmc_type = 2::numeric THEN 'новорожденный'::character varying
            WHEN p.pmc_type = 3::numeric THEN 'неизвестный'::character varying
            WHEN p.pmc_type IS NULL THEN 'пациент'::character varying
            ELSE NULL::character varying
        END AS pmc_type_name,
    a.enp,
    p.outnumb,
    p.ia_printed,
        CASE
            WHEN p.ia_printed = 0::numeric THEN 'Нет'::character varying
            ELSE 'Да'::character varying
        END AS ia_printed_mnemo,
    p.sms_agree,
        CASE
            WHEN p.sms_agree = 0::numeric THEN 'Нет'::character varying
            ELSE 'Да'::character varying
        END AS sms_agree_mnemo,
    d_pkg_str_tools.fio(ae.surname::character varying, ae.firstname::character varying, ae.lastname::character varying) AS emp_fio,
    p.emp_id,
    NULL::text AS marker_id,
    NULL::text AS marker_code,
    NULL::text AS marker_name,
    NULL::text AS marker_description,
    p.email_agree,
    p.phenotype,
    a.education AS education_id,
    c.cl_name,
    a.according_relatives,
    a.nation AS agent_nation
   FROM d_persmedcard p
     JOIN d_agents a ON a.id = p.agent
     LEFT JOIN d_bloodgroupe b ON b.id = p.bloodgroupe
     LEFT JOIN d_divisions d ON d.id = p.reg_division
     LEFT JOIN d_rhesus r ON r.rh_code = p.rhesus
     LEFT JOIN d_employers e ON e.id = p.emp_id
     LEFT JOIN d_agents ae ON ae.id = e.agent
     LEFT JOIN d_cablab c ON c.id = p.cablab
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = p.cid AND ur.unitcode::text = 'PERSMEDCARD'::text
         LIMIT 1));
```

---

### Вьюха №14: D_V_LPU

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_LPU
 SELECT l.id,
    l.fullname,
    l.headdoctor_fullname AS headdoctor_fullname_old,
    d_pkg_headdoctor.get_actual_on_date(l.id::numeric, sysdate()) AS headdoctor_id,
    COALESCE(d_pkg_headdoctor.get_actual_on_date(l.id::numeric, sysdate(), 'SURNAME FIRSTNAME LASTNAME'::character varying), l.headdoctor_fullname) AS headdoctor_fullname,
    d_pkg_headdoctor.get_actual_on_date(l.id::numeric, sysdate(), 'FIO'::character varying) AS headdoctor_fio,
    l.fulladdress,
    l.phones,
    l.website,
    l.rec_ser_priv,
    l.rec_ser,
    l.code_lpu,
    l.code_ogrn,
    l.code_okpo,
    l.code_okdp,
    l.code_okonh,
    l.code_okato,
    l.code_okogu,
    l.code_ocopph,
    l.code_okfs,
    l.bookkeeper_fullname,
    l.headeconomist_fullname,
    ld.headdoct AS lpudict_headdoct,
    ld.bookkeeper AS lpudict_bookkeeper,
    ld.lpu_code AS lpudict,
    ld.id AS lpudict_id,
    ld.lpu_name AS lpudict_name,
    ld.lpu_fullname AS lpudict_fullname,
    l.geografy AS geografy_id,
    g.geoname AS geografy,
    g.geofull,
    ld.agent AS agent_id,
    l.userforms,
    l.gennumb_group,
    l.exec_authority,
    l.rec_ser_priv_88,
    l.ip_addr,
    ld.version,
    ld.is_resp,
    l.by_es_only,
    ld.date_b AS lpudict_date_b,
    ld.date_e AS lpudict_date_e,
    COALESCE(ld.date_e, sysdate()) AS lpudict_date_e_sysdate,
    l.address
   FROM d_lpu l
     LEFT JOIN d_lpudict ld ON ld.id = l.lpudict
     LEFT JOIN d_geografy g ON g.id = l.geografy
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE COALESCE(ur.lpu, - 1::bigint) = (- 1::bigint) AND COALESCE(ur.version, - 1::bigint) = (- 1::bigint) AND ur.unitcode::text = 'LPU'::text
         LIMIT 1));
```

---

### Вьюха №15: D_V_CABLAB

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CABLAB
 SELECT c.id,
    c.lpu,
    c.pid,
    c.cid,
    c.cl_code,
    c.cl_name,
    c.department AS department_id,
    d.dp_code AS department,
    d.dp_name AS department_name,
    d.dp_kind AS department_kind,
    d.dp_type AS department_type,
    d.division AS department_division_id,
    c.division AS division_id,
    dv.div_code AS division_code,
    dv.div_name AS division_name,
    c.building AS building_id,
    b.code AS building_code,
    c.floor AS floor_id,
    bf.name AS floor_name,
    c.is_comm,
    c.begin_date,
    c.end_date,
    c.cablab_type,
    ct.cablab_code AS cablab_type_code,
    ct.cablab_type AS cablab_type_name,
    ct.cablab_code,
    c.cl_begin_date,
    c.cl_end_date,
        CASE
            WHEN trunc(c.cl_begin_date) <= trunc(sysdate()) AND (trunc(c.cl_end_date) >= trunc(sysdate()) OR c.cl_end_date IS NULL) THEN 1
            ELSE 0
        END AS is_active
   FROM d_cablab c
     JOIN d_deps d ON d.id = c.department
     LEFT JOIN d_divisions dv ON dv.id = c.division
     LEFT JOIN d_buildings b ON b.id = c.building
     LEFT JOIN d_build_floors bf ON bf.id = c.floor
     LEFT JOIN d_cablab_type ct ON ct.id = c.cablab_type
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = c.cid AND ur.unitcode::text = 'CABLAB'::text
         LIMIT 1));
```

---

### Вьюха №16: D_V_DIRECTIONS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_DIRECTIONS
 SELECT d.id,
    d.lpu,
    d.outer_direction AS outer_direction_id,
    concat(od.d_date, ' ', od.d_numb) AS outer_direction,
    od.represent AS outer_direction_represent_id,
    d.lpu_to AS lpu_to_id,
    ld.lpu_code AS lpu_to,
    ld.lpu_name AS lpu_to_name,
    ld.lpu_fullname AS to_lpu_fullname,
    d.lpu_to_handle,
    d.patient AS patient_id,
    pmc.card_numb AS patient_card,
    pa.surname AS pat_surname,
    pa.firstname AS pat_firstname,
    pa.lastname AS pat_lastname,
    pa.birthdate AS pat_birthdate,
    d_pkg_str_tools.fio(fssurname => pa.surname::character varying, fsname => pa.firstname::character varying, fspatrname => pa.lastname::character varying, fsreturn_full => 1::character varying) AS patient,
    pa.sex AS pat_sex,
    pa.id AS pat_agent_id,
    d.reg_visit AS reg_visit_id,
        CASE
            WHEN d.reg_visit IS NOT NULL THEN ( SELECT concat(to_char(v.visit_date, d_pkg_std.frm_d()::text, 'NLS_DATE_LANGUAGE=RUSSIAN'::text), ' ', ( SELECT vp.vp_content
                       FROM d_visitplaces vp
                      WHERE vp.id = v.visit_place)) AS concat
               FROM d_visits v
              WHERE v.id = d.reg_visit)
            ELSE NULL::text
        END AS reg_visit,
    d.reg_employer AS reg_employer_id,
    ea.surname AS reg_emp_surname,
    ea.firstname AS reg_emp_firstname,
    ea.lastname AS reg_emp_lastname,
    emp.jobtitle AS reg_employer_jbt_id,
    ea.sex AS reg_employer_sex,
    ea.agn_code AS reg_employer_agn,
    emp.kod_vracha AS reg_employer,
    d_pkg_str_tools.fio(fssurname => ea.surname::character varying, fsname => ea.firstname::character varying, fspatrname => ea.lastname::character varying) AS reg_employer_fio,
    emp.agent AS reg_employer_agent,
    emp.speciality AS reg_employer_spec_id,
    espc.title AS reg_employer_spec_name,
    emp.department AS reg_emp_department_id,
    edep.dp_name AS reg_emp_department,
    d.reg_date,
    d.dir_comment,
    d.reg_type,
    ( SELECT drt.drt_name
           FROM d_dir_reg_types drt
          WHERE drt.drt_code = d.reg_type) AS reg_type_mnemo,
    d.dir_type,
    d.hosp_mkb AS hosp_mkb_id,
    mkb.mkb_code AS hosp_mkb,
    mkb.mkb_name AS hosp_mkb_name,
    d.hosp_kind AS hosp_kind_id,
    hk.hk_name AS hosp_kind_name,
    hk.hk_code AS hosp_kind,
    d.dir_numb,
    d.speciality AS speciality_id,
    ( SELECT spc.code
           FROM d_specialities spc
          WHERE spc.id = d.speciality AND spc.version::numeric = d_pkg_versions.get_version_by_lpu(pnraise => 0::numeric, pnlpu => d.lpu::numeric, psunitcode => 'SPECIALITIES'::character varying)::bigint::numeric) AS speciality,
    ( SELECT max(hh.date_in) AS max
           FROM d_hpk_plan_journals hpkj
             CROSS JOIN d_hosp_histories hh
          WHERE hpkj.direction = d.id AND hh.hpk_plan_journal = hpkj.id) AS last_hosp_date,
    d.ex_cause_mkb AS ex_cause_mkb_id,
    ( SELECT ecmkb.mkb_code
           FROM d_mkb10 ecmkb
          WHERE ecmkb.id = d.ex_cause_mkb) AS ex_cause_mkb,
    d.injure_kind AS injure_kind_id,
    injk.ik_code AS injure_kind,
    injk.ik_name AS injure_kind_name,
    d.injure_time,
    d.direction_kind AS direction_kind_id,
    dk.dk_code AS direction_kind_code,
    dk.dk_name AS direction_kind,
    d.hosp_dep AS hosp_dep_id,
    dep.dp_code AS hosp_dep,
    dep.dp_name AS hosp_dep_name,
    d.dir_pref,
    d.hosp_bed_type AS hosp_bed_type_id,
    bedt.bt_code AS hosp_bed_type,
    bedt.bt_name AS hosp_bed_type_name,
    d.mes AS mes_id,
    ( SELECT mes.m_code
           FROM d_meses mes
          WHERE mes.id = d.mes) AS mes,
    d.reg_hpkpj,
    d.hosp_reason AS hosp_reason_id,
    hr.hr_code AS hosp_reason,
    hr.hr_name AS hosp_reason_name,
    d.is_canceled,
    d.canc_reason AS canc_reason_id,
    dcr.dcr_code AS canc_reason,
    dcr.dcr_name AS canc_reason_name,
    d.canc_employer,
    COALESCE(d.canc_employer_fio, d_pkg_str_tools.fio(fssurname => cea.surname::character varying, fsname => cea.firstname::character varying, fspatrname => cea.lastname::character varying)) AS canc_employer_fio,
    d.canc_date,
    d.reg_dir_serv,
    d.hosp_plan_date,
    d.date_tr,
    d.hosp_depdict AS hosp_depdict_id,
    dd.dep_code AS hosp_depdict,
    dd.dep_name AS hosp_depdict_name,
    d.doc_comment,
    d.hosp_type AS hosp_type_id,
    ht.hk_code AS hosp_type,
    ht.hk_name AS hosp_type_name,
    d.talon_vmp_date,
    d.hosp_direct_type AS hosp_direct_type_id,
    hdt.dt_code AS hosp_direct_type,
    hdt.dt_name AS hosp_direct_type_name,
    d.hosp_reason_streetkids,
    d.transportation_kind AS transportation_kind_id,
    tk.tk_code AS transportation_kind,
    tk.tk_name AS transportation_kind_name,
    d.hosp_hour,
    ( SELECT hhour.hour_name
           FROM d_hosp_hours hhour
          WHERE hhour.hour_code = d.hosp_hour) AS hosp_hour_name,
    d.hosp_mkb_exact,
    d.talon_vmp_num,
    d.is_onko,
    d.reg_dep AS reg_dep_id,
    d.realised_days,
    d.dir_form,
    emp.quot_resource AS reg_emp_quot_resource,
    edep.division AS reg_emp_division,
    d.therapy_schemes AS med_therapy_scheme_id,
    mts.code AS med_therapy_scheme_code,
    d.vmp,
    d.direction_form AS direction_form_id,
    df.ds_code AS direction_form_code,
    df.ds_name AS direction_form_name,
    d.type_med_help AS type_med_help_id,
    tmh.code AS type_med_help_code,
    tmh.name AS type_med_help_name,
    d.direction_condition AS direction_condition_id,
    dc.dc_code AS direction_condition_code,
    dc.dc_name AS direction_condition_name,
    d.direction_reason
   FROM d_directions d
     JOIN d_persmedcard pmc ON pmc.id = d.patient
     JOIN d_agents pa ON pa.id = pmc.agent
     LEFT JOIN d_outer_directions od ON od.id = d.outer_direction
     LEFT JOIN d_lpudict ld ON ld.id = d.lpu_to
     LEFT JOIN d_employers emp ON emp.id = d.reg_employer
     LEFT JOIN d_agents ea ON ea.id = emp.agent
     LEFT JOIN d_specialities espc ON espc.id = emp.speciality
     LEFT JOIN d_deps edep ON edep.id = emp.department
     LEFT JOIN d_mkb10 mkb ON mkb.id = d.hosp_mkb
     LEFT JOIN d_hospitalizationkinds hk ON hk.id = d.hosp_kind AND hk.version::numeric = d_pkg_versions.get_version_by_lpu(pnraise => 0::numeric, pnlpu => d.lpu::numeric, psunitcode => 'HOSPITALIZATIONKINDS'::character varying)::bigint::numeric
     LEFT JOIN d_injure_kinds injk ON injk.id = d.injure_kind
     LEFT JOIN d_direction_kinds dk ON dk.id = d.direction_kind
     LEFT JOIN d_deps dep ON dep.id = d.hosp_dep
     LEFT JOIN d_bed_types bedt ON bedt.id = d.hosp_bed_type
     LEFT JOIN d_hosp_reasons hr ON hr.id = d.hosp_reason
     LEFT JOIN d_dir_canc_reasons dcr ON dcr.id = d.canc_reason
     LEFT JOIN d_employers cemp ON cemp.id = d.canc_employer
     LEFT JOIN d_agents cea ON cea.id = cemp.agent
     LEFT JOIN d_depdict dd ON dd.id = d.hosp_depdict
     LEFT JOIN d_hospitalization_types ht ON ht.id = d.hosp_type
     LEFT JOIN d_hosp_direct_types hdt ON hdt.id = d.hosp_direct_type
     LEFT JOIN d_transportation_kinds tk ON tk.id = d.transportation_kind
     LEFT JOIN d_med_therapy_schemes mts ON mts.id = d.therapy_schemes
     LEFT JOIN d_direction_forms df ON df.id = d.direction_form
     LEFT JOIN d_type_med_help tmh ON tmh.id = d.type_med_help
     LEFT JOIN d_direction_conditions dc ON dc.id = d.direction_condition
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = d.lpu AND ur.unitcode::text = 'DIRECTIONS'::text
         LIMIT 1));
```

---

### Вьюха №17: D_V_OUTER_DIRECTIONS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_OUTER_DIRECTIONS
 SELECT od.id,
    od.lpu,
    od.patient AS patient_id,
    p.card_numb AS patient_card,
    concat(a.surname, ' ', a.firstname, ' ', a.lastname) AS patient,
    od.d_date,
    od.d_numb,
    od.represent AS represent_id,
    ld.id AS lpudict_id,
    ld.lpu_code AS represent_code,
    ld.lpu_name AS represent_lpu_name,
    ld.lpu_fullname AS represent_lpu_fullname,
    ld.date_b AS lpu_date_b,
    ld.date_e AS lpu_date_e,
    ar.agn_okpo AS represent_okpo,
    ar.agn_name AS represent,
    od.represent_handle,
    od.diagnosis AS diagnosis_id,
    mkb.mkb_code AS diagnosis,
    mkb.mkb_name AS diagnosis_name,
    od.diagnosis_handle,
    od.doctor AS doctor_id,
    ad.agn_code AS doctor,
    ad.snils AS doctor_snils,
        CASE
            WHEN od.doctor = NULL::bigint OR check_null(od.doctor::character varying, NULL::character varying) THEN NULL::character varying
            ELSE d_pkg_str_tools.fio(ad.surname::character varying, ad.firstname::character varying, ad.lastname::character varying)
        END AS doctor_fio,
    od.doctor_handle,
    ( SELECT max(dir.reg_date) AS max
           FROM d_directions dir
          WHERE dir.outer_direction = od.id) AS last_dir_date,
    od.d_pref,
    od.represent_direction,
    od.hosp_plan_date,
    od.doc_speciality,
    s.code AS doc_speciality_code,
    od.reason AS reason_id,
    rd.name_reason AS reason,
    od.d_date_end,
    od.service,
    sv.se_code AS service_code,
    sv.se_name AS service_name,
    sv.se_type AS service_type,
    od.ex_system,
    es.s_code AS ex_system_code,
    ar.agn_ogrn AS represent_ogrn,
    a.id AS agent_id,
    od.diagnosis_exact,
    od.include_result,
    od.department,
    od.jobtitle AS jobtitle_id,
    j.title AS jobtitle,
    od.outdir_type,
    od.diseasecharacter,
    od.to_doctor,
    od.speciality,
    sp.code AS speciality_code,
    sp.title AS speciality_title,
    od.profile AS profile_id,
    ep.p_name AS profile,
    od.sch_resource,
    od.direction_form AS direction_form_id,
    df.ds_code AS direction_form_code,
    df.ds_name AS direction_form_name,
    od.type_med_help AS type_med_help_id,
    tmh.code AS type_med_help_code,
    tmh.name AS type_med_help_name,
    od.direction_condition AS direction_condition_id,
    dc.dc_code AS direction_condition_code,
    dc.dc_name AS direction_condition_name,
    od.direction_reason,
    od.employer,
    od.dir_note
   FROM d_outer_directions od
     JOIN d_persmedcard p ON p.id = od.patient
     JOIN d_agents a ON p.agent = a.id
     LEFT JOIN d_agents ar ON ar.id = od.represent
     LEFT JOIN d_mkb10 mkb ON mkb.id = od.diagnosis
     LEFT JOIN d_lpudict ld ON ld.agent = ar.id
     LEFT JOIN d_agents ad ON ad.id = od.doctor
     LEFT JOIN d_specialities s ON s.id = od.doc_speciality
     LEFT JOIN d_reason_direction rd ON rd.id = od.reason
     LEFT JOIN d_ex_systems es ON es.id = od.ex_system
     LEFT JOIN d_services sv ON sv.id = od.service
     LEFT JOIN d_jobtitles j ON j.id = od.jobtitle
     LEFT JOIN d_specialities sp ON sp.id = od.speciality
     LEFT JOIN d_er_profiles ep ON ep.id = od.profile
     LEFT JOIN d_direction_forms df ON df.id = od.direction_form
     LEFT JOIN d_type_med_help tmh ON tmh.id = od.type_med_help
     LEFT JOIN d_direction_conditions dc ON dc.id = od.direction_condition
     LEFT JOIN d_cablab c ON c.id = p.cablab
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = od.lpu AND ur.unitcode::text = 'OUTER_DIRECTIONS'::text
         LIMIT 1));
```

---

### Вьюха №18: D_V_HOSP_HISTORY_DEPS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HOSP_HISTORY_DEPS
 SELECT t.id,
    t.pid,
    t.date_in,
    t.date_out,
    t.dep AS dep_id,
    t1.dp_code AS dep,
    t1.dp_name AS dep_name,
    t1.division AS dep_division,
    t1.dp_type AS dp_type_id,
    t3.dt_code AS dp_type,
    t3.dt_name AS dp_name,
    t3.id AS deps_type_id,
    t.lpu,
    t.mkb AS mkb_id,
    t2.mkb_code AS mkb,
    t2.mkb_name,
    t.mkb_exact,
    t12.mkb_clinic_exact,
    t.healing_emp,
    t8.agent AS healing_emp_agent,
    t8.kod_vracha AS healing_emp_kod,
    a.surname AS healing_emp_surname,
    a.firstname AS healing_emp_firstname,
    a.lastname AS healing_emp_lastname,
    t.payment_kind AS payment_kind_id,
    t4.pk_code AS payment_kind,
    t4.pk_name AS payment_kind_name,
    t4.is_commerc AS payment_kind_is_commerc,
    t.hosp_result AS hosp_result_id,
    t5.r_code AS hosp_result,
    t5.r_name AS hosp_result_name,
        CASE
            WHEN t.hosp_result IS NOT NULL THEN concat(t5.r_code, ' - ', t5.r_name)::character varying
            ELSE NULL::character varying
        END AS hosp_result_codename,
    t.ksg AS ksg_id,
    t6.ksg_code AS ksg,
    t.bed_type AS bed_type_id,
    t7.bt_code AS bed_type,
    t7.bt_name AS bed_type_name,
    d_pkg_str_tools.fio(a.surname::character varying, a.firstname::character varying, a.lastname::character varying) AS fio,
    COALESCE(( SELECT d_pkg_str_tools.fio(an.surname::character varying, an.firstname::character varying, an.lastname::character varying) AS fio
           FROM d_agent_names an
          WHERE an.pid = a.id AND an.begin_date <= trunc(t.date_out) AND (an.end_date >= trunc(t.date_out) OR an.end_date IS NULL)), d_pkg_str_tools.fio(a.surname::character varying, a.firstname::character varying, a.lastname::character varying)) AS fio_actual,
    COALESCE(t12.mkb_clinic, t12.mkb_receive, t12.mkb_send) AS diagnosis_id,
    m1.mkb_code AS diagnosis_code,
    m1.mkb_name AS diagnosis_name,
    t12.discard_status,
    t12.patient,
    t12.diseasecase,
    t.facial_account,
    t.hhd_pref,
    t.hhd_numb,
    t.vmp AS vmp_id,
    t13.vmp_code AS vmp,
    t13.vmp_name,
    t.prvsid,
    t.hhd_level,
    t.hosp_outcome AS hosp_outcome_id,
    t14.r_code AS hosp_outcome,
    t14.r_name AS hosp_outcome_name,
        CASE
            WHEN t.hosp_outcome IS NOT NULL THEN concat(t14.r_code, ' - ', t14.r_name)::character varying
            ELSE NULL::character varying
        END AS hosp_outcome_codename,
    t.is_last,
    t12.date_in AS hosp_history_date_in,
    t.mts_desc,
    t16.code AS mts_code,
    t16.name AS mts_name,
    t16.description AS mts_description,
    t.alv,
    t17.alv_code,
    t17.alv_name,
    t.scale_rehab,
    t18.sr_code,
    t18.sr_name,
    t.ksgid
   FROM d_hosp_history_deps t
     JOIN d_hosp_histories t12 ON t12.id = t.pid
     JOIN d_deps t1 ON t1.id = t.dep
     JOIN d_deps_types t3 ON t3.id = t1.dp_type
     JOIN d_payment_kind t4 ON t4.id = t.payment_kind
     LEFT JOIN d_mkb10 t2 ON t2.id = t.mkb
     LEFT JOIN d_hosp_results t5 ON t5.id = t.hosp_result
     LEFT JOIN d_ksgcodes t6 ON t6.id = t.ksg
     LEFT JOIN d_bed_types t7 ON t7.id = t.bed_type
     LEFT JOIN d_employers t8 ON t8.id = t.healing_emp
     LEFT JOIN d_agents a ON a.id = t8.agent
     LEFT JOIN d_mkb10 m1 ON m1.id = COALESCE(t12.mkb_clinic, COALESCE(t12.mkb_receive, t12.mkb_send))
     LEFT JOIN d_vmp t13 ON t13.id = t.vmp
     LEFT JOIN d_hosp_outcomes t14 ON t14.id = t.hosp_outcome
     LEFT JOIN d_med_therapy_schemes_desc t15 ON t15.id = t.mts_desc
     LEFT JOIN d_med_therapy_schemes t16 ON t16.id = t15.pid
     LEFT JOIN d_alv t17 ON t17.id = t.alv
     LEFT JOIN d_scale_rehab t18 ON t18.id = t.scale_rehab
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'HOSP_HISTORY_DEPS'::text));
```

---

### Вьюха №19: D_V_HOSP_HISTORIES

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HOSP_HISTORIES
 SELECT t.id,
    t.lpu,
    t.hpk_plan_journal,
    t.patient AS patient_id,
    t1.card_numb AS patient,
    t1.agent AS patient_agent,
    concat(t1a.surname, ' ', t1a.firstname, ' ', t1a.lastname) AS patient_fio,
    t1a.surname AS patient_surname,
    t1a.firstname AS patient_firstname,
    t1a.lastname AS patient_lastname,
    t1a.birthdate AS patient_birthdate,
    t1a.sex AS patient_sex,
    t1a.snils AS patient_snils,
    t.hh_pref,
    t.hh_numb,
    t.hh_numb_type,
    t.hh_numb_altern,
    t.hosp_reason AS hosp_reason_id,
    t2.hr_code AS hosp_reason,
    t2.hr_name AS hosp_reason_name,
    t.reception_emp,
    t.date_in,
    t.plan_date_out,
    t.date_out,
    t.hospitalization_type AS hospitalization_type_id,
    t3.hk_code AS hospitalization_type,
    t3.hk_name AS hospitalization_type_name,
    t.transportation_kind AS transportation_kind_id,
    t4.tk_code AS transportation_kind,
    t4.tk_name AS transportation_kind_name,
    t.lpu_from AS lpu_from_id,
    t5.lpu_code AS lpu_from,
    t5.lpu_name AS lpu_from_name,
    t.mkb_send AS mkb_send_id,
    t6.mkb_code AS mkb_send,
    t6.mkb_name AS mkb_send_name,
    t.mkb_send_exact,
    t.mkb_clinic AS mkb_clinic_id,
    t8.mkb_code AS mkb_clinic,
    t8.mkb_name AS mkb_clinic_name,
    t.mkb_clinic_exact,
    t.mkb_clinic_date,
    t.mkb_final AS mkb_final_id,
    t10.mkb_code AS mkb_final,
    t10.mkb_name AS mkb_final_name,
    t.mkb_final_exact,
    t.mkb_fin_comp AS mkb_fin_comp_id,
    t12.mkb_code AS mkb_fin_comp,
    t12.mkb_name AS mkb_fin_comp_name,
    t.mkb_fin_comp_exact,
    t.mkb_fin_add AS mkb_fin_add_id,
    t14.mkb_code AS mkb_fin_add,
    t14.mkb_name AS mkb_fin_add_name,
    t.mkb_fin_add_exact,
    t.hosp_times,
    t.hosp_result AS hosp_result_id,
    t16.r_code AS hosp_result,
    t16.r_name AS hosp_result_name,
    t.mkb_receive AS mkb_receive_id,
    t17.mkb_code AS mkb_receive,
    t17.mkb_name AS mkb_receive_name,
    t.mkb_receive_exact,
    t.relative AS relative_id,
        CASE
            WHEN t.relative IS NULL THEN NULL::character varying
            ELSE ( SELECT d_pkg_str_tools.fio(t19a.surname::character varying, t19a.firstname::character varying, t19a.lastname::character varying) AS fio
               FROM d_agent_relatives t19
                 JOIN d_agents t19a ON t19a.id = t19.agent
              WHERE t19.id = t.relative)
        END AS relative,
    t.diseasecase,
    t.discard_status,
    t.is_well_timed_hosp,
    t.is_enough_volume,
    t.is_correct_healing,
    t.is_same_diagn,
    t20.direction,
    t20.payment_kind,
    t20.has_privileges,
    t20.register_date,
    t20.hh_direction_date,
    t22.vs_name AS has_privileges_mnemo,
    t.arch_date,
    t.arch_dep AS arch_dep_id,
    t21.dp_code AS arch_dep,
    t21.dp_name AS arch_dep_name,
    t.arch_comment,
    t.hh_type,
    t.hosp_hour AS hosp_hour_id,
    t24.hour_name AS hosp_hour,
    t.hosp_outcome AS hosp_outcome_id,
    t23.r_code AS hosp_outcome,
    t23.r_name AS hosp_outcome_name,
    t.transfer_lpu,
    t.transfer_reason,
    t25.lpu_code AS transfer_lpu_code,
    t25.lpu_name AS transfer_lpu_name,
    t26.tr_name AS transfer_reason_name,
    t.hh_numb_full,
    t.hh_numb_mask,
    t.ability_status AS ability_status_id,
    t27.as_name AS ability_status,
    t.other_therapy,
    t.features,
    t.date_departure,
    t.relative_hh,
    t.abandonment,
    t.death_came AS death_came_id,
    t28.dd_code AS death_came_code,
    t28.dd_name AS death_came_name,
    t.arrive_order,
    t.judge_decision,
    t.hosp_income AS hosp_income_id,
    t29.code AS hosp_income,
    t29.income_name,
    t.hosp_is_first,
    t.seized_items,
    t.novor_num,
    t.direction_hosp AS direction_hosp_id,
    t30.dh_code AS direction_hosp,
    t30.dh_name AS direction_hosp_name,
    t1.note,
    t.attending_empl_id,
    t.department_id
   FROM d_hosp_histories t
     JOIN d_persmedcard t1 ON t1.id = t.patient
     JOIN d_agents t1a ON t1a.id = t1.agent
     JOIN d_hosp_reasons t2 ON t2.id = t.hosp_reason
     JOIN d_hospitalization_types t3 ON t3.id = t.hospitalization_type
     JOIN d_transportation_kinds t4 ON t4.id = t.transportation_kind
     LEFT JOIN d_lpudict t5 ON t5.id = t.lpu_from
     LEFT JOIN d_mkb10 t6 ON t6.id = t.mkb_send
     LEFT JOIN d_mkb10 t8 ON t8.id = t.mkb_clinic
     LEFT JOIN d_mkb10 t10 ON t10.id = t.mkb_final
     LEFT JOIN d_mkb10 t12 ON t12.id = t.mkb_fin_comp
     LEFT JOIN d_mkb10 t14 ON t14.id = t.mkb_fin_add
     LEFT JOIN d_hosp_results t16 ON t16.id = t.hosp_result
     LEFT JOIN d_mkb10 t17 ON t17.id = t.mkb_receive
     LEFT JOIN d_deps t21 ON t21.id = t.arch_dep
     LEFT JOIN d_hpk_plan_journals t20 ON t20.id = t.hpk_plan_journal
     LEFT JOIN d_hpkpj_vmp_states t22 ON t22.vs_code = t20.has_privileges
     LEFT JOIN d_hosp_outcomes t23 ON t23.id = t.hosp_outcome
     LEFT JOIN d_hosp_hours t24 ON t24.hour_code = t.hosp_hour
     LEFT JOIN d_lpudict t25 ON t25.id = t.transfer_lpu
     LEFT JOIN d_hh_transfer_reasons t26 ON t26.id = t.transfer_reason
     LEFT JOIN d_ability_status t27 ON t27.id = t.ability_status
     LEFT JOIN d_directories_data_ver t28 ON t28.id = t.death_came
     LEFT JOIN d_hosp_incomes t29 ON t29.id = t.hosp_income
     LEFT JOIN d_direction_hosp t30 ON t30.id = t.direction_hosp
  WHERE t.discard_status = 0::numeric AND t.relative_hh IS NULL AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'HOSP_HISTORIES'::text
         LIMIT 1));
```

---

### Вьюха №20: D_V_HPK_SCHEDULE_REG

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_SCHEDULE_REG
 SELECT id,
    direction,
    hpk_schedule,
    date_rec,
    employer,
    date_create
   FROM d_hpk_schedule_reg hsr
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu IS NULL AND ur.version IS NULL AND ur.unitcode::text = 'HPK_SCHEDULE_REG'::text
         LIMIT 1));
```

---

### Вьюха №21: D_V_HOSP_HISTORY_DEPS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HOSP_HISTORY_DEPS_BASE
 SELECT id,
    pid,
    date_in,
    date_out,
    dep,
    lpu,
    mkb,
    mkb_exact,
    healing_emp,
    payment_kind,
    hosp_result,
    ksg,
    bed_type,
    facial_account,
    hhd_pref,
    hhd_numb,
    vmp,
    prvsid,
    hhd_level,
    hosp_outcome,
    is_last,
    mts_desc,
    alv,
    scale_rehab,
    ksgid
   FROM d_hosp_history_deps t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'HOSP_HISTORY_DEPS'::text
         LIMIT 1));
```

---

### Вьюха №22: D_V_CONTRACTS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CONTRACTS_BASE
 SELECT t.id,
    t.lpu,
    t.cid,
    t.contract,
    t.employer AS employer_id,
    t.doc_pref,
    t.doc_numb,
    concat(t.doc_pref, '/', t.doc_numb) AS doc_pref_numb,
    t.doc_date,
    t.ext_numb,
    t.ext_date,
    t.agent AS agent_id,
    t2.agn_code AS agent,
    t2.agn_name AS agent_name,
    t.date_begin,
    t.date_end,
    t.phone,
    t.person,
    t.summ,
    t.is_open,
    t.facial_account,
    t.debt_summ,
    t.contract_summ,
    t.plan_summ,
    t.note,
    t.status,
    t.workdate,
    t.is_imported,
    t.represent AS represent_id,
    t.contract_type
   FROM d_contracts t
     JOIN d_agents t2 ON t2.id = t.agent
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'CONTRACTS'::text
         LIMIT 1));
```

---

### Вьюха №23: D_V_SMP_CALL_EX_SYSTEM

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SMP_CALL_EX_SYSTEM
 SELECT sces.id,
    sces.version,
    sces.lpu_code,
    sces.agent AS agent_id,
    a.surname,
    a.firstname,
    a.lastname,
    sces.lpu_code_smp,
    sces.call_id,
    sces.call_numb,
    sces.call_date,
    sces.call_place,
    sces.call_reason,
    sces.call_structure,
    sces.main_mkb,
    sces.add_mkb,
    sces.complications,
    sces.delivery_type,
    sces.help_on_place,
    sces.help_in_car,
    sces.hosp_status,
        CASE sces.hosp_status
            WHEN 0 THEN 'начало доставки'::character varying
            WHEN 1 THEN 'отмена доставки'::character varying
            WHEN 2 THEN 'факт доставки'::character varying
            WHEN 3 THEN 'принят в приемном покое'::character varying
            WHEN 4 THEN 'госпитализирован'::character varying
            WHEN 5 THEN 'отказано в госпитализации'::character varying
            WHEN 6 THEN 'выписан из стационара'::character varying
            ELSE ''::character varying
        END AS hosp_status_name,
    sces.refuse_reason,
    sces.call_address,
    sces.phone,
    sces.call_type,
    sces.call_status,
        CASE sces.call_status
            WHEN 1 THEN 'новый'::character varying
            WHEN 2 THEN 'отмена'::character varying
            WHEN 3 THEN 'отказ'::character varying
            WHEN 4 THEN 'подтверждение'::character varying
            WHEN 5 THEN 'факт оказания'::character varying
            ELSE ''::character varying
        END AS call_status_name,
    sces.send_time,
    sces.employer,
    sces.refuse_reason_pol,
    sces.who_call,
    sces.call,
    sces.accident_cause,
    sces.profile_teams,
    sces.intoxication,
    sces.complaints,
    sces.anamnesis,
    sces.objectiv_data,
    sces.result,
    sces.sender_person,
    sces.hj_id,
    sces.hpk_id,
    sces.hh_id,
    d.dir_pref,
    d.dir_numb,
    hh.hh_numb_full,
    COALESCE(substr2(sces.lpu_code, 0::numeric, (dbms_lob.instr(sces.lpu_code::text, '/'::text) - 1)::numeric), sces.lpu_code::text) AS main_lpu_code,
    sces.smp,
    sces.smp_team,
    sces.time_take,
    sces.time_team,
    sces.time_depart,
    sces.time_arriv,
    sces.time_transf,
    sces.time_delivery,
    sces.time_complit,
    sces.time_return,
    sces.deathdate,
    sces.ishod,
    sces.senior_team,
    sces.senior_team_code,
    sces.send_result,
    sces.senior_emp_code,
    sces.smp_id,
    sces.adis_id,
    sces.amb_id,
    sces.adis_reason,
    sces.adis_result,
    sces.adis_desc_result,
    sces.senior_disp_code,
    sces.division_code,
    sces.bed_profile,
    sces.ex_system AS ex_system_id,
    es.s_code AS ex_system,
    es.stopped AS ex_system_stopped,
    sces.territorial_smp,
    sces.reception_smp
   FROM d_smp_call_ex_system sces
     JOIN d_agents a ON a.id = sces.agent
     LEFT JOIN d_hpk_plan_journals hpj ON hpj.id = sces.hpk_id
     LEFT JOIN d_directions d ON d.id = hpj.direction
     LEFT JOIN d_ex_systems es ON es.id = sces.ex_system
     LEFT JOIN d_hosp_histories hh ON hh.id = sces.hh_id
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.version = sces.version AND ur.unitcode::text = 'SMP_CALL_EX_SYSTEM'::text
         LIMIT 1));
```

---

### Вьюха №24: D_V_LPU_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_LPU_BASE
 SELECT id,
    fullname,
    headdoctor_fullname AS headdoctor_fullname_old,
    d_pkg_headdoctor.get_actual_on_date(id::numeric, sysdate()) AS headdoctor_id,
    COALESCE(d_pkg_headdoctor.get_actual_on_date(id::numeric, sysdate(), 'SURNAME FIRSTNAME LASTNAME'::character varying), headdoctor_fullname) AS headdoctor_fullname,
    d_pkg_headdoctor.get_actual_on_date(id::numeric, sysdate(), 'FIO'::character varying) AS headdoctor_fio,
    fulladdress,
    phones,
    rec_ser_priv,
    rec_ser,
    code_lpu,
    code_ogrn,
    code_okpo,
    code_okdp,
    code_okonh,
    code_okato,
    code_okogu,
    code_ocopph,
    code_okfs,
    bookkeeper_fullname,
    headeconomist_fullname,
    userforms,
    gennumb_group,
    exec_authority,
    rec_ser_priv_88,
    lpudict,
    ip_addr,
    by_es_only,
    geografy,
    website,
    address
   FROM d_lpu t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE COALESCE(ur.lpu, - 1::bigint) = (- 1::bigint) AND COALESCE(ur.version, - 1::bigint) = (- 1::bigint) AND ur.unitcode::text = 'LPU'::text
         LIMIT 1));
```

---

### Вьюха №25: D_V_SMP_CALL_EX_SYSTEM_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SMP_CALL_EX_SYSTEM_BASE
 SELECT id,
    version,
    lpu_code,
    agent AS agent_id,
    lpu_code_smp,
    call_id,
    call_numb,
    call_date,
    call_place,
    call_reason,
    call_structure,
    main_mkb,
    add_mkb,
    complications,
    delivery_type,
    help_on_place,
    help_in_car,
    hosp_status,
        CASE hosp_status
            WHEN 0 THEN 'начало доставки'::text
            WHEN 1 THEN 'отмена доставки'::text
            WHEN 2 THEN 'факт доставки'::text
            WHEN 3 THEN 'принят в приемном покое'::text
            WHEN 4 THEN 'госпитализирован'::text
            WHEN 5 THEN 'отказано в госпитализации'::text
            WHEN 6 THEN 'выписан из стационара'::text
            ELSE NULL::text
        END AS hosp_status_name,
    refuse_reason,
    call_address,
    phone,
    call_type,
    call_status,
        CASE call_status
            WHEN 1 THEN 'новый'::text
            WHEN 2 THEN 'отмена'::text
            WHEN 3 THEN 'отказ'::text
            WHEN 4 THEN 'подтверждение'::text
            WHEN 5 THEN 'факт оказания'::text
            ELSE NULL::text
        END AS call_status_name,
    send_time,
    employer,
    refuse_reason_pol,
    who_call,
    call,
    accident_cause,
    profile_teams,
    intoxication,
    complaints,
    anamnesis,
    objectiv_data,
    result,
    sender_person,
    hj_id,
    hpk_id,
    hh_id,
    COALESCE(substr2(lpu_code, 0::numeric, (dbms_lob.instr(lpu_code::text, '/'::text) - 1)::numeric), lpu_code::text) AS main_lpu_code,
    smp,
    smp_team,
    time_take,
    time_team,
    time_depart,
    time_arriv,
    time_transf,
    time_delivery,
    time_complit,
    time_return,
    deathdate,
    ishod,
    senior_team,
    senior_team_code,
    send_result,
    senior_emp_code,
    senior_disp_code,
    adis_reason,
    adis_result,
    adis_desc_result,
    amb_id,
    territorial_smp,
    reception_smp,
    bed_profile
   FROM d_smp_call_ex_system sces
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.version = sces.version AND ur.unitcode::text = 'SMP_CALL_EX_SYSTEM'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 25

---

### Вьюха №1: D_V_HPK_PLAN_JOURNALS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_PLAN_JOURNALS_BASE
select t.ID,
       t.LPU,
       t.HPK_PLAN,
       t.HPK,
       t.PATIENT,
       t.DIRECTED_BY,
       t.DIRECTED_TO,
       t.REGISTERED_BY,
       t.REGISTER_DATE,
       t.HAS_PRIVILEGES,
       t.OPERATION OPERATION_ID,
       t.CID,
       t.DIRECTION,
       t.PAYMENT_KIND              PAYMENT_KIND_ID,
       t.IS_READY,
       t.HH_DIRECTION_DATE,
       t.IS_OPER,
       t.DISEASECASE,
       decode(t.IS_OPER,0,'Консервативный',1,'Оперативный') IS_OPER_MNEMO,
       decode(t.IS_READY,0,'Не готов',1,'Готов')            IS_READY_MNEMO,
       t.COMMENTS,
       t.QUOTA_Q,
       t.RECORD_STATUS,
       case when t.RECORD_STATUS = 0 then 'Не отработана' else 'Отработана' end
                         RECORD_STATUS_MNEMO,
       t.RECORD_NUMB,
       t.RECORD_PREF,
       t.RECORD_PREF||'-'||t.RECORD_NUMB   RECORD_PREF_NUMB,
       t.RL_RECORD,
       t.SCH_RESOURCE,
       t.EMPLOYMENT_STATUS,
       t.DATE_ACTUAL,
       t.DATE_END_SERV,
       t.CONTRACT,
       t.ALCOHOL_DATE,
       t.ALCOHOL_RES,
       t.DRUG_DATE,
       t.DRUG_RES,
       t.DIRECTION_STATUS
  from D_HPK_PLAN_JOURNALS t  --Журнал госпитализации
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'HPK_PLAN_JOURNALS')
```

---

### Вьюха №2: D_V_HOSP_HISTORIES_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HOSP_HISTORIES_BASE
select -- Представление для раздела : Истории болезни (базовое)
       t.ID,
       t.LPU,
       t.HPK_PLAN_JOURNAL,
       t.PATIENT,
       t.HH_PREF,
       t.HH_NUMB,
       t.HH_NUMB_TYPE,
       t.HH_NUMB_ALTERN,
       t.HOSP_REASON,
       t.RECEPTION_EMP,
       t.DATE_IN,
       t.PLAN_DATE_OUT,
       t.DATE_OUT,
       t.HOSPITALIZATION_TYPE,
       t.TRANSPORTATION_KIND,
       t.LPU_FROM,
       t.MKB_SEND,
       t.MKB_SEND_EXACT,
       t.MKB_CLINIC,
       t.MKB_CLINIC_EXACT,
       t.MKB_CLINIC_DATE,
       t.MKB_FINAL,
       t.MKB_FINAL_EXACT,
       t.MKB_FIN_COMP,
       t.MKB_FIN_COMP_EXACT,
       t.MKB_FIN_ADD,
       t.MKB_FIN_ADD_EXACT,
       t.HOSP_TIMES,
       t.HOSP_RESULT,
       t.MKB_RECEIVE,
       t.MKB_RECEIVE_EXACT,
       t.RELATIVE,
       t.DISEASECASE,
       t.DISCARD_STATUS,
       t.IS_WELL_TIMED_HOSP,
       t.IS_ENOUGH_VOLUME,
       t.IS_CORRECT_HEALING,
       t.IS_SAME_DIAGN,
       t.ARCH_DATE,
       t.ARCH_DEP,
       t.ARCH_COMMENT,
       t.ARCH_NUMB,
       t.HH_TYPE,
       t.HOSP_HOUR,
       t.HOSP_OUTCOME,
       t.TRANSFER_LPU,
       t.TRANSFER_REASON,
       t.HH_NUMB_FULL,
       t.HH_NUMB_MASK,
       t.OTHER_THERAPY,
       t.ABILITY_STATUS,
       t.FEATURES,
       t.ABANDONMENT,
       t.ARRIVE_ORDER,
       t.DEATH_CAME,
       t.HOSP_INCOME,
       t.HOSP_IS_FIRST,
       t.DIRECTION_HOSP,
       t.DATE_DEPARTURE,
       t.RELATIVE_HH,
       t.JUDGE_DECISION,
       t.SEIZED_ITEMS,
       t.NOVOR_NUM,
       t.ATTENDING_EMPL_ID,
       t.DEPARTMENT_ID
  from D_HOSP_HISTORIES        t
 where t.DISCARD_STATUS = 0
   and t.RELATIVE_HH is null
   and exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'HOSP_HISTORIES' and rownum = 1 )
```

---

### Вьюха №3: D_V_HOSP_PLAN_KINDS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HOSP_PLAN_KINDS
select --Представление для раздела : Виды планов госпитализации
       t.ID,
       t.HP_CODE,
       t.HP_NAME,
       t.MAX_PRIOR,
       t.LPU,
       t.CID,
       D_PKG_HOSP_PLAN_DEPS.GET_DEPS(t.ID) DEPS,
       t.MIN_AGE,
       t.MAX_AGE,
       t.HAS_MKB_CONSTRAINTS,
       decode(t.HAS_MKB_CONSTRAINTS,0,'Нет',1,'Да','') sHAS_MKB_CONSTRAINTS,
       t.HAS_LIMITS,
       decode(t.HAS_LIMITS,0,'Нет',1,'Да','') sHAS_LIMITS,
       t.HAS_PAYMENT_CONSTRAINTS,
       decode(t.HAS_PAYMENT_CONSTRAINTS,0,'Нет',1,'Да','') sHAS_PAYMENT_CONSTRAINTS,
       t.NUMB_GROUP,
       t.JOURNAL_TYPE,
       t1.JT_NAME      JOURNAL_TYPE_MNEMO,
       t.IS_OPER,
       case when t.IS_OPER = 0 then 'Консервативный'
            when t.IS_OPER = 1 then 'Оперативный'
        end            OPER_MNEMO,
        t.HP_BLOCK,
        decode(t.HP_BLOCK,1,'Журнал заблокирован',null) HP_BLOCK_MNEMO,
        t.OPEN_DATE,
        t.CLOSE_DATE
  from D_HOSP_PLAN_KINDS   t,     --Виды планов госпитализации
       D_HPK_JOURNAL_TYPES t1     --Типы журналов записей пациентов
 where t1.JT_CODE = t.JOURNAL_TYPE
   and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'HOSP_PLAN_KINDS')
```

---

### Вьюха №4: D_V_AGENT_FLU_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_AGENT_FLU_BASE
select -- Представление для раздела : Контрагенты : Флюорография
       t.ID,
       t.PID,
       t.CID,
       t.VERSION,
       t.FLU_LPU             FLU_LPU_ID,
       t.FLU_LPU_HANDLE,
       t.FLU_DIAGNOSIS,
       t.FLU_DATE,
       t.FLU_CONCLUSION,
       t.FLU_REJECTION       FLU_REJECTION_ID,
       t.IS_LAST,
       t.RAD_DOSE,
       t.RAD_MEASURE
  from D_AGENT_FLU                     t  -- Контрагенты : Флюорография
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = t.CID
                  and ur.UNITCODE = 'AGENT_FLU'
                  and rownum = 1)
```

---

### Вьюха №5: D_V_AGENT_FLU_PMC_LAST

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_AGENT_FLU_PMC_LAST
select -- Представление для раздела : Контрагенты : Флюорография для окна "пациенты в стационаре"
       t.ID,
       t.PID,
       t.CID,
       t.VERSION,
       t.FLU_LPU             FLU_LPU_ID,
       t.FLU_LPU_HANDLE,
       t.FLU_DIAGNOSIS,
       t.FLU_DATE,
       t.FLU_CONCLUSION,
       t1.FLU_PURPOSE,
       t1.NEXT_DATE,
       t1.FLU_METHOD
  from D_AGENT_FLU                     t  -- Контрагенты : Флюорография
       left join D_PMC_FLU             t1   -- Карта пациента : флюорографии
              on t1.AGENT_FLU = t.ID
 where /*t.FLU_DATE   = (select max(a1.FLU_DATE)
                               from D_AGENT_FLU a1
                              where a1.PID = t.PID)*/
       t.IS_LAST = 1
   and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'AGENT_FLU')
```

---

### Вьюха №6: D_V_HPK_PLAN_JOURNALS_GRID

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_PLAN_JOURNALS_GRID
select -- Представление для окна : Журнал Госпитализации
       j.ID,
       j.ID || hh.ID                   UNIQ_N,
       j.HPK_PLAN,
       j.LPU,
       j.HPK                           HOSP_PLAN_KIND,
       hpk.HP_NAME                     HOSP_PLAN_KIND_NAME,
       agn.ID                          PATIENT_AGENT,
       D_PKG_STR_TOOLS.FIO(fsSURNAME     => agn.SURNAME,
                           fsNAME        => agn.FIRSTNAME,
                           fsPATRNAME    => agn.LASTNAME,
                           fsRETURN_FULL => 1)
                                       PATIENT,
       trim(D_PKG_AGENT_NAMES.GET_ACTUAL_ON_DATE(fnAGENT => agn.ID,
                                                 fdDATE  => coalesce(hh.DATE_IN, j.REGISTER_DATE),
                                                 fsFIELD => 'SURNAME FIRSTNAME LASTNAME'))
                                       PATIENT_ACTUAL,
       j.PATIENT                       PATIENT_ID,
       pmc.CARD_NUMB                   PATIENT_CARD_NUMB,
       j.DIRECTED_BY                   DIRECTED_BY_ID,
       (select D_PKG_STR_TOOLS.FIO(dir_by_agn.SURNAME, dir_by_agn.FIRSTNAME, dir_by_agn.LASTNAME)
          from D_EMPLOYERS dir_by_emp
               join D_AGENTS dir_by_agn on dir_by_agn.ID = dir_by_emp.AGENT
         where dir_by_emp.ID = j.DIRECTED_BY)
                                       DIRECTED_BY,
       j.DIRECTED_TO                   DIRECTED_TO_ID,
       (select D_PKG_STR_TOOLS.FIO(dir_to_agn.SURNAME, dir_to_agn.FIRSTNAME, dir_to_agn.LASTNAME)
          from D_EMPLOYERS dir_to_emp
               join D_AGENTS dir_to_agn on dir_to_agn.ID = dir_to_emp.AGENT
         where dir_to_emp.ID = j.DIRECTED_TO)
                                       DIRECTED_TO,
       j.REGISTERED_BY                 REGISTERED_BY_ID,
       (select D_PKG_STR_TOOLS.FIO(reg_by_agn.SURNAME, reg_by_agn.FIRSTNAME, reg_by_agn.LASTNAME)
          from D_EMPLOYERS reg_by_emp
               join D_AGENTS reg_by_agn on reg_by_agn.ID = reg_by_emp.AGENT
         where reg_by_emp.ID = j.REGISTERED_BY)
                                       REGISTERED_BY,
       j.REGISTER_DATE,
       j.HAS_PRIVILEGES,
       vs.VS_NAME                      sHAS_PRIVILEGES,
       j.OPERATION                     OPERATION_ID,
       (select D_TP_SS(srv.SE_CODE, srv.SE_NAME)
          from D_SERVICES srv
         where srv.ID = j.OPERATION)   OPERATION,
       j.CID,
       j.DIRECTION,
       j.PAYMENT_KIND                  PAYMENT_KIND_ID,
       (select pay_kind.PK_NAME
          from D_PAYMENT_KIND pay_kind
         where pay_kind.ID = j.PAYMENT_KIND)
                                       PAYMENT_KIND,
       (select sch_r.DATE_REC
          from D_HPK_SCHEDULE_REG sch_r
         where sch_r.DIRECTION = j.DIRECTION)
                                       DATE_REC,

       j.SCH_RESOURCE,
       (select c.CL_NAME
          from D_SCH_RESOURCES sr
               join D_CABLAB c on c.ID = sr.CABLAB
         where sr.ID = j.SCH_RESOURCE) CABLAB_NAME,
       case when hh.ID is not null
              then (select max(pk.PK_NAME) keep (dense_rank first order by hhd1.DATE_IN asc)
                      from D_HOSP_HISTORY_DEPS hhd1
                           join D_PAYMENT_KIND pk on pk.ID = hhd1.PAYMENT_KIND
                     where hhd1.PID = hh.ID)
       end                             HOSP_PAYMENT_KIND,
       j.IS_READY,
       j.HH_DIRECTION_DATE,
       j.IS_OPER,
       case j.IS_OPER
            when 0 then 'Консервативный'
            when 1 then 'Оперативный'
       end                             IS_OPER_MNEMO,
       case j.IS_READY
            when 0 then 'Не готов'
            when 1 then 'Готов'
       end                             IS_READY_MNEMO,
       hpk.HP_NAME,
       j.COMMENTS                      COMMS,
       j.DISEASECASE,
       j.CONTRACT,
       hh.HH_TYPE,
       hh.DISCARD_STATUS  HOSP_HISTORY_DS,
       hh.ID              HOSP_HISTORY,
       hh.RELATIVE        HH_AGENT_RELATIVE,
       case when hh.ID is not null
              then hh.HH_NUMB_FULL ||
                   (select case when db1.PID is not null
                                  then ', Койка: ' || db1.DB_CODE
                           end || ', Палата: ' ||
                           case when db1.PID is null
                                  then db1.DB_CODE
                                else (select db2.DB_CODE
                                        from D_DEP_BEDS db2
                                       where db2.ID = db1.PID)
                           end
                      from D_HOSP_HISTORY_DEPS q
                           join D_HH_DEP_BEDS d
                             on d.PID = q.ID
                           join D_DEP_BEDS db1 on db1.ID = d.DEP_BED
                    where q.PID = hh.ID
                      and q.DATE_OUT is null
                      and d.DATE_OUT is null)
       end                             DEPBED,

       case when hh.ID is not null
              then (select dep.DP_NAME
                      from D_HOSP_HISTORY_DEPS hhd1
                           join D_DEPS dep on dep.ID = hhd1.DEP
                     where hhd1.PID = hh.ID
                       and hhd1.PRVSID is null)
       end                             DEP,
       case when hh.ID is not null
              then (select hhd1.DEP
                      from D_HOSP_HISTORY_DEPS hhd1
                     where hhd1.PID = hh.ID
                       and hhd1.PRVSID is null)
       else null end                   DEP_ID,
       case when hh.HOSP_RESULT is null or exists (select null
                                                     from D_HOSP_RESULTS hr
                                                    where hr.ID = hh.HOSP_RESULT
                                                      and hr.R_CODE != '6')
              then (select dep.DP_NAME
                      from D_HOSP_HISTORY_DEPS hhd
                           join D_DEPS dep on dep.ID = hhd.DEP
                     where hhd.PID = hh.ID
                       and hhd.PRVSID is null)
       end                             HOSP_IN_DEP,
       (select D_STRAGG(m.M_NAME || '/' || sc.GRAD_FROM || '/' || sc.GRAD_TO)
          from D_PMC_MARKERS pm
               join D_MARKERS m on m.ID = pm.MARKER
               join D_SCHEDULE_COLORS sc on sc.ID = m.COLOR
         where pm.PID = j.PATIENT
           and sysdate between pm.BEGIN_DATE and coalesce(pm.END_DATE, to_date('31.12.2999', D_PKG_STD.FRM_D))
           and (select D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => j.LPU,
                                                      psUNITCODE => 'MARKERS',
                                                      pnUNIT_ID  => pm.MARKER,
                                                      psRIGHT    => 1, -- Запрет просмотра маркера
                                                      pnCABLAB   => sys_context('MED', 'CABLAB'))
                  from dual) != 1
       )                               MARKER,
       (select 'Тел.: ' || D_STRAGG_EX(D_TP_STRAGG_REC(t.CONTACT || (case when t.NOTE is not null then ' ' || t.NOTE end), ';' || chr(10), 'ASC', null, null))
          from D_AGENT_CONTACTS t
               join D_CONTACT_TYPES ct
                 on ct.ID = t.CONTACT_TYPE
         where t.PID = agn.ID
           and ct.CT_CODE in ('1', '2'))
                                       COMMENTS,
       (select aa.RAION
          from (select a.PID,
                       a.RAION,
                       a.BEGIN_DATE,
                       a.END_DATE,
                       row_number() over (partition by a.PID order by a.BEGIN_DATE desc) RN
                  from D_AGENT_ADDRS a
                 where a.IS_REG = 1) aa
         where aa.PID = agn.ID
           and aa.BEGIN_DATE <= coalesce(hp.PLAN_DATE, sysdate)
           and (aa.END_DATE >= coalesce(hp.PLAN_DATE, sysdate) or aa.END_DATE is null)
           and aa.RN = 1)
                                       RAION_ID,
       hp.PLAN_DATE,
       hosp_type.HK_NAME               HOSP_TYPE,
       hosp_type.HK_CODE               HK_CODE,
       ld.LPU_FULLNAME                 LPU_FROM,
       case when coalesce(length(mkb.MKB_CODE), 0) + coalesce(length(mkb.MKB_NAME), 0) + coalesce(length(hh.MKB_RECEIVE_EXACT), 0) <= 3998
              then mkb.MKB_CODE || ' ' || mkb.MKB_NAME || ' ' || hh.MKB_RECEIVE_EXACT
            else mkb.MKB_CODE || ' ' || mkb.MKB_NAME || ' ' || substr(hh.MKB_RECEIVE_EXACT, 1, 3995 - coalesce(length(mkb.MKB_CODE), 0) - coalesce(length(mkb.MKB_NAME), 0)) || '...'
       end                             DIAGNOSIS_FROM,
       agn.BIRTHDATE                   PATIENT_BIRTHDATE,
       hh.DATE_IN,
       hh.DATE_OUT,
       (select s.SOC_CODE
          from D_AGENT_SOCIAL_STATES pss
               join D_SOCIALSTATES s on s.ID = pss.SOCIAL_STATE
         where pss.PID = pmc.AGENT
           and pss.BEGIN_DATE <= sysdate
           and (pss.END_DATE >= trunc(sysdate) or pss.END_DATE is null))
                                       PATIENT_SOCIAL_STATE,
       (select case when pwp.WORK_PLACE is null
                      then pwp.WORK_PLACE_HAND
                    else (select ag.AGN_NAME
                            from D_AGENTS ag
                           where ag.ID = pwp.WORK_PLACE)
               end
          from D_AGENT_WORK_PLACES pwp
         where pwp.PID = pmc.AGENT
           and pwp.IS_MAIN  = 1
           and pwp.BEGIN_DATE <= sysdate
           and (pwp.END_DATE >= trunc(sysdate) or pwp.END_DATE is null))
                                       PATIENT_WORK_PLACE,
       (select ct.CAT_CODE
          from D_AGENT_CATEGORIES pct
               join D_CATEGORIES ct on ct.ID = pct.CATEGORY
         where pct.PID = pmc.AGENT
           and pct.DATE_B <= sysdate
           and (pct.DATE_E >= trunc(sysdate) or pct.DATE_E is null)
           and rownum = 1)
                                       PATIENT_CATEGORIES,
       D_PKG_AGENT_POLIS.GET_ACTUAL_ON_DATE(fnPATIENT => agn.ID,
                                            fdDATE    => sysdate,
                                            fnTYPE    => 0,
                                            fsFIELD   => 'P_SER P_NUM') PATIENT_POLIS,
       (select apd.PD_SER || ' ' || apd.PD_NUMB
          from D_AGENT_PERSDOCS apd
         where apd.PID = agn.ID
           and apd.IS_MAIN = 1
           and apd.PERIOD_BEGIN <= sysdate
           and (apd.PERIOD_END >= trunc(sysdate) or apd.PERIOD_END is null))
                                       PATIENT_PERSDOC,
       (select D_PKG_AGENT_ADDRS.GET_SHORT_ADDRESS_BY_ID(aa1.ID)
          from (select a.ID,
                       a.PID,
                       a.BEGIN_DATE,
                       a.END_DATE,
                       row_number() over (partition by a.PID order by a.BEGIN_DATE desc) RN
                  from D_AGENT_ADDRS a
                 where a.IS_REG = 1) aa1
         where aa1.PID  = agn.ID
           and aa1.BEGIN_DATE <= sysdate
           and (aa1.END_DATE >= trunc(sysdate) or aa1.END_DATE is null)
           and aa1.RN = 1)             PATIENT_ADDRESS,
       dir.DIR_PREF,
       dir.DIR_NUMB,
       dir_kind.ID                     DIRECTION_KIND_ID,
       dir_kind.DK_CODE                DIRECTION_KIND,
       dir_kind.DK_NAME                DIRECTION_KIND_NAME,
       dir_kind.SHORT_NAME             DIRECTION_KIND_SHORT_NAME,
       j.RECORD_STATUS,
       case when j.RECORD_STATUS = 0
              then 'Не отработана'
            else 'Отработана'
       end                             RECORD_STATUS_MNEMO,
       D_PKG_DOC_TOOLS.SNILS_FROM_STR(agn.SNILS) PATIENT_SNILS,
       dir.IS_CANCELED,
       dir.CANC_REASON                 CANC_REASON_ID,
       dcr.DCR_CODE                    CANC_REASON,
       dcr.DCR_NAME                    CANC_REASON_NAME,
       hpk.JOURNAL_TYPE                HPK_JOURNAL_TYPE,
       j.RECORD_PREF                   RECORD_PREF,
       j.RECORD_NUMB                   RECORD_NUMB,
       dir.OUTER_DIRECTION             OUTER_DIRECTION_ID,
       (select od.D_NUMB
          from D_OUTER_DIRECTIONS od
         where od.ID = dir.OUTER_DIRECTION)
                                       OUTER_DIRECTION_NUMB,
       dir.DIR_COMMENT,
       coalesce((select listagg(ts.CODE, ';') within group (order by dts.ID)
                   from D_DIR_THERAPY_SCHEMES dts
                        join D_MED_THERAPY_SCHEMES_DESC tsd on tsd.ID = dts.MED_THERAPY_SCHEMES
                        join D_MED_THERAPY_SCHEMES ts on ts.ID = tsd.PID
                  where dts.PID = j.DIRECTION),
                (select mts.CODE
                   from D_DIRECTIONS d
                        join D_MED_THERAPY_SCHEMES mts on d.THERAPY_SCHEMES = mts.ID
                  where d.ID = j.DIRECTION))
                                       MED_THERAPY_SCHEME_CODE,


       dir.HOSP_MKB                    HOSP_MKB_ID,
       (select D_TP_SS(hosp_mkb.MKB_CODE, hosp_mkb.MKB_NAME)
          from D_MKB10 hosp_mkb
         where hosp_mkb.ID = dir.HOSP_MKB)
                                       HOSP_MKB,
       dir.REG_VISIT,
       dir.HOSP_DEP                    DIR_HOSP_DEP_ID,
       (select dhd.DP_NAME
          from D_DEPS dhd
         where dhd.ID = dir.HOSP_DEP)  DIR_HOSP_DEP_NAME,
       j.ALCOHOL_DATE,
       j.ALCOHOL_RES,
       j.DRUG_DATE,
       j.DRUG_RES,
       hpk.CLOSE_DATE                  HOSP_PLAN_KIND_CLOSE_DATE,
       hosp_rel.PATIENT                RELATIVE_PATIENT_ID,
       (select trim(rel_ag.SURNAME || ' ' || rel_ag.FIRSTNAME || ' ' || rel_ag.LASTNAME)
          from D_PERSMEDCARD rel_pat
               join D_AGENTS rel_ag on rel_ag.ID = rel_pat.AGENT
         where rel_pat.ID = hosp_rel.PATIENT)
                                       RELATIVE_PATIENT,
       hosp_rel.ID                     RELATIVE_HOSP_HISTORY,
       hosp_rel.DISEASECASE            RELATIVE_DISEASECASE,
       (select hpk_rel.PAYMENT_KIND
          from D_HPK_PLAN_JOURNALS hpk_rel
         where hpk_rel.ID = hosp_rel.HPK_PLAN_JOURNAL)
                                       RELATIVE_PAYMENT_KIND_ID,
       case when (cr.NAME is not null and hcr.COMM is not null) then cr.NAME || ';' || hcr.COMM
            else coalesce(cr.NAME, hcr.COMM)
       end HH_CANC_REASON,
       hcr.ID HH_CANC_REASON_ID
  from D_HPK_PLAN_JOURNALS j
       left join D_HPK_PLANS hp on hp.ID = j.HPK_PLAN and hp.LPU = j.LPU
       join D_HOSP_PLAN_KINDS hpk on hpk.ID = j.HPK and hpk.LPU = j.LPU
       join D_PERSMEDCARD pmc on pmc.ID = j.PATIENT and pmc.LPU = j.LPU
       join D_AGENTS agn on agn.ID = pmc.AGENT
       left join D_HOSP_HISTORIES hh
                 join D_HOSPITALIZATION_TYPES hosp_type on hosp_type.ID = hh.HOSPITALIZATION_TYPE
                 left join D_LPUDICT ld on ld.ID = hh.LPU_FROM
                 left join D_MKB10 mkb on mkb.ID = hh.MKB_RECEIVE
                 left join D_HOSP_HISTORIES hosp_rel
                        on hosp_rel.RELATIVE_HH = hh.ID
                       and (hosp_rel.DATE_OUT is null or hosp_rel.DATE_OUT = hh.DATE_OUT)
                 left join D_HH_CANC_REASON hcr on hcr.PID = hh.ID
                 left join D_CANC_REASON cr on cr.ID = hcr.CANC_REASON
              on hh.HPK_PLAN_JOURNAL = j.ID and hh.RELATIVE_HH is null
       join D_HPKPJ_VMP_STATES vs on vs.VS_CODE  = j.HAS_PRIVILEGES
       left join D_DIRECTIONS dir
                 left join D_DIRECTION_KINDS dir_kind on dir_kind.ID = dir.DIRECTION_KIND
                 left join D_DIR_CANC_REASONS dcr on dcr.ID = dir.CANC_REASON
              on dir.ID = j.DIRECTION
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = j.CID
                  and ur.UNITCODE = 'HPK_PLAN_JOURNALS')
```

---

### Вьюха №7: D_V_WL_RECORDS74

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_WL_RECORDS74
select -- Представление для раздела : Журналы очередей : Талоны электронной очереди (ЭО)
       t.ID,
       t.LPU,
       t.PID,
       t.HID,
       t.AGENT               AGENT_ID,
       D_PKG_STR_TOOLS.FIO(t1.SURNAME,t1.FIRSTNAME,t1.LASTNAME)
                             AGENT,
       t.PREF,
       t.NUMB,
       t.PREF||t.NUMB        PREF_NUMB,
       t.TICKET_TYPE,
       t.IS_ILL,
       (case when t.IS_ILL = 0
             then 'Здоровый'
             else 'Больной'
             end)            IS_ILL_NAME,
       t.SERVICE             SERVICE_ID,
       t2.SE_CODE            SERVICE,
       t2.SE_NAME            SERVICE_NAME,
       t.DIR_SERV            DIR_SERV_ID,
       t3.UK_HASH            DIR_SERV,
       t.PAYMENT_KIND        PAYMENT_KIND_ID,
       t4.PK_CODE            PAYMENT_KIND,
       t4.PK_NAME            PAYMENT_KIND_NAME,
       t.REG_DATE,
       t.CALL_DATE,
       t.STATUS,
       (case t.STATUS
             when 0 then 'Находится в очереди'
             when 1 then 'Вызван из очереди'
             when 2 then 'Неявка'
             when 3 then 'Услуга оказана'
             end)            STATUS_NAME,
       t.CALL_NUMB,
       t.CHECKUP_DATE,
       t.VISIT_DATE,
       t.EMPLOYER            EMPLOYER_ID,
       D_PKG_STR_TOOLS.FIO(t6.SURNAME,t6.FIRSTNAME,t6.LASTNAME)
                             EMPLOYER,
       t7.ID                 PERSMEDCARD,
       t.CABLAB,
       t.PROFCARD
  from D_WL_RECORDS74                            t  -- Журналы очередей : Талоны электронной очереди (ЭО)
       left join D_AGENTS                        t1 -- Контрагенты
              on t1.ID = t.AGENT
       join D_SERVICES                           t2 -- Услуги
         on t2.ID = t.SERVICE
       left join D_DIRECTION_SERVICES            t3 -- Направления : услуги
              on t3.ID = t.DIR_SERV
       left join D_PAYMENT_KIND                  t4 -- Виды оплаты
              on t4.ID = t.PAYMENT_KIND
       left join D_EMPLOYERS                     t5 -- Сотрудники
              on t5.ID = t.EMPLOYER
                 left join D_AGENTS              t6 -- Сотрудники контрагенты
                        on t6.ID = t5.AGENT
       left join D_PERSMEDCARD                   t7 -- Медицинские карты
              on t7.AGENT = t.AGENT
             and t7.LPU   = t.LPU
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'WL_RECORDS74' and rownum = 1)
```

---

### Вьюха №8: D_V_VMP_LINKS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_VMP_LINKS
select -- Представление для раздела : соответствие заявки ВМП - талона ВМП - записи в журнале госпитализации
       t.ID,
       t.VERSION,
       t.VMP_APPL,
       t.VMP_TALON,
       t.DIRECTION
  from D_VMP_LINKS                               t	-- соответствие заявки ВМП - талона ВМП - записи в журнале госпитализации
 where exists (select null from D_V_URPRIVS ur where ur.VERSION = t.VERSION and ur.UNITCODE = 'VMP_LINKS' and rownum = 1)
```

---

### Вьюха №9: D_V_HPK_PLAN_JOURNALS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_PLAN_JOURNALS
select t.ID,
       t.LPU,
       t.HPK_PLAN,
       t.HPK               HOSP_PLAN_KIND,
       t2.HP_NAME          HOSP_PLAN_KIND_NAME,
       t.PATIENT           PATIENT_ID,
       t3.AGENT            PATIENT_AGENT,
       a3.SURNAME||' '||a3.FIRSTNAME||' '||a3.LASTNAME          PATIENT,
       a3.SEX,
       a3.BIRTHDATE        PATIENT_BIRTHDATE,
       t.DIRECTED_BY                                            DIRECTED_BY_ID,
       D_PKG_STR_TOOLS.FIO(a5.SURNAME,a5.FIRSTNAME,a5.LASTNAME) DIRECTED_BY,
       t.DIRECTED_TO                                            DIRECTED_TO_ID,
       D_PKG_STR_TOOLS.FIO(a6.SURNAME,a6.FIRSTNAME,a6.LASTNAME) DIRECTED_TO,
       t.REGISTERED_BY                                          REGISTERED_BY_ID,
       D_PKG_STR_TOOLS.FIO(a7.SURNAME,a7.FIRSTNAME,a7.LASTNAME) REGISTERED_BY,
       t.REGISTER_DATE,
       t.HAS_PRIVILEGES,
       t10.VS_NAME sHAS_PRIVILEGES,
       t.OPERATION OPERATION_ID,
       t4.SE_NAME  OPERATION,
       t.CID,
       t.DIRECTION,
       t.PAYMENT_KIND              PAYMENT_KIND_ID,
       t8.PK_CODE||' '||t8.PK_NAME PAYMENT_KIND,
       t8.PK_NAME                  PAYMENT_KIND_NAME,
       t8.PK_CODE                  PAYMENT_KIND_CODE,
       t8.IS_COMMERC PAYMENT_KIND_IS_COMMERC,
       t.IS_READY,
       t.HH_DIRECTION_DATE,
       t.IS_OPER,
       t.DISEASECASE,
       decode(t.IS_OPER,0,'Консервативный',1,'Оперативный') IS_OPER_MNEMO,
       decode(t.IS_READY,0,'Не готов',1,'Готов')            IS_READY_MNEMO,
       t2.HP_NAME,
       t.COMMENTS,
       D_PKG_AGENT_ADDRS.GET_REG_RAION(a3.ID,t1.PLAN_DATE,0) RAION_ID,
       D_PKG_AGENT_ADDRS.GET_REG_RAION(a3.ID,t1.PLAN_DATE,2) RAION_NAME,
       t9.ID HOSP_HISTORY,
       t9.DISCARD_STATUS HOSP_HISTORY_DS,
       t1.PLAN_DATE,
       t9.HH_PREF        HOSP_HISTORY_PREF,
       t9.HH_NUMB        HOSP_HISTORY_NUMB,
       t9.HH_NUMB_ALTERN HOSP_HISTORY_NUMB_ALT,
       t9.HH_NUMB_FULL   HOSP_HISTORY_NUMB_FULL,
       t9.MKB_SEND       HOSP_HISTORY_MKB_SEND,
       t9.MKB_SEND_EXACT HOSP_HISTORY_MKB_SEND_EXACT,
       t9.DATE_IN        HOSP_HISTORY_DATE_IN,
       t9.HOSPITALIZATION_TYPE HOSP_HISTORY_HOSP_TYPE,
       t.QUOTA_Q,
       t.HPK,
       t.RECORD_STATUS,
       case when t.RECORD_STATUS = 0 then 'Не отработана' else 'Отработана' end
                         RECORD_STATUS_MNEMO,
       t.RECORD_NUMB,
       t.RECORD_PREF,
       t.RECORD_PREF||'-'||t.RECORD_NUMB   RECORD_PREF_NUMB,
       t.RL_RECORD,
       t.SCH_RESOURCE,
       t2.HP_CODE      HOSP_PLAN_KIND_CODE,
       t11.HOSP_PLAN_DATE,
       t12.ID          DEP_ID,
       t12.DP_CODE     DEP_CODE,
       t12.DP_NAME     DEP,
       t13.ID      EMPLOYMENT_STATUS_ID,
       t13.ES_CODE EMPLOYMENT_STATUS,
       t13.ES_NAME EMPLOYMENT_STATUS_NAME,
       t.DATE_ACTUAL,
       t.DATE_END_SERV,
       t.RC_RECORD,
       t.CONTRACT,
       t.ALCOHOL_DATE,
       t.ALCOHOL_RES,
       t.DRUG_DATE,
       t.DRUG_RES,
       t.DIRECTION_STATUS
  from D_HPK_PLAN_JOURNALS t,  --Журнал госпитализации
       D_HPK_PLANS         t1, --Планы госпитализации
       D_HOSP_PLAN_KINDS   t2, --Виды планов госпитализации
       D_PERSMEDCARD       t3, --Персональные медицинские карты
       D_AGENTS            a3, --Контрагенты
       D_SERVICES          t4, --Услуги
       D_EMPLOYERS         t5, --Персонал
       D_AGENTS            a5, --Контрагенты
       D_EMPLOYERS         t6, --Персонал
       D_AGENTS            a6, --Контрагенты
       D_EMPLOYERS         t7, --Персонал
       D_AGENTS            a7, --Контрагенты
       D_PAYMENT_KIND      t8, --Виды оплаты
       (select hh.ID,
               hh.DISCARD_STATUS,
               hh.HH_PREF,
               hh.HH_NUMB,
               hh.HH_NUMB_ALTERN,
               hh.HPK_PLAN_JOURNAL,
               hh.MKB_SEND,
               hh.MKB_SEND_EXACT,
               hh.HH_NUMB_FULL,
               hh.HOSPITALIZATION_TYPE,
               hh.DATE_IN
          from D_HOSP_HISTORIES hh
         where hh.DISCARD_STATUS = 0
           and hh.RELATIVE_HH is null)
                           t9, --Истории болезни
       D_HPKPJ_VMP_STATES  t10,
       D_DIRECTIONS        t11,
       D_DEPS              t12,
       D_EMPLOYMENT_STATUS t13
 where t1.ID(+)     = t.HPK_PLAN
   and t2.ID        = t.HPK
   and t3.ID        = t.PATIENT
   and a3.ID        = t3.AGENT
   and t4.ID(+)     = t.OPERATION
   and t5.ID(+)     = t.DIRECTED_BY
   and a5.ID(+)     = t5.AGENT
   and t6.ID(+)     = t.DIRECTED_TO
   and a6.ID(+)     = t6.AGENT
   and t7.ID        = t.REGISTERED_BY
   and a7.ID(+)     = t7.AGENT
   and t8.ID(+)     = t.PAYMENT_KIND
   and t.ID         = t9.HPK_PLAN_JOURNAL(+)
   and t10.VS_CODE  = t.HAS_PRIVILEGES
   and t11.ID       = t.DIRECTION
   and t12.ID(+)    = t11.HOSP_DEP
   and t13.ID(+)    = t.EMPLOYMENT_STATUS
   and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'HPK_PLAN_JOURNALS')
```

---

### Вьюха №10: D_V_DIRECTIONS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_DIRECTIONS_BASE
select --Представление для раздела : Направления (базовое)
       t.ID,
       t.LPU,
       t.OUTER_DIRECTION,
       t.LPU_TO,
       t.LPU_TO_HANDLE,
       t.PATIENT,
       t.REG_VISIT,
       t.REG_EMPLOYER,
       t.REG_DATE,
       t.DIR_COMMENT,
       t.REG_TYPE,
       t.DIR_TYPE,
       t.HOSP_MKB,
       t.HOSP_KIND,
       t.DIR_NUMB,
       t.SPECIALITY,
       t.EX_CAUSE_MKB,
       t.INJURE_KIND,
       t.INJURE_TIME,
       t.DIRECTION_KIND,
       t.HOSP_DEP,
       t.DIR_PREF,
       t.HOSP_BED_TYPE,
       t.MES,
       t.REG_HPKPJ,
       t.HOSP_REASON,
       t.IS_CANCELED,
       t.CANC_REASON,
       t.CANC_EMPLOYER,
       t.CANC_EMPLOYER_FIO,
       t.CANC_DATE,
       t.REG_DIR_SERV,
       t.HOSP_PLAN_DATE,
       t.DATE_TR,
       t.HOSP_DEPDICT,
       t.DOC_COMMENT,
       t.HOSP_TYPE,
       t.TALON_VMP_DATE,
       t.HOSP_DIRECT_TYPE,
       t.HOSP_REASON_STREETKIDS,
       t.TRANSPORTATION_KIND,
       t.HOSP_HOUR,
       t.REG_DEP,
       t.HOSP_MKB_EXACT,
       t.TALON_VMP_NUM,
       t.IS_ONKO,
       t.REALISED_DAYS,
       t.DIR_FORM,
       t.THERAPY_SCHEMES,
       t.DIRECTION_FORM,
       t.TYPE_MED_HELP,
       t.DIRECTION_CONDITION,
       t.DIRECTION_REASON,
       t.VMP
  from D_DIRECTIONS           t   --Направления
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU = t.LPU
                  and ur.UNITCODE = 'DIRECTIONS'
                  and rownum = 1)
```

---

### Вьюха №11: D_V_HPK_PLANS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_PLANS
select --Представление для раздела: Планы госпитализации
       t.ID,
       t.LPU,
       t.PID,
       t1.HP_NAME PLAN_KIND,
       t.PLAN_DATE,
       to_char(t.PLAN_DATE,'DAY','NLS_DATE_LANGUAGE=AMERICAN') PLAN_DAY_ENG,
       --case
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'MONDAY' then 'Понедельник'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'TUESDAY' then 'Вторник'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'WEDNESDAY' then 'Среда'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'THURSDAY' then 'Четверг'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'FRIDAY' then 'Пятница'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'SATURDAY' then 'Суббота'
--         when trim(to_char(t.PLAN_DATE,'DAY')) = 'SUNDAY' then 'Воскресение'
--       end
       initcap(to_char(t.PLAN_DATE,'DAY','NLS_DATE_LANGUAGE=RUSSIAN')) PLAN_DAY_RUS,
       t.MALE_COUNT,
       decode(t.MALE_COUNT,null,null,(t.GEN_COUNT - t.MALE_COUNT))     FEMALE_COUNT,
       t.OPER_COUNT,
       decode(t.OPER_COUNT,null,null,(t.GEN_COUNT - t.OPER_COUNT))     CONS_COUNT,
       t.GEN_COUNT,
       decode(t.GEN_COUNT,null,D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,0)||'(неогр.)',D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,0)||'('||t.GEN_COUNT||')') GEN_COUNT_S,
       decode(t.MALE_COUNT,null,D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,1)||'(неогр.)',D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,1)||'('||t.MALE_COUNT||')') MALE_COUNT_S,
       decode(t.MALE_COUNT,null,D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,2)||'(неогр.)',D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,2)||'('||(t.GEN_COUNT - t.MALE_COUNT)||')') FEMALE_COUNT_S,
       decode(t.OPER_COUNT,null,D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,3)||'(неогр.)',D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,3)||'('||t.OPER_COUNT||')') OPER_COUNT_S,
       decode(t.OPER_COUNT,null,D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,4)||'(неогр.)',D_PKG_HPK_PLAN_JOURNALS.GET_INFO(t.LPU,t1.ID,t.PLAN_DATE,1,4)||'('||(t.GEN_COUNT - t.OPER_COUNT)||')') CON_COUNT_S,
       t.CID
  from D_HPK_PLANS       t,  -- Планы госпитализации
       D_HOSP_PLAN_KINDS t1  -- Виды планов госпитализации
 where t1.ID        = t.PID
   and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'HPK_PLANS')

 
```

---

### Вьюха №12: D_V_HPK_PLANS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_PLANS_BASE
select --Представление для раздела: Планы госпитализации
       t.ID,
       t.LPU,
       t.PID,
       t1.HP_NAME PLAN_KIND,
       t.PLAN_DATE,
       to_char(t.PLAN_DATE,'DAY','NLS_DATE_LANGUAGE=AMERICAN') PLAN_DAY_ENG,
       initcap(to_char(t.PLAN_DATE,'DAY','NLS_DATE_LANGUAGE=RUSSIAN')) PLAN_DAY_RUS,
       t.MALE_COUNT,
       decode(t.MALE_COUNT,null,null,(t.GEN_COUNT - t.MALE_COUNT))     FEMALE_COUNT,
       t.OPER_COUNT,
       decode(t.OPER_COUNT,null,null,(t.GEN_COUNT - t.OPER_COUNT))     CONS_COUNT,
       t.GEN_COUNT,
       t.CID
  from D_HPK_PLANS       t  -- Планы госпитализации
  join D_HOSP_PLAN_KINDS t1 on t1.ID = t.PID -- Виды планов госпитализации
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'HPK_PLANS')
```

---

### Вьюха №13: D_V_PERSMEDCARD

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_PERSMEDCARD
select --Представление для раздела : Персональные медицинские карты
       p.ID,
       p.CID,
       p.LPU,
       p.CARD_NUMB,
       p.AGENT,
       a.FIRSTNAME,
       a.SURNAME,
       a.LASTNAME,
       a.BIRTHDATE,
       a.DEATHDATE,
       decode(a.SEX,0,'Женский',1,'Мужской','')                   SEX,
       a.SEX                                                      nSEX,
       a.BIRTHPLACE,
       p.CREATEDATE,
       p.MODDATE,
       p.NOTE,
       r.RH_NAME                                                  RHESUS,
       p.RHESUS                                                   nRHESUS,
       p.BLOODGROUPE                                              BLOODGROUPE_ID,
       b.BG_CODE                                                  BLOODGROUPE,
       p.ECOLOR,
       a.SNILS,
       p.REG_DIVISION                                             REG_DIVISION_ID,
       d.DIV_CODE                                                 REG_DIVISION,
       p.TP_PRINTED,
       case when p.TP_PRINTED = 0 then 'Нет'
            when p.TP_PRINTED = 1 then 'Да'
       end                                                        TP_PRINTED_MNEMO,
       p.PMC_TYPE,
       case when p.PMC_TYPE = 1     then 'аноним'
            when p.PMC_TYPE = 2     then 'новорожденный'
            when p.PMC_TYPE = 3     then 'неизвестный'
            when p.PMC_TYPE is null then 'пациент'
       end                                                        PMC_TYPE_NAME,
       a.ENP,
       p.OUTNUMB,
       p.IA_PRINTED,
       case when p.IA_PRINTED = 0 then 'Нет'
            else 'Да'
       end                                                        IA_PRINTED_MNEMO,
       p.SMS_AGREE,
       case when p.SMS_AGREE = 0 then 'Нет'
            else 'Да'
       end                                                        SMS_AGREE_MNEMO,
       D_PKG_STR_TOOLS.FIO(ae.SURNAME, ae.FIRSTNAME, ae.LASTNAME) EMP_FIO,
       p.EMP_ID,
       null MARKER_ID, --Не используется
       null MARKER_CODE,--Не используется
       null MARKER_NAME,--Не используется
       null MARKER_DESCRIPTION,--Не используется
       p.EMAIL_AGREE,
       p.PHENOTYPE,
       a.EDUCATION                                                EDUCATION_ID,
       c.CL_NAME,
       a.ACCORDING_RELATIVES,
       a.NATION                                                   AGENT_NATION
  from D_PERSMEDCARD           p                           --Персональные медицинские карты
       join D_AGENTS           a  on a.ID = p.AGENT        --Контрагенты
       left join D_BLOODGROUPE b  on b.ID = p.BLOODGROUPE  --Справочник : Группы крови
       left join D_DIVISIONS   d  on d.ID = p.REG_DIVISION --Подразделения ЛПУ
       left join D_RHESUS      r  on r.RH_CODE = p.RHESUS  --Резус-фактор
       left join D_EMPLOYERS   e  on e.ID = p.EMP_ID       --Персонал
       left join D_AGENTS      ae on ae.ID = e.AGENT       --Контрагенты
       left join D_CABLAB      c  on c.ID = p.CABLAB       --Кабинет
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = p.CID
                  and ur.UNITCODE = 'PERSMEDCARD'
                  and rownum = 1)
```

---

### Вьюха №14: D_V_LPU

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_LPU
select --Представление для раздела: Основная таблица ЛПУ
       l.ID,
       l.FULLNAME,
       l.HEADDOCTOR_FULLNAME                                   HEADDOCTOR_FULLNAME_OLD,
       D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(l.ID,sysdate)       HEADDOCTOR_ID,
       coalesce(
         D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(l.ID,sysdate,'SURNAME FIRSTNAME LASTNAME'),
         l.HEADDOCTOR_FULLNAME)                                HEADDOCTOR_FULLNAME,
       D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(l.ID,sysdate,'FIO') HEADDOCTOR_FIO,
       l.FULLADDRESS,
       l.PHONES,
       l.WEBSITE,
       l.REC_SER_PRIV,
       l.REC_SER,
       l.CODE_LPU,
       l.CODE_OGRN,
       l.CODE_OKPO,
       l.CODE_OKDP,
       l.CODE_OKONH,
       l.CODE_OKATO,
       l.CODE_OKOGU,
       l.CODE_OCOPPH,
       l.CODE_OKFS,
       l.BOOKKEEPER_FULLNAME,
       l.HEADECONOMIST_FULLNAME,
       ld.HEADDOCT                                              LPUDICT_HEADDOCT,
       ld.BOOKKEEPER                                            LPUDICT_BOOKKEEPER,
       ld.LPU_CODE                                              LPUDICT,
       ld.ID                                                    LPUDICT_ID,
       ld.LPU_NAME                                              LPUDICT_NAME,
       ld.LPU_FULLNAME                                          LPUDICT_FULLNAME,
       l.GEOGRAFY                                               GEOGRAFY_ID,
       g.GEONAME                                                GEOGRAFY,
       g.GEOFULL,
       ld.AGENT                                                 AGENT_ID,
       l.USERFORMS,
       l.GENNUMB_GROUP,
       l.EXEC_AUTHORITY,
       l.REC_SER_PRIV_88,
       l.IP_ADDR,
       ld.VERSION,
       ld.IS_RESP,
       l.BY_ES_ONLY,
       ld.DATE_B as LPUDICT_DATE_B,
       ld.DATE_E as LPUDICT_DATE_E,
       coalesce(ld.DATE_E, sysdate) as LPUDICT_DATE_E_SYSDATE,
       l.ADDRESS
  from D_LPU l  --Основная таблица ЛПУ
       left join D_LPUDICT ld on ld.ID = l.LPUDICT  --Список ЛПУ
       left join D_GEOGRAFY g on g.ID = l.GEOGRAFY --Географические понятия
 where exists (select null
                 from D_V_URPRIVS ur
                where coalesce(ur.LPU, -1) = -1
                  and coalesce(ur.VERSION, -1) = -1
                  and ur.UNITCODE = 'LPU'
                  and rownum = 1)
```

---

### Вьюха №15: D_V_CABLAB

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CABLAB
select -- Представление для раздела: Кабинеты и лаборатории
           c.ID,
           c.LPU,
           c.PID,
           c.CID,
           c.CL_CODE,
           c.CL_NAME,
           c.DEPARTMENT DEPARTMENT_ID,
           d.DP_CODE   DEPARTMENT,
           d.DP_NAME   DEPARTMENT_NAME,
           d.DP_KIND   DEPARTMENT_KIND,
           d.DP_TYPE   DEPARTMENT_TYPE,
           d.DIVISION  DEPARTMENT_DIVISION_ID,
           c.DIVISION   DIVISION_ID,
           dv.DIV_CODE  DIVISION_CODE,
           dv.DIV_NAME  DIVISION_NAME,
           c.BUILDING   BUILDING_ID,
           b.CODE      BUILDING_CODE,
           c.FLOOR      FLOOR_ID,
           bf.NAME      FLOOR_NAME,
           c.IS_COMM,
           c.BEGIN_DATE,
           c.END_DATE,
           c.CABLAB_TYPE,
           ct.CABLAB_CODE CABLAB_TYPE_CODE,
           ct.CABLAB_TYPE CABLAB_TYPE_NAME,
           ct.CABLAB_CODE,
           c.CL_BEGIN_DATE,
           c.CL_END_DATE,
           case when trunc(c.CL_BEGIN_DATE) <= trunc(sysdate)
                 and (trunc(c.CL_END_DATE) >= trunc(sysdate) or c.CL_END_DATE is null)
                then 1
                else 0
           end IS_ACTIVE
      from D_CABLAB       c                            -- Кабинеты и лаборатории
      join D_DEPS         d  on d.ID  = c.DEPARTMENT   -- Отделения
 left join D_DIVISIONS    dv on dv.ID = c.DIVISION     -- Подразделения
 left join D_BUILDINGS    b  on b.ID  = c.BUILDING     -- Здания
 left join D_BUILD_FLOORS bf on bf.ID = c.FLOOR        -- Здания: этажи
 LEFT JOIN D_CABLAB_TYPE  ct ON ct.ID  = c.CABLAB_TYPE  -- Типы кабинетов
     where exists (select null from D_V_URPRIVS ur where ur.CATALOG = c.CID and ur.UNITCODE = 'CABLAB' and rownum = 1)
```

---

### Вьюха №16: D_V_DIRECTIONS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_DIRECTIONS
select /*+ use_nl(d pmc pa od ld emp ea espc edep mkb hk injk dk dep bedt hr dcr cemp cea dd ht hdt tk)*/
         d.ID                                     as ID,                                         -- ID направления
         d.LPU                                    as LPU,                                        -- ЛПУ направления
         d.OUTER_DIRECTION                        as OUTER_DIRECTION_ID,                         -- ID внешнего направления
         od.D_DATE||' '||od.D_NUMB                as OUTER_DIRECTION,                            -- Дата и номер внешнего направления
         od.REPRESENT                             as OUTER_DIRECTION_REPRESENT_ID,               -- Направившее ЛПУ
         d.LPU_TO                                 as LPU_TO_ID,                                  -- ЛПУ, куда направляется
         ld.LPU_CODE                              as LPU_TO,                                     -- Код ЛПУ, куда направляется
         ld.LPU_NAME                              as LPU_TO_NAME,                                -- Наименование ЛПУ, куда направляется
         ld.LPU_FULLNAME                          as TO_LPU_FULLNAME,                            -- Полное наименование ЛПУ, куда направляется
         d.LPU_TO_HANDLE                          as LPU_TO_HANDLE,                              -- ЛПУ, куда направляется (ручной ввод)
         d.PATIENT                                as PATIENT_ID,                                 -- Пациент
         pmc.CARD_NUMB                            as PATIENT_CARD,                               -- Номер мед.карты пациента
         pa.SURNAME                               as PAT_SURNAME,                                -- Фамилия пациента
         pa.FIRSTNAME                             as PAT_FIRSTNAME,                              -- Имя пациента
         pa.LASTNAME                              as PAT_LASTNAME,                               -- Отчество пациента
         pa.BIRTHDATE                             as PAT_BIRTHDATE,                              -- ДР пациента
         D_PKG_STR_TOOLS.FIO(
           fsSURNAME  => pa.SURNAME,
           fsNAME     => pa.FIRSTNAME,
           fsPATRNAME => pa.LASTNAME,
           fsRETURN_FULL => 1)                    as PATIENT,                                    -- ФИО пациента
         pa.SEX                                   as PAT_SEX,                                    -- Пол пациента
         pa.ID                                    as PAT_AGENT_ID,                               -- ID контрагента пациента
         d.REG_VISIT                              as REG_VISIT_ID,                               -- ID визита
         case
           when d.REG_VISIT is not null then
             (select to_char(v.VISIT_DATE,D_PKG_STD.FRM_D)||' '||
                     (select vp.VP_CONTENT
                        from D_VISITPLACES vp
                       where vp.ID = v.VISIT_PLACE)
                from D_VISITS v
               where v.ID = d.REG_VISIT)
           else null
         end                                      as REG_VISIT,                                  -- Дата и место визита
         d.REG_EMPLOYER                           as REG_EMPLOYER_ID,                            -- Врач, выписавший направление
         ea.SURNAME                               as REG_EMP_SURNAME,                            -- Фамилия врача выписавшего направление
         ea.FIRSTNAME                             as REG_EMP_FIRSTNAME,                          -- Имя врача выписавшего направление
         ea.LASTNAME                              as REG_EMP_LASTNAME,                           -- Отчество врача выписавшего направление
         emp.JOBTITLE                             as REG_EMPLOYER_JBT_ID,                        -- Должность врача выписавшего направление
         ea.SEX                                   as REG_EMPLOYER_SEX,                           -- Пол врача выписавшего направление
         ea.AGN_CODE                              as REG_EMPLOYER_AGN,                           -- Код агента-врача выписавшего направление
         emp.KOD_VRACHA                           as REG_EMPLOYER,                               -- Код врача, выписавшего направление
         D_PKG_STR_TOOLS.FIO(
           fsSURNAME  => ea.SURNAME,
           fsNAME     => ea.FIRSTNAME,
           fsPATRNAME => ea.LASTNAME)             as REG_EMPLOYER_FIO,                           -- ФИО врача, выписавшего направление
         emp.AGENT                                as REG_EMPLOYER_AGENT,                         -- ID контрагента врача, выписавшего направление
         emp.SPECIALITY                           as REG_EMPLOYER_SPEC_ID,                       -- ID специальности врача, выписавшего направление
         espc.TITLE                               as REG_EMPLOYER_SPEC_NAME,                     -- Специальность врача, выписавшего направление
         emp.DEPARTMENT                           as REG_EMP_DEPARTMENT_ID,                      -- ID отделения врача, выписавшего направление
         edep.DP_NAME                             as REG_EMP_DEPARTMENT,                         -- Отеделение врача, выписавшего направление
         d.REG_DATE                               as REG_DATE,                                   -- Дата создания направления
         d.DIR_COMMENT                            as DIR_COMMENT,                                -- Комментарий к направлению
         d.REG_TYPE                               as REG_TYPE,                                   -- Тип регистрации направления
         (select drt.DRT_NAME
            from D_DIR_REG_TYPES drt
           where drt.DRT_CODE = d.REG_TYPE)       as REG_TYPE_MNEMO,                             -- Наименование типа регистрации направления
         d.DIR_TYPE                               as DIR_TYPE,                                   -- Тип направления: 0 - на услугу; 1 - на госпитализацию
         d.HOSP_MKB                               as HOSP_MKB_ID,                                -- ID диагноза госпитализации
         mkb.MKB_CODE                             as HOSP_MKB,                                   -- Код диагноза госпитализации
         mkb.MKB_NAME                             as HOSP_MKB_NAME,                              -- Диагноз госпитализации
         d.HOSP_KIND                              as HOSP_KIND_ID,                               -- ID вида госпитализации
         hk.HK_NAME                               as HOSP_KIND_NAME,                             -- Наименование вида госпитализации
         hk.HK_CODE                               as HOSP_KIND,                                  -- Код вида госпитализации
         d.DIR_NUMB                               as DIR_NUMB,                                   -- Номер направления
         d.SPECIALITY                             as SPECIALITY_ID,                              -- ID cпециальности
         (select spc.CODE
            from D_SPECIALITIES spc
           where spc.ID = d.SPECIALITY
             and spc.VERSION = D_PKG_VERSIONS.GET_VERSION_BY_LPU(
                                 pnRAISE => 0,
                                 pnLPU   => d.LPU,
                                 psUNITCODE => 'SPECIALITIES')) as SPECIALITY,                   -- Специальность
         (select max(hh.DATE_IN)
            from D_HPK_PLAN_JOURNALS hpkj,
                 D_HOSP_HISTORIES    hh
           where hpkj.DIRECTION      = d.ID
             and hh.HPK_PLAN_JOURNAL = hpkj.ID)   as LAST_HOSP_DATE,                             -- Дата последней госпитализации
         d.EX_CAUSE_MKB                           as EX_CAUSE_MKB_ID,                            -- ID внешней причины травмы
         (select ecmkb.MKB_CODE
            from D_MKB10 ecmkb
           where ecmkb.ID = d.EX_CAUSE_MKB)       as EX_CAUSE_MKB,                               -- Код внешней причины травмы
         d.INJURE_KIND                            as INJURE_KIND_ID,                             -- ID вида травмы
         injk.IK_CODE                             as INJURE_KIND,                                -- Код вида травмы
         injk.IK_NAME                             as INJURE_KIND_NAME,                           -- Наименование вида травмы
         d.INJURE_TIME                            as INJURE_TIME,                                -- Прошло времени с получения травмы (часы)
         d.DIRECTION_KIND                         as DIRECTION_KIND_ID,                          -- ID вида направления
         dk.DK_CODE                               as DIRECTION_KIND_CODE,                        -- Код вида направления
         dk.DK_NAME                               as DIRECTION_KIND,                             -- Наименование вида направления
         d.HOSP_DEP                               as HOSP_DEP_ID,                                -- ID отделения госпитализации
         dep.DP_CODE                              as HOSP_DEP,                                   -- Код отделения госпитализации
         dep.DP_NAME                              as HOSP_DEP_NAME,                              -- Наименование отделения госпитализации
         d.DIR_PREF                               as DIR_PREF,                                   -- Префикс направления
         d.HOSP_BED_TYPE                          as HOSP_BED_TYPE_ID,                           -- ID профиля койки госпитализации
         bedt.BT_CODE                             as HOSP_BED_TYPE,                              -- Код профиля койки госпитализации
         bedt.BT_NAME                             as HOSP_BED_TYPE_NAME,                         -- Наименование профиля койки госпитализации
         d.MES                                    as MES_ID,                                     -- ID МЭС
         (select mes.M_CODE
            from D_MESES mes
           where mes.ID = d.MES)                  as MES,                                        -- Код МЭС
         d.REG_HPKPJ                              as REG_HPKPJ,                                  -- Запись очереди, по которой назначено напрвление
         d.HOSP_REASON                            as HOSP_REASON_ID,                             -- ID причины госпитализации
         hr.HR_CODE                               as HOSP_REASON,                                -- Код причины госпитализации
         hr.HR_NAME                               as HOSP_REASON_NAME,                           -- Наименование причины госпитализации
         d.IS_CANCELED                            as IS_CANCELED,                                -- Признак отказа: 0 - нет, 1 - да
         d.CANC_REASON                            as CANC_REASON_ID,                             -- ID причины отказа
         dcr.DCR_CODE                             as CANC_REASON,                                -- Код причины отказа
         dcr.DCR_NAME                             as CANC_REASON_NAME,                           -- Наименование причины отказа
         d.CANC_EMPLOYER                          as CANC_EMPLOYER,                              -- Сотрудник, выполнивший отказ
         coalesce(d.CANC_EMPLOYER_FIO,
                  D_PKG_STR_TOOLS.FIO(
                    fsSURNAME  => cea.SURNAME,
                    fsNAME     => cea.FIRSTNAME,
                    fsPATRNAME => cea.LASTNAME))  as CANC_EMPLOYER_FIO,                          -- ФИО сотрудника, выполнившего отказ
         d.CANC_DATE                              as CANC_DATE,                                  -- Дата отказа
         d.REG_DIR_SERV                           as REG_DIR_SERV,                               -- Направление, на котором выписано направление
         d.HOSP_PLAN_DATE                         as HOSP_PLAN_DATE,                             -- Плановая дата госпитализации
         d.DATE_TR                                as DATE_TR,                                    -- Дата травмы
         d.HOSP_DEPDICT                           as HOSP_DEPDICT_ID,                            -- ID отделения в списке
         dd.DEP_CODE                              as HOSP_DEPDICT,                               -- Код отделения в списке
         dd.DEP_NAME                              as HOSP_DEPDICT_NAME,                          -- Наименование отделения в списке
         d.DOC_COMMENT                            as DOC_COMMENT,                                -- Комментарий врача
         d.HOSP_TYPE                              as HOSP_TYPE_ID,                               -- ID типа госпитализации
         ht.HK_CODE                               as HOSP_TYPE,                                  -- Код типа госпитализации
         ht.HK_NAME                               as HOSP_TYPE_NAME,                             -- Наименование типа госпитализации
         d.TALON_VMP_DATE                         as TALON_VMP_DATE,                             -- Дата выдачи талона на ВМП
         d.HOSP_DIRECT_TYPE                       as HOSP_DIRECT_TYPE_ID,                        -- ID типа направления
         hdt.DT_CODE                              as HOSP_DIRECT_TYPE,                           -- Код типа направления
         hdt.DT_NAME                              as HOSP_DIRECT_TYPE_NAME,                      -- Наименование типа направления
         d.HOSP_REASON_STREETKIDS                 as HOSP_REASON_STREETKIDS,                     -- Причина помещения в МО (беспризорные)
         d.TRANSPORTATION_KIND                    as TRANSPORTATION_KIND_ID,                     -- ID типа транспортировки
         tk.TK_CODE                               as TRANSPORTATION_KIND,                        -- Код типа транспортировки
         tk.TK_NAME                               as TRANSPORTATION_KIND_NAME,                   -- Наименование типа транспортировки
         d.HOSP_HOUR                              as HOSP_HOUR,                                  -- Код количества часов с начала заболевания до госпитализации
         (select hhour.HOUR_NAME
            from D_HOSP_HOURS hhour
           where hhour.HOUR_CODE = d.HOSP_HOUR)   as HOSP_HOUR_NAME,                             -- Наименование количества часов с начала заболевания до госпитализации
         d.HOSP_MKB_EXACT                         as HOSP_MKB_EXACT,                             -- Диагноз госпитализации уточненный
         d.TALON_VMP_NUM                          as TALON_VMP_NUM,                              -- Номер талона на ВМП
         d.IS_ONKO                                as IS_ONKO,                                    -- Признак направления при подозрении на ЗНО /направления при онкозаболевании (1 - да)
         d.REG_DEP                                as REG_DEP_ID,                                 -- ID Направившее отделение
         d.REALISED_DAYS                          as REALISED_DAYS,                              -- Количество дней реализации направления
         d.DIR_FORM                               as DIR_FORM,                                   -- Форма направления: 1 - форма 028/у, 2 - форма 057/у
         emp.QUOT_RESOURCE                        as REG_EMP_QUOT_RESOURCE,                      -- Ресурс квотирования врача
         edep.DIVISION                            as REG_EMP_DIVISION,                           -- Подразделение ЛПУ врача
         d.THERAPY_SCHEMES                        as MED_THERAPY_SCHEME_ID,
         mts.CODE                                 as MED_THERAPY_SCHEME_CODE,
         d.VMP,
         d.DIRECTION_FORM                         as DIRECTION_FORM_ID,                          -- ID формы мед. помощи
         df.DS_CODE                               as DIRECTION_FORM_CODE,                        -- Код формы мед. помощи
         df.DS_NAME                               as DIRECTION_FORM_NAME,                        -- Название формы мед. помощи
         d.TYPE_MED_HELP                          as TYPE_MED_HELP_ID,                           -- ID Вида медицинской помощи
         tmh.CODE                                 as TYPE_MED_HELP_CODE,                         -- Код вида мед.помощи
         tmh.NAME                                 as TYPE_MED_HELP_NAME,                         -- Название вида помощи
         d.DIRECTION_CONDITION                    as DIRECTION_CONDITION_ID,                     -- ID условий оказаний мед. помощи
         dc.DC_CODE                               as DIRECTION_CONDITION_CODE,                   -- Код условий оказаний мед. помощи
         dc.DC_NAME                               as DIRECTION_CONDITION_NAME,                   -- Название условий оказаний мед. помощи
         d.DIRECTION_REASON
    from D_DIRECTIONS d                                                                     -- Направления
         join D_PERSMEDCARD pmc                 on pmc.ID = d.PATIENT                       -- Карта пациента
         join D_AGENTS pa                       on pa.ID = pmc.AGENT                        -- Контрагенты
         left join D_OUTER_DIRECTIONS od        on od.ID = d.OUTER_DIRECTION                -- Внешние направления
         left join D_LPUDICT ld                 on ld.ID = d.LPU_TO                         -- Список ЛПУ
         left join D_EMPLOYERS emp              on emp.ID = d.REG_EMPLOYER                  -- Врачи
         left join D_AGENTS ea                  on ea.ID = emp.AGENT                        -- ФИО врача
         left join D_SPECIALITIES espc          on espc.ID = emp.SPECIALITY                 -- Специальность врача
         left join D_DEPS edep                  on edep.ID = emp.DEPARTMENT                 -- Отделение врача
         left join D_MKB10 mkb                  on mkb.ID = d.HOSP_MKB                      -- Справочник МКБ-10
         left join D_HOSPITALIZATIONKINDS hk                                                -- Виды госпитализации
                on hk.ID = d.HOSP_KIND
               and hk.VERSION = D_PKG_VERSIONS.GET_VERSION_BY_LPU(
                                  pnRAISE => 0,
                                  pnLPU   => d.LPU,
                                  psUNITCODE => 'HOSPITALIZATIONKINDS')
         left join D_INJURE_KINDS injk          on injk.ID = d.INJURE_KIND                  -- Виды травм
         left join D_DIRECTION_KINDS dk         on dk.ID = d.DIRECTION_KIND                 -- Вид направления
         left join D_DEPS dep                   on dep.ID = d.HOSP_DEP                      -- Отделения
         left join D_BED_TYPES bedt             on bedt.ID = d.HOSP_BED_TYPE                -- Профили коек
         left join D_HOSP_REASONS hr            on hr.ID = d.HOSP_REASON                    -- Причины госпитализации
         left join D_DIR_CANC_REASONS dcr       on dcr.ID = d.CANC_REASON                   -- Причина отказа от госпитализации
         left join D_EMPLOYERS cemp             on cemp.ID = d.CANC_EMPLOYER                -- Сотрудник, выполнивший отказ
         left join D_AGENTS cea                 on cea.ID = cemp.AGENT                      -- Контрагент сотрудника, выполнившего отказ
         left join D_DEPDICT dd                 on dd.ID = d.HOSP_DEPDICT                   -- Отделение в списке
         left join D_HOSPITALIZATION_TYPES ht   on ht.ID = d.HOSP_TYPE                      -- Типы госпитализации
         left join D_HOSP_DIRECT_TYPES hdt      on hdt.ID = d.HOSP_DIRECT_TYPE              -- Тип направления
         left join D_TRANSPORTATION_KINDS tk    on tk.ID = d.TRANSPORTATION_KIND            -- Виды транспортировки
         left join D_MED_THERAPY_SCHEMES mts    on mts.ID = d.THERAPY_SCHEMES
         left join D_DIRECTION_FORMS df on df.ID = d.DIRECTION_FORM                         -- Формы оказания медицинской помощи
         left join D_TYPE_MED_HELP tmh on tmh.ID = d.TYPE_MED_HELP                          -- Вид медицинской помощи
         left join D_DIRECTION_CONDITIONS dc on dc.ID = d.DIRECTION_CONDITION               -- Условия оказания медицинской помощи
   where exists (select null
                   from D_V_URPRIVS ur
                  where ur.LPU = d.LPU
                    and ur.UNITCODE = 'DIRECTIONS'
                    and rownum = 1)
```

---

### Вьюха №17: D_V_OUTER_DIRECTIONS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_OUTER_DIRECTIONS
select --Представление для раздела: Внешние направления
       od.ID,
       od.LPU,
       od.PATIENT PATIENT_ID,
       p.CARD_NUMB PATIENT_CARD,
       a.SURNAME||' '||a.FIRSTNAME||' '||a.LASTNAME PATIENT,
       od.D_DATE,
       od.D_NUMB,
       od.REPRESENT REPRESENT_ID,
       ld.ID                      LPUDICT_ID,
       ld.LPU_CODE REPRESENT_CODE,
       ld.LPU_NAME REPRESENT_LPU_NAME,
       ld.LPU_FULLNAME REPRESENT_LPU_FULLNAME,
       ld.DATE_B LPU_DATE_B,
       ld.DATE_E LPU_DATE_E,
       ar.agn_okpo REPRESENT_OKPO,
       ar.AGN_NAME REPRESENT,
       od.REPRESENT_HANDLE,
       od.DIAGNOSIS DIAGNOSIS_ID,
       mkb.MKB_CODE DIAGNOSIS,
       mkb.MKB_NAME DIAGNOSIS_NAME,
       od.DIAGNOSIS_HANDLE,
       od.DOCTOR    DOCTOR_ID,
       ad.AGN_CODE DOCTOR,
       ad.SNILS DOCTOR_SNILS,
       DECODE(od.DOCTOR, null, null, D_PKG_STR_TOOLS.FIO(ad.SURNAME, ad.FIRSTNAME, ad.LASTNAME))  DOCTOR_FIO,
       od.DOCTOR_HANDLE,
       (select max(dir.REG_DATE)
          from D_DIRECTIONS dir
         where dir.OUTER_DIRECTION = od.ID)  LAST_DIR_DATE,
       od.D_PREF,
       od.REPRESENT_DIRECTION,
       od.HOSP_PLAN_DATE,
       od.DOC_SPECIALITY,
       s.CODE          DOC_SPECIALITY_CODE,
       od.REASON       REASON_ID,
       rd.NAME_REASON  REASON,
       od.D_DATE_END,
       od.SERVICE,
       sv.SE_CODE      SERVICE_CODE,
       sv.SE_NAME      SERVICE_NAME,
       sv.SE_TYPE      SERVICE_TYPE,
       od.EX_SYSTEM,
       es.S_CODE       EX_SYSTEM_CODE,
       ar.AGN_OGRN     REPRESENT_OGRN,
       a.ID            AGENT_ID,
       od.DIAGNOSIS_EXACT,
       od.INCLUDE_RESULT,
       od.DEPARTMENT,
       od.JOBTITLE JOBTITLE_ID,
       j.TITLE JOBTITLE,
       od.OUTDIR_TYPE,
       od.DISEASECHARACTER,
       od.TO_DOCTOR,
       od.SPECIALITY,
       sp.CODE         SPECIALITY_CODE,
       sp.TITLE        SPECIALITY_TITLE,
       od.PROFILE      PROFILE_ID,
       ep.P_NAME       PROFILE,
       od.SCH_RESOURCE,
       od.DIRECTION_FORM DIRECTION_FORM_ID,
       df.DS_CODE DIRECTION_FORM_CODE,
       df.DS_NAME DIRECTION_FORM_NAME,
       od.TYPE_MED_HELP TYPE_MED_HELP_ID,
       tmh.CODE TYPE_MED_HELP_CODE,
       tmh.NAME TYPE_MED_HELP_NAME,
       od.DIRECTION_CONDITION DIRECTION_CONDITION_ID,
       dc.DC_CODE DIRECTION_CONDITION_CODE,
       dc.DC_NAME DIRECTION_CONDITION_NAME,
       od.DIRECTION_REASON,
       od.EMPLOYER,
       od.DIR_NOTE
  from D_OUTER_DIRECTIONS od	                                           --Внешние направления
       join D_PERSMEDCARD           p	   on p.ID = od.PATIENT	           --Персональные медицинские карты
       join D_AGENTS                a    on p.AGENT = a.ID               --Контрагенты
       left join D_AGENTS           ar	 on ar.ID = od.REPRESENT         --Контрагенты
       left join D_MKB10            mkb	 on mkb.ID = od.DIAGNOSIS        --Справочник МКБ-10
       left join D_LPUDICT          ld   on ld.AGENT = ar.ID             --Список ЛПУ
       left join D_AGENTS           ad   on ad.ID = od.DOCTOR            --Контрагенты
       left join D_SPECIALITIES     s    on s.ID = od.DOC_SPECIALITY     --Специальности
       left join D_REASON_DIRECTION rd   on rd.ID = od.REASON            --Обоснования направлений
       left join D_EX_SYSTEMS       es   on es.ID = od.EX_SYSTEM         --Внешние системы
       left join D_SERVICES         sv   on sv.ID = od.SERVICE           --Услуги
       left join D_JOBTITLES        j    on j.id = od.JOBTITLE           --Должность врача, к которому осуществляется направление
       left join D_SPECIALITIES     sp   on sp.ID = od.SPECIALITY        --Специальности
       left join D_ER_PROFILES      ep   on ep.ID = od.PROFILE           --Профили
       left join D_DIRECTION_FORMS df on df.ID = od.DIRECTION_FORM       --Формы оказания медицинской помощи
       left join D_TYPE_MED_HELP tmh on tmh.ID = od.TYPE_MED_HELP        --Вид медицинской помощи
       left join D_DIRECTION_CONDITIONS dc on dc.ID = od.DIRECTION_CONDITION --Условия оказания медицинской помощи
       left join D_CABLAB c on c.ID = p.CABLAB
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU = od.LPU
                  and ur.UNITCODE = 'OUTER_DIRECTIONS'
                  and rownum = 1)
```

---

### Вьюха №18: D_V_HOSP_HISTORY_DEPS

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HOSP_HISTORY_DEPS
select           --Представление для раздела : Истории болезней : отделения
          t.ID,
          t.PID,
          t.DATE_IN,
          t.DATE_OUT,
          t.DEP          DEP_ID,
          t1.DP_CODE     DEP,
          t1.DP_NAME     DEP_NAME,
          t1.DIVISION    DEP_DIVISION,
          t1.DP_TYPE     DP_TYPE_ID,
          t3.DT_CODE     DP_TYPE,
          t3.DT_NAME     DP_NAME,
          t3.ID          DEPS_TYPE_ID,
          t.LPU,
          t.MKB          MKB_ID,
          t2.MKB_CODE    MKB,
          t2.MKB_NAME    MKB_NAME,
          t.MKB_EXACT,
          t12.MKB_CLINIC_EXACT,
          t.HEALING_EMP,
          t8.AGENT       HEALING_EMP_AGENT,
          t8.KOD_VRACHA  HEALING_EMP_KOD,
          a.SURNAME      HEALING_EMP_SURNAME,
          a.FIRSTNAME    HEALING_EMP_FIRSTNAME,
          a.LASTNAME     HEALING_EMP_LASTNAME,
          t.PAYMENT_KIND PAYMENT_KIND_ID,
          t4.PK_CODE     PAYMENT_KIND,
          t4.PK_NAME     PAYMENT_KIND_NAME,
          t4.IS_COMMERC  PAYMENT_KIND_IS_COMMERC,
          t.HOSP_RESULT  HOSP_RESULT_ID,
          t5.R_CODE      HOSP_RESULT,
          t5.R_NAME      HOSP_RESULT_NAME,
          case when t.HOSP_RESULT is not null then t5.R_CODE || ' - ' || t5.R_NAME
               else null
          end            HOSP_RESULT_CODENAME,
          t.KSG          KSG_ID,
          t6.KSG_CODE    KSG,
          t.BED_TYPE     BED_TYPE_ID,
          t7.BT_CODE     BED_TYPE,
          t7.BT_NAME     BED_TYPE_NAME,
          D_PKG_STR_TOOLS.FIO(a.SURNAME, a.FIRSTNAME, a.LASTNAME) FIO,
          coalesce((select D_PKG_STR_TOOLS.FIO(an.SURNAME, an.FIRSTNAME, an.LASTNAME)
                      from D_AGENT_NAMES an
                     where an.PID = a.ID
                       and an.BEGIN_DATE <= trunc(t.DATE_OUT)
                       and (an.END_DATE  >= trunc(t.DATE_OUT) or an.END_DATE is null)),
                  D_PKG_STR_TOOLS.FIO(a.SURNAME, a.FIRSTNAME, a.LASTNAME)) FIO_ACTUAL,
          coalesce(t12.MKB_CLINIC, t12.MKB_RECEIVE, t12.MKB_SEND) DIAGNOSIS_ID,
          m1.MKB_CODE    DIAGNOSIS_CODE,
          m1.MKB_NAME    DIAGNOSIS_NAME,
          t12.DISCARD_STATUS,
          t12.PATIENT,
          t12.DISEASECASE,
          t.FACIAL_ACCOUNT,
          t.HHD_PREF,
          t.HHD_NUMB,
          t.VMP VMP_ID,
          t13.VMP_CODE   VMP,
          t13.VMP_NAME   VMP_NAME,
          t.PRVSID,
          t.HHD_LEVEL,
          t.HOSP_OUTCOME HOSP_OUTCOME_ID,
          t14.R_CODE     HOSP_OUTCOME,
          t14.R_NAME     HOSP_OUTCOME_NAME,
          case when t.HOSP_OUTCOME is not null then t14.R_CODE || ' - ' || t14.R_NAME
               else null
          end            HOSP_OUTCOME_CODENAME,
          t.IS_LAST,
          t12.DATE_IN    HOSP_HISTORY_DATE_IN,
          t.MTS_DESC,
          t16.CODE       MTS_CODE,
          t16.NAME       MTS_NAME,
          t16.DESCRIPTION  MTS_DESCRIPTION,
          t.ALV,
          t17.ALV_CODE,
          t17.ALV_NAME,
          t.SCALE_REHAB,
          t18.SR_CODE,
          t18.SR_NAME,
          t.KSGID
     from D_HOSP_HISTORY_DEPS       t        --Истории болезней : отделения
          join D_HOSP_HISTORIES     t12 on t12.ID = t.PID          --Истории болезни
          join D_DEPS               t1 on t1.ID = t.DEP            --Отделения
          join D_DEPS_TYPES         t3 on t3.ID = t1.dp_type       --Тип отделения
          join D_PAYMENT_KIND       t4 on t4.ID = t.PAYMENT_KIND   --Виды оплаты
          left join D_MKB10         t2 on t2.ID = t.MKB            --Справочник МКБ-10
          left join D_HOSP_RESULTS  t5 on t5.ID = t.HOSP_RESULT    --Результат госпитализации
          left join D_KSGCODES      t6 on t6.ID = t.KSG            --Коды клинико-cтатистических групп
          left join D_BED_TYPES     t7 on t7.ID = t.BED_TYPE       --Профили коек
          left join D_EMPLOYERS     t8 on t8.ID = t.HEALING_EMP    --Сотрудники
          left join D_AGENTS        a on a.ID = t8.AGENT           --Контрагент
          left join D_MKB10         m1 on m1.ID = nvl(t12.MKB_CLINIC, nvl(t12.MKB_RECEIVE, t12.MKB_SEND)) --Справочник МКБ-10
          left join D_VMP           t13 on t13.ID = t.VMP          --ВМП
          left join D_HOSP_OUTCOMES t14 on t14.ID = t.HOSP_OUTCOME --Исход госпитализации
          left join D_MED_THERAPY_SCHEMES_DESC t15 on t15.ID = t.MTS_DESC --Схемы лекарственного лечения : Дополнительные поля
          left join D_MED_THERAPY_SCHEMES      t16 on t16.ID = t15.PID    --Схемы лекарственного лечения
          left join D_ALV           t17 on t17.ID = t.ALV         --Проведение ИВЛ
          left join D_SCALE_REHAB   t18 on t18.ID = t.SCALE_REHAB --Шкала реабилитационной маршрутизации
    where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'HOSP_HISTORY_DEPS')
```

---

### Вьюха №19: D_V_HOSP_HISTORIES

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HOSP_HISTORIES
select -- Представление для раздела : Истории болезни
       t.ID,
       t.LPU,
       t.HPK_PLAN_JOURNAL,
       t.PATIENT               PATIENT_ID,
       t1.CARD_NUMB            PATIENT,
       t1.AGENT                PATIENT_AGENT,
       t1a.SURNAME||' '||t1a.FIRSTNAME||' '||t1a.LASTNAME
                               PATIENT_FIO,
       t1a.SURNAME             PATIENT_SURNAME,
       t1a.FIRSTNAME           PATIENT_FIRSTNAME,
       t1a.LASTNAME            PATIENT_LASTNAME,
       t1a.BIRTHDATE           PATIENT_BIRTHDATE,
       t1a.SEX                 PATIENT_SEX,
       t1a.SNILS              PATIENT_SNILS,
       t.HH_PREF,
       t.HH_NUMB,
       t.HH_NUMB_TYPE,
       t.HH_NUMB_ALTERN,
       t.HOSP_REASON           HOSP_REASON_ID,
       t2.HR_CODE              HOSP_REASON,
       t2.HR_NAME              HOSP_REASON_NAME,
       t.RECEPTION_EMP,
       t.DATE_IN,
       t.PLAN_DATE_OUT,
       t.DATE_OUT,
       t.HOSPITALIZATION_TYPE HOSPITALIZATION_TYPE_ID,
       t3.HK_CODE             HOSPITALIZATION_TYPE,
       t3.HK_NAME             HOSPITALIZATION_TYPE_NAME,
       t.TRANSPORTATION_KIND  TRANSPORTATION_KIND_ID,
       t4.TK_CODE             TRANSPORTATION_KIND,
       t4.TK_NAME             TRANSPORTATION_KIND_NAME,
       t.LPU_FROM             LPU_FROM_ID,
       t5.LPU_CODE            LPU_FROM,
       t5.LPU_NAME            LPU_FROM_NAME,
       t.MKB_SEND             MKB_SEND_ID,
       t6.MKB_CODE            MKB_SEND,
       t6.MKB_NAME            MKB_SEND_NAME,
       t.MKB_SEND_EXACT,
       t.MKB_CLINIC           MKB_CLINIC_ID,
       t8.MKB_CODE            MKB_CLINIC,
       t8.MKB_NAME            MKB_CLINIC_NAME,
       t.MKB_CLINIC_EXACT,
       t.MKB_CLINIC_DATE,
       t.MKB_FINAL            MKB_FINAL_ID,
       t10.MKB_CODE           MKB_FINAL,
       t10.MKB_NAME           MKB_FINAL_NAME,
       t.MKB_FINAL_EXACT,
       t.MKB_FIN_COMP         MKB_FIN_COMP_ID,
       t12.MKB_CODE           MKB_FIN_COMP,
       t12.MKB_NAME           MKB_FIN_COMP_NAME,
       t.MKB_FIN_COMP_EXACT,
       t.MKB_FIN_ADD          MKB_FIN_ADD_ID,
       t14.MKB_CODE           MKB_FIN_ADD,
       t14.MKB_NAME           MKB_FIN_ADD_NAME,
       t.MKB_FIN_ADD_EXACT,
       t.HOSP_TIMES,
       t.HOSP_RESULT          HOSP_RESULT_ID,
       t16.R_CODE             HOSP_RESULT,
       t16.R_NAME             HOSP_RESULT_NAME,
       t.MKB_RECEIVE          MKB_RECEIVE_ID,
       t17.MKB_CODE           MKB_RECEIVE,
       t17.MKB_NAME           MKB_RECEIVE_NAME,
       t.MKB_RECEIVE_EXACT,
       t.RELATIVE             RELATIVE_ID,
       (case when t.RELATIVE is null then null
             else (select D_PKG_STR_TOOLS.FIO(t19a.SURNAME, t19a.FIRSTNAME, t19a.LASTNAME)
                     from D_AGENT_RELATIVES t19
                          join D_AGENTS t19a on t19a.ID = t19.AGENT
                    where t19.ID = t.RELATIVE)
        end) RELATIVE,
       t.DISEASECASE,
       t.DISCARD_STATUS,
       t.IS_WELL_TIMED_HOSP,
       t.IS_ENOUGH_VOLUME,
       t.IS_CORRECT_HEALING,
       t.IS_SAME_DIAGN,
       t20.DIRECTION,
       t20.PAYMENT_KIND,
       t20.HAS_PRIVILEGES,
       t20.REGISTER_DATE,
       t20.HH_DIRECTION_DATE,
       t22.VS_NAME            HAS_PRIVILEGES_MNEMO,
       t.ARCH_DATE,
       t.ARCH_DEP             ARCH_DEP_ID,
       t21.DP_CODE            ARCH_DEP,
       t21.DP_NAME            ARCH_DEP_NAME,
       t.ARCH_COMMENT,
       t.HH_TYPE,
       t.HOSP_HOUR            HOSP_HOUR_ID,
       t24.HOUR_NAME          HOSP_HOUR,
       t.HOSP_OUTCOME         HOSP_OUTCOME_ID,
       t23.R_CODE             HOSP_OUTCOME,
       t23.R_NAME             HOSP_OUTCOME_NAME,
       t.TRANSFER_LPU,
       t.TRANSFER_REASON,
       t25.LPU_CODE            TRANSFER_LPU_CODE,
       t25.LPU_NAME            TRANSFER_LPU_NAME,
       t26.TR_NAME             TRANSFER_REASON_NAME,
       t.HH_NUMB_FULL,
       t.HH_NUMB_MASK,
       t.ABILITY_STATUS        ABILITY_STATUS_ID,
       t27.AS_NAME             ABILITY_STATUS,
       t.OTHER_THERAPY,
       t.FEATURES,
       t.DATE_DEPARTURE,
       t.RELATIVE_HH,
       t.ABANDONMENT,
       t.DEATH_CAME            DEATH_CAME_ID,
       t28.DD_CODE             DEATH_CAME_CODE,
       t28.DD_NAME             DEATH_CAME_NAME,
       t.ARRIVE_ORDER,
       t.JUDGE_DECISION,
       t.HOSP_INCOME           HOSP_INCOME_ID,
       t29.CODE                HOSP_INCOME,
       t29.INCOME_NAME,
       t.HOSP_IS_FIRST,
       t.SEIZED_ITEMS,
       t.NOVOR_NUM,
       t.DIRECTION_HOSP        DIRECTION_HOSP_ID,
       t30.DH_CODE             DIRECTION_HOSP,
       t30.DH_NAME             DIRECTION_HOSP_NAME,
       t1.NOTE,
       t.ATTENDING_EMPL_ID,
       t.DEPARTMENT_ID
  from D_HOSP_HISTORIES t                                                --Истории болезни
       join D_PERSMEDCARD t1 on t1.ID = t.PATIENT                        --Персональные медицинские карты
       join D_AGENTS t1a on t1a.ID = t1.AGENT                            --Контрагенты
       join D_HOSP_REASONS t2 on t2.ID = t.HOSP_REASON                   --Причины госпитализации
       join D_HOSPITALIZATION_TYPES t3 on t3.ID = t.HOSPITALIZATION_TYPE --Тип госпитализации
       join D_TRANSPORTATION_KINDS t4 on t4.ID = t.TRANSPORTATION_KIND   --Виды транспортировки
       left join D_LPUDICT t5 on t5.ID = t.LPU_FROM                      --Список ЛПУ
       left join D_MKB10 t6 on t6.ID = t.MKB_SEND                        --Справочник МКБ-10
       left join D_MKB10 t8 on t8.ID = t.MKB_CLINIC                      --Справочник МКБ-10
       left join D_MKB10 t10 on t10.ID = t.MKB_FINAL                     --Справочник МКБ-10
       left join D_MKB10 t12 on t12.ID = t.MKB_FIN_COMP                  --Справочник МКБ-10
       left join D_MKB10 t14 on t14.ID = t.MKB_FIN_ADD                   --Справочник МКБ-10
       left join D_HOSP_RESULTS t16 on t16.ID = t.HOSP_RESULT            --Результат госпитализации
       left join D_MKB10 t17 on t17.ID = t.MKB_RECEIVE                   --Справочник МКБ-10
       left join D_DEPS t21 on t21.ID = t.ARCH_DEP                       --Отделения
       left join D_HPK_PLAN_JOURNALS t20 on t20.ID = t.HPK_PLAN_JOURNAL  --Журнал госпитализации
       left join D_HPKPJ_VMP_STATES t22 on t22.VS_CODE = t20.HAS_PRIVILEGES
       left join D_HOSP_OUTCOMES t23 on t23.ID = t.HOSP_OUTCOME          --Исход госпитализации
       left join D_HOSP_HOURS t24 on t24.HOUR_CODE = t.HOSP_HOUR         --Количество часов с начала заболевания
       left join D_LPUDICT t25 on  t25.ID = t.TRANSFER_LPU               --ЛПУ перевода
       left join D_HH_TRANSFER_REASONS t26 on t26.ID = t.TRANSFER_REASON --Причины госпитализации в другое ЛПУ при выписке из стационара
       left join D_ABILITY_STATUS t27 on t27.ID = t.ABILITY_STATUS       --Статус трудоспособности
       left join D_DIRECTORIES_DATA_VER t28 on t28.ID = t.DEATH_CAME     --Смерть наступила(роды)
       left join D_HOSP_INCOMES t29 on t29.ID = t.HOSP_INCOME            --Откуда поступил
       left join D_DIRECTION_HOSP t30 on t30.ID = t.DIRECTION_HOSP       --Направлен на долечивание
 where t.DISCARD_STATUS = 0
   and t.RELATIVE_HH is null
   and exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU = t.LPU
                  and ur.UNITCODE = 'HOSP_HISTORIES'
                  and rownum = 1)
```

---

### Вьюха №20: D_V_HPK_SCHEDULE_REG

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_SCHEDULE_REG
select hsr.ID,
       hsr.DIRECTION,
       hsr.HPK_SCHEDULE,
       hsr.DATE_REC,
       hsr.EMPLOYER,
       hsr.DATE_CREATE
  from D_HPK_SCHEDULE_REG hsr
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU is null
                  and ur.VERSION is null
                  and ur.UNITCODE = 'HPK_SCHEDULE_REG'
                  and rownum = 1)
```

---

### Вьюха №21: D_V_HOSP_HISTORY_DEPS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HOSP_HISTORY_DEPS_BASE
select --Представление для раздела : Истории болезней : отделения (базовое)
       t.ID,
       t.PID,
       t.DATE_IN,
       t.DATE_OUT,
       t.DEP,
       t.LPU,
       t.MKB,
       t.MKB_EXACT,
       t.HEALING_EMP,
       t.PAYMENT_KIND,
       t.HOSP_RESULT,
       t.KSG,
       t.BED_TYPE,
       t.FACIAL_ACCOUNT,
       t.HHD_PREF,
       t.HHD_NUMB,
       t.VMP,
       t.PRVSID,
       t.HHD_LEVEL,
       t.HOSP_OUTCOME,
       t.IS_LAST,
       t.MTS_DESC,
       t.ALV,
       t.SCALE_REHAB,
       t.KSGID
  from D_HOSP_HISTORY_DEPS t   --Истории болезней : отделения
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'HOSP_HISTORY_DEPS' and rownum = 1)
```

---

### Вьюха №22: D_V_CONTRACTS_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CONTRACTS_BASE
select --Представление для раздела : Договоры
       t.ID,
       t.LPU,
       t.CID,
       t.CONTRACT,
       t.EMPLOYER      EMPLOYER_ID,
       t.DOC_PREF,
       t.DOC_NUMB,
       t.DOC_PREF||'/'||t.DOC_NUMB DOC_PREF_NUMB,
       t.DOC_DATE,
       t.EXT_NUMB,
       t.EXT_DATE,
       t.AGENT         AGENT_ID,
       t2.AGN_CODE     AGENT,
       t2.AGN_NAME     AGENT_NAME,
       t.DATE_BEGIN,
       t.DATE_END,
       t.PHONE,
       t.PERSON,
       t.SUMM,
       t.IS_OPEN,
       t.FACIAL_ACCOUNT,
       t.DEBT_SUMM,
       t.CONTRACT_SUMM,
       t.PLAN_SUMM,
       t.NOTE,
       t.STATUS,
       t.WORKDATE,
       t.IS_IMPORTED,
       t.REPRESENT     REPRESENT_ID,
       t.CONTRACT_TYPE
  from D_CONTRACTS t
       join D_AGENTS t2 on t2.ID = t.AGENT
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'CONTRACTS' and rownum = 1)
```

---

### Вьюха №23: D_V_SMP_CALL_EX_SYSTEM

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SMP_CALL_EX_SYSTEM
select -- Представление для раздела : Интеграция с АСУ Скорая помощь
       sces.ID,
       sces.VERSION,
       sces.LPU_CODE,
       sces.AGENT AGENT_ID,
       a.SURNAME,
       a.FIRSTNAME,
       a.LASTNAME,
       sces.LPU_CODE_SMP,
       sces.CALL_ID,
       sces.CALL_NUMB,
       sces.CALL_DATE,
       sces.CALL_PLACE,
       sces.CALL_REASON,
       sces.CALL_STRUCTURE,
       sces.MAIN_MKB,
       sces.ADD_MKB,
       sces.COMPLICATIONS,
       sces.DELIVERY_TYPE,
       sces.HELP_ON_PLACE,
       sces.HELP_IN_CAR,
       sces.HOSP_STATUS,
       case sces.HOSP_STATUS
            when 0 then 'начало доставки'
            when 1 then 'отмена доставки'
            when 2 then 'факт доставки'
            when 3 then 'принят в приемном покое'
            when 4 then 'госпитализирован'
            when 5 then 'отказано в госпитализации'
            when 6 then 'выписан из стационара'
            else ''
       end HOSP_STATUS_NAME,
       sces.REFUSE_REASON,
       sces.CALL_ADDRESS,
       sces.PHONE,
       sces.CALL_TYPE,
       sces.CALL_STATUS,
       case sces.CALL_STATUS
            when 1 then 'новый'
            when 2 then 'отмена'
            when 3 then 'отказ'
            when 4 then 'подтверждение'
            when 5 then 'факт оказания'
            else ''
       end CALL_STATUS_NAME,
       sces.SEND_TIME,
       sces.EMPLOYER,
       sces.REFUSE_REASON_POL,
       sces.WHO_CALL,
       sces.CALL,
       sces.ACCIDENT_CAUSE,
       sces.PROFILE_TEAMS,
       sces.INTOXICATION,
       sces.COMPLAINTS,
       sces.ANAMNESIS,
       sces.OBJECTIV_DATA,
       sces.RESULT,
       sces.SENDER_PERSON,
       sces.HJ_ID,
       sces.HPK_ID,
       sces.HH_ID,
       d.DIR_PREF,
       d.DIR_NUMB,
       hh.HH_NUMB_FULL,
       coalesce(substr(sces.LPU_CODE, 0, instr(sces.LPU_CODE, '/') -1), sces.LPU_CODE) MAIN_LPU_CODE,
       sces.SMP,
       sces.SMP_TEAM,
       sces.TIME_TAKE,
       sces.TIME_TEAM,
       sces.TIME_DEPART,
       sces.TIME_ARRIV,
       sces.TIME_TRANSF,
       sces.TIME_DELIVERY,
       sces.TIME_COMPLIT,
       sces.TIME_RETURN,
       sces.DEATHDATE,
       sces.ISHOD,
       sces.SENIOR_TEAM,
       sces.SENIOR_TEAM_CODE,
       sces.SEND_RESULT,
       sces.SENIOR_EMP_CODE,
       sces.SMP_ID,
       sces.ADIS_ID,
       sces.AMB_ID,
       sces.ADIS_REASON,
       sces.ADIS_RESULT,
       sces.ADIS_DESC_RESULT,
       sces.SENIOR_DISP_CODE,
       sces.DIVISION_CODE,
       sces.BED_PROFILE,
       sces.EX_SYSTEM EX_SYSTEM_ID,
       es.S_CODE EX_SYSTEM,
       es.STOPPED EX_SYSTEM_STOPPED,
       sces.TERRITORIAL_SMP,
       sces.RECEPTION_SMP
  from D_SMP_CALL_EX_SYSTEM sces                                  -- Справочник МКБ-10
       join D_AGENTS a on a.ID = sces.AGENT                       -- Контрагенты
       left join D_HPK_PLAN_JOURNALS hpj on hpj.ID = sces.HPK_ID  -- Журнал госпитализации
       left join D_DIRECTIONS d on d.ID = hpj.DIRECTION           -- Направления
       left join D_EX_SYSTEMS es on es.ID = sces.EX_SYSTEM        -- Внешние системы
       left join D_HOSP_HISTORIES hh on hh.ID = sces.HH_ID        -- Истории болезни
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.VERSION = sces.VERSION
                  and ur.UNITCODE = 'SMP_CALL_EX_SYSTEM'
                  and rownum = 1)
```

---

### Вьюха №24: D_V_LPU_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_LPU_BASE
select --Представление для раздела: Основная таблица ЛПУ
       t.ID,
       t.FULLNAME,
       t.HEADDOCTOR_FULLNAME                                   HEADDOCTOR_FULLNAME_OLD,
       D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(t.ID,sysdate)       HEADDOCTOR_ID,
       coalesce(D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(t.ID,sysdate,'SURNAME FIRSTNAME LASTNAME'),
                t.HEADDOCTOR_FULLNAME)                              HEADDOCTOR_FULLNAME,
       D_PKG_HEADDOCTOR.GET_ACTUAL_ON_DATE(t.ID,sysdate,'FIO') HEADDOCTOR_FIO,
       t.FULLADDRESS,
       t.PHONES,
       t.REC_SER_PRIV,
       t.REC_SER,
       t.CODE_LPU,
       t.CODE_OGRN,
       t.CODE_OKPO,
       t.CODE_OKDP,
       t.CODE_OKONH,
       t.CODE_OKATO,
       t.CODE_OKOGU,
       t.CODE_OCOPPH,
       t.CODE_OKFS,
       t.BOOKKEEPER_FULLNAME,
       t.HEADECONOMIST_FULLNAME,
       t.USERFORMS,
       t.GENNUMB_GROUP,
       t.EXEC_AUTHORITY,
       t.REC_SER_PRIV_88,
       t.LPUDICT,
       t.IP_ADDR,
       t.BY_ES_ONLY,
       t.GEOGRAFY,
       t.WEBSITE,
       t.ADDRESS
  from D_LPU t
 where exists (select null
                 from D_V_URPRIVS ur
                where coalesce(ur.LPU, -1) = -1
                  and coalesce(ur.VERSION, -1) = -1
                  and ur.UNITCODE = 'LPU'
                  and rownum = 1)
```

---

### Вьюха №25: D_V_SMP_CALL_EX_SYSTEM_BASE

**Используется в формах:**
- /Forms/HospPlan/hospplan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SMP_CALL_EX_SYSTEM_BASE
select -- Представление для раздела : Интеграция с АСУ Скорая помощь
       sces.ID,
       sces.VERSION,
       sces.LPU_CODE,
       sces.AGENT AGENT_ID,
       sces.LPU_CODE_SMP,
       sces.CALL_ID,
       sces.CALL_NUMB,
       sces.CALL_DATE,
       sces.CALL_PLACE,
       sces.CALL_REASON,
       sces.CALL_STRUCTURE,
       sces.MAIN_MKB,
       sces.ADD_MKB,
       sces.COMPLICATIONS,
       sces.DELIVERY_TYPE,
       sces.HELP_ON_PLACE,
       sces.HELP_IN_CAR,
       sces.HOSP_STATUS,
       case sces.HOSP_STATUS
            when 0 then 'начало доставки'
            when 1 then 'отмена доставки'
            when 2 then 'факт доставки'
            when 3 then 'принят в приемном покое'
            when 4 then 'госпитализирован'
            when 5 then 'отказано в госпитализации'
            when 6 then 'выписан из стационара'
       end HOSP_STATUS_NAME,
       sces.REFUSE_REASON,
       sces.CALL_ADDRESS,
       sces.PHONE,
       sces.CALL_TYPE,
       sces.CALL_STATUS,
       case sces.CALL_STATUS
            when 1 then 'новый'
            when 2 then 'отмена'
            when 3 then 'отказ'
            when 4 then 'подтверждение'
            when 5 then 'факт оказания'
       end CALL_STATUS_NAME,
       sces.SEND_TIME,
       sces.EMPLOYER,
       sces.REFUSE_REASON_POL,
       sces.WHO_CALL,
       sces.CALL,
       sces.ACCIDENT_CAUSE,
       sces.PROFILE_TEAMS,
       sces.INTOXICATION,
       sces.COMPLAINTS,
       sces.ANAMNESIS,
       sces.OBJECTIV_DATA,
       sces.RESULT,
       sces.SENDER_PERSON,
       sces.HJ_ID,
       sces.HPK_ID,
       sces.HH_ID,
       coalesce(substr(sces.LPU_CODE, 0, instr(sces.LPU_CODE, '/') - 1), sces.LPU_CODE) MAIN_LPU_CODE,
       sces.SMP,
       sces.SMP_TEAM,
       sces.TIME_TAKE,
       sces.TIME_TEAM,
       sces.TIME_DEPART,
       sces.TIME_ARRIV,
       sces.TIME_TRANSF,
       sces.TIME_DELIVERY,
       sces.TIME_COMPLIT,
       sces.TIME_RETURN,
       sces.DEATHDATE,
       sces.ISHOD,
       sces.SENIOR_TEAM,
       sces.SENIOR_TEAM_CODE,
       sces.SEND_RESULT,
       sces.SENIOR_EMP_CODE,
       sces.SENIOR_DISP_CODE,
       sces.ADIS_REASON,
       sces.ADIS_RESULT,
       sces.ADIS_DESC_RESULT,
       sces.AMB_ID,
       sces.TERRITORIAL_SMP,
       sces.RECEPTION_SMP,
       sces.BED_PROFILE
  from D_SMP_CALL_EX_SYSTEM sces  -- Справочник МКБ-10
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.VERSION = sces.VERSION
                  and ur.UNITCODE = 'SMP_CALL_EX_SYSTEM'
                  and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 25
- Всего уникальных таблиц: 93

### Связь вьюх и таблиц

**D_V_HPK_PLAN_JOURNALS_BASE** использует таблицы:
- D_HPK_PLAN_JOURNALS

**D_V_HOSP_HISTORIES_BASE** использует таблицы:
- D_HOSP_HISTORIES

**D_V_HOSP_PLAN_KINDS** использует таблицы:
- D_HOSP_PLAN_KINDS
- D_HPK_JOURNAL_TYPES

**D_V_AGENT_FLU_BASE** использует таблицы:
- D_AGENT_FLU

**D_V_AGENT_FLU_PMC_LAST** использует таблицы:
- D_AGENT_FLU
- D_PMC_FLU

**D_V_HPK_PLAN_JOURNALS_GRID** использует таблицы:
- D_PKG_AGENT_NAMES
- D_EMPLOYERS
- D_AGENTS
- D_SERVICES
- D_PAYMENT_KIND
- D_HPK_SCHEDULE_REG
- D_SCH_RESOURCES
- D_CABLAB
- D_HOSP_HISTORY_DEPS
- D_DEP_BEDS
- D_HH_DEP_BEDS
- D_DEPS
- D_HOSP_RESULTS
- D_PMC_MARKERS
- D_MARKERS
- D_SCHEDULE_COLORS
- D_AGENT_CONTACTS
- D_CONTACT_TYPES
- D_AGENT_ADDRS
- D_AGENT_SOCIAL_STATES
- D_SOCIALSTATES
- D_AGENT_WORK_PLACES
- D_AGENT_CATEGORIES
- D_CATEGORIES
- D_AGENT_PERSDOCS
- D_OUTER_DIRECTIONS
- D_DIR_THERAPY_SCHEMES
- D_MED_THERAPY_SCHEMES_DESC
- D_MED_THERAPY_SCHEMES
- D_DIRECTIONS
- D_MKB10
- D_PERSMEDCARD
- D_HPK_PLAN_JOURNALS
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS
- D_HOSPITALIZATION_TYPES
- D_LPUDICT
- D_HOSP_HISTORIES
- D_HH_CANC_REASON
- D_CANC_REASON_GUID
- D_HPKPJ_VMP_STATES
- D_DIRECTION_KINDS
- D_DIR_CANC_REASONS

**D_V_WL_RECORDS74** использует таблицы:
- D_WL_RECORDS74
- D_AGENTS
- D_SERVICES
- D_DIRECTION_SERVICES
- D_PAYMENT_KIND
- D_EMPLOYERS
- D_PERSMEDCARD

**D_V_VMP_LINKS** использует таблицы:
- D_VMP_LINKS

**D_V_HPK_PLAN_JOURNALS** использует таблицы:
- D_HPK_PLAN_JOURNALS
- D_HOSP_PLAN_KINDS
- D_PERSMEDCARD
- D_AGENTS
- D_EMPLOYERS
- D_HPKPJ_VMP_STATES
- D_DIRECTIONS
- D_HPK_PLANS
- D_SERVICES
- D_PAYMENT_KIND
- D_HOSP_HISTORIES
- D_DEPS
- D_EMPLOYMENT_STATUS

**D_V_DIRECTIONS_BASE** использует таблицы:
- D_DIRECTIONS

**D_V_HPK_PLANS** использует таблицы:
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS

**D_V_HPK_PLANS_BASE** использует таблицы:
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS

**D_V_PERSMEDCARD** использует таблицы:
- D_PERSMEDCARD
- D_AGENTS
- D_BLOODGROUPE
- D_DIVISIONS
- D_RHESUS
- D_EMPLOYERS
- D_CABLAB

**D_V_LPU** использует таблицы:
- D_LPU
- D_LPUDICT
- D_GEOGRAFY

**D_V_CABLAB** использует таблицы:
- D_CABLAB
- D_DEPS
- D_DIVISIONS
- D_BUILDINGS
- D_BUILD_FLOORS
- D_CABLAB_TYPE

**D_V_DIRECTIONS** использует таблицы:
- D_VISITPLACES
- D_VISITS
- D_DIR_REG_TYPES
- D_SPECIALITIES
- D_HPK_PLAN_JOURNALS
- D_HOSP_HISTORIES
- D_MKB10
- D_MESES
- D_HOSP_HOURS
- D_DIRECTIONS
- D_PERSMEDCARD
- D_AGENTS
- D_OUTER_DIRECTIONS
- D_LPUDICT
- D_EMPLOYERS
- D_DEPS
- D_HOSPITALIZATIONKINDS
- D_INJURE_KINDS
- D_DIRECTION_KINDS
- D_BED_TYPES
- D_HOSP_REASONS
- D_DIR_CANC_REASONS
- D_DEPDICT
- D_HOSPITALIZATION_TYPES
- D_HOSP_DIRECT_TYPES
- D_TRANSPORTATION_KINDS
- D_MED_THERAPY_SCHEMES
- D_DIRECTION_FORMS
- D_TYPE_MED_HELP
- D_DIRECTION_CONDITIONS

**D_V_OUTER_DIRECTIONS** использует таблицы:
- D_DIRECTIONS
- D_OUTER_DIRECTIONS
- D_PERSMEDCARD
- D_AGENTS
- D_MKB10
- D_LPUDICT
- D_SPECIALITIES
- D_REASON_DIRECTION
- D_EX_SYSTEMS
- D_SERVICES
- D_JOBTITLES
- D_ER_PROFILES
- D_DIRECTION_FORMS
- D_TYPE_MED_HELP
- D_DIRECTION_CONDITIONS
- D_CABLAB

**D_V_HOSP_HISTORY_DEPS** использует таблицы:
- D_AGENT_NAMES
- D_HOSP_HISTORY_DEPS
- D_HOSP_HISTORIES
- D_DEPS
- D_DEPS_TYPES
- D_PAYMENT_KIND
- D_MKB10
- D_HOSP_RESULTS
- D_KSGCODES
- D_BED_TYPES
- D_EMPLOYERS
- D_AGENTS
- D_VMP
- D_HOSP_OUTCOMES
- D_MED_THERAPY_SCHEMES_DESC
- D_MED_THERAPY_SCHEMES
- D_ALV
- D_SCALE_REHAB

**D_V_HOSP_HISTORIES** использует таблицы:
- D_AGENT_RELATIVES
- D_AGENTS
- D_HOSP_HISTORIES
- D_PERSMEDCARD
- D_HOSP_REASONS
- D_HOSPITALIZATION_TYPES
- D_TRANSPORTATION_KINDS
- D_LPUDICT
- D_MKB10
- D_HOSP_RESULTS
- D_DEPS
- D_HPK_PLAN_JOURNALS
- D_HPKPJ_VMP_STATES
- D_HOSP_OUTCOMES
- D_HOSP_HOURS
- D_HH_TRANSFER_REASONS
- D_ABILITY_STATUS
- D_DIRECTORIES_DATA_VER
- D_HOSP_INCOMES
- D_DIRECTION_HOSP

**D_V_HPK_SCHEDULE_REG** использует таблицы:
- D_HPK_SCHEDULE_REG

**D_V_HOSP_HISTORY_DEPS_BASE** использует таблицы:
- D_HOSP_HISTORY_DEPS

**D_V_CONTRACTS_BASE** использует таблицы:
- D_CONTRACTS
- D_AGENTS

**D_V_SMP_CALL_EX_SYSTEM** использует таблицы:
- D_SMP_CALL_EX_SYSTEM
- D_AGENTS
- D_HPK_PLAN_JOURNALS
- D_DIRECTIONS
- D_EX_SYSTEMS
- D_HOSP_HISTORIES

**D_V_LPU_BASE** использует таблицы:
- D_LPU

**D_V_SMP_CALL_EX_SYSTEM_BASE** использует таблицы:
- D_SMP_CALL_EX_SYSTEM

### DDL определения таблиц

---

#### Таблица №1: D_HPK_PLAN_JOURNALS

```sql
CREATE TABLE D_HPK_PLAN_JOURNALS (
    id bigint,
    lpu bigint,
    cid bigint,
    hpk_plan bigint,
    patient bigint,
    directed_by bigint,
    directed_to bigint,
    registered_by bigint,
    register_date timestamp without time zone,
    has_privileges numeric(1) DEFAULT 0,
    operation bigint,
    direction bigint,
    payment_kind bigint,
    is_ready numeric(1) DEFAULT 0,
    hh_direction_date timestamp without time zone,
    is_oper numeric(1) DEFAULT 0,
    comments character varying(4000),
    diseasecase bigint,
    quota_q bigint,
    hpk bigint,
    record_status numeric(1) DEFAULT 0,
    record_numb numeric(6),
    record_pref character varying(6),
    rl_record bigint,
    sch_resource bigint,
    employment_status bigint,
    date_actual timestamp without time zone,
    date_end_serv timestamp without time zone,
    rc_record bigint,
    contract bigint,
    alcohol_date timestamp without time zone,
    alcohol_res numeric(1),
    drug_date timestamp without time zone,
    drug_res numeric(1),
    direction_status numeric(2) DEFAULT 0
);
```

---

#### Таблица №2: D_HOSP_HISTORIES

```sql
CREATE TABLE D_HOSP_HISTORIES (
    id bigint,
    lpu bigint,
    hpk_plan_journal bigint,
    patient bigint,
    hh_pref character varying(20),
    hh_numb character varying(20),
    hosp_reason bigint,
    reception_emp bigint,
    date_in timestamp without time zone,
    plan_date_out timestamp without time zone,
    date_out timestamp without time zone,
    hospitalization_type bigint,
    transportation_kind bigint,
    lpu_from bigint,
    mkb_send bigint,
    mkb_send_exact character varying(4000),
    mkb_clinic bigint,
    mkb_clinic_exact character varying(4000),
    mkb_clinic_date timestamp without time zone,
    mkb_final bigint,
    mkb_final_exact character varying(4000),
    mkb_fin_comp bigint,
    mkb_fin_comp_exact character varying(4000),
    mkb_fin_add bigint,
    mkb_fin_add_exact character varying(4000),
    hosp_times numeric(3) DEFAULT 0,
    hosp_result bigint,
    mkb_receive bigint,
    mkb_receive_exact character varying(4000),
    relative bigint,
    diseasecase bigint,
    discard_status numeric(1) DEFAULT 0,
    hh_numb_altern character varying(20),
    is_well_timed_hosp numeric(1) DEFAULT 1,
    is_enough_volume numeric(1) DEFAULT 1,
    is_correct_healing numeric(1) DEFAULT 1,
    is_same_diagn numeric(1) DEFAULT 1,
    arch_date timestamp without time zone,
    arch_dep bigint,
    arch_comment character varying(250),
    hh_numb_type character varying(4),
    hh_type numeric(1),
    hosp_hour numeric(3),
    hosp_outcome bigint,
    transfer_lpu bigint,
    transfer_reason bigint,
    hh_numb_full character varying(60),
    hh_numb_mask character varying(60),
    other_therapy character varying(2000),
    ability_status bigint,
    features character varying(2000),
    date_departure timestamp without time zone,
    relative_hh bigint,
    abandonment numeric(1) DEFAULT NULL::numeric,
    death_came bigint,
    arrive_order numeric(1),
    judge_decision numeric(1),
    hosp_income bigint,
    hosp_is_first numeric(1),
    seized_items character varying(2000),
    novor_num bigint,
    direction_hosp bigint,
    arch_numb character varying(50),
    attending_empl_id bigint,
    department_id bigint
);
```

---

#### Таблица №3: D_HOSP_PLAN_KINDS

```sql
CREATE TABLE D_HOSP_PLAN_KINDS (
    id bigint,
    hp_code character varying(20),
    hp_name character varying(160),
    max_prior numeric(5),
    lpu bigint,
    cid bigint,
    min_age numeric(3),
    max_age numeric(3),
    has_mkb_constraints numeric(1) DEFAULT 0,
    has_limits numeric(1) DEFAULT 1,
    has_payment_constraints numeric(1) DEFAULT 0,
    numb_group numeric(2) DEFAULT 0,
    journal_type numeric(1) DEFAULT 0,
    is_oper numeric(1) DEFAULT 0,
    hp_block numeric(1) DEFAULT 0,
    close_date timestamp without time zone,
    open_date timestamp without time zone DEFAULT to_timestamp_simple('01.01.1990'::text, 'dd.mm.yyyy'::text)
);
```

---

#### Таблица №4: D_HPK_JOURNAL_TYPES

```sql
CREATE TABLE D_HPK_JOURNAL_TYPES (
    jt_code numeric(1),
    jt_name character varying(60)
);
```

---

#### Таблица №5: D_AGENT_FLU

```sql
CREATE TABLE D_AGENT_FLU (
    id bigint,
    pid bigint,
    cid bigint,
    version bigint,
    flu_lpu bigint,
    flu_diagnosis character varying(350),
    flu_date timestamp without time zone,
    flu_conclusion numeric(1),
    flu_rejection bigint,
    flu_lpu_handle character varying(250),
    is_last numeric(1),
    rad_dose numeric(31,7),
    rad_measure bigint
);
```

---

#### Таблица №6: D_PMC_FLU

```sql
CREATE TABLE D_PMC_FLU (
    id bigint,
    pid bigint,
    lpu bigint,
    cid bigint,
    agent_flu bigint,
    flu_num character varying(10),
    flu_method bigint,
    result1 character varying(4000),
    employer1 bigint,
    result2 character varying(4000),
    employer2 bigint,
    next_date timestamp without time zone,
    control_result bigint,
    note character varying(4000),
    is_control numeric(1) DEFAULT 0,
    visit bigint,
    kod_vracha1 character varying(11),
    kod_vracha2 character varying(11),
    flu_purpose numeric(1) DEFAULT 1
);
```

---

#### Таблица №7: D_PKG_AGENT_NAMES

```sql
CREATE TABLE D_PKG_AGENT_NAMES (

);
```

---

#### Таблица №8: D_EMPLOYERS

```sql
CREATE TABLE D_EMPLOYERS (
    id bigint,
    jobtitle bigint,
    regdate timestamp without time zone,
    speciality bigint,
    kod_vracha character varying(11),
    registr_kod character varying(10),
    lpu bigint,
    speciality_ed bigint,
    skill_category bigint,
    is_dismissed numeric(1) DEFAULT 0,
    dismiss_date timestamp without time zone,
    department bigint,
    sysuser bigint,
    agent bigint,
    cid bigint,
    report_sign character varying(400),
    emp_numb character varying(50),
    quot_resource bigint,
    rate numeric(7,2),
    personal_card_guid character varying(36)
);
```

---

#### Таблица №9: D_AGENTS

```sql
CREATE TABLE D_AGENTS (
    id bigint,
    version bigint,
    cid bigint,
    agn_code character varying(40),
    agn_name character varying(250),
    agn_type numeric(1),
    agn_inn numeric(12),
    agn_kpp bigint,
    note character varying(250),
    firstname character varying(40),
    surname character varying(40),
    lastname character varying(40),
    birthdate timestamp without time zone,
    sex numeric(1),
    okved bigint,
    education bigint,
    is_employer numeric(1) DEFAULT 0,
    snils character varying(11),
    agn_ogrn character varying(13),
    agn_okpo character varying(10),
    deathdate timestamp without time zone,
    deathdoctype bigint,
    deathdocdate timestamp without time zone,
    deathdocnum character varying(20),
    agn_okfs bigint,
    enp character varying(16),
    birthplace character varying(400),
    nation bigint,
    is_home numeric(1) DEFAULT 0,
    gest_age_mother numeric(4,1),
    is_anonym numeric(1) DEFAULT 0,
    deathplace character varying(4000),
    full_classes numeric(2),
    accuracy_date_death numeric(1),
    accuracy_date_birth numeric(1),
    ind_enterp numeric(1),
    agn_ogrn_ind character varying(15),
    convict_amount numeric(3),
    allerg_date timestamp without time zone,
    according_relatives numeric(1) DEFAULT 0,
    birthplace_geo bigint,
    webiomed_guid character varying(36),
    webiomed_url character varying(2048),
    medicbk_guid character varying(36),
    medicbk_url character varying(2048),
    birthplace_gar_address_id bigint,
    max_info numeric(1) DEFAULT 0,
    epgu numeric(1) DEFAULT 0
);
```

---

#### Таблица №10: D_SERVICES

```sql
CREATE TABLE D_SERVICES (
    id bigint,
    se_code character varying(30),
    se_name character varying(1000),
    se_code_pgg character varying(100),
    se_type bigint DEFAULT 0,
    pggservice bigint,
    se_kind bigint,
    operkind bigint,
    version bigint,
    cid bigint,
    is_combined numeric(1) DEFAULT 0,
    taxgr bigint,
    se_profile bigint,
    vmp bigint,
    uet_doctor numeric(7,2),
    uet_nurse numeric(7,2),
    treatment_stage numeric(1),
    pat_restriction bigint,
    uet_doctor_det numeric(7,2),
    gen_ehr numeric(1),
    tmp_disp_service bigint,
    form30_service bigint,
    fed_service bigint,
    uet_dent_technician numeric(7,2),
    primary numeric(1),
    open_date timestamp without time zone DEFAULT to_timestamp_simple('01.01.1900'::text, 'dd.mm.yyyy'::text),
    close_date timestamp without time zone,
    se_comment character varying(500),
    previd bigint,
    uet_polisher numeric(9,2),
    uet_caster numeric(9,2),
    preg_serv numeric(1),
    se_tax_code numeric(1),
    epgu_service bigint
);
```

---

#### Таблица №11: D_PAYMENT_KIND

```sql
CREATE TABLE D_PAYMENT_KIND (
    id bigint,
    pk_code character varying(20),
    pk_name character varying(160),
    is_commerc numeric(1) DEFAULT 0,
    version bigint,
    contract_type bigint,
    fac_acc_debt numeric(1) DEFAULT 1,
    fnsi bigint,
    pk_fcode bigint,
    serv_pay numeric(1) DEFAULT 0
);
```

---

#### Таблица №12: D_HPK_SCHEDULE_REG

```sql
CREATE TABLE D_HPK_SCHEDULE_REG (
    id bigint,
    direction bigint,
    hpk_schedule bigint,
    date_rec timestamp without time zone,
    employer bigint,
    date_create timestamp without time zone
);
```

---

#### Таблица №13: D_SCH_RESOURCES

```sql
CREATE TABLE D_SCH_RESOURCES (
    id bigint,
    lpu bigint,
    cablab bigint,
    service bigint,
    employer bigint,
    allow_record numeric(1) DEFAULT 0,
    default_service bigint,
    r_info character varying(400),
    pat_restriction bigint,
    er_for_chronics numeric(1) DEFAULT 0,
    allow_anonyms numeric(1) DEFAULT 0,
    record_period numeric(2),
    period_type numeric(1) DEFAULT 0,
    wait_list numeric(1) DEFAULT 0,
    need_direction numeric(1) DEFAULT 0,
    er_for_chronics_use numeric(1) DEFAULT 0,
    need_questioning numeric(1) DEFAULT 0,
    need_direction_use numeric(1) DEFAULT 0,
    without_reg numeric(1) DEFAULT 0,
    med_exam numeric(1) DEFAULT 0,
    datamart_restriction bigint,
    specialities bigint
);
```

---

#### Таблица №14: D_CABLAB

```sql
CREATE TABLE D_CABLAB (
    id bigint,
    lpu bigint,
    department bigint,
    cl_code character varying(20),
    cl_name character varying(160),
    cid bigint,
    pid bigint,
    schedule_type numeric(1),
    division bigint,
    building bigint,
    floor bigint,
    is_comm numeric(1) DEFAULT 0,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    cablab_type bigint,
    cl_begin_date timestamp without time zone,
    cl_end_date timestamp without time zone
);
```

---

#### Таблица №15: D_HOSP_HISTORY_DEPS

```sql
CREATE TABLE D_HOSP_HISTORY_DEPS (
    id bigint,
    pid bigint,
    date_in timestamp without time zone,
    date_out timestamp without time zone,
    dep bigint,
    lpu bigint,
    mkb bigint,
    mkb_exact character varying(4000),
    healing_emp bigint,
    payment_kind bigint,
    hosp_result bigint,
    ksg bigint,
    bed_type bigint,
    facial_account bigint,
    hhd_pref character varying(20),
    hhd_numb character varying(20),
    vmp bigint,
    prvsid bigint,
    hhd_level numeric(3) DEFAULT 0,
    is_last numeric(1),
    hosp_outcome bigint,
    mts_desc bigint,
    alv bigint,
    scale_rehab bigint,
    ksgid bigint
);
```

---

#### Таблица №16: D_DEP_BEDS

```sql
CREATE TABLE D_DEP_BEDS (
    id bigint,
    pid bigint,
    lpu bigint,
    dep bigint,
    db_code character varying(50),
    place_numb numeric(3) DEFAULT 1,
    is_normative numeric(1) DEFAULT NULL::numeric,
    shift_bed bigint,
    post bigint
);
```

---

#### Таблица №17: D_HH_DEP_BEDS

```sql
CREATE TABLE D_HH_DEP_BEDS (
    id bigint,
    pid bigint,
    date_in timestamp without time zone,
    date_out timestamp without time zone,
    dep_bed bigint,
    lpu bigint
);
```

---

#### Таблица №18: D_DEPS

```sql
CREATE TABLE D_DEPS (
    id bigint,
    lpu bigint,
    dp_code character varying(20),
    dp_name character varying(256),
    dp_kind bigint,
    dp_type bigint,
    division bigint,
    belong_to bigint,
    dp_ree_code character varying(20),
    dp_mgr bigint,
    profil bigint,
    is_comm numeric(1) DEFAULT 0,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    dp_begin_date timestamp without time zone DEFAULT NULL::timestamp without time zone,
    dp_end_date timestamp without time zone,
    hh_numb_group numeric(1),
    is_load numeric(1),
    is_suspended_hh numeric(1) DEFAULT 0,
    dp_oid_frmo character varying(50)
);
```

---

#### Таблица №19: D_HOSP_RESULTS

```sql
CREATE TABLE D_HOSP_RESULTS (
    id bigint,
    r_code character varying(5),
    r_name character varying(512),
    version bigint,
    date_b timestamp without time zone,
    date_e timestamp without time zone
);
```

---

#### Таблица №20: D_PMC_MARKERS

```sql
CREATE TABLE D_PMC_MARKERS (
    id bigint,
    lpu bigint,
    cid bigint,
    pid bigint,
    marker bigint,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    note character varying(2000),
    is_before_date_out numeric(1) DEFAULT 0
);
```

---

#### Таблица №21: D_MARKERS

```sql
CREATE TABLE D_MARKERS (
    id bigint,
    lpu bigint,
    m_code character varying(20),
    m_name character varying(300),
    description character varying(4000),
    color bigint
);
```

---

#### Таблица №22: D_SCHEDULE_COLORS

```sql
CREATE TABLE D_SCHEDULE_COLORS (
    id bigint,
    name character varying(30),
    grad_from character varying(7),
    grad_to character varying(7),
    code numeric(2)
);
```

---

#### Таблица №23: D_AGENT_CONTACTS

```sql
CREATE TABLE D_AGENT_CONTACTS (
    id bigint,
    pid bigint,
    version bigint,
    tmp_phone1 character varying(60),
    tmp_phone2 character varying(60),
    tmp_email character varying(120),
    tmp_fax character varying(60),
    tmp_telex character varying(60),
    tmp_begin_date timestamp without time zone,
    tmp_end_date timestamp without time zone,
    cid bigint,
    contact_type bigint,
    contact character varying(100),
    note character varying(200),
    is_main numeric DEFAULT 0,
    phone_reverse character varying(100),
    contact_lpudict bigint
);
```

---

#### Таблица №24: D_CONTACT_TYPES

```sql
CREATE TABLE D_CONTACT_TYPES (
    id bigint,
    version bigint,
    ct_code character varying(5),
    ct_name character varying(15),
    ct_full_name character varying(60),
    ct_mask bigint
);
```

---

#### Таблица №25: D_AGENT_ADDRS

```sql
CREATE TABLE D_AGENT_ADDRS (
    id bigint,
    pid bigint,
    version bigint,
    city bigint,
    street bigint,
    house character varying(11),
    houselit character varying(1),
    block numeric(5),
    flatlit character varying(1),
    addr_index character varying(10),
    is_main numeric(1) DEFAULT 0,
    is_real numeric(1) DEFAULT 0,
    is_tempreg numeric(1) DEFAULT 0,
    is_jur numeric(1) DEFAULT 0,
    is_post numeric(1) DEFAULT 0,
    is_birth numeric(1) DEFAULT 0,
    raion bigint,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    manual_input character varying(4000),
    cid bigint,
    is_citizen numeric(1),
    is_reg numeric(1) DEFAULT 0,
    flat character varying(11),
    addrobj character varying(36),
    fias_house character varying(36),
    building character varying(5),
    houselit1 character varying(1),
    blocklit character varying(1),
    citizen_type bigint,
    apartmentguid character varying(36),
    address bigint
);
```

---

#### Таблица №26: D_AGENT_SOCIAL_STATES

```sql
CREATE TABLE D_AGENT_SOCIAL_STATES (
    id bigint,
    pid bigint,
    cid bigint,
    social_state bigint,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    social_category numeric(1),
    version bigint,
    is_main numeric(1) DEFAULT 0
);
```

---

#### Таблица №27: D_SOCIALSTATES

```sql
CREATE TABLE D_SOCIALSTATES (
    id bigint,
    soc_code character varying(20),
    soc_name character varying(200),
    version bigint,
    social_category numeric(1) DEFAULT 0,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    soc_fcode bigint
);
```

---

#### Таблица №28: D_AGENT_WORK_PLACES

```sql
CREATE TABLE D_AGENT_WORK_PLACES (
    id bigint,
    pid bigint,
    work_place bigint,
    work_okved bigint,
    work_raion bigint,
    work_place_hand character varying(250),
    work_place_dep bigint,
    jobtitle bigint,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    cid bigint,
    version bigint,
    is_main numeric(1) DEFAULT 1,
    is_work numeric(1) DEFAULT 0,
    edu_level bigint,
    edu_org_type bigint,
    work_description character varying(256)
);
```

---

#### Таблица №29: D_AGENT_CATEGORIES

```sql
CREATE TABLE D_AGENT_CATEGORIES (
    id bigint,
    pid bigint,
    category bigint,
    ac_date timestamp without time zone,
    date_b timestamp without time zone,
    date_e timestamp without time zone,
    doc_ser character varying(10),
    doc_numb character varying(20),
    cid bigint,
    version bigint,
    org_name character varying(250)
);
```

---

#### Таблица №30: D_CATEGORIES

```sql
CREATE TABLE D_CATEGORIES (
    id bigint,
    cat_code character varying(20),
    cat_name character varying(400),
    version bigint
);
```

---

#### Таблица №31: D_AGENT_PERSDOCS

```sql
CREATE TABLE D_AGENT_PERSDOCS (
    id bigint,
    pid bigint,
    version bigint,
    pd_type bigint,
    pd_ser character varying(20),
    pd_numb character varying(20),
    pd_when timestamp without time zone,
    pd_who character varying(250),
    is_main numeric(1) DEFAULT 0,
    period_begin timestamp without time zone,
    period_end timestamp without time zone,
    cid bigint,
    citizenship bigint,
    pd_who_div character varying(20),
    no_resident_status bigint
);
```

---

#### Таблица №32: D_OUTER_DIRECTIONS

```sql
CREATE TABLE D_OUTER_DIRECTIONS (
    id bigint,
    lpu bigint,
    patient bigint,
    d_date timestamp without time zone,
    d_numb character varying(30),
    represent bigint,
    represent_handle character varying(250),
    diagnosis bigint,
    diagnosis_handle character varying(250),
    doctor_handle character varying(90),
    doctor bigint,
    d_pref character varying(20),
    represent_direction bigint,
    hosp_plan_date timestamp without time zone,
    doc_speciality bigint,
    reason bigint,
    d_date_end timestamp without time zone,
    service bigint,
    ex_system bigint,
    diagnosis_exact character varying(4000),
    department character varying(250),
    include_result numeric(1),
    jobtitle bigint,
    outdir_type numeric(2),
    diseasecharacter bigint,
    to_doctor bigint,
    speciality bigint,
    profile bigint,
    sch_resource bigint,
    direction_form bigint,
    type_med_help bigint,
    direction_condition bigint,
    direction_reason character varying(4000),
    employer bigint,
    dir_note character varying(500)
);
```

---

#### Таблица №33: D_DIR_THERAPY_SCHEMES

```sql
CREATE TABLE D_DIR_THERAPY_SCHEMES (
    id bigint,
    lpu bigint,
    pid bigint,
    med_therapy_schemes bigint
);
```

---

#### Таблица №34: D_MED_THERAPY_SCHEMES_DESC

```sql
CREATE TABLE D_MED_THERAPY_SCHEMES_DESC (
    id bigint,
    version bigint,
    pid bigint,
    days character varying(10),
    ksg bigint,
    rek_cnt1 numeric(4),
    rek_cnt2 numeric(4),
    neob numeric(1),
    hosp_type numeric(1),
    is_ksg numeric(1),
    mkb bigint,
    cost numeric(12,2),
    begin_date timestamp without time zone,
    end_date timestamp without time zone
);
```

---

#### Таблица №35: D_MED_THERAPY_SCHEMES

```sql
CREATE TABLE D_MED_THERAPY_SCHEMES (
    id bigint,
    version bigint,
    code character varying(60),
    name character varying(100),
    description character varying(4000)
);
```

---

#### Таблица №36: D_DIRECTIONS

```sql
CREATE TABLE D_DIRECTIONS (
    id bigint,
    lpu bigint,
    outer_direction bigint,
    lpu_to bigint,
    lpu_to_handle character varying(250),
    patient bigint,
    reg_visit bigint,
    reg_employer bigint,
    reg_date timestamp without time zone,
    dir_comment character varying(4000),
    reg_type numeric(1) DEFAULT 0,
    dir_type numeric(1) DEFAULT 0,
    hosp_mkb bigint,
    hosp_kind bigint,
    dir_numb character varying(30),
    speciality bigint,
    ex_cause_mkb bigint,
    injure_kind bigint,
    injure_time numeric(7,2),
    direction_kind bigint,
    hosp_dep bigint,
    dir_pref character varying(20),
    hosp_bed_type bigint,
    mes bigint,
    reg_hpkpj bigint,
    hosp_reason bigint,
    is_canceled numeric(1) DEFAULT 0,
    canc_reason bigint,
    canc_employer bigint,
    canc_employer_fio character varying(150),
    canc_date timestamp without time zone,
    reg_dir_serv bigint,
    hosp_plan_date timestamp without time zone,
    date_tr timestamp without time zone,
    hosp_depdict bigint,
    doc_comment character varying(4000),
    hosp_type bigint,
    talon_vmp_date timestamp without time zone,
    hosp_direct_type bigint,
    hosp_reason_streetkids bigint,
    transportation_kind bigint,
    hosp_hour numeric(3),
    reg_dep bigint,
    hosp_mkb_exact character varying(4000),
    talon_vmp_num character varying(20),
    is_onko numeric(1),
    realised_days numeric(3),
    dir_form numeric(1) DEFAULT NULL::numeric,
    therapy_schemes bigint,
    vmp bigint,
    direction_form bigint,
    type_med_help bigint,
    direction_condition bigint,
    direction_reason character varying(4000)
);
```

---

#### Таблица №37: D_MKB10

```sql
CREATE TABLE D_MKB10 (
    id bigint,
    mkb_code character varying(10),
    mkb_name character varying(500),
    pid bigint,
    allow_in numeric(1) DEFAULT 0,
    hlevel numeric(1),
    version bigint,
    sex numeric(1),
    sex_check numeric(1),
    age_from numeric(3),
    age_to numeric(3),
    age_check numeric(1),
    allow_in_main numeric(1),
    speciality_check numeric(1),
    actual_till timestamp without time zone
);
```

---

#### Таблица №38: D_PERSMEDCARD

```sql
CREATE TABLE D_PERSMEDCARD (
    id bigint,
    bloodgroupe bigint,
    rhesus numeric(1),
    ecolor character varying(40),
    createdate timestamp without time zone,
    moddate timestamp without time zone,
    card_numb character varying(26),
    lpu bigint,
    agent bigint,
    note character varying(4000),
    cid bigint,
    reg_division bigint,
    tp_printed numeric(1) DEFAULT 1,
    outnumb character varying(12),
    pmc_type numeric(1),
    ia_printed numeric(1) DEFAULT 0,
    sms_agree numeric(1) DEFAULT 0,
    emp_id bigint,
    email_agree numeric(1) DEFAULT 0,
    phenotype character varying(10),
    cablab bigint
);
```

---

#### Таблица №39: D_HPK_PLANS

```sql
CREATE TABLE D_HPK_PLANS (
    id bigint,
    lpu bigint,
    pid bigint,
    plan_date timestamp without time zone,
    male_count numeric(5),
    oper_count numeric(5),
    gen_count numeric(5),
    cid bigint
);
```

---

#### Таблица №40: D_HOSPITALIZATION_TYPES

```sql
CREATE TABLE D_HOSPITALIZATION_TYPES (
    id bigint,
    version bigint,
    hk_code character varying(2),
    hk_name character varying(150),
    is_active numeric(1) DEFAULT 1,
    urgent numeric(1) DEFAULT 0
);
```

---

#### Таблица №41: D_LPUDICT

```sql
CREATE TABLE D_LPUDICT (
    id bigint,
    lpu_code character varying(20),
    lpu_fullname character varying(300),
    headdoct character varying(160),
    is_resp numeric(1) DEFAULT 0,
    bookkeeper character varying(150),
    date_b timestamp without time zone,
    date_e timestamp without time zone,
    priv_date_b timestamp without time zone,
    priv_date_e timestamp without time zone,
    has_priv_rec numeric(1),
    version bigint,
    lpukind bigint,
    agent bigint,
    lpu_name character varying(100),
    hid bigint,
    cid bigint
);
```

---

#### Таблица №42: D_HH_CANC_REASON

```sql
CREATE TABLE D_HH_CANC_REASON (
    id bigint,
    lpu bigint,
    pid bigint,
    guid bigint,
    comm character varying(4000),
    employer bigint,
    canc_date timestamp without time zone
);
```

---

#### Таблица №43: D_CANC_REASON_GUID

```sql
CREATE TABLE D_CANC_REASON_GUID (
    id bigint,
    code numeric(3),
    name character varying(1000)
);
```

---

#### Таблица №44: D_HPKPJ_VMP_STATES

```sql
CREATE TABLE D_HPKPJ_VMP_STATES (
    vs_code numeric(1),
    vs_name character varying(30)
);
```

---

#### Таблица №45: D_DIRECTION_KINDS

```sql
CREATE TABLE D_DIRECTION_KINDS (
    id bigint,
    version bigint,
    dk_code character varying(10),
    dk_name character varying(400),
    short_name character varying(6),
    is_active numeric(1) DEFAULT 1
);
```

---

#### Таблица №46: D_DIR_CANC_REASONS

```sql
CREATE TABLE D_DIR_CANC_REASONS (
    dcr_code numeric(2),
    dcr_name character varying(120),
    id bigint,
    version bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone
);
```

---

#### Таблица №47: D_WL_RECORDS74

```sql
CREATE TABLE D_WL_RECORDS74 (
    id bigint,
    lpu bigint,
    pid bigint,
    hid bigint,
    agent bigint,
    pref character varying(1),
    numb numeric(5),
    ticket_type numeric(1),
    is_ill numeric(1),
    service bigint,
    dir_serv bigint,
    payment_kind bigint,
    reg_date timestamp without time zone DEFAULT sysdate(),
    call_date timestamp without time zone,
    status numeric(1) DEFAULT 0,
    call_numb numeric(2) DEFAULT 0,
    checkup_date timestamp without time zone,
    visit_date timestamp without time zone,
    employer bigint,
    cablab bigint,
    profcard bigint
);
```

---

#### Таблица №48: D_DIRECTION_SERVICES

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
```

---

#### Таблица №49: D_VMP_LINKS

```sql
CREATE TABLE D_VMP_LINKS (
    id bigint,
    version bigint,
    vmp_appl bigint,
    vmp_talon bigint,
    direction bigint
);
```

---

#### Таблица №50: D_EMPLOYMENT_STATUS

```sql
CREATE TABLE D_EMPLOYMENT_STATUS (
    id bigint,
    lpu bigint,
    cid bigint,
    pid bigint,
    es_code numeric(5),
    es_name character varying(60)
);
```

---

#### Таблица №51: D_BLOODGROUPE

```sql
CREATE TABLE D_BLOODGROUPE (
    id bigint,
    bg_code character varying(20),
    bg_name character varying(160),
    version bigint
);
```

---

#### Таблица №52: D_DIVISIONS

```sql
CREATE TABLE D_DIVISIONS (
    id bigint,
    lpu bigint,
    div_code character varying(40),
    div_name character varying(256),
    lpu_branch bigint,
    rec_ser_priv character varying(10),
    hid bigint,
    rec_ser_priv_88 character varying(10),
    pat_restriction bigint,
    version bigint,
    div_oid_frmo character varying(50)
);
```

---

#### Таблица №53: D_RHESUS

```sql
CREATE TABLE D_RHESUS (
    rh_code numeric(1),
    rh_name character varying(10)
);
```

---

#### Таблица №54: D_LPU

```sql
CREATE TABLE D_LPU (
    id bigint,
    fullname character varying(300),
    headdoctor_fullname character varying(160),
    fulladdress character varying(160),
    phones character varying(80),
    rec_ser_priv character varying(10),
    rec_ser character varying(10),
    code_lpu character varying(20),
    code_ogrn character varying(15),
    code_okpo character varying(10),
    code_okdp character varying(8),
    code_okonh character varying(5),
    code_okato character varying(11),
    code_okogu character varying(10),
    code_ocopph character varying(5),
    code_okfs character varying(2),
    lpudict bigint,
    bookkeeper_fullname character varying(160),
    headeconomist_fullname character varying(160),
    geografy bigint,
    userforms character varying(64),
    gennumb_group bigint,
    exec_authority character varying(150),
    rec_ser_priv_88 character varying(10),
    ip_addr character varying(250),
    by_es_only numeric(1),
    website character varying(250),
    is_tech_lpu numeric(1) DEFAULT 0,
    address bigint
);
```

---

#### Таблица №55: D_GEOGRAFY

```sql
CREATE TABLE D_GEOGRAFY (
    id bigint,
    pid bigint,
    geoname character varying(60),
    geoloctype bigint,
    kladr_code character varying(20),
    kladr_index character varying(6),
    kladr_gninmb character varying(4),
    kladr_ocatd character varying(11),
    kladr_status numeric(1),
    version bigint,
    geofull character varying(4000),
    fias_code character varying(36),
    address bigint
);
```

---

#### Таблица №56: D_BUILDINGS

```sql
CREATE TABLE D_BUILDINGS (
    id bigint,
    lpu bigint,
    code character varying(250),
    name character varying(4000),
    addrs_city bigint,
    addrs_street bigint,
    addrs_house character varying(11),
    addrs_houselit character varying(1),
    addrs_block numeric(5),
    addrs_manual character varying(250),
    address bigint
);
```

---

#### Таблица №57: D_BUILD_FLOORS

```sql
CREATE TABLE D_BUILD_FLOORS (
    id bigint,
    lpu bigint,
    pid bigint,
    name character varying(160)
);
```

---

#### Таблица №58: D_CABLAB_TYPE

```sql
CREATE TABLE D_CABLAB_TYPE (
    id bigint,
    version bigint,
    cablab_type character varying(160),
    cablab_code numeric(2)
);
```

---

#### Таблица №59: D_VISITPLACES

```sql
CREATE TABLE D_VISITPLACES (
    id bigint,
    vp_content character varying(200),
    vp_code character varying(6),
    version bigint,
    vp_fcode numeric(1),
    open_date timestamp without time zone DEFAULT sysdate(),
    close_date timestamp without time zone
);
```

---

#### Таблица №60: D_VISITS

```sql
CREATE TABLE D_VISITS (
    id bigint,
    pid bigint,
    employer bigint,
    visit_date timestamp without time zone,
    visit_place bigint,
    visit_result bigint,
    lpu bigint,
    nurse bigint,
    helper bigint,
    anesthetization numeric(2),
    dgroup bigint,
    prof_disease bigint,
    ser_count numeric(21,2) DEFAULT 1,
    ser_koeff numeric(8,3) DEFAULT 1,
    uet_count numeric(7,2) DEFAULT 0,
    minutes numeric(5),
    cablab bigint,
    visit_template bigint,
    ref_result bigint,
    med_care_kind bigint,
    app_kind bigint,
    anest_kind bigint,
    anest_indications bigint,
    is_emergency numeric(1),
    patient bigint
);
```

---

#### Таблица №61: D_DIR_REG_TYPES

```sql
CREATE TABLE D_DIR_REG_TYPES (
    drt_code numeric(1),
    drt_name character varying(30)
);
```

---

#### Таблица №62: D_SPECIALITIES

```sql
CREATE TABLE D_SPECIALITIES (
    id bigint,
    code character varying(10),
    title character varying(250),
    version bigint,
    cid bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    record_period numeric(2)
);
```

---

#### Таблица №63: D_MESES

```sql
CREATE TABLE D_MESES (
    id bigint,
    cid bigint,
    m_code character varying(50),
    m_name character varying(500),
    m_type numeric(1),
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    pay_constraint numeric(1) DEFAULT 0,
    patient_sex_constraint numeric(1) DEFAULT 2,
    plan_days numeric(5) DEFAULT 0,
    full_price numeric(19,2),
    comments character varying(4000),
    days_on_bed numeric(5),
    is_day_hospital numeric(1),
    serv_for_prescribes bigint,
    serv_for_dietaries bigint,
    is_vmp numeric(1) DEFAULT 0,
    version bigint
);
```

---

#### Таблица №64: D_HOSP_HOURS

```sql
CREATE TABLE D_HOSP_HOURS (
    hour_code numeric(3),
    hour_name character varying(100)
);
```

---

#### Таблица №65: D_HOSPITALIZATIONKINDS

```sql
CREATE TABLE D_HOSPITALIZATIONKINDS (
    id bigint,
    hk_code character varying(20),
    hk_name character varying(160),
    version bigint
);
```

---

#### Таблица №66: D_INJURE_KINDS

```sql
CREATE TABLE D_INJURE_KINDS (
    id bigint,
    ik_code character varying(2),
    ik_name character varying(60),
    ik_type numeric(1) DEFAULT 0,
    version bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    ik_fcode bigint
);
```

---

#### Таблица №67: D_BED_TYPES

```sql
CREATE TABLE D_BED_TYPES (
    id bigint,
    version bigint,
    bt_code character varying(30),
    bt_name character varying(400),
    is_active numeric(1) DEFAULT 1,
    short_name character varying(100)
);
```

---

#### Таблица №68: D_HOSP_REASONS

```sql
CREATE TABLE D_HOSP_REASONS (
    id bigint,
    version bigint,
    hr_code character varying(10),
    hr_name character varying(100),
    begin_date timestamp without time zone,
    end_date timestamp without time zone
);
```

---

#### Таблица №69: D_DEPDICT

```sql
CREATE TABLE D_DEPDICT (
    id bigint,
    version bigint,
    dep_code character varying(100),
    dep_name character varying(300),
    dp_kind bigint,
    lpudict bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone
);
```

---

#### Таблица №70: D_HOSP_DIRECT_TYPES

```sql
CREATE TABLE D_HOSP_DIRECT_TYPES (
    id bigint,
    version bigint,
    dt_code bigint,
    dt_name character varying(256)
);
```

---

#### Таблица №71: D_TRANSPORTATION_KINDS

```sql
CREATE TABLE D_TRANSPORTATION_KINDS (
    id bigint,
    version bigint,
    tk_code character varying(2),
    tk_name character varying(150)
);
```

---

#### Таблица №72: D_DIRECTION_FORMS

```sql
CREATE TABLE D_DIRECTION_FORMS (
    id bigint,
    version bigint,
    ds_code character varying(20),
    ds_name character varying(160)
);
```

---

#### Таблица №73: D_TYPE_MED_HELP

```sql
CREATE TABLE D_TYPE_MED_HELP (
    id bigint,
    version bigint,
    code numeric(3),
    hid bigint,
    name character varying(500),
    sort_value bigint
);
```

---

#### Таблица №74: D_DIRECTION_CONDITIONS

```sql
CREATE TABLE D_DIRECTION_CONDITIONS (
    id bigint,
    version bigint,
    dc_code character varying(20),
    dc_name character varying(160)
);
```

---

#### Таблица №75: D_REASON_DIRECTION

```sql
CREATE TABLE D_REASON_DIRECTION (
    id bigint,
    name_reason character varying(500),
    version bigint,
    code_reason character varying(5)
);
```

---

#### Таблица №76: D_EX_SYSTEMS

```sql
CREATE TABLE D_EX_SYSTEMS (
    id bigint,
    s_code character varying(50),
    s_name character varying(300),
    url_service character varying(300),
    stopped numeric(1)
);
```

---

#### Таблица №77: D_JOBTITLES

```sql
CREATE TABLE D_JOBTITLES (
    id bigint,
    title character varying(250),
    code character varying(20),
    cid bigint,
    version bigint,
    short_title character varying(100),
    show_for_bull numeric(1) DEFAULT 0,
    staff_level numeric(1) DEFAULT 0,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    show_for_cf numeric(1) DEFAULT 0
);
```

---

#### Таблица №78: D_ER_PROFILES

```sql
CREATE TABLE D_ER_PROFILES (
    id bigint,
    version bigint,
    p_name character varying(80),
    p_info character varying(200),
    allow_record numeric(1) DEFAULT 1
);
```

---

#### Таблица №79: D_AGENT_NAMES

```sql
CREATE TABLE D_AGENT_NAMES (
    id bigint,
    pid bigint,
    version bigint,
    firstname character varying(40),
    surname character varying(400),
    lastname character varying(40),
    is_main numeric(1) DEFAULT 0,
    begin_date timestamp without time zone,
    end_date timestamp without time zone,
    firstname_fr character varying(40),
    surname_fr character varying(400),
    lastname_fr character varying(40),
    firstname_to character varying(40),
    surname_to character varying(400),
    lastname_to character varying(40),
    firstname_ac character varying(40),
    surname_ac character varying(400),
    lastname_ac character varying(40),
    firstname_abl character varying(40),
    surname_abl character varying(400),
    lastname_abl character varying(40),
    cid bigint
);
```

---

#### Таблица №80: D_DEPS_TYPES

```sql
CREATE TABLE D_DEPS_TYPES (
    id bigint,
    dt_code character varying(20),
    dt_name character varying(160),
    version bigint
);
```

---

#### Таблица №81: D_KSGCODES

```sql
CREATE TABLE D_KSGCODES (
    id bigint,
    version bigint,
    ksg_code character varying(50),
    ksg_name character varying(400),
    k_type numeric(1) DEFAULT NULL::numeric,
    ksg_ree_code character varying(20),
    hosp_type numeric(1)
);
```

---

#### Таблица №82: D_VMP

```sql
CREATE TABLE D_VMP (
    id bigint,
    version bigint,
    vmp_code character varying(20),
    vmp_name character varying(1000),
    date_b timestamp without time zone,
    date_e timestamp without time zone,
    hid bigint,
    is_available numeric(1) DEFAULT 1,
    tmp_hmodp numeric(6),
    hmodp bigint,
    profile bigint,
    hgr numeric(3),
    part numeric(1)
);
```

---

#### Таблица №83: D_HOSP_OUTCOMES

```sql
CREATE TABLE D_HOSP_OUTCOMES (
    id bigint,
    version bigint,
    r_code character varying(5),
    r_name character varying(600),
    date_b timestamp without time zone,
    date_e timestamp without time zone
);
```

---

#### Таблица №84: D_ALV

```sql
CREATE TABLE D_ALV (
    id bigint,
    version bigint,
    alv_code character varying(20),
    alv_name character varying(1000),
    begin_date timestamp without time zone,
    end_date timestamp without time zone
);
```

---

#### Таблица №85: D_SCALE_REHAB

```sql
CREATE TABLE D_SCALE_REHAB (
    id bigint,
    version bigint,
    sr_code character varying(20),
    sr_name character varying(1000),
    begin_date timestamp without time zone,
    end_date timestamp without time zone
);
```

---

#### Таблица №86: D_AGENT_RELATIVES

```sql
CREATE TABLE D_AGENT_RELATIVES (
    id bigint,
    pid bigint,
    version bigint,
    relationship bigint,
    agent bigint,
    firstname character varying(30),
    surname character varying(30),
    lastname character varying(30),
    birthdate timestamp without time zone,
    ar_code character varying(20),
    cid bigint,
    represent numeric(1),
    legal_status bigint,
    represent_er numeric(1) DEFAULT 0,
    trusted numeric(1) DEFAULT 0
);
```

---

#### Таблица №87: D_HH_TRANSFER_REASONS

```sql
CREATE TABLE D_HH_TRANSFER_REASONS (
    id bigint,
    version bigint,
    tr_name character varying(150)
);
```

---

#### Таблица №88: D_ABILITY_STATUS

```sql
CREATE TABLE D_ABILITY_STATUS (
    id bigint,
    as_name character varying(150)
);
```

---

#### Таблица №89: D_DIRECTORIES_DATA_VER

```sql
CREATE TABLE D_DIRECTORIES_DATA_VER (
    id bigint,
    version bigint,
    dir numeric(3),
    dd_code character varying(20),
    dd_name character varying(350)
);
```

---

#### Таблица №90: D_HOSP_INCOMES

```sql
CREATE TABLE D_HOSP_INCOMES (
    id bigint,
    version bigint,
    code character varying(100),
    income_name character varying(100)
);
```

---

#### Таблица №91: D_DIRECTION_HOSP

```sql
CREATE TABLE D_DIRECTION_HOSP (
    id bigint,
    version bigint,
    dh_code character varying(10),
    dh_name character varying(255),
    date_begin timestamp without time zone,
    date_end timestamp without time zone
);
```

---

#### Таблица №92: D_CONTRACTS

```sql
CREATE TABLE D_CONTRACTS (
    id bigint,
    lpu bigint,
    cid bigint,
    contract_type bigint,
    doc_pref character varying(20),
    doc_date timestamp without time zone,
    ext_numb character varying(40),
    ext_date timestamp without time zone,
    agent bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    phone character varying(20),
    person character varying(250),
    summ numeric(19,2),
    contract bigint,
    employer bigint,
    is_open numeric(1) DEFAULT 0,
    facial_account bigint,
    debt_summ numeric(22,5) DEFAULT 0,
    contract_summ numeric(22,5) DEFAULT 0,
    plan_summ numeric(22,5),
    note character varying(2000),
    is_imported numeric(1) DEFAULT 0,
    workdate timestamp without time zone,
    status numeric(1) DEFAULT 0,
    represent bigint,
    center numeric(1) DEFAULT 1,
    doc_numb character varying(20)
);
```

---

#### Таблица №93: D_SMP_CALL_EX_SYSTEM

```sql
CREATE TABLE D_SMP_CALL_EX_SYSTEM (
    id bigint,
    version bigint,
    lpu_code character varying(20),
    agent bigint,
    lpu_code_smp character varying(20),
    call_id character varying(100),
    call_numb character varying(30),
    call_date timestamp without time zone,
    call_place character varying(100),
    call_reason character varying(100),
    call_structure numeric(3),
    main_mkb character varying(10),
    add_mkb character varying(500),
    complications character varying(400),
    delivery_type character varying(100),
    help_on_place character varying(4000),
    help_in_car character varying(4000),
    hosp_status numeric(1),
    refuse_reason numeric(2),
    call_address character varying(500),
    phone character varying(200),
    call_type numeric(1),
    call_status numeric(1),
    send_time timestamp without time zone,
    employer bigint,
    refuse_reason_pol character varying(400),
    who_call character varying(100),
    call character varying(100),
    accident_cause character varying(400),
    profile_teams character varying(400),
    intoxication numeric(1),
    complaints character varying(4000),
    anamnesis character varying(2000),
    objectiv_data character varying(400),
    result character varying(400),
    sender_person character varying(100),
    hj_id bigint,
    hpk_id bigint,
    hh_id bigint,
    smp character varying(20),
    smp_team character varying(20),
    time_take timestamp without time zone,
    time_team timestamp without time zone,
    time_depart timestamp without time zone,
    time_arriv timestamp without time zone,
    time_transf timestamp without time zone,
    time_delivery timestamp without time zone,
    time_complit timestamp without time zone,
    time_return timestamp without time zone,
    deathdate timestamp without time zone,
    ishod character varying(20),
    senior_team character varying(122),
    senior_team_code character varying(5),
    smp_id bigint,
    send_result character varying(500),
    senior_emp_code character varying(5),
    dop_info character varying(100),
    first_assistant_code character varying(5),
    second_assistant_code character varying(5),
    driver_code character varying(5),
    senior_disp_code character varying(5),
    reception_disp_code character varying(5),
    dest_disp_code character varying(5),
    close_disp_code character varying(5),
    recieved_by numeric(1),
    area_code character varying(5),
    adis_id character varying(100),
    amb_id bigint,
    adis_reason character varying(100),
    adis_result character varying(100),
    adis_desc_result character varying(500),
    division_code character varying(20),
    ex_system bigint,
    bed_profile character varying(100),
    territorial_smp character varying(10),
    reception_smp character varying(10)
);
```


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 25
- Всего уникальных таблиц: 90

### Связь вьюх и таблиц

**D_V_HPK_PLAN_JOURNALS_BASE** использует таблицы:
- D_HPK_PLAN_JOURNALS

**D_V_HOSP_HISTORIES_BASE** использует таблицы:
- D_HOSP_HISTORIES

**D_V_HOSP_PLAN_KINDS** использует таблицы:
- D_HOSP_PLAN_KINDS

**D_V_AGENT_FLU_BASE** использует таблицы:
- D_AGENT_FLU

**D_V_AGENT_FLU_PMC_LAST** использует таблицы:
- D_AGENT_FLU
- D_PMC_FLU

**D_V_HPK_PLAN_JOURNALS_GRID** использует таблицы:
- D_EMPLOYERS
- D_AGENTS
- D_SERVICES
- D_PAYMENT_KIND
- D_HPK_SCHEDULE_REG
- D_SCH_RESOURCES
- D_CABLAB
- D_HOSP_HISTORY_DEPS
- D_DEP_BEDS
- D_HH_DEP_BEDS
- D_DEPS
- D_HOSP_RESULTS
- D_PMC_MARKERS
- D_MARKERS
- D_SCHEDULE_COLORS
- D_AGENT_CONTACTS
- D_CONTACT_TYPES
- D_AGENT_ADDRS
- D_AGENT_SOCIAL_STATES
- D_SOCIALSTATES
- D_AGENT_WORK_PLACES
- D_AGENT_CATEGORIES
- D_CATEGORIES
- D_AGENT_PERSDOCS
- D_OUTER_DIRECTIONS
- D_DIR_THERAPY_SCHEMES
- D_MED_THERAPY_SCHEMES_DESC
- D_MED_THERAPY_SCHEMES
- D_DIRECTIONS
- D_MKB10
- D_PERSMEDCARD
- D_HPK_PLAN_JOURNALS
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS
- D_HOSP_HISTORIES
- D_HOSPITALIZATION_TYPES
- D_LPUDICT
- D_HH_CANC_REASON
- D_CANC_REASON
- D_HPKPJ_VMP_STATES
- D_DIRECTION_KINDS
- D_DIR_CANC_REASONS

**D_V_WL_RECORDS74** использует таблицы:
- D_WL_RECORDS74
- D_AGENTS
- D_SERVICES
- D_DIRECTION_SERVICES
- D_PAYMENT_KIND
- D_EMPLOYERS
- D_PERSMEDCARD

**D_V_VMP_LINKS** использует таблицы:
- D_VMP_LINKS

**D_V_HPK_PLAN_JOURNALS** использует таблицы:
- D_HPK_PLAN_JOURNALS
- D_HOSP_HISTORIES

**D_V_DIRECTIONS_BASE** использует таблицы:
- D_DIRECTIONS

**D_V_HPK_PLANS** использует таблицы:
- D_HPK_PLANS

**D_V_HPK_PLANS_BASE** использует таблицы:
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS

**D_V_PERSMEDCARD** использует таблицы:
- D_PERSMEDCARD
- D_AGENTS
- D_BLOODGROUPE
- D_DIVISIONS
- D_RHESUS
- D_EMPLOYERS
- D_CABLAB

**D_V_LPU** использует таблицы:
- D_LPU
- D_LPUDICT
- D_GEOGRAFY

**D_V_CABLAB** использует таблицы:
- D_CABLAB
- D_DEPS
- D_DIVISIONS
- D_BUILDINGS
- D_BUILD_FLOORS
- D_CABLAB_TYPE

**D_V_DIRECTIONS** использует таблицы:
- D_VISITPLACES
- D_VISITS
- D_DIR_REG_TYPES
- D_SPECIALITIES
- D_HPK_PLAN_JOURNALS
- D_MKB10
- D_MESES
- D_HOSP_HOURS
- D_DIRECTIONS
- D_PERSMEDCARD
- D_AGENTS
- D_OUTER_DIRECTIONS
- D_LPUDICT
- D_EMPLOYERS
- D_DEPS
- D_HOSPITALIZATIONKINDS
- D_INJURE_KINDS
- D_DIRECTION_KINDS
- D_BED_TYPES
- D_HOSP_REASONS
- D_DIR_CANC_REASONS
- D_DEPDICT
- D_HOSPITALIZATION_TYPES
- D_HOSP_DIRECT_TYPES
- D_TRANSPORTATION_KINDS
- D_MED_THERAPY_SCHEMES
- D_DIRECTION_FORMS
- D_TYPE_MED_HELP
- D_DIRECTION_CONDITIONS

**D_V_OUTER_DIRECTIONS** использует таблицы:
- D_DIRECTIONS
- D_OUTER_DIRECTIONS
- D_PERSMEDCARD
- D_AGENTS
- D_MKB10
- D_LPUDICT
- D_SPECIALITIES
- D_REASON_DIRECTION
- D_EX_SYSTEMS
- D_SERVICES
- D_JOBTITLES
- D_ER_PROFILES
- D_DIRECTION_FORMS
- D_TYPE_MED_HELP
- D_DIRECTION_CONDITIONS
- D_CABLAB

**D_V_HOSP_HISTORY_DEPS** использует таблицы:
- D_AGENT_NAMES
- D_HOSP_HISTORY_DEPS
- D_HOSP_HISTORIES
- D_DEPS
- D_DEPS_TYPES
- D_PAYMENT_KIND
- D_MKB10
- D_HOSP_RESULTS
- D_KSGCODES
- D_BED_TYPES
- D_EMPLOYERS
- D_AGENTS
- D_VMP
- D_HOSP_OUTCOMES
- D_MED_THERAPY_SCHEMES_DESC
- D_MED_THERAPY_SCHEMES
- D_ALV
- D_SCALE_REHAB

**D_V_HOSP_HISTORIES** использует таблицы:
- D_AGENT_RELATIVES
- D_AGENTS
- D_HOSP_HISTORIES
- D_PERSMEDCARD
- D_HOSP_REASONS
- D_HOSPITALIZATION_TYPES
- D_TRANSPORTATION_KINDS
- D_LPUDICT
- D_MKB10
- D_HOSP_RESULTS
- D_DEPS
- D_HPK_PLAN_JOURNALS
- D_HPKPJ_VMP_STATES
- D_HOSP_OUTCOMES
- D_HOSP_HOURS
- D_HH_TRANSFER_REASONS
- D_ABILITY_STATUS
- D_DIRECTORIES_DATA_VER
- D_HOSP_INCOMES
- D_DIRECTION_HOSP

**D_V_HPK_SCHEDULE_REG** использует таблицы:
- D_HPK_SCHEDULE_REG

**D_V_HOSP_HISTORY_DEPS_BASE** использует таблицы:
- D_HOSP_HISTORY_DEPS

**D_V_CONTRACTS_BASE** использует таблицы:
- D_CONTRACTS
- D_AGENTS

**D_V_SMP_CALL_EX_SYSTEM** использует таблицы:
- D_SMP_CALL_EX_SYSTEM
- D_AGENTS
- D_HPK_PLAN_JOURNALS
- D_DIRECTIONS
- D_EX_SYSTEMS
- D_HOSP_HISTORIES

**D_V_LPU_BASE** использует таблицы:
- D_LPU

**D_V_SMP_CALL_EX_SYSTEM_BASE** использует таблицы:
- D_SMP_CALL_EX_SYSTEM

### DDL определения таблиц

---

#### Таблица №1: D_HPK_PLAN_JOURNALS

```sql
CREATE TABLE D_HPK_PLAN_JOURNALS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    HPK_PLAN NUMBER(17),
    PATIENT NUMBER(17) NOT NULL,
    DIRECTED_BY NUMBER(17),
    DIRECTED_TO NUMBER(17),
    REGISTERED_BY NUMBER(17) NOT NULL,
    REGISTER_DATE DATE NOT NULL,
    HAS_PRIVILEGES NUMBER(1) NOT NULL,
    OPERATION NUMBER(17),
    DIRECTION NUMBER(17),
    PAYMENT_KIND NUMBER(17),
    IS_READY NUMBER(1) NOT NULL,
    HH_DIRECTION_DATE DATE,
    IS_OPER NUMBER(1) NOT NULL,
    COMMENTS VARCHAR2(4000),
    DISEASECASE NUMBER(17) NOT NULL,
    QUOTA_Q NUMBER(17),
    HPK NUMBER(17) NOT NULL,
    RECORD_STATUS NUMBER(1) NOT NULL,
    RECORD_NUMB NUMBER(6),
    RECORD_PREF VARCHAR2(6),
    RL_RECORD NUMBER(17),
    SCH_RESOURCE NUMBER(17),
    EMPLOYMENT_STATUS NUMBER(17),
    DATE_ACTUAL DATE,
    DATE_END_SERV DATE,
    RC_RECORD NUMBER(17),
    CONTRACT NUMBER(17),
    ALCOHOL_DATE DATE,
    ALCOHOL_RES NUMBER(1),
    DRUG_DATE DATE,
    DRUG_RES NUMBER(1),
    DIRECTION_STATUS NUMBER(2),
    CONSTRAINT PK_D_HPK_PLAN_JOURNALS PRIMARY KEY (ID)
);
```

---

#### Таблица №2: D_HOSP_HISTORIES

```sql
CREATE TABLE D_HOSP_HISTORIES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    HPK_PLAN_JOURNAL NUMBER(17),
    PATIENT NUMBER(17) NOT NULL,
    HH_PREF VARCHAR2(20) NOT NULL,
    HH_NUMB VARCHAR2(20) NOT NULL,
    HOSP_REASON NUMBER(17) NOT NULL,
    RECEPTION_EMP NUMBER(17),
    DATE_IN DATE NOT NULL,
    PLAN_DATE_OUT DATE,
    DATE_OUT DATE,
    HOSPITALIZATION_TYPE NUMBER(17) NOT NULL,
    TRANSPORTATION_KIND NUMBER(17) NOT NULL,
    LPU_FROM NUMBER(17),
    MKB_SEND NUMBER(17),
    MKB_SEND_EXACT VARCHAR2(4000),
    MKB_CLINIC NUMBER(17),
    MKB_CLINIC_EXACT VARCHAR2(4000),
    MKB_CLINIC_DATE DATE,
    MKB_FINAL NUMBER(17),
    MKB_FINAL_EXACT VARCHAR2(4000),
    MKB_FIN_COMP NUMBER(17),
    MKB_FIN_COMP_EXACT VARCHAR2(4000),
    MKB_FIN_ADD NUMBER(17),
    MKB_FIN_ADD_EXACT VARCHAR2(4000),
    HOSP_TIMES NUMBER(3) NOT NULL,
    HOSP_RESULT NUMBER(17),
    MKB_RECEIVE NUMBER(17),
    MKB_RECEIVE_EXACT VARCHAR2(4000),
    RELATIVE NUMBER(17),
    DISEASECASE NUMBER(17) NOT NULL,
    DISCARD_STATUS NUMBER(1) NOT NULL,
    HH_NUMB_ALTERN VARCHAR2(20),
    IS_WELL_TIMED_HOSP NUMBER(1) NOT NULL,
    IS_ENOUGH_VOLUME NUMBER(1) NOT NULL,
    IS_CORRECT_HEALING NUMBER(1) NOT NULL,
    IS_SAME_DIAGN NUMBER(1) NOT NULL,
    ARCH_DATE DATE,
    ARCH_DEP NUMBER(17),
    ARCH_COMMENT VARCHAR2(250),
    HH_NUMB_TYPE VARCHAR2(4),
    HH_TYPE NUMBER(1),
    HOSP_HOUR NUMBER(3),
    HOSP_OUTCOME NUMBER(17),
    TRANSFER_LPU NUMBER(17),
    TRANSFER_REASON NUMBER(17),
    HH_NUMB_FULL VARCHAR2(60) NOT NULL,
    HH_NUMB_MASK VARCHAR2(60) NOT NULL,
    OTHER_THERAPY VARCHAR2(2000),
    ABILITY_STATUS NUMBER(17),
    FEATURES VARCHAR2(2000),
    DATE_DEPARTURE DATE,
    RELATIVE_HH NUMBER(17),
    ABANDONMENT NUMBER(1),
    DEATH_CAME NUMBER(17),
    ARRIVE_ORDER NUMBER(1),
    JUDGE_DECISION NUMBER(1),
    HOSP_INCOME NUMBER(17),
    HOSP_IS_FIRST NUMBER(1),
    SEIZED_ITEMS VARCHAR2(2000),
    NOVOR_NUM NUMBER(17),
    DIRECTION_HOSP NUMBER(17),
    ARCH_NUMB VARCHAR2(50),
    ATTENDING_EMPL_ID NUMBER(17),
    DEPARTMENT_ID NUMBER(17),
    CONSTRAINT PK_D_HOSP_HISTORIES PRIMARY KEY (ID)
);
```

---

#### Таблица №3: D_HOSP_PLAN_KINDS

```sql
CREATE TABLE D_HOSP_PLAN_KINDS (
    ID NUMBER(17) NOT NULL,
    HP_CODE VARCHAR2(20) NOT NULL,
    HP_NAME VARCHAR2(160) NOT NULL,
    MAX_PRIOR NUMBER(5),
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    MIN_AGE NUMBER(3),
    MAX_AGE NUMBER(3),
    HAS_MKB_CONSTRAINTS NUMBER(1) NOT NULL,
    HAS_LIMITS NUMBER(1) NOT NULL,
    HAS_PAYMENT_CONSTRAINTS NUMBER(1) NOT NULL,
    NUMB_GROUP NUMBER(2) NOT NULL,
    JOURNAL_TYPE NUMBER(1) NOT NULL,
    IS_OPER NUMBER(1) NOT NULL,
    HP_BLOCK NUMBER(1) NOT NULL,
    CLOSE_DATE DATE,
    OPEN_DATE DATE NOT NULL,
    CONSTRAINT PK_D_HOSP_PLAN_KINDS PRIMARY KEY (ID)
);
```

---

#### Таблица №4: D_AGENT_FLU

```sql
CREATE TABLE D_AGENT_FLU (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    FLU_LPU NUMBER(17),
    FLU_DIAGNOSIS VARCHAR2(350),
    FLU_DATE DATE NOT NULL,
    FLU_CONCLUSION NUMBER(1) NOT NULL,
    FLU_REJECTION NUMBER(17),
    FLU_LPU_HANDLE VARCHAR2(250),
    IS_LAST NUMBER(1),
    RAD_DOSE NUMBER(24,7),
    RAD_MEASURE NUMBER(17),
    CONSTRAINT PK_D_AGENT_FLU PRIMARY KEY (ID)
);
```

---

#### Таблица №5: D_PMC_FLU

```sql
CREATE TABLE D_PMC_FLU (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    AGENT_FLU NUMBER(17) NOT NULL,
    FLU_NUM VARCHAR2(10),
    FLU_METHOD NUMBER(17),
    RESULT1 VARCHAR2(4000),
    EMPLOYER1 NUMBER(17),
    RESULT2 VARCHAR2(4000),
    EMPLOYER2 NUMBER(17),
    NEXT_DATE DATE,
    CONTROL_RESULT NUMBER(17),
    NOTE VARCHAR2(4000),
    IS_CONTROL NUMBER(1) NOT NULL,
    VISIT NUMBER(17),
    KOD_VRACHA1 VARCHAR2(11),
    KOD_VRACHA2 VARCHAR2(11),
    FLU_PURPOSE NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_PMC_FLU PRIMARY KEY (ID)
);
```

---

#### Таблица №6: D_EMPLOYERS

```sql
CREATE TABLE D_EMPLOYERS (
    ID NUMBER(17) NOT NULL,
    JOBTITLE NUMBER(17),
    REGDATE DATE NOT NULL,
    SPECIALITY NUMBER(17),
    KOD_VRACHA VARCHAR2(11),
    REGISTR_KOD VARCHAR2(10),
    LPU NUMBER(17) NOT NULL,
    SPECIALITY_ED NUMBER(17),
    SKILL_CATEGORY NUMBER(17),
    IS_DISMISSED NUMBER(1) NOT NULL,
    DISMISS_DATE DATE,
    DEPARTMENT NUMBER(17),
    SYSUSER NUMBER(17),
    AGENT NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    REPORT_SIGN VARCHAR2(400),
    EMP_NUMB VARCHAR2(50),
    QUOT_RESOURCE NUMBER(17),
    RATE NUMBER(5,2),
    PERSONAL_CARD_GUID VARCHAR2(36),
    CONSTRAINT PK_D_EMPLOYERS PRIMARY KEY (ID)
);
```

---

#### Таблица №7: D_AGENTS

```sql
CREATE TABLE D_AGENTS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    AGN_CODE VARCHAR2(40) NOT NULL,
    AGN_NAME VARCHAR2(250) NOT NULL,
    AGN_TYPE NUMBER(1) NOT NULL,
    AGN_INN NUMBER(12),
    AGN_KPP NUMBER(17),
    NOTE VARCHAR2(250),
    FIRSTNAME VARCHAR2(40),
    SURNAME VARCHAR2(40),
    LASTNAME VARCHAR2(40),
    BIRTHDATE DATE,
    SEX NUMBER(1),
    OKVED NUMBER(17),
    EDUCATION NUMBER(17),
    IS_EMPLOYER NUMBER(1) NOT NULL,
    SNILS VARCHAR2(11),
    AGN_OGRN VARCHAR2(13),
    AGN_OKPO VARCHAR2(10),
    DEATHDATE DATE,
    DEATHDOCTYPE NUMBER(17),
    DEATHDOCDATE DATE,
    DEATHDOCNUM VARCHAR2(20),
    AGN_OKFS NUMBER(17),
    ENP VARCHAR2(16),
    BIRTHPLACE VARCHAR2(400),
    NATION NUMBER(17),
    IS_HOME NUMBER(1) NOT NULL,
    GEST_AGE_MOTHER NUMBER(3,1),
    IS_ANONYM NUMBER(1) NOT NULL,
    DEATHPLACE VARCHAR2(4000),
    FULL_CLASSES NUMBER(2),
    ACCURACY_DATE_DEATH NUMBER(1),
    ACCURACY_DATE_BIRTH NUMBER(1),
    IND_ENTERP NUMBER(1),
    AGN_OGRN_IND VARCHAR2(15),
    CONVICT_AMOUNT NUMBER(3),
    ALLERG_DATE DATE,
    ACCORDING_RELATIVES NUMBER(1) NOT NULL,
    BIRTHPLACE_GEO NUMBER(17),
    WEBIOMED_GUID VARCHAR2(36),
    WEBIOMED_URL VARCHAR2(2048),
    MEDICBK_GUID VARCHAR2(36),
    MEDICBK_URL VARCHAR2(2048),
    BIRTHPLACE_GAR_ADDRESS_ID NUMBER(17),
    MAX_INFO NUMBER(1),
    EPGU NUMBER(1),
    CONSTRAINT PK_D_AGENTS PRIMARY KEY (ID)
);
```

---

#### Таблица №8: D_SERVICES

```sql
CREATE TABLE D_SERVICES (
    ID NUMBER(17) NOT NULL,
    SE_CODE VARCHAR2(30) NOT NULL,
    SE_NAME VARCHAR2(1000) NOT NULL,
    SE_CODE_PGG VARCHAR2(100),
    SE_TYPE NUMBER(17) NOT NULL,
    PGGSERVICE NUMBER(17),
    SE_KIND NUMBER(17),
    OPERKIND NUMBER(17),
    VERSION NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    IS_COMBINED NUMBER(1) NOT NULL,
    TAXGR NUMBER(17) NOT NULL,
    SE_PROFILE NUMBER(17),
    VMP NUMBER(17),
    UET_DOCTOR NUMBER(5,2),
    UET_NURSE NUMBER(5,2),
    TREATMENT_STAGE NUMBER(1),
    PAT_RESTRICTION NUMBER(17),
    UET_DOCTOR_DET NUMBER(5,2),
    GEN_EHR NUMBER(1),
    TMP_DISP_SERVICE NUMBER(17),
    FORM30_SERVICE NUMBER(17),
    FED_SERVICE NUMBER(17),
    UET_DENT_TECHNICIAN NUMBER(5,2),
    PRIMARY NUMBER(1),
    OPEN_DATE DATE NOT NULL,
    CLOSE_DATE DATE,
    SE_COMMENT VARCHAR2(500),
    PREVID NUMBER(17),
    UET_POLISHER NUMBER(7,2),
    UET_CASTER NUMBER(7,2),
    PREG_SERV NUMBER(1),
    SE_TAX_CODE NUMBER(1),
    EPGU_SERVICE NUMBER(17),
    CONSTRAINT PK_D_SERVICES PRIMARY KEY (ID)
);
```

---

#### Таблица №9: D_PAYMENT_KIND

```sql
CREATE TABLE D_PAYMENT_KIND (
    ID NUMBER(17) NOT NULL,
    PK_CODE VARCHAR2(20) NOT NULL,
    PK_NAME VARCHAR2(160) NOT NULL,
    IS_COMMERC NUMBER(1) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONTRACT_TYPE NUMBER(17),
    FAC_ACC_DEBT NUMBER(1) NOT NULL,
    FNSI NUMBER(17),
    PK_FCODE NUMBER(17),
    SERV_PAY NUMBER(1),
    CONSTRAINT PK_D_PAYMENT_KIND PRIMARY KEY (ID)
);
```

---

#### Таблица №10: D_HPK_SCHEDULE_REG

```sql
CREATE TABLE D_HPK_SCHEDULE_REG (
    ID NUMBER(17) NOT NULL,
    DIRECTION NUMBER(17) NOT NULL,
    HPK_SCHEDULE NUMBER(17) NOT NULL,
    DATE_REC DATE NOT NULL,
    EMPLOYER NUMBER(17) NOT NULL,
    DATE_CREATE DATE NOT NULL,
    CONSTRAINT PK_D_HPK_SCHEDULE_REG PRIMARY KEY (ID)
);
```

---

#### Таблица №11: D_SCH_RESOURCES

```sql
CREATE TABLE D_SCH_RESOURCES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CABLAB NUMBER(17) NOT NULL,
    SERVICE NUMBER(17),
    EMPLOYER NUMBER(17),
    ALLOW_RECORD NUMBER(1) NOT NULL,
    DEFAULT_SERVICE NUMBER(17),
    R_INFO VARCHAR2(400),
    PAT_RESTRICTION NUMBER(17),
    ER_FOR_CHRONICS NUMBER(1),
    ALLOW_ANONYMS NUMBER(1),
    RECORD_PERIOD NUMBER(2),
    PERIOD_TYPE NUMBER(1) NOT NULL,
    WAIT_LIST NUMBER(1) NOT NULL,
    NEED_DIRECTION NUMBER(1) NOT NULL,
    ER_FOR_CHRONICS_USE NUMBER(1) NOT NULL,
    NEED_QUESTIONING NUMBER(1) NOT NULL,
    NEED_DIRECTION_USE NUMBER(1) NOT NULL,
    WITHOUT_REG NUMBER(1),
    MED_EXAM NUMBER(1) NOT NULL,
    DATAMART_RESTRICTION NUMBER(17),
    SPECIALITIES NUMBER(17),
    CONSTRAINT PK_D_SCH_RESOURCES PRIMARY KEY (ID)
);
```

---

#### Таблица №12: D_CABLAB

```sql
CREATE TABLE D_CABLAB (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    DEPARTMENT NUMBER(17) NOT NULL,
    CL_CODE VARCHAR2(20) NOT NULL,
    CL_NAME VARCHAR2(160) NOT NULL,
    CID NUMBER(17) NOT NULL,
    PID NUMBER(17),
    SCHEDULE_TYPE NUMBER(1),
    DIVISION NUMBER(17) NOT NULL,
    BUILDING NUMBER(17),
    FLOOR NUMBER(17),
    IS_COMM NUMBER(1) NOT NULL,
    BEGIN_DATE DATE,
    END_DATE DATE,
    CABLAB_TYPE NUMBER(17),
    CL_BEGIN_DATE DATE NOT NULL,
    CL_END_DATE DATE,
    CONSTRAINT PK_D_CABLAB PRIMARY KEY (ID)
);
```

---

#### Таблица №13: D_HOSP_HISTORY_DEPS

```sql
CREATE TABLE D_HOSP_HISTORY_DEPS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    DATE_IN DATE NOT NULL,
    DATE_OUT DATE,
    DEP NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    MKB NUMBER(17),
    MKB_EXACT VARCHAR2(4000),
    HEALING_EMP NUMBER(17),
    PAYMENT_KIND NUMBER(17) NOT NULL,
    HOSP_RESULT NUMBER(17),
    KSG NUMBER(17),
    BED_TYPE NUMBER(17),
    FACIAL_ACCOUNT NUMBER(17),
    HHD_PREF VARCHAR2(20),
    HHD_NUMB VARCHAR2(20),
    VMP NUMBER(17),
    PRVSID NUMBER(17),
    HHD_LEVEL NUMBER(3) NOT NULL,
    IS_LAST NUMBER(1),
    HOSP_OUTCOME NUMBER(17),
    MTS_DESC NUMBER(17),
    ALV NUMBER(17),
    SCALE_REHAB NUMBER(17),
    KSGID NUMBER(17),
    CONSTRAINT PK_D_HOSP_HISTORY_DEPS PRIMARY KEY (ID)
);
```

---

#### Таблица №14: D_DEP_BEDS

```sql
CREATE TABLE D_DEP_BEDS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17),
    LPU NUMBER(17) NOT NULL,
    DEP NUMBER(17) NOT NULL,
    DB_CODE VARCHAR2(50) NOT NULL,
    PLACE_NUMB NUMBER(3) NOT NULL,
    IS_NORMATIVE NUMBER(1),
    SHIFT_BED NUMBER(17),
    POST NUMBER(17),
    CONSTRAINT PK_D_DEP_BEDS PRIMARY KEY (ID)
);
```

---

#### Таблица №15: D_HH_DEP_BEDS

```sql
CREATE TABLE D_HH_DEP_BEDS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    DATE_IN DATE NOT NULL,
    DATE_OUT DATE,
    DEP_BED NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_HH_DEP_BEDS PRIMARY KEY (ID)
);
```

---

#### Таблица №16: D_DEPS

```sql
CREATE TABLE D_DEPS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    DP_CODE VARCHAR2(20) NOT NULL,
    DP_NAME VARCHAR2(256) NOT NULL,
    DP_KIND NUMBER(17) NOT NULL,
    DP_TYPE NUMBER(17) NOT NULL,
    DIVISION NUMBER(17) NOT NULL,
    BELONG_TO NUMBER(17),
    DP_REE_CODE VARCHAR2(20),
    DP_MGR NUMBER(17),
    PROFIL NUMBER(17),
    IS_COMM NUMBER(1) NOT NULL,
    BEGIN_DATE DATE,
    END_DATE DATE,
    DP_BEGIN_DATE DATE NOT NULL,
    DP_END_DATE DATE,
    HH_NUMB_GROUP NUMBER(1),
    IS_LOAD NUMBER(1),
    IS_SUSPENDED_HH NUMBER(1) NOT NULL,
    DP_OID_FRMO VARCHAR2(50),
    CONSTRAINT PK_D_DEPS PRIMARY KEY (ID)
);
```

---

#### Таблица №17: D_HOSP_RESULTS

```sql
CREATE TABLE D_HOSP_RESULTS (
    ID NUMBER(17) NOT NULL,
    R_CODE VARCHAR2(5) NOT NULL,
    R_NAME VARCHAR2(512) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DATE_B DATE NOT NULL,
    DATE_E DATE,
    CONSTRAINT PK_D_HOSP_RESULTS PRIMARY KEY (ID)
);
```

---

#### Таблица №18: D_PMC_MARKERS

```sql
CREATE TABLE D_PMC_MARKERS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    MARKER NUMBER(17) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    NOTE VARCHAR2(2000),
    IS_BEFORE_DATE_OUT NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_PMC_MARKERS PRIMARY KEY (ID)
);
```

---

#### Таблица №19: D_MARKERS

```sql
CREATE TABLE D_MARKERS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    M_CODE VARCHAR2(20) NOT NULL,
    M_NAME VARCHAR2(300) NOT NULL,
    DESCRIPTION VARCHAR2(4000),
    COLOR NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_MARKERS PRIMARY KEY (ID)
);
```

---

#### Таблица №20: D_SCHEDULE_COLORS

```sql
CREATE TABLE D_SCHEDULE_COLORS (
    ID NUMBER(17) NOT NULL,
    NAME VARCHAR2(30) NOT NULL,
    GRAD_FROM VARCHAR2(7) NOT NULL,
    GRAD_TO VARCHAR2(7) NOT NULL,
    CODE NUMBER(2) NOT NULL,
    CONSTRAINT PK_D_SCHEDULE_COLORS PRIMARY KEY (ID)
);
```

---

#### Таблица №21: D_AGENT_CONTACTS

```sql
CREATE TABLE D_AGENT_CONTACTS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    TMP_PHONE1 VARCHAR2(60),
    TMP_PHONE2 VARCHAR2(60),
    TMP_EMAIL VARCHAR2(120),
    TMP_FAX VARCHAR2(60),
    TMP_TELEX VARCHAR2(60),
    TMP_BEGIN_DATE DATE,
    TMP_END_DATE DATE,
    CID NUMBER(17) NOT NULL,
    CONTACT_TYPE NUMBER(17) NOT NULL,
    CONTACT VARCHAR2(100) NOT NULL,
    NOTE VARCHAR2(200),
    IS_MAIN NUMBER NOT NULL,
    PHONE_REVERSE VARCHAR2(100),
    CONTACT_LPUDICT NUMBER(17),
    CONSTRAINT PK_D_AGENT_CONTACTS PRIMARY KEY (ID)
);
```

---

#### Таблица №22: D_CONTACT_TYPES

```sql
CREATE TABLE D_CONTACT_TYPES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CT_CODE VARCHAR2(5) NOT NULL,
    CT_NAME VARCHAR2(15) NOT NULL,
    CT_FULL_NAME VARCHAR2(60) NOT NULL,
    CT_MASK NUMBER(17),
    CONSTRAINT PK_D_CONTACT_TYPES PRIMARY KEY (ID)
);
```

---

#### Таблица №23: D_AGENT_ADDRS

```sql
CREATE TABLE D_AGENT_ADDRS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CITY NUMBER(17),
    STREET NUMBER(17),
    HOUSE VARCHAR2(11),
    HOUSELIT VARCHAR2(1),
    BLOCK NUMBER(5),
    FLATLIT VARCHAR2(1),
    ADDR_INDEX VARCHAR2(10),
    IS_MAIN NUMBER(1) NOT NULL,
    IS_REAL NUMBER(1) NOT NULL,
    IS_TEMPREG NUMBER(1) NOT NULL,
    IS_JUR NUMBER(1) NOT NULL,
    IS_POST NUMBER(1) NOT NULL,
    IS_BIRTH NUMBER(1) NOT NULL,
    RAION NUMBER(17),
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    MANUAL_INPUT VARCHAR2(4000),
    CID NUMBER(17) NOT NULL,
    IS_CITIZEN NUMBER(1),
    IS_REG NUMBER(1) NOT NULL,
    FLAT VARCHAR2(11),
    ADDROBJ VARCHAR2(36),
    FIAS_HOUSE VARCHAR2(36),
    BUILDING VARCHAR2(5),
    HOUSELIT1 VARCHAR2(1),
    BLOCKLIT VARCHAR2(1),
    CITIZEN_TYPE NUMBER(17),
    APARTMENTGUID VARCHAR2(36),
    ADDRESS NUMBER(17),
    CONSTRAINT PK_D_AGENT_ADDRS PRIMARY KEY (ID)
);
```

---

#### Таблица №24: D_AGENT_SOCIAL_STATES

```sql
CREATE TABLE D_AGENT_SOCIAL_STATES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    SOCIAL_STATE NUMBER(17),
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    SOCIAL_CATEGORY NUMBER(1),
    VERSION NUMBER(17) NOT NULL,
    IS_MAIN NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_AGENT_SOCIAL_STATES PRIMARY KEY (ID)
);
```

---

#### Таблица №25: D_SOCIALSTATES

```sql
CREATE TABLE D_SOCIALSTATES (
    ID NUMBER(17) NOT NULL,
    SOC_CODE VARCHAR2(20) NOT NULL,
    SOC_NAME VARCHAR2(200) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    SOCIAL_CATEGORY NUMBER(1) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    SOC_FCODE NUMBER(17),
    CONSTRAINT PK_D_SOCIALSTATES PRIMARY KEY (ID)
);
```

---

#### Таблица №26: D_AGENT_WORK_PLACES

```sql
CREATE TABLE D_AGENT_WORK_PLACES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    WORK_PLACE NUMBER(17),
    WORK_OKVED NUMBER(17),
    WORK_RAION NUMBER(17),
    WORK_PLACE_HAND VARCHAR2(250),
    WORK_PLACE_DEP NUMBER(17),
    JOBTITLE NUMBER(17),
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    CID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    IS_MAIN NUMBER(1) NOT NULL,
    IS_WORK NUMBER(1) NOT NULL,
    EDU_LEVEL NUMBER(17),
    EDU_ORG_TYPE NUMBER(17),
    WORK_DESCRIPTION VARCHAR2(256),
    CONSTRAINT PK_D_AGENT_WORK_PLACES PRIMARY KEY (ID)
);
```

---

#### Таблица №27: D_AGENT_CATEGORIES

```sql
CREATE TABLE D_AGENT_CATEGORIES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CATEGORY NUMBER(17) NOT NULL,
    AC_DATE DATE,
    DATE_B DATE NOT NULL,
    DATE_E DATE,
    DOC_SER VARCHAR2(10),
    DOC_NUMB VARCHAR2(20),
    CID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    ORG_NAME VARCHAR2(250),
    CONSTRAINT PK_D_AGENT_CATEGORIES PRIMARY KEY (ID)
);
```

---

#### Таблица №28: D_CATEGORIES

```sql
CREATE TABLE D_CATEGORIES (
    ID NUMBER(17) NOT NULL,
    CAT_CODE VARCHAR2(20) NOT NULL,
    CAT_NAME VARCHAR2(400) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_CATEGORIES PRIMARY KEY (ID)
);
```

---

#### Таблица №29: D_AGENT_PERSDOCS

```sql
CREATE TABLE D_AGENT_PERSDOCS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    PD_TYPE NUMBER(17),
    PD_SER VARCHAR2(20),
    PD_NUMB VARCHAR2(20),
    PD_WHEN DATE,
    PD_WHO VARCHAR2(250),
    IS_MAIN NUMBER(1) NOT NULL,
    PERIOD_BEGIN DATE NOT NULL,
    PERIOD_END DATE,
    CID NUMBER(17) NOT NULL,
    CITIZENSHIP NUMBER(17),
    PD_WHO_DIV VARCHAR2(20),
    NO_RESIDENT_STATUS NUMBER(17),
    CONSTRAINT PK_D_AGENT_PERSDOCS PRIMARY KEY (ID)
);
```

---

#### Таблица №30: D_OUTER_DIRECTIONS

```sql
CREATE TABLE D_OUTER_DIRECTIONS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PATIENT NUMBER(17) NOT NULL,
    D_DATE DATE,
    D_NUMB VARCHAR2(30),
    REPRESENT NUMBER(17),
    REPRESENT_HANDLE VARCHAR2(250),
    DIAGNOSIS NUMBER(17),
    DIAGNOSIS_HANDLE VARCHAR2(250),
    DOCTOR_HANDLE VARCHAR2(90),
    DOCTOR NUMBER(17),
    D_PREF VARCHAR2(20),
    REPRESENT_DIRECTION NUMBER(17),
    HOSP_PLAN_DATE DATE,
    DOC_SPECIALITY NUMBER(17),
    REASON NUMBER(17),
    D_DATE_END DATE,
    SERVICE NUMBER(17),
    EX_SYSTEM NUMBER(17),
    DIAGNOSIS_EXACT VARCHAR2(4000),
    DEPARTMENT VARCHAR2(250),
    INCLUDE_RESULT NUMBER(1),
    JOBTITLE NUMBER(17),
    OUTDIR_TYPE NUMBER(2),
    DISEASECHARACTER NUMBER(17),
    TO_DOCTOR NUMBER(17),
    SPECIALITY NUMBER(17),
    PROFILE NUMBER(17),
    SCH_RESOURCE NUMBER(17),
    DIRECTION_FORM NUMBER(17),
    TYPE_MED_HELP NUMBER(17),
    DIRECTION_CONDITION NUMBER(17),
    DIRECTION_REASON VARCHAR2(4000),
    EMPLOYER NUMBER(17),
    DIR_NOTE VARCHAR2(500),
    CONSTRAINT PK_D_OUTER_DIRECTIONS PRIMARY KEY (ID)
);
```

---

#### Таблица №31: D_DIR_THERAPY_SCHEMES

```sql
CREATE TABLE D_DIR_THERAPY_SCHEMES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    MED_THERAPY_SCHEMES NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_DIR_THERAPY_SCHEMES PRIMARY KEY (ID)
);
```

---

#### Таблица №32: D_MED_THERAPY_SCHEMES_DESC

```sql
CREATE TABLE D_MED_THERAPY_SCHEMES_DESC (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    DAYS VARCHAR2(10),
    KSG NUMBER(17),
    REK_CNT1 NUMBER(4),
    REK_CNT2 NUMBER(4),
    NEOB NUMBER(1),
    HOSP_TYPE NUMBER(1),
    IS_KSG NUMBER(1),
    MKB NUMBER(17),
    COST NUMBER(10,2),
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    CONSTRAINT PK_D_MED_THERAPY_SCHEMES_DESC PRIMARY KEY (ID)
);
```

---

#### Таблица №33: D_MED_THERAPY_SCHEMES

```sql
CREATE TABLE D_MED_THERAPY_SCHEMES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CODE VARCHAR2(60) NOT NULL,
    NAME VARCHAR2(100) NOT NULL,
    DESCRIPTION VARCHAR2(4000),
    CONSTRAINT PK_D_MED_THERAPY_SCHEMES PRIMARY KEY (ID)
);
```

---

#### Таблица №34: D_DIRECTIONS

```sql
CREATE TABLE D_DIRECTIONS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    OUTER_DIRECTION NUMBER(17),
    LPU_TO NUMBER(17),
    LPU_TO_HANDLE VARCHAR2(250),
    PATIENT NUMBER(17) NOT NULL,
    REG_VISIT NUMBER(17),
    REG_EMPLOYER NUMBER(17),
    REG_DATE DATE NOT NULL,
    DIR_COMMENT VARCHAR2(4000),
    REG_TYPE NUMBER(1) NOT NULL,
    DIR_TYPE NUMBER(1) NOT NULL,
    HOSP_MKB NUMBER(17),
    HOSP_KIND NUMBER(17),
    DIR_NUMB VARCHAR2(30),
    SPECIALITY NUMBER(17),
    EX_CAUSE_MKB NUMBER(17),
    INJURE_KIND NUMBER(17),
    INJURE_TIME NUMBER(5,2),
    DIRECTION_KIND NUMBER(17),
    HOSP_DEP NUMBER(17),
    DIR_PREF VARCHAR2(20),
    HOSP_BED_TYPE NUMBER(17),
    MES NUMBER(17),
    REG_HPKPJ NUMBER(17),
    HOSP_REASON NUMBER(17),
    IS_CANCELED NUMBER(1) NOT NULL,
    CANC_REASON NUMBER(17),
    CANC_EMPLOYER NUMBER(17),
    CANC_EMPLOYER_FIO VARCHAR2(150),
    CANC_DATE DATE,
    REG_DIR_SERV NUMBER(17),
    HOSP_PLAN_DATE DATE,
    DATE_TR DATE,
    HOSP_DEPDICT NUMBER(17),
    DOC_COMMENT VARCHAR2(4000),
    HOSP_TYPE NUMBER(17),
    TALON_VMP_DATE DATE,
    HOSP_DIRECT_TYPE NUMBER(17),
    HOSP_REASON_STREETKIDS NUMBER(17),
    TRANSPORTATION_KIND NUMBER(17),
    HOSP_HOUR NUMBER(3),
    REG_DEP NUMBER(17),
    HOSP_MKB_EXACT VARCHAR2(4000),
    TALON_VMP_NUM VARCHAR2(20),
    IS_ONKO NUMBER(1),
    REALISED_DAYS NUMBER(3),
    DIR_FORM NUMBER(1),
    THERAPY_SCHEMES NUMBER(17),
    VMP NUMBER(17),
    DIRECTION_FORM NUMBER(17),
    TYPE_MED_HELP NUMBER(17),
    DIRECTION_CONDITION NUMBER(17),
    DIRECTION_REASON VARCHAR2(4000),
    CONSTRAINT PK_D_DIRECTIONS PRIMARY KEY (ID)
);
```

---

#### Таблица №35: D_MKB10

```sql
CREATE TABLE D_MKB10 (
    ID NUMBER(17) NOT NULL,
    MKB_CODE VARCHAR2(10) NOT NULL,
    MKB_NAME VARCHAR2(500) NOT NULL,
    PID NUMBER(17),
    ALLOW_IN NUMBER(1) NOT NULL,
    HLEVEL NUMBER(1) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    SEX NUMBER(1),
    SEX_CHECK NUMBER(1),
    AGE_FROM NUMBER(3),
    AGE_TO NUMBER(3),
    AGE_CHECK NUMBER(1),
    ALLOW_IN_MAIN NUMBER(1) NOT NULL,
    SPECIALITY_CHECK NUMBER(1),
    ACTUAL_TILL DATE,
    CONSTRAINT PK_D_MKB10 PRIMARY KEY (ID)
);
```

---

#### Таблица №36: D_PERSMEDCARD

```sql
CREATE TABLE D_PERSMEDCARD (
    ID NUMBER(17) NOT NULL,
    BLOODGROUPE NUMBER(17),
    RHESUS NUMBER(1),
    ECOLOR VARCHAR2(40),
    CREATEDATE DATE,
    MODDATE DATE,
    CARD_NUMB VARCHAR2(26),
    LPU NUMBER(17) NOT NULL,
    AGENT NUMBER(17) NOT NULL,
    NOTE VARCHAR2(4000),
    CID NUMBER(17) NOT NULL,
    REG_DIVISION NUMBER(17),
    TP_PRINTED NUMBER(1) NOT NULL,
    OUTNUMB VARCHAR2(12) NOT NULL,
    PMC_TYPE NUMBER(1),
    IA_PRINTED NUMBER(1) NOT NULL,
    SMS_AGREE NUMBER(1) NOT NULL,
    EMP_ID NUMBER(17),
    EMAIL_AGREE NUMBER(1) NOT NULL,
    PHENOTYPE VARCHAR2(10),
    CABLAB NUMBER(17),
    CONSTRAINT PK_D_PERSMEDCARD PRIMARY KEY (ID)
);
```

---

#### Таблица №37: D_HPK_PLANS

```sql
CREATE TABLE D_HPK_PLANS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    PLAN_DATE DATE NOT NULL,
    MALE_COUNT NUMBER(5),
    OPER_COUNT NUMBER(5),
    GEN_COUNT NUMBER(5),
    CID NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_HPK_PLANS PRIMARY KEY (ID)
);
```

---

#### Таблица №38: D_HOSPITALIZATION_TYPES

```sql
CREATE TABLE D_HOSPITALIZATION_TYPES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    HK_CODE VARCHAR2(2) NOT NULL,
    HK_NAME VARCHAR2(150) NOT NULL,
    IS_ACTIVE NUMBER(1) NOT NULL,
    URGENT NUMBER(1),
    CONSTRAINT PK_D_HOSPITALIZATION_TYPES PRIMARY KEY (ID)
);
```

---

#### Таблица №39: D_LPUDICT

```sql
CREATE TABLE D_LPUDICT (
    ID NUMBER(17) NOT NULL,
    LPU_CODE VARCHAR2(20) NOT NULL,
    LPU_FULLNAME VARCHAR2(300),
    HEADDOCT VARCHAR2(160),
    IS_RESP NUMBER(1) NOT NULL,
    BOOKKEEPER VARCHAR2(150),
    DATE_B DATE,
    DATE_E DATE,
    PRIV_DATE_B DATE,
    PRIV_DATE_E DATE,
    HAS_PRIV_REC NUMBER(1),
    VERSION NUMBER(17) NOT NULL,
    LPUKIND NUMBER(17),
    AGENT NUMBER(17),
    LPU_NAME VARCHAR2(100),
    HID NUMBER(17),
    CID NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_LPUDICT PRIMARY KEY (ID)
);
```

---

#### Таблица №40: D_HH_CANC_REASON

```sql
CREATE TABLE D_HH_CANC_REASON (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CANC_REASON NUMBER(17),
    COMM VARCHAR2(4000),
    EMPLOYER NUMBER(17) NOT NULL,
    CANC_DATE DATE NOT NULL,
    CONSTRAINT PK_D_HH_CANC_REASON PRIMARY KEY (ID)
);
```

---

#### Таблица №41: D_CANC_REASON

```sql
CREATE TABLE D_CANC_REASON (
    ID NUMBER(17) NOT NULL,
    CODE NUMBER(3) NOT NULL,
    NAME VARCHAR2(1000) NOT NULL,
    CONSTRAINT PK_D_CANC_REASON PRIMARY KEY (ID)
);
```

---

#### Таблица №42: D_HPKPJ_VMP_STATES

```sql
CREATE TABLE D_HPKPJ_VMP_STATES (
    VS_CODE NUMBER(1) NOT NULL,
    VS_NAME VARCHAR2(30) NOT NULL,
    CONSTRAINT PK_D_HPKPJ_VMP_STATES PRIMARY KEY (VS_CODE)
);
```

---

#### Таблица №43: D_DIRECTION_KINDS

```sql
CREATE TABLE D_DIRECTION_KINDS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DK_CODE VARCHAR2(10) NOT NULL,
    DK_NAME VARCHAR2(400) NOT NULL,
    SHORT_NAME VARCHAR2(6),
    IS_ACTIVE NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_DIRECTION_KINDS PRIMARY KEY (ID)
);
```

---

#### Таблица №44: D_DIR_CANC_REASONS

```sql
CREATE TABLE D_DIR_CANC_REASONS (
    DCR_CODE NUMBER(2) NOT NULL,
    DCR_NAME VARCHAR2(120) NOT NULL,
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    CONSTRAINT PK_D_DIR_CANC_REASONS PRIMARY KEY (ID)
);
```

---

#### Таблица №45: D_WL_RECORDS74

```sql
CREATE TABLE D_WL_RECORDS74 (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    HID NUMBER(17),
    AGENT NUMBER(17),
    PREF VARCHAR2(1),
    NUMB NUMBER(5) NOT NULL,
    TICKET_TYPE NUMBER(1) NOT NULL,
    IS_ILL NUMBER(1) NOT NULL,
    SERVICE NUMBER(17) NOT NULL,
    DIR_SERV NUMBER(17),
    PAYMENT_KIND NUMBER(17),
    REG_DATE DATE NOT NULL,
    CALL_DATE DATE,
    STATUS NUMBER(1) NOT NULL,
    CALL_NUMB NUMBER(2) NOT NULL,
    CHECKUP_DATE DATE,
    VISIT_DATE DATE,
    EMPLOYER NUMBER(17),
    CABLAB NUMBER(17),
    PROFCARD NUMBER(17),
    CONSTRAINT PK_D_WL_RECORDS74 PRIMARY KEY (ID)
);
```

---

#### Таблица №46: D_DIRECTION_SERVICES

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
```

---

#### Таблица №47: D_VMP_LINKS

```sql
CREATE TABLE D_VMP_LINKS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    VMP_APPL NUMBER(17) NOT NULL,
    VMP_TALON NUMBER(17) NOT NULL,
    DIRECTION NUMBER(17),
    CONSTRAINT PK_D_VMP_LINKS PRIMARY KEY (ID)
);
```

---

#### Таблица №48: D_BLOODGROUPE

```sql
CREATE TABLE D_BLOODGROUPE (
    ID NUMBER(17) NOT NULL,
    BG_CODE VARCHAR2(20) NOT NULL,
    BG_NAME VARCHAR2(160) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_BLOODGROUPE PRIMARY KEY (ID)
);
```

---

#### Таблица №49: D_DIVISIONS

```sql
CREATE TABLE D_DIVISIONS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    DIV_CODE VARCHAR2(40) NOT NULL,
    DIV_NAME VARCHAR2(256) NOT NULL,
    LPU_BRANCH NUMBER(17),
    REC_SER_PRIV VARCHAR2(10),
    HID NUMBER(17),
    REC_SER_PRIV_88 VARCHAR2(10),
    PAT_RESTRICTION NUMBER(17),
    VERSION NUMBER(17),
    DIV_OID_FRMO VARCHAR2(50),
    CONSTRAINT PK_D_DIVISIONS PRIMARY KEY (ID)
);
```

---

#### Таблица №50: D_RHESUS

```sql
CREATE TABLE D_RHESUS (
    RH_CODE NUMBER(1) NOT NULL,
    RH_NAME VARCHAR2(10) NOT NULL,
    CONSTRAINT PK_D_RHESUS PRIMARY KEY (RH_CODE)
);
```

---

#### Таблица №51: D_LPU

```sql
CREATE TABLE D_LPU (
    ID NUMBER(17) NOT NULL,
    FULLNAME VARCHAR2(300) NOT NULL,
    HEADDOCTOR_FULLNAME VARCHAR2(160),
    FULLADDRESS VARCHAR2(160),
    PHONES VARCHAR2(80),
    REC_SER_PRIV VARCHAR2(10),
    REC_SER VARCHAR2(10),
    CODE_LPU VARCHAR2(20),
    CODE_OGRN VARCHAR2(15),
    CODE_OKPO VARCHAR2(10),
    CODE_OKDP VARCHAR2(8),
    CODE_OKONH VARCHAR2(5),
    CODE_OKATO VARCHAR2(11),
    CODE_OKOGU VARCHAR2(10),
    CODE_OCOPPH VARCHAR2(5),
    CODE_OKFS VARCHAR2(2),
    LPUDICT NUMBER(17),
    BOOKKEEPER_FULLNAME VARCHAR2(160),
    HEADECONOMIST_FULLNAME VARCHAR2(160),
    GEOGRAFY NUMBER(17),
    USERFORMS VARCHAR2(64),
    GENNUMB_GROUP NUMBER(17),
    EXEC_AUTHORITY VARCHAR2(150),
    REC_SER_PRIV_88 VARCHAR2(10),
    IP_ADDR VARCHAR2(250),
    BY_ES_ONLY NUMBER(1),
    WEBSITE VARCHAR2(250),
    IS_TECH_LPU NUMBER(1) NOT NULL,
    ADDRESS NUMBER(17),
    CONSTRAINT PK_D_LPU PRIMARY KEY (ID)
);
```

---

#### Таблица №52: D_GEOGRAFY

```sql
CREATE TABLE D_GEOGRAFY (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17),
    GEONAME VARCHAR2(60) NOT NULL,
    GEOLOCTYPE NUMBER(17) NOT NULL,
    KLADR_CODE VARCHAR2(20),
    KLADR_INDEX VARCHAR2(6),
    KLADR_GNINMB VARCHAR2(4),
    KLADR_OCATD VARCHAR2(11),
    KLADR_STATUS NUMBER(1),
    VERSION NUMBER(17) NOT NULL,
    GEOFULL VARCHAR2(4000),
    FIAS_CODE VARCHAR2(36),
    ADDRESS NUMBER(17),
    CONSTRAINT PK_D_GEOGRAFY PRIMARY KEY (ID)
);
```

---

#### Таблица №53: D_BUILDINGS

```sql
CREATE TABLE D_BUILDINGS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CODE VARCHAR2(250) NOT NULL,
    NAME VARCHAR2(4000) NOT NULL,
    ADDRS_CITY NUMBER(17),
    ADDRS_STREET NUMBER(17),
    ADDRS_HOUSE VARCHAR2(11),
    ADDRS_HOUSELIT VARCHAR2(1),
    ADDRS_BLOCK NUMBER(5),
    ADDRS_MANUAL VARCHAR2(250),
    ADDRESS NUMBER(17),
    CONSTRAINT PK_D_BUILDINGS PRIMARY KEY (ID)
);
```

---

#### Таблица №54: D_BUILD_FLOORS

```sql
CREATE TABLE D_BUILD_FLOORS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    NAME VARCHAR2(160) NOT NULL,
    CONSTRAINT PK_D_BUILD_FLOORS PRIMARY KEY (ID)
);
```

---

#### Таблица №55: D_CABLAB_TYPE

```sql
CREATE TABLE D_CABLAB_TYPE (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CABLAB_TYPE VARCHAR2(160) NOT NULL,
    CABLAB_CODE NUMBER(2) NOT NULL,
    CONSTRAINT PK_D_CABLAB_TYPE PRIMARY KEY (ID)
);
```

---

#### Таблица №56: D_VISITPLACES

```sql
CREATE TABLE D_VISITPLACES (
    ID NUMBER(17) NOT NULL,
    VP_CONTENT VARCHAR2(200) NOT NULL,
    VP_CODE VARCHAR2(6) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    VP_FCODE NUMBER(1),
    OPEN_DATE DATE NOT NULL,
    CLOSE_DATE DATE,
    CONSTRAINT PK_D_VISITPLACES PRIMARY KEY (ID)
);
```

---

#### Таблица №57: D_VISITS

```sql
CREATE TABLE D_VISITS (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    EMPLOYER NUMBER(17) NOT NULL,
    VISIT_DATE DATE NOT NULL,
    VISIT_PLACE NUMBER(17),
    VISIT_RESULT NUMBER(17),
    LPU NUMBER(17) NOT NULL,
    NURSE NUMBER(17),
    HELPER NUMBER(17),
    ANESTHETIZATION NUMBER(2),
    DGROUP NUMBER(17),
    PROF_DISEASE NUMBER(17),
    SER_COUNT NUMBER(19,2) NOT NULL,
    SER_KOEFF NUMBER(5,3) NOT NULL,
    UET_COUNT NUMBER(5,2) NOT NULL,
    MINUTES NUMBER(5),
    CABLAB NUMBER(17),
    VISIT_TEMPLATE NUMBER(17),
    REF_RESULT NUMBER(17),
    MED_CARE_KIND NUMBER(17),
    APP_KIND NUMBER(17),
    ANEST_KIND NUMBER(17),
    ANEST_INDICATIONS NUMBER(17),
    IS_EMERGENCY NUMBER(1),
    PATIENT NUMBER(17),
    CONSTRAINT PK_D_VISITS PRIMARY KEY (ID)
);
```

---

#### Таблица №58: D_DIR_REG_TYPES

```sql
CREATE TABLE D_DIR_REG_TYPES (
    DRT_CODE NUMBER(1) NOT NULL,
    DRT_NAME VARCHAR2(30) NOT NULL,
    CONSTRAINT PK_D_DIR_REG_TYPES PRIMARY KEY (DRT_CODE)
);
```

---

#### Таблица №59: D_SPECIALITIES

```sql
CREATE TABLE D_SPECIALITIES (
    ID NUMBER(17) NOT NULL,
    CODE VARCHAR2(10) NOT NULL,
    TITLE VARCHAR2(250) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    RECORD_PERIOD NUMBER(2),
    CONSTRAINT PK_D_SPECIALITIES PRIMARY KEY (ID)
);
```

---

#### Таблица №60: D_MESES

```sql
CREATE TABLE D_MESES (
    ID NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    M_CODE VARCHAR2(50) NOT NULL,
    M_NAME VARCHAR2(500) NOT NULL,
    M_TYPE NUMBER(1) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    PAY_CONSTRAINT NUMBER(1) NOT NULL,
    PATIENT_SEX_CONSTRAINT NUMBER(1) NOT NULL,
    PLAN_DAYS NUMBER(5) NOT NULL,
    FULL_PRICE NUMBER(17,2),
    COMMENTS VARCHAR2(4000),
    DAYS_ON_BED NUMBER(5),
    IS_DAY_HOSPITAL NUMBER(1),
    SERV_FOR_PRESCRIBES NUMBER(17),
    SERV_FOR_DIETARIES NUMBER(17),
    IS_VMP NUMBER(1) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_MESES PRIMARY KEY (ID)
);
```

---

#### Таблица №61: D_HOSP_HOURS

```sql
CREATE TABLE D_HOSP_HOURS (
    HOUR_CODE NUMBER(3) NOT NULL,
    HOUR_NAME VARCHAR2(100) NOT NULL,
    CONSTRAINT PK_D_HOSP_HOURS PRIMARY KEY (HOUR_CODE)
);
```

---

#### Таблица №62: D_HOSPITALIZATIONKINDS

```sql
CREATE TABLE D_HOSPITALIZATIONKINDS (
    ID NUMBER(17) NOT NULL,
    HK_CODE VARCHAR2(20) NOT NULL,
    HK_NAME VARCHAR2(160) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_HOSPITALIZATIONKINDS PRIMARY KEY (ID)
);
```

---

#### Таблица №63: D_INJURE_KINDS

```sql
CREATE TABLE D_INJURE_KINDS (
    ID NUMBER(17) NOT NULL,
    IK_CODE VARCHAR2(2) NOT NULL,
    IK_NAME VARCHAR2(60) NOT NULL,
    IK_TYPE NUMBER(1) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    IK_FCODE NUMBER(17),
    CONSTRAINT PK_D_INJURE_KINDS PRIMARY KEY (ID)
);
```

---

#### Таблица №64: D_BED_TYPES

```sql
CREATE TABLE D_BED_TYPES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    BT_CODE VARCHAR2(30) NOT NULL,
    BT_NAME VARCHAR2(400) NOT NULL,
    IS_ACTIVE NUMBER(1) NOT NULL,
    SHORT_NAME VARCHAR2(100),
    CONSTRAINT PK_D_BED_TYPES PRIMARY KEY (ID)
);
```

---

#### Таблица №65: D_HOSP_REASONS

```sql
CREATE TABLE D_HOSP_REASONS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    HR_CODE VARCHAR2(10) NOT NULL,
    HR_NAME VARCHAR2(100) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    CONSTRAINT PK_D_HOSP_REASONS PRIMARY KEY (ID)
);
```

---

#### Таблица №66: D_DEPDICT

```sql
CREATE TABLE D_DEPDICT (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DEP_CODE VARCHAR2(100) NOT NULL,
    DEP_NAME VARCHAR2(300) NOT NULL,
    DP_KIND NUMBER(17),
    LPUDICT NUMBER(17),
    DATE_BEGIN DATE,
    DATE_END DATE,
    CONSTRAINT PK_D_DEPDICT PRIMARY KEY (ID)
);
```

---

#### Таблица №67: D_HOSP_DIRECT_TYPES

```sql
CREATE TABLE D_HOSP_DIRECT_TYPES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DT_CODE NUMBER(17) NOT NULL,
    DT_NAME VARCHAR2(256) NOT NULL,
    CONSTRAINT PK_D_HOSP_DIRECT_TYPES PRIMARY KEY (ID)
);
```

---

#### Таблица №68: D_TRANSPORTATION_KINDS

```sql
CREATE TABLE D_TRANSPORTATION_KINDS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    TK_CODE VARCHAR2(2) NOT NULL,
    TK_NAME VARCHAR2(150) NOT NULL,
    CONSTRAINT PK_D_TRANSPORTATION_KINDS PRIMARY KEY (ID)
);
```

---

#### Таблица №69: D_DIRECTION_FORMS

```sql
CREATE TABLE D_DIRECTION_FORMS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DS_CODE VARCHAR2(20),
    DS_NAME VARCHAR2(160),
    CONSTRAINT PK_D_DIRECTION_FORMS PRIMARY KEY (ID)
);
```

---

#### Таблица №70: D_TYPE_MED_HELP

```sql
CREATE TABLE D_TYPE_MED_HELP (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CODE NUMBER(3),
    HID NUMBER(17),
    NAME VARCHAR2(500),
    SORT_VALUE NUMBER(17),
    CONSTRAINT PK_D_TYPE_MED_HELP PRIMARY KEY (ID)
);
```

---

#### Таблица №71: D_DIRECTION_CONDITIONS

```sql
CREATE TABLE D_DIRECTION_CONDITIONS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DC_CODE VARCHAR2(20),
    DC_NAME VARCHAR2(160),
    CONSTRAINT PK_D_DIRECTION_CONDITIONS PRIMARY KEY (ID)
);
```

---

#### Таблица №72: D_REASON_DIRECTION

```sql
CREATE TABLE D_REASON_DIRECTION (
    ID NUMBER(17) NOT NULL,
    NAME_REASON VARCHAR2(500) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CODE_REASON VARCHAR2(5),
    CONSTRAINT PK_D_REASON_DIRECTION PRIMARY KEY (ID)
);
```

---

#### Таблица №73: D_EX_SYSTEMS

```sql
CREATE TABLE D_EX_SYSTEMS (
    ID NUMBER(17) NOT NULL,
    S_CODE VARCHAR2(50) NOT NULL,
    S_NAME VARCHAR2(300) NOT NULL,
    URL_SERVICE VARCHAR2(300),
    STOPPED NUMBER(1),
    CONSTRAINT PK_D_EX_SYSTEMS PRIMARY KEY (ID)
);
```

---

#### Таблица №74: D_JOBTITLES

```sql
CREATE TABLE D_JOBTITLES (
    ID NUMBER(17) NOT NULL,
    TITLE VARCHAR2(250) NOT NULL,
    CODE VARCHAR2(20) NOT NULL,
    CID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    SHORT_TITLE VARCHAR2(100),
    SHOW_FOR_BULL NUMBER(1) NOT NULL,
    STAFF_LEVEL NUMBER(1) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    SHOW_FOR_CF NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_JOBTITLES PRIMARY KEY (ID)
);
```

---

#### Таблица №75: D_ER_PROFILES

```sql
CREATE TABLE D_ER_PROFILES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    P_NAME VARCHAR2(80) NOT NULL,
    P_INFO VARCHAR2(200),
    ALLOW_RECORD NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_ER_PROFILES PRIMARY KEY (ID)
);
```

---

#### Таблица №76: D_AGENT_NAMES

```sql
CREATE TABLE D_AGENT_NAMES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    FIRSTNAME VARCHAR2(40),
    SURNAME VARCHAR2(400),
    LASTNAME VARCHAR2(40),
    IS_MAIN NUMBER(1) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    FIRSTNAME_FR VARCHAR2(40),
    SURNAME_FR VARCHAR2(400),
    LASTNAME_FR VARCHAR2(40),
    FIRSTNAME_TO VARCHAR2(40),
    SURNAME_TO VARCHAR2(400),
    LASTNAME_TO VARCHAR2(40),
    FIRSTNAME_AC VARCHAR2(40),
    SURNAME_AC VARCHAR2(400),
    LASTNAME_AC VARCHAR2(40),
    FIRSTNAME_ABL VARCHAR2(40),
    SURNAME_ABL VARCHAR2(400),
    LASTNAME_ABL VARCHAR2(40),
    CID NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_AGENT_NAMES PRIMARY KEY (ID)
);
```

---

#### Таблица №77: D_DEPS_TYPES

```sql
CREATE TABLE D_DEPS_TYPES (
    ID NUMBER(17) NOT NULL,
    DT_CODE VARCHAR2(20) NOT NULL,
    DT_NAME VARCHAR2(160) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_DEPS_TYPES PRIMARY KEY (ID)
);
```

---

#### Таблица №78: D_KSGCODES

```sql
CREATE TABLE D_KSGCODES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    KSG_CODE VARCHAR2(50) NOT NULL,
    KSG_NAME VARCHAR2(400) NOT NULL,
    K_TYPE NUMBER(1) NOT NULL,
    KSG_REE_CODE VARCHAR2(20),
    HOSP_TYPE NUMBER(1),
    CONSTRAINT PK_D_KSGCODES PRIMARY KEY (ID)
);
```

---

#### Таблица №79: D_VMP

```sql
CREATE TABLE D_VMP (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    VMP_CODE VARCHAR2(20) NOT NULL,
    VMP_NAME VARCHAR2(1000) NOT NULL,
    DATE_B DATE NOT NULL,
    DATE_E DATE,
    HID NUMBER(17),
    IS_AVAILABLE NUMBER(1) NOT NULL,
    TMP_HMODP NUMBER(6),
    HMODP NUMBER(17),
    PROFILE NUMBER(17),
    HGR NUMBER(3),
    PART NUMBER(1),
    CONSTRAINT PK_D_VMP PRIMARY KEY (ID)
);
```

---

#### Таблица №80: D_HOSP_OUTCOMES

```sql
CREATE TABLE D_HOSP_OUTCOMES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    R_CODE VARCHAR2(5) NOT NULL,
    R_NAME VARCHAR2(600) NOT NULL,
    DATE_B DATE NOT NULL,
    DATE_E DATE,
    CONSTRAINT PK_D_HOSP_OUTCOMES PRIMARY KEY (ID)
);
```

---

#### Таблица №81: D_ALV

```sql
CREATE TABLE D_ALV (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    ALV_CODE VARCHAR2(20) NOT NULL,
    ALV_NAME VARCHAR2(1000) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    CONSTRAINT PK_D_ALV PRIMARY KEY (ID)
);
```

---

#### Таблица №82: D_SCALE_REHAB

```sql
CREATE TABLE D_SCALE_REHAB (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    SR_CODE VARCHAR2(20) NOT NULL,
    SR_NAME VARCHAR2(1000) NOT NULL,
    BEGIN_DATE DATE NOT NULL,
    END_DATE DATE,
    CONSTRAINT PK_D_SCALE_REHAB PRIMARY KEY (ID)
);
```

---

#### Таблица №83: D_AGENT_RELATIVES

```sql
CREATE TABLE D_AGENT_RELATIVES (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    RELATIONSHIP NUMBER(17) NOT NULL,
    AGENT NUMBER(17),
    FIRSTNAME VARCHAR2(30),
    SURNAME VARCHAR2(30),
    LASTNAME VARCHAR2(30),
    BIRTHDATE DATE,
    AR_CODE VARCHAR2(20),
    CID NUMBER(17) NOT NULL,
    REPRESENT NUMBER(1) NOT NULL,
    LEGAL_STATUS NUMBER(17),
    REPRESENT_ER NUMBER(1) NOT NULL,
    TRUSTED NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_AGENT_RELATIVES PRIMARY KEY (ID)
);
```

---

#### Таблица №84: D_HH_TRANSFER_REASONS

```sql
CREATE TABLE D_HH_TRANSFER_REASONS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    TR_NAME VARCHAR2(150) NOT NULL,
    CONSTRAINT PK_D_HH_TRANSFER_REASONS PRIMARY KEY (ID)
);
```

---

#### Таблица №85: D_ABILITY_STATUS

```sql
CREATE TABLE D_ABILITY_STATUS (
    ID NUMBER(17) NOT NULL,
    AS_NAME VARCHAR2(150) NOT NULL,
    CONSTRAINT PK_D_ABILITY_STATUS PRIMARY KEY (ID)
);
```

---

#### Таблица №86: D_DIRECTORIES_DATA_VER

```sql
CREATE TABLE D_DIRECTORIES_DATA_VER (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DIR NUMBER(3) NOT NULL,
    DD_CODE VARCHAR2(20) NOT NULL,
    DD_NAME VARCHAR2(350) NOT NULL,
    CONSTRAINT PK_D_DIRECTORIES_DATA_VER PRIMARY KEY (ID)
);
```

---

#### Таблица №87: D_HOSP_INCOMES

```sql
CREATE TABLE D_HOSP_INCOMES (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CODE VARCHAR2(100) NOT NULL,
    INCOME_NAME VARCHAR2(100) NOT NULL,
    CONSTRAINT PK_D_HOSP_INCOMES PRIMARY KEY (ID)
);
```

---

#### Таблица №88: D_DIRECTION_HOSP

```sql
CREATE TABLE D_DIRECTION_HOSP (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    DH_CODE VARCHAR2(10) NOT NULL,
    DH_NAME VARCHAR2(255) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    CONSTRAINT PK_D_DIRECTION_HOSP PRIMARY KEY (ID)
);
```

---

#### Таблица №89: D_CONTRACTS

```sql
CREATE TABLE D_CONTRACTS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    CONTRACT_TYPE NUMBER(17) NOT NULL,
    DOC_PREF VARCHAR2(20),
    DOC_DATE DATE,
    EXT_NUMB VARCHAR2(40),
    EXT_DATE DATE,
    AGENT NUMBER(17) NOT NULL,
    DATE_BEGIN DATE,
    DATE_END DATE,
    PHONE VARCHAR2(20),
    PERSON VARCHAR2(250),
    SUMM NUMBER(17,2),
    CONTRACT NUMBER(17),
    EMPLOYER NUMBER(17),
    IS_OPEN NUMBER(1) NOT NULL,
    FACIAL_ACCOUNT NUMBER(17),
    DEBT_SUMM NUMBER(17,5) NOT NULL,
    CONTRACT_SUMM NUMBER(17,5) NOT NULL,
    PLAN_SUMM NUMBER(17,5),
    NOTE VARCHAR2(2000),
    IS_IMPORTED NUMBER(1) NOT NULL,
    WORKDATE DATE,
    STATUS NUMBER(1) NOT NULL,
    REPRESENT NUMBER(17),
    CENTER NUMBER(1),
    DOC_NUMB VARCHAR2(20),
    CONSTRAINT PK_D_CONTRACTS PRIMARY KEY (ID)
);
```

---

#### Таблица №90: D_SMP_CALL_EX_SYSTEM

```sql
CREATE TABLE D_SMP_CALL_EX_SYSTEM (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    LPU_CODE VARCHAR2(20),
    AGENT NUMBER(17) NOT NULL,
    LPU_CODE_SMP VARCHAR2(20),
    CALL_ID VARCHAR2(100) NOT NULL,
    CALL_NUMB VARCHAR2(30) NOT NULL,
    CALL_DATE DATE NOT NULL,
    CALL_PLACE VARCHAR2(100),
    CALL_REASON VARCHAR2(100),
    CALL_STRUCTURE NUMBER(3),
    MAIN_MKB VARCHAR2(10),
    ADD_MKB VARCHAR2(500),
    COMPLICATIONS VARCHAR2(400),
    DELIVERY_TYPE VARCHAR2(100),
    HELP_ON_PLACE VARCHAR2(4000),
    HELP_IN_CAR VARCHAR2(4000),
    HOSP_STATUS NUMBER(1),
    REFUSE_REASON NUMBER(2),
    CALL_ADDRESS VARCHAR2(500),
    PHONE VARCHAR2(200),
    CALL_TYPE NUMBER(1),
    CALL_STATUS NUMBER(1),
    SEND_TIME DATE,
    EMPLOYER NUMBER(17),
    REFUSE_REASON_POL VARCHAR2(400),
    WHO_CALL VARCHAR2(100),
    CALL VARCHAR2(100),
    ACCIDENT_CAUSE VARCHAR2(400),
    PROFILE_TEAMS VARCHAR2(400),
    INTOXICATION NUMBER(1),
    COMPLAINTS VARCHAR2(4000),
    ANAMNESIS VARCHAR2(2000),
    OBJECTIV_DATA VARCHAR2(400),
    RESULT VARCHAR2(400),
    SENDER_PERSON VARCHAR2(100),
    HJ_ID NUMBER(17),
    HPK_ID NUMBER(17),
    HH_ID NUMBER(17),
    SMP VARCHAR2(20),
    SMP_TEAM VARCHAR2(20),
    TIME_TAKE DATE,
    TIME_TEAM DATE,
    TIME_DEPART DATE,
    TIME_ARRIV DATE,
    TIME_TRANSF DATE,
    TIME_DELIVERY DATE,
    TIME_COMPLIT DATE,
    TIME_RETURN DATE,
    DEATHDATE DATE,
    ISHOD VARCHAR2(20),
    SENIOR_TEAM VARCHAR2(122),
    SENIOR_TEAM_CODE VARCHAR2(5),
    SMP_ID NUMBER(17),
    SEND_RESULT VARCHAR2(500),
    SENIOR_EMP_CODE VARCHAR2(5),
    DOP_INFO VARCHAR2(100),
    FIRST_ASSISTANT_CODE VARCHAR2(5),
    SECOND_ASSISTANT_CODE VARCHAR2(5),
    DRIVER_CODE VARCHAR2(5),
    SENIOR_DISP_CODE VARCHAR2(5),
    RECEPTION_DISP_CODE VARCHAR2(5),
    DEST_DISP_CODE VARCHAR2(5),
    CLOSE_DISP_CODE VARCHAR2(5),
    RECIEVED_BY NUMBER(1),
    AREA_CODE VARCHAR2(5),
    ADIS_ID VARCHAR2(100),
    AMB_ID NUMBER(17),
    ADIS_REASON VARCHAR2(100),
    ADIS_RESULT VARCHAR2(100),
    ADIS_DESC_RESULT VARCHAR2(500),
    DIVISION_CODE VARCHAR2(20),
    EX_SYSTEM NUMBER(17),
    BED_PROFILE VARCHAR2(100),
    TERRITORIAL_SMP VARCHAR2(10),
    RECEPTION_SMP VARCHAR2(10),
    CONSTRAINT PK_D_SMP_CALL_EX_SYSTEM PRIMARY KEY (ID)
);
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 34
- Загружено тел функций: 26

---

### Функция №1: D_PKG_SIGNAL_INFO_SETS.GET_FULL_SIGNAL_INFORMATION

```sql
-- Oracle PACKAGE: GET_FULL_SIGNAL_INFORMATION
--======================================================================
procedure GET_FULL_SIGNAL_INFORMATION
(
  pnLPU                                in NUMBER,
  pnSI_PLACE                           in NUMBER,
  pnPATIENT                            in NUMBER,
  psINFO                               out VARCHAR2,
  pnDIRECTION_SERVICES                 in NUMBER default null,
  pnDISEASECASE                        in NUMBER default null
)
is
  clIDS                 D_CL_ID;
  sINFO                 VARCHAR2(4000);
  nICON                 D_SIGNAL_INFO_SETS.ICON%type;
begin
  clIDS := D_PKG_SIGNAL_INFO_SETS.GET_SINFO_SETS(pnLPU,pnSI_PLACE);
  if clIDS.COUNT = 0 then
    return;
  end if;
  for i in clIDS.FIRST..clIDS.LAST
  loop
    sINFO := null;
    D_PKG_SIGNAL_INFO_SETS.GET_SIGNAL_INFORMATION(pnLPU,clIDS(i),pnPATIENT,sINFO,pnDIRECTION_SERVICES, pnDISEASECASE);
    if sINFO is not null then
      if pnSI_PLACE in (3, 4, 5, 6, 7, 8, 9, 10) then
        begin
          select t.ICON
            into nICON
            from D_SIGNAL_INFO_SETS t
           where t.ID = clIDS(i);
        exception
          when NO_DATA_FOUND
            then nICON := null;  
        end;
```

---

### Функция №2: D_PKG_CSE_ACCESSES.GET_ID_WITH_RIGHTS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №3: D_PKG_STD.FRM_DT

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №4: D_PKG_HPK_PLAN_JOURNALS.GET_HOSP_HISTORY_STATUS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №5: D_PKG_STD.FRM_D

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №6: D_PKG_DAT_TOOLS.FULL_YEARS

```sql
-- Oracle PACKAGE: FULL_YEARS
-- Возвращает: return NUMBER
--======================================================================
function FULL_YEARS
(
  fdDATE_TO                            DATE,
  fdDATE_FROM                          DATE 
) return NUMBER
is
begin
  return trunc(MONTHS_BETWEEN(fdDATE_TO,fdDATE_FROM)/12);
end FULL_YEARS;
```

---

### Функция №7: D_PKG_CSE_ACCESSES.CHECK_RIGHT

```sql
-- Oracle PACKAGE: CHECK_RIGHT
-- Возвращает: return number
--======================================================================
function CHECK_RIGHT
(
  pnLPU                                in NUMBER,          --ЛПУ
  psUNITCODE                           in VARCHAR2,        --Код раздела
  pnUNIT_ID                            in NUMBER,          --ID записи в разделе
  psRIGHT                              in VARCHAR2,        --Код действия в разделе
  pnCABLAB                             in NUMBER default null, --Кабинет
  pnSERVICE                            in NUMBER default null, --Услуга
  pnRAISE                              in NUMBER default 0 --Генерировать ошибку    1 - да,0 - нет
)
return number
as
begin
  return CHECK_EMPLOYER_RIGHT(pnLPU,D_PKG_EMPLOYERS.GET_ID(pnLPU),psUNITCODE,pnUNIT_ID,psRIGHT,pnCABLAB,pnSERVICE,pnRAISE);
end CHECK_RIGHT;
```

---

### Функция №8: D_PKG_HPK_PLANS.ADD

```sql
-- Oracle PACKAGE: ADD
--======================================================================
procedure ADD
(
  pnD_INSERT_ID                        out NUMBER,
  pnLPU                                in NUMBER,
  pnPID                                in NUMBER,          --Вид плана госпитализации
  pdPLAN_DATE                          in DATE,            --Дата плана
  pnMALE_COUNT                         in NUMBER,          --Максимальное кол-во мужских мест
  pnOPER_COUNT                         in NUMBER,          --Максимальное кол-во оперативных больных
  pnGEN_COUNT                          in NUMBER           --Общее кол-во
)
is
  nCID                  D_PKG_STD.tREF;
begin
  -- Поиск каталога --
  D_PKG_HOSP_PLAN_KINDS.EXIST(pnPID, pnLPU, nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'HPK_PLANS_INSERT',null);
  CHECKS(null,
         pnPID,
         pdPLAN_DATE,
         pnMALE_COUNT,
         pnOPER_COUNT,
         pnGEN_COUNT);
  begin
    insert into D_HPK_PLANS
    (
      ID,
      LPU,
      PID,
      PLAN_DATE,
      MALE_COUNT,
      OPER_COUNT,
      GEN_COUNT
    )
      values
    (
      D_GEN_ID,
      pnLPU,
      pnPID,
      trunc(pdPLAN_DATE),
      pnMALE_COUNT,
      pnOPER_COUNT,
      pnGEN_COUNT
    ) returning ID into pnD_INSERT_ID;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'I', sqlcode);
  end;
```

---

### Функция №9: D_PKG_VMP_LINKS.UPD

```sql
-- Oracle PACKAGE: UPD
--======================================================================
procedure UPD
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnVMP_APPL                           in NUMBER,          -- Заявка в лист ожидания ВМП
  pnVMP_TALON                          in NUMBER,          -- Талоны ВМП
  pnDIRECTION                          in NUMBER,          -- Направление на госпитализацию
  vAPI_VERSION                         in NUMBER default 1 -- Версионность API
)
is
  nVERSION              D_PKG_STD.tREF;
begin
  -- Поиск версии по ЛПУ --
  D_PKG_VERSIONS.GET_VERSION_BY_LPU(1,pnLPU,'VMP_LINKS',nVERSION);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU, nVERSION, null, null, 'VMP_LINKS_UPDATE', pnID);
  begin
    update D_VMP_LINKS t set
      t.VMP_APPL                    = pnVMP_APPL,
      t.VMP_TALON                   = pnVMP_TALON,
      t.DIRECTION                   = pnDIRECTION
     where t.ID      = pnID
       and t.VERSION = nVERSION;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'U', sqlcode);
  end;
```

---

### Функция №10: D_PKG_HPK_PLAN_JOURNALS.DEL

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
  nCID                  D_PKG_STD.tREF;
  nDIR                  D_PKG_STD.tREF;
  nTMP                  D_PKG_STD.tREF;
  nCOUNT                number(2);
  nDISEASECASE          D_HPK_PLAN_JOURNALS.DISEASECASE%TYPE;
  nDIR_ID               NUMBER(17);
begin
  -- Поиск каталога --
  EXIST(pnID,pnLPU,nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'HPK_PLAN_JOURNALS_DELETE',pnID);
  begin
    select t.DIRECTION,t.DISEASECASE
      into nDIR, nDISEASECASE
      from D_HPK_PLAN_JOURNALS t
     where t.ID  = pnID
       and t.LPU = pnLPU;
  exception when NO_DATA_FOUND then
    D_PKG_MSG.RECORD_NOT_FOUND(pnID,'HPK_PLAN_JOURNALS');
  end;
```

---

### Функция №11: D_PKG_HPK_PLAN_JOURNALS.SET_RECORD

```sql
-- Oracle PACKAGE: SET_RECORD
--======================================================================
procedure SET_RECORD
(
  pnLPU                                in NUMBER,
  pnPATIENT                            in NUMBER,          --Пациент, для которого ищем первую запись
  pnEXIST                              out NUMBER,         --Записи существуют:0-нет,1-да
  pdDATE                               out DATE,           --Дата найденных записей
  pnPLAN_KIND                          out NUMBER,         --Вид плана госпитализации найденных записей
  pnHAVE_NEXT                          out NUMBER,         --Есть следущая запись:0-нет,1-да
  pnHAVE_PREV                          out NUMBER,         --Есть ли предыдущая запись : 0-нет,1-да
  pnDIRECTION                          in NUMBER default null --ID направления для поиска
)
is
  nCOUNT                D_PKG_STD.tREF;
  nHAVE_NEXT            NUMBER(17);
begin
  --Проверяем, есть ли вообще его записи с PID
  select count(1)
    into nCOUNT
    from D_HPK_PLAN_JOURNALS t
   where t.PATIENT    = pnPATIENT
     and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
     and t.LPU        = pnLPU
     and t.HPK_PLAN is not null;
  if nCOUNT < 1 then
    --если нет записей c PID вообще
    pnEXIST := 0;
    pnPLAN_KIND := null;
    pdDATE      := null;
    pnHAVE_NEXT := 0;
    pnHAVE_PREV := 0;
  elsif nCOUNT = 1 then
    --если есть только одна запись с PID
    pnEXIST := 1;
    pnHAVE_NEXT := 0;
    pnHAVE_PREV := 0;
    --Находим дату регистрации и вид плана
    select t1.PLAN_DATE,
           t1.PID
      into pdDATE,
           pnPLAN_KIND
      from D_HPK_PLAN_JOURNALS t,
           D_HPK_PLANS t1
     where t.PATIENT    = pnPATIENT
       and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
       and t.LPU        = pnLPU
       and t.HPK_PLAN   is not null
       and t1.ID        = t.HPK_PLAN;
  elsif nCOUNT > 1 then
    --если есть несколько записей с PID
    pnEXIST := 1;
    --Проверяем, на сколько дат есть записи
    nCOUNT := 0;
    select count(distinct t1.PLAN_DATE)
      into nCOUNT
      from D_HPK_PLAN_JOURNALS t,
           D_HPK_PLANS t1
     where t1.ID        = t.HPK_PLAN
       and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
       and t.PATIENT    = pnPATIENT
       and t.LPU        = pnLPU
       and t.HPK_PLAN   is not null;
    if nCOUNT = 1 then
      --Только одна дата, находим ее
      select distinct t1.PLAN_DATE
        into pdDATE
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID        = t.HPK_PLAN
         and t.PATIENT    = pnPATIENT
         and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
         and t.LPU        = pnLPU
         and t.HPK_PLAN   is not null;
      COUNT_BY_PLAN_KIND(pnLPU,0,pnPATIENT,pdDATE,null,pnPLAN_KIND,nHAVE_NEXT,pnDIRECTION);
      --Ищем pnHAVE_NEXT
      if nHAVE_NEXT = 1 then
        pnHAVE_NEXT := nHAVE_NEXT;
      elsif nHAVE_NEXT = 0 then
        nCOUNT := 0;
        select count(1)
          into nCOUNT
          from D_HPK_PLAN_JOURNALS t,
               D_HPK_PLANS t1
         where t1.ID        = t.HPK_PLAN
           and t.PATIENT    = pnPATIENT
           and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
           and t.LPU        = pnLPU
           and t.HPK_PLAN   is not null
           and t1.PLAN_DATE > pdDATE;
        if nCOUNT < 1 then
          pnHAVE_NEXT := 0;
        else
          pnHAVE_NEXT := 1;
        end if;
      end if;
      --Ищем предыдущую
      COUNT_BY_PLAN_KIND(pnLPU,2,pnPATIENT,pdDATE,null,pnPLAN_KIND,nHAVE_NEXT,pnDIRECTION);
      if nHAVE_NEXT = 1 then
        pnHAVE_PREV := nHAVE_NEXT;
      elsif nHAVE_NEXT = 0 then
        nCOUNT := 0;
        select count(1)
          into nCOUNT
          from D_HPK_PLAN_JOURNALS t,
               D_HPK_PLANS t1
         where t1.ID        = t.HPK_PLAN
           and t.PATIENT    = pnPATIENT
           and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
           and t.LPU        = pnLPU
           and t.HPK_PLAN   is not null
           and t1.PLAN_DATE < pdDATE;
        if nCOUNT < 1 then
          pnHAVE_PREV := 0;
        else
          pnHAVE_PREV := 1;
        end if;
      end if;
      return;
    elsif nCOUNT > 1 then
      pnHAVE_NEXT := 1;
      --Находим минимальную дату из всех его записей
      select MIN (t1.PLAN_DATE)
        into pdDATE
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID        = t.HPK_PLAN
         and t.PATIENT    = pnPATIENT
         and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
         and t.LPU        = pnLPU
         and t.HPK_PLAN   is not null;
      --Проверяем, сколько записей на этот день
      nCOUNT := 0;
      select count(1)
        into nCOUNT
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID         = t.HPK_PLAN
         and t1.PLAN_DATE  = pdDATE
         and t.PATIENT     = pnPATIENT
         and (t.DIRECTION  = pnDIRECTION or pnDIRECTION is null)
         and t.LPU         = pnLPU
         and t.HPK_PLAN is not null;
      if nCOUNT = 1 then
        --Если только одна запись на этот день
        select t1.PID
          into pnPLAN_KIND
          from D_HPK_PLAN_JOURNALS t,
               D_HPK_PLANS t1
         where t.PATIENT    = pnPATIENT
           and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
           and t1.PLAN_DATE = pdDATE
           and t.LPU        = pnLPU
           and t.HPK_PLAN   is not null
           and t1.ID        = t.HPK_PLAN
         group by t1.PID;
      elsif nCOUNT > 1 then
        --Если есть несколько записей на этот день
        --Ищем записи с PID
        COUNT_BY_PLAN_KIND(pnLPU,0,pnPATIENT,pdDATE,null,pnPLAN_KIND,pnHAVE_NEXT,pnDIRECTION);
      end if;
      pnHAVE_PREV := 0;
    end if;
  end if;
end SET_RECORD;
```

---

### Функция №12: D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD

```sql
-- Oracle PACKAGE: SET_NEXT_RECORD
--======================================================================
procedure SET_NEXT_RECORD
(
  pnLPU                                in NUMBER,
  pnPATIENT                            in NUMBER,          --Пациент
  pdDATE                               in DATE,            --Заданная дата
  pnPLAN_KIND                          in NUMBER,          --Заданный вид плана
  pdNEXT_DATE                          out DATE,           --Дата следущих записей
  pnNEXT_PLAN_KIND                     out NUMBER,         --Вид плана следующей записи
  pnHAVE_NEXT                          out NUMBER,         --Есть следущая запись:0-нет,1-да
  pnDIRECTION                          in NUMBER default null  --Направление
)
is
  nCOUNT                D_PKG_STD.tREF;
  dNEXT_DATE            D_HPK_PLAN_JOURNALS.REGISTER_DATE%type;
  nHAVE_NEXT            NUMBER(17);
begin
  --Проверяем есть ли записи на следущий вид плана на сегодяшний день
  nCOUNT := 0;
  dNEXT_DATE := null;
  select count (distinct t.PID)
    into nCOUNT
    from D_HPK_PLANS t,
         D_HPK_PLAN_JOURNALS t1
   where t1.PATIENT        = pnPATIENT
     and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
     and t.PLAN_DATE       = pdDATE
     and t1.HPK_PLAN is not null
     and t1.HPK_PLAN       = t.ID
     and t1.LPU            = pnLPU
     and t.PID             > pnPLAN_KIND;
  if nCOUNT < 1 then
    --Нет таких, берем следущую дату
    select MIN (t1.PLAN_DATE)
      into dNEXT_DATE
      from D_HPK_PLAN_JOURNALS t,
           D_HPK_PLANS t1
     where t1.ID        = t.HPK_PLAN
       and t.PATIENT    = pnPATIENT
       and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
       and t.LPU        = pnLPU
       and t.HPK_PLAN   is not null
       and t1.PLAN_DATE > pdDATE;
  else
    pdNEXT_DATE := pdDATE;
    COUNT_BY_PLAN_KIND(pnLPU,1,pnPATIENT,pdNEXT_DATE,pnPLAN_KIND,pnNEXT_PLAN_KIND,nHAVE_NEXT,pnDIRECTION);
    --Находим pnHAVE_NEXT
    if nHAVE_NEXT = 1 then
      pnHAVE_NEXT := nHAVE_NEXT;
    elsif nHAVE_NEXT = 0 then
      nCOUNT := 0;
      select count(1)
        into nCOUNT
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID        = t.HPK_PLAN
         and t.PATIENT    = pnPATIENT
         and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
         and t.LPU        = pnLPU
         and t.HPK_PLAN   is not null
         and t1.PLAN_DATE > pdNEXT_DATE;
      if nCOUNT < 1 then
        pnHAVE_NEXT := 0;
      else
        pnHAVE_NEXT := 1;
      end if;
    end if;
    return;
  end if;
  --Если ищем на следущую дату
  if dNEXT_DATE is not null then
    pdNEXT_DATE := dNEXT_DATE;
    --Считаем кол-во разных видов плана
    select count (distinct t.PID)
      into nCOUNT
      from D_HPK_PLANS t,
           D_HPK_PLAN_JOURNALS t1
     where t1.PATIENT        = pnPATIENT
       and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
       and t.PLAN_DATE       = dNEXT_DATE
       and t1.HPK_PLAN is not null
       and t1.HPK_PLAN       = t.ID
       and t1.LPU            = pnLPU;
    if nCOUNT = 1 then
      --Если только один вид плана на нашу дату
      --Определяем pnHAVE_NEXT
      select count (t1.PLAN_DATE)
        into nCOUNT
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID        = t.HPK_PLAN
         and t.PATIENT    = pnPATIENT
         and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
         and t.LPU        = pnLPU
         and t.HPK_PLAN   is not null
         and t1.PLAN_DATE > dNEXT_DATE;
      if nCOUNT < 1 then
        pnHAVE_NEXT := 0;
      elsif nCOUNT >= 1 then
        pnHAVE_NEXT := 1;
      end if;
      --Тащим вид плана
      select t.PID
        into pnNEXT_PLAN_KIND
        from D_HPK_PLANS t,
             D_HPK_PLAN_JOURNALS t1
       where t1.PATIENT        = pnPATIENT
         and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
         and t.PLAN_DATE       = dNEXT_DATE
         and t1.HPK_PLAN is not null
         and t1.HPK_PLAN       = t.ID
         and t1.LPU            = pnLPU
       group by t.PID;
    elsif nCOUNT > 1 then
      COUNT_BY_PLAN_KIND(pnLPU,0,pnPATIENT,pdNEXT_DATE,null,pnNEXT_PLAN_KIND,pnHAVE_NEXT,pnDIRECTION);
    end if;
  end if;
end SET_NEXT_RECORD;
```

---

### Функция №13: D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD

```sql
-- Oracle PACKAGE: SET_PREV_RECORD
--======================================================================
procedure SET_PREV_RECORD
(
  pnLPU                                in NUMBER,
  pnPATIENT                            in NUMBER,          --Пациент
  pdDATE                               in DATE,            --Заданная дата
  pnPLAN_KIND                          in NUMBER,          --Заданный вид плана
  pdPREV_DATE                          out DATE,           --Дата предыдущих записей
  pnPREV_PLAN_KIND                     out NUMBER,         --Вид плана предыдущей записи
  pnHAVE_PREV                          out NUMBER,         --Есть предыдущая запись:0-нет,1-да
  pnDIRECTION                          in NUMBER default null --Направление
)
is
  nCOUNT                D_PKG_STD.tREF;
  nFLAG                 D_PKG_STD.tREF;
  dPREV_DATE            D_HPK_PLAN_JOURNALS.REGISTER_DATE%type;
  nHAVE_PREV            NUMBER(17);
begin
  --Проверяем есть ли записи на предыдущий вид плана на сегодяшний день
  nCOUNT := 0;
  dPREV_DATE := null;
  select count (distinct t.PID)
    into nCOUNT
    from D_HPK_PLANS t,
         D_HPK_PLAN_JOURNALS t1
   where t1.PATIENT        = pnPATIENT
     and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
     and t.PLAN_DATE       = pdDATE
     and t1.HPK_PLAN is not null
     and t1.HPK_PLAN       = t.ID
     and t1.LPU            = pnLPU
     and t1.DIRECTED_BY is null
     and t.PID             < pnPLAN_KIND;
  if nCOUNT < 1 then
    --Нет таких, берем следущую дату
    nFLAG := 1;
    select MAX (t1.PLAN_DATE)
      into dPREV_DATE
      from D_HPK_PLAN_JOURNALS t,
           D_HPK_PLANS t1
     where t1.ID        = t.HPK_PLAN
       and t.PATIENT    = pnPATIENT
       and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
       and t.LPU        = pnLPU
       and t.HPK_PLAN   is not null
       and t1.PLAN_DATE < pdDATE;
  else
    nFLAG := 0;
    pdPREV_DATE := pdDATE;
    COUNT_BY_PLAN_KIND(pnLPU,2,pnPATIENT,pdPREV_DATE,pnPLAN_KIND,pnPREV_PLAN_KIND,nHAVE_PREV,pnDIRECTION);
    --Ищем pnHAVE_PREV
    if nHAVE_PREV = 1 then
      pnHAVE_PREV := nHAVE_PREV;
    elsif nHAVE_PREV = 0 then
      nCOUNT := 0;
      select count(1)
        into nCOUNT
        from D_HPK_PLAN_JOURNALS t,
             D_HPK_PLANS t1
       where t1.ID        = t.HPK_PLAN
         and t.PATIENT    = pnPATIENT
         and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
         and t.LPU        = pnLPU
         and t.HPK_PLAN   is not null
         and t1.PLAN_DATE < pdPREV_DATE;
      if nCOUNT < 1 then
        pnHAVE_PREV := 0;
      else
        pnHAVE_PREV := 1;
      end if;
    end if;
    return;
  end if;
  --Если ищем на следущую дату
  if nFLAG = 1 then
    if dPREV_DATE is not null then
      pdPREV_DATE := dPREV_DATE;
      --Считаем кол-во разных видов плана
      select count (distinct t.PID)
        into nCOUNT
        from D_HPK_PLANS t,
             D_HPK_PLAN_JOURNALS t1
       where t1.PATIENT        = pnPATIENT
         and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
         and t.PLAN_DATE       = dPREV_DATE
         and t1.HPK_PLAN is not null
         and t1.HPK_PLAN       = t.ID
         and t1.LPU            = pnLPU;
      if nCOUNT = 1 then
        --Если только один вид плана на нашу дату
        --Определяем pnHAVE_PREV
        select count (t1.PLAN_DATE)
          into nCOUNT
          from D_HPK_PLAN_JOURNALS t,
               D_HPK_PLANS t1
         where t1.ID        = t.HPK_PLAN
           and t.PATIENT    = pnPATIENT
           and (t.DIRECTION = pnDIRECTION or pnDIRECTION is null)
           and t.LPU        = pnLPU
           and t.HPK_PLAN   is not null
           and t1.PLAN_DATE < dPREV_DATE;
        if nCOUNT < 1 then
          pnHAVE_PREV := 0;
        elsif nCOUNT >= 1 then
          pnHAVE_PREV := 1;
        end if;
        --Тащим вид плана
        select t.PID
          into pnPREV_PLAN_KIND
          from D_HPK_PLANS t,
               D_HPK_PLAN_JOURNALS t1
         where t1.PATIENT        = pnPATIENT
           and (t1.DIRECTION     = pnDIRECTION or pnDIRECTION is null)
           and t.PLAN_DATE       = dPREV_DATE
           and t1.HPK_PLAN is not null
           and t1.HPK_PLAN       = t.ID
           and t1.LPU            = pnLPU
         group by t.PID;
      elsif nCOUNT > 1 then
        COUNT_BY_PLAN_KIND(pnLPU,3,pnPATIENT,pdPREV_DATE,null,pnPREV_PLAN_KIND,nHAVE_PREV,pnDIRECTION);
        --Ищем pnHAVE_PREV
        if nHAVE_PREV = 1 then
          pnHAVE_PREV := nHAVE_PREV;
        elsif nHAVE_PREV = 0 then
          nCOUNT := 0;
          select count(1)
            into nCOUNT
            from D_HPK_PLAN_JOURNALS t,
                 D_HPK_PLANS t1
           where t1.ID         = t.HPK_PLAN
             and t.PATIENT     = pnPATIENT
             and (t.DIRECTION  = pnDIRECTION or pnDIRECTION is null)
             and t.LPU         = pnLPU
             and t.HPK_PLAN    is not null
             and t1.PLAN_DATE  < pdPREV_DATE;
          if nCOUNT < 1 then
            pnHAVE_PREV := 0;
          else
            pnHAVE_PREV := 1;
          end if;
        end if;
        return;
      end if;
    elsif dPREV_DATE is null then
      pnHAVE_PREV := 0;
    end if;
  end if;
end SET_PREV_RECORD;
```

---

### Функция №14: D_PKG_HPK_PLAN_JOURNALS.SET_HH_DIRECTION_DATE

```sql
-- Oracle PACKAGE: SET_HH_DIRECTION_DATE
--======================================================================
procedure SET_HH_DIRECTION_DATE
(
  pnID                                 in NUMBER
)
is
  dHH_DIRECTION_DATE    D_HPK_PLAN_JOURNALS.HH_DIRECTION_DATE%type;
begin
  select t.HH_DIRECTION_DATE
    into dHH_DIRECTION_DATE
    from D_HPK_PLAN_JOURNALS t
   where t.ID    = pnID;
  if dHH_DIRECTION_DATE is null then
    update D_HPK_PLAN_JOURNALS t set
      t.HH_DIRECTION_DATE       = sysdate
     where t.ID                 = pnID;
  elsif dHH_DIRECTION_DATE is not null then
    update D_HPK_PLAN_JOURNALS t set
      t.HH_DIRECTION_DATE       = null
     where t.ID                 = pnID;
  end if;
end SET_HH_DIRECTION_DATE;
```

---

### Функция №15: D_PKG_HPK_PLAN_JOURNALS.NEAREST_DATE_SEARCH

```sql
-- Oracle PACKAGE: NEAREST_DATE_SEARCH
-- Возвращает: return DATE
--======================================================================
function NEAREST_DATE_SEARCH
(
  pnLPU                                in NUMBER,
  pnHPK                                in NUMBER,
  pdSTART_DATE                         in DATE,
  pnSEX                                in NUMBER, --0-женский;1-мужской;null-неважно
  pnOPER_TYPE                          in NUMBER  --0-консервативный;1-оперативный;null-неважно
) return DATE
is
  nGEN_COUNT            NUMBER(10);
  nMALE_COUNT           NUMBER(10);
  nFEMALE_COUNT         NUMBER(10);
  nOPER_COUNT           NUMBER(10);
  nCON_COUNT            NUMBER(10);
  dPLAN_DATE            DATE;
  nSTATUS               NUMBER(1);
  dEND_DATE             DATE;
  nSEX_FLAG             NUMBER(1);
  nOPER_FLAG            NUMBER(1);
begin
  select nvl(max(PLAN_DATE),pdSTART_DATE)
    into dEND_DATE
    from D_HPK_PLANS
   where LPU = pnLPU
     and PID = pnHPK;
  if trunc(pdSTART_DATE) >= trunc(sysdate) then
    dPLAN_DATE := trunc(pdSTART_DATE);
  else
    dPLAN_DATE := trunc(sysdate);
  end if;
  loop
    nSEX_FLAG := 0;
    nOPER_FLAG := 0;
    GET_INFO
    (
      pnLPU,
      pnHPK,
      dPLAN_DATE,
      0,
      nGEN_COUNT,
      nOPER_COUNT,
      nCON_COUNT,
      nMALE_COUNT,
      nFEMALE_COUNT,
      nSTATUS
    );
    if nSTATUS = 1 then
     --Проверка пола
      if pnSEX = 0 and (nFEMALE_COUNT > 0 or nFEMALE_COUNT is null and nGEN_COUNT > 0)  then
        nSEX_FLAG := 1;
      elsif  pnSEX = 1 and (nMALE_COUNT > 0 or nMALE_COUNT is null and nGEN_COUNT > 0)  then
        nSEX_FLAG := 1;
      elsif  pnSEX is null and nGEN_COUNT > 0 then
        nSEX_FLAG := 1;
      end if;
      --Проверка оперативности
      if pnOPER_TYPE = 0 and (nCON_COUNT > 0 or nCON_COUNT is null and nGEN_COUNT > 0)  then
        nOPER_FLAG := 1;
      elsif  pnOPER_TYPE = 1 and (nOPER_COUNT > 0 or nOPER_COUNT is null and nGEN_COUNT > 0)  then
        nOPER_FLAG := 1;
      elsif  pnOPER_TYPE is null and nGEN_COUNT > 0 then
        nOPER_FLAG := 1;
      end if;
      if  nOPER_FLAG = 1 and nSEX_FLAG = 1 then
        return trunc(dPLAN_DATE);
      end if;
    end if;
    exit when dPLAN_DATE > dEND_DATE;
    dPLAN_DATE := dPLAN_DATE + 1;
  end loop;
  return null;
end;
```

---

### Функция №16: D_PKG_HPK_PLAN_JOURNALS.GET_QUANT_BEDS_PROFILES_NEW

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №17: D_PKG_CATALOGS.FIND_ROOT_CATALOG

```sql
-- Oracle PACKAGE: FIND_ROOT_CATALOG
--======================================================================
procedure FIND_ROOT_CATALOG
(
  pnRAISE                              in NUMBER,          --1 отобразить сообщение при ошибке, 0 - не сообщать
  pnLPU                                in NUMBER,          --ЛПУ
  psUNITCODE                           in VARCHAR2,        --Раздел
  pnCATALOG                            out NUMBER          --ID найденного каталога
)
is
  nVERSION              D_PKG_STD.tREF;
  nVER_LPU              D_UNITLIST.VER_LPU%type;
begin
  nVER_LPU := D_PKG_UNITLIST.GET_PARAM(psUNITCODE, 'VER_LPU');
  begin
    if nVER_LPU = 0 then
      D_PKG_VERSIONS.GET_VERSION_BY_LPU(1, pnLPU, psUNITCODE, nVERSION);
      select t.ID
       into pnCATALOG
       from D_CATALOGS t
      where t.C_UNITCODE = psUNITCODE
        and t.VERSION    = nVERSION
        and t.LPU        is null
        and t.C_LEVEL    = 0;
    elsif nVER_LPU = 1 then
      select t.ID
        into pnCATALOG
        from D_CATALOGS t
       where t.C_UNITCODE = psUNITCODE
         and t.LPU        = pnLPU
         and t.VERSION    is null
         and t.C_LEVEL    = 0;
    else
      select t.ID
        into pnCATALOG
        from D_CATALOGS t
       where t.C_UNITCODE = psUNITCODE
         and t.LPU        is null
         and t.VERSION    is null
         and t.C_LEVEL    = 0;
    end if;
  exception when NO_DATA_FOUND then
    if pnRAISE = 1 then
      D_P_EXC('2.1. Не найден корневой каталог для раздела "'||psUNITCODE||'".');
    else
      pnCATALOG := null;
    end if;
  when TOO_MANY_ROWS then
    if pnRAISE = 1 then
      D_P_EXC('2.2. Найдено более одного корневого каталога для раздела "'||psUNITCODE||'".');
    else
      pnCATALOG := null;
    end if;
  end;
```

---

### Функция №18: D_PKG_URPRIVS.GET_STANDART_PRIVS

```sql
-- Oracle PACKAGE: GET_STANDART_PRIVS
--======================================================================
procedure GET_STANDART_PRIVS
(
  pnLPU                                in NUMBER,
  psUNITCODE                           in VARCHAR2,
  pnCID                                in NUMBER,
  pnINSERT                             out INTEGER,
  pnUPDATE                             out INTEGER,
  pnDELETE                             out INTEGER,
  pnMOVE_OUT                           out INTEGER
)
is
  iRESULT               INTEGER;
  nVERSION              D_PKG_STD.tREF;
  rUNIT                 D_UNITLIST%rowtype;
  nUSER                 D_USERS.ID%type;
begin
  pnINSERT   := 0;
  pnUPDATE   := 0;
  pnDELETE   := 0;
  pnMOVE_OUT := 0;
  -- считывание параметров раздела
  begin
    select UNITCODE,USE_CATALOGS, VER_LPU
      into rUNIT.UNITCODE,rUNIT.USE_CATALOGS,rUNIT.VER_LPU
      from D_UNITLIST
      where UNITCODE = psUNITCODE;
  exception when NO_DATA_FOUND then
    D_PKG_MSG.UNIT_NOT_FOUND(psUNITCODE);
  end;
```

---

### Функция №19: D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS

```sql
-- Oracle PACKAGE: SET_DISCARD_STATUS
--======================================================================
procedure SET_DISCARD_STATUS
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnDISCARD_STATUS                     in NUMBER
)
is
  dDATE_OUT             DATE;
  dDATE_IN              DATE;
  sHH_NUMB              D_HOSP_HISTORIES.HH_NUMB%type;
  sHH_PREF              D_HOSP_HISTORIES.HH_PREF%type;
  sHH_NUMB_ALTERN       D_HOSP_HISTORIES.HH_NUMB_ALTERN%type;
  sHH_NUMB_TYPE         D_HOSP_HISTORIES.HH_NUMB_TYPE%type;
  nPATIENT              D_HOSP_HISTORIES.PATIENT%type;
  nHPK_PLAN_JOURNAL     D_HOSP_HISTORIES.HPK_PLAN_JOURNAL%type;
  nFND                  NUMBER;
  nHSC_RESULT           NUMBER(1);
  sHH_NUMB_MASK         D_HOSP_HISTORIES.HH_NUMB_MASK%type;
  sHH_NUMB_FULL         D_HOSP_HISTORIES.HH_NUMB_FULL%type;
  nHAVE_HSC             NUMBER(1);
  rHH                   D_HOSP_HISTORIES%rowtype;
  nRELATIVE_HH          NUMBER(17);
  nDISEASECASE          NUMBER(17);
begin
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,null,null,'HOSP_HISTORIES_SET_DISCART_STATUS',pnID);
  -- Проверка, выписана ли ИБ
  select hh.DATE_IN,hh.DATE_OUT, hh.HH_NUMB, hh.HH_PREF, hh.HH_NUMB_ALTERN, hh.HH_NUMB_TYPE, hh.PATIENT, hh.HPK_PLAN_JOURNAL, hh.HH_NUMB_MASK,
         (select count(1) from D_HOSP_STAT_CARDS t where t.HOSP_HISTORY = hh.ID and t.LPU = pnLPU), hh.RELATIVE_HH, hh.DISEASECASE
    into dDATE_IN,  dDATE_OUT,   sHH_NUMB,   sHH_PREF, sHH_NUMB_ALTERN,  sHH_NUMB_TYPE,   nPATIENT, nHPK_PLAN_JOURNAL, sHH_NUMB_MASK, nHAVE_HSC, nRELATIVE_HH,nDISEASECASE
    from D_HOSP_HISTORIES hh
   where hh.ID = pnID;
  if pnDISCARD_STATUS in (1,2) then
    select count(1)
      into nFND
      from D_HP_MEAS_PRESCS mp,
           D_HOSP_HISTORIES hh
     where hh.id = pnID
       and mp.DISEASECASE = hh.DISEASECASE
       and mp.MP_CONDITION in (0,1,2)
       and rownum = 1;
    if nFND = 1 then
      if pnDISCARD_STATUS = 1 then
        D_P_EXC('5. Запрещено аннулировать историю болезни. У пациента есть действующие измерения.Для аннулирования ИБ необходимо сначала отменить назначенные измерения.');
      elsif pnDISCARD_STATUS = 2 then
        D_P_EXC('5.1. Запрещено направлять на списание историю болезни. У пациента есть действующие измерения. Необходимо сначала отменить назначенные измерения.');
      end if;
    end if;
  end if;
  if dDATE_OUT is not null then
    if pnDISCARD_STATUS = 1 then
      D_P_EXC('1.0 Запрещено аннулировать выписанную историю болезни.');
    elsif pnDISCARD_STATUS = 2 then
      D_P_EXC('1.1 Запрещено направлять на списание выписанную историю болезни.');
    end if;
  elsif nHAVE_HSC != 0 and nvl(D_PKG_OPTIONS.GET('HSCIdenticalToHH',pnLPU),0) = 0 then
    if pnDISCARD_STATUS = 1 then
      D_P_EXC('1.2 Запрещено аннулировать историю болезни, по которой создана статкарта.');
    elsif pnDISCARD_STATUS = 2 then
      D_P_EXC('1.3 Запрещено направлять на списание историю болезни, по которой создана статкарта.');
    end if;
  else
    -- Аннулируем ИБ сопровождающих
    for y in (select * from D_HOSP_HISTORIES t where t.RELATIVE_HH = pnID)
    loop
      SET_DISCARD_STATUS(y.ID,y.LPU,pnDISCARD_STATUS);
    end loop;
    if pnDISCARD_STATUS in (1,2) then -- 15.09.2011 из-за проблемы с уникальным номером при аннулировании (статус 1)--
      -- Изменим номер для ИБ аннулированных под одним номером
      select count(1)
        into nFND
        from D_HOSP_HISTORIES hh
       where hh.LPU            = pnLPU
         and hh.DISCARD_STATUS = pnDISCARD_STATUS
         and hh.HH_PREF        = sHH_PREF
         and (hh.HH_NUMB_TYPE  = sHH_NUMB_TYPE or hh.HH_NUMB_TYPE is null and sHH_NUMB_TYPE is null)
         and hh.HH_NUMB        = sHH_NUMB
         and rownum            = 1;
      if nFND <> 0 then
        if instr(sHH_NUMB,'#') > 0 then
          sHH_NUMB := substr(sHH_NUMB,1,instr(sHH_NUMB,'#') -1);
        end if;
        select nvl(max(to_number(replace(hh.HH_NUMB,sHH_NUMB||'#'))),-1)
          into nFND
          from D_HOSP_HISTORIES hh
         where hh.LPU            = pnLPU
           and hh.DISCARD_STATUS = pnDISCARD_STATUS
           and hh.HH_PREF        = sHH_PREF
           and (hh.HH_NUMB_TYPE  = sHH_NUMB_TYPE or hh.HH_NUMB_TYPE is null and sHH_NUMB_TYPE is null)
           and hh.HH_NUMB        like sHH_NUMB||'#%'
           and ltrim(replace(hh.HH_NUMB,sHH_NUMB||'#'),'0123456789') is null;
        sHH_NUMB := sHH_NUMB||'#'||to_char(nFND + 1);
      end if;
    end if;
    -- Проверка, госпитализирован ли данный пациент
    if pnDISCARD_STATUS = 0 then
      select count(*)
        into nFND
        from D_HOSP_HISTORIES hh
       where hh.PATIENT        = nPATIENT
         and hh.ID            != pnID
         and hh.DISCARD_STATUS = 0
         and D_PKG_DAT_TOOLS.DATE_RANGES_ERROR(hh.DATE_IN,hh.DATE_OUT,dDATE_IN,dDATE_OUT,1) != 0
         and rownum = 1;
      if nFND > 0 then
        D_P_EXC('3. Данный пациент уже госпитализирован.');
      end if;
      select count(*)
        into nFND
        from D_HOSP_HISTORIES hh
       where hh.HPK_PLAN_JOURNAL = nHPK_PLAN_JOURNAL
         and hh.DISCARD_STATUS   = 0
         and hh.ID              != pnID
         and not exists (select null                       -- Исключаем сопровождаемого
                           from D_HOSP_HISTORIES hh1
                          where hh1.HPK_PLAN_JOURNAL = nHPK_PLAN_JOURNAL
                            and hh1.RELATIVE_HH = hh.ID)
         and not exists (select null                       -- Исключаем сопровождающих
                           from D_HOSP_HISTORIES hh1
                          where hh1.HPK_PLAN_JOURNAL = nHPK_PLAN_JOURNAL
                            and hh1.ID = hh.RELATIVE_HH)   
         and rownum = 1;
      if nFND > 0 then
        D_P_EXC('4. По данному направлению пациент уже госпитализирован.');
      end if;
    end if;
    sHH_NUMB_FULL := GET_HH_NUMB_FULL(sHH_PREF, sHH_NUMB, sHH_NUMB_ALTERN, sHH_NUMB_MASK);
    select count(1)
      into nFND
      from D_HOSP_HISTORIES hh
           join D_HOSP_HISTORY_DEPS hhd on hhd.PID = hh.ID
           join D_MP_PRESCRIBES mp on mp.HH_DEP = hhd.ID
     where hh.ID  = pnID
       and hh.LPU = pnLPU
       and mp.MP_CONDITION in (2,3);
    if nFND > 0 and pnDISCARD_STATUS = 1 then
      D_P_EXC('5. У пациента есть исполненные лекарственные назначения. Аннулирование запрещено. Для аннулирования ИБ необходимо сначала отменить исполнение лекарственных назначений');
    end if;
    update D_HOSP_HISTORIES t set
        t.DISCARD_STATUS = pnDISCARD_STATUS,
        t.HH_NUMB        = sHH_NUMB,
        t.HH_NUMB_FULL   = sHH_NUMB_FULL
     where t.ID  = pnID
       and t.LPU = pnLPU;
    if pnDISCARD_STATUS in (1,2) then
      CLEAR_DEP_BEDS(pnID,pnLPU);
      if pnDISCARD_STATUS = 1 then
        --Отмена услуг
        for r in (select t2.ID , t3.SE_NAME
                    from D_HOSP_HISTORIES      t1,
                         D_HOSP_HISTORY_DEPS   t4,
                         D_DIRECTION_SERVICES  t2,
                         D_SERVICES            t3
                   where t1.ID  = pnID
                     and t1.LPU = pnLPU
                     and t2.HH_DEP = t4.ID
                     and t4.PID = t1.ID
                     and t3.ID = t2.SERVICE
                  union all
                  select t22.ID , t33.SE_NAME
                    from D_DIRECTION_SERVICES  t22,
                         D_SERVICES            t33
                   where t22.LPU = pnLPU
                     and t22.DISEASECASE = nDISEASECASE
                     and t22.HH_DEP is null
                     and t33.ID = t22.SERVICE)
        loop
          begin
            D_PKG_DIRECTION_SERVICES.CANCEL(pnLPU,r.ID,sysdate,'Аннулирование ИБ');
          exception when others then D_P_EXC('2. При аннулировании истории болезни отмена оказанных услуг произошла с ошибкой: '||
                                             D_PKG_MSG.GET_ERROR(sqlerrm)||chr(10)||
                                             'Услуга: '||r.SE_NAME|| ' '|| r.ID);
          end;
```

---

### Функция №20: D_PKG_WLH_REQUESTS.UPD_BY_HH_SET_DISCARD

```sql
-- Oracle PACKAGE: UPD_BY_HH_SET_DISCARD
--======================================================================
procedure UPD_BY_HH_SET_DISCARD
(
  pnHH_ID                              in NUMBER,         -- Ссылка на ИБ
  pnLPU                                in NUMBER
)
is
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS)
      into nWHL_ID,
           nCURRENT_STATUS_CODE
      from D_HOSP_HISTORIES hh
           join D_WLH_REQUESTS req on req.HPK_PLAN_JOURNAL = hh.HPK_PLAN_JOURNAL
     where hh.ID = pnHH_ID;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №21: D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE_DISCARD

```sql
-- Oracle PACKAGE: UPD_BY_HH_REMOVE_DISCARD
--======================================================================
procedure UPD_BY_HH_REMOVE_DISCARD
(
  pnHH_ID                              in NUMBER,         -- Ссылка на ИБ
  pnLPU                                in NUMBER
)
is
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS)
      into nWHL_ID,
           nCURRENT_STATUS_CODE
      from D_HOSP_HISTORIES hh
           join D_WLH_REQUESTS req on req.HPK_PLAN_JOURNAL = hh.HPK_PLAN_JOURNAL
     where hh.ID = pnHH_ID;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №22: D_PKG_HOSP_HISTORIES.CHECK_HOSP_ONE_TIME

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №23: D_PKG_WLH_REQUESTS.UPD_BY_HH_ADD

```sql
-- Oracle PACKAGE: UPD_BY_HH_ADD
--======================================================================
procedure UPD_BY_HH_ADD
(
  pnHH_ID                              in NUMBER,          -- Ссылка на ИБ
  pnLPU                                in NUMBER
)
is
  nVERSION              D_PKG_STD.tREF;
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
  nSTATUS               D_PKG_STD.tREF;
  nDEPARTMENT_ID        D_PKG_STD.tREF;
  dHOSP_DATE            D_WLH_REQUESTS.HOSP_DATE%type;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS),
           hh.DEPARTMENT_ID,
           hh.DATE_IN
      into nWHL_ID,
           nCURRENT_STATUS_CODE,
           nDEPARTMENT_ID,
           dHOSP_DATE
      from D_HOSP_HISTORIES hh
           join D_WLH_REQUESTS req on req.HPK_PLAN_JOURNAL = hh.HPK_PLAN_JOURNAL
     where hh.ID = pnHH_ID;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №24: D_PKG_WLH_REQUESTS.UPD_BY_HH_CANCEL

```sql
-- Oracle PACKAGE: UPD_BY_HH_CANCEL
--======================================================================
procedure UPD_BY_HH_CANCEL
(
  pnHPK_PLAN_JOURNAL                   in NUMBER,          -- Ссылка на запись в ЖГ
  pnLPU                                in NUMBER
)
is
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS)
      into nWHL_ID,
           nCURRENT_STATUS_CODE
      from D_WLH_REQUESTS req
     where req.HPK_PLAN_JOURNAL = pnHPK_PLAN_JOURNAL;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №25: D_PKG_HPK_SCHEDULE_REG.DEL

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
begin
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU, null, null, null, 'HPK_SCHEDULE_REG_DELETE', pnID);
  begin
    delete D_HPK_SCHEDULE_REG hsr
     where hsr.ID = pnID;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```

---

### Функция №26: D_PKG_TOOLS.STR_SEPARATE_TO_IDS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №27: D_PKG_SMP_CALL_EX_SYSTEM.SET_HOSP_HISTORY

```sql
-- Oracle PACKAGE: SET_HOSP_HISTORY
--======================================================================
procedure SET_HOSP_HISTORY
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnHH_ID                              in NUMBER,
  pnHPK_ID                             in NUMBER   
)
is
  nVERSION              D_PKG_STD.tREF;
begin
  -- Поиск версии по ЛПУ --
  D_PKG_VERSIONS.GET_VERSION_BY_LPU(1, pnLPU, 'SMP_CALL_EX_SYSTEM', nVERSION);
  begin
    update D_SMP_CALL_EX_SYSTEM t set 
      t.HH_ID     = pnHH_ID,
      t.HPK_ID    = pnHPK_ID
     where t.ID = pnID
       and t.VERSION = nVERSION;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'U', sqlcode);
  end;
```

---

### Функция №28: D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE

```sql
-- Oracle PACKAGE: UPD_BY_HH_REMOVE
--======================================================================
procedure UPD_BY_HH_REMOVE
(
  pnHH_ID                              in NUMBER,          -- Ссылка на ИБ
  pnLPU                                in NUMBER
)
is
  nVERSION              D_PKG_STD.tREF;
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
  nSTATUS               D_PKG_STD.tREF;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS)
      into nWHL_ID,
           nCURRENT_STATUS_CODE
      from D_HOSP_HISTORIES hh
           join D_WLH_REQUESTS req on req.HPK_PLAN_JOURNAL = hh.HPK_PLAN_JOURNAL
     where hh.ID = pnHH_ID;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №29: D_PKG_HOSP_HISTORIES.DEL

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
  nHSC_RESULT           NUMBER(1);
  nHPK_PLAN_JOURNAL     NUMBER(17);
  nEM_DIRECTION         NUMBER(17);
  nDIRECTION            NUMBER(17);
  nCNT                  NUMBER;
  nRELATIVE_HH          NUMBER(17);
  nDISEASECASE          NUMBER(17);
  dDATE_IN              DATE;
  rDC                   D_DISEASECASES%rowtype;
  nAT                   NUMBER(17);
  sDC_CONTENT           VARCHAR2(200);
  nMKB                  NUMBER(17);
  dREGISTER_DATE        DATE;
  sDS_NAME              D_DIAGNOSIS_STAGES.DS_NAME%type;
  nSTAGE                NUMBER(17);
  nLAST_VIS             NUMBER(17);
  nHH_TYPE              D_HOSP_HISTORIES.HH_TYPE%type;
  rAGN_PREG             D_AGENT_PREGNANCY%rowtype;
  nBIRTH_LPU            D_PKG_STD.tREF;
  nLINK                 D_PKG_STD.tREF;
begin
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,null,null,'HOSP_HISTORIES_DELETE',pnID);
  CHECKS
  (
    'DEL',
    pnID,
    pnLPU,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
  );
  -- Если необходимо, удаляем статкарту
  if D_PKG_OPTION_SPECS.GET('HSCIdenticalToHH',pnLPU) = 1 or D_PKG_OPTION_SPECS.GET('HSCCreateFromHH',pnLPU) = 1 then
    nHSC_RESULT := D_PKG_HOSP_STAT_CARDS.UPD_HSC_BY_HOSP_HISTORY(
      pnID,pnLPU,'DEL',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
      null,null,null,null,null,null);
  end if;
  -- Если у пациента заведены ИБ сопровождающих, то сначала удалим их
  for r in(select t.* from D_HOSP_HISTORIES t where t.RELATIVE_HH = pnID)
  loop
    DEL(r.ID,pnLPU);
  end loop;
  select t.DISEASECASE, t.DATE_IN
    into nDISEASECASE, dDATE_IN
    from D_HOSP_HISTORIES t
   where t.ID          = pnID
     and t.LPU         = pnLPU;
  -- Если имеются услуги, оказанные после даты госпитализации, то выводим ошибку
  select count(1)
    into nCNT
    from D_DIRECTION_SERVICES ds,
         D_VISITS v,
         D_HOSP_HISTORY_DEPS hhd
   where ds.ID = v.PID
     and ds.HH_DEP = hhd.ID
     and ds.DISEASECASE = nDISEASECASE
     and v.VISIT_DATE  >= dDATE_IN
     and hhd.PID = pnID;
  if nCNT != 0 then
    D_P_EXC('Невозможно отменить госпитализацию, т.к. у пациента есть услуги, оказанные после госпитализации. Для отмены госпитализации необходимо отменить оказание услуг, дата оказания которых больше или равна дате поступления в стационар.');
  end if;
  -- Удаление дочерних записей
  for crDEP in (select hhd.ID
                  from D_HOSP_HISTORY_DEPS hhd
                 where hhd.PID = pnID
                   and hhd.LPU = pnLPU)
  loop
    for crHHD_B in (select h.ID
                      from D_HH_DEP_BEDS h
                     where h.PID = crDEP.ID)
    loop
      D_PKG_HH_DEP_BEDS.DEL(crHHD_B.ID, pnLPU);
    end loop;
    for crHHD_H in (select h.ID
                      from D_HH_DEP_HEALERS h
                     where h.PID = crDEP.ID)
    loop
      D_PKG_HH_DEP_HEALERS.DEL(crHHD_H.ID, pnLPU);
    end loop;
    D_PKG_HOSP_HISTORY_DEPS.DEL(crDEP.ID, pnLPU);
  end loop;
  for crHHA in (select j.ID
                  from D_HOSP_HISTORY_ARCHS j
                 where j.PID = pnID
                   and j.LPU = pnLPU
                 order by j.ARCH_DATE desc, j.ID desc)
  loop
    D_PKG_HOSP_HISTORY_ARCHS.DEL(crHHA.ID, pnLPU);
  end loop;
  for crDG in (select t.ID
                 from D_HOSP_HISTORY_DIAGNS t
                where t.PID = pnID
                  and t.LPU = pnLPU)
  loop
    D_PKG_HOSP_HISTORY_DIAGNS.DEL(crDG.ID, pnLPU);
  end loop;
  for crHHSK in (select hhsk.ID
                   from D_HH_STREETKIDS hhsk
                  where hhsk.PID = pnID
                    and hhsk.LPU = pnLPU)
  loop
    D_PKG_HH_STREETKIDS.DEL(crHHSK.ID,pnLPU);
  end loop;
  for crLPAH in (select l.ID LINK_ID, l.OUT_UNIT_ID ID
                   from D_LINKS l
                  where l.IN_UNIT = 'HOSP_HISTORIES'
                    and l.IN_UNIT_ID = pnID
                    and l.OUT_UNIT = 'AGENT_PSY_ANAMN_HOSP')
  loop
    D_PKG_LINKS.DEL(crLPAH.LINK_ID, pnLPU);
    D_PKG_AGENT_PSY_ANAMN_HOSP.DEL(crLPAH.ID, pnLPU);
  end loop;
  for crHHDIET in (select hhdiet.ID
                     from D_HH_DIETARIES hhdiet
                    where hhdiet.PID = pnID
                      and hhdiet.LPU = pnLPU)
  loop
    D_PKG_HH_DIETARIES.DEL(crHHDIET.ID, pnLPU);
  end loop;
  D_PKG_WLH_REQUESTS.UPD_BY_HH_DEL(pnHH_ID => pnID,
                                   pnLPU   => pnLPU);
  begin
    delete D_HOSP_HISTORIES t
     where t.ID          = pnID
       and t.LPU         = pnLPU returning t.HPK_PLAN_JOURNAL, t.RELATIVE_HH into nHPK_PLAN_JOURNAL, nRELATIVE_HH;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```

---

### Функция №30: D_PKG_WLH_REQUESTS.UPD_BY_HH_REVERSE_CANCEL

```sql
-- Oracle PACKAGE: UPD_BY_HH_REVERSE_CANCEL
--======================================================================
procedure UPD_BY_HH_REVERSE_CANCEL
(
  pnHPK_PLAN_JOURNAL                   in NUMBER,          -- Ссылка на запись в ЖГ
  pnLPU                                in NUMBER
)
is
  sOPTION               NUMBER(1) := coalesce(to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'HPKWLH',
                                                                          pnLPU     => pnLPU,
                                                                          pnRAISE   => 0)), 0);
  nWHL_ID               D_PKG_STD.tREF;
  nCURRENT_STATUS_CODE  D_WLH_STATUSES.CODE%type;
begin
  if sOPTION = 0 then
    return;
  end if;
  
  begin
    select req.ID,
           (select s.CODE
              from D_WLH_STATUSES s
             where s.ID = req.MAIN_STATUS)
      into nWHL_ID,
           nCURRENT_STATUS_CODE
      from D_WLH_REQUESTS req
     where req.HPK_PLAN_JOURNAL = pnHPK_PLAN_JOURNAL;
  exception when no_data_found then
    return;
  end;
```

---

### Функция №31: D_PKG_DIRECTIONS.SET_CANCELED

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №32: D_PKG_CSE_ACCESSES.CHECK_EMPLOYER_RIGHT

```sql
-- Oracle PACKAGE: CHECK_EMPLOYER_RIGHT
-- Возвращает: return number
--======================================================================
function CHECK_EMPLOYER_RIGHT
(
  pnLPU                                in NUMBER,          --ЛПУ
  pnEMPLOYER                           in NUMBER,
  psUNITCODE                           in VARCHAR2,        --Код раздела
  pnUNIT_ID                            in NUMBER,          --ID записи в разделе
  psRIGHT                              in VARCHAR2,        --Код действия в разделе
  pnCABLAB                             in NUMBER default null, --Кабинет
  pnSERVICE                            in NUMBER default null, --Услуга
  pnRAISE                              in NUMBER default 0 --Генерировать ошибку    1 - да,0 - нет
)
return number
as
  iRESULT               INTEGER;
  nSPECIALITY           D_PKG_STD.tREF;
  nSYSUSER              D_PKG_STD.tREF;
  nACCESS               D_PKG_STD.tREF;
  nRIGHT                D_PKG_STD.tREF;
begin
  -- Поиск всех прав для всех пользователей
  begin
    select t.ALL_RIGHTS, t.ID
      into iRESULT, nACCESS
      from D_CSE_ACCESSES t
     where t.UNITCODE         = psUNITCODE
       and t.UNIT_ID          = pnUNIT_ID
       and t.LPU              = pnLPU;
  exception when NO_DATA_FOUND then
    iRESULT := null;
    nACCESS := null;
  end;
```

---

### Функция №33: D_PKG_EMPLOYERS.GET_ID

```sql
-- Oracle PACKAGE: GET_ID
-- Возвращает: return number
--======================================================================
function GET_ID
(
  pnLPU                                NUMBER              --ID ЛПУ
) 
return number
is
 nRES                   D_EMPLOYERS.ID%type;               --ID сотрудника
begin
  nRES := D_PKG_SES.GETCONTEXT('MED','EMPLOYER');
  if nRES is null then
    begin
      select d.ID
        into nRES
        from D_EMPLOYERS d
             join D_USERS u
               on u.ID        = d.SYSUSER
              and u.USERNAME  = upper(D_F_GET_USERS())
       where d.LPU       = pnLPU
         and rownum = 1;
    exception when NO_DATA_FOUND then nRES := null;
              when TOO_MANY_ROWS then D_P_EXC('1. Найдено несколько сотрудников в данном МО связанных с пользователем: '||upper(D_F_GET_USERS()));
    end;
```

---

### Функция №34: D_PKG_HPK_PLAN_JOURNALS.SET_IS_READY

```sql
-- Oracle PACKAGE: SET_IS_READY
--======================================================================
procedure SET_IS_READY
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnIS_READY                           in NUMBER           --Готов ли пациент к госпитализации:0-нет;1-да
)
is
  nCID                  D_PKG_STD.tREF;
begin
  -- Поиск каталога --
  EXIST(pnID,pnLPU,nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'HPK_PLAN_JOURNALS_UPDATE',pnID);
  begin
    update D_HPK_PLAN_JOURNALS t set
      t.IS_READY = nvl(pnIS_READY,0)
     where t.ID        = pnID
       and t.LPU       = pnLPU;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm,'U');
  end;
```


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 34
- Загружено тел функций: 34

---

### Функция №1: d_pkg_signal_info_sets.get_full_signal_information

```sql
CREATE OR REPLACE FUNCTION d_pkg_signal_info_sets.get_full_signal_information(fnlpu numeric, fnsi_place numeric, fnpatient numeric, fndirection_services numeric DEFAULT NULL::numeric, fndiseasecase numeric DEFAULT NULL::numeric)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    sRESULT varchar(4000);
BEGIN
    CALL d_pkg_signal_info_sets.get_full_signal_information(pnLPU => fnlpu, pnSI_PLACE => fnsi_place, pnPATIENT => fnpatient, psINFO => sresult, pnDIRECTION_SERVICES => fndirection_services, pnDISEASECASE => fndiseasecase);
    return sresult;
END
$function$
```

---

### Функция №2: d_pkg_cse_accesses.get_id_with_rights

```sql
CREATE OR REPLACE FUNCTION d_pkg_cse_accesses.get_id_with_rights(pnlpu numeric, psunitcode character varying, psright character varying, pncablab numeric DEFAULT NULL::numeric, pnservice numeric DEFAULT NULL::numeric)
 RETURNS d_c_id
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    return d_pkg_cse_accesses.get_id_with_emp_rights(pnlpu,psunitcode,psright,d_pkg_employers.get_id(pnlpu)::numeric,pncablab,pnservice);
END
$function$
```

---

### Функция №3: d_pkg_std.frm_dt

```sql
CREATE OR REPLACE FUNCTION d_pkg_std.frm_dt()
 RETURNS character varying
 LANGUAGE sql
 IMMUTABLE STRICT SECURITY DEFINER
AS $function$
    SELECT 'DD.MM.YYYY HH24:MI';
$function$
```

---

### Функция №4: d_pkg_hpk_plan_journals.get_hosp_history_status

```sql
CREATE OR REPLACE FUNCTION d_pkg_hpk_plan_journals.get_hosp_history_status(pnlpu numeric, pnhpk_plan_journal numeric, pnhosp_histories numeric DEFAULT NULL::numeric)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
DECLARE
    rstatus_data record;
    crhosp_hist_status_data CURSOR  FOR
        SELECT
            hh.id  hosp_histories_id,
            hh.date_out,
            hhd.id  hh_deps_id,
            hhdb.id  hh_dep_beds_id
        FROM
            d_hosp_histories hh 
                    LEFT JOIN     d_hosp_history_deps hhd ON hhd.pid = hh.id  
                    LEFT JOIN     d_hh_dep_beds hhdb ON hhdb.pid = hhd.id 
        WHERE
            hh.hpk_plan_journal = pnhpk_plan_journal::bigint
                 AND ( hh.id = pnhosp_histories::bigint
                 OR pnhosp_histories IS NULL )
                 AND hh.lpu = pnlpu::bigint
                 AND discard_status = 0
             LIMIT 1;
BEGIN
    FOR rstatus_data IN crhosp_hist_status_data
    LOOP
        IF nullif((rstatus_data.date_out)::varchar,'') IS NOT NULL THEN
            return 3;

        ELSIF nullif((rstatus_data.hh_deps_id)::varchar,'') IS NULL THEN
            return 4;

        ELSIF nullif((rstatus_data.hh_dep_beds_id)::varchar,'') IS NOT NULL THEN
            return 2;

        ELSIF nullif((rstatus_data.hh_dep_beds_id)::varchar,'') IS NULL THEN
            return 1;

        END IF;
    END LOOP;
    --  строки не найдены, возвращаем 0
    return 0;
END
$function$
```

---

### Функция №5: d_pkg_std.frm_d

```sql
CREATE OR REPLACE FUNCTION d_pkg_std.frm_d()
 RETURNS character varying
 LANGUAGE sql
 IMMUTABLE STRICT SECURITY DEFINER
AS $function$
    SELECT 'DD.MM.YYYY';
$function$
```

---

### Функция №6: d_pkg_dat_tools.full_years

```sql
CREATE OR REPLACE FUNCTION d_pkg_dat_tools.full_years(fddate_to timestamp without time zone, fddate_from timestamp without time zone)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    return trunc(months_between(fddate_to,fddate_from)::numeric / 12);
END
$function$
```

---

### Функция №7: d_pkg_cse_accesses.check_right

```sql
CREATE OR REPLACE FUNCTION d_pkg_cse_accesses.check_right(pnlpu numeric, psunitcode character varying, pnunit_id numeric, psright character varying, pncablab numeric DEFAULT NULL::numeric, pnservice numeric DEFAULT NULL::numeric, pnraise numeric DEFAULT 0)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    return d_pkg_cse_accesses.check_employer_right(pnlpu,d_pkg_employers.get_id(pnlpu)::numeric,psunitcode,pnunit_id,psright,pncablab,pnservice,pnraise);
END
$function$
```

---

### Функция №8: d_pkg_hpk_plans.add

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.add(INOUT pnd_insert_id numeric, IN pnlpu numeric, IN pnpid numeric, IN pdplan_date timestamp without time zone, IN pnmale_count numeric, IN pnoper_count numeric, IN pngen_count numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    pnd_insert_id := null;
    CALL d_pkg_hosp_plan_kinds.exist(pnpid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLANS_INSERT', (null)::numeric);
    CALL d_pkg_hpk_plans.checks((null)::numeric, pnpid, pdplan_date, pnmale_count, pnoper_count, pngen_count);
    BEGIN
        INSERT INTO d_hpk_plans ( "id" , "lpu" , "pid" , "plan_date" , "male_count" , "oper_count" , "gen_count" ) VALUES ( d_gen_id(),pnlpu,pnpid,trunc(pdplan_date),pnmale_count,pnoper_count,pngen_count ) RETURNING id INTO pnd_insert_id;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'I',(SQLSTATE)::varchar);

    END;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLANS_INSERT', pnd_insert_id);
END
$procedure$
```

---

### Функция №9: d_pkg_vmp_links.upd

```sql
CREATE OR REPLACE PROCEDURE d_pkg_vmp_links.upd(IN pnid numeric, IN pnlpu numeric, IN pnvmp_appl numeric, IN pnvmp_talon numeric, IN pndirection numeric, IN vapi_version numeric DEFAULT 1)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
BEGIN
    CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, 'VMP_LINKS', nversion);
    CALL d_pkg_bpenv.beforebp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'VMP_LINKS_UPDATE', pnid);
    BEGIN
        update d_vmp_links t set vmp_appl = pnvmp_appl , vmp_talon = pnvmp_talon , direction = pndirection where t.id = pnid::bigint
             AND t.version = nversion::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'VMP_LINKS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'VMP_LINKS_UPDATE', pnid);
END
$procedure$
```

---

### Функция №10: d_pkg_hpk_plan_journals.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
    nDIR numeric(17);
    nTMP numeric(17);
    nCOUNT NUMERIC(2);
    nDISEASECASE d_hpk_plan_journals.diseasecase%TYPE;
    nDIR_ID NUMERIC(17);
    x record;
BEGIN
    CALL d_pkg_hpk_plan_journals.exist(pnid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLAN_JOURNALS_DELETE', pnid);
    SELECT
        t.direction,
        t.diseasecase
    INTO ndir, ndiseasecase
    FROM
        d_hpk_plan_journals t
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_PLAN_JOURNALS');

    END IF;
    SELECT
        count ( * )
    INTO STRICT ncount
    FROM
        ( --  Если есть случаи заболевания, то почистить их, если на них нет ссылок в услугах
            SELECT
                *
            FROM
                d_direction_services ds
            WHERE
                ds.diseasecase = ndiseasecase
                     AND NOT exists ( SELECT
                    null as null
                FROM
                    d_emergencyjournal t 
                            JOIN     d_visits v ON v.id = t.visit
     AND v.lpu = t.lpu 
                WHERE
                    v.pid = ds.id )
                 LIMIT 1 ) t_alias_0;
    IF ncount > 0 THEN
        PERFORM d_p_exc(1,'1. По данному случаю заболевания существуют направления на услуги');

    END IF;
    FOR x IN (
        SELECT
            h.id
        FROM
            d_hpkpj_archs h
        WHERE
            h.hpk_plan_journals = pnid::bigint
                 AND h.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_hpkpj_archs.del((x.id)::numeric, pnlpu);
    END LOOP;
    CALL d_pkg_wlh_requests.upd_by_hpk_del(pnHPK_PLAN_JOURNAL => pnid, pnLPU => pnlpu);
    BEGIN
        DELETE FROM d_hpk_plan_journals t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_PLAN_JOURNALS');

    END IF;
    --  Если из другого ЛПУ (есть OUTER_DIRECTIONS), почистим DIRECTIONS.HOSP_DEP
        IF ndir IS NOT NULL THEN
        BEGIN
            SELECT
                t2.id
            INTO STRICT ntmp
            FROM
                d_directions t
                CROSS JOIN                 d_outer_directions t2
            WHERE
                t.id = ndir::bigint
                     AND t.lpu = pnlpu::bigint
                     AND t.outer_direction = t2.id;
            update d_directions t set hosp_dep = null where t.id = ndir::bigint
                 AND t.lpu = pnlpu::bigint;
            EXCEPTION
                WHEN no_data_found THEN
                            null;

        END;

    END IF;
    IF ( ndiseasecase IS NOT NULL ) THEN
        SELECT
            count ( * )
        INTO STRICT ncount
        FROM
            ( SELECT
                    *
                FROM
                    d_direction_services ds
                WHERE
                    ds.diseasecase = ndiseasecase
                         AND exists ( SELECT
                        null as null
                    FROM
                        d_emergencyjournal t 
                                JOIN     d_visits v ON v.id = t.visit
     AND v.lpu = t.lpu 
                    WHERE
                        v.pid = ds.id )
                     LIMIT 1 ) t_alias_1;
        IF ( ncount = 0 ) THEN
            SELECT
                count(d_lp.id)
            INTO STRICT ncount
            FROM
                d_labmed_patjour d_lp
            WHERE
                d_lp.unit_code = 'DISEASECASES'
                     AND d_lp.unit_id = ndiseasecase;

        END IF;
        IF ( ncount = 0 ) THEN
            CALL d_pkg_diseasecases.del((ndiseasecase)::numeric, pnlpu);

        END IF;

    END IF;
    SELECT
        d.id
    INTO ndir_id
    FROM
        d_hpk_plan_journals hpj 
                JOIN     d_hosp_plan_kinds hpk ON hpj.hpk = hpk.id  
                JOIN     d_directions d ON hpj.direction = d.id 
    WHERE
        hpk.hp_code = 'ROUND_DENTURES'
             AND hpj.id = pnid::bigint;
    IF NOT FOUND THEN
        ndir_id := null;
        IF ( ndir_id IS NOT NULL ) THEN
            CALL d_pkg_directions.del(pnID => ndir_id, pnLPU => pnlpu);

        END IF;

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLAN_JOURNALS_DELETE', pnid);
END
$procedure$
```

---

### Функция №11: d_pkg_hpk_plan_journals.set_record

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.set_record(IN pnlpu numeric, IN pnpatient numeric, INOUT pnexist numeric, INOUT pddate timestamp without time zone, INOUT pnplan_kind numeric, INOUT pnhave_next numeric, INOUT pnhave_prev numeric, IN pndirection numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCOUNT numeric(17);
    nHAVE_NEXT NUMERIC(17);
BEGIN
    pnhave_prev := null;
    pnhave_next := null;
    pnplan_kind := null;
    pddate := null;
    pnexist := null;
    -- Проверяем, есть ли вообще его записи с PID
    SELECT
        count(1)
    INTO STRICT ncount
    FROM
        d_hpk_plan_journals t
    WHERE
        t.patient = pnpatient::bigint
             AND ( t.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.lpu = pnlpu::bigint
             AND t.hpk_plan IS NOT NULL;
    IF ncount < 1 THEN
        pnexist := 0;
        pnplan_kind := null;
        pddate := null;
        pnhave_next := 0;
        pnhave_prev := 0;

    ELSIF ncount = 1 THEN
        pnexist := 1;
    pnhave_next := 0;
    pnhave_prev := 0;
    -- Находим дату регистрации и вид плана
    SELECT
        t1.plan_date,
        t1.pid
    INTO STRICT pddate, pnplan_kind
    FROM
        d_hpk_plan_journals t
        CROSS JOIN         d_hpk_plans t1
    WHERE
        t.patient = pnpatient::bigint
             AND ( t.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.lpu = pnlpu::bigint
             AND t.hpk_plan IS NOT NULL
             AND t1.id = t.hpk_plan;

    ELSIF ncount > 1 THEN
        pnexist := 1;
    ncount := 0;
    SELECT
        count(DISTINCT t1.plan_date)
    INTO STRICT ncount
    FROM
        d_hpk_plan_journals t
        CROSS JOIN         d_hpk_plans t1
    WHERE
        t1.id = t.hpk_plan
             AND ( t.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.patient = pnpatient::bigint
             AND t.lpu = pnlpu::bigint
             AND t.hpk_plan IS NOT NULL;
    IF ncount = 1 THEN
        -- Только одна дата, находим ее
        SELECT DISTINCT
            t1.plan_date
        INTO STRICT pddate
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL;
        CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 0, pnpatient, pddate, (null)::numeric, pnplan_kind, nhave_next, pndirection);
        -- Ищем pnHAVE_NEXT
                IF nhave_next = 1 THEN
            pnhave_next := nhave_next;

        ELSIF nhave_next = 0 THEN
            ncount := 0;
        SELECT
            count(1)
        INTO STRICT ncount
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date > pddate;
        IF ncount < 1 THEN
            pnhave_next := 0;

        ELSE
            pnhave_next := 1;

        END IF;

        END IF;
        CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 2, pnpatient, pddate, (null)::numeric, pnplan_kind, nhave_next, pndirection);
        IF nhave_next = 1 THEN
            pnhave_prev := nhave_next;

        ELSIF nhave_next = 0 THEN
            ncount := 0;
        SELECT
            count(1)
        INTO STRICT ncount
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date < pddate;
        IF ncount < 1 THEN
            pnhave_prev := 0;

        ELSE
            pnhave_prev := 1;

        END IF;

        END IF;
        return;

    ELSIF ncount > 1 THEN
        pnhave_next := 1;
    -- Находим минимальную дату из всех его записей
    SELECT
        MIN ( t1.plan_date )
    INTO STRICT pddate
    FROM
        d_hpk_plan_journals t
        CROSS JOIN         d_hpk_plans t1
    WHERE
        t1.id = t.hpk_plan
             AND t.patient = pnpatient::bigint
             AND ( t.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.lpu = pnlpu::bigint
             AND t.hpk_plan IS NOT NULL;
    ncount := 0;
    SELECT
        count(1)
    INTO STRICT ncount
    FROM
        d_hpk_plan_journals t
        CROSS JOIN         d_hpk_plans t1
    WHERE
        t1.id = t.hpk_plan
             AND t1.plan_date = pddate
             AND t.patient = pnpatient::bigint
             AND ( t.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.lpu = pnlpu::bigint
             AND t.hpk_plan IS NOT NULL;
    IF ncount = 1 THEN
        -- Если только одна запись на этот день
        SELECT
            t1.pid
        INTO STRICT pnplan_kind
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t1.plan_date = pddate
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.id = t.hpk_plan group by t1.pid ;

    ELSIF ncount > 1 THEN
        CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 0, pnpatient, pddate, (null)::numeric, pnplan_kind, pnhave_next, pndirection);

    END IF;
    pnhave_prev := 0;

    END IF;

    END IF;
END
$procedure$
```

---

### Функция №12: d_pkg_hpk_plan_journals.set_next_record

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.set_next_record(IN pnlpu numeric, IN pnpatient numeric, IN pddate timestamp without time zone, IN pnplan_kind numeric, INOUT pdnext_date timestamp without time zone, INOUT pnnext_plan_kind numeric, INOUT pnhave_next numeric, IN pndirection numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCOUNT numeric(17);
    dNEXT_DATE d_hpk_plan_journals.register_date%TYPE;
    nHAVE_NEXT NUMERIC(17);
BEGIN
    pnhave_next := null;
    pnnext_plan_kind := null;
    pdnext_date := null;
    ncount := 0;
    dnext_date := null;
    SELECT
        count(DISTINCT t.pid)
    INTO STRICT ncount
    FROM
        d_hpk_plans t
        CROSS JOIN         d_hpk_plan_journals t1
    WHERE
        t1.patient = pnpatient::bigint
             AND ( t1.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.plan_date = pddate
             AND t1.hpk_plan IS NOT NULL
             AND t1.hpk_plan = t.id
             AND t1.lpu = pnlpu::bigint
             AND t.pid > pnplan_kind::bigint;
    IF ncount < 1 THEN
        -- Нет таких, берем следущую дату
        SELECT
            MIN ( t1.plan_date )
        INTO STRICT dnext_date
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date > pddate;

    ELSE
        pdnext_date := pddate;
        CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 1, pnpatient, pdnext_date, pnplan_kind, pnnext_plan_kind, nhave_next, pndirection);
        -- Находим pnHAVE_NEXT
                IF nhave_next = 1 THEN
            pnhave_next := nhave_next;

        ELSIF nhave_next = 0 THEN
            ncount := 0;
        SELECT
            count(1)
        INTO STRICT ncount
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date > pdnext_date;
        IF ncount < 1 THEN
            pnhave_next := 0;

        ELSE
            pnhave_next := 1;

        END IF;

        END IF;
        return;

    END IF;
    -- Если ищем на следущую дату
        IF dnext_date IS NOT NULL THEN
        pdnext_date := dnext_date;
        -- Считаем кол-во разных видов плана
        SELECT
            count(DISTINCT t.pid)
        INTO STRICT ncount
        FROM
            d_hpk_plans t
            CROSS JOIN             d_hpk_plan_journals t1
        WHERE
            t1.patient = pnpatient::bigint
                 AND ( t1.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.plan_date = dnext_date
                 AND t1.hpk_plan IS NOT NULL
                 AND t1.hpk_plan = t.id
                 AND t1.lpu = pnlpu::bigint;
        IF ncount = 1 THEN
            /* Если только один вид плана на нашу дату
Определяем pnHAVE_NEXT */
SELECT
                count(t1.plan_date)
            INTO STRICT ncount
            FROM
                d_hpk_plan_journals t
                CROSS JOIN                 d_hpk_plans t1
            WHERE
                t1.id = t.hpk_plan
                     AND t.patient = pnpatient::bigint
                     AND ( t.direction = pndirection::bigint
                     OR pndirection IS NULL )
                     AND t.lpu = pnlpu::bigint
                     AND t.hpk_plan IS NOT NULL
                     AND t1.plan_date > dnext_date;
            IF ncount < 1 THEN
                pnhave_next := 0;

            ELSIF ncount >= 1 THEN
                pnhave_next := 1;

            END IF;
            -- Тащим вид плана
            SELECT
                t.pid
            INTO STRICT pnnext_plan_kind
            FROM
                d_hpk_plans t
                CROSS JOIN                 d_hpk_plan_journals t1
            WHERE
                t1.patient = pnpatient::bigint
                     AND ( t1.direction = pndirection::bigint
                     OR pndirection IS NULL )
                     AND t.plan_date = dnext_date
                     AND t1.hpk_plan IS NOT NULL
                     AND t1.hpk_plan = t.id
                     AND t1.lpu = pnlpu::bigint group by t.pid ;

        ELSIF ncount > 1 THEN
            CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 0, pnpatient, pdnext_date, (null)::numeric, pnnext_plan_kind, pnhave_next, pndirection);

        END IF;

    END IF;
END
$procedure$
```

---

### Функция №13: d_pkg_hpk_plan_journals.set_prev_record

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.set_prev_record(IN pnlpu numeric, IN pnpatient numeric, IN pddate timestamp without time zone, IN pnplan_kind numeric, INOUT pdprev_date timestamp without time zone, INOUT pnprev_plan_kind numeric, INOUT pnhave_prev numeric, IN pndirection numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCOUNT numeric(17);
    nFLAG numeric(17);
    dPREV_DATE d_hpk_plan_journals.register_date%TYPE;
    nHAVE_PREV NUMERIC(17);
BEGIN
    pnhave_prev := null;
    pnprev_plan_kind := null;
    pdprev_date := null;
    ncount := 0;
    dprev_date := null;
    SELECT
        count(DISTINCT t.pid)
    INTO STRICT ncount
    FROM
        d_hpk_plans t
        CROSS JOIN         d_hpk_plan_journals t1
    WHERE
        t1.patient = pnpatient::bigint
             AND ( t1.direction = pndirection::bigint
             OR pndirection IS NULL )
             AND t.plan_date = pddate
             AND t1.hpk_plan IS NOT NULL
             AND t1.hpk_plan = t.id
             AND t1.lpu = pnlpu::bigint
             AND t1.directed_by IS NULL
             AND t.pid < pnplan_kind::bigint;
    IF ncount < 1 THEN
        nflag := 1;
        SELECT
            max(t1.plan_date)
        INTO STRICT dprev_date
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date < pddate;

    ELSE
        nflag := 0;
        pdprev_date := pddate;
        CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 2, pnpatient, pdprev_date, pnplan_kind, pnprev_plan_kind, nhave_prev, pndirection);
        -- Ищем pnHAVE_PREV
                IF nhave_prev = 1 THEN
            pnhave_prev := nhave_prev;

        ELSIF nhave_prev = 0 THEN
            ncount := 0;
        SELECT
            count(1)
        INTO STRICT ncount
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hpk_plans t1
        WHERE
            t1.id = t.hpk_plan
                 AND t.patient = pnpatient::bigint
                 AND ( t.direction = pndirection::bigint
                 OR pndirection IS NULL )
                 AND t.lpu = pnlpu::bigint
                 AND t.hpk_plan IS NOT NULL
                 AND t1.plan_date < pdprev_date;
        IF ncount < 1 THEN
            pnhave_prev := 0;

        ELSE
            pnhave_prev := 1;

        END IF;

        END IF;
        return;

    END IF;
    -- Если ищем на следущую дату
        IF nflag = 1 THEN
        IF dprev_date IS NOT NULL THEN
            pdprev_date := dprev_date;
            -- Считаем кол-во разных видов плана
            SELECT
                count(DISTINCT t.pid)
            INTO STRICT ncount
            FROM
                d_hpk_plans t
                CROSS JOIN                 d_hpk_plan_journals t1
            WHERE
                t1.patient = pnpatient::bigint
                     AND ( t1.direction = pndirection::bigint
                     OR pndirection IS NULL )
                     AND t.plan_date = dprev_date
                     AND t1.hpk_plan IS NOT NULL
                     AND t1.hpk_plan = t.id
                     AND t1.lpu = pnlpu::bigint;
            IF ncount = 1 THEN
                /* Если только один вид плана на нашу дату
Определяем pnHAVE_PREV */
SELECT
                    count(t1.plan_date)
                INTO STRICT ncount
                FROM
                    d_hpk_plan_journals t
                    CROSS JOIN                     d_hpk_plans t1
                WHERE
                    t1.id = t.hpk_plan
                         AND t.patient = pnpatient::bigint
                         AND ( t.direction = pndirection::bigint
                         OR pndirection IS NULL )
                         AND t.lpu = pnlpu::bigint
                         AND t.hpk_plan IS NOT NULL
                         AND t1.plan_date < dprev_date;
                IF ncount < 1 THEN
                    pnhave_prev := 0;

                ELSIF ncount >= 1 THEN
                    pnhave_prev := 1;

                END IF;
                -- Тащим вид плана
                SELECT
                    t.pid
                INTO STRICT pnprev_plan_kind
                FROM
                    d_hpk_plans t
                    CROSS JOIN                     d_hpk_plan_journals t1
                WHERE
                    t1.patient = pnpatient::bigint
                         AND ( t1.direction = pndirection::bigint
                         OR pndirection IS NULL )
                         AND t.plan_date = dprev_date
                         AND t1.hpk_plan IS NOT NULL
                         AND t1.hpk_plan = t.id
                         AND t1.lpu = pnlpu::bigint group by t.pid ;

            ELSIF ncount > 1 THEN
                CALL d_pkg_hpk_plan_journals.count_by_plan_kind(pnlpu, 3, pnpatient, pdprev_date, (null)::numeric, pnprev_plan_kind, nhave_prev, pndirection);
            -- Ищем pnHAVE_PREV
                        IF nhave_prev = 1 THEN
                pnhave_prev := nhave_prev;

            ELSIF nhave_prev = 0 THEN
                ncount := 0;
            SELECT
                count(1)
            INTO STRICT ncount
            FROM
                d_hpk_plan_journals t
                CROSS JOIN                 d_hpk_plans t1
            WHERE
                t1.id = t.hpk_plan
                     AND t.patient = pnpatient::bigint
                     AND ( t.direction = pndirection::bigint
                     OR pndirection IS NULL )
                     AND t.lpu = pnlpu::bigint
                     AND t.hpk_plan IS NOT NULL
                     AND t1.plan_date < pdprev_date;
            IF ncount < 1 THEN
                pnhave_prev := 0;

            ELSE
                pnhave_prev := 1;

            END IF;

            END IF;
            return;

            END IF;

        ELSIF dprev_date IS NULL THEN
            pnhave_prev := 0;

        END IF;

    END IF;
END
$procedure$
```

---

### Функция №14: d_pkg_hpk_plan_journals.set_hh_direction_date

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.set_hh_direction_date(IN pnid numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    dHH_DIRECTION_DATE d_hpk_plan_journals.hh_direction_date%TYPE;
BEGIN
    SELECT
        t.hh_direction_date
    INTO STRICT dhh_direction_date
    FROM
        d_hpk_plan_journals t
    WHERE
        t.id = pnid::bigint;
    IF dhh_direction_date IS NULL THEN
        update d_hpk_plan_journals t set hh_direction_date = sysdate() where t.id = pnid::bigint;

    ELSIF dhh_direction_date IS NOT NULL THEN
        update d_hpk_plan_journals t set hh_direction_date = null where t.id = pnid::bigint;

    END IF;
END
$procedure$
```

---

### Функция №15: d_pkg_hpk_plan_journals.nearest_date_search

```sql
CREATE OR REPLACE FUNCTION d_pkg_hpk_plan_journals.nearest_date_search(pnlpu numeric, pnhpk numeric, pdstart_date timestamp without time zone, pnsex numeric, pnoper_type numeric)
 RETURNS timestamp without time zone
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    nGEN_COUNT NUMERIC(10);
    nMALE_COUNT NUMERIC(10);
    nFEMALE_COUNT NUMERIC(10);
    nOPER_COUNT NUMERIC(10);
    nCON_COUNT NUMERIC(10);
    dPLAN_DATE timestamp(0);
    nSTATUS NUMERIC(1);
    dEND_DATE timestamp(0);
    nSEX_FLAG NUMERIC(1);
    nOPER_FLAG NUMERIC(1);
BEGIN
    SELECT
        coalesce(max(plan_date)::timestamp,pdstart_date)
    INTO STRICT dend_date
    FROM
        d_hpk_plans
    WHERE
        lpu = pnlpu::bigint
             AND pid = pnhpk::bigint;
    IF trunc(pdstart_date) >= trunc(sysdate()) THEN
        dplan_date := trunc(pdstart_date);

    ELSE
        dplan_date := trunc(sysdate());

    END IF;
    
    LOOP
        nsex_flag := 0;
        noper_flag := 0;
        CALL d_pkg_hpk_plan_journals.get_info(pnlpu, pnhpk, dplan_date, 0, ngen_count, noper_count, ncon_count, nmale_count, nfemale_count, nstatus);
        IF nstatus = 1 THEN
            -- Проверка пола
                        IF pnsex = 0
     AND ( nfemale_count > 0
     OR nfemale_count IS NULL
     AND ngen_count > 0 ) THEN
                nsex_flag := 1;

            ELSIF pnsex = 1
                 AND ( nmale_count > 0
                 OR nmale_count IS NULL
                 AND ngen_count > 0 ) THEN
                nsex_flag := 1;

            ELSIF pnsex IS NULL
                 AND ngen_count > 0 THEN
                nsex_flag := 1;

            END IF;
            -- Проверка оперативности
                        IF pnoper_type = 0
     AND ( ncon_count > 0
     OR ncon_count IS NULL
     AND ngen_count > 0 ) THEN
                noper_flag := 1;

            ELSIF pnoper_type = 1
                 AND ( noper_count > 0
                 OR noper_count IS NULL
                 AND ngen_count > 0 ) THEN
                noper_flag := 1;

            ELSIF pnoper_type IS NULL
                 AND ngen_count > 0 THEN
                noper_flag := 1;

            END IF;
            IF noper_flag = 1
     AND nsex_flag = 1 THEN
                return trunc(dplan_date);

            END IF;

        END IF;
        EXIT WHEN dplan_date > dend_date;
        dplan_date := dplan_date + 1;
    END LOOP;
    return null;
END
$function$
```

---

### Функция №16: d_pkg_hpk_plan_journals.get_quant_beds_profiles_new

```sql
CREATE OR REPLACE FUNCTION d_pkg_hpk_plan_journals.get_quant_beds_profiles_new(pnhpk_plan numeric, pnlpu numeric)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    nI NUMERIC(17) := 0;
    tPROF d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__nprof := DICTIONARY_CREATE_INT();
    sOPTION varchar(1000);
    sSTR_PROFILE_FREE varchar(4000) := '';   /*  Сюда загружаем все что навычисляли */
    nQUANT_MALE_OCCUPIED NUMERIC(10);   /*  Занятых коек мужских */
    nQUANT_FEM_OCCUPIED NUMERIC(10);   /*  Занятых коек женских */
    nQUANT_NO_SEX_OCCUPIED NUMERIC(10);   /*  Занятых общих коек */
    nSQUANT_MALE_OCCUPIED NUMERIC(10);   /*  Занятых коек мужских (реан.) */
    nSQUANT_FEM_OCCUPIED NUMERIC(10);   /*  Занятых коек женских (реан.) */
    nQUANT_MALE_PLAN_OUT NUMERIC(10);   /*  План выписки (мужчины) */
    nQUANT_FEM_PLAN_OUT NUMERIC(10);   /*  План выписки (женщины) */
    nQUANT_NO_SEX_PLAN_OUT NUMERIC(10);   /*  План выписки (общие койки) */
    nQUANT_MALE_PLAN_IN NUMERIC(10);   /*  План госпитализации (мужчины) */
    nQUANT_FEM_PLAN_IN NUMERIC(10);   /*  План госпитализации (женщины) */
    nBEDS_MALE_FREE NUMERIC(17);   /*  Количество свободных коек мужских */
    nBEDS_FEM_FREE NUMERIC(17);   /*  Количество свободных коек женских */
    nBEDS_NO_SEX_FREE NUMERIC(17);   /*  Количество свободных коек общих */
    or2pgTmpVar0_0 d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof;
    cr record;
    sp record;
BEGIN
    soption := d_pkg_options.get('BedTypeAfterReanimation',pnlpu)::varchar;
    /*  цикл по отделениям в плане
 есть отделение */
 FOR cr IN (
        SELECT
            t3.bed_type,
            t2.dep,
            t4.dp_code  dep_code,
            t4.dp_name,
            t1.plan_date
        FROM
            d_hpk_plans t1
            CROSS JOIN             d_hosp_plan_deps t2
            CROSS JOIN             d_deps t4 
                    LEFT OUTER JOIN     d_hosp_plan_dep_beds t3 ON t2.id = t3.pid 
        WHERE
            t1.id = pnhpk_plan::bigint
                 AND t1.pid = t2.hp_kind
                 AND t1.lpu = pnlpu::bigint
                 AND true = true
                 AND t4.id = t2.dep)
    LOOP
        FOR sp IN (
            SELECT
                sum((tt.male)::numeric)  male_beds,
                sum((tt.female)::numeric)  female_beds,
                sum((tt.no_sex)::numeric)  no_sex_beds,
                tt.profile_id,
                tt.profile_code,
                tt.profile_name
            FROM
                ( SELECT
                        (CASE
                            WHEN t6.sex_type = 1 THEN 1
                            ELSE 0
                        END)  male,
                        (CASE
                            WHEN t6.sex_type = 0 THEN 1
                            ELSE 0
                        END)  female,
                        (CASE
                            WHEN t6.sex_type IS NULL THEN 1
                            ELSE 0
                        END)  no_sex,
                        t7.id  profile_id,
                        t7.bt_code  profile_code,
                        t7.bt_name  profile_name
                    FROM
                        d_dep_beds t5
                        CROSS JOIN                         d_dep_bed_profiles t6
                        CROSS JOIN                         d_bed_types t7
                    WHERE
                        t5.dep = cr.dep::bigint
                             AND t5.pid IS NOT NULL
                             AND trunc(t6.date_begin) <= cr.plan_date::timestamp
                             AND ( trunc(t6.date_end) > cr.plan_date::timestamp
                             OR t6.date_end IS NULL )
                             AND t5.id = t6.pid
                             AND t7.id = t6.bed_type
                             AND ( t7.id = cr.bed_type::bigint
                             OR nullif((cr.bed_type)::varchar,'') IS NULL ) ) tt group by tt.profile_id , tt.profile_code , tt.profile_name 
ORDER BY to_number(tt.profile_id))
        LOOP
            SELECT
                sum((tr.male)::numeric),
                sum((tr.female)::numeric),
                sum((tr.no_sex)::numeric)
            INTO STRICT nquant_male_occupied, nquant_fem_occupied, nquant_no_sex_occupied
            FROM
                ( SELECT
                        (CASE
                            WHEN s.sex_type = 1
                                 OR s.pid IS NULL
                                 AND t8.sex = 1 THEN 1
                            ELSE 0
                        END)  male,
                        (CASE
                            WHEN s.sex_type = 0
                                 OR s.pid IS NULL
                                 AND t8.sex = 0 THEN 1
                            ELSE 0
                        END)  female,
                        (CASE
                            WHEN s.sex_type IS NULL
                                 AND s.pid IS NOT NULL THEN 1
                            ELSE 0
                        END)  no_sex
                    FROM
                        d_hpk_plan_journals t1
                        CROSS JOIN                         d_hosp_histories t2
                        CROSS JOIN                         d_hosp_history_deps t3
                        CROSS JOIN                         d_persmedcard t7
                        CROSS JOIN                         d_agents t8
                        CROSS JOIN                         d_deps t9
                        CROSS JOIN                         d_deps_types t10 
                                LEFT OUTER JOIN     ( SELECT
        t6.bed_type,
        t4.pid,
        t6.sex_type
    FROM
        d_hh_dep_beds t4
        CROSS JOIN         d_dep_beds t5
        CROSS JOIN         d_dep_bed_profiles t6
    WHERE
        ( t4.date_in <= cr.plan_date::timestamp
             OR t4.date_in IS NULL )
             AND ( trunc(t4.date_out) > cr.plan_date::timestamp
             OR t4.date_out IS NULL )
             AND t5.id = t4.dep_bed
             AND t5.id = t6.pid ) s ON t3.id = s.pid 
                    WHERE
                        t1.hpk_plan = pnhpk_plan::bigint
                             AND t1.lpu = pnlpu::bigint
                             AND t1.id = t2.hpk_plan_journal
                             AND trunc(t2.date_in) <= cr.plan_date::timestamp
                             AND ( trunc(t2.date_out) > cr.plan_date::timestamp
                             OR t2.date_out IS NULL )
                             AND t2.id = t3.pid
                             AND trunc(t3.date_in) <= cr.plan_date::timestamp
                             AND ( trunc(t3.date_out) > cr.plan_date::timestamp
                             OR t3.date_out IS NULL )
                             AND t9.id = t3.dep
                             AND t10.id = t9.dp_type
                             AND t7.id = t2.patient
                             AND t8.id = t7.agent
                             AND true = true
                             AND t3.dep = cr.dep::bigint
                             AND coalesce(s.bed_type,t3.bed_type) = sp.profile_id::bigint
                             AND t10.dt_code <> '5' ) tr;
            IF nquant_male_occupied IS NULL THEN
                nquant_male_occupied := 0;

            END IF;
            IF nquant_fem_occupied IS NULL THEN
                nquant_fem_occupied := 0;

            END IF;
            IF nquant_no_sex_occupied IS NULL THEN
                nquant_no_sex_occupied := 0;

            END IF;
            /*  К занятым койкам  добавляем еще пациентов в реанимации. Смотрим, если ли отделение типа реанимация,
 у которого в поле основное отделение (D_DEPS.BELONG_TO) стоит  то, которое привязано в журнале госпитализации.
 Профиль койки - системная опция.
 Системная опция – профиль койки, на которую пойдет  пациент после реанимации
 (код отделения, код профиля койки; код отделения, код профиля койки). */
SELECT
                count(DISTINCT (CASE
    WHEN ( t8.sex = 1 ) OR ( check_null((t8.sex)::varchar,(1)::varchar) ) THEN t7.id
    ELSE null
END)),
                count(DISTINCT (CASE
    WHEN ( t8.sex = 0 ) OR ( check_null((t8.sex)::varchar,(0)::varchar) ) THEN t7.id
    ELSE null
END))
            INTO STRICT nsquant_male_occupied, nsquant_fem_occupied
            FROM
                d_hpk_plan_journals t1
                CROSS JOIN                 d_hosp_histories t2
                CROSS JOIN                 d_hosp_history_deps t3
                CROSS JOIN                 d_persmedcard t7
                CROSS JOIN                 d_agents t8
                CROSS JOIN                 d_deps t9
                CROSS JOIN                 d_deps_types t10
            WHERE
                t1.hpk_plan = pnhpk_plan::bigint
                     AND t1.lpu = pnlpu::bigint
                     AND t1.id = t2.hpk_plan_journal
                     AND trunc(t2.date_in) <= cr.plan_date::timestamp
                     AND ( trunc(t2.date_out) > cr.plan_date::timestamp
                     OR t2.date_out IS NULL )
                     AND t2.id = t3.pid
                     AND trunc(t3.date_in) <= cr.plan_date::timestamp
                     AND ( trunc(t3.date_out) > cr.plan_date::timestamp
                     OR t3.date_out IS NULL )
                     AND t9.id = t3.dep
                     AND t10.id = t9.dp_type
                     AND t7.id = t2.patient
                     AND t8.id = t7.agent
                     AND t10.dt_code = '5'
                     AND t9.belong_to = cr.dep::bigint
                     AND dbms_lob.instr(soption,(sp.profile_code)::text) = dbms_lob.instr(soption,(cr.dep_code)::text) + length(cr.dep_code) + 1;
            nquant_male_occupied := nquant_male_occupied + nsquant_male_occupied;
            nquant_fem_occupied := nquant_fem_occupied + nsquant_fem_occupied;
            --  план выписки
            SELECT
                sum((tr1.male)::numeric),
                sum((tr1.female)::numeric),
                sum((tr1.no_sex)::numeric)
            INTO STRICT nquant_male_plan_out, nquant_fem_plan_out, nquant_no_sex_plan_out
            FROM
                ( SELECT
                        (CASE
                            WHEN s.sex_type = 1
                                 OR s.pid IS NULL
                                 AND t8.sex = 1 THEN 1
                            ELSE 0
                        END)  male,
                        (CASE
                            WHEN s.sex_type = 0
                                 OR s.pid IS NULL
                                 AND t8.sex = 0 THEN 1
                            ELSE 0
                        END)  female,
                        (CASE
                            WHEN s.sex_type IS NULL
                                 AND s.pid IS NOT NULL THEN 1
                            ELSE 0
                        END)  no_sex
                    FROM
                        d_hpk_plan_journals t1
                        CROSS JOIN                         d_hosp_histories t2
                        CROSS JOIN                         d_hosp_history_deps t3
                        CROSS JOIN                         d_persmedcard t7
                        CROSS JOIN                         d_agents t8
                        CROSS JOIN                         d_deps t9
                        CROSS JOIN                         d_deps_types t10 
                                LEFT OUTER JOIN     ( SELECT
        t6.bed_type,
        t4.pid,
        t6.sex_type
    FROM
        d_hh_dep_beds t4
        CROSS JOIN         d_dep_beds t5
        CROSS JOIN         d_dep_bed_profiles t6
    WHERE
        ( t4.date_in <= sysdate()
             OR t4.date_in IS NULL )
             AND ( trunc(t4.date_out) > sysdate()
             OR t4.date_out IS NULL )
             AND t5.id = t4.dep_bed
             AND t5.id = t6.pid ) s ON t3.id = s.pid 
                    WHERE
                        t1.hpk_plan = pnhpk_plan::bigint
                             AND t1.lpu = pnlpu::bigint
                             AND t1.id = t2.hpk_plan_journal
                             AND trunc(t2.plan_date_out) = trunc(cr.plan_date)::timestamp
                             AND ( t2.plan_date_out > t2.date_out
                             OR t2.date_out IS NULL )
                             AND t2.id = t3.pid
                             AND trunc(t3.date_in) <= sysdate()
                             AND ( trunc(t3.date_out) > sysdate()
                             OR t3.date_out IS NULL )
                             AND t9.id = t3.dep
                             AND t10.id = t9.dp_type
                             AND t3.dep = cr.dep::bigint
                             AND true = true
                             AND t7.id = t2.patient
                             AND t8.id = t7.agent
                             AND coalesce(s.bed_type,t3.bed_type) = sp.profile_id::bigint ) tr1;
            IF nquant_male_plan_out IS NULL THEN
                nquant_male_plan_out := 0;

            END IF;
            IF nquant_fem_plan_out IS NULL THEN
                nquant_fem_plan_out := 0;

            END IF;
            IF nquant_no_sex_plan_out IS NULL THEN
                nquant_no_sex_plan_out := 0;

            END IF;
            --  План госпитализации
            SELECT
                count(DISTINCT (CASE
    WHEN ( t5.sex = 1 ) OR ( check_null((t5.sex)::varchar,(1)::varchar) ) THEN t2.id
    ELSE null
END)),
                count(DISTINCT (CASE
    WHEN ( t5.sex = 0 ) OR ( check_null((t5.sex)::varchar,(0)::varchar) ) THEN t2.id
    ELSE null
END))
            INTO STRICT nquant_male_plan_in, nquant_fem_plan_in
            FROM
                d_hpk_plan_journals t1
                CROSS JOIN                 d_persmedcard t2
                CROSS JOIN                 d_agents t5
                CROSS JOIN                 d_directions t6
            WHERE
                t1.hpk_plan = pnhpk_plan::bigint
                     AND t1.lpu = pnlpu::bigint
                     AND t2.id = t1.patient
                     AND t6.hosp_dep = cr.dep::bigint
                     AND t5.id = t2.agent
                     AND t6.id = t1.direction
                     AND sp.profile_id::bigint = t6.hosp_bed_type
                     AND NOT exists ( SELECT
                    null as null
                FROM
                    d_hosp_histories hh
                WHERE
                    t1.id = hh.hpk_plan_journal
                         AND hh.discard_status = 0 );
            nbeds_male_free := (sp.male_beds - nquant_male_occupied + nquant_male_plan_out - nquant_male_plan_in)::numeric;
            nbeds_fem_free := (sp.female_beds - nquant_fem_occupied + nquant_fem_plan_out - nquant_fem_plan_in)::numeric;
            nbeds_no_sex_free := (sp.no_sex_beds - nquant_no_sex_occupied + nquant_no_sex_plan_out)::numeric;
            IF nbeds_male_free < 0 THEN
                nbeds_no_sex_free := nbeds_no_sex_free + nbeds_male_free;
                nbeds_male_free := 0;

            END IF;
            IF nbeds_fem_free < 0 THEN
                nbeds_no_sex_free := nbeds_no_sex_free + nbeds_fem_free;
                nbeds_fem_free := 0;

            END IF;
            IF DICTIONARY_EXISTS(tprof,sp.profile_id::varchar) THEN
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.allb := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).allb + nbeds_no_sex_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.female := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).female + nbeds_fem_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.male := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).male + nbeds_male_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);

            ELSE
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.allb := nbeds_no_sex_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.female := nbeds_fem_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.male := nbeds_male_free;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.prof_code := sp.profile_code;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);
                or2pgTmpVar0_0 := ((DICTIONARY_GET(tPROF,sp.profile_id))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof);
                or2pgTmpVar0_0.prof_name := sp.profile_name;
                tPROF := DICTIONARY_SET(tPROF,or2pgTmpVar0_0,sp.profile_id);

            END IF;
            ni := ni + 1;
        END LOOP;
    END LOOP;
    IF ni > 0 THEN
        ni := nullif(DICTIONARY_FIRST(tprof),'')::numeric;
        WHILE ni IS NOT NULL
        LOOP
            sstr_profile_free := concat(sstr_profile_free, '[', ((DICTIONARY_GET(tPROF,ni))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).prof_name, '] Муж.-', ((DICTIONARY_GET(tPROF,ni))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).male, ' Жен.-', ((DICTIONARY_GET(tPROF,ni))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).female, ' Общ.-', ((DICTIONARY_GET(tPROF,ni))::d_pkg_hpk_plan_journals.get_quant_beds_profiles_new__ntprof).allb, ' ');
            ni := nullif(DICTIONARY_NEXT(tprof,ni::varchar),'')::numeric;
        END LOOP;

    END IF;
    CALL dbms_output.put_line(concat('Результат: ', sstr_profile_free));
    return ( sstr_profile_free );
END
$function$
```

---

### Функция №17: d_pkg_catalogs.find_root_catalog

```sql
CREATE OR REPLACE PROCEDURE d_pkg_catalogs.find_root_catalog(IN pnraise numeric, IN pnlpu numeric, IN psunitcode character varying, INOUT pncatalog numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
    nVER_LPU d_unitlist.ver_lpu%TYPE;
BEGIN
    pncatalog := null;
    nver_lpu := d_pkg_unitlist.get_param(psunitcode,'VER_LPU')::numeric;
    BEGIN
        IF nver_lpu = 0 THEN
            CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, psunitcode, nversion);
            SELECT
                t.id
            INTO STRICT pncatalog
            FROM
                d_catalogs t
            WHERE
                t.c_unitcode = psunitcode
                     AND t.version = nversion::bigint
                     AND t.lpu IS NULL
                     AND t.c_level = 0;

        ELSIF nver_lpu = 1 THEN
            SELECT
            t.id
        INTO STRICT pncatalog
        FROM
            d_catalogs t
        WHERE
            t.c_unitcode = psunitcode
                 AND t.lpu = pnlpu::bigint
                 AND t.version IS NULL
                 AND t.c_level = 0;

        ELSE
            SELECT
                t.id
            INTO STRICT pncatalog
            FROM
                d_catalogs t
            WHERE
                t.c_unitcode = psunitcode
                     AND t.lpu IS NULL
                     AND t.version IS NULL
                     AND t.c_level = 0;

        END IF;
        EXCEPTION
            WHEN no_data_found THEN
                        IF pnraise = 1 THEN
                    PERFORM d_p_exc(1,(concat('2.1. Не найден корневой каталог для раздела "', psunitcode, '".'))::varchar);

                ELSE
                    pncatalog := null;

                END IF;

            WHEN too_many_rows THEN
                        IF pnraise = 1 THEN
                    PERFORM d_p_exc(1,(concat('2.2. Найдено более одного корневого каталога для раздела "', psunitcode, '".'))::varchar);

                ELSE
                    pncatalog := null;

                END IF;

    END;
END
$procedure$
```

---

### Функция №18: d_pkg_urprivs.get_standart_privs

```sql
CREATE OR REPLACE PROCEDURE d_pkg_urprivs.get_standart_privs(IN pnlpu numeric, IN psunitcode character varying, IN pncid numeric, INOUT pninsert integer, INOUT pnupdate integer, INOUT pndelete integer, INOUT pnmove_out integer)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    iRESULT INTEGER;
    nVERSION numeric(17);
    rUNIT d_unitlist;
    nUSER d_users.id%TYPE;
    or2pgTmpVar0_0 varchar;
    or2pgTmpVar1_1 varchar;
    or2pgTmpVar2_2 varchar;
    x record;
BEGIN
    pnmove_out := null;
    pndelete := null;
    pnupdate := null;
    pninsert := null;
    pninsert := 0;
    pnupdate := 0;
    pndelete := 0;
    pnmove_out := 0;
    SELECT
        unitcode,
        use_catalogs,
        ver_lpu
    INTO or2pgTmpVar0_0, or2pgTmpVar1_1, or2pgTmpVar2_2
    FROM
        d_unitlist
    WHERE
        unitcode = psunitcode;
    runit.unitcode := or2pgTmpVar0_0;
    runit.use_catalogs := or2pgTmpVar1_1;
    runit.ver_lpu := or2pgTmpVar2_2;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.unit_not_found(1,psunitcode);

    END IF;
    --  Поиск ID пользователя
    SELECT
        t.id
    INTO STRICT nuser
    FROM
        d_users t
    WHERE
        (t.username)::varchar = d_f_get_users();
    FOR x IN (
        SELECT
            *
        FROM
            d_unitbps u
        WHERE
            u.unitcode = psunitcode
                 AND u.standard_action IN ( 'INSERT' , 'UPDATE' , 'DELETE' , 'MOVE_OUT' ))
    LOOP
        iresult := 0;
        /*  если задан каталог то проверка по нему, и принадлежность организации или версии не принципиальна
 поиск по каталогу (раздел с каталогами) */
        IF pncid IS NOT NULL THEN
            SELECT
                count(1)
            INTO STRICT iresult
            WHERE
                exists ( SELECT
                    null as null
                FROM
                    d_urprivs up
                    CROSS JOIN                     d_unitbpprivs cp
                WHERE
                    up.id = cp.pid
                         AND cp.unitbp = x.unitbpcode::varchar
                         AND up.unitcode = x.unitcode::varchar
                         AND up.catalog = pncid::bigint
                         AND ( (up.username)::varchar = d_f_get_users()
                         OR exists ( SELECT
                        null as null
                    FROM
                        d_userroles ur
                    WHERE
                        ur.roleid = up.roleid
                             AND ur.sysuser = nuser ) )   /* + ORDERED  */ );

        --  поиск по организации (раздел делится по организациям)
        ELSIF runit.ver_lpu = 1 THEN
            SELECT
            count(1)
        INTO STRICT iresult
        WHERE
            exists ( SELECT
                null as null
            FROM
                d_urprivs up
                CROSS JOIN                 d_unitbpprivs cp
            WHERE
                up.id = cp.pid
                     AND cp.unitbp = x.unitbpcode::varchar
                     AND up.unitcode = x.unitcode::varchar
                     AND up.lpu = pnlpu::bigint
                     AND ( (up.username)::varchar = d_f_get_users()
                     OR exists ( SELECT
                    null as null
                FROM
                    d_userroles ur
                WHERE
                    ur.roleid = up.roleid
                         AND ur.sysuser = nuser ) )   /* + ORDERED  */ );

        --  поиск по версии (раздел делится по версиям)
        ELSIF runit.ver_lpu = 0 THEN
            CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, psunitcode, nversion);
        SELECT
            count(1)
        INTO STRICT iresult
        WHERE
            exists ( SELECT
                null as null
            FROM
                d_urprivs up
                CROSS JOIN                 d_unitbpprivs cp
            WHERE
                up.id = cp.pid
                     AND cp.unitbp = x.unitbpcode::varchar
                     AND up.unitcode = x.unitcode::varchar
                     AND up.version = nversion::bigint
                     AND ( (up.username)::varchar = d_f_get_users()
                     OR exists ( SELECT
                    null as null
                FROM
                    d_userroles ur
                WHERE
                    ur.roleid = up.roleid
                         AND ur.sysuser = nuser ) )   /* + ORDERED  */ );

        --  поиск по разделу (раздел не делится ни по организациям, ни по версиям)
        ELSIF runit.ver_lpu = 2 THEN
            SELECT
            count(1)
        INTO STRICT iresult
        WHERE
            exists ( SELECT
                null as null
            FROM
                d_urprivs up
                CROSS JOIN                 d_unitbpprivs cp
            WHERE
                up.id = cp.pid
                     AND cp.unitbp = x.unitbpcode::varchar
                     AND up.unitcode = x.unitcode::varchar
                     AND ( (up.username)::varchar = d_f_get_users()
                     OR exists ( SELECT
                    null as null
                FROM
                    d_userroles ur
                WHERE
                    ur.roleid = up.roleid
                         AND ur.sysuser = nuser ) )   /* + ORDERED  */ );

        ELSE
            PERFORM d_p_exc(1,(concat('Для проверки прав недостаточно информации: LPU = ', pnlpu, ', CATALOG = ', pncid, ', UNITBP = ', x.unitbpcode, '. Обратитесь к Администратору.'))::varchar);

        END IF;
        --  возврат результата
                IF iresult = 1 THEN
            IF x.standard_action = 'INSERT' THEN
                pninsert := 1;

            ELSIF x.standard_action = 'UPDATE' THEN
                pnupdate := 1;

            ELSIF x.standard_action = 'DELETE' THEN
                pndelete := 1;

            ELSIF x.standard_action = 'MOVE_OUT' THEN
                pnmove_out := 1;

            ELSE
                null;

            END IF;

        END IF;
    END LOOP;
END
$procedure$
```

---

### Функция №19: d_pkg_hosp_histories.set_discard_status

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hosp_histories.set_discard_status(IN pnid numeric, IN pnlpu numeric, IN pndiscard_status numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    dDATE_OUT timestamp(0);
    dDATE_IN timestamp(0);
    sHH_NUMB d_hosp_histories.hh_numb%TYPE;
    sHH_PREF d_hosp_histories.hh_pref%TYPE;
    sHH_NUMB_ALTERN d_hosp_histories.hh_numb_altern%TYPE;
    sHH_NUMB_TYPE d_hosp_histories.hh_numb_type%TYPE;
    nPATIENT d_hosp_histories.patient%TYPE;
    nHPK_PLAN_JOURNAL d_hosp_histories.hpk_plan_journal%TYPE;
    nFND NUMERIC;
    nHSC_RESULT NUMERIC(1);
    sHH_NUMB_MASK d_hosp_histories.hh_numb_mask%TYPE;
    sHH_NUMB_FULL d_hosp_histories.hh_numb_full%TYPE;
    nHAVE_HSC NUMERIC(1);
    rHH d_hosp_histories;
    nRELATIVE_HH NUMERIC(17);
    nDISEASECASE NUMERIC(17);
    y record;
    r record;
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HOSP_HISTORIES_SET_DISCART_STATUS', pnid);
    --  Проверка, выписана ли ИБ
    SELECT
        hh.date_in,
        hh.date_out,
        hh.hh_numb,
        hh.hh_pref,
        hh.hh_numb_altern,
        hh.hh_numb_type,
        hh.patient,
        hh.hpk_plan_journal,
        hh.hh_numb_mask,
        ( SELECT
            count(1)
        FROM
            d_hosp_stat_cards t
        WHERE
            t.hosp_history = hh.id
                 AND t.lpu = pnlpu::bigint ),
        hh.relative_hh,
        hh.diseasecase
    INTO STRICT ddate_in, ddate_out, shh_numb, shh_pref, shh_numb_altern, shh_numb_type, npatient, nhpk_plan_journal, shh_numb_mask, nhave_hsc, nrelative_hh, ndiseasecase
    FROM
        d_hosp_histories hh
    WHERE
        hh.id = pnid::bigint;
    IF pndiscard_status IN ( 1 , 2 ) THEN
        SELECT
            count ( * )
        INTO STRICT nfnd
        FROM
            ( SELECT
                    *
                FROM
                    d_hp_meas_prescs mp
                    CROSS JOIN                     d_hosp_histories hh
                WHERE
                    hh.id = pnid::bigint
                         AND mp.diseasecase = hh.diseasecase
                         AND mp.mp_condition IN ( 0 , 1 , 2 )
                     LIMIT 1 ) t_alias_0;
        IF nfnd = 1 THEN
            IF pndiscard_status = 1 THEN
                PERFORM d_p_exc(1,'5. Запрещено аннулировать историю болезни. У пациента есть действующие измерения.Для аннулирования ИБ необходимо сначала отменить назначенные измерения.');

            ELSIF pndiscard_status = 2 THEN
                PERFORM d_p_exc(1,'5.1. Запрещено направлять на списание историю болезни. У пациента есть действующие измерения. Необходимо сначала отменить назначенные измерения.');

            END IF;

        END IF;

    END IF;
    IF ddate_out IS NOT NULL THEN
        IF pndiscard_status = 1 THEN
            PERFORM d_p_exc(1,'1.0 Запрещено аннулировать выписанную историю болезни.');

        ELSIF pndiscard_status = 2 THEN
            PERFORM d_p_exc(1,'1.1 Запрещено направлять на списание выписанную историю болезни.');

        END IF;

    ELSIF nhave_hsc != 0
         AND coalesce(d_pkg_options.get('HSCIdenticalToHH',pnlpu)::numeric,0) = 0 THEN
        IF pndiscard_status = 1 THEN
        PERFORM d_p_exc(1,'1.2 Запрещено аннулировать историю болезни, по которой создана статкарта.');

    ELSIF pndiscard_status = 2 THEN
        PERFORM d_p_exc(1,'1.3 Запрещено направлять на списание историю болезни, по которой создана статкарта.');

    END IF;

    ELSE
        --  Аннулируем ИБ сопровождающих
         FOR y IN (
            SELECT
                *
            FROM
                d_hosp_histories t
            WHERE
                t.relative_hh = pnid::bigint)
        LOOP
            CALL d_pkg_hosp_histories.set_discard_status((y.id)::numeric, (y.lpu)::numeric, pndiscard_status);
        END LOOP;
        IF pndiscard_status IN ( 1 , 2 ) THEN
            SELECT
                count ( * )
            INTO STRICT nfnd
            FROM
                ( --  Изменим номер для ИБ аннулированных под одним номером
                    SELECT
                        *
                    FROM
                        d_hosp_histories hh
                    WHERE
                        hh.lpu = pnlpu::bigint
                             AND hh.discard_status = pndiscard_status
                             AND hh.hh_pref = shh_pref
                             AND ( hh.hh_numb_type = shh_numb_type
                             OR nullif(hh.hh_numb_type,'') IS NULL
                             AND nullif(shh_numb_type,'') IS NULL )
                             AND hh.hh_numb = shh_numb
                         LIMIT 1 ) t_alias_1;
            IF nfnd <> 0 THEN
                IF dbms_lob.instr(shh_numb,'#') > 0 THEN
                    shh_numb := substr2(shh_numb,1,dbms_lob.instr(shh_numb,'#') - 1);

                END IF;
                SELECT
                    coalesce(max(to_number(replace(hh.hh_numb,concat(shh_numb, '#'),''))),- 1)
                INTO STRICT nfnd
                FROM
                    d_hosp_histories hh
                WHERE
                    hh.lpu = pnlpu::bigint
                         AND hh.discard_status = pndiscard_status
                         AND hh.hh_pref = shh_pref
                         AND ( hh.hh_numb_type = shh_numb_type
                         OR nullif(hh.hh_numb_type,'') IS NULL
                         AND nullif(shh_numb_type,'') IS NULL )
                         AND hh.hh_numb like concat(shh_numb, '#%')
                         AND nullif(ltrim(replace(hh.hh_numb,concat(shh_numb, '#'),''),'0123456789'),'') IS NULL;
                shh_numb := concat(shh_numb, '#', to_char(nfnd + 1));

            END IF;

        END IF;
        --  Проверка, госпитализирован ли данный пациент
                IF pndiscard_status = 0 THEN
            SELECT
                count ( * )
            INTO STRICT nfnd
            FROM
                ( SELECT
                        *
                    FROM
                        d_hosp_histories hh
                    WHERE
                        hh.patient = npatient
                             AND hh.id != pnid::bigint
                             AND hh.discard_status = 0
                             AND d_pkg_dat_tools.date_ranges_error((hh.date_in)::timestamp,(hh.date_out)::timestamp,ddate_in,ddate_out,1) != (0)::NUMERIC
                         LIMIT 1 ) t_alias_2;
            IF nfnd > 0 THEN
                PERFORM d_p_exc(1,'3. Данный пациент уже госпитализирован.');

            END IF;
            SELECT
                count ( * )
            INTO STRICT nfnd
            FROM
                ( SELECT
                        *
                    FROM
                        d_hosp_histories hh
                    WHERE
                        hh.hpk_plan_journal = nhpk_plan_journal
                             AND hh.discard_status = 0
                             AND hh.id != pnid::bigint
                             AND NOT exists ( SELECT
                            null as null
                        FROM
                            d_hosp_histories hh1
                        WHERE
                            hh1.hpk_plan_journal = nhpk_plan_journal
                                 AND hh1.relative_hh = hh.id )
                             AND NOT exists ( SELECT
                            null as null
                        FROM
                            d_hosp_histories hh1
                        WHERE
                            hh1.hpk_plan_journal = nhpk_plan_journal
                                 AND hh1.id = hh.relative_hh )
                         LIMIT 1 ) t_alias_3;
            IF nfnd > 0 THEN
                PERFORM d_p_exc(1,'4. По данному направлению пациент уже госпитализирован.');

            END IF;

        END IF;
        shh_numb_full := d_pkg_hosp_histories.get_hh_numb_full(shh_pref,shh_numb,shh_numb_altern,shh_numb_mask)::varchar;
        SELECT
            count(1)
        INTO STRICT nfnd
        FROM
            d_hosp_histories hh 
                    JOIN     d_hosp_history_deps hhd ON hhd.pid = hh.id  
                    JOIN     d_mp_prescribes mp ON mp.hh_dep = hhd.id 
        WHERE
            hh.id = pnid::bigint
                 AND hh.lpu = pnlpu::bigint
                 AND mp.mp_condition IN ( 2 , 3 );
        IF nfnd > 0
     AND pndiscard_status = 1 THEN
            PERFORM d_p_exc(1,'5. У пациента есть исполненные лекарственные назначения. Аннулирование запрещено. Для аннулирования ИБ необходимо сначала отменить исполнение лекарственных назначений');

        END IF;
        update d_hosp_histories t set discard_status = pndiscard_status , hh_numb = shh_numb , hh_numb_full = shh_numb_full where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
        IF pndiscard_status IN ( 1 , 2 ) THEN
            CALL d_pkg_hosp_histories.clear_dep_beds(pnid, pnlpu);
            IF pndiscard_status = 1 THEN
                -- Отмена услуг
                 FOR r IN (
                    SELECT
                        t2.id,
                        t3.se_name
                    FROM
                        d_hosp_histories t1
                        CROSS JOIN                         d_hosp_history_deps t4
                        CROSS JOIN                         d_direction_services t2
                        CROSS JOIN                         d_services t3
                    WHERE
                        t1.id = pnid::bigint
                             AND t1.lpu = pnlpu::bigint
                             AND t2.hh_dep = t4.id
                             AND t4.pid = t1.id
                             AND t3.id = t2.service 
UNION ALL
 SELECT
                        t22.id,
                        t33.se_name
                    FROM
                        d_direction_services t22
                        CROSS JOIN                         d_services t33
                    WHERE
                        t22.lpu = pnlpu::bigint
                             AND t22.diseasecase = ndiseasecase::bigint
                             AND t22.hh_dep IS NULL
                             AND t33.id = t22.service)
                LOOP
                    BEGIN
                        CALL d_pkg_direction_services.cancel(pnlpu, (r.id)::numeric, sysdate()::timestamp, 'Аннулирование ИБ');
                        EXCEPTION
                            WHEN others THEN
                                        PERFORM d_p_exc(1,(concat('2. При аннулировании истории болезни отмена оказанных услуг произошла с ошибкой: ', d_pkg_msg.get_error((sqlerrm)::varchar), chr(10), 'Услуга: ', r.se_name, ' ', r.id))::varchar);

                    END;
                END LOOP;

            END IF;

        ELSE
            IF nhpk_plan_journal IS NOT NULL THEN
                CALL d_pkg_hpk_plan_journals.set_record_status((nhpk_plan_journal)::numeric, pnlpu, 1);

            END IF;

        END IF;

    END IF;
    IF ( d_pkg_option_specs.get('HSCIdenticalToHH',pnlpu)::numeric = 1
     OR d_pkg_option_specs.get('HSCCreateFromHH',pnlpu)::numeric = 1 ) THEN
        IF pndiscard_status = 1 THEN
            nhsc_result := d_pkg_hosp_stat_cards.upd_hsc_by_hosp_history(pnid,pnlpu,'UPD',(null)::numeric,(null)::varchar,(null)::varchar,(null)::varchar,(null)::varchar,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::timestamp,(null)::timestamp,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric)::numeric;

        ELSE
            --  Если необходимо, правим статкарту
            SELECT
                hh.*
            INTO STRICT rhh
            FROM
                d_hosp_histories hh
            WHERE
                hh.id = pnid::bigint;
            nhsc_result := d_pkg_hosp_stat_cards.upd_hsc_by_hosp_history((rhh.id)::numeric,(rhh.lpu)::numeric,'UPD',rhh.discard_status,rhh.hh_pref,rhh.hh_numb,rhh.hh_numb_altern,rhh.hh_numb_full,(rhh.patient)::numeric,(rhh.lpu_from)::numeric,(rhh.relative)::numeric,(rhh.hosp_reason)::numeric,(rhh.hospitalization_type)::numeric,rhh.hosp_times,rhh.date_in,rhh.date_out,(rhh.hosp_result)::numeric,(rhh.reception_emp)::numeric,rhh.is_well_timed_hosp,rhh.is_enough_volume,rhh.is_correct_healing,rhh.is_same_diagn,rhh.hosp_hour,(rhh.hosp_outcome)::numeric)::numeric;

        END IF;

    END IF;
    IF pndiscard_status = 1 THEN
        FOR r IN (
            SELECT
                *
            FROM
                d_smp_call_ex_system es
            WHERE
                es.hh_id = pnid::bigint)
        LOOP
            CALL d_pkg_smp_call_ex_system.set_hosp_status((r.id)::numeric, pnlpu, 3, (r.hpk_id)::numeric, (null)::numeric, (null)::numeric);
        END LOOP;

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HOSP_HISTORIES_SET_DISCART_STATUS', pnid);
END
$procedure$
```

---

### Функция №20: d_pkg_wlh_requests.upd_by_hh_set_discard

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_set_discard(IN pnhh_id numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status )
    INTO nwhl_id, ncurrent_status_code
    FROM
        d_hosp_histories hh 
                JOIN     d_wlh_requests req ON req.hpk_plan_journal = hh.hpk_plan_journal 
    WHERE
        hh.id = pnhh_id::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 4
     AND ncurrent_status_code != 10 THEN
        return;

    END IF;
    CALL d_pkg_wlh_requests.set_status_by_code(pnID => nwhl_id, pnLPU => pnlpu, pnCODE => ((CASE
    ncurrent_status_code     WHEN 4 THEN 10   /*  4 - Госпитализирован -> 10 - ИБ направлена на удаление */
    WHEN 10 THEN 11   /*  10 - ИБ направлена на удаление -> 11 - ИБ аннулирована */
END))::numeric   /*  ИБ направлена на удаление */, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №21: d_pkg_wlh_requests.upd_by_hh_remove_discard

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_remove_discard(IN pnhh_id numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status )
    INTO nwhl_id, ncurrent_status_code
    FROM
        d_hosp_histories hh 
                JOIN     d_wlh_requests req ON req.hpk_plan_journal = hh.hpk_plan_journal 
    WHERE
        hh.id = pnhh_id::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 10 THEN
        return;

    END IF;
    CALL d_pkg_wlh_requests.set_status_by_code(pnID => nwhl_id, pnLPU => pnlpu, pnCODE => 4   /*  Госпитализирован */, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №22: d_pkg_hosp_histories.check_hosp_one_time

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hosp_histories.check_hosp_one_time(IN pnlpu numeric, IN pnpatient numeric, IN pddate_in timestamp without time zone, IN pddate_out timestamp without time zone, INOUT pserr character varying, INOUT pswarn character varying)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nAGENT d_agents.id%TYPE;
    sHH_NUMB d_hosp_histories.hh_numb%TYPE;
    sHH_PREF d_hosp_histories.hh_pref%TYPE;
    sLPU d_lpu.fullname%TYPE;
    nOPTION_VALUE NUMERIC(1);
    nHH_TYPE d_hosp_histories.hh_type%TYPE;
    nEXISTS_PSY_ANAMN_FT integer := 0;
    nPSY_ANAMN_FT_VERS NUMERIC(17);
    nHH_LPU NUMERIC(17);
    nHIDEHOSPFROMLPUS NUMERIC(1);
BEGIN
    pswarn := null;
    pserr := null;
    pserr := null;
    pswarn := null;
    noption_value := d_pkg_options.get('HHOtherLPUOneTime',pnlpu)::numeric;
    IF noption_value IN ( 1 , 2 ) THEN
        SELECT
            p.agent
        INTO STRICT nagent
        FROM
            d_persmedcard p
        WHERE
            p.id = pnpatient::bigint
                 AND p.lpu = pnlpu::bigint;
        SELECT
            hh.hh_numb,
            hh.hh_pref,
            l.fullname,
            hh.hh_type,
            hh.lpu
        INTO shh_numb, shh_pref, slpu, nhh_type, nhh_lpu
        FROM
            d_hosp_histories hh 
                    JOIN     d_persmedcard pmc ON pmc.id = hh.patient  
                    JOIN     d_lpu l ON l.id = hh.lpu 
        WHERE
            hh.lpu <> pnlpu::bigint
                 AND hh.discard_status = 0
                 AND d_pkg_dat_tools.date_ranges_error((hh.date_in)::timestamp,(hh.date_out)::timestamp,coalesce(pddate_in,sysdate())::timestamp,pddate_out) <> (0)::NUMERIC
                 AND pmc.agent = nagent
                 AND hh.date_out IS NULL
             LIMIT 1;
        IF NOT FOUND THEN
            shh_numb := null;
            shh_pref := null;
            slpu := null;

        END IF;
        IF nullif(shh_numb,'') IS NOT NULL THEN
            /* если тип истории болезни (в другом ЛПУ) 4 - "История болезни псих./нарк. больного",
то проверим, нет ли у пациента принудительного лечения по решению суда */
            IF coalesce(nhh_type,0) = 4 THEN
                npsy_anamn_ft_vers := d_pkg_versions.get_version_by_lpu(0,pnlpu,'AGENT_PSY_ANAMN_FT')::numeric;
                SELECT
                    coalesce(max(1)::numeric,0)
                INTO STRICT nexists_psy_anamn_ft
                FROM
                    d_agent_psy_anamn_ft ft 
                            JOIN     d_agent_psy_anamnesis a ON ft.pid = a.id 
                WHERE
                    a.pid = nagent
                         AND ft.date_begin IS NOT NULL
                         AND ft.date_end IS NULL
                         AND ft.version = npsy_anamn_ft_vers::bigint;

            END IF;
            nhidehospfromlpus := d_pkg_options.get('HideHospFromLPUs',nhh_lpu)::numeric;
            -- показываем сообщения на клиенте, только если не на принудительном лечении в другом ЛПУ
                        IF nexists_psy_anamn_ft = 0 THEN
                IF nhidehospfromlpus = 0 THEN
                    IF noption_value = 1 THEN
                        pserr := concat('У контрагента имеется открытая история болезни ', shh_pref, '-', shh_numb, ' в ЛПУ ', slpu);

                    ELSIF noption_value = 2 THEN
                        pswarn := concat('У контрагента имеется открытая история болезни ', shh_pref, '-', shh_numb, ' в ЛПУ ', slpu);

                    END IF;

                END IF;

            END IF;

        END IF;

    END IF;
END
$procedure$
```

---

### Функция №23: d_pkg_wlh_requests.upd_by_hh_add

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_add(IN pnhh_id numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
    nSTATUS numeric(17);
    nDEPARTMENT_ID numeric(17);
    dHOSP_DATE d_wlh_requests.hosp_date%TYPE;
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status ),
        hh.department_id,
        hh.date_in
    INTO nwhl_id, ncurrent_status_code, ndepartment_id, dhosp_date
    FROM
        d_hosp_histories hh 
                JOIN     d_wlh_requests req ON req.hpk_plan_journal = hh.hpk_plan_journal 
    WHERE
        hh.id = pnhh_id::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 8 THEN
        return;

    END IF;
    nversion := d_pkg_versions.get_version_by_lpu(1,pnlpu,'WLH_STATUSES');
    BEGIN
        SELECT
            t.id
        INTO STRICT nstatus
        FROM
            d_wlh_statuses t
        WHERE
            t.code = 4   /*  Госпитализирован */
                 AND t.version = nversion::bigint   /*  Отказ  */;
    END;
    nversion := d_pkg_versions.get_version_by_lpu(1,pnlpu,'WLH_REQUESTS');
    CALL d_pkg_bpenv.beforebp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'WLH_REQUESTS_UPDATE', nwhl_id);
    BEGIN
        update d_wlh_requests t set hosp_date = dhosp_date , hosp_dep = ndepartment_id , hosp_history = pnhh_id , main_status = nstatus where t.id = nwhl_id::bigint
             AND t.version = nversion::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,nwhl_id,'WLH_REQUESTS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'WLH_REQUESTS_UPDATE', nwhl_id);
    CALL d_pkg_wlh_statuses_history.set_status(pnWLH_REQUESTS_ID => nwhl_id, pnLPU => pnlpu, pnSTATUS => nstatus, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №24: d_pkg_wlh_requests.upd_by_hh_cancel

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_cancel(IN pnhpk_plan_journal numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status )
    INTO nwhl_id, ncurrent_status_code
    FROM
        d_wlh_requests req
    WHERE
        req.hpk_plan_journal = pnhpk_plan_journal::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 8 THEN
        return;

    END IF;
    CALL d_pkg_wlh_requests.set_status_by_code(pnID => nwhl_id, pnLPU => pnlpu, pnCODE => 12   /*  Отказ от госпитализации */, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №25: d_pkg_hpk_schedule_reg.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_schedule_reg.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HPK_SCHEDULE_REG_DELETE', pnid);
    BEGIN
        DELETE FROM d_hpk_schedule_reg hsr where hsr.id = pnid::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_SCHEDULE_REG');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HPK_SCHEDULE_REG_DELETE', pnid);
END
$procedure$
```

---

### Функция №26: d_pkg_tools.str_separate_to_ids

```sql
CREATE OR REPLACE FUNCTION d_pkg_tools.str_separate_to_ids(pssource_str character varying)
 RETURNS d_cl_id
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    sSEPARATOR varchar(1);
    sSOURCE_STR varchar(32000) := pssource_str;
    clIDS_COLLECTION numeric(17)[];
    nIND_BEG NUMERIC := 1;
    nIND_END NUMERIC;
    nCOUNT NUMERIC := 1;
BEGIN
    sseparator := d_pkg_tools.find_id_delimiter((ssource_str)::text);
    IF substr2(ssource_str,(- 1)::numeric) != sseparator THEN
        ssource_str := concat(ssource_str, sseparator);

    END IF;
    IF regexp_count(ssource_str,sseparator) > 0 THEN
        ncount := regexp_count(ssource_str,sseparator);

    END IF;
    clIDS_COLLECTION [ array_upper2(clIDS_COLLECTION) + 1] := null;
    nind_end := coalesce(dbms_lob.instr(ssource_str,sseparator,nind_beg),length(ssource_str) + 1);
    FOR ind IN 1 .. ncount
    LOOP
        clIDS_COLLECTION[ind] := nullif(substr2(ssource_str,nind_beg,nind_end - nind_beg),'')::numeric;
        nind_beg := nind_end + 1;
        nind_end := dbms_lob.instr(ssource_str,sseparator,nind_end + 1);
    END LOOP;
    return clids_collection;
END
$function$
```

---

### Функция №27: d_pkg_smp_call_ex_system.set_hosp_history

```sql
CREATE OR REPLACE PROCEDURE d_pkg_smp_call_ex_system.set_hosp_history(IN pnid numeric, IN pnlpu numeric, IN pnhh_id numeric, IN pnhpk_id numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
BEGIN
    CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, 'SMP_CALL_EX_SYSTEM', nversion);
    BEGIN
        update d_smp_call_ex_system t set hh_id = pnhh_id , hpk_id = pnhpk_id where t.id = pnid::bigint
             AND t.version = nversion::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'SMP_CALL_EX_SYSTEM');

    END IF;
END
$procedure$
```

---

### Функция №28: d_pkg_wlh_requests.upd_by_hh_remove

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_remove(IN pnhh_id numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
    nSTATUS numeric(17);
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status )
    INTO nwhl_id, ncurrent_status_code
    FROM
        d_hosp_histories hh 
                JOIN     d_wlh_requests req ON req.hpk_plan_journal = hh.hpk_plan_journal 
    WHERE
        hh.id = pnhh_id::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 4 THEN
        return;

    END IF;
    nversion := d_pkg_versions.get_version_by_lpu(1,pnlpu,'WLH_STATUSES');
    BEGIN
        SELECT
            t.id
        INTO STRICT nstatus
        FROM
            d_wlh_statuses t
        WHERE
            t.code = 8   /*  Добавлен в ЖГ */
                 AND t.version = nversion::bigint   /*  Отказ  */;
    END;
    CALL d_pkg_wlh_requests.set_status(pnID => nwhl_id, pnLPU => pnlpu, pnSTATUS => nstatus, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №29: d_pkg_hosp_histories.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hosp_histories.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nHSC_RESULT NUMERIC(1);
    nHPK_PLAN_JOURNAL NUMERIC(17);
    nEM_DIRECTION NUMERIC(17);
    nDIRECTION NUMERIC(17);
    nCNT NUMERIC;
    nRELATIVE_HH NUMERIC(17);
    nDISEASECASE NUMERIC(17);
    dDATE_IN timestamp(0);
    rDC d_diseasecases;
    nAT NUMERIC(17);
    sDC_CONTENT varchar(200);
    nMKB NUMERIC(17);
    dREGISTER_DATE timestamp(0);
    sDS_NAME d_diagnosis_stages.ds_name%TYPE;
    nSTAGE NUMERIC(17);
    nLAST_VIS NUMERIC(17);
    nHH_TYPE d_hosp_histories.hh_type%TYPE;
    rAGN_PREG d_agent_pregnancy;
    nBIRTH_LPU numeric(17);
    nLINK numeric(17);
    r record;
    crdep record;
    crhhd_b record;
    crhhd_h record;
    crhha record;
    crdg record;
    crhhsk record;
    crlpah record;
    crhhdiet record;
    w record;
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HOSP_HISTORIES_DELETE', pnid);
    CALL d_pkg_hosp_histories.checks('DEL', pnid, pnlpu, (null)::varchar, (null)::varchar, (null)::varchar, (null)::timestamp, (null)::timestamp, (null)::numeric, (null)::numeric, (null)::numeric, (null)::numeric, (null)::numeric, (null)::numeric, (null)::numeric);
    --  Если необходимо, удаляем статкарту
        IF d_pkg_option_specs.get('HSCIdenticalToHH',pnlpu)::numeric = 1
     OR d_pkg_option_specs.get('HSCCreateFromHH',pnlpu)::numeric = 1 THEN
        nhsc_result := d_pkg_hosp_stat_cards.upd_hsc_by_hosp_history(pnid,pnlpu,'DEL',(null)::numeric,(null)::varchar,(null)::varchar,(null)::varchar,(null)::varchar,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::timestamp,(null)::timestamp,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric,(null)::numeric)::numeric;

    END IF;
    --  Если у пациента заведены ИБ сопровождающих, то сначала удалим их
     FOR r IN (
        SELECT
            t.*
        FROM
            d_hosp_histories t
        WHERE
            t.relative_hh = pnid::bigint)
    LOOP
        CALL d_pkg_hosp_histories.del((r.id)::numeric, pnlpu);
    END LOOP;
    SELECT
        t.diseasecase,
        t.date_in
    INTO STRICT ndiseasecase, ddate_in
    FROM
        d_hosp_histories t
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    --  Если имеются услуги, оказанные после даты госпитализации, то выводим ошибку
    SELECT
        count(1)
    INTO STRICT ncnt
    FROM
        d_direction_services ds
        CROSS JOIN         d_visits v
        CROSS JOIN         d_hosp_history_deps hhd
    WHERE
        ds.id = v.pid
             AND ds.hh_dep = hhd.id
             AND ds.diseasecase = ndiseasecase::bigint
             AND v.visit_date >= ddate_in
             AND hhd.pid = pnid::bigint;
    IF ncnt != 0 THEN
        PERFORM d_p_exc(1,'Невозможно отменить госпитализацию, т.к. у пациента есть услуги, оказанные после госпитализации. Для отмены госпитализации необходимо отменить оказание услуг, дата оказания которых больше или равна дате поступления в стационар.');

    END IF;
    --  Удаление дочерних записей
     FOR crdep IN (
        SELECT
            hhd.id
        FROM
            d_hosp_history_deps hhd
        WHERE
            hhd.pid = pnid::bigint
                 AND hhd.lpu = pnlpu::bigint)
    LOOP
        FOR crhhd_b IN (
            SELECT
                h.id
            FROM
                d_hh_dep_beds h
            WHERE
                h.pid = crdep.id::bigint)
        LOOP
            CALL d_pkg_hh_dep_beds.del((crhhd_b.id)::numeric, pnlpu);
        END LOOP;
        FOR crhhd_h IN (
            SELECT
                h.id
            FROM
                d_hh_dep_healers h
            WHERE
                h.pid = crdep.id::bigint)
        LOOP
            CALL d_pkg_hh_dep_healers.del((crhhd_h.id)::numeric, pnlpu);
        END LOOP;
        CALL d_pkg_hosp_history_deps.del((crdep.id)::numeric, pnlpu);
    END LOOP;
    FOR crhha IN (
        SELECT
            j.id
        FROM
            d_hosp_history_archs j
        WHERE
            j.pid = pnid::bigint
                 AND j.lpu = pnlpu::bigint
ORDER BY j.arch_date desc, j.id desc)
    LOOP
        CALL d_pkg_hosp_history_archs.del((crhha.id)::numeric, pnlpu);
    END LOOP;
    FOR crdg IN (
        SELECT
            t.id
        FROM
            d_hosp_history_diagns t
        WHERE
            t.pid = pnid::bigint
                 AND t.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_hosp_history_diagns.del((crdg.id)::numeric, pnlpu);
    END LOOP;
    FOR crhhsk IN (
        SELECT
            hhsk.id
        FROM
            d_hh_streetkids hhsk
        WHERE
            hhsk.pid = pnid::bigint
                 AND hhsk.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_hh_streetkids.del((crhhsk.id)::numeric, pnlpu);
    END LOOP;
    FOR crlpah IN (
        SELECT
            l.id  link_id,
            l.out_unit_id  id
        FROM
            d_links l
        WHERE
            l.in_unit = 'HOSP_HISTORIES'
                 AND l.in_unit_id = pnid
                 AND l.out_unit = 'AGENT_PSY_ANAMN_HOSP')
    LOOP
        CALL d_pkg_links.del((crlpah.link_id)::numeric, pnlpu);
        CALL d_pkg_agent_psy_anamn_hosp.del((crlpah.id)::numeric, pnlpu);
    END LOOP;
    FOR crhhdiet IN (
        SELECT
            hhdiet.id
        FROM
            d_hh_dietaries hhdiet
        WHERE
            hhdiet.pid = pnid::bigint
                 AND hhdiet.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_hh_dietaries.del((crhhdiet.id)::numeric, pnlpu);
    END LOOP;
    CALL d_pkg_wlh_requests.upd_by_hh_del(pnHH_ID => pnid, pnLPU => pnlpu);
    BEGIN
        DELETE FROM d_hosp_histories t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint RETURNING t.hpk_plan_journal,t.relative_hh INTO nhpk_plan_journal, nrelative_hh;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HOSP_HISTORIES');

    END IF;
    --  Установка статуса отработки записи журнала госпитализации либо удаление вместе с направлением, если ИБ создана из журнала неотложной помощи
        IF nhpk_plan_journal IS NOT NULL THEN
        FOR w IN (
            SELECT
                ds.id
            FROM
                d_hpk_plan_journals hp 
                        JOIN     d_diseasecases d ON d.id = hp.diseasecase  
                        JOIN     d_direction_services ds ON ds.diseasecase = d.id  
                        JOIN     d_visits v ON v.pid = ds.id  
                        JOIN     d_hosp_histories hh ON hh.id = pnid::bigint 
            WHERE
                hp.id = nhpk_plan_journal::bigint
                     AND v.visit_date <= hh.date_in
                     AND ds.hh_dep IS NOT NULL)
        LOOP
            CALL d_pkg_direction_services.set_hh_dep(pnID => (w.id)::numeric, pnLPU => pnlpu, pnHH_DEP => (null)::numeric);
        END LOOP;
        SELECT
            (CASE
                WHEN e.id IS NOT NULL THEN k.direction
                ELSE null
            END),
            k.direction,
            k.register_date,
            ( SELECT
                count ( * )
            FROM
                ( SELECT
                        *
                    FROM
                        d_hosp_histories h
                    WHERE
                        h.hpk_plan_journal = k.id
                         LIMIT 1 ) t_alias_0 )
        INTO STRICT nem_direction, ndirection, dregister_date, ncnt
        FROM
            d_hpk_plan_journals k 
                    LEFT OUTER JOIN     d_directions d ON d.id = k.direction  
                    LEFT OUTER JOIN     d_emergencyjournal e ON e.visit = d.reg_visit 
        WHERE
            k.id = nhpk_plan_journal::bigint
                 AND true = true
                 AND true = true;
        IF nem_direction IS NOT NULL
     AND ncnt = 0 THEN
            CALL d_pkg_hpk_plan_journals.del(nhpk_plan_journal, pnlpu);
            CALL d_pkg_directions.del(nem_direction, pnlpu);

        ELSE
            CALL d_pkg_hpk_plan_journals.set_record_status(nhpk_plan_journal, pnlpu, 0);

        END IF;

    END IF;
    SELECT
        t.*
    INTO rdc
    FROM
        d_diseasecases t
    WHERE
        t.id = ndiseasecase::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        ndiseasecase := null;

    END IF;
    IF ndiseasecase IS NOT NULL THEN
        CALL d_pkg_diseasecases.upd(pnid => ndiseasecase, pnlpu => pnlpu, psdc_content => rdc.dc_content, pddc_opendate => rdc.dc_opendate, pndc_type => 0, pndc_source => 2);
        nlast_vis := d_pkg_diseasecases.get_last_visit(ndiseasecase,pnlpu,(null)::numeric,(null)::numeric,1);
        IF nlast_vis IS NOT NULL THEN
            BEGIN
                SELECT
                    d.mkb,
                    ds.ds_name,
                    d.stage
                INTO STRICT nmkb, sds_name, nstage
                FROM
                    d_visits v
                    CROSS JOIN                     d_vis_diagnosises d
                    CROSS JOIN                     d_diagnosis_stages ds
                WHERE
                    v.id = nlast_vis::bigint
                         AND d.pid = v.id
                         AND d.is_main = 0
                         AND ds.ds_code = d.stage;
                EXCEPTION
                    WHEN no_data_found THEN
                                null;

                    WHEN too_many_rows THEN
                                PERFORM d_p_exc(1,'4. Найдено несколько основных диагнозов последнего посещения.');

            END;
            IF nstage = 2 THEN
                CALL d_pkg_diseasecases.set_mkb(ndiseasecase, pnlpu, nmkb, nlast_vis, sds_name);

            ELSE
                CALL d_pkg_diseasecases.set_mkb(ndiseasecase, pnlpu, nmkb, nlast_vis);

            END IF;

        ELSE
            CALL d_pkg_diseasecases.set_mkb(ndiseasecase, pnlpu, (null)::numeric, (null)::numeric);

        END IF;
        --  Создаем АТ для услуг, оказанных до госпитализации
         FOR r IN (
            SELECT
                v.*
            FROM
                d_direction_services ds
                CROSS JOIN                 d_visits v
            WHERE
                ds.id = v.pid
                     AND ds.diseasecase = ndiseasecase::bigint
                     AND NOT exists ( SELECT
                    null as null
                FROM
                    d_amb_talon_visits atv
                WHERE
                    atv.visit = v.id ))
        LOOP
            nat := null;
            CALL d_pkg_visits.work_in_at((r.id)::numeric, pnlpu, nat);
        END LOOP;

    END IF;
    --  Обновляем родителя
        IF nrelative_hh IS NOT NULL THEN
        CALL d_pkg_hosp_histories.reset_relative(nrelative_hh, pnlpu);

    END IF;
    CALL d_pkg_agent_prgn_hosp.upd_by_direction(ndirection, pnlpu);
    nlink := d_pkg_links.find_link_id_out(pnLPU => pnlpu,psUNIT => 'HOSP_HISTORIES',pnUNIT_ID => pnid,psUNIT_OUT => 'AGENT_CHILDREN')::numeric;
    IF nlink IS NOT NULL THEN
        CALL d_pkg_links.del(nlink, pnlpu);

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HOSP_HISTORIES_DELETE', pnid);
END
$procedure$
```

---

### Функция №30: d_pkg_wlh_requests.upd_by_hh_reverse_cancel

```sql
CREATE OR REPLACE PROCEDURE d_pkg_wlh_requests.upd_by_hh_reverse_cancel(IN pnhpk_plan_journal numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    sOPTION NUMERIC(1) := coalesce(to_number(d_pkg_options.get(psSO_CODE => 'HPKWLH',pnLPU => pnlpu,pnRAISE => 0)),0);
    nWHL_ID numeric(17);
    nCURRENT_STATUS_CODE d_wlh_statuses.code%TYPE;
BEGIN
    IF soption = 0 THEN
        return;

    END IF;
    SELECT
        req.id,
        ( SELECT
            s.code
        FROM
            d_wlh_statuses s
        WHERE
            s.id = req.main_status )
    INTO nwhl_id, ncurrent_status_code
    FROM
        d_wlh_requests req
    WHERE
        req.hpk_plan_journal = pnhpk_plan_journal::bigint;
    IF NOT FOUND THEN
        return;

    END IF;
    IF ncurrent_status_code != 12 THEN
        return;

    END IF;
    CALL d_pkg_wlh_requests.set_status_by_code(pnID => nwhl_id, pnLPU => pnlpu, pnCODE => 8   /*  Добавлен в ЖГ */, psCOMMENT => (null)::varchar);
END
$procedure$
```

---

### Функция №31: d_pkg_directions.set_canceled

```sql
CREATE OR REPLACE PROCEDURE d_pkg_directions.set_canceled(IN pnid numeric, IN pnlpu numeric, IN pnis_canceled numeric, IN pncanc_reason numeric, IN pncanc_employer numeric, IN pdcanc_date timestamp without time zone, IN pscanc_employer_fio character varying DEFAULT NULL::character varying)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    rDIR d_directions;
    rHPK d_hpk_plan_journals;
    sFIO d_directions.canc_employer_fio%TYPE;
    nTMP numeric(17);
    nQUOTA_Q numeric(17);
    sHH_NUMB_FULL d_hosp_histories.hh_numb_full%TYPE;
    sHHRESULT_REFUSE varchar(300);
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'DIRECTIONS_UPDATE', pnid);
    SELECT
        d_pkg_options.get('HHRESULT_REFUSE',pnlpu)
    INTO shhresult_refuse;
    IF NOT FOUND THEN
        shhresult_refuse := null;

    END IF;
    BEGIN
        SELECT
            t1.*
        INTO STRICT rhpk
        FROM
            d_hpk_plan_journals t1
        WHERE
            t1.direction = pnid::bigint;
        EXCEPTION
            WHEN too_many_rows THEN
                        PERFORM d_p_exc(1,(concat('Найдено несколько записей в разделе Журнал госпитализации для направления с ID=', pnid))::varchar);

            WHEN no_data_found THEN
                        null;

    END;
    SELECT
        t.*
    INTO rdir
    FROM
        d_directions t
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'DIRECTIONS');

    END IF;
    IF rhpk.id IS NOT NULL
     AND pnis_canceled = 0
     AND rdir.is_canceled = 1 THEN
        CALL d_pkg_hpk_plan_journals.checks(pnID => (rhpk.id)::numeric, pnLPU => pnlpu, pnHPK => (rhpk.hpk)::numeric, pnHPK_PLAN => (rhpk.hpk_plan)::numeric, pnPATIENT => (rhpk.patient)::numeric, pnDIRECTED_BY => (rhpk.directed_by)::numeric, pdREGISTER_DATE => rhpk.register_date, pnOPERATION => (rhpk.directed_to)::numeric, pnPAYMENT_KIND => (rhpk.registered_by)::numeric, pnDIRECTION => (rhpk.operation)::numeric, pnIS_OPER => rhpk.is_oper, pdHH_DIRECTION_DATE => rhpk.hh_direction_date, pnRECORD_NUMB => rhpk.record_numb, psRECORD_PREF => rhpk.record_pref, pnSCH_RESOURCE => (rhpk.sch_resource)::numeric, pnHAS_PRIVILEGES => rhpk.has_privileges, pnVID => 1, pnQUOTA_Q => nquota_q);

    END IF;
    -- #161922 проверка на наличие ИБ
        IF pnis_canceled = 1 THEN
        SELECT
            max(hh.hh_numb_full)
        INTO STRICT shh_numb_full
        FROM
            d_hpk_plan_journals t
            CROSS JOIN             d_hosp_histories hh
            CROSS JOIN             d_hosp_results hr
        WHERE
            t.direction = pnid::bigint
                 AND hh.hpk_plan_journal = t.id
                 AND hr.id = coalesce(hh.hosp_result,hr.id)
                 AND ( nullif(shhresult_refuse,'') IS NULL
                 OR hr.r_code NOT  IN ( WITH RECURSIVE tmp AS (
    (
    SELECT regexp_substr(shhresult_refuse,'[^;]+',1,1)  regexp_substr, 1 as level
    )
    UNION
    (
    SELECT regexp_substr(shhresult_refuse,'[^;]+',1,tmp.level+1)  regexp_substr, tmp.level + 1 as level
    FROM tmp
    WHERE tmp.level+1 <= length(pg_catalog.regexp_replace(shhresult_refuse,'[^;]+','','g')) + 1
    )
)
SELECT  regexp_substr 
FROM tmp ) );
        IF nullif(shh_numb_full,'') IS NOT NULL THEN
            PERFORM d_p_exc(1,(concat('По данному направлению создана История болезни ', shh_numb_full, ', невозможно выполнить отказ в госпитализации. Для выполнения отказа необходимо отменить госпитализацию.'))::varchar);

        END IF;

    END IF;
    /* if rDIR.DIR_TYPE != 1 then
    D_P_EXC('Отказ возможно только для направлений на госпитализацию.');
  end if;
  if rDIR.IS_CANCELED = pnIS_CANCELED then
    D_P_EXC('Направление уже находится в указанном статусе.');
  end if;

update текущей записи */
    BEGIN
        update d_directions t set is_canceled = pnis_canceled , canc_reason = pncanc_reason , canc_employer = pncanc_employer , canc_employer_fio = pscanc_employer_fio , canc_date = pdcanc_date where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'DIRECTIONS');

    END IF;
    IF rdir.dir_type != 0 THEN
        IF pncanc_employer IS NOT NULL THEN
            SELECT
                concat(t2.surname, ' ', (CASE
                    WHEN ( t2.firstname = null ) OR ( check_null(t2.firstname,(null)::varchar) ) THEN (null)::varchar
                    ELSE (concat(' ', t2.firstname))::varchar
                END), (CASE
                    WHEN ( t2.lastname = null ) OR ( check_null(t2.lastname,(null)::varchar) ) THEN (null)::varchar
                    ELSE (concat(' ', t2.lastname))::varchar
                END))
            INTO sfio
            FROM
                d_employers t
                CROSS JOIN                 d_agents t2
            WHERE
                t.id = pncanc_employer::bigint
                     AND t.lpu = pnlpu::bigint
                     AND t.agent = t2.id;
            IF NOT FOUND THEN
                PERFORM d_pkg_msg.record_not_found(1,pncanc_employer,'EMPLOYERS');

            END IF;

        ELSE
            sfio := pscanc_employer_fio;

        END IF;
        -- Поиск записи по связи в другой МО
                IF rdir.outer_direction IS NOT NULL THEN
            SELECT
                t.represent_direction
            INTO ntmp
            FROM
                d_outer_directions t
            WHERE
                t.id = rdir.outer_direction;
            IF NOT FOUND THEN
                PERFORM d_pkg_msg.record_not_found(1,(rdir.outer_direction)::numeric,'OUTER_DIRECTIONS');

            END IF;

        ELSE
            -- поищем направление в другой МО
                        BEGIN
                SELECT
                    t1.id
                INTO STRICT ntmp
                FROM
                    d_outer_directions t
                    CROSS JOIN                     d_directions t1
                WHERE
                    t.represent_direction = pnid::bigint
                         AND t1.outer_direction = t.id;
                EXCEPTION
                    WHEN no_data_found THEN
                                null;

                    WHEN too_many_rows THEN
                                PERFORM d_p_exc(1,'Найдено несколько внешних направлений, соответствующих обрабатываемому направлению.');

            END;

        END IF;
        IF ntmp IS NOT NULL THEN
            -- #161922 проверка на наличие ИБ
                        IF pnis_canceled = 1 THEN
                SELECT
                    max(hh.hh_numb_full)
                INTO STRICT shh_numb_full
                FROM
                    d_hpk_plan_journals t
                    CROSS JOIN                     d_hosp_histories hh
                    CROSS JOIN                     d_hosp_results hr
                WHERE
                    t.direction = ntmp::bigint
                         AND hh.hpk_plan_journal = t.id
                         AND hr.id = hh.hosp_result
                         AND ( nullif(shhresult_refuse,'') IS NULL
                         OR (hr.r_code != all(string_to_varchar_array(shhresult_refuse,';')) and coalesce(hr.r_code,'') != '') );
                IF nullif(shh_numb_full,'') IS NOT NULL THEN
                    PERFORM d_p_exc(1,(concat('По данному направлению создана История болезни ', shh_numb_full, ', невозможно выполнить отказ в госпитализации. Для выполнения отказа необходимо отменить госпитализацию.'))::varchar);

                END IF;

            END IF;
            BEGIN
                update d_directions t set is_canceled = pnis_canceled , canc_reason = pncanc_reason , canc_employer = null , canc_employer_fio = sfio , canc_date = pdcanc_date where t.id = ntmp::bigint;
                EXCEPTION
                    WHEN others THEN
                                PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

            END;
            IF ( NOT FOUND ) THEN
                PERFORM d_pkg_msg.record_not_found(1,ntmp,'DIRECTIONS');

            END IF;

        END IF;

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'DIRECTIONS_UPDATE', pnid);
END
$procedure$
```

---

### Функция №32: d_pkg_cse_accesses.check_employer_right

```sql
CREATE OR REPLACE FUNCTION d_pkg_cse_accesses.check_employer_right(pnlpu numeric, pnemployer numeric, psunitcode character varying, pnunit_id numeric, psright character varying, pncablab numeric DEFAULT NULL::numeric, pnservice numeric DEFAULT NULL::numeric, pnraise numeric DEFAULT 0)
 RETURNS numeric
 LANGUAGE sql
 STABLE SECURITY DEFINER
AS $function$

WITH cse_accesses as (
  SELECT t.id 
  FROM  d_cse_accesses t 
  WHERE t.unitcode = psunitcode 
    AND t.unit_id = pnunit_id 
    AND t.lpu = pnlpu), 
cse_rights as (
  SELECT t.id 
  FROM d_cse_rights t 
  WHERE t.r_code = psright 
    AND t.unitcode = psunitcode),
emp_rights as (
  SELECT speciality, sysuser
  FROM ( SELECT speciality, sysuser
         FROM  d_employers t
         WHERE t.id = pnemployer
         UNION ALL
         SELECT NULL, min(t.id)
         FROM d_users t
         WHERE t.username = upper(utl.get_curr_user())
         ) a
   limit 1
) 
	SELECT 1
	FROM d_cse_acs_all_rights t, cse_accesses, cse_rights 
	WHERE t.pid = cse_accesses.id 
	  AND t.right = cse_rights.id 
	  AND t.lpu = pnlpu 
	UNION ALL 
	SELECT 1 
	FROM  d_cse_acs_cablabs t, d_cse_acs_cl_rights t1, cse_accesses, cse_rights 
	WHERE t.lpu = pnlpu 
	  AND t.pid = cse_accesses.id 
	  AND t.cablab = pncablab 
	  AND t1.lpu = pnlpu 
	  AND t1.pid = t.id 
	  AND t1.right = cse_rights.id 
	UNION ALL 
	SELECT 1 
	FROM d_cse_acs_servs t, d_cse_acs_serv_rights t1, cse_accesses, cse_rights 
	WHERE t.lpu = pnlpu 
	  AND t.pid = cse_accesses.id 
	  AND t.service = pnservice 
	  AND t1.lpu = pnlpu 
	  AND t1.pid = t.id 
	  AND t1.right = cse_rights.id 
	UNION ALL 
	SELECT 1 
	FROM d_cse_acs_emps t, d_cse_acs_emp_rights t1, cse_accesses, cse_rights 
	WHERE t.pid = cse_accesses.id 
	  AND t.employer = pnemployer 
	  AND t.lpu = pnlpu 
	  AND t1.pid = t.id 
	  AND t1.right = cse_rights.id 
	UNION ALL 
	SELECT 1 
	FROM  d_cse_acs_specs t, d_cse_acs_spec_rights t1, cse_accesses, cse_rights, emp_rights
	WHERE t.pid = cse_accesses.id 
	  AND t.lpu = pnlpu 
	  AND t.speciality = emp_rights.speciality 
	  AND t1.pid = t.id 
	  AND t1.right = cse_rights.id 
	UNION ALL 
	SELECT 1 
	FROM  d_cse_acs_roles t,  d_cse_acs_role_rights t1, d_userroles t2, d_users t3, cse_accesses, cse_rights, emp_rights
	WHERE t.pid = cse_accesses.id 
	  AND t.lpu = pnlpu 
	  AND t1.pid = t.id 
	  AND t1.right = cse_rights.id 
	  AND t2.roleid = t.role 
	  AND t2.sysuser = emp_rights.sysuser
	  AND t3.id = t2.sysuser
	UNION ALL
   SELECT 0
    LIMIT 1;
$function$
```

---

### Функция №33: d_pkg_employers.get_id

```sql
CREATE OR REPLACE FUNCTION d_pkg_employers.get_id(pnlpu numeric)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
DECLARE
    nRES d_employers.id%TYPE;   /* ID сотрудника */
BEGIN
    nres := d_pkg_ses.getcontext('MED','EMPLOYER')::bigint;
    IF nres IS NULL THEN
        BEGIN
            SELECT
                d.id
            INTO STRICT nres
            FROM
                d_employers d 
                        JOIN     d_users u ON u.id = d.sysuser
     AND u.username = upper(d_f_get_users()) 
            WHERE
                d.lpu = pnlpu::bigint
                 LIMIT 1;
            EXCEPTION
                WHEN no_data_found THEN
                            nres := null;

                WHEN too_many_rows THEN
                            PERFORM d_p_exc(1,(concat('1. Найдено несколько сотрудников в данном МО связанных с пользователем: ', upper(d_f_get_users())))::varchar);

        END;

    END IF;
    return nres;
END
$function$
```

---

### Функция №34: d_pkg_hpk_plan_journals.set_is_ready

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plan_journals.set_is_ready(IN pnid numeric, IN pnlpu numeric, IN pnis_ready numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    CALL d_pkg_hpk_plan_journals.exist(pnid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLAN_JOURNALS_UPDATE', pnid);
    BEGIN
        update d_hpk_plan_journals t set is_ready = coalesce(pnis_ready,0) where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U');

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_PLAN_JOURNALS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLAN_JOURNALS_UPDATE', pnid);
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
3) Все имена контролов должны писаться в кэмэлекйсе и начинаться с "ctrl" (пример "ctrlCode")
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
    <component cmptype="Action" name="acGetGw">
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
12) имена cmpAction должны начинаться с "ac" и дальше в кэмэл кейсе пример:
    не правильно :<component cmptype="ActionVar" name="AC_get_gw">
	правильно :<component cmptype="ActionVar" name="acGetGw">
    не правильно :<component cmptype="ActionVar" name="AC_save">
	правильно :<component cmptype="ActionVar" name="acSave">
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
	правильно :      if (getVar('action') == 'INSERT') {
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

35) Имя DataSetдолжно начинаться с маленьких букв "ds" и дальше в кэмэлкейсе  переписывается старое имя. это применимо для тэгов  cmpDataSet  и <component cmptype="DataSet"
36) После каждой JS функции нужно ставить ";" пример:
     не правильно :
	Form.onCreate = function() {
            ***
        }
    правильно:
	Form.onCreate = function() {
            ***
        };
36) Имя  cmptype="Grid" или cmpGrid начинаться с маленьких букв "grid" и дальше в кэмэлкейсе  переписывается старое имя.
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
47.9 Имена Action должны начинаться с `ac` и дальше в camelCase

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
    <component cmptype="ProtectedBlock" alert="true" modcode="HospitalIncome">
        <!-- ... остальной код формы ... -->
    </component>
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
2. **ProtectedBlock** (при наличии) - оборачивает весь основной контент формы
3. **Action компоненты** (все SQL/PLSQL блоки)
4. **DataSet компоненты** (все SQL/PLSQL блоки)
5. **Script компонент** (весь JavaScript код)
6. **HTML/таблицы и визуальные компоненты** (Grid, Edit, Button, и т.д.)
7. **Все остальные компоненты** (MaskInspector, DepControls, SubForm и др.) - могут находиться в любом месте

### 76.2 Важные замечания

- **SubForm**, **MaskInspector**, **DepControls** и другие вспомогательные компоненты **не подчиняются** данному правилу группировки и могут находиться в любом месте формы
- **ProtectedBlock** при наличии оборачивает **весь основной контент формы** (все компоненты, кроме самого ProtectedBlock и комментария)
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

    <component cmptype="ProtectedBlock" alert="true" modcode="HospitalIncome">
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

        <!-- ===== SCRIPT ===== -->
        <component cmptype="Script">
            <![CDATA[
            Form.onCreate = function() { ... };
            Form.onShow = function() { ... };
            Form.onButtonOKClick = function() { ... };
            ]]>
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
    </component>
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
- [ ] ProtectedBlock оборачивает основной контент (при наличии)
- [ ] Все Action компоненты сгруппированы вместе
- [ ] Action отсортированы по функциональному назначению (Select → Insert → Update → Delete → Check)
- [ ] Все DataSet компоненты сгруппированы вместе после Action
- [ ] Script компонент расположен после всех SQL/PLSQL компонентов
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
