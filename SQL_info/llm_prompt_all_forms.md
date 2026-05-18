# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ Forms/HospitPlanning/hospit_planning.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm
**Статус:** ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Mon May 18 22:36:53 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 12
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_HOSP_PLAN_KINDS
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_HOSP_PLAN_KINDS" mode="Range">
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                select hpk.ID,
                       hpk.HP_CODE,
                       hpk.HP_NAME,
                       hpk.MAX_PRIOR,
                       hpk.DEPS,
                       hpk.MIN_AGE,
                       hpk.MAX_AGE,
                       hpk.HAS_MKB_CONSTRAINTS,
                       hpk.SHAS_MKB_CONSTRAINTS,
                       hpk.SHAS_LIMITS,
                       hpk.SHAS_PAYMENT_CONSTRAINTS,
                       hpk.NUMB_GROUP,
                       hpk.JOURNAL_TYPE_MNEMO,
                       hpk.OPER_MNEMO,
                       to_char(hpk.OPEN_DATE, 'DD.MM.YYYY') as OPEN_DATE,
                       to_char(hpk.CLOSE_DATE, 'DD.MM.YYYY') as CLOSE_DATE
                  from D_V_HOSP_PLAN_KINDS hpk
                 where hpk.LPU = to_number(:pnLPU)
                   and (D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => to_number(:pnLPU),
                                                       psUNITCODE => 'HOSP_PLAN_KINDS',
                                                       pnUNIT_ID  => hpk.ID,
                                                       psRIGHT    => 11,
                                                       pnCABLAB   => to_number(:pnCABLAB),
                                                       pnSERVICE  => null) = 1
                    or to_number(:pnSHOW_CSE) = 1)
                ]]>
            </component>
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                select hpk.ID,
                       hpk.HP_CODE,
                       hpk.HP_NAME,
                       hpk.MAX_PRIOR,
                       hpk.DEPS,
                       hpk.MIN_AGE,
                       hpk.MAX_AGE,
                       hpk.HAS_MKB_CONSTRAINTS,
                       hpk.SHAS_MKB_CONSTRAINTS,
                       hpk.SHAS_LIMITS,
                       hpk.SHAS_PAYMENT_CONSTRAINTS,
                       hpk.NUMB_GROUP,
                       hpk.JOURNAL_TYPE_MNEMO,
                       hpk.OPER_MNEMO,
                       to_char(hpk.OPEN_DATE, 'DD.MM.YYYY') as OPEN_DATE,
                       to_char(hpk.CLOSE_DATE, 'DD.MM.YYYY') as CLOSE_DATE
                  from D_V_HOSP_PLAN_KINDS hpk
                 where hpk.LPU = (:pnLPU)::numeric
                   and (D_PKG_CSE_ACCESSES.CHECK_RIGHT(pnLPU      => (:pnLPU)::numeric,
                                                       psUNITCODE => 'HOSP_PLAN_KINDS',
                                                       pnUNIT_ID  => hpk.ID,
                                                       psRIGHT    => 11,
                                                       pnCABLAB   => (:pnCABLAB)::numeric,
                                                       pnSERVICE  => null) = 1
                    or (:pnSHOW_CSE)::numeric = 1)
                ]]>
            </component>
            <component cmptype="Variable" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="Variable" name="pnCABLAB" src="CABLAB" srctype="session" />
            <component cmptype="Variable" name="pnSHOW_CSE" src="SHOW_CSE" srctype="var" />
            <component cmptype="Variable" name="r1c" src="r1c" srctype="var" default="10" />
            <component cmptype="Variable" name="r1s" src="r1s" srctype="var" default="1" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HOSP_PLAN_KINDS
**Используемые пакеты/функции:** D_PKG_CSE_ACCESSES.CHECK_RIGHT

---

### Запрос №2

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_HPK_PLANS
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_HPK_PLANS" activateoncreate="false" mode="Range">
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                select hp.ID,
                       trunc(hp.PLAN_DATE) as PLAN_DATE,
                       hp.PLAN_DAY_RUS,
                       trim(hp.PLAN_DAY_ENG) as PLAN_DAY_ENG,
                       to_char(hp.PLAN_DATE, 'D') as PLAN_DAY_NUMBER,
                       hp.MALE_COUNT_S,
                       hp.FEMALE_COUNT_S,
                       hp.OPER_COUNT_S,
                       hp.CON_COUNT_S,
                       hp.GEN_COUNT_S
                  from D_V_HPK_PLANS hp
                 where hp.PID = to_number(:pnHOSP_PLAN_KINDS)
                   and (hp.PLAN_DATE >= to_date(:pdPLAN_DATE_FROM, 'DD.MM.YYYY') or :pdPLAN_DATE_FROM is null)
                   and (hp.PLAN_DATE <= to_date(:pdPLAN_DATE_TO, 'DD.MM.YYYY') or :pdPLAN_DATE_TO is null)
                ]]>
            </component>
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                select hp.ID,
                       date_trunc('day', hp.PLAN_DATE) as PLAN_DATE,
                       hp.PLAN_DAY_RUS,
                       trim(hp.PLAN_DAY_ENG) as PLAN_DAY_ENG,
                       to_char(hp.PLAN_DATE, 'D') as PLAN_DAY_NUMBER,
                       hp.MALE_COUNT_S,
                       hp.FEMALE_COUNT_S,
                       hp.OPER_COUNT_S,
                       hp.CON_COUNT_S,
                       hp.GEN_COUNT_S
                  from D_V_HPK_PLANS hp
                 where hp.PID = (:pnHOSP_PLAN_KINDS)::numeric
                   and (hp.PLAN_DATE >= (:pdPLAN_DATE_FROM)::date or :pdPLAN_DATE_FROM is null)
                   and (hp.PLAN_DATE <= (:pdPLAN_DATE_TO)::date or :pdPLAN_DATE_TO is null)
                ]]>
            </component>
            <component cmptype="Variable" name="pnHOSP_PLAN_KINDS" src="HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="Variable" name="pdPLAN_DATE_FROM" src="PLAN_DATE_FROM" srctype="ctrl" />
            <component cmptype="Variable" name="pdPLAN_DATE_TO" src="PLAN_DATE_TO" srctype="ctrl" />
            <component cmptype="Variable" name="r1c" src="r1c" srctype="var" default="10" />
            <component cmptype="Variable" name="r1s" src="r1s" srctype="var" default="1" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_PLANS

---

### Запрос №3

