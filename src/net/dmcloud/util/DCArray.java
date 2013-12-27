package net.dmcloud.util;

import java.util.ArrayList;

public class DCArray extends ArrayList {

    static public DCArray create() {
        return new DCArray();
    }

    static public DCArray create(ArrayList array) {
        DCArray obj = create();
        obj.addAll(array);
        return obj;
    }

    public DCArray push(Object s) {
        this.add(s);
        return this;
    }
}
