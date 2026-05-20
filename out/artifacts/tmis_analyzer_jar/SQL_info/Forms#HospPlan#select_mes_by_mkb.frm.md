# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ Forms/HospPlan/select_mes_by_mkb.frm

> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL

## Контекст задачи

Перед тобой техническая документация по форме(ам) системы T-MIS. Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) и таблицам в базах данных Oracle и PostgreSQL.

**Анализируемая форма:** Forms/HospPlan/select_mes_by_mkb.frm
**Базовая форма:** C:\AppServ\www\6_mis_MEDDEV-151331\Forms\HospPlan\select_mes_by_mkb.frm

**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, чтобы понять бизнес-логику системы и взаимосвязи между объектами.

**Дата генерации:** Wed May 20 10:23:54 NOVT 2026

---


## 1. SQL ЗАПРОСЫ С ТЭГАМИ

Ниже представлены все SQL запросы, извлеченные из форм. Каждый запрос включает XML-теги компонента (DataSet или Action) и содержит информацию об источнике.

**Статистика:**
- Всего SQL запросов: 1
- Всего форм: 1

---

### Запрос №1

**Тип компонента:** M2 DataSet
**Имя компонента:** DS_MESES
**Источник:** Forms/HospPlan/select_mes_by_mkb.frm
**Базовая форма:** C:\AppServ\www\6_mis_MEDDEV-151331\Forms\HospPlan\select_mes_by_mkb.frm

**SQL код:**

```xml
<component cmptype="DataSet" name="DS_MESES">
	<![CDATA[
	 	select  t.ID,
		 t.M_CODE,
		t.M_NAME,
		z.APPLICABILITY_ERR,
		t.DAYS_ON_BED,
		t.COMMENTS
	  from  table(cast(d_pkg_meses.meses_for_visit(:LPU,:MKB,:PATIENT,1,null,null,sysdate) as d_cl_meses_for_visit)) z,
	       d_v_meses t
	  where t.id = z.mes_id
	]]>
	<component cmptype="Variable" name="LPU" src="LPU" srctype="session" />
	<component cmptype="Variable" name="MKB" src="HOSP_MKB" srctype="var" get="v1" />
	<component cmptype="Variable" name="PATIENT" src="PATIENT_ID" srctype="var" get="v2" />
</component>
```

**Используемые пакеты/функции:** D_PKG_MESES.MESES_FOR_VISIT


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
- Всего уникальных пакетных функций: 1
- Загружено тел функций: 0

---

### Функция №1: D_PKG_MESES.MESES_FOR_VISIT

Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).


## 6.5. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘

Ниже представлены тела функций и процедур из PostgreSQL, которые используются в SQL запросах форм.

**Статистика:**
- Всего уникальных функций/процедур: 1
- Загружено тел функций: 0

---

### Функция №1: d_pkg_meses.meses_for_visit

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
                                           (select t.ID
                                              from D_V_CSE_ACS_ALL t
                                             where t.WHO_ID    = (:pnEMPLOYER)::numeric
                                               and t.WHO_CODE  = 'EMPLOYERS'
                                               and t.UNITCODE  = :psUNITCODE
                                               and t.UNIT_ID   = (:pnUNIT_ID)::numeric),
                                           'HOSP_PLAN_KINDS',
                                           (:pnUNIT_ID)::numeric) as t(RIGHT_ID, NRIGHT_CODE, RIGHT_NAME)
   where instr(';' || :psCHECK_CODE || ';', ';' || t.NRIGHT_CODE || ';') > 0;

  -- Присвоение результата выходной переменной
  :pnRES_ACCESS := nRES_ACCESS;
end;
```


### 78  В SQL запросах "exception" и "when" пишутся на одной строке
Не правильный пример:
```
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            begin
              select hpk.ID
                into :pnD_INSERT_ID
                from D_V_HPK_PLANS hpk
               where hpk.LPU = :pnLPU
            exception
              when no_data_found then
                D_PKG_HPK_PLANS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Идентификатор новой записи
                                    pnGEN_COUNT   => null);            -- Общее кол-во мест
            end;
            ]]>
        </component>
