package com.aptana.editor.php.internal.parser.phpdoc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org2.eclipse.php.util.StringUtils;

/**
 * @author Robin Debreuil
 */
public abstract class DocumentationBase // $codepro.audit.disable noAbstractMethods
{
	private static final String[] NO_STRINGS = new String[0];
	private String fAuthor = ""; //$NON-NLS-1$
	private String fName = ""; //$NON-NLS-1$
	private String fDescription = ""; //$NON-NLS-1$
	private String fVersion = ""; //$NON-NLS-1$
	private ArrayList<String> fSees;
	private ArrayList<String> fSDocLocations;
	private int fType = IDocumentation.TYPE_FUNCTION; // default type
	// private HashMap<String, ArrayList<CodeLocation>> fID;
	//
	// private transient ArrayList<ErrorMessage> fErrors;
	private ArrayList<String> fExamples;
	private String fRemarks = ""; //$NON-NLS-1$
	private String userAgent = ""; //$NON-NLS-1$

	/**
	 * @see com.aptana.metadata.IDocumentation#getName()
	 */
	public String getName()
	{
		return fName;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setName(java.lang.String)
	 */
	public void setName(String value)
	{
		fName = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getDescription()
	 */
	public String getDescription()
	{
		return fDescription;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setDescription(java.lang.String)
	 */
	public void setDescription(String value)
	{
		fDescription = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getRemarks()
	 */
	public String getRemarks()
	{
		return fRemarks;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setRemarks(java.lang.String)
	 */
	public void setRemarks(String value)
	{
		fRemarks = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getAuthor()
	 */
	public String getAuthor()
	{
		return fAuthor;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setAuthor(java.lang.String)
	 */
	public void setAuthor(String value)
	{
		fAuthor = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getVersion()
	 */
	public String getVersion()
	{
		return fVersion;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setVersion(java.lang.String)
	 */
	public void setVersion(String value)
	{
		fVersion = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getSees()
	 */
	public String[] getSees()
	{
		if (fSees == null)
		{
			return NO_STRINGS;
		}
		return fSees.toArray(NO_STRINGS);
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#addSee(java.lang.String)
	 */
	public void addSee(String value)
	{
		value = (value == null) ? "" : value; //$NON-NLS-1$

		if (fSees == null)
		{
			fSees = new ArrayList<String>();
		}

		fSees.add(value);
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getDocumentType()
	 */
	public int getDocumentType()
	{
		return fType;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setDocumentType(int)
	 */
	public void setDocumentType(int type)
	{
		fType = type;
	}

	/**
	 * @return Returns the userAgent.
	 */
	public String getUserAgent()
	{
		return userAgent;
	}

	/**
	 * @param userAgent
	 *            The userAgent to set.
	 */
	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	/**
	 * Read in a binary representation of this object
	 * 
	 * @param input
	 * @throws IOException
	 */
	public void read(DataInput input) throws IOException
	{
		this.fAuthor = input.readUTF();
		this.fName = input.readUTF();
		this.fDescription = input.readUTF();
		this.addExample(input.readUTF());
		this.fRemarks = input.readUTF();
		this.fVersion = input.readUTF();

		int size = input.readInt();

		if (size > 0)
		{
			this.fSees = new ArrayList<String>();

			for (int i = 0; i < size; i++)
			{
				this.fSees.add(input.readUTF());
			}
		}
	}

	/**
	 * Write out a binary representation of this object
	 * 
	 * @param output
	 * @throws IOException
	 */
	public void write(DataOutput output) throws IOException
	{
		output.writeUTF(this.fAuthor);
		output.writeUTF(this.fName);
		output.writeUTF(this.fDescription);
		output.writeUTF(StringUtils.join("\n\n", getExamples())); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
		output.writeUTF(this.fRemarks);
		output.writeUTF(this.fVersion);

		if (this.fSees != null)
		{
			output.writeInt(this.fSees.size());

			for (int i = 0; i < this.fSees.size(); i++)
			{
				output.writeUTF(this.fSees.get(i));
			}
		}
		else
		{
			output.writeInt(0);
		}
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#addSDocLocation(java.lang.String)
	 */
	public void addSDocLocation(String value)
	{
		value = (value == null) ? "" : value; //$NON-NLS-1$

		if (fSDocLocations == null)
		{
			fSDocLocations = new ArrayList<String>();
		}

		fSDocLocations.add(value);
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getSDocLocations()
	 */
	public String[] getSDocLocations()
	{
		if (fSDocLocations == null)
		{
			return NO_STRINGS;
		}
		return fSDocLocations.toArray(NO_STRINGS);
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#getExamples()
	 */
	public String[] getExamples()
	{
		if (fExamples != null)
		{
			return fExamples.toArray(NO_STRINGS);
		}
		else
		{
			return NO_STRINGS;
		}
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setExample(java.lang.String)
	 */
	public void addExample(String value)
	{
		if (value == null)
			return;

		if (fExamples == null)
		{
			fExamples = new ArrayList<String>();
		}

		fExamples.add(value);
	}
}
