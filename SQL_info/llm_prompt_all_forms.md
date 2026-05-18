# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ /Forms/Certificate/certificate_forms.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** /Forms/Certificate/certificate_forms.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\Certificate\certificate_forms.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Mon May 18 09:52:42 GMT+07:00 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 4
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** D3 DataSet
**Имя компонента:** DS_CERTIFICATE_FORMS
**Источник:** /Forms/Certificate/certificate_forms.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\Certificate\certificate_forms.frm

**SQL код:**

```xml
<cmpDataSet name="DS_CERTIFICATE_FORMS" mode="Range">
        <cmpSubSelect condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            select cf.ID,
                   cont.ID CONT_ID,
                   cf.C_NUM_CHAR,
                   to_char(cf.DATE_OUT, 'dd.mm.yyyy') DATE_OUT,
                   cf.C_STATE,
                   cf.C_STATE_NAME,
                   cf.GIVEN_OUT_EMP_FIO,
                   cf.C_KIND,
                   pmc.ID PATIENT_ID,
                   pmc.FIO PATIENT_FIO,
                   cf.C_NUM,
                   cf.C_SER,
                   cont.CF_FORM352,
                   cf.IS_ACTS,
                   cf.IS_ACTS_MNEMO
              from D_V_CERTIFICATE_MOVING s
                   join D_V_CERTIFICATE_MOVING_SP s1 on s1.PID = s.ID
                   join D_V_CERTIFICATE_FORMS cf
                        on cf.ID = s1.CERTIFICATE_ID
                       and cf.C_JOUR_ID = s.CJ_TO_ID
                       and cf.LPU = :LPU
                   left join (select bc.ID,
                                     bc.PID,
                                     1 C_KIND,
                                     bc.AGENT_ID,
                                     bc.CF_FORM352
                                from D_V_CF_BIRTH_CONTENTS_BASE bc
                             union all
                              select dc.ID,
                                     dc.PID,
                                     2 C_KIND,
                                     dc.AGENT_ID,
                                     dc.CF_FORM352
                                from D_V_CF_DEATH_CONTENTS_BASE dc
                             union all
                              select pc.ID,
                                     pc.PID,
                                     3 C_KIND,
                                     pc.AGENT_ID,
                                     pc.CF_FORM352
                                from D_V_CF_PERDEATH_CONTENTS_BASE pc) cont
                        on cont.PID = cf.ID
                       and cont.C_KIND = cf.C_KIND
                   left join D_V_PERSMEDCARD_FIO pmc
                        on pmc.AGENT = cont.AGENT_ID
                       and pmc.LPU = :LPU
            where s.ID = :C_ID
            ]]>
        </cmpSubSelect>
        <cmpSubSelect condition="TYPE_DATABASE=POSTGRE">
            <![CDATA[
            SELECT
                cf.id,
                cont.id AS cont_id,
                cf.c_num_char,
                to_char(cf.date_out,'dd.mm.yyyy')  date_out,
                cf.c_state,
                cf.c_state_name,
                cf.given_out_emp_fio,
                cf.c_kind,
                pmc.id AS patient_id,
                pmc.fio AS patient_fio,
                cf.c_num,
                cf.c_ser,
                cont.cf_form352,
                cf.is_acts,
                cf.is_acts_mnemo
            FROM d_v_certificate_moving s
            JOIN d_v_certificate_moving_sp s1 ON s1.pid = s.id
            JOIN d_v_certificate_forms cf ON cf.id = s1.certificate_id
                AND cf.c_jour_id = s.cj_to_id
                AND cf.lpu = :LPU::bigint
            LEFT JOIN (
                SELECT
                    bc.id,
                    bc.pid,
                    1 AS c_kind,
                    bc.agent_id,
                    bc.cf_form352
                FROM d_v_cf_birth_contents_base bc
                UNION ALL
                SELECT
                    dc.id,
                    dc.pid,
                    2 AS c_kind,
                    dc.agent_id,
                    dc.cf_form352
                FROM d_v_cf_death_contents_base dc
                UNION ALL
                SELECT
                    pc.id,
                    pc.pid,
                    3 AS c_kind,
                    pc.agent_id,
                    pc.cf_form352
                FROM d_v_cf_perdeath_contents_base pc
            ) cont ON cont.pid = cf.id
                  AND cont.c_kind = cf.c_kind
            LEFT JOIN d_v_persmedcard_fio pmc ON pmc.agent = cont.agent_id
                AND pmc.lpu = :LPU::bigint
            WHERE
                s.id = :C_ID::bigint
            ]]>
        </cmpSubSelect>
        <cmpVariable name="LPU" src="LPU" srctype="session" />
        <cmpVariable name="C_ID" get="vC_ID" src="C_ID" srctype="var" />
        <cmpVariable type="count" srctype="var" src="dscount" default="20" />
        <cmpVariable type="start" srctype="var" src="dsstart" default="1" />
    </cmpDataSet>
```

**Используемые таблицы/вьюхи:** D_V_CERTIFICATE_MOVING, D_V_CERTIFICATE_MOVING_SP, D_V_CERTIFICATE_FORMS, D_V_CF_BIRTH_CONTENTS_BASE, D_V_CF_DEATH_CONTENTS_BASE, D_V_CF_PERDEATH_CONTENTS_BASE, D_V_PERSMEDCARD_FIO

---

### Запрос №2

**Тип компонента:** D3 Action
**Имя компонента:** CheckChangeState
**Источник:** /Forms/Certificate/certificate_forms.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\Certificate\certificate_forms.frm

**SQL код:**

```xml
<cmpAction name="CheckChangeState">
        <cmpActionRouter condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            begin
              :HAS_SEMD := 0;
              select t.C_STATE,
                     (select count(1) from D_V_CF_DEATH_CONTENTS_BASE dc where dc.PID = t.ID),
                     (select count(1) from D_V_CF_PERDEATH_CONTENTS_BASE pdc where pdc.PID = t.ID),
                     (select count(1) from D_V_CF_BIRTH_CONTENTS_BASE bc where bc.PID = t.ID)
                into :OLD_STATE,
                     :DEATH_CONT_COUNT,
                     :PER_DEATH_CONT_COUNT,
                     :BIRTH_CONT_COUNT
                from D_V_CERTIFICATE_FORMS_BASE t
               where t.ID = :ID;

              select count(1)
                into :HAS_SEMD
                from D_V_EHRS_BASE e
                     join D_V_EHR_STATES_BASE es on es.PID = e.ID
               where e.UNIT_ID = :CONT_ID
                 and e.UNIT in ('CF_BIRTH_CONTENTS', 'CF_DEATH_CONTENTS', 'CF_PERDEATH_CONTENTS')
                 and es.SGN_HASH is not null
                 and rownum = 1;
            end;
            ]]>
        </cmpActionRouter>
        <cmpActionRouter condition="TYPE_DATABASE=POSTGRE">
            <![CDATA[
            DO $$
            DECLARE
                PL2PG_VAR_HAS_SEMD             varchar := :HAS_SEMD;
                PL2PG_VAR_OLD_STATE            varchar := :OLD_STATE;
                PL2PG_VAR_DEATH_CONT_COUNT     varchar := :DEATH_CONT_COUNT;
                PL2PG_VAR_PER_DEATH_CONT_COUNT varchar := :PER_DEATH_CONT_COUNT;
                PL2PG_VAR_BIRTH_CONT_COUNT     varchar := :BIRTH_CONT_COUNT;
                PL2PG_VAR_ID                   varchar := :ID;
                PL2PG_VAR_CONT_ID              varchar := :CONT_ID;
            BEGIN
                PL2PG_VAR_HAS_SEMD := 0;

                SELECT t.c_state,
                    (SELECT count(1) FROM d_v_cf_death_contents_base dc WHERE dc.pid = t.id ),
                    (SELECT count(1) FROM d_v_cf_perdeath_contents_base pdc WHERE pdc.pid = t.id ),
                    (SELECT count(1) FROM d_v_cf_birth_contents_base bc WHERE bc.pid = t.id )
                INTO STRICT
                    PL2PG_VAR_OLD_STATE,
                    PL2PG_VAR_DEATH_CONT_COUNT,
                    PL2PG_VAR_PER_DEATH_CONT_COUNT,
                    PL2PG_VAR_BIRTH_CONT_COUNT
                FROM d_v_certificate_forms_base t
                WHERE t.id = PL2PG_VAR_ID::bigint;
                SELECT count (*)
                INTO STRICT PL2PG_VAR_HAS_SEMD
                FROM (
                    SELECT 1
                    FROM d_v_ehrs_base e
                    JOIN d_v_ehr_states_base es ON es.pid = e.id
                    WHERE e.unit_id = PL2PG_VAR_CONT_ID
                      AND e.unit IN ('CF_BIRTH_CONTENTS' , 'CF_DEATH_CONTENTS' , 'CF_PERDEATH_CONTENTS')
                      AND nullif(es.sgn_hash,'') IS NOT NULL
                    LIMIT 1
                ) t_alias_0;
            END $$;
            ]]>
        </cmpActionRouter>
        <cmpActionVar name="ID" src="GRID_CERTIFICATE_FORMS" srctype="ctrl" />
        <cmpActionVar name="CONT_ID" src="CONT_ID" srctype="var" />
        <cmpActionVar name="OLD_STATE" src="OLD_STATE" srctype="var" put="" len="17" />
        <cmpActionVar name="DEATH_CONT_COUNT" src="DEATH_CONT_COUNT" srctype="var" put="" len="1" />
        <cmpActionVar name="PER_DEATH_CONT_COUNT" src="PER_DEATH_CONT_COUNT" srctype="var" put="" len="1" />
        <cmpActionVar name="BIRTH_CONT_COUNT" src="BIRTH_CONT_COUNT" srctype="var" put="" len="1" />
        <cmpActionVar name="HAS_SEMD" src="HAS_SEMD" srctype="var" put="" len="1" />
    </cmpAction>
```

**Используемые таблицы/вьюхи:** D_V_CF_DEATH_CONTENTS_BASE, D_V_CF_PERDEATH_CONTENTS_BASE, D_V_CF_BIRTH_CONTENTS_BASE, D_V_CERTIFICATE_FORMS_BASE, D_V_EHRS_BASE, D_V_EHR_STATES_BASE

---

### Запрос №3

**Тип компонента:** D3 Action
**Имя компонента:** SetState
**Источник:** /Forms/Certificate/certificate_forms.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\Certificate\certificate_forms.frm

**SQL код:**

```xml
<cmpAction name="SetState">
        <![CDATA[
        begin
          D_PKG_CERTIFICATE_FORMS.SET_STATE(:ID, :LPU, :STATE, :EMPLOYER);
        end;
        ]]>
        <cmpActionVar name="LPU" src="LPU" srctype="session" />
        <cmpActionVar name="EMPLOYER" src="EMPLOYER" srctype="session" />
        <cmpActionVar name="ID" src="GRID_CERTIFICATE_FORMS" srctype="ctrl" get="vID" />
        <cmpActionVar name="STATE" src="STATE" srctype="var" get="vSTATE" />
    </cmpAction>
```

**Используемые пакеты/функции:** D_PKG_CERTIFICATE_FORMS.SET_STATE

---