**Тип компонента:** M2 DataSet
**Имя компонента:** dsHPKSchedule
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="dsHPKSchedule" activateoncreate="false" mode="Range">
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                select hs.ID,
                       hs.DATE_BEGIN,
                       hs.DATE_END,
                       sc.CODE,
                       sc.NAME,
                       hs.IS_ACTIVE,
                       hs.IS_PRIORITY,
                       hs.OVER_LIMITS,
                       hs.HOSP_PLAN_KINDS,
                       hs.SCHEDULE
                  from D_V_HPK_SCHEDULE_BASE hs
                  join D_V_SCHEDULE_BASE sc on hs.SCHEDULE = sc.ID
                 where hs.HOSP_PLAN_KINDS = to_number(:pnHOSP_PLAN_KINDS)
                   and (hs.DATE_BEGIN >= to_date(:pdDATE_FROM, 'DD.MM.YYYY') or :pdDATE_FROM is null)
                   and (hs.DATE_BEGIN <= to_date(:pdDATE_TO, 'DD.MM.YYYY') or :pdDATE_TO is null)
                ]]>
            </component>
            <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                select hs.ID,
                       hs.DATE_BEGIN,
                       hs.DATE_END,
                       sc.CODE,
                       sc.NAME,
                       hs.IS_ACTIVE,
                       hs.IS_PRIORITY,
                       hs.OVER_LIMITS,
                       hs.HOSP_PLAN_KINDS,
                       hs.SCHEDULE
                  from D_V_HPK_SCHEDULE_BASE hs
                  join D_V_SCHEDULE_BASE sc on hs.SCHEDULE = sc.ID
                 where hs.HOSP_PLAN_KINDS = (:pnHOSP_PLAN_KINDS)::numeric
                   and (hs.DATE_BEGIN >= (:pdDATE_FROM)::date or :pdDATE_FROM is null)
                   and (hs.DATE_BEGIN <= (:pdDATE_TO)::date or :pdDATE_TO is null)
                ]]>
            </component>
            <component cmptype="Variable" name="pnHOSP_PLAN_KINDS" src="HOSP_PLAN_KINDS" srctype="ctrl" />
            <component cmptype="Variable" name="pdDATE_FROM" src="deSchDateFrom" srctype="ctrl" />
            <component cmptype="Variable" name="pdDATE_TO" src="deSchDateTo" srctype="ctrl" />
            <component cmptype="Variable" name="r1c" src="r1c" srctype="var" default="10" />
            <component cmptype="Variable" name="r1s" src="r1s" srctype="var" default="1" />
        </component>
```

**Используемые таблицы/вьюхи:** D_V_HPK_SCHEDULE_BASE, D_V_SCHEDULE_BASE

---

### Запрос №4

**Тип компонента:** M2 Action
**Имя компонента:** acSetPlanForDay
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acSetPlanForDay">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                begin
                  if to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY') < to_date(:pdDATE_TO, 'DD.MM.YYYY') then
                    D_P_EXC('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY'));
                  end if;
                  D_PKG_HPK_PLANS.SET_PLAN_FOR_DAY(pnLPU        => to_number(:pnLPU),
                                                   pnPLAN       => to_number(:pnHPK_PLANS),
                                                   psDAY        => :psPLAN_DAY_ENG,
                                                   pdSTART_DATE => to_date(:pdDATE_FROM, 'DD.MM.YYYY'),
                                                   pdEND_DATE   => to_date(:pdDATE_TO, 'DD.MM.YYYY'));
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  if (:pdCLOSE_DATE_PLAN)::date < (:pdDATE_TO)::date then
                    PERFORM D_P_EXC(1, ('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || (:pdCLOSE_DATE_PLAN)::text));
                  end if;
                  call D_PKG_HPK_PLANS.SET_PLAN_FOR_DAY(pnLPU        => (:pnLPU)::numeric,
                                                        pnPLAN       => (:pnHPK_PLANS)::numeric,
                                                        psDAY        => (:psPLAN_DAY_ENG)::text,
                                                        pdSTART_DATE => (:pdDATE_FROM)::date,
                                                        pdEND_DATE   => (:pdDATE_TO)::date);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnHPK_PLANS" src="HPK_PLANS" srctype="ctrl" />
            <component cmptype="ActionVar" name="psPLAN_DAY_ENG" src="PLAN_DAY_ENG" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_FROM" src="return_from_value" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_TO" src="return_to_value" srctype="var" />
            <component cmptype="ActionVar" name="pdCLOSE_DATE_PLAN" src="CLOSE_DATE_PLAN" srctype="var" />
            <component cmptype="ActionVar" name="psHP_NAME" src="HP_NAME" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.SET_PLAN_FOR_DAY

---

### Запрос №5

**Тип компонента:** M2 Action
**Имя компонента:** acSetPlanForWeek
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acSetPlanForWeek">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                begin
                  if to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY') < to_date(:pdDATE_TO, 'DD.MM.YYYY') then
                    D_P_EXC('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY'));
                  end if;
                  D_PKG_HPK_PLANS.SET_PLAN_FOR_WEEK(pnLPU        => to_number(:pnLPU),
                                                    pnHOSP_PLAN  => to_number(:pnHPK_PLANS),
                                                    pdSTART_DATE => to_date(:pdDATE_FROM, 'DD.MM.YYYY'),
                                                    pdEND_DATE   => to_date(:pdDATE_TO, 'DD.MM.YYYY'));
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  if (:pdCLOSE_DATE_PLAN)::date < (:pdDATE_TO)::date then
                    PERFORM D_P_EXC(1, ('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || (:pdCLOSE_DATE_PLAN)::text));
                  end if;
                  call D_PKG_HPK_PLANS.SET_PLAN_FOR_WEEK(pnLPU        => (:pnLPU)::numeric,
                                                         pnHOSP_PLAN  => (:pnHPK_PLANS)::numeric,
                                                         pdSTART_DATE => (:pdDATE_FROM)::date,
                                                         pdEND_DATE   => (:pdDATE_TO)::date);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnHPK_PLANS" src="HPK_PLANS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE_FROM" src="return_from_value" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_TO" src="return_to_value" srctype="var" />
            <component cmptype="ActionVar" name="pdCLOSE_DATE_PLAN" src="CLOSE_DATE_PLAN" srctype="var" />
            <component cmptype="ActionVar" name="psHP_NAME" src="HP_NAME" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.SET_PLAN_FOR_WEEK

---

### Запрос №6

**Тип компонента:** M2 Action
**Имя компонента:** acDelHpkPlan
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acDelHpkPlan">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HPK_PLANS" action="DELETE" />
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  call D_PKG_HPK_PLANS.DEL(pnID  => (:pnID)::numeric,
                                           pnLPU => (:pnLPU)::numeric);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnID" src="HPK_PLANS" srctype="ctrl" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.DEL

---

### Запрос №7

**Тип компонента:** M2 Action
**Имя компонента:** acDelHospPlanKind
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acDelHospPlanKind">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE" unit="HOSP_PLAN_KINDS" action="DELETE" />
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  call D_PKG_HOSP_PLAN_KINDS.DEL(pnID  => (:pnID)::numeric,
                                                 pnLPU => (:pnLPU)::numeric);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnID" src="HOSP_PLAN_KINDS" srctype="ctrl" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HOSP_PLAN_KINDS.DEL

---

### Запрос №8

**Тип компонента:** M2 Action
**Имя компонента:** acGetNewMonth
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acGetNewMonth">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                declare
                  ndNEW_DATE DATE;
                begin
                  ndNEW_DATE := ADD_MONTHS(to_date(:pdPLAN_DATE_FROM_S, 'dd.mm.yyyy'), :pnSEARCH_DIRECTION);
                  :pdPLAN_DATE_FROM := '01.' || to_char(ndNEW_DATE, 'mm.yyyy');
                  :pdPLAN_DATE_TO := to_char(LAST_DAY(ndNEW_DATE), 'DD.MM.YYYY');
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                declare
                  ndNEW_DATE date;
                begin
                  ndNEW_DATE := (:pdPLAN_DATE_FROM_S)::date + ((:pnSEARCH_DIRECTION)::integer || ' months')::interval;
                  :pdPLAN_DATE_FROM := '01.' || to_char(ndNEW_DATE, 'MM.YYYY');
                  :pdPLAN_DATE_TO := to_char(date_trunc('month', ndNEW_DATE) + interval '1 month - 1 day', 'DD.MM.YYYY');
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pdPLAN_DATE_FROM_S" src="PLAN_DATE_FROM" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnSEARCH_DIRECTION" src="SEARCH_DIRECTION" srctype="var" />
            <component cmptype="ActionVar" name="pdPLAN_DATE_FROM" src="PLAN_DATE_FROM" srctype="ctrl" put="" len="15" />
            <component cmptype="ActionVar" name="pdPLAN_DATE_TO" src="PLAN_DATE_TO" srctype="ctrl" put="" len="15" />
        </component>
```