```
Правильный пример:
```
        <component cmptype="ActionRouter" condition="TYPE_DATABASE=ORACLE">
            <![CDATA[
            begin
              select hpk.ID
                into :pnD_INSERT_ID
                from D_V_HPK_PLANS hpk
               where hpk.LPU = :pnLPU
            exception when no_data_found then
              D_PKG_HPK_PLANS.ADD(pnD_INSERT_ID => :pnD_INSERT_ID,   -- Идентификатор новой записи
                                  pnGEN_COUNT   => null);            -- Общее кол-во мест
            end;
            ]]>
        </component>
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
<div cmptype="bogus" oncreate="base().OnCreate();" window_size="998x600">
<div cmptype="title">Подходящие/ограниченно-подходящие МЭСы</div>
<component cmptype="Script">
	Form.OnCreate = function()
	{
		setVar('ModalResult', 0, 1);
		setVar('HOSP_MKB', getVar('HOSP_MKB', 1));
		setVar('PATIENT_ID', getVar('PATIENT_ID', 1));
	}
	Form.SelectEmployer = function()
	{
		if(empty(getValue('MESES')))
		{
			alert('Не выбрана запись');
			return;
		}
		setVar('ModalResult', 1, 1);
		setVar('return_id',getValue('MESES'),1)
		setVar('return',getCaption('MESES'),1);
		closeWindow();
	}
</component>
<component cmptype="DataSet" name="DS_MESES">
	<![CDATA[
	 	select  t.ID,
		 t.M_CODE,
		t.M_NAME,
		z.APPLICABILITY_ERR,
		t.DAYS_ON_BED,
		t.COMMENTS
	  from  table(cast(d_pkg_meses.meses_for_visit(:LPU,:MKB,:PATIENT,1,null,null,sysdate) as d_cl_meses_for_visit)) z,
	       d_v_meses t
	  where t.id = z.mes_id
	]]>
	<component cmptype="Variable" name="LPU" src="LPU" srctype="session"/>
	<component cmptype="Variable" name="MKB" src="HOSP_MKB" srctype="var" get="v1"/>
	<component cmptype="Variable" name="PATIENT" src="PATIENT_ID" srctype="var" get="v2"/>
</component>
<table style="width:100%;height:100%;">
<tbody valign="top" style="height:100%;width:100%;">
<tr>
	<td style="width:100%;" valign="top">
		<component cmptype="Grid" name="MESES" dataset="DS_MESES" field="ID" returnfield="M_CODE" grid_caption="МЭСы"
						ondblclick="base().SelectEmployer();" width="100%" height="500px">
			<component cmptype="Column" caption="Код" field="M_CODE" filter="M_CODE" sort="M_CODE"/>
			<component cmptype="Column" caption="Наименование" field="M_NAME" filter="M_NAME" sort="M_NAME"/>
			<component cmptype="Column" caption="Уточнение" field="APPLICABILITY_ERR" filter="APPLICABILITY_ERR" sort="APPLICABILITY_ERR"/>
			<component cmptype="Column" caption="Кол-во дней" field="DAYS_ON_BED" filter="DAYS_ON_BED" sort="DAYS_ON_BED"/>
			<component cmptype="Column" caption="Описание" field="COMMENTS" filter="COMMENTS" sort="COMMENTS"/>
			<component cmptype="GridFooter" separate="true">
				<component cmptype="Range" insteadrefresh="InsteadRefresh(this);" id="range1" count="5" varstart="r1s" varcount="r1c" valuecount="15" valuestart="1" />
			</component>
		</component>
	</td>
</tr>
<tr>
	<td style="width:100%;text-align:right;">
		<component cmptype="Button" name="ButtonOk" onclick="base().SelectEmployer();" caption="OK" style="width:86px"/>
		<component cmptype="Button" name="ButtonCancel" onclick="closeWindow();" caption="Отмена" style="width:86px"/>
	</td>
</tr>
</tbody>
</table>
</div>

```