### Запрос №4

**Тип компонента:** D3 Action
**Имя компонента:** DEL_BLANK
**Источник:** /Forms/Certificate/certificate_forms.frm
**Базовая форма:** C:\AppServ\www\4_mis_MEDDEV-151437\Forms\Certificate\certificate_forms.frm

**SQL код:**

```xml
<cmpAction name="DEL_BLANK">
        <cmpActionRouter condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            declare
              nIS_COND              NUMBER(1);
            begin
              nIS_COND := :HAS_SEMD;

              if nIS_COND = 0 then
                begin
                  select 2
                    into nIS_COND
                    from D_V_CERTIFICATE_FORMS_BASE t
                   where t.INSTEAD_CF = :ID
                     and rownum = 1;
                exception when NO_DATA_FOUND then
                  null;
                end;
              end if;
              if nIS_COND != 0 then
                if nIS_COND = 1 then
                  :ERR_MSG := 'Для свидетельства сформирован СЭМД, удаление запрещено.';
                else
                  D_PKG_CERTIFICATE_FORMS.SET_STATE(pnID            => :ID,
                                                    pnLPU           => :LPU,
                                                    pnC_STATE       => 3,
                                                    pnGIVEN_OUT_EMP => null
                                                   );
                end if;
              else
                D_PKG_CERTIFICATE_FORMS.CLEAR(:ID, :LPU);
              end if;
            end;
            ]]>
        </cmpActionRouter>
        <cmpActionRouter condition="TYPE_DATABASE=POSTGRE">
            <![CDATA[
            DO $$
            DECLARE
                nis_cond numeric(1) ;
            BEGIN
                nIS_COND := :HAS_SEMD::numeric;

                IF nis_cond = 0 THEN
                    SELECT 2
                      INTO nis_cond
                      FROM d_v_certificate_forms_base t
                     WHERE t.instead_cf = PL2PG_VAR_ID::bigint
                     LIMIT 1;

                    IF NOT FOUND THEN
                        nIS_COND := 0;
                    END IF;
                END IF;

                IF nis_cond != 0 THEN
                    IF nis_cond = 1 THEN
                        :ERR_MSG := 'Для свидетельства сформирован СЭМД, удаление запрещено.';
                    ELSE
                        CALL D_PKG_CERTIFICATE_FORMS.SET_STATE(
                            pnID            => :ID::bigint,
                            pnLPU           => :LPU::numeric,
                            pnC_STATE       => 3,
                            pnGIVEN_OUT_EMP => NULL
                        );
                    END IF;
                END IF;
            END $$;
            ]]>
        </cmpActionRouter>
        <cmpActionVar name="LPU" src="LPU" srctype="session" />
        <cmpActionVar name="ID" src="GRID_CERTIFICATE_FORMS" srctype="ctrl" />
        <cmpActionVar name="CONT_ID" src="CONT_ID" srctype="var" />
        <cmpActionVar name="HAS_SEMD" src="HAS_SEMD" srctype="var" />
        <cmpActionVar name="ERR_MSG" src="ERR_MSG" srctype="var" put="" />
    </cmpAction>
```

**Используемые таблицы/вьюхи:** D_V_CERTIFICATE_FORMS_BASE
**Используемые пакеты/функции:** D_PKG_CERTIFICATE_FORMS.SET_STATE, D_PKG_CERTIFICATE_FORMS.CLEAR


## 2. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных PostgreSQL.

**Статистика:**
- Всего вьюх: 10

---

### Вьюха №1: D_V_CERTIFICATE_MOVING

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CERTIFICATE_MOVING
 SELECT t.id,
    t.lpu,
    t.cj_from AS cj_from_id,
    t1.cj_name AS cj_from,
    t.cj_to AS cj_to_id,
    t2.cj_name AS cj_to,
    t.moving_type,
    t.moving_date,
    t.employer,
    t.employer_to,
    t.range_begin,
    t.range_end,
    d_pkg_certificate_forms.get_c_num(t.range_begin::numeric) AS range_begin_char,
    d_pkg_certificate_forms.get_c_num(t.range_end::numeric) AS range_end_char
   FROM d_certificate_moving t
     CROSS JOIN d_certificate_journals t2
     LEFT JOIN d_certificate_journals t1 ON t1.id = t.cj_from
  WHERE true = true AND t2.id = t.cj_to AND d_pkg_cmp.num(t1.cj_type::numeric, 2::numeric) = 0::numeric AND d_pkg_cmp.num(t2.cj_type::numeric, 2::numeric) = 0::numeric AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'CERTIFICATE_MOVING'::text));
```

---

### Вьюха №2: D_V_CERTIFICATE_MOVING_SP

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CERTIFICATE_MOVING_SP
 SELECT t.id,
    t.lpu,
    t.pid,
    t.certificate AS certificate_id,
    t1.c_ser AS certificate
   FROM d_certificate_moving_sp t
     CROSS JOIN d_certificate_forms t1
  WHERE t1.id = t.certificate AND (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'CERTIFICATE_MOVING_SP'::text));
```

---

### Вьюха №3: D_V_CERTIFICATE_FORMS

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CERTIFICATE_FORMS
 SELECT t.id,
    t.lpu,
    t.c_jour AS c_jour_id,
    t1.cj_name AS c_jour,
    t1.cj_type AS c_jour_type,
    t.c_kind,
    t.c_ser,
    t.c_num,
    t.c_state,
    t.date_out,
    t.given_out_emp,
    t.instead_cf,
    t.lost_cf,
    t.date_lost,
    t.spine,
    ( SELECT d_pkg_str_tools.fio(t_e.surname::character varying, t_e.firstname::character varying, t_e.lastname::character varying) AS fio
           FROM d_v_employers t_e
          WHERE t_e.id = t.given_out_emp) AS given_out_emp_fio,
    t3.cs_name AS c_state_name,
    d_pkg_certificate_forms.get_c_num(t.c_num::numeric) AS c_num_char,
    t.lpu_giver_handle,
    t.lpu_giver,
    t.another_lpu_cert,
    t4.lpu_name AS lpu_giver_name,
    t.internal_number,
    t.act_numb,
    t.act_date,
    t.fio_emp_zags,
    t.name_zags,
    t.blocked,
    t.date_first_fill,
    t.cf_form352,
    t.cf_type,
        CASE
            WHEN t.cf_type = 0::numeric THEN 'Бумажное'::character varying
            WHEN t.cf_type = 1::numeric THEN 'Электронное'::character varying
            ELSE NULL::character varying
        END AS cf_type_name,
    t.is_acts,
        CASE
            WHEN t.is_acts = 1::numeric THEN 'Да'::character varying
            ELSE 'Нет'::character varying
        END AS is_acts_mnemo,
    t.print_count
   FROM d_certificate_forms t
     LEFT JOIN d_certificate_journals t1 ON t1.id = t.c_jour
     JOIN d_certificate_states t3 ON t3.cs_code = t.c_state
     LEFT JOIN d_lpudict t4 ON t4.id = t.lpu_giver
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'CERTIFICATE_FORMS'::text
         LIMIT 1));
```

---

### Вьюха №4: D_V_CF_BIRTH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CF_BIRTH_CONTENTS_BASE
 SELECT id,
    lpu,
    pid,
    agent AS agent_id,
    m_agent AS m_agent_id,
    m_unknown_birth,
    dir_marital_s AS dir_marital_s_id,
    dir_education AS dir_education_id,
    dir_employment AS dir_employment_id,
    first_visit,
    child_count,
    dir_childbirth AS dir_childbirth_id,
    child_weight,
    child_length,
    poly_account,
    poly_all,
    dir_person_births AS dir_person_births_id,
    agent_to AS agent_to_id,
    given_out_emp,
    birth_place,
    birth_place_is_city,
    pl_birth_city,
    pl_birth_house,
    pl_birth_building,
    pl_birth_korpus,
    pl_birth_flat,
    surname_hand,
    name_hand,
    lastname_hand,
    ser_doc_receiver,
    number_doc_receiver,
    doc_who,
    relation_child_old,
    date_receive,
    print_child_surname,
    header_emp,
    dir_person_births_employer,
    relative,
    date_doc_receiver,
    ser_doc_auth_receiver,
    number_doc_auth_receiver,
    doc_auth_who,
    date_doc_auth_receiver,
    pl_mother_rayon,
    pl_mother_city,
    accuracy_date_munkn,
    cf_form352,
    snils_receiver,
    doctype_receiver,
    lpu_giver,
    written_from_words,
    receiver_agreement,
    dir_pb_other_surname,
    dir_pb_other_name,
    dir_pb_other_lastname,
    hosp_history,
    relation_child,
    service_childbirth,
    birth_place_address
   FROM d_cf_birth_contents cbc
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = cbc.lpu AND ur.unitcode::text = 'CF_BIRTH_CONTENTS'::text
         LIMIT 1));
```

---

