# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Sun May 17 20:41:42 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 9
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** D3 DataSet
**Имя компонента:** dsDispPlanServices
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpDataSet name="dsDispPlanServices" activateoncreate="false" compile="true">
        <![CDATA[
        select *
          from (select pc.ID,
                       coalesce(pc.SERVICE_ID, pc.SPECIALITY_ID) SERV_SPEC,
                       pc.SERVICE_ID,
                       pc.SERVICE,
                       pc.SERVICE_NAME,
                       pc.DS_SERVICE,
                       pc.DS_SERVICE_NAME,
                       pc.SPECIALITY_ID,
                       pc.SPECIALITY,
                       pc.PLAN_DATE,
                       pc.REC_DATE,
                       pc.SSERV_REG,
                       pc.VISIT_DATE,
                       pc.SSERV_VISIT,
                       pc.CONTROL_CARD,
                       pc.REG_ID,
                       pc.SDEL_DIRECTION,
                       pc.SSERV_NULL,
                       pc.VISIT_ID,
                       pc.DIRECTION_SERVICE,
                       pc.DISP_PLACE,
                       coalesce((case when pc.DS_SERVICE is not null
                                        then pc.DS_SERVICE || ' - ' || pc.DS_SERVICE_NAME
                                 end),
                                (case when pc.SERVICE is not null
                                        then pc.SERVICE || ' - ' || pc.SERVICE_NAME
                                 end),
                                pc.SPECIALITY) SERV_SPEC_NAME,
                       case when pc.SERVICE_ID is null
                              then 0
                            else 1
                       end TYPE,
                       to_char(pc.PLAN_DATE, 'MM.YYYY') DATE_GROUP,
                       trunc(pc.PLAN_DATE, 'MM') DATE_GROUP_SORT,
                       D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS(pnDS_ID => pc.DIRECTION_SERVICE) IS_LIS,
                       10000 * extract(year from pc.PLAN_DATE) + extract(month from pc.PLAN_DATE) TREE_HID,
                       (select sr.SE_TYPE
                          from D_V_SERVICES_BASE sr
                         where sr.ID = pc.SERVICE_ID) SE_TYPE,
                       pc.NSERV_STATUS
                  from D_V_PMC_DISP_PLAN_CONTROL pc
                 where pc.CONTROL_CARD = :CONTROL_CARD
               ) t
        @if (:SE_TYPE != '') {
         where t.SE_TYPE = :SE_TYPE
        @}
        ]]>
        <cmpDataSetVar name="CONTROL_CARD" src="ID" srctype="var" />
        <cmpDataSetVar name="SE_TYPE" src="SE_TYPE" srctype="ctrl" />
    </cmpDataSet>
```

**Используемые таблицы/вьюхи:** D_V_SERVICES_BASE, D_V_PMC_DISP_PLAN_CONTROL
**Используемые пакеты/функции:** D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS

---

### Запрос №2

**Тип компонента:** D3 DataSet
**Имя компонента:** dsPmcDispPlanControl
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpDataSet name="dsPmcDispPlanControl" compile="true" activateoncreate="false">
        <![CDATA[
          with dpc as (select t.ID,
                              D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS(pnDS_ID => t.DIRECTION_SERVICE) IS_LIS,
                              coalesce(t.SERVICE_ID, t.SPECIALITY_ID) SERV_SPEC,
                              t.STATE,
                              t.SERVICE_ID,
                              t.SERVICE,
                              t.SERVICE_NAME,
                              t.DS_SERVICE,
                              t.DS_SERVICE_NAME,
                              t.SPECIALITY_ID,
                              t.SPECIALITY,
                              t.PLAN_DATE,
                              t.REC_DATE,
                              case when t.REC_DATE is not null
                                     then t.SSERV_REG
                              end SSERV_REG,
                              t.VISIT_DATE,
                              t.SSERV_VISIT,
                              t.CONTROL_CARD,
                              t.REG_ID,
                              t.SDEL_DIRECTION,
                              t.SSERV_NULL,
                              t.VISIT_ID,
                              t.DIRECTION_SERVICE,
                              t.DISP_PLACE,
                              coalesce((case when t.DS_SERVICE is not null
                                               then t.DS_SERVICE || ' - ' || t.DS_SERVICE_NAME
                                        end),
                                       (case when t.SERVICE is not null
                                               then t.SERVICE || ' - ' || t.SERVICE_NAME
                                        end),
                                       t.SPECIALITY) SERV_SPEC_NAME,
                              case when t.SERVICE_ID is null
                                     then 0
                                   else 1
                              end TYPE,
                              to_char(t.PLAN_DATE, 'MM.YYYY') DATE_GROUP,
                              trunc(t.PLAN_DATE, 'MM') DATE_GROUP_SORT,
                              10000 * extract(year from t.PLAN_DATE) + extract(month from t.PLAN_DATE) TREE_HID,
                              sr.SE_TYPE,
                              t.DIRECTION,
                              t.NSERV_STATUS
                         from D_V_PMC_DISP_PLAN_CONTROL t
                              join D_V_SERVICES_BASE sr on sr.ID = t.SERVICE_ID
                        where t.CONTROL_CARD = to_number(:CONTROL_CARD)
                      @if (:PARENT_VAR) {
                          and 10000 * extract(year from t.PLAN_DATE) + extract(month from t.PLAN_DATE) = to_number(:PARENT_VAR)
                      @}
                      @if (:SE_TYPE != "") {
                          and sr.SE_TYPE = :SE_TYPE
                      @}
                      )
        @if (:PARENT_VAR) {
          select dpc.TREE_HID,
                 0 HAS_CHILDREN,
                 dpc.ID,
                 dpc.STATE,
                 dpc.SERV_SPEC,
                 dpc.SERVICE_ID,
                 dpc.SERVICE,
                 dpc.SERVICE_NAME,
                 dpc.DS_SERVICE,
                 dpc.DS_SERVICE_NAME,
                 dpc.SPECIALITY_ID,
                 dpc.SPECIALITY,
                 dpc.PLAN_DATE,
                 dpc.REC_DATE,
                 dpc.SSERV_REG,
                 dpc.VISIT_DATE,
                 dpc.SSERV_VISIT,
                 dpc.CONTROL_CARD,
                 dpc.REG_ID,
                 dpc.SDEL_DIRECTION,
                 dpc.SSERV_NULL,
                 dpc.VISIT_ID,
                 dpc.DIRECTION_SERVICE,
                 dpc.DISP_PLACE,
                 dpc.SERV_SPEC_NAME,
                 dpc.TYPE,
                 dpc.DATE_GROUP,
                 dpc.DATE_GROUP_SORT,
                 dpc.IS_LIS,
                 dpc.SE_TYPE,
                 dpc.DIRECTION,
                 dpc.NSERV_STATUS
            from dpc
        @} else {
          select null TREE_HID,
                 1 HAS_CHILDREN,
                 dpc.TREE_HID ID,
                 null STATE,
                 null SERV_SPEC,
                 null SERVICE_ID,
                 null SERVICE,
                 null SERVICE_NAME,
                 null DS_SERVICE,
                 null DS_SERVICE_NAME,
                 null SPECIALITY_ID,
                 null SPECIALITY,
                 null PLAN_DATE,
                 null REC_DATE,
                 null SSERV_REG,
                 null VISIT_DATE,
                 null SSERV_VISIT,
                 null CONTROL_CARD,
                 null REG_ID,
                 null SDEL_DIRECTION,
                 null SSERV_NULL,
                 null VISIT_ID,
                 null DIRECTION_SERVICE,
                 null DISP_PLACE,
                 to_char(dpc.DATE_GROUP) SERV_SPEC_NAME,
                 null TYPE,
                 dpc.DATE_GROUP,
                 dpc.DATE_GROUP_SORT,
                 null IS_LIS,
                 null SE_TYPE,
                 null DIRECTION,
                 null NSERV_STATUS
            from dpc
           group by dpc.DATE_GROUP,
                    dpc.TREE_HID,
                    dpc.DATE_GROUP_SORT
        @}
        ]]>
        <cmpDataSetVar name="CONTROL_CARD" src="ID" srctype="var" />
        <cmpDataSetVar name="PARENT_VAR" src="PARENT_VAR" srctype="var" />
        <cmpDataSetVar name="SE_TYPE" src="SE_TYPE" srctype="ctrl" />
    </cmpDataSet>
```

**Используемые таблицы/вьюхи:** D_V_PMC_DISP_PLAN_CONTROL, D_V_SERVICES_BASE
**Используемые пакеты/функции:** D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS

---

### Запрос №3

**Тип компонента:** D3 DataSet
**Имя компонента:** dsChildrenRows
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpDataSet name="dsChildrenRows" activateoncreate="false">
        <![CDATA[
        select pc.ID,
               coalesce(pc.SERVICE_ID, pc.SPECIALITY_ID) SERV_SPEC,
               pc.STATE,
               pc.SERVICE_ID,
               pc.SERVICE,
               pc.SERVICE_NAME,
               pc.DS_SERVICE,
               pc.DS_SERVICE_NAME,
               pc.SPECIALITY_ID,
               pc.SPECIALITY,
               pc.PLAN_DATE,
               pc.REC_DATE,
               case when pc.REC_DATE is not null
                      then pc.SSERV_REG
               end SSERV_REG,
               pc.VISIT_DATE,
               pc.SSERV_VISIT,
               pc.CONTROL_CARD,
               pc.REG_ID,
               pc.SDEL_DIRECTION,
               pc.SSERV_NULL,
               pc.VISIT_ID,
               pc.DIRECTION_SERVICE,
               pc.DISP_PLACE,
               coalesce((case when pc.DS_SERVICE is not null
                                then pc.DS_SERVICE || ' - ' || pc.DS_SERVICE_NAME
                         end),
                        (case when pc.SERVICE is not null
                                then pc.SERVICE || ' - ' || pc.SERVICE_NAME
                         end),
                        pc.SPECIALITY) SERV_SPEC_NAME,
               case when pc.SERVICE_ID is null
                      then 0
                    else 1
               end TYPE,
               to_char(coalesce(pc.REC_DATE, pc.PLAN_DATE), 'MM.YYYY') DATE_GROUP,
               trunc(coalesce(pc.REC_DATE, pc.PLAN_DATE), 'MM') DATE_GROUP_SORT,
               D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS(pnDS_ID => pc.DIRECTION_SERVICE) IS_LIS,
               (select sr.SE_TYPE
                  from D_V_SERVICES_BASE sr
                 where sr.ID = pc.SERVICE_ID) SE_TYPE,
               pc.DIRECTION,
               pc.NSERV_STATUS
          from D_V_PMC_DISP_PLAN_CONTROL pc
         where pc.CONTROL_CARD = :CONTROL_CARD
           and ((pc.REC_DATE is not null and pc.REC_DATE between :CHECKED_DATE and D_PKG_DATE_TOOLS.END_OF_MONTH(:CHECKED_DATE))
                or ((pc.PLAN_DATE is not null and pc.PLAN_DATE between :CHECKED_DATE and D_PKG_DATE_TOOLS.END_OF_MONTH(:CHECKED_DATE))))
        ]]>
        <cmpDataSetVar name="CONTROL_CARD" src="ID" srctype="var" />
        <cmpDataSetVar name="CHECKED_DATE" src="CHECKED_DATE" srctype="var" />
    </cmpDataSet>
```

**Используемые таблицы/вьюхи:** D_V_SERVICES_BASE, D_V_PMC_DISP_PLAN_CONTROL
**Используемые пакеты/функции:** D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS, D_PKG_DATE_TOOLS.END_OF_MONTH

---

### Запрос №4

**Тип компонента:** D3 Action
**Имя компонента:** UPDATE_ACTION_PMC_DISP_PLAN
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpAction name="UPDATE_ACTION_PMC_DISP_PLAN">
        <![CDATA[
        begin
          D_PKG_PMC_DISP_PLAN.SET_DISP_PLACE(pclID         => :pclID,
                                             pnLPU         => to_number(:pnLPU),
                                             pclDISP_PLACE => :pclDISP_PLACE);
        end;
        ]]>
        <cmpActionVar name="pnLPU" src="LPU" srctype="session" />
        <cmpActionVar name="pclID" src="PMC_DISP_PLAN_ID" srctype="var" type="collection" tdo="D_CL_ID" />
        <cmpActionVar name="pclDISP_PLACE" src="DISP_PLACE" srctype="var" type="collection" tdo="D_CL_ID" />
    </cmpAction>
```