---

### Запрос №9

**Тип компонента:** M2 Action
**Имя компонента:** acGetDefParams
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

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
                  nCID numeric;
                begin
                  :pdSYS_DATE := to_char(current_date, 'DD.MM.YYYY');
                  :pdFILTER_DATE_FROM := '01.' || to_char(current_date, 'MM.YYYY');
                  :pdFILTER_DATE_TO := to_char(date_trunc('month', current_date) + interval '1 month - 1 day', 'DD.MM.YYYY');

                  select D_PKG_URPRIVS.CHECK_BPPRIV((:pnLPU)::numeric, 'HOSP_PLAN_KINDS_VIEW_CSE_ACCESS', null, 0)
                    into :pnSHOW_CSE;

                  nCID := :pnCID;

                  call D_PKG_CATALOGS.FIND_ROOT_CATALOG(pnRAISE    => 1,
                                                        pnLPU      => (:pnLPU)::numeric,
                                                        psUNITCODE => 'HOSP_PLAN_KINDS',
                                                        pnCATALOG  => nCID);

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
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pdFILTER_DATE_FROM" src="PLAN_DATE_FROM" srctype="ctrl" put="" len="15" />
            <component cmptype="ActionVar" name="pdFILTER_DATE_TO" src="PLAN_DATE_TO" srctype="ctrl" put="" len="15" />
            <component cmptype="ActionVar" name="pdFILTER_DATE_FROM" src="deSchDateFrom" srctype="ctrl" put="" len="15" />
            <component cmptype="ActionVar" name="pdFILTER_DATE_TO" src="deSchDateTo" srctype="ctrl" put="" len="15" />
            <component cmptype="ActionVar" name="pnSHOW_CSE" src="SHOW_CSE" srctype="var" put="" len="10" />
            <component cmptype="ActionVar" name="pnCID" src="MAIN_CID" srctype="var" put="" len="17" />
            <component cmptype="ActionVar" name="pdSYS_DATE" src="SYS_DATE" srctype="var" put="" len="15" />
            <component cmptype="ActionVar" name="pnSCH_ADD" src="SCH_ADD" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnSCH_UPD" src="SCH_UPD" srctype="var" put="" len="1" />
            <component cmptype="ActionVar" name="pnSCH_DEL" src="SCH_DEL" srctype="var" put="" len="1" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_URPRIVS.CHECK_BPPRIV, D_PKG_CATALOGS.FIND_ROOT_CATALOG

---

### Запрос №10

**Тип компонента:** M2 Action
**Имя компонента:** acCopyPlanForDay
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acCopyPlanForDay">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                begin
                  if to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY') < to_date(:pdDATE_TO, 'DD.MM.YYYY') then
                    D_P_EXC('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY'));
                  end if;
                  D_PKG_HPK_PLANS.COPY_PLAN_FOR_DAY(pnLPU        => to_number(:pnLPU),
                                                    pnPLAN       => to_number(:pnHPK_PLANS),
                                                    psDAY        => :psPLAN_DAY_ENG,
                                                    pdSTART_DATE => to_date(:pdDATE_FROM, 'DD.MM.YYYY'),
                                                    pdEND_DATE   => to_date(:pdDATE_TO, 'DD.MM.YYYY'));
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  if (:pdCLOSE_DATE_PLAN)::date < (:pdDATE_TO)::date then
                    PERFORM D_P_EXC(1, ('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || (:pdCLOSE_DATE_PLAN)::text));
                  end if;
                  call D_PKG_HPK_PLANS.COPY_PLAN_FOR_DAY(pnLPU        => (:pnLPU)::numeric,
                                                         pnPLAN       => (:pnHPK_PLANS)::numeric,
                                                         psDAY        => (:psPLAN_DAY_ENG)::text,
                                                         pdSTART_DATE => (:pdDATE_FROM)::date,
                                                         pdEND_DATE   => (:pdDATE_TO)::date);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnHPK_PLANS" src="HPK_PLANS" srctype="ctrl" />
            <component cmptype="ActionVar" name="psPLAN_DAY_ENG" src="PLAN_DAY_ENG" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_FROM" src="return_from_value" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_TO" src="return_to_value" srctype="var" />
            <component cmptype="ActionVar" name="pdCLOSE_DATE_PLAN" src="CLOSE_DATE_PLAN" srctype="var" />
            <component cmptype="ActionVar" name="psHP_NAME" src="HP_NAME" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.COPY_PLAN_FOR_DAY

---

### Запрос №11

**Тип компонента:** M2 Action
**Имя компонента:** acCopyPlanForWeek
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acCopyPlanForWeek">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                begin
                  if to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY') < to_date(:pdDATE_TO, 'DD.MM.YYYY') then
                    D_P_EXC('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || to_date(:pdCLOSE_DATE_PLAN, 'DD.MM.YYYY'));
                  end if;
                  D_PKG_HPK_PLANS.COPY_PLAN_FOR_WEEK(pnLPU        => to_number(:pnLPU),
                                                     pnHOSP_PLAN  => to_number(:pnHPK_PLANS),
                                                     pdSTART_DATE => to_date(:pdDATE_FROM, 'DD.MM.YYYY'),
                                                     pdEND_DATE   => to_date(:pdDATE_TO, 'DD.MM.YYYY'));
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  if (:pdCLOSE_DATE_PLAN)::date < (:pdDATE_TO)::date then
                    PERFORM D_P_EXC(1, ('Дата плана не может быть больше даты окончания действия вида плана госпитализации. Для "' || :psHP_NAME || '" установлена дата окончания действия ' || (:pdCLOSE_DATE_PLAN)::text));
                  end if;
                  call D_PKG_HPK_PLANS.COPY_PLAN_FOR_WEEK(pnLPU        => (:pnLPU)::numeric,
                                                          pnHOSP_PLAN  => (:pnHPK_PLANS)::numeric,
                                                          pdSTART_DATE => (:pdDATE_FROM)::date,
                                                          pdEND_DATE   => (:pdDATE_TO)::date);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
            <component cmptype="ActionVar" name="pnHPK_PLANS" src="HPK_PLANS" srctype="ctrl" />
            <component cmptype="ActionVar" name="pdDATE_FROM" src="return_from_value" srctype="var" />
            <component cmptype="ActionVar" name="pdDATE_TO" src="return_to_value" srctype="var" />
            <component cmptype="ActionVar" name="pdCLOSE_DATE_PLAN" src="CLOSE_DATE_PLAN" srctype="var" />
            <component cmptype="ActionVar" name="psHP_NAME" src="HP_NAME" srctype="var" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_PLANS.COPY_PLAN_FOR_WEEK

---

### Запрос №12

**Тип компонента:** M2 Action
**Имя компонента:** acDelSchedule
**Источник:** Forms/HospitPlanning/hospit_planning.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospitPlanning\hospit_planning.frm

**SQL код:**

```xml
<component cmptype="Action" name="acDelSchedule">
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
                <![CDATA[
                begin
                  D_PKG_HPK_SCHEDULE.DEL(:pnID, :pnLPU);
                end;
                ]]>
            </component>
            <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
                <![CDATA[
                begin
                  call D_PKG_HPK_SCHEDULE.DEL(pnID  => (:pnID)::numeric,
                                              pnLPU => (:pnLPU)::numeric);
                end;
                ]]>
            </component>
            <component cmptype="ActionVar" name="pnID" src="gridSchedule" srctype="ctrl" />
            <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
        </component>
```

**Используемые пакеты/функции:** D_PKG_HPK_SCHEDULE.DEL


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 4

---

### Вьюха №1: D_V_HOSP_PLAN_KINDS

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

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

### Вьюха №2: D_V_HPK_PLANS

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

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

### Вьюха №3: D_V_HPK_SCHEDULE_BASE

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_HPK_SCHEDULE_BASE
 SELECT id,
    lpu,
    hosp_plan_kinds,
    schedule,
    date_begin,
    date_create,
    is_active,
    is_priority,
    date_end,
    over_limits
   FROM d_hpk_schedule hse
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = hse.lpu AND ur.unitcode::text = 'HPK_SCHEDULE'::text
         LIMIT 1));
```

---

### Вьюха №4: D_V_SCHEDULE_BASE

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_SCHEDULE_BASE
 SELECT id,
    lpu,
    cid,
    code,
    name,
    start_date,
    sch_type,
    quoting,
    holidays,
    sch_kind
   FROM d_schedule d
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = d.cid AND ur.unitcode::text = 'SCHEDULE'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 4

---

### Вьюха №1: D_V_HOSP_PLAN_KINDS

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

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

### Вьюха №2: D_V_HPK_PLANS

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

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

### Вьюха №3: D_V_HPK_SCHEDULE_BASE

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

**DDL определение:**

```sql
-- Oracle View: D_V_HPK_SCHEDULE_BASE
select -- Представление для раздела: Виды планов госпитализации : графики (базовое)
       hse.ID,
       hse.LPU,
       hse.HOSP_PLAN_KINDS,
       hse.SCHEDULE,
       hse.DATE_BEGIN,
       hse.DATE_CREATE,
       hse.IS_ACTIVE,
       hse.IS_PRIORITY,
       hse.DATE_END,
       hse.OVER_LIMITS
  from D_HPK_SCHEDULE hse     -- Виды планов госпитализации : графики
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU = hse.LPU
                  and ur.UNITCODE = 'HPK_SCHEDULE'
                  and rownum = 1)
