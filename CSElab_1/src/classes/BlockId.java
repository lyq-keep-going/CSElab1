package classes;

import interfaces.Id;

public class BlockId implements Id {
    int id;

    public int getId() {
        return id;
    }

    BlockId(int id){
        this.id = id;
    }
}
