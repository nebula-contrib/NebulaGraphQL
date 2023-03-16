package com.nebulagraphql.rsboot.domain;

import java.util.List;

public class Path {
    private final List<Segment> segments;

    public Path(List<Segment> segments) {
        this.segments = segments;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return "Path{" +
                "segments=" + segments +
                '}';
    }
}
