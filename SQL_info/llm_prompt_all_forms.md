# ЗАПРОС К LLM: АНАЛИЗ ФОРМ T-MIS

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Mon May 18 22:01:16 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 5
- Всего форм: 3

---

### Запрос №1

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_RELATIVES
**Источник:** Forms/HospPlan/hp_relativechoise.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hp_relativechoise.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_RELATIVES" mode="Range">
        <component cmptype="DataSetRouter" condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            select rel.ID,
                   rel.SURNAME,
                   rel.FIRSTNAME,
                   rel.LASTNAME,
                   rel.BIRTHDATE,
                   rel.RELATIONSHIP_NAME,
                   rel.SURNAME || ' ' || rel.FIRSTNAME || ' ' || rel.LASTNAME as REL_FIO,
                   rel.PID,
                   rel.AGENT_ID
              from D_V_AGENTS_PERSMEDCARD pmc,
                   D_V_AGENT_RELATIVES rel
             where pmc.ID_PERSMEDCARD = to_number(:pnPATIENTID)
               and pmc.ID = rel.PID
            ]]>
        </component>
        <component cmptype="DataSetRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            select rel.ID,
                   rel.SURNAME,
                   rel.FIRSTNAME,
                   rel.LASTNAME,
                   rel.BIRTHDATE,
                   rel.RELATIONSHIP_NAME,
                   rel.SURNAME || ' ' || rel.FIRSTNAME || ' ' || rel.LASTNAME as REL_FIO,
                   rel.PID,
                   rel.AGENT_ID
              from D_V_AGENTS_PERSMEDCARD pmc,
                   D_V_AGENT_RELATIVES rel
             where pmc.ID_PERSMEDCARD = (:pnPATIENTID)::numeric
               and pmc.ID = rel.PID
            ]]>
        </component>
        <component cmptype="Variable" name="pnPATIENTID" src="PATIENT_ID" srctype="var" />
        <component cmptype="Variable" type="count" src="ds8count" default="5" />
        <component cmptype="Variable" type="start" src="ds8start" default="1" />
    </component>
```

**Используемые таблицы/вьюхи:** D_V_AGENTS_PERSMEDCARD

---

### Запрос №2

**Тип компонента:** M2 Action
**Имя компонента:** acGetAgentId
**Источник:** Forms/HospPlan/hp_relativechoise.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hp_relativechoise.frm

**SQL код:**

```xml
<component cmptype="Action" name="acGetAgentId">
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            begin
              select t.AGENT
                into :pnAGENT
                from D_V_PERSMEDCARD t
               where t.ID = to_number(:pnPATIENT);
            end;
            ]]>
        </component>
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            begin
              select t.AGENT
                into :pnAGENT
                from D_V_PERSMEDCARD t
               where t.ID = (:pnPATIENT)::numeric;
            end;
            ]]>
        </component>
        <component cmptype="ActionVar" name="pnPATIENT" src="PATIENT_ID" srctype="var" />
        <component cmptype="ActionVar" name="pnAGENT" src="AGENT" srctype="var" put="" len="17" />
    </component>
```

**Используемые таблицы/вьюхи:** D_V_PERSMEDCARD

---

### Запрос №3

**Тип компонента:** M2 Action
**Имя компонента:** acDelRelative
**Источник:** Forms/HospPlan/hp_relativechoise.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\HospPlan\hp_relativechoise.frm

**SQL код:**

```xml
<component cmptype="Action" name="acDelRelative">
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            begin
              D_PKG_AGENT_RELATIVES.DEL(pnID  => to_number(:pnIDD),
                                        pnLPU => to_number(:pnLPU));
            end;
            ]]>
        </component>
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis">
            <![CDATA[
            declare
              nIDD numeric;
            begin
              nIDD := (:pnIDD)::numeric;

              call D_PKG_AGENT_RELATIVES.DEL(pnID  => nIDD,
                                            pnLPU => (:pnLPU)::numeric);

              :pnIDD := nIDD;
            end;
            ]]>
        </component>
        <component cmptype="ActionVar" name="pnLPU" src="LPU" srctype="session" />
        <component cmptype="ActionVar" name="pnIDD" src="GR_RELATIVES" srctype="ctrl" />
    </component>
```

**Используемые пакеты/функции:** D_PKG_AGENT_RELATIVES.DEL

---

### Запрос №4

**Тип компонента:** M2 Action
**Имя компонента:** getOptionValue
**Источник:** Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\ArmPatientsInDep\SubForms\hh_mp_prescribes.frm

**SQL код:**

```xml
<component cmptype="Action" name="getOptionValue">
		begin
		  :ADD_MED_NAZN_FROM_HH := D_PKG_OPTIONS.GET('AddMedNaznFromHH', to_number(:LPU));
          :CHECK_ONKO_PRESC_EDIT := D_PKG_OPTIONS.GET(psSO_CODE =&gt; 'CheckHospOnkoPrescribesEdit',
                                                      pnLPU     =&gt; to_number(:LPU),
                                                      pnRAISE   =&gt; 0);

                D_PKG_OPTIONS.GET(psSO_CODE =&gt; 'RegOKSCode', pnLPU =&gt; :LPU);
               D_PKG_OPTIONS.GET(psSO_CODE =&gt; 'RegStrokeCode', pnLPU =&gt; :pnLPU, pnRAISE =&gt; 0)

           :PKOMS_OPT := D_PKG_OPTION_SPECS.GET('PKOMS', :LPU);
           :ADDDIRBEDPROFILE := D_PKG_OPTIONS.GET('AddDirBedProfile', :LPU);
		end;
		<component cmptype="ActionVar" name="LPU" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="ADD_MED_NAZN_FROM_HH" src="AddMedNaznFromHH" srctype="var" put="" len="1" />
        <component cmptype="ActionVar" name="CHECK_ONKO_PRESC_EDIT" src="checkOnkoPrescEdit" srctype="var" put="" len="1" />
	</component>
```


---

### Запрос №5

**Тип компонента:** M2 Action
**Имя компонента:** cancelConfirmationPrescr
**Источник:** Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm
**Базовая форма:** C:\AppServ\www\5_mis_MEDDEV-151210\Forms\ArmPatientsInDep\SubForms\hh_mp_prescribes.frm

**SQL код:**

```xml
<component cmptype="Action" name="cancelConfirmationPrescr">
        <![CDATA[
	  	begin
            for cur in (select t.ID
                          from D_V_MP_PRESCRIBES t
                         where instr(';' || :IDS || ';', ';' || t.ID || ';') <> 0)
            loop
                d_pkg_mp_prescribes.set_mp_condition(cur.ID, :LPU, 0, null);
            end loop;
	  	end;
        ]]>
	  	<component cmptype="ActionVar" name="LPU" src="LPU" srctype="session" />
		<component cmptype="ActionVar" name="IDS" src="IDS" srctype="var" get="gIDS" />
	</component>
```

**Используемые таблицы/вьюхи:** D_V_MP_PRESCRIBES
**Используемые пакеты/функции:** D_PKG_MP_PRESCRIBES.SET_MP_CONDITION


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 3

---

### Вьюха №1: D_V_AGENTS_PERSMEDCARD

**Используется в формах:**
- Forms/HospPlan/hp_relativechoise.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_AGENTS_PERSMEDCARD
 SELECT t1.id,
    t1.surname,
    t1.firstname,
    t1.lastname,
    t1.birthdate,
    t1.sex,
    t2.id AS id_persmedcard,
    t2.card_numb,
    t1.snils,
    t2.lpu,
    t1.version,
    t1.enp
   FROM d_agents t1
     CROSS JOIN d_persmedcard t2
  WHERE t1.id = t2.agent AND t1.agn_type = 1::numeric AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t2.cid AND ur.unitcode::text = 'PERSMEDCARD'::text))
UNION
 SELECT t1.id,
    t1.surname,
    t1.firstname,
    t1.lastname,
    t1.birthdate,
    t1.sex,
    NULL::bigint AS id_persmedcard,
    NULL::character varying AS card_numb,
    NULL::character varying AS snils,
    t2.id AS lpu,
    t1.version,
    t1.enp
   FROM d_agents t1
     CROSS JOIN d_lpu t2
  WHERE t1.agn_type = 1::numeric AND NOT (EXISTS ( SELECT NULL::text AS "null"
           FROM d_persmedcard pmc
          WHERE pmc.agent = t1.id AND pmc.lpu = t2.id)) AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = t1.cid AND ur.unitcode::text = 'AGENTS'::text));
```

---

### Вьюха №2: D_V_PERSMEDCARD

**Используется в формах:**
- Forms/HospPlan/hp_relativechoise.frm

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

### Вьюха №3: D_V_MP_PRESCRIBES

**Используется в формах:**
- Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_MP_PRESCRIBES
 SELECT prsc.id,
    prsc.lpu,
    prsc.diseasecase,
    prsc.direction_service,
    prsc.mp_type AS mp_type_code,
    ( SELECT tp.mpt_name
           FROM d_mp_types tp
          WHERE tp.mpt_code = prsc.mp_type) AS mp_type_name,
    prsc.nommodif AS nommodif_id,
    nmm.mod_code AS nommodif_code,
    nmm.mod_name AS nommodif_name,
    nmm.dose AS nommodif_dose,
    nmm.dose_default AS nommodif_dose_default,
    nmm.dose_pack AS nommodif_dose_pack,
    nmm.acm_dose AS nommodif_acm_dose,
    nmm.acm_dose_pack AS nommodif_acm_dose_pack,
    ( SELECT dmsr.mnemocode
           FROM d_dose_measures dmsr
          WHERE dmsr.id = nmm.acm_dose_measure) AS nommodif_acm_dose_measure,
    nmm.angro,
    nmm.pid AS nombase_id,
    nmm.koeff,
    prsc.dose,
    prsc.dose_measure AS dose_measure_id,
    ( SELECT dmsr.mnemocode
           FROM d_dose_measures dmsr
          WHERE dmsr.id = prsc.dose_measure) AS dose_measure,
    prsc.dose_quant,
    prsc.mp_describe AS mp_describe_id,
    dsc.d_code AS mp_describe_code,
    dsc.d_name AS mp_describe_name,
    dsc.d_type AS mp_describe_type,
    prsc.med_use_method AS med_use_method_id,
    um.mum_code AS med_use_method_code,
    um.mum_name AS med_use_method_name,
    prsc.date_begin,
    prsc.date_end,
    prsc.date_cancel,
    prsc.date_create,
    prsc.mp_condition AS mp_condition_code,
    ( SELECT cnd.mpc_name
           FROM d_mp_conditiones cnd
          WHERE cnd.mpc_code = prsc.mp_condition) AS mp_condition_name,
    prsc.cancel_reason,
    prsc.employer AS employer_id,
    emp.kod_vracha AS employer_code,
    d_pkg_str_tools.fio(fssurname => a.surname::character varying, fsname => a.firstname::character varying, fspatrname => a.lastname::character varying) AS employer,
    prsc.ma_attribute AS ma_attribute_id,
    ( SELECT aat.aa_code
           FROM d_mp_add_attributes aat
          WHERE aat.id = prsc.ma_attribute) AS ma_attribute,
    prsc.is_pat_nombase,
    prsc.commentary,
    d_pkg_acc_farm_prod.get_rounded_value(pnnommodif => prsc.nommodif::numeric, pnlpu => prsc.lpu::numeric, pnvalue => prsc.quantity::numeric) AS quantity,
    ( SELECT msr.mnemocode
           FROM d_measures msr
          WHERE msr.id = nmm.main_measure) AS nommodif_main_measure,
    prsc.use_until_close,
    prsc.hh_dep,
    prsc.patient AS patient_id,
    prsc.cancel_ds,
    prsc.cancel_employer,
    prsc.hid,
    d_pkg_acc_farm_prod.get_rounded_value(pnnommodif => prsc.nommodif::numeric, pnlpu => prsc.lpu::numeric, pnvalue => prsc.alt_dose::numeric) AS alt_dose,
    ( SELECT msr.mnemocode
           FROM d_measures msr
          WHERE msr.id = nmm.alt_measure) AS alt_measure_code,
    prsc.chemotherapy,
    prsc.nommodif_str,
    prsc.is_recipe,
        CASE prsc.is_recipe
            WHEN 1 THEN 'Да'::character varying
            ELSE 'Нет'::character varying
        END AS is_recipe_str,
    prsc.mts_desc,
    prsc.is_onko,
    prsc.rtub_chemophase,
    prsc.duration,
    prsc.duration_measure,
    prsc.date_affirm,
    prsc.priority,
    prsc.number_ser AS implant_ser_number,
    prsc.reaction,
    prsc.mp_class,
    prsc.reas_cancel AS reas_cancel_id,
    ( SELECT rcp.reas_name
           FROM d_reason_cancel_prescribe rcp
          WHERE rcp.id = prsc.reas_cancel) AS reas_cancel_name,
    prsc.speed,
    ( SELECT mnn.mnn_name_rus
           FROM d_nombase_mnn mnn
          WHERE mnn.id = prsc.mnn) AS mnn_name_rus,
    prsc.mnn AS mnn_id,
    prsc.med_forms,
    prsc.red_inj,
    ( SELECT hhd.dep
           FROM d_hosp_history_deps hhd
          WHERE hhd.id = prsc.hh_dep) AS dep_id
   FROM d_mp_prescribes prsc
     JOIN d_employers emp ON emp.id = prsc.employer
     JOIN d_agents a ON a.id = emp.agent
     LEFT JOIN d_nommodif nmm ON nmm.id = prsc.nommodif AND prsc.nommodif IS NOT NULL
     LEFT JOIN d_mp_describes dsc ON dsc.id = prsc.mp_describe AND prsc.mp_describe IS NOT NULL
     LEFT JOIN d_med_use_methods um ON um.id = prsc.med_use_method AND prsc.med_use_method IS NOT NULL
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = prsc.lpu AND ur.unitcode::text = 'MP_PRESCRIBES'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 3

---

### Вьюха №1: D_V_AGENTS_PERSMEDCARD

**Используется в формах:**
- Forms/HospPlan/hp_relativechoise.frm

**DDL определение:**

```sql
-- Oracle View: D_V_AGENTS_PERSMEDCARD
select --Представление для раздела : персональные медицинские карты контрагентов
       t1.ID,
       t1.SURNAME,
       t1.FIRSTNAME,
       t1.LASTNAME,
       t1.BIRTHDATE,
       t1.SEX,
       t2.ID        ID_PERSMEDCARD,
       t2.CARD_NUMB,
       t1.SNILS,
       t2.LPU,
       t1.VERSION,
       t1.ENP
  from D_AGENTS      t1,      -- Контрагенты
       D_PERSMEDCARD t2       -- Карта пациента
where t1.ID        = t2.AGENT
  and t1.AGN_TYPE  = 1
  and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t2.CID and ur.UNITCODE = 'PERSMEDCARD')
union
select t1.ID,
       t1.SURNAME,
       t1.FIRSTNAME,
       t1.LASTNAME,
       t1.BIRTHDATE,
       t1.SEX,
       null  ID_PERSMEDCARD,
       null  CARD_NUMB,
       null  SNILS,
       t2.ID LPU,
       t1.VERSION,
       t1.ENP
  from D_AGENTS      t1,      -- Контрагенты
       D_LPU         t2       -- ЛПУ
where t1.AGN_TYPE  = 1
  and not exists (select null from D_PERSMEDCARD pmc where pmc.AGENT = t1.ID and pmc.LPU = t2.ID)
  and exists (select null from D_V_URPRIVS ur where ur.CATALOG = t1.CID and ur.UNITCODE = 'AGENTS')

 
```

