package lib.api;

import lib.api.deser.DataEntry;
import lib.api.deser.ScriptTransfer;

import java.util.List;

public class StateChanges {

    //TODO deser to HashMap or custom object with filter methods
    public List<DataEntry> data;
    public List<ScriptTransfer> transfers;

}
