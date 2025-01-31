package ma.enset.app;

record SubjectItem(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}