### Вьюха №5: D_V_CF_DEATH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CF_DEATH_CONTENTS_BASE
 SELECT id,
    lpu,
    pid,
    status,
    agent AS agent_id,
    relative,
    agent_to AS agent_to_id,
    date_death,
        CASE
            WHEN accuracy_date_death = 0::numeric THEN to_char(date_death, 'DD.MM.YYYY hh24:mi'::text)::character varying
            WHEN accuracy_date_death = 1::numeric THEN concat(to_char(date_death, 'DD.MM.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 2::numeric THEN concat('XX', to_char(date_death, '.MM.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 3::numeric THEN concat('XX.XX', to_char(date_death, '.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 4::numeric THEN 'XX.XX.XXXX --'::character varying
            ELSE to_char(date_death)
        END AS date_death_str,
    place_death AS place_death_id,
    pl_death_city,
    pl_death_street,
    pl_death_house,
    pl_death_flat,
    pl_death_is_city,
    pl_death_building,
    pl_death_korpus,
    dir_place AS dir_place_id,
    dir_child_born AS dir_child_born_id,
    child_weight,
    child_count,
    child_m_agent,
    dir_marital_s AS dir_marital_s_id,
    dir_education AS dir_education_id,
    dir_employment AS dir_employment_id,
    dir_death_from AS dir_death_from_id,
    date_accident,
    accident_fact,
    dir_reason_set AS dir_reason_set_id,
    reason_set_employer AS reason_set_employer_id,
    dir_basis_est AS dir_basis_est_id,
    dir_dtp AS dir_dtp_id,
    dir_pregndeath AS dir_pregndeath_id,
    given_out_emp,
    surname_hand,
    name_hand,
    lastname_hand,
    ser_doc_receiver,
    number_doc_receiver,
    doc_who,
    date_doc_receiver,
    snils_hand,
    doc_when,
    child_m_sur,
    child_m_name,
    child_m_lastname,
    child_m_birth_date,
    child_m_marital_s AS child_m_marital_s_id,
    child_m_education AS child_m_education_id,
    child_m_employment AS child_m_employment_id,
    header_emp AS header_emp_id,
    assigned_person AS assigned_person_id,
    assigned_date,
    division_giver AS division_giver_id,
    unknown_place_death,
    injure_kind,
    accuracy_date_death,
    accuracy_date_accident,
    reason_set_emp_an_lpu_hand,
    given_out_emp_an_lpu_hand,
    header_lpu_an_lpu_hand,
    assigned_person_an_lpu_hand,
    dir_place_lpu,
    law_result,
    law_result_num,
    cf_form352,
    lpu_giver,
    instead_cf,
    is_child_under_one,
    receiver_agreement,
    doctype_receiver,
    place_death_address
   FROM d_cf_death_contents cdc
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = cdc.lpu AND ur.unitcode::text = 'CF_DEATH_CONTENTS'::text
         LIMIT 1));
```

---

### Вьюха №6: D_V_CF_PERDEATH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CF_PERDEATH_CONTENTS_BASE
 SELECT id,
    lpu,
    pid,
    status,
    agent AS agent_id,
    born_is_live,
    certif_birth,
    date_birth,
    date_death,
        CASE
            WHEN accuracy_date_death = 0::numeric THEN to_char(date_death, 'DD.MM.YYYY hh24:mi'::text)::character varying
            WHEN accuracy_date_death = 1::numeric THEN concat(to_char(date_death, 'DD.MM.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 2::numeric THEN concat('XX', to_char(date_death, '.MM.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 3::numeric THEN concat('XX.XX', to_char(date_death, '.YYYY'::text), ' --')::character varying
            WHEN accuracy_date_death = 4::numeric THEN 'XX.XX.XXXX --'::character varying
            ELSE to_char(date_death)
        END AS date_death_str,
    dir_death_came AS dir_death_came_id,
    m_agent AS m_agent_id,
    m_unknown_birth,
    dir_marital_s AS dir_marital_s_id,
    dir_education AS dir_education_id,
    dir_employment AS dir_employment_id,
    birth_count,
    place_death AS place_death_id,
    pl_death_city,
    pl_death_is_city,
    pl_death_street,
    pl_death_house,
    pl_death_flat,
    pl_death_building,
    pl_death_korpus,
    dir_place AS dir_place_id,
    child_weight,
    child_length,
    child_count,
    poly_account,
    poly_all,
    dir_death_from AS dir_death_from_id,
    dir_person_births AS dir_person_births_id,
    dir_reason_set AS dir_reason_set_id,
    dir_basis_est AS dir_basis_est_id,
    agent_to,
    given_out_emp,
    surname_hand,
    name_hand,
    lastname_hand,
    ser_doc_receiver,
    number_doc_receiver,
    doc_who,
    relation_child_old,
    date_receive,
    header_emp AS header_emp_id,
    assigned_person AS assigned_person_id,
    assigned_date,
    division_giver AS division_giver_id,
    print_child_surname,
    accuracy_date_death,
    dir_place_lpu,
    law_result,
    law_result_num,
    snils_hand,
    cf_form352,
    lpu_giver,
    instead_cf,
    doctype_receiver,
    receiver_agreement,
    dir_person_births_employer,
    dir_pb_other_surname,
    dir_pb_other_name,
    dir_pb_other_lastname,
    hosp_history,
    relation_child,
    date_doc_receiver,
    service_childbirth,
    place_death_address
   FROM d_cf_perdeath_contents cdc
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = cdc.lpu AND ur.unitcode::text = 'CF_PERDEATH_CONTENTS'::text
         LIMIT 1));
```

---

### Вьюха №7: D_V_PERSMEDCARD_FIO

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_PERSMEDCARD_FIO
 SELECT p.id,
    p.cid,
    p.lpu,
    p.agent,
    p.card_numb,
    ap.firstname,
    ap.surname,
    ap.lastname,
    ap.birthdate,
    d_pkg_str_tools.fio(ap.surname::character varying, ap.firstname::character varying, ap.lastname::character varying) AS fio,
    concat(ap.surname, ' ', ap.firstname, ' ', ap.lastname) AS full_fio,
        CASE ap.sex
            WHEN 0 THEN 'Женский'::text
            WHEN 1 THEN 'Мужской'::text
            ELSE NULL::text
        END AS sex,
    ap.sex AS nsex,
    ap.agn_name AS pat_agn_name,
    p.createdate,
    p.moddate,
    p.note,
        CASE p.rhesus
            WHEN 0 THEN 'RH-'::text
            WHEN 1 THEN 'RH+'::text
            ELSE NULL::text
        END AS rhesus,
    p.rhesus AS nrhesus,
    p.bloodgroupe,
    p.ecolor,
    p.pmc_type,
    ap.snils,
    ap.deathdate,
    ap.education,
    p.emp_id,
    ( SELECT d_pkg_str_tools.fio(ae.surname::character varying, ae.firstname::character varying, ae.lastname::character varying) AS fio
           FROM d_agents ae
          WHERE ae.id = e.agent) AS emp_fio,
    ap.accuracy_date_death
   FROM d_persmedcard p
     JOIN d_agents ap ON ap.id = p.agent
     LEFT JOIN d_employers e ON e.id = p.emp_id
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.catalog = p.cid AND ur.unitcode::text = 'PERSMEDCARD'::text
         LIMIT 1));
```

---

### Вьюха №8: D_V_CERTIFICATE_FORMS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_CERTIFICATE_FORMS_BASE
 SELECT id,
    lpu,
    c_jour AS c_jour_id,
    c_kind,
    c_ser,
    c_num,
    c_state,
    date_out,
    given_out_emp,
    instead_cf,
    lost_cf,
    date_lost,
    spine,
    lpu_giver_handle,
    lpu_giver,
    another_lpu_cert,
    internal_number,
    act_numb,
    act_date,
    fio_emp_zags,
    name_zags,
    blocked,
    date_first_fill,
    cf_form352,
    cf_type,
    is_acts,
    print_count
   FROM d_certificate_forms t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'CERTIFICATE_FORMS'::text
         LIMIT 1));
```

---

### Вьюха №9: D_V_EHRS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_EHRS_BASE
 SELECT id,
    lpu,
    doc_type,
    unit,
    unit_id,
    created_by,
    create_date,
    rep_code,
    patient,
    ex_system,
    edf_type,
    diseasecase
   FROM d_ehrs t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'EHRS'::text
         LIMIT 1));
```

---

### Вьюха №10: D_V_EHR_STATES_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- PostgreSQL View: D_V_EHR_STATES_BASE
 SELECT id,
    lpu,
    pid,
    created_by,
    create_date,
    updated_by,
    update_date,
    sgn_author,
    sgn_timestamp,
    sgn_hash,
    sgn,
    sgn_doc,
    sgnreadable_hash,
    sgnreadable,
    sgnreadable_doc,
    is_locked,
    vers,
    sgn_zip,
    local_uid,
    repo_document_id,
    ehr_life_end,
    status
   FROM d_ehr_states t
  WHERE (EXISTS ( SELECT NULL::text AS "null"
           FROM d_v_urprivs ur
          WHERE ur.lpu = t.lpu AND ur.unitcode::text = 'EHR_STATES'::text
         LIMIT 1));
```


## 3. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠

Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, извлеченные из базы данных Oracle.

**Статистика:**
- Всего вьюх: 10

---

### Вьюха №1: D_V_CERTIFICATE_MOVING

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CERTIFICATE_MOVING
select -- Представление для раздела : Движение бланков по журналам
       t.ID,
       t.LPU,
       t.CJ_FROM             CJ_FROM_ID,
       t1.CJ_NAME            CJ_FROM,
       t.CJ_TO               CJ_TO_ID,
       t2.CJ_NAME            CJ_TO,
       t.MOVING_TYPE,
       t.MOVING_DATE,
       t.EMPLOYER,
       t.EMPLOYER_TO,
       t.RANGE_BEGIN,
       t.RANGE_END,
       D_PKG_CERTIFICATE_FORMS.GET_C_NUM(t.RANGE_BEGIN)
                             RANGE_BEGIN_CHAR,
       D_PKG_CERTIFICATE_FORMS.GET_C_NUM(t.RANGE_END)
                             RANGE_END_CHAR
  from D_CERTIFICATE_MOVING            t,  -- Движение бланков по журналам
       D_CERTIFICATE_JOURNALS          t1,  -- Журналы бланков
       D_CERTIFICATE_JOURNALS          t2  -- Журналы бланков
 where t1.ID(+)     = t.CJ_FROM
   and t2.ID        = t.CJ_TO
   and D_PKG_CMP.NUM(t1.CJ_TYPE,2) = 0
   and D_PKG_CMP.NUM(t2.CJ_TYPE,2) = 0
   and exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'CERTIFICATE_MOVING')
```

---

### Вьюха №2: D_V_CERTIFICATE_MOVING_SP

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CERTIFICATE_MOVING_SP
select -- Представление для раздела : Движение бланков по журналам: спецификация
       t.ID,
       t.LPU,
       t.PID,
       t.CERTIFICATE         CERTIFICATE_ID,
       t1.C_SER              CERTIFICATE
  from D_CERTIFICATE_MOVING_SP         t,	-- Движение бланков по журналам: спецификация
       D_CERTIFICATE_FORMS             t1	-- Бланки медицинских свидетельств
 where t1.ID        = t.CERTIFICATE
   and exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'CERTIFICATE_MOVING_SP')
```

---