**Используемые пакеты/функции:** D_PKG_PMC_DISP_PLAN.SET_DISP_PLACE

---

### Запрос №5

**Тип компонента:** D3 Action
**Имя компонента:** cancelAttach
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpAction name="cancelAttach">
        <![CDATA[
        declare
          nSPEC_DU              NUMBER(10);
          nAT                   NUMBER(17);
        begin
          D_PKG_PMC_DISP_PLAN.SET_REG(pnID       => to_number(:pnID),
                                      pnLPU      => to_number(:pnLPU),
                                      psREG_CODE => null,
                                      pnREG_ID   => null,
                                      pnSTATE    => null);

          :CertShowPrintParam := D_PKG_OPTION_SPECS.GET('CertShowPrintParam', :LPU, 0);
          :CERT_CHECK_RESULT := coalesce(D_PKG_OPTIONS.GET('CertCheckResult', :LPU, 0), 0);
          :CertProhibitPrintSpoiled := coalesce(D_PKG_OPTIONS.GET('CertProhibitPrintSpoiled', :LPU, 0), '0');
          :CertPrintDublicateAuto := coalesce(D_PKG_OPTIONS.GET('CertPrintDublicateAuto', :LPU, 0), '0');


          nSPEC_DU := to_number(D_PKG_OPTIONS.GET(psSO_CODE => 'FindDuchTAP',
                                                  pnLPU     => to_number(:pnLPU),
                                                  pnRAISE   => 0));
          if nSPEC_DU = 1 then
            D_PKG_AMB_TALONS.WORK_IN_AT_ATTACH_DU(pnLPU               => to_number(:pnLPU),
                                                  pnDIRECTION_SERVICE => to_number(:pnREG_ID),
                                                  pnCONTROL_CARD      => to_number(:pnCONTROL_CARD),
                                                  pnPMC_DISP_PLAN     => to_number(:pnID),
                                                  pnSTATE             => null,
                                                  psACTION            => 'DEL',
                                                  pnAT                => nAT);
          end if;
        end;
        ]]>
        <cmpActionVar name="pnLPU" src="LPU" srctype="session" />
        <cmpActionVar name="pnID" src="PLAN_GR" srctype="ctrl" />
        <cmpActionVar name="pnCONTROL_CARD" src="ID" srctype="var" />
        <cmpActionVar name="pnREG_ID" src="REG_ID" srctype="var" />
    </cmpAction>
```

**Используемые пакеты/функции:** D_PKG_PMC_DISP_PLAN.SET_REG, D_PKG_AMB_TALONS.WORK_IN_AT_ATTACH_DU

---

### Запрос №6

**Тип компонента:** D3 Action
**Имя компонента:** REBUILD_PLAN
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpAction name="REBUILD_PLAN">
        <![CDATA[
          begin
            D_PKG_CONTROL_CARD.RECOUNT_PLAN_DATES(:CONTROL_CARD, :LPU);
          end;
        ]]>
        <cmpActionVar name="LPU" src="LPU" srctype="session" />
        <cmpActionVar name="CONTROL_CARD" src="ID" srctype="var" />
    </cmpAction>
```

**Используемые пакеты/функции:** D_PKG_CONTROL_CARD.RECOUNT_PLAN_DATES

---

### Запрос №7

**Тип компонента:** D3 Action
**Имя компонента:** CLEAR_REG_FROM_DISP_PLAN
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpAction name="CLEAR_REG_FROM_DISP_PLAN">
        <![CDATA[
		  begin
		    D_PKG_CONTROL_CARD.CLEAR_REG_FROM_DISP_PLAN(:CONTROL_CARD_ID, :DIRECTION_SERVICE_ID);
		  end;
		]]>
        <cmpActionVar name="CONTROL_CARD_ID" src="ID" srctype="var" />
        <cmpActionVar name="DIRECTION_SERVICE_ID" src="DIRECTION_SERVICE" srctype="var" />
    </cmpAction>
```

**Используемые пакеты/функции:** D_PKG_CONTROL_CARD.CLEAR_REG_FROM_DISP_PLAN

---

### Запрос №8

**Тип компонента:** D3 Action
**Имя компонента:** DELETE_SERV_FROM_PLAN
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<cmpAction name="DELETE_SERV_FROM_PLAN">
        <![CDATA[
        declare
          cDEL_ID               D_CL_ID   := :DEL_ID;
          nEXISTS               NUMBER(1) := 0;
        begin
          for i in 1..cDEL_ID.count
          loop
            if cDEL_ID is not empty then
              select count(1)
                into nEXISTS
                from D_V_PMC_DISP_PLAN_BASE pdp
               where pdp.ID = cDEL_ID(i);
            end if;
            if nEXISTS > 0 then
              D_PKG_PMC_DISP_PLAN.DEL(pnID  => cDEL_ID(i),
                                      pnLPU => :LPU);
            end if;
          end loop;
        end;
        ]]>
        <cmpActionVar name="LPU" src="LPU" srctype="session" />
        <cmpActionVar name="DEL_ID" src="DEL_ID" srctype="var" type="collection" tdo="D_CL_ID" />
    </cmpAction>
```

**Используемые таблицы/вьюхи:** D_V_PMC_DISP_PLAN_BASE
**Используемые пакеты/функции:** D_PKG_PMC_DISP_PLAN.DEL

---

### Запрос №9

**Тип компонента:** M2 Action
**Имя компонента:** sendFr
**Источник:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**SQL код:**

```xml
<component cmptype="Action" name="sendFr">
        <![CDATA[
        declare
          sSQL                  VARCHAR2(4000);
          nAGENT                NUMBER(17);
        begin
          begin
              select u.NNMBLOCK
                into sSQL
                from D_V_USERPROCS u
               where u.PR_CODE = 'HIVSendPatientManual';
          exception when no_data_found then null;
          end;

          begin
            select np.AGENT
              into nAGENT
              from D_V_HIV_NR_PATIENTS_BASE np
             where np.ID = to_number(:pnNR_PATIENT_ID);
          exception when no_data_found then null;
          end;

          if sSQL is null then
            D_P_EXC('Не найдена пользовательская процедура HIVSendPatientManual.');
          else
            execute immediate sSQL using nAGENT;
          end if;
        end;
        ]]>
        <component cmptype="ActionVar" name="pnNR_PATIENT_ID" src="hivRegistryGrid" srctype="ctrl" />
    </component>
```

**Используемые таблицы/вьюхи:** D_V_USERPROCS, D_V_HIV_NR_PATIENTS_BASE


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 5

---

### Вьюха №1: D_V_SERVICES_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SERVICES_BASE
 SELECT id,
    se_code,
    se_name,
    se_code_pgg,
    se_type,
    tmp_disp_service,
    pggservice AS pggservice_id,
    se_kind AS se_kind_id,
    operkind AS operkind_id,
    version,
    cid,
    is_combined,
    taxgr AS taxgr_id,
    se_profile AS se_profile_id,
    vmp AS vmp_id,
    uet_doctor,
    uet_doctor_det,
    uet_nurse,
    treatment_stage,
    pat_restriction AS pat_restriction_id,
    gen_ehr,
        CASE
            WHEN gen_ehr = 1::numeric OR check_null(gen_ehr::character varying, 1::character varying) THEN 'Да'::character varying
            ELSE 'Нет'::character varying
        END AS gen_ehr_mnemo,
    form30_service AS form30_service_id,
    fed_service AS fed_service_id,
    uet_dent_technician,
    "primary",
    open_date,
    close_date,
    se_comment,
    previd,
    uet_polisher,
    uet_caster,
    preg_serv,
    se_tax_code,
    epgu_service
   FROM d_services t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'SERVICES'::text
         LIMIT 1));
```

---

### Вьюха №2: D_V_PMC_DISP_PLAN_CONTROL

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_PMC_DISP_PLAN_CONTROL
 SELECT dp.id,
    dp.pid AS patient,
    dp.service AS service_id,
    ( SELECT sr.se_code
           FROM d_services sr
          WHERE sr.id = dp.service) AS service,
    ( SELECT sr.se_name
           FROM d_services sr
          WHERE sr.id = dp.service) AS service_name,
    ( SELECT dssr.se_code
           FROM d_services dssr
          WHERE dssr.id = ds.service) AS ds_service,
    ( SELECT dssr.se_name
           FROM d_services dssr
          WHERE dssr.id = ds.service) AS ds_service_name,
    dp.speciality AS speciality_id,
    ( SELECT sp.title
           FROM d_specialities sp
          WHERE sp.id = dp.speciality) AS speciality,
    dp.state,
    dp.plan_date,
    vis.id AS visit_id,
    ds.id AS direction_service,
    ds.rec_date,
    ds.serv_status AS nserv_status,
    ds.pid AS direction,
    ds.employer_to AS direction_service_emp,
        CASE
            WHEN ds.serv_status IS NULL THEN concat('Записать ', to_char(dp.plan_date, 'DD.MM.YYYY'::text))::character varying
            ELSE NULL::character varying
        END AS sserv_null,
        CASE
            WHEN dp.state IS NULL AND ds.serv_status IS NOT NULL THEN concat('Записан на ', to_char(ds.rec_date, 'DD.MM.YYYY'::text),
            CASE
                WHEN ds.employer_to IS NOT NULL THEN ( SELECT concat(', к врачу: (', emp.kod_vracha, ') ', d_pkg_str_tools.fio(a.surname::character varying, a.firstname::character varying, a.lastname::character varying)) AS concat
                   FROM d_employers emp
                     JOIN d_agents a ON a.id = emp.agent
                  WHERE emp.id = ds.employer_to)
                ELSE NULL::text
            END,
            CASE
                WHEN ds.cablab_to IS NOT NULL THEN concat(', в ', ( SELECT cl.cl_name
                   FROM d_cablab cl
                  WHERE cl.id = ds.cablab_to))::character varying
                ELSE NULL::character varying
            END)::character varying
            ELSE NULL::character varying
        END AS sserv_reg,
        CASE
            WHEN dp.state IS NOT NULL THEN concat('Зачтена услугой оказанной ранее в ',
            CASE dp.state
                WHEN 1 THEN 'данном МО'::character varying
                WHEN 2 THEN ( SELECT ld.lpu_name
                   FROM d_lpudict ld
                     JOIN d_lpu l ON l.lpudict = ld.id
                  WHERE l.id = vis.lpu)
                ELSE NULL::character varying
            END, ' (', to_char(vis.visit_date, 'DD.MM.YYYY'::text), ')')::character varying
            ELSE
            CASE ds.serv_status
                WHEN 1 THEN concat('Оказана ', to_char(vis.visit_date, d_pkg_std.frm_dts()::text, 'NLS_DATE_LANGUAGE=RUSSIAN'::text),
                CASE
                    WHEN vis.employer IS NOT NULL THEN ( SELECT concat(', врачом: (', empv.kod_vracha, ') ', d_pkg_str_tools.fio(a.surname::character varying, a.firstname::character varying, a.lastname::character varying)) AS concat
                       FROM d_employers empv
                         JOIN d_agents a ON a.id = empv.agent
                      WHERE empv.id = vis.employer)
                    ELSE NULL::text
                END,
                CASE
                    WHEN vis.cablab IS NOT NULL THEN concat(', ', ( SELECT clv.cl_name
                       FROM d_cablab clv
                      WHERE clv.id = vis.cablab))::character varying
                    ELSE NULL::character varying
                END)
                WHEN 2 THEN concat('Отменена ', to_char(ds.rec_date, 'DD.MM.YYYY'::text),
                CASE
                    WHEN ds.employer_to IS NOT NULL THEN ( SELECT concat(', к врачу: (', emp.kod_vracha, ') ', d_pkg_str_tools.fio(a.surname::character varying, a.firstname::character varying, a.lastname::character varying)) AS concat
                       FROM d_employers emp
                         JOIN d_agents a ON a.id = emp.agent
                      WHERE emp.id = ds.employer_to)
                    ELSE NULL::text
                END,
                CASE
                    WHEN ds.cablab_to IS NOT NULL THEN concat(', в ', ( SELECT cl.cl_name
                       FROM d_cablab cl
                      WHERE cl.id = ds.cablab_to))::character varying
                    ELSE NULL::character varying
                END)
                ELSE NULL::text
            END::character varying
        END AS sserv_visit,
        CASE
            WHEN ds.serv_status = 0::numeric THEN 'Удалить'::character varying
            ELSE NULL::character varying
        END AS sdel_direction,
    vis.visit_date,
    dp.control_card,
    dp.reg_id,
    dp.off_plan,
    dp.disp_place
   FROM d_pmc_disp_plan dp
     LEFT JOIN d_direction_services ds ON ds.id = dp.reg_id
     LEFT JOIN d_visits vis ON vis.pid = ds.id;
```

