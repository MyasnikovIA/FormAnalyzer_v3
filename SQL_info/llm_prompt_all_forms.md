# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** /Forms/ControlCard/dispensary_observation_plan/dispensary_observation_plan.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\ControlCard\dispensary_observation_plan\dispensary_observation_plan.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Sun May 17 21:32:05 GMT+07:00 2026

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


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

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


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

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

**Oracle SQL тело функции 🟠:**

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

**PostgreSQL тело функции 🐘:**

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

### Брокер №2: unit=DIRECTION_SERVICES, action=DELETE

**Тип брокера:** Требуется поиск в D_UNITBPS

**Вызываемая функция:**
```
D_PKG_DIRECTION_SERVICES.DEL
```

**Oracle SQL тело функции 🟠:**

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnDEL_DIR                            in NUMBER default 1,
  pnDEL_PATJOUR                        in NUMBER default 0,
  pnDEL_SERV                           in NUMBER default 0,
  pnCORS_NEURONIQ                      in NUMBER default null
)
is
  nSERV_STATUS          D_DIRECTION_SERVICES.SERV_STATUS%type;
  nPID                  D_PKG_STD.tREF;
  nCOUNT                D_PKG_STD.tREF;
  nHH_DEP               NUMBER(17);
  nSERVICE              NUMBER(17);
  nHSC_RESULT           NUMBER(1);
  nLINK                 D_PKG_STD.tREF;
  nNURSE_USER_TEMPLATES D_DIRECTION_SERVICES.NURSE_USER_TEMPLATES%type;
  sSO_REGION            VARCHAR2(50);
  nSE_TYPE              D_SERVICES.SE_TYPE%type;
  nCABLAB_TO            NUMBER(17);
  nEMPLOYER_TO          NUMBER(17);
  dREC_DATE             DATE;
  nLPU                  NUMBER(17);
  nIS_COMBINED          D_SERVICES.IS_COMBINED%type;
  nCOMBSERVICE          NUMBER;
  sEHR_LIST             VARCHAR2(4000);
  nLBM_SLIS_QUOTE_USE_ID NUMBER(17);
begin
  clOUT_ERR_INFO := D_CL_SS();
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,null,null,'DIRECTION_SERVICES_DELETE',pnID);
  begin
    select t.SERV_STATUS,
           t.PID,
           t.HH_DEP,
           t.SERVICE,
           t.NURSE_USER_TEMPLATES,
           s.SE_TYPE,
           t.CABLAB_TO,
           t.EMPLOYER_TO,
           t.REC_DATE,
           t.LPU
      into nSERV_STATUS,
           nPID,
           nHH_DEP,
           nSERVICE,
           nNURSE_USER_TEMPLATES,
           nSE_TYPE,
           nCABLAB_TO,
           nEMPLOYER_TO,
           dREC_DATE,
           nLPU
      from D_DIRECTION_SERVICES t
           join D_SERVICES s on s.ID = t.SERVICE
     where t.ID   = pnID
       and t.LPU  = pnLPU;
  exception when NO_DATA_FOUND then
    D_PKG_MSG.RECORD_NOT_FOUND(pnID, 'DIRECTION_SERVICES');
  end;
```

**PostgreSQL тело функции 🐘:**

```sql
CREATE OR REPLACE PROCEDURE d_pkg_direction_services.del(IN pnid numeric, IN pnlpu numeric, IN pndel_dir numeric DEFAULT 1, IN pndel_patjour numeric DEFAULT 0, IN pndel_serv numeric DEFAULT 0, IN pncors_neuroniq numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nSERV_STATUS d_direction_services.serv_status%TYPE;
    nPID numeric(17);
    nCOUNT numeric(17);
    nHH_DEP NUMERIC(17);
    nSERVICE NUMERIC(17);
    nHSC_RESULT NUMERIC(1);
    nLINK numeric(17);
    nNURSE_USER_TEMPLATES d_direction_services.nurse_user_templates%TYPE;
    sSO_REGION varchar(50);
    nSE_TYPE d_services.se_type%TYPE;
    nCABLAB_TO NUMERIC(17);
    nEMPLOYER_TO NUMERIC(17);
    dREC_DATE timestamp(0);
    nLPU NUMERIC(17);
    nIS_COMBINED d_services.is_combined%TYPE;
    nCOMBSERVICE NUMERIC;
    sEHR_LIST varchar(4000);
    nLBM_SLIS_QUOTE_USE_ID NUMERIC(17);
    scompl_info d_pkg_std.tlstr;
    rec record;
    cur record;
    cr record;
    r record;
    s record;
    g record;
    dse record;
    rci record;
    x record;
    i record;
    cv record;
    crdisp_plan record;
    crdata_therapy record;
BEGIN
    PERFORM set_config ('d_pkg_direction_services.clout_err_info', d_cl_ss()::varchar, false);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'DIRECTION_SERVICES_DELETE', pnid);
    SELECT
        t.serv_status,
        t.pid,
        t.hh_dep,
        t.service,
        t.nurse_user_templates,
        s.se_type,
        t.cablab_to,
        t.employer_to,
        t.rec_date,
        t.lpu
    INTO nserv_status, npid, nhh_dep, nservice, nnurse_user_templates, nse_type, ncablab_to, nemployer_to, drec_date, nlpu
    FROM
        d_direction_services t 
                JOIN     d_services s ON s.id = t.service 
    WHERE
        t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;
    IF NOT FOUND THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'DIRECTION_SERVICES');

    END IF;
    IF nserv_status = 1
     AND pndel_serv = 0 THEN
        PERFORM d_p_exc(1,psTEXT => (concat('1. Услуга уже оказана. Удаление запрещено. ', d_pkg_info.info('DIRECTION_SERVICES',pnid,'Дата:[7] Услуга:[2]')))::varchar);

    END IF;
    IF nse_type = 8 THEN
        FOR rec IN (
            SELECT
                lp.is_picked
            FROM
                d_labmed_patjour lp
            WHERE
                lp.direction_service = pnid::bigint)
        LOOP
            IF rec.is_picked::numeric = 1 THEN
                PERFORM d_p_exc(1,'Удаление направления недоступно: забран биоматериал.');

            END IF;
        END LOOP;

    END IF;
    SELECT
        string_agg(to_char(e.id),';')
    INTO STRICT sehr_list
    FROM
        d_ehrs e 
                JOIN     d_ehr_states es ON es.pid = e.id 
    WHERE
        e.unit_id = pnid
             AND e.unit = 'DIRECTION_SERVICES'
             AND es.status IN ( 6 , 7 , 10 );
    IF nullif(sehr_list,'') IS NOT NULL THEN
        PERFORM d_p_exc(1,(concat('Запись раздела [Направление на услугу] не может быть удалена, так как есть отправленные или зарегистрированные документы в Архиве документов. (', sehr_list, ')'))::varchar);

    END IF;
    CALL d_pkg_ehr_states.set_unsgn_by_ds(pnid);
    --  Удалим запись из стат карты, если таковая имеется
        IF nhh_dep IS NOT NULL
     AND ( d_pkg_option_specs.get('HSCIdenticalToHH',pnlpu)::numeric = 1
     OR d_pkg_option_specs.get('HSCCreateFromHH',pnlpu)::numeric = 1 ) THEN
        nhsc_result := d_pkg_hosp_stat_cards.upd_hsc_by_direction_service(nhh_dep,pnlpu,pnid,'DEL',nservice,nserv_status)::numeric;

    END IF;
    --  Удаление нижних уровней иерархии
     FOR cur IN (
        SELECT
            t.id
        FROM
            d_direction_services t
        WHERE
            t.hid = pnid::bigint
                 AND t.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_direction_services.del((cur.id)::numeric, pnlpu);
    END LOOP;
    --  Удаление автоматически связанных направлений на услуги
     FOR cr IN (
        SELECT
            t.id,
            t.serv_status,
            ( SELECT
                count ( * )
            FROM
                ( SELECT
                        *
                    FROM
                        d_visits v
                    WHERE
                        v.pid = t.id
                         LIMIT 1 ) t_alias_0 )  has_visit,
            ( SELECT
                count ( * )
            FROM
                ( SELECT
                        *
                    FROM
                        d_mp_prescribes mp
                        CROSS JOIN                         d_mp_prescribe_specs mps
                    WHERE
                        mp.direction_service = t.id
                             AND mps.pid = mp.id
                             AND mps.goodsupply IS NOT NULL
                         LIMIT 1 ) t_alias_1 )  has_gs_mps
        FROM
            d_direction_services t
        WHERE
            t.rpid = pnid::bigint
                 AND t.lpu = pnlpu::bigint)
    LOOP
        --  Проверка наличия списанных медикаментов
                IF cr.has_gs_mps::numeric = 1 THEN
            PERFORM d_p_exc(1,psTEXT => (concat('2. На связанной услуге: ', d_pkg_info.info('DIRECTION_SERVICES',(cr.id)::numeric,'Дата:[7] Услуга:[2]'), ' есть списание медикаментов. Удаление запрещено.'))::varchar);

        END IF;
        --  Для оказанных направлений трём связь
                IF cr.serv_status::numeric = 1
     OR cr.has_visit::numeric = 1 THEN
            update d_direction_services u set rpid = null where u.id = cr.id::bigint;

        ELSE
            CALL d_pkg_direction_services.del((cr.id)::numeric, pnlpu);

        END IF;
    END LOOP   /*  cr */;
    --  Отсоединение внесенных результатов - направлений на услуги
    update d_direction_services u set irid = null where u.irid = pnid::bigint;
    --  Удаление истории перезаписей
     FOR cr IN (
        SELECT
            j.id
        FROM
            d_dir_serv_changes j
        WHERE
            j.pid = pnid::bigint)
    LOOP
        CALL d_pkg_dir_serv_changes.del((cr.id)::numeric, pnlpu);
    END LOOP;
    --  Удаление истории записи к врачу
     FOR cr IN (
        SELECT
            dsl.id
        FROM
            d_direction_services_log dsl
        WHERE
            dsl.pid = pnid::bigint)
    LOOP
        CALL d_pkg_direction_services_log.del((cr.id)::numeric, pnlpu);
    END LOOP;
    --  Удаление параметров направления
     FOR cr IN (
        SELECT
            j.id
        FROM
            d_dir_serv_fields j
        WHERE
            j.pid = pnid::bigint)
    LOOP
        CALL d_pkg_dir_serv_fields.del((cr.id)::numeric, pnlpu);
    END LOOP;
    /*  Удаление медкарты
 удаляем Карты медосмотров: дополнительная информация о состоянии здоровья ребенка если не заполнены визиты */
 FOR r IN (
        SELECT
            t1.id
        FROM
            d_prof_card_services t
            CROSS JOIN             d_pc_orphan_health t1
        WHERE
            t1.pid = t.pid
                 AND exists ( SELECT
                null as null
            FROM
                d_prof_card_dir_servs tt
            WHERE
                tt.dir_serv = pnid::bigint
                     AND tt.pid = t.id )
                 AND t.lpu = pnlpu::bigint
                 AND t1.visit IS NULL
                 AND t1.vac_made_visit IS NULL)
    LOOP
        CALL d_pkg_pc_orphan_health.del((r.id)::numeric, pnlpu);
    END LOOP;
    CALL d_pkg_prof_card.del_from_schedule(pnid, pnlpu);
    --  Удаление видов оплат
     FOR cur IN (
        SELECT
            t.id
        FROM
            d_dir_serv_payments t
        WHERE
            t.pid = pnid::bigint
                 AND t.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_dir_serv_payments.extended_del((cur.id)::numeric, pnlpu);
    END LOOP;
    CALL d_pkg_labmed_patjour.del_from_dir_servs(pnid, pnlpu, pndel_patjour);
    --  Удаление назначений
     FOR r IN (
        SELECT
            t.id
        FROM
            d_mp_prescribes t
        WHERE
            t.direction_service = pnid::bigint)
    LOOP
        --  Удаление исполнений
         FOR s IN (
            SELECT
                t.id
            FROM
                d_mp_prescribe_specs t
            WHERE
                t.pid = r.id
                     AND t.is_executed = 0)
        LOOP
            CALL d_pkg_mp_prescribe_specs.del((s.id)::numeric, pnlpu, 0);
        END LOOP;
        CALL d_pkg_mp_prescribes.del((r.id)::numeric, pnlpu);
    END LOOP;
    --  Удаление данных из маршрутных талонов
     FOR r IN (
        SELECT
            t.id,
            t.lpu
        FROM
            d_routelistsp t
        WHERE
            t.direction_service = pnid::bigint)
    LOOP
        CALL d_pkg_routelistsp.del((r.id)::numeric, (r.lpu)::numeric);
    END LOOP;
    --  Удаление из журнала реализаций
     FOR r IN (
        SELECT
            t.id
        FROM
            d_rendering_journal t
        WHERE
            t.lpu = pnlpu::bigint
                 AND t.direction_service = pnid::bigint)
    LOOP
        FOR s IN (
            SELECT
                t.id
            FROM
                d_rj_fac_accounts t
            WHERE
                t.pid = r.id)
        LOOP
            FOR g IN (
                SELECT
                    t.id
                FROM
                    d_rj_facc_payments t
                WHERE
                    t.pid = s.id)
            LOOP
                CALL d_pkg_rj_facc_payments.del((g.id)::numeric, pnlpu);
            END LOOP;
            CALL d_pkg_rj_fac_accounts.del((s.id)::numeric, pnlpu);
        END LOOP;
        CALL d_pkg_rendering_journal.del((r.id)::numeric, pnlpu);
    END LOOP;
    --  Удаление назначенных сотрудников
     FOR dse IN (
        SELECT
            e.id
        FROM
            d_dir_serv_employers e
        WHERE
            e.pid = pnid::bigint)
    LOOP
        CALL d_pkg_dir_serv_employers.del((dse.id)::numeric, pnlpu);
    END LOOP;
    --  Очистка привязки в брони
     FOR r IN (
        SELECT
            re.id,
            re.status,
            re.lpu
        FROM
            d_reservation re
        WHERE
            re.direction_service = pnid::bigint)
    LOOP
        update d_reservation res set direction_service = null , status = 3 where res.id = r.id;
    END LOOP;
    --  Удаление ссылки из Профкарты
    update d_prof_card t set final_dir_serv = null where t.final_dir_serv = pnid::bigint;
    --  Удаление ссылки из направления
    update d_directions t set reg_dir_serv = null where t.reg_dir_serv = pnid::bigint;
    CALL d_pkg_labmed_direction_line.del_from_dir_servs(pnid, pnlpu);
    --  удвление автоинформирования пациента о записи на прием
     FOR rci IN (
        SELECT
            ci.id
        FROM
            d_calls_info ci
        WHERE
            ci.direction_services = pnid::bigint)
    LOOP
        CALL d_pkg_calls_info.del((rci.id)::numeric, pnlpu);
    END LOOP;
    CALL d_pkg_ib_queue_mode.del_ib_queue(pnid);
    CALL d_pkg_ib_cabgroup_cabs.del_ib_invitees(pnDIRECTION_SERVICE => pnid);
    CALL d_pkg_timetable.dir_link(pnlpu, pnid, 'DELETE', 0, (null)::numeric, nservice, ncablab_to, nemployer_to, drec_date, nlpu);
    sso_region := d_pkg_options.get(psSO_CODE => 'Region',pnLPU => pnlpu,pnRAISE => 0)::varchar;
    IF nullif(sso_region,'') IS NOT NULL
     AND sso_region = '54'
     AND pncors_neuroniq IS NULL THEN
        CALL int_pkg_el_queue.talon_close_registration((pnid)::bigint);

    END IF;
    /*  Удаление связанных записей по электронной очереди
 */
 FOR x IN (
        SELECT
            its.id
        FROM
            d_ib_talon_status its
        WHERE
            its.direction_service = pnid::bigint)
    LOOP
        CALL d_pkg_ib_talon_status.del_ib_talonstatus(pnID => (x.id)::numeric, pnLPU => pnlpu);
    END LOOP;
    --  Удаление ссылки из Вызов СМП: состояние
    update d_smp_call_state t set num_dir_serv = null where t.num_dir_serv = pnid::bigint;
    --  Удаление записей на услуги комплекса
        IF nse_type = 11 THEN
        scompl_info = d_pkg_direction_services.get_compl_ds_info(pnid);
                BEGIN
            SELECT
                s.is_combined
            INTO STRICT nis_combined
            FROM
                d_direction_services ds 
                        JOIN     d_services s ON s.id = ds.service 
            WHERE
                ds.id = pnid::bigint;
            IF nis_combined = 1 THEN
                SELECT
                    count(1)
                INTO STRICT ncombservice
                FROM
                    d_direction_services ds 
                            JOIN     d_dir_serv_payments ddsp ON ddsp.pid = ds.id  
                            JOIN     d_contract_payments cp ON cp.dir_serv_payment = ddsp.id 
                WHERE
                    ds.complid = pnid::bigint
                         AND cp.debt_summ < ( cp.dir_summ - cp.discount_summ )::numeric;
                IF ncombservice > 0 THEN
                    PERFORM d_p_exc(1,'Невозможно удалить запись на комплексную услугу с комбинированным ценообразованием, так как некоторые из услуг в составе комплекса уже оплачены');

                END IF;
                IF nullif(scompl_info,'') IS NULL THEN
                    FOR i IN (
                        SELECT
                            t.id,
                            t.lpu
                        FROM
                            d_direction_services t
                        WHERE
                            t.complid = pnid::bigint)
                    LOOP
                        CALL d_pkg_direction_services.del(pnID => (i.id)::numeric, pnLPU => (i.lpu)::numeric, pnDEL_DIR => pndel_dir, pnDEL_PATJOUR => pndel_patjour, pnDEL_SERV => pndel_serv, pnCORS_NEURONIQ => pncors_neuroniq);
                    END LOOP;

                ELSE
                    PERFORM d_p_exc(1,(concat('Удаление записей на комплексную услугу невозможно, так как есть записи на услуги комплекса: ', scompl_info))::varchar);

                END IF;

            END IF;
        END;

    END IF;
    --  удаление ссылки в разделе D_CHILDBIRTH_VISITS
     FOR cv IN (
        SELECT
            c.id
        FROM
            d_childbirth_visits c
        WHERE
            c.dir_serv = pnid::bigint)
    LOOP
        CALL d_pkg_childbirth_visits.set_visit(pnID => (cv.id)::numeric, pnLPU => pnlpu, pnVISIT => (null)::numeric);
        CALL d_pkg_childbirth_visits.set_dir_serv(pnID => (cv.id)::numeric, pnLPU => pnlpu, pnDIR_SERV => (null)::numeric);
    END LOOP;
    --  удаление ссылки в разделе D_WL_RECORDS
     FOR cv IN (
        SELECT
            wlr.id
        FROM
            d_wl_records wlr
        WHERE
            wlr.direction_service = pnid::bigint
                 AND wlr.lpu = pnlpu::bigint)
    LOOP
        CALL d_pkg_wl_records.set_direction_service(pnID => (cv.id)::numeric, pnLPU => pnlpu, pnDIRECTION_SERVICE => (null)::numeric);
    END LOOP;
    --  Очистка привязки в плане диспансерного наблюдения
     FOR crdisp_plan IN (
        SELECT
            pmc.id
        FROM
            d_pmc_disp_plan pmc
        WHERE
            pmc.reg_id = pnid)
    LOOP
        update d_pmc_disp_plan pmc set reg_id = null , reg_code = null where pmc.id = crdisp_plan.id::bigint;
    END LOOP;
    --  Удаление связи с данными ЗНО в статкарте
     FOR crdata_therapy IN (
        SELECT
            t.id
        FROM
            d_hscd_onk_data_therapy t
        WHERE
            t.direction_service = pnid::bigint)
    LOOP
        update d_hscd_onk_data_therapy t set direction_service = null where t.id = crdata_therapy.id::bigint;
    END LOOP;
    --  Удаление резервирования квоты на лабораторное исследование
     FOR x IN (
        SELECT
            lsqu.id
        FROM
            d_lbm_slis_quote_use lsqu
        WHERE
            lsqu.ds_quote_use_id = pnid::bigint)
    LOOP
        CALL d_pkg_lbm_slis_quote_use.del(pnID => (x.id)::numeric, pnLPU => pnlpu);
    END LOOP;
    --  Удаление
        BEGIN
        DELETE FROM d_direction_services t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'DIRECTION_SERVICES');

    END IF;
    --  Удаление направления
        IF pndel_dir = 1 THEN
        SELECT
            count(1)
        INTO STRICT ncount
        FROM
            d_direction_services t
        WHERE
            t.pid = npid::bigint;
        IF ncount = 0 THEN
            CALL d_pkg_directions.del(npid, pnlpu);

        END IF;

    END IF;
    nlink := d_pkg_links.find_link_id_in(pnLPU => pnlpu,psUNIT => 'DIRECTION_SERVICES',pnUNIT_ID => pnid,psUNIT_IN => 'AGENT_PRGN_DSPLAN')::numeric;
    IF nlink IS NOT NULL THEN
        CALL d_pkg_links.del(nlink, pnlpu);

    END IF;
    --  Удаление шаблона медсестры
        IF nnurse_user_templates IS NOT NULL THEN
        CALL d_pkg_vis_user_templates.del_draft((nnurse_user_templates)::numeric, pnlpu);

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'DIRECTION_SERVICES_DELETE', pnid);
END
$procedure$
```

---

### Брокер №3: action=D_PKG_R_ZNO_RISK_DATA_REASONS.DEL

**Тип брокера:** Прямое указание функции

**Вызываемая функция:**
```
D_PKG_R_ZNO_RISK_DATA_REASONS.DEL
```

**Oracle SQL тело функции 🟠:**

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
  nVERSION              D_PKG_STD.tREF;
begin
  -- Поиск версии по ЛПУ --
  D_PKG_VERSIONS.GET_VERSION_BY_LPU(1,pnLPU,'R_ZNO_RISK_DATA_REASONS',nVERSION);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU, nVERSION, null, null, 'R_ZNO_RISK_DATA_REASONS_DELETE', pnID);
  begin
    delete D_R_ZNO_RISK_DATA_REASONS t
     where t.ID      = pnID
       and t.VERSION = nVERSION;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```

