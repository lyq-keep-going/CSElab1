package classes;

import interfaces.Id;

public class BlockManagerId implements Id {
    int id;

    public int getId() {
        return id;
    }

    public BlockManagerId(int id){
        this.id = id;
    }
}