```

---

### Вьюха №4: D_V_SCHEDULE_BASE

**Используется в формах:**
- Forms/HospitPlanning/hospit_planning.frm

**DDL определение:**

```sql
-- Oracle View: D_V_SCHEDULE_BASE
select --Представление для раздела: Графики
       d.ID,
       d.LPU,
       d.CID,
       d.CODE,
       d.NAME,
       d.START_DATE,
       d.SCH_TYPE,
       d.QUOTING,
       d.HOLIDAYS,
       d.SCH_KIND
  from D_SCHEDULE d   --Графики
 where exists (select null from D_V_URPRIVS ur where ur.CATALOG = d.CID and ur.UNITCODE = 'SCHEDULE' and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 4
- Всего уникальных таблиц: 5

### Связь вьюх и таблиц

**D_V_HOSP_PLAN_KINDS** использует таблицы:
- D_HOSP_PLAN_KINDS
- D_HPK_JOURNAL_TYPES

**D_V_HPK_PLANS** использует таблицы:
- D_HPK_PLANS
- D_HOSP_PLAN_KINDS

**D_V_HPK_SCHEDULE_BASE** использует таблицы:
- D_HPK_SCHEDULE

**D_V_SCHEDULE_BASE** использует таблицы:
- D_SCHEDULE

### DDL определения таблиц

---

#### Таблица №1: D_HOSP_PLAN_KINDS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.id IS 'ID';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.hp_code IS 'Код';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.hp_name IS 'Наименование';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.max_prior IS 'На сколько дней вперед можно записывать в план';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.cid IS 'Каталог';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.min_age IS 'Минимальное ограничение по возрасту';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.max_age IS 'Максимальное ограничение по возрасту';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.has_mkb_constraints IS 'Имеется ли ограничения по диагнозу : 0 - нет; 1- да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.has_limits IS 'Накладываются ли ограничения на данный план : 0 - нет; 1 - да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.has_payment_constraints IS 'Имеются ли ограничения по видам оплаты: 0-нет;1-да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.numb_group IS 'Группа сквозной нумерации';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.journal_type IS 'Тип журнала';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.is_oper IS 'Тип оперативности: 0 - консервативный; 1 - оперативный';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.hp_block IS 'Журнал заблокирован: 0-нет, 1-да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.close_date IS 'Дата окончания действия плана';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.open_date IS 'Дата начала дейтсвия плана';

COMMENT ON TABLE D_HOSP_PLAN_KINDS IS 'Виды планов госпитализации';
```

---

#### Таблица №2: D_HPK_JOURNAL_TYPES

```sql
CREATE TABLE D_HPK_JOURNAL_TYPES (
    jt_code numeric(1),
    jt_name character varying(60)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HPK_JOURNAL_TYPES.jt_code IS 'Код';
COMMENT ON COLUMN D_HPK_JOURNAL_TYPES.jt_name IS 'Наименование';

COMMENT ON TABLE D_HPK_JOURNAL_TYPES IS 'Типы журналов записей пациентов';
```

---

#### Таблица №3: D_HPK_PLANS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HPK_PLANS.id IS 'ID';
COMMENT ON COLUMN D_HPK_PLANS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_HPK_PLANS.pid IS 'Вид плана госпитализации';
COMMENT ON COLUMN D_HPK_PLANS.plan_date IS 'Дата плана';
COMMENT ON COLUMN D_HPK_PLANS.male_count IS 'Максимальное кол-во мужских мест';
COMMENT ON COLUMN D_HPK_PLANS.oper_count IS 'Максимальное кол-во оперативных больных';
COMMENT ON COLUMN D_HPK_PLANS.gen_count IS 'Общее количество';
COMMENT ON COLUMN D_HPK_PLANS.cid IS 'Каталог';

COMMENT ON TABLE D_HPK_PLANS IS 'Планы госпитализации';
```

---

#### Таблица №4: D_HPK_SCHEDULE

```sql
CREATE TABLE D_HPK_SCHEDULE (
    id bigint,
    lpu bigint,
    hosp_plan_kinds bigint,
    is_active numeric(1) DEFAULT 0,
    is_priority numeric(1) DEFAULT 0,
    schedule bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    over_limits numeric(1) DEFAULT 0,
    date_create timestamp without time zone
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HPK_SCHEDULE.id IS 'ID';
COMMENT ON COLUMN D_HPK_SCHEDULE.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_HPK_SCHEDULE.hosp_plan_kinds IS 'План госпитализации';
COMMENT ON COLUMN D_HPK_SCHEDULE.is_active IS 'Действующий: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.is_priority IS 'Приоритетный: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.schedule IS 'Шаблон графика';
COMMENT ON COLUMN D_HPK_SCHEDULE.date_begin IS 'Дата начала действия';
COMMENT ON COLUMN D_HPK_SCHEDULE.date_end IS 'Дата окончания действия';
COMMENT ON COLUMN D_HPK_SCHEDULE.over_limits IS 'Разрешать превышать лимит: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.date_create IS 'Дата создания';

COMMENT ON TABLE D_HPK_SCHEDULE IS 'Виды планов госпитализации : графики';
```

---

#### Таблица №5: D_SCHEDULE

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


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 4
- Всего уникальных таблиц: 4

### Связь вьюх и таблиц

**D_V_HOSP_PLAN_KINDS** использует таблицы:
- D_HOSP_PLAN_KINDS

**D_V_HPK_PLANS** использует таблицы:
- D_HPK_PLANS

**D_V_HPK_SCHEDULE_BASE** использует таблицы:
- D_HPK_SCHEDULE

**D_V_SCHEDULE_BASE** использует таблицы:
- D_SCHEDULE

### DDL определения таблиц

---

#### Таблица №1: D_HOSP_PLAN_KINDS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.ID IS 'ID';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HP_CODE IS 'Код';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HP_NAME IS 'Наименование';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.MAX_PRIOR IS 'На сколько дней вперед можно записывать в план';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.CID IS 'Каталог';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.MIN_AGE IS 'Минимальное ограничение по возрасту';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.MAX_AGE IS 'Максимальное ограничение по возрасту';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HAS_MKB_CONSTRAINTS IS 'Имеется ли ограничения по диагнозу : 0 - нет; 1- да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HAS_LIMITS IS 'Накладываются ли ограничения на данный план : 0 - нет; 1 - да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HAS_PAYMENT_CONSTRAINTS IS 'Имеются ли ограничения по видам оплаты: 0-нет;1-да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.NUMB_GROUP IS 'Группа сквозной нумерации';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.JOURNAL_TYPE IS 'Тип журнала';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.IS_OPER IS 'Тип оперативности: 0 - консервативный; 1 - оперативный';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.HP_BLOCK IS 'Журнал заблокирован: 0-нет, 1-да';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.CLOSE_DATE IS 'Дата окончания действия плана';
COMMENT ON COLUMN D_HOSP_PLAN_KINDS.OPEN_DATE IS 'Дата начала дейтсвия плана';

COMMENT ON TABLE D_HOSP_PLAN_KINDS IS 'Виды планов госпитализации';
```

---

#### Таблица №2: D_HPK_PLANS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HPK_PLANS.ID IS 'ID';
COMMENT ON COLUMN D_HPK_PLANS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_HPK_PLANS.PID IS 'Вид плана госпитализации';
COMMENT ON COLUMN D_HPK_PLANS.PLAN_DATE IS 'Дата плана';
COMMENT ON COLUMN D_HPK_PLANS.MALE_COUNT IS 'Максимальное кол-во мужских мест';
COMMENT ON COLUMN D_HPK_PLANS.OPER_COUNT IS 'Максимальное кол-во оперативных больных';
COMMENT ON COLUMN D_HPK_PLANS.GEN_COUNT IS 'Общее количество';
COMMENT ON COLUMN D_HPK_PLANS.CID IS 'Каталог';

COMMENT ON TABLE D_HPK_PLANS IS 'Планы госпитализации';
```

---

#### Таблица №3: D_HPK_SCHEDULE

```sql
CREATE TABLE D_HPK_SCHEDULE (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    HOSP_PLAN_KINDS NUMBER(17) NOT NULL,
    IS_ACTIVE NUMBER(1),
    IS_PRIORITY NUMBER(1),
    SCHEDULE NUMBER(17) NOT NULL,
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    OVER_LIMITS NUMBER(1),
    DATE_CREATE DATE NOT NULL,
    CONSTRAINT PK_D_HPK_SCHEDULE PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HPK_SCHEDULE.ID IS 'ID';
COMMENT ON COLUMN D_HPK_SCHEDULE.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_HPK_SCHEDULE.HOSP_PLAN_KINDS IS 'План госпитализации';
COMMENT ON COLUMN D_HPK_SCHEDULE.IS_ACTIVE IS 'Действующий: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.IS_PRIORITY IS 'Приоритетный: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.SCHEDULE IS 'Шаблон графика';
COMMENT ON COLUMN D_HPK_SCHEDULE.DATE_BEGIN IS 'Дата начала действия';
COMMENT ON COLUMN D_HPK_SCHEDULE.DATE_END IS 'Дата окончания действия';
COMMENT ON COLUMN D_HPK_SCHEDULE.OVER_LIMITS IS 'Разрешать превышать лимит: 0 - нет, 1 - да';
COMMENT ON COLUMN D_HPK_SCHEDULE.DATE_CREATE IS 'Дата создания';

COMMENT ON TABLE D_HPK_SCHEDULE IS 'Виды планов госпитализации : графики';
```

---

#### Таблица №4: D_SCHEDULE

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


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 10
- Загружено тел функций: 10

---

### Функция №1: D_PKG_CSE_ACCESSES.CHECK_RIGHT

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

### Функция №2: D_PKG_HPK_PLANS.SET_PLAN_FOR_DAY

```sql
-- Oracle PACKAGE: SET_PLAN_FOR_DAY
--======================================================================
procedure SET_PLAN_FOR_DAY
(
  pnLPU                                in NUMBER,
  pnPLAN                               in NUMBER,
  psDAY                                in VARCHAR2,
  pdSTART_DATE                         in DATE,
  pdEND_DATE                           in DATE
)
is
  rPLAN                 D_HPK_PLANS%rowtype;
  nLOOP_FLAG            NUMBER(1) := 0;
  dPLAN_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nID                   D_HPK_PLANS.ID%type;
begin
  if psDAY is not null and lower(psDAY) not in ('monday','tuesday','wednesday','thursday','friday','saturday','sunday') then
    D_P_EXC('Не верно задан день недели.');
  end if;
  --тащим план
  select t.*
    into rPLAN
    from D_HPK_PLANS t
   where t.ID  = pnPLAN
     and t.LPU = pnLPU;
  --making new plans for incoming dates
  if psDAY is not null then
    dPLAN_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(pdSTART_DATE,lower(psDAY)));
  else
    dPLAN_DATE := pdSTART_DATE;
  end if;
  loop
    exit when nLOOP_FLAG = 1;
    if dPLAN_DATE >= trunc(pdSTART_DATE) and dPLAN_DATE <= trunc(pdEND_DATE) then
      begin
        ADD(nID,pnLPU,rPLAN.PID,dPLAN_DATE,rPLAN.MALE_COUNT,rPLAN.OPER_COUNT,rPLAN.GEN_COUNT);
      exception
        when others then null;
      end;
```

---

### Функция №3: D_PKG_HPK_PLANS.SET_PLAN_FOR_WEEK

```sql
-- Oracle PACKAGE: SET_PLAN_FOR_WEEK
--======================================================================
procedure SET_PLAN_FOR_WEEK
(
  pnLPU                                in NUMBER,
  pnHOSP_PLAN                          in NUMBER,
  pdSTART_DATE                         in DATE,
  pdEND_DATE                           in DATE
)
is
  nHPK                  D_HOSP_PLAN_KINDS.ID%type;
  dPLAN_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nMALE_COUNT           D_HPK_PLANS.MALE_COUNT%type;
  nOPER_COUNT           D_HPK_PLANS.OPER_COUNT%type;
  nGEN_COUNT            D_HPK_PLANS.GEN_COUNT%type;
  dCURR_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nIND                  NUMBER(1);
  nNEW_PLAN_ID          D_HPK_PLANS.ID%type;
  nFLAG                 NUMBER(1);
begin
  if pnHOSP_PLAN is null then
    return;
  end if;
  if pdSTART_DATE >= pdEND_DATE or pdEND_DATE is null or pdSTART_DATE is null then
    D_P_EXC('Неверно указан временной интервал');
  end if; 
  --Выборка вида плана
  select PID,
         PLAN_DATE
    into nHPK,
         dPLAN_DATE
    from D_HPK_PLANS 
   where ID  = pnHOSP_PLAN
     and LPU = pnLPU;
  --Нахождение первого дня источника 
  dPLAN_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(trunc(dPLAN_DATE - 7),'monday'));
  if to_char(pdSTART_DATE,'day') <> 'monday' then 
    dCURR_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(trunc(pdSTART_DATE - 7),'monday'));
  else
    dCURR_DATE := trunc(pdSTART_DATE);
  end if;
  --Цикл по дням
  loop
    --Понедельный цикл
    for nIND in 0..6 
    loop
      begin
        select 1,
               MALE_COUNT,
               OPER_COUNT,
               GEN_COUNT
          into nFLAG,
               nMALE_COUNT,       
               nOPER_COUNT,  
               nGEN_COUNT 
          from D_HPK_PLANS
         where PID              = nHPK
           and trunc(PLAN_DATE) = trunc(dPLAN_DATE + nIND) 
           and LPU              = pnLPU;
      exception
        when NO_DATA_FOUND then nFLAG := 0;
      end;
```

---

### Функция №4: D_PKG_HPK_PLANS.DEL

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
  -- Поиск каталога --
  EXIST(pnID, pnLPU, nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'HPK_PLANS_DELETE',pnID);
  begin
    delete D_HPK_PLANS t
     where t.ID            = pnID
       and t.LPU           = pnLPU;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```

---

### Функция №5: D_PKG_HOSP_PLAN_KINDS.DEL

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
  -- Поиск каталога --
  EXIST(pnID, pnLPU, nCID);
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU,null,nCID,null,'HOSP_PLAN_KINDS_DELETE',pnID);
  begin
    delete D_HOSP_PLAN_KINDS t
     where t.ID            = pnID
       and t.LPU           = pnLPU;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```

---

### Функция №6: D_PKG_URPRIVS.CHECK_BPPRIV

```sql
-- Oracle PACKAGE: CHECK_BPPRIV
-- Возвращает: return boolean
--======================================================================
function CHECK_BPPRIV
(
  pnLPU                                in NUMBER,
  pnVERSION                            in NUMBER,
  psUNITCODE                           in VARCHAR2,
  pnCATALOG                            in NUMBER,
  psUNITBP                             in VARCHAR2,        --Действие в разделе
  pnRAISE                              in NUMBER default 1,--Генерировать ошибку    1 - да,0 - нет
  pnVER_LPU                            in NUMBER default 1 -- 0-запись на версию, 1-запись на ЛПУ+верия
)
return boolean
as
  iRESULT               integer;
  rUNIT                 D_UNITLIST%ROWTYPE;
  nUSER                 D_USERS.ID%type;
  sMESSAGE              D_PKG_STD.tLSTR;
  nVERSION              D_VERSIONS.ID%type;
  sCURRENT_USER         VARCHAR2(40) := D_F_GET_USERS();
begin
  -- инициализация результата
  iRESULT  := 1;
  -- считывание параметров раздела
  begin
    select u.UNITCODE,
           u.USE_CATALOGS,
           u.VER_LPU
      into rUNIT.UNITCODE,
           rUNIT.USE_CATALOGS,
           rUNIT.VER_LPU
      from D_UNITLIST u
     where u.UNITCODE = psUNITCODE;
  exception when NO_DATA_FOUND then
    D_PKG_MSG.UNIT_NOT_FOUND(psUNITCODE);
  end;
```

---

### Функция №7: D_PKG_CATALOGS.FIND_ROOT_CATALOG

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

### Функция №8: D_PKG_HPK_PLANS.COPY_PLAN_FOR_DAY

```sql
-- Oracle PACKAGE: COPY_PLAN_FOR_DAY
--======================================================================
procedure COPY_PLAN_FOR_DAY
(
  pnLPU                                in NUMBER,
  pnPLAN                               in NUMBER,
  psDAY                                in VARCHAR2,
  pdSTART_DATE                         in DATE,
  pdEND_DATE                           in DATE
)
is
  rSAMPLE_PLAN          D_HPK_PLANS%rowtype;
  nLOOP_FLAG            NUMBER(1) := 0;
  dPLAN_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nID                   D_HPK_PLANS.ID%type;
begin
  if psDAY is not null and lower(psDAY) not in ('monday','tuesday','wednesday','thursday','friday','saturday','sunday') then
    D_P_EXC('Не верно задан день недели.');
  end if;
  --тащим план
  select t.*
    into rSAMPLE_PLAN
    from D_HPK_PLANS t
   where t.ID  = pnPLAN
     and t.LPU = pnLPU;
  --making new plans for incoming dates
  if psDAY is not null then
    dPLAN_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(pdSTART_DATE,lower(psDAY)));
  else
    dPLAN_DATE := pdSTART_DATE;
  end if;
  loop
    exit when nLOOP_FLAG = 1;
    if dPLAN_DATE >= trunc(pdSTART_DATE) and dPLAN_DATE <= trunc(pdEND_DATE) then
      begin
        select hp.ID
          into nID
          from D_HPK_PLANS hp
         where hp.LPU       = pnLPU
           and hp.PID       = rSAMPLE_PLAN.PID
           and hp.PLAN_DATE = dPLAN_DATE;
        UPD(nID,pnLPU,dPLAN_DATE,rSAMPLE_PLAN.MALE_COUNT, rSAMPLE_PLAN.OPER_COUNT, rSAMPLE_PLAN.GEN_COUNT);
      exception
        when others then null;
      end;
```

---

### Функция №9: D_PKG_HPK_PLANS.COPY_PLAN_FOR_WEEK

```sql
-- Oracle PACKAGE: COPY_PLAN_FOR_WEEK
--======================================================================
procedure COPY_PLAN_FOR_WEEK
(
  pnLPU                                in NUMBER,
  pnHOSP_PLAN                          in NUMBER,
  pdSTART_DATE                         in DATE,
  pdEND_DATE                           in DATE
)
is
  nHPK                  D_HOSP_PLAN_KINDS.ID%type;
  dPLAN_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nMALE_COUNT           D_HPK_PLANS.MALE_COUNT%type;
  nOPER_COUNT           D_HPK_PLANS.OPER_COUNT%type;
  nGEN_COUNT            D_HPK_PLANS.GEN_COUNT%type;
  dCURR_DATE            D_HPK_PLANS.PLAN_DATE%type;
  nIND                  NUMBER(1);
  nID                   D_HPK_PLANS.ID%type;
  nFLAG                 NUMBER(1);
begin
  if pnHOSP_PLAN is null then
    return;
  end if;
  if pdSTART_DATE >= pdEND_DATE or pdEND_DATE is null or pdSTART_DATE is null then
    D_P_EXC('Неверно указан временной интервал');
  end if; 
  --Выборка вида плана
  select PID,
         PLAN_DATE
    into nHPK,
         dPLAN_DATE
    from D_HPK_PLANS 
   where ID  = pnHOSP_PLAN
     and LPU = pnLPU;
  --Нахождение первого дня источника 
  dPLAN_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(trunc(dPLAN_DATE - 7),'monday'));
  if to_char(pdSTART_DATE,'day') <> 'monday' then 
    dCURR_DATE := trunc(D_PKG_DAT_TOOLS.NEXT_WEEK_DAY(trunc(pdSTART_DATE - 7),'monday'));
  else
    dCURR_DATE := trunc(pdSTART_DATE);
  end if;
  --Цикл по дням
  loop
    --Понедельный цикл
    for nIND in 0..6 
    loop
      begin
        select 1,
               MALE_COUNT,
               OPER_COUNT,
               GEN_COUNT
          into nFLAG,
               nMALE_COUNT,       
               nOPER_COUNT,  
               nGEN_COUNT 
          from D_HPK_PLANS
         where PID              = nHPK
           and trunc(PLAN_DATE) = trunc(dPLAN_DATE + nIND) 
           and LPU              = pnLPU;
      exception
        when NO_DATA_FOUND then nFLAG := 0;
      end;
```

---

### Функция №10: D_PKG_HPK_SCHEDULE.DEL

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
  D_PKG_BPENV.BEFOREBP(pnLPU, null, null, null, 'HPK_SCHEDULE_DELETE', pnID);
  begin
    delete D_HPK_SCHEDULE hse where hse.ID = pnID;
  exception when others then D_PKG_MSG.IUD_ERRORS(sqlerrm, 'D', sqlcode);
  end;
```


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 10
- Загружено тел функций: 10

---

### Функция №1: d_pkg_cse_accesses.check_right

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

### Функция №2: d_pkg_hpk_plans.set_plan_for_day

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.set_plan_for_day(IN pnlpu numeric, IN pnplan numeric, IN psday character varying, IN pdstart_date timestamp without time zone, IN pdend_date timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    rPLAN d_hpk_plans;
    nLOOP_FLAG NUMERIC(1) := 0;
    dPLAN_DATE d_hpk_plans.plan_date%TYPE;
    nID d_hpk_plans.id%TYPE;
    or2pgTmpVar0_1 numeric;
BEGIN
    IF nullif(psday,'') IS NOT NULL
     AND lower(psday) NOT  IN ( 'monday' , 'tuesday' , 'wednesday' , 'thursday' , 'friday' , 'saturday' , 'sunday' ) THEN
        PERFORM d_p_exc(1,'Не верно задан день недели.');

    END IF;
    -- тащим план
    SELECT
        t.*
    INTO STRICT rplan
    FROM
        d_hpk_plans t
    WHERE
        t.id = pnplan::bigint
             AND t.lpu = pnlpu::bigint;
    -- making new plans for incoming dates
        IF nullif(psday,'') IS NOT NULL THEN
        dplan_date := trunc(d_pkg_dat_tools.next_week_day(pdstart_date,lower(psday)::varchar));

    ELSE
        dplan_date := pdstart_date;

    END IF;
    
    LOOP
        EXIT WHEN nloop_flag = 1;
        IF dplan_date >= trunc(pdstart_date)
     AND dplan_date <= trunc(pdend_date) THEN
            BEGIN
                or2pgTmpVar0_1 := (nid)::numeric;
                 CALL d_pkg_hpk_plans.add(or2pgTmpVar0_1, pnlpu, (rplan.pid)::numeric, dplan_date, rplan.male_count, rplan.oper_count, rplan.gen_count);
                nid := (or2pgTmpVar0_1)::bigint;
                EXCEPTION
                    WHEN others THEN
                                null;

            END;
            IF nullif(psday,'') IS NOT NULL THEN
                dplan_date := trunc(d_pkg_dat_tools.next_week_day(dplan_date,lower(psday)::varchar));

            ELSE
                dplan_date := dplan_date + 1;

            END IF;

        ELSE
            nloop_flag := 1;

        END IF;
    END LOOP;
END
$procedure$
```

---

### Функция №3: d_pkg_hpk_plans.set_plan_for_week

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.set_plan_for_week(IN pnlpu numeric, IN pnhosp_plan numeric, IN pdstart_date timestamp without time zone, IN pdend_date timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nHPK d_hosp_plan_kinds.id%TYPE;
    dPLAN_DATE d_hpk_plans.plan_date%TYPE;
    nMALE_COUNT d_hpk_plans.male_count%TYPE;
    nOPER_COUNT d_hpk_plans.oper_count%TYPE;
    nGEN_COUNT d_hpk_plans.gen_count%TYPE;
    dCURR_DATE d_hpk_plans.plan_date%TYPE;
    nIND NUMERIC(1);
    nNEW_PLAN_ID d_hpk_plans.id%TYPE;
    nFLAG NUMERIC(1);
    or2pgTmpVar0_1 numeric;
BEGIN
    IF pnhosp_plan IS NULL THEN
        return;

    END IF;
    IF pdstart_date >= pdend_date
     OR pdend_date IS NULL
     OR pdstart_date IS NULL THEN
        PERFORM d_p_exc(1,'Неверно указан временной интервал');

    END IF;
    -- Выборка вида плана
    SELECT
        pid,
        plan_date
    INTO STRICT nhpk, dplan_date
    FROM
        d_hpk_plans
    WHERE
        id = pnhosp_plan::bigint
             AND lpu = pnlpu::bigint;
    dplan_date := trunc(d_pkg_dat_tools.next_week_day(trunc(dplan_date - 7)::timestamp,'monday'));
    IF to_char(pdstart_date,'TMday') <> 'monday' THEN
        dcurr_date := trunc(d_pkg_dat_tools.next_week_day(trunc(pdstart_date - 7)::timestamp,'monday'));

    ELSE
        dcurr_date := trunc(pdstart_date);

    END IF;
    -- Цикл по дням
    
    LOOP
        -- Понедельный цикл
         FOR nind IN 0 .. 6
        LOOP
            SELECT
                1  "1",
                male_count,
                oper_count,
                gen_count
            INTO nflag, nmale_count, noper_count, ngen_count
            FROM
                d_hpk_plans
            WHERE
                pid = nhpk
                     AND trunc(plan_date) = trunc(dplan_date + nind)
                     AND lpu = pnlpu::bigint;
            IF NOT FOUND THEN
                nflag := 0;

            END IF;
            -- Добавление плана
                        IF nflag = 1 THEN
                BEGIN
                    or2pgTmpVar0_1 := (nnew_plan_id)::numeric;
                     CALL d_pkg_hpk_plans.add(or2pgTmpVar0_1, pnlpu, (nhpk)::numeric, trunc(dcurr_date)::timestamp, nmale_count, noper_count, ngen_count);
                    nnew_plan_id := (or2pgTmpVar0_1)::bigint;
                    EXCEPTION
                        WHEN others THEN
                                    null;

                END;

            END IF;
            dcurr_date := dcurr_date + 1;
        END LOOP;
        EXIT WHEN trunc(dcurr_date) > trunc(pdend_date);
    END LOOP;
END
$procedure$
```

---

### Функция №4: d_pkg_hpk_plans.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    CALL d_pkg_hpk_plans.exist(pnid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLANS_DELETE', pnid);
    BEGIN
        DELETE FROM d_hpk_plans t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_PLANS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HPK_PLANS_DELETE', pnid);
END
$procedure$
```

---

### Функция №5: d_pkg_hosp_plan_kinds.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hosp_plan_kinds.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nCID numeric(17);
BEGIN
    CALL d_pkg_hosp_plan_kinds.exist(pnid, pnlpu, ncid);
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HOSP_PLAN_KINDS_DELETE', pnid);
    BEGIN
        DELETE FROM d_hosp_plan_kinds t where t.id = pnid::bigint
     AND t.lpu = pnlpu::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HOSP_PLAN_KINDS');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, ncid, (null)::numeric, 'HOSP_PLAN_KINDS_DELETE', pnid);
END
$procedure$
```

---

### Функция №6: d_pkg_urprivs.check_bppriv

```sql
CREATE OR REPLACE FUNCTION d_pkg_urprivs.check_bppriv(pnlpu numeric, psunitbp character varying, pncatalog numeric, pnraise numeric DEFAULT 1)
 RETURNS numeric
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    nVER_LPU d_unitlist.ver_lpu%TYPE;
    nVERSION d_versions.id%TYPE;
    sUNITCODE d_unitlist.unitcode%TYPE;
    nRES NUMERIC(1);
    or2pgTmpVar0_1 numeric;
BEGIN
    SELECT
        t.ver_lpu,
        t.unitcode
    INTO nver_lpu, sunitcode
    FROM
        d_unitbps u
        CROSS JOIN         d_unitlist t
    WHERE
        u.unitbpcode = psunitbp
             AND t.unitcode = u.unitcode;
    IF NOT FOUND THEN
        IF pnraise = 1 THEN
            PERFORM d_pkg_msg.record_not_found(1,'UNITBPS','Действие над разделом',psunitbp);

        ELSE
            return 0;

        END IF;

    END IF;
    IF nver_lpu = 0 THEN
        or2pgTmpVar0_1 := (nversion)::numeric;
         CALL d_pkg_versions.get_version_by_lpu(pnraise, pnlpu, sunitcode, or2pgTmpVar0_1);
        nversion := (or2pgTmpVar0_1)::bigint;

    END IF;
    IF d_pkg_urprivs.check_bppriv(pnlpu,(nversion)::numeric,sunitcode,pncatalog,psunitbp,pnraise) = true THEN
        nres := 1;

    ELSE
        nres := 0;

    END IF;
    return nres;
END
$function$
```

---

### Функция №7: d_pkg_catalogs.find_root_catalog

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

### Функция №8: d_pkg_hpk_plans.copy_plan_for_day

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.copy_plan_for_day(IN pnlpu numeric, IN pnplan numeric, IN psday character varying, IN pdstart_date timestamp without time zone, IN pdend_date timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    rSAMPLE_PLAN d_hpk_plans;
    nLOOP_FLAG NUMERIC(1) := 0;
    dPLAN_DATE d_hpk_plans.plan_date%TYPE;
    nID d_hpk_plans.id%TYPE;
BEGIN
    IF nullif(psday,'') IS NOT NULL
     AND lower(psday) NOT  IN ( 'monday' , 'tuesday' , 'wednesday' , 'thursday' , 'friday' , 'saturday' , 'sunday' ) THEN
        PERFORM d_p_exc(1,'Не верно задан день недели.');

    END IF;
    -- тащим план
    SELECT
        t.*
    INTO STRICT rsample_plan
    FROM
        d_hpk_plans t
    WHERE
        t.id = pnplan::bigint
             AND t.lpu = pnlpu::bigint;
    -- making new plans for incoming dates
        IF nullif(psday,'') IS NOT NULL THEN
        dplan_date := trunc(d_pkg_dat_tools.next_week_day(pdstart_date,lower(psday)::varchar));

    ELSE
        dplan_date := pdstart_date;

    END IF;
    
    LOOP
        EXIT WHEN nloop_flag = 1;
        IF dplan_date >= trunc(pdstart_date)
     AND dplan_date <= trunc(pdend_date) THEN
            BEGIN
                SELECT
                    hp.id
                INTO STRICT nid
                FROM
                    d_hpk_plans hp
                WHERE
                    hp.lpu = pnlpu::bigint
                         AND hp.pid = rsample_plan.pid
                         AND hp.plan_date = dplan_date;
                CALL d_pkg_hpk_plans.upd((nid)::numeric, pnlpu, dplan_date, rsample_plan.male_count, rsample_plan.oper_count, rsample_plan.gen_count);
                EXCEPTION
                    WHEN others THEN
                                null;

            END;
            IF nullif(psday,'') IS NOT NULL THEN
                dplan_date := trunc(d_pkg_dat_tools.next_week_day(dplan_date,lower(psday)::varchar));

            ELSE
                dplan_date := dplan_date + 1;

            END IF;

        ELSE
            nloop_flag := 1;

        END IF;
    END LOOP;
END
$procedure$
```

---

### Функция №9: d_pkg_hpk_plans.copy_plan_for_week

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_plans.copy_plan_for_week(IN pnlpu numeric, IN pnhosp_plan numeric, IN pdstart_date timestamp without time zone, IN pdend_date timestamp without time zone)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nHPK d_hosp_plan_kinds.id%TYPE;
    dPLAN_DATE d_hpk_plans.plan_date%TYPE;
    nMALE_COUNT d_hpk_plans.male_count%TYPE;
    nOPER_COUNT d_hpk_plans.oper_count%TYPE;
    nGEN_COUNT d_hpk_plans.gen_count%TYPE;
    dCURR_DATE d_hpk_plans.plan_date%TYPE;
    nIND NUMERIC(1);
    nID d_hpk_plans.id%TYPE;
    nFLAG NUMERIC(1);
BEGIN
    IF pnhosp_plan IS NULL THEN
        return;

    END IF;
    IF pdstart_date >= pdend_date
     OR pdend_date IS NULL
     OR pdstart_date IS NULL THEN
        PERFORM d_p_exc(1,'Неверно указан временной интервал');

    END IF;
    -- Выборка вида плана
    SELECT
        pid,
        plan_date
    INTO STRICT nhpk, dplan_date
    FROM
        d_hpk_plans
    WHERE
        id = pnhosp_plan::bigint
             AND lpu = pnlpu::bigint;
    dplan_date := trunc(d_pkg_dat_tools.next_week_day(trunc(dplan_date - 7)::timestamp,'monday'));
    IF to_char(pdstart_date,'TMday') <> 'monday' THEN
        dcurr_date := trunc(d_pkg_dat_tools.next_week_day(trunc(pdstart_date - 7)::timestamp,'monday'));

    ELSE
        dcurr_date := trunc(pdstart_date);

    END IF;
    -- Цикл по дням
    
    LOOP
        -- Понедельный цикл
         FOR nind IN 0 .. 6
        LOOP
            SELECT
                1  "1",
                male_count,
                oper_count,
                gen_count
            INTO nflag, nmale_count, noper_count, ngen_count
            FROM
                d_hpk_plans
            WHERE
                pid = nhpk
                     AND trunc(plan_date) = trunc(dplan_date + nind)
                     AND lpu = pnlpu::bigint;
            IF NOT FOUND THEN
                nflag := 0;

            END IF;
            -- Добавление плана
                        IF nflag = 1 THEN
                BEGIN
                    SELECT
                        hp.id
                    INTO STRICT nid
                    FROM
                        d_hpk_plans hp
                    WHERE
                        hp.lpu = pnlpu::bigint
                             AND hp.pid = nhpk
                             AND hp.plan_date = trunc(dcurr_date);
                    CALL d_pkg_hpk_plans.upd((nid)::numeric, pnlpu, trunc(dcurr_date)::timestamp, nmale_count, noper_count, ngen_count);
                    EXCEPTION
                        WHEN others THEN
                                    null;

                END;

            END IF;
            dcurr_date := dcurr_date + 1;
        END LOOP;
        EXIT WHEN trunc(dcurr_date) > trunc(pdend_date);
    END LOOP;
END
$procedure$
```

---

### Функция №10: d_pkg_hpk_schedule.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_hpk_schedule.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HPK_SCHEDULE_DELETE', pnid);
    BEGIN
        DELETE FROM d_hpk_schedule hse where hse.id = pnid::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'HPK_SCHEDULE');

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'HPK_SCHEDULE_DELETE', pnid);
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