---

### Вьюха №2: D_V_PERSMEDCARD

**Используется в формах:**
- Forms/HospPlan/hp_relativechoise.frm

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

### Вьюха №3: D_V_MP_PRESCRIBES

**Используется в формах:**
- Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm

**DDL определение:**

```sql
-- Oracle View: D_V_MP_PRESCRIBES
select prsc.ID                                                as ID,                          -- ID D_MP_PRESCRIBES
         prsc.LPU                                               as LPU,                         -- ЛПУ
         prsc.DISEASECASE                                       as DISEASECASE,                 -- Случай заболевания
         prsc.DIRECTION_SERVICE                                 as DIRECTION_SERVICE,           -- Направление на услугу
         prsc.MP_TYPE                                           as MP_TYPE_CODE,                -- Тип назначения
         (select tp.MPT_NAME
            from D_MP_TYPES tp
           where tp.MPT_CODE = prsc.MP_TYPE)                    as MP_TYPE_NAME,                -- Типы назначений лекарственных средств
         prsc.NOMMODIF                                          as NOMMODIF_ID,                 -- Модификация номенклатуры
         nmm.MOD_CODE                                           as NOMMODIF_CODE,               -- Мнемокод
         nmm.MOD_NAME                                           as NOMMODIF_NAME,               -- Наименование
         nmm.DOSE                                               as NOMMODIF_DOSE,               -- Разовая доза
         nmm.DOSE_DEFAULT                                       as NOMMODIF_DOSE_DEFAULT,       -- Разовая доза по умолчанию
         nmm.DOSE_PACK                                          as NOMMODIF_DOSE_PACK,          -- Количество доз в одной основной единице измерения (ОЕИ)
         nmm.ACM_DOSE                                           as NOMMODIF_ACM_DOSE,           -- Дозировка действующих веществ
         nmm.ACM_DOSE_PACK                                      as NOMMODIF_ACM_DOSE_PACK,      -- Количество доз действующего вещества в ОЕИ
         (select dmsr.MNEMOCODE
            from D_DOSE_MEASURES dmsr
           where dmsr.ID = nmm.ACM_DOSE_MEASURE)                as NOMMODIF_ACM_DOSE_MEASURE,   -- Единицы измерения дозировки ЛС
         nmm.ANGRO                                              as ANGRO,                       -- Относится к неучетной группе (0 - нет, 1 - да)
         nmm.PID                                                as NOMBASE_ID,                  -- Номенклатор
         nmm.KOEFF                                              as KOEFF,                       -- Коэффициент пересчета из основной в дополнительную единицу измерения
         prsc.DOSE                                              as DOSE,                        -- Дозировка
         prsc.DOSE_MEASURE                                      as DOSE_MEASURE_ID,             -- Единица измерения дозировки
         (select dmsr.MNEMOCODE
            from D_DOSE_MEASURES dmsr
           where dmsr.ID = prsc.DOSE_MEASURE)                   as DOSE_MEASURE,                -- Единицы измерения дозировки ЛС
         prsc.DOSE_QUANT                                        as DOSE_QUANT,                  -- Кол-во доз
         prsc.MP_DESCRIBE                                       as MP_DESCRIBE_ID,              -- Периодичность приема
         dsc.D_CODE                                             as MP_DESCRIBE_CODE,            -- Код
         dsc.D_NAME                                             as MP_DESCRIBE_NAME,            -- Наименование
         dsc.D_TYPE                                             as MP_DESCRIBE_TYPE,            -- Привязка : 0 - по времени; 1 - по условию; 2 - условно-временная; 3 - по интервалу; 4 - по времени вручную; 5 – по времени с перерывом; 6 - по графику
         prsc.MED_USE_METHOD                                    as MED_USE_METHOD_ID,           -- Способ введения препарата
         um.MUM_CODE                                            as MED_USE_METHOD_CODE,         -- Код
         um.MUM_NAME                                            as MED_USE_METHOD_NAME,         -- Наименование
         prsc.DATE_BEGIN                                        as DATE_BEGIN,                  -- Дата начала
         prsc.DATE_END                                          as DATE_END,                    -- Дата окончания
         prsc.DATE_CANCEL                                       as DATE_CANCEL,                 -- Дата отмены
         prsc.DATE_CREATE                                       as DATE_CREATE,                 -- Дата создания назначения
         prsc.MP_CONDITION                                      as MP_CONDITION_CODE,           -- Состояние назначения
         (select cnd.MPC_NAME
            from D_MP_CONDITIONES cnd
           where cnd.MPC_CODE = prsc.MP_CONDITION)              as MP_CONDITION_NAME,           -- Состояния назначения
         prsc.CANCEL_REASON                                     as CANCEL_REASON,               -- Причина отмены
         prsc.EMPLOYER                                          as EMPLOYER_ID,                 -- Врач, создавший назначение
         emp.KOD_VRACHA                                         as EMPLOYER_CODE,               -- Код врача, создавшего назначение
         D_PKG_STR_TOOLS.FIO(
           fsSURNAME  => a.SURNAME,
           fsNAME     => a.FIRSTNAME,
           fsPATRNAME => a.LASTNAME)                            as EMPLOYER,                    -- ФИО врача, создавшего назначение
         prsc.MA_ATTRIBUTE                                      as MA_ATTRIBUTE_ID,             -- Дополнительный признак назначения
         (select aat.AA_CODE
            from D_MP_ADD_ATTRIBUTES aat
           where aat.ID = prsc.MA_ATTRIBUTE)                    as MA_ATTRIBUTE,                -- Дополнительные признаки назначения
         prsc.IS_PAT_NOMBASE                                    as IS_PAT_NOMBASE,              -- Используется медикамент пациента : 0 - нет, 1 - да
         prsc.COMMENTARY                                        as COMMENTARY,                  -- Комментарий
         D_PKG_ACC_FARM_PROD.GET_ROUNDED_VALUE(pnNOMMODIF => prsc.NOMMODIF,
                                               pnLPU      => prsc.LPU,
                                               pnVALUE    => prsc.QUANTITY) as QUANTITY,        -- Кол-во в ОЕИ
         (select msr.MNEMOCODE
            from D_MEASURES msr
           where msr.ID = nmm.MAIN_MEASURE)                     as NOMMODIF_MAIN_MEASURE,       -- Единицы измерения
         prsc.USE_UNTIL_CLOSE                                   as USE_UNTIL_CLOSE,             -- Продлевать назначения вплоть до выписки (0-нет;1-да)
         prsc.HH_DEP                                            as HH_DEP,                      -- Отделение истории болезни
         prsc.PATIENT                                           as PATIENT_ID,                  -- Пациент
         prsc.CANCEL_DS                                         as CANCEL_DS,                   -- Услуга, на которой отменено назначение
         prsc.CANCEL_EMPLOYER                                   as CANCEL_EMPLOYER,             -- Врач, отменивший назначение
         prsc.HID                                               as HID,                         -- Ссылка на верхний уровень иерархии D_MP_PRESCRIBES
         D_PKG_ACC_FARM_PROD.GET_ROUNDED_VALUE(pnNOMMODIF => prsc.NOMMODIF,
                                               pnLPU      => prsc.LPU,
                                               pnVALUE    => prsc.ALT_DOSE) as ALT_DOSE,        -- Дозировка в дополнительной ЕИ
         (select msr.MNEMOCODE
            from D_MEASURES msr
           where msr.ID = nmm.ALT_MEASURE)                      as ALT_MEASURE_CODE,            -- Единицы измерения
         prsc.CHEMOTHERAPY                                      as CHEMOTHERAPY,                -- Назначенная химиотерапия
         prsc.NOMMODIF_STR                                      as NOMMODIF_STR,                -- Модификация номенклатуры (ручной ввод)
         prsc.IS_RECIPE                                         as IS_RECIPE,                   -- Назначение для выписки рецепта: 0 - нет, 1 - да
         case prsc.IS_RECIPE
           when 1 then 'Да'
           else 'Нет'
         end                                                    as IS_RECIPE_STR,               -- Расшифровка назначения для выписки рецепта
         prsc.MTS_DESC                                          as MTS_DESC,                    -- Схема лекарственного лечения
         prsc.IS_ONKO                                           as IS_ONKO,                     -- является препаратом при лечении ЗНО (1-да)
         prsc.RTUB_CHEMOPHASE                                   as RTUB_CHEMOPHASE,             -- Ссылка на фазу химиотерапии
         prsc.DURATION                                          as DURATION,                    -- Длительность введения
         prsc.DURATION_MEASURE                                  as DURATION_MEASURE,            -- Ед. измерения длительности введения
         prsc.DATE_AFFIRM                                       as DATE_AFFIRM,                 -- Дата подписания назначения
         prsc.PRIORITY,                                                                         -- Порядок
         prsc.NUMBER_SER                                        as IMPLANT_SER_NUMBER,          -- Серийный номер импланта
         prsc.REACTION,                                                                         -- Реакция на инфузионное введение
         prsc.MP_CLASS,                                                                         -- Класс назначения (0-обычное, 1-комплексное, 2-экстемпоральное, 3-Заявка ОЦР)
         prsc.REAS_CANCEL                                       REAS_CANCEL_ID,                 -- ID D_REASON_CANCEL_PRESCRIBE
         (select rcp.REAS_NAME
            from D_REASON_CANCEL_PRESCRIBE rcp
           where rcp.ID = prsc.REAS_CANCEL)                     REAS_CANCEL_NAME,               -- Название причины отмены
         prsc.SPEED,                                                                            -- Ед. изм. скорости введения
         (select mnn.MNN_NAME_RUS
            from D_NOMBASE_MNN mnn
           where mnn.ID = prsc.MNN)                             MNN_NAME_RUS,                   -- Международные непатентованные наименования
         prsc.MNN                                               MNN_ID,                         -- МНН препарата
         prsc.MED_FORMS,
         prsc.RED_INJ,
         (select hhd.DEP
            from D_HOSP_HISTORY_DEPS hhd
           where hhd.ID = prsc.HH_DEP) DEP_ID
  from D_MP_PRESCRIBES prsc                                                                     -- Лист назначений
       join D_EMPLOYERS emp on emp.ID = prsc.EMPLOYER                                           -- Персонал
       join D_AGENTS a on a.ID = emp.AGENT                                                      -- Контрагенты
       left join D_NOMMODIF nmm on nmm.ID = prsc.NOMMODIF                                       -- Модификации номенклатуры
                               and prsc.NOMMODIF is not null
       left join D_MP_DESCRIBES dsc on dsc.ID = prsc.MP_DESCRIBE                                -- Периодичность назначений
                                   and prsc.MP_DESCRIBE is not null
       left join D_MED_USE_METHODS um on um.ID = prsc.MED_USE_METHOD                            -- Способы приема лекарственных средств
                                     and prsc.MED_USE_METHOD is not null
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.LPU = prsc.LPU
                  and ur.UNITCODE = 'MP_PRESCRIBES'
                  and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 3
- Всего уникальных таблиц: 20

### Связь вьюх и таблиц

**D_V_AGENTS_PERSMEDCARD** использует таблицы:
- D_AGENTS
- D_PERSMEDCARD
- D_LPU

**D_V_PERSMEDCARD** использует таблицы:
- D_PERSMEDCARD
- D_AGENTS
- D_BLOODGROUPE
- D_DIVISIONS
- D_RHESUS
- D_EMPLOYERS
- D_CABLAB

**D_V_MP_PRESCRIBES** использует таблицы:
- D_MP_TYPES
- D_DOSE_MEASURES
- D_MP_CONDITIONES
- D_MP_ADD_ATTRIBUTES
- D_MEASURES
- D_REASON_CANCEL_PRESCRIBE
- D_NOMBASE_MNN
- D_HOSP_HISTORY_DEPS
- D_MP_PRESCRIBES
- D_EMPLOYERS
- D_AGENTS
- D_NOMMODIF
- D_MP_DESCRIBES
- D_MED_USE_METHODS

### DDL определения таблиц

---

#### Таблица №1: D_AGENTS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_AGENTS.id IS 'ID';
COMMENT ON COLUMN D_AGENTS.version IS 'Версия';
COMMENT ON COLUMN D_AGENTS.cid IS 'Каталог';
COMMENT ON COLUMN D_AGENTS.agn_code IS 'Код';
COMMENT ON COLUMN D_AGENTS.agn_name IS 'Наименование';
COMMENT ON COLUMN D_AGENTS.agn_type IS 'Тип : 0 - юридический, 1 - физический';
COMMENT ON COLUMN D_AGENTS.agn_inn IS 'ИНН';
COMMENT ON COLUMN D_AGENTS.agn_kpp IS 'КПП';
COMMENT ON COLUMN D_AGENTS.note IS 'Примечание';
COMMENT ON COLUMN D_AGENTS.firstname IS 'Имя';
COMMENT ON COLUMN D_AGENTS.surname IS 'Фамилия';
COMMENT ON COLUMN D_AGENTS.lastname IS 'Отчество';
COMMENT ON COLUMN D_AGENTS.birthdate IS 'Дата рождения';
COMMENT ON COLUMN D_AGENTS.sex IS 'Пол : 0 - женский, 1 - мужской';
COMMENT ON COLUMN D_AGENTS.okved IS 'Код по ОКВЕД';
COMMENT ON COLUMN D_AGENTS.education IS 'Образование';
COMMENT ON COLUMN D_AGENTS.is_employer IS 'Сотрудник: 1 - да, 0 - нет';
COMMENT ON COLUMN D_AGENTS.snils IS 'СНИЛС';
COMMENT ON COLUMN D_AGENTS.agn_ogrn IS 'Код ОГРН';
COMMENT ON COLUMN D_AGENTS.agn_okpo IS 'Код ОКПО';
COMMENT ON COLUMN D_AGENTS.deathdate IS 'Дата и время смерти';
COMMENT ON COLUMN D_AGENTS.deathdoctype IS 'Тип документа о смерти';
COMMENT ON COLUMN D_AGENTS.deathdocdate IS 'Дата оформления документа о смерти';
COMMENT ON COLUMN D_AGENTS.deathdocnum IS 'Номер документа о смерти';
COMMENT ON COLUMN D_AGENTS.agn_okfs IS 'Код по ОКФС';
COMMENT ON COLUMN D_AGENTS.enp IS 'ЕНП';
COMMENT ON COLUMN D_AGENTS.birthplace IS 'Место рождения';
COMMENT ON COLUMN D_AGENTS.nation IS 'Национальность';
COMMENT ON COLUMN D_AGENTS.is_home IS 'Лежачий пациент';
COMMENT ON COLUMN D_AGENTS.gest_age_mother IS 'Срок гестации матери(в неделях) при родах';
COMMENT ON COLUMN D_AGENTS.is_anonym IS 'Аноним: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.deathplace IS 'Место смерти';
COMMENT ON COLUMN D_AGENTS.full_classes IS 'Количество полных классов/курсов';
COMMENT ON COLUMN D_AGENTS.accuracy_date_death IS 'Точность даты смерти: 0 - неизвестно время; 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью';
COMMENT ON COLUMN D_AGENTS.accuracy_date_birth IS 'Точность даты рождения: 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью';
COMMENT ON COLUMN D_AGENTS.ind_enterp IS 'Индивидуальный предприниматель: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.agn_ogrn_ind IS 'Код ОГРН ИП';
COMMENT ON COLUMN D_AGENTS.convict_amount IS 'Общее число судимостей';
COMMENT ON COLUMN D_AGENTS.allerg_date IS 'Дата опроса о наличии аллергии';
COMMENT ON COLUMN D_AGENTS.according_relatives IS 'Заполнено со слов родственников: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.birthplace_geo IS 'Место рождения: географическое понятие';
COMMENT ON COLUMN D_AGENTS.webiomed_guid IS 'Идентификатор от МИС НП для мониторинга Webiomed';
COMMENT ON COLUMN D_AGENTS.webiomed_url IS 'Ссылка на результаты "Мониторинг Webiomed"';
COMMENT ON COLUMN D_AGENTS.medicbk_guid IS 'Идентификатор для MedicBK';
COMMENT ON COLUMN D_AGENTS.medicbk_url IS 'Ссылка на результаты MedicBK';
COMMENT ON COLUMN D_AGENTS.birthplace_gar_address_id IS 'Место рождения : географическое понятие (ГАР)';
COMMENT ON COLUMN D_AGENTS.max_info IS 'Информирование в MAX: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.epgu IS 'Признак: 1 - да, 0 - нет';

COMMENT ON TABLE D_AGENTS IS 'Контрагенты';
```

