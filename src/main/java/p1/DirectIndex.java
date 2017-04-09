package p1;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Gheorghe on 4/2/2017.
 */

//obiect de tip direct index, contine calea catre fisieul
// curent si numarul de aparitii ale cuvantului in documentul curent
public class DirectIndex {

    @JsonProperty(value = "word")
    private String key;

    @JsonProperty(value = "pair")
    private MyPair value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MyPair getValue() {
        return value;
    }

    public void setValue(MyPair value) {
        this.value = value;
    }


}
