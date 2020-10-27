package classes;

import interfaces.Id;

public class FileManagerId implements Id {
    int id;

    public FileManagerId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