**PostgreSQL тело функции 🐘:**

```sql
CREATE OR REPLACE PROCEDURE d_pkg_r_zno_risk_data_reasons.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
BEGIN
    CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, 'R_ZNO_RISK_DATA_REASONS', nversion);
    CALL d_pkg_bpenv.beforebp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'R_ZNO_RISK_DATA_REASONS_DELETE', pnid);
    BEGIN
        DELETE FROM d_r_zno_risk_data_reasons t where t.id = pnid::bigint
     AND t.version = nversion::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'R_ZNO_RISK_DATA_REASONS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'R_ZNO_RISK_DATA_REASONS_DELETE', pnid);
END
$procedure$
```

---

### Брокер №4: action=D_PKG_R_ZNO_RISK_DATA_REASONS.ADD

**Тип брокера:** Прямое указание функции

**Вызываемая функция:**
```
D_PKG_R_ZNO_RISK_DATA_REASONS.ADD
```

**Oracle SQL тело функции 🟠:**

```sql
-- Oracle PACKAGE: ADD
--======================================================================
procedure ADD
(
  pnD_INSERT_ID                        out NUMBER,
  pnLPU                                in NUMBER,
  pnPID                                in NUMBER,          -- Данные о риске заболевания ЗНО: заявка
  pnREASON                             in NUMBER           -- Дата включения в регистр
)
is
  nVERSION              D_PKG_STD.tREF;
begin
  -- Поиск версии по ЛПУ --
  D_PKG_VERSIONS.GET_VERSION_BY_LPU(1,pnLPU,'R_ZNO_RISK_DATA_REASONS',nVERSION);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU, nVERSION, null, null, 'R_ZNO_RISK_DATA_REASONS_INSERT', null);
  begin
    insert into D_R_ZNO_RISK_DATA_REASONS
    (
      ID,
      VERSION,
      PID,
      REASON
    )
      values
    (
      D_GEN_ID,
      nVERSION,
      pnPID,
      pnREASON
    ) returning ID into pnD_INSERT_ID;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'I', sqlcode);
  end;
```

**PostgreSQL тело функции 🐘:**