---

#### Таблица №2: D_PERSMEDCARD

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_PERSMEDCARD.id IS 'ID';
COMMENT ON COLUMN D_PERSMEDCARD.bloodgroupe IS 'Группа крови (ссылка на справочник)';
COMMENT ON COLUMN D_PERSMEDCARD.rhesus IS 'Резус фактор';
COMMENT ON COLUMN D_PERSMEDCARD.ecolor IS 'Цвет глаз';
COMMENT ON COLUMN D_PERSMEDCARD.createdate IS 'Дата создания карты';
COMMENT ON COLUMN D_PERSMEDCARD.moddate IS 'Дата внесения последних изменений';
COMMENT ON COLUMN D_PERSMEDCARD.card_numb IS 'Код карты';
COMMENT ON COLUMN D_PERSMEDCARD.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_PERSMEDCARD.agent IS 'Контрагент';
COMMENT ON COLUMN D_PERSMEDCARD.note IS 'Примечание';
COMMENT ON COLUMN D_PERSMEDCARD.cid IS 'Каталог';
COMMENT ON COLUMN D_PERSMEDCARD.reg_division IS 'Место регистрации карты';
COMMENT ON COLUMN D_PERSMEDCARD.tp_printed IS 'Информация о печати титульного листа';
COMMENT ON COLUMN D_PERSMEDCARD.outnumb IS 'Код карты для выгрузок';
COMMENT ON COLUMN D_PERSMEDCARD.pmc_type IS 'Тип карты пациента: null - пациент, 1 - аноним, 2 - новорожденный, 3 - неизвестный';
COMMENT ON COLUMN D_PERSMEDCARD.ia_printed IS 'Информация о печати информированного согласия на обработку персональных данных';
COMMENT ON COLUMN D_PERSMEDCARD.sms_agree IS 'Согласие на получение СМС';
COMMENT ON COLUMN D_PERSMEDCARD.emp_id IS 'Идентификатор регистратора, добавившего карту';
COMMENT ON COLUMN D_PERSMEDCARD.email_agree IS 'Согласие на отправку результатов по эл. почте: 0- нет, 1 - да';
COMMENT ON COLUMN D_PERSMEDCARD.phenotype IS 'Фенотип (комбинированное значение из четырех групп антигенов -  D_PHENOTYPES.PH_NAME)

';
COMMENT ON COLUMN D_PERSMEDCARD.cablab IS 'Кабинет, в котором создали карту';

COMMENT ON TABLE D_PERSMEDCARD IS 'Карта пациента';
```

---

#### Таблица №3: D_LPU

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_LPU.id IS 'ID';
COMMENT ON COLUMN D_LPU.fullname IS 'Полное наименование ЛПУ';
COMMENT ON COLUMN D_LPU.headdoctor_fullname IS 'ФИО главврача';
COMMENT ON COLUMN D_LPU.fulladdress IS 'Адрес ЛПУ';
COMMENT ON COLUMN D_LPU.phones IS 'Телефоны ЛПУ';
COMMENT ON COLUMN D_LPU.rec_ser_priv IS 'Серия для выписки рецептов 148-1/у-04
на льготные медикаменты';
COMMENT ON COLUMN D_LPU.rec_ser IS 'Серия для выписки рецептов
на нельготные медикаменты';
COMMENT ON COLUMN D_LPU.code_lpu IS 'Код ЛПУ';
COMMENT ON COLUMN D_LPU.code_ogrn IS 'Код ЛПУ по ОГРН';
COMMENT ON COLUMN D_LPU.code_okpo IS 'Код ЛПУ по ОКПО';
COMMENT ON COLUMN D_LPU.code_okdp IS 'Код ЛПУ по ОКДП';
COMMENT ON COLUMN D_LPU.code_okonh IS 'Код ЛПУ по ОКОНХ';
COMMENT ON COLUMN D_LPU.code_okato IS 'Код ЛПУ по ОКАТО';
COMMENT ON COLUMN D_LPU.code_okogu IS 'Код ЛПУ по ОКОГУ';
COMMENT ON COLUMN D_LPU.code_ocopph IS 'Код ЛПУ по ОКОПФ';
COMMENT ON COLUMN D_LPU.code_okfs IS 'Код ЛПУ по ОКФС';
COMMENT ON COLUMN D_LPU.lpudict IS 'АПУ';
COMMENT ON COLUMN D_LPU.bookkeeper_fullname IS 'ФИО главбуха';
COMMENT ON COLUMN D_LPU.headeconomist_fullname IS 'ФИО главного экономиста';
COMMENT ON COLUMN D_LPU.geografy IS 'Регион ЛПУ';
COMMENT ON COLUMN D_LPU.userforms IS 'Каталог пользовательских форм';
COMMENT ON COLUMN D_LPU.gennumb_group IS 'Группа нумерации карт';
COMMENT ON COLUMN D_LPU.exec_authority IS 'Орган исполнительной власти субъекта РФ';
COMMENT ON COLUMN D_LPU.rec_ser_priv_88 IS 'Серия для выписки рецептов 148-1/у-88
на льготные медикаменты';
COMMENT ON COLUMN D_LPU.ip_addr IS 'Доступные IP';
COMMENT ON COLUMN D_LPU.by_es_only IS 'Вход в ЛПУ осуществляется только по электронной подписи: 0 - нет, 1 - да';
COMMENT ON COLUMN D_LPU.website IS 'Сайт МО';
COMMENT ON COLUMN D_LPU.is_tech_lpu IS 'Техническая УЗ';
COMMENT ON COLUMN D_LPU.address IS 'Адрес ГАР';

COMMENT ON TABLE D_LPU IS 'Основная таблица ЛПУ';
```

---

#### Таблица №4: D_BLOODGROUPE

```sql
CREATE TABLE D_BLOODGROUPE (
    id bigint,
    bg_code character varying(20),
    bg_name character varying(160),
    version bigint
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_BLOODGROUPE.id IS 'ID';
COMMENT ON COLUMN D_BLOODGROUPE.bg_code IS 'Код';
COMMENT ON COLUMN D_BLOODGROUPE.bg_name IS 'Наименование';
COMMENT ON COLUMN D_BLOODGROUPE.version IS 'Версия';

COMMENT ON TABLE D_BLOODGROUPE IS 'Группы крови';
```

---

#### Таблица №5: D_DIVISIONS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DIVISIONS.id IS 'ID';
COMMENT ON COLUMN D_DIVISIONS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_DIVISIONS.div_code IS 'Код';
COMMENT ON COLUMN D_DIVISIONS.div_name IS 'Наименование';
COMMENT ON COLUMN D_DIVISIONS.lpu_branch IS 'Филиал ЛПУ';
COMMENT ON COLUMN D_DIVISIONS.rec_ser_priv IS 'Серия для выписки рецептов 04
на льготные медикаменты';
COMMENT ON COLUMN D_DIVISIONS.hid IS 'Ссылка на верхний уровень иерархии';
COMMENT ON COLUMN D_DIVISIONS.rec_ser_priv_88 IS 'Серия для выписки рецептов 88
на льготные медикаменты 
';
COMMENT ON COLUMN D_DIVISIONS.pat_restriction IS 'Ограничения по пациентам';
COMMENT ON COLUMN D_DIVISIONS.version IS 'Версия';
COMMENT ON COLUMN D_DIVISIONS.div_oid_frmo IS 'OID в ФРМО';

