package lib.netmania.data.dtos;

/**
 * Created by hansangcheol on 2017. 4. 12..
 */

public class Scheme extends BaseDto {

    public String name;
    public Class<?> dataFormat;
    public boolean nullable;
    public String default_value;

    public Scheme () {
        //nothing yet;
    }

    public Scheme (String name, Class<?> dataFormat, boolean nullable, String default_value) {
        this.name = name;
        this.dataFormat = dataFormat;
        this.nullable = nullable;
        this.default_value = default_value;
    }

}
