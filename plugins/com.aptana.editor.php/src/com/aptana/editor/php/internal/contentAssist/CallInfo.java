package com.aptana.editor.php.internal.contentAssist;

/**
 * Call info which holds the caller name and end offset. The call can be a function call or a class instansiation.
 * 
 * @author Denis Denisenko, Shalom G.
 */
class CallInfo
{
	/**
	 * Name of the function/class called.
	 */
	private String name;

	/**
	 * The function/class name end offset.
	 */
	private int nameEndOffset;

	/**
	 * CallInfo constructor.
	 * 
	 * @param name
	 *            - name of the function called.
	 * @param nameEndPos
	 *            - the function name end position.
	 */
	protected CallInfo(String name, int nameEndPos)
	{
		this.name = name;
		this.nameEndOffset = nameEndPos;
	}

	/**
	 * Returns the name of the caller.
	 * 
	 * @return name of the function/class call
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the end offset for the name of the call.
	 * 
	 * @return the function/class name end-position.
	 */
	public int getNameEndPos()
	{
		return nameEndOffset;
	}
}
