package lib.netmania.data.callback;

import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2017. 4. 12..
 */

public interface DataControlListener {
    void onDataFormatAdd (DataFormatDto dataFormat);
    void onDataFormatRemove (DataFormatDto dataFormat);
}
