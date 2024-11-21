package com.example.dishdash.DetailedRecipeClass;

import com.example.dishdash.RandomRecipeClass.Metric;
import com.example.dishdash.RandomRecipeClass.Us;

public class Measures {
    public Us us;
    public Metric metric;

    public Measures(Us us, Metric metric) {
        this.us = us;
        this.metric = metric;
    }

    public Measures() {
    }

    public Us getUs() {
        return us;
    }

    public void setUs(Us us) {
        this.us = us;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }
}
