// core/extractor/DfmOverrideProcessor.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DfmOverrideProcessor {

    private static final Pattern NODE_PATTERN = Pattern.compile(
            "<node\\s+target=[\"']([^\"']+)[\"']\\s+pos=[\"']([^\"']+)[\"']\\s*>(.*?)</node>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "<attr\\s+target=[\"']([^\"']+)[\"']\\s+pos=[\"']([^\"']+)[\"']\\s+name=[\"']([^\"']+)[\"']\\s+value=[\"']([^\"']*)[\"']\\s*/?>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final boolean APPLY_OVERRIDES = false; // Отключено по умолчанию

    public String applyOverrides(String baseContent, String dfrmContent) {
        if (!APPLY_OVERRIDES) {
            return baseContent;
        }

        if (baseContent == null || dfrmContent == null) {
            return baseContent;
        }

        List<OverrideOperation> operations = parseDfrmFile(dfrmContent);
        if (operations.isEmpty()) {
            return baseContent;
        }

        try {
            Document doc = Jsoup.parse(baseContent, "", org.jsoup.parser.Parser.xmlParser());

            for (OverrideOperation op : operations) {
                try {
                    applyOperation(doc, op);
                } catch (Exception e) {
                    System.err.println("Error applying operation: " + op + " - " + e.getMessage());
                }
            }

            return doc.html();
        } catch (Exception e) {
            System.err.println("Error applying overrides: " + e.getMessage());
            return baseContent;
        }
    }

    private List<OverrideOperation> parseDfrmFile(String dfrmContent) {
        List<OverrideOperation> operations = new ArrayList<>();

        if (dfrmContent == null || dfrmContent.isEmpty()) {
            return operations;
        }

        String wrappedContent = wrapFullDfrmContent(dfrmContent);

        Matcher nodeMatcher = NODE_PATTERN.matcher(wrappedContent);
        while (nodeMatcher.find()) {
            String target = nodeMatcher.group(1);
            String position = nodeMatcher.group(2).toLowerCase();
            String content = nodeMatcher.group(3).trim();
            operations.add(OverrideOperation.createNode(target, position, content));
        }

        Matcher attrMatcher = ATTR_PATTERN.matcher(wrappedContent);
        while (attrMatcher.find()) {
            String target = attrMatcher.group(1);
            String position = attrMatcher.group(2).toLowerCase();
            String name = attrMatcher.group(3);
            String value = attrMatcher.group(4);
            operations.add(OverrideOperation.createAttr(target, position, name, value));
        }

        return operations;
    }

    private String wrapFullDfrmContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "<div></div>";
        }

        String trimmed = content.trim();

        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            int openCount = trimmed.length() - trimmed.replace("<", "").length();
            int closeCount = trimmed.length() - trimmed.replace(">", "").length();
            if (openCount <= closeCount + 2) {
                return trimmed;
            }
        }

        return "<div class=\"dfrm-root\">" + trimmed + "</div>";
    }

    private void applyOperation(Document doc, OverrideOperation op) {
        if ("node".equals(op.getType())) {
            applyNodeOperation(doc, op);
        } else if ("attr".equals(op.getType())) {
            applyAttrOperation(doc, op);
        }
    }

    private void applyNodeOperation(Document doc, OverrideOperation op) {
        String target = op.getTarget();
        String position = op.getPosition();
        String content = op.getContent();

        Elements targets = doc.select("[name=" + target + "]");
        if (targets.isEmpty()) {
            targets = doc.select("[id=" + target + "]");
        }
        if (targets.isEmpty()) {
            targets = doc.select(target);
        }

        if (targets.isEmpty()) {
            return;
        }

        Element targetElement = targets.first();
        Element parent = targetElement.parent();

        if (parent == null) return;

        switch (position) {
            case "after":
                String afterHtml = wrapContent(content);
                Elements afterChildren = Jsoup.parse(afterHtml, "", org.jsoup.parser.Parser.xmlParser()).body().children();
                if (!afterChildren.isEmpty()) {
                    parent.insertChildren(targetElement.siblingIndex() + 1, afterChildren);
                }
                break;

            case "before":
                String beforeHtml = wrapContent(content);
                Elements beforeChildren = Jsoup.parse(beforeHtml, "", org.jsoup.parser.Parser.xmlParser()).body().children();
                if (!beforeChildren.isEmpty()) {
                    parent.insertChildren(targetElement.siblingIndex(), beforeChildren);
                }
                break;

            case "replace":
                String replaceHtml = wrapContent(content);
                Element newElement = Jsoup.parse(replaceHtml, "", org.jsoup.parser.Parser.xmlParser()).body().children().first();
                if (newElement != null) {
                    targetElement.replaceWith(newElement);
                } else {
                    targetElement.remove();
                }
                break;

            case "delete":
                // Не удаляем
                break;
        }
    }

    private void applyAttrOperation(Document doc, OverrideOperation op) {
        String target = op.getTarget();
        String position = op.getPosition();
        String attrName = op.getName();
        String attrValue = op.getValue();

        Elements targets = doc.select("[name=" + target + "]");
        if (targets.isEmpty()) {
            targets = doc.select("[id=" + target + "]");
        }
        if (targets.isEmpty()) {
            targets = doc.select(target);
        }

        if (targets.isEmpty()) {
            return;
        }

        Element targetElement = targets.first();

        switch (position) {
            case "add":
            case "replace":
                targetElement.attr(attrName, attrValue);
                break;
            case "delete":
                targetElement.removeAttr(attrName);
                break;
            case "append":
                String currentValue = targetElement.attr(attrName);
                if (currentValue != null && !currentValue.isEmpty()) {
                    targetElement.attr(attrName, currentValue + attrValue);
                } else {
                    targetElement.attr(attrName, attrValue);
                }
                break;
        }
    }

    private String wrapContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "<div></div>";
        }

        String trimmed = content.trim();

        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            int firstClose = trimmed.indexOf('>');
            int lastOpen = trimmed.lastIndexOf('<');

            if (firstClose > 0 && lastOpen > firstClose) {
                String firstTag = trimmed.substring(1, firstClose).split("\\s")[0];
                String lastTag = trimmed.substring(lastOpen + 1, trimmed.length() - 1);
                if (firstTag.equals(lastTag)) {
                    return trimmed;
                }
            }

            if (trimmed.indexOf('<', 1) < trimmed.lastIndexOf('<')) {
                return "<root>" + trimmed + "</root>";
            }
            return trimmed;
        }

        return "<div>" + trimmed + "</div>";
    }

    private static class OverrideOperation {
        private String type;
        private String target;
        private String position;
        private String name;
        private String value;
        private String content;

        public static OverrideOperation createNode(String target, String position, String content) {
            OverrideOperation op = new OverrideOperation();
            op.type = "node";
            op.target = target;
            op.position = position;
            op.content = content;
            return op;
        }

        public static OverrideOperation createAttr(String target, String position, String name, String value) {
            OverrideOperation op = new OverrideOperation();
            op.type = "attr";
            op.target = target;
            op.position = position;
            op.name = name;
            op.value = value;
            return op;
        }

        public String getType() { return type; }
        public String getTarget() { return target; }
        public String getPosition() { return position; }
        public String getName() { return name; }
        public String getValue() { return value; }
        public String getContent() { return content; }

        @Override
        public String toString() {
            if ("node".equals(type)) {
                return String.format("node target='%s' pos='%s'", target, position);
            } else {
                return String.format("attr target='%s' pos='%s' name='%s' value='%s'",
                        target, position, name, value);
            }
        }
    }
}