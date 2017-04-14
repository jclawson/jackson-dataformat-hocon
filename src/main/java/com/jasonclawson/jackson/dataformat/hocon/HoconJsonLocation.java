package com.jasonclawson.jackson.dataformat.hocon;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.impl.ConfigImplUtil;

public class HoconJsonLocation extends JsonLocation implements ConfigOrigin {
    private static final long serialVersionUID = 1L;
    
    private final ConfigOrigin origin;
    
    public HoconJsonLocation(final ConfigOrigin origin) {
        super(origin.description(), -1L, origin.lineNumber(), -1);
        this.origin = origin;
    }

    @Override
    public String description() {
        return origin.description();
    }

    @Override
    public String filename() {
        return origin.filename();
    }

    @Override
    public URL url() {
        return origin.url();
    }

    @Override
    public String resource() {
        return origin.resource();
    }

    @Override
    public int lineNumber() {
        return origin.lineNumber();
    }

    @Override
    public List<String> comments() {
        return origin.comments();
    }

    @Override
    public ConfigOrigin withComments(List<String> comments) {
        return origin.withComments(comments);
    }

    @Override
    public ConfigOrigin withLineNumber(int i) {
        return origin.withLineNumber(i);
    }
}
