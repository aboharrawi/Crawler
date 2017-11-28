public class XMLFormat {

    private String base;
    private String domain;

    private int depth;
    private int threads;


    public XMLFormat() {

    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public String toString() {
        return String.format("%s %s %d %d", domain, base, depth, threads);
    }
}
