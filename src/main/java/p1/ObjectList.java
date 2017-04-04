package p1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class ObjectList {

    @JsonProperty(value = "list")
    private List<DirectIndex> list;

    public ObjectList(){

    }

    public List<DirectIndex> getList() {
        return list;
    }

    public void setList(List<DirectIndex> list) {
        this.list = list;
    }

    public void add(DirectIndex directIndex){
        this.list.add(directIndex);
    }
}
