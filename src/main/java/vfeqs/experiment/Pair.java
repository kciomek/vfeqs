package vfeqs.experiment;

public class Pair<FIRST, SECOND> {
    private final FIRST first;
    private final SECOND second;

    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }

        Pair<?, ?> p = (Pair<?, ?>) o;

        return (first == null ? p.first == null : first.equals(p.first)) && (second == null ? p.second == null : second.equals(p.second));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (first == null ? 0 : first.hashCode());
        result = prime * result + (second == null ? 0 : second.hashCode());
        return result;
    }

//    public static <A, B> Pair<A, B> of(A a, B b) {
//        return new Pair<A, B>(a, b);
//    }
}