COMMENT ON TABLE D_DIVISIONS IS 'Подразделения ЛПУ';
```

---

#### Таблица №6: D_RHESUS

```sql
CREATE TABLE D_RHESUS (
    rh_code numeric(1),
    rh_name character varying(10)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_RHESUS.rh_code IS 'Код наименование';
COMMENT ON COLUMN D_RHESUS.rh_name IS 'Наименование';

COMMENT ON TABLE D_RHESUS IS 'Резус-фактор';
```

---

#### Таблица №7: D_EMPLOYERS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_EMPLOYERS.id IS 'ID';
COMMENT ON COLUMN D_EMPLOYERS.jobtitle IS 'Должность';
COMMENT ON COLUMN D_EMPLOYERS.regdate IS 'Дата регистрации';
COMMENT ON COLUMN D_EMPLOYERS.speciality IS 'Специальность';
COMMENT ON COLUMN D_EMPLOYERS.kod_vracha IS 'Код врача';
COMMENT ON COLUMN D_EMPLOYERS.registr_kod IS 'Регистрационный код';
COMMENT ON COLUMN D_EMPLOYERS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_EMPLOYERS.speciality_ed IS 'Специальность по образованию';
COMMENT ON COLUMN D_EMPLOYERS.skill_category IS 'Квалификационная категория';
COMMENT ON COLUMN D_EMPLOYERS.is_dismissed IS 'Сотрудник уволен ';
COMMENT ON COLUMN D_EMPLOYERS.dismiss_date IS 'Дата увольнения';
COMMENT ON COLUMN D_EMPLOYERS.department IS 'Отделение';
COMMENT ON COLUMN D_EMPLOYERS.sysuser IS 'Пользователь';
COMMENT ON COLUMN D_EMPLOYERS.agent IS 'Контрагент';
COMMENT ON COLUMN D_EMPLOYERS.cid IS 'Каталог';
COMMENT ON COLUMN D_EMPLOYERS.report_sign IS 'Подпись врача в отчетах';
COMMENT ON COLUMN D_EMPLOYERS.emp_numb IS 'Табельный номер';
COMMENT ON COLUMN D_EMPLOYERS.quot_resource IS 'Ресурс квотирования';
COMMENT ON COLUMN D_EMPLOYERS.rate IS 'Занимаемое количество ставок';
COMMENT ON COLUMN D_EMPLOYERS.personal_card_guid IS 'Идентификатор записи личного дела';

COMMENT ON TABLE D_EMPLOYERS IS 'Персонал';
```

---

#### Таблица №8: D_CABLAB

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_CABLAB.id IS 'ID';
COMMENT ON COLUMN D_CABLAB.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_CABLAB.department IS 'Отделение';
COMMENT ON COLUMN D_CABLAB.cl_code IS 'Код';
COMMENT ON COLUMN D_CABLAB.cl_name IS 'Наименование';
COMMENT ON COLUMN D_CABLAB.cid IS 'Каталог';
COMMENT ON COLUMN D_CABLAB.pid IS 'Кабинет';
COMMENT ON COLUMN D_CABLAB.schedule_type IS 'Тип назначенного графика(поддерживается автоматически)';
COMMENT ON COLUMN D_CABLAB.division IS 'Подразделение';
COMMENT ON COLUMN D_CABLAB.building IS 'Здание';
COMMENT ON COLUMN D_CABLAB.floor IS 'Этаж';
COMMENT ON COLUMN D_CABLAB.is_comm IS 'Кабинет платных услуг';
COMMENT ON COLUMN D_CABLAB.begin_date IS 'Начало функционирования кабинета платных услуг';
COMMENT ON COLUMN D_CABLAB.end_date IS 'Окончание функционирования кабинета платных услуг';
COMMENT ON COLUMN D_CABLAB.cablab_type IS 'Тип кабинета';
COMMENT ON COLUMN D_CABLAB.cl_begin_date IS 'Дата начала действия';
COMMENT ON COLUMN D_CABLAB.cl_end_date IS 'Дата окончания действия';

COMMENT ON TABLE D_CABLAB IS 'Кабинеты и лаборатории';
```

---

#### Таблица №9: D_MP_TYPES

```sql
CREATE TABLE D_MP_TYPES (
    mpt_code numeric(2),
    mpt_name character varying(200)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_TYPES.mpt_code IS 'Код назначения';
COMMENT ON COLUMN D_MP_TYPES.mpt_name IS 'Наименование назначение';

COMMENT ON TABLE D_MP_TYPES IS 'Типы назначений лекарственных средств';
```

---

#### Таблица №10: D_DOSE_MEASURES

```sql
CREATE TABLE D_DOSE_MEASURES (
    id bigint,
    code numeric(8),
    mnemocode character varying(200),
    version bigint,
    recipe_show_flag numeric(1) DEFAULT 0,
    mnemocode_lat character varying(200)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DOSE_MEASURES.id IS 'ID';
COMMENT ON COLUMN D_DOSE_MEASURES.code IS 'Код единицы измерения';
COMMENT ON COLUMN D_DOSE_MEASURES.mnemocode IS 'Мнемокод единицы измерения';
COMMENT ON COLUMN D_DOSE_MEASURES.version IS 'Версия';
COMMENT ON COLUMN D_DOSE_MEASURES.recipe_show_flag IS 'Отображать при выписке рецепта';
COMMENT ON COLUMN D_DOSE_MEASURES.mnemocode_lat IS 'Латинское наименование';

COMMENT ON TABLE D_DOSE_MEASURES IS 'Единицы измерения дозировки ЛС';
```

---

#### Таблица №11: D_MP_CONDITIONES

```sql
CREATE TABLE D_MP_CONDITIONES (
    mpc_code numeric(4),
    mpc_name character varying(200)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_CONDITIONES.mpc_code IS 'Код';
COMMENT ON COLUMN D_MP_CONDITIONES.mpc_name IS 'Наименование';

COMMENT ON TABLE D_MP_CONDITIONES IS 'Состояния назначения';
```

---

#### Таблица №12: D_MP_ADD_ATTRIBUTES

```sql
CREATE TABLE D_MP_ADD_ATTRIBUTES (
    id bigint,
    lpu bigint,
    cid bigint,
    aa_code character varying(20),
    aa_name character varying(200),
    koeff numeric(7,2) DEFAULT 1
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.id IS 'ID';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.cid IS 'Каталог';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.aa_code IS 'Код';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.aa_name IS 'Наименование';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.koeff IS 'Коэффициент пересчёта дозировки';

COMMENT ON TABLE D_MP_ADD_ATTRIBUTES IS 'Дополнительные признаки назначения';
```

---

#### Таблица №13: D_MEASURES

```sql
CREATE TABLE D_MEASURES (
    id bigint,
    code numeric(10),
    mnemocode character varying(150),
    m_name character varying(200),
    m_category bigint,
    etalon numeric(1),
    koeff numeric(22,5),
    cid bigint,
    version bigint,
    mnemocode_lat character varying(200)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MEASURES.id IS 'ID';
COMMENT ON COLUMN D_MEASURES.code IS 'Код единицы измерения';
COMMENT ON COLUMN D_MEASURES.mnemocode IS 'Мнемокод единицы измерения';
COMMENT ON COLUMN D_MEASURES.m_name IS 'Наименование единиц измерения';
COMMENT ON COLUMN D_MEASURES.m_category IS 'Категория ';
COMMENT ON COLUMN D_MEASURES.etalon IS 'Признак эталона единицы измерения (0 - эталон, 1 - производная)';
COMMENT ON COLUMN D_MEASURES.koeff IS 'Коэффициент отношения к эталону (ПР = К*ЭТ)';
COMMENT ON COLUMN D_MEASURES.cid IS 'Каталог';
COMMENT ON COLUMN D_MEASURES.version IS 'Версия';
COMMENT ON COLUMN D_MEASURES.mnemocode_lat IS 'Латинское наименование';

COMMENT ON TABLE D_MEASURES IS 'Единицы измерения';
```

---

#### Таблица №14: D_REASON_CANCEL_PRESCRIBE

```sql
CREATE TABLE D_REASON_CANCEL_PRESCRIBE (
    id bigint,
    reas_name character varying(256),
    reas_discription character varying(2000) DEFAULT NULL::character varying
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.id IS 'ID';
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.reas_name IS 'Название причины отмены';
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.reas_discription IS 'Описание причины отмены';

COMMENT ON TABLE D_REASON_CANCEL_PRESCRIBE IS 'Причины отмены назначения';
```

---

#### Таблица №15: D_NOMBASE_MNN

```sql
CREATE TABLE D_NOMBASE_MNN (
    id bigint,
    version bigint,
    cid bigint,
    mnn_name_rus character varying(600),
    mnn_name_lat character varying(300),
    input_code character varying(50)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_NOMBASE_MNN.id IS 'ID';
COMMENT ON COLUMN D_NOMBASE_MNN.version IS 'Версия';
COMMENT ON COLUMN D_NOMBASE_MNN.cid IS 'Каталог';
COMMENT ON COLUMN D_NOMBASE_MNN.mnn_name_rus IS 'Наименование';
COMMENT ON COLUMN D_NOMBASE_MNN.mnn_name_lat IS 'Латинское наименование';
COMMENT ON COLUMN D_NOMBASE_MNN.input_code IS 'Код узла СМНН НСИ';

COMMENT ON TABLE D_NOMBASE_MNN IS 'Международные непатентованные наименования';
```

---

#### Таблица №16: D_HOSP_HISTORY_DEPS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.id IS 'ID';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.pid IS 'История болезни';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.date_in IS 'Дата поступления в отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.date_out IS 'Дата выписки из отделения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.dep IS 'Отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.mkb IS 'Диагноз отделения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.mkb_exact IS 'Диагноз отделения уточненный';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.healing_emp IS 'Лечащий врач';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.payment_kind IS 'Вид оплаты';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.hosp_result IS 'Внутрибольничный (промежуточный) результат госпитализации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.ksg IS 'Код клинико-статистической группы';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.bed_type IS 'Профиль койки';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.facial_account IS 'Лицевой счёт';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.hhd_pref IS 'Префикс номера отделения ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.hhd_numb IS 'Номер отделения ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.vmp IS 'ВМП';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.prvsid IS 'Предыдущее отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.hhd_level IS 'Номер отделенческого случая ИБ(0 - открытый)';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.is_last IS 'Признак последнего отделения в ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.hosp_outcome IS 'Исход  госпитализации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.mts_desc IS 'Схема лекарственного лечения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.alv IS 'Проведение ИВЛ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.scale_rehab IS 'Шкала реабилитационной маршрутизации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.ksgid IS 'Идентификатор клинико-статистической группы';

COMMENT ON TABLE D_HOSP_HISTORY_DEPS IS 'Истории болезни : отделения';
```

---

#### Таблица №17: D_MP_PRESCRIBES

```sql
CREATE TABLE D_MP_PRESCRIBES (
    id bigint,
    lpu bigint,
    diseasecase bigint,
    direction_service bigint,
    mp_type numeric(2),
    nommodif bigint,
    dose numeric(13,3),
    dose_measure bigint,
    dose_quant numeric(13,3),
    mp_describe bigint,
    med_use_method bigint,
    date_begin timestamp without time zone,
    date_end timestamp without time zone,
    date_cancel timestamp without time zone,
    date_create timestamp without time zone,
    mp_condition numeric(4),
    cancel_reason character varying(600),
    employer bigint,
    ma_attribute bigint,
    is_pat_nombase numeric(1) DEFAULT 0,
    commentary character varying(2000),
    quantity numeric(28,8),
    use_until_close numeric(1) DEFAULT 0,
    hh_dep bigint,
    patient bigint,
    cancel_ds bigint,
    cancel_employer bigint,
    hid bigint,
    agent_trfa bigint,
    alt_dose numeric(28,8),
    nommodif_str character varying(300),
    is_recipe numeric(1) DEFAULT 0,
    chemotherapy bigint,
    mts_desc bigint,
    is_onko numeric(1),
    rtub_chemophase bigint,
    duration numeric(13,3),
    duration_measure character varying(200),
    date_affirm timestamp without time zone,
    priority numeric(4),
    number_ser character varying(27),
    reaction character varying(600),
    mp_class numeric(1),
    reas_cancel bigint,
    speed bigint,
    mnn bigint,
    med_forms bigint,
    red_inj numeric(1) DEFAULT 0
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_PRESCRIBES.id IS 'ID';
COMMENT ON COLUMN D_MP_PRESCRIBES.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_MP_PRESCRIBES.diseasecase IS 'Случай заболевания';
COMMENT ON COLUMN D_MP_PRESCRIBES.direction_service IS 'Направление на услугу';
COMMENT ON COLUMN D_MP_PRESCRIBES.mp_type IS 'Тип назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.nommodif IS 'Модификация номенклатуры';
COMMENT ON COLUMN D_MP_PRESCRIBES.dose IS 'Дозировка';
COMMENT ON COLUMN D_MP_PRESCRIBES.dose_measure IS 'Единица измерения дозировки';
COMMENT ON COLUMN D_MP_PRESCRIBES.dose_quant IS 'Кол-во доз';
COMMENT ON COLUMN D_MP_PRESCRIBES.mp_describe IS 'Периодичность приема';
COMMENT ON COLUMN D_MP_PRESCRIBES.med_use_method IS 'Способ введения препарата';
COMMENT ON COLUMN D_MP_PRESCRIBES.date_begin IS 'Дата начала';
COMMENT ON COLUMN D_MP_PRESCRIBES.date_end IS 'Дата окончания';
COMMENT ON COLUMN D_MP_PRESCRIBES.date_cancel IS 'Дата отмены';
COMMENT ON COLUMN D_MP_PRESCRIBES.date_create IS 'Дата создания назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.mp_condition IS 'Состояние назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.cancel_reason IS 'Причина отмены';
COMMENT ON COLUMN D_MP_PRESCRIBES.employer IS 'Врач, создавший назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.ma_attribute IS 'Дополнительный признак назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.is_pat_nombase IS 'Используется медикамент пациента : 0 - нет, 1 - да';
COMMENT ON COLUMN D_MP_PRESCRIBES.commentary IS 'Комментарий';
COMMENT ON COLUMN D_MP_PRESCRIBES.quantity IS 'Кол-во в ОЕИ';
COMMENT ON COLUMN D_MP_PRESCRIBES.use_until_close IS 'Продлевать назначения вплоть до выписки (0-нет;1-да)';
COMMENT ON COLUMN D_MP_PRESCRIBES.hh_dep IS 'Отделение истории болезни';
COMMENT ON COLUMN D_MP_PRESCRIBES.patient IS 'Пациент';
COMMENT ON COLUMN D_MP_PRESCRIBES.cancel_ds IS 'Услуга, на которой отменено назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.cancel_employer IS 'Врач, отменивший назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.hid IS 'Ссылка на верхний уровень иерархии';
COMMENT ON COLUMN D_MP_PRESCRIBES.agent_trfa IS 'Трансфузиологический анамнез';
COMMENT ON COLUMN D_MP_PRESCRIBES.alt_dose IS 'Дозировка в дополнительной ЕИ';
COMMENT ON COLUMN D_MP_PRESCRIBES.nommodif_str IS 'Модификация номенклатуры (ручной ввод)';
COMMENT ON COLUMN D_MP_PRESCRIBES.is_recipe IS 'Назначение для выписки рецепта: 0 - нет, 1 - да';
COMMENT ON COLUMN D_MP_PRESCRIBES.chemotherapy IS 'Назначенная химиотерапия';
COMMENT ON COLUMN D_MP_PRESCRIBES.mts_desc IS 'Схема лекарственного лечения';
COMMENT ON COLUMN D_MP_PRESCRIBES.is_onko IS 'является препаратом при лечении ЗНО (1-да)';
COMMENT ON COLUMN D_MP_PRESCRIBES.rtub_chemophase IS 'Ссылка на фазу химиотерапии';
COMMENT ON COLUMN D_MP_PRESCRIBES.duration IS 'Длительность введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.duration_measure IS 'Ед. измерения длительности введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.date_affirm IS 'Дата подписания назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.priority IS 'Порядок';
COMMENT ON COLUMN D_MP_PRESCRIBES.number_ser IS 'Серийный номер импланта';
COMMENT ON COLUMN D_MP_PRESCRIBES.reaction IS 'Реакция на инфузионное введение';
COMMENT ON COLUMN D_MP_PRESCRIBES.mp_class IS 'Класс назначения (0-обычное, 1-комплексное, 2-экстемпоральное, 3-Заявка ОЦР, 4-МНН(обычное), 5 - Комбинированное)';
COMMENT ON COLUMN D_MP_PRESCRIBES.reas_cancel IS 'Причина отмены из справочника';
COMMENT ON COLUMN D_MP_PRESCRIBES.speed IS 'Ед. изм. скорости введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.mnn IS 'МНН препарата';
COMMENT ON COLUMN D_MP_PRESCRIBES.med_forms IS 'Лекарственная форма';
COMMENT ON COLUMN D_MP_PRESCRIBES.red_inj IS 'Признак редукции';

COMMENT ON TABLE D_MP_PRESCRIBES IS 'Лист назначений';
```

---

#### Таблица №18: D_NOMMODIF

```sql
CREATE TABLE D_NOMMODIF (
    id bigint,
    version bigint,
    pid bigint,
    mod_code character varying(20),
    cid bigint,
    mod_name character varying(450),
    bar_code character varying(200),
    producer bigint,
    med_forms bigint,
    dose numeric(28,8),
    dose_measure bigint,
    main_measure bigint,
    dose_pack numeric(9,2),
    life numeric(3),
    life_measure bigint,
    climat_begin numeric(3),
    climat_end numeric(3),
    hum_begin numeric(3),
    hum_end numeric(3),
    life_special bigint,
    alt_measure bigint,
    koeff numeric(13,3),
    lat_name character varying(200),
    discard_type numeric(1) DEFAULT 0,
    acm_dose_measure bigint,
    acm_koeff numeric(13,3),
    nom_access numeric(1) DEFAULT 0,
    angro numeric(1) DEFAULT 0,
    acm_dose character varying(150),
    country bigint,
    acm_dose_pack numeric(9,2),
    multi_med_use_method character varying(150),
    input_code character varying(160),
    add_place numeric(3),
    dose_default numeric(13,3),
    is_active numeric(1) DEFAULT 1
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_NOMMODIF.id IS 'ID';
COMMENT ON COLUMN D_NOMMODIF.version IS 'Версия';
COMMENT ON COLUMN D_NOMMODIF.pid IS 'Номенклатор';
COMMENT ON COLUMN D_NOMMODIF.mod_code IS 'Мнемокод';
COMMENT ON COLUMN D_NOMMODIF.cid IS 'Каталог';
COMMENT ON COLUMN D_NOMMODIF.mod_name IS 'Наименование';
COMMENT ON COLUMN D_NOMMODIF.bar_code IS 'Штрих-код';
COMMENT ON COLUMN D_NOMMODIF.producer IS 'Производитель';
COMMENT ON COLUMN D_NOMMODIF.med_forms IS 'Лекарственная форма';
COMMENT ON COLUMN D_NOMMODIF.dose IS 'Разовая доза';
COMMENT ON COLUMN D_NOMMODIF.dose_measure IS 'Ед.измерения дозы';
COMMENT ON COLUMN D_NOMMODIF.main_measure IS 'Единицы измерения';
COMMENT ON COLUMN D_NOMMODIF.dose_pack IS 'Количество доз в одной ОЕИ';
COMMENT ON COLUMN D_NOMMODIF.life IS 'Срок хранения';
COMMENT ON COLUMN D_NOMMODIF.life_measure IS 'Единица измерения срока хранения';
COMMENT ON COLUMN D_NOMMODIF.climat_begin IS 'Условия хранения (Температура С)';
COMMENT ON COLUMN D_NOMMODIF.climat_end IS 'Условия хранения (Температура По)';
COMMENT ON COLUMN D_NOMMODIF.hum_begin IS 'Условия хранения (Влажность С)';
COMMENT ON COLUMN D_NOMMODIF.hum_end IS 'Условия хранения (Влажность По)';
COMMENT ON COLUMN D_NOMMODIF.life_special IS 'Особые условия хранения ( группа А,Б)';
COMMENT ON COLUMN D_NOMMODIF.alt_measure IS 'Дополнительная единица измерения';
COMMENT ON COLUMN D_NOMMODIF.koeff IS 'Коэффициент пересчета из основной в дополнительную единицу измерения';
COMMENT ON COLUMN D_NOMMODIF.lat_name IS 'Латинское наименование';
COMMENT ON COLUMN D_NOMMODIF.discard_type IS 'Тип списания на пациента:0-не списывать;1-в ОЕИ(по дозировке);2-в ОЕИ с округлением до целого';
COMMENT ON COLUMN D_NOMMODIF.acm_dose_measure IS 'Ед. измерения дозы действующего вещества';
COMMENT ON COLUMN D_NOMMODIF.acm_koeff IS 'Коэффициент пересчета из дозы лекарства в дозу действующего вещества';
COMMENT ON COLUMN D_NOMMODIF.nom_access IS 'Доступно в отделениях : 0 - Да, 1 - Нет';
COMMENT ON COLUMN D_NOMMODIF.angro IS 'Относится к неучетной группе (0 - нет, 1 - да)';
COMMENT ON COLUMN D_NOMMODIF.acm_dose IS 'Дозировка действующих веществ';
COMMENT ON COLUMN D_NOMMODIF.country IS 'Страна производителя';
COMMENT ON COLUMN D_NOMMODIF.acm_dose_pack IS 'Количество доз действующего вещ - ва в основной ЕИ';
COMMENT ON COLUMN D_NOMMODIF.multi_med_use_method IS 'Способ введения';
COMMENT ON COLUMN D_NOMMODIF.input_code IS 'Ссылка на внешний id';
COMMENT ON COLUMN D_NOMMODIF.add_place IS 'Место, через которое добавлялся препарат (1 - МИС, 2 - Загрузка ЕСКЛП)';
COMMENT ON COLUMN D_NOMMODIF.dose_default IS 'Доза по умолчанию';
COMMENT ON COLUMN D_NOMMODIF.is_active IS 'Признак активности препарата (0 - нет, 1 - да)';

COMMENT ON TABLE D_NOMMODIF IS 'Модификации номенклатуры';
```

---

#### Таблица №19: D_MP_DESCRIBES

```sql
CREATE TABLE D_MP_DESCRIBES (
    id bigint,
    lpu bigint,
    cid bigint,
    d_code character varying(20),
    d_name character varying(200),
    d_type numeric(2) DEFAULT 0,
    is_active numeric(1) DEFAULT 1,
    count_day_activ numeric(2) DEFAULT 1,
    count_day_inactiv numeric(2) DEFAULT 0
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_DESCRIBES.id IS 'ID';
COMMENT ON COLUMN D_MP_DESCRIBES.lpu IS 'ЛПУ';
COMMENT ON COLUMN D_MP_DESCRIBES.cid IS 'Каталог';
COMMENT ON COLUMN D_MP_DESCRIBES.d_code IS 'Код';
COMMENT ON COLUMN D_MP_DESCRIBES.d_name IS 'Наименование';
COMMENT ON COLUMN D_MP_DESCRIBES.d_type IS 'Привязка : 0 - по времени; 1 - по условию; 2 - условно-временная; 3 - по интервалу; 4 - по времени вручную; 5 – по времени с перерывом; 6 - по графику';
COMMENT ON COLUMN D_MP_DESCRIBES.is_active IS 'Действует: 1 - да; 0 - нет';
COMMENT ON COLUMN D_MP_DESCRIBES.count_day_activ IS 'Количество дней приема';
COMMENT ON COLUMN D_MP_DESCRIBES.count_day_inactiv IS 'Количество дней перерыва';

COMMENT ON TABLE D_MP_DESCRIBES IS 'Периодичность назначений';
```

---

#### Таблица №20: D_MED_USE_METHODS

```sql
CREATE TABLE D_MED_USE_METHODS (
    id bigint,
    version bigint,
    mum_code character varying(10),
    mum_name character varying(200),
    serv_for_mes bigint
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MED_USE_METHODS.id IS 'ID';
COMMENT ON COLUMN D_MED_USE_METHODS.version IS 'Версия';
COMMENT ON COLUMN D_MED_USE_METHODS.mum_code IS 'Код ';
COMMENT ON COLUMN D_MED_USE_METHODS.mum_name IS 'Наименование';
COMMENT ON COLUMN D_MED_USE_METHODS.serv_for_mes IS 'ПМУ для МЭС';

COMMENT ON TABLE D_MED_USE_METHODS IS 'Способы приема лекарственных средств';
```


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 3
- Всего уникальных таблиц: 19

### Связь вьюх и таблиц

**D_V_AGENTS_PERSMEDCARD** использует таблицы:
- D_AGENTS
- D_PERSMEDCARD

**D_V_PERSMEDCARD** использует таблицы:
- D_PERSMEDCARD
- D_AGENTS
- D_BLOODGROUPE
- D_DIVISIONS
- D_RHESUS
- D_EMPLOYERS
- D_CABLAB

**D_V_MP_PRESCRIBES** использует таблицы:
- D_MP_TYPES
- D_DOSE_MEASURES
- D_MP_CONDITIONES
- D_MP_ADD_ATTRIBUTES
- D_MEASURES
- D_REASON_CANCEL_PRESCRIBE
- D_NOMBASE_MNN
- D_HOSP_HISTORY_DEPS
- D_MP_PRESCRIBES
- D_EMPLOYERS
- D_AGENTS
- D_NOMMODIF
- D_MP_DESCRIBES
- D_MED_USE_METHODS

### DDL определения таблиц

---

#### Таблица №1: D_AGENTS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_AGENTS.ID IS 'ID';
COMMENT ON COLUMN D_AGENTS.VERSION IS 'Версия';
COMMENT ON COLUMN D_AGENTS.CID IS 'Каталог';
COMMENT ON COLUMN D_AGENTS.AGN_CODE IS 'Код';
COMMENT ON COLUMN D_AGENTS.AGN_NAME IS 'Наименование';
COMMENT ON COLUMN D_AGENTS.AGN_TYPE IS 'Тип : 0 - юридический, 1 - физический';
COMMENT ON COLUMN D_AGENTS.AGN_INN IS 'ИНН';
COMMENT ON COLUMN D_AGENTS.AGN_KPP IS 'КПП';
COMMENT ON COLUMN D_AGENTS.NOTE IS 'Примечание';
COMMENT ON COLUMN D_AGENTS.FIRSTNAME IS 'Имя';
COMMENT ON COLUMN D_AGENTS.SURNAME IS 'Фамилия';
COMMENT ON COLUMN D_AGENTS.LASTNAME IS 'Отчество';
COMMENT ON COLUMN D_AGENTS.BIRTHDATE IS 'Дата рождения';
COMMENT ON COLUMN D_AGENTS.SEX IS 'Пол : 0 - женский, 1 - мужской';
COMMENT ON COLUMN D_AGENTS.OKVED IS 'Код по ОКВЕД';
COMMENT ON COLUMN D_AGENTS.EDUCATION IS 'Образование';
COMMENT ON COLUMN D_AGENTS.IS_EMPLOYER IS 'Сотрудник: 1 - да, 0 - нет';
COMMENT ON COLUMN D_AGENTS.SNILS IS 'СНИЛС';
COMMENT ON COLUMN D_AGENTS.AGN_OGRN IS 'Код ОГРН';
COMMENT ON COLUMN D_AGENTS.AGN_OKPO IS 'Код ОКПО';
COMMENT ON COLUMN D_AGENTS.DEATHDATE IS 'Дата и время смерти';
COMMENT ON COLUMN D_AGENTS.DEATHDOCTYPE IS 'Тип документа о смерти';
COMMENT ON COLUMN D_AGENTS.DEATHDOCDATE IS 'Дата оформления документа о смерти';
COMMENT ON COLUMN D_AGENTS.DEATHDOCNUM IS 'Номер документа о смерти';
COMMENT ON COLUMN D_AGENTS.AGN_OKFS IS 'Код по ОКФС';
COMMENT ON COLUMN D_AGENTS.ENP IS 'ЕНП';
COMMENT ON COLUMN D_AGENTS.BIRTHPLACE IS 'Место рождения';
COMMENT ON COLUMN D_AGENTS.NATION IS 'Национальность';
COMMENT ON COLUMN D_AGENTS.IS_HOME IS 'Лежачий пациент';
COMMENT ON COLUMN D_AGENTS.GEST_AGE_MOTHER IS 'Срок гестации матери(в неделях) при родах';
COMMENT ON COLUMN D_AGENTS.IS_ANONYM IS 'Аноним: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.DEATHPLACE IS 'Место смерти';
COMMENT ON COLUMN D_AGENTS.FULL_CLASSES IS 'Количество полных классов/курсов';
COMMENT ON COLUMN D_AGENTS.ACCURACY_DATE_DEATH IS 'Точность даты смерти: 0 - неизвестно время; 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью';
COMMENT ON COLUMN D_AGENTS.ACCURACY_DATE_BIRTH IS 'Точность даты рождения: 1 - неизвестно число; 2 - неизвестно число и месяц; 3 - неизвестна дата полностью';
COMMENT ON COLUMN D_AGENTS.IND_ENTERP IS 'Индивидуальный предприниматель: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.AGN_OGRN_IND IS 'Код ОГРН ИП';
COMMENT ON COLUMN D_AGENTS.CONVICT_AMOUNT IS 'Общее число судимостей';
COMMENT ON COLUMN D_AGENTS.ALLERG_DATE IS 'Дата опроса о наличии аллергии';
COMMENT ON COLUMN D_AGENTS.ACCORDING_RELATIVES IS 'Заполнено со слов родственников: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.EPGU IS 'Признак: 1 - да, 0 - нет';
COMMENT ON COLUMN D_AGENTS.MEDICBK_GUID IS 'Идентификатор для MedicBK';
COMMENT ON COLUMN D_AGENTS.MEDICBK_URL IS 'Ссылка на результаты MedicBK';
COMMENT ON COLUMN D_AGENTS.MAX_INFO IS 'Информирование в MAX: 0 - нет, 1 - да';
COMMENT ON COLUMN D_AGENTS.BIRTHPLACE_GAR_ADDRESS_ID IS 'Место рождения : географическое понятие (ГАР)';
COMMENT ON COLUMN D_AGENTS.WEBIOMED_GUID IS 'Идентификатор от МИС НП для мониторинга Webiomed';
COMMENT ON COLUMN D_AGENTS.WEBIOMED_URL IS 'Ссылка на результаты "Мониторинг Webiomed"';
COMMENT ON COLUMN D_AGENTS.BIRTHPLACE_GEO IS 'Место рождения: географическое понятие';

COMMENT ON TABLE D_AGENTS IS 'Контрагенты';
```

---

#### Таблица №2: D_PERSMEDCARD

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_PERSMEDCARD.BLOODGROUPE IS 'Группа крови (ссылка на справочник)';
COMMENT ON COLUMN D_PERSMEDCARD.RHESUS IS 'Резус фактор';
COMMENT ON COLUMN D_PERSMEDCARD.ECOLOR IS 'Цвет глаз';
COMMENT ON COLUMN D_PERSMEDCARD.CREATEDATE IS 'Дата создания карты';
COMMENT ON COLUMN D_PERSMEDCARD.MODDATE IS 'Дата внесения последних изменений';
COMMENT ON COLUMN D_PERSMEDCARD.CARD_NUMB IS 'Код карты';
COMMENT ON COLUMN D_PERSMEDCARD.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_PERSMEDCARD.AGENT IS 'Контрагент';
COMMENT ON COLUMN D_PERSMEDCARD.NOTE IS 'Примечание';
COMMENT ON COLUMN D_PERSMEDCARD.CID IS 'Каталог';
COMMENT ON COLUMN D_PERSMEDCARD.REG_DIVISION IS 'Место регистрации карты';
COMMENT ON COLUMN D_PERSMEDCARD.TP_PRINTED IS 'Информация о печати титульного листа';
COMMENT ON COLUMN D_PERSMEDCARD.OUTNUMB IS 'Код карты для выгрузок';
COMMENT ON COLUMN D_PERSMEDCARD.PMC_TYPE IS 'Тип карты пациента: null - пациент, 1 - аноним, 2 - новорожденный, 3 - неизвестный';
COMMENT ON COLUMN D_PERSMEDCARD.IA_PRINTED IS 'Информация о печати информированного согласия на обработку персональных данных';
COMMENT ON COLUMN D_PERSMEDCARD.SMS_AGREE IS 'Согласие на получение СМС';
COMMENT ON COLUMN D_PERSMEDCARD.EMP_ID IS 'Идентификатор регистратора, добавившего карту';
COMMENT ON COLUMN D_PERSMEDCARD.EMAIL_AGREE IS 'Согласие на отправку результатов по эл. почте: 0- нет, 1 - да';
COMMENT ON COLUMN D_PERSMEDCARD.PHENOTYPE IS 'Фенотип (комбинированное значение из четырех групп антигенов -  D_PHENOTYPES.PH_NAME)

';
COMMENT ON COLUMN D_PERSMEDCARD.ID IS 'ID';
COMMENT ON COLUMN D_PERSMEDCARD.CABLAB IS 'Кабинет, в котором создали карту';

COMMENT ON TABLE D_PERSMEDCARD IS 'Карта пациента';
```

---

#### Таблица №3: D_BLOODGROUPE

```sql
CREATE TABLE D_BLOODGROUPE (
    ID NUMBER(17) NOT NULL,
    BG_CODE VARCHAR2(20) NOT NULL,
    BG_NAME VARCHAR2(160) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_BLOODGROUPE PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_BLOODGROUPE.BG_CODE IS 'Код';
COMMENT ON COLUMN D_BLOODGROUPE.BG_NAME IS 'Наименование';
COMMENT ON COLUMN D_BLOODGROUPE.VERSION IS 'Версия';
COMMENT ON COLUMN D_BLOODGROUPE.ID IS 'ID';

COMMENT ON TABLE D_BLOODGROUPE IS 'Группы крови';
```

---

#### Таблица №4: D_DIVISIONS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DIVISIONS.ID IS 'ID';
COMMENT ON COLUMN D_DIVISIONS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_DIVISIONS.DIV_CODE IS 'Код';
COMMENT ON COLUMN D_DIVISIONS.DIV_NAME IS 'Наименование';
COMMENT ON COLUMN D_DIVISIONS.LPU_BRANCH IS 'Филиал ЛПУ';
COMMENT ON COLUMN D_DIVISIONS.REC_SER_PRIV IS 'Серия для выписки рецептов 04
на льготные медикаменты';
COMMENT ON COLUMN D_DIVISIONS.HID IS 'Ссылка на верхний уровень иерархии';
COMMENT ON COLUMN D_DIVISIONS.REC_SER_PRIV_88 IS 'Серия для выписки рецептов 88
на льготные медикаменты 
';
COMMENT ON COLUMN D_DIVISIONS.PAT_RESTRICTION IS 'Ограничения по пациентам';
COMMENT ON COLUMN D_DIVISIONS.VERSION IS 'Версия';
COMMENT ON COLUMN D_DIVISIONS.DIV_OID_FRMO IS 'OID в ФРМО';

COMMENT ON TABLE D_DIVISIONS IS 'Подразделения ЛПУ';
```

---

#### Таблица №5: D_RHESUS

```sql
CREATE TABLE D_RHESUS (
    RH_CODE NUMBER(1) NOT NULL,
    RH_NAME VARCHAR2(10) NOT NULL,
    CONSTRAINT PK_D_RHESUS PRIMARY KEY (RH_CODE)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_RHESUS.RH_CODE IS 'Код наименование';
COMMENT ON COLUMN D_RHESUS.RH_NAME IS 'Наименование';

COMMENT ON TABLE D_RHESUS IS 'Резус-фактор';
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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_EMPLOYERS.ID IS 'ID';
COMMENT ON COLUMN D_EMPLOYERS.JOBTITLE IS 'Должность';
COMMENT ON COLUMN D_EMPLOYERS.REGDATE IS 'Дата регистрации';
COMMENT ON COLUMN D_EMPLOYERS.SPECIALITY IS 'Специальность';
COMMENT ON COLUMN D_EMPLOYERS.KOD_VRACHA IS 'Код врача';
COMMENT ON COLUMN D_EMPLOYERS.REGISTR_KOD IS 'Регистрационный код';
COMMENT ON COLUMN D_EMPLOYERS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_EMPLOYERS.SPECIALITY_ED IS 'Специальность по образованию';
COMMENT ON COLUMN D_EMPLOYERS.SKILL_CATEGORY IS 'Квалификационная категория';
COMMENT ON COLUMN D_EMPLOYERS.IS_DISMISSED IS 'Сотрудник уволен ';
COMMENT ON COLUMN D_EMPLOYERS.DISMISS_DATE IS 'Дата увольнения';
COMMENT ON COLUMN D_EMPLOYERS.DEPARTMENT IS 'Отделение';
COMMENT ON COLUMN D_EMPLOYERS.SYSUSER IS 'Пользователь';
COMMENT ON COLUMN D_EMPLOYERS.AGENT IS 'Контрагент';
COMMENT ON COLUMN D_EMPLOYERS.CID IS 'Каталог';
COMMENT ON COLUMN D_EMPLOYERS.REPORT_SIGN IS 'Подпись врача в отчетах';
COMMENT ON COLUMN D_EMPLOYERS.EMP_NUMB IS 'Табельный номер';
COMMENT ON COLUMN D_EMPLOYERS.QUOT_RESOURCE IS 'Ресурс квотирования';
COMMENT ON COLUMN D_EMPLOYERS.PERSONAL_CARD_GUID IS 'Идентификатор записи личного дела';
COMMENT ON COLUMN D_EMPLOYERS.RATE IS 'Занимаемое количество ставок';

COMMENT ON TABLE D_EMPLOYERS IS 'Персонал';
```

---

#### Таблица №7: D_CABLAB

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_CABLAB.ID IS 'ID';
COMMENT ON COLUMN D_CABLAB.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_CABLAB.DEPARTMENT IS 'Отделение';
COMMENT ON COLUMN D_CABLAB.CL_CODE IS 'Код';
COMMENT ON COLUMN D_CABLAB.CL_NAME IS 'Наименование';
COMMENT ON COLUMN D_CABLAB.CID IS 'Каталог';
COMMENT ON COLUMN D_CABLAB.PID IS 'Кабинет';
COMMENT ON COLUMN D_CABLAB.SCHEDULE_TYPE IS 'Тип назначенного графика(поддерживается автоматически)';
COMMENT ON COLUMN D_CABLAB.DIVISION IS 'Подразделение';
COMMENT ON COLUMN D_CABLAB.BUILDING IS 'Здание';
COMMENT ON COLUMN D_CABLAB.FLOOR IS 'Этаж';
COMMENT ON COLUMN D_CABLAB.IS_COMM IS 'Кабинет платных услуг';
COMMENT ON COLUMN D_CABLAB.BEGIN_DATE IS 'Начало функционирования кабинета платных услуг';
COMMENT ON COLUMN D_CABLAB.END_DATE IS 'Окончание функционирования кабинета платных услуг';
COMMENT ON COLUMN D_CABLAB.CABLAB_TYPE IS 'Тип кабинета';
COMMENT ON COLUMN D_CABLAB.CL_BEGIN_DATE IS 'Дата начала действия';
COMMENT ON COLUMN D_CABLAB.CL_END_DATE IS 'Дата окончания действия';

COMMENT ON TABLE D_CABLAB IS 'Кабинеты и лаборатории';
```

---

#### Таблица №8: D_MP_TYPES

```sql
CREATE TABLE D_MP_TYPES (
    MPT_CODE NUMBER(2) NOT NULL,
    MPT_NAME VARCHAR2(200) NOT NULL,
    CONSTRAINT PK_D_MP_TYPES PRIMARY KEY (MPT_CODE)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_TYPES.MPT_CODE IS 'Код назначения';
COMMENT ON COLUMN D_MP_TYPES.MPT_NAME IS 'Наименование назначение';

COMMENT ON TABLE D_MP_TYPES IS 'Типы назначений лекарственных средств';
```

---

#### Таблица №9: D_DOSE_MEASURES

```sql
CREATE TABLE D_DOSE_MEASURES (
    ID NUMBER(17) NOT NULL,
    CODE NUMBER(8) NOT NULL,
    MNEMOCODE VARCHAR2(200) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    RECIPE_SHOW_FLAG NUMBER(1) NOT NULL,
    MNEMOCODE_LAT VARCHAR2(200),
    CONSTRAINT PK_D_DOSE_MEASURES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_DOSE_MEASURES.ID IS 'ID';
COMMENT ON COLUMN D_DOSE_MEASURES.CODE IS 'Код единицы измерения';
COMMENT ON COLUMN D_DOSE_MEASURES.MNEMOCODE IS 'Мнемокод единицы измерения';
COMMENT ON COLUMN D_DOSE_MEASURES.VERSION IS 'Версия';
COMMENT ON COLUMN D_DOSE_MEASURES.RECIPE_SHOW_FLAG IS 'Отображать при выписке рецепта';
COMMENT ON COLUMN D_DOSE_MEASURES.MNEMOCODE_LAT IS 'Латинское наименование';

COMMENT ON TABLE D_DOSE_MEASURES IS 'Единицы измерения дозировки ЛС';
```

---

#### Таблица №10: D_MP_CONDITIONES

```sql
CREATE TABLE D_MP_CONDITIONES (
    MPC_CODE NUMBER(4) NOT NULL,
    MPC_NAME VARCHAR2(200) NOT NULL,
    CONSTRAINT PK_D_MP_CONDITIONES PRIMARY KEY (MPC_CODE)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_CONDITIONES.MPC_CODE IS 'Код';
COMMENT ON COLUMN D_MP_CONDITIONES.MPC_NAME IS 'Наименование';

COMMENT ON TABLE D_MP_CONDITIONES IS 'Состояния назначения';
```

---

#### Таблица №11: D_MP_ADD_ATTRIBUTES

```sql
CREATE TABLE D_MP_ADD_ATTRIBUTES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    AA_CODE VARCHAR2(20) NOT NULL,
    AA_NAME VARCHAR2(200) NOT NULL,
    KOEFF NUMBER(5,2) NOT NULL,
    CONSTRAINT PK_D_MP_ADD_ATTRIBUTES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.ID IS 'ID';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.CID IS 'Каталог';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.AA_CODE IS 'Код';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.AA_NAME IS 'Наименование';
COMMENT ON COLUMN D_MP_ADD_ATTRIBUTES.KOEFF IS 'Коэффициент пересчёта дозировки';

COMMENT ON TABLE D_MP_ADD_ATTRIBUTES IS 'Дополнительные признаки назначения';
```

---

#### Таблица №12: D_MEASURES

```sql
CREATE TABLE D_MEASURES (
    ID NUMBER(17) NOT NULL,
    CODE NUMBER(10) NOT NULL,
    MNEMOCODE VARCHAR2(150) NOT NULL,
    M_NAME VARCHAR2(200) NOT NULL,
    M_CATEGORY NUMBER(17),
    ETALON NUMBER(1),
    KOEFF NUMBER(17,5),
    CID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    MNEMOCODE_LAT VARCHAR2(200),
    CONSTRAINT PK_D_MEASURES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MEASURES.ID IS 'ID';
COMMENT ON COLUMN D_MEASURES.CODE IS 'Код единицы измерения';
COMMENT ON COLUMN D_MEASURES.MNEMOCODE IS 'Мнемокод единицы измерения';
COMMENT ON COLUMN D_MEASURES.M_NAME IS 'Наименование единиц измерения';
COMMENT ON COLUMN D_MEASURES.M_CATEGORY IS 'Категория ';
COMMENT ON COLUMN D_MEASURES.ETALON IS 'Признак эталона единицы измерения (0 - эталон, 1 - производная)';
COMMENT ON COLUMN D_MEASURES.KOEFF IS 'Коэффициент отношения к эталону (ПР = К*ЭТ)';
COMMENT ON COLUMN D_MEASURES.CID IS 'Каталог';
COMMENT ON COLUMN D_MEASURES.VERSION IS 'Версия';
COMMENT ON COLUMN D_MEASURES.MNEMOCODE_LAT IS 'Латинское наименование';

COMMENT ON TABLE D_MEASURES IS 'Единицы измерения';
```

---

#### Таблица №13: D_REASON_CANCEL_PRESCRIBE

```sql
CREATE TABLE D_REASON_CANCEL_PRESCRIBE (
    ID NUMBER(17) NOT NULL,
    REAS_NAME VARCHAR2(256) NOT NULL,
    REAS_DISCRIPTION VARCHAR2(2000),
    CONSTRAINT PK_D_REASON_CANCEL_PRESCRIBE PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.ID IS 'ID';
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.REAS_NAME IS 'Название причины отмены';
COMMENT ON COLUMN D_REASON_CANCEL_PRESCRIBE.REAS_DISCRIPTION IS 'Описание причины отмены';

COMMENT ON TABLE D_REASON_CANCEL_PRESCRIBE IS 'Причины отмены назначения';
```

---

#### Таблица №14: D_NOMBASE_MNN

```sql
CREATE TABLE D_NOMBASE_MNN (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    MNN_NAME_RUS VARCHAR2(600) NOT NULL,
    MNN_NAME_LAT VARCHAR2(300),
    INPUT_CODE VARCHAR2(50),
    CONSTRAINT PK_D_NOMBASE_MNN PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_NOMBASE_MNN.ID IS 'ID';
COMMENT ON COLUMN D_NOMBASE_MNN.VERSION IS 'Версия';
COMMENT ON COLUMN D_NOMBASE_MNN.CID IS 'Каталог';
COMMENT ON COLUMN D_NOMBASE_MNN.MNN_NAME_RUS IS 'Наименование';
COMMENT ON COLUMN D_NOMBASE_MNN.MNN_NAME_LAT IS 'Латинское наименование';
COMMENT ON COLUMN D_NOMBASE_MNN.INPUT_CODE IS 'Код узла СМНН НСИ';

COMMENT ON TABLE D_NOMBASE_MNN IS 'Международные непатентованные наименования';
```

---

#### Таблица №15: D_HOSP_HISTORY_DEPS

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

-- Комментарии к колонкам:
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.ID IS 'ID';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.PID IS 'История болезни';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.DATE_IN IS 'Дата поступления в отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.DATE_OUT IS 'Дата выписки из отделения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.DEP IS 'Отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.MKB IS 'Диагноз отделения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.MKB_EXACT IS 'Диагноз отделения уточненный';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HEALING_EMP IS 'Лечащий врач';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.PAYMENT_KIND IS 'Вид оплаты';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HOSP_RESULT IS 'Внутрибольничный (промежуточный) результат госпитализации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.KSG IS 'Код клинико-статистической группы';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.BED_TYPE IS 'Профиль койки';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.FACIAL_ACCOUNT IS 'Лицевой счёт';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HHD_PREF IS 'Префикс номера отделения ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HHD_NUMB IS 'Номер отделения ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.VMP IS 'ВМП';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.PRVSID IS 'Предыдущее отделение';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HHD_LEVEL IS 'Номер отделенческого случая ИБ(0 - открытый)';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.IS_LAST IS 'Признак последнего отделения в ИБ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.HOSP_OUTCOME IS 'Исход  госпитализации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.MTS_DESC IS 'Схема лекарственного лечения';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.ALV IS 'Проведение ИВЛ';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.SCALE_REHAB IS 'Шкала реабилитационной маршрутизации';
COMMENT ON COLUMN D_HOSP_HISTORY_DEPS.KSGID IS 'Идентификатор клинико-статистической группы';

COMMENT ON TABLE D_HOSP_HISTORY_DEPS IS 'Истории болезни : отделения';
```

---

#### Таблица №16: D_MP_PRESCRIBES

```sql
CREATE TABLE D_MP_PRESCRIBES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    DISEASECASE NUMBER(17),
    DIRECTION_SERVICE NUMBER(17),
    MP_TYPE NUMBER(2) NOT NULL,
    NOMMODIF NUMBER(17),
    DOSE NUMBER(10,3),
    DOSE_MEASURE NUMBER(17),
    DOSE_QUANT NUMBER(10,3),
    MP_DESCRIBE NUMBER(17),
    MED_USE_METHOD NUMBER(17),
    DATE_BEGIN DATE NOT NULL,
    DATE_END DATE,
    DATE_CANCEL DATE,
    DATE_CREATE DATE NOT NULL,
    MP_CONDITION NUMBER(4),
    CANCEL_REASON VARCHAR2(600),
    EMPLOYER NUMBER(17) NOT NULL,
    MA_ATTRIBUTE NUMBER(17),
    IS_PAT_NOMBASE NUMBER(1) NOT NULL,
    COMMENTARY VARCHAR2(2000),
    QUANTITY NUMBER(20,8),
    USE_UNTIL_CLOSE NUMBER(1) NOT NULL,
    HH_DEP NUMBER(17),
    PATIENT NUMBER(17) NOT NULL,
    CANCEL_DS NUMBER(17),
    CANCEL_EMPLOYER NUMBER(17),
    HID NUMBER(17),
    AGENT_TRFA NUMBER(17),
    ALT_DOSE NUMBER(20,8),
    NOMMODIF_STR VARCHAR2(300),
    IS_RECIPE NUMBER(1) NOT NULL,
    CHEMOTHERAPY NUMBER(17),
    MTS_DESC NUMBER(17),
    IS_ONKO NUMBER(1),
    RTUB_CHEMOPHASE NUMBER(17),
    DURATION NUMBER(10,3),
    DURATION_MEASURE VARCHAR2(200),
    DATE_AFFIRM DATE,
    PRIORITY NUMBER(4),
    NUMBER_SER VARCHAR2(27),
    REACTION VARCHAR2(600),
    MP_CLASS NUMBER(1),
    REAS_CANCEL NUMBER(17),
    SPEED NUMBER(17),
    MNN NUMBER(17),
    MED_FORMS NUMBER(17),
    RED_INJ NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_MP_PRESCRIBES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_PRESCRIBES.ID IS 'ID';
COMMENT ON COLUMN D_MP_PRESCRIBES.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_MP_PRESCRIBES.DISEASECASE IS 'Случай заболевания';
COMMENT ON COLUMN D_MP_PRESCRIBES.DIRECTION_SERVICE IS 'Направление на услугу';
COMMENT ON COLUMN D_MP_PRESCRIBES.MP_TYPE IS 'Тип назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.NOMMODIF IS 'Модификация номенклатуры';
COMMENT ON COLUMN D_MP_PRESCRIBES.DOSE IS 'Дозировка';
COMMENT ON COLUMN D_MP_PRESCRIBES.DOSE_MEASURE IS 'Единица измерения дозировки';
COMMENT ON COLUMN D_MP_PRESCRIBES.DOSE_QUANT IS 'Кол-во доз';
COMMENT ON COLUMN D_MP_PRESCRIBES.MP_DESCRIBE IS 'Периодичность приема';
COMMENT ON COLUMN D_MP_PRESCRIBES.MED_USE_METHOD IS 'Способ введения препарата';
COMMENT ON COLUMN D_MP_PRESCRIBES.DATE_BEGIN IS 'Дата начала';
COMMENT ON COLUMN D_MP_PRESCRIBES.DATE_END IS 'Дата окончания';
COMMENT ON COLUMN D_MP_PRESCRIBES.DATE_CANCEL IS 'Дата отмены';
COMMENT ON COLUMN D_MP_PRESCRIBES.DATE_CREATE IS 'Дата создания назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.MP_CONDITION IS 'Состояние назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.CANCEL_REASON IS 'Причина отмены';
COMMENT ON COLUMN D_MP_PRESCRIBES.EMPLOYER IS 'Врач, создавший назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.MA_ATTRIBUTE IS 'Дополнительный признак назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.IS_PAT_NOMBASE IS 'Используется медикамент пациента : 0 - нет, 1 - да';
COMMENT ON COLUMN D_MP_PRESCRIBES.COMMENTARY IS 'Комментарий';
COMMENT ON COLUMN D_MP_PRESCRIBES.QUANTITY IS 'Кол-во в ОЕИ';
COMMENT ON COLUMN D_MP_PRESCRIBES.USE_UNTIL_CLOSE IS 'Продлевать назначения вплоть до выписки (0-нет;1-да)';
COMMENT ON COLUMN D_MP_PRESCRIBES.HH_DEP IS 'Отделение истории болезни';
COMMENT ON COLUMN D_MP_PRESCRIBES.PATIENT IS 'Пациент';
COMMENT ON COLUMN D_MP_PRESCRIBES.CANCEL_DS IS 'Услуга, на которой отменено назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.CANCEL_EMPLOYER IS 'Врач, отменивший назначение';
COMMENT ON COLUMN D_MP_PRESCRIBES.HID IS 'Ссылка на верхний уровень иерархии';
COMMENT ON COLUMN D_MP_PRESCRIBES.AGENT_TRFA IS 'Трансфузиологический анамнез';
COMMENT ON COLUMN D_MP_PRESCRIBES.ALT_DOSE IS 'Дозировка в дополнительной ЕИ';
COMMENT ON COLUMN D_MP_PRESCRIBES.NOMMODIF_STR IS 'Модификация номенклатуры (ручной ввод)';
COMMENT ON COLUMN D_MP_PRESCRIBES.IS_RECIPE IS 'Назначение для выписки рецепта: 0 - нет, 1 - да';
COMMENT ON COLUMN D_MP_PRESCRIBES.CHEMOTHERAPY IS 'Назначенная химиотерапия';
COMMENT ON COLUMN D_MP_PRESCRIBES.MTS_DESC IS 'Схема лекарственного лечения';
COMMENT ON COLUMN D_MP_PRESCRIBES.IS_ONKO IS 'является препаратом при лечении ЗНО (1-да)';
COMMENT ON COLUMN D_MP_PRESCRIBES.RTUB_CHEMOPHASE IS 'Ссылка на фазу химиотерапии';
COMMENT ON COLUMN D_MP_PRESCRIBES.DURATION IS 'Длительность введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.DURATION_MEASURE IS 'Ед. измерения длительности введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.PRIORITY IS 'Порядок';
COMMENT ON COLUMN D_MP_PRESCRIBES.DATE_AFFIRM IS 'Дата подписания назначения';
COMMENT ON COLUMN D_MP_PRESCRIBES.REAS_CANCEL IS 'Причина отмены из справочника';
COMMENT ON COLUMN D_MP_PRESCRIBES.NUMBER_SER IS 'Серийный номер импланта';
COMMENT ON COLUMN D_MP_PRESCRIBES.REACTION IS 'Реакция на инфузионное введение';
COMMENT ON COLUMN D_MP_PRESCRIBES.MP_CLASS IS 'Класс назначения (0-обычное, 1-комплексное, 2-экстемпоральное, 3-Заявка ОЦР, 4-МНН(обычное), 5 - Комбинированное)';
COMMENT ON COLUMN D_MP_PRESCRIBES.SPEED IS 'Ед. изм. скорости введения';
COMMENT ON COLUMN D_MP_PRESCRIBES.MNN IS 'МНН препарата';
COMMENT ON COLUMN D_MP_PRESCRIBES.MED_FORMS IS 'Лекарственная форма';
COMMENT ON COLUMN D_MP_PRESCRIBES.RED_INJ IS 'Признак редукции';

COMMENT ON TABLE D_MP_PRESCRIBES IS 'Лист назначений';
```

---

#### Таблица №17: D_NOMMODIF

```sql
CREATE TABLE D_NOMMODIF (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    MOD_CODE VARCHAR2(20) NOT NULL,
    CID NUMBER(17) NOT NULL,
    MOD_NAME VARCHAR2(450) NOT NULL,
    BAR_CODE VARCHAR2(200),
    PRODUCER NUMBER(17),
    MED_FORMS NUMBER(17),
    DOSE NUMBER(20,8),
    DOSE_MEASURE NUMBER(17),
    MAIN_MEASURE NUMBER(17) NOT NULL,
    DOSE_PACK NUMBER(7,2),
    LIFE NUMBER(3),
    LIFE_MEASURE NUMBER(17),
    CLIMAT_BEGIN NUMBER(3),
    CLIMAT_END NUMBER(3),
    HUM_BEGIN NUMBER(3),
    HUM_END NUMBER(3),
    LIFE_SPECIAL NUMBER(17),
    ALT_MEASURE NUMBER(17),
    KOEFF NUMBER(10,3),
    LAT_NAME VARCHAR2(200),
    DISCARD_TYPE NUMBER(1) NOT NULL,
    ACM_DOSE_MEASURE NUMBER(17),
    ACM_KOEFF NUMBER(10,3),
    NOM_ACCESS NUMBER(1) NOT NULL,
    ANGRO NUMBER(1) NOT NULL,
    ACM_DOSE VARCHAR2(150),
    COUNTRY NUMBER(17),
    ACM_DOSE_PACK NUMBER(7,2),
    MULTI_MED_USE_METHOD VARCHAR2(150),
    INPUT_CODE VARCHAR2(160),
    ADD_PLACE NUMBER(3),
    DOSE_DEFAULT NUMBER(10,3),
    IS_ACTIVE NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_NOMMODIF PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_NOMMODIF.ID IS 'ID';
COMMENT ON COLUMN D_NOMMODIF.VERSION IS 'Версия';
COMMENT ON COLUMN D_NOMMODIF.PID IS 'Номенклатор';
COMMENT ON COLUMN D_NOMMODIF.MOD_CODE IS 'Мнемокод';
COMMENT ON COLUMN D_NOMMODIF.CID IS 'Каталог';
COMMENT ON COLUMN D_NOMMODIF.MOD_NAME IS 'Наименование';
COMMENT ON COLUMN D_NOMMODIF.BAR_CODE IS 'Штрих-код';
COMMENT ON COLUMN D_NOMMODIF.PRODUCER IS 'Производитель';
COMMENT ON COLUMN D_NOMMODIF.MED_FORMS IS 'Лекарственная форма';
COMMENT ON COLUMN D_NOMMODIF.DOSE IS 'Разовая доза';
COMMENT ON COLUMN D_NOMMODIF.DOSE_MEASURE IS 'Ед.измерения дозы';
COMMENT ON COLUMN D_NOMMODIF.MAIN_MEASURE IS 'Единицы измерения';
COMMENT ON COLUMN D_NOMMODIF.DOSE_PACK IS 'Количество доз в одной ОЕИ';
COMMENT ON COLUMN D_NOMMODIF.LIFE IS 'Срок хранения';
COMMENT ON COLUMN D_NOMMODIF.LIFE_MEASURE IS 'Единица измерения срока хранения';
COMMENT ON COLUMN D_NOMMODIF.CLIMAT_BEGIN IS 'Условия хранения (Температура С)';
COMMENT ON COLUMN D_NOMMODIF.CLIMAT_END IS 'Условия хранения (Температура По)';
COMMENT ON COLUMN D_NOMMODIF.HUM_BEGIN IS 'Условия хранения (Влажность С)';
COMMENT ON COLUMN D_NOMMODIF.HUM_END IS 'Условия хранения (Влажность По)';
COMMENT ON COLUMN D_NOMMODIF.LIFE_SPECIAL IS 'Особые условия хранения ( группа А,Б)';
COMMENT ON COLUMN D_NOMMODIF.ALT_MEASURE IS 'Дополнительная единица измерения';
COMMENT ON COLUMN D_NOMMODIF.KOEFF IS 'Коэффициент пересчета из основной в дополнительную единицу измерения';
COMMENT ON COLUMN D_NOMMODIF.LAT_NAME IS 'Латинское наименование';
COMMENT ON COLUMN D_NOMMODIF.DISCARD_TYPE IS 'Тип списания на пациента:0-не списывать;1-в ОЕИ(по дозировке);2-в ОЕИ с округлением до целого';
COMMENT ON COLUMN D_NOMMODIF.ACM_DOSE_MEASURE IS 'Ед. измерения дозы действующего вещества';
COMMENT ON COLUMN D_NOMMODIF.ACM_KOEFF IS 'Коэффициент пересчета из дозы лекарства в дозу действующего вещества';
COMMENT ON COLUMN D_NOMMODIF.NOM_ACCESS IS 'Доступно в отделениях : 0 - Да, 1 - Нет';
COMMENT ON COLUMN D_NOMMODIF.ANGRO IS 'Относится к неучетной группе (0 - нет, 1 - да)';
COMMENT ON COLUMN D_NOMMODIF.ACM_DOSE IS 'Дозировка действующих веществ';
COMMENT ON COLUMN D_NOMMODIF.COUNTRY IS 'Страна производителя';
COMMENT ON COLUMN D_NOMMODIF.ACM_DOSE_PACK IS 'Количество доз действующего вещ - ва в основной ЕИ';
COMMENT ON COLUMN D_NOMMODIF.MULTI_MED_USE_METHOD IS 'Способ введения';
COMMENT ON COLUMN D_NOMMODIF.INPUT_CODE IS 'Ссылка на внешний id';
COMMENT ON COLUMN D_NOMMODIF.ADD_PLACE IS 'Место, через которое добавлялся препарат (1 - МИС, 2 - Загрузка ЕСКЛП)';
COMMENT ON COLUMN D_NOMMODIF.IS_ACTIVE IS 'Признак активности препарата (0 - нет, 1 - да)';
COMMENT ON COLUMN D_NOMMODIF.DOSE_DEFAULT IS 'Доза по умолчанию';

COMMENT ON TABLE D_NOMMODIF IS 'Модификации номенклатуры';
```

---

#### Таблица №18: D_MP_DESCRIBES

```sql
CREATE TABLE D_MP_DESCRIBES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    D_CODE VARCHAR2(20) NOT NULL,
    D_NAME VARCHAR2(200) NOT NULL,
    D_TYPE NUMBER(2) NOT NULL,
    IS_ACTIVE NUMBER(1) NOT NULL,
    COUNT_DAY_ACTIV NUMBER(2) NOT NULL,
    COUNT_DAY_INACTIV NUMBER(2) NOT NULL,
    CONSTRAINT PK_D_MP_DESCRIBES PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MP_DESCRIBES.ID IS 'ID';
COMMENT ON COLUMN D_MP_DESCRIBES.LPU IS 'ЛПУ';
COMMENT ON COLUMN D_MP_DESCRIBES.CID IS 'Каталог';
COMMENT ON COLUMN D_MP_DESCRIBES.D_CODE IS 'Код';
COMMENT ON COLUMN D_MP_DESCRIBES.D_NAME IS 'Наименование';
COMMENT ON COLUMN D_MP_DESCRIBES.D_TYPE IS 'Привязка : 0 - по времени; 1 - по условию; 2 - условно-временная; 3 - по интервалу; 4 - по времени вручную; 5 – по времени с перерывом; 6 - по графику';
COMMENT ON COLUMN D_MP_DESCRIBES.IS_ACTIVE IS 'Действует: 1 - да; 0 - нет';
COMMENT ON COLUMN D_MP_DESCRIBES.COUNT_DAY_ACTIV IS 'Количество дней приема';
COMMENT ON COLUMN D_MP_DESCRIBES.COUNT_DAY_INACTIV IS 'Количество дней перерыва';

COMMENT ON TABLE D_MP_DESCRIBES IS 'Периодичность назначений';
```

---

#### Таблица №19: D_MED_USE_METHODS

```sql
CREATE TABLE D_MED_USE_METHODS (
    ID NUMBER(17) NOT NULL,
    VERSION NUMBER(17) NOT NULL,
    MUM_CODE VARCHAR2(10) NOT NULL,
    MUM_NAME VARCHAR2(200) NOT NULL,
    SERV_FOR_MES NUMBER(17),
    CONSTRAINT PK_D_MED_USE_METHODS PRIMARY KEY (ID)
);

-- Комментарии к колонкам:
COMMENT ON COLUMN D_MED_USE_METHODS.ID IS 'ID';
COMMENT ON COLUMN D_MED_USE_METHODS.VERSION IS 'Версия';
COMMENT ON COLUMN D_MED_USE_METHODS.MUM_CODE IS 'Код ';
COMMENT ON COLUMN D_MED_USE_METHODS.MUM_NAME IS 'Наименование';
COMMENT ON COLUMN D_MED_USE_METHODS.SERV_FOR_MES IS 'ПМУ для МЭС';

COMMENT ON TABLE D_MED_USE_METHODS IS 'Способы приема лекарственных средств';
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 2
- Загружено тел функций: 2

---

### Функция №1: D_PKG_AGENT_RELATIVES.DEL

```sql
-- Oracle PACKAGE: DEL
--======================================================================
procedure DEL
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
  nVERSION               D_PKG_STD.tREF;
  nCID                   D_PKG_STD.tREF;
  nPERSMEDCARD           D_PKG_STD.tREF;
begin
  -- Поиск версии по ЛПУ --
  D_PKG_VERSIONS.GET_VERSION_BY_LPU(1,pnLPU,'AGENT_RELATIVES',nVERSION);
  -- Поиск каталога --
  begin
    select t.CID, t3.ID
      into nCID, nPERSMEDCARD
      from D_AGENT_RELATIVES t,
           D_AGENTS t1,
           (select t2.ID, t2.AGENT from D_PERSMEDCARD t2 where t2.LPU = pnLPU) t3
     where t.ID        = pnID
       and t.VERSION   = nVERSION
       and t1.ID       = t.PID
       and t3.AGENT(+) = t1.ID;
  exception 
    when NO_DATA_FOUND then
      D_PKG_MSG.RECORD_NOT_FOUND(pnID,'AGENT_RELATIVES');
    when TOO_MANY_ROWS then
      D_P_EXC('Найдено несколько карт пациента в текущем МО.');
  end;
```

---

### Функция №2: D_PKG_MP_PRESCRIBES.SET_MP_CONDITION

```sql
-- Oracle PACKAGE: SET_MP_CONDITION
--======================================================================
procedure SET_MP_CONDITION
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnMP_CONDITION                       in NUMBER,
  psCANCEL_REASON                      in VARCHAR2 default null,
  pdCANCEL_DATE                        in DATE default sysdate,
  pnCANCEL_DS                          in NUMBER default null,
  pnCANCEL_EMPLOYER                    in NUMBER default null,
  pnCANCEL_REASON                      in NUMBER default null,
  pnEMPLOYER                           in NUMBER default null
)
is
  nFLAG                 NUMBER(1);
  nMP_CONDITION         D_MP_PRESCRIBES.MP_CONDITION%type;
  nPATIENT              D_MP_PRESCRIBES.PATIENT%type;
  nDOSE                 D_MP_PRESCRIBES.DOSE%type;
  nIS_MANUAL            NUMBER(1);
begin
  -- Инициализация бизнес-процесса --
  D_PKG_BPENV.BEFOREBP(pnLPU, null, null, null, 'MP_PRESCRIBES_UPDATE', pnID);
  --Проверка состояния
  select r.MP_CONDITION,
         r.PATIENT,
         coalesce(r.DOSE, 0)
    into nMP_CONDITION,
         nPATIENT,
         nDOSE
    from D_MP_PRESCRIBES r
   where r.ID           = pnID
     and r.LPU          = pnLPU;
  if nMP_CONDITION = pnMP_CONDITION then
    D_P_EXC('1.0. Назначение уже находится в указанном состоянии.');
  end if;
  if pnMP_CONDITION = 1 then
    nIS_MANUAL := GET_IS_MANUAL(pnID);
    if nIS_MANUAL = 1 then
      D_P_EXC('1.1. В назначении не заполнена Дозировка и/или Дата окончания назначения. Заполните данные и повторите попытку подтверждения.');
    end if;
    if nIS_MANUAL = 2 then
      D_P_EXC('1.2. В назначении отсутствуют спецификации, заполните обязательные данные, сохраните и повторите попытку подписания.');
    end if;
    if nDOSE = 0 then
      D_P_EXC('1.3. Дозировка не может быть равна 0, необходимо внести изменение.');
    end if;
  end if;
  select count(1)
    into nFLAG
    from D_MP_PRESCRIBES p
   where p.HID                 = pnID;
  if nFLAG > 0 and nvl(pnMP_CONDITION,0) != 4 then
    D_P_EXC('1.4. Для назначения с измененной дозировкой, статус должен быть "Отменено".');
  end if;
  --Проверка наличия невыполненных назначений
  if pnMP_CONDITION = 4 then
    -- Проверка, есть ли исполненные назначения датой больше даты отмены
    select count(ID)
      into nFLAG
      from D_MP_PRESCRIBE_SPECS
     where PID         = pnID
       and LPU         = pnLPU
       and IS_EXECUTED = 0
       and rownum      <= 1;
    if nFLAG = 0 then
      D_P_EXC('1.5. Назначение полностью выполнено. Отмена невозможна');
    end if;
    select count(ID)
      into nFLAG
      from D_MP_PRESCRIBE_SPECS
     where PID         = pnID
       and LPU         = pnLPU
       and IS_EXECUTED = 1
       and EX_DATE > pdCANCEL_DATE
       and rownum      <= 1;
    if nFLAG != 0 then
      D_P_EXC('1.6. Существует исполненное назначение датой больше, чем дата отмены');
    end if;
    --Проверка корректности услуги отмены назначения
    if pnCANCEL_DS is not null then
      select count(1)
        into nFLAG
        from D_DIRECTION_SERVICES g,
             D_DIRECTIONS         d
       where g.ID = pnCANCEL_DS
         and d.ID = g.PID
         and d.PATIENT = nPATIENT;
      if nFLAG = 0 then
        D_P_EXC('1.7. Услуга, на которой отменено назначение, не относится к данному пациенту.');
      end if;
    end if;
    if nMP_CONDITION = 2 and psCANCEL_REASON is null and pnCANCEL_REASON is null then
      select count(1)
        into nFLAG
        from D_OPTIONS o
       where o.SO_CODE   = 'MpPrescribesCancelCause'
         and o.UNITCODE  = 'MP_PRESCRIBES'
         and o.VALUE_NUM = 1;
      if nFLAG != 0 then
        D_P_EXC('1.8. Необходимо заполнить причину отмены.');
      end if;
    end if;
  end if;
  if pnCANCEL_REASON is null then
    update D_MP_PRESCRIBES t set
      t.MP_CONDITION    = pnMP_CONDITION,
      t.DATE_CANCEL     = case when pnMP_CONDITION = 4 then pdCANCEL_DATE end,
      t.CANCEL_REASON   = case when pnMP_CONDITION = 4 then psCANCEL_REASON end,
      t.CANCEL_DS       = case when pnMP_CONDITION = 4 then pnCANCEL_DS end,
      t.CANCEL_EMPLOYER = case when pnMP_CONDITION = 4 then pnCANCEL_EMPLOYER end,
      t.EMPLOYER        = coalesce(pnEMPLOYER, t.EMPLOYER)
     where t.ID  = pnID
       and t.LPU = pnLPU;
  else
    update D_MP_PRESCRIBES t set
      t.MP_CONDITION    = pnMP_CONDITION,
      t.DATE_CANCEL     = case when pnMP_CONDITION = 4 then pdCANCEL_DATE end,
      t.REAS_CANCEL     = case when pnMP_CONDITION = 4 then pnCANCEL_REASON end,
      t.CANCEL_DS       = case when pnMP_CONDITION = 4 then pnCANCEL_DS end,
      t.CANCEL_EMPLOYER = case when pnMP_CONDITION = 4 then pnCANCEL_EMPLOYER end,
      t.EMPLOYER        = coalesce(pnEMPLOYER, t.EMPLOYER)
     where t.ID  = pnID
       and t.LPU = pnLPU;      
  end if;
  -- связь с ИКБР
  D_PKG_AGENT_PRGN_PRESCS.MOD_BY_MP_PRESC(pnLPU,pnID);
  -- Завершение бизнес-процесса --
  D_PKG_BPENV.AFTERBP(pnLPU, null, null, null, 'MP_PRESCRIBES_UPDATE', pnID);
end SET_MP_CONDITION;
```


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 2
- Загружено тел функций: 2

---

### Функция №1: d_pkg_agent_relatives.del

```sql
CREATE OR REPLACE PROCEDURE d_pkg_agent_relatives.del(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nVERSION numeric(17);
    nCID numeric(17);
    nPERSMEDCARD numeric(17);
BEGIN
    CALL d_pkg_versions.get_version_by_lpu(1, pnlpu, 'AGENT_RELATIVES', nversion);
    --  Поиск каталога --
        BEGIN
        SELECT
            t.cid,
            t3.id
        INTO STRICT ncid, npersmedcard
        FROM
            d_agent_relatives t
            CROSS JOIN             d_agents t1 
                    LEFT OUTER JOIN     ( SELECT
        t2.id,
        t2.agent
    FROM
        d_persmedcard t2
    WHERE
        t2.lpu = pnlpu::bigint ) t3 ON t3.agent = t1.id 
        WHERE
            t.id = pnid::bigint
                 AND t.version = nversion::bigint
                 AND t1.id = t.pid
                 AND true = true;
        EXCEPTION
            WHEN no_data_found THEN
                        PERFORM d_pkg_msg.record_not_found(1,pnid,'AGENT_RELATIVES');

            WHEN too_many_rows THEN
                        PERFORM d_p_exc(1,'Найдено несколько карт пациента в текущем ЛПУ.');

    END;
    CALL d_pkg_bpenv.beforebp(pnlpu, nversion, ncid, (null)::numeric, 'AGENT_RELATIVES_DELETE', pnid);
    BEGIN
        DELETE FROM d_agent_relatives where id = pnid::bigint
     AND version = nversion::bigint;
        EXCEPTION
            WHEN others THEN
                        PERFORM d_pkg_msg.iud_errors(1,(sqlerrm)::varchar,'D',(SQLSTATE)::varchar);

    END;
    IF ( NOT FOUND ) THEN
        PERFORM d_pkg_msg.record_not_found(1,pnid,'AGENT_RELATIVES');

    END IF;
    --  Установка даты изменения карты пациента 
        IF npersmedcard IS NOT NULL THEN
        CALL d_pkg_persmedcard.set_moddate(npersmedcard, pnlpu, trunc(sysdate())::timestamp);

    END IF;
    CALL d_pkg_bpenv.afterbp(pnlpu, nversion, ncid, (null)::numeric, 'AGENT_RELATIVES_DELETE', pnid);
END
$procedure$
```

---

### Функция №2: d_pkg_mp_prescribes.set_mp_condition

```sql
CREATE OR REPLACE PROCEDURE d_pkg_mp_prescribes.set_mp_condition(IN pnid numeric, IN pnlpu numeric, IN pnmp_condition numeric, IN pscancel_reason character varying DEFAULT NULL::character varying, IN pdcancel_date timestamp without time zone DEFAULT sysdate(), IN pncancel_ds numeric DEFAULT NULL::numeric, IN pncancel_employer numeric DEFAULT NULL::numeric, IN pncancel_reason numeric DEFAULT NULL::numeric, IN pnemployer numeric DEFAULT NULL::numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    nFLAG NUMERIC(1);
    nMP_CONDITION d_mp_prescribes.mp_condition%TYPE;
    nPATIENT d_mp_prescribes.patient%TYPE;
    nDOSE d_mp_prescribes.dose%TYPE;
    nIS_MANUAL NUMERIC(1);
BEGIN
    CALL d_pkg_bpenv.beforebp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'MP_PRESCRIBES_UPDATE', pnid);
    -- Проверка состояния
    SELECT
        r.mp_condition,
        r.patient,
        coalesce(r.dose,0)
    INTO STRICT nmp_condition, npatient, ndose
    FROM
        d_mp_prescribes r
    WHERE
        r.id = pnid::bigint
             AND r.lpu = pnlpu::bigint;
    IF nmp_condition = pnmp_condition THEN
        PERFORM d_p_exc(1,'1.0. Назначение уже находится в указанном состоянии.');

    END IF;
    IF pnmp_condition = 1 THEN
        nis_manual := d_pkg_mp_prescribes.get_is_manual(pnid)::numeric;
        IF nis_manual = 1 THEN
            PERFORM d_p_exc(1,'1.1. В назначении не заполнена Дозировка и/или Дата окончания назначения. Заполните данные и повторите попытку подтверждения.');

        END IF;
        IF nis_manual = 2 THEN
            PERFORM d_p_exc(1,'1.2. В назначении отсутствуют спецификации, заполните обязательные данные, сохраните и повторите попытку подписания.');

        END IF;
        IF ndose = 0 THEN
            PERFORM d_p_exc(1,'1.3. Дозировка не может быть равна 0, необходимо внести изменение.');

        END IF;

    END IF;
    SELECT
        count(1)
    INTO STRICT nflag
    FROM
        d_mp_prescribes p
    WHERE
        p.hid = pnid::bigint;
    IF nflag > 0
     AND coalesce(pnmp_condition,0) != 4 THEN
        PERFORM d_p_exc(1,'1.4. Для назначения с измененной дозировкой, статус должен быть "Отменено".');

    END IF;
    -- Проверка наличия невыполненных назначений
        IF pnmp_condition = 4 THEN
        SELECT
            count ( * )
        INTO STRICT nflag
        FROM
            ( --  Проверка, есть ли исполненные назначения датой больше даты отмены
                SELECT
                    *
                FROM
                    d_mp_prescribe_specs
                WHERE
                    pid = pnid::bigint
                         AND lpu = pnlpu::bigint
                         AND is_executed = 0
                     LIMIT 1 ) t_alias_0;
        IF nflag = 0 THEN
            PERFORM d_p_exc(1,'1.5. Назначение полностью выполнено. Отмена невозможна');

        END IF;
        SELECT
            count ( * )
        INTO STRICT nflag
        FROM
            ( SELECT
                    *
                FROM
                    d_mp_prescribe_specs
                WHERE
                    pid = pnid::bigint
                         AND lpu = pnlpu::bigint
                         AND is_executed = 1
                         AND ex_date > pdcancel_date
                     LIMIT 1 ) t_alias_1;
        IF nflag != 0 THEN
            PERFORM d_p_exc(1,'1.6. Существует исполненное назначение датой больше, чем дата отмены');

        END IF;
        -- Проверка корректности услуги отмены назначения
                IF pncancel_ds IS NOT NULL THEN
            SELECT
                count(1)
            INTO STRICT nflag
            FROM
                d_direction_services g
                CROSS JOIN                 d_directions d
            WHERE
                g.id = pncancel_ds::bigint
                     AND d.id = g.pid
                     AND d.patient = npatient;
            IF nflag = 0 THEN
                PERFORM d_p_exc(1,'1.7. Услуга, на которой отменено назначение, не относится к данному пациенту.');

            END IF;

        END IF;
        IF nmp_condition = 2
     AND nullif(pscancel_reason,'') IS NULL
     AND pncancel_reason IS NULL THEN
            SELECT
                count(1)
            INTO STRICT nflag
            FROM
                d_options o
            WHERE
                o.so_code = 'MpPrescribesCancelCause'
                     AND o.unitcode = 'MP_PRESCRIBES'
                     AND o.value_num = 1;
            IF nflag != 0 THEN
                PERFORM d_p_exc(1,'1.8. Необходимо заполнить причину отмены.');

            END IF;

        END IF;

    END IF;
    IF pncancel_reason IS NULL THEN
        update d_mp_prescribes t set mp_condition = pnmp_condition , date_cancel = (CASE
            WHEN pnmp_condition = 4 THEN pdcancel_date
        END)::timestamp , cancel_reason = (CASE
            WHEN pnmp_condition = 4 THEN pscancel_reason
        END)::varchar , cancel_ds = (CASE
            WHEN pnmp_condition = 4 THEN pncancel_ds
        END)::bigint , cancel_employer = (CASE
            WHEN pnmp_condition = 4 THEN pncancel_employer
        END)::bigint , employer = coalesce(pnemployer,t.employer) where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;

    ELSE
        update d_mp_prescribes t set mp_condition = pnmp_condition , date_cancel = (CASE
            WHEN pnmp_condition = 4 THEN pdcancel_date
        END)::timestamp , reas_cancel = (CASE
            WHEN pnmp_condition = 4 THEN pncancel_reason
        END)::bigint , cancel_ds = (CASE
            WHEN pnmp_condition = 4 THEN pncancel_ds
        END)::bigint , cancel_employer = (CASE
            WHEN pnmp_condition = 4 THEN pncancel_employer
        END)::bigint , employer = coalesce(pnemployer,t.employer) where t.id = pnid::bigint
             AND t.lpu = pnlpu::bigint;

    END IF;
    CALL d_pkg_agent_prgn_prescs.mod_by_mp_presc(pnlpu, pnid);
    CALL d_pkg_bpenv.afterbp(pnlpu, (null)::numeric, (null)::numeric, (null)::numeric, 'MP_PRESCRIBES_UPDATE', pnid);
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
