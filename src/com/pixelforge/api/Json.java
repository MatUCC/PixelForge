package com.pixelforge.api;

import com.pixelforge.core.LayerInfo;
import com.pixelforge.core.TreatmentType;
import com.pixelforge.orders.Order;

import java.util.List;

/**
 * Tiny hand-written JSON serializer, so the project needs no external libraries.
 * Only covers the objects this API returns.
 */
public final class Json {

    private Json() {
    }

    public static String treatments() {
        StringBuilder json = new StringBuilder("[");
        for (TreatmentType type : TreatmentType.values()) {
            if (!type.isSelectable()) {
                continue;
            }
            if (json.length() > 1) {
                json.append(',');
            }
            json.append('{')
                .append("\"id\":").append(quote(type.name())).append(',')
                .append("\"name\":").append(quote(type.getDisplayName())).append(',')
                .append("\"cost\":").append(type.getCost()).append(',')
                .append("\"seconds\":").append(type.getSeconds()).append(',')
                .append("\"parameterHint\":").append(type.getParameterHint() == null ? "null" : quote(type.getParameterHint()))
                .append('}');
        }
        return json.append(']').toString();
    }

    public static String order(Order order) {
        return "{"
                + "\"id\":" + order.getId() + ','
                + "\"createdAt\":" + quote(order.getCreatedAt().toString()) + ','
                + "\"description\":" + quote(order.getDescription()) + ','
                + "\"totalCost\":" + order.getTotalCost() + ','
                + "\"totalSeconds\":" + order.getTotalSeconds() + ','
                + "\"layers\":" + layers(order.getLayers())
                + "}";
    }

    public static String orders(List<Order> orders) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(order(orders.get(i)));
        }
        return json.append(']').toString();
    }

    public static String processResult(Order order, String format, String imageBase64) {
        return "{"
                + "\"order\":" + order(order) + ','
                + "\"imageFormat\":" + quote(format) + ','
                + "\"imageBase64\":" + quote(imageBase64)
                + "}";
    }

    public static String error(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    private static String layers(List<LayerInfo> layers) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < layers.size(); i++) {
            LayerInfo layer = layers.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append('{')
                .append("\"name\":").append(quote(layer.getName())).append(',')
                .append("\"cost\":").append(layer.getCost()).append(',')
                .append("\"seconds\":").append(layer.getSeconds())
                .append('}');
        }
        return json.append(']').toString();
    }

    private static String quote(String text) {
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':  escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.append('"').toString();
    }
}