### Вьюха №3: D_V_CERTIFICATE_FORMS

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CERTIFICATE_FORMS
select -- Представление для раздела : Бланки медицинских свидетельств
       t.ID,
       t.LPU,
       t.C_JOUR              C_JOUR_ID,
       t1.CJ_NAME            C_JOUR,
       t1.CJ_TYPE            C_JOUR_TYPE,
       t.C_KIND,
       t.C_SER,
       t.C_NUM,
       t.C_STATE,
       t.DATE_OUT,
       t.GIVEN_OUT_EMP,
       t.INSTEAD_CF,
       t.LOST_CF,
       t.DATE_LOST,
       t.SPINE,
       (select D_PKG_STR_TOOLS.FIO(t_e.SURNAME,t_e.FIRSTNAME,t_e.LASTNAME)
          from D_V_EMPLOYERS t_e where t_e.ID = t.GIVEN_OUT_EMP
       )                     GIVEN_OUT_EMP_FIO,
       t3.CS_NAME            C_STATE_NAME,
       D_PKG_CERTIFICATE_FORMS.GET_C_NUM(t.C_NUM)
                             C_NUM_CHAR,
       t.LPU_GIVER_HANDLE,
       t.LPU_GIVER,
       t.ANOTHER_LPU_CERT,
       t4.LPU_NAME           LPU_GIVER_NAME,
       t.INTERNAL_NUMBER,
       t.ACT_NUMB,
       t.ACT_DATE,
       t.FIO_EMP_ZAGS,
       t.NAME_ZAGS,
       t.BLOCKED,
       t.DATE_FIRST_FILL,
       t.CF_FORM352,
       t.CF_TYPE,
       case when t.CF_TYPE = 0 then 'Бумажное'
            when t.CF_TYPE = 1 then 'Электронное'
       end CF_TYPE_NAME,
       t.IS_ACTS,
       case when t.IS_ACTS = 1 then 'Да'
            else 'Нет'
       end IS_ACTS_MNEMO,
       t.PRINT_COUNT
  from D_CERTIFICATE_FORMS              t 	  -- Бланки медицинских свидетельств
       left join D_CERTIFICATE_JOURNALS t1 on t1.ID = t.C_JOUR -- Журналы бланков
       join D_CERTIFICATE_STATES        t3 on t3.CS_CODE = t.C_STATE
       left join D_LPUDICT              t4 on t4.ID = t.LPU_GIVER
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'CERTIFICATE_FORMS' and rownum = 1)
```

---

### Вьюха №4: D_V_CF_BIRTH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CF_BIRTH_CONTENTS_BASE
select -- Представление для раздела : Бланки медицинских свидетельств: Содержание свидетельств о рождении
       cbc.ID,
       cbc.LPU,
       cbc.PID,
       cbc.AGENT                    AGENT_ID,
       cbc.M_AGENT                  M_AGENT_ID,
       cbc.M_UNKNOWN_BIRTH,
       cbc.DIR_MARITAL_S            DIR_MARITAL_S_ID,
       cbc.DIR_EDUCATION            DIR_EDUCATION_ID,
       cbc.DIR_EMPLOYMENT           DIR_EMPLOYMENT_ID,
       cbc.FIRST_VISIT,
       cbc.CHILD_COUNT,
       cbc.DIR_CHILDBIRTH           DIR_CHILDBIRTH_ID,
       cbc.CHILD_WEIGHT,
       cbc.CHILD_LENGTH,
       cbc.POLY_ACCOUNT,
       cbc.POLY_ALL,
       cbc.DIR_PERSON_BIRTHS        DIR_PERSON_BIRTHS_ID,
       cbc.AGENT_TO                 AGENT_TO_ID,
       cbc.GIVEN_OUT_EMP,
       cbc.BIRTH_PLACE,
       cbc.BIRTH_PLACE_IS_CITY,
       cbc.PL_BIRTH_CITY,
       cbc.PL_BIRTH_HOUSE,
       cbc.PL_BIRTH_BUILDING,
       cbc.PL_BIRTH_KORPUS,
       cbc.PL_BIRTH_FLAT,
       cbc.SURNAME_HAND,
       cbc.NAME_HAND,
       cbc.LASTNAME_HAND,
       cbc.SER_DOC_RECEIVER,
       cbc.NUMBER_DOC_RECEIVER,
       cbc.DOC_WHO,
       cbc.RELATION_CHILD_OLD,
       cbc.DATE_RECEIVE,
       cbc.PRINT_CHILD_SURNAME,
       cbc.HEADER_EMP,
       cbc.DIR_PERSON_BIRTHS_EMPLOYER,
       cbc.RELATIVE,
       cbc.DATE_DOC_RECEIVER,
       cbc.SER_DOC_AUTH_RECEIVER,
       cbc.NUMBER_DOC_AUTH_RECEIVER,
       cbc.DOC_AUTH_WHO,
       cbc.DATE_DOC_AUTH_RECEIVER,
       cbc.PL_MOTHER_RAYON,
       cbc.PL_MOTHER_CITY,
       cbc.ACCURACY_DATE_MUNKN,
       cbc.CF_FORM352,
       cbc.SNILS_RECEIVER,
       cbc.DOCTYPE_RECEIVER,
       cbc.LPU_GIVER,
       cbc.WRITTEN_FROM_WORDS,
       cbc.RECEIVER_AGREEMENT,
       cbc.DIR_PB_OTHER_SURNAME,
       cbc.DIR_PB_OTHER_NAME,
       cbc.DIR_PB_OTHER_LASTNAME,
       cbc.HOSP_HISTORY,
       cbc.RELATION_CHILD,
       cbc.SERVICE_CHILDBIRTH,
       cbc.BIRTH_PLACE_ADDRESS
  from D_CF_BIRTH_CONTENTS cbc
 where exists (select null from D_V_URPRIVS ur where ur.LPU = cbc.LPU and ur.UNITCODE = 'CF_BIRTH_CONTENTS' and rownum = 1)
```

---

