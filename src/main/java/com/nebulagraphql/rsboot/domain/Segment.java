package com.nebulagraphql.rsboot.domain;

public class Segment {
    private final Vertex src;
    private final Vertex dst;
    private final Edge edge;

    public Segment(Vertex src, Vertex dst, Edge edge) {
        this.src = src;
        this.dst = dst;
        this.edge = edge;
    }

    public Vertex getSrc() {
        return src;
    }

    public Vertex getDst() {
        return dst;
    }

    public Edge getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "src=" + src +
                ", dst=" + dst +
                ", edge=" + edge +
                '}';
    }
}
