package p1;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Gheorghe on 3/13/2017.
 */
public class MyPair {

    @JsonProperty(value = "key")
    private String key;
    @JsonProperty(value = "value")
    private Integer value;

    public MyPair(){

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return this.getKey() + " : " + this.getValue();
    }

}