### Вьюха №5: D_V_CF_DEATH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CF_DEATH_CONTENTS_BASE
select -- Представление для раздела : Бланки медицинских свидетельств: Содержание свидетельства о смерти
       cdc.ID,
       cdc.LPU,
       cdc.PID,
       cdc.STATUS,
       cdc.AGENT                    AGENT_ID,
       cdc.RELATIVE,
       cdc.AGENT_TO                 AGENT_TO_ID,
       cdc.DATE_DEATH,
       case when cdc.ACCURACY_DATE_DEATH = 0 then
              to_char(cdc.DATE_DEATH,'DD.MM.YYYY hh24:mi')
            when cdc.ACCURACY_DATE_DEATH = 1 then
              to_char(cdc.DATE_DEATH,'DD.MM.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 2 then
              'XX' || to_char(cdc.DATE_DEATH,'.MM.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 3 then
              'XX.XX' || to_char(cdc.DATE_DEATH,'.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 4 then
              'XX.XX.XXXX --'
            else to_char(cdc.DATE_DEATH)
       end                          DATE_DEATH_STR,
       cdc.PLACE_DEATH              PLACE_DEATH_ID,
       cdc.PL_DEATH_CITY,
       cdc.PL_DEATH_STREET,
       cdc.PL_DEATH_HOUSE,
       cdc.PL_DEATH_FLAT,
       cdc.PL_DEATH_IS_CITY,
       cdc.PL_DEATH_BUILDING,
       cdc.PL_DEATH_KORPUS,
       cdc.DIR_PLACE                DIR_PLACE_ID,
       cdc.DIR_CHILD_BORN           DIR_CHILD_BORN_ID,
       cdc.CHILD_WEIGHT,
       cdc.CHILD_COUNT,
       cdc.CHILD_M_AGENT,
       cdc.DIR_MARITAL_S            DIR_MARITAL_S_ID,
       cdc.DIR_EDUCATION            DIR_EDUCATION_ID,
       cdc.DIR_EMPLOYMENT           DIR_EMPLOYMENT_ID,
       cdc.DIR_DEATH_FROM           DIR_DEATH_FROM_ID,
       cdc.DATE_ACCIDENT,
       cdc.ACCIDENT_FACT,
       cdc.DIR_REASON_SET           DIR_REASON_SET_ID,
       cdc.REASON_SET_EMPLOYER      REASON_SET_EMPLOYER_ID,
       cdc.DIR_BASIS_EST            DIR_BASIS_EST_ID,
       cdc.DIR_DTP                  DIR_DTP_ID,
       cdc.DIR_PREGNDEATH           DIR_PREGNDEATH_ID,
       cdc.GIVEN_OUT_EMP,
       cdc.SURNAME_HAND,
       cdc.NAME_HAND,
       cdc.LASTNAME_HAND,
       cdc.SER_DOC_RECEIVER,
       cdc.NUMBER_DOC_RECEIVER,
       cdc.DOC_WHO,
       cdc.DATE_DOC_RECEIVER,
       cdc.SNILS_HAND,
       cdc.DOC_WHEN,
       cdc.CHILD_M_SUR,
       cdc.CHILD_M_NAME,
       cdc.CHILD_M_LASTNAME,
       cdc.CHILD_M_BIRTH_DATE,
       cdc.CHILD_M_MARITAL_S        CHILD_M_MARITAL_S_ID,
       cdc.CHILD_M_EDUCATION        CHILD_M_EDUCATION_ID,
       cdc.CHILD_M_EMPLOYMENT       CHILD_M_EMPLOYMENT_ID,
       cdc.HEADER_EMP               HEADER_EMP_ID,
       cdc.ASSIGNED_PERSON          ASSIGNED_PERSON_ID,
       cdc.ASSIGNED_DATE,
       cdc.DIVISION_GIVER           DIVISION_GIVER_ID,
       cdc.UNKNOWN_PLACE_DEATH,
       cdc.INJURE_KIND,
       cdc.ACCURACY_DATE_DEATH,
       cdc.ACCURACY_DATE_ACCIDENT,
       cdc.REASON_SET_EMP_AN_LPU_HAND,
       cdc.GIVEN_OUT_EMP_AN_LPU_HAND,
       cdc.HEADER_LPU_AN_LPU_HAND,
       cdc.ASSIGNED_PERSON_AN_LPU_HAND,
       cdc.DIR_PLACE_LPU,
       cdc.LAW_RESULT,
       cdc.LAW_RESULT_NUM,
       cdc.CF_FORM352,
       cdc.LPU_GIVER,
       cdc.INSTEAD_CF,
       cdc.IS_CHILD_UNDER_ONE,
       cdc.RECEIVER_AGREEMENT,
       cdc.DOCTYPE_RECEIVER,
       cdc.PLACE_DEATH_ADDRESS
  from D_CF_DEATH_CONTENTS cdc
 where exists (select null from D_V_URPRIVS ur where ur.LPU = cdc.LPU and ur.UNITCODE = 'CF_DEATH_CONTENTS' and rownum = 1)
```

---

### Вьюха №6: D_V_CF_PERDEATH_CONTENTS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CF_PERDEATH_CONTENTS_BASE
select -- Представление для раздела : Бланки медицинских свидетельств: Содержание свидетельств перинатальной смерти
       cdc.ID,
       cdc.LPU,
       cdc.PID,
       cdc.STATUS,
       cdc.AGENT                    AGENT_ID,
       cdc.BORN_IS_LIVE,
       cdc.CERTIF_BIRTH,
       cdc.DATE_BIRTH,
       cdc.DATE_DEATH,
       case when cdc.ACCURACY_DATE_DEATH = 0 then
              to_char(cdc.DATE_DEATH,'DD.MM.YYYY hh24:mi')
            when cdc.ACCURACY_DATE_DEATH = 1 then
              to_char(cdc.DATE_DEATH,'DD.MM.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 2 then
              'XX' || to_char(cdc.DATE_DEATH,'.MM.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 3 then
              'XX.XX' || to_char(cdc.DATE_DEATH,'.YYYY')||' --'
            when cdc.ACCURACY_DATE_DEATH = 4 then
              'XX.XX.XXXX --'
            else to_char(cdc.DATE_DEATH)
       end                          DATE_DEATH_STR,
       cdc.DIR_DEATH_CAME           DIR_DEATH_CAME_ID,
       cdc.M_AGENT                  M_AGENT_ID,
       cdc.M_UNKNOWN_BIRTH,
       cdc.DIR_MARITAL_S            DIR_MARITAL_S_ID,
       cdc.DIR_EDUCATION            DIR_EDUCATION_ID,
       cdc.DIR_EMPLOYMENT           DIR_EMPLOYMENT_ID,
       cdc.BIRTH_COUNT,
       cdc.PLACE_DEATH              PLACE_DEATH_ID,
       cdc.PL_DEATH_CITY,
       cdc.PL_DEATH_IS_CITY,
       cdc.PL_DEATH_STREET,
       cdc.PL_DEATH_HOUSE,
       cdc.PL_DEATH_FLAT,
       cdc.PL_DEATH_BUILDING,
       cdc.PL_DEATH_KORPUS,
       cdc.DIR_PLACE                DIR_PLACE_ID,
       cdc.CHILD_WEIGHT,
       cdc.CHILD_LENGTH,
       cdc.CHILD_COUNT,
       cdc.POLY_ACCOUNT,
       cdc.POLY_ALL,
       cdc.DIR_DEATH_FROM           DIR_DEATH_FROM_ID,
       cdc.DIR_PERSON_BIRTHS        DIR_PERSON_BIRTHS_ID,
       cdc.DIR_REASON_SET           DIR_REASON_SET_ID,
       cdc.DIR_BASIS_EST            DIR_BASIS_EST_ID,
       cdc.AGENT_TO,
       cdc.GIVEN_OUT_EMP,
       cdc.SURNAME_HAND,
       cdc.NAME_HAND,
       cdc.LASTNAME_HAND,
       cdc.SER_DOC_RECEIVER,
       cdc.NUMBER_DOC_RECEIVER,
       cdc.DOC_WHO,
       cdc.RELATION_CHILD_OLD,
       cdc.DATE_RECEIVE,
       cdc.HEADER_EMP               HEADER_EMP_ID,
       cdc.ASSIGNED_PERSON          ASSIGNED_PERSON_ID,
       cdc.ASSIGNED_DATE,
       cdc.DIVISION_GIVER           DIVISION_GIVER_ID,
       cdc.PRINT_CHILD_SURNAME,
       cdc.ACCURACY_DATE_DEATH,
       cdc.DIR_PLACE_LPU,
       cdc.LAW_RESULT,
       cdc.LAW_RESULT_NUM,
       cdc.SNILS_HAND,
       cdc.CF_FORM352,
       cdc.LPU_GIVER,
       cdc.INSTEAD_CF,
       cdc.DOCTYPE_RECEIVER,
       cdc.RECEIVER_AGREEMENT,
       cdc.DIR_PERSON_BIRTHS_EMPLOYER,
       cdc.DIR_PB_OTHER_SURNAME,
       cdc.DIR_PB_OTHER_NAME,
       cdc.DIR_PB_OTHER_LASTNAME,
       cdc.HOSP_HISTORY,
       cdc.RELATION_CHILD,
       cdc.DATE_DOC_RECEIVER,
       cdc.SERVICE_CHILDBIRTH,
       cdc.PLACE_DEATH_ADDRESS
  from D_CF_PERDEATH_CONTENTS cdc
 where exists (select null from D_V_URPRIVS ur where ur.LPU = cdc.LPU and ur.UNITCODE = 'CF_PERDEATH_CONTENTS' and rownum = 1)
```

---

### Вьюха №7: D_V_PERSMEDCARD_FIO

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_PERSMEDCARD_FIO
select -- Персональные медицинские карты с ФИО
       p.ID,
       p.CID,
       p.LPU,
       p.AGENT,
       p.CARD_NUMB,
       ap.FIRSTNAME,
       ap.SURNAME,
       ap.LASTNAME,
       ap.BIRTHDATE,
       D_PKG_STR_TOOLS.FIO(ap.SURNAME, ap.FIRSTNAME, ap.LASTNAME) FIO,
       ap.SURNAME || ' ' || ap.FIRSTNAME || ' ' || ap.LASTNAME    FULL_FIO,
       case ap.SEX
            when 0 then 'Женский'
            when 1 then 'Мужской'
       end SEX,
       ap.SEX                                                     nSEX,
       ap.AGN_NAME                                                PAT_AGN_NAME,
       p.CREATEDATE,
       p.MODDATE,
       p.NOTE,
       case p.RHESUS
            when 0 then 'RH-'
            when 1 then 'RH+'
       end                                                        RHESUS,
       p.RHESUS                                                   nRHESUS,
       p.BLOODGROUPE,
       p.ECOLOR,
       p.PMC_TYPE,
       ap.SNILS,
       ap.DEATHDATE,
       ap.EDUCATION,
       p.EMP_ID,
       (select D_PKG_STR_TOOLS.FIO(ae.SURNAME, ae.FIRSTNAME, ae.LASTNAME)
          from D_AGENTS ae
         where ae.ID = e.AGENT) EMP_FIO,
       ap.ACCURACY_DATE_DEATH
  from D_PERSMEDCARD p                            -- Персональные медицинские карты
       join D_AGENTS ap on ap.ID = p.AGENT        -- Контрагенты
       left join D_EMPLOYERS e on e.ID = p.EMP_ID -- Персонал
 where exists (select null
                 from D_V_URPRIVS ur
                where ur.CATALOG = p.CID
                  and ur.UNITCODE = 'PERSMEDCARD'
                  and rownum = 1)
```

---

### Вьюха №8: D_V_CERTIFICATE_FORMS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_CERTIFICATE_FORMS_BASE
select -- Представление для раздела : Бланки медицинских свидетельств
       t.ID,
       t.LPU,
       t.C_JOUR              C_JOUR_ID,
       t.C_KIND,
       t.C_SER,
       t.C_NUM,
       t.C_STATE,
       t.DATE_OUT,
       t.GIVEN_OUT_EMP,
       t.INSTEAD_CF,
       t.LOST_CF,
       t.DATE_LOST,
       t.SPINE,
       t.LPU_GIVER_HANDLE,
       t.LPU_GIVER,
       t.ANOTHER_LPU_CERT,
       t.INTERNAL_NUMBER,
       t.ACT_NUMB,
       t.ACT_DATE,
       t.FIO_EMP_ZAGS,
       t.NAME_ZAGS,
       t.BLOCKED,
       t.DATE_FIRST_FILL,
       t.CF_FORM352,
       t.CF_TYPE,
       t.IS_ACTS,
       t.PRINT_COUNT
  from D_CERTIFICATE_FORMS t 	  -- Бланки медицинских свидетельств
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'CERTIFICATE_FORMS' and rownum = 1)
```

---

### Вьюха №9: D_V_EHRS_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_EHRS_BASE
select -- Представление для раздела : Архив медицинских документов
       t.ID,
       t.LPU,
       t.DOC_TYPE,
       t.UNIT,
       t.UNIT_ID,
       t.CREATED_BY,
       t.CREATE_DATE,
       t.REP_CODE,
       t.PATIENT,
       t.EX_SYSTEM,
       t.EDF_TYPE,
       t.DISEASECASE
  from D_EHRS t -- Архив медицинских документов
 where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'EHRS' and rownum = 1)
```

---

### Вьюха №10: D_V_EHR_STATES_BASE

**Используется в формах:**
- /Forms/Certificate/certificate_forms.frm

**DDL определение:**

```sql
-- Oracle View: D_V_EHR_STATES_BASE
select -- Представление для раздела : Архив медицинских документов : экземпляры
          t.ID,
          t. LPU,
          t. PID ,
          t.CREATED_BY,
          t.CREATE_DATE,
          t.UPDATED_BY,
          t.UPDATE_DATE,
          t.SGN_AUTHOR,
          t.SGN_TIMESTAMP,
          t.SGN_HASH,
          t.SGN,
          t.SGN_DOC,
          t.SGNREADABLE_HASH,
          t.SGNREADABLE,
          t.SGNREADABLE_DOC,
          t.IS_LOCKED,
          t.VERS,
          t.SGN_ZIP,
          t.LOCAL_UID,
          t.REPO_DOCUMENT_ID,
          t.EHR_LIFE_END,
          t.STATUS
  from D_EHR_STATES                    t
where exists (select null from D_V_URPRIVS ur where ur.LPU = t.LPU and ur.UNITCODE = 'EHR_STATES' and rownum = 1)
```


## 4.5. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ

Брокеры для анализа не найдены.


## 4. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 10
- Всего уникальных таблиц: 14

### Связь вьюх и таблиц

**D_V_CERTIFICATE_MOVING** использует таблицы:
- D_CERTIFICATE_MOVING
- D_CERTIFICATE_JOURNALS

**D_V_CERTIFICATE_MOVING_SP** использует таблицы:
- D_CERTIFICATE_MOVING_SP
- D_CERTIFICATE_FORMS

**D_V_CERTIFICATE_FORMS** использует таблицы:
- D_CERTIFICATE_FORMS
- D_CERTIFICATE_JOURNALS
- D_CERTIFICATE_STATES
- D_LPUDICT

**D_V_CF_BIRTH_CONTENTS_BASE** использует таблицы:
- D_CF_BIRTH_CONTENTS

**D_V_CF_DEATH_CONTENTS_BASE** использует таблицы:
- D_CF_DEATH_CONTENTS

**D_V_CF_PERDEATH_CONTENTS_BASE** использует таблицы:
- D_CF_PERDEATH_CONTENTS

**D_V_PERSMEDCARD_FIO** использует таблицы:
- D_AGENTS
- D_PERSMEDCARD
- D_EMPLOYERS

**D_V_CERTIFICATE_FORMS_BASE** использует таблицы:
- D_CERTIFICATE_FORMS

**D_V_EHRS_BASE** использует таблицы:
- D_EHRS

**D_V_EHR_STATES_BASE** использует таблицы:
- D_EHR_STATES

### DDL определения таблиц

---

#### Таблица №1: D_CERTIFICATE_MOVING

```sql
CREATE TABLE D_CERTIFICATE_MOVING (
    id bigint,
    lpu bigint,
    cj_from bigint,
    cj_to bigint,
    moving_type numeric(1) DEFAULT 0,
    moving_date timestamp without time zone,
    employer bigint,
    employer_to bigint,
    range_begin numeric(10),
    range_end numeric(10),
    is_actual numeric(1) DEFAULT 0
);
```

---

#### Таблица №2: D_CERTIFICATE_JOURNALS

```sql
CREATE TABLE D_CERTIFICATE_JOURNALS (
    id bigint,
    lpu bigint,
    cid bigint,
    cj_name character varying(150),
    cj_type numeric(1),
    cj_kind numeric(1),
    cj_form352 numeric(1) DEFAULT 0,
    cf_type numeric(1) DEFAULT 0
);
```

---

#### Таблица №3: D_CERTIFICATE_MOVING_SP

```sql
CREATE TABLE D_CERTIFICATE_MOVING_SP (
    id bigint,
    lpu bigint,
    pid bigint,
    certificate bigint
);
```

---

#### Таблица №4: D_CERTIFICATE_FORMS

```sql
CREATE TABLE D_CERTIFICATE_FORMS (
    id bigint,
    lpu bigint,
    c_jour bigint,
    c_kind numeric(1),
    c_ser character varying(10),
    c_num numeric(10),
    c_state numeric(1),
    date_out timestamp without time zone,
    given_out_emp bigint,
    instead_cf bigint,
    lost_cf bigint,
    date_lost timestamp without time zone,
    spine numeric(1) DEFAULT 0,
    lpu_giver bigint,
    lpu_giver_handle character varying(400),
    another_lpu_cert numeric(1) DEFAULT 0,
    internal_number character varying(10),
    act_numb character varying(10),
    act_date timestamp without time zone,
    fio_emp_zags character varying(70),
    name_zags character varying(250),
    blocked numeric(1) DEFAULT 0,
    date_first_fill timestamp without time zone,
    cf_form352 numeric(1) DEFAULT 0,
    cf_type numeric(1) DEFAULT 0,
    is_acts numeric(1) DEFAULT 0,
    print_count numeric(5) DEFAULT 0
);
```

---

#### Таблица №5: D_CERTIFICATE_STATES

```sql
CREATE TABLE D_CERTIFICATE_STATES (
    cs_code numeric(1),
    cs_name character varying(15)
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

#### Таблица №7: D_CF_BIRTH_CONTENTS

```sql
CREATE TABLE D_CF_BIRTH_CONTENTS (
    id bigint,
    lpu bigint,
    pid bigint,
    agent bigint,
    m_agent bigint,
    dir_marital_s bigint,
    dir_education bigint,
    dir_employment bigint,
    first_visit numeric(2),
    child_count numeric(2),
    dir_childbirth bigint,
    child_weight numeric(5),
    child_length numeric(2),
    poly_account numeric(1) DEFAULT 0,
    poly_all numeric(1) DEFAULT 0,
    dir_person_births bigint,
    agent_to bigint,
    given_out_emp bigint,
    birth_place bigint,
    birth_place_is_city numeric(1),
    pl_birth_city character varying(30),
    surname_hand character varying(40),
    name_hand character varying(40),
    lastname_hand character varying(40),
    ser_doc_receiver character varying(10),
    number_doc_receiver character varying(20),
    doc_who character varying(250),
    date_receive timestamp without time zone,
    print_child_surname numeric(1) DEFAULT 1,
    header_emp bigint,
    dir_person_births_employer bigint,
    relative numeric(1),
    date_doc_receiver timestamp without time zone,
    ser_doc_auth_receiver character varying(10),
    number_doc_auth_receiver character varying(20),
    doc_auth_who character varying(250),
    date_doc_auth_receiver timestamp without time zone,
    pl_mother_rayon character varying(30),
    pl_mother_city character varying(30),
    accuracy_date_munkn numeric(1),
    m_unknown_birth timestamp without time zone,
    cf_form352 numeric(1) DEFAULT 0,
    snils_receiver character varying(11),
    doctype_receiver bigint,
    lpu_giver bigint,
    pl_birth_house character varying(10),
    pl_birth_building character varying(10),
    pl_birth_korpus character varying(10),
    pl_birth_flat character varying(7),
    written_from_words numeric(1) DEFAULT 0,
    receiver_agreement numeric(1) DEFAULT 0,
    dir_pb_other_surname character varying(40),
    dir_pb_other_name character varying(40),
    dir_pb_other_lastname character varying(40),
    hosp_history bigint,
    relation_child_old character varying(20),
    relation_child bigint,
    service_childbirth bigint,
    birth_place_address bigint
);
```

---

#### Таблица №8: D_CF_DEATH_CONTENTS

```sql
CREATE TABLE D_CF_DEATH_CONTENTS (
    id bigint,
    lpu bigint,
    pid bigint,
    status numeric(1),
    agent bigint,
    agent_to bigint,
    date_death timestamp without time zone,
    place_death bigint,
    pl_death_city character varying(40),
    pl_death_street character varying(40),
    pl_death_house character varying(10),
    pl_death_flat character varying(7),
    pl_death_is_city numeric(1),
    dir_place bigint,
    dir_child_born bigint,
    child_weight numeric(5),
    child_count numeric(2),
    child_m_agent bigint,
    dir_marital_s bigint,
    dir_education bigint,
    dir_employment bigint,
    dir_death_from bigint,
    date_accident timestamp without time zone,
    accident_fact character varying(400),
    dir_reason_set bigint,
    dir_basis_est bigint,
    dir_dtp bigint,
    dir_pregndeath bigint,
    given_out_emp bigint,
    surname_hand character varying(40),
    name_hand character varying(40),
    lastname_hand character varying(40),
    ser_doc_receiver character varying(10),
    number_doc_receiver character varying(20),
    doc_who character varying(250),
    doc_when timestamp without time zone,
    child_m_sur character varying(40),
    child_m_name character varying(40),
    child_m_lastname character varying(40),
    child_m_birth_date timestamp without time zone,
    child_m_marital_s bigint,
    child_m_education bigint,
    child_m_employment bigint,
    relative numeric(1),
    reason_set_employer bigint,
    date_doc_receiver timestamp without time zone,
    snils_hand character varying(11),
    header_emp bigint,
    assigned_person bigint,
    assigned_date timestamp without time zone,
    division_giver bigint,
    unknown_place_death character varying(400),
    injure_kind bigint,
    accuracy_date_death numeric(1) DEFAULT 0,
    accuracy_date_accident numeric(1),
    doctor_check bigint,
    header_lpu bigint,
    reason_set_emp_an_lpu_hand character varying(150),
    given_out_emp_an_lpu_hand character varying(150),
    header_lpu_an_lpu_hand character varying(150),
    assigned_person_an_lpu_hand character varying(150),
    dir_place_lpu bigint,
    law_result numeric(1) DEFAULT 0,
    law_result_num character varying(15),
    cf_form352 numeric(1) DEFAULT 0,
    lpu_giver bigint,
    instead_cf bigint,
    pl_death_korpus character varying(10),
    pl_death_building character varying(10),
    is_child_under_one numeric(1) DEFAULT 0,
    receiver_agreement numeric(1) DEFAULT 0,
    doctype_receiver bigint,
    place_death_address bigint
);
```

---

#### Таблица №9: D_CF_PERDEATH_CONTENTS

```sql
CREATE TABLE D_CF_PERDEATH_CONTENTS (
    id bigint,
    lpu bigint,
    pid bigint,
    status numeric(1),
    agent bigint,
    born_is_live numeric(1),
    certif_birth bigint,
    date_birth timestamp without time zone,
    date_death timestamp without time zone,
    dir_death_came bigint,
    m_agent bigint,
    m_unknown_birth character varying(10),
    dir_marital_s bigint,
    dir_education bigint,
    dir_employment bigint,
    birth_count numeric(2),
    place_death bigint,
    pl_death_city character varying(30),
    pl_death_is_city numeric(1) DEFAULT 0,
    dir_place bigint,
    child_weight numeric(5),
    child_length numeric(2),
    child_count numeric(2),
    poly_account numeric(1) DEFAULT 0,
    poly_all numeric(1) DEFAULT 0,
    dir_death_from bigint,
    dir_person_births bigint,
    dir_reason_set bigint,
    dir_basis_est bigint,
    agent_to bigint,
    given_out_emp bigint,
    surname_hand character varying(40),
    name_hand character varying(40),
    lastname_hand character varying(40),
    ser_doc_receiver character varying(10),
    number_doc_receiver character varying(20),
    doc_who character varying(250),
    relation_child_old character varying(20),
    date_receive timestamp without time zone,
    header_emp bigint,
    assigned_person bigint,
    assigned_date timestamp without time zone,
    division_giver bigint,
    print_child_surname numeric(1) DEFAULT 1,
    tmp_act_numb numeric(7),
    tmp_act_date timestamp without time zone,
    tmp_name_zags character varying(250),
    tmp_fio_emp_zags character varying(70),
    accuracy_date_death numeric(1) DEFAULT 0,
    doctor_check bigint,
    header_lpu bigint,
    dir_place_lpu bigint,
    law_result numeric(1) DEFAULT 0,
    law_result_num character varying(15),
    pl_death_street character varying(40),
    pl_death_house character varying(10),
    pl_death_flat character varying(7),
    snils_hand character varying(11),
    cf_form352 numeric(1) DEFAULT 0,
    lpu_giver bigint,
    instead_cf bigint,
    pl_death_korpus character varying(10),
    pl_death_building character varying(10),
    doctype_receiver bigint,
    receiver_agreement numeric(1) DEFAULT 0,
    dir_person_births_employer bigint,
    dir_pb_other_surname character varying(40),
    dir_pb_other_name character varying(40),
    dir_pb_other_lastname character varying(40),
    hosp_history bigint,
    relation_child bigint,
    date_doc_receiver timestamp without time zone,
    service_childbirth bigint,
    place_death_address bigint
);
```

---

#### Таблица №10: D_AGENTS

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

#### Таблица №11: D_PERSMEDCARD

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

#### Таблица №12: D_EMPLOYERS

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

#### Таблица №13: D_EHRS

```sql
CREATE TABLE D_EHRS (
    id bigint,
    lpu bigint,
    doc_type bigint,
    unit character varying(30),
    unit_id bigint,
    created_by bigint,
    create_date timestamp without time zone,
    rep_code character varying(40),
    patient bigint,
    ex_system bigint,
    edf_type bigint,
    diseasecase bigint,
    rep_name character varying(160)
);
```

---

#### Таблица №14: D_EHR_STATES

```sql
CREATE TABLE D_EHR_STATES (
    id bigint,
    lpu bigint,
    pid bigint,
    created_by bigint,
    create_date timestamp without time zone,
    updated_by bigint,
    update_date timestamp without time zone,
    sgn_author character varying(1000),
    sgn_timestamp timestamp without time zone,
    sgn_hash character varying(256),
    sgnreadable_hash character varying(256),
    is_locked numeric(1) DEFAULT 0,
    vers bigint DEFAULT 1,
    sgn bigint,
    sgn_doc bigint,
    sgnreadable bigint,
    sgnreadable_doc bigint,
    sgn_zip bigint,
    nci_doc_type bigint,
    local_uid character varying(50),
    status numeric(3),
    repo_document_id character varying(44),
    ehr_life_end timestamp without time zone
);
```


## 5. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ

Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, а также их DDL определения.

**Статистика:**
- Всего вьюх с таблицами: 10
- Всего уникальных таблиц: 14

### Связь вьюх и таблиц

**D_V_CERTIFICATE_MOVING** использует таблицы:
- D_CERTIFICATE_MOVING

**D_V_CERTIFICATE_MOVING_SP** использует таблицы:
- D_CERTIFICATE_MOVING_SP

**D_V_CERTIFICATE_FORMS** использует таблицы:
- D_CERTIFICATE_FORMS
- D_CERTIFICATE_JOURNALS
- D_CERTIFICATE_STATES
- D_LPUDICT

**D_V_CF_BIRTH_CONTENTS_BASE** использует таблицы:
- D_CF_BIRTH_CONTENTS

**D_V_CF_DEATH_CONTENTS_BASE** использует таблицы:
- D_CF_DEATH_CONTENTS

**D_V_CF_PERDEATH_CONTENTS_BASE** использует таблицы:
- D_CF_PERDEATH_CONTENTS

**D_V_PERSMEDCARD_FIO** использует таблицы:
- D_AGENTS
- D_PERSMEDCARD
- D_EMPLOYERS

**D_V_CERTIFICATE_FORMS_BASE** использует таблицы:
- D_CERTIFICATE_FORMS

**D_V_EHRS_BASE** использует таблицы:
- D_EHRS

**D_V_EHR_STATES_BASE** использует таблицы:
- D_EHR_STATES

### DDL определения таблиц

---

#### Таблица №1: D_CERTIFICATE_MOVING

```sql
CREATE TABLE D_CERTIFICATE_MOVING (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CJ_FROM NUMBER(17),
    CJ_TO NUMBER(17) NOT NULL,
    MOVING_TYPE NUMBER(1) NOT NULL,
    MOVING_DATE DATE NOT NULL,
    EMPLOYER NUMBER(17) NOT NULL,
    EMPLOYER_TO NUMBER(17),
    RANGE_BEGIN NUMBER(10) NOT NULL,
    RANGE_END NUMBER(10) NOT NULL,
    IS_ACTUAL NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_CERTIFICATE_MOVING PRIMARY KEY (ID)
);
```

---

#### Таблица №2: D_CERTIFICATE_MOVING_SP

```sql
CREATE TABLE D_CERTIFICATE_MOVING_SP (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CERTIFICATE NUMBER(17) NOT NULL,
    CONSTRAINT PK_D_CERTIFICATE_MOVING_SP PRIMARY KEY (ID)
);
```

---

#### Таблица №3: D_CERTIFICATE_FORMS

```sql
CREATE TABLE D_CERTIFICATE_FORMS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    C_JOUR NUMBER(17),
    C_KIND NUMBER(1) NOT NULL,
    C_SER VARCHAR2(10),
    C_NUM NUMBER(10),
    C_STATE NUMBER(1) NOT NULL,
    DATE_OUT DATE,
    GIVEN_OUT_EMP NUMBER(17),
    INSTEAD_CF NUMBER(17),
    LOST_CF NUMBER(17),
    DATE_LOST DATE,
    SPINE NUMBER(1) NOT NULL,
    LPU_GIVER NUMBER(17),
    LPU_GIVER_HANDLE VARCHAR2(400),
    ANOTHER_LPU_CERT NUMBER(1) NOT NULL,
    INTERNAL_NUMBER VARCHAR2(10),
    ACT_NUMB VARCHAR2(10),
    ACT_DATE DATE,
    FIO_EMP_ZAGS VARCHAR2(70),
    NAME_ZAGS VARCHAR2(250),
    BLOCKED NUMBER(1) NOT NULL,
    DATE_FIRST_FILL DATE,
    CF_FORM352 NUMBER(1) NOT NULL,
    CF_TYPE NUMBER(1) NOT NULL,
    IS_ACTS NUMBER(1) NOT NULL,
    PRINT_COUNT NUMBER(5) NOT NULL,
    CONSTRAINT PK_D_CERTIFICATE_FORMS PRIMARY KEY (ID)
);
```

---

#### Таблица №4: D_CERTIFICATE_JOURNALS

```sql
CREATE TABLE D_CERTIFICATE_JOURNALS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    CID NUMBER(17) NOT NULL,
    CJ_NAME VARCHAR2(150) NOT NULL,
    CJ_TYPE NUMBER(1) NOT NULL,
    CJ_KIND NUMBER(1) NOT NULL,
    CJ_FORM352 NUMBER(1) NOT NULL,
    CF_TYPE NUMBER(1) NOT NULL,
    CONSTRAINT PK_D_CERTIFICATE_JOURNALS PRIMARY KEY (ID)
);
```

---

#### Таблица №5: D_CERTIFICATE_STATES

```sql
CREATE TABLE D_CERTIFICATE_STATES (
    CS_CODE NUMBER(1) NOT NULL,
    CS_NAME VARCHAR2(15) NOT NULL,
    CONSTRAINT PK_D_CERTIFICATE_STATES PRIMARY KEY (CS_CODE)
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

#### Таблица №7: D_CF_BIRTH_CONTENTS

```sql
CREATE TABLE D_CF_BIRTH_CONTENTS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17),
    AGENT NUMBER(17) NOT NULL,
    M_AGENT NUMBER(17),
    DIR_MARITAL_S NUMBER(17) NOT NULL,
    DIR_EDUCATION NUMBER(17) NOT NULL,
    DIR_EMPLOYMENT NUMBER(17) NOT NULL,
    FIRST_VISIT NUMBER(2),
    CHILD_COUNT NUMBER(2),
    DIR_CHILDBIRTH NUMBER(17) NOT NULL,
    CHILD_WEIGHT NUMBER(5),
    CHILD_LENGTH NUMBER(2),
    POLY_ACCOUNT NUMBER(1) NOT NULL,
    POLY_ALL NUMBER(1) NOT NULL,
    DIR_PERSON_BIRTHS NUMBER(17) NOT NULL,
    AGENT_TO NUMBER(17),
    GIVEN_OUT_EMP NUMBER(17) NOT NULL,
    BIRTH_PLACE NUMBER(17),
    BIRTH_PLACE_IS_CITY NUMBER(1) NOT NULL,
    PL_BIRTH_CITY VARCHAR2(30),
    SURNAME_HAND VARCHAR2(40),
    NAME_HAND VARCHAR2(40),
    LASTNAME_HAND VARCHAR2(40),
    SER_DOC_RECEIVER VARCHAR2(10),
    NUMBER_DOC_RECEIVER VARCHAR2(20),
    DOC_WHO VARCHAR2(250),
    DATE_RECEIVE DATE,
    PRINT_CHILD_SURNAME NUMBER(1) NOT NULL,
    HEADER_EMP NUMBER(17),
    DIR_PERSON_BIRTHS_EMPLOYER NUMBER(17),
    RELATIVE NUMBER(1),
    DATE_DOC_RECEIVER DATE,
    SER_DOC_AUTH_RECEIVER VARCHAR2(10),
    NUMBER_DOC_AUTH_RECEIVER VARCHAR2(20),
    DOC_AUTH_WHO VARCHAR2(250),
    DATE_DOC_AUTH_RECEIVER DATE,
    PL_MOTHER_RAYON VARCHAR2(30),
    PL_MOTHER_CITY VARCHAR2(30),
    ACCURACY_DATE_MUNKN NUMBER(1),
    M_UNKNOWN_BIRTH DATE,
    CF_FORM352 NUMBER(1) NOT NULL,
    SNILS_RECEIVER VARCHAR2(11),
    DOCTYPE_RECEIVER NUMBER(17),
    LPU_GIVER NUMBER(17),
    PL_BIRTH_HOUSE VARCHAR2(10),
    PL_BIRTH_BUILDING VARCHAR2(10),
    PL_BIRTH_KORPUS VARCHAR2(10),
    PL_BIRTH_FLAT VARCHAR2(7),
    WRITTEN_FROM_WORDS NUMBER(1) NOT NULL,
    RECEIVER_AGREEMENT NUMBER(1) NOT NULL,
    DIR_PB_OTHER_SURNAME VARCHAR2(40),
    DIR_PB_OTHER_NAME VARCHAR2(40),
    DIR_PB_OTHER_LASTNAME VARCHAR2(40),
    HOSP_HISTORY NUMBER(17),
    RELATION_CHILD_OLD VARCHAR2(20),
    RELATION_CHILD NUMBER(17),
    SERVICE_CHILDBIRTH NUMBER(17),
    BIRTH_PLACE_ADDRESS NUMBER(17),
    CONSTRAINT PK_D_CF_BIRTH_CONTENTS PRIMARY KEY (ID)
);
```

---

#### Таблица №8: D_CF_DEATH_CONTENTS

```sql
CREATE TABLE D_CF_DEATH_CONTENTS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17),
    STATUS NUMBER(1) NOT NULL,
    AGENT NUMBER(17) NOT NULL,
    AGENT_TO NUMBER(17),
    DATE_DEATH DATE NOT NULL,
    PLACE_DEATH NUMBER(17),
    PL_DEATH_CITY VARCHAR2(40),
    PL_DEATH_STREET VARCHAR2(40),
    PL_DEATH_HOUSE VARCHAR2(10),
    PL_DEATH_FLAT VARCHAR2(7),
    PL_DEATH_IS_CITY NUMBER(1) NOT NULL,
    DIR_PLACE NUMBER(17) NOT NULL,
    DIR_CHILD_BORN NUMBER(17),
    CHILD_WEIGHT NUMBER(5),
    CHILD_COUNT NUMBER(2),
    CHILD_M_AGENT NUMBER(17),
    DIR_MARITAL_S NUMBER(17),
    DIR_EDUCATION NUMBER(17),
    DIR_EMPLOYMENT NUMBER(17),
    DIR_DEATH_FROM NUMBER(17) NOT NULL,
    DATE_ACCIDENT DATE,
    ACCIDENT_FACT VARCHAR2(400),
    DIR_REASON_SET NUMBER(17) NOT NULL,
    DIR_BASIS_EST NUMBER(17) NOT NULL,
    DIR_DTP NUMBER(17),
    DIR_PREGNDEATH NUMBER(17),
    GIVEN_OUT_EMP NUMBER(17),
    SURNAME_HAND VARCHAR2(40),
    NAME_HAND VARCHAR2(40),
    LASTNAME_HAND VARCHAR2(40),
    SER_DOC_RECEIVER VARCHAR2(10),
    NUMBER_DOC_RECEIVER VARCHAR2(20),
    DOC_WHO VARCHAR2(250),
    DOC_WHEN DATE,
    CHILD_M_SUR VARCHAR2(40),
    CHILD_M_NAME VARCHAR2(40),
    CHILD_M_LASTNAME VARCHAR2(40),
    CHILD_M_BIRTH_DATE DATE,
    CHILD_M_MARITAL_S NUMBER(17),
    CHILD_M_EDUCATION NUMBER(17),
    CHILD_M_EMPLOYMENT NUMBER(17),
    RELATIVE NUMBER(1),
    REASON_SET_EMPLOYER NUMBER(17),
    DATE_DOC_RECEIVER DATE,
    SNILS_HAND VARCHAR2(11),
    HEADER_EMP NUMBER(17),
    ASSIGNED_PERSON NUMBER(17),
    ASSIGNED_DATE DATE,
    DIVISION_GIVER NUMBER(17),
    UNKNOWN_PLACE_DEATH VARCHAR2(400),
    INJURE_KIND NUMBER(17),
    ACCURACY_DATE_DEATH NUMBER(1) NOT NULL,
    ACCURACY_DATE_ACCIDENT NUMBER(1),
    DOCTOR_CHECK NUMBER(17),
    HEADER_LPU NUMBER(17),
    REASON_SET_EMP_AN_LPU_HAND VARCHAR2(150),
    GIVEN_OUT_EMP_AN_LPU_HAND VARCHAR2(150),
    HEADER_LPU_AN_LPU_HAND VARCHAR2(150),
    ASSIGNED_PERSON_AN_LPU_HAND VARCHAR2(150),
    DIR_PLACE_LPU NUMBER(17),
    LAW_RESULT NUMBER(1) NOT NULL,
    LAW_RESULT_NUM VARCHAR2(15),
    CF_FORM352 NUMBER(1) NOT NULL,
    LPU_GIVER NUMBER(17),
    INSTEAD_CF NUMBER(17),
    PL_DEATH_KORPUS VARCHAR2(10),
    PL_DEATH_BUILDING VARCHAR2(10),
    IS_CHILD_UNDER_ONE NUMBER(1) NOT NULL,
    RECEIVER_AGREEMENT NUMBER(1) NOT NULL,
    DOCTYPE_RECEIVER NUMBER(17),
    PLACE_DEATH_ADDRESS NUMBER(17),
    CONSTRAINT PK_D_CF_DEATH_CONTENTS PRIMARY KEY (ID)
);
```

---

#### Таблица №9: D_CF_PERDEATH_CONTENTS

```sql
CREATE TABLE D_CF_PERDEATH_CONTENTS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17),
    STATUS NUMBER(1) NOT NULL,
    AGENT NUMBER(17) NOT NULL,
    BORN_IS_LIVE NUMBER(1) NOT NULL,
    CERTIF_BIRTH NUMBER(17),
    DATE_BIRTH DATE,
    DATE_DEATH DATE NOT NULL,
    DIR_DEATH_CAME NUMBER(17) NOT NULL,
    M_AGENT NUMBER(17),
    M_UNKNOWN_BIRTH VARCHAR2(10),
    DIR_MARITAL_S NUMBER(17) NOT NULL,
    DIR_EDUCATION NUMBER(17) NOT NULL,
    DIR_EMPLOYMENT NUMBER(17) NOT NULL,
    BIRTH_COUNT NUMBER(2),
    PLACE_DEATH NUMBER(17),
    PL_DEATH_CITY VARCHAR2(30),
    PL_DEATH_IS_CITY NUMBER(1) NOT NULL,
    DIR_PLACE NUMBER(17) NOT NULL,
    CHILD_WEIGHT NUMBER(5) NOT NULL,
    CHILD_LENGTH NUMBER(2) NOT NULL,
    CHILD_COUNT NUMBER(2),
    POLY_ACCOUNT NUMBER(1) NOT NULL,
    POLY_ALL NUMBER(1) NOT NULL,
    DIR_DEATH_FROM NUMBER(17) NOT NULL,
    DIR_PERSON_BIRTHS NUMBER(17) NOT NULL,
    DIR_REASON_SET NUMBER(17) NOT NULL,
    DIR_BASIS_EST NUMBER(17) NOT NULL,
    AGENT_TO NUMBER(17),
    GIVEN_OUT_EMP NUMBER(17) NOT NULL,
    SURNAME_HAND VARCHAR2(40),
    NAME_HAND VARCHAR2(40),
    LASTNAME_HAND VARCHAR2(40),
    SER_DOC_RECEIVER VARCHAR2(10),
    NUMBER_DOC_RECEIVER VARCHAR2(20),
    DOC_WHO VARCHAR2(250),
    RELATION_CHILD_OLD VARCHAR2(20),
    DATE_RECEIVE DATE,
    HEADER_EMP NUMBER(17),
    ASSIGNED_PERSON NUMBER(17),
    ASSIGNED_DATE DATE,
    DIVISION_GIVER NUMBER(17),
    PRINT_CHILD_SURNAME NUMBER(1) NOT NULL,
    TMP_ACT_NUMB NUMBER(7),
    TMP_ACT_DATE DATE,
    TMP_NAME_ZAGS VARCHAR2(250),
    TMP_FIO_EMP_ZAGS VARCHAR2(70),
    ACCURACY_DATE_DEATH NUMBER(1) NOT NULL,
    DOCTOR_CHECK NUMBER(17),
    HEADER_LPU NUMBER(17),
    DIR_PLACE_LPU NUMBER(17),
    LAW_RESULT NUMBER(1) NOT NULL,
    LAW_RESULT_NUM VARCHAR2(15),
    PL_DEATH_STREET VARCHAR2(40),
    PL_DEATH_HOUSE VARCHAR2(10),
    PL_DEATH_FLAT VARCHAR2(7),
    SNILS_HAND VARCHAR2(11),
    CF_FORM352 NUMBER(1) NOT NULL,
    LPU_GIVER NUMBER(17),
    INSTEAD_CF NUMBER(17),
    PL_DEATH_KORPUS VARCHAR2(10),
    PL_DEATH_BUILDING VARCHAR2(10),
    DOCTYPE_RECEIVER NUMBER(17),
    RECEIVER_AGREEMENT NUMBER(1) NOT NULL,
    DIR_PERSON_BIRTHS_EMPLOYER NUMBER(17),
    DIR_PB_OTHER_SURNAME VARCHAR2(40),
    DIR_PB_OTHER_NAME VARCHAR2(40),
    DIR_PB_OTHER_LASTNAME VARCHAR2(40),
    HOSP_HISTORY NUMBER(17),
    RELATION_CHILD NUMBER(17),
    DATE_DOC_RECEIVER DATE,
    SERVICE_CHILDBIRTH NUMBER(17),
    PLACE_DEATH_ADDRESS NUMBER(17),
    CONSTRAINT PK_D_CF_PERDEATH_CONTENTS PRIMARY KEY (ID)
);
```

---

#### Таблица №10: D_AGENTS

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

#### Таблица №11: D_PERSMEDCARD

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

#### Таблица №12: D_EMPLOYERS

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

#### Таблица №13: D_EHRS

```sql
CREATE TABLE D_EHRS (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    DOC_TYPE NUMBER(17) NOT NULL,
    UNIT VARCHAR2(30) NOT NULL,
    UNIT_ID NUMBER(17) NOT NULL,
    CREATED_BY NUMBER(17) NOT NULL,
    CREATE_DATE DATE NOT NULL,
    REP_CODE VARCHAR2(40),
    PATIENT NUMBER(17),
    EX_SYSTEM NUMBER(17),
    EDF_TYPE NUMBER(17),
    DISEASECASE NUMBER(17),
    REP_NAME VARCHAR2(160),
    CONSTRAINT PK_D_EHRS PRIMARY KEY (ID)
);
```

---

#### Таблица №14: D_EHR_STATES

```sql
CREATE TABLE D_EHR_STATES (
    ID NUMBER(17) NOT NULL,
    LPU NUMBER(17) NOT NULL,
    PID NUMBER(17) NOT NULL,
    CREATED_BY NUMBER(17) NOT NULL,
    CREATE_DATE DATE NOT NULL,
    UPDATED_BY NUMBER(17),
    UPDATE_DATE DATE,
    SGN_AUTHOR VARCHAR2(1000),
    SGN_TIMESTAMP DATE,
    SGN_HASH VARCHAR2(256),
    SGNREADABLE_HASH VARCHAR2(256),
    IS_LOCKED NUMBER(1) NOT NULL,
    VERS NUMBER(17) NOT NULL,
    SGN NUMBER(17),
    SGN_DOC NUMBER(17),
    SGNREADABLE NUMBER(17),
    SGNREADABLE_DOC NUMBER(17),
    SGN_ZIP NUMBER(17),
    NCI_DOC_TYPE NUMBER(17),
    LOCAL_UID VARCHAR2(50),
    STATUS NUMBER(3),
    REPO_DOCUMENT_ID VARCHAR2(44),
    EHR_LIFE_END DATE,
    CONSTRAINT PK_D_EHR_STATES PRIMARY KEY (ID)
);
```


## 6. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠

Ниже представлены тела функций из Oracle пакетов, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных пакетных функций: 2
- Загружено тел функций: 2

---

### Функция №1: D_PKG_CERTIFICATE_FORMS.SET_STATE

```sql
-- Oracle PACKAGE: SET_STATE
--======================================================================
procedure SET_STATE
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER,
  pnC_STATE                            in NUMBER,           -- Состояние бланка
  pnGIVEN_OUT_EMP                      in NUMBER            -- Выдавший сотрудник
)
is
  rC_FORM                    D_CERTIFICATE_FORMS%rowtype;
