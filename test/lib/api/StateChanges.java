package lib.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lib.api.deser.DataEntry;
import lib.api.deser.ScriptTransfer;
import lib.api.deser.StateChangesDeser;

import java.util.List;

@JsonDeserialize(using = StateChangesDeser.class)
public class StateChanges implements ITransaction {

    public List<DataEntry> data;
    public List<ScriptTransfer> transfers;

}
