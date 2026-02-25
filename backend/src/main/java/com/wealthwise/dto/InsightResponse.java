package com.wealthwise.dto;

import java.util.List;

public class InsightResponse {

    private List<Insight> insights;

    public InsightResponse() {}

    public InsightResponse(List<Insight> insights) {
        this.insights = insights;
    }

    public List<Insight> getInsights() { return insights; }
    public void setInsights(List<Insight> v) { insights = v; }

    // ---------------------------------------------------------------
    // Each individual insight card
    // ---------------------------------------------------------------
    public static class Insight {

        /** POSITIVE | WARNING | DANGER | INFO */
        private String type;
        private String title;
        private String message;
        /** Optional numeric delta, e.g. 20.5 for "20.5% increase". null if not applicable. */
        private Double value;

        public Insight() {}

        public Insight(String type, String title, String message, Double value) {
            this.type    = type;
            this.title   = title;
            this.message = message;
            this.value   = value;
        }

        public String  getType()    { return type; }
        public String  getTitle()   { return title; }
        public String  getMessage() { return message; }
        public Double  getValue()   { return value; }

        public void setType(String v)    { type    = v; }
        public void setTitle(String v)   { title   = v; }
        public void setMessage(String v) { message = v; }
        public void setValue(Double v)   { value   = v; }
    }
}
