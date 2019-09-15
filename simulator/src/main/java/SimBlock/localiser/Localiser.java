package SimBlock.localiser;

import SimBlock.node.Node;

public class Localiser {

    private static final int w = 10;
    private static final double T = 1;
    public static final int numOfTrials = 500;

    private Node j;
    private Node k;

    private Long dj;
    private Long dk;
    private Long di;
    private Long cjk;
    private Long cij;

    public Localiser(long di, Node j, Node k) {
        this.di = di;
        this.j = j;
        this.k = k;
    }


    public void setdj(long degree, long cost) {
        this.dj = degree;
        this.cjk = cost;
    }

    public void setdk(long dk) {
        this.dk = dk;
    }

    public void setcij(long cost) {
        this.cij = cost;
    }

    public Node getJ() {
        return j;
    }

    public Node getK() {
        return k;
    }

    private long calcE() {
        long e = 2 * w * (dk - di + 1) + cjk + cij;
        return e;
    }

    public double calcP() {
        long e = calcE();
        double value =  Math.pow(Math.E, -(e/T)) * (di * (di-1))/(dk * (dk+1));
        return Math.min(value, 1);
    }

    public boolean calculatable() {
        if(dj == null || dk == null || di == null || cjk == null || cij == null) {
            return false;
        }
        return true;
    }
}