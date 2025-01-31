package ma.enset.entities;

import java.io.Serializable;

public class Student implements Serializable {
    private int id;
    private String name;
    private byte[] embeddings;

    public Student(String name, byte[] embeddings) {
        this.name = name;
        this.embeddings = embeddings;
    }

    public Student(String name) {
        this.name = name;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() { return name; }

    public byte[] getEmbeddings() { return embeddings; }
    public void setEmbeddings(byte[] embeddings) { this.embeddings = embeddings; }

    public String toString() {
        return String.format("Student{id=%d, name='%s', embeddings=%d bytes}",
                id,
                name,
                embeddings != null ? embeddings.length : 0);
    }
}