```sql
CREATE OR REPLACE PROCEDURE d_pkg_r_zno_risk_data_reasons.add(INOUT pnd_insert_id numeric, IN pnlpu numeric, IN pnpid numeric, IN pnreason numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
BEGIN
    pnd_insert_id := null;
    CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, 'R_ZNO_RISK_DATA_REASONS', nversion);
    CALL d_pkg_bpenv.beforebp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'R_ZNO_RISK_DATA_REASONS_INSERT', (null)::numeric);
    BEGIN
        INSERT INTO d_r_zno_risk_data_reasons ( "id" , "version" , "pid" , "reason" ) VALUES ( d_gen_id(),nversion,pnpid,pnreason ) RETURNING id INTO pnd_insert_id;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'I',(SQLSTATE)::varchar);

    END;
    CALL d_pkg_bpenv.afterbp(pnlpu, nversion, (null)::numeric, (null)::numeric, 'R_ZNO_RISK_DATA_REASONS_INSERT', pnd_insert_id);
END
$procedure$
```


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
    id bigint  -- ID,
    se_code character varying(30)  -- Код,
    se_name character varying(1000)  -- Наименование,
    se_code_pgg character varying(100)  -- Код ПГГ,
    se_type bigint DEFAULT 0  -- Тип услуги,
    pggservice bigint  -- Услуга по ПГГ,
    se_kind bigint  -- Вид услуги,
    operkind bigint  -- Вид операции,
    version bigint  -- Версия,
    cid bigint  -- Каталог,
    is_combined numeric(1,0) DEFAULT 0  -- Ценообразование услуги (0-общее;1-комбинированное),
    taxgr bigint  -- Ставка НДС,
    se_profile bigint  -- Профиль услуги,
    vmp bigint  -- ВМП,
    uet_doctor numeric(7,2)  -- Ует врача,
    uet_nurse numeric(7,2)  -- Ует сестры,
    treatment_stage numeric(1,0)  -- Этап лечения,
    pat_restriction bigint  -- Ограничение по данным пациента,
    uet_doctor_det numeric(7,2)  -- Ует врача (детский прием),
    gen_ehr numeric(1,0)  -- Формировать ЭМЗ (1-да,null-нет),
    tmp_disp_service bigint  -- Услуга диспансеризации,
    form30_service bigint  -- Услуга по форме 30,
    fed_service bigint  -- Федеральная услуга,
    uet_dent_technician numeric(7,2)  -- Ует Зуб.техника,
    primary numeric(1,0)  -- Первичность услуги (1 - Первичная, 2 - Повторная),
    open_date timestamp(0) without time zone DEFAULT to_timestamp_simple('01.01.1900'::text, 'dd.mm.yyyy'::text)  -- Дата начала действия услуги,
    close_date timestamp(0) without time zone  -- Дата прекращения действия услуги,
    se_comment character varying(500)  -- Комментарий услуги,
    previd bigint  -- ID услуги взамен которой активирована новая,
    uet_polisher numeric(9,2)  -- УЕТ Полировщика,
    uet_caster numeric(9,2)  -- УЕТ Литейщика,
    preg_serv numeric(1,0)  -- Завершение беременности: null - проставляется по умолчанию, 0 - относится к успешному завершению беременности, 1 - относиться к прерыванию беременности,
    se_tax_code numeric(1,0)  -- Код для налогового вычета (1 - стандарт; 2 - за дорогостоящее лечение),
    epgu_service bigint  -- D_EPGU_SERVICE.ID
);
```

---

#### Таблица №2: D_SPECIALITIES

```sql
CREATE TABLE D_SPECIALITIES (
    id bigint  -- ID,
    code character varying(10)  -- Код,
    title character varying(250)  -- Наименование,
    version bigint  -- Версия,
    cid bigint  -- Каталог,
    date_begin timestamp(0) without time zone  -- Дата начала действия,
    date_end timestamp(0) without time zone  -- Дата окончания действия,
    record_period numeric(2,0)  -- Период доступности записи
);
```

---

#### Таблица №3: D_EMPLOYERS

```sql
CREATE TABLE D_EMPLOYERS (
    id bigint  -- ID,
    jobtitle bigint  -- Должность,
    regdate timestamp(0) without time zone  -- Дата регистрации,
    speciality bigint  -- Специальность,
    kod_vracha character varying(11)  -- Код врача,
    registr_kod character varying(10)  -- Регистрационный код,
    lpu bigint  -- ЛПУ,
    speciality_ed bigint  -- Специальность по образованию,
    skill_category bigint  -- Квалификационная категория,
    is_dismissed numeric(1,0) DEFAULT 0  -- Сотрудник уволен,
    dismiss_date timestamp(0) without time zone  -- Дата увольнения,
    department bigint  -- Отделение,
    sysuser bigint  -- Пользователь,
    agent bigint  -- Контрагент,
    cid bigint  -- Каталог,
    report_sign character varying(400)  -- Подпись врача в отчетах,
    emp_numb character varying(50)  -- Табельный номер,
    quot_resource bigint  -- Ресурс квотирования,
    rate numeric(7,2)  -- Занимаемое количество ставок,
    personal_card_guid character varying(36)  -- Идентификатор записи личного дела
);
```

---

#### Таблица №4: D_AGENTS

```sql
CREATE TABLE D_AGENTS (
    id bigint  -- ID,
    version bigint  -- Версия,
    cid bigint  -- Каталог,
    agn_code character varying(40)  -- Код,
    agn_name character varying(250)  -- Наименование,
    agn_type numeric(1,0)  -- Тип : 0 - юридический, 1 - физический,
    agn_inn numeric(12,0)  -- ИНН,
    agn_kpp bigint  -- КПП,
    note character varying(250)  -- Примечание,
    firstname character varying(40)  -- Имя,
    surname character varying(40)  -- Фамилия,
    lastname character varying(40)  -- Отчество,
    birthdate timestamp(0) without time zone  -- Дата рождения,
    sex numeric(1,0)  -- Пол : 0 - женский, 1 - мужской,
    okved bigint  -- Код по ОКВЕД,
    education bigint  -- Образование,
    is_employer numeric(1,0) DEFAULT 0  -- Сотрудник: 1 - да, 0 - нет,
    snils character varying(11)  -- СНИЛС,
    agn_ogrn character varying(13)  -- Код ОГРН,
    agn_okpo character varying(10)  -- Код ОКПО,
    deathdate timestamp(0) without time zone  -- Дата и время смерти,
    deathdoctype bigint  -- Тип документа о смерти,
    deathdocdate timestamp(0) without time zone  -- Дата оформления документа о смерти,
    deathdocnum character varying(20)  -- Номер документа о смерти,
    agn_okfs bigint  -- Код по ОКФС,
    enp character varying(16)  -- ЕНП,
    birthplace character varying(400)  -- Место рождения,
    nation bigint  -- Национальность,
    is_home numeric(1,0) DEFAULT 0  -- Лежачий пациент,
    gest_age_mother numeric(4,1)  -- Срок гестации матери(в неделях) при родах,
    is_anonym numeric(1,0) DEFAULT 0  -- Аноним: 0 - нет, 1 - да,
    deathplace character varying(4000)  -- Место смерти,
    full_classes numeric(2,0)  -- Количество полных классов/курсов,
    accuracy_date_death numeric(1,0)  -- Точность даты смерти: 0 - неизвестно время; 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью,
    accuracy_date_birth numeric(1,0)  -- Точность даты рождения: 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью,
    ind_enterp numeric(1,0)  -- Индивидуальный предприниматель: 0 - нет, 1 - да,
    agn_ogrn_ind character varying(15)  -- Код ОГРН ИП,
    convict_amount numeric(3,0)  -- Общее число судимостей,
    allerg_date timestamp(0) without time zone  -- Дата опроса о наличии аллергии,
    according_relatives numeric(1,0) DEFAULT 0  -- Заполнено со слов родственников: 0 - нет, 1 - да,
    birthplace_geo bigint  -- Место рождения: географическое понятие,
    webiomed_guid character varying(36)  -- Идентификатор от МИС НП для мониторинга Webiomed,
    webiomed_url character varying(2048)  -- Ссылка на результаты "Мониторинг Webiomed",
    medicbk_guid character varying(36)  -- Идентификатор для MedicBK,
    medicbk_url character varying(2048)  -- Ссылка на результаты MedicBK,
    birthplace_gar_address_id bigint  -- Место рождения : географическое понятие (ГАР),
    max_info numeric(1,0) DEFAULT 0  -- Информирование в MAX: 0 - нет, 1 - да,
    epgu numeric(1,0) DEFAULT 0  -- Признак: 1 - да, 0 - нет
);
```

---

#### Таблица №5: D_CABLAB

```sql
CREATE TABLE D_CABLAB (
    id bigint  -- ID,
    lpu bigint  -- ЛПУ,
    department bigint  -- Отделение,
    cl_code character varying(20)  -- Код,
    cl_name character varying(160)  -- Наименование,
    cid bigint  -- Каталог,
    pid bigint  -- Кабинет,
    schedule_type numeric(1,0)  -- Тип назначенного графика(поддерживается автоматически),
    division bigint  -- Подразделение,
    building bigint  -- Здание,
    floor bigint  -- Этаж,
    is_comm numeric(1,0) DEFAULT 0  -- Кабинет платных услуг,
    begin_date timestamp(0) without time zone  -- Начало функционирования кабинета платных услуг,
    end_date timestamp(0) without time zone  -- Окончание функционирования кабинета платных услуг,
    cablab_type bigint  -- Тип кабинета,
    cl_begin_date timestamp(0) without time zone  -- Дата начала действия,
    cl_end_date timestamp(0) without time zone  -- Дата окончания действия
);
```

---

#### Таблица №6: D_LPUDICT

```sql
CREATE TABLE D_LPUDICT (
    id bigint  -- ID,
    lpu_code character varying(20)  -- Код,
    lpu_fullname character varying(300)  -- Полное наименование,
    headdoct character varying(160)  -- Главврач,
    is_resp numeric(1,0) DEFAULT 0  -- Тип ЛПУ :  0 - Районное, 1 - Областное,  2-Городское,
    bookkeeper character varying(150)  -- Главбух,
    date_b timestamp(0) without time zone  -- Дата включения в справочник,
    date_e timestamp(0) without time zone  -- Дата исключения из справочника,
    priv_date_b timestamp(0) without time zone  -- Дата начала выписки льготных рецептов,
    priv_date_e timestamp(0) without time zone  -- Дата конца выписки льготных рецептов,
    has_priv_rec numeric(1,0)  -- Имеет ли право на выписку льготных рецептов: 0 - нет, 1- да,
    version bigint  -- Версия,
    lpukind bigint  -- Вид ЛПУ,
    agent bigint  -- Контрагент,
    lpu_name character varying(100)  -- Краткое наименование,
    hid bigint  -- Главное ЛПУ,
    cid bigint  -- Каталог
);
```

---

#### Таблица №7: D_LPU

```sql
CREATE TABLE D_LPU (
    id bigint  -- ID,
    fullname character varying(300)  -- Полное наименование ЛПУ,
    headdoctor_fullname character varying(160)  -- ФИО главврача,
    fulladdress character varying(160)  -- Адрес ЛПУ,
    phones character varying(80)  -- Телефоны ЛПУ,
    rec_ser_priv character varying(10)  -- Серия для выписки рецептов 148-1/у-04 на льготные медикаменты,
    rec_ser character varying(10)  -- Серия для выписки рецептов на нельготные медикаменты,
    code_lpu character varying(20)  -- Код ЛПУ,
    code_ogrn character varying(15)  -- Код ЛПУ по ОГРН,
    code_okpo character varying(10)  -- Код ЛПУ по ОКПО,
    code_okdp character varying(8)  -- Код ЛПУ по ОКДП,
    code_okonh character varying(5)  -- Код ЛПУ по ОКОНХ,
    code_okato character varying(11)  -- Код ЛПУ по ОКАТО,
    code_okogu character varying(10)  -- Код ЛПУ по ОКОГУ,
    code_ocopph character varying(5)  -- Код ЛПУ по ОКОПФ,
    code_okfs character varying(2)  -- Код ЛПУ по ОКФС,
    lpudict bigint  -- АПУ,
    bookkeeper_fullname character varying(160)  -- ФИО главбуха,
    headeconomist_fullname character varying(160)  -- ФИО главного экономиста,
    geografy bigint  -- Регион ЛПУ,
    userforms character varying(64)  -- Каталог пользовательских форм,
    gennumb_group bigint  -- Группа нумерации карт,
    exec_authority character varying(150)  -- Орган исполнительной власти субъекта РФ,
    rec_ser_priv_88 character varying(10)  -- Серия для выписки рецептов 148-1/у-88 на льготные медикаменты,
    ip_addr character varying(250)  -- Доступные IP,
    by_es_only numeric(1,0)  -- Вход в ЛПУ осуществляется только по электронной подписи: 0 - нет, 1 - да,
    website character varying(250)  -- Сайт МО,
    is_tech_lpu numeric(1,0) DEFAULT 0  -- Техническая УЗ,
    address bigint  -- Адрес ГАР
);
```

---

#### Таблица №8: D_PMC_DISP_PLAN

```sql
CREATE TABLE D_PMC_DISP_PLAN (
    id bigint  -- ID,
    pid bigint  -- Карта пациента,
    service bigint  -- Назначенная услуга,
    speciality bigint  -- Назначенная специальность врача,
    plan_date timestamp(0) without time zone  -- Дата явки по плану,
    cid bigint  -- Каталог,
    lpu bigint  -- ЛПУ,
    reg_id bigint  -- Запись в регистратуре,
    reg_code character varying(30)  -- Раздел,
    off_plan numeric(1,0) DEFAULT 0  -- Услуга добавлена вручную вне плана: 0 - нет, 1 - да,
    disp_place numeric(1,0) DEFAULT 0  -- Место проведения приема: 0 - в МО, 1 - на дому,
    control_card bigint  -- Контрольная карта,
    state bigint  -- Статус
);
```

---

#### Таблица №9: D_DIRECTION_SERVICES

```sql
CREATE TABLE D_DIRECTION_SERVICES (
    id bigint  -- ID,
    lpu bigint  -- ЛПУ,
    pid bigint  -- Направления,
    hid bigint  -- Иерархия,
    is_combined_payment numeric(1,0)  -- Используется ли комбинированная оплата : 0 - нет; 1 - да,
    is_necessary numeric(1,0)  -- Обязательна ли услуга для закрытия направления : 0-нет ; 1-да,
    service bigint  -- Услуга,
    employer_to bigint  -- Врач, которому назначена услуга,
    cablab_to bigint  -- Кабинет, которому назначена услуга,
    rec_date timestamp(0) without time zone  -- Время назначения,
    visit_purpose bigint  -- Цель посещения,
    ref_kind bigint  -- Вид обращения,
    visit_kind bigint  -- Вид посещения,
    diseasecase bigint  -- Случай заболевания,
    reg_type numeric(1,0)  -- Тип регистрации,
    serv_status numeric(1,0)  -- Статус услуги,
    is_primary numeric(1,0)  -- Услуга первичная : 0- нет; 1- да,
    s_commnet character varying(1200)  -- Комментарий,
    hh_dep bigint  -- Истоия болезни : отделения,
    rec_type numeric(1,0)  -- Тип регистратуры : 0 - врачей, 1 - услуг,
    ser_count numeric(21,2)  -- Кратность,
    time_type bigint  -- Тип интевала, на который произведена запись,
    rpid bigint  -- Направление на услугу, явл. точкой отсчета,
    irid bigint  -- Направление на услугу, инициирующее внесение результата,
    payment_kind bigint  -- Вид оплаты,
    serv_status_reason character varying(250)  -- Причина смены статуса услуги,
    quota_q bigint  -- Ограничение по квоте,
    uk_hash character varying(75)  -- Уникальный ключ записи,
    dc_diagnosis bigint  -- Диагноз случая заболевания,
    lpu_service bigint  -- Услуга ЛПУ,
    rec_duration numeric(5,0)  -- Длительность оказания в минутах,
    ticket_n numeric(5,0)  -- Номер квитка расписания,
    ticket_s character varying(75)  -- Номер,описание квитка расписания,
    rqs_limit bigint  -- Ресурс квоты записи в расписание,
    ex_system bigint  -- Источник записи,
    purchase_order bigint  -- Заказ-наряд,
    is_confirmed numeric(1,0)  -- Признак подтверждения записи на прием пациентом: 0 - не подтверждена, 1 - подтверждена, 2 - запись отменена, 3 - запись перенесена, 4 - явка, 5 - не дозвонились,
    nurse_user_templates bigint  -- Шаблон медсестры,
    confirm_date timestamp(0) without time zone  -- Дата подтверждения записи,
    conference_type numeric(1,0)  -- Выбор из дополнительного словаря - TM_TYPE - Тип консультации,
    conference character varying(200)  -- Консультация,
    employer_cancel bigint  -- Врач, отменивший направление,
    localization bigint  -- Группы локализаций,
    serv_desc character varying(2000)  -- Описание услуги,
    complid bigint  -- Ссылка на запись головной комплексной услуги,
    compstr bigint  -- Ссылка на настройку состава комплексной услуги,
    important numeric(1,0) DEFAULT 0  -- Важное: 1 - да, 0 - нет,
    patient bigint  -- Пациент,
    guid character varying(36)  -- GUID,
    attendance_state numeric(1,0) DEFAULT 0  -- Cостояние посещаемости: 0 – Не определено, 1 – Явка, 2 – Неявка
);
```

---

#### Таблица №10: D_VISITS

```sql
CREATE TABLE D_VISITS (
    id bigint  -- ID,
    pid bigint  -- Направления : услуги,
    employer bigint  -- Сотрудник,
    visit_date timestamp(0) without time zone  -- Дата посещения,
    visit_place bigint  -- Место приема,
    visit_result bigint  -- Результат визита,
    lpu bigint  -- ЛПУ,
    nurse bigint  -- Медсестра,
    helper bigint  -- Дополнительный врач (помощник),
    anesthetization numeric(2,0)  -- Тип анестезии,
    dgroup bigint  -- Группа диспансерного учета,
    prof_disease bigint  -- Проффесинальное заболевание,
    ser_count numeric(21,2) DEFAULT 1  -- Кратность услуги,
    ser_koeff numeric(8,3) DEFAULT 1  -- Коэффициент оплаты услуги,
    uet_count numeric(7,2) DEFAULT 0  -- Кол-во затраченных Условных Единиц Труда (УЕТ),
    minutes numeric(5,0)  -- Кол-во минут, затраченных на проведение услуги,
    cablab bigint  -- Кабинет,
    visit_template bigint  -- Шаблон оказания,
    ref_result bigint  -- Результат обращения,
    med_care_kind bigint  -- Вид мед. помощи,
    app_kind bigint  -- Вид аппаратуры,
    anest_kind bigint  -- Вид анестезии,
    anest_indications bigint  -- Показания к анестезии,
    is_emergency numeric(1,0)  -- Экстренное посещение : 0 - нет,1 - да,
    patient bigint  -- Пациент
);
```

---

#### Таблица №11: D_USERPROCS

```sql
CREATE TABLE D_USERPROCS (
    id bigint  -- ID,
    lpu bigint  -- LPU,
    pr_code character varying(40)  -- Код,
    pr_name character varying(250)  -- Наименование,
    pr_note character varying(500)  -- Примечание,
    pr_type numeric(1,0) DEFAULT 0  -- Тип : 0 - хранимая процедура, 1 - неименованный блок,
    pr_exec_type numeric(1,0) DEFAULT 0  -- Тип запуска : 0 - вручную, 1 - автоматический,
    storedproc character varying(61)  -- Хранимая процедура,
    nnmblock character varying(4000)  -- Неименованный блок,
    schema character varying(30)  -- Схема,
    overloadnumb bigint  -- Номер перегрузки процедуры,
    cid bigint  -- Каталог
);
```

---

#### Таблица №12: D_HIV_NR_PATIENTS

```sql
CREATE TABLE D_HIV_NR_PATIENTS (
    id bigint  -- ID,
    version bigint  -- Версия,
    pid bigint  -- РВИЧ. Случаи ВИЧ,
    nos_registr bigint  -- Нозологический регистр,
    agent bigint  -- Контрагент,
    numb_reg character varying(22)  -- Уникальный номер в регистре ВИЧ,
    create_date timestamp(0) without time zone  -- Дата включения в регистр ВИЧ,
    close_date timestamp(0) without time zone  -- Дата исключения из регистра ВИЧ,
    create_emp bigint  -- Сотрудник, включивший пациента в регистр,
    close_emp bigint  -- Сотрудник, исключивший пациента из регистра,
    remove_reason bigint  -- Причина исключения из регистра,
    dropped_out bigint  -- Тип учреждения куда выбыл,
    dropped_out_org bigint  -- Учреждение выбытия,
    is_loaded numeric(1,0) DEFAULT 0  -- Признак загруженной записи, 0 - не загружен, 1 - загружен, 2 - импортирован,
    last_date_export timestamp(0) without time zone  -- Дата последней выгрузки,
    last_change_date timestamp(0) without time zone DEFAULT sysdate()  -- Дата время последнего изменения записи,
    category bigint  -- Категория контингента,
    pat_numb character varying(17)  -- № карты в дисп.отделе
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
    ID NUMBER(17) NOT NULL  -- ID,
    SE_CODE VARCHAR2(30) NOT NULL  -- Код,
    SE_NAME VARCHAR2(1000) NOT NULL  -- Наименование,
    SE_CODE_PGG VARCHAR2(100)  -- Код ПГГ,
    SE_TYPE NUMBER(17) NOT NULL  -- Тип услуги,
    PGGSERVICE NUMBER(17)  -- Услуга по ПГГ,
    SE_KIND NUMBER(17)  -- Вид услуги,
    OPERKIND NUMBER(17)  -- Вид операции,
    VERSION NUMBER(17) NOT NULL  -- Версия,
    CID NUMBER(17) NOT NULL  -- Каталог,
    IS_COMBINED NUMBER(1) NOT NULL  -- Ценообразование услуги (0-общее;1-комбинированное),
    TAXGR NUMBER(17) NOT NULL  -- Ставка НДС,
    SE_PROFILE NUMBER(17)  -- Профиль услуги,
    VMP NUMBER(17)  -- ВМП,
    UET_DOCTOR NUMBER(5,2)  -- Ует врача,
    UET_NURSE NUMBER(5,2)  -- Ует сестры,
    TREATMENT_STAGE NUMBER(1)  -- Этап лечения,
    PAT_RESTRICTION NUMBER(17)  -- Ограничение по данным пациента,
    UET_DOCTOR_DET NUMBER(5,2)  -- Ует врача (детский прием),
    GEN_EHR NUMBER(1)  -- Формировать ЭМЗ (1-да,null-нет),
    TMP_DISP_SERVICE NUMBER(17)  -- Услуга диспансеризации,
    FORM30_SERVICE NUMBER(17)  -- Услуга по форме 30,
    FED_SERVICE NUMBER(17)  -- Федеральная услуга,
    UET_DENT_TECHNICIAN NUMBER(5,2)  -- Ует Зуб.техника,
    PRIMARY NUMBER(1)  -- Первичность услуги (1 - Первичная, 2 - Повторная),
    OPEN_DATE DATE NOT NULL  -- Дата начала действия услуги,
    CLOSE_DATE DATE  -- Дата прекращения действия услуги,
    SE_COMMENT VARCHAR2(500)  -- Комментарий услуги,
    PREVID NUMBER(17)  -- ID услуги взамен которой активирована новая,
    UET_POLISHER NUMBER(7,2)  -- УЕТ Полировщика,
    UET_CASTER NUMBER(7,2)  -- УЕТ Литейщика,
    PREG_SERV NUMBER(1)  -- Завершение беременности: null - проставляется по умолчанию, 0 - относится к успешному завершению беременности, 1 - относиться к прерыванию беременности,
    SE_TAX_CODE NUMBER(1)  -- Код для налогового вычета (1 - стандарт; 2 - за дорогостоящее лечение),
    EPGU_SERVICE NUMBER(17)  -- D_EPGU_SERVICE.ID,
    CONSTRAINT PK_D_SERVICES PRIMARY KEY (ID)
);

ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_DSE FOREIGN KEY (TMP_DISP_SERVICE) REFERENCES D_DISP_SERVICES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_EPGU_SERV FOREIGN KEY (EPGU_SERVICE) REFERENCES D_EPGU_SERVICE(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_FRM30 FOREIGN KEY (FORM30_SERVICE) REFERENCES D_FORM30_SERVICES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_FS FOREIGN KEY (FED_SERVICE) REFERENCES D_FED_SERVICES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_OK FOREIGN KEY (OPERKIND) REFERENCES D_OPERKINDS(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_PGG FOREIGN KEY (PGGSERVICE) REFERENCES D_SERVICES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_PRID FOREIGN KEY (PREVID) REFERENCES D_SERVICES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_PRO FOREIGN KEY (SE_PROFILE) REFERENCES D_SERVPROFILES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_PRS FOREIGN KEY (PAT_RESTRICTION) REFERENCES D_PAT_RESTRICTIONS(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_SK FOREIGN KEY (SE_KIND) REFERENCES D_SERVKINDS(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_ST FOREIGN KEY (SE_TYPE) REFERENCES D_SERVTYPES(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_STG FOREIGN KEY (TREATMENT_STAGE) REFERENCES D_TREATMENT_STAGES(TS_CODE);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_TG FOREIGN KEY (TAXGR) REFERENCES D_TAXGR(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_VER FOREIGN KEY (VERSION) REFERENCES D_VERSIONS(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT FK_D_SERVICES_VMP FOREIGN KEY (VMP) REFERENCES D_VMP(ID);
ALTER TABLE D_SERVICES ADD CONSTRAINT UK_D_SERVICES_OD(SE_CODE);
ALTER TABLE D_SERVICES ADD CONSTRAINT UK_D_SERVICES_OD(VERSION);
ALTER TABLE D_SERVICES ADD CONSTRAINT UK_D_SERVICES_OD(OPEN_DATE);
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_CD CHECK (CLOSE_DATE is null or CLOSE_DATE > OPEN_DATE);
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_GEHR CHECK (GEN_EHR is null or GEN_EHR = 1);
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_IC CHECK (IS_COMBINED in (0,1));
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_ICT CHECK (SE_TYPE = 1 and IS_COMBINED = 0 or SE_TYPE != 1);
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_PRIMARY CHECK (PRIMARY is null or PRIMARY in (1,2));
ALTER TABLE D_SERVICES ADD CONSTRAINT CH_D_SERVICES_TRIM CHECK (SE_CODE = trim(SE_CODE) and SE_NAME = trim(SE_NAME) and SE_CODE_PGG = trim(SE_CODE_PGG));
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207605 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207606 CHECK ("SE_CODE" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207607 CHECK ("SE_NAME" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207608 CHECK ("SE_TYPE" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207609 CHECK ("VERSION" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207610 CHECK ("CID" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207611 CHECK ("IS_COMBINED" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00207612 CHECK ("TAXGR" IS NOT NULL);
ALTER TABLE D_SERVICES ADD CONSTRAINT SYS_C00238620 CHECK ("OPEN_DATE" IS NOT NULL);
```

---

#### Таблица №2: D_SPECIALITIES

```sql
CREATE TABLE D_SPECIALITIES (
    ID NUMBER(17) NOT NULL  -- ID,
    CODE VARCHAR2(10) NOT NULL  -- Код,
    TITLE VARCHAR2(250) NOT NULL  -- Наименование,
    VERSION NUMBER(17) NOT NULL  -- Версия,
    CID NUMBER(17) NOT NULL  -- Каталог,
    DATE_BEGIN DATE NOT NULL  -- Дата начала действия,
    DATE_END DATE  -- Дата окончания действия,
    RECORD_PERIOD NUMBER(2)  -- Период доступности записи,
    CONSTRAINT PK_D_SPECIALITIES PRIMARY KEY (ID)
);

ALTER TABLE D_SPECIALITIES ADD CONSTRAINT FK_D_SPECIALITIES_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT FK_D_SPECIALITIES_VER FOREIGN KEY (VERSION) REFERENCES D_VERSIONS(ID);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT UK_D_SPECIALITIES(CODE);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT UK_D_SPECIALITIES(VERSION);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT CH_D_SPECIALITIES_DB CHECK (DATE_BEGIN = trunc(DATE_BEGIN));
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT CH_D_SPECIALITIES_DBE CHECK (DATE_END is null or DATE_END >= DATE_BEGIN);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT CH_D_SPECIALITIES_DE CHECK (DATE_END is null or DATE_END = trunc(DATE_END));
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207486 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207487 CHECK ("CODE" IS NOT NULL);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207488 CHECK ("TITLE" IS NOT NULL);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207489 CHECK ("VERSION" IS NOT NULL);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207490 CHECK ("CID" IS NOT NULL);
ALTER TABLE D_SPECIALITIES ADD CONSTRAINT SYS_C00207491 CHECK ("DATE_BEGIN" IS NOT NULL);
```

---

#### Таблица №3: D_EMPLOYERS

```sql
CREATE TABLE D_EMPLOYERS (
    ID NUMBER(17) NOT NULL  -- ID,
    JOBTITLE NUMBER(17)  -- Должность,
    REGDATE DATE NOT NULL  -- Дата регистрации,
    SPECIALITY NUMBER(17)  -- Специальность,
    KOD_VRACHA VARCHAR2(11)  -- Код врача,
    REGISTR_KOD VARCHAR2(10)  -- Регистрационный код,
    LPU NUMBER(17) NOT NULL  -- ЛПУ,
    SPECIALITY_ED NUMBER(17)  -- Специальность по образованию,
    SKILL_CATEGORY NUMBER(17)  -- Квалификационная категория,
    IS_DISMISSED NUMBER(1) NOT NULL  -- Сотрудник уволен,
    DISMISS_DATE DATE  -- Дата увольнения,
    DEPARTMENT NUMBER(17)  -- Отделение,
    SYSUSER NUMBER(17)  -- Пользователь,
    AGENT NUMBER(17) NOT NULL  -- Контрагент,
    CID NUMBER(17) NOT NULL  -- Каталог,
    REPORT_SIGN VARCHAR2(400)  -- Подпись врача в отчетах,
    EMP_NUMB VARCHAR2(50)  -- Табельный номер,
    QUOT_RESOURCE NUMBER(17)  -- Ресурс квотирования,
    RATE NUMBER(5,2)  -- Занимаемое количество ставок,
    PERSONAL_CARD_GUID VARCHAR2(36)  -- Идентификатор записи личного дела,
    CONSTRAINT PK_D_EMPLOYERS PRIMARY KEY (ID)
);

ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_AG FOREIGN KEY (AGENT) REFERENCES D_AGENTS(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_DP FOREIGN KEY (DEPARTMENT) REFERENCES D_DEPS(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_JT FOREIGN KEY (JOBTITLE) REFERENCES D_JOBTITLES(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_LP FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_QR FOREIGN KEY (QUOT_RESOURCE) REFERENCES D_QUOTING_RESOURCES(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_SC FOREIGN KEY (SKILL_CATEGORY) REFERENCES D_SKILL_CATEGORIES(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_SE FOREIGN KEY (SPECIALITY_ED) REFERENCES D_SPECIALITIES_ED(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_SP FOREIGN KEY (SPECIALITY) REFERENCES D_SPECIALITIES(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT FK_D_EMPLOYERS_US FOREIGN KEY (SYSUSER) REFERENCES D_USERS(ID);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT CH_D_EMPLOYERS_DT CHECK (IS_DISMISSED = 0 and DISMISS_DATE is null or IS_DISMISSED = 1 and DISMISS_DATE is not null);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT CH_D_EMPLOYERS_DT_RD CHECK (DISMISS_DATE >= REGDATE or DISMISS_DATE is null);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT CH_D_EMPLOYERS_IS_DISMISSED CHECK (IS_DISMISSED in (0,1));
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209591 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209592 CHECK ("REGDATE" IS NOT NULL);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209593 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209594 CHECK ("IS_DISMISSED" IS NOT NULL);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209595 CHECK ("AGENT" IS NOT NULL);
ALTER TABLE D_EMPLOYERS ADD CONSTRAINT SYS_C00209596 CHECK ("CID" IS NOT NULL);
```

---

#### Таблица №4: D_AGENTS

```sql
CREATE TABLE D_AGENTS (
    ID NUMBER(17) NOT NULL  -- ID,
    VERSION NUMBER(17) NOT NULL  -- Версия,
    CID NUMBER(17) NOT NULL  -- Каталог,
    AGN_CODE VARCHAR2(40) NOT NULL  -- Код,
    AGN_NAME VARCHAR2(250) NOT NULL  -- Наименование,
    AGN_TYPE NUMBER(1) NOT NULL  -- Тип : 0 - юридический, 1 - физический,
    AGN_INN NUMBER(12)  -- ИНН,
    AGN_KPP NUMBER(17)  -- КПП,
    NOTE VARCHAR2(250)  -- Примечание,
    FIRSTNAME VARCHAR2(40)  -- Имя,
    SURNAME VARCHAR2(40)  -- Фамилия,
    LASTNAME VARCHAR2(40)  -- Отчество,
    BIRTHDATE DATE  -- Дата рождения,
    SEX NUMBER(1)  -- Пол : 0 - женский, 1 - мужской,
    OKVED NUMBER(17)  -- Код по ОКВЕД,
    EDUCATION NUMBER(17)  -- Образование,
    IS_EMPLOYER NUMBER(1) NOT NULL  -- Сотрудник: 1 - да, 0 - нет,
    SNILS VARCHAR2(11)  -- СНИЛС,
    AGN_OGRN VARCHAR2(13)  -- Код ОГРН,
    AGN_OKPO VARCHAR2(10)  -- Код ОКПО,
    DEATHDATE DATE  -- Дата и время смерти,
    DEATHDOCTYPE NUMBER(17)  -- Тип документа о смерти,
    DEATHDOCDATE DATE  -- Дата оформления документа о смерти,
    DEATHDOCNUM VARCHAR2(20)  -- Номер документа о смерти,
    AGN_OKFS NUMBER(17)  -- Код по ОКФС,
    ENP VARCHAR2(16)  -- ЕНП,
    BIRTHPLACE VARCHAR2(400)  -- Место рождения,
    NATION NUMBER(17)  -- Национальность,
    IS_HOME NUMBER(1) NOT NULL  -- Лежачий пациент,
    GEST_AGE_MOTHER NUMBER(3,1)  -- Срок гестации матери(в неделях) при родах,
    IS_ANONYM NUMBER(1) NOT NULL  -- Аноним: 0 - нет, 1 - да,
    DEATHPLACE VARCHAR2(4000)  -- Место смерти,
    FULL_CLASSES NUMBER(2)  -- Количество полных классов/курсов,
    ACCURACY_DATE_DEATH NUMBER(1)  -- Точность даты смерти: 0 - неизвестно время; 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью,
    ACCURACY_DATE_BIRTH NUMBER(1)  -- Точность даты рождения: 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью,
    IND_ENTERP NUMBER(1)  -- Индивидуальный предприниматель: 0 - нет, 1 - да,
    AGN_OGRN_IND VARCHAR2(15)  -- Код ОГРН ИП,
    CONVICT_AMOUNT NUMBER(3)  -- Общее число судимостей,
    ALLERG_DATE DATE  -- Дата опроса о наличии аллергии,
    ACCORDING_RELATIVES NUMBER(1) NOT NULL  -- Заполнено со слов родственников: 0 - нет, 1 - да,
    BIRTHPLACE_GEO NUMBER(17)  -- Место рождения: географическое понятие,
    WEBIOMED_GUID VARCHAR2(36)  -- Идентификатор от МИС НП для мониторинга Webiomed,
    WEBIOMED_URL VARCHAR2(2048)  -- Ссылка на результаты "Мониторинг Webiomed",
    MEDICBK_GUID VARCHAR2(36)  -- Идентификатор для MedicBK,
    MEDICBK_URL VARCHAR2(2048)  -- Ссылка на результаты MedicBK,
    BIRTHPLACE_GAR_ADDRESS_ID NUMBER(17)  -- Место рождения : географическое понятие (ГАР),
    MAX_INFO NUMBER(1)  -- Информирование в MAX: 0 - нет, 1 - да,
    EPGU NUMBER(1)  -- Признак: 1 - да, 0 - нет,
    CONSTRAINT PK_D_AGENTS PRIMARY KEY (ID)
);

ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_BIRTHPLACE_GAR FOREIGN KEY (BIRTHPLACE_GAR_ADDRESS_ID) REFERENCES D_ADDRESS(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_BIRTHPLACE_GEO FOREIGN KEY (BIRTHPLACE_GEO) REFERENCES D_GEOGRAFY(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_DEATHDOCTYPE FOREIGN KEY (DEATHDOCTYPE) REFERENCES D_DOC_TYPES(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_EDU FOREIGN KEY (EDUCATION) REFERENCES D_EDUCATION_TYPES(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_NAT FOREIGN KEY (NATION) REFERENCES D_FED_NATIONALITIES(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_OKFS FOREIGN KEY (AGN_OKFS) REFERENCES D_OKFS(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_OKV FOREIGN KEY (OKVED) REFERENCES D_OKVED(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT FK_D_AGENTS_VER FOREIGN KEY (VERSION) REFERENCES D_VERSIONS(ID);
ALTER TABLE D_AGENTS ADD CONSTRAINT UK_D_AGENTS_CODE(VERSION);
ALTER TABLE D_AGENTS ADD CONSTRAINT UK_D_AGENTS_CODE(AGN_CODE);
ALTER TABLE D_AGENTS ADD CONSTRAINT UK_D_AGENTS_NAME(VERSION);
ALTER TABLE D_AGENTS ADD CONSTRAINT UK_D_AGENTS_NAME(AGN_NAME);
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_ACCORDING_REL CHECK (ACCORDING_RELATIVES in (0, 1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_ACCURACY_DATE_B CHECK (ACCURACY_DATE_BIRTH is null or ACCURACY_DATE_BIRTH in (1,2,3));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_ACCURACY_DATE_D CHECK (ACCURACY_DATE_DEATH is null or ACCURACY_DATE_DEATH in (0,1,2,3));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_AT CHECK (AGN_TYPE in (0,1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_DD CHECK (AGN_TYPE = 0 and BIRTHDATE is null and DEATHDATE is null or AGN_TYPE = 1 and (nvl(BIRTHDATE,DEATHDATE) <= nvl(DEATHDATE,BIRTHDATE) or BIRTHDATE is null and DEATHDATE is null));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_DEATH_DOC CHECK ((DEATHDATE is null and DEATHDOCTYPE is null and DEATHDOCDATE is null and DEATHDOCNUM is null) or DEATHDATE is not null or (DEATHDATE is null and DEATHDOCTYPE is not null and DEATHDOCDATE is not null and DEATHDOCNUM is not null));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_E CHECK (EPGU in (0, 1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_ENP CHECK (LENGTHB(ENP) = 16 or ENP is null);
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_FNAME CHECK ((FIRSTNAME is not null and FIRSTNAME = trim(FIRSTNAME) and regexp_like(FIRSTNAME, '^[а-яА-Я0-9IV ,''.()-]+$')) or FIRSTNAME is null);
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_IND_ENTERP CHECK ((AGN_TYPE = 1 and IND_ENTERP is null) or (AGN_TYPE = 0 and IND_ENTERP in (0,1)));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_IS_ANONYM CHECK (IS_ANONYM in (0,1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_IS_EMP CHECK (IS_EMPLOYER in (0,1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_IS_HOME CHECK (IS_HOME in (0,1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_JUR CHECK (AGN_TYPE = 1 and AGN_OGRN is null and AGN_OKPO is null and OKVED is null and AGN_OKFS is null or AGN_TYPE = 0);
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_LNAME CHECK ((LASTNAME is not null and LASTNAME = trim(LASTNAME) and regexp_like(LASTNAME, '^[а-яА-Я0-9IV ,''.()-]+$')) or LASTNAME is null);
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_MAX CHECK (MAX_INFO in (0, 1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_SEX CHECK (SEX in (0,1));
ALTER TABLE D_AGENTS ADD CONSTRAINT CH_D_AGENTS_SNAME CHECK ((SURNAME is not null and SURNAME = trim(SURNAME) and regexp_like(SURNAME, '^[а-яА-Я0-9IV ,''.()-]+$')) or SURNAME is null);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210631 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210632 CHECK ("VERSION" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210633 CHECK ("CID" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210634 CHECK ("AGN_CODE" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210635 CHECK ("AGN_NAME" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210636 CHECK ("AGN_TYPE" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210637 CHECK ("IS_EMPLOYER" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210638 CHECK ("IS_HOME" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00210639 CHECK ("IS_ANONYM" IS NOT NULL);
ALTER TABLE D_AGENTS ADD CONSTRAINT SYS_C00252437 CHECK ("ACCORDING_RELATIVES" IS NOT NULL);
```

---

#### Таблица №5: D_CABLAB

```sql
CREATE TABLE D_CABLAB (
    ID NUMBER(17) NOT NULL  -- ID,
    LPU NUMBER(17) NOT NULL  -- ЛПУ,
    DEPARTMENT NUMBER(17) NOT NULL  -- Отделение,
    CL_CODE VARCHAR2(20) NOT NULL  -- Код,
    CL_NAME VARCHAR2(160) NOT NULL  -- Наименование,
    CID NUMBER(17) NOT NULL  -- Каталог,
    PID NUMBER(17)  -- Кабинет,
    SCHEDULE_TYPE NUMBER(1)  -- Тип назначенного графика(поддерживается автоматически),
    DIVISION NUMBER(17) NOT NULL  -- Подразделение,
    BUILDING NUMBER(17)  -- Здание,
    FLOOR NUMBER(17)  -- Этаж,
    IS_COMM NUMBER(1) NOT NULL  -- Кабинет платных услуг,
    BEGIN_DATE DATE  -- Начало функционирования кабинета платных услуг,
    END_DATE DATE  -- Окончание функционирования кабинета платных услуг,
    CABLAB_TYPE NUMBER(17)  -- Тип кабинета,
    CL_BEGIN_DATE DATE NOT NULL  -- Дата начала действия,
    CL_END_DATE DATE  -- Дата окончания действия,
    CONSTRAINT PK_D_CABLAB PRIMARY KEY (ID)
);

ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_BUILDING FOREIGN KEY (BUILDING) REFERENCES D_BUILDINGS(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_CLTP FOREIGN KEY (CABLAB_TYPE) REFERENCES D_CABLAB_TYPE(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_DEP FOREIGN KEY (DEPARTMENT) REFERENCES D_DEPS(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_DIV FOREIGN KEY (DIVISION) REFERENCES D_DIVISIONS(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_FLOOR FOREIGN KEY (FLOOR) REFERENCES D_BUILD_FLOORS(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_LPU FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT FK_D_CABLAB_PID FOREIGN KEY (PID) REFERENCES D_CABLAB(ID);
ALTER TABLE D_CABLAB ADD CONSTRAINT UK_D_CABLAB_CODE(LPU);
ALTER TABLE D_CABLAB ADD CONSTRAINT UK_D_CABLAB_CODE(PID);
ALTER TABLE D_CABLAB ADD CONSTRAINT UK_D_CABLAB_CODE(CL_CODE);
ALTER TABLE D_CABLAB ADD CONSTRAINT CH_D_CABLAB_CLDATES CHECK (CL_BEGIN_DATE <= CL_END_DATE or CL_END_DATE is null);
ALTER TABLE D_CABLAB ADD CONSTRAINT CH_D_CABLAB_COMM_DATE CHECK (IS_COMM = 0 and BEGIN_DATE is null and END_DATE is null or IS_COMM = 1 and BEGIN_DATE is not null);
ALTER TABLE D_CABLAB ADD CONSTRAINT CH_D_CABLAB_DATE_OVER CHECK (END_DATE is null or BEGIN_DATE is not null and END_DATE >= BEGIN_DATE);
ALTER TABLE D_CABLAB ADD CONSTRAINT CH_D_CABLAB_IS_COMM CHECK (IS_COMM in (0,1));
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210282 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210283 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210284 CHECK ("DEPARTMENT" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210285 CHECK ("CL_CODE" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210286 CHECK ("CL_NAME" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210287 CHECK ("CID" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210288 CHECK ("DIVISION" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210289 CHECK ("IS_COMM" IS NOT NULL);
ALTER TABLE D_CABLAB ADD CONSTRAINT SYS_C00210290 CHECK ("CL_BEGIN_DATE" IS NOT NULL);
```

---

#### Таблица №6: D_LPUDICT

```sql
CREATE TABLE D_LPUDICT (
    ID NUMBER(17) NOT NULL  -- ID,
    LPU_CODE VARCHAR2(20) NOT NULL  -- Код,
    LPU_FULLNAME VARCHAR2(300)  -- Полное наименование,
    HEADDOCT VARCHAR2(160)  -- Главврач,
    IS_RESP NUMBER(1) NOT NULL  -- Тип ЛПУ :  0 - Районное, 1 - Областное,  2-Городское,
    BOOKKEEPER VARCHAR2(150)  -- Главбух,
    DATE_B DATE  -- Дата включения в справочник,
    DATE_E DATE  -- Дата исключения из справочника,
    PRIV_DATE_B DATE  -- Дата начала выписки льготных рецептов,
    PRIV_DATE_E DATE  -- Дата конца выписки льготных рецептов,
    HAS_PRIV_REC NUMBER(1)  -- Имеет ли право на выписку льготных рецептов: 0 - нет, 1- да,
    VERSION NUMBER(17) NOT NULL  -- Версия,
    LPUKIND NUMBER(17)  -- Вид ЛПУ,
    AGENT NUMBER(17)  -- Контрагент,
    LPU_NAME VARCHAR2(100)  -- Краткое наименование,
    HID NUMBER(17)  -- Главное ЛПУ,
    CID NUMBER(17) NOT NULL  -- Каталог,
    CONSTRAINT PK_D_LPUDICT PRIMARY KEY (ID)
);

ALTER TABLE D_LPUDICT ADD CONSTRAINT FK_D_LPUDICT_AG FOREIGN KEY (AGENT) REFERENCES D_AGENTS(ID);
ALTER TABLE D_LPUDICT ADD CONSTRAINT FK_D_LPUDICT_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_LPUDICT ADD CONSTRAINT FK_D_LPUDICT_HID FOREIGN KEY (HID) REFERENCES D_LPUDICT(ID);
ALTER TABLE D_LPUDICT ADD CONSTRAINT FK_D_LPUDICT_LK FOREIGN KEY (LPUKIND) REFERENCES D_LPUKINDS(ID);
ALTER TABLE D_LPUDICT ADD CONSTRAINT FK_D_LPUDICT_VER FOREIGN KEY (VERSION) REFERENCES D_VERSIONS(ID);
ALTER TABLE D_LPUDICT ADD CONSTRAINT UK_D_LPUDICT_AGN(AGENT);
ALTER TABLE D_LPUDICT ADD CONSTRAINT UK_D_LPUDICT_CODE(VERSION);
ALTER TABLE D_LPUDICT ADD CONSTRAINT UK_D_LPUDICT_CODE(LPU_CODE);
ALTER TABLE D_LPUDICT ADD CONSTRAINT CH_D_LPUDICT_AG CHECK ((HID is null and AGENT is not null) or (HID is not null));
ALTER TABLE D_LPUDICT ADD CONSTRAINT CH_D_LPUDICT_HPR CHECK (HAS_PRIV_REC in (0,1));
ALTER TABLE D_LPUDICT ADD CONSTRAINT CH_D_LPUDICT_ISR CHECK (IS_RESP in (0,1,2));
ALTER TABLE D_LPUDICT ADD CONSTRAINT CH_D_LPUDICT_PRD CHECK ((HAS_PRIV_REC = 1 and PRIV_DATE_B is not null) or (HAS_PRIV_REC = 0 and PRIV_DATE_B is null and PRIV_DATE_E is null));
ALTER TABLE D_LPUDICT ADD CONSTRAINT SYS_C00208875 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_LPUDICT ADD CONSTRAINT SYS_C00208876 CHECK ("LPU_CODE" IS NOT NULL);
ALTER TABLE D_LPUDICT ADD CONSTRAINT SYS_C00208877 CHECK ("IS_RESP" IS NOT NULL);
ALTER TABLE D_LPUDICT ADD CONSTRAINT SYS_C00208878 CHECK ("VERSION" IS NOT NULL);
ALTER TABLE D_LPUDICT ADD CONSTRAINT SYS_C00208879 CHECK ("CID" IS NOT NULL);
```

---

#### Таблица №7: D_LPU

```sql
CREATE TABLE D_LPU (
    ID NUMBER(17) NOT NULL  -- ID,
    FULLNAME VARCHAR2(300) NOT NULL  -- Полное наименование ЛПУ,
    HEADDOCTOR_FULLNAME VARCHAR2(160)  -- ФИО главврача,
    FULLADDRESS VARCHAR2(160)  -- Адрес ЛПУ,
    PHONES VARCHAR2(80)  -- Телефоны ЛПУ,
    REC_SER_PRIV VARCHAR2(10)  -- Серия для выписки рецептов 148-1/у-04 на льготные медикаменты,
    REC_SER VARCHAR2(10)  -- Серия для выписки рецептов на нельготные медикаменты,
    CODE_LPU VARCHAR2(20)  -- Код ЛПУ,
    CODE_OGRN VARCHAR2(15)  -- Код ЛПУ по ОГРН,
    CODE_OKPO VARCHAR2(10)  -- Код ЛПУ по ОКПО,
    CODE_OKDP VARCHAR2(8)  -- Код ЛПУ по ОКДП,
    CODE_OKONH VARCHAR2(5)  -- Код ЛПУ по ОКОНХ,
    CODE_OKATO VARCHAR2(11)  -- Код ЛПУ по ОКАТО,
    CODE_OKOGU VARCHAR2(10)  -- Код ЛПУ по ОКОГУ,
    CODE_OCOPPH VARCHAR2(5)  -- Код ЛПУ по ОКОПФ,
    CODE_OKFS VARCHAR2(2)  -- Код ЛПУ по ОКФС,
    LPUDICT NUMBER(17)  -- АПУ,
    BOOKKEEPER_FULLNAME VARCHAR2(160)  -- ФИО главбуха,
    HEADECONOMIST_FULLNAME VARCHAR2(160)  -- ФИО главного экономиста,
    GEOGRAFY NUMBER(17)  -- Регион ЛПУ,
    USERFORMS VARCHAR2(64)  -- Каталог пользовательских форм,
    GENNUMB_GROUP NUMBER(17)  -- Группа нумерации карт,
    EXEC_AUTHORITY VARCHAR2(150)  -- Орган исполнительной власти субъекта РФ,
    REC_SER_PRIV_88 VARCHAR2(10)  -- Серия для выписки рецептов 148-1/у-88 на льготные медикаменты,
    IP_ADDR VARCHAR2(250)  -- Доступные IP,
    BY_ES_ONLY NUMBER(1)  -- Вход в ЛПУ осуществляется только по электронной подписи: 0 - нет, 1 - да,
    WEBSITE VARCHAR2(250)  -- Сайт МО,
    IS_TECH_LPU NUMBER(1) NOT NULL  -- Техническая УЗ,
    ADDRESS NUMBER(17)  -- Адрес ГАР,
    CONSTRAINT PK_D_LPU PRIMARY KEY (ID)
);

ALTER TABLE D_LPU ADD CONSTRAINT FK_D_LPU_ADDRESS FOREIGN KEY (ADDRESS) REFERENCES D_ADDRESS(ID);
ALTER TABLE D_LPU ADD CONSTRAINT FK_D_LPU_GEO FOREIGN KEY (GEOGRAFY) REFERENCES D_GEOGRAFY(ID);
ALTER TABLE D_LPU ADD CONSTRAINT FK_D_LPU_LD FOREIGN KEY (LPUDICT) REFERENCES D_LPUDICT(ID);
ALTER TABLE D_LPU ADD CONSTRAINT UK_D_LPU_CODE_LPU(CODE_LPU);
ALTER TABLE D_LPU ADD CONSTRAINT CH_D_LPU_ES CHECK (BY_ES_ONLY in (0,1));
ALTER TABLE D_LPU ADD CONSTRAINT SYS_C00208880 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_LPU ADD CONSTRAINT SYS_C00208881 CHECK ("FULLNAME" IS NOT NULL);
ALTER TABLE D_LPU ADD CONSTRAINT SYS_C00261284 CHECK ("IS_TECH_LPU" IS NOT NULL);
```

---

#### Таблица №8: D_PMC_DISP_PLAN

```sql
CREATE TABLE D_PMC_DISP_PLAN (
    ID NUMBER(17) NOT NULL  -- ID,
    PID NUMBER(17) NOT NULL  -- Карта пациента,
    SERVICE NUMBER(17)  -- Назначенная услуга,
    SPECIALITY NUMBER(17)  -- Назначенная специальность врача,
    PLAN_DATE DATE NOT NULL  -- Дата явки по плану,
    CID NUMBER(17) NOT NULL  -- Каталог,
    LPU NUMBER(17) NOT NULL  -- ЛПУ,
    REG_ID NUMBER(17)  -- Запись в регистратуре,
    REG_CODE VARCHAR2(30)  -- Раздел,
    OFF_PLAN NUMBER(1)  -- Услуга добавлена вручную вне плана: 0 - нет, 1 - да,
    DISP_PLACE NUMBER(1)  -- Место проведения приема: 0 - в МО, 1 - на дому,
    CONTROL_CARD NUMBER(17) NOT NULL  -- Контрольная карта,
    STATE NUMBER(17)  -- Статус,
    CONSTRAINT PK_D_PMC_DISP_PLAN PRIMARY KEY (ID)
);

ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_CC FOREIGN KEY (CONTROL_CARD) REFERENCES D_CONTROL_CARD(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_LPU FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_PID FOREIGN KEY (PID) REFERENCES D_PERSMEDCARD(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_RC FOREIGN KEY (REG_CODE) REFERENCES D_UNITLIST(UNITCODE);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_SPEC FOREIGN KEY (SPECIALITY) REFERENCES D_SPECIALITIES(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_SRV FOREIGN KEY (SERVICE) REFERENCES D_SERVICES(ID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT FK_D_PMC_DISP_PLAN_STATE FOREIGN KEY (STATE) REFERENCES D_PC_SERV_STATES(ST_CODE);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(LPU);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(PID);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(PLAN_DATE);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(SERVICE);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(SPECIALITY);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT UK_D_PMC_DISP_PLAN(CONTROL_CARD);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT CH_D_PMC_DISP_PLAN_DISP_PLACE CHECK (DISP_PLACE in (0,1));
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00208193 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00208194 CHECK ("PID" IS NOT NULL);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00208195 CHECK ("PLAN_DATE" IS NOT NULL);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00208196 CHECK ("CID" IS NOT NULL);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00208197 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_PMC_DISP_PLAN ADD CONSTRAINT SYS_C00702855 CHECK ("CONTROL_CARD" IS NOT NULL);
```

---

#### Таблица №9: D_DIRECTION_SERVICES

```sql
CREATE TABLE D_DIRECTION_SERVICES (
    ID NUMBER(17) NOT NULL  -- ID,
    LPU NUMBER(17) NOT NULL  -- ЛПУ,
    PID NUMBER(17) NOT NULL  -- Направления,
    HID NUMBER(17)  -- Иерархия,
    IS_COMBINED_PAYMENT NUMBER(1) NOT NULL  -- Используется ли комбинированная оплата : 0 - нет; 1 - да,
    IS_NECESSARY NUMBER(1) NOT NULL  -- Обязательна ли услуга для закрытия направления : 0-нет ; 1-да,
    SERVICE NUMBER(17) NOT NULL  -- Услуга,
    EMPLOYER_TO NUMBER(17)  -- Врач, которому назначена услуга,
    CABLAB_TO NUMBER(17)  -- Кабинет, которому назначена услуга,
    REC_DATE DATE  -- Время назначения,
    VISIT_PURPOSE NUMBER(17)  -- Цель посещения,
    REF_KIND NUMBER(17)  -- Вид обращения,
    VISIT_KIND NUMBER(17)  -- Вид посещения,
    DISEASECASE NUMBER(17)  -- Случай заболевания,
    REG_TYPE NUMBER(1) NOT NULL  -- Тип регистрации,
    SERV_STATUS NUMBER(1) NOT NULL  -- Статус услуги,
    IS_PRIMARY NUMBER(1) NOT NULL  -- Услуга первичная : 0- нет; 1- да,
    S_COMMNET VARCHAR2(1200)  -- Комментарий,
    HH_DEP NUMBER(17)  -- Истоия болезни : отделения,
    REC_TYPE NUMBER(1)  -- Тип регистратуры : 0 - врачей, 1 - услуг,
    SER_COUNT NUMBER(19,2) NOT NULL  -- Кратность,
    TIME_TYPE NUMBER(17)  -- Тип интевала, на который произведена запись,
    RPID NUMBER(17)  -- Направление на услугу, явл. точкой отсчета,
    IRID NUMBER(17)  -- Направление на услугу, инициирующее внесение результата,
    PAYMENT_KIND NUMBER(17)  -- Вид оплаты,
    SERV_STATUS_REASON VARCHAR2(250)  -- Причина смены статуса услуги,
    QUOTA_Q NUMBER(17)  -- Ограничение по квоте,
    UK_HASH VARCHAR2(75) NOT NULL  -- Уникальный ключ записи,
    DC_DIAGNOSIS NUMBER(17)  -- Диагноз случая заболевания,
    LPU_SERVICE NUMBER(17)  -- Услуга ЛПУ,
    REC_DURATION NUMBER(5)  -- Длительность оказания в минутах,
    TICKET_N NUMBER(5)  -- Номер квитка расписания,
    TICKET_S VARCHAR2(75)  -- Номер,описание квитка расписания,
    RQS_LIMIT NUMBER(17)  -- Ресурс квоты записи в расписание,
    EX_SYSTEM NUMBER(17)  -- Источник записи,
    PURCHASE_ORDER NUMBER(17)  -- Заказ-наряд,
    IS_CONFIRMED NUMBER(1) NOT NULL  -- Признак подтверждения записи на прием пациентом: 0 - не подтверждена, 1 - подтверждена, 2 - запись отменена, 3 - запись перенесена, 4 - явка, 5 - не дозвонились,
    NURSE_USER_TEMPLATES NUMBER(17)  -- Шаблон медсестры,
    CONFIRM_DATE DATE  -- Дата подтверждения записи,
    CONFERENCE_TYPE NUMBER(1)  -- Выбор из дополнительного словаря - TM_TYPE - Тип консультации,
    CONFERENCE VARCHAR2(200)  -- Консультация,
    EMPLOYER_CANCEL NUMBER(17)  -- Врач, отменивший направление,
    LOCALIZATION NUMBER(17)  -- Группы локализаций,
    SERV_DESC VARCHAR2(2000)  -- Описание услуги,
    COMPLID NUMBER(17)  -- Ссылка на запись головной комплексной услуги,
    COMPSTR NUMBER(17)  -- Ссылка на настройку состава комплексной услуги,
    IMPORTANT NUMBER(1) NOT NULL  -- Важное: 1 - да, 0 - нет,
    PATIENT NUMBER(17)  -- Пациент,
    GUID VARCHAR2(36)  -- GUID,
    ATTENDANCE_STATE NUMBER(1) NOT NULL  -- Cостояние посещаемости: 0 – Не определено, 1 – Явка, 2 – Неявка,
    CONSTRAINT PK_D_DIRECTION_SERVICES PRIMARY KEY (ID)
);

ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_CL FOREIGN KEY (CABLAB_TO) REFERENCES D_CABLAB(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_CMPSTR FOREIGN KEY (COMPSTR) REFERENCES D_LPU_SERV_COMPSTR(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_COMPL FOREIGN KEY (COMPLID) REFERENCES D_DIRECTION_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_DCD FOREIGN KEY (DC_DIAGNOSIS) REFERENCES D_DC_DIAGNOSISES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_DSC FOREIGN KEY (DISEASECASE) REFERENCES D_DISEASECASES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_EMP FOREIGN KEY (EMPLOYER_TO) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_ES FOREIGN KEY (EX_SYSTEM) REFERENCES D_EX_SYSTEMS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_HHD FOREIGN KEY (HH_DEP) REFERENCES D_HOSP_HISTORY_DEPS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_HID FOREIGN KEY (HID) REFERENCES D_DIRECTION_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_IR FOREIGN KEY (IRID) REFERENCES D_DIRECTION_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_LG FOREIGN KEY (LOCALIZATION) REFERENCES D_LOC_GROUPS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_LPU FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_LS FOREIGN KEY (LPU_SERVICE) REFERENCES D_LPU_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_NUT FOREIGN KEY (NURSE_USER_TEMPLATES) REFERENCES D_VIS_USER_TEMPLATES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_PID FOREIGN KEY (PID) REFERENCES D_DIRECTIONS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_PK FOREIGN KEY (PAYMENT_KIND) REFERENCES D_PAYMENT_KIND(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_PMC FOREIGN KEY (PATIENT) REFERENCES D_PERSMEDCARD(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_PO FOREIGN KEY (PURCHASE_ORDER) REFERENCES D_PURCHASE_ORDER(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_QQ FOREIGN KEY (QUOTA_Q) REFERENCES D_QUOTA_QUANTS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_REC FOREIGN KEY (REC_TYPE) REFERENCES D_DS_REC_TYPES(RCT_CODE);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_REG FOREIGN KEY (REG_TYPE) REFERENCES D_REG_TYPES(REG_CODE);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_RK FOREIGN KEY (REF_KIND) REFERENCES D_REFERENCE_KINDS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_RPID FOREIGN KEY (RPID) REFERENCES D_DIRECTION_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_RQSL FOREIGN KEY (RQS_LIMIT) REFERENCES D_RQS_LIMITS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_SRV FOREIGN KEY (SERVICE) REFERENCES D_SERVICES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_SS FOREIGN KEY (SERV_STATUS) REFERENCES D_DS_SERV_STATUSES(SS_CODE);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_TT FOREIGN KEY (TIME_TYPE) REFERENCES D_SCHEDULE_TIME_TYPES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_VK FOREIGN KEY (VISIT_KIND) REFERENCES D_VISITKINDS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT FK_D_DIRECTION_SERVICES_VP FOREIGN KEY (VISIT_PURPOSE) REFERENCES D_VISITPURPOSES(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00227790 FOREIGN KEY (EMPLOYER_CANCEL) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT UK_D_DIRECTION_SERVICES_HS(UK_HASH);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_AC CHECK (ATTENDANCE_STATE in (0, 1, 2));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_AC_SS CHECK ((ATTENDANCE_STATE = 0 and SERV_STATUS != 1) or (ATTENDANCE_STATE = 1 and SERV_STATUS in (0, 1, 3, 4)) or (ATTENDANCE_STATE = 2 and SERV_STATUS in (0, 2, 3, 4)));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_DCD CHECK (DC_DIAGNOSIS is null or DC_DIAGNOSIS is not null and DISEASECASE is not null and IRID is not null);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_ICNF CHECK (IS_CONFIRMED is null or IS_CONFIRMED in (0,1,2,3,4,5));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_ICP CHECK (IS_COMBINED_PAYMENT in (0,1));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_ICPK CHECK (IS_COMBINED_PAYMENT = 1 and PAYMENT_KIND is null or IS_COMBINED_PAYMENT = 0);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_IN CHECK (IS_NECESSARY in (0,1));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_IP CHECK (IS_PRIMARY in (0,1));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_IR_RP CHECK ((IRID is null or IRID != ID) and (RPID is null or RPID != ID));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_NUT CHECK (NURSE_USER_TEMPLATES is null or SERV_STATUS != 1);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_RD CHECK (REC_DURATION is null or REC_DURATION > 0 and REC_DURATION <= 32000);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_RRT CHECK (REC_TYPE in (0,1,2,3,4,5));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_RT CHECK (REG_TYPE in (0,1,2,3));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_RTC CHECK (REG_TYPE = 3 and (CABLAB_TO is not null or EMPLOYER_TO is not null) or REG_TYPE <> 3);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_SC CHECK (SER_COUNT > 0);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT CH_D_DIRECTION_SERVICES_SS CHECK (SERV_STATUS in (0,1,2,3,4));
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199042 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199043 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199044 CHECK ("PID" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199045 CHECK ("IS_COMBINED_PAYMENT" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199046 CHECK ("IS_NECESSARY" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199047 CHECK ("SERVICE" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199048 CHECK ("REG_TYPE" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199049 CHECK ("SERV_STATUS" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199050 CHECK ("IS_PRIMARY" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199051 CHECK ("SER_COUNT" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199052 CHECK ("UK_HASH" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00199053 CHECK ("IS_CONFIRMED" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00702674 CHECK ("IMPORTANT" IS NOT NULL);
ALTER TABLE D_DIRECTION_SERVICES ADD CONSTRAINT SYS_C00748775 CHECK ("ATTENDANCE_STATE" IS NOT NULL);
```

---

#### Таблица №10: D_VISITS

```sql
CREATE TABLE D_VISITS (
    ID NUMBER(17) NOT NULL  -- ID,
    PID NUMBER(17) NOT NULL  -- Направления : услуги,
    EMPLOYER NUMBER(17) NOT NULL  -- Сотрудник,
    VISIT_DATE DATE NOT NULL  -- Дата посещения,
    VISIT_PLACE NUMBER(17)  -- Место приема,
    VISIT_RESULT NUMBER(17)  -- Результат визита,
    LPU NUMBER(17) NOT NULL  -- ЛПУ,
    NURSE NUMBER(17)  -- Медсестра,
    HELPER NUMBER(17)  -- Дополнительный врач (помощник),
    ANESTHETIZATION NUMBER(2)  -- Тип анестезии,
    DGROUP NUMBER(17)  -- Группа диспансерного учета,
    PROF_DISEASE NUMBER(17)  -- Проффесинальное заболевание,
    SER_COUNT NUMBER(19,2) NOT NULL  -- Кратность услуги,
    SER_KOEFF NUMBER(5,3) NOT NULL  -- Коэффициент оплаты услуги,
    UET_COUNT NUMBER(5,2) NOT NULL  -- Кол-во затраченных Условных Единиц Труда (УЕТ),
    MINUTES NUMBER(5)  -- Кол-во минут, затраченных на проведение услуги,
    CABLAB NUMBER(17)  -- Кабинет,
    VISIT_TEMPLATE NUMBER(17)  -- Шаблон оказания,
    REF_RESULT NUMBER(17)  -- Результат обращения,
    MED_CARE_KIND NUMBER(17)  -- Вид мед. помощи,
    APP_KIND NUMBER(17)  -- Вид аппаратуры,
    ANEST_KIND NUMBER(17)  -- Вид анестезии,
    ANEST_INDICATIONS NUMBER(17)  -- Показания к анестезии,
    IS_EMERGENCY NUMBER(1)  -- Экстренное посещение : 0 - нет,1 - да,
    PATIENT NUMBER(17)  -- Пациент,
    CONSTRAINT PK_D_VISITS PRIMARY KEY (ID)
);

ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_AI FOREIGN KEY (ANEST_INDICATIONS) REFERENCES D_ANESTH_INDICATIONS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_AK FOREIGN KEY (APP_KIND) REFERENCES D_DIRECTORIES_FN_DATA(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_ANK FOREIGN KEY (ANEST_KIND) REFERENCES D_ANESTHETIZATION_KIND(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_AT FOREIGN KEY (ANESTHETIZATION) REFERENCES D_ANESTH_TYPES(AT_CODE);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_CAB FOREIGN KEY (CABLAB) REFERENCES D_CABLAB(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_DG FOREIGN KEY (DGROUP) REFERENCES D_PATIENTS_DGROUP(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_EM FOREIGN KEY (EMPLOYER) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_HLP FOREIGN KEY (HELPER) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_LP FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_MK FOREIGN KEY (MED_CARE_KIND) REFERENCES D_DIRECTORIES_FN_DATA(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_NU FOREIGN KEY (NURSE) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_PD FOREIGN KEY (PROF_DISEASE) REFERENCES D_PROF_DISEASES(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_PID FOREIGN KEY (PID) REFERENCES D_DIRECTION_SERVICES(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_PMC FOREIGN KEY (PATIENT) REFERENCES D_PERSMEDCARD(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_RR FOREIGN KEY (REF_RESULT) REFERENCES D_REFERENCE_RESULTS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_VP FOREIGN KEY (VISIT_PLACE) REFERENCES D_VISITPLACES(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_VR FOREIGN KEY (VISIT_RESULT) REFERENCES D_VISITRESULTS(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT FK_D_VISITS_VT FOREIGN KEY (VISIT_TEMPLATE) REFERENCES D_VISIT_TEMPLATES(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT TMP$$_FK_D_VISITS_PID0 FOREIGN KEY (PID) REFERENCES D_DIRECTION_SERVICES_RED(ID);
ALTER TABLE D_VISITS ADD CONSTRAINT UK_D_VISITS(PID);
ALTER TABLE D_VISITS ADD CONSTRAINT CH_D_VISITS_IE CHECK (IS_EMERGENCY is null or IS_EMERGENCY in (0,1));
ALTER TABLE D_VISITS ADD CONSTRAINT CH_D_VISITS_MI CHECK (MINUTES is null or MINUTES > 0);
ALTER TABLE D_VISITS ADD CONSTRAINT CH_D_VISITS_SC CHECK (SER_COUNT > 0);
ALTER TABLE D_VISITS ADD CONSTRAINT CH_D_VISITS_SK CHECK (SER_KOEFF > 0);
ALTER TABLE D_VISITS ADD CONSTRAINT CH_D_VISITS_UC CHECK (UET_COUNT >= 0);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207112 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207113 CHECK ("PID" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207114 CHECK ("EMPLOYER" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207115 CHECK ("VISIT_DATE" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207116 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207117 CHECK ("SER_COUNT" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207118 CHECK ("SER_KOEFF" IS NOT NULL);
ALTER TABLE D_VISITS ADD CONSTRAINT SYS_C00207119 CHECK ("UET_COUNT" IS NOT NULL);
```

---

#### Таблица №11: D_USERPROCS

```sql
CREATE TABLE D_USERPROCS (
    ID NUMBER(17) NOT NULL  -- ID,
    LPU NUMBER(17) NOT NULL  -- LPU,
    PR_CODE VARCHAR2(40) NOT NULL  -- Код,
    PR_NAME VARCHAR2(250) NOT NULL  -- Наименование,
    PR_NOTE VARCHAR2(500)  -- Примечание,
    PR_TYPE NUMBER(1) NOT NULL  -- Тип : 0 - хранимая процедура, 1 - неименованный блок,
    PR_EXEC_TYPE NUMBER(1) NOT NULL  -- Тип запуска : 0 - вручную, 1 - автоматический,
    STOREDPROC VARCHAR2(61)  -- Хранимая процедура,
    NNMBLOCK VARCHAR2(4000)  -- Неименованный блок,
    SCHEMA VARCHAR2(30)  -- Схема,
    OVERLOADNUMB NUMBER(17)  -- Номер перегрузки процедуры,
    CID NUMBER(17) NOT NULL  -- Каталог,
    CONSTRAINT PK_D_USERPROCS PRIMARY KEY (ID)
);

ALTER TABLE D_USERPROCS ADD CONSTRAINT FK_D_USERPROCS_CID FOREIGN KEY (CID) REFERENCES D_CATALOGS(ID);
ALTER TABLE D_USERPROCS ADD CONSTRAINT FK_D_USERPROCS_LPU FOREIGN KEY (LPU) REFERENCES D_LPU(ID);
ALTER TABLE D_USERPROCS ADD CONSTRAINT UK_D_USERPROCS_CODE(LPU);
ALTER TABLE D_USERPROCS ADD CONSTRAINT UK_D_USERPROCS_CODE(PR_CODE);
ALTER TABLE D_USERPROCS ADD CONSTRAINT UK_D_USERPROCS_NAME(LPU);
ALTER TABLE D_USERPROCS ADD CONSTRAINT UK_D_USERPROCS_NAME(PR_NAME);
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_ETYPE CHECK (PR_EXEC_TYPE in (0,1));
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_ON CHECK (OVERLOADNUMB > 0 or OVERLOADNUMB is null);
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_SP CHECK (schema = upper(schema) and STOREDPROC = upper(STOREDPROC));
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_TYPE CHECK (PR_TYPE in (0,1));
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_TYPEC CHECK ((PR_TYPE = 0 and NNMBLOCK is null and STOREDPROC is not null) or (PR_TYPE = 1 and STOREDPROC is null and NNMBLOCK is not null));
ALTER TABLE D_USERPROCS ADD CONSTRAINT CH_D_USERPROCS_TYPEEX CHECK ((PR_TYPE = 0 and PR_EXEC_TYPE = 0) or (PR_TYPE = 1));
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207262 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207263 CHECK ("LPU" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207264 CHECK ("PR_CODE" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207265 CHECK ("PR_NAME" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207266 CHECK ("PR_TYPE" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207267 CHECK ("PR_EXEC_TYPE" IS NOT NULL);
ALTER TABLE D_USERPROCS ADD CONSTRAINT SYS_C00207268 CHECK ("CID" IS NOT NULL);
```

---

#### Таблица №12: D_HIV_NR_PATIENTS

```sql
CREATE TABLE D_HIV_NR_PATIENTS (
    ID NUMBER(17) NOT NULL  -- ID,
    VERSION NUMBER(17) NOT NULL  -- Версия,
    PID NUMBER(17) NOT NULL  -- РВИЧ. Случаи ВИЧ,
    NOS_REGISTR NUMBER(17) NOT NULL  -- Нозологический регистр,
    AGENT NUMBER(17) NOT NULL  -- Контрагент,
    NUMB_REG VARCHAR2(22) NOT NULL  -- Уникальный номер в регистре ВИЧ,
    CREATE_DATE DATE NOT NULL  -- Дата включения в регистр ВИЧ,
    CLOSE_DATE DATE  -- Дата исключения из регистра ВИЧ,
    CREATE_EMP NUMBER(17) NOT NULL  -- Сотрудник, включивший пациента в регистр,
    CLOSE_EMP NUMBER(17)  -- Сотрудник, исключивший пациента из регистра,
    REMOVE_REASON NUMBER(17)  -- Причина исключения из регистра,
    DROPPED_OUT NUMBER(17)  -- Тип учреждения куда выбыл,
    DROPPED_OUT_ORG NUMBER(17)  -- Учреждение выбытия,
    IS_LOADED NUMBER(1) NOT NULL  -- Признак загруженной записи, 0 - не загружен, 1 - загружен, 2 - импортирован,
    LAST_DATE_EXPORT DATE  -- Дата последней выгрузки,
    LAST_CHANGE_DATE DATE NOT NULL  -- Дата время последнего изменения записи,
    CATEGORY NUMBER(17)  -- Категория контингента,
    PAT_NUMB VARCHAR2(17)  -- № карты в дисп.отделе,
    CONSTRAINT PK_D_HIV_NR_PATIENTS PRIMARY KEY (ID)
);

ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_AGENT FOREIGN KEY (AGENT) REFERENCES D_AGENTS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_CATEGORY FOREIGN KEY (CATEGORY) REFERENCES D_NR_CLS_CONTINGENTS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_CLOSE_EMP FOREIGN KEY (CLOSE_EMP) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_CREATE_EM FOREIGN KEY (CREATE_EMP) REFERENCES D_EMPLOYERS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_DOO FOREIGN KEY (DROPPED_OUT_ORG) REFERENCES D_LPU(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_DROPPED_O FOREIGN KEY (DROPPED_OUT) REFERENCES D_HIVD_PLACE_DEPARTURE(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_NOS_REGIS FOREIGN KEY (NOS_REGISTR) REFERENCES D_NOS_REGISTRS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_PID FOREIGN KEY (PID) REFERENCES D_HIV_CASES(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_REASON FOREIGN KEY (REMOVE_REASON) REFERENCES D_NR_REMOVE_REASONS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT FK_D_HIV_NR_PATIENTS_VERSION FOREIGN KEY (VERSION) REFERENCES D_VERSIONS(ID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VAC(VERSION);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VAC(AGENT);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VAC(CLOSE_DATE);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VNR(VERSION);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VNR(NUMB_REG);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VPC(VERSION);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VPC(PID);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT UK_D_HIV_NR_PATIENTS_VPC(CREATE_DATE);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT CH_D_HIV_NR_PATIENTS_CD CHECK (CLOSE_DATE >= CREATE_DATE  or CLOSE_DATE is NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT CH_D_HIV_NR_PATIENTS_ISL CHECK (IS_LOADED in (0,1,2));
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198279 CHECK ("ID" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198280 CHECK ("VERSION" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198281 CHECK ("PID" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198282 CHECK ("NOS_REGISTR" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198283 CHECK ("AGENT" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198285 CHECK ("CREATE_DATE" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198286 CHECK ("CREATE_EMP" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00198287 CHECK ("LAST_CHANGE_DATE" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00236811 CHECK ("NUMB_REG" IS NOT NULL);
ALTER TABLE D_HIV_NR_PATIENTS ADD CONSTRAINT SYS_C00747054 CHECK ("IS_LOADED" IS NOT NULL);
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

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


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

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
