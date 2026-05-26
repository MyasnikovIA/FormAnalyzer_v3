// core/model/RouterVariable.java
package ru.tmis.analyzer.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Переменные в Router компонентах (ActionVar, DataSetVar, SubActionVar, Variable)
 */
public class RouterVariable {

    private final String name;           // Имя переменной (bind-параметр)
    private final String src;            // Источник значения
    private final String srcType;        // Тип источника (session, var, ctrl, parent и т.д.)
    private final String get;            // GET атрибут
    private final String put;            // PUT атрибут
    private final String type;           // Тип данных (integer, string и т.д.)
    private final String len;            // Длина
    private final String defaultValue;   // Значение по умолчанию
    private final Map<String, String> attributes; // Все остальные атрибуты

    private RouterVariable(Builder builder) {
        this.name = builder.name;
        this.src = builder.src;
        this.srcType = builder.srcType;
        this.get = builder.get;
        this.put = builder.put;
        this.type = builder.type;
        this.len = builder.len;
        this.defaultValue = builder.defaultValue;
        this.attributes = builder.attributes;
    }

    // Геттеры
    public String getName() { return name; }
    public String getSrc() { return src; }
    public String getSrcType() { return srcType; }
    public String getGet() { return get; }
    public String getPut() { return put; }
    public String getType() { return type; }
    public String getLen() { return len; }
    public String getDefaultValue() { return defaultValue; }
    public Map<String, String> getAttributes() { return attributes; }

    public boolean isInput() {
        return src != null && !src.isEmpty() && (put == null || put.isEmpty());
    }

    public boolean isOutput() {
        return put != null && !put.isEmpty();
    }

    public boolean isParentSource() {
        return "parent".equalsIgnoreCase(srcType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Variable{name='").append(name).append("'");
        if (src != null) sb.append(", src='").append(src).append("'");
        if (srcType != null) sb.append(", srctype='").append(srcType).append("'");
        if (get != null) sb.append(", get='").append(get).append("'");
        if (put != null) sb.append(", put='").append(put).append("'");
        if (type != null) sb.append(", type='").append(type).append("'");
        sb.append("}");
        return sb.toString();
    }

    public static class Builder {
        private String name;
        private String src;
        private String srcType;
        private String get;
        private String put;
        private String type;
        private String len;
        private String defaultValue;
        private final Map<String, String> attributes = new LinkedHashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder setSrc(String src) { this.src = src; return this; }
        public Builder setSrcType(String srcType) { this.srcType = srcType; return this; }
        public Builder setGet(String get) { this.get = get; return this; }
        public Builder setPut(String put) { this.put = put; return this; }
        public Builder setType(String type) { this.type = type; return this; }
        public Builder setLen(String len) { this.len = len; return this; }
        public Builder setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; return this; }
        public Builder addAttribute(String key, String value) { this.attributes.put(key, value); return this; }

        public RouterVariable build() {
            return new RouterVariable(this);
        }
    }
}