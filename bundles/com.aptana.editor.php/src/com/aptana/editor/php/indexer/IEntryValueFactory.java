package com.aptana.editor.php.indexer;

import java.io.DataInputStream;
import java.io.IOException;

public interface IEntryValueFactory {

	IReportable createValue(DataInputStream stream) throws IOException;
}