---

### Вьюха №3: D_V_PMC_DISP_PLAN_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_PMC_DISP_PLAN_BASE
 SELECT pdp.control_card,
    pdp.id,
    pdp.lpu,
    pdp.pid,
    pdp.cid,
    pdp.service AS service_id,
    pdp.speciality AS speciality_id,
    pdp.plan_date,
    pdp.reg_code,
    pdp.reg_id,
    pdp.off_plan,
    pdp.disp_place,
    pdp.state,
    ds.serv_status,
    ds.rec_date
   FROM d_pmc_disp_plan pdp
     LEFT JOIN d_direction_services ds ON ds.id = pdp.reg_id
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = pdp.cid AND ur.unitcode::text = 'PMC_DISP_PLAN'::text));
```

---

### Вьюха №4: D_V_USERPROCS

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_USERPROCS
 SELECT id,
    lpu,
    pr_code,
    pr_name,
    pr_note,
    pr_type,
        CASE
            WHEN pr_type = 0::numeric OR check_null(pr_type::character varying, 0::character varying) THEN 'Хранимая процедура'::character varying
            WHEN pr_type = 1::numeric OR check_null(pr_type::character varying, 1::character varying) THEN 'Неименованный блок'::character varying
            ELSE NULL::character varying
        END AS pr_type_for_use,
    pr_exec_type,
        CASE
            WHEN pr_exec_type = 0::numeric OR check_null(pr_exec_type::character varying, 0::character varying) THEN 'Вручную'::character varying
            WHEN pr_exec_type = 1::numeric OR check_null(pr_exec_type::character varying, 1::character varying) THEN 'Автоматически'::character varying
            ELSE NULL::character varying
        END AS pr_exec_type_for_use,
    storedproc,
    nnmblock,
    schema,
    overloadnumb,
    cid
   FROM d_userprocs t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t.cid AND ur.unitcode::text = 'USERPROCS'::text));
```

---

### Вьюха №5: D_V_HIV_NR_PATIENTS_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HIV_NR_PATIENTS_BASE
 SELECT id,
    version,
    pid,
    nos_registr,
    agent,
    numb_reg,
    create_date,
    close_date,
    create_emp,
    close_emp,
    remove_reason,
    dropped_out,
    dropped_out_org,
    is_loaded,
    last_date_export,
    last_change_date,
    pat_numb,
    category
   FROM d_hiv_nr_patients t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.version = t.version AND ur.unitcode::text = 'HIV_NR_PATIENTS'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 5

---

### Вьюха №1: D_V_SERVICES_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SERVICES_BASE
select --Представление для раздела : Услуги
       t.ID,
       t.SE_CODE,
       t.SE_NAME,
       t.SE_CODE_PGG,
       t.SE_TYPE,
       t.TMP_DISP_SERVICE,
       t.PGGSERVICE    PGGSERVICE_ID,
       t.SE_KIND       SE_KIND_ID,
       t.OPERKIND      OPERKIND_ID,
       t.VERSION,
       t.CID,
       t.IS_COMBINED,
       t.TAXGR         TAXGR_ID,
       t.SE_PROFILE    SE_PROFILE_ID,
       t.VMP           VMP_ID,
       t.UET_DOCTOR,
       t.UET_DOCTOR_DET,
       t.UET_NURSE,
       t.TREATMENT_STAGE,
       t.PAT_RESTRICTION PAT_RESTRICTION_ID,
       t.GEN_EHR,
       decode(t.GEN_EHR,1,'Да','Нет') GEN_EHR_MNEMO,
       t.FORM30_SERVICE  FORM30_SERVICE_ID,
       t.FED_SERVICE     FED_SERVICE_ID,
       t.UET_DENT_TECHNICIAN,
       t.PRIMARY,
       t.OPEN_DATE,
       t.CLOSE_DATE,
       t.SE_COMMENT,
       t.PREVID,
       t.UET_POLISHER,
       t.UET_CASTER,
       t.PREG_SERV,
       t.SE_TAX_CODE,
       t.EPGU_SERVICE
  from D_SERVICES         t     --Услуги
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'SERVICES' and rownum = 1)
```

---

### Вьюха №2: D_V_PMC_DISP_PLAN_CONTROL

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_PMC_DISP_PLAN_CONTROL
select dp.ID,
       dp.PID PATIENT,
       dp.SERVICE SERVICE_ID,
       (select SR.SE_CODE from D_SERVICES sr where sr.ID = dp.SERVICE) SERVICE,
       (select SR.SE_NAME from D_SERVICES sr where sr.ID = dp.SERVICE) SERVICE_NAME,
       (select DSSR.SE_CODE from D_SERVICES dssr where dssr.ID = ds.SERVICE) DS_SERVICE,
       (select DSSR.SE_NAME from D_SERVICES dssr where dssr.ID = ds.SERVICE) DS_SERVICE_NAME,
       dp.SPECIALITY SPECIALITY_ID,
       (select SP.TITLE from D_SPECIALITIES sp where sp.ID = dp.SPECIALITY) SPECIALITY,
       dp.STATE,
       dp.PLAN_DATE,
       vis.ID VISIT_ID,
       ds.ID DIRECTION_SERVICE,
       ds.REC_DATE,
       ds.SERV_STATUS NSERV_STATUS,
       ds.PID DIRECTION,
       ds.EMPLOYER_TO DIRECTION_SERVICE_EMP,
       case when ds.SERV_STATUS is null
              then 'Записать ' || to_char(dp.PLAN_DATE, 'DD.MM.YYYY')
       end SSERV_NULL,
       case when dp.STATE is null and ds.SERV_STATUS is not null
              then 'Записан на ' || to_char(ds.REC_DATE, 'DD.MM.YYYY')
                   || case when ds.EMPLOYER_TO is not null
                             then (select ', к врачу: (' || emp.KOD_VRACHA || ') ' || D_PKG_STR_TOOLS.FIO(a.SURNAME, a.FIRSTNAME, a.LASTNAME)
                                     from D_EMPLOYERS emp   --Персонал
                                          join D_AGENTS a on a.ID = emp.AGENT
                                    where emp.ID = ds.EMPLOYER_TO)
                      end
                   || case when ds.CABLAB_TO is not null
                             then ', в ' || (select cl.CL_NAME
                                               from D_CABLAB cl
                                              where cl.ID = ds.CABLAB_TO)
                      end
       end SSERV_REG,
       case when dp.STATE is not null
              then 'Зачтена услугой оказанной ранее в '
                   || case dp.STATE
                           when 1 then 'данном МО'
                           when 2 then (select ld.LPU_NAME
                                          from D_LPUDICT ld
                                               join D_LPU l on l.LPUDICT = ld.ID
                                         where l.id = vis.LPU)
                      end
                   || ' (' || to_char(vis.VISIT_DATE, 'DD.MM.YYYY') || ')'
            else case ds.SERV_STATUS
                      when 1 then 'Оказана '
                                  || to_char(vis.VISIT_DATE, D_PKG_STD.FRM_DTS)
                                  || case when vis.EMPLOYER is not null
                                            then (select ', врачом: (' || empv.KOD_VRACHA || ') ' || D_PKG_STR_TOOLS.FIO(a.SURNAME, a.FIRSTNAME, a.LASTNAME)
                                                    from D_EMPLOYERS empv
                                                         join D_AGENTS a on a.ID = empv.AGENT
                                                   where empv.ID = vis.EMPLOYER)
                                     end
                                  || case when vis.CABLAB is not null
                                            then ', ' || (select clv.CL_NAME
                                                            from D_CABLAB clv
                                                           where clv.ID = vis.CABLAB)
                                     end
                      when 2 then 'Отменена '
                                  || to_char(ds.REC_DATE, 'DD.MM.YYYY')
                                  || case when ds.EMPLOYER_TO is not null
                                            then (select ', к врачу: (' || emp.KOD_VRACHA || ') ' || D_PKG_STR_TOOLS.FIO(a.SURNAME, a.FIRSTNAME, a.LASTNAME)
                                                    from D_EMPLOYERS emp
                                                         join D_AGENTS a on a.ID = emp.AGENT
                                                   where emp.ID = ds.EMPLOYER_TO)
                                     end
                                  || case when ds.CABLAB_TO is not null
                                            then ', в ' || (select cl.CL_NAME
                                                              from D_CABLAB cl
                                                             where cl.ID = ds.CABLAB_TO)
                                     end
                end
       end SSERV_VISIT,
       case when DS.SERV_STATUS = 0 then 'Удалить' end SDEL_DIRECTION,
       vis.VISIT_DATE,
       dp.CONTROL_CARD,
       dp.REG_ID,   --Запись в регистратуре
       dp.OFF_PLAN, --#228459
       dp.DISP_PLACE
  from D_PMC_DISP_PLAN dp
       left join D_DIRECTION_SERVICES ds on ds.ID = dp.REG_ID
       left join D_VISITS vis on vis.PID = ds.ID
```

---

### Вьюха №3: D_V_PMC_DISP_PLAN_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_PMC_DISP_PLAN_BASE
select --Представление для раздела : Карта пациента : План диспансерного наблюдения
       pdp.CONTROL_CARD,
       pdp.ID,
       pdp.LPU,
       pdp.PID,
       pdp.CID,
       pdp.SERVICE     SERVICE_ID,
       pdp.SPECIALITY  SPECIALITY_ID,
       pdp.PLAN_DATE,
       pdp.REG_CODE,
       pdp.REG_ID,
       pdp.OFF_PLAN,
       pdp.DISP_PLACE,
       pdp.STATE,
       ds.SERV_STATUS,
       ds.REC_DATE
  from D_PMC_DISP_PLAN pdp                                     -- Карта пациента : План диспансерного наблюдения
       left join D_DIRECTION_SERVICES ds on ds.ID = pdp.REG_ID -- Направления : услуги
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = pdp.CID
                  and ur.UNITCODE = 'PMC_DISP_PLAN'
               )
```

---

### Вьюха №4: D_V_USERPROCS

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_USERPROCS
select
       t.ID,
       t.LPU,
       t.PR_CODE,
       t.PR_NAME,
       t.PR_NOTE,
       t.PR_TYPE,
       decode(t.PR_TYPE,0,'Хранимая процедура',1,'Неименованный блок') PR_TYPE_FOR_USE,
       t.PR_EXEC_TYPE,
       decode(t.PR_EXEC_TYPE,0,'Вручную',1,'Автоматически') PR_EXEC_TYPE_FOR_USE,
       t.STOREDPROC,
       t.NNMBLOCK,
       t.SCHEMA,
       t.OVERLOADNUMB,
       t.CID
  from D_USERPROCS t
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = t.CID and ur.UNITCODE = 'USERPROCS')

 
```

---

### Вьюха №5: D_V_HIV_NR_PATIENTS_BASE

