package ma.enset.dao;

import java.util.List;

public interface Dao <T,U> {
    List<T> findAll();
    T findById(U id);
    void save(T o);
    void deleteById(U id);
    void update(T o);
}
