package com.aptana.editor.php.indexer;

import java.io.DataOutputStream;
import java.io.IOException;

public interface IReportable {

	int getKind();
		
	public void store(DataOutputStream da) throws IOException;
}
