package ru.tmis.analyzer.core.model;

import java.util.Objects;

public class BrokerInfo {
    private String unit;
    private String action;
    private String execProc;
    private String directFunction;
    private BrokerType type;

    public enum BrokerType {
        TYPE1_UNIT_ACTION,   // unit="XXX" action="INSERT" - нужен поиск
        TYPE2_DIRECT_FUNCTION // action="D_PKG_XXX.YYY" - функция уже известна
    }

    // Конструктор для типа 1 (unit + action)
    public BrokerInfo(String unit, String action) {
        this.unit = unit;
        this.action = action;
        this.type = BrokerType.TYPE1_UNIT_ACTION;
    }

    // Конструктор для типа 2 (прямое указание функции)
    public BrokerInfo(String directFunction) {
        this.directFunction = directFunction;
        this.type = BrokerType.TYPE2_DIRECT_FUNCTION;
        this.execProc = directFunction;
    }

    // Геттеры
    public String getUnit() { return unit; }
    public String getAction() { return action; }
    public String getExecProc() { return execProc; }
    public String getDirectFunction() { return directFunction; }
    public BrokerType getType() { return type; }

    // Сеттеры
    public void setExecProc(String execProc) {
        this.execProc = execProc;
    }

    public String getFormattedString() {
        if (type == BrokerType.TYPE1_UNIT_ACTION) {
            return String.format("unit:%s  action:%s;", unit, action);
        } else {
            return String.format("action:%s;", directFunction);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerInfo that = (BrokerInfo) o;
        if (type == BrokerType.TYPE1_UNIT_ACTION && that.type == BrokerType.TYPE1_UNIT_ACTION) {
            return Objects.equals(unit, that.unit) && Objects.equals(action, that.action);
        } else if (type == BrokerType.TYPE2_DIRECT_FUNCTION && that.type == BrokerType.TYPE2_DIRECT_FUNCTION) {
            return Objects.equals(directFunction, that.directFunction);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (type == BrokerType.TYPE1_UNIT_ACTION) {
            return Objects.hash(unit, action, type);
        } else {
            return Objects.hash(directFunction, type);
        }
    }
}