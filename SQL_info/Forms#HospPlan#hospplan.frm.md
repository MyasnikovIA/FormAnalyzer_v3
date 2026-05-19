# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ Forms/HospPlan\hospplan.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm
**Статус:** ПОЛНОСТЬЮ ЗАМЕНЕНА
**Файл замены:** C:\AppServ\www\5_mis_MEDDEV-151210\UserFormsFNKC\HospPlan\hospplan.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Tue May 19 15:07:25 GMT+07:00 2026

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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
**Источник:** Forms/HospPlan\hospplan.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hospplan.frm

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

Вьюхи в PostgreSQL не найдены или не удалось получить DDL.


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Вьюхи в Oracle не найдены или не удалось получить DDL.


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Таблицы из PostgreSQL вьюх не найдены.


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Таблицы из Oracle вьюх не найдены.


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 34
- Загружено тел функций: 0

---

### Функция №1: D_PKG_SIGNAL_INFO_SETS.GET_FULL_SIGNAL_INFORMATION

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

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

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №7: D_PKG_CSE_ACCESSES.CHECK_RIGHT

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №8: D_PKG_HPK_PLANS.ADD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №9: D_PKG_VMP_LINKS.UPD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №10: D_PKG_HPK_PLAN_JOURNALS.DEL

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №11: D_PKG_HPK_PLAN_JOURNALS.SET_RECORD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №12: D_PKG_HPK_PLAN_JOURNALS.SET_NEXT_RECORD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №13: D_PKG_HPK_PLAN_JOURNALS.SET_PREV_RECORD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №14: D_PKG_HPK_PLAN_JOURNALS.SET_HH_DIRECTION_DATE

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №15: D_PKG_HPK_PLAN_JOURNALS.NEAREST_DATE_SEARCH

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №16: D_PKG_HPK_PLAN_JOURNALS.GET_QUANT_BEDS_PROFILES_NEW

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №17: D_PKG_CATALOGS.FIND_ROOT_CATALOG

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №18: D_PKG_URPRIVS.GET_STANDART_PRIVS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №19: D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №20: D_PKG_WLH_REQUESTS.UPD_BY_HH_SET_DISCARD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №21: D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE_DISCARD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №22: D_PKG_HOSP_HISTORIES.CHECK_HOSP_ONE_TIME

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №23: D_PKG_WLH_REQUESTS.UPD_BY_HH_ADD

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №24: D_PKG_WLH_REQUESTS.UPD_BY_HH_CANCEL

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №25: D_PKG_HPK_SCHEDULE_REG.DEL

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №26: D_PKG_TOOLS.STR_SEPARATE_TO_IDS

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №27: D_PKG_SMP_CALL_EX_SYSTEM.SET_HOSP_HISTORY

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №28: D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №29: D_PKG_HOSP_HISTORIES.DEL

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №30: D_PKG_WLH_REQUESTS.UPD_BY_HH_REVERSE_CANCEL

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №31: D_PKG_DIRECTIONS.SET_CANCELED

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №32: D_PKG_CSE_ACCESSES.CHECK_EMPLOYER_RIGHT

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №33: D_PKG_EMPLOYERS.GET_ID

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №34: D_PKG_HPK_PLAN_JOURNALS.SET_IS_READY

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 34
- Загружено тел функций: 0

---

### Функция №1: d_pkg_signal_info_sets.get_full_signal_information

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №2: d_pkg_cse_accesses.get_id_with_rights

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №3: d_pkg_std.frm_dt

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №4: d_pkg_hpk_plan_journals.get_hosp_history_status

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №5: d_pkg_std.frm_d

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №6: d_pkg_dat_tools.full_years

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №7: d_pkg_cse_accesses.check_right

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №8: d_pkg_hpk_plans.add

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №9: d_pkg_vmp_links.upd

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №10: d_pkg_hpk_plan_journals.del

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №11: d_pkg_hpk_plan_journals.set_record

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №12: d_pkg_hpk_plan_journals.set_next_record

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №13: d_pkg_hpk_plan_journals.set_prev_record

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №14: d_pkg_hpk_plan_journals.set_hh_direction_date

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №15: d_pkg_hpk_plan_journals.nearest_date_search

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №16: d_pkg_hpk_plan_journals.get_quant_beds_profiles_new

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №17: d_pkg_catalogs.find_root_catalog

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №18: d_pkg_urprivs.get_standart_privs

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №19: d_pkg_hosp_histories.set_discard_status

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №20: d_pkg_wlh_requests.upd_by_hh_set_discard

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №21: d_pkg_wlh_requests.upd_by_hh_remove_discard

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №22: d_pkg_hosp_histories.check_hosp_one_time

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №23: d_pkg_wlh_requests.upd_by_hh_add

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №24: d_pkg_wlh_requests.upd_by_hh_cancel

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №25: d_pkg_hpk_schedule_reg.del

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №26: d_pkg_tools.str_separate_to_ids

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №27: d_pkg_smp_call_ex_system.set_hosp_history

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №28: d_pkg_wlh_requests.upd_by_hh_remove

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №29: d_pkg_hosp_histories.del

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №30: d_pkg_wlh_requests.upd_by_hh_reverse_cancel

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №31: d_pkg_directions.set_canceled

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №32: d_pkg_cse_accesses.check_employer_right

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №33: d_pkg_employers.get_id

Тело функции/процедуры не найдено в PostgreSQL.

---

### Функция №34: d_pkg_hpk_plan_journals.set_is_ready