begin
  if pnC_STATE = 0 then
    SET_LOSTED(pnID,pnLPU,null,0);                          -- Пометить "В наличии"
  elsif pnC_STATE = 1 then
    SET_LOSTED(pnID,pnLPU,trunc(sysdate));
  elsif pnC_STATE = 4 then
    SET_GIVED(pnID,pnLPU,trunc(sysdate),pnGIVEN_OUT_EMP);
  else
    begin
      select t.*
        into rC_FORM
        from D_CERTIFICATE_FORMS t
       where t.ID  = pnID
         and t.LPU = pnLPU;
    exception when NO_DATA_FOUND then
      D_PKG_MSG.RECORD_NOT_FOUND(pnID,'CERTIFICATE_FORMS');
    end;
```

---

### Функция №2: D_PKG_CERTIFICATE_FORMS.CLEAR

```sql
-- Oracle PACKAGE: CLEAR
--======================================================================
procedure CLEAR
(
  pnID                                 in NUMBER,
  pnLPU                                in NUMBER
)
is
  sMESSAGE D_PKG_STD.tSTR; -- Сообщение об открытых картах ДУ    
begin
  CLEAR(pnID, pnLPU, sMESSAGE);
end;
```


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 2
- Загружено тел функций: 2

---

### Функция №1: d_pkg_certificate_forms.set_state

```sql
CREATE OR REPLACE PROCEDURE d_pkg_certificate_forms.set_state(IN pnid numeric, IN pnlpu numeric, IN pnc_state numeric, IN pngiven_out_emp numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    rC_FORM d_certificate_forms;
BEGIN
    IF pnc_state = 0 THEN
        CALL d_pkg_certificate_forms.set_losted(pnid, pnlpu, (null)::timestamp, 0)   /*  Пометить "В наличии" */;

    ELSIF pnc_state = 1 THEN
        CALL d_pkg_certificate_forms.set_losted(pnid, pnlpu, trunc(sysdate())::timestamp);

    ELSIF pnc_state = 4 THEN
        CALL d_pkg_certificate_forms.set_gived(pnid, pnlpu, trunc(sysdate())::timestamp, pngiven_out_emp);

    ELSE
        SELECT
            t.*
        INTO rc_form
        FROM
            d_certificate_forms t
        WHERE
            t.id = pnid::bigint
                 AND t.lpu = pnlpu::bigint;
        IF NOT FOUND THEN
            PERFORM d_pkg_msg.record_not_found(1,pnid,'CERTIFICATE_FORMS');

        END IF;
        --  Если похищен в ЛПУ то нужно заполнить дату похищения
                IF pnc_state = 2
     OR pnc_state = 3 THEN
            rc_form.date_lost := trunc(sysdate());

        END IF;
        CALL d_pkg_certificate_forms.upd(pnID => (rc_form.id)::numeric, pnLPU => (rc_form.lpu)::numeric, pnC_JOUR => (rc_form.c_jour)::numeric, pnC_KIND => rc_form.c_kind, psC_SER => rc_form.c_ser, pnC_NUM => rc_form.c_num, pnC_STATE => pnc_state, pdDATE_OUT => rc_form.date_out, pnGIVEN_OUT_EMP => (rc_form.given_out_emp)::numeric, pnINSTEAD_CF => (rc_form.instead_cf)::numeric, pnLOST_CF => (rc_form.lost_cf)::numeric, pdDATE_LOST => rc_form.date_lost, pnSPINE => rc_form.spine, vAPI_VERSION => 5, pnLPU_GIVER => (rc_form.lpu_giver)::numeric, psLPU_GIVER_HANDLE => rc_form.lpu_giver_handle, pnANOTHER_LPU_CERT => rc_form.another_lpu_cert, psINTERNAL_NUMBER => rc_form.internal_number, psACT_NUMB => rc_form.act_numb, pdACT_DATE => rc_form.act_date, psFIO_EMP_ZAGS => rc_form.fio_emp_zags, psNAME_ZAGS => rc_form.name_zags, pnCF_FORM352 => rc_form.cf_form352, pnCF_TYPE => rc_form.cf_type);

    END IF;
END
$procedure$
```

---

### Функция №2: d_pkg_certificate_forms.clear

```sql
CREATE OR REPLACE PROCEDURE d_pkg_certificate_forms.clear(IN pnid numeric, IN pnlpu numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $procedure$
DECLARE
    sMESSAGE varchar(4000);
BEGIN
    CALL d_pkg_certificate_forms.clear(pnid, pnlpu, smessage);
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
