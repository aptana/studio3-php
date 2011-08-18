package com.aptana.editor.php.internal.indexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.aptana.editor.php.indexer.IPHPIndexConstants;

public class NamespacePHPEntryValue extends AbstractPHPEntryValue
{

	public NamespacePHPEntryValue(DataInputStream di) throws IOException
	{
		super(di);
	}

	public NamespacePHPEntryValue(int modifiers, String namespace)
	{
		super(modifiers, namespace);

	}

	@Override
	public int getKind()
	{
		return IPHPIndexConstants.NAMESPACE_CATEGORY;
	}

	@Override
	protected void internalRead(DataInputStream di)
	{

	}

	@Override
	protected void internalWrite(DataOutputStream da)
	{

	}

}