**Используется в формах:**
- /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HIV_NR_PATIENTS_BASE
select -- Представление для раздела : РВИЧ: Нозологический регистр ВИЧ
       t.ID,
       t.VERSION,
       t.PID,
       t.NOS_REGISTR,
       t.AGENT,
       t.NUMB_REG,
       t.CREATE_DATE,
       t.CLOSE_DATE,
       t.CREATE_EMP,
       t.CLOSE_EMP,
       t.REMOVE_REASON,
       t.DROPPED_OUT,
       t.DROPPED_OUT_ORG,
       t.IS_LOADED,
       t.LAST_DATE_EXPORT,
       t.LAST_CHANGE_DATE,
       t.PAT_NUMB,
       t.CATEGORY
  from D_HIV_NR_PATIENTS                         t  -- РВИЧ: Нозологический регистр ВИЧ
 where exists (select null from D_V_URPRIVS ur where ur.VERSION = t.VERSION and ur.UNITCODE = 'HIV_NR_PATIENTS' and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Ниже представлены брокеры (Action с атрибутами unit/action или прямым указанием функции), соответствующие им функции, а также DDL этих функций.

**Статистика:**
- Всего брокеров: 4

**Типы брокеров:**
- Тип 1 (unit + action, требуется поиск): 2
- Тип 2 (прямое указание функции): 2

---

### Брокер №1: unit=PMC_DISP_PLAN, action=SET_REG

**Тип брокера:** Требуется поиск в D_UNITBPS

**Вызываемая функция:**
```
D_PKG_PMC_DISP_PLAN.SET_REG
```

**Oracle SQL тело функции не найдено.**

**PostgreSQL тело функции не найдено.**

---

### Брокер №2: unit=DIRECTION_SERVICES, action=DELETE

**Тип брокера:** Требуется поиск в D_UNITBPS

**Вызываемая функция:**
```
D_PKG_DIRECTION_SERVICES.DEL
```

**Oracle SQL тело функции не найдено.**

**PostgreSQL тело функции не найдено.**

---

### Брокер №3: action=D_PKG_R_ZNO_RISK_DATA_REASONS.DEL

**Тип брокера:** Прямое указание функции

**Вызываемая функция:**
```
D_PKG_R_ZNO_RISK_DATA_REASONS.DEL
```

**Oracle SQL тело функции не найдено.**

**PostgreSQL тело функции не найдено.**

---

### Брокер №4: action=D_PKG_R_ZNO_RISK_DATA_REASONS.ADD

**Тип брокера:** Прямое указание функции

**Вызываемая функция:**
```
D_PKG_R_ZNO_RISK_DATA_REASONS.ADD
```

**Oracle SQL тело функции не найдено.**

**PostgreSQL тело функции не найдено.**


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 5
- Всего уникальных таблиц: 12

### Связь вьюх и таблиц

**D_V_SERVICES_BASE** использует таблицы:
- D_SERVICES

**D_V_PMC_DISP_PLAN_CONTROL** использует таблицы:
- D_SERVICES
- D_SPECIALITIES
- D_EMPLOYERS
- D_AGENTS
- D_CABLAB
- D_LPUDICT
- D_LPU
- D_PMC_DISP_PLAN
- D_DIRECTION_SERVICES
- D_VISITS

**D_V_PMC_DISP_PLAN_BASE** использует таблицы:
- D_PMC_DISP_PLAN
- D_DIRECTION_SERVICES

**D_V_USERPROCS** использует таблицы:
- D_USERPROCS

**D_V_HIV_NR_PATIENTS_BASE** использует таблицы:
- D_HIV_NR_PATIENTS

### DDL определения таблиц

---

#### Таблица №1: D_SERVICES

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

#### Таблица №2: D_SPECIALITIES

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

#### Таблица №3: D_EMPLOYERS

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

#### Таблица №4: D_AGENTS

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

#### Таблица №5: D_CABLAB

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

#### Таблица №6: D_LPUDICT

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

#### Таблица №7: D_LPU

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

#### Таблица №8: D_PMC_DISP_PLAN

```sql
CREATE TABLE D_PMC_DISP_PLAN (
    id bigint,
    pid bigint,
    service bigint,
    speciality bigint,
    plan_date timestamp without time zone,
    cid bigint,
    lpu bigint,
    reg_id bigint,
    reg_code character varying(30),
    off_plan numeric(1) DEFAULT 0,
    disp_place numeric(1) DEFAULT 0,
    control_card bigint,
    state bigint
);
```

---

#### Таблица №9: D_DIRECTION_SERVICES

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

#### Таблица №10: D_VISITS

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

#### Таблица №11: D_USERPROCS

```sql
CREATE TABLE D_USERPROCS (
    id bigint,
    lpu bigint,
    pr_code character varying(40),
    pr_name character varying(250),
    pr_note character varying(500),
    pr_type numeric(1) DEFAULT 0,
    pr_exec_type numeric(1) DEFAULT 0,
    storedproc character varying(61),
    nnmblock character varying(4000),
    schema character varying(30),
    overloadnumb bigint,
    cid bigint
);
```

---

#### Таблица №12: D_HIV_NR_PATIENTS

```sql
CREATE TABLE D_HIV_NR_PATIENTS (
    id bigint,
    version bigint,
    pid bigint,
    nos_registr bigint,
    agent bigint,
    numb_reg character varying(22),
    create_date timestamp without time zone,
    close_date timestamp without time zone,
    create_emp bigint,
    close_emp bigint,
    remove_reason bigint,
    dropped_out bigint,
    dropped_out_org bigint,
    is_loaded numeric(1) DEFAULT 0,
    last_date_export timestamp without time zone,
    last_change_date timestamp without time zone DEFAULT sysdate(),
    category bigint,
    pat_numb character varying(17)
);
```


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 5
- Всего уникальных таблиц: 12

### Связь вьюх и таблиц

**D_V_SERVICES_BASE** использует таблицы:
- D_SERVICES

**D_V_PMC_DISP_PLAN_CONTROL** использует таблицы:
- D_SERVICES
- D_SPECIALITIES
- D_EMPLOYERS
- D_AGENTS
- D_CABLAB
- D_LPUDICT
- D_LPU
- D_PMC_DISP_PLAN
- D_DIRECTION_SERVICES
- D_VISITS

**D_V_PMC_DISP_PLAN_BASE** использует таблицы:
- D_PMC_DISP_PLAN
- D_DIRECTION_SERVICES

**D_V_USERPROCS** использует таблицы:
- D_USERPROCS

**D_V_HIV_NR_PATIENTS_BASE** использует таблицы:
- D_HIV_NR_PATIENTS

### DDL определения таблиц

---

#### Таблица №1: D_SERVICES

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

#### Таблица №2: D_SPECIALITIES

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

#### Таблица №3: D_EMPLOYERS

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

#### Таблица №4: D_AGENTS

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

#### Таблица №5: D_CABLAB

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

#### Таблица №6: D_LPUDICT

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

#### Таблица №7: D_LPU

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

#### Таблица №8: D_PMC_DISP_PLAN

```sql
CREATE TABLE D_PMC_DISP_PLAN (
    ID NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    SERVICE NUMBER(17),
    SPECIALITY NUMBER(17),
    PLAN_DATE DATE NOT NULL,
    CID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    REG_ID NUMBER(17),
    REG_CODE VARCHAR2(30),
    OFF_PLAN NUMBER(1),
    DISP_PLACE NUMBER(1),
    CONTROL_CARD NUMBER(17) NOT NULL,
    STATE NUMBER(17),
    CONSTRAINT PK_D_PMC_DISP_PLAN PRIMARY KEY (ID)
);
```

---

#### Таблица №9: D_DIRECTION_SERVICES

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

#### Таблица №10: D_VISITS

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

#### Таблица №11: D_USERPROCS

```sql
CREATE TABLE D_USERPROCS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PR_CODE VARCHAR2(40) NOT NULL,
    PR_NAME VARCHAR2(250) NOT NULL,
    PR_NOTE VARCHAR2(500),
    PR_TYPE NUMBER(1) NOT NULL,
    PR_EXEC_TYPE NUMBER(1) NOT NULL,
    STOREDPROC VARCHAR2(61),
    NNMBLOCK VARCHAR2(4000),
    SCHEMA VARCHAR2(30),
    OVERLOADNUMB NUMBER(17),
    CID NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_USERPROCS PRIMARY KEY (ID)
);
```

---

#### Таблица №12: D_HIV_NR_PATIENTS

```sql
CREATE TABLE D_HIV_NR_PATIENTS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    NOS_REGISTR NUMBER(17) NOT NULL,
    AGENT NUMBER(17) NOT NULL,
    NUMB_REG VARCHAR2(22) NOT NULL,
    CREATE_DATE DATE NOT NULL,
    CLOSE_DATE DATE,
    CREATE_EMP NUMBER(17) NOT NULL,
    CLOSE_EMP NUMBER(17),
    REMOVE_REASON NUMBER(17),
    DROPPED_OUT NUMBER(17),
    DROPPED_OUT_ORG NUMBER(17),
    IS_LOADED NUMBER(1) NOT NULL,
    LAST_DATE_EXPORT DATE,
    LAST_CHANGE_DATE DATE NOT NULL,
    CATEGORY NUMBER(17),
    PAT_NUMB VARCHAR2(17),
    CONSTRAINT PK_D_HIV_NR_PATIENTS PRIMARY KEY (ID)
);
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 8
- Загружено тел функций: 7

---

### Функция №1: D_PKG_LABMED_ANALYZE.IS_LIS_ANALYZE_BY_DS

```sql
-- Oracle PACKAGE: IS_LIS_ANALYZE_BY_DS
-- Возвращает: return NUMBER
--======================================================================
function IS_LIS_ANALYZE_BY_DS
(
    pnDS_ID              in NUMBER         -- DIRECTION_SERVICE ID
) return NUMBER
is
    IS_LIS                D_OPTION_SPECS.VALUE_NUM%type;
begin
    --  на ЛПУ использующих одновременно ЛИС и КДЛ операться на СО LabDirLineEnabled нельзя (#258319)
    select coalesce(max(1), 0)
      into IS_LIS
      from D_DIRECTION_SERVICES ds
        join D_LABMED_DIRECTION_LINE dl on dl.DIR_SERV = ds.ID
     where ds.HID = pnDS_ID
       and rownum = 1;

  return IS_LIS;
end IS_LIS_ANALYZE_BY_DS;
```

---

### Функция №2: D_PKG_DATE_TOOLS.END_OF_MONTH

```sql
-- Oracle PACKAGE: END_OF_MONTH
-- Возвращает: return date
--======================================================================
function END_OF_MONTH(fdDATE in date default sysdate) return date is
begin
  return D_PKG_DATE_TOOLS.END_OF_DAY(last_day(fdDATE));
end;
```

---

### Функция №3: D_PKG_PMC_DISP_PLAN.SET_DISP_PLACE

```sql
-- Oracle PACKAGE: SET_DISP_PLACE
--======================================================================
procedure SET_DISP_PLACE
(
  pclID                                 in D_CL_ID,
  pnLPU                                 in NUMBER,
  pclDISP_PLACE                         in D_CL_ID
)
is
  nCID                   D_PKG_STD.tREF;
begin
  if pclID.count = 0 or pclDISP_PLACE.count = 0 then
    return;
  end if;
  for r in pclID.first .. pclID.last
  loop
    /* Поиск каталога */
    begin
      select t.CID
        into nCID
        from D_PMC_DISP_PLAN t
       where t.ID      = pclID(r)
         and t.LPU     = pnLPU;
    exception when no_data_found then
      D_PKG_MSG.RECORD_NOT_FOUND(pclID(r), 'PMC_DISP_PLAN');
    end;
```

---

### Функция №4: D_PKG_PMC_DISP_PLAN.SET_REG

```sql
-- Oracle PACKAGE: SET_REG
--======================================================================
procedure SET_REG
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  psREG_CODE                           in VARCHAR2,
  pnREG_ID                             in NUMBER,
  pnSTATE                              in NUMBER default null
)
is
  nCID                  D_PKG_STD.tREF;
begin
  /* Поиск каталога */
  begin
    select t.CID
      into nCID
      from D_PMC_DISP_PLAN t
     where t.ID = pnID
       and t.LPU = pnLPU;
  exception when no_data_found then
    D_PKG_MSG.RECORD_NOT_FOUND(pnID, 'PMC_DISP_PLAN');
  end;
```

---

### Функция №5: D_PKG_AMB_TALONS.WORK_IN_AT_ATTACH_DU

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).

---

### Функция №6: D_PKG_CONTROL_CARD.RECOUNT_PLAN_DATES

```sql
-- Oracle PACKAGE: RECOUNT_PLAN_DATES
--======================================================================
procedure RECOUNT_PLAN_DATES
(
  pnCONTROL_CARD                       in NUMBER,          -- ID Control_Card
  pnLPU                                in NUMBER           -- ID LPU
)
is
  dDATE_FROM            D_CONTROL_CARD.DATE_FROM%type;
  dREMOVE_DATE          D_CONTROL_CARD.REMOVE_DATE%type;
  dF_REC_DATE           DATE;
  dF_PLAN_DATE          DATE;
  dF_DAYS_COUNT         NUMBER(5);
begin
  begin
    select t.DATE_FROM,
           t.REMOVE_DATE
      into dDATE_FROM,
           dREMOVE_DATE
      from D_CONTROL_CARD t
     where t.ID = pnCONTROL_CARD
       and t.LPU = pnLPU;
  exception when no_data_found then
    D_PKG_MSG.RECORD_NOT_FOUND(pnCONTROL_CARD, 'CONTROL_CARD');
  end;
```

---

### Функция №7: D_PKG_CONTROL_CARD.CLEAR_REG_FROM_DISP_PLAN

```sql
-- Oracle PACKAGE: CLEAR_REG_FROM_DISP_PLAN
--======================================================================
procedure CLEAR_REG_FROM_DISP_PLAN
(
  pnCONTROL_CARD_ID                    in NUMBER,          --ID Контрольной карты
  pnDIRECTION_SERVICE_ID               in NUMBER          -- ID DIRECTION_SERVICE
)
is
begin
  --   D_P_EXC('pnCONTROL_CARD_ID '||pnCONTROL_CARD_ID); не прокидывается с формы, нужно проверить
  for x in (select dp.ID DISP_PLAN_ID
              from D_CONTROL_CARD cc
                   join D_PMC_DISP_PLAN dp on dp.CONTROL_CARD = cc.ID
             where cc.ID = pnCONTROL_CARD_ID)
  loop
    begin
      update D_PMC_DISP_PLAN t set
        t.REG_ID = null,
        t.REG_CODE = null
      where t.ID = x.DISP_PLAN_ID
        and t.REG_ID = pnDIRECTION_SERVICE_ID;
    exception when no_data_found then
      null;
    end;
```

---

### Функция №8: D_PKG_PMC_DISP_PLAN.DEL

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
begin
  /* Поиск каталога */
  begin
    select t.CID
      into nCID
      from D_PMC_DISP_PLAN t
     where t.ID      = pnID
       and t.LPU     = pnLPU;
  exception when no_data_found then
    D_PKG_MSG.RECORD_NOT_FOUND(pnID, 'PMC_DISP_PLAN');
  end;
```


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 8
- Загружено тел функций: 8

---

### Функция №1: d_pkg_labmed_analyze.is_lis_analyze_by_ds

```sql
CREATE OR REPLACE FUNCTION d_pkg_labmed_analyze.is_lis_analyze_by_ds(pnds_id numeric)
 RETURNS numeric
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
DECLARE
    IS_LIS d_option_specs.value_num%TYPE;
BEGIN
    --   на ЛПУ использующих одновременно ЛИС и КДЛ операться на СО LabDirLineEnabled нельзя (#258319)
    SELECT
        coalesce(max(1)::numeric,0)
    INTO STRICT is_lis
    FROM
        d_direction_services ds 
                JOIN     d_labmed_direction_line dl ON dl.dir_serv = ds.id 
    WHERE
        ds.hid = pnds_id::bigint
         LIMIT 1;
    return is_lis;
END
$function$
```

---

### Функция №2: d_pkg_date_tools.end_of_month

```sql
CREATE OR REPLACE FUNCTION d_pkg_date_tools.end_of_month(fddate timestamp without time zone DEFAULT sysdate())
 RETURNS timestamp without time zone
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    return d_pkg_date_tools.end_of_day(last_day(fddate)::timestamp);
END
$function$
```

---

### Функция №3: d_pkg_pmc_disp_plan.set_disp_place

```sql
CREATE OR REPLACE PROCEDURE d_pkg_pmc_disp_plan.set_disp_place(IN pclid numeric[], IN pnlpu numeric, IN pcldisp_place numeric[])
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    IF array_length2(pclid) = 0
     OR array_length2(pcldisp_place) = 0 THEN
        return;

    END IF;
    FOR r IN array_lower2(pclid) .. array_upper2(pclid)
    LOOP
        SELECT
            t.cid
        INTO ncid
        FROM
            d_pmc_disp_plan t
        WHERE
            t.id = pclid[r]::bigint
                 AND t.lpu = pnlpu::bigint;
        IF NOT FOUND THEN
            PERFORM d_pkg_msg.record_not_found(1,(pclid[r])::numeric,'PMC_DISP_PLAN');

        END IF;
        CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_UPDATE', 'D_PMC_DISP_PLAN', (pclid[r])::numeric);
        IF pcldisp_place[r] IS NULL THEN
            PERFORM d_p_exc(1,(concat('Необходимо указать планируемое место проведения приема для записи PMC_DISP_PLAN: ', pclid[r]))::varchar);

        END IF;
        BEGIN
            update d_pmc_disp_plan t set disp_place = pcldisp_place[r] where t.id = pclid[r]::bigint
                 AND t.lpu = pnlpu::bigint;
            EXCEPTION
                WHEN others THEN
                            PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

        END;
        IF ( NOT FOUND ) THEN
            PERFORM d_pkg_msg.record_not_found(1,(pclid[r])::numeric,'PMC_DISP_PLAN');

        END IF;
        CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_UPDATE', 'D_PMC_DISP_PLAN', (pclid[r])::numeric);
    END LOOP;
END
$procedure$
```

---

### Функция №4: d_pkg_pmc_disp_plan.set_reg

```sql
CREATE OR REPLACE PROCEDURE d_pkg_pmc_disp_plan.set_reg(IN pnid numeric, IN pnlpu numeric, IN psreg_code character varying, IN pnreg_id numeric, IN pnstate numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    SELECT
        t.cid
    INTO ncid
    FROM
        d_pmc_disp_plan t
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'PMC_DISP_PLAN');

    END IF;
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_UPDATE', 'D_PMC_DISP_PLAN', pnid);
    IF ( ( nullif(psreg_code,'') IS NULL
     AND nullif(psreg_code,'') IS NOT NULL )
     OR ( nullif(psreg_code,'') IS NOT NULL
     AND nullif(psreg_code,'') IS NULL ) ) THEN
        PERFORM d_p_exc(1,'Должны заполняться оба поля');

    END IF;
    BEGIN
        update d_pmc_disp_plan t set reg_code = psreg_code , reg_id = pnreg_id , state = pnstate where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'U',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'PMC_DISP_PLAN');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_UPDATE', 'D_PMC_DISP_PLAN', pnid);
END
$procedure$
```

---

### Функция №5: d_pkg_amb_talons.work_in_at_attach_du

```sql
CREATE OR REPLACE PROCEDURE d_pkg_amb_talons.work_in_at_attach_du(IN pnlpu numeric, IN pndirection_service numeric, IN pncontrol_card numeric, IN pnpmc_disp_plan numeric, IN pnstate numeric, IN psaction character varying, INOUT pnat numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    rAT_OTHER_VISIT d_amb_talon_other_visits;   /*  Данные по посещению */
    nPERSMEDCARD d_pmc_disp_plan.pid%TYPE;   /*  Медкарта */
    nATV_OTHER_ID d_amb_talon_other_visits.id%TYPE;   /*  Данные по связываемому визиту */
    nSERVICE d_services.id%TYPE;   /*  Связывамая улсуга */
    nVISIT d_visits.id%TYPE;   /*  Визит для связывания */
    sLPU_OTHER_NAME d_other_lpu.lpu_name%TYPE;   /*  Внешнее ЛПУ */
    nLPU_OTHER d_other_lpu.id%TYPE;   /*  ID Внешнего ЛПУ */
    nDEP_OTHER d_other_deps.id%TYPE;   /*  Внешнее отделение */
    nEMPLOYER_OTHER d_other_employers.id%TYPE;   /*  Внешний сотрудник */
    sERROR varchar(4000);
    nATV_ID d_amb_talon_visits.id%TYPE;   /*  ID посещение из талона */
    nVISIT_ID d_visits.id%TYPE;   /*  ID посещение для копирования */
    nHID_VISIT_ID d_visits.id%TYPE;   /*  ID посещение для копирования услуги состава */
    nVISIT_TEMPLATE d_visits.visit_template%TYPE;   /*  Шаблон оказания */
    dVISIT_DATE d_visits.visit_date%TYPE;   /*  Дата посещения */
    nDS_SERVICE d_direction_services.service%TYPE;   /*  Услуга их направления */
    sSERVICE varchar(1500);   /*  Услуга для вывода в ошибку */
    nDS_PAYMENT_KIND d_direction_services.payment_kind%TYPE;
    --  Тип оплаты из направления
nVISIT_TYPE d_vis_temp_services.visit_type%TYPE;
    --  Тип визита
nSERV_TYPE d_services.se_type%TYPE;   /*  Тип услуги */
    nCONTROL_CARD d_pmc_disp_plan.control_card%TYPE;   /*  ID контрольной карты */
    or2pgTmpVar5_5 varchar;
    or2pgTmpVar43_43 varchar;
    or2pgTmpVar3_3 varchar;
    or2pgTmpVar47_47 varchar;
    or2pgTmpVar36_36 varchar;
    or2pgTmpVar9_9 varchar;
    or2pgTmpVar11_11 varchar;
    or2pgTmpVar7_7 varchar;
    or2pgTmpVar15_15 varchar;
    or2pgTmpVar19_19 varchar;
    or2pgTmpVar50_50 varchar;
    or2pgTmpVar33_33 varchar;
    or2pgTmpVar22_22 varchar;
    or2pgTmpVar1_1 varchar;
    or2pgTmpVar26_26 varchar;
    or2pgTmpVar40_40 varchar;
    or2pgTmpVar44_44 varchar;
    or2pgTmpVar37_37 varchar;
    or2pgTmpVar10_10 varchar;
    or2pgTmpVar18_18 varchar;
    or2pgTmpVar14_14 varchar;
    or2pgTmpVar32_32 varchar;
    or2pgTmpVar21_21 varchar;
    or2pgTmpVar48_48 varchar;
    or2pgTmpVar25_25 varchar;
    or2pgTmpVar29_29 varchar;
    or2pgTmpVar6_6 varchar;
    or2pgTmpVar41_41 varchar;
    or2pgTmpVar4_4 varchar;
    or2pgTmpVar45_45 varchar;
    or2pgTmpVar34_34 varchar;
    or2pgTmpVar51_1 numeric;
    or2pgTmpVar13_13 varchar;
    or2pgTmpVar38_38 varchar;
    or2pgTmpVar17_17 varchar;
    or2pgTmpVar8_8 varchar;
    or2pgTmpVar31_31 varchar;
    or2pgTmpVar0_0 varchar;
    or2pgTmpVar20_20 varchar;
    or2pgTmpVar49_49 varchar;
    or2pgTmpVar2_2 varchar;
    or2pgTmpVar24_24 varchar;
    or2pgTmpVar28_28 varchar;
    or2pgTmpVar42_42 varchar;
    or2pgTmpVar46_46 varchar;
    or2pgTmpVar35_35 varchar;
    or2pgTmpVar12_12 varchar;
    or2pgTmpVar39_39 varchar;
    or2pgTmpVar16_16 varchar;
    or2pgTmpVar30_30 varchar;
    or2pgTmpVar23_23 varchar;
    or2pgTmpVar27_27 varchar;
BEGIN
    IF psaction = 'ADD' THEN
        SELECT
            mcdp.service,
            mcdp.pid,
            s.se_type,
            mcdp.control_card
        INTO nservice, npersmedcard, nserv_type, ncontrol_card
        FROM
            d_pmc_disp_plan mcdp 
                    JOIN     d_direction_services ds ON ds.id = mcdp.reg_id
     AND mcdp.reg_code = 'DIRECTION_SERVICES'  
                    JOIN     d_services s ON s.id = mcdp.service 
        WHERE
            mcdp.id = pnpmc_disp_plan::bigint
                 AND mcdp.lpu = pnlpu::bigint;
        IF NOT FOUND THEN
            PERFORM d_p_exc(1,'Не найдена информация по плану диспансерного учета.');

        END IF;
        ncontrol_card := coalesce(pncontrol_card,ncontrol_card);
        IF pnstate = 1 THEN
            SELECT
                v.id,
                v.visit_template,
                trunc(v.visit_date),
                ds.service,
                ds.payment_kind,
                atv.id
            INTO nvisit_id, nvisit_template, dvisit_date, nds_service, nds_payment_kind, natv_id
            FROM
                d_direction_services ds 
                        JOIN     d_visits v ON v.pid = ds.id
     AND v.lpu = ds.lpu  
                        LEFT JOIN     d_amb_talon_visits atv ON atv.visit = v.id
     AND atv.lpu = v.lpu 
            WHERE
                ds.lpu = pnlpu::bigint
                     AND ds.id = pndirection_service::bigint;
            IF NOT FOUND THEN
                nvisit_id := null;
                nvisit_template := null;
                dvisit_date := null;
                nds_service := null;
                natv_id := null;

            END IF;
            --  Если не нашлась информация по посещению, поищем по услуге состава
                        IF natv_id IS NULL THEN
                BEGIN
                    SELECT
                        v.id,
                        atv.id
                    INTO STRICT nhid_visit_id, natv_id
                    FROM
                        d_direction_services ds 
                                JOIN     d_visits v ON v.pid = ds.id  
                                JOIN     d_amb_talon_visits atv ON atv.visit = v.id 
                    WHERE
                        ds.lpu = pnlpu::bigint
                             AND ds.hid = pndirection_service::bigint;
                    EXCEPTION
                        WHEN no_data_found THEN
                                    nhid_visit_id := null;
                            natv_id := null;

                        WHEN too_many_rows THEN
                                    nhid_visit_id := null;
                            natv_id := null;
                            serror := 'Не удается подобрать нужную информацию, услуга имеет несколько услуг состава.';

                END;

            END IF;
            IF nhid_visit_id IS NOT NULL THEN
                nvisit_id := nhid_visit_id;

            END IF;
            IF natv_id IS NULL THEN
                BEGIN
                    SELECT
                        vs.visit_type
                    INTO STRICT nvisit_type
                    FROM
                        d_vis_temp_services vs
                    WHERE
                        vs.pid = nvisit_template
                             AND vs.service = nds_service
                             AND trunc(vs.begin_date) <= dvisit_date
                             AND ( trunc(vs.end_date) >= dvisit_date
                             OR vs.end_date IS NULL );
                    EXCEPTION
                        WHEN others THEN
                                    nvisit_type := null;

                END;
                IF nvisit_type IN ( 0 , 3 )
     OR nvisit_type IS NULL THEN
                    SELECT
                        concat(s.se_code, ' ', s.se_name)
                    INTO STRICT sservice
                    FROM
                        d_services s
                    WHERE
                        s.id = nds_service;
                    PERFORM d_p_exc(1,(concat('Невозможно связать с услугой ', sservice, ', т.к. способ формирования АТ ', (CASE
    WHEN nvisit_type = 0 THEN ('"посещение"')::varchar
    WHEN nvisit_type = 3 THEN ('"операция"')::varchar
    ELSE ('не указан')::varchar
END), '. Способ формирования АТ должен быть "манипуляция" или "без формирования АТ".'))::varchar);

                END IF;

            END IF;
            SELECT
                atv.id,
                pnlpu,
                null  cid,
                null  pid,
                coalesce(atv.visit,v.id)  visit,
                coalesce(atv.vis_date,v.visit_date)  visit_date,
                coalesce(atv.vis_employer,v.employer)  vis_employer,
                atv.vis_purpose,
                atv.vis_kind,
                atv.vis_place,
                atv.vis_is_primary,
                coalesce(atv.vis_payment_kind,nds_payment_kind)  vis_payment_kind,
                null  reestrsp,
                coalesce(atv.service,nds_service)  service,
                atv.vis_result,
                atv.vis_prof_disease,
                atv.dgroup,
                coalesce(atv.ser_count,v.ser_count)  ser_count,
                coalesce(atv.ser_koeff,v.ser_koeff)  ser_koeff,
                coalesce(atv.uet_count,v.uet_count)  uet_count,
                atv.ser_price,
                atv.ser_summ,
                coalesce(atv.inc_to_reestr,1)  inc_to_reestr,
                coalesce(atv.row_type,1)  row_type,
                atv.hosp_reestrsp,
                atv.vis_ref_result,
                (CASE
                    WHEN atv.is_first IS NOT NULL THEN 1
                END)  is_first,
                atv.vis_dep,
                atv.is_last,
                atv.lpu_to,
                atv.dir_purpose,
                atv.note,
                atv.smp_team_profile,
                null  other_lpu_code,
                null  other_lpu_name,
                null  other_dep_code,
                null  other_emp_fio,
                null  other_emp_code,
                null  other_emp_snils,
                null  other_emp_spec,
                null  other_emp_jobt,
                null  other_emp_spec_ed,
                1  mo_state,
                atv.recommendation,
                atv.physical_group,
                4  state_reason,
                atv.pc_pat,
                null  gr_inter,
                null  gr_s_usl,
                null  other_emp_profil,
                atv.tarif
            INTO or2pgTmpVar0_0, or2pgTmpVar1_1, or2pgTmpVar2_2, or2pgTmpVar3_3, or2pgTmpVar4_4, or2pgTmpVar5_5, or2pgTmpVar6_6, or2pgTmpVar7_7, or2pgTmpVar8_8, or2pgTmpVar9_9, or2pgTmpVar10_10, or2pgTmpVar11_11, or2pgTmpVar12_12, or2pgTmpVar13_13, or2pgTmpVar14_14, or2pgTmpVar15_15, or2pgTmpVar16_16, or2pgTmpVar17_17, or2pgTmpVar18_18, or2pgTmpVar19_19, or2pgTmpVar20_20, or2pgTmpVar21_21, or2pgTmpVar22_22, or2pgTmpVar23_23, or2pgTmpVar24_24, or2pgTmpVar25_25, or2pgTmpVar26_26, or2pgTmpVar27_27, or2pgTmpVar28_28, or2pgTmpVar29_29, or2pgTmpVar30_30, or2pgTmpVar31_31, or2pgTmpVar32_32, or2pgTmpVar33_33, or2pgTmpVar34_34, or2pgTmpVar35_35, or2pgTmpVar36_36, or2pgTmpVar37_37, or2pgTmpVar38_38, or2pgTmpVar39_39, or2pgTmpVar40_40, or2pgTmpVar41_41, or2pgTmpVar42_42, or2pgTmpVar43_43, or2pgTmpVar44_44, or2pgTmpVar45_45, or2pgTmpVar46_46, or2pgTmpVar47_47, or2pgTmpVar48_48, or2pgTmpVar49_49, or2pgTmpVar50_50
            FROM
                d_visits v 
                        LEFT JOIN     d_amb_talon_visits atv ON atv.visit = v.id
     AND atv.lpu = v.lpu 
            WHERE
                v.id = nvisit_id
                     AND v.lpu = pnlpu::bigint;
            rat_other_visit.id := or2pgTmpVar0_0;
            rat_other_visit.lpu := or2pgTmpVar1_1;
            rat_other_visit.cid := or2pgTmpVar2_2;
            rat_other_visit.pid := or2pgTmpVar3_3;
            rat_other_visit.visit := or2pgTmpVar4_4;
            rat_other_visit.vis_date := or2pgTmpVar5_5;
            rat_other_visit.vis_employer := or2pgTmpVar6_6;
            rat_other_visit.vis_purpose := or2pgTmpVar7_7;
            rat_other_visit.vis_kind := or2pgTmpVar8_8;
            rat_other_visit.vis_place := or2pgTmpVar9_9;
            rat_other_visit.vis_is_primary := or2pgTmpVar10_10;
            rat_other_visit.vis_payment_kind := or2pgTmpVar11_11;
            rat_other_visit.reestrsp := or2pgTmpVar12_12;
            rat_other_visit.service := or2pgTmpVar13_13;
            rat_other_visit.vis_result := or2pgTmpVar14_14;
            rat_other_visit.vis_prof_disease := or2pgTmpVar15_15;
            rat_other_visit.dgroup := or2pgTmpVar16_16;
            rat_other_visit.ser_count := or2pgTmpVar17_17;
            rat_other_visit.ser_koeff := or2pgTmpVar18_18;
            rat_other_visit.uet_count := or2pgTmpVar19_19;
            rat_other_visit.ser_price := or2pgTmpVar20_20;
            rat_other_visit.ser_summ := or2pgTmpVar21_21;
            rat_other_visit.inc_to_reestr := or2pgTmpVar22_22;
            rat_other_visit.row_type := or2pgTmpVar23_23;
            rat_other_visit.hosp_reestrsp := or2pgTmpVar24_24;
            rat_other_visit.vis_ref_result := or2pgTmpVar25_25;
            rat_other_visit.is_first := or2pgTmpVar26_26;
            rat_other_visit.vis_dep := or2pgTmpVar27_27;
            rat_other_visit.is_last := or2pgTmpVar28_28;
            rat_other_visit.lpu_to := or2pgTmpVar29_29;
            rat_other_visit.dir_purpose := or2pgTmpVar30_30;
            rat_other_visit.note := or2pgTmpVar31_31;
            rat_other_visit.smp_team_profile := or2pgTmpVar32_32;
            rat_other_visit.other_lpu_code := or2pgTmpVar33_33;
            rat_other_visit.other_lpu_name := or2pgTmpVar34_34;
            rat_other_visit.other_dep_code := or2pgTmpVar35_35;
            rat_other_visit.other_emp_fio := or2pgTmpVar36_36;
            rat_other_visit.other_emp_code := or2pgTmpVar37_37;
            rat_other_visit.other_emp_snils := or2pgTmpVar38_38;
            rat_other_visit.other_emp_spec := or2pgTmpVar39_39;
            rat_other_visit.other_emp_jobt := or2pgTmpVar40_40;
            rat_other_visit.other_emp_spec_ed := or2pgTmpVar41_41;
            rat_other_visit.mo_state := or2pgTmpVar42_42;
            rat_other_visit.recommendation := or2pgTmpVar43_43;
            rat_other_visit.physical_group := or2pgTmpVar44_44;
            rat_other_visit.state_reason := or2pgTmpVar45_45;
            rat_other_visit.pc_pat := or2pgTmpVar46_46;
            rat_other_visit.gr_inter := or2pgTmpVar47_47;
            rat_other_visit.gr_s_usl := or2pgTmpVar48_48;
            rat_other_visit.other_emp_profil := or2pgTmpVar49_49;
            rat_other_visit.tarif := or2pgTmpVar50_50;
            IF NOT FOUND THEN
                serror := concat('Ошибка при поиске информации по визиту по связываемой услуге.', ' ', serror);
                PERFORM d_p_exc(1,serror);

            END IF;

        ELSIF pnstate = 2 THEN
            SELECT
            v.id
        INTO nvisit
        FROM
            d_visits v
        WHERE
            v.pid = pndirection_service::bigint;
        IF NOT FOUND THEN
            nvisit := null;

        END IF;
        IF nvisit IS NOT NULL THEN
            SELECT
                atv.id,
                pnlpu  lpu,
                null  cid,
                null  pid,
                coalesce(atv.visit,v.id)  visit,
                v.visit_date,
                null  vis_employer,
                atv.vis_purpose,
                atv.vis_kind,
                atv.vis_place,
                atv.vis_is_primary,
                atv.vis_payment_kind,
                null  reestrsp,
                nservice  service,
                atv.vis_result,
                atv.vis_prof_disease,
                atv.dgroup,
                1  ser_count,
                1  ser_koeff,
                1  uet_count,
                atv.ser_price,
                atv.ser_summ,
                coalesce(atv.inc_to_reestr,0)  inc_to_reestr,
                coalesce(atv.row_type,1)  row_type,
                null  hosp_reestrsp,
                atv.vis_ref_result,
                (CASE
                    WHEN atv.is_first IS NOT NULL THEN 1
                END)  is_first,
                null  vis_dep,
                atv.is_last,
                atv.lpu_to,
                atv.dir_purpose,
                atv.note,
                atv.smp_team_profile,
                ld.lpu_code  other_lpu_code,
                ld.lpu_name  other_lpu_name,
                coalesce(d.dp_code,db.dp_code)  other_dep_code,
                coalesce(d_pkg_str_tools.fio((ag.surname)::varchar,(ag.firstname)::varchar,(ag.lastname)::varchar,'.',(1)::varchar)::varchar,d_pkg_str_tools.fio((vag.surname)::varchar,(vag.firstname)::varchar,(vag.lastname)::varchar,'.',(1)::varchar)::varchar,'Врач Врач Врач')  other_emp_fio,
                coalesce(e.kod_vracha,ve.kod_vracha,'1234567890')  other_emp_code,
                coalesce(ag.snils,vag.snils,'12345678901')  other_emp_snils,
                coalesce(sp.code,vsp.code)  other_emp_spec,
                coalesce(jt.code,vjt.code)  other_emp_jobt,
                coalesce(sp_ed.se_code,vsp_ed.se_code)  other_emp_spec_ed,
                null  mo_state,
                atv.recommendation,
                null  physical_group,
                null  state_reason,
                null  pc_pat,
                null  gr_inter,
                null  gr_s_usl,
                null  other_emp_profil,
                null  tarif
            INTO or2pgTmpVar0_0, or2pgTmpVar1_1, or2pgTmpVar2_2, or2pgTmpVar3_3, or2pgTmpVar4_4, or2pgTmpVar5_5, or2pgTmpVar6_6, or2pgTmpVar7_7, or2pgTmpVar8_8, or2pgTmpVar9_9, or2pgTmpVar10_10, or2pgTmpVar11_11, or2pgTmpVar12_12, or2pgTmpVar13_13, or2pgTmpVar14_14, or2pgTmpVar15_15, or2pgTmpVar16_16, or2pgTmpVar17_17, or2pgTmpVar18_18, or2pgTmpVar19_19, or2pgTmpVar20_20, or2pgTmpVar21_21, or2pgTmpVar22_22, or2pgTmpVar23_23, or2pgTmpVar24_24, or2pgTmpVar25_25, or2pgTmpVar26_26, or2pgTmpVar27_27, or2pgTmpVar28_28, or2pgTmpVar29_29, or2pgTmpVar30_30, or2pgTmpVar31_31, or2pgTmpVar32_32, or2pgTmpVar33_33, or2pgTmpVar34_34, or2pgTmpVar35_35, or2pgTmpVar36_36, or2pgTmpVar37_37, or2pgTmpVar38_38, or2pgTmpVar39_39, or2pgTmpVar40_40, or2pgTmpVar41_41, or2pgTmpVar42_42, or2pgTmpVar43_43, or2pgTmpVar44_44, or2pgTmpVar45_45, or2pgTmpVar46_46, or2pgTmpVar47_47, or2pgTmpVar48_48, or2pgTmpVar49_49, or2pgTmpVar50_50
            FROM
                d_visits v 
                        JOIN     d_lpu l ON l.id = v.lpu  
                        JOIN     d_employers ve ON ve.id = v.employer  
                        JOIN     d_agents vag ON vag.id = ve.agent  
                        LEFT JOIN     d_jobtitles vjt ON vjt.id = ve.jobtitle  
                        LEFT JOIN     d_specialities vsp ON vsp.id = ve.speciality  
                        LEFT JOIN     d_specialities_ed vsp_ed ON vsp_ed.id = ve.speciality_ed  
                        LEFT JOIN     d_lpudict ld ON ld.id = l.lpudict  
                        LEFT JOIN     d_cablab c ON c.id = v.cablab  
                        LEFT JOIN     d_deps d ON d.id = c.department  
                        LEFT JOIN     d_amb_talon_visits atv ON atv.visit = v.id  
                        LEFT JOIN     d_deps db ON db.id = atv.vis_dep  
                        LEFT JOIN     d_employers e ON e.id = atv.vis_employer  
                        LEFT JOIN     d_agents ag ON ag.id = e.agent  
                        LEFT JOIN     d_jobtitles jt ON jt.id = e.jobtitle  
                        LEFT JOIN     d_specialities sp ON sp.id = e.speciality  
                        LEFT JOIN     d_specialities_ed sp_ed ON sp_ed.id = e.speciality_ed 
            WHERE
                v.id = nvisit;
            rat_other_visit.id := or2pgTmpVar0_0;
            rat_other_visit.lpu := or2pgTmpVar1_1;
            rat_other_visit.cid := or2pgTmpVar2_2;
            rat_other_visit.pid := or2pgTmpVar3_3;
            rat_other_visit.visit := or2pgTmpVar4_4;
            rat_other_visit.vis_date := or2pgTmpVar5_5;
            rat_other_visit.vis_employer := or2pgTmpVar6_6;
            rat_other_visit.vis_purpose := or2pgTmpVar7_7;
            rat_other_visit.vis_kind := or2pgTmpVar8_8;
            rat_other_visit.vis_place := or2pgTmpVar9_9;
            rat_other_visit.vis_is_primary := or2pgTmpVar10_10;
            rat_other_visit.vis_payment_kind := or2pgTmpVar11_11;
            rat_other_visit.reestrsp := or2pgTmpVar12_12;
            rat_other_visit.service := or2pgTmpVar13_13;
            rat_other_visit.vis_result := or2pgTmpVar14_14;
            rat_other_visit.vis_prof_disease := or2pgTmpVar15_15;
            rat_other_visit.dgroup := or2pgTmpVar16_16;
            rat_other_visit.ser_count := or2pgTmpVar17_17;
            rat_other_visit.ser_koeff := or2pgTmpVar18_18;
            rat_other_visit.uet_count := or2pgTmpVar19_19;
            rat_other_visit.ser_price := or2pgTmpVar20_20;
            rat_other_visit.ser_summ := or2pgTmpVar21_21;
            rat_other_visit.inc_to_reestr := or2pgTmpVar22_22;
            rat_other_visit.row_type := or2pgTmpVar23_23;
            rat_other_visit.hosp_reestrsp := or2pgTmpVar24_24;
            rat_other_visit.vis_ref_result := or2pgTmpVar25_25;
            rat_other_visit.is_first := or2pgTmpVar26_26;
            rat_other_visit.vis_dep := or2pgTmpVar27_27;
            rat_other_visit.is_last := or2pgTmpVar28_28;
            rat_other_visit.lpu_to := or2pgTmpVar29_29;
            rat_other_visit.dir_purpose := or2pgTmpVar30_30;
            rat_other_visit.note := or2pgTmpVar31_31;
            rat_other_visit.smp_team_profile := or2pgTmpVar32_32;
            rat_other_visit.other_lpu_code := or2pgTmpVar33_33;
            rat_other_visit.other_lpu_name := or2pgTmpVar34_34;
            rat_other_visit.other_dep_code := or2pgTmpVar35_35;
            rat_other_visit.other_emp_fio := or2pgTmpVar36_36;
            rat_other_visit.other_emp_code := or2pgTmpVar37_37;
            rat_other_visit.other_emp_snils := or2pgTmpVar38_38;
            rat_other_visit.other_emp_spec := or2pgTmpVar39_39;
            rat_other_visit.other_emp_jobt := or2pgTmpVar40_40;
            rat_other_visit.other_emp_spec_ed := or2pgTmpVar41_41;
            rat_other_visit.mo_state := or2pgTmpVar42_42;
            rat_other_visit.recommendation := or2pgTmpVar43_43;
            rat_other_visit.physical_group := or2pgTmpVar44_44;
            rat_other_visit.state_reason := or2pgTmpVar45_45;
            rat_other_visit.pc_pat := or2pgTmpVar46_46;
            rat_other_visit.gr_inter := or2pgTmpVar47_47;
            rat_other_visit.gr_s_usl := or2pgTmpVar48_48;
            rat_other_visit.other_emp_profil := or2pgTmpVar49_49;
            rat_other_visit.tarif := or2pgTmpVar50_50;
            IF NOT FOUND THEN
                PERFORM d_p_exc(1,'Ошибка при поиске информации по визиту по связываемой услуге');

            END IF;
            SELECT
                ol.id,
                ol.lpu_name
            INTO nlpu_other, slpu_other_name
            FROM
                d_other_lpu ol
            WHERE
                ol.lpu_ree_numb = rat_other_visit.other_lpu_code;
            IF NOT FOUND THEN
                nlpu_other := null;
                slpu_other_name := null;

            END IF;
            IF nlpu_other IS NOT NULL THEN
                IF slpu_other_name != rat_other_visit.other_lpu_name THEN
                    CALL d_pkg_other_lpu.upd(pnID => (nlpu_other)::numeric, pnLPU => pnlpu, psLPU_REE_NUMB => rat_other_visit.other_lpu_code, psLPU_NAME => rat_other_visit.other_lpu_name, psPHONE => (null)::varchar, psEMAIL => (null)::varchar);

                END IF;

            ELSE
                or2pgTmpVar51_1 := (nlpu_other)::numeric;
                 CALL d_pkg_other_lpu.add(pnD_INSERT_ID => or2pgTmpVar51_1, pnLPU => pnlpu, psLPU_REE_NUMB => rat_other_visit.other_lpu_code, psLPU_NAME => rat_other_visit.other_lpu_name);
                nlpu_other := (or2pgTmpVar51_1)::bigint;

            END IF;
            SELECT
                od.id
            INTO ndep_other
            FROM
                d_other_deps od
            WHERE
                od.other_lpu = nlpu_other
                     AND od.ree_numb = rat_other_visit.other_dep_code
                 LIMIT 1;
            IF NOT FOUND THEN
                ndep_other := null;

            END IF;
            IF ( ndep_other IS NULL
     AND ( nullif(rat_other_visit.other_dep_code,'') IS NOT NULL
     OR nserv_type != 8 ) ) THEN
                or2pgTmpVar51_1 := (ndep_other)::numeric;
                 CALL d_pkg_other_deps.add(pnD_INSERT_ID => or2pgTmpVar51_1, pnLPU => pnlpu, pnOTHER_LPU => (nlpu_other)::numeric, psREE_NUMB => rat_other_visit.other_dep_code);
                ndep_other := (or2pgTmpVar51_1)::bigint;

            END IF;
            SELECT
                oe.id
            INTO nemployer_other
            FROM
                d_other_employers oe
            WHERE
                oe.other_lpu = nlpu_other
                     AND ( ( oe.other_dep = ndep_other )
                     OR ( ndep_other IS NULL
                     AND oe.other_dep IS NULL ) )
                     AND oe.emp_fio = rat_other_visit.other_emp_fio
                     AND ( ( oe.emp_code = rat_other_visit.other_emp_code )
                     OR ( nullif(rat_other_visit.other_emp_code,'') IS NULL
                     AND nullif(oe.emp_code,'') IS NULL ) )
                 LIMIT 1;
            IF NOT FOUND THEN
                nemployer_other := null;

            END IF;
            IF nemployer_other IS NULL THEN
                or2pgTmpVar51_1 := (nemployer_other)::numeric;
                 CALL d_pkg_other_employers.add(pnD_INSERT_ID => or2pgTmpVar51_1, pnLPU => pnlpu, pnOTHER_LPU => (nlpu_other)::numeric, pnOTHER_DEP => (ndep_other)::numeric, psEMP_FIO => rat_other_visit.other_emp_fio, psEMP_CODE => rat_other_visit.other_emp_code, psEMP_SNILS => rat_other_visit.other_emp_snils, psEMP_JOBT => rat_other_visit.other_emp_jobt, psEMP_SPEC => rat_other_visit.other_emp_spec, psEMP_SPEC_ED => rat_other_visit.other_emp_spec_ed);
                nemployer_other := (or2pgTmpVar51_1)::bigint;

            END IF;

        END IF;

        END IF;
        IF pnat IS NULL THEN
            CALL d_pkg_amb_talons.add(pnD_INSERT_ID => pnat, pnLPU => pnlpu, pnCID => d_pkg_catalogs.find_root_catalog_f1(1,pnlpu,'AMB_TALONS')::numeric, pnPERSMEDCARD => (npersmedcard)::numeric, pnAT_TYPE => 0, pnFIRST_REF_PURPOSE => (null)::numeric, pnREF_KIND => (null)::numeric, pdDIRECT_DATE => (null)::timestamp, psDIRECT_NUMB => (null)::varchar, pnDIRECT_LPU => (null)::numeric, pnDISEASECASE => (null)::numeric, pnHOSP_MKB => (null)::numeric, pnHOSP_KIND => (null)::numeric, pnREF_RESULT => (null)::numeric, pnD_GROUP => (null)::numeric, pdEND_DATE => (null)::timestamp, pnAT_DC_TYPE => 0, pdDEATHDATE => (null)::timestamp, pnDEATHDOCTYPE => (null)::numeric, pdDEATHDOCDATE => (null)::timestamp, psDEATHDOCNUM => (null)::varchar, psDEATHCERTIFY_EMP => (null)::varchar, pnFD_PRVD_CONDIT => 3, pnKSG => (null)::numeric, pnDC_IS_CLOSE => 0, pnIS_AUTO => 1, pnPC_TYPE => (null)::numeric, pnPC_STEP => (null)::numeric, pnHID => (null)::numeric, pnDC_DIAGNOSIS => (null)::numeric, pnDIRECT_SPECIALITY => (null)::numeric, pnPC_RESULT => (null)::numeric, pnPC_CLOSE_REASON => (null)::numeric, pnSTATE_MEK => (null)::numeric, pnMOBILE_BRIG => (null)::numeric, pnCONTROL_CARD => (ncontrol_card)::numeric);

        END IF;
        or2pgTmpVar51_1 := (natv_other_id)::numeric;
         CALL d_pkg_amb_talon_other_visits.add(pnD_INSERT_ID => or2pgTmpVar51_1, pnLPU => pnlpu, pnPID => pnat, pnVISIT => (rat_other_visit.visit)::numeric, pdVIS_DATE => rat_other_visit.vis_date, pnVIS_EMPLOYER => (rat_other_visit.vis_employer)::numeric, pnVIS_PURPOSE => (rat_other_visit.vis_purpose)::numeric, pnVIS_KIND => (rat_other_visit.vis_kind)::numeric, pnVIS_PLACE => (rat_other_visit.vis_place)::numeric, pnVIS_IS_PRIMARY => rat_other_visit.vis_is_primary, pnVIS_PAYMENT_KIND => (rat_other_visit.vis_payment_kind)::numeric, pnREESTRSP => (rat_other_visit.reestrsp)::numeric, pnSERVICE => (rat_other_visit.service)::numeric, pnVIS_RESULT => (rat_other_visit.vis_result)::numeric, pnVIS_PROF_DISEASE => (rat_other_visit.vis_prof_disease)::numeric, pnDGROUP => (rat_other_visit.dgroup)::numeric, pnSER_COUNT => rat_other_visit.ser_count, pnSER_KOEFF => rat_other_visit.ser_koeff, pnUET_COUNT => rat_other_visit.uet_count, pnSER_PRICE => rat_other_visit.ser_price, pnSER_SUMM => rat_other_visit.ser_summ, pnINC_TO_REESTR => rat_other_visit.inc_to_reestr, pnROW_TYPE => rat_other_visit.row_type, pnHOSP_REESTRSP => (rat_other_visit.hosp_reestrsp)::numeric, pnVIS_REF_RESULT => (rat_other_visit.vis_ref_result)::numeric, pnIS_FIRST => rat_other_visit.is_first, pnVIS_DEP => (rat_other_visit.vis_dep)::numeric, pnIS_LAST => rat_other_visit.is_last, pnLPU_TO => (rat_other_visit.lpu_to)::numeric, pnDIR_PURPOSE => (rat_other_visit.dir_purpose)::numeric, psNOTE => rat_other_visit.note, pnSMP_TEAM_PROFILE => (rat_other_visit.smp_team_profile)::numeric, psOTHER_LPU_CODE => rat_other_visit.other_lpu_code, psOTHER_LPU_NAME => rat_other_visit.other_lpu_name, psOTHER_DEP_CODE => rat_other_visit.other_dep_code, psOTHER_EMP_FIO => rat_other_visit.other_emp_fio, psOTHER_EMP_CODE => rat_other_visit.other_emp_code, psOTHER_EMP_SNILS => rat_other_visit.other_emp_snils, psOTHER_EMP_SPEC => rat_other_visit.other_emp_spec, psOTHER_EMP_JOBT => rat_other_visit.other_emp_jobt, psOTHER_EMP_SPEC_ED => rat_other_visit.other_emp_spec_ed, pnMO_STATE => rat_other_visit.mo_state, pnRECOMMENDATION => (rat_other_visit.recommendation)::numeric, pnPHYSICAL_GROUP => (rat_other_visit.physical_group)::numeric, pnSTATE_REASON => rat_other_visit.state_reason, pnPC_PAT => rat_other_visit.pc_pat, psOTHER_EMP_PROFIL => rat_other_visit.other_emp_profil, pnTARIF => (rat_other_visit.tarif)::numeric);
        natv_other_id := (or2pgTmpVar51_1)::bigint;

    ELSIF psaction = 'DEL' THEN
        SELECT
        mcdp.service,
        mcdp.pid,
        mcdp.control_card
    INTO nservice, npersmedcard, ncontrol_card
    FROM
        d_pmc_disp_plan mcdp
    WHERE
        mcdp.id = pnpmc_disp_plan::bigint
             AND mcdp.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_p_exc(1,'Не найдена информация по плану диспансерного учета');

    END IF;
    ncontrol_card := coalesce(pncontrol_card,ncontrol_card);
    SELECT
        atov.id,
        at.id
    INTO natv_other_id, pnat
    FROM
        d_direction_services ds 
                JOIN     d_visits v ON v.pid = ds.id  
                JOIN     d_amb_talon_other_visits atov ON atov.visit = v.id  
                JOIN     d_amb_talons at ON at.id = atov.pid 
    WHERE
        ds.id = pndirection_service::bigint
             AND at.persmedcard = npersmedcard
             AND at.control_card = ncontrol_card
             AND at.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        natv_other_id := null;

    END IF;
    --  в случае составной услуги поищем данные по направлению через HID
        IF natv_other_id IS NULL THEN
        SELECT
            atov.id,
            at.id
        INTO natv_other_id, pnat
        FROM
            d_direction_services ds 
                    JOIN     d_visits v ON v.pid = ds.id  
                    JOIN     d_amb_talon_other_visits atov ON atov.visit = v.id  
                    JOIN     d_amb_talons at ON at.id = atov.pid 
        WHERE
            ds.hid = pndirection_service::bigint
                 AND at.persmedcard = npersmedcard
                 AND at.control_card = ncontrol_card
                 AND at.lpu = pnlpu::bigint;
        IF NOT FOUND THEN
            natv_other_id := null;

        END IF;

    END IF;
    IF natv_other_id IS NOT NULL THEN
        CALL d_pkg_amb_talon_other_visits.del(pnID => (natv_other_id)::numeric, pnLPU => pnlpu);

    END IF;
    BEGIN
        SELECT
            at.id
        INTO STRICT pnat
        FROM
            d_amb_talons at
        WHERE
            at.id = pnat::bigint
                 AND at.lpu = pnlpu::bigint
                 AND at.control_card = ncontrol_card
                 AND NOT exists ( SELECT
                null as null
            FROM
                d_amb_talon_visits atv
            WHERE
                atv.pid = at.id
                     AND atv.lpu = pnlpu::bigint )
                 AND NOT exists ( SELECT
                null as null
            FROM
                d_amb_talon_other_visits atv
            WHERE
                atv.pid = at.id
                     AND atv.lpu = pnlpu::bigint );
        IF pnat IS NOT NULL THEN
            CALL d_pkg_amb_talons.del(pnID => pnat, pnLPU => pnlpu, pnIS_AUTO => 1);

        END IF;
        EXCEPTION
            WHEN no_data_found THEN
                        pnat := null;

    END;

    END IF;
END
$procedure$
```

---

### Функция №6: d_pkg_control_card.recount_plan_dates

```sql
CREATE OR REPLACE PROCEDURE d_pkg_control_card.recount_plan_dates(IN pncontrol_card numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    dDATE_FROM d_control_card.date_from%TYPE;
    dREMOVE_DATE d_control_card.remove_date%TYPE;
    dF_REC_DATE timestamp(0);
    dF_PLAN_DATE timestamp(0);
    dF_DAYS_COUNT NUMERIC(5);
    x record;
BEGIN
    SELECT
        t.date_from,
        t.remove_date
    INTO ddate_from, dremove_date
    FROM
        d_control_card t
    WHERE
        t.id = pncontrol_card::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pncontrol_card,'CONTROL_CARD');

    END IF;
    IF dremove_date IS NOT NULL THEN
        PERFORM d_p_exc(1,'Невозможно изменить план после снятия с учета.');

    END IF;
    SELECT
        f.rec_date,
        f.plan_date,
        trunc(f.rec_date) - trunc(f.plan_date)
    INTO df_rec_date, df_plan_date, df_days_count
    FROM
        ( SELECT
                dss.rec_date,
                ds.plan_date,
                row_number ( ) over ( ORDER BY dss.rec_date desc )  rn
            FROM
                d_pmc_disp_plan ds 
                        JOIN     d_direction_services dss ON dss.id = ds.reg_id 
            WHERE
                ds.control_card = pncontrol_card::bigint
                     AND trunc(ddate_from) < trunc(dss.rec_date) ) f
    WHERE
        f.rn::numeric = 1;
    IF NOT FOUND THEN
        null   /* если не нашли ни одной записанной услуги - то ничего не делаем */;

    END IF;
    --  продолжаем, только если нашли нужную запись и действительно произошел сдвиг
        IF df_rec_date IS NOT NULL
     AND df_plan_date IS NOT NULL
     AND df_days_count > 0 THEN
        FOR x IN (
            SELECT
                ds.id,
                ds.plan_date,
                ds.service,
                ds.speciality
            FROM
                d_pmc_disp_plan ds
            WHERE
                ds.control_card = pncontrol_card::bigint
                     AND ds.plan_date >= df_plan_date
                     AND NOT exists ( SELECT
                    null as null
                FROM
                    d_direction_services dss
                WHERE
                    dss.id = ds.reg_id )
ORDER BY ds.id desc)
        LOOP
            CALL d_pkg_pmc_disp_plan.upd((x.id)::numeric, pnlpu, (x.service)::numeric, (x.speciality)::numeric, (x.plan_date + df_days_count)::timestamp, 0);
        END LOOP;
        CALL d_pkg_control_card.set_date_from(pncontrol_card, pnlpu, trunc(df_rec_date)::timestamp);

    END IF;
END
$procedure$
```

---

### Функция №7: d_pkg_control_card.clear_reg_from_disp_plan

```sql
CREATE OR REPLACE PROCEDURE d_pkg_control_card.clear_reg_from_disp_plan(IN pncontrol_card_id numeric, IN pndirection_service_id numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    x record;
BEGIN
    --    D_P_EXC('pnCONTROL_CARD_ID '||pnCONTROL_CARD_ID); не прокидывается с формы, нужно проверить
     FOR x IN (
        SELECT
            dp.id  disp_plan_id
        FROM
            d_control_card cc 
                    JOIN     d_pmc_disp_plan dp ON dp.control_card = cc.id 
        WHERE
            cc.id = pncontrol_card_id::bigint)
    LOOP
        BEGIN
            update d_pmc_disp_plan t set reg_id = null , reg_code = null where t.id = x.disp_plan_id::bigint
                 AND t.reg_id = pndirection_service_id;
            EXCEPTION
                WHEN no_data_found THEN
                            null;

        END;
    END LOOP;
END
$procedure$
```

---

### Функция №8: d_pkg_pmc_disp_plan.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_pmc_disp_plan.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    SELECT
        t.cid
    INTO ncid
    FROM
        d_pmc_disp_plan t
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'PMC_DISP_PLAN');

    END IF;
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_DELETE', 'D_PMC_DISP_PLAN', pnid);
    BEGIN
        DELETE FROM d_pmc_disp_plan t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'PMC_DISP_PLAN');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'PMC_DISP_PLAN', 'PMC_DISP_PLAN_DELETE', 'D_PMC_DISP_PLAN', pnid);
END
$procedure$
```



---

## ИНСТРУКЦИЯ ДЛЯ АНАЛИЗА

Проанализируй предоставленную информацию и ответь на вопросы:

1. Какие основные бизнес-сущности используются?
2. Какие связи между таблицами можно выделить?
3. Есть ли потенциальные проблемы с производительностью?
4. Какие вьюхи наиболее часто используются?
5. Есть ли дублирование логики?
6. Какие рекомендации по оптимизации?

Обращай внимание на:
- Префиксы: D_V_* (вьюхи), D_* (таблицы), D_PKG_* (пакеты)
- Константы из D_PKG_CONSTANTS.SEARCH_*
- Системные опции из D_PKG_OPTIONS.GET
