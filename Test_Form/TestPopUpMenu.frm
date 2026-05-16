<cmpForm class="d3form formBackground dispensary-observation-plan"
         oncreate="Form.onCreateDispensaryObservationPlan();"
         onshow="Form.onShowDispensaryObservationPlan();">
    <!--
        вкладка "План диспансерного наблюдения"
        ContrlCardControl2
    -->

    <cmpSubForm path="ControlCard/subforms/visit_functions"/>
    <cmpScript name="DispensaryObservationPlanScript">
        <![CDATA[
        /* массив с услугами отмеченные чекбоксами */
        Form.servicesInPlan = [];
        Form.onCreateDispensaryObservationPlan = function() {
            setVar('tabLoaded', false);
            setVar('PARENT_VAR', null);
            setVar('PARENT_ROW_ID', null);
            const checksAfterSaveDispPlan = getPage().getVar('checksAfterSaveDispPlan') || [];
            getPage().setVar('checksAfterSaveDispPlan', checksAfterSaveDispPlan);
            /* вызов экшена для апдейта UPDATE_ACTION_PMC_DISP_PLAN */
            if (+getVar('DispPlanPlace') === 1) {
                checksAfterSaveDispPlan.push(function(type, callback) {
                    Form.saveDispensaryObservationPlan(type, callback);
                });
            }
        };
        Form.onShowDispensaryObservationPlan = function() {
            setVar('ID', getVar('ID') || getPage().getVar('ID'));
            setVar('CONTROL_CARD', getVar('ID') || getPage().getVar('ID'));
            refreshDataSet('dsPmcDispPlanControl');
            getControl('dsPmcDispPlanControl_DATE_GROUP_SORT_SortItem').closest('td').style.display = 'none';
            Form.handlerSelectList();
        };
        Form.saveDispensaryObservationPlan = function(type, callback) {
            const dispId = [];
            const dispPlaceValues = [];
            const clones = getRepeater('PLAN_GR_repeater').clones();
            for (let i = 0; i < clones.length; i++) {
                closureContext(clones[i]);
                    if (!empty(clones[i].clone.data['TREE_HID'])) {
                        dispId.push(clones[i].clone.data['ID']);
                        dispPlaceValues.push(getValue('DISP_PLACE'));
                    }
                unClosureContext();
            }

            setVar('PMC_DISP_PLAN_ID', dispId);
            setVar('DISP_PLACE', dispPlaceValues);
            beginRequest();
                executeAction('UPDATE_ACTION_PMC_DISP_PLAN', function() {
                    if (+type === 1) {
                        refreshDataSet('dsPmcDispPlanControl');
                    }
                });
            endRequest(null, null, function() {
                callback && callback(type);
            });
        };
        /*
        общая выпадашка для изменения места проведения приема
         */
        Form.onChangeAllDispPlace = function() {
            const allDispPlace = getValue('ALL_DISP_PLACE');
            const clones = getRepeater('PLAN_GR_repeater').clones();
            for (let i = 0; i < clones.length; i++) {
                closureContext(clones[i]);
                if ((getValue('PLAN_GR_SelectList') + ';').indexOf((clones[i].clone.data['ID'] + ';')) > -1) {
                    if (!empty(clones[i].clone.data['TREE_HID'])) {
                        setValue('DISP_PLACE', allDispPlace);
                    } else {
                        //если отмечен родитель, то проставляем в выпадашке для всех его детей
                        const childrenRows = getControl('PLAN_GR').querySelectorAll('tr[name="PLAN_GR_Row"][parentvalue="' + clones[i].clone.data['ID'] + '"]');
                        Array.prototype.forEach.call(childrenRows, function(row) {
                            closureContext(row);
                                setValue('DISP_PLACE', allDispPlace);
                            unClosureContext();
                        });
                    }
                }
                unClosureContext();
            }
        };
        Form.unCheckRows = function() {
            var rows = getRepeater('PLAN_GR_repeater').clones();
            rows.forEach(function(row) {
                closureContext(row);
                    var checkBox = getDomBy(row, '.SelectListItem');
                    checkBox.checked = false;
                unClosureContext();
            });
        };
        Form.resetAllChecks = function(domSelectList) {
            Form.servicesInPlan.length = 0;
            domSelectList.D3SelectList.values_count = 0;
            domSelectList.D3SelectList.allc = 0;
            Form.unCheckRows();
        };
        Form.handlerSelectList = function() {
            var domSelectList = getPage().form.DOM.querySelector('[name="PLAN_GR_SelectList"]');
            domSelectList.onclick = function() {
                var serviceList = getDataSet('dsPmcDispPlanControl').data;
                D3Api.SelectListCtrl.onMouseClick(domSelectList);
                /*
                state = 0 - снят главный чек
                state = 2 - отмечен главный чек
                 */
                var state = +domSelectList.D3SelectList.state;
                setVar('PLAN_GR_SELECT_LIST_STATE', state);
                if (state === 0 || serviceList.length === 0) {
                    Form.resetAllChecks(domSelectList);
                    return;
                }

                refreshDataSet('dsDispPlanServices', function() {
                    Form.servicesInPlan = getDataSet('dsDispPlanServices').data;
                    if (Form.servicesInPlan.length) {
                        var ids = Form.servicesInPlan.map(service => service.TREE_HID);
                        var countServices = ids.length;
                        if (countServices > 0) {
                            for (var i = 0; i < countServices; i++) {
                                domSelectList.D3SelectList.data[ids[i]] = null;
                            }
                            domSelectList.D3SelectList.values_count = countServices;
                            domSelectList.D3SelectList.allc = countServices;
                        }
                    }
                });
            };
        };
        /*
        клик по ссылке в колонке статус
         */
        Form.genRegPlanDate = function(dom) {
            var data = getControlProperty('PLAN_GR', 'data');
            var type = data['TYPE'];
            var pmcId = getPage().getVar('PERSMEDCARD');
            var pmcDispPlanId = data['ID'];

            var page = openWindow({
                name: 'GenRegistry/reg_full',
                vars: {
                    UNIT: 'CC',
                    DISP_PLAN_ID: data['ID'],
                    MODE: 'close',
                    SERV_ID: +type === 1 ? data['SERVICE_ID'] : null,
                    SPEC_ID: +type === 1 ? null : data['SPECIALITY_ID'],
                    DS_REC_TYPE: +type === 1 ? 1 : 0,
                    DS_DATE: data['PLAN_DATE'] || getValue(dom)
                }
            }, true);

            page.addListener('onshow', function() {
                if (typeof(base().SetSearchPatient) == 'function') {
                    base().SetSearchPatient(pmcId);
                }
            });

            page.addListener('onafterclose', function(mod) {
                if (+getPage().getVar('ModalResult') === 1) {
                    setVar('PMC_DISP_PLAN_ID', pmcDispPlanId);
                    setVar('DIRECTION_SERVICE_ID', mod.ParamDirectionServiceID || getPage().getVar('ParamDirectionServiceID'));
                    setVar('psREG_CODE', 'DIRECTION_SERVICES');
                    executeAction('PMC_DISP_PLAN_SET_REG', function() {
                        setControlProperty('PLAN_GR', 'locate', pmcDispPlanId);
                        refreshDataSet('dsPmcDispPlanControl');
                    });
                }
            });
        };
        /*
        после рефреша дерева
         */
        Form.afterRefreshDispPlan = function() {
            var hasAppointments = getDataSet('dsPmcDispPlanControl').data && getDataSet('dsPmcDispPlanControl').data.length > 0;
            ['DISP_PLAN', 'DateApply'].forEach(function(e) {
                getPage().setControlProperty(e, 'enabled', !hasAppointments);
            });
            var hintMessage = 'По плану ' + getPage().getCaption('DISP_PLAN') + ' есть запланированные явки, смена плана невозможна.';
            _setHint(getPage().getControlByName('DISP_PLAN'), hasAppointments ? hintMessage : '');

            if (getVar('PARENT_ROW_ID')) {
                Form.setCheckedChildRows(getVar('PARENT_ROW_ID'));
            }

            var domSelectList = getPage().form.DOM.querySelector('[name="PLAN_GR_SelectList"]');
            var state = +domSelectList.D3SelectList.state;
            if (state === 0) {
                Form.resetAllChecks(domSelectList);
            }

            getPage().getVar('IS_CC_PREVIEW') && getPage().getVar('previewControlCard') && getPage().getVar('previewControlCard')();

            if (+getVar('DispPlanPlace') === 0) {
                setDomVisible(getControl('ALL_DISP_PLACE'), false);
                Form.hideGridCol(getControl('PLAN_GR'), 'DISP_PLACE_COLUMN');
            }
        };
        /*
        костыль ф-ия для скрытия колонки в гриде, тк Grid_hideColByName работает некорректно
        при изменении настроек в профиле колонки всё равно отображаются
         */
        Form.hideGridCol = function(gridDom, colName) {
            const cols = gridDom.querySelectorAll('[column_name="' + colName + '"]');
            for (let i = 0; i < cols.length; i++) {
                cols[i].style.display = 'none';
            }

            const data = gridDom.D3Store.cols;
            const foundColumn = data.find(item => item.name === colName);

            if (foundColumn) {
                foundColumn._show = false;
            }
        };
        Form.setCheckedChildRows = function(parentId, currentCheckBox) {
            var childRows = getControl('PLAN_GR').querySelectorAll('[name="PLAN_GR_Row"][parentvalue="' + parentId + '"]');
            [...childRows].forEach(function(row) {
                var checkBox = getDomBy(row, '.SelectListItem');
                if (currentCheckBox) {
                    D3Api.SelectListItemCtrl.setState(checkBox, currentCheckBox.checked);
                } else {
                    D3Api.SelectListItemCtrl.setState(checkBox, true);
                }
            });
        };
        Form.onToggleParentNode = function(clone) {
            clone.querySelector('.btnOC').onmousedown = function(event) {
                D3Api.setEvent(event);
                D3Api.TreeCtrl.toggleNode(clone);
                D3Api.addClass(clone, 'children-loaded');
                setVar('PARENT_ROW_ID', '');
                if (clone.querySelector('.SelectListItem').checked) {
                    setVar('PARENT_ROW_ID', clone.getAttribute('keyvalue'));
                }
            };
        };
        Form.onClickCheckBox = function(clone) {
            clone.querySelector('.SelectListItem').onclick = function(event) {
                var currentCheckBox = clone.querySelector('.SelectListItem');
                var currentRowId = currentCheckBox.getAttribute('item_value');
                var currentRow = getControl('PLAN_GR').querySelector('[name="PLAN_GR_Row"][keyvalue="' + currentRowId + '"]');
                var parentId = currentRow.getAttribute('parentvalue');

                if (parentId) {
                    //дочерняя строка
                    Form.onClickChildCheckBox(parentId, currentCheckBox, currentRowId);
                } else {
                    //родительская строка
                    Form.onClickParentCheckBox(currentCheckBox, currentRow);
                }
            };
        };
        Form.onClickParentCheckBox = function(currentCheckBox, currentRow) {
            var parentRowId = currentCheckBox.getAttribute('item_value');//PARENT_ROW_ID
            Form.setCheckedChildRows(parentRowId, currentCheckBox);
            if (!currentCheckBox.checked) {
                Form.servicesInPlan = Form.servicesInPlan.filter(service => +service.PARENT_ROW_ID !== +parentRowId);
                setVar('servicesInPlan', Form.servicesInPlan);
            } else {
                Form.onAddChildrenRows(currentRow, parentRowId);
            }
        };
        Form.onAddChildrenRows = function(currentRow, parentRowId) {
            var parentPropertyObj = {
                PARENT_ROW_ID: parentRowId
            };
            if (!D3Api.hasClass(currentRow, 'children-loaded')) {
                setVar('CHECKED_DATE', '01.' + getDomBy(currentRow, 'span').textContent);
                refreshDataSet('dsChildrenRows', function() {
                    var childrenRows = getDataSet('dsChildrenRows').data;
                    if (childrenRows.length) {
                        childrenRows = childrenRows.map(child => Object.assign(child, parentPropertyObj));
                        Array.prototype.push.apply(Form.servicesInPlan, childrenRows);
                    }
                    setVar('servicesInPlan', Form.servicesInPlan);
                });
            } else {
                var children = D3Api.getAllDomBy(getControl('PLAN_GR'), '[parentvalue="' + D3Api.TreeCtrl.getValue(currentRow) + '"]');
                var childrenRows = [];
                for (var childInd = 0; childInd < children.length; childInd++) {
                    if (children[childInd].querySelector('.SelectListItem').checked) {
                        childrenRows.push(Object.assign(children[childInd].clone.data, parentPropertyObj));
                    }
                }
                Array.prototype.push.apply(Form.servicesInPlan, childrenRows);
                setVar('servicesInPlan', Form.servicesInPlan);
            }
        };
        Form.onClickChildCheckBox = function(parentId, currentCheckBox, currentRowId) {
            var parentRow = getControl('PLAN_GR').querySelector('[name="PLAN_GR_Row"][keyvalue="' + parentId + '"]');
            var parentCheckBox = getDomBy(parentRow, '.SelectListItem');
            D3Api.SelectListItemCtrl.onMouseClick(currentCheckBox);

            if (!currentCheckBox.checked) {
                D3Api.SelectListItemCtrl.setState(parentCheckBox, false);
                //удалить из списка
                var childIndex = Form.servicesInPlan.findIndex(el => +el.ID === +currentCheckBox.getAttribute('item_value'));
                Form.servicesInPlan.splice(childIndex, 1);
            } else {
                var childRows = getControl('PLAN_GR').querySelectorAll('[name="PLAN_GR_Row"][parentvalue="' + parentId + '"]');
                var countChildCheckBoxesChecked = 0;
                for (var i = 0; i < childRows.length; i++) {
                    var currentChildCheckBox = getDomBy(childRows[i], '.SelectListItem');
                    if (!currentChildCheckBox.checked) {
                        D3Api.SelectListItemCtrl.setState(parentCheckBox, false);
                        break;
                    } else {
                        countChildCheckBoxesChecked++;
                    }
                }

                D3Api.SelectListItemCtrl.setState(parentCheckBox, countChildCheckBoxesChecked === childRows.length);
                //добавить в список
                Form.servicesInPlan.push(currentCheckBox.closest('tr').clone.data);
            }
        };
        Form.afterCloneDispPlan = function(clone, data) {
            Form.onToggleParentNode(clone);
            Form.onClickCheckBox(clone);
            closureContext(clone);
                if (empty(data['STATE']) && data['TREE_HID']) {
                    setCaption('VISIT_LINK', 'Принять');
                }
                if (empty(data['STATE']) && data['TREE_HID'] && data['VISIT_ID']) {
                    setCaption('VISIT_LINK', 'Редактировать');
                }
                if (+data['NSERV_STATUS'] === 2) {
                    setDomVisible(getControl('VISIT_LINK'), false);
                    setDomVisible(getControl('REWRITE_LINK'), true);
                }

                if (+getVar('DispPlanPlace') === 1) {
                    Form.removeDispPlace && Form.removeDispPlace(data);
                }
                Form.setRowStyleByState(clone, data);
            unClosureContext();
        };
        Form.removeDispPlace = function(data) {
            if (empty(data['TREE_HID'])) {
                getControl('DISP_PLACE').remove();
            }
        };
        /*
        кнопка пересчитать вверху дерева
        */
        Form.rebuildPlan = function() {
            executeAction('REBUILD_PLAN', function() {
                refreshDataSet('dsPmcDispPlanControl');
            });
        };
        /*
        попап меню
         */
        Form.delDirectionService = function() {
            D3Api.showConfirm('Вы действительно хотите удалить запись?', function() {
                var data = getControlProperty('PLAN_GR', 'data');
                setVar('DIRECTION_SERVICE', data['DIRECTION_SERVICE']);
                executeAction('CLEAR_REG_FROM_DISP_PLAN', function() {
                    executeAction('DIRECTION_SERVICES_DELETE', function() {
                        Form.onRefreshPlan();
                    });
                });
            });
        };
        Form.delServices = function() {
            D3Api.showConfirm('Вы действительно хотите удалить услугу из плана?', function() {
                var servicesWithoutReg = [];
                var servicesWithReg = [];
                var servicesWithVisit = [];

                var currentRowInServiceList = Form.servicesInPlan.filter(function(item) {
                    return +item['ID'] === +getVar('CURRENT_ROW')['ID'];
                })[0];
                if (!currentRowInServiceList) {
                    Form.servicesInPlan.push(getVar('CURRENT_ROW'));
                }

                Form.servicesInPlan.forEach(function(el) {
                    if (empty(el.REG_ID)) {
                        servicesWithoutReg.push(el.ID);
                    }
                    if (!empty(el.REG_ID) && empty(el.VISIT_ID)) {
                        servicesWithReg.push(el);
                    }
                    if (!empty(el.VISIT_ID)) {
                        servicesWithVisit.push(el);
                    }
                });

                if (servicesWithoutReg.length > 0) {
                    setVar('DEL_ID', servicesWithoutReg);
                    executeAction('DELETE_SERV_FROM_PLAN', function() {
                        Form.onAfterDeleteServicesFromPlan(servicesWithReg, servicesWithVisit);
                    });
                } else {
                    Form.onAfterDeleteServicesFromPlan(servicesWithReg, servicesWithVisit);
                }
            });
        };
        Form.onAfterDeleteServicesFromPlan = function(servicesWithReg, servicesWithVisit) {
            if (servicesWithReg.length) {
                Form.onShowDeleteConfirm(Form.servicesInPlan, servicesWithReg, servicesWithVisit);
                return;
            }
            if (servicesWithVisit.length) {
                Form.onAfterDeleteServicesWithReg(servicesWithVisit);
                return;
            }
            Form.onRefreshPlan();
        };
        Form.onShowDeleteConfirm = function(servicesInPlan, servicesWithReg, servicesWithVisit) {
            openD3Form('ControlCard/subforms/confirm_delete_services_with_reg', true, {
                width: 700,
                height: 450,
                vars: {
                    CC_ID: getVar('ID') || getVar('CONTROL_CARD'),
                    CHECKED_ALL_SERVICES: servicesInPlan,
                    SERVICES_WITH_REG: servicesWithReg,
                    SERVICES_WITH_VISIT: servicesWithVisit
                },
                onclose: function(mod) {
                    Form.servicesInPlan = [];
                    if (servicesWithVisit.length) {
                        Form.onAfterDeleteServicesWithReg(servicesWithVisit);
                    } else {
                        Form.onRefreshPlan();
                    }
                }
            });
        };
        Form.onAfterDeleteServicesWithReg = function(servicesWithVisit) {
            openD3Form('ControlCard/subforms/services_with_visit', true, {
                width: 700,
                height: 450,
                vars: {
                    SERVICES_WITH_VISIT: servicesWithVisit
                },
                onclose: function(mod) {
                    Form.servicesInPlan = [];
                    Form.onRefreshPlan();
                }
            });
        };
        Form.onRefreshPlan = function() {
            refreshDataSet('dsPmcDispPlanControl', function() {
                D3Api.SelectListCtrl.unCheckAll(getControl('PLAN_GR_SelectList'));
            });
        };
        Form.addServiceInPlan = function() {
            openWindow({
                name: 'UniversalComposition/UniversalComposition',
                unit: 'LPU_SERVICES',
                composition: 'GRID',
                multisel: true
            }, true, 610, 460).addListener('onafterclose', function(mod) {
                if (mod && mod['return_id']) {
                    openD3Form('ControlCard/control_card_appointment', true, {
                        width: 400,
                        height: 0,
                        vars: {
                            'SERV_ID': mod['return_id'],
                            'PERSMEDCARD': getPage().getVar('PERSMEDCARD'),
                            'CONTROL_CARD': getVar('ID')
                        },
                        onclose: function(res) {
                            if (res && res['ModalResult']) {
                                refreshDataSet('dsPmcDispPlanControl');
                            }
                        }
                    });
                }
            });
        };
        Form.dispPlanOnPopup = function() {
            var data = getControlProperty('PLAN_GR', 'data');
            setVar('CURRENT_ROW', data);
            var isChildRow = !empty(data['TREE_HID']);
            var isVisibleServiceAttached = !empty(getValue('PLAN_GR')) && (empty(data['STATE']) && empty(data['VISIT_ID']) && empty(data['REG_ID']));

            setVisible(getControl('piDel'), isChildRow && (!empty(data['SSERV_REG']) && empty(data['VISIT_ID'])));
            setVisible(getControl('piDelServ'), isChildRow && empty(data['SSERV_VISIT']));
            /* связать с услугой в текущем МО */
            setVisible(getControl('piAttachServiceInCurrentLpu'), isChildRow && isVisibleServiceAttached);
            /* связать с услугой в другом МО */
            setVisible(getControl('piAttachServiceInOtherLpu'), isChildRow && isVisibleServiceAttached);
            setVisible(getControl('piCancelAttach'), isChildRow && [1, 2].includes(+data['STATE']));
            const autoPopupItems = D3Api.getAllDomBy(getControl('DISP_PLAN_POPUP'), '[name="additionalMainMenu"]');
            autoPopupItems.forEach(item => D3Api.setControlPropertyByDom(item, 'visible', !empty(getValue('PLAN_GR')) && isChildRow));
        };
        Form.refreshDispPlan = function() {
            refreshDataSet('dsPmcDispPlanControl');
        };
        Form.onPrintMassAnalysis = function() {
            var selectList = getValue('PLAN_GR_SelectList').split(';');
            if (!selectList.length) {
                D3Api.showAlert('Вы не выбрали ни одной услуги');
                return;
            }

            var dsIds = [];
            var data = Form.servicesInPlan;
            for (var i = 0; i < data.length; i++) {
                if (selectList.includes(data[i]['ID']) && +data[i]['IS_LIS']) {
                    dsIds.push(data[i]['DIRECTION_SERVICE']);
                }
            }

            if (dsIds.length === 0) {
                D3Api.showAlert('Для выбранных услуг не создано направление на анализ');
                return;
            }

            getPage().setVar('DS_IDS', dsIds.join(';'));
            getPage().setVar('DIR_SERV_ID', dsIds.join(';'));
            getPage().setVar('FIND_CHILDREN', 1);
            printReportByCode('lis_directions_mass');
        };
        Form.onChangeSeType = function() {
            if (D3Api.isUserEvent()) {
                refreshDataSet('dsPmcDispPlanControl');
            }
        };
        /* state: 1 - текущее, 2 - другое ЛПУ */
        Form.onAttachService = function(state) {
            var data = getControlProperty('PLAN_GR', 'data');
            var vars = {
                STATE: state,
                PLAN_ROW_ID: data['ID'],
                TREE_HID: data['TREE_HID'] || data['ID'],
                SERVICE_CODE: data['SERVICE'],
                SERVICE_ID: data['SERVICE_ID'],
                DATE_MM_YY: data['DATE_GROUP'],
                DATE_DD_MM_YY: data['DATE_GROUP_SORT'],
                CC_ID: getVar('ID') || getPage().getVar('ID'),
                AGENT_ID: getPage().getVar('AGENT'),
                PERSMEDCARD_ID: getVar('PERSMEDCARD') || getPage().getVar('PERSMEDCARD'),
                PATIENT_AGE: getPage().getVar('PATIENT_AGE'),
                MKB_ID: getPage().getVar('MKB')
            };

            openD3Form('ControlCard/cc_attach_services', true, {
                width: 1000,
                height: 550,
                vars: vars,
                onclose: function(mod) {
                    if (mod && +mod['ModalResult'] === 1) {
                        refreshDataSet('dsPmcDispPlanControl');
                    }
                }
            });
        };
        Form.onCancelAttach = function() {
            var data = getControlProperty('PLAN_GR', 'data');
            setVar('REG_CODE', 'DIRECTION_SERVICES');
            setVar('STATE', '');
            setVar('REG_ID', data['REG_ID']);
            executeAction('cancelAttach', function() {
                refreshDataSet('dsPmcDispPlanControl');
            });
        };
        /* установка стилей для услуг в зависимости от STATE */
        Form.setRowStyleByState = function(dom, arrayData) {
            var state = arrayData['STATE'];
            /*
            STATE = 1 - связано с услугой в текущем МО
            STATE = 2 - связано с услугой в другом МО
             */
            if ([1, 2].includes(+state)) {
                dom.classList.add('attached-service');
            } else {
                dom.classList.remove('attached-service');
            }
        };
        Form.onClickSservVisit = function(visitId) {
            base().PrintVisit(visitId, D3Api.TreeCtrl.getActiveRow(getControl('PLAN_GR')).clone.data.DIRECTION_SERVICE);
        }
        ]]>
    </cmpScript>

    <!-- для апдейта мест проведения приема-->
    <cmpAction name="UPDATE_ACTION_PMC_DISP_PLAN">
        <![CDATA[
        begin
          D_PKG_PMC_DISP_PLAN.SET_DISP_PLACE(pclID         => :pclID,
                                             pnLPU         => to_number(:pnLPU),
                                             pclDISP_PLACE => :pclDISP_PLACE);
        end;
        ]]>
        <cmpActionVar name="pnLPU"         src="LPU"              srctype="session"/>
        <cmpActionVar name="pclID"         src="PMC_DISP_PLAN_ID" srctype="var" type="collection" tdo="D_CL_ID"/>
        <cmpActionVar name="pclDISP_PLACE" src="DISP_PLACE"       srctype="var" type="collection" tdo="D_CL_ID"/>
    </cmpAction>

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
        <cmpActionVar name="pnLPU"          src="LPU"     srctype="session"/>
        <cmpActionVar name="pnID"           src="PLAN_GR" srctype="ctrl"/>
        <cmpActionVar name="pnCONTROL_CARD" src="ID"      srctype="var"/>
        <cmpActionVar name="pnREG_ID"       src="REG_ID"  srctype="var"/>
    </cmpAction>

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
        <cmpDataSetVar name="CONTROL_CARD" src="ID"      srctype="var"/>
        <cmpDataSetVar name="SE_TYPE"      src="SE_TYPE" srctype="ctrl"/>
    </cmpDataSet>

    <cmpAction name="PMC_DISP_PLAN_SET_REG" unit="PMC_DISP_PLAN" action="SET_REG" mode="post">
        <cmpActionVar name="pnID"       src="PMC_DISP_PLAN_ID"     srctype="var"/>
        <cmpActionVar name="pnLPU"      src="LPU"                  srctype="session"/>
        <cmpActionVar name="psREG_CODE" src="psREG_CODE"           srctype="var"/>
        <cmpActionVar name="pnREG_ID"   src="DIRECTION_SERVICE_ID" srctype="var"/>
    </cmpAction>
    <!-- кнопка пересчитать план -->
    <cmpAction name="REBUILD_PLAN" >
        <![CDATA[
          begin
            D_PKG_CONTROL_CARD.RECOUNT_PLAN_DATES(:CONTROL_CARD, :LPU);
          end;
        ]]>
        <cmpActionVar name="LPU"          src="LPU" srctype="session"/>
        <cmpActionVar name="CONTROL_CARD" src="ID"  srctype="var"/>
    </cmpAction>
    <!-- попап меню удалить запись -->
    <cmpAction name="CLEAR_REG_FROM_DISP_PLAN">
        <![CDATA[
		  begin
		  -- Убирает ссылки на запись из плана при удалении записи на услугу
		    D_PKG_CONTROL_CARD.CLEAR_REG_FROM_DISP_PLAN(:CONTROL_CARD_ID, :DIRECTION_SERVICE_ID);
		  end;
		]]>
        <cmpActionVar name="CONTROL_CARD_ID"      src="ID"                srctype="var"/>
        <cmpActionVar name="DIRECTION_SERVICE_ID" src="DIRECTION_SERVICE" srctype="var"/>
    </cmpAction>
    <cmpAction cmptype="Action" name="DIRECTION_SERVICES_DELETE" unit="DIRECTION_SERVICES" action="DELETE">
        <cmpActionVar name="pnLPU"     src="LPU"               srctype="session"/>
        <cmpActionVar name="pnID"      src="DIRECTION_SERVICE" srctype="var"/>
        <cmpActionVar name="pnDEL_DIR" src="1"                 srctype="const"/>
    </cmpAction>
    <!-- попап меню удалить -->
    <!-- language=Oracle-->
    <cmpAction name="DELETE_SERV_FROM_PLAN">
        <![CDATA[
        declare
          cDEL_ID               D_CL_ID   := :DEL_ID; -- коллекция полученных ID выделенных записей
          nEXISTS               NUMBER(1) := 0;       -- признак наличия записи с данным ID в таблице D_PMC_DISP_PLAN
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
        <cmpActionVar name="LPU"    src="LPU"    srctype="session" />
        <cmpActionVar name="DEL_ID" src="DEL_ID" srctype="var" type="collection" tdo="D_CL_ID"/>
    </cmpAction>

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
        <cmpDataSetVar name="CONTROL_CARD" src="ID"         srctype="var"/>
        <cmpDataSetVar name="PARENT_VAR"   src="PARENT_VAR" srctype="var"/>
        <cmpDataSetVar name="SE_TYPE"      src="SE_TYPE"    srctype="ctrl"/>
    </cmpDataSet>

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
        <cmpDataSetVar name="CONTROL_CARD" src="ID"           srctype="var"/>
        <cmpDataSetVar name="CHECKED_DATE" src="CHECKED_DATE" srctype="var"/>
    </cmpDataSet>

    <div class="wrapper-form">
        <div name="userFragBlock" class="observation-cont">
            <div cmptype="tmp" name="WRAPPER_HEADER_DISP" style="display: flex;">
                <div>
                    <cmpButton name="Restart_Plan" caption="Пересчитать план" onclick="Form.rebuildPlan();"/>
                </div>
                <div style="margin-left: 5px;">
                    <cmpComboBox name="SE_TYPE" onchange="Form.onChangeSeType();">
                        <cmpComboItem value=""  caption="Все"/>
                        <cmpComboItem value="3" caption="Посещения"/>
                        <cmpComboItem value="8" caption="Анализы"/>
                        <cmpComboItem value="0" caption="Исследования"/>
                    </cmpComboBox>
                </div>
                <div style="margin-left: 5px;">
                    <cmpComboBox name="ALL_DISP_PLACE" onchange="Form.onChangeAllDispPlace();" width="100%">
                        <cmpComboItem value="0" caption="в МО"/>
                        <cmpComboItem value="1" caption="на дому"/>
                    </cmpComboBox>
                </div>
            </div>
            <div class="tree-cont">
                <cmpTree name="PLAN_GR"
                         dataset="dsPmcDispPlanControl"
                         selectlist="ID"
                         keyfield="ID"
                         parentfield="TREE_HID"
                         childsfield="HAS_CHILDREN"
                         parentvar="PARENT_VAR"
                         locatedataset="locatedataset"
                         caption="План диспансерного наблюдения"
                         opened="false"
                         onafter_clone="Form.afterCloneDispPlan(clone, data);"
                         onafter_refresh="Form.afterRefreshDispPlan();">
                    <cmpTreeColumn caption="Наименование" field="SERV_SPEC_NAME" width="60%" sort="DATE_GROUP_SORT" sortorder="1" />
                    <cmpTreeColumn caption="Статус" name="STATUS_COLUMN">
                        <cmpHyperLink name="SSERV_NULL" field="SSERV_NULL" data="caption:SSERV_NULL;value:PLAN_DATE" onclick="Form.genRegPlanDate(this);"/>
                        <cmpLabel name="SSERV_REG" field="SSERV_REG" data="caption:SSERV_REG"/>
                        <cmpHyperLink name="SSERV_VISIT" field="SSERV_VISIT" data="caption:SSERV_VISIT;value:VISIT_ID" onclick="Form.onClickSservVisit(getValue(this));"/>
                    </cmpTreeColumn>
                    <cmpTreeColumn caption="Планируемое место проведения приема" name="DISP_PLACE_COLUMN">
                        <cmpComboBox name="DISP_PLACE" width="100%" data="value:DISP_PLACE">
                            <cmpComboItem value="0" caption="в МО"/>
                            <cmpComboItem value="1" caption="на дому"/>
                        </cmpComboBox>
                    </cmpTreeColumn>
                    <cmpTreeColumn caption="Принять" name="VISIT_COLUMN">
                        <cmpHyperLink name="VISIT_LINK" onclick="Form.openVisit();"/>
                        <cmpHyperLink name="REWRITE_LINK" caption="Перезаписать" onclick="Form.genRegPlanDate(this);" style="display:none;"/>
                    </cmpTreeColumn>
                    <cmpTreeFooter>
                        <cmpRange dataset="dsPmcDispPlanControl" default_amount="15"/>
                    </cmpTreeFooter>
                </cmpTree>
                <cmpPopupMenu name="DISP_PLAN_POPUP" popupobject="PLAN_GR" onpopup="Form.dispPlanOnPopup();">
                    <cmpPopupItem name="piRefresh"                    caption="Обновить"                        onclick="Form.refreshDispPlan();"     icon="~CmpPopupMenu/Icons/refresh"/>
                    <cmpPopupItem name="piDel"                        caption="Удалить запись"                  onclick="Form.delDirectionService();" icon="~CmpPopupMenu/Icons/delete"/>
                    <cmpPopupItem name="piAddServ"                    caption="Добавить услугу"                 onclick="Form.addServiceInPlan();"    icon="~CmpPopupMenu/Icons/insert"/>
                    <cmpPopupItem name="piAttachServiceInCurrentLpu"  caption="Связать с услугой в текущей МО"  onclick="Form.onAttachService(1);"    icon="Images/Icons/PopUpMenu/chain-plus"/>
                    <cmpPopupItem name="piAttachServiceInOtherLpu"    caption="Связать с услугой в другой МО"   onclick="Form.onAttachService(2);"    icon="Images/Icons/PopUpMenu/chain-plus"/>
                    <cmpPopupItem name="piCancelAttach"               caption="Отменить связывание услуг"       onclick="Form.onCancelAttach();"      icon="Images/img2/repeate_cancel.png"/>
                    <cmpPopupItem name="piDelServ"                    caption="Удалить услугу из плана"         onclick="Form.delServices();"         icon="~CmpPopupMenu/Icons/delete"/>
                </cmpPopupMenu>
                <cmpAutoPopupMenu name="piReports" join_menu="DISP_PLAN_POPUP" popupobject="PLAN_GR" reports="true">
                    <cmpPopupItem name="piPrintMassAnalysis" caption="Массовая печать направлений на анализы" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem name="piPrintMassAnalysis2" caption="Массовая печать V2" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem name="piPrintMassAnalysis3" caption="Массовая печать V3" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem caption="Сервис_v2">
                        <cmpPopupItem caption="Логи1" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        <cmpPopupItem caption="Логи2" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                    </cmpPopupItem>
                </cmpAutoPopupMenu>
            </div>
                <cmpPopupMenu name="DISP_PLAN_POPUP333" popupobject="PLAN_GR" onpopup="Form.dispPlanOnPopup();">
                    <cmpPopupItem name="piRefresh"                    caption="Обновить"                        onclick="Form.refreshDispPlan();"     icon="~CmpPopupMenu/Icons/refresh"/>
                    <cmpPopupItem name="piDel"                        caption="Удалить запись"                  onclick="Form.delDirectionService();" icon="~CmpPopupMenu/Icons/delete"/>
                </cmpPopupMenu>
                <cmpAutoPopupMenu name="piReports" join_menu="DISP_PLAN_POPUP333" popupobject="PLAN_GR" reports="true" unit="HOSP_HISTORIES">
                    <cmpPopupItem name="piPrintMassAnalysis" caption="Массовая печать направлений на анализы" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem name="piPrintMassAnalysis2" caption="Массовая печать V4" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem name="piPrintMassAnalysis3" caption="Массовая печать V5" onclick="Form.onPrintMassAnalysis();" icon="~CmpPopupMenu/Icons/print"/>
                    <cmpPopupItem caption="Сервис_v2">
                        <cmpPopupItem caption="Логи6" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        <cmpPopupItem caption="Сервис_v3">
                            <cmpPopupItem caption="Логи10" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                            <cmpPopupItem caption="Логи11" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        </cmpPopupItem>
                        <cmpPopupItem caption="Логи7" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        <cmpPopupItem caption="Логи8" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        <cmpPopupItem caption="Логи9" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        <cmpPopupItem caption="Сервис_v4">
                            <cmpPopupItem caption="Логи12" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                            <cmpPopupItem caption="Логи13" name="logs" onclick="Form.getLogs();" icon="~CmpPopupMenu/Icons/report"/>
                        </cmpPopupItem>
                    </cmpPopupItem>
                </cmpAutoPopupMenu>


        </div>
    </div>


            "Обновить"
            "Удалить запись"
            "Добавить услугу"
            "Связать с услугой в текущей МО"
            "Связать с услугой в другой МО"
            "Отменить связывание услуг"
            "Удалить услугу из плана"
    <style>
        .dispensary-observation-plan .observation-tab .tree-cont {
            margin-top: 8px;
            flex-grow: 1;
        }
        .dispensary-observation-plan .observation-cont {
            height: 100%;
            display: flex;
            flex-direction: column;
        }
        .dispensary-observation-plan div.tree_columns.box-sizing-force > table {
            width: 100% !important;
        }
        .dispensary-observation-plan div.tree_columns table.tree_columns {
            padding-right: 0 !important;
        }
        .dispensary-observation-plan div.tree_data_cont.box-sizing-force > div {
            min-height: 300px;
        }
        .dispensary-observation-plan .attached-service,
        .dispensary-observation-plan .attached-service td a {
            color: green;
        }
    </style>
</cmpForm>