Тело функции/процедуры не найдено в PostgreSQL.



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
<FORM>
```
Покажи только текст  переработанный  формы


---

## ИСХОДНЫЙ ТЕКСТ ФОРМЫ

Ниже представлен исходный XML код анализируемой формы:

```xml
<div cmptype="bogus" oncreate="base().OnCreate();" onshow="base().OnShow();" name="BASE_FORM" style="height: 100%;" >
    <!--
       openWindow("HospPlan/hospplan");
    -->
    <component cmptype="SubForm" path="Markers/subscripts/subscript_markers" name="MarkersScripts"/>
	<component cmptype="SubForm" path="ArmPatientsInDep/SubForms/sep_calculation"/>
    <component cmptype="ProtectedBlock" alert="true" modcode="HospitalIncome">
		<!--
		Форма планы госпитализации по конкретной дате
			!-ComboBox "Журнал:" содержит только те записи, на которые у пользователя имеются права-!
		-->
  		<div cmptype="title">Планы госпитализации (по конкретной дате)</div>
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
            <component cmptype="ActionVar" name="pnLPU"         src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pdPLAN_DATE"   src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnPID"         src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnD_INSERT_ID" src="NewID"                srctype="var" put="" len="17"/>
        </component>
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
            <component cmptype="ActionVar" name="pnHPK_ID"                  src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnHAS_PAYMENT_CONSTRAINTS" src="HAS_PK_CONSTR"        srctype="var"  put="" len="2"/>
        </component>

  		<!--Action для проверки прав на отправку на ИБ-->
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
            <component cmptype="ActionVar" name="pnLPU"      src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnCABLAB"   src="CABLAB"               srctype="var"/>
            <component cmptype="ActionVar" name="pnPLANID"   src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnCHECK_IB" src="CHIB"                 srctype="var" put="" len="2"/>
        </component>

  		<!--Action для проверки прав на запись, редакт., удаление-->
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
            <component cmptype="ActionVar" name="pnLPU"       src="LPU"               srctype="var"/>
            <component cmptype="ActionVar" name="pnCABLAB"    src="CABLAB"            srctype="var"/>
            <component cmptype="ActionVar" name="pnPLANID"    src="C_HOSP_PLAN_KINDS" srctype="var"/>
            <component cmptype="ActionVar" name="pnCHECK_REC" src="CHREC"             srctype="var" put="" len="2"/>
        </component>

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
            <component cmptype="ActionVar" name="pnLPU" src="LPU"            srctype="var"/>
            <component cmptype="ActionVar" name="pnID"  src="HPK_PL_DAY_VAR" srctype="var"/>
        </component>

  		<!--Action: минус день-->
	  	<component cmptype="Action" name="prevDay">
			<![CDATA[
			begin
				:START_DATE := to_char(to_date(:CURR_DATE)-1,'dd.mm.yyyy');
			end;
			]]>
			<component cmptype="ActionVar" name="START_DATE" src="DDATE" srctype="ctrl" put="var1" len="11"/>
			<component cmptype="ActionVar" name="CURR_DATE"  src="DDATE" srctype="ctrl" get="var3"         />
	  	</component>

  		<!--Action: плюс день-->
	  	<component cmptype="Action" name="nextDay">
			<![CDATA[
			begin
				:START_DATE := to_char(to_date(:CURR_DATE)+1,'dd.mm.yyyy');
			end;
			]]>
			<component cmptype="ActionVar" name="START_DATE" src="DDATE" srctype="ctrl" put="var1" len="11"/>
			<component cmptype="ActionVar" name="CURR_DATE"  src="DDATE" srctype="ctrl" get="var3"         />
	  	</component>

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
            <component cmptype="ActionVar" name="pnLPU"          src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPATIENT"      src="PERSMEDCARD"          srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND_IN" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnEXIST"        src="EXIST"                srctype="var"  put="" len="2"/>
            <component cmptype="ActionVar" name="pdDATE"         src="DDATE"                srctype="ctrl" put="" len="11"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND"    src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_NEXT"    src="HAVE_NEXT"            srctype="var"  put="" len="2"/>
            <component cmptype="ActionVar" name="pnHAVE_PREV"    src="HAVE_PREV"            srctype="var"  put="" len="2"/>
        </component>
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
                :pnDIR_RESULT := 0; -- Успешное выполнение
              end if;
            end;
            ]]>
            <component cmptype="ActionVar" name="pnLPU"        src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="psDIR_PREF"   src="DIR_PREF"             srctype="ctrl"/>
            <component cmptype="ActionVar" name="psDIR_NUMB"   src="DIR_NUMB"             srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnDIR_RESULT" src="DIR_RESULT"           srctype="var"  put="" len="2"/>
            <component cmptype="ActionVar" name="pnPMC"        src="PMC_DIR"              srctype="var"  put="" len="17"/>
            <component cmptype="ActionVar" name="pnDIR"        src="DIR_DIR"              srctype="var"  put="" len="17"/>
            <component cmptype="ActionVar" name="pdDATE"       src="DDATE"                srctype="ctrl" put="" len="25"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND"  src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_NEXT"  src="DIR_HAVE_NEXT"        srctype="var"  put="" len="3"/>
            <component cmptype="ActionVar" name="pnHAVE_PREV"  src="DIR_HAVE_PREV"        srctype="var"  put="" len="3"/>
            <component cmptype="ActionVar" name="pnEXIST"      src="EXIST"                srctype="var"  put="" len="2"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"       src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPATIENT"   src="PERSMEDCARD"          srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdDATE"      src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="HAVE_NEXTT"           srctype="var" put="" len="2"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"            src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPATIENT"        src="PERSMEDCARD"          srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdDATE"           src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND"      src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdNEXT_DATE"      src="DDATE"                srctype="ctrl" put="" len="11"/>
            <component cmptype="ActionVar" name="pnNEXT_PLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_NEXT"      src="HAVE_NEXTT"           srctype="var"  put="" len="2"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"       src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPMC"       src="PMC_DIR"              srctype="var"/>
            <component cmptype="ActionVar" name="pnDIR"       src="DIR_DIR"              srctype="var"/>
            <component cmptype="ActionVar" name="pdDATE_IN"   src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnHPK_IN"    src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdDATE_OUT"  src="DDATE"                srctype="ctrl" put="" len="25"/>
            <component cmptype="ActionVar" name="pnHPK_OUT"   src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_NEXT" src="DIR_HAVE_NEXTT"       srctype="var"  put="" len="3"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"       src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPMC"       src="PMC_DIR"              srctype="var"/>
            <component cmptype="ActionVar" name="pnDIR"       src="DIR_DIR"              srctype="var"/>
            <component cmptype="ActionVar" name="pdDATE_IN"   src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnHPK_IN"    src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdDATE_OUT"  src="DDATE"                srctype="ctrl" put="" len="25"/>
            <component cmptype="ActionVar" name="pnHPK_OUT"   src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_PREV" src="DIR_HAVE_PREVV"       srctype="var"  put="" len="3"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"            src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnPATIENT"        src="PERSMEDCARD"          srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdDATE"           src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnPLAN_KIND"      src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdPREV_DATE"      src="DDATE"                srctype="ctrl" put="" len="11"/>
            <component cmptype="ActionVar" name="pnPREV_PLAN_KIND" src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl" put="" len="17"/>
            <component cmptype="ActionVar" name="pnHAVE_PREV"      src="HAVE_PREVV"           srctype="var"  put="" len="2"/>
        </component>

        <component cmptype="Action" name="ActionIB">
            <![CDATA[
            begin
              D_PKG_HPK_PLAN_JOURNALS.SET_HH_DIRECTION_DATE(pnID => to_number(:pnID));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnID" src="HPK_PL_DAY_VAR" srctype="var"/>
        </component>

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
            <component cmptype="ActionVar" name="pnLPU"                     src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnHPKID"                   src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="psSTART_DATE"              src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="psMAX_PRIOR"               src="max_reg_date"         srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psMIN_AGE"                 src="min_reg_age"          srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psMAX_AGE"                 src="max_reg_age"          srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psHAS_MKB_CONSTRAINTS"     src="has_mkb_ogr"          srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psPAYMENT_KIND"            src="pay_reg_kind"         srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psHAS_LIMITS"              src="has_reg_lim"          srctype="ctrlcaption" put="" len="60"/>
            <component cmptype="ActionVar" name="psGEN_COUNT_S"             src="Place_all"            srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psMALE_COUNT_S"            src="Place_male"           srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psFEMALE_COUNT_S"          src="Place_female"         srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psOPER_COUNT_S"            src="Place_oper"           srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psCON_COUNT_S"             src="Place_cons"           srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="pnAVAIL_ADD"               src="AVAIL_ADD"            srctype="var"         put="" len="1"/>
            <component cmptype="ActionVar" name="pnHAS_PAYMENT_CONSTRAINTS" src="HAS_PK_CONSTR"        srctype="var"         put="" len="2"/>
            <component cmptype="ActionVar" name="pnIDD"                     src="THISPLANID"           srctype="var"         put="" len="17"/>
            <component cmptype="ActionVar" name="pnJOURNAL_TYPE"            src="JOURNAL_TYPE"         srctype="var"         put="" len="1"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"        src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnHPK"        src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdSTART_DATE" src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnSEX"        src="C_HOSP_READY"         srctype="ctrl"/>
            <component cmptype="ActionVar" name="pnOPER_TYPE"  src="C_HOSP_KIND"          srctype="ctrl"/>
            <component cmptype="ActionVar" name="pdRESULT"     src="DDATE"                srctype="ctrl" put="" len="11"/>
        </component>
        <component cmptype="Action" name="getQuantBeds">
            <![CDATA[
            begin
              :pnQUANT_BEDS := D_PKG_HPK_PLAN_JOURNALS.GET_QUANT_BEDS_PROFILES_NEW(pnHPK_PLAN => to_number(:pnHPK_PLAN),
                                                                                   pnLPU      => to_number(:pnLPU));
            end;
            ]]>
            <component cmptype="ActionVar" name="pnHPK_PLAN"   src="THISPLANID" srctype="var"/>
            <component cmptype="ActionVar" name="pnLPU"        src="LPU"        srctype="var"/>
            <component cmptype="ActionVar" name="pnQUANT_BEDS" src="quant_beds" srctype="var" put="" len="700"/>
        </component>

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
            <component cmptype="ActionVar" name="pnLPU"            src="LPU"                  srctype="var"/>
            <component cmptype="ActionVar" name="pnHPKID"          src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
            <component cmptype="ActionVar" name="psSTART_DATE"     src="DDATE"                srctype="ctrl"/>
            <component cmptype="ActionVar" name="psGEN_COUNT_S"    src="Place_all"            srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psMALE_COUNT_S"   src="Place_male"           srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psFEMALE_COUNT_S" src="Place_female"         srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psOPER_COUNT_S"   src="Place_oper"           srctype="ctrlcaption" put="" len="82"/>
            <component cmptype="ActionVar" name="psCON_COUNT_S"    src="Place_cons"           srctype="ctrlcaption" put="" len="82"/>
        </component>
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
          <component cmptype="ActionVar" name="pnLPU"       src="LPU"                  srctype="var"/>
          <component cmptype="ActionVar" name="pnPLANIDD"   src="C_DS_HOSP_PLAN_KINDS" srctype="ctrl"/>
          <component cmptype="ActionVar" name="pnCABLAB"    src="CABLAB"               srctype="var"/>
          <component cmptype="ActionVar" name="pnCHECK_HPK" src="CHECK_HPK"            srctype="var" put="" len="1"/>
        </component>

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
             where j.ID in (select /*+ cardinality(t, 1) */ t.COLUMN_VALUE
                              from table(cast(:DS_IDS as D_CL_ID)) t)
            ]]>
            <component cmptype="Variable" name="LPU"    src="LPU"    srctype="session"/>
            <component cmptype="Variable" name="DS_IDS" src="DS_IDS" srctype="var" type="collection" tdo="D_CL_ID"/>
        </component>

  		<!--DataSet: Для элемента ComboBox (виды госпитализации, доступные пользователю)-->
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
  		  	<component cmptype="Variable" name="PNLPU"            src="LPU"              srctype="var" get="var1"/>
  		  	<component cmptype="Variable" name="CABLAB"           src="CABLAB"           srctype="var" get="var0"/>
			<component cmptype="Variable" name="isVmpWaitingList" src="isVmpWaitingList" srctype="var"/>
  		</component>

  		<!--DataSet: Содержимое таблицы-->
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
		               j.HOSP_MKB.STR1 || ' ' || j.HOSP_MKB.STR2 HOSP_MKB, -- MKB_CODE || ' ' || MKB_NAME
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
			<component cmptype="Variable" name="PNLPU"                src="LPU"                   srctype="var"   get="var0"/>
			<component cmptype="Variable" name="CABLAB"   			  src="CABLAB" 		          srctype="session"/>
			<component cmptype="Variable" name="PLANIDD"              src="C_DS_HOSP_PLAN_KINDS"  srctype="ctrl"  get="var3"/>
			<component cmptype="Variable" name="START_DATE"           src="DDATE"                 srctype="ctrl"  get="var2"/>
        	<component cmptype="Variable" name="JOURNAL_TYPE"         src="JOURNAL_TYPE"          srctype="var"   get="var4"/>
			<component cmptype="Variable" name="CABLAB"	              src="CABLAB"                srctype="var"   get="var5"/>
			<component cmptype="Variable" name="CHECK_HPK"	          src="CHECK_HPK"             srctype="var"   get="var6"/>
			<component cmptype="Variable" name="GenDirNumbToDirKinds" src="GenDirNumbToDirKinds"  srctype="var"   get="var7"/>
			<component cmptype="Variable" name="CH_HH_ANNUL"          src="CH_HH_ANNUL"           srctype="ctrl"  get="var8"/>
			<component cmptype="Variable" name="PERSMEDCARD"          src="PERSMEDCARD"           srctype="ctrl"  get="var9"/>
			<component cmptype="Variable" name="SHOW_FLG"             src="SHOW_FLG"              srctype="var"   get="gSHOW_FLG"/>
			<component cmptype="Variable" name="ADMISSION_HOSP"       src="ADMISSION_HOSP"        srctype="var"/>
			<component cmptype="Variable" type="count"                src="ds1count"              srctype="var"   default="5"/>
			<component cmptype="Variable" type="start"                src="ds1start"              srctype="var"   default="1"/>
            <component cmptype="Variable" name="USE_QUEUE74"          src="0"                     srctype="const" get="pUSE_QUEUE74" />
            <component cmptype="Variable" name="SERV_QUEUE74"         src="0"                     srctype="const" get="pSERV_QUEUE74" />
  		</component>
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
            <component cmptype="ActionVar" name="pnLPU"                          src="LPU"                       srctype="var"/>
            <component cmptype="ActionVar" name="pnPMC"                          src="PMC_ID"                    srctype="var"/>
            <component cmptype="ActionVar" name="pnSHOW_FLG"                     src="SHOW_FLG"                  srctype="var"         put="" len="1"/>
            <component cmptype="ActionVar" name="pnINSERT"                       src="PINSS"                     srctype="var"         put="" len="2"/>
            <component cmptype="ActionVar" name="pnUPDATE"                       src="PUPDD"                     srctype="var"         put="" len="2"/>
            <component cmptype="ActionVar" name="pnDELETE"                       src="PDELL"                     srctype="var"         put="" len="2"/>
            <component cmptype="ActionVar" name="pnMOVE_OUT"                     src="PMOVV"                     srctype="var"         put="" len="2"/>
            <component cmptype="ActionVar" name="psCARDN"                        src="PERSMEDCARD"               srctype="ctrlcaption" put="" len="26"/>
            <component cmptype="ActionVar" name="pnPMC_ID"                       src="PERSMEDCARD"               srctype="ctrl"        put="" len="17"/>
            <component cmptype="ActionVar" name="pnADMISSION_HOSP"               src="ADMISSION_HOSP"            srctype="var"         put="" len="1"/>
            <component cmptype="ActionVar" name="pnOPTION_THERAPY_SCHEMES_FIELD" src="optionTherapySchemesField" srctype="var"         put="" len="1"/>
            <component cmptype="ActionVar" name="pnGEN_DIR_NUMB_TO_DIR_KINDS"    src="GenDirNumbToDirKinds"      srctype="var"         put="" len="1"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"             src="LPU"             srctype="var"/>
            <component cmptype="ActionVar" name="pnHOSP_HISTORY"    src="HOSP_HISTORY"    srctype="var"/>
            <component cmptype="ActionVar" name="pnHOSP_HISTORY_DS" src="HOSP_HISTORY_DS" srctype="var"/>
        </component>
		<component cmptype="Action" name="rollbackAnnIB">
			<![CDATA[
			begin
				D_PKG_HOSP_HISTORIES.SET_DISCARD_STATUS(to_number(:HOSP_HISTORY), to_number(:LPU), 0);
			    D_PKG_WLH_REQUESTS.UPD_BY_HH_REMOVE_DISCARD(pnHH_ID => to_number(:HOSP_HISTORY),
                                                            pnLPU   => to_number(:LPU));
			end;
			]]>
			<component cmptype="ActionVar" name="LPU"          get="LPU"          src="LPU"          srctype="var"/>
			<component cmptype="ActionVar" name="HOSP_HISTORY" get="HOSP_HISTORY" src="HOSP_HISTORY" srctype="var"/>
		</component>
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
            <component cmptype="ActionVar" name="pnOUTER_LPUDICT" src="OUTER_LPUDICT" srctype="var"/>
            <component cmptype="ActionVar" name="pnDDIR_ID"       src="DDIR_ID"       srctype="var"/>
            <component cmptype="ActionVar" name="pnLPU"           src="LPU"           srctype="var" put="" len="17"/>
            <component cmptype="ActionVar" name="pnCABLAB"        src="CABLAB"        srctype="var" put="" len="17"/>
            <component cmptype="ActionVar" name="psERR"           src="ERR"           srctype="var" put="" len="100"/>
            <component cmptype="ActionVar" name="pnDDIR_IDN"      src="DDIR_ID"       srctype="var" put="" len="17"/>
        </component>

        <component cmptype="Action" name="getCurrentCablab">
            <component cmptype="ActionVar" name="CABLAB" src="CABLAB" srctype="session"/>
            <component cmptype="ActionVar" name="CABLAB" src="CABLAB" srctype="var" put="" len="17"/>
        </component>

        <component cmptype="Action" name="getCurrentLPU">
            <component cmptype="ActionVar" name="LPU" src="LPU" srctype="session"/>
            <component cmptype="ActionVar" name="LPU" src="LPU" srctype="var" put="" len="17"/>
        </component>

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
            <component cmptype="ActionVar" name="pnHH_ID"     src="HH_ID"     srctype="var"/>
            <component cmptype="ActionVar" name="pnHH_DEP_ID" src="HH_DEP_ID" srctype="var" put="" len="17"/>
        </component>
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
            <component cmptype="ActionVar" name="pnLPU"              src="LPU"         srctype="session"/>
            <component cmptype="ActionVar" name="pnPATIENT"          src="PATIENT_ID"  srctype="var"/>
            <component cmptype="ActionVar" name="pnHPK_PLAN_JOURNAL" src="ID"          srctype="var"/>
            <component cmptype="ActionVar" name="psDATE_IN"          src="DATE_IN"     srctype="var"/>
            <component cmptype="ActionVar" name="psDATE_OUT"         src="DATE_OUT"    srctype="var"/>
            <component cmptype="ActionVar" name="pnIS_CANCELED"      src="IS_CANCELED" srctype="var" put="" len="1"/>
            <component cmptype="ActionVar" name="psERR"              src="ERR"         srctype="var" put="" len="4000"/>
            <component cmptype="ActionVar" name="psWARN"             src="WARN"        srctype="var" put="" len="4000"/>
        </component>
        <component cmptype="Action" name="updateWLH">
            <![CDATA[
			begin
              D_PKG_WLH_REQUESTS.UPD_BY_HH_ADD(pnHH_ID => to_number(:pnHH_ID),
                                               pnLPU   => to_number(:pnLPU));
			end;
			]]>
            <component cmptype="ActionVar" name="pnLPU"   src="LPU"   srctype="session"/>
            <component cmptype="ActionVar" name="pnHH_ID" src="HH_ID" srctype="var"/>
        </component>
        <component cmptype="Action" name="updateWLHByHospCancel">
            <![CDATA[
			begin
              D_PKG_WLH_REQUESTS.UPD_BY_HH_CANCEL(pnHPK_PLAN_JOURNAL => to_number(:HPK_ID),
                                                  pnLPU              => to_number(:pnLPU));
			end;
			]]>
            <component cmptype="ActionVar" name="pnLPU"  src="LPU"    srctype="session"/>
            <component cmptype="ActionVar" name="HPK_ID" src="HPK_ID" srctype="var"/>
        </component>
		<component cmptype="Script" name="onShow">
			<![CDATA[
				Form.OnShow = function() {
					if (getVar('FROM_PREV_PAGE') == 1) {
						setValue('DDATE', getVar('HELPDATE'));
						setValue('C_DS_HOSP_PLAN_KINDS', getVar('HELPPLAN'));
						if (!empty(getVar('ID_FOR_LOCATE')))
							setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('ID_FOR_LOCATE'));
						Form.onJournalChange(getVar('noActive'));
					} else {
					    setValue('DDATE', SysDate('dd.mm.yyyy'));
						Form.onJournalChange(getVar('noActive'));
					}
					Form.focusOnFilterField('GR_HPK_PLAN_DAY', 'DS_HPK_PLAN_DAY_PATIENT_ACTUAL_FilterItem');
				};
			]]>
		</component>
  		<component cmptype="Script" name="Script">
	  		<![CDATA[
			Form.OnCreate = function() {
				setVar('HELPDATE', getVar('DDATE', 1));
				setVar('HELPID', getVar('IDD', 1));
				setVar('ID_FOR_LOCATE', getVar('ID_FOR_LOCATE', 1));
				setVar('HELPPLAN', getVar('C_HOSP_PLAN_KINDS', 1));
				setVar('SFLAG', getVar('SFLAG', 1));
				if (empty(getVar('DDIR_ID'))) setVar('DDIR_ID', getVar('DDIR', 1));
				//PMC_ID может передаваться при открытии окна
				if (empty(getVar('PMC_ID')))
					setVar('PMC_ID', getVar('PERSMEDCARD_ID', 1));
				if (getVar('SFLAG') == 1) {
					setVar('VISIT_ID', getVar('VISIT', 1));
					setVar('PMC_ID', getVar('PATIENT', 1));
				}
				if (getVar('SFLAG') == 2) {
					setVar('PMC_ID', getVar('PMC_ID', 1));
					setVar('DDIR_ID', getVar('DIRECTION', 1));
				}

				if (window.sessionStorage) {
					// PERSMEDCARD_ID сохраняется в sessionStorage при выборе пациента в расписании
					if (empty(getVar('PMC_ID')) && sessionStorage.getItem('PERSMEDCARD_ID') !== null) {
						setVar('PMC_ID', sessionStorage.getItem('PERSMEDCARD_ID'));
						sessionStorage.removeItem('PERSMEDCARD_ID');
					}
				}

				// OUTER_LPUDICT передается с формы hp_directions_edit
				setVar('OUTER_LPUDICT', getVar('OUTER_LPUDICT', 1));
				if (!empty(getVar('OUTER_LPUDICT'))) {
					executeAction('getOuterLPUCablab', function() {
						if (!empty(getVar('ERR'))) {
							alert(getVar('ERR'));
							closeWindow();
						}
						setControlProperty('PERSMEDCARD', 'enabled', false);
						Form.GetCardNumbAndStandartRights();
					});
				} else {
                    executeAction('getCurrentLPU', null, null, 0, 0);
                    executeAction('getCurrentCablab', null, null, 0, 0);
				    Form.GetCardNumbAndStandartRights();
				}
			};

			Form.GetCardNumbAndStandartRights = function() {
				executeAction('GetCardNumbAndStandartRights', function() {
                    if ((!empty(getVar('HELPID'))) || ((!empty(getVar('HELPDATE'))) &amp;&amp; (!empty(getVar('HELPPLAN'))))) {
                        setVar('FROM_PREV_PAGE', 1);
                    } else {
                        setVar('FROM_PREV_PAGE', 0);
                    }
                    if (+getVar('optionTherapySchemesField') !== 1) {
                        let tds = getControlByName("GR_HPK_PLAN_DAY").querySelectorAll('[column_name="MED_THERAPY_SCHEME_CODE"]');
                        Array.prototype.forEach.call(tds, function(td) {
                            td.style.display = 'none';
                        })
                    }
                    if (getVar('ADMISSION_HOSP') !== '1' && getVar('ADMISSION_HOSP') !== '2') {
                        Grid_hideColByName(getControlByName('GR_HPK_PLAN_DAY'), 'DATE_REC');
                    }
				});
			};

			Form.focusOnFilterField = function(grid, field) {
				// раскрыть фильтр по-умолчанию и установить фокус
				ToogleDisplayFilter(grid);
				var filter = field;
				if (filter && isExistsControlByName(filter)) {
					filter = getControlByName(filter).querySelector('.input-ctrl');
					if (filter) filter.focus();
				}
			}
			Form.AddPat = function() {
				setVar('C_HOSP_PLAN_KINDS', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_PLAN_KIND']);
				if (getVar('PINSS') != 1) {
					alert('Недостаточно прав!');
					return;
				}
				setVar('PMC_ID', getValue('PERSMEDCARD'));
				setVar('PLANDAY_ID', null);
				if (getValue('C_DS_HOSP_PLAN_KINDS') != -1)
					setVar('PLAN_JOURNAL', getValue('C_DS_HOSP_PLAN_KINDS'));
				else setVar('PLAN_JOURNAL', null);
				setVar('DDATE', getValue('DDATE'));
				openWindow({
					name: 'HospPlanJournal/hp_add_direction',
					vars: {VIS_ID: getVar('VIS_ID'),
					          LPU: getVar('LPU'),
					   PLANDAY_ID: setVar('PLANDAY_ID'),
					        DDATE: getValue('DDATE'),
					       PMC_ID: getValue('PERSMEDCARD'),
 				          DDIR_ID: getVar('DDIR_ID')}
				}, true, 520, 650)
						.addListener('onafterclose',
								function() {
									if (getVar('ModalResult') == 1) {
										setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('newid'));
										base().refreshHPK_PLAN_DAY();
									}
									executeAction('ActionPlaces');
								});
			};
			Form.NearestDayFind = function() {
				if (!empty(getValue('C_DS_HOSP_PLAN_KINDS')) &amp;&amp; getValue('C_DS_HOSP_PLAN_KINDS') != '-1')
					executeAction('NearestDaySearch', base().onJournalChange);
				else alert('Не выбран журнал.');
			};
			Form.EditPat = function() {
				if (getVar('PUPDD') != 1) {
					alert('Недостаточно прав!');
					return;
				}
				if (empty(getValue('GR_HPK_PLAN_DAY'))) {
					alert('Не выбрана запись.');
					return;
				}
				setVar('PLANDAY_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('PLAN_JOURNAL', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_PLAN_KIND']);
				setVar('IDD', getControlProperty('GR_HPK_PLAN_DAY', 'data')['THISPLANID']);
				setVar('DDATE', getValue('DDATE'));
				setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
				openWindow({name: 'HospPlanJournal/hp_add_direction', vars: {LPU: getVar('LPU')}}, true, 520, 650)
						.addListener('onafterclose',
								function() {
									if (getVar('ModalResult') == 1)
										base().refreshHPK_PLAN_DAY();
								});
			};
			Form.BackDay = function() {
				executeAction('prevDay', base().onJournalChange, null, null);
			};
			Form.HistHosp = function() {
				if (empty(getValue('GR_HPK_PLAN_DAY'))) {
					alert('Не выбрана запись, либо остутствует план госпитализации!');
					return;
				}
				setVar('PMC_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				setVar('PMC_FIO', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT']);
				openWindow('HospPlan/hosp_hist', true, 1000, 510);
			};
			Form.DirSearch = function() {
				if (empty(getValue('DIR_PREF')) && empty(getValue('DIR_NUMB'))) {
					alert('Для поиска необходимо выбрать номер и префикс направления!');
					return;
				}
				executeAction('startSearchDirection', base().AfterSearchDirection);
			};
			Form.AfterSearchDirection = function() {
				if (getVar('DIR_RESULT') == 0 || getVar('DIR_RESULT') == -1) {
					alert('Не найдено направление по данному префиксу и номеру!');
					return;
				}
				if (getVar('DIR_RESULT') == -2) {
					alert('Найдено более одного направления по данному префиксу и номеру!');
					return;
				}
				if (getVar('DIR_HAVE_NEXT') == 1)
					setControlProperty('dirnex', 'enabled', true);
				else
					setControlProperty('dirnex', 'enabled', false);
				if (getVar('DIR_HAVE_PREV') == 1)
					setControlProperty('dirpre', 'enabled', true);
				else
					setControlProperty('dirpre', 'enabled', false);
				base().onJournalChange();
			};
			Form.DirNext = function() {
				executeAction('searchDirNext', base().afterSearchDirNext, null, null);
			};
			Form.afterSearchDirNext = function() {
				setControlProperty('dirpre', 'enabled', true);
				if (getVar('DIR_HAVE_NEXTT') != 1)
					setControlProperty('dirnex', 'enabled', false);
				base().onJournalChange();
			};
			Form.DirPrev = function() {
				executeAction('searchDirPrev', base().afterSearchDirPrev, null, null);
			};
			Form.afterSearchDirPrev = function() {
				setControlProperty('dirnex', 'enabled', true);
				if (getVar('DIR_HAVE_PREVV') != 1)
					setControlProperty('dirpre', 'enabled', false);
				base().onJournalChange();
			};

			Form.PatSearch = function() {
				if (!empty(getValue('PERSMEDCARD'))) {
					executeAction('SearchPatient', base().AfterSearchPatient, null, null);
				}
				else {
					alert('Для поиска необходимо выбрать пациента.');
				}
			};
			Form.AfterSearchPatient = function() {
				if (getVar('HAVE_NEXT') == 1) {
                    setControlProperty('patnex', 'enabled', true);
                } else {
                    executeAction('checkNextExists', function() {
                        setControlProperty('patnex', 'enabled', (+getVar('HAVE_NEXTT') === 1));
                    });
                }
				if (getVar('HAVE_PREV') == 1) {
                    setControlProperty('patpre', 'enabled', true);
                } else {
                    setControlProperty('patpre', 'enabled', false);
                }
				base().onJournalChange();
			};
			Form.PatNext = function() {
				executeAction('SearchPatientNext', base().AfterSearchPatientNext, null, null);
			};
			Form.AfterSearchPatientNext = function() {
				setControlProperty('patpre', 'enabled', true);
				if (getVar('HAVE_NEXTT') != 1) {
					setControlProperty('patnex', 'enabled', false);
				}
				base().onJournalChange();
			};
			Form.PatPrev = function() {
				executeAction('SearchPatientPrev', base().AfterSearchPatientPrev, null, null);
			};
			Form.AfterSearchPatientPrev = function() {
				setControlProperty('patnex', 'enabled', true);
				if (getVar('HAVE_PREVV') != 1) {
					setControlProperty('patpre', 'enabled', false);
				}
				base().onJournalChange();
			};
			Form.ForwDay = function() {
				executeAction('nextDay', base().onJournalChange, null, null);
			};
			Form.OtobrDay = function() {
				base().onJournalChange();
			};
			Form.ChangeDate = function() {
				if (empty(getValue('GR_HPK_PLAN_DAY'))) {
					alert('Не выбрана запись.');
					return;
				}
				setVar('ModalResult', 0);
				setVar('C_HOSP_PLAN_KINDS', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_PLAN_KIND']);
				executeAction('CheckRightsREC', base().ChangeDateAfter, null, null);
			};
			Form.ChangeDateAfter = function() {
				if (!empty(getValue('C_DS_HOSP_PLAN_KINDS'))) {
					if (getVar('CHREC') &lt; 1) {
						alert('Недостаточно прав!');
						return;
					}
					setVar('GR_HPK_PLAN_DAY', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
					setVar('FLAG_MOVE', 1);
					openWindow('HospPlan/hospplanperiod', true, 1000, 540)
							.addListener('onafterclose', function() {
										setValue('DDATE', getVar('NEW_PLAN_DDATE'));
										setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
										base().refreshHPK_PLAN_DAY();
									}
									, null, false);
				}
				else {
					setVar('GR_HPK_PLAN_DAY', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
					setVar('FLAG_MOVE', 1);
					openWindow('HospPlan/hospplanperiod', true, 1000, 540)
							.addListener('onafterclose', function() {
										setValue('DDATE', getVar('NEW_PLAN_DDATE'));
										setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
										base().refreshHPK_PLAN_DAY();
									}
									, null, false);
				}
			};
			Form.GoIBAfterCheckRights = function() {
				if (getVar('CHIB') &lt;= 1) {
					alert('Недостаточно прав!');
					return;
				}
				if (confirm('Отправить на создание ИБ?')) {
					setVar('HPK_PL_DAY_VAR', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
					executeAction('ActionIB', function() {
						base().refreshHPK_PLAN_DAY();
						setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
					}, null, null);
				}
			};
			Form.GoIB = function() {
				if (empty(getValue('GR_HPK_PLAN_DAY'))) {
					alert('Не выбрана запись, либо остутствует план госпитализации!');
					return;
				}
				executeAction('CheckRightsIB', function() {
					base().GoIBAfterCheckRights();
				}, null, null);
			};
			Form.GoDelPatAfterCheckRights = function(data) {
				setVar('HPK_PL_DAY_VAR', data['ID']);
				setVar('LPU_TO', data['LPU']);
				setVar('OUTER_DIRECTION_ID', data['OUTER_DIRECTION_ID']);
				var GoDelPatAfterCheckRightsNextStep = function() {
                    if (!empty(getValue('C_DS_HOSP_PLAN_KINDS')) && +getValue('C_DS_HOSP_PLAN_KINDS') !== -1) {
                        if (getVar('CHREC') &lt; 1) {
                            alert('Недостаточно прав!');
                            return;
                        }
                        executeAction('ActionDelPlanDay', base().refreshWithCurrentLocate, null, null);//function(){base().refreshHPK_PLAN_DAY();}, null, null);
                    } else {
                        executeAction('ActionDelPlanDay', base().refreshWithCurrentLocate, null, null);//function(){base().refreshHPK_PLAN_DAY();}, null, null);
                    }
                }
                if (!empty(getVar('HPK_SCHEDULE_REG'))) {
                    D3Api.showConfirm('По данному направлению запись произведена через график госпитализации. При удалении текущей записи так же будет удалена запись из графика. Удалить запись?',
                        function() {
                            executeAction('delInfoHpkScheduleReg', GoDelPatAfterCheckRightsNextStep);
                        }, null, {
                            button_confirm_caption: 'Да',
                            button_cancel_caption: 'Нет',
                        }
                    );
                } else {
                    GoDelPatAfterCheckRightsNextStep();
                }
			};
			Form.DelPat = function() {
                var data = getControlProperty('GR_HPK_PLAN_DAY', 'data');
				if (!empty(getValue('GR_HPK_PLAN_DAY'))) {
                    setVar('DIRECTION', data['DIRECTION']);
                    executeAction('getInfoHpkScheduleReg', null, null, null, false, 0); // проверка наличия записи в расписании D_V_HPK_SCHEDULE_REG
                    if (!empty(getVar('HPK_SCHEDULE_REG'))) {
                        Form.GoDelPatAfterCheckRights(data);
                        return;
                    }
					if (confirm('Вы действительно хотите удалить запись?')) {
						setVar('C_HOSP_PLAN_KINDS', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_PLAN_KIND']);
						executeAction('CheckRightsREC', Form.GoDelPatAfterCheckRights.bind(null, data));
					}
				}
				else {
					alert('Не выбрана запись.');
				}
			};
			Form.onJournalChange = function (_param) {
				executeAction('ActionMax_Prior', function() {
					if (+getValue('C_DS_HOSP_PLAN_KINDS') === -1) {
						setControlProperty('BedFondPlan', 'enabled', false)
					} else {
					    setControlProperty('BedFondPlan', 'enabled', true);
					}
					Form.refreshHPK_PLAN_DAY(_param, true);
					if (getVar('JOURNAL_TYPE') === '1') {
						setValue('DS_HPK_PLAN_DAY_RECORD_STATUS_MNEMO_FilterItem', '0');
					} else {
						setValue('DS_HPK_PLAN_DAY_RECORD_STATUS_MNEMO_FilterItem', 0);
					}
                    refreshDataSet('DS_HPK_PLAN_DAY');
				});
			};
			Form.genRegPat = function() {
				setVar('PERSMEDCARD_ID', getVar('PATIENT_ID'));
				setVar('HPK_PLAN_JOURNAL', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('PARAM_VISIT_ID', null);
				openWindow({
					name: 'GenRegistry/reg_full',
					vars: {
						MODE: 'close'
					}
				}, true)
						.addListener('onafterclose', function() {
							//if (getVar('ModalResult') == 1) {
							base().refreshHPK_PLAN_DAY();
							//}
						});
			}
			Form.RecordPat = function() {
				setVar('Modalresult', 0);
				setVar('SERV_ID', null);
				setVar('PARAM_VISIT_ID', null);
				// setVar('PARAM_DISEASECASE_ID', getVar('DISEASECASE'));
				// setVar('PARAM_HH_DEP', getVar('HH_DEP'));
				setVar('PARAM_REG_TYPE', 1);
				setVar('PERSMEDCARD_ID', getVar('PATIENT_ID'));
				setVar('CONTRACT_CODE', null);
				setVar('CONTRACT_ID', null);
				setVar('CONTR_AMI_DIR_CODE', null);
				setVar('CONTR_AMI_DIR_ID', null);
				setVar('HPK_PLAN_JOURNAL', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);

				executeAction('getDefDepFromOption', function() {
					openWindow('Registry/reg_short', true, 1000, 800)
							.addListener('onafterclose', function() {
								if (getVar('ModalResult') == 1)
									base().refreshHPK_PLAN_DAY();
							});
				});
			}
			Form.MoveHistory = function() {
				setVar('PRIMARY', getValue('GR_HPK_PLAN_DAY'));
				openWindow('HospPlan/move_history', true, 400, 250);
			}
			Form.showPopUpjt = function (pItemName) {
				if (Form.jt == 1) {
					PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), pItemName, true);
				} else {
					PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), pItemName, false);
				}
			}

			Form.onPaint = function (_dataArray) {
				var _domObject = getControlByName('PAT_STATUS');
				switch (parseInt(_dataArray['BEDS'])) {
					case 0: {
						_domObject.style.backgroundColor = '';
						break;
					}
					case 4: {
						_domObject.style.backgroundColor = '#FF0000';
						break;
					}
					case 1: {
						_domObject.style.backgroundColor = '#669999';
						break;
					}
					case 2: {
						_domObject.style.backgroundColor = '#66CC66';
						break;
					}
					case 3: {
						_domObject.style.backgroundColor = '#6699FF';
						break;
					}
					default : {
						_domObject.style.backgroundColor = '';
					}
				}
				switch (parseInt(_dataArray['HOSP_HISTORY_DS'])) {
					case 1: {
						_domObject.style.backgroundColor = '#996600';
						break;
					}
					case 2: {
						_domObject.style.backgroundColor = '#A7A7A7';
						break;
					}
				}
				if (_dataArray['IS_CANCELED'] == 1) _domObject.style.backgroundColor = '#db9d00';
			};
			Form.ShowHideLegend = function() {
				var _dom = getControlByName('legenddiv');
				if (_dom.style.display == 'none') {
					setCaption('link1', 'Скрыть легенду');
					_dom.style.display = '';
				}
				else {
					setCaption('link1', 'Показать легенду');
					_dom.style.display = 'none';
				}
			};

			Form.toHosp = function() {
				var dataGrid = getControlProperty('GR_HPK_PLAN_DAY', 'data');
				if (empty(getValue('GR_HPK_PLAN_DAY'))) {
					alert('Не выбрана запись, либо отсутствует план госпитализации!');
					return;
				}
				setVar('ModalResult', 0);
				setVar('HPK_PLAN_JOURNAL_ID', dataGrid['ID']);
				setVar('PMC_ID', dataGrid['PATIENT_ID']);
				setVar('PMC_FIO', dataGrid['PATIENT']);
				setVar('DIRECTION_ID', dataGrid['DIRECTION']);
				setVar('HOSP_PLAN_KIND', dataGrid['HOSP_PLAN_KIND']);
				setVar('HOSP_HISTORY', dataGrid['HOSP_HISTORY']);

				setVar('CONTRACT', dataGrid['CONTRACT_ID']);
				setVar('HH_ID', dataGrid['HOSP_HISTORY']);
				setVar('PAYMENT_KIND_ID', dataGrid['PAYMENT_KIND_ID']);
				executeAction('getInfoFromHHDeps',null, null, null, false);

				openWindow({
					name: 'HospPlan/hospitalisation',
					vars: {
						CONTRACT_CAPTION: getVar('CONTRACT_CAPTION'),
					}
				}, true)
				.addListener('onafterclose', function() {
					if (getVar('ModalResult') == 1) {
						setControlProperty('GR_HPK_PLAN_DAY', 'locate', dataGrid['ID'] + getVar('HH_ID'));
                        executeAction('updateWLH');
						base().refreshHPK_PLAN_DAY();
					}
				});
			};

			Form.checkIfHHIsSingle = function() {
				var dataGrid = getControlProperty('GR_HPK_PLAN_DAY', 'data');
				[
				    'PATIENT_ID',
					'DATE_IN',
					'DATE_OUT',
					'ID'
				].forEach(function(elem) {
					setVar(elem, dataGrid[elem]);
				});
				executeAction('ACT_CHECK_IF_HH_IS_SINGLE', function() {
				    if (!empty(getVar('IS_CANCELED')) && +getVar('IS_CANCELED') === 1) {
				        showAlert('По данному направлению выполнен отказ в госпитализации, невозможно создать ИБ. Для создания ИБ необходимо отменить отказ в госпитализации.');
				        return;
				    }
					if (!empty(getVar('ERR'))) {
						showAlert(getVar('ERR'));
					} else if (!empty(getVar('WARN'))) {
						showConfirm(getVar('WARN') + ' Вы действительно хотите госпитализировать?', null, 100, 100, base().toHosp, null, 'yesno');
					} else {
						base().toHosp();
					}
				});
			};
            Form.CancelHosp = function() {
                setVar('HH_ID_DEL', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
                executeAction('ACancelCheck', function() {
                    var msg = 'Вы действительно хотите отменить госпитализацию?';
                    if (!empty(getVar('SMP_CALL_EXS_IDS'))) {
                        msg = 'Вы действительно хотите отменить госпитализацию? Для этой ИБ есть связь с вызовом СМП, при подтверждении она будет разорвана.';
                    }
                    if (!confirm(msg)) {
                        return;
                    }
                    executeAction('ACancelHosp', function() {
                        setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('LOC'));
                        Form.refreshHPK_PLAN_DAY();
                    });
                });
            };
			Form.ViewQuotes = function() {
				if (empty(getValue('C_DS_HOSP_PLAN_KINDS')) || getValue('C_DS_HOSP_PLAN_KINDS') == -1) {
					alert('Не выбран журнал!');
					return;
				}
				setVar('JOURNAL_ID', getValue('C_DS_HOSP_PLAN_KINDS'));
				openWindow('HospPlan/showQuotes', true);
			};
			Form.showLoadKF = function() {
				setVar('JOURNAL_ID', getValue('C_DS_HOSP_PLAN_KINDS'));
				setVar('JOURNAL_DATE', getValue('DDATE'));
				openWindow('HospPlan/showLoadKF', true);
			};
			Form.showPlaning = function() {
				setVar('JOURNAL_ID', getValue('C_DS_HOSP_PLAN_KINDS'));
				setVar('JOURNAL_DATE', getValue('DDATE'));
				openWindow('HospPlan/showPlaning', true);
			};
            Form.ADD_PERSMEDCARD = function(_dom) {
                setVar('PERSMEDCARD', getControlValue(_dom));
                setVar('AGENT_ID', null);
                openWindow('Persmedcard/persmedcard_edit', true, 800, 580)
                    .addListener('onafterclose', function() {
                        if (getVar('ModalResult') !== 1) {
                            setVar('AGENT_ID', null);
                            return;
                        }

                        setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
                        refreshDataSet('DS_HPK_PLAN_DAY');
                    });
            };
			Form.SHOWHIST = function() {
				var pmc_array = getControlProperty('GR_HPK_PLAN_DAY', 'data');
				setVar('PatientID', pmc_array['PATIENT_ID']);
				setVar('PatientFIO', pmc_array['PATIENT']);
				openWindow('DiseaseCase/diseasecase', true, 1000, 700);

			};
			Form.annulmentHH = function() {
				if (confirm('Аннулированная ИБ будет недоступна для работы. Для высвобождения номера требуется подтверждение в регистратуре. Продолжить?')) {
					_grd = getControlProperty('GR_HPK_PLAN_DAY', 'data');
					setVar('HOSP_HISTORY', _grd['HOSP_HISTORY']);
					setVar('HOSP_HISTORY_DS', _grd['HOSP_HISTORY_DS']);
					executeAction('changeAnnStatus', function() {
						setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
						refreshDataSet('DS_HPK_PLAN_DAY');
					});
				}
			}

			Form.rollbackIB = function() {
				if (confirm('Отменить аннулирование ИБ? Часть информации по госпитализации восстановлена не будет')) {
					setVar('HOSP_HISTORY', _grd['HOSP_HISTORY']);
					executeAction('rollbackAnnIB', function() {
						setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
						base().refreshHPK_PLAN_DAY();
					});
				}
			};
			Form.setVMP = function() {
				setVar('HPK_PLAN_JOURNAL', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('ModalResult', 0);
				openWindow({
                    name: 'HospPlan/setPrivilege',
                    vars: {
                        'AGENT_ID': getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_AGENT']
                    }
                }, true).addListener('onafterclose',
								function() {
									if (getVar('ModalResult') == 1) {
										setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
										base().refreshHPK_PLAN_DAY();
									}
								});
			};
			Form.showPayAcc = function() {
				setVar('PERSMEDCARD', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				setVar('AGENT', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_AGENT']);
				openWindow('PersonalAccount/patient_contracts', true, 1222, 684);
			};
			Form.openJoinedDocs = function() {
				openD3Form('HospPlan/hp_view_joined_docs', true, {
					width: 800,
					height: 350,
					vars: {
						DOC_ID: getControlProperty('GR_HPK_PLAN_DAY', 'data').DIRECTION,
						UNITCODE: 'DIRECTIONS',
						ACTION: getControlProperty('GR_HPK_PLAN_DAY', 'data').DIRECTION ? 'edit' : 'new'
					}
				});
			};
			Form.showAnalyze = function() {
				setVar('PERSMEDCARD', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				setVar('DISEASECASE_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['DISEASECASE']);
				openWindow('HospPlan/patient_analyze', true);
			};
			Form.showHHOperations = function() {
				setVar('DATE_IN', getControlProperty('GR_HPK_PLAN_DAY', 'data')['DATE_IN']);
				setVar('FROM_HOSP_PLAN', 1);
				setVar('HOSP_HISTORY_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
				setVar('HPK_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('PERSMEDCARD', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				setVar('DISEASECASE', getControlProperty('GR_HPK_PLAN_DAY', 'data')['DISEASECASE']);
				executeAction('getCurrentHHDep', null, null, 0, 0);
                openWindow({
                    name: 'ArmPatientsInDep/Wrappers/direction_operations',
                    vars: {
                        'DATE_IN': getControlProperty('GR_HPK_PLAN_DAY', 'data')['DATE_IN'],
                        'AGENT_ID': getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_AGENT']
                    }
                }, true);
			};
			Form.showDirectionServiceControl = function(type) {
				setVar('TYPE', type);
				type = !empty(type) ? type + '_' : '';
				var dataGrid = getControlProperty('GR_HPK_PLAN_DAY', 'data');
				setVar('CONTRACT', dataGrid['CONTRACT_ID']);
				setVar('HH_ID', dataGrid[type + 'HOSP_HISTORY']);
				setVar('PAYMENT_KIND_ID', dataGrid['PAYMENT_KIND_ID']);
				setVar('HPK_PLAN_JOURNAL', dataGrid['ID']);
				executeAction('getInfoFromHHDeps',null, null, null, false);
				var pkFromHH = getVar('HH_PAYMENT_KIND');

				openWindow({
					name: 'HospPlan/Dirs/hh_direction_service_control',
					vars: {
					    TYPE: getVar('TYPE'),
						HH_ID: dataGrid[type + 'HOSP_HISTORY'],
						PMC_ID: dataGrid[type + 'PATIENT_ID'],
						PMC_FIO: dataGrid[type + 'PATIENT'],
						DISEASECASE: dataGrid[type + 'DISEASECASE'],
						DIR_SERV_CONTROL_MODAL_MODE: 0,
						PAYMENT_KIND_ID: pkFromHH !== dataGrid['PAYMENT_KIND_ID'] ? pkFromHH : dataGrid['PAYMENT_KIND_ID'],
						CONTRACT_ID: empty(getVar('CONTRACT_CAPTION')) ? null : dataGrid['CONTRACT_ID'],
						CONTRACT_CAPTION: getVar('CONTRACT_CAPTION'),
						HPK_PLAN_JOURNAL: getVar('HPK_PLAN_JOURNAL')
					}}, true);
			};

			Form.PrintPatientAgreement = function() {
				setVar('ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('AGENT_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_AGENT']);
				printReportByCode('patient_agreement');
			};
			Form.PrintStacionaryAct = function() {
				setVar('ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				openWindow('Reports/HospPlan/stationary_act', true);
			};
			Form.PrintComissionVMP = function() {
				setVar('ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				openWindow('Reports/HospPlan/comission_vmp_protocol_call', true, 350, 375);
			}
			Form.PrintPlanOnDate = function() {
				setVar('JOURNAL_ID', getValue('C_DS_HOSP_PLAN_KINDS'));
				if (!empty(getVar('JOURNAL_ID')) && (getVar('JOURNAL_ID') > 0)) {
					setVar('DATE_PLAN', getValue('DDATE'));
					printReportByCode('hosp_plan_on_date');
				} else {
					alert('Не выбран журнал для отчета')
				}
			}
			Form.PrintReportJournalPatient = function() {
				openWindow('Reports/HospPlan/journal_hpk_patient_call', true);
			};
            Form.PrintReportJournalPatient530n = function() {
				openD3Form('Reports/HospPlan/journal_hpk_patient_530n_call', true);
			};
			Form.PrintReportPlanDir = function() {
				setVar('DATE', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PLAN_DATE']);
				printReportByCode('hpk_plan_directions', 830, 768);
			};
			Form.PrintFirstList = function() {

				var HOSP_HISTORY_DS = getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY_DS'];
				if (HOSP_HISTORY_DS == 2) {
					alert('ИБ направлена на удаление');
					return;
				}
				if (HOSP_HISTORY_DS == 1) {
					alert('ИБ аннулирована');
					return;
				}
				if (getControlProperty('GR_HPK_PLAN_DAY', 'data')['BEDS'] == 0) {
					alert('История болезни не создана');
					return;
				}
				setVar('HH_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
				if (getControlProperty('GR_HPK_PLAN_DAY', 'data')['HH_TYPE'] == 1)
					printReportByCode('birth_history', 830, 768);
				else
					printReportByCode('hosphistory_head', 830, 768);

			};
			Form.PrintStopCard = function() {
				setVar('REP_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
				printReportByCode('stop_pregnancy_card', 830, 768);
			};
			Form.PrintPatientsIndept = function() {
				openWindow('Reports/HospPlan/patient_indept_sys_hpk_call', true);
			};
			Form.PrintReportListPat = function() {
				openWindow('Reports/HospPlan/patient_incom_date_call', true);
			};

			Form.reHideInfo = function() {
			    var control = getControlByName('HINT1');
			    var isNotShow = control.style.display === 'none';
				setDomVisible(control, !isNotShow);
			};

			Form.openWindowHint = function() {
				showLegend("Журнал госпитализации",
						[{
							backgroundColor: "#FFF",
							caption: 'История болезни не создана'
						}, {
							backgroundColor: "#F00",
							caption: 'Не направлен в отделение'
						}, {
							backgroundColor: "#699",
							caption: 'Направлен в отделение'
						}, {
							backgroundColor: "#6C6",
							caption: 'Госпитализирован'
						}, {
							backgroundColor: "#69F",
							caption: 'Выписан'
						}, {
							backgroundColor: "#A7A7A7",
							caption: 'ИБ направлена на удаление'
						}, {
							backgroundColor: "#960",
							caption: 'ИБ аннулирована'
						}, {
							backgroundColor: "#db9d00",
							caption: 'Отказ от госпитализации'
						}], 425, 260
				);
			};
			Form.createIb = function (is_ready) {
				setVar('HPK_PLAN_DAY_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID']);
				setVar('IS_READY', is_ready);
				executeAction('setIsReady', function() {
					setControlProperty('GR_HPK_PLAN_DAY', 'locate', getValue('GR_HPK_PLAN_DAY'));
					base().refreshHPK_PLAN_DAY();
				});
			};
			Form.PrintPatientAgreePers = function() {
				setVar('AGENT_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_AGENT']);
				printReportByCode('reception_agreement_personal_info');
			};

			Form.PrintPatientAgreeSif = function() {
				setVar('HH_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
				setVar('PATIENT_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				printReportByCode('reception_agreement_sifilis');
			};

			Form.PrintPatientAgreeSifDop = function() {
				printReportByCode('reception_agreement_sifilis_dop');
			};

			Form.refreshHPK_PLAN_DAY = function(_param, notRefresh) {
				if (!empty(getVar('JOURNAL_TYPE')) || (getVar('JOURNAL_TYPE') == 0 && getValue('C_DS_HOSP_PLAN_KINDS') != -1)) {
					executeAction('GetRight', null, null, 0, 0);
				}
				if (empty(_param) && (notRefresh !== true)) {
					refreshDataSet('DS_HPK_PLAN_DAY');
				}
			};

			Form.refreshWithCurrentLocate = function() {
				var rows = getCloneObjectsByRepeaterName('GR_HPK_PLAN_DAY_Row', 'GR_HPK_PLAN_DAY_Row');
				for (var i = 0; i < rows.length; i++) {
					if (rows[i].className == 'activdata') {
						if (rows[i + 1] != undefined) {
							setControlProperty('GR_HPK_PLAN_DAY', 'locate', rows[i + 1].clone.data['ID']);
							break;
						} else if (rows[i - 1] != undefined) {
							setControlProperty('GR_HPK_PLAN_DAY', 'locate', rows[i - 1].clone.data['ID']);
							break;
						}
						//var prior = getControlByName('GR_HPK_PLAN_DAY').querySelectorAll('[cmptype][class*="prior_page"]')[0];
						break;
					}
				}
				base().refreshHPK_PLAN_DAY();
			}
			Form.cancelHosp = function() {
				setVar('LOC', getValue('GR_HPK_PLAN_DAY'));
				setVar('HOSP_DIR', getControlProperty('GR_HPK_PLAN_DAY', 'data')['DIRECTION']);
				setVar('HPK_ID', getValue('GR_HPK_PLAN_DAY'));
				openWindow('Directions/direction_cancel_reason', true)
				.addListener('onafterclose', function() {
					if (getVar('ModalResult') == 1) {
						setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('LOC'));
                        executeAction('updateWLHByHospCancel');
						base().refreshHPK_PLAN_DAY();
					}
				});
			};

			Form.reverseCancelHosp = function() {
				setVar('LOC', getValue('GR_HPK_PLAN_DAY'));
				setVar('HOSP_DIR', getControlProperty('GR_HPK_PLAN_DAY', 'data')['DIRECTION']);
				setVar('HPK_ID', getValue('GR_HPK_PLAN_DAY'));

				executeAction('reverseCancelHosp', function() {
					setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('LOC'));
					base().refreshHPK_PLAN_DAY();
				});
			};

			Form.massPrintAdrList = function(type) {
				setVar('REP_DATE', getValue('DDATE'));
				setVar('REP_TYPE', type);

				if (!empty(getValue('GR_HPK_PLAN_DAY_SelectList'))) {
					setVar('IDS', getValue('GR_HPK_PLAN_DAY_SelectList'));
				}

				openWindow('Reports/HospPlan/adr_list_mass_call', true);
			};

			Form.getRealPRIMARY = function() {
				/* костыль для присоединенных документов, поскольку им нужна ID. Эту функция вызывается в компоненте AutoPopupMenu.
				 * Сам  костыль сделан в  Jun 07 2013 #110975 Ошибка при выборе в контекстном меню пункта "Присоединенные документы"
				 */
				return getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID'];
			};

			Form.afterSelPMC = function() {
				if (getVar('newid')) {
					setControlProperty('GR_HPK_PLAN_DAY', 'locate', getVar('newid'));
					refreshDataSet('DS_HPK_PLAN_DAY');
					setVar('newid', '');
					executeAction('ActionPlaces');
				}
			};

			Form.prepareVariables = function() {
			    if (!empty(getValue('GR_HPK_PLAN_DAY'))) {
			        var data = getControlProperty('GR_HPK_PLAN_DAY', 'data');
					setVar('HPK_PLAN_JOURNAL_ID', data['ID']);
					setVar('PMC_ID', data['PATIENT_ID']);
					setVar('PMC_FIO', data['PATIENT']);
					setVar('DIRECTION_ID', data['DIRECTION']);
					setVar('HOSP_PLAN_KIND', data['HOSP_PLAN_KIND']);
                    setVar('REP_AGENT_ID', data['PATIENT_AGENT']);
			    }
			};

			Form.setBedLoadKF = function() {
				setControlProperty('BedLoadKF','enabled', !empty(getValue('DDATE')));
				if (!empty(getValue('DDATE'))) {
					getControlByName('BedLoadKF').removeAttribute('title')
				} else {
					getControlByName('BedLoadKF').setAttribute('title','Необходимо заполнить дату')
				}
			};

			Form.openPatientIa = function() {
				setVar('PATIENT', getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID']);
				openWindow({
					name: 'InformedAgreement/Patient/patient_ia_window',
					vars: {
						PATIENT: getVar('PATIENT'),
					}
				}, true, 600, 400);
			};

			Form.onPatientChange = function() {
				openWindow({
					name: 'Persmedcard/search_patient_change',
					vars: {
						PLAN_JOURNAL: getControlProperty('GR_HPK_PLAN_DAY', 'data')['ID'],
						PATIENT: getControlProperty('GR_HPK_PLAN_DAY', 'data')['PATIENT_ID'],
					}
				}, true, 800, 400)
				.addListener('onafterclose', function(){
					if (+getVar('ModalResult') === 1) {
						base().refreshHPK_PLAN_DAY();
					}
				});
			};
			Form.setHidePopUp = function(popUpList, bool) {
			    var popCtrl = getControlByName('P_HPK_PLAN');
                (popUpList || []).forEach(function(pName) {
                    PopUpItem_SetHide(popCtrl, pName, bool);
                });
            };
			]]>
		</component>
		<component cmptype="Script" name="popup_script">
			<![CDATA[
                Form.onPopupFunc = function() {
                    _grd = getControlProperty('GR_HPK_PLAN_DAY', 'data');
                    setVar('DIRECTION', _grd['DIRECTION']);
                    if (!empty(getValue('C_DS_HOSP_PLAN_KINDS')) && empty(getValue('DDATE'))) {
                        setVar('UNFILLED_DATE', 1);
                    } else {
                        setVar('UNFILLED_DATE', 0);
                    }
                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pHOSPIT_S', true);
                    if (+_grd['JT'] === 0) { // не очередь
                        PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pSHOW_OPERS', false);
                    } else {
                        PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pSHOW_OPERS', true);
                    }
                    if (+getVar('UNFILLED_DATE') === 1 || +getVar('AVAIL_ADD') === 0) {
                        PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pADD', true);
                    } else {
                        PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pADD', false);
                    }
                    if (empty(getVar('OUTER_LPUDICT'))) {
                        Form.jt = (_grd['JT']) ? _grd['JT'] : 0;
                        if ((+getVar('JOURNAL_TYPE') === 1 || +Form.jt === 1) && +_grd['RECORD_STATUS'] === 0) {
                            PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pRECORD', false);
                        } else {
                            PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pRECORD', true);
                        }
                        if (empty(getValue('GR_HPK_PLAN_DAY'))) { //pCANC_ANN pANN pANN_DO
                            var popUpList = ['pEDIT', 'pMOVE_HISTORY', 'pDEL','pCANC_ANN', 'pANN', 'pANN_DO', 'pHOSPIT', 'pHOSPIT_CANC', 'pOPENIB', 'pDIR_S', 'pANALYZE',
                                            //переместить на другую дату
                                            'pCH_PLAN', 'pCH_PLAN_RAZD', 'pREPS', 'pSHOW_VMP', 'pHIST_HOSP', 'pFAC_ACC', 'pRAZDELITEL', 'RAZD2', 'RAZD3', 'RAZD4', 'pCancelHosp',
                                            'pReverseCancelHosp', 'pCREATEIB_true', 'pCREATEIB_false', 'pDIR_SERVICES', 'pDIR_SERVICES2'];
                            Form.setHidePopUp(popUpList, true);
                        } else {
                            var popUpList = ['pMOVE_HISTORY', 'RAZD2', 'RAZD3', 'RAZD4', 'pRAZDELITEL', 'pREPS', 'pFAC_ACC', 'pANALYZE', 'pCREATEIB_true', 'pCREATEIB_false'];
                            Form.setHidePopUp(popUpList, false);
                            setVar('PATIENT_ID', _grd['PATIENT_ID']);
                            setVar('AGENT_ID', _grd['PATIENT_AGENT']);
                            setVar('HH_ID', _grd['HOSP_HISTORY']);
                            setVar('HP_ID', _grd['ID']);
                            if (+_grd['BEDS'] !== 0) {  // если есть ИБ,то не доступны Редактировать, Удалить
                                Form.setHidePopUp(['pEDIT', 'pDEL'], true);
                            } else {
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pEDIT', false);
                                /*если нет ИБ, и оформлен отказ,то не доступно Удалить*/
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pDEL', (+_grd['IS_CANCELED'] === 1));
                            }
                            /*Если статус записи Отработана или есть отказ от госпитализации,
                             не доступен пункт Перенести на другую дату*/
                            if ((+_grd['RECORD_STATUS'] === 1) || (+_grd['IS_CANCELED'] === 1)) {
                                Form.setHidePopUp(['pCH_PLAN', 'pCH_PLAN_RAZD'], true);
                            } else {
                                Form.setHidePopUp(['pCH_PLAN', 'pCH_PLAN_RAZD'], false);
                            }
                            /*Направления на услуги показываем всегда, кроме случая, когда ИБ списана*/
                            if (+_grd['HOSP_HISTORY_DS'] === 1) {
                                Form.setHidePopUp(['pDIR_SERVICES', 'pDIR_SERVICES2'], true);
                            } else {
                                /*если есть ИБ и есть ИБ сопровождающего то показываем направления пациента и сопровождающего, иначе - только пациента*/
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pDIR_SERVICES', !empty(_grd['RELATIVE_HOSP_HISTORY']));
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pDIR_SERVICES2', empty(_grd['RELATIVE_HOSP_HISTORY']));
                            }

                            /*Иб действует и пациент не выписан*/
                            if (!empty(_grd['HOSP_HISTORY']) && +_grd['HOSP_HISTORY_DS'] === 0 && +_grd['BEDS'] !== 3) {
                                Form.setHidePopUp(['pCANC_ANN', 'pANN_DO', 'pHOSPIT'], true);
                                Form.setHidePopUp(['pHOSPIT_CANC', 'pANN', 'pOPENIB'], false);
                            } else if (+_grd['HOSP_HISTORY_DS'] === 1) { // ИБ списана
                                Form.setHidePopUp(['pANN', 'pCANC_ANN', 'pANN_DO', 'pHOSPIT', 'pHOSPIT_CANC', 'pOPENIB'], true);
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pHOSPIT_S', false);
                            } else if (+_grd['HOSP_HISTORY_DS'] === 2) { // ИБ направлена на списание
                                Form.setHidePopUp(['pHOSPIT_CANC', 'pANN', 'pHOSPIT', 'pOPENIB'], true);
                                Form.setHidePopUp(['pCANC_ANN', 'pANN_DO'], false);
                            } else { // нет ИБ или выписан
                                Form.setHidePopUp(['pCANC_ANN', 'pANN_DO', 'pHOSPIT_CANC'], true);
                                if (empty(_grd['HOSP_HISTORY'])) {
                                    Form.showPopUpjt('pHOSPIT');
                                    Form.setHidePopUp(['pANN', 'pOPENIB'], true);
                                } else {
                                    Form.setHidePopUp(['pANN', 'pOPENIB'], false);
                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pHOSPIT', true);
                                }
                            }
                            if ((+_grd['HPK_JOURNAL_TYPE']) !== 0 || +_grd['IS_CANCELED'] === 1) { // 0- обычный, то есть ниже только для очередей или если есть отказ от госпитализации*/
                                Form.setHidePopUp(['pHOSPIT', 'pHOSPIT_CANC', 'pANN', 'pOPENIB'], true);
                            }
                            // Отказ от госпитализации, отправлен на создание ИБ
                            if (!empty(_grd['HOSP_HISTORY'])) { // 18.12.2013 #66488 Доработка "Истории заболеваний и результаты исследований"отображать Причину отказа от госпитализации другого ГУЗ НСО.
                                Form.setHidePopUp(['pCancelHosp', 'pReverseCancelHosp', 'pCREATEIB_true', 'pCREATEIB_false'], true);
                            } else {
                                if (+_grd['IS_CANCELED'] === 1) {
                                    Form.setHidePopUp(['pCancelHosp', 'pCREATEIB_true', 'pCREATEIB_false'], true);
                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pReverseCancelHosp', false);
                                } else {
                                    Form.showPopUpjt('pCancelHosp');
                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pReverseCancelHosp', true);
                                    /*  7078:5a20587ad107 - Бурнаев 12.12.2012
                                     #87153: Перенести на WEB-интерфейс возможность подтверждать направление в Журнал госитализации
                                     #81221: Настройка прав на мед. словарь "видимость мед. словаря по услуге"
                                     */
                                    if ((_grd['hosp_ready_rights'] === undefined) && (+_grd['IS_CANCELED'] === 0)) {
                                        setVar('HOSP_PLAN_KIND', _grd['HOSP_PLAN_KIND']);
                                        executeAction('A_hosp_ready_rights', function() {
                                            if (+getVar('hosp_ready_rights') === 1) {
                                                if (+_grd['IS_READY'] === 1) {
                                                    Form.showPopUpjt('pCREATEIB_true');
                                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pCREATEIB_false', true);
                                                } else {
                                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pCREATEIB_true', true);
                                                    Form.showPopUpjt('pCREATEIB_false');
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                            /*ВМП,Хронология*/
                            /*
                            * Отдельная проверка для ВМП по задаче 247398
                            */
                            var V_HH_ID = parseInt(getVar("HH_ID"));
                            if (V_HH_ID != V_HH_ID || +_grd['IS_CANCELED'] === 1) { //проверка на isNaN
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pSHOW_VMP', true);
                            } else {
                                executeAction('getCurrentHHDep', null, null, 0, 0)
                                var V_HH_DEP_ID = parseInt(getVar("HH_DEP_ID"));
                                if (V_HH_DEP_ID != V_HH_DEP_ID) { //проверка на isNaN
                                    PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pSHOW_VMP', true);
                                } else {
                                    Form.showPopUpjt('pSHOW_VMP');
                                }
                            }
                            if (+_grd['IS_CANCELED'] === 1) {
                                PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pHIST_HOSP', true);
                            } else {
                                Form.showPopUpjt('pHIST_HOSP');
                            }
                        }
                    } else {// если записть в журнал чужого ЛПУ
                        var popUpList = ['pRECORD', 'pEDIT', 'pMOVE_HISTORY', 'pDEL', 'pCANC_ANN', 'pANN', 'pANN_DO', 'pHOSPIT', 'pHOSPIT_CANC', 'pOPENIB', 'pDIR_S', 'pANALYZE',
                                         //переместить на другую дату
                                         'pCH_PLAN', 'pCH_PLAN_RAZD', 'pREPS', 'pSHOW_VMP', 'pHIST_HOSP', 'pFAC_ACC', 'pRAZDELITEL', 'RAZD2', 'RAZD3', 'RAZD4', 'pDIR_SERVICES',
                                         'pDIR_SERVICES2', 'pCancelHosp', 'pReverseCancelHosp'];
                        Form.setHidePopUp(popUpList, true);
                        if (empty(getValue('GR_HPK_PLAN_DAY'))) {
                            Form.setHidePopUp(['pEDIT', 'pDEL'], true);
                        } else {
                            Form.setHidePopUp(['pEDIT', 'pDEL'], false);
                            if (+_grd['BEDS'] !== 0) {
                                Form.setHidePopUp(['pEDIT', 'pDEL'], true);
                            }
                        }
                    }
                    if (typeof Form.SHOW_POPUP_EDIT === 'function') {
                        Form.SHOW_POPUP_EDIT(); //для РКОД
                    }
                    if (+_grd['IS_CANCELED'] === 1 || !empty(_grd['HOSP_HISTORY'])) {
                        PopUpItem_SetHide(getControlByName('P_HPK_PLAN'), 'pHOSPIT', true);
                    }
                };
                Form.printReport057Y04 = function() {
                    var data = getControlProperty('GR_HPK_PLAN_DAY', 'data');
                    setVar('DIRECTION', data['DIRECTION']);
                    setVar('OUTER_DIRECTION', data['OUTER_DIRECTION_ID']);
                    printReportByCode('OutDirServNew');
                }
            ]]>
		</component>
	  	<component cmptype="Script" name="script_tat">
			<![CDATA[
			Form.PrintStatCard = function() {
				setVar('REP_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']);
				if (+getControlProperty('GR_HPK_PLAN_DAY', 'data')['BEDS'] === 3) {
					printReportByCode('outhost_stat_card', 830, 768);
				} else {
					printReportByCode('out_history_card', 830, 768);
				}
			};
			]]>
	  	</component>
        <component cmptype="Script" name="script_ehr">
            <![CDATA[
            Form.openEHR = function() {
                openWindow({
                    name: 'HospPlan/hospplan_ehrs',
                    vars: {
                        EHR_UNIT_ID: getControlProperty('GR_HPK_PLAN_DAY', 'data')['DISEASECASE'],
                        EHR_UNIT: 'DISEASECASE'
                    }
                }, true);
            }
            ]]>
        </component>
		<component cmptype="Action" name="delInfoHpkScheduleReg">
			<![CDATA[
            begin
              D_PKG_HPK_SCHEDULE_REG.DEL(pnID  => :pnHPK_SCHEDULE_REG,
                                         pnLPU => :pnLPU_TO_ID);
            end;
            ]]>
			<component cmptype="ActionVar" name="pnHPK_SCHEDULE_REG"   src="HPK_SCHEDULE_REG"   srctype="var"/>
			<component cmptype="ActionVar" name="pnOUTER_DIRECTION_ID" src="OUTER_DIRECTION_ID" srctype="var"/>
			<component cmptype="ActionVar" name="pnLPU_TO_ID"          src="LPU_TO"             srctype="var"/>
		</component>
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
			<component cmptype="ActionVar" name="pnDIRECTION"        src="DIRECTION"        srctype="var"/>
			<component cmptype="ActionVar" name="pnHPK_SCHEDULE_REG" src="HPK_SCHEDULE_REG" srctype="var" put="" len="17"/>
		</component>
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
			<component cmptype="ActionVar" name="LPU" 				src="LPU" 				srctype="session" get="g1"/>
			<component cmptype="ActionVar" name="HH_ID" 			src="HH_ID" 			srctype="var" get="gHH_ID"/>
			<component cmptype="ActionVar" name="CONTRACT" 			src="CONTRACT" 			srctype="var" get="gCONTRACT"/>
			<component cmptype="ActionVar" name="PAYMENT_KIND_ID" 	src="PAYMENT_KIND_ID" 	srctype="var" get="gPAYMENT_KIND_ID"/>
			<component cmptype="ActionVar" name="CONTRACT_CAPTION"  src="CONTRACT_CAPTION" 	srctype="var" put="gCONTRACT_CAPTION" len="1000"/>
			<component cmptype="ActionVar" name="HH_PAYMENT_KIND"   src="HH_PAYMENT_KIND" 	srctype="var" put="gHH_PAYMENT_KIND"  len="100"/>
		</component>
		<component cmptype="Action" name="ACancelHosp">
			<![CDATA[
			begin
			  if :SMP_CALL_EXS_IDS is not null then
                for r in (select sces.ID,
                                 sces.HOSP_STATUS,
                                 sces.HPK_ID,
                                 sces.REFUSE_REASON
                            from D_V_SMP_CALL_EX_SYSTEM sces
                           where sces.ID in (select /*+ cardinality(ids, 1)*/ ids.COLUMN_VALUE ID
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
			<component cmptype="ActionVar" name="SMP_CALL_EXS_IDS" src="SMP_CALL_EXS_IDS" srctype="var"/>
			<component cmptype="ActionVar" name="HH_ID"            src="HH_ID_DEL"        srctype="var"/>
			<component cmptype="ActionVar" name="LPU"              src="LPU"              srctype="session"/>
		</component>
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
            <component cmptype="ActionVar" name="HH_ID"            src="HH_ID_DEL"        srctype="var"/>
            <component cmptype="ActionVar" name="SMP_CALL_EXS_IDS" src="SMP_CALL_EXS_IDS" srctype="var" put=""/>
        </component>
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
			<component cmptype="ActionVar" name="HOSP_DIR" src="HOSP_DIR" srctype="var"     get="g0"/>
			<component cmptype="ActionVar" name="HPK_ID"   src="HPK_ID"   srctype="var"     get="g1"/>
			<component cmptype="ActionVar" name="LPU"      src="LPU"      srctype="session" get="g2"/>
			<component cmptype="SubAction" mode="execlast">
				<![CDATA[
				begin
					D_PKG_HPK_PLAN_JOURNALS.SET_RECORD_STATUS(:HPK_ID, :LPU, 0);
				end;
				]]>
				<component cmptype="SubActionVar" name="HPK_ID" get="g3" src="HPK_ID" srctype="parent"/>
				<component cmptype="SubActionVar" name="LPU"    get="g4" src="LPU"    srctype="parent"/>
			</component>
		</component>
		<component cmptype="Action" name="getDefDepFromOption">
			<![CDATA[
			begin
				:DP_NAME := D_PKG_OPTION_SPECS.GET('SchRegDepDefault', :LPU);
			end;
			]]>
			<component cmptype="ActionVar" name="LPU"     src="LPU"                   srctype="var" get="v0"/>
			<component cmptype="ActionVar" name="DP_NAME" src="DP_NAME_FOR_REG_SHORT" srctype="var" put="DPN" len="4000"/>
		</component>
		<component cmptype="Action" name="A_hosp_ready_rights">
			<![CDATA[
				BEGIN
					:RIGHTS := D_PKG_CSE_ACCESSES.check_employer_right(:pnlpu,d_pkg_employers.get_id(:pnlpu),'HOSP_PLAN_KINDS',:unit_id,8);
				END;
			]]>
			<component cmptype="ActionVar" name="pnlpu"   src="LPU"               srctype="var" get="lpu" />
			<component cmptype="ActionVar" name="unit_id" src="HOSP_PLAN_KIND"    srctype="var" get="unit_id" />
			<component cmptype="ActionVar" name="RIGHTS"  src="hosp_ready_rights" srctype="var" put="hosp_ready_rights" len="1" />
		</component>
		<component cmptype="Action" name="setIsReady">
			<![CDATA[
			BEGIN
				D_PKG_HPK_PLAN_JOURNALS.SET_IS_READY(pnID => :ID,
				                                    pnLPU => :LPU,
				                               pnIS_READY => :IS_READY);
			END;
			]]>
			<component cmptype="ActionVar" name="ID"       src="HPK_PLAN_DAY_ID" srctype="var" get="id" />
			<component cmptype="ActionVar" name="LPU"      src="LPU"             srctype="var" get="lpu" />
			<component cmptype="ActionVar" name="IS_READY" src="IS_READY"        srctype="var" get="is_ready" />
		</component>
  		<component cmptype="Popup" name="P_HPK_PLAN" popupobject="GR_HPK_PLAN_DAY" onpopup="base().onPopupFunc();">
			<component cmptype="PopupItem" name="pREF"  caption="Обновить"   onclick="base().onJournalChange();" cssimg="refresh"/>
			<component cmptype="PopupItem" name="pPatientIa"  caption="Информированные согласия и отказы" onclick="base().openPatientIa();" cssimg="report"/>
			<component cmptype="PopupItem" name="pRAZDELITEL" caption="-"/>
			<!--<component cmptype="PopupItem" name="pChangePatient"  caption="Сменить пациента"   onclick="base().onPatientChange();" />-->
			<component cmptype="PopupItem" name="pCH_PLAN" caption="Перенести на другую дату" onclick="base().ChangeDate();" cssimg="move"/>
			<component cmptype="PopupItem" name="pCH_PLAN_RAZD" caption="-"/>
			<component cmptype="PopupItem" name="pADD"  caption="Добавить"   onclick="base().AddPat();" cssimg="insert"/>
			<component cmptype="PopupItem" name="pEDIT" caption="Изменить"   onclick="base().EditPat();" cssimg="edit"/>
			<component cmptype="PopupItem" name="pDEL"  caption="Удалить" onclick="base().DelPat();" cssimg="delete"/>
			<component cmptype="PopupItem" name="pCancelHosp" caption="Оформить отказ от госпитализации" onclick="base().cancelHosp()" cssimg="ok"/>
			<component cmptype="PopupItem" name="pReverseCancelHosp" caption="Отменить отказ от госпитализации" onclick="base().reverseCancelHosp()" image="Images/img2/ok-confirm.png"/>
			<component cmptype="PopupItem" name="RAZD2" caption="-"/>
    		<!-- @gen_reg начало -->
    		<component cmptype="PopupItem" name="pRECORD"  caption="Записать"   onclick="base().genRegPat();" cssimg="insert"/>
    		<!-- @gen_reg конец -->
    		<component cmptype="PopupItem" name="pMOVE_HISTORY"  caption="История перемещений"   onclick="base().MoveHistory();"/>
			<!-- Госпитализировать -->
			<component cmptype="PopupItem" name="pHOSPIT" caption="Госпитализировать" onclick="base().checkIfHHIsSingle();" image="Images/img2/ok-confirm.png"/>
			<component cmptype="PopupItem" name="pHOSPIT_S" caption="Госпитализировать повторно" onclick="base().checkIfHHIsSingle();" image="Images/img2/ok-confirm.png"/>
			<component cmptype="PopupItem" name="pHOSPIT_CANC" caption="Отменить госпитализацию" onclick="base().CancelHosp();" image="Images/img2/cancel.png"/>
			<component cmptype="PopupItem" name="pOPENIB" caption="Открыть ИБ" onclick="base().toHosp();" image="Images/img2/rights.gif"/>
      		<component cmptype="PopupItem" name="pCREATEIB_true" caption="Отправлен на создание ИБ" onclick="base().createIb(0);" image="Images/img2/ok.gif" />
      		<component cmptype="PopupItem" name="pCREATEIB_false" caption="Отправлен на создание ИБ" onclick="base().createIb(1);" />
      		<!-- Анн ИБ-->
			<component cmptype="PopupItem" name="pANN"	caption="Аннулировать ИБ" onclick="base().annulmentHH();" cssimg="delete" unitbp="HOSP_HISTORIES_SET_DISCART_STATUS"/>
			<component cmptype="PopupItem" name="pCANC_ANN" caption="Отменить аннулирование ИБ" onclick="base().rollbackIB();" image="Images/img2/cancel.png" unitbp="HOSP_HISTORIES_SET_DISCART_STATUS"/>
			<component cmptype="PopupItem" name="pANN_DO" caption="Подтвердить аннулирование ИБ" onclick="base().annulmentHH();" cssimg="delete" unitbp="HOSP_HISTORIES_SET_DISCART_STATUS"/>
			<!-- -->
			<component cmptype="PopupItem" name="pSHOW_VMP" caption="ВМП" image="Forms/HospPlan/img/vmp.png" onclick="base().setVMP();"/>
			<component cmptype="PopupItem" name="RAZD3" caption="-"/>
			<component cmptype="PopupItem" name="pHIST_HOSP"  caption="Хронология госпитализаций" onclick="base().HistHosp();" image="Forms/HospPlan/img/list.gif"/>
			<component cmptype="PopupItem" name="pSHOW_OPERS"  caption="Операции" onclick="base().showHHOperations();" image="Images/img2/docum.png"/>
			<component cmptype="PopupItem" name="pDIR_SERVICES" caption="Направления на услуги" onclick="base().showDirectionServiceControl();" image="Images/img2/docum.png"/>
			<component cmptype="PopupItem" name="pDIR_SERVICES2" caption="Направления на услуги" image="Images/img2/docum.png">
            	<component cmptype="PopupItem" caption="Пациент" onclick="base().showDirectionServiceControl();"/>
            	<component cmptype="PopupItem" caption="Сопровождающее лицо" onclick="base().showDirectionServiceControl('RELATIVE');"/>
        	</component>
			<component cmptype="PopupItem" name="pANALYZE" caption="Анализы" onclick="base().showAnalyze();" image="Images/img2/docum.png"/>
			<component cmptype="PopupItem" name="pFAC_ACC" caption="Лицевой счет" onclick="base().showPayAcc();" cssimg="pay"/>
			<component cmptype="PopupItem" name="pJOINED_DOCS" caption="Просмотр прикрепленных документов" onclick="base().openJoinedDocs();" image="Images/img2/docum.png"/>
			<component cmptype="PopupItem" name="RAZD4" caption="-"/>
			<component cmptype="PopupItem" name="pREPS" caption="Отчеты" cssimg="print">
				<component cmptype="PopupItem" name="pFIRST_LIST_HH" caption="Первый лист ИБ (действителен до 1 марта 2023 г.)" cssimg="print" onclick="base().PrintFirstList();" />
                <component cmptype="PopupItem" name="pSTOP_PREG_CARD" caption="Карта прерывания беременности" cssimg="print" onclick="base().PrintStopCard();" />
				<component cmptype="PopupItem" name="pPRINTSTATCARD" caption="Статистическая карта" cssimg="print" onclick="base().PrintStatCard();"/>
				<component cmptype="PopupItem" name="pAMB" caption="-"/>
				<component cmptype="PopupItem" name="pACT_STAC_HELP" caption="Акт на стационарную помощь" cssimg="print"  onclick="base().PrintStacionaryAct();"/>
				<component cmptype="PopupItem" name="pREP_AGREEGroup" caption="Информационное согласие" cssimg="print">
            		<component cmptype="PopupItem" name="pREP_AGREE" caption="Информационное согласие" cssimg="print" onclick="base().PrintPatientAgreement();"/>
            		<component cmptype="PopupItem" name="pREP_AGREE_PERS" caption="Инф. согл. на обработку перс. данных" cssimg="print" onclick="base().PrintPatientAgreePers();"/>
            		<component cmptype="PopupItem" name="pREP_AGREE_SIF" caption="Инф. Согласие на лечение и обследование на сифилис" cssimg="print" onclick="base().PrintPatientAgreeSif();"/>
            		<component cmptype="PopupItem" name="pREP_AGREE_SIF_DOP" caption="Вкладыш Обследование больного на сифилис" cssimg="print" onclick="base().PrintPatientAgreeSifDop();"/>
            	</component>
        		<component cmptype="PopupItem" caption="Массовая печать" cssimg="print">
	        		<component cmptype="PopupItem" caption="Форма № 2 - Адресный листок прибытия" cssimg="print" onclick="base().massPrintAdrList(1);"/>
	        		<component cmptype="PopupItem" caption="Форма № 7 - Адресный листок убытия" cssimg="print" onclick="base().massPrintAdrList(2);"/>
	    		</component>
				<component cmptype="PopupItem" name="RAZD5" caption="-"/>
        		<component cmptype="PopupItem" name="pREP_VMP" caption="Протокол заседания врачебной комиссии для оказания ВМП" cssimg="print" onclick="base().PrintComissionVMP();"/>
				<component cmptype="PopupItem" name="pREP_DP" caption="План госпитализации на день" cssimg="print" onclick="base().PrintPlanOnDate();"/>
				<component cmptype="PopupItem" name="pREP_PSD" caption="Поступившие в стационар за день" cssimg="print" onclick="base().PrintReportPlanDir();"/>
				<component cmptype="PopupItem" name="pINCOMING" caption="Список поступивших в стационар" cssimg="print" onclick="base().PrintReportListPat();"/>
                <component cmptype="PopupItem" name="pFORM_2" caption="Форма № 2 - Адресный листок прибытия" cssimg="print" onclick="setVar('HH_ID',getControlProperty('GR_HPK_PLAN_DAY','data')['HOSP_HISTORY']); printReportByCode('list_prib');"/>
                <component cmptype="PopupItem" name="pFORM_7" caption="Форма № 7 - Адресный листок убытия" cssimg="print" onclick="setVar('HH_ID',getControlProperty('GR_HPK_PLAN_DAY','data')['HOSP_HISTORY']); printReportByCode('list_ubyt');"/>
				<component cmptype="PopupItem" name="pILL" caption="Список больных стационара" cssimg="print" onclick="base().PrintPatientsIndept();"/>
				<component cmptype="PopupItem" name="pJOURNAL" caption="Журнал учета приема больных и отказов" cssimg="print" onclick="base().PrintReportJournalPatient();"/>
				<component cmptype="PopupItem" name="pJOURNAL530" caption="Журнал учета приема больных и отказов (530Н)" cssimg="print" onclick="base().PrintReportJournalPatient530n();"/>
				<component cmptype="PopupItem" name="pDAILY_LIST" caption="Алфавитный журнал" cssimg="print" onclick="openWindow('Reports/HospPlan/patient_leave_date_call', true);"/>
        		<component cmptype="PopupItem" name="pPAT_LIST" caption="Листок ежедневного учета движения больных и коечного фонда стационара" cssimg="print" onclick="openWindow('Reports/Statistic/movement_daily_beds_patients_call', true);"/>
        		<component cmptype="PopupItem" name="pPAT_LIST_DAY" caption="Листок ежедневного учета движения больных и коечного фонда дневного стационара" cssimg="print" onclick="openWindow('Reports/Statistic/movement_daily_beds_patients_day_call', true);"/>
        		<component cmptype="PopupItem" name="pREP_OPG" caption="Очередь пациентов на госпитализацию" cssimg="print" onclick="openWindow('Reports/HospPlan/turn_on_hospitalization_call', true);"/>
        		<component cmptype="PopupItem" name="pREP_BEREM" caption="Журнал учета приема беременных, рожениц и родильниц" cssimg="print" onclick="printReportByCode('journal_childbirth');"/>
        		<component cmptype="PopupItem" name="pWORK_FRONT_DESK_CALL" caption="Работа приемного отделения" cssimg="print" onclick="printReportByCode('work_front_desk')"/>
        		<component cmptype="PopupItem" name="pACCOMPANYING_SHEET" caption="114/у «Сопроводительный лист»" cssimg="print" onclick="setVar('HPK_ID', getControlProperty('GR_HPK_PLAN_DAY','data')['ID']);printReportByCode('114y_accompanying_sheet');"/>
        		<component cmptype="PopupItem" name="pHOSPITAL_REPORT" caption="Сводный отчет по стационару" cssimg="print" onclick="openD3Form('Reports/HospitalRep/HospitalRep_call', true);"/>
				<component cmptype="PopupItem" name="pREPORT_MED_PREGCARD_096y" caption="096/у-20 Медицинская карта беременной, роженицы и родильницы, получающей медицинскую помощь в стационарных условиях" cssimg="print" onclick="setVar('HH_ID', getControlProperty('GR_HPK_PLAN_DAY', 'data')['HOSP_HISTORY']); printReportByCode('med_pregnant_card_096y20');"/>
                <component cmptype="PopupItem" name="pOutDirServ" caption="Направление 057/у" onclick="base().printReport057Y04();" cssimg="print"/>
			</component>
            <component cmptype="PopupItem" name="pEHR" caption="Документы" onclick="base().openEHR()" image="Images/document_prepare.png"/>
 		</component>
 		<component cmptype="AutoPopupMenu" unit="HPK_PLAN_JOURNALS" all="true" join_menu="P_HPK_PLAN" popupobject="GR_HPK_PLAN_DAY">
     		<component cmptype="PopupItem" name="SummaryInformation" caption="Сводная заявка на питание (№22-МЗ)" cssimg="print" onclick="openWindow({name:'Reports/InformationConsistingNutrition/SummaryInformation_call',vars:{'param':'summary'}},true)"/>
 		</component>
    	<table style="width:100%;height:100%;">
        	<tr>
  		  		<td style="padding:3pt;border-left:1px solid #DDE2DA; border-right:1px solid #DDE2DA;border-top:1px solid #DDE2DA;text-align:center;">
	  				<component cmptype="Label" caption="Журнал: " />
	  				<component cmptype="ComboBox" name="C_DS_HOSP_PLAN_KINDS" onchange="base().onJournalChange();">
						<component cmptype="ComboItem" caption="Все" value="-1" activ="true"/>
						<!--component cmptype="ComboItem" caption="ОЧЕРЕДЬ" value=""/-->
						<component cmptype="ComboItem" datafield="ID" captionfield="HP_NAME" dataset="DS_HOSP_PLAN_KINDS" repeate="0"/>
  	  				</component>
  	  				<br/>
  	  				<component cmptype="CheckBox" name="CH_HH_ANNUL" caption="Скрывать аннулированные записи" valuechecked="1" valueunchecked="0" activ="0" onchange="refreshDataSet('DS_HPK_PLAN_DAY');"/>
	  	  		</td>
          	 	<td style="padding:3pt;border-left:1px solid #DDE2DA; border-right:1px solid #DDE2DA;border-top:1px solid #DDE2DA;text-align:center;" cmptype="tmp" name="TD_DDATE">
	  				<component cmptype="Label" caption="Дата: " />
	  				<component cmptype="DateEdit" name="DDATE" width="145pt" typeMask="date" onkeypress="onEnter(function(){base().OtobrDay();});" onchange="base().setBedLoadKF();" onblur="base().setBedLoadKF();"/>
  		  		</td>
  		  		<td style="padding:3pt;border-left:1px solid #DDE2DA; border-right:1px solid #DDE2DA;border-top:1px solid #DDE2DA;text-align:center;">
					<component cmptype="Label" caption="Поиск пациентов: " />
					<nobr>
						<component cmptype="UnitEdit" name="PERSMEDCARD" unit="PERSMEDCARD" composition="PMC_WITH_REG" width="185px" addListener="base().afterSelPMC">
							<!-- BUTTON_EDIT_DEFAULT -->
							<component cmptype="Button" type="micro" background="Icons/btn_erase" onclick="setValue('PERSMEDCARD',null,null);setCaption('PERSMEDCARD',null);setControlProperty('patpre','enabled',false); setControlProperty('patnex','enabled',false);"/>
						</component>
					</nobr>
  		  		</td>
  		  		<td style="padding-left:3pt;padding-top:3pt;padding-bottom:3pt;border-left:1px solid #DDE2DA; border-right:0px solid #DDE2DA;border-top:1px solid #DDE2DA;text-align:right;">
					<component cmptype="Label" caption="Префикс напр-я:"/><br/><component cmptype="Label" caption="Номер:"/>
  		  		</td>
		 		<td style="padding-left:3pt;padding-top:3pt;padding-bottom:3pt;border-right:1px solid #DDE2DA;border-top:1px solid #DDE2DA;text-align:left;">
  		  			<img name="pict" cmptype="pict" src="Forms/Reg/img/hlp.png" title="Показать легенду" style="cursor:pointer;float:right;" onclick="base().openWindowHint();" ondragstart="return false;"></img>
				  	<component cmptype="Edit" name="DIR_PREF" width="100"/>
					<br/>
					<component cmptype="Edit" name="DIR_NUMB" width="100"/>
  		  		</td>
			</tr>
  			<tr name="TrHEAD">
  		  		<td style="padding:3pt;border-left:1px solid #DDE2DA;border-right:1px solid #DDE2DA;border-bottom:1px solid #DDE2DA;text-align:center;">
					<component cmptype="Button" caption="Посмотреть квоты" onclick="base().ViewQuotes();" width="150"/>
	  	  		</td>
          	  	<td style="white-space: nowrap;padding:3pt;border-left:1px solid #DDE2DA;border-right:1px solid #DDE2DA;border-bottom:1px solid #DDE2DA;text-align:center;" cmptype="tmp" name="TD_BUT_DDATE">
	  				<component cmptype="Button" caption="&lt;&lt;&lt;" onclick="base().BackDay();"/>
	  				<component cmptype="Button" name="selected" caption="Отобрать" onclick="base().OtobrDay();" style="width:76px"/>
	  				<component cmptype="Button" caption="&gt;&gt;&gt;" onclick="base().ForwDay();"/>
					<component cmptype="MaskInspector" controls="DDATE" effectControls="selected"/>

  		  		</td>
  		  		<td style="white-space: nowrap;padding:3pt;border-left:1px solid #DDE2DA;border-right:1px solid #DDE2DA;border-bottom:1px solid #DDE2DA;text-align:center;">
	  				<component cmptype="Button" name="patpre" caption="&lt;&lt;&lt;" enabled="false" onclick="base().PatPrev();"/>
	  				<component cmptype="Button" caption="Поиск" onclick="base().PatSearch();" style="width:76px"/>
	  				<component cmptype="Button" name="patnex" caption="&gt;&gt;&gt;" enabled="false" onclick="base().PatNext();"/>
  		  		</td>
  		  		<td colspan="2" style="white-space: nowrap;padding-left:3pt;padding-top:3pt;padding-bottom:3pt;border-left:1px solid #DDE2DA;border-right:1px solid #DDE2DA;border-bottom:1px solid #DDE2DA;text-align:center;">
  	  				<component cmptype="Button" name="dirpre" caption="&lt;&lt;&lt;" enabled="false" onclick="base().DirPrev();"/>
	  				<component cmptype="Button" caption="Поиск" onclick="base().DirSearch();" style="width:76px"/>
	  				<component cmptype="Button" name="dirnex" caption="&gt;&gt;&gt;" enabled="false" onclick="base().DirNext();"/>
  		  		</td>
  			</tr>
  			<tr>
  				<td style="padding-top:3pt;">
  				</td>
  			</tr>
			<tr cmptype="bogus" name="trBedQuant">
				<td style="padding-left:3pt;border:1px solid #DDE2DA;" colspan="3">
					<div>
						<component cmptype="Label" name="Place_all"/>
						<component cmptype="Label" name="Place_oper"/>
						<component cmptype="Label" name="Place_cons"/>
						<component cmptype="Label" name="Place_male"/>
						<component cmptype="Label" name="Place_female"/>
					</div>
					<div>
						<component cmptype="Label" name="max_reg_date"/>
						<component cmptype="Label" name="min_reg_age"/>
						<component cmptype="Label" name="max_reg_age"/>
						<component cmptype="Label" name="has_mkb_ogr"/>
					</div>
					<div>
						<component cmptype="Label" name="pay_reg_kind"/>
						<component cmptype="Label" name="has_reg_lim"/>
					</div>
					<component cmptype="Label" name="quant_beds" style="float:left;"/>
				</td>
				<td style="padding-left:3pt;border:1px solid #DDE2DA;text-align:center;" colspan="2">
					<div style="width: 100%">
						<component cmptype="Button" caption="Загруженность к/ф" onclick="base().showLoadKF();" name="BedLoadKF" title="Этот текст будет показан при наведении"/>
					</div>
					<div style="width: 100%;padding-top: 10px">
						<component cmptype="Button" style="width: 134px" caption="Планирование к/ф" onclick="base().showPlaning();" name="BedFondPlan"/>
					</div>

				</td>
	    	</tr>
			<tr>
				<td colspan="5" cmptype="tmp" name="TD_EXPANDER">
					<component cmptype="Expander" control="trBedQuant" name="trBedQuantExpand" caption="Параметры направления"/>
				</td>
			</tr>
			<tr>
				<td style="padding-top:3pt;height:80%" colspan="5" cmptype="tmp" name="TD_GRID">
					<div cmptype="tmp" repeate="0" dataset="DS_SI_ICONS" style="display:none;" afterrefresh="Form.afterRefreshDsSepIcons('GR_HPK_PLAN_DAY', 'SI_ICON', 'SI_ICON_LAB', 'DS_SI_ICONS', 'UNIQ_N');"/>
					<component cmptype="Grid"
                               grid_caption="Журнал госпитализации"
                               name="GR_HPK_PLAN_DAY"
                               dataset="DS_HPK_PLAN_DAY"
                               excel="true"
                               field="UNIQ_N"
                               style="width: 100%;"
                               height="100%"
                               onclone="base().onPaint(_dataArray);"
                               selectlist="ID"
                               onchange="base().prepareVariables();"
                               onpostclone="base().onPostCloneMarker(_clone,_dataArray);"
                               afterrefresh="Form.afterRefreshDsSep('DS_HPK_PLAN_DAY', 'DS_SI_ICONS', 'ID');">
						<component cmptype="Column" caption="№ ИБ" field="DEPBED" sort="DEPBED" filter="DEPBED" excelfield="DEPBED"/>
						<component cmptype="Column" caption="№" field="ROW_NUM" sort="ROW_NUM" filter="ROW_NUM" excelfield="ROW_NUM"/>
						<component cmptype="Column" caption="№ записи" field="RECORD_PREF_NUMB" sort="RECORD_PREF_NUMB" filter="RECORD_PREF_NUMB" excelfield="RECORD_PREF_NUMB"/>
                        <component cmptype="Column" caption="Сигнальная информация" name="COL_SIGNAL_INFO" class="signal-info" excelfield="SI_ICON">
                            <component cmptype="Label" name="SI_ICON_LAB" />
                        </component>
						<component cmptype="Column" caption="Пациент"               field="PATIENT_ACTUAL" name ="PAT_STATUS" sort="PATIENT_ACTUAL" filter="PATIENT_ACTUAL" excelfield="PATIENT_ACTUAL">
							<div class="column_btn">
								<img name="pat_img" ondragstart="return false;" cmptype="img" src="Icons/result" title="История заболеваний и результаты исследований" onclick="base().SHOWHIST();"/>
							</div>
							<component cmptype="HyperLink" captionfield="PATIENT_ACTUAL" width="100px" datafield="PATIENT_ID" onclick="base().ADD_PERSMEDCARD(this);"/>
						</component>
                        <component cmptype="Column" caption="Маркер" field="MARKER" class="marker" width="30px" excelfield="MARKER">
                            <component cmptype="SubForm" path="Markers/subforms/subforms_markers"/>
                        </component>
						<component cmptype="Column" caption="Сопровождающее лицо"   field="RELATIVE_PATIENT" name ="RELATIVE_PATIENT" sort="RELATIVE_PATIENT" filter="RELATIVE_PATIENT" profile_hidden="true" excelfield="RELATIVE_PATIENT">
							<component cmptype="HyperLink" captionfield="RELATIVE_PATIENT" width="100px" datafield="RELATIVE_PATIENT_ID" onclick="base().ADD_PERSMEDCARD(this);"/>
						</component>
						<component cmptype="Column" caption="Дата рожд." field="PATIENT_BIRTHDATE" sort="PATIENT_BIRTHDATE" filter="PATIENT_BIRTHDATE" filterkind="date" condition="eq" excelfield="PATIENT_BIRTHDATE"/>
						<component cmptype="Column" caption="Адрес" field="PATIENT_ADDRESS" sort="PATIENT_ADDRESS" filter="PATIENT_ADDRESS" excelfield="PATIENT_ADDRESS"/>
						<component cmptype="Column" caption="СНИЛС" field="PATIENT_SNILS" sort="PATIENT_SNILS" filter="PATIENT_SNILS" excelfield="PATIENT_SNILS" />
						<component cmptype="Column" caption="Статус" field="RECORD_STATUS_MNEMO" sort="RECORD_STATUS_MNEMO" filter="RECORD_STATUS_MNEMO" filterkind="combo" fcontent="Отработана|Отработана;Не отработана|Не отработана" fdefault="Все" condition="eq" excelfield="RECORD_STATUS_MNEMO"/>
						<component cmptype="Column" caption="Диагноз при поступлении" field="DIAGNOSIS_FROM" sort="DIAGNOSIS_FROM_ORDER" filter="DIAGNOSIS_FROM" excelfield="DIAGNOSIS_FROM"/>
						<component cmptype="Column" caption="Комментарий направления" field="DIR_COMMENTS" sort="DIR_COMMENTS" profile_hidden="true" excelfield="DIR_COMMENTS"/>
						<component cmptype="Column" caption="Вид оплаты" field="PAYMENT_KIND_NAME" sort="PAYMENT_KIND_NAME" filter="PAYMENT_KIND_NAME" excelfield="PAYMENT_KIND_NAME"/>
						<component cmptype="Column" caption="Журнал" field="DEP" sort="DEP" filter="DEP" excelfield="DEP"/>
						<component cmptype="Column" caption="Записал"               field="REGISTERED_BY" sort="REGISTERED_BY" filter="REGISTERED_BY" excelfield="REGISTERED_BY"/>
						<component cmptype="Column" caption="Диагноз госпитализации"  field="HOSP_MKB" sort="HOSP_MKB" profile_hidden="true" excelfield="HOSP_MKB"/>
						<component cmptype="Column" caption="Комментарий" 	field="COMMENTS" sort="COMMENTS" excelfield="COMMENTS"/>
						<component cmptype="Column" caption="Полис"	field="PATIENT_POLIS" sort="PATIENT_POLIS" filter="PATIENT_POLIS" excelfield="PATIENT_POLIS" excelstyle='mso-number-format:"@";'/>
						<component cmptype="Column" caption="Контакты" field="PATIENT_CONTACTS" sort="PATIENT_CONTACTS" name="COLUMN_PATIENT_CONTACTS" excelfield="PATIENT_CONTACTS"/>
						<component cmptype="Column" caption="№ карты" field="CARD_NUMB" sort="CARD_NUMB" filter="CARD_NUMB" excelfield="CARD_NUMB"/>
						<component cmptype="Column" caption="Направление" field="DIR_PREF_NUMB" sort="DIR_PREF_NUMB" filter="DIR_PREF_NUMB" excelfield="DIR_PREF_NUMB"/>
						<component cmptype="Column" caption="Готов" field="IS_READY_MNEMO" sort="IS_READY_MNEMO" excelfield="IS_READY_MNEMO"/>
						<component cmptype="Column" caption="Операция" field="OPERATION" sort="OPERATION" filter="OPERATION" excelfield="OPERATION"/>
						<component cmptype="Column" caption="Направлен к" field="DIRECTED_TO" sort="DIRECTED_TO" filter="DIRECTED_TO" excelfield="DIRECTED_TO"/>
						<component cmptype="Column" caption="Кабинет" field="CABLAB_NAME" sort="CABLAB_NAME" filter="CABLAB_NAME" excelfield="CABLAB_NAME"/>
						<component cmptype="Column" caption="Дата взятия пробы на алкоголь" field="ALCOHOL_DATE" sort="ALCOHOL_DATE" filter="ALCOHOL_DATE" filterkind="date" excelfield="ALCOHOL_DATE"/>
						<component cmptype="Column" caption="Результат пробы на употребление  алкоголя" field="ALCOHOL_RES" sort="ALCOHOL_RES" filter="ALCOHOL_RES" filterkind="combo" fcontent="Отрицательно|Отрицательно;Положительно|Положительно" excelfield="ALCOHOL_RES"/>
						<component cmptype="Column" caption="Дата взятия пробы на психоактивные вещества" field="DRUG_DATE" sort="DRUG_DATE" filter="DRUG_DATE" filterkind="date" excelfield="DRUG_DATE"/>
						<component cmptype="Column" caption="Результат пробы на употребление иных психоактивных  веществ" field="DRUG_RES" sort="DRUG_RES" filter="DRUG_RES" filterkind="combo" fcontent="Отрицательно|Отрицательно;Положительно|Положительно" excelfield="DRUG_RES"/>
                        <component cmptype="Column" caption="Госпитализировал" field="DIRECTED_BY" sort="DIRECTED_BY" filter="DIRECTED_BY" excelfield="DIRECTED_BY"/>
						<component cmptype="Column" caption="Дата и время госпитализации" field="DATE_IN" sort="DATE_IN" filter="DATE_IN_TRUNC" filterkind="date" condition="eq" excelfield="DATE_IN"/>
						<component cmptype="Column" caption="Время записи на госпитализацию" field="DATE_REC" sort="DATE_REC" filter="DATE_REC" name="DATE_REC" filterkind="periodtime" condition="eq" excelfield="DATE_REC"/>
						<component cmptype="Column" caption="Госпитализирован в отделение" field="HOSP_IN_DEP" sort="HOSP_IN_DEP" filter="HOSP_IN_DEP" excelfield="HOSP_IN_DEP"/>
						<component cmptype="Column" caption="Дата и время записи" field="REGISTER_DATE" sort="REGISTER_DATE" filter="REGISTER_DATE_TRUNC" filterkind="date" condition="eq" excelfield="REGISTER_DATE"/>
						<component cmptype="Column" caption="Вид направления" field="DIRECTION_KIND_NAME" sort="DIRECTION_KIND_NAME" filter="DIRECTION_KIND_ID" filterkind="cmb_unit" funit="DIRECTION_KINDS" fmethod="LIST" condition="eq" excelfield="DIRECTION_KIND_NAME"/>
						<component cmptype="Column" caption="Внешнее направление" field="OD_NUMB" sort="OD_NUMB" filter="OD_NUMB" profile_hidden="true" excelfield="OD_NUMB"/>
						<component cmptype="Column" caption="Причина отказа" field="CANC_REASON_NAME" sort="CANC_REASON_NAME" filter="CANC_REASON_NAME" profile_hidden="true" excelfield="CANC_REASON_NAME"/>
						<component cmptype="GridFooter" separate="true">
								<component insteadrefresh="InsteadRefresh(this);" count="10" cmptype="Range" varstart="ds1start" varcount="ds1count" valuecount="10" valuestart="1"/>
						</component>
						<component cmptype="Column" caption="Тип госпитализации" field="HOSPITALIZATION_TYPE_NAME" sort="HOSPITALIZATION_TYPE_NAME" filter="HOSPITALIZATION_TYPE_NAME" excelfield="HOSPITALIZATION_TYPE_NAME"/>
                        <component cmptype="Column" caption="Схема лекарственного лечения" field="MED_THERAPY_SCHEME_CODE" sort="MED_THERAPY_SCHEME_CODE" excelfield="MED_THERAPY_SCHEME_CODE"/>
                        <component cmptype="Column" caption="Причина аннулирования" field="HH_CANC_REASON" sort="HH_CANC_REASON" excelfield="HH_CANC_REASON"/>
                    </component>
				</td>
			</tr>
    	</table>
	</component>
    <component cmptype="Hint" name="MarkersHint"/>
</div>